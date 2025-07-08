/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.fog.environment;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.environment.MobEffectFogEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class DarknessFogEnvironment
extends MobEffectFogEnvironment {
    @Override
    public Holder<MobEffect> getMobEffect() {
        return MobEffects.DARKNESS;
    }

    @Override
    public void setupFog(FogData fogData, Entity entity, BlockPos blockPos, ClientLevel clientLevel, float f, DeltaTracker deltaTracker) {
        LivingEntity livingEntity;
        MobEffectInstance mobEffectInstance;
        if (entity instanceof LivingEntity && (mobEffectInstance = (livingEntity = (LivingEntity)entity).getEffect(this.getMobEffect())) != null) {
            float f2 = Mth.lerp(mobEffectInstance.getBlendFactor(livingEntity, deltaTracker.getGameTimeDeltaPartialTick(false)), f, 15.0f);
            fogData.environmentalStart = f2 * 0.75f;
            fogData.environmentalEnd = f2;
            fogData.skyEnd = f2;
            fogData.cloudEnd = f2;
        }
    }

    @Override
    public float getModifiedDarkness(LivingEntity livingEntity, float f, float f2) {
        MobEffectInstance mobEffectInstance = livingEntity.getEffect(this.getMobEffect());
        return mobEffectInstance != null ? Math.max(mobEffectInstance.getBlendFactor(livingEntity, f2), f) : f;
    }
}

