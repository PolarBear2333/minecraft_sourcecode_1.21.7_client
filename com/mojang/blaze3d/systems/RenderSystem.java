/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.IntConsumer
 *  javax.annotation.Nullable
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fStack
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.lwjgl.glfw.GLFW
 *  org.lwjgl.glfw.GLFWErrorCallbackI
 *  org.lwjgl.system.MemoryUtil
 *  org.slf4j.Logger
 */
package com.mojang.blaze3d.systems;

import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.TracyFrameCapture;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.GpuFence;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.ScissorState;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiFunction;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DynamicUniforms;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ArrayListDeque;
import net.minecraft.util.Mth;
import net.minecraft.util.TimeSource;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@DontObfuscate
public class RenderSystem {
    static final Logger LOGGER = LogUtils.getLogger();
    public static final int MINIMUM_ATLAS_TEXTURE_SIZE = 1024;
    public static final int PROJECTION_MATRIX_UBO_SIZE = new Std140SizeCalculator().putMat4f().get();
    @Nullable
    private static Thread renderThread;
    @Nullable
    private static GpuDevice DEVICE;
    private static double lastDrawTime;
    private static final AutoStorageIndexBuffer sharedSequential;
    private static final AutoStorageIndexBuffer sharedSequentialQuad;
    private static final AutoStorageIndexBuffer sharedSequentialLines;
    private static ProjectionType projectionType;
    private static ProjectionType savedProjectionType;
    private static final Matrix4fStack modelViewStack;
    private static Matrix4f textureMatrix;
    public static final int TEXTURE_COUNT = 12;
    private static final GpuTextureView[] shaderTextures;
    @Nullable
    private static GpuBufferSlice shaderFog;
    @Nullable
    private static GpuBufferSlice shaderLightDirections;
    @Nullable
    private static GpuBufferSlice projectionMatrixBuffer;
    @Nullable
    private static GpuBufferSlice savedProjectionMatrixBuffer;
    private static final Vector3f modelOffset;
    private static float shaderLineWidth;
    private static String apiDescription;
    private static final AtomicLong pollEventsWaitStart;
    private static final AtomicBoolean pollingEvents;
    @Nullable
    private static GpuBuffer QUAD_VERTEX_BUFFER;
    private static final ArrayListDeque<GpuAsyncTask> PENDING_FENCES;
    @Nullable
    public static GpuTextureView outputColorTextureOverride;
    @Nullable
    public static GpuTextureView outputDepthTextureOverride;
    @Nullable
    private static GpuBuffer globalSettingsUniform;
    @Nullable
    private static DynamicUniforms dynamicUniforms;
    private static ScissorState scissorStateForRenderTypeDraws;

    public static void initRenderThread() {
        if (renderThread != null) {
            throw new IllegalStateException("Could not initialize render thread");
        }
        renderThread = Thread.currentThread();
    }

    public static boolean isOnRenderThread() {
        return Thread.currentThread() == renderThread;
    }

    public static void assertOnRenderThread() {
        if (!RenderSystem.isOnRenderThread()) {
            throw RenderSystem.constructThreadException();
        }
    }

    private static IllegalStateException constructThreadException() {
        return new IllegalStateException("Rendersystem called from wrong thread");
    }

    private static void pollEvents() {
        pollEventsWaitStart.set(Util.getMillis());
        pollingEvents.set(true);
        GLFW.glfwPollEvents();
        pollingEvents.set(false);
    }

    public static boolean isFrozenAtPollEvents() {
        return pollingEvents.get() && Util.getMillis() - pollEventsWaitStart.get() > 200L;
    }

    public static void flipFrame(long l, @Nullable TracyFrameCapture tracyFrameCapture) {
        RenderSystem.pollEvents();
        Tesselator.getInstance().clear();
        GLFW.glfwSwapBuffers((long)l);
        if (tracyFrameCapture != null) {
            tracyFrameCapture.endFrame();
        }
        dynamicUniforms.reset();
        Minecraft.getInstance().levelRenderer.endFrame();
        RenderSystem.pollEvents();
    }

    public static void limitDisplayFPS(int n) {
        double d = lastDrawTime + 1.0 / (double)n;
        double d2 = GLFW.glfwGetTime();
        while (d2 < d) {
            GLFW.glfwWaitEventsTimeout((double)(d - d2));
            d2 = GLFW.glfwGetTime();
        }
        lastDrawTime = d2;
    }

