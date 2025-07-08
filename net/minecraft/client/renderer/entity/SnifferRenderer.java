/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.SnifferModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.SnifferRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.sniffer.Sniffer;
import net.minecraft.world.phys.AABB;

public class SnifferRenderer
extends AgeableMobRenderer<Sniffer, SnifferRenderState, SnifferModel> {
    private static final ResourceLocation SNIFFER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/sniffer/sniffer.png");

    public SnifferRenderer(EntityRendererProvider.Context context) {
        super(context, new SnifferModel(context.bakeLayer(ModelLayers.SNIFFER)), new SnifferModel(context.bakeLayer(ModelLayers.SNIFFER_BABY)), 1.1f);
    }

    @Override
    public ResourceLocation getTextureLocation(SnifferRenderState snifferRenderState) {
        return SNIFFER_LOCATION;
    }

    @Override
    public SnifferRenderState createRenderState() {
        return new SnifferRenderState();
    }

    @Override
    public void extractRenderState(Sniffer sniffer, SnifferRenderState snifferRenderState, float f) {
        super.extractRenderState(sniffer, snifferRenderState, f);
        snifferRenderState.isSearching = sniffer.isSearching();
        snifferRenderState.diggingAnimationState.copyFrom(sniffer.diggingAnimationState);
        snifferRenderState.sniffingAnimationState.copyFrom(sniffer.sniffingAnimationState);
        snifferRenderState.risingAnimationState.copyFrom(sniffer.risingAnimationState);
        snifferRenderState.feelingHappyAnimationState.copyFrom(sniffer.feelingHappyAnimationState);
        snifferRenderState.scentingAnimationState.copyFrom(sniffer.scentingAnimationState);
    }

    @Override
    protected AABB getBoundingBoxForCulling(Sniffer sniffer) {
        return super.getBoundingBoxForCulling(sniffer).inflate(0.6f);
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((SnifferRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

