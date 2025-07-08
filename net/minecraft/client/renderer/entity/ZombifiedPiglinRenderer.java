/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.ZombifiedPiglinModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.PiglinRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.ZombifiedPiglinRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.ZombifiedPiglin;

public class ZombifiedPiglinRenderer
extends HumanoidMobRenderer<ZombifiedPiglin, ZombifiedPiglinRenderState, ZombifiedPiglinModel> {
    private static final ResourceLocation ZOMBIFIED_PIGLIN_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/piglin/zombified_piglin.png");

    public ZombifiedPiglinRenderer(EntityRendererProvider.Context context, ModelLayerLocation modelLayerLocation, ModelLayerLocation modelLayerLocation2, ModelLayerLocation modelLayerLocation3, ModelLayerLocation modelLayerLocation4, ModelLayerLocation modelLayerLocation5, ModelLayerLocation modelLayerLocation6) {
        super(context, new ZombifiedPiglinModel(context.bakeLayer(modelLayerLocation)), new ZombifiedPiglinModel(context.bakeLayer(modelLayerLocation2)), 0.5f, PiglinRenderer.PIGLIN_CUSTOM_HEAD_TRANSFORMS);
        this.addLayer(new HumanoidArmorLayer(this, new HumanoidArmorModel(context.bakeLayer(modelLayerLocation3)), new HumanoidArmorModel(context.bakeLayer(modelLayerLocation4)), new HumanoidArmorModel(context.bakeLayer(modelLayerLocation5)), new HumanoidArmorModel(context.bakeLayer(modelLayerLocation6)), context.getEquipmentRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(ZombifiedPiglinRenderState zombifiedPiglinRenderState) {
        return ZOMBIFIED_PIGLIN_LOCATION;
    }

    @Override
    public ZombifiedPiglinRenderState createRenderState() {
        return new ZombifiedPiglinRenderState();
    }

    @Override
    public void extractRenderState(ZombifiedPiglin zombifiedPiglin, ZombifiedPiglinRenderState zombifiedPiglinRenderState, float f) {
        super.extractRenderState(zombifiedPiglin, zombifiedPiglinRenderState, f);
        zombifiedPiglinRenderState.isAggressive = zombifiedPiglin.isAggressive();
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((ZombifiedPiglinRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

