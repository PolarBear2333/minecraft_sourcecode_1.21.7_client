/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.TippableArrowRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.projectile.Arrow;

public class TippableArrowRenderer
extends ArrowRenderer<Arrow, TippableArrowRenderState> {
    public static final ResourceLocation NORMAL_ARROW_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/projectiles/arrow.png");
    public static final ResourceLocation TIPPED_ARROW_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/projectiles/tipped_arrow.png");

    public TippableArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected ResourceLocation getTextureLocation(TippableArrowRenderState tippableArrowRenderState) {
        return tippableArrowRenderState.isTipped ? TIPPED_ARROW_LOCATION : NORMAL_ARROW_LOCATION;
    }

    @Override
    public TippableArrowRenderState createRenderState() {
        return new TippableArrowRenderState();
    }

    @Override
    public void extractRenderState(Arrow arrow, TippableArrowRenderState tippableArrowRenderState, float f) {
        super.extractRenderState(arrow, tippableArrowRenderState, f);
        tippableArrowRenderState.isTipped = arrow.getColor() > 0;
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

