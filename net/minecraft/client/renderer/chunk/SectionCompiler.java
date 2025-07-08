/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.chunk;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.client.renderer.chunk.VisGraph;
import net.minecraft.client.renderer.chunk.VisibilitySet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class SectionCompiler {
    private final BlockRenderDispatcher blockRenderer;
    private final BlockEntityRenderDispatcher blockEntityRenderer;

    public SectionCompiler(BlockRenderDispatcher blockRenderDispatcher, BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
        this.blockRenderer = blockRenderDispatcher;
        this.blockEntityRenderer = blockEntityRenderDispatcher;
    }

    public Results compile(SectionPos sectionPos, RenderSectionRegion renderSectionRegion, VertexSorting vertexSorting, SectionBufferBuilderPack sectionBufferBuilderPack) {
        Object object;
        Object object2;
        Results results = new Results();
        BlockPos blockPos = sectionPos.origin();
        BlockPos blockPos2 = blockPos.offset(15, 15, 15);
        VisGraph visGraph = new VisGraph();
        PoseStack poseStack = new PoseStack();
        ModelBlockRenderer.enableCaching();
        EnumMap<ChunkSectionLayer, BufferBuilder> enumMap = new EnumMap<ChunkSectionLayer, BufferBuilder>(ChunkSectionLayer.class);
        RandomSource randomSource = RandomSource.create();
        ObjectArrayList objectArrayList = new ObjectArrayList();
        for (BlockPos object3 : BlockPos.betweenClosed(blockPos, blockPos2)) {
            BufferBuilder bufferBuilder;
            ChunkSectionLayer chunkSectionLayer;
            object2 = renderSectionRegion.getBlockState(object3);
            if (((BlockBehaviour.BlockStateBase)object2).isSolidRender()) {
                visGraph.setOpaque(object3);
            }
            if (((BlockBehaviour.BlockStateBase)object2).hasBlockEntity() && (object = renderSectionRegion.getBlockEntity(object3)) != null) {
                this.handleBlockEntity(results, object);
            }
            if (!((FluidState)(object = ((BlockBehaviour.BlockStateBase)object2).getFluidState())).isEmpty()) {
                chunkSectionLayer = ItemBlockRenderTypes.getRenderLayer((FluidState)object);
                bufferBuilder = this.getOrBeginLayer(enumMap, sectionBufferBuilderPack, chunkSectionLayer);
                this.blockRenderer.renderLiquid(object3, renderSectionRegion, bufferBuilder, (BlockState)object2, (FluidState)object);
            }
            if (((BlockBehaviour.BlockStateBase)object2).getRenderShape() != RenderShape.MODEL) continue;
            chunkSectionLayer = ItemBlockRenderTypes.getChunkRenderType((BlockState)object2);
            bufferBuilder = this.getOrBeginLayer(enumMap, sectionBufferBuilderPack, chunkSectionLayer);
            randomSource.setSeed(((BlockBehaviour.BlockStateBase)object2).getSeed(object3));
            this.blockRenderer.getBlockModel((BlockState)object2).collectParts(randomSource, (List<BlockModelPart>)objectArrayList);
            poseStack.pushPose();
            poseStack.translate(SectionPos.sectionRelative(object3.getX()), SectionPos.sectionRelative(object3.getY()), SectionPos.sectionRelative(object3.getZ()));
            this.blockRenderer.renderBatched((BlockState)object2, object3, renderSectionRegion, poseStack, bufferBuilder, true, (List<BlockModelPart>)objectArrayList);
            poseStack.popPose();
            objectArrayList.clear();
        }
        for (Map.Entry entry : enumMap.entrySet()) {
            object2 = (ChunkSectionLayer)((Object)entry.getKey());
            object = ((BufferBuilder)entry.getValue()).build();
            if (object == null) continue;
            if (object2 == ChunkSectionLayer.TRANSLUCENT) {
                results.transparencyState = ((MeshData)object).sortQuads(sectionBufferBuilderPack.buffer((ChunkSectionLayer)((Object)object2)), vertexSorting);
            }
            results.renderedLayers.put((ChunkSectionLayer)((Object)object2), (MeshData)object);
        }
        ModelBlockRenderer.clearCache();
        results.visibilitySet = visGraph.resolve();
        return results;
    }

    private BufferBuilder getOrBeginLayer(Map<ChunkSectionLayer, BufferBuilder> map, SectionBufferBuilderPack sectionBufferBuilderPack, ChunkSectionLayer chunkSectionLayer) {
        BufferBuilder bufferBuilder = map.get((Object)chunkSectionLayer);
        if (bufferBuilder == null) {
            ByteBufferBuilder byteBufferBuilder = sectionBufferBuilderPack.buffer(chunkSectionLayer);
            bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
            map.put(chunkSectionLayer, bufferBuilder);
        }
        return bufferBuilder;
    }

    private <E extends BlockEntity> void handleBlockEntity(Results results, E e) {
        BlockEntityRenderer<E> blockEntityRenderer = this.blockEntityRenderer.getRenderer(e);
        if (blockEntityRenderer != null && !blockEntityRenderer.shouldRenderOffScreen()) {
            results.blockEntities.add(e);
        }
    }

    public static final class Results {
        public final List<BlockEntity> blockEntities = new ArrayList<BlockEntity>();
        public final Map<ChunkSectionLayer, MeshData> renderedLayers = new EnumMap<ChunkSectionLayer, MeshData>(ChunkSectionLayer.class);
        public VisibilitySet visibilitySet = new VisibilitySet();
        @Nullable
        public MeshData.SortState transparencyState;

        public void release() {
            this.renderedLayers.values().forEach(MeshData::close);
        }
    }
}

