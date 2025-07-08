/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.CowModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.MushroomCowRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionfc;

public class MushroomCowMushroomLayer
extends RenderLayer<MushroomCowRenderState, CowModel> {
    private final BlockRenderDispatcher blockRenderer;

    public MushroomCowMushroomLayer(RenderLayerParent<MushroomCowRenderState, CowModel> renderLayerParent, BlockRenderDispatcher blockRenderDispatcher) {
        super(renderLayerParent);
        this.blockRenderer = blockRenderDispatcher;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, MushroomCowRenderState mushroomCowRenderState, float f, float f2) {
        boolean bl;
        if (mushroomCowRenderState.isBaby) {
            return;
        }
        boolean bl2 = bl = mushroomCowRenderState.appearsGlowing && mushroomCowRenderState.isInvisible;
        if (mushroomCowRenderState.isInvisible && !bl) {
            return;
        }
        BlockState blockState = mushroomCowRenderState.variant.getBlockState();
        int n2 = LivingEntityRenderer.getOverlayCoords(mushroomCowRenderState, 0.0f);
        BlockStateModel blockStateModel = this.blockRenderer.getBlockModel(blockState);
        poseStack.pushPose();
        poseStack.translate(0.2f, -0.35f, 0.5f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(-48.0f));
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        poseStack.translate(-0.5f, -0.5f, -0.5f);
        this.renderMushroomBlock(poseStack, multiBufferSource, n, bl, blockState, n2, blockStateModel);
        poseStack.popPose();
        poseStack.pushPose();
        poseStack.translate(0.2f, -0.35f, 0.5f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(42.0f));
        poseStack.translate(0.1f, 0.0f, -0.6f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(-48.0f));
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        poseStack.translate(-0.5f, -0.5f, -0.5f);
        this.renderMushroomBlock(poseStack, multiBufferSource, n, bl, blockState, n2, blockStateModel);
        poseStack.popPose();
        poseStack.pushPose();
        ((CowModel)this.getParentModel()).getHead().translateAndRotate(poseStack);
        poseStack.translate(0.0f, -0.7f, -0.2f);
        poseStack.mulPose((Quaternionfc)Axis.YP.rotationDegrees(-78.0f));
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        poseStack.translate(-0.5f, -0.5f, -0.5f);
        this.renderMushroomBlock(poseStack, multiBufferSource, n, bl, blockState, n2, blockStateModel);
        poseStack.popPose();
    }

    private void renderMushroomBlock(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, boolean bl, BlockState blockState, int n2, BlockStateModel blockStateModel) {
        if (bl) {
            ModelBlockRenderer.renderModel(poseStack.last(), multiBufferSource.getBuffer(RenderType.outline(TextureAtlas.LOCATION_BLOCKS)), blockStateModel, 0.0f, 0.0f, 0.0f, n, n2);
        } else {
            this.blockRenderer.renderSingleBlock(blockState, poseStack, multiBufferSource, n, n2);
        }
    }
}