    public static void setShaderFog(GpuBufferSlice gpuBufferSlice) {
        shaderFog = gpuBufferSlice;
    }

    @Nullable
    public static GpuBufferSlice getShaderFog() {
        return shaderFog;
    }

    public static void setShaderLights(GpuBufferSlice gpuBufferSlice) {
        shaderLightDirections = gpuBufferSlice;
    }

    @Nullable
    public static GpuBufferSlice getShaderLights() {
        return shaderLightDirections;
    }

    public static void lineWidth(float f) {
        RenderSystem.assertOnRenderThread();
        shaderLineWidth = f;
    }

    public static float getShaderLineWidth() {
        RenderSystem.assertOnRenderThread();
        return shaderLineWidth;
    }

    public static void enableScissorForRenderTypeDraws(int n, int n2, int n3, int n4) {
        scissorStateForRenderTypeDraws.enable(n, n2, n3, n4);
    }

    public static void disableScissorForRenderTypeDraws() {
        scissorStateForRenderTypeDraws.disable();
    }

    public static ScissorState getScissorStateForRenderTypeDraws() {
        return scissorStateForRenderTypeDraws;
    }

    public static String getBackendDescription() {
        return String.format(Locale.ROOT, "LWJGL version %s", GLX._getLWJGLVersion());
    }

    public static String getApiDescription() {
        return apiDescription;
    }

    public static TimeSource.NanoTimeSource initBackendSystem() {
        return GLX._initGlfw()::getAsLong;
    }

