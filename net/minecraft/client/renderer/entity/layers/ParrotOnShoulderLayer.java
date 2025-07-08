/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ParrotModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ParrotRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.ParrotRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.animal.Parrot;

public class ParrotOnShoulderLayer
extends RenderLayer<PlayerRenderState, PlayerModel> {
    private final ParrotModel model;
    private final ParrotRenderState parrotState = new ParrotRenderState();

    public ParrotOnShoulderLayer(RenderLayerParent<PlayerRenderState, PlayerModel> renderLayerParent, EntityModelSet entityModelSet) {
        super(renderLayerParent);
        this.model = new ParrotModel(entityModelSet.bakeLayer(ModelLayers.PARROT));
        this.parrotState.pose = ParrotModel.Pose.ON_SHOULDER;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, PlayerRenderState playerRenderState, float f, float f2) {
        Parrot.Variant variant;
        Parrot.Variant variant2 = playerRenderState.parrotOnLeftShoulder;
        if (variant2 != null) {
            this.renderOnShoulder(poseStack, multiBufferSource, n, playerRenderState, variant2, f, f2, true);
        }
        if ((variant = playerRenderState.parrotOnRightShoulder) != null) {
            this.renderOnShoulder(poseStack, multiBufferSource, n, playerRenderState, variant, f, f2, false);
        }
    }

    private void renderOnShoulder(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, PlayerRenderState playerRenderState, Parrot.Variant variant, float f, float f2, boolean bl) {
        poseStack.pushPose();
        poseStack.translate(bl ? 0.4f : -0.4f, playerRenderState.isCrouching ? -1.3f : -1.5f, 0.0f);
        this.parrotState.ageInTicks = playerRenderState.ageInTicks;
        this.parrotState.walkAnimationPos = playerRenderState.walkAnimationPos;
        this.parrotState.walkAnimationSpeed = playerRenderState.walkAnimationSpeed;
        this.parrotState.yRot = f;
        this.parrotState.xRot = f2;
        this.model.setupAnim(this.parrotState);
        this.model.renderToBuffer(poseStack, multiBufferSource.getBuffer(this.model.renderType(ParrotRenderer.getVariantTexture(variant))), n, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }
}

