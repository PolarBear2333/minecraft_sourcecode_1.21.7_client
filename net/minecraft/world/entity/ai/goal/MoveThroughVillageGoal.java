/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.ai.goal;

import com.google.common.collect.Lists;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class MoveThroughVillageGoal
extends Goal {
    protected final PathfinderMob mob;
    private final double speedModifier;
    @Nullable
    private Path path;
    private BlockPos poiPos;
    private final boolean onlyAtNight;
    private final List<BlockPos> visited = Lists.newArrayList();
    private final int distanceToPoi;
    private final BooleanSupplier canDealWithDoors;

    public MoveThroughVillageGoal(PathfinderMob pathfinderMob, double d, boolean bl, int n, BooleanSupplier booleanSupplier) {
        this.mob = pathfinderMob;
        this.speedModifier = d;
        this.onlyAtNight = bl;
        this.distanceToPoi = n;
        this.canDealWithDoors = booleanSupplier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        if (!GoalUtils.hasGroundPathNavigation(pathfinderMob)) {
            throw new IllegalArgumentException("Unsupported mob for MoveThroughVillageGoal");
        }
    }

    @Override
    public boolean canUse() {
        BlockPos blockPos;
        if (!GoalUtils.hasGroundPathNavigation(this.mob)) {
            return false;
        }
        this.updateVisited();
        if (this.onlyAtNight && this.mob.level().isBrightOutside()) {
            return false;
        }
        ServerLevel serverLevel = (ServerLevel)this.mob.level();
        if (!serverLevel.isCloseToVillage(blockPos = this.mob.blockPosition(), 6)) {
            return false;
        }
        Vec3 vec3 = LandRandomPos.getPos(this.mob, 15, 7, blockPos3 -> {
            if (!serverLevel.isVillage((BlockPos)blockPos3)) {
                return Double.NEGATIVE_INFINITY;
            }
            Optional<BlockPos> optional = serverLevel.getPoiManager().find(holder -> holder.is(PoiTypeTags.VILLAGE), this::hasNotVisited, (BlockPos)blockPos3, 10, PoiManager.Occupancy.IS_OCCUPIED);
            return optional.map(blockPos2 -> -blockPos2.distSqr(blockPos)).orElse(Double.NEGATIVE_INFINITY);
        });
        if (vec3 == null) {
            return false;
        }
        Optional<BlockPos> optional = serverLevel.getPoiManager().find(holder -> holder.is(PoiTypeTags.VILLAGE), this::hasNotVisited, BlockPos.containing(vec3), 10, PoiManager.Occupancy.IS_OCCUPIED);
        if (optional.isEmpty()) {
            return false;
        }
        this.poiPos = optional.get().immutable();
        PathNavigation pathNavigation = this.mob.getNavigation();
        pathNavigation.setCanOpenDoors(this.canDealWithDoors.getAsBoolean());
        this.path = pathNavigation.createPath(this.poiPos, 0);
        pathNavigation.setCanOpenDoors(true);
        if (this.path == null) {
            Vec3 vec32 = DefaultRandomPos.getPosTowards(this.mob, 10, 7, Vec3.atBottomCenterOf(this.poiPos), 1.5707963705062866);
            if (vec32 == null) {
                return false;
            }
            pathNavigation.setCanOpenDoors(this.canDealWithDoors.getAsBoolean());
            this.path = this.mob.getNavigation().createPath(vec32.x, vec32.y, vec32.z, 0);
            pathNavigation.setCanOpenDoors(true);
            if (this.path == null) {
                return false;
            }
        }
        for (int i = 0; i < this.path.getNodeCount(); ++i) {
            Node node = this.path.getNode(i);
            BlockPos blockPos2 = new BlockPos(node.x, node.y + 1, node.z);
            if (!DoorBlock.isWoodenDoor(this.mob.level(), blockPos2)) continue;
            this.path = this.mob.getNavigation().createPath(node.x, (double)node.y, node.z, 0);
            break;
        }
        return this.path != null;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.mob.getNavigation().isDone()) {
            return false;
        }
        return !this.poiPos.closerToCenterThan(this.mob.position(), this.mob.getBbWidth() + (float)this.distanceToPoi);
    }

    @Override
    public void start() {
        this.mob.getNavigation().moveTo(this.path, this.speedModifier);
    }

    @Override
    public void stop() {
        if (this.mob.getNavigation().isDone() || this.poiPos.closerToCenterThan(this.mob.position(), this.distanceToPoi)) {
            this.visited.add(this.poiPos);
        }
    }

    private boolean hasNotVisited(BlockPos blockPos) {
        for (BlockPos blockPos2 : this.visited) {
            if (!Objects.equals(blockPos, blockPos2)) continue;
            return false;
        }
        return true;
    }

    private void updateVisited() {
        if (this.visited.size() > 15) {
            this.visited.remove(0);
        }
    }
}

