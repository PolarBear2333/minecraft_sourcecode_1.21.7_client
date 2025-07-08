/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.ai.goal.target;

import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class DefendVillageTargetGoal
extends TargetGoal {
    private final IronGolem golem;
    @Nullable
    private LivingEntity potentialTarget;
    private final TargetingConditions attackTargeting = TargetingConditions.forCombat().range(64.0);

    public DefendVillageTargetGoal(IronGolem ironGolem) {
        super(ironGolem, false, true);
        this.golem = ironGolem;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
        LivingEntity livingEntity;
        AABB aABB = this.golem.getBoundingBox().inflate(10.0, 8.0, 10.0);
        ServerLevel serverLevel = DefendVillageTargetGoal.getServerLevel(this.golem);
        List<Villager> list = serverLevel.getNearbyEntities(Villager.class, this.attackTargeting, this.golem, aABB);
        List<Player> list2 = serverLevel.getNearbyPlayers(this.attackTargeting, this.golem, aABB);
        Object object = list.iterator();
        while (object.hasNext()) {
            livingEntity = object.next();
            Villager villager = (Villager)livingEntity;
            for (Player player : list2) {
                int n = villager.getPlayerReputation(player);
                if (n > -100) continue;
                this.potentialTarget = player;
            }
        }
        if (this.potentialTarget == null) {
            return false;
        }
        livingEntity = this.potentialTarget;
        return !(livingEntity instanceof Player) || !((Player)(object = (Player)livingEntity)).isSpectator() && !((Player)object).isCreative();
    }

    @Override
    public void start() {
        this.golem.setTarget(this.potentialTarget);
        super.start();
    }
}

