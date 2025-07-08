/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerEarsModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;

public class Deadmau5EarsLayer
extends RenderLayer<PlayerRenderState, PlayerModel> {
    private final HumanoidModel<PlayerRenderState> model;

    public Deadmau5EarsLayer(RenderLayerParent<PlayerRenderState, PlayerModel> renderLayerParent, EntityModelSet entityModelSet) {
        super(renderLayerParent);
        this.model = new PlayerEarsModel(entityModelSet.bakeLayer(ModelLayers.PLAYER_EARS));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, PlayerRenderState playerRenderState, float f, float f2) {
        if (!"deadmau5".equals(playerRenderState.name) || playerRenderState.isInvisible) {
            return;
        }
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entitySolid(playerRenderState.skin.texture()));
        int n2 = LivingEntityRenderer.getOverlayCoords(playerRenderState, 0.0f);
        ((PlayerModel)this.getParentModel()).copyPropertiesTo(this.model);
        this.model.setupAnim(playerRenderState);
        this.model.renderToBuffer(poseStack, vertexConsumer, n, n2);
    }
}

