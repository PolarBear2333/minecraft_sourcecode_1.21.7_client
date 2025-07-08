/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SpecialBlockModelRenderer;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class BlockRenderDispatcher
implements ResourceManagerReloadListener {
    private final BlockModelShaper blockModelShaper;
    private final ModelBlockRenderer modelRenderer;
    private final Supplier<SpecialBlockModelRenderer> specialBlockModelRenderer;
    private final LiquidBlockRenderer liquidBlockRenderer;
    private final RandomSource singleThreadRandom = RandomSource.create();
    private final List<BlockModelPart> singleThreadPartList = new ArrayList<BlockModelPart>();
    private final BlockColors blockColors;

    public BlockRenderDispatcher(BlockModelShaper blockModelShaper, Supplier<SpecialBlockModelRenderer> supplier, BlockColors blockColors) {
        this.blockModelShaper = blockModelShaper;
        this.specialBlockModelRenderer = supplier;
        this.blockColors = blockColors;
        this.modelRenderer = new ModelBlockRenderer(this.blockColors);
        this.liquidBlockRenderer = new LiquidBlockRenderer();
    }

    public BlockModelShaper getBlockModelShaper() {
        return this.blockModelShaper;
    }

    public void renderBreakingTexture(BlockState blockState, BlockPos blockPos, BlockAndTintGetter blockAndTintGetter, PoseStack poseStack, VertexConsumer vertexConsumer) {
        if (blockState.getRenderShape() != RenderShape.MODEL) {
            return;
        }
        BlockStateModel blockStateModel = this.blockModelShaper.getBlockModel(blockState);
        this.singleThreadRandom.setSeed(blockState.getSeed(blockPos));
        this.singleThreadPartList.clear();
        blockStateModel.collectParts(this.singleThreadRandom, this.singleThreadPartList);
        this.modelRenderer.tesselateBlock(blockAndTintGetter, this.singleThreadPartList, blockState, blockPos, poseStack, vertexConsumer, true, OverlayTexture.NO_OVERLAY);
    }

    public void renderBatched(BlockState blockState, BlockPos blockPos, BlockAndTintGetter blockAndTintGetter, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, List<BlockModelPart> list) {
        try {
            this.modelRenderer.tesselateBlock(blockAndTintGetter, list, blockState, blockPos, poseStack, vertexConsumer, bl, OverlayTexture.NO_OVERLAY);
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Tesselating block in world");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Block being tesselated");
            CrashReportCategory.populateBlockDetails(crashReportCategory, blockAndTintGetter, blockPos, blockState);
            throw new ReportedException(crashReport);
        }
    }

    public void renderLiquid(BlockPos blockPos, BlockAndTintGetter blockAndTintGetter, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState) {
        try {
            this.liquidBlockRenderer.tesselate(blockAndTintGetter, blockPos, vertexConsumer, blockState, fluidState);
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Tesselating liquid in world");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Block being tesselated");
            CrashReportCategory.populateBlockDetails(crashReportCategory, blockAndTintGetter, blockPos, blockState);
            throw new ReportedException(crashReport);
        }
    }

    public ModelBlockRenderer getModelRenderer() {
        return this.modelRenderer;
    }

    public BlockStateModel getBlockModel(BlockState blockState) {
        return this.blockModelShaper.getBlockModel(blockState);
    }

    public void renderSingleBlock(BlockState blockState, PoseStack poseStack, MultiBufferSource multiBufferSource, int n, int n2) {
        RenderShape renderShape = blockState.getRenderShape();
        if (renderShape == RenderShape.INVISIBLE) {
            return;
        }
        BlockStateModel blockStateModel = this.getBlockModel(blockState);
        int n3 = this.blockColors.getColor(blockState, null, null, 0);
        float f = (float)(n3 >> 16 & 0xFF) / 255.0f;
        float f2 = (float)(n3 >> 8 & 0xFF) / 255.0f;
        float f3 = (float)(n3 & 0xFF) / 255.0f;
        ModelBlockRenderer.renderModel(poseStack.last(), multiBufferSource.getBuffer(ItemBlockRenderTypes.getRenderType(blockState)), blockStateModel, f, f2, f3, n, n2);
        this.specialBlockModelRenderer.get().renderByBlock(blockState.getBlock(), ItemDisplayContext.NONE, poseStack, multiBufferSource, n, n2);
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        this.liquidBlockRenderer.setupSprites();
    }
}

