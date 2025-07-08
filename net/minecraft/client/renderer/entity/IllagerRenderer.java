/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.item.CrossbowItem;

public abstract class IllagerRenderer<T extends AbstractIllager, S extends IllagerRenderState>
extends MobRenderer<T, S, IllagerModel<S>> {
    protected IllagerRenderer(EntityRendererProvider.Context context, IllagerModel<S> illagerModel, float f) {
        super(context, illagerModel, f);
        this.addLayer(new CustomHeadLayer(this, context.getModelSet()));
    }

    @Override
    public void extractRenderState(T t, S s, float f) {
        super.extractRenderState(t, s, f);
        ArmedEntityRenderState.extractArmedEntityRenderState(t, s, this.itemModelResolver);
        ((IllagerRenderState)s).isRiding = ((Entity)t).isPassenger();
        ((IllagerRenderState)s).mainArm = ((Mob)t).getMainArm();
        ((IllagerRenderState)s).armPose = ((AbstractIllager)t).getArmPose();
        ((IllagerRenderState)s).maxCrossbowChargeDuration = ((IllagerRenderState)s).armPose == AbstractIllager.IllagerArmPose.CROSSBOW_CHARGE ? CrossbowItem.getChargeDuration(((LivingEntity)t).getUseItem(), t) : 0;
        ((IllagerRenderState)s).ticksUsingItem = ((LivingEntity)t).getTicksUsingItem();
        ((IllagerRenderState)s).attackAnim = ((LivingEntity)t).getAttackAnim(f);
        ((IllagerRenderState)s).isAggressive = ((Mob)t).isAggressive();
    }
}

