/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.SpinAttackEffectModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class SpinAttackEffectLayer
extends RenderLayer<PlayerRenderState, PlayerModel> {
    public static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/trident_riptide.png");
    private final SpinAttackEffectModel model;

    public SpinAttackEffectLayer(RenderLayerParent<PlayerRenderState, PlayerModel> renderLayerParent, EntityModelSet entityModelSet) {
        super(renderLayerParent);
        this.model = new SpinAttackEffectModel(entityModelSet.bakeLayer(ModelLayers.PLAYER_SPIN_ATTACK));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, PlayerRenderState playerRenderState, float f, float f2) {
        if (!playerRenderState.isAutoSpinAttack) {
            return;
        }
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(this.model.renderType(TEXTURE));
        this.model.setupAnim(playerRenderState);
        this.model.renderToBuffer(poseStack, vertexConsumer, n, OverlayTexture.NO_OVERLAY);
    }
}

