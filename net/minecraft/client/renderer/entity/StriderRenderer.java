/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.StriderModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.SimpleEquipmentLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.StriderRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Strider;

public class StriderRenderer
extends AgeableMobRenderer<Strider, StriderRenderState, StriderModel> {
    private static final ResourceLocation STRIDER_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/strider/strider.png");
    private static final ResourceLocation COLD_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/strider/strider_cold.png");
    private static final float SHADOW_RADIUS = 0.5f;

    public StriderRenderer(EntityRendererProvider.Context context) {
        super(context, new StriderModel(context.bakeLayer(ModelLayers.STRIDER)), new StriderModel(context.bakeLayer(ModelLayers.STRIDER_BABY)), 0.5f);
        this.addLayer(new SimpleEquipmentLayer<StriderRenderState, StriderModel, StriderModel>(this, context.getEquipmentRenderer(), EquipmentClientInfo.LayerType.STRIDER_SADDLE, striderRenderState -> striderRenderState.saddle, new StriderModel(context.bakeLayer(ModelLayers.STRIDER_SADDLE)), new StriderModel(context.bakeLayer(ModelLayers.STRIDER_BABY_SADDLE))));
    }

    @Override
    public ResourceLocation getTextureLocation(StriderRenderState striderRenderState) {
        return striderRenderState.isSuffocating ? COLD_LOCATION : STRIDER_LOCATION;
    }

    @Override
    protected float getShadowRadius(StriderRenderState striderRenderState) {
        float f = super.getShadowRadius(striderRenderState);
        if (striderRenderState.isBaby) {
            return f * 0.5f;
        }
        return f;
    }

    @Override
    public StriderRenderState createRenderState() {
        return new StriderRenderState();
    }

    @Override
    public void extractRenderState(Strider strider, StriderRenderState striderRenderState, float f) {
        super.extractRenderState(strider, striderRenderState, f);
        striderRenderState.saddle = strider.getItemBySlot(EquipmentSlot.SADDLE).copy();
        striderRenderState.isSuffocating = strider.isSuffocating();
        striderRenderState.isRidden = strider.isVehicle();
    }

    @Override
    protected boolean isShaking(StriderRenderState striderRenderState) {
        return super.isShaking(striderRenderState) || striderRenderState.isSuffocating;
    }

    @Override
    protected /* synthetic */ float getShadowRadius(LivingEntityRenderState livingEntityRenderState) {
        return this.getShadowRadius((StriderRenderState)livingEntityRenderState);
    }

    @Override
    protected /* synthetic */ boolean isShaking(LivingEntityRenderState livingEntityRenderState) {
        return this.isShaking((StriderRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((StriderRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }

    @Override
    protected /* synthetic */ float getShadowRadius(EntityRenderState entityRenderState) {
        return this.getShadowRadius((StriderRenderState)entityRenderState);
    }
}

