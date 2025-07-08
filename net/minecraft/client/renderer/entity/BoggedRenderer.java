/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.BoggedModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractSkeletonRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.SkeletonClothingLayer;
import net.minecraft.client.renderer.entity.state.BoggedRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Bogged;

public class BoggedRenderer
extends AbstractSkeletonRenderer<Bogged, BoggedRenderState> {
    private static final ResourceLocation BOGGED_SKELETON_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/skeleton/bogged.png");
    private static final ResourceLocation BOGGED_OUTER_LAYER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/skeleton/bogged_overlay.png");

    public BoggedRenderer(EntityRendererProvider.Context context) {
        super(context, ModelLayers.BOGGED_INNER_ARMOR, ModelLayers.BOGGED_OUTER_ARMOR, new BoggedModel(context.bakeLayer(ModelLayers.BOGGED)));
        this.addLayer(new SkeletonClothingLayer<BoggedRenderState, SkeletonModel<BoggedRenderState>>(this, context.getModelSet(), ModelLayers.BOGGED_OUTER_LAYER, BOGGED_OUTER_LAYER_LOCATION));
    }

    @Override
    public ResourceLocation getTextureLocation(BoggedRenderState boggedRenderState) {
        return BOGGED_SKELETON_LOCATION;
    }

    @Override
    public BoggedRenderState createRenderState() {
        return new BoggedRenderState();
    }

    @Override
    public void extractRenderState(Bogged bogged, BoggedRenderState boggedRenderState, float f) {
        super.extractRenderState(bogged, boggedRenderState, f);
        boggedRenderState.isSheared = bogged.isSheared();
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((BoggedRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

