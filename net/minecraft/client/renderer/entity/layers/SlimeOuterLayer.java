/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;

public class SlimeOuterLayer
extends RenderLayer<SlimeRenderState, SlimeModel> {
    private final SlimeModel model;

    public SlimeOuterLayer(RenderLayerParent<SlimeRenderState, SlimeModel> renderLayerParent, EntityModelSet entityModelSet) {
        super(renderLayerParent);
        this.model = new SlimeModel(entityModelSet.bakeLayer(ModelLayers.SLIME_OUTER));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, SlimeRenderState slimeRenderState, float f, float f2) {
        boolean bl;
        boolean bl2 = bl = slimeRenderState.appearsGlowing && slimeRenderState.isInvisible;
        if (slimeRenderState.isInvisible && !bl) {
            return;
        }
        VertexConsumer vertexConsumer = bl ? multiBufferSource.getBuffer(RenderType.outline(SlimeRenderer.SLIME_LOCATION)) : multiBufferSource.getBuffer(RenderType.entityTranslucent(SlimeRenderer.SLIME_LOCATION));
        this.model.setupAnim(slimeRenderState);
        this.model.renderToBuffer(poseStack, vertexConsumer, n, LivingEntityRenderer.getOverlayCoords(slimeRenderState, 0.0f));
    }
}

