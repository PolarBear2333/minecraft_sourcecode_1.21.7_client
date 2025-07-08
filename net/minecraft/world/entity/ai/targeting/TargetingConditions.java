/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.ai.targeting;

import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class TargetingConditions {
    public static final TargetingConditions DEFAULT = TargetingConditions.forCombat();
    private static final double MIN_VISIBILITY_DISTANCE_FOR_INVISIBLE_TARGET = 2.0;
    private final boolean isCombat;
    private double range = -1.0;
    private boolean checkLineOfSight = true;
    private boolean testInvisible = true;
    @Nullable
    private Selector selector;

    private TargetingConditions(boolean bl) {
        this.isCombat = bl;
    }

    public static TargetingConditions forCombat() {
        return new TargetingConditions(true);
    }

    public static TargetingConditions forNonCombat() {
        return new TargetingConditions(false);
    }

    public TargetingConditions copy() {
        TargetingConditions targetingConditions = this.isCombat ? TargetingConditions.forCombat() : TargetingConditions.forNonCombat();
        targetingConditions.range = this.range;
        targetingConditions.checkLineOfSight = this.checkLineOfSight;
        targetingConditions.testInvisible = this.testInvisible;
        targetingConditions.selector = this.selector;
        return targetingConditions;
    }

    public TargetingConditions range(double d) {
        this.range = d;
        return this;
    }

    public TargetingConditions ignoreLineOfSight() {
        this.checkLineOfSight = false;
        return this;
    }

    public TargetingConditions ignoreInvisibilityTesting() {
        this.testInvisible = false;
        return this;
    }

    public TargetingConditions selector(@Nullable Selector selector) {
        this.selector = selector;
        return this;
    }

    public boolean test(ServerLevel serverLevel, @Nullable LivingEntity livingEntity, LivingEntity livingEntity2) {
        if (livingEntity == livingEntity2) {
            return false;
        }
        if (!livingEntity2.canBeSeenByAnyone()) {
            return false;
        }
        if (this.selector != null && !this.selector.test(livingEntity2, serverLevel)) {
            return false;
        }
        if (livingEntity == null) {
            if (this.isCombat && (!livingEntity2.canBeSeenAsEnemy() || serverLevel.getDifficulty() == Difficulty.PEACEFUL)) {
                return false;
            }
        } else {
            Mob mob;
            if (this.isCombat && (!livingEntity.canAttack(livingEntity2) || !livingEntity.canAttackType(livingEntity2.getType()) || livingEntity.isAlliedTo(livingEntity2))) {
                return false;
            }
            if (this.range > 0.0) {
                double d = this.testInvisible ? livingEntity2.getVisibilityPercent(livingEntity) : 1.0;
                double d2 = Math.max(this.range * d, 2.0);
                double d3 = livingEntity.distanceToSqr(livingEntity2.getX(), livingEntity2.getY(), livingEntity2.getZ());
                if (d3 > d2 * d2) {
                    return false;
                }
            }
            if (this.checkLineOfSight && livingEntity instanceof Mob && !(mob = (Mob)livingEntity).getSensing().hasLineOfSight(livingEntity2)) {
                return false;
            }
        }
        return true;
    }

    @FunctionalInterface
    public static interface Selector {
        public boolean test(LivingEntity var1, ServerLevel var2);
    }
}

