/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.chunk;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.EnumMap;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.SequencedCollection;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;

public record ChunkSectionsToRender(EnumMap<ChunkSectionLayer, List<RenderPass.Draw<GpuBufferSlice[]>>> drawsPerLayer, int maxIndicesRequired, GpuBufferSlice[] dynamicTransforms) {
    public void renderGroup(ChunkSectionLayerGroup chunkSectionLayerGroup) {
        RenderSystem.AutoStorageIndexBuffer autoStorageIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
        GpuBuffer gpuBuffer = this.maxIndicesRequired == 0 ? null : autoStorageIndexBuffer.getBuffer(this.maxIndicesRequired);
        VertexFormat.IndexType indexType = this.maxIndicesRequired == 0 ? null : autoStorageIndexBuffer.type();
        ChunkSectionLayer[] chunkSectionLayerArray = chunkSectionLayerGroup.layers();
        Minecraft minecraft = Minecraft.getInstance();
        boolean bl = false;
        RenderTarget renderTarget = chunkSectionLayerGroup.outputTarget();
        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Section layers for " + chunkSectionLayerGroup.label(), renderTarget.getColorTextureView(), OptionalInt.empty(), renderTarget.getDepthTextureView(), OptionalDouble.empty());){
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.bindSampler("Sampler2", minecraft.gameRenderer.lightTexture().getTextureView());
            for (ChunkSectionLayer chunkSectionLayer : chunkSectionLayerArray) {
                SequencedCollection<RenderPass.Draw<Object>> sequencedCollection = this.drawsPerLayer.get((Object)chunkSectionLayer);
                if (sequencedCollection.isEmpty()) continue;
                if (chunkSectionLayer == ChunkSectionLayer.TRANSLUCENT) {
                    sequencedCollection = sequencedCollection.reversed();
                }
                renderPass.setPipeline(bl ? RenderPipelines.WIREFRAME : chunkSectionLayer.pipeline());
                renderPass.bindSampler("Sampler0", chunkSectionLayer.textureView());
                renderPass.drawMultipleIndexed(sequencedCollection, gpuBuffer, indexType, List.of("DynamicTransforms"), this.dynamicTransforms);
            }
        }
    }
}

