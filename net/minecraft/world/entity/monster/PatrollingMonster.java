/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.monster;

import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public abstract class PatrollingMonster
extends Monster {
    private static final boolean DEFAULT_PATROL_LEADER = false;
    private static final boolean DEFAULT_PATROLLING = false;
    @Nullable
    private BlockPos patrolTarget;
    private boolean patrolLeader = false;
    private boolean patrolling = false;

    protected PatrollingMonster(EntityType<? extends PatrollingMonster> entityType, Level level) {
        super((EntityType<? extends Monster>)entityType, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(4, new LongDistancePatrolGoal<PatrollingMonster>(this, 0.7, 0.595));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.storeNullable("patrol_target", BlockPos.CODEC, this.patrolTarget);
        valueOutput.putBoolean("PatrolLeader", this.patrolLeader);
        valueOutput.putBoolean("Patrolling", this.patrolling);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.patrolTarget = valueInput.read("patrol_target", BlockPos.CODEC).orElse(null);
        this.patrolLeader = valueInput.getBooleanOr("PatrolLeader", false);
        this.patrolling = valueInput.getBooleanOr("Patrolling", false);
    }

    public boolean canBeLeader() {
        return true;
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData) {
        if (entitySpawnReason != EntitySpawnReason.PATROL && entitySpawnReason != EntitySpawnReason.EVENT && entitySpawnReason != EntitySpawnReason.STRUCTURE && serverLevelAccessor.getRandom().nextFloat() < 0.06f && this.canBeLeader()) {
            this.patrolLeader = true;
        }
        if (this.isPatrolLeader()) {
            this.setItemSlot(EquipmentSlot.HEAD, Raid.getOminousBannerInstance(this.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN)));
            this.setDropChance(EquipmentSlot.HEAD, 2.0f);
        }
        if (entitySpawnReason == EntitySpawnReason.PATROL) {
            this.patrolling = true;
        }
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData);
    }

    public static boolean checkPatrollingMonsterSpawnRules(EntityType<? extends PatrollingMonster> entityType, LevelAccessor levelAccessor, EntitySpawnReason entitySpawnReason, BlockPos blockPos, RandomSource randomSource) {
        if (levelAccessor.getBrightness(LightLayer.BLOCK, blockPos) > 8) {
            return false;
        }
        return PatrollingMonster.checkAnyLightMonsterSpawnRules(entityType, levelAccessor, entitySpawnReason, blockPos, randomSource);
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        return !this.patrolling || d > 16384.0;
    }

    public void setPatrolTarget(BlockPos blockPos) {
        this.patrolTarget = blockPos;
        this.patrolling = true;
    }

    public BlockPos getPatrolTarget() {
        return this.patrolTarget;
    }

    public boolean hasPatrolTarget() {
        return this.patrolTarget != null;
    }

    public void setPatrolLeader(boolean bl) {
        this.patrolLeader = bl;
        this.patrolling = true;
    }

    public boolean isPatrolLeader() {
        return this.patrolLeader;
    }

    public boolean canJoinPatrol() {
        return true;
    }

    public void findPatrolTarget() {
        this.patrolTarget = this.blockPosition().offset(-500 + this.random.nextInt(1000), 0, -500 + this.random.nextInt(1000));
        this.patrolling = true;
    }

    protected boolean isPatrolling() {
        return this.patrolling;
    }

    protected void setPatrolling(boolean bl) {
        this.patrolling = bl;
    }

    public static class LongDistancePatrolGoal<T extends PatrollingMonster>
    extends Goal {
        private static final int NAVIGATION_FAILED_COOLDOWN = 200;
        private final T mob;
        private final double speedModifier;
        private final double leaderSpeedModifier;
        private long cooldownUntil;

        public LongDistancePatrolGoal(T t, double d, double d2) {
            this.mob = t;
            this.speedModifier = d;
            this.leaderSpeedModifier = d2;
            this.cooldownUntil = -1L;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            boolean bl = ((Entity)this.mob).level().getGameTime() < this.cooldownUntil;
            return ((PatrollingMonster)this.mob).isPatrolling() && ((Mob)this.mob).getTarget() == null && !((Entity)this.mob).hasControllingPassenger() && ((PatrollingMonster)this.mob).hasPatrolTarget() && !bl;
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
        }

        @Override
        public void tick() {
            boolean bl = ((PatrollingMonster)this.mob).isPatrolLeader();
            PathNavigation pathNavigation = ((Mob)this.mob).getNavigation();
            if (pathNavigation.isDone()) {
                List<PatrollingMonster> list = this.findPatrolCompanions();
                if (((PatrollingMonster)this.mob).isPatrolling() && list.isEmpty()) {
                    ((PatrollingMonster)this.mob).setPatrolling(false);
                } else if (!bl || !((PatrollingMonster)this.mob).getPatrolTarget().closerToCenterThan(((Entity)this.mob).position(), 10.0)) {
                    Vec3 vec3 = Vec3.atBottomCenterOf(((PatrollingMonster)this.mob).getPatrolTarget());
                    Vec3 vec32 = ((Entity)this.mob).position();
                    Vec3 vec33 = vec32.subtract(vec3);
                    vec3 = vec33.yRot(90.0f).scale(0.4).add(vec3);
                    Vec3 vec34 = vec3.subtract(vec32).normalize().scale(10.0).add(vec32);
                    BlockPos blockPos = BlockPos.containing(vec34);
                    blockPos = ((Entity)this.mob).level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, blockPos);
                    if (!pathNavigation.moveTo(blockPos.getX(), blockPos.getY(), blockPos.getZ(), bl ? this.leaderSpeedModifier : this.speedModifier)) {
                        this.moveRandomly();
                        this.cooldownUntil = ((Entity)this.mob).level().getGameTime() + 200L;
                    } else if (bl) {
                        for (PatrollingMonster patrollingMonster : list) {
                            patrollingMonster.setPatrolTarget(blockPos);
                        }
                    }
                } else {
                    ((PatrollingMonster)this.mob).findPatrolTarget();
                }
            }
        }

        private List<PatrollingMonster> findPatrolCompanions() {
            return ((Entity)this.mob).level().getEntitiesOfClass(PatrollingMonster.class, ((Entity)this.mob).getBoundingBox().inflate(16.0), patrollingMonster -> patrollingMonster.canJoinPatrol() && !patrollingMonster.is((Entity)this.mob));
        }

        private boolean moveRandomly() {
            RandomSource randomSource = ((Entity)this.mob).getRandom();
            BlockPos blockPos = ((Entity)this.mob).level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ((Entity)this.mob).blockPosition().offset(-8 + randomSource.nextInt(16), 0, -8 + randomSource.nextInt(16)));
            return ((Mob)this.mob).getNavigation().moveTo(blockPos.getX(), blockPos.getY(), blockPos.getZ(), this.speedModifier);
        }
    }
}

