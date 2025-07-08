/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.effect;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class MobEffectUtil {
    public static Component formatDuration(MobEffectInstance mobEffectInstance, float f, float f2) {
        if (mobEffectInstance.isInfiniteDuration()) {
            return Component.translatable("effect.duration.infinite");
        }
        int n = Mth.floor((float)mobEffectInstance.getDuration() * f);
        return Component.literal(StringUtil.formatTickDuration(n, f2));
    }

    public static boolean hasDigSpeed(LivingEntity livingEntity) {
        return livingEntity.hasEffect(MobEffects.HASTE) || livingEntity.hasEffect(MobEffects.CONDUIT_POWER);
    }

    public static int getDigSpeedAmplification(LivingEntity livingEntity) {
        int n = 0;
        int n2 = 0;
        if (livingEntity.hasEffect(MobEffects.HASTE)) {
            n = livingEntity.getEffect(MobEffects.HASTE).getAmplifier();
        }
        if (livingEntity.hasEffect(MobEffects.CONDUIT_POWER)) {
            n2 = livingEntity.getEffect(MobEffects.CONDUIT_POWER).getAmplifier();
        }
        return Math.max(n, n2);
    }

    public static boolean hasWaterBreathing(LivingEntity livingEntity) {
        return livingEntity.hasEffect(MobEffects.WATER_BREATHING) || livingEntity.hasEffect(MobEffects.CONDUIT_POWER);
    }

    public static List<ServerPlayer> addEffectToPlayersAround(ServerLevel serverLevel, @Nullable Entity entity, Vec3 vec3, double d, MobEffectInstance mobEffectInstance, int n) {
        Holder<MobEffect> holder = mobEffectInstance.getEffect();
        List<ServerPlayer> list = serverLevel.getPlayers(serverPlayer -> !(!serverPlayer.gameMode.isSurvival() || entity != null && entity.isAlliedTo((Entity)serverPlayer) || !vec3.closerThan(serverPlayer.position(), d) || serverPlayer.hasEffect(holder) && serverPlayer.getEffect(holder).getAmplifier() >= mobEffectInstance.getAmplifier() && !serverPlayer.getEffect(holder).endsWithin(n - 1)));
        list.forEach(serverPlayer -> serverPlayer.addEffect(new MobEffectInstance(mobEffectInstance), entity));
        return list;
    }
}

