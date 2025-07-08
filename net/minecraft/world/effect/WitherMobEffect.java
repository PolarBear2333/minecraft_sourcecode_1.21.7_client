/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.effect;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class WitherMobEffect
extends MobEffect {
    public static final int DAMAGE_INTERVAL = 40;

    protected WitherMobEffect(MobEffectCategory mobEffectCategory, int n) {
        super(mobEffectCategory, n);
    }

    @Override
    public boolean applyEffectTick(ServerLevel serverLevel, LivingEntity livingEntity, int n) {
        livingEntity.hurtServer(serverLevel, livingEntity.damageSources().wither(), 1.0f);
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int n, int n2) {
        int n3 = 40 >> n2;
        if (n3 > 0) {
            return n % n3 == 0;
        }
        return true;
    }
}

