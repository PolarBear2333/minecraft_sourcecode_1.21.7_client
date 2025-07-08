/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HappyGhastModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HappyGhastRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;

public class RopesLayer<M extends HappyGhastModel>
extends RenderLayer<HappyGhastRenderState, M> {
    private final RenderType ropes;
    private final HappyGhastModel adultModel;
    private final HappyGhastModel babyModel;

    public RopesLayer(RenderLayerParent<HappyGhastRenderState, M> renderLayerParent, EntityModelSet entityModelSet, ResourceLocation resourceLocation) {
        super(renderLayerParent);
        this.ropes = RenderType.entityCutoutNoCull(resourceLocation);
        this.adultModel = new HappyGhastModel(entityModelSet.bakeLayer(ModelLayers.HAPPY_GHAST_ROPES));
        this.babyModel = new HappyGhastModel(entityModelSet.bakeLayer(ModelLayers.HAPPY_GHAST_BABY_ROPES));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int n, HappyGhastRenderState happyGhastRenderState, float f, float f2) {
        if (!happyGhastRenderState.isLeashHolder || !happyGhastRenderState.bodyItem.is(ItemTags.HARNESSES)) {
            return;
        }
        HappyGhastModel happyGhastModel = happyGhastRenderState.isBaby ? this.babyModel : this.adultModel;
        happyGhastModel.setupAnim(happyGhastRenderState);
        happyGhastModel.renderToBuffer(poseStack, multiBufferSource.getBuffer(this.ropes), n, OverlayTexture.NO_OVERLAY);
    }
}

