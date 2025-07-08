/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.CrossedArmsItemLayer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HoldingEntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.npc.Villager;

public class VillagerRenderer
extends AgeableMobRenderer<Villager, VillagerRenderState, VillagerModel> {
    private static final ResourceLocation VILLAGER_BASE_SKIN = ResourceLocation.withDefaultNamespace("textures/entity/villager/villager.png");
    public static final CustomHeadLayer.Transforms CUSTOM_HEAD_TRANSFORMS = new CustomHeadLayer.Transforms(-0.1171875f, -0.07421875f, 1.0f);

    public VillagerRenderer(EntityRendererProvider.Context context) {
        super(context, new VillagerModel(context.bakeLayer(ModelLayers.VILLAGER)), new VillagerModel(context.bakeLayer(ModelLayers.VILLAGER_BABY)), 0.5f);
        this.addLayer(new CustomHeadLayer<VillagerRenderState, VillagerModel>(this, context.getModelSet(), CUSTOM_HEAD_TRANSFORMS));
        this.addLayer(new VillagerProfessionLayer<VillagerRenderState, VillagerModel>(this, context.getResourceManager(), "villager"));
        this.addLayer(new CrossedArmsItemLayer<VillagerRenderState, VillagerModel>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(VillagerRenderState villagerRenderState) {
        return VILLAGER_BASE_SKIN;
    }

    @Override
    protected float getShadowRadius(VillagerRenderState villagerRenderState) {
        float f = super.getShadowRadius(villagerRenderState);
        if (villagerRenderState.isBaby) {
            return f * 0.5f;
        }
        return f;
    }

    @Override
    public VillagerRenderState createRenderState() {
        return new VillagerRenderState();
    }

    @Override
    public void extractRenderState(Villager villager, VillagerRenderState villagerRenderState, float f) {
        super.extractRenderState(villager, villagerRenderState, f);
        HoldingEntityRenderState.extractHoldingEntityRenderState(villager, villagerRenderState, this.itemModelResolver);
        villagerRenderState.isUnhappy = villager.getUnhappyCounter() > 0;
        villagerRenderState.villagerData = villager.getVillagerData();
    }

    @Override
    protected /* synthetic */ float getShadowRadius(LivingEntityRenderState livingEntityRenderState) {
        return this.getShadowRadius((VillagerRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((VillagerRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }

    @Override
    protected /* synthetic */ float getShadowRadius(EntityRenderState entityRenderState) {
        return this.getShadowRadius((VillagerRenderState)entityRenderState);
    }
}

