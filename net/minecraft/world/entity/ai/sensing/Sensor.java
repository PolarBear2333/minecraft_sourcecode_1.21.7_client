/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.sensing;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

public abstract class Sensor<E extends LivingEntity> {
    private static final RandomSource RANDOM = RandomSource.createThreadSafe();
    private static final int DEFAULT_SCAN_RATE = 20;
    private static final int DEFAULT_TARGETING_RANGE = 16;
    private static final TargetingConditions TARGET_CONDITIONS = TargetingConditions.forNonCombat().range(16.0);
    private static final TargetingConditions TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING = TargetingConditions.forNonCombat().range(16.0).ignoreInvisibilityTesting();
    private static final TargetingConditions ATTACK_TARGET_CONDITIONS = TargetingConditions.forCombat().range(16.0);
    private static final TargetingConditions ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING = TargetingConditions.forCombat().range(16.0).ignoreInvisibilityTesting();
    private static final TargetingConditions ATTACK_TARGET_CONDITIONS_IGNORE_LINE_OF_SIGHT = TargetingConditions.forCombat().range(16.0).ignoreLineOfSight();
    private static final TargetingConditions ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_AND_LINE_OF_SIGHT = TargetingConditions.forCombat().range(16.0).ignoreLineOfSight().ignoreInvisibilityTesting();
    private final int scanRate;
    private long timeToTick;

    public Sensor(int n) {
        this.scanRate = n;
        this.timeToTick = RANDOM.nextInt(n);
    }

    public Sensor() {
        this(20);
    }

    public final void tick(ServerLevel serverLevel, E e) {
        if (--this.timeToTick <= 0L) {
            this.timeToTick = this.scanRate;
            this.updateTargetingConditionRanges(e);
            this.doTick(serverLevel, e);
        }
    }

    private void updateTargetingConditionRanges(E e) {
        double d = ((LivingEntity)e).getAttributeValue(Attributes.FOLLOW_RANGE);
        TARGET_CONDITIONS.range(d);
        TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING.range(d);
        ATTACK_TARGET_CONDITIONS.range(d);
        ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING.range(d);
        ATTACK_TARGET_CONDITIONS_IGNORE_LINE_OF_SIGHT.range(d);
        ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_AND_LINE_OF_SIGHT.range(d);
    }

    protected abstract void doTick(ServerLevel var1, E var2);

    public abstract Set<MemoryModuleType<?>> requires();

    public static boolean isEntityTargetable(ServerLevel serverLevel, LivingEntity livingEntity, LivingEntity livingEntity2) {
        if (livingEntity.getBrain().isMemoryValue(MemoryModuleType.ATTACK_TARGET, livingEntity2)) {
            return TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING.test(serverLevel, livingEntity, livingEntity2);
        }
        return TARGET_CONDITIONS.test(serverLevel, livingEntity, livingEntity2);
    }

    public static boolean isEntityAttackable(ServerLevel serverLevel, LivingEntity livingEntity, LivingEntity livingEntity2) {
        if (livingEntity.getBrain().isMemoryValue(MemoryModuleType.ATTACK_TARGET, livingEntity2)) {
            return ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_TESTING.test(serverLevel, livingEntity, livingEntity2);
        }
        return ATTACK_TARGET_CONDITIONS.test(serverLevel, livingEntity, livingEntity2);
    }

    public static BiPredicate<ServerLevel, LivingEntity> wasEntityAttackableLastNTicks(LivingEntity livingEntity, int n) {
        return Sensor.rememberPositives(n, (serverLevel, livingEntity2) -> Sensor.isEntityAttackable(serverLevel, livingEntity, livingEntity2));
    }

    public static boolean isEntityAttackableIgnoringLineOfSight(ServerLevel serverLevel, LivingEntity livingEntity, LivingEntity livingEntity2) {
        if (livingEntity.getBrain().isMemoryValue(MemoryModuleType.ATTACK_TARGET, livingEntity2)) {
            return ATTACK_TARGET_CONDITIONS_IGNORE_INVISIBILITY_AND_LINE_OF_SIGHT.test(serverLevel, livingEntity, livingEntity2);
        }
        return ATTACK_TARGET_CONDITIONS_IGNORE_LINE_OF_SIGHT.test(serverLevel, livingEntity, livingEntity2);
    }

    static <T, U> BiPredicate<T, U> rememberPositives(int n, BiPredicate<T, U> biPredicate) {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        return (object, object2) -> {
            if (biPredicate.test(object, object2)) {
                atomicInteger.set(n);
                return true;
            }
            return atomicInteger.decrementAndGet() >= 0;
        };
    }
}