    public static void initRenderer(long l, int n, boolean bl, BiFunction<ResourceLocation, ShaderType, String> biFunction, boolean bl2) {
        DEVICE = new GlDevice(l, n, bl, biFunction, bl2);
        apiDescription = RenderSystem.getDevice().getImplementationInformation();
        dynamicUniforms = new DynamicUniforms();
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(DefaultVertexFormat.POSITION.getVertexSize() * 4);){
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
            bufferBuilder.addVertex(0.0f, 0.0f, 0.0f);
            bufferBuilder.addVertex(1.0f, 0.0f, 0.0f);
            bufferBuilder.addVertex(1.0f, 1.0f, 0.0f);
            bufferBuilder.addVertex(0.0f, 1.0f, 0.0f);
            try (MeshData meshData = bufferBuilder.buildOrThrow();){
                QUAD_VERTEX_BUFFER = RenderSystem.getDevice().createBuffer(() -> "Quad", 32, meshData.vertexBuffer());
            }
        }
    }

    public static void setErrorCallback(GLFWErrorCallbackI gLFWErrorCallbackI) {
        GLX._setGlfwErrorCallback(gLFWErrorCallbackI);
    }

    public static void setupDefaultState() {
        modelViewStack.clear();
        textureMatrix.identity();
    }

    public static void setupOverlayColor(@Nullable GpuTextureView gpuTextureView) {
        RenderSystem.assertOnRenderThread();
        RenderSystem.setShaderTexture(1, gpuTextureView);
    }

    public static void teardownOverlayColor() {
        RenderSystem.assertOnRenderThread();
        RenderSystem.setShaderTexture(1, null);
    }

    public static void setShaderTexture(int n, @Nullable GpuTextureView gpuTextureView) {
        RenderSystem.assertOnRenderThread();
        if (n >= 0 && n < shaderTextures.length) {
            RenderSystem.shaderTextures[n] = gpuTextureView;
        }
    }

    @Nullable
    public static GpuTextureView getShaderTexture(int n) {
        RenderSystem.assertOnRenderThread();
        if (n >= 0 && n < shaderTextures.length) {
            return shaderTextures[n];
        }
        return null;
    }

    public static void setProjectionMatrix(GpuBufferSlice gpuBufferSlice, ProjectionType projectionType) {
        RenderSystem.assertOnRenderThread();
        projectionMatrixBuffer = gpuBufferSlice;
        RenderSystem.projectionType = projectionType;
    }

    public static void setTextureMatrix(Matrix4f matrix4f) {
        RenderSystem.assertOnRenderThread();
        textureMatrix = new Matrix4f((Matrix4fc)matrix4f);
    }

    public static void resetTextureMatrix() {
        RenderSystem.assertOnRenderThread();
        textureMatrix.identity();
    }

    public static void backupProjectionMatrix() {
        RenderSystem.assertOnRenderThread();
        savedProjectionMatrixBuffer = projectionMatrixBuffer;
        savedProjectionType = projectionType;
    }

    public static void restoreProjectionMatrix() {
        RenderSystem.assertOnRenderThread();
        projectionMatrixBuffer = savedProjectionMatrixBuffer;
        projectionType = savedProjectionType;
    }

    @Nullable
    public static GpuBufferSlice getProjectionMatrixBuffer() {
        RenderSystem.assertOnRenderThread();
        return projectionMatrixBuffer;
    }

    public static Matrix4f getModelViewMatrix() {
        RenderSystem.assertOnRenderThread();
        return modelViewStack;
    }

    public static Matrix4fStack getModelViewStack() {
        RenderSystem.assertOnRenderThread();
        return modelViewStack;
    }

    public static Matrix4f getTextureMatrix() {
        RenderSystem.assertOnRenderThread();
        return textureMatrix;
    }

    public static AutoStorageIndexBuffer getSequentialBuffer(VertexFormat.Mode mode) {
        RenderSystem.assertOnRenderThread();
        return switch (mode) {
            case VertexFormat.Mode.QUADS -> sharedSequentialQuad;
            case VertexFormat.Mode.LINES -> sharedSequentialLines;
            default -> sharedSequential;
        };
    }

    public static void setGlobalSettingsUniform(GpuBuffer gpuBuffer) {
        globalSettingsUniform = gpuBuffer;
    }

    @Nullable
    public static GpuBuffer getGlobalSettingsUniform() {
        return globalSettingsUniform;
    }

    public static ProjectionType getProjectionType() {
        RenderSystem.assertOnRenderThread();
        return projectionType;
    }

    public static GpuBuffer getQuadVertexBuffer() {
        if (QUAD_VERTEX_BUFFER == null) {
            throw new IllegalStateException("Can't getQuadVertexBuffer() before renderer was initialized");
        }
        return QUAD_VERTEX_BUFFER;
    }

    public static void setModelOffset(float f, float f2, float f3) {
        RenderSystem.assertOnRenderThread();
        modelOffset.set(f, f2, f3);
    }

    public static void resetModelOffset() {
        RenderSystem.assertOnRenderThread();
        modelOffset.set(0.0f, 0.0f, 0.0f);
    }

    public static Vector3f getModelOffset() {
        RenderSystem.assertOnRenderThread();
        return modelOffset;
    }

    public static void queueFencedTask(Runnable runnable) {
        PENDING_FENCES.addLast(new GpuAsyncTask(runnable, RenderSystem.getDevice().createCommandEncoder().createFence()));
    }

    public static void executePendingTasks() {
        GpuAsyncTask gpuAsyncTask = PENDING_FENCES.peekFirst();
        while (gpuAsyncTask != null) {
            if (gpuAsyncTask.fence.awaitCompletion(0L)) {
                try {
                    gpuAsyncTask.callback.run();
                }
                finally {
                    gpuAsyncTask.fence.close();
                }
                PENDING_FENCES.removeFirst();
                gpuAsyncTask = PENDING_FENCES.peekFirst();
                continue;
            }
            return;
        }
    }

    public static GpuDevice getDevice() {
        if (DEVICE == null) {
            throw new IllegalStateException("Can't getDevice() before it was initialized");
        }
        return DEVICE;
    }

    @Nullable
    public static GpuDevice tryGetDevice() {
        return DEVICE;
    }

    public static DynamicUniforms getDynamicUniforms() {
        if (dynamicUniforms == null) {
            throw new IllegalStateException("Can't getDynamicUniforms() before device was initialized");
        }
        return dynamicUniforms;
    }

    public static void bindDefaultUniforms(RenderPass renderPass) {
        GpuBufferSlice gpuBufferSlice;
        GpuBuffer gpuBuffer;
        GpuBufferSlice gpuBufferSlice2;
        GpuBufferSlice gpuBufferSlice3 = RenderSystem.getProjectionMatrixBuffer();
        if (gpuBufferSlice3 != null) {
            renderPass.setUniform("Projection", gpuBufferSlice3);
        }
        if ((gpuBufferSlice2 = RenderSystem.getShaderFog()) != null) {
            renderPass.setUniform("Fog", gpuBufferSlice2);
        }
        if ((gpuBuffer = RenderSystem.getGlobalSettingsUniform()) != null) {
            renderPass.setUniform("Globals", gpuBuffer);
        }
        if ((gpuBufferSlice = RenderSystem.getShaderLights()) != null) {
            renderPass.setUniform("Lighting", gpuBufferSlice);
        }
    }

    static {
        lastDrawTime = Double.MIN_VALUE;
        sharedSequential = new AutoStorageIndexBuffer(1, 1, java.util.function.IntConsumer::accept);
        sharedSequentialQuad = new AutoStorageIndexBuffer(4, 6, (intConsumer, n) -> {
            intConsumer.accept(n);
            intConsumer.accept(n + 1);
            intConsumer.accept(n + 2);
            intConsumer.accept(n + 2);
            intConsumer.accept(n + 3);
            intConsumer.accept(n);
        });
        sharedSequentialLines = new AutoStorageIndexBuffer(4, 6, (intConsumer, n) -> {
            intConsumer.accept(n);
            intConsumer.accept(n + 1);
            intConsumer.accept(n + 2);
            intConsumer.accept(n + 3);
            intConsumer.accept(n + 2);
            intConsumer.accept(n + 1);
        });
        projectionType = ProjectionType.PERSPECTIVE;
        savedProjectionType = ProjectionType.PERSPECTIVE;
        modelViewStack = new Matrix4fStack(16);
        textureMatrix = new Matrix4f();
        shaderTextures = new GpuTextureView[12];
        shaderFog = null;
        modelOffset = new Vector3f();
        shaderLineWidth = 1.0f;
        apiDescription = "Unknown";
        pollEventsWaitStart = new AtomicLong();
        pollingEvents = new AtomicBoolean(false);
        PENDING_FENCES = new ArrayListDeque();
        scissorStateForRenderTypeDraws = new ScissorState();
    }

    public static final class AutoStorageIndexBuffer {
        private final int vertexStride;
        private final int indexStride;
        private final IndexGenerator generator;
        @Nullable
        private GpuBuffer buffer;
        private VertexFormat.IndexType type = VertexFormat.IndexType.SHORT;
        private int indexCount;

        AutoStorageIndexBuffer(int n, int n2, IndexGenerator indexGenerator) {
            this.vertexStride = n;
            this.indexStride = n2;
            this.generator = indexGenerator;
        }

        public boolean hasStorage(int n) {
            return n <= this.indexCount;
        }

        public GpuBuffer getBuffer(int n) {
            this.ensureStorage(n);
            return this.buffer;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private void ensureStorage(int n) {
            if (this.hasStorage(n)) {
                return;
            }
            n = Mth.roundToward(n * 2, this.indexStride);
            LOGGER.debug("Growing IndexBuffer: Old limit {}, new limit {}.", (Object)this.indexCount, (Object)n);
            int n2 = n / this.indexStride;
            int n3 = n2 * this.vertexStride;
            VertexFormat.IndexType indexType = VertexFormat.IndexType.least(n3);
            int n4 = Mth.roundToward(n * indexType.bytes, 4);
            ByteBuffer byteBuffer = MemoryUtil.memAlloc((int)n4);
            try {
                this.type = indexType;
                IntConsumer intConsumer = this.intConsumer(byteBuffer);
                for (int i = 0; i < n; i += this.indexStride) {
                    this.generator.accept(intConsumer, i * this.vertexStride / this.indexStride);
                }
                byteBuffer.flip();
                if (this.buffer != null) {
                    this.buffer.close();
                }
                this.buffer = RenderSystem.getDevice().createBuffer(() -> "Auto Storage index buffer", 64, byteBuffer);
            }
            finally {
                MemoryUtil.memFree((Buffer)byteBuffer);
            }
            this.indexCount = n;
        }

        private IntConsumer intConsumer(ByteBuffer byteBuffer) {
            switch (this.type) {
                case SHORT: {
                    return n -> byteBuffer.putShort((short)n);
                }
            }
            return byteBuffer::putInt;
        }

        public VertexFormat.IndexType type() {
            return this.type;
        }

        static interface IndexGenerator {
            public void accept(IntConsumer var1, int var2);
        }
    }

    static final class GpuAsyncTask
    extends Record {
        final Runnable callback;
        final GpuFence fence;

        GpuAsyncTask(Runnable runnable, GpuFence gpuFence) {
            this.callback = runnable;
            this.fence = gpuFence;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{GpuAsyncTask.class, "callback;fence", "callback", "fence"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{GpuAsyncTask.class, "callback;fence", "callback", "fence"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{GpuAsyncTask.class, "callback;fence", "callback", "fence"}, this, object);
        }

        public Runnable callback() {
            return this.callback;
        }

        public GpuFence fence() {
            return this.fence;
        }
    }
}

