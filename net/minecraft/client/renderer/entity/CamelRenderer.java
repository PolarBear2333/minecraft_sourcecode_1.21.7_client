/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.CamelModel;
import net.minecraft.client.model.CamelSaddleModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.SimpleEquipmentLayer;
import net.minecraft.client.renderer.entity.state.CamelRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.camel.Camel;

public class CamelRenderer
extends AgeableMobRenderer<Camel, CamelRenderState, CamelModel> {
    private static final ResourceLocation CAMEL_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/camel/camel.png");

    public CamelRenderer(EntityRendererProvider.Context context) {
        super(context, new CamelModel(context.bakeLayer(ModelLayers.CAMEL)), new CamelModel(context.bakeLayer(ModelLayers.CAMEL_BABY)), 0.7f);
        this.addLayer(new SimpleEquipmentLayer<CamelRenderState, CamelModel, CamelSaddleModel>(this, context.getEquipmentRenderer(), EquipmentClientInfo.LayerType.CAMEL_SADDLE, camelRenderState -> camelRenderState.saddle, new CamelSaddleModel(context.bakeLayer(ModelLayers.CAMEL_SADDLE)), new CamelSaddleModel(context.bakeLayer(ModelLayers.CAMEL_BABY_SADDLE))));
    }

    @Override
    public ResourceLocation getTextureLocation(CamelRenderState camelRenderState) {
        return CAMEL_LOCATION;
    }

    @Override
    public CamelRenderState createRenderState() {
        return new CamelRenderState();
    }

    @Override
    public void extractRenderState(Camel camel, CamelRenderState camelRenderState, float f) {
        super.extractRenderState(camel, camelRenderState, f);
        camelRenderState.saddle = camel.getItemBySlot(EquipmentSlot.SADDLE).copy();
        camelRenderState.isRidden = camel.isVehicle();
        camelRenderState.jumpCooldown = Math.max((float)camel.getJumpCooldown() - f, 0.0f);
        camelRenderState.sitAnimationState.copyFrom(camel.sitAnimationState);
        camelRenderState.sitPoseAnimationState.copyFrom(camel.sitPoseAnimationState);
        camelRenderState.sitUpAnimationState.copyFrom(camel.sitUpAnimationState);
        camelRenderState.idleAnimationState.copyFrom(camel.idleAnimationState);
        camelRenderState.dashAnimationState.copyFrom(camel.dashAnimationState);
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((CamelRenderState)livingEntityRenderState);
    }

    @Override
    public /* synthetic */ EntityRenderState createRenderState() {
        return this.createRenderState();
    }
}

