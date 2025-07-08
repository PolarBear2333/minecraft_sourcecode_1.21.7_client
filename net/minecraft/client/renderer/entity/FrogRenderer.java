/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.FrogModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.FrogRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.frog.Frog;

public class FrogRenderer
extends MobRenderer<Frog, FrogRenderState, FrogModel> {
    public FrogRenderer(EntityRendererProvider.Context context) {
        super(context, new FrogModel(context.bakeLayer(ModelLayers.FROG)), 0.3f);
    }

    @Override
    public ResourceLocation getTextureLocation(FrogRenderState frogRenderState) {
        return frogRenderState.texture;
    }

    @Override
    public FrogRenderState createRenderState() {
        return new FrogRenderState();
    }

    @Override
    public void extractRenderState(Frog frog, FrogRenderState frogRenderState, float f) {
        super.extractRenderState(frog, frogRenderState, f);
        frogRenderState.isSwimming = frog.isInWater();
        frogRenderState.jumpAnimationState.copyFrom(frog.jumpAnimationState);
        frogRenderState.croakAnimationState.copyFrom(frog.croakAnimationState);
        frogRenderState.tongueAnimationState.copyFrom(frog.tongueAnimationState);
        frogRenderState.swimIdleAnimationState.copyFrom(frog.swimIdleAnimationState);
        frogRenderState.texture = frog.getVariant().value().assetInfo().texturePath();
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((FrogRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

