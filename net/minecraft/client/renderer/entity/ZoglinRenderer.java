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
import net.minecraft.world.entity.monster.Zoglin;

public class ZoglinRenderer
extends AbstractHoglinRenderer<Zoglin> {
    private static final ResourceLocation ZOGLIN_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/hoglin/zoglin.png");

    public ZoglinRenderer(EntityRendererProvider.Context context) {
        super(context, ModelLayers.ZOGLIN, ModelLayers.ZOGLIN_BABY, 0.7f);
    }

    @Override
    public ResourceLocation getTextureLocation(HoglinRenderState hoglinRenderState) {
        return ZOGLIN_LOCATION;
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((HoglinRenderState)livingEntityRenderState);
    }
}

