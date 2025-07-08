/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ArrowRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.SpectralArrow;

public class SpectralArrowRenderer
extends ArrowRenderer<SpectralArrow, ArrowRenderState> {
    public static final ResourceLocation SPECTRAL_ARROW_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/projectiles/spectral_arrow.png");

    public SpectralArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected ResourceLocation getTextureLocation(ArrowRenderState arrowRenderState) {
        return SPECTRAL_ARROW_LOCATION;
    }

    @Override
    public ArrowRenderState createRenderState() {
        return new ArrowRenderState();
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

