/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.DonkeyModel;
import net.minecraft.client.model.EquineSaddleModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AbstractHorseRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.SimpleEquipmentLayer;
import net.minecraft.client.renderer.entity.state.DonkeyRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;

public class DonkeyRenderer<T extends AbstractChestedHorse>
extends AbstractHorseRenderer<T, DonkeyRenderState, DonkeyModel> {
    private final ResourceLocation texture;

    public DonkeyRenderer(EntityRendererProvider.Context context, Type type) {
        super(context, new DonkeyModel(context.bakeLayer(type.model)), new DonkeyModel(context.bakeLayer(type.babyModel)));
        this.texture = type.texture;
        this.addLayer(new SimpleEquipmentLayer<DonkeyRenderState, DonkeyModel, EquineSaddleModel>(this, context.getEquipmentRenderer(), type.saddleLayer, donkeyRenderState -> donkeyRenderState.saddle, new EquineSaddleModel(context.bakeLayer(type.saddleModel)), new EquineSaddleModel(context.bakeLayer(type.babySaddleModel))));
    }

    @Override
    public ResourceLocation getTextureLocation(DonkeyRenderState donkeyRenderState) {
        return this.texture;
    }

    @Override
    public DonkeyRenderState createRenderState() {
        return new DonkeyRenderState();
    }

    @Override
    public void extractRenderState(T t, DonkeyRenderState donkeyRenderState, float f) {
        super.extractRenderState(t, donkeyRenderState, f);
        donkeyRenderState.hasChest = ((AbstractChestedHorse)t).hasChest();
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((DonkeyRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }

    public static enum Type {
        DONKEY(ResourceLocation.withDefaultNamespace("textures/entity/horse/donkey.png"), ModelLayers.DONKEY, ModelLayers.DONKEY_BABY, EquipmentClientInfo.LayerType.DONKEY_SADDLE, ModelLayers.DONKEY_SADDLE, ModelLayers.DONKEY_BABY_SADDLE),
        MULE(ResourceLocation.withDefaultNamespace("textures/entity/horse/mule.png"), ModelLayers.MULE, ModelLayers.MULE_BABY, EquipmentClientInfo.LayerType.MULE_SADDLE, ModelLayers.MULE_SADDLE, ModelLayers.MULE_BABY_SADDLE);

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

