/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 *  org.joml.Matrix4fc
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.joml.Vector4f
 *  org.joml.Vector4fc
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

public class WorldBorderRenderer {
    public static final ResourceLocation FORCEFIELD_LOCATION = ResourceLocation.withDefaultNamespace("textures/misc/forcefield.png");
    private boolean needsRebuild = true;
    private double lastMinX;
    private double lastMinZ;
    private double lastBorderMinX;
    private double lastBorderMaxX;
    private double lastBorderMinZ;
    private double lastBorderMaxZ;
    private final GpuBuffer worldBorderBuffer = RenderSystem.getDevice().createBuffer(() -> "World border vertex buffer", 40, 16 * DefaultVertexFormat.POSITION_TEX.getVertexSize());
    private final RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);

    private void rebuildWorldBorderBuffer(WorldBorder worldBorder, double d, double d2, double d3, float f, float f2, float f3) {
        try (ByteBufferBuilder byteBufferBuilder = ByteBufferBuilder.exactlySized(DefaultVertexFormat.POSITION_TEX.getVertexSize() * 4 * 4);){
            double d4 = worldBorder.getMinX();
            double d5 = worldBorder.getMaxX();
            double d6 = worldBorder.getMinZ();
            double d7 = worldBorder.getMaxZ();
            double d8 = Math.max((double)Mth.floor(d2 - d), d6);
            double d9 = Math.min((double)Mth.ceil(d2 + d), d7);
            float f4 = (float)(Mth.floor(d8) & 1) * 0.5f;
            float f5 = (float)(d9 - d8) / 2.0f;
            double d10 = Math.max((double)Mth.floor(d3 - d), d4);
            double d11 = Math.min((double)Mth.ceil(d3 + d), d5);
            float f6 = (float)(Mth.floor(d10) & 1) * 0.5f;
            float f7 = (float)(d11 - d10) / 2.0f;
            BufferBuilder bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            bufferBuilder.addVertex(0.0f, -f, (float)(d7 - d8)).setUv(f6, f2);
            bufferBuilder.addVertex((float)(d11 - d10), -f, (float)(d7 - d8)).setUv(f7 + f6, f2);
            bufferBuilder.addVertex((float)(d11 - d10), f, (float)(d7 - d8)).setUv(f7 + f6, f3);
            bufferBuilder.addVertex(0.0f, f, (float)(d7 - d8)).setUv(f6, f3);
            bufferBuilder.addVertex(0.0f, -f, 0.0f).setUv(f4, f2);
            bufferBuilder.addVertex(0.0f, -f, (float)(d9 - d8)).setUv(f5 + f4, f2);
            bufferBuilder.addVertex(0.0f, f, (float)(d9 - d8)).setUv(f5 + f4, f3);
            bufferBuilder.addVertex(0.0f, f, 0.0f).setUv(f4, f3);
            bufferBuilder.addVertex((float)(d11 - d10), -f, 0.0f).setUv(f6, f2);
            bufferBuilder.addVertex(0.0f, -f, 0.0f).setUv(f7 + f6, f2);
            bufferBuilder.addVertex(0.0f, f, 0.0f).setUv(f7 + f6, f3);
            bufferBuilder.addVertex((float)(d11 - d10), f, 0.0f).setUv(f6, f3);
            bufferBuilder.addVertex((float)(d5 - d10), -f, (float)(d9 - d8)).setUv(f4, f2);
            bufferBuilder.addVertex((float)(d5 - d10), -f, 0.0f).setUv(f5 + f4, f2);
            bufferBuilder.addVertex((float)(d5 - d10), f, 0.0f).setUv(f5 + f4, f3);
            bufferBuilder.addVertex((float)(d5 - d10), f, (float)(d9 - d8)).setUv(f4, f3);
            try (MeshData meshData = bufferBuilder.buildOrThrow();){
                RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.worldBorderBuffer.slice(), meshData.vertexBuffer());
            }
            this.lastBorderMinX = d4;
            this.lastBorderMaxX = d5;
            this.lastBorderMinZ = d6;
            this.lastBorderMaxZ = d7;
            this.lastMinX = d10;
            this.lastMinZ = d8;
            this.needsRebuild = false;
        }
    }

    public void render(WorldBorder worldBorder, Vec3 vec3, double d, double d2) {
        GpuTextureView gpuTextureView;
        GpuTextureView gpuTextureView2;
        double d3 = worldBorder.getMinX();
        double d4 = worldBorder.getMaxX();
        double d5 = worldBorder.getMinZ();
        double d6 = worldBorder.getMaxZ();
        if (vec3.x < d4 - d && vec3.x > d3 + d && vec3.z < d6 - d && vec3.z > d5 + d || vec3.x < d3 - d || vec3.x > d4 + d || vec3.z < d5 - d || vec3.z > d6 + d) {
            return;
        }
        double d7 = 1.0 - worldBorder.getDistanceToBorder(vec3.x, vec3.z) / d;
        d7 = Math.pow(d7, 4.0);
        d7 = Mth.clamp(d7, 0.0, 1.0);
        double d8 = vec3.x;
        double d9 = vec3.z;
        float f = (float)d2;
        int n = worldBorder.getStatus().getColor();
        float f2 = (float)ARGB.red(n) / 255.0f;
        float f3 = (float)ARGB.green(n) / 255.0f;
        float f4 = (float)ARGB.blue(n) / 255.0f;
        float f5 = (float)(Util.getMillis() % 3000L) / 3000.0f;
        float f6 = (float)(-Mth.frac(vec3.y * 0.5));
        float f7 = f6 + f;
        if (this.shouldRebuildWorldBorderBuffer(worldBorder)) {
            this.rebuildWorldBorderBuffer(worldBorder, d, d9, d8, f, f7, f6);
        }
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        AbstractTexture abstractTexture = textureManager.getTexture(FORCEFIELD_LOCATION);
        abstractTexture.setUseMipmaps(false);
        RenderPipeline renderPipeline = RenderPipelines.WORLD_BORDER;
        RenderTarget renderTarget = Minecraft.getInstance().getMainRenderTarget();
        RenderTarget renderTarget2 = Minecraft.getInstance().levelRenderer.getWeatherTarget();
        if (renderTarget2 != null) {
            gpuTextureView2 = renderTarget2.getColorTextureView();
            gpuTextureView = renderTarget2.getDepthTextureView();
        } else {
            gpuTextureView2 = renderTarget.getColorTextureView();
            gpuTextureView = renderTarget.getDepthTextureView();
        }
        GpuBuffer gpuBuffer = this.indices.getBuffer(6);
        GpuBufferSlice gpuBufferSlice = RenderSystem.getDynamicUniforms().writeTransform((Matrix4fc)RenderSystem.getModelViewMatrix(), (Vector4fc)new Vector4f(f2, f3, f4, (float)d7), (Vector3fc)new Vector3f((float)(this.lastMinX - d8), (float)(-vec3.y), (float)(this.lastMinZ - d9)), (Matrix4fc)new Matrix4f().translation(f5, f5, 0.0f), 0.0f);
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "World border", gpuTextureView2, OptionalInt.empty(), gpuTextureView, OptionalDouble.empty());){
            renderPass.setPipeline(renderPipeline);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", gpuBufferSlice);
            renderPass.setIndexBuffer(gpuBuffer, this.indices.type());
            renderPass.bindSampler("Sampler0", abstractTexture.getTextureView());
            renderPass.setVertexBuffer(0, this.worldBorderBuffer);
            ArrayList arrayList = new ArrayList();
            for (WorldBorder.DistancePerDirection distancePerDirection : worldBorder.closestBorder(d8, d9)) {
                if (!(distancePerDirection.distance() < d)) continue;
                int n2 = distancePerDirection.direction().get2DDataValue();
                arrayList.add(new RenderPass.Draw(0, this.worldBorderBuffer, gpuBuffer, this.indices.type(), 6 * n2, 6));
            }
            renderPass.drawMultipleIndexed(arrayList, null, null, Collections.emptyList(), this);
        }
    }

    public void invalidate() {
        this.needsRebuild = true;
    }

    private boolean shouldRebuildWorldBorderBuffer(WorldBorder worldBorder) {
        return this.needsRebuild || worldBorder.getMinX() != this.lastBorderMinX || worldBorder.getMinZ() != this.lastBorderMinZ || worldBorder.getMaxX() != this.lastBorderMaxX || worldBorder.getMaxZ() != this.lastBorderMaxZ;
    }
}

