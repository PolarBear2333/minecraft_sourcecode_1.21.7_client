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

public class BlindnessFogEnvironment
extends MobEffectFogEnvironment {
    @Override
    public Holder<MobEffect> getMobEffect() {
        return MobEffects.BLINDNESS;
    }

    @Override
    public void setupFog(FogData fogData, Entity entity, BlockPos blockPos, ClientLevel clientLevel, float f, DeltaTracker deltaTracker) {
        LivingEntity livingEntity;
        MobEffectInstance mobEffectInstance;
        if (entity instanceof LivingEntity && (mobEffectInstance = (livingEntity = (LivingEntity)entity).getEffect(this.getMobEffect())) != null) {
            float f2 = mobEffectInstance.isInfiniteDuration() ? 5.0f : Mth.lerp(Math.min(1.0f, (float)mobEffectInstance.getDuration() / 20.0f), f, 5.0f);
            fogData.environmentalStart = f2 * 0.25f;
            fogData.environmentalEnd = f2;
            fogData.skyEnd = f2 * 0.8f;
            fogData.cloudEnd = f2 * 0.8f;
        }
    }

    @Override
    public float getModifiedDarkness(LivingEntity livingEntity, float f, float f2) {
        MobEffectInstance mobEffectInstance = livingEntity.getEffect(this.getMobEffect());
        if (mobEffectInstance != null) {
            f = mobEffectInstance.endsWithin(19) ? Math.max((float)mobEffectInstance.getDuration() / 20.0f, f) : 1.0f;
        }
        return f;
    }
}

