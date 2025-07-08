/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ZombieVillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.ZombieVillagerRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.ZombieVillager;

public class ZombieVillagerRenderer
extends HumanoidMobRenderer<ZombieVillager, ZombieVillagerRenderState, ZombieVillagerModel<ZombieVillagerRenderState>> {
    private static final ResourceLocation ZOMBIE_VILLAGER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/zombie_villager/zombie_villager.png");

    public ZombieVillagerRenderer(EntityRendererProvider.Context context) {
        super(context, new ZombieVillagerModel(context.bakeLayer(ModelLayers.ZOMBIE_VILLAGER)), new ZombieVillagerModel(context.bakeLayer(ModelLayers.ZOMBIE_VILLAGER_BABY)), 0.5f, VillagerRenderer.CUSTOM_HEAD_TRANSFORMS);
        this.addLayer(new HumanoidArmorLayer(this, new ZombieVillagerModel(context.bakeLayer(ModelLayers.ZOMBIE_VILLAGER_INNER_ARMOR)), new ZombieVillagerModel(context.bakeLayer(ModelLayers.ZOMBIE_VILLAGER_OUTER_ARMOR)), new ZombieVillagerModel(context.bakeLayer(ModelLayers.ZOMBIE_VILLAGER_BABY_INNER_ARMOR)), new ZombieVillagerModel(context.bakeLayer(ModelLayers.ZOMBIE_VILLAGER_BABY_OUTER_ARMOR)), context.getEquipmentRenderer()));
        this.addLayer(new VillagerProfessionLayer<ZombieVillagerRenderState, ZombieVillagerModel<ZombieVillagerRenderState>>(this, context.getResourceManager(), "zombie_villager"));
    }

    @Override
    public ResourceLocation getTextureLocation(ZombieVillagerRenderState zombieVillagerRenderState) {
        return ZOMBIE_VILLAGER_LOCATION;
    }

    @Override
    public ZombieVillagerRenderState createRenderState() {
        return new ZombieVillagerRenderState();
    }

    @Override
    public void extractRenderState(ZombieVillager zombieVillager, ZombieVillagerRenderState zombieVillagerRenderState, float f) {
        super.extractRenderState(zombieVillager, zombieVillagerRenderState, f);
        zombieVillagerRenderState.isConverting = zombieVillager.isConverting();
        zombieVillagerRenderState.villagerData = zombieVillager.getVillagerData();
        zombieVillagerRenderState.isAggressive = zombieVillager.isAggressive();
    }

    @Override
    protected boolean isShaking(ZombieVillagerRenderState zombieVillagerRenderState) {
        return super.isShaking(zombieVillagerRenderState) || zombieVillagerRenderState.isConverting;
    }

    @Override
    protected /* synthetic */ boolean isShaking(LivingEntityRenderState livingEntityRenderState) {
        return this.isShaking((ZombieVillagerRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((ZombieVillagerRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

