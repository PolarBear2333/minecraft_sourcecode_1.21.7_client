/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.CreakingModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.LivingEntityEmissiveLayer;
import net.minecraft.client.renderer.entity.state.CreakingRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.creaking.Creaking;

public class CreakingRenderer<T extends Creaking>
extends MobRenderer<T, CreakingRenderState, CreakingModel> {
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/creaking/creaking.png");
    private static final ResourceLocation EYES_TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/creaking/creaking_eyes.png");

    public CreakingRenderer(EntityRendererProvider.Context context) {
        super(context, new CreakingModel(context.bakeLayer(ModelLayers.CREAKING)), 0.6f);
        this.addLayer(new LivingEntityEmissiveLayer<CreakingRenderState, CreakingModel>(this, EYES_TEXTURE_LOCATION, (creakingRenderState, f) -> 1.0f, CreakingModel::getHeadModelParts, RenderType::eyes, true));
    }

    @Override
    public ResourceLocation getTextureLocation(CreakingRenderState creakingRenderState) {
        return TEXTURE_LOCATION;
    }

    @Override
    public CreakingRenderState createRenderState() {
        return new CreakingRenderState();
    }

    @Override
    public void extractRenderState(T t, CreakingRenderState creakingRenderState, float f) {
        super.extractRenderState(t, creakingRenderState, f);
        creakingRenderState.attackAnimationState.copyFrom(((Creaking)t).attackAnimationState);
        creakingRenderState.invulnerabilityAnimationState.copyFrom(((Creaking)t).invulnerabilityAnimationState);
        creakingRenderState.deathAnimationState.copyFrom(((Creaking)t).deathAnimationState);
        if (((Creaking)t).isTearingDown()) {
            creakingRenderState.deathTime = 0.0f;
            creakingRenderState.hasRedOverlay = false;
            creakingRenderState.eyesGlowing = ((Creaking)t).hasGlowingEyes();
        } else {
            creakingRenderState.eyesGlowing = ((Creaking)t).isActive();
        }
        creakingRenderState.canMove = ((Creaking)t).canMove();
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((CreakingRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

