/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.ai.goal.target;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class NearestAttackableTargetGoal<T extends LivingEntity>
extends TargetGoal {
    private static final int DEFAULT_RANDOM_INTERVAL = 10;
    protected final Class<T> targetType;
    protected final int randomInterval;
    @Nullable
    protected LivingEntity target;
    protected TargetingConditions targetConditions;

    public NearestAttackableTargetGoal(Mob mob, Class<T> clazz, boolean bl) {
        this(mob, clazz, 10, bl, false, null);
    }

    public NearestAttackableTargetGoal(Mob mob, Class<T> clazz, boolean bl, TargetingConditions.Selector selector) {
        this(mob, clazz, 10, bl, false, selector);
    }

    public NearestAttackableTargetGoal(Mob mob, Class<T> clazz, boolean bl, boolean bl2) {
        this(mob, clazz, 10, bl, bl2, null);
    }

    public NearestAttackableTargetGoal(Mob mob, Class<T> clazz, int n, boolean bl, boolean bl2, @Nullable TargetingConditions.Selector selector) {
        super(mob, bl, bl2);
        this.targetType = clazz;
        this.randomInterval = NearestAttackableTargetGoal.reducedTickDelay(n);
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        this.targetConditions = TargetingConditions.forCombat().range(this.getFollowDistance()).selector(selector);
    }

    @Override
    public boolean canUse() {
        if (this.randomInterval > 0 && this.mob.getRandom().nextInt(this.randomInterval) != 0) {
            return false;
        }
        this.findTarget();
        return this.target != null;
    }

    protected AABB getTargetSearchArea(double d) {
        return this.mob.getBoundingBox().inflate(d, d, d);
    }

    protected void findTarget() {
        ServerLevel serverLevel = NearestAttackableTargetGoal.getServerLevel(this.mob);
        this.target = this.targetType == Player.class || this.targetType == ServerPlayer.class ? serverLevel.getNearestPlayer(this.getTargetConditions(), this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ()) : serverLevel.getNearestEntity(this.mob.level().getEntitiesOfClass(this.targetType, this.getTargetSearchArea(this.getFollowDistance()), livingEntity -> true), this.getTargetConditions(), this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
    }

    @Override
    public void start() {
        this.mob.setTarget(this.target);
        super.start();
    }

    public void setTarget(@Nullable LivingEntity livingEntity) {
        this.target = livingEntity;
    }

    private TargetingConditions getTargetConditions() {
        return this.targetConditions.range(this.getFollowDistance());
    }
}

