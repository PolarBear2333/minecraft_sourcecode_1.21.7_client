/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;

public abstract class HumanoidMobRenderer<T extends Mob, S extends HumanoidRenderState, M extends HumanoidModel<S>>
extends AgeableMobRenderer<T, S, M> {
    public HumanoidMobRenderer(EntityRendererProvider.Context context, M m, float f) {
        this(context, m, m, f);
    }

    public HumanoidMobRenderer(EntityRendererProvider.Context context, M m, M m2, float f) {
        this(context, m, m2, f, CustomHeadLayer.Transforms.DEFAULT);
    }

    public HumanoidMobRenderer(EntityRendererProvider.Context context, M m, M m2, float f, CustomHeadLayer.Transforms transforms) {
        super(context, m, m2, f);
        this.addLayer(new CustomHeadLayer(this, context.getModelSet(), transforms));
        this.addLayer(new WingsLayer(this, context.getModelSet(), context.getEquipmentRenderer()));
        this.addLayer(new ItemInHandLayer(this));
    }

    protected HumanoidModel.ArmPose getArmPose(T t, HumanoidArm humanoidArm) {
        return HumanoidModel.ArmPose.EMPTY;
    }

    @Override
    public void extractRenderState(T t, S s, float f) {
        super.extractRenderState(t, s, f);
        HumanoidMobRenderer.extractHumanoidRenderState(t, s, f, this.itemModelResolver);
        ((HumanoidRenderState)s).leftArmPose = this.getArmPose(t, HumanoidArm.LEFT);
        ((HumanoidRenderState)s).rightArmPose = this.getArmPose(t, HumanoidArm.RIGHT);
    }

    public static void extractHumanoidRenderState(LivingEntity livingEntity, HumanoidRenderState humanoidRenderState, float f, ItemModelResolver itemModelResolver) {
        ArmedEntityRenderState.extractArmedEntityRenderState(livingEntity, humanoidRenderState, itemModelResolver);
        humanoidRenderState.isCrouching = livingEntity.isCrouching();
        humanoidRenderState.isFallFlying = livingEntity.isFallFlying();
        humanoidRenderState.isVisuallySwimming = livingEntity.isVisuallySwimming();
        humanoidRenderState.isPassenger = livingEntity.isPassenger();
        humanoidRenderState.speedValue = 1.0f;
        if (humanoidRenderState.isFallFlying) {
            humanoidRenderState.speedValue = (float)livingEntity.getDeltaMovement().lengthSqr();
            humanoidRenderState.speedValue /= 0.2f;
            humanoidRenderState.speedValue *= humanoidRenderState.speedValue * humanoidRenderState.speedValue;
        }
        if (humanoidRenderState.speedValue < 1.0f) {
            humanoidRenderState.speedValue = 1.0f;
        }
        humanoidRenderState.attackTime = livingEntity.getAttackAnim(f);
        humanoidRenderState.swimAmount = livingEntity.getSwimAmount(f);
        humanoidRenderState.attackArm = HumanoidMobRenderer.getAttackArm(livingEntity);
        humanoidRenderState.useItemHand = livingEntity.getUsedItemHand();
        humanoidRenderState.maxCrossbowChargeDuration = CrossbowItem.getChargeDuration(livingEntity.getUseItem(), livingEntity);
        humanoidRenderState.ticksUsingItem = livingEntity.getTicksUsingItem();
        humanoidRenderState.isUsingItem = livingEntity.isUsingItem();
        humanoidRenderState.elytraRotX = livingEntity.elytraAnimationState.getRotX(f);
        humanoidRenderState.elytraRotY = livingEntity.elytraAnimationState.getRotY(f);
        humanoidRenderState.elytraRotZ = livingEntity.elytraAnimationState.getRotZ(f);
        humanoidRenderState.headEquipment = HumanoidMobRenderer.getEquipmentIfRenderable(livingEntity, EquipmentSlot.HEAD);
        humanoidRenderState.chestEquipment = HumanoidMobRenderer.getEquipmentIfRenderable(livingEntity, EquipmentSlot.CHEST);
        humanoidRenderState.legsEquipment = HumanoidMobRenderer.getEquipmentIfRenderable(livingEntity, EquipmentSlot.LEGS);
        humanoidRenderState.feetEquipment = HumanoidMobRenderer.getEquipmentIfRenderable(livingEntity, EquipmentSlot.FEET);
    }

    private static ItemStack getEquipmentIfRenderable(LivingEntity livingEntity, EquipmentSlot equipmentSlot) {
        ItemStack itemStack = livingEntity.getItemBySlot(equipmentSlot);
        return HumanoidArmorLayer.shouldRender(itemStack, equipmentSlot) ? itemStack.copy() : ItemStack.EMPTY;
    }

    private static HumanoidArm getAttackArm(LivingEntity livingEntity) {
        HumanoidArm humanoidArm = livingEntity.getMainArm();
        return livingEntity.swingingArm == InteractionHand.MAIN_HAND ? humanoidArm : humanoidArm.getOpposite();
    }
}

