/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.WardenModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.LivingEntityEmissiveLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.WardenRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.warden.Warden;

public class WardenRenderer
extends MobRenderer<Warden, WardenRenderState, WardenModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/warden/warden.png");
    private static final ResourceLocation BIOLUMINESCENT_LAYER_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/warden/warden_bioluminescent_layer.png");
    private static final ResourceLocation HEART_TEXTURE = ResourceLocation.withDefaultNamespace("textures/entity/warden/warden_heart.png");
    private static final ResourceLocation PULSATING_SPOTS_TEXTURE_1 = ResourceLocation.withDefaultNamespace("textures/entity/warden/warden_pulsating_spots_1.png");
    private static final ResourceLocation PULSATING_SPOTS_TEXTURE_2 = ResourceLocation.withDefaultNamespace("textures/entity/warden/warden_pulsating_spots_2.png");

    public WardenRenderer(EntityRendererProvider.Context context) {
        super(context, new WardenModel(context.bakeLayer(ModelLayers.WARDEN)), 0.9f);
        this.addLayer(new LivingEntityEmissiveLayer<WardenRenderState, WardenModel>(this, BIOLUMINESCENT_LAYER_TEXTURE, (wardenRenderState, f) -> 1.0f, WardenModel::getBioluminescentLayerModelParts, RenderType::entityTranslucentEmissive, false));
        this.addLayer(new LivingEntityEmissiveLayer<WardenRenderState, WardenModel>(this, PULSATING_SPOTS_TEXTURE_1, (wardenRenderState, f) -> Math.max(0.0f, Mth.cos(f * 0.045f) * 0.25f), WardenModel::getPulsatingSpotsLayerModelParts, RenderType::entityTranslucentEmissive, false));
        this.addLayer(new LivingEntityEmissiveLayer<WardenRenderState, WardenModel>(this, PULSATING_SPOTS_TEXTURE_2, (wardenRenderState, f) -> Math.max(0.0f, Mth.cos(f * 0.045f + (float)Math.PI) * 0.25f), WardenModel::getPulsatingSpotsLayerModelParts, RenderType::entityTranslucentEmissive, false));
        this.addLayer(new LivingEntityEmissiveLayer<WardenRenderState, WardenModel>(this, TEXTURE, (wardenRenderState, f) -> wardenRenderState.tendrilAnimation, WardenModel::getTendrilsLayerModelParts, RenderType::entityTranslucentEmissive, false));
        this.addLayer(new LivingEntityEmissiveLayer<WardenRenderState, WardenModel>(this, HEART_TEXTURE, (wardenRenderState, f) -> wardenRenderState.heartAnimation, WardenModel::getHeartLayerModelParts, RenderType::entityTranslucentEmissive, false));
    }

    @Override
    public ResourceLocation getTextureLocation(WardenRenderState wardenRenderState) {
        return TEXTURE;
    }

    @Override
    public WardenRenderState createRenderState() {
        return new WardenRenderState();
    }

    @Override
    public void extractRenderState(Warden warden, WardenRenderState wardenRenderState, float f) {
        super.extractRenderState(warden, wardenRenderState, f);
        wardenRenderState.tendrilAnimation = warden.getTendrilAnimation(f);
        wardenRenderState.heartAnimation = warden.getHeartAnimation(f);
        wardenRenderState.roarAnimationState.copyFrom(warden.roarAnimationState);
        wardenRenderState.sniffAnimationState.copyFrom(warden.sniffAnimationState);
        wardenRenderState.emergeAnimationState.copyFrom(warden.emergeAnimationState);
        wardenRenderState.diggingAnimationState.copyFrom(warden.diggingAnimationState);
        wardenRenderState.attackAnimationState.copyFrom(warden.attackAnimationState);
        wardenRenderState.sonicBoomAnimationState.copyFrom(warden.sonicBoomAnimationState);
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((WardenRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

