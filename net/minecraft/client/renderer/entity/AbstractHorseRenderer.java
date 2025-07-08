/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EquineRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;

public abstract class AbstractHorseRenderer<T extends AbstractHorse, S extends EquineRenderState, M extends EntityModel<? super S>>
extends AgeableMobRenderer<T, S, M> {
    public AbstractHorseRenderer(EntityRendererProvider.Context context, M m, M m2) {
        super(context, m, m2, 0.75f);
    }

    @Override
    public void extractRenderState(T t, S s, float f) {
        super.extractRenderState(t, s, f);
        ((EquineRenderState)s).saddle = ((LivingEntity)t).getItemBySlot(EquipmentSlot.SADDLE).copy();
        ((EquineRenderState)s).isRidden = ((Entity)t).isVehicle();
        ((EquineRenderState)s).eatAnimation = ((AbstractHorse)t).getEatAnim(f);
        ((EquineRenderState)s).standAnimation = ((AbstractHorse)t).getStandAnim(f);
        ((EquineRenderState)s).feedingAnimation = ((AbstractHorse)t).getMouthAnim(f);
        ((EquineRenderState)s).animateTail = ((AbstractHorse)t).tailCounter > 0;
    }
}

