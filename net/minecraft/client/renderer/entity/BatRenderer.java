/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.BatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.BatRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ambient.Bat;

public class BatRenderer
extends MobRenderer<Bat, BatRenderState, BatModel> {
    private static final ResourceLocation BAT_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/bat.png");

    public BatRenderer(EntityRendererProvider.Context context) {
        super(context, new BatModel(context.bakeLayer(ModelLayers.BAT)), 0.25f);
    }

    @Override
    public ResourceLocation getTextureLocation(BatRenderState batRenderState) {
        return BAT_LOCATION;
    }

    @Override
    public BatRenderState createRenderState() {
        return new BatRenderState();
    }

    @Override
    public void extractRenderState(Bat bat, BatRenderState batRenderState, float f) {
        super.extractRenderState(bat, batRenderState, f);
        batRenderState.isResting = bat.isResting();
        batRenderState.flyAnimationState.copyFrom(bat.flyAnimationState);
        batRenderState.restAnimationState.copyFrom(bat.restAnimationState);
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((BatRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

