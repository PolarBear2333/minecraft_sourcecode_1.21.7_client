/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public abstract class MobRenderer<T extends Mob, S extends LivingEntityRenderState, M extends EntityModel<? super S>>
extends LivingEntityRenderer<T, S, M> {
    public MobRenderer(EntityRendererProvider.Context context, M m, float f) {
        super(context, m, f);
    }

    @Override
    protected boolean shouldShowName(T t, double d) {
        return super.shouldShowName(t, d) && (((LivingEntity)t).shouldShowName() || ((Entity)t).hasCustomName() && t == this.entityRenderDispatcher.crosshairPickEntity);
    }

    @Override
    protected float getShadowRadius(S s) {
        return super.getShadowRadius(s) * ((LivingEntityRenderState)s).ageScale;
    }

    @Override
    protected /* synthetic */ float getShadowRadius(EntityRenderState entityRenderState) {
        return this.getShadowRadius((S)((LivingEntityRenderState)entityRenderState));
    }
}

