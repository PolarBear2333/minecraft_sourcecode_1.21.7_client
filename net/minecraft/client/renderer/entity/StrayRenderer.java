/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractSkeletonRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.SkeletonClothingLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.SkeletonRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Stray;

public class StrayRenderer
extends AbstractSkeletonRenderer<Stray, SkeletonRenderState> {
    private static final ResourceLocation STRAY_SKELETON_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/skeleton/stray.png");
    private static final ResourceLocation STRAY_CLOTHES_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/skeleton/stray_overlay.png");

    public StrayRenderer(EntityRendererProvider.Context context) {
        super(context, ModelLayers.STRAY, ModelLayers.STRAY_INNER_ARMOR, ModelLayers.STRAY_OUTER_ARMOR);
        this.addLayer(new SkeletonClothingLayer<SkeletonRenderState, SkeletonModel<SkeletonRenderState>>(this, context.getModelSet(), ModelLayers.STRAY_OUTER_LAYER, STRAY_CLOTHES_LOCATION));
    }

    @Override
    public ResourceLocation getTextureLocation(SkeletonRenderState skeletonRenderState) {
        return STRAY_SKELETON_LOCATION;
    }

    @Override
    public SkeletonRenderState createRenderState() {
        return new SkeletonRenderState();
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

