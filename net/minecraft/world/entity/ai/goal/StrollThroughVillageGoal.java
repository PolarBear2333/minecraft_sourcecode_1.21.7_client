/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.ai.goal;

import java.util.EnumSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class StrollThroughVillageGoal
extends Goal {
    private static final int DISTANCE_THRESHOLD = 10;
    private final PathfinderMob mob;
    private final int interval;
    @Nullable
    private BlockPos wantedPos;

    public StrollThroughVillageGoal(PathfinderMob pathfinderMob, int n) {
        this.mob = pathfinderMob;
        this.interval = StrollThroughVillageGoal.reducedTickDelay(n);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        BlockPos blockPos2;
        if (this.mob.hasControllingPassenger()) {
            return false;
        }
        if (this.mob.level().isBrightOutside()) {
            return false;
        }
        if (this.mob.getRandom().nextInt(this.interval) != 0) {
            return false;
        }
        ServerLevel serverLevel = (ServerLevel)this.mob.level();
        if (!serverLevel.isCloseToVillage(blockPos2 = this.mob.blockPosition(), 6)) {
            return false;
        }
        Vec3 vec3 = LandRandomPos.getPos(this.mob, 15, 7, blockPos -> -serverLevel.sectionsToVillage(SectionPos.of(blockPos)));
        this.wantedPos = vec3 == null ? null : BlockPos.containing(vec3);
        return this.wantedPos != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.wantedPos != null && !this.mob.getNavigation().isDone() && this.mob.getNavigation().getTargetPos().equals(this.wantedPos);
    }

    @Override
    public void tick() {
        if (this.wantedPos == null) {
            return;
        }
        PathNavigation pathNavigation = this.mob.getNavigation();
        if (pathNavigation.isDone() && !this.wantedPos.closerToCenterThan(this.mob.position(), 10.0)) {
            Vec3 vec3 = Vec3.atBottomCenterOf(this.wantedPos);
            Vec3 vec32 = this.mob.position();
            Vec3 vec33 = vec32.subtract(vec3);
            vec3 = vec33.scale(0.4).add(vec3);
            Vec3 vec34 = vec3.subtract(vec32).normalize().scale(10.0).add(vec32);
            BlockPos blockPos = BlockPos.containing(vec34);
            blockPos = this.mob.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockPos);
            if (!pathNavigation.moveTo(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 1.0)) {
                this.moveRandomly();
            }
        }
    }

    private void moveRandomly() {
        RandomSource randomSource = this.mob.getRandom();
        BlockPos blockPos = this.mob.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.mob.blockPosition().offset(-8 + randomSource.nextInt(16), 0, -8 + randomSource.nextInt(16)));
        this.mob.getNavigation().moveTo(blockPos.getX(), blockPos.getY(), blockPos.getZ(), 1.0);
    }
}

