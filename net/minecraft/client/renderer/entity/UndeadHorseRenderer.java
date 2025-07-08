/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.AbstractEquineModel;
import net.minecraft.client.model.EquineSaddleModel;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.SimpleEquipmentLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public class UndeadHorseRenderer
extends AbstractHorseRenderer<AbstractHorse, EquineRenderState, AbstractEquineModel<EquineRenderState>> {
    private final ResourceLocation texture;

    public UndeadHorseRenderer(EntityRendererProvider.Context context, Type type) {
        super(context, new HorseModel(context.bakeLayer(type.model)), new HorseModel(context.bakeLayer(type.babyModel)));
        this.texture = type.texture;
        this.addLayer(new SimpleEquipmentLayer<EquineRenderState, AbstractEquineModel<EquineRenderState>, EquineSaddleModel>(this, context.getEquipmentRenderer(), type.saddleLayer, equineRenderState -> equineRenderState.saddle, new EquineSaddleModel(context.bakeLayer(type.saddleModel)), new EquineSaddleModel(context.bakeLayer(type.babySaddleModel))));
    }

    @Override
    public ResourceLocation getTextureLocation(EquineRenderState equineRenderState) {
        return this.texture;
    }

    @Override
    public EquineRenderState createRenderState() {
        return new EquineRenderState();
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((EquineRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }

    public static enum Type {
        SKELETON(ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_skeleton.png"), ModelLayers.SKELETON_HORSE, ModelLayers.SKELETON_HORSE_BABY, EquipmentClientInfo.LayerType.SKELETON_HORSE_SADDLE, ModelLayers.SKELETON_HORSE_SADDLE, ModelLayers.SKELETON_HORSE_BABY_SADDLE),
        ZOMBIE(ResourceLocation.withDefaultNamespace("textures/entity/horse/horse_zombie.png"), ModelLayers.ZOMBIE_HORSE, ModelLayers.ZOMBIE_HORSE_BABY, EquipmentClientInfo.LayerType.ZOMBIE_HORSE_SADDLE, ModelLayers.ZOMBIE_HORSE_SADDLE, ModelLayers.ZOMBIE_HORSE_BABY_SADDLE);

        final ResourceLocation texture;
        final ModelLayerLocation model;
        final ModelLayerLocation babyModel;
        final EquipmentClientInfo.LayerType saddleLayer;
        final ModelLayerLocation saddleModel;
        final ModelLayerLocation babySaddleModel;

        private Type(ResourceLocation resourceLocation, ModelLayerLocation modelLayerLocation, ModelLayerLocation modelLayerLocation2, EquipmentClientInfo.LayerType layerType, ModelLayerLocation modelLayerLocation3, ModelLayerLocation modelLayerLocation4) {
            this.texture = resourceLocation;
            this.model = modelLayerLocation;
            this.babyModel = modelLayerLocation2;
            this.saddleLayer = layerType;
            this.saddleModel = modelLayerLocation3;
            this.babySaddleModel = modelLayerLocation4;
        }
    }
}

