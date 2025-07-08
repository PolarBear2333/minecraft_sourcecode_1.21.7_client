/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;

public class LookAtPlayerGoal
extends Goal {
    public static final float DEFAULT_PROBABILITY = 0.02f;
    protected final Mob mob;
    @Nullable
    protected Entity lookAt;
    protected final float lookDistance;
    private int lookTime;
    protected final float probability;
    private final boolean onlyHorizontal;
    protected final Class<? extends LivingEntity> lookAtType;
    protected final TargetingConditions lookAtContext;

    public LookAtPlayerGoal(Mob mob, Class<? extends LivingEntity> clazz, float f) {
        this(mob, clazz, f, 0.02f);
    }

    public LookAtPlayerGoal(Mob mob, Class<? extends LivingEntity> clazz, float f, float f2) {
        this(mob, clazz, f, f2, false);
    }

    public LookAtPlayerGoal(Mob mob, Class<? extends LivingEntity> clazz, float f, float f2, boolean bl) {
        this.mob = mob;
        this.lookAtType = clazz;
        this.lookDistance = f;
        this.probability = f2;
        this.onlyHorizontal = bl;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
        if (clazz == Player.class) {
            Predicate<Entity> predicate = EntitySelector.notRiding(mob);
            this.lookAtContext = TargetingConditions.forNonCombat().range(f).selector((livingEntity, serverLevel) -> predicate.test(livingEntity));
        } else {
            this.lookAtContext = TargetingConditions.forNonCombat().range(f);
        }
    }

    @Override
    public boolean canUse() {
        if (this.mob.getRandom().nextFloat() >= this.probability) {
            return false;
        }
        if (this.mob.getTarget() != null) {
            this.lookAt = this.mob.getTarget();
        }
        ServerLevel serverLevel = LookAtPlayerGoal.getServerLevel(this.mob);
        this.lookAt = this.lookAtType == Player.class ? serverLevel.getNearestPlayer(this.lookAtContext, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ()) : serverLevel.getNearestEntity(this.mob.level().getEntitiesOfClass(this.lookAtType, this.mob.getBoundingBox().inflate(this.lookDistance, 3.0, this.lookDistance), livingEntity -> true), this.lookAtContext, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        return this.lookAt != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.lookAt.isAlive()) {
            return false;
        }
        if (this.mob.distanceToSqr(this.lookAt) > (double)(this.lookDistance * this.lookDistance)) {
            return false;
        }
        return this.lookTime > 0;
    }

    @Override
    public void start() {
        this.lookTime = this.adjustedTickDelay(40 + this.mob.getRandom().nextInt(40));
    }

    @Override
    public void stop() {
        this.lookAt = null;
    }

    @Override
    public void tick() {
        if (!this.lookAt.isAlive()) {
            return;
        }
        double d = this.onlyHorizontal ? this.mob.getEyeY() : this.lookAt.getEyeY();
        this.mob.getLookControl().setLookAt(this.lookAt.getX(), d, this.lookAt.getZ());
        --this.lookTime;
    }
}

