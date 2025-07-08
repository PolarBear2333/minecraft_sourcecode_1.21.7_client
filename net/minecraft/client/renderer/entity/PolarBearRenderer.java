/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.PolarBearModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PolarBearRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.PolarBear;

public class PolarBearRenderer
extends AgeableMobRenderer<PolarBear, PolarBearRenderState, PolarBearModel> {
    private static final ResourceLocation BEAR_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/bear/polarbear.png");

    public PolarBearRenderer(EntityRendererProvider.Context context) {
        super(context, new PolarBearModel(context.bakeLayer(ModelLayers.POLAR_BEAR)), new PolarBearModel(context.bakeLayer(ModelLayers.POLAR_BEAR_BABY)), 0.9f);
    }

    @Override
    public ResourceLocation getTextureLocation(PolarBearRenderState polarBearRenderState) {
        return BEAR_LOCATION;
    }

    @Override
    public PolarBearRenderState createRenderState() {
        return new PolarBearRenderState();
    }

    @Override
    public void extractRenderState(PolarBear polarBear, PolarBearRenderState polarBearRenderState, float f) {
        super.extractRenderState(polarBear, polarBearRenderState, f);
        polarBearRenderState.standScale = polarBear.getStandingAnimationScale(f);
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((PolarBearRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

