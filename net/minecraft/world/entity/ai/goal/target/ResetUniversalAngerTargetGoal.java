/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.goal.target;

import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;

public class ResetUniversalAngerTargetGoal<T extends Mob>
extends Goal {
    private static final int ALERT_RANGE_Y = 10;
    private final T mob;
    private final boolean alertOthersOfSameType;
    private int lastHurtByPlayerTimestamp;

    public ResetUniversalAngerTargetGoal(T t, boolean bl) {
        this.mob = t;
        this.alertOthersOfSameType = bl;
    }

    @Override
    public boolean canUse() {
        return ResetUniversalAngerTargetGoal.getServerLevel(this.mob).getGameRules().getBoolean(GameRules.RULE_UNIVERSAL_ANGER) && this.wasHurtByPlayer();
    }

    private boolean wasHurtByPlayer() {
        return ((LivingEntity)this.mob).getLastHurtByMob() != null && ((LivingEntity)this.mob).getLastHurtByMob().getType() == EntityType.PLAYER && ((LivingEntity)this.mob).getLastHurtByMobTimestamp() > this.lastHurtByPlayerTimestamp;
    }

    @Override
    public void start() {
        this.lastHurtByPlayerTimestamp = ((LivingEntity)this.mob).getLastHurtByMobTimestamp();
        ((NeutralMob)this.mob).forgetCurrentTargetAndRefreshUniversalAnger();
        if (this.alertOthersOfSameType) {
            this.getNearbyMobsOfSameType().stream().filter(mob -> mob != this.mob).map(mob -> (NeutralMob)((Object)mob)).forEach(NeutralMob::forgetCurrentTargetAndRefreshUniversalAnger);
        }
        super.start();
    }

    private List<? extends Mob> getNearbyMobsOfSameType() {
        double d = ((LivingEntity)this.mob).getAttributeValue(Attributes.FOLLOW_RANGE);
        AABB aABB = AABB.unitCubeFromLowerCorner(((Entity)this.mob).position()).inflate(d, 10.0, d);
        return ((Entity)this.mob).level().getEntitiesOfClass(this.mob.getClass(), aABB, EntitySelector.NO_SPECTATORS);
    }
}

