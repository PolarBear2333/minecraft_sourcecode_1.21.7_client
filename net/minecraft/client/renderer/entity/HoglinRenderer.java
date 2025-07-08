/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractHoglinRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.HoglinRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.hoglin.Hoglin;

public class HoglinRenderer
extends AbstractHoglinRenderer<Hoglin> {
    private static final ResourceLocation HOGLIN_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/hoglin/hoglin.png");

    public HoglinRenderer(EntityRendererProvider.Context context) {
        super(context, ModelLayers.HOGLIN, ModelLayers.HOGLIN_BABY, 0.7f);
    }

    @Override
    public ResourceLocation getTextureLocation(HoglinRenderState hoglinRenderState) {
        return HOGLIN_LOCATION;
    }

    @Override
    public void extractRenderState(Hoglin hoglin, HoglinRenderState hoglinRenderState, float f) {
        super.extractRenderState(hoglin, hoglinRenderState, f);
        hoglinRenderState.isConverting = hoglin.isConverting();
    }

    @Override
    protected boolean isShaking(HoglinRenderState hoglinRenderState) {
        return super.isShaking(hoglinRenderState) || hoglinRenderState.isConverting;
    }

    @Override
    protected /* synthetic */ boolean isShaking(LivingEntityRenderState livingEntityRenderState) {
        return this.isShaking((HoglinRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((HoglinRenderState)livingEntityRenderState);
    }
}

