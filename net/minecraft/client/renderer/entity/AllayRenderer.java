/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.AllayModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.AllayRenderState;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.allay.Allay;

public class AllayRenderer
extends MobRenderer<Allay, AllayRenderState, AllayModel> {
    private static final ResourceLocation ALLAY_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/allay/allay.png");

    public AllayRenderer(EntityRendererProvider.Context context) {
        super(context, new AllayModel(context.bakeLayer(ModelLayers.ALLAY)), 0.4f);
        this.addLayer(new ItemInHandLayer<AllayRenderState, AllayModel>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(AllayRenderState allayRenderState) {
        return ALLAY_TEXTURE;
    }

    @Override
    public AllayRenderState createRenderState() {
        return new AllayRenderState();
    }

    @Override
    public void extractRenderState(Allay allay, AllayRenderState allayRenderState, float f) {
        super.extractRenderState(allay, allayRenderState, f);
        ArmedEntityRenderState.extractArmedEntityRenderState(allay, allayRenderState, this.itemModelResolver);
        allayRenderState.isDancing = allay.isDancing();
        allayRenderState.isSpinning = allay.isSpinning();
        allayRenderState.spinningProgress = allay.getSpinningProgress(f);
        allayRenderState.holdingAnimationProgress = allay.getHoldingItemAnimationProgress(f);
    }

    @Override
    protected int getBlockLightLevel(Allay allay, BlockPos blockPos) {
        return 15;
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((AllayRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

