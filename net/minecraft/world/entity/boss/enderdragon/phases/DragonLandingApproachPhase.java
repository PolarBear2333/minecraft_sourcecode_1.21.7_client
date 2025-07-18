/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.boss.enderdragon.phases;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.AbstractDragonPhaseInstance;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndPodiumFeature;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class DragonLandingApproachPhase
extends AbstractDragonPhaseInstance {
    private static final TargetingConditions NEAR_EGG_TARGETING = TargetingConditions.forCombat().ignoreLineOfSight();
    @Nullable
    private Path currentPath;
    @Nullable
    private Vec3 targetLocation;

    public DragonLandingApproachPhase(EnderDragon enderDragon) {
        super(enderDragon);
    }

    public EnderDragonPhase<DragonLandingApproachPhase> getPhase() {
        return EnderDragonPhase.LANDING_APPROACH;
    }

    @Override
    public void begin() {
        this.currentPath = null;
        this.targetLocation = null;
    }

    @Override
    public void doServerTick(ServerLevel serverLevel) {
        double d;
        double d2 = d = this.targetLocation == null ? 0.0 : this.targetLocation.distanceToSqr(this.dragon.getX(), this.dragon.getY(), this.dragon.getZ());
        if (d < 100.0 || d > 22500.0 || this.dragon.horizontalCollision || this.dragon.verticalCollision) {
            this.findNewTarget(serverLevel);
        }
    }

    @Override
    @Nullable
    public Vec3 getFlyTargetLocation() {
        return this.targetLocation;
    }

    private void findNewTarget(ServerLevel serverLevel) {
        if (this.currentPath == null || this.currentPath.isDone()) {
            int n;
            Object object;
            int n2 = this.dragon.findClosestNode();
            BlockPos blockPos = serverLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, EndPodiumFeature.getLocation(this.dragon.getFightOrigin()));
            Player player = serverLevel.getNearestPlayer(NEAR_EGG_TARGETING, this.dragon, (double)blockPos.getX(), (double)blockPos.getY(), blockPos.getZ());
            if (player != null) {
                object = new Vec3(player.getX(), 0.0, player.getZ()).normalize();
                n = this.dragon.findClosestNode(-((Vec3)object).x * 40.0, 105.0, -((Vec3)object).z * 40.0);
            } else {
                n = this.dragon.findClosestNode(40.0, blockPos.getY(), 0.0);
            }
            object = new Node(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            this.currentPath = this.dragon.findPath(n2, n, (Node)object);
            if (this.currentPath != null) {
                this.currentPath.advance();
            }
        }
        this.navigateToNextPathNode();
        if (this.currentPath != null && this.currentPath.isDone()) {
            this.dragon.getPhaseManager().setPhase(EnderDragonPhase.LANDING);
        }
    }

    private void navigateToNextPathNode() {
        if (this.currentPath != null && !this.currentPath.isDone()) {
            double d;
            BlockPos blockPos = this.currentPath.getNextNodePos();
            this.currentPath.advance();
            double d2 = blockPos.getX();
            double d3 = blockPos.getZ();
            while ((d = (double)((float)blockPos.getY() + this.dragon.getRandom().nextFloat() * 20.0f)) < (double)blockPos.getY()) {
            }
            this.targetLocation = new Vec3(d2, d, d3);
        }
    }
}

