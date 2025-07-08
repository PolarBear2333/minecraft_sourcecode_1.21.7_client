/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Zombie;

public abstract class AbstractZombieRenderer<T extends Zombie, S extends ZombieRenderState, M extends ZombieModel<S>>
extends HumanoidMobRenderer<T, S, M> {
    private static final ResourceLocation ZOMBIE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/zombie/zombie.png");

    protected AbstractZombieRenderer(EntityRendererProvider.Context context, M m, M m2, M m3, M m4, M m5, M m6) {
        super(context, m, m2, 0.5f);
        this.addLayer(new HumanoidArmorLayer(this, m3, m4, m5, m6, context.getEquipmentRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(S s) {
        return ZOMBIE_LOCATION;
    }

    @Override
    public void extractRenderState(T t, S s, float f) {
        super.extractRenderState(t, s, f);
        ((ZombieRenderState)s).isAggressive = ((Mob)t).isAggressive();
        ((ZombieRenderState)s).isConverting = ((Zombie)t).isUnderWaterConverting();
    }

    @Override
    protected boolean isShaking(S s) {
        return super.isShaking(s) || ((ZombieRenderState)s).isConverting;
    }

    @Override
    protected /* synthetic */ boolean isShaking(LivingEntityRenderState livingEntityRenderState) {
        return this.isShaking((S)((ZombieRenderState)livingEntityRenderState));
    }

    @Override
    public /* synthetic */ ResourceLocation getTextureLocation(LivingEntityRenderState livingEntityRenderState) {
        return this.getTextureLocation((S)((ZombieRenderState)livingEntityRenderState));
    }
}

