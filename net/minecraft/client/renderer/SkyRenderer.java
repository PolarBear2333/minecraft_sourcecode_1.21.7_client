/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix3f
 *  org.joml.Matrix3fc
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fStack
 *  org.joml.Matrix4fc
 *  org.joml.Quaternionfc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

public class SkyRenderer
implements AutoCloseable {
    private static final ResourceLocation SUN_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/sun.png");
    private static final ResourceLocation MOON_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/moon_phases.png");
    public static final ResourceLocation END_SKY_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/end_sky.png");
    private static final float SKY_DISC_RADIUS = 512.0f;
    private static final int SKY_VERTICES = 10;
    private static final int STAR_COUNT = 1500;
    private static final int END_SKY_QUAD_COUNT = 6;
    private final GpuBuffer starBuffer;
    private final RenderSystem.AutoStorageIndexBuffer starIndices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
    private final GpuBuffer topSkyBuffer;
    private final GpuBuffer bottomSkyBuffer;
    private final GpuBuffer endSkyBuffer;
    private int starIndexCount;

    public SkyRenderer() {
        this.starBuffer = this.buildStars();
        this.endSkyBuffer = SkyRenderer.buildEndSky();
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(10 * DefaultVertexFormat.POSITION.getVertexSize());){
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
            this.buildSkyDisc(bufferBuilder, 16.0f);
            try (MeshData meshData = bufferBuilder.buildOrThrow();){
                this.topSkyBuffer = RenderSystem.getDevice().createBuffer(() -> "Top sky vertex buffer", 32, meshData.vertexBuffer());
            }
            bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
            this.buildSkyDisc(bufferBuilder, -16.0f);
            meshData = bufferBuilder.buildOrThrow();
            try {
                this.bottomSkyBuffer = RenderSystem.getDevice().createBuffer(() -> "Bottom sky vertex buffer", 32, meshData.vertexBuffer());
            }
            finally {
                if (meshData != null) {
                    meshData.close();
                }
            }
        }
    }

    private GpuBuffer buildStars() {
        RandomSource randomSource = RandomSource.create(10842L);
        float f = 100.0f;
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(DefaultVertexFormat.POSITION.getVertexSize() * 1500 * 4);){
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
            for (int i = 0; i < 1500; ++i) {
                float f2 = randomSource.nextFloat() * 2.0f - 1.0f;
                float f3 = randomSource.nextFloat() * 2.0f - 1.0f;
                float f4 = randomSource.nextFloat() * 2.0f - 1.0f;
                float f5 = 0.15f + randomSource.nextFloat() * 0.1f;
                float f6 = Mth.lengthSquared(f2, f3, f4);
                if (f6 <= 0.010000001f || f6 >= 1.0f) continue;
                Vector3f vector3f = new Vector3f(f2, f3, f4).normalize(100.0f);
                float f7 = (float)(randomSource.nextDouble() * 3.1415927410125732 * 2.0);
                Matrix3f matrix3f = new Matrix3f().rotateTowards((Vector3fc)new Vector3f((Vector3fc)vector3f).negate(), (Vector3fc)new Vector3f(0.0f, 1.0f, 0.0f)).rotateZ(-f7);
                bufferBuilder.addVertex(new Vector3f(f5, -f5, 0.0f).mul((Matrix3fc)matrix3f).add((Vector3fc)vector3f));
                bufferBuilder.addVertex(new Vector3f(f5, f5, 0.0f).mul((Matrix3fc)matrix3f).add((Vector3fc)vector3f));
                bufferBuilder.addVertex(new Vector3f(-f5, f5, 0.0f).mul((Matrix3fc)matrix3f).add((Vector3fc)vector3f));
                bufferBuilder.addVertex(new Vector3f(-f5, -f5, 0.0f).mul((Matrix3fc)matrix3f).add((Vector3fc)vector3f));
            }
            MeshData meshData = bufferBuilder.buildOrThrow();
            try {
                this.starIndexCount = meshData.drawState().indexCount();
                GpuBuffer gpuBuffer = RenderSystem.getDevice().createBuffer(() -> "Stars vertex buffer", 40, meshData.vertexBuffer());
                if (meshData != null) {
                    meshData.close();
                }
                return gpuBuffer;
            }
            catch (Throwable throwable) {
                if (meshData != null) {
                    try {
                        meshData.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
        }
    }

    private void buildSkyDisc(VertexConsumer vertexConsumer, float f) {
        float f2 = Math.signum(f) * 512.0f;
        vertexConsumer.addVertex(0.0f, f, 0.0f);
        for (int i = -180; i <= 180; i += 45) {
            vertexConsumer.addVertex(f2 * Mth.cos((float)i * ((float)Math.PI / 180)), f, 512.0f * Mth.sin((float)i * ((float)Math.PI / 180)));
        }
    }

    public void renderSkyDisc(float f, float f2, float f3) {
        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)new Vector4f(f, f2, f3, 1.0f), (Vector3fc)new Vector3f(), (Matrix4fc)new Matrix4f(), 0.0f);
        GpuTextureView gpuTextureView = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
        GpuTextureView gpuTextureView2 = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView();
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Sky disc", gpuTextureView, OptionalInt.empty(), gpuTextureView2, OptionalDouble.empty());){
            renderPass.setPipeline(RenderPipelines.SKY);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
            renderPass.setVertexBuffer(0, this.topSkyBuffer);
            renderPass.draw(0, 10);
        }
    }

    public void renderDarkDisc() {
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.translate(0.0f, 12.0f, 0.0f);
        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform((Matrix4fc)matrix4fStack, (Vector4fc)new Vector4f(0.0f, 0.0f, 0.0f, 1.0f), (Vector3fc)new Vector3f(), (Matrix4fc)new Matrix4f(), 0.0f);
        GpuTextureView gpuTextureView = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
        GpuTextureView gpuTextureView2 = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView();
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Sky dark", gpuTextureView, OptionalInt.empty(), gpuTextureView2, OptionalDouble.empty());){
            renderPass.setPipeline(RenderPipelines.SKY);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
            renderPass.setVertexBuffer(0, this.bottomSkyBuffer);
            renderPass.draw(0, 10);
        }
        matrix4fStack.popMatrix();
    }

    public void renderSunMoonAndStars(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float f, int n, float f2, float f3) {
        poseStack.pushPose();
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(-90.0f));
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(f * 360.0f));
        this.renderSun(f2, bufferSource, poseStack);
        this.renderMoon(n, f2, bufferSource, poseStack);
        bufferSource.endBatch();
        if (f3 > 0.0f) {
            this.renderStars(f3, poseStack);
        }
        poseStack.popPose();
    }

    private void renderSun(float f, MultiBufferSource multiBufferSource, PoseStack poseStack) {
        float f2 = 30.0f;
        float f3 = 100.0f;
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.celestial(SUN_LOCATION));
        int n = ARGB.white(f);
        Matrix4f matrix4f = poseStack.last().pose();
        vertexConsumer.addVertex(matrix4f, -30.0f, 100.0f, -30.0f).setUv(0.0f, 0.0f).setColor(n);
        vertexConsumer.addVertex(matrix4f, 30.0f, 100.0f, -30.0f).setUv(1.0f, 0.0f).setColor(n);
        vertexConsumer.addVertex(matrix4f, 30.0f, 100.0f, 30.0f).setUv(1.0f, 1.0f).setColor(n);
        vertexConsumer.addVertex(matrix4f, -30.0f, 100.0f, 30.0f).setUv(0.0f, 1.0f).setColor(n);
    }

    private void renderMoon(int n, float f, MultiBufferSource multiBufferSource, PoseStack poseStack) {
        float f2 = 20.0f;
        int n2 = n % 4;
        int n3 = n / 4 % 2;
        float f3 = (float)(n2 + 0) / 4.0f;
        float f4 = (float)(n3 + 0) / 2.0f;
        float f5 = (float)(n2 + 1) / 4.0f;
        float f6 = (float)(n3 + 1) / 2.0f;
        float f7 = 100.0f;
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.celestial(MOON_LOCATION));
        int n4 = ARGB.white(f);
        Matrix4f matrix4f = poseStack.last().pose();
        vertexConsumer.addVertex(matrix4f, -20.0f, -100.0f, 20.0f).setUv(f5, f6).setColor(n4);
        vertexConsumer.addVertex(matrix4f, 20.0f, -100.0f, 20.0f).setUv(f3, f6).setColor(n4);
        vertexConsumer.addVertex(matrix4f, 20.0f, -100.0f, -20.0f).setUv(f3, f4).setColor(n4);
        vertexConsumer.addVertex(matrix4f, -20.0f, -100.0f, -20.0f).setUv(f5, f4).setColor(n4);
    }

    private void renderStars(float f, PoseStack poseStack) {
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
        matrix4fStack.mul((Matrix4fc)poseStack.last().pose());
        RenderPipeline renderPipeline = RenderPipelines.STARS;
        GpuTextureView gpuTextureView = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
        GpuTextureView gpuTextureView2 = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView();
        GpuBuffer gpuBuffer = this.starIndices.getBuffer(this.starIndexCount);
        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform((Matrix4fc)matrix4fStack, (Vector4fc)new Vector4f(f, f, f, f), (Vector3fc)new Vector3f(), (Matrix4fc)new Matrix4f(), 0.0f);
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Stars", gpuTextureView, OptionalInt.empty(), gpuTextureView2, OptionalDouble.empty());){
            renderPass.setPipeline(renderPipeline);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
            renderPass.setVertexBuffer(0, this.starBuffer);
            renderPass.setIndexBuffer(gpuBuffer, this.starIndices.type());
            renderPass.drawIndexed(0, 0, this.starIndexCount, 1);
        }
        matrix4fStack.popMatrix();
    }

    public void renderSunriseAndSunset(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float f, int n) {
        poseStack.pushPose();
        poseStack.mulPose((Quaternionfc)Axis.XP.rotationDegrees(90.0f));
        float f2 = Mth.sin(f) < 0.0f ? 180.0f : 0.0f;
        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(f2));
        poseStack.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(90.0f));
        Matrix4f matrix4f = poseStack.last().pose();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.sunriseSunset());
        float f3 = ARGB.alphaFloat(n);
        vertexConsumer.addVertex(matrix4f, 0.0f, 100.0f, 0.0f).setColor(n);
        int n2 = ARGB.transparent(n);
        int n3 = 16;
        for (int i = 0; i <= 16; ++i) {
            float f4 = (float)i * ((float)Math.PI * 2) / 16.0f;
            float f5 = Mth.sin(f4);
            float f6 = Mth.cos(f4);
            vertexConsumer.addVertex(matrix4f, f5 * 120.0f, f6 * 120.0f, -f6 * 40.0f * f3).setColor(n2);
        }
        poseStack.popPose();
    }

    private static GpuBuffer buildEndSky() {
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(24 * DefaultVertexFormat.POSITION_TEX_COLOR.getVertexSize());){
            Object object;
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            for (int i = 0; i < 6; ++i) {
                object = new Matrix4f();
                switch (i) {
                    case 1: {
                        object.rotationX(1.5707964f);
                        break;
                    }
                    case 2: {
                        object.rotationX(-1.5707964f);
                        break;
                    }
                    case 3: {
                        object.rotationX((float)Math.PI);
                        break;
                    }
                    case 4: {
                        object.rotationZ(1.5707964f);
                        break;
                    }
                    case 5: {
                        object.rotationZ(-1.5707964f);
                    }
                }
                bufferBuilder.addVertex((Matrix4f)object, -100.0f, -100.0f, -100.0f).setUv(0.0f, 0.0f).setColor(-14145496);
                bufferBuilder.addVertex((Matrix4f)object, -100.0f, -100.0f, 100.0f).setUv(0.0f, 16.0f).setColor(-14145496);
                bufferBuilder.addVertex((Matrix4f)object, 100.0f, -100.0f, 100.0f).setUv(16.0f, 16.0f).setColor(-14145496);
                bufferBuilder.addVertex((Matrix4f)object, 100.0f, -100.0f, -100.0f).setUv(16.0f, 0.0f).setColor(-14145496);
            }
            MeshData meshData = bufferBuilder.buildOrThrow();
            try {
                object = RenderSystem.getDevice().createBuffer(() -> "End sky vertex buffer", 40, meshData.vertexBuffer());
                if (meshData != null) {
                    meshData.close();
                }
                return object;
            }
            catch (Throwable throwable) {
                if (meshData != null) {
                    try {
                        meshData.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
        }
    }

    public void renderEndSky() {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        AbstractTexture abstractTexture = textureManager.getTexture(END_SKY_LOCATION);
        abstractTexture.setUseMipmaps(false);
        RenderSystem.AutoStorageIndexBuffer autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer gpuBuffer = autoStorageIndexBuffer.getBuffer(36);
        GpuTextureView gpuTextureView = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
        GpuTextureView gpuTextureView2 = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView();
        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), (Vector3fc)new Vector3f(), (Matrix4fc)new Matrix4f(), 0.0f);
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "End sky", gpuTextureView, OptionalInt.empty(), gpuTextureView2, OptionalDouble.empty());){
            renderPass.setPipeline(RenderPipelines.END_SKY);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
            renderPass.bindSampler("Sampler0", abstractTexture.getTextureView());
            renderPass.setVertexBuffer(0, this.endSkyBuffer);
            renderPass.setIndexBuffer(gpuBuffer, autoStorageIndexBuffer.type());
            renderPass.drawIndexed(0, 0, 36, 1);
        }
    }

    @Override
    public void close() {
        this.starBuffer.close();
        this.topSkyBuffer.close();
        this.bottomSkyBuffer.close();
        this.endSkyBuffer.close();
    }
}

