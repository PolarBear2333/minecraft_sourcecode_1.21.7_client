/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.JumpGoal;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

public class DolphinJumpGoal
extends JumpGoal {
    private static final int[] STEPS_TO_CHECK = new int[]{0, 1, 4, 5, 6, 7};
    private final Dolphin dolphin;
    private final int interval;
    private boolean breached;

    public DolphinJumpGoal(Dolphin dolphin, int n) {
        this.dolphin = dolphin;
        this.interval = DolphinJumpGoal.reducedTickDelay(n);
    }

    @Override
    public boolean canUse() {
        if (this.dolphin.getRandom().nextInt(this.interval) != 0) {
            return false;
        }
        Direction direction = this.dolphin.getMotionDirection();
        int n = direction.getStepX();
        int n2 = direction.getStepZ();
        BlockPos blockPos = this.dolphin.blockPosition();
        for (int n3 : STEPS_TO_CHECK) {
            if (this.waterIsClear(blockPos, n, n2, n3) && this.surfaceIsClear(blockPos, n, n2, n3)) continue;
            return false;
        }
        return true;
    }

    private boolean waterIsClear(BlockPos blockPos, int n, int n2, int n3) {
        BlockPos blockPos2 = blockPos.offset(n * n3, 0, n2 * n3);
        return this.dolphin.level().getFluidState(blockPos2).is(FluidTags.WATER) && !this.dolphin.level().getBlockState(blockPos2).blocksMotion();
    }

    private boolean surfaceIsClear(BlockPos blockPos, int n, int n2, int n3) {
        return this.dolphin.level().getBlockState(blockPos.offset(n * n3, 1, n2 * n3)).isAir() && this.dolphin.level().getBlockState(blockPos.offset(n * n3, 2, n2 * n3)).isAir();
    }

    @Override
    public boolean canContinueToUse() {
        double d = this.dolphin.getDeltaMovement().y;
        return !(d * d < (double)0.03f && this.dolphin.getXRot() != 0.0f && Math.abs(this.dolphin.getXRot()) < 10.0f && this.dolphin.isInWater() || this.dolphin.onGround());
    }

    @Override
    public boolean isInterruptable() {
        return false;
    }

    @Override
    public void start() {
        Direction direction = this.dolphin.getMotionDirection();
        this.dolphin.setDeltaMovement(this.dolphin.getDeltaMovement().add((double)direction.getStepX() * 0.6, 0.7, (double)direction.getStepZ() * 0.6));
        this.dolphin.getNavigation().stop();
    }

    @Override
    public void stop() {
        this.dolphin.setXRot(0.0f);
    }

    @Override
    public void tick() {
        Object object;
        boolean bl = this.breached;
        if (!bl) {
            object = this.dolphin.level().getFluidState(this.dolphin.blockPosition());
            this.breached = ((FluidState)object).is(FluidTags.WATER);
        }
        if (this.breached && !bl) {
            this.dolphin.playSound(SoundEvents.DOLPHIN_JUMP, 1.0f, 1.0f);
        }
        object = this.dolphin.getDeltaMovement();
        if (((Vec3)object).y * ((Vec3)object).y < (double)0.03f && this.dolphin.getXRot() != 0.0f) {
            this.dolphin.setXRot(Mth.rotLerp(0.2f, this.dolphin.getXRot(), 0.0f));
        } else if (((Vec3)object).length() > (double)1.0E-5f) {
            double d = ((Vec3)object).horizontalDistance();
            double d2 = Math.atan2(-((Vec3)object).y, d) * 57.2957763671875;
            this.dolphin.setXRot((float)d2);
        }
    }
}

