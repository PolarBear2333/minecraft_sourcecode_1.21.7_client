/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.GiantZombieModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Giant;

public class GiantMobRenderer
extends MobRenderer<Giant, ZombieRenderState, HumanoidModel<ZombieRenderState>> {
    private static final ResourceLocation ZOMBIE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/zombie/zombie.png");

    public GiantMobRenderer(EntityRendererProvider.Context context, float f) {
        super(context, new GiantZombieModel(context.bakeLayer(ModelLayers.GIANT)), 0.5f * f);
        this.addLayer(new ItemInHandLayer<ZombieRenderState, HumanoidModel<ZombieRenderState>>(this));
        this.addLayer(new HumanoidArmorLayer<ZombieRenderState, HumanoidModel<ZombieRenderState>, GiantZombieModel>(this, new GiantZombieModel(context.bakeLayer(ModelLayers.GIANT_INNER_ARMOR)), new GiantZombieModel(context.bakeLayer(ModelLayers.GIANT_OUTER_ARMOR)), context.getEquipmentRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(ZombieRenderState zombieRenderState) {
        return ZOMBIE_LOCATION;
    }

    @Override
    public ZombieRenderState createRenderState() {
        return new ZombieRenderState();
    }

    @Override
    public void extractRenderState(Giant giant, ZombieRenderState zombieRenderState, float f) {
        super.extractRenderState(giant, zombieRenderState, f);
        HumanoidMobRenderer.extractHumanoidRenderState(giant, zombieRenderState, f, this.itemModelResolver);
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((ZombieRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

