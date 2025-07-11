/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BegGoal
extends Goal {
    private final Wolf wolf;
    @Nullable
    private Player player;
    private final ServerLevel level;
    private final float lookDistance;
    private int lookTime;
    private final TargetingConditions begTargeting;

    public BegGoal(Wolf wolf, float f) {
        this.wolf = wolf;
        this.level = BegGoal.getServerLevel(wolf);
        this.lookDistance = f;
        this.begTargeting = TargetingConditions.forNonCombat().range(f);
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        this.player = this.level.getNearestPlayer(this.begTargeting, this.wolf);
        if (this.player == null) {
            return false;
        }
        return this.playerHoldingInteresting(this.player);
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.player.isAlive()) {
            return false;
        }
        if (this.wolf.distanceToSqr(this.player) > (double)(this.lookDistance * this.lookDistance)) {
            return false;
        }
        return this.lookTime > 0 && this.playerHoldingInteresting(this.player);
    }

    @Override
    public void start() {
        this.wolf.setIsInterested(true);
        this.lookTime = this.adjustedTickDelay(40 + this.wolf.getRandom().nextInt(40));
    }

    @Override
    public void stop() {
        this.wolf.setIsInterested(false);
        this.player = null;
    }

    @Override
    public void tick() {
        this.wolf.getLookControl().setLookAt(this.player.getX(), this.player.getEyeY(), this.player.getZ(), 10.0f, this.wolf.getMaxHeadXRot());
        --this.lookTime;
    }

    private boolean playerHoldingInteresting(Player player) {
        for (InteractionHand interactionHand : InteractionHand.values()) {
            ItemStack itemStack = player.getItemInHand(interactionHand);
            if (!itemStack.is(Items.BONE) && !this.wolf.isFood(itemStack)) continue;
            return true;
        }
        return false;
    }
}

