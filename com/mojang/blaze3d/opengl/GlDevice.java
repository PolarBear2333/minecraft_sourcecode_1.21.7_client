/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.StringUtils
 *  org.lwjgl.glfw.GLFW
 *  org.lwjgl.opengl.GL
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.opengl.GL31
 *  org.lwjgl.opengl.GLCapabilities
 *  org.slf4j.Logger
 */
package com.mojang.blaze3d.opengl;

import com.mojang.blaze3d.GpuOutOfMemoryException;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.BufferStorage;
import com.mojang.blaze3d.opengl.DirectStateAccess;
import com.mojang.blaze3d.opengl.GlBuffer;
import com.mojang.blaze3d.opengl.GlCommandEncoder;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlDebug;
import com.mojang.blaze3d.opengl.GlDebugLabel;
import com.mojang.blaze3d.opengl.GlProgram;
import com.mojang.blaze3d.opengl.GlRenderPipeline;
import com.mojang.blaze3d.opengl.GlShaderModule;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.opengl.GlTextureView;
import com.mojang.blaze3d.opengl.VertexArrayCache;
import com.mojang.blaze3d.pipeline.CompiledRenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.logging.LogUtils;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.ShaderDefines;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GLCapabilities;
import org.slf4j.Logger;

public class GlDevice
implements GpuDevice {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected static boolean USE_GL_ARB_vertex_attrib_binding = true;
    protected static boolean USE_GL_KHR_debug = true;
    protected static boolean USE_GL_EXT_debug_label = true;
    protected static boolean USE_GL_ARB_debug_output = true;
    protected static boolean USE_GL_ARB_direct_state_access = true;
    protected static boolean USE_GL_ARB_buffer_storage = true;
    private final CommandEncoder encoder;
    @Nullable
    private final GlDebug debugLog;
    private final GlDebugLabel debugLabels;
    private final int maxSupportedTextureSize;
    private final DirectStateAccess directStateAccess;
    private final BiFunction<ResourceLocation, ShaderType, String> defaultShaderSource;
    private final Map<RenderPipeline, GlRenderPipeline> pipelineCache = new IdentityHashMap<RenderPipeline, GlRenderPipeline>();
    private final Map<ShaderCompilationKey, GlShaderModule> shaderCache = new HashMap<ShaderCompilationKey, GlShaderModule>();
    private final VertexArrayCache vertexArrayCache;
    private final BufferStorage bufferStorage;
    private final Set<String> enabledExtensions = new HashSet<String>();
    private final int uniformOffsetAlignment;

    public GlDevice(long l, int n, boolean bl, BiFunction<ResourceLocation, ShaderType, String> biFunction, boolean bl2) {
        GLFW.glfwMakeContextCurrent((long)l);
        GLCapabilities gLCapabilities = GL.createCapabilities();
        int n2 = GlDevice.getMaxSupportedTextureSize();
        GLFW.glfwSetWindowSizeLimits((long)l, (int)-1, (int)-1, (int)n2, (int)n2);
        this.debugLog = GlDebug.enableDebugCallback(n, bl, this.enabledExtensions);
        this.debugLabels = GlDebugLabel.create(gLCapabilities, bl2, this.enabledExtensions);
        this.vertexArrayCache = VertexArrayCache.create(gLCapabilities, this.debugLabels, this.enabledExtensions);
        this.bufferStorage = BufferStorage.create(gLCapabilities, this.enabledExtensions);
        this.directStateAccess = DirectStateAccess.create(gLCapabilities, this.enabledExtensions);
        this.maxSupportedTextureSize = n2;
        this.defaultShaderSource = biFunction;
        this.encoder = new GlCommandEncoder(this);
        this.uniformOffsetAlignment = GL11.glGetInteger((int)35380);
        GL11.glEnable((int)34895);
    }

    public GlDebugLabel debugLabels() {
        return this.debugLabels;
    }

    @Override
    public CommandEncoder createCommandEncoder() {
        return this.encoder;
    }

    @Override
    public GpuTexture createTexture(@Nullable Supplier<String> supplier, int n, TextureFormat textureFormat, int n2, int n3, int n4, int n5) {
        return this.createTexture(this.debugLabels.exists() && supplier != null ? supplier.get() : null, n, textureFormat, n2, n3, n4, n5);
    }

    @Override
    public GpuTexture createTexture(@Nullable String string, int n, TextureFormat textureFormat, int n2, int n3, int n4, int n5) {
        int n6;
        int n7;
        boolean bl;
        if (n5 < 1) {
            throw new IllegalArgumentException("mipLevels must be at least 1");
        }
        if (n4 < 1) {
            throw new IllegalArgumentException("depthOrLayers must be at least 1");
        }
        boolean bl2 = bl = (n & 0x10) != 0;
        if (bl) {
            if (n2 != n3) {
                throw new IllegalArgumentException("Cubemap compatible textures must be square, but size is " + n2 + "x" + n3);
            }
            if (n4 % 6 != 0) {
                throw new IllegalArgumentException("Cubemap compatible textures must have a layer count with a multiple of 6, was " + n4);
            }
            if (n4 > 6) {
                throw new UnsupportedOperationException("Array textures are not yet supported");
            }
        } else if (n4 > 1) {
            throw new UnsupportedOperationException("Array or 3D textures are not yet supported");
        }
        GlStateManager.clearGlErrors();
        int n8 = GlStateManager._genTexture();
        if (string == null) {
            string = String.valueOf(n8);
        }
        if (bl) {
            GL11.glBindTexture((int)34067, (int)n8);
            n7 = 34067;
        } else {
            GlStateManager._bindTexture(n8);
            n7 = 3553;
        }
        GlStateManager._texParameter(n7, 33085, n5 - 1);
        GlStateManager._texParameter(n7, 33082, 0);
        GlStateManager._texParameter(n7, 33083, n5 - 1);
        if (textureFormat.hasDepthAspect()) {
            GlStateManager._texParameter(n7, 34892, 0);
        }
        if (bl) {
            for (int n9 : GlConst.CUBEMAP_TARGETS) {
                for (int i = 0; i < n5; ++i) {
                    GlStateManager._texImage2D(n9, i, GlConst.toGlInternalId(textureFormat), n2 >> i, n3 >> i, 0, GlConst.toGlExternalId(textureFormat), GlConst.toGlType(textureFormat), null);
                }
            }
        } else {
            for (int i = 0; i < n5; ++i) {
                GlStateManager._texImage2D(n7, i, GlConst.toGlInternalId(textureFormat), n2 >> i, n3 >> i, 0, GlConst.toGlExternalId(textureFormat), GlConst.toGlType(textureFormat), null);
            }
        }
        if ((n6 = GlStateManager._getError()) == 1285) {
            throw new GpuOutOfMemoryException("Could not allocate texture of " + n2 + "x" + n3 + " for " + string);
        }
        if (n6 != 0) {
            throw new IllegalStateException("OpenGL error " + n6);
        }
        GlTexture glTexture = new GlTexture(n, string, textureFormat, n2, n3, n4, n5, n8);
        this.debugLabels.applyLabel(glTexture);
        return glTexture;
    }

    @Override
    public GpuTextureView createTextureView(GpuTexture gpuTexture) {
        return this.createTextureView(gpuTexture, 0, gpuTexture.getMipLevels());
    }

    @Override
    public GpuTextureView createTextureView(GpuTexture gpuTexture, int n, int n2) {
        if (gpuTexture.isClosed()) {
            throw new IllegalArgumentException("Can't create texture view with closed texture");
        }
        if (n < 0 || n + n2 > gpuTexture.getMipLevels()) {
            throw new IllegalArgumentException(n2 + " mip levels starting from " + n + " would be out of range for texture with only " + gpuTexture.getMipLevels() + " mip levels");
        }
        return new GlTextureView((GlTexture)gpuTexture, n, n2);
    }

    @Override
    public GpuBuffer createBuffer(@Nullable Supplier<String> supplier, int n, int n2) {
        if (n2 <= 0) {
            throw new IllegalArgumentException("Buffer size must be greater than zero");
        }
        GlStateManager.clearGlErrors();
        GlBuffer glBuffer = this.bufferStorage.createBuffer(this.directStateAccess, supplier, n, n2);
        int n3 = GlStateManager._getError();
        if (n3 == 1285) {
            throw new GpuOutOfMemoryException("Could not allocate buffer of " + n2 + " for " + String.valueOf(supplier));
        }
        if (n3 != 0) {
            throw new IllegalStateException("OpenGL error " + n3);
        }
        this.debugLabels.applyLabel(glBuffer);
        return glBuffer;
    }

    @Override
    public GpuBuffer createBuffer(@Nullable Supplier<String> supplier, int n, ByteBuffer byteBuffer) {
        if (!byteBuffer.hasRemaining()) {
            throw new IllegalArgumentException("Buffer source must not be empty");
        }
        GlStateManager.clearGlErrors();
        long l = byteBuffer.remaining();
        GlBuffer glBuffer = this.bufferStorage.createBuffer(this.directStateAccess, supplier, n, byteBuffer);
        int n2 = GlStateManager._getError();
        if (n2 == 1285) {
            throw new GpuOutOfMemoryException("Could not allocate buffer of " + l + " for " + String.valueOf(supplier));
        }
        if (n2 != 0) {
            throw new IllegalStateException("OpenGL error " + n2);
        }
        this.debugLabels.applyLabel(glBuffer);
        return glBuffer;
    }

    @Override
    public String getImplementationInformation() {
        if (GLFW.glfwGetCurrentContext() == 0L) {
            return "NO CONTEXT";
        }
        return GlStateManager._getString(7937) + " GL version " + GlStateManager._getString(7938) + ", " + GlStateManager._getString(7936);
    }

    @Override
    public List<String> getLastDebugMessages() {
        return this.debugLog == null ? Collections.emptyList() : this.debugLog.getLastOpenGlDebugMessages();
    }

    @Override
    public boolean isDebuggingEnabled() {
        return this.debugLog != null;
    }

    @Override
    public String getRenderer() {
        return GlStateManager._getString(7937);
    }

    @Override
    public String getVendor() {
        return GlStateManager._getString(7936);
    }

    @Override
    public String getBackendName() {
        return "OpenGL";
    }

    @Override
    public String getVersion() {
        return GlStateManager._getString(7938);
    }

    private static int getMaxSupportedTextureSize() {
        int n;
        int n2 = GlStateManager._getInteger(3379);
        for (n = Math.max(32768, n2); n >= 1024; n >>= 1) {
            GlStateManager._texImage2D(32868, 0, 6408, n, n, 0, 6408, 5121, null);
            int n3 = GlStateManager._getTexLevelParameter(32868, 0, 4096);
            if (n3 == 0) continue;
            return n;
        }
        n = Math.max(n2, 1024);
        LOGGER.info("Failed to determine maximum texture size by probing, trying GL_MAX_TEXTURE_SIZE = {}", (Object)n);
        return n;
    }

    @Override
    public int getMaxTextureSize() {
        return this.maxSupportedTextureSize;
    }

    @Override
    public int getUniformOffsetAlignment() {
        return this.uniformOffsetAlignment;
    }

    @Override
    public void clearPipelineCache() {
        for (GlRenderPipeline object : this.pipelineCache.values()) {
            if (object.program() == GlProgram.INVALID_PROGRAM) continue;
            object.program().close();
        }
        this.pipelineCache.clear();
        for (GlShaderModule glShaderModule : this.shaderCache.values()) {
            if (glShaderModule == GlShaderModule.INVALID_SHADER) continue;
            glShaderModule.close();
        }
        this.shaderCache.clear();
        String string = GlStateManager._getString(7937);
        if (string.contains("AMD")) {
            GlDevice.amdDummyShaderWorkaround();
        }
    }

    private static void amdDummyShaderWorkaround() {
        int n = GlStateManager.glCreateShader(35633);
        GlStateManager.glShaderSource(n, "#version 150\nvoid main() {\n    gl_Position = vec4(0.0);\n}\n");
        GlStateManager.glCompileShader(n);
        int n2 = GlStateManager.glCreateShader(35632);
        GlStateManager.glShaderSource(n2, "#version 150\nlayout(std140) uniform Dummy {\n    float Value;\n};\nout vec4 fragColor;\nvoid main() {\n    fragColor = vec4(0.0);\n}\n");
        GlStateManager.glCompileShader(n2);
        int n3 = GlStateManager.glCreateProgram();
        GlStateManager.glAttachShader(n3, n);
        GlStateManager.glAttachShader(n3, n2);
        GlStateManager.glLinkProgram(n3);
        GL31.glGetUniformBlockIndex((int)n3, (CharSequence)"Dummy");
        GlStateManager.glDeleteShader(n);
        GlStateManager.glDeleteShader(n2);
        GlStateManager.glDeleteProgram(n3);
    }

    @Override
    public List<String> getEnabledExtensions() {
        return new ArrayList<String>(this.enabledExtensions);
    }

    @Override
    public void close() {
        this.clearPipelineCache();
    }

    public DirectStateAccess directStateAccess() {
        return this.directStateAccess;
    }

    protected GlRenderPipeline getOrCompilePipeline(RenderPipeline renderPipeline) {
        return this.pipelineCache.computeIfAbsent(renderPipeline, renderPipeline2 -> this.compilePipeline(renderPipeline, this.defaultShaderSource));
    }

    protected GlShaderModule getOrCompileShader(ResourceLocation resourceLocation, ShaderType shaderType, ShaderDefines shaderDefines, BiFunction<ResourceLocation, ShaderType, String> biFunction) {
        ShaderCompilationKey shaderCompilationKey = new ShaderCompilationKey(resourceLocation, shaderType, shaderDefines);
        return this.shaderCache.computeIfAbsent(shaderCompilationKey, shaderCompilationKey2 -> this.compileShader(shaderCompilationKey, biFunction));
    }

    @Override
    public GlRenderPipeline precompilePipeline(RenderPipeline renderPipeline, @Nullable BiFunction<ResourceLocation, ShaderType, String> biFunction) {
        BiFunction<ResourceLocation, ShaderType, String> biFunction2 = biFunction == null ? this.defaultShaderSource : biFunction;
        return this.pipelineCache.computeIfAbsent(renderPipeline, renderPipeline2 -> this.compilePipeline(renderPipeline, biFunction2));
    }

    private GlShaderModule compileShader(ShaderCompilationKey shaderCompilationKey, BiFunction<ResourceLocation, ShaderType, String> biFunction) {
        String string = biFunction.apply(shaderCompilationKey.id, shaderCompilationKey.type);
        if (string == null) {
            LOGGER.error("Couldn't find source for {} shader ({})", (Object)shaderCompilationKey.type, (Object)shaderCompilationKey.id);
            return GlShaderModule.INVALID_SHADER;
        }
        String string2 = GlslPreprocessor.injectDefines(string, shaderCompilationKey.defines);
        int n = GlStateManager.glCreateShader(GlConst.toGl(shaderCompilationKey.type));
        GlStateManager.glShaderSource(n, string2);
        GlStateManager.glCompileShader(n);
        if (GlStateManager.glGetShaderi(n, 35713) == 0) {
            String string3 = StringUtils.trim((String)GlStateManager.glGetShaderInfoLog(n, 32768));
            LOGGER.error("Couldn't compile {} shader ({}): {}", new Object[]{shaderCompilationKey.type.getName(), shaderCompilationKey.id, string3});
            return GlShaderModule.INVALID_SHADER;
        }
        GlShaderModule glShaderModule = new GlShaderModule(n, shaderCompilationKey.id, shaderCompilationKey.type);
        this.debugLabels.applyLabel(glShaderModule);
        return glShaderModule;
    }

    private GlRenderPipeline compilePipeline(RenderPipeline renderPipeline, BiFunction<ResourceLocation, ShaderType, String> biFunction) {
        GlProgram glProgram;
        GlShaderModule glShaderModule = this.getOrCompileShader(renderPipeline.getVertexShader(), ShaderType.VERTEX, renderPipeline.getShaderDefines(), biFunction);
        GlShaderModule glShaderModule2 = this.getOrCompileShader(renderPipeline.getFragmentShader(), ShaderType.FRAGMENT, renderPipeline.getShaderDefines(), biFunction);
        if (glShaderModule == GlShaderModule.INVALID_SHADER) {
            LOGGER.error("Couldn't compile pipeline {}: vertex shader {} was invalid", (Object)renderPipeline.getLocation(), (Object)renderPipeline.getVertexShader());
            return new GlRenderPipeline(renderPipeline, GlProgram.INVALID_PROGRAM);
        }
        if (glShaderModule2 == GlShaderModule.INVALID_SHADER) {
            LOGGER.error("Couldn't compile pipeline {}: fragment shader {} was invalid", (Object)renderPipeline.getLocation(), (Object)renderPipeline.getFragmentShader());
            return new GlRenderPipeline(renderPipeline, GlProgram.INVALID_PROGRAM);
        }
        try {
            glProgram = GlProgram.link(glShaderModule, glShaderModule2, renderPipeline.getVertexFormat(), renderPipeline.getLocation().toString());
        }
        catch (ShaderManager.CompilationException compilationException) {
            LOGGER.error("Couldn't compile program for pipeline {}: {}", (Object)renderPipeline.getLocation(), (Object)compilationException);
            return new GlRenderPipeline(renderPipeline, GlProgram.INVALID_PROGRAM);
        }
        glProgram.setupUniforms(renderPipeline.getUniforms(), renderPipeline.getSamplers());
        this.debugLabels.applyLabel(glProgram);
        return new GlRenderPipeline(renderPipeline, glProgram);
    }

    public VertexArrayCache vertexArrayCache() {
        return this.vertexArrayCache;
    }

    public BufferStorage getBufferStorage() {
        return this.bufferStorage;
    }

    public /* synthetic */ CompiledRenderPipeline precompilePipeline(RenderPipeline renderPipeline, @Nullable BiFunction biFunction) {
        return this.precompilePipeline(renderPipeline, biFunction);
    }

    static final class ShaderCompilationKey
    extends Record {
        final ResourceLocation id;
        final ShaderType type;
        final ShaderDefines defines;

        ShaderCompilationKey(ResourceLocation resourceLocation, ShaderType shaderType, ShaderDefines shaderDefines) {
            this.id = resourceLocation;
            this.type = shaderType;
            this.defines = shaderDefines;
        }

        @Override
        public String toString() {
            String string = String.valueOf(this.id) + " (" + String.valueOf((Object)this.type) + ")";
            if (!this.defines.isEmpty()) {
                return string + " with " + String.valueOf(this.defines);
            }
            return string;
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ShaderCompilationKey.class, "id;type;defines", "id", "type", "defines"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ShaderCompilationKey.class, "id;type;defines", "id", "type", "defines"}, this, object);
        }

        public ResourceLocation id() {
            return this.id;
        }

        public ShaderType type() {
            return this.type;
        }

        public ShaderDefines defines() {
            return this.defines;
        }
    }
}

