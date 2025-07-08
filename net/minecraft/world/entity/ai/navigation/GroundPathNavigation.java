/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.Vec3;

public class GroundPathNavigation
extends PathNavigation {
    private boolean avoidSun;

    public GroundPathNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    protected PathFinder createPathFinder(int n) {
        this.nodeEvaluator = new WalkNodeEvaluator();
        return new PathFinder(this.nodeEvaluator, n);
    }

    @Override
    protected boolean canUpdatePath() {
        return this.mob.onGround() || this.mob.isInLiquid() || this.mob.isPassenger();
    }

    @Override
    protected Vec3 getTempMobPos() {
        return new Vec3(this.mob.getX(), this.getSurfaceY(), this.mob.getZ());
    }

    @Override
    public Path createPath(BlockPos blockPos, int n) {
        BlockPos.MutableBlockPos mutableBlockPos;
        LevelChunk levelChunk = this.level.getChunkSource().getChunkNow(SectionPos.blockToSectionCoord(blockPos.getX()), SectionPos.blockToSectionCoord(blockPos.getZ()));
        if (levelChunk == null) {
            return null;
        }
        if (levelChunk.getBlockState(blockPos).isAir()) {
            mutableBlockPos = blockPos.mutable().move(Direction.DOWN);
            while (mutableBlockPos.getY() >= this.level.getMinY() && levelChunk.getBlockState(mutableBlockPos).isAir()) {
                mutableBlockPos.move(Direction.DOWN);
            }
            if (mutableBlockPos.getY() >= this.level.getMinY()) {
                return super.createPath((BlockPos)mutableBlockPos.above(), n);
            }
            mutableBlockPos.setY(blockPos.getY() + 1);
            while (mutableBlockPos.getY() <= this.level.getMaxY() && levelChunk.getBlockState(mutableBlockPos).isAir()) {
                mutableBlockPos.move(Direction.UP);
            }
            blockPos = mutableBlockPos;
        }
        if (levelChunk.getBlockState(blockPos).isSolid()) {
            mutableBlockPos = blockPos.mutable().move(Direction.UP);
            while (mutableBlockPos.getY() <= this.level.getMaxY() && levelChunk.getBlockState(mutableBlockPos).isSolid()) {
                mutableBlockPos.move(Direction.UP);
            }
            return super.createPath(mutableBlockPos.immutable(), n);
        }
        return super.createPath(blockPos, n);
    }

    @Override
    public Path createPath(Entity entity, int n) {
        return this.createPath(entity.blockPosition(), n);
    }

    private int getSurfaceY() {
        if (!this.mob.isInWater() || !this.canFloat()) {
            return Mth.floor(this.mob.getY() + 0.5);
        }
        int n = this.mob.getBlockY();
        BlockState blockState = this.level.getBlockState(BlockPos.containing(this.mob.getX(), n, this.mob.getZ()));
        int n2 = 0;
        while (blockState.is(Blocks.WATER)) {
            blockState = this.level.getBlockState(BlockPos.containing(this.mob.getX(), ++n, this.mob.getZ()));
            if (++n2 <= 16) continue;
            return this.mob.getBlockY();
        }
        return n;
    }

    @Override
    protected void trimPath() {
        super.trimPath();
        if (this.avoidSun) {
            if (this.level.canSeeSky(BlockPos.containing(this.mob.getX(), this.mob.getY() + 0.5, this.mob.getZ()))) {
                return;
            }
            for (int i = 0; i < this.path.getNodeCount(); ++i) {
                Node node = this.path.getNode(i);
                if (!this.level.canSeeSky(new BlockPos(node.x, node.y, node.z))) continue;
                this.path.truncateNodes(i);
                return;
            }
        }
    }

    @Override
    public boolean canNavigateGround() {
        return true;
    }

    protected boolean hasValidPathType(PathType pathType) {
        if (pathType == PathType.WATER) {
            return false;
        }
        if (pathType == PathType.LAVA) {
            return false;
        }
        return pathType != PathType.OPEN;
    }

    public void setAvoidSun(boolean bl) {
        this.avoidSun = bl;
    }

    public void setCanWalkOverFences(boolean bl) {
        this.nodeEvaluator.setCanWalkOverFences(bl);
    }
}

