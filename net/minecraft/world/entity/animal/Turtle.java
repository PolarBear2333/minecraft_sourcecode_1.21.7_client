/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.animal;

import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class Turtle
extends Animal {
    private static final EntityDataAccessor<Boolean> HAS_EGG = SynchedEntityData.defineId(Turtle.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> LAYING_EGG = SynchedEntityData.defineId(Turtle.class, EntityDataSerializers.BOOLEAN);
    private static final float BABY_SCALE = 0.3f;
    private static final EntityDimensions BABY_DIMENSIONS = EntityType.TURTLE.getDimensions().withAttachments(EntityAttachments.builder().attach(EntityAttachment.PASSENGER, 0.0f, EntityType.TURTLE.getHeight(), -0.25f)).scale(0.3f);
    private static final boolean DEFAULT_HAS_EGG = false;
    int layEggCounter;
    public static final TargetingConditions.Selector BABY_ON_LAND_SELECTOR = (livingEntity, serverLevel) -> livingEntity.isBaby() && !livingEntity.isInWater();
    BlockPos homePos = BlockPos.ZERO;
    @Nullable
    BlockPos travelPos;
    boolean goingHome;

    public Turtle(EntityType<? extends Turtle> entityType, Level level) {
        super((EntityType<? extends Animal>)entityType, level);
        this.setPathfindingMalus(PathType.WATER, 0.0f);
        this.setPathfindingMalus(PathType.DOOR_IRON_CLOSED, -1.0f);
        this.setPathfindingMalus(PathType.DOOR_WOOD_CLOSED, -1.0f);
        this.setPathfindingMalus(PathType.DOOR_OPEN, -1.0f);
        this.moveControl = new TurtleMoveControl(this);
    }

    public void setHomePos(BlockPos blockPos) {
        this.homePos = blockPos;
    }

    public boolean hasEgg() {
        return this.entityData.get(HAS_EGG);
    }

    void setHasEgg(boolean bl) {
        this.entityData.set(HAS_EGG, bl);
    }

    public boolean isLayingEgg() {
        return this.entityData.get(LAYING_EGG);
    }

    void setLayingEgg(boolean bl) {
        this.layEggCounter = bl ? 1 : 0;
        this.entityData.set(LAYING_EGG, bl);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(HAS_EGG, false);
        builder.define(LAYING_EGG, false);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.store("home_pos", BlockPos.CODEC, this.homePos);
        valueOutput.putBoolean("has_egg", this.hasEgg());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        this.setHomePos(valueInput.read("home_pos", BlockPos.CODEC).orElse(this.blockPosition()));
        super.readAdditionalSaveData(valueInput);
        this.setHasEgg(valueInput.getBooleanOr("has_egg", false));
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData) {
        this.setHomePos(this.blockPosition());
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData);
    }

    public static boolean checkTurtleSpawnRules(EntityType<Turtle> entityType, LevelAccessor levelAccessor, EntitySpawnReason entitySpawnReason, BlockPos blockPos, RandomSource randomSource) {
        return blockPos.getY() < levelAccessor.getSeaLevel() + 4 && TurtleEggBlock.onSand(levelAccessor, blockPos) && Turtle.isBrightEnoughToSpawn(levelAccessor, blockPos);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new TurtlePanicGoal(this, 1.2));
        this.goalSelector.addGoal(1, new TurtleBreedGoal(this, 1.0));
        this.goalSelector.addGoal(1, new TurtleLayEggGoal(this, 1.0));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.1, itemStack -> itemStack.is(ItemTags.TURTLE_FOOD), false));
        this.goalSelector.addGoal(3, new TurtleGoToWaterGoal(this, 1.0));
        this.goalSelector.addGoal(4, new TurtleGoHomeGoal(this, 1.0));
        this.goalSelector.addGoal(7, new TurtleTravelGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(9, new TurtleRandomStrollGoal(this, 1.0, 100));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.MOVEMENT_SPEED, 0.25).add(Attributes.STEP_HEIGHT, 1.0);
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 200;
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        if (!this.isInWater() && this.onGround() && !this.isBaby()) {
            return SoundEvents.TURTLE_AMBIENT_LAND;
        }
        return super.getAmbientSound();
    }

    @Override
    protected void playSwimSound(float f) {
        super.playSwimSound(f * 1.5f);
    }

    @Override
    protected SoundEvent getSwimSound() {
        return SoundEvents.TURTLE_SWIM;
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        if (this.isBaby()) {
            return SoundEvents.TURTLE_HURT_BABY;
        }
        return SoundEvents.TURTLE_HURT;
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        if (this.isBaby()) {
            return SoundEvents.TURTLE_DEATH_BABY;
        }
        return SoundEvents.TURTLE_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        SoundEvent soundEvent = this.isBaby() ? SoundEvents.TURTLE_SHAMBLE_BABY : SoundEvents.TURTLE_SHAMBLE;
        this.playSound(soundEvent, 0.15f, 1.0f);
    }

    @Override
    public boolean canFallInLove() {
        return super.canFallInLove() && !this.hasEgg();
    }

    @Override
    protected float nextStep() {
        return this.moveDist + 0.15f;
    }

    @Override
    public float getAgeScale() {
        return this.isBaby() ? 0.3f : 1.0f;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new TurtlePathNavigation(this, level);
    }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return EntityType.TURTLE.create(serverLevel, EntitySpawnReason.BREEDING);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(ItemTags.TURTLE_FOOD);
    }

    @Override
    public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
        if (!this.goingHome && levelReader.getFluidState(blockPos).is(FluidTags.WATER)) {
            return 10.0f;
        }
        if (TurtleEggBlock.onSand(levelReader, blockPos)) {
            return 10.0f;
        }
        return levelReader.getPathfindingCostFromLightLevels(blockPos);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.isAlive() && this.isLayingEgg() && this.layEggCounter >= 1 && this.layEggCounter % 5 == 0) {
            BlockPos blockPos = this.blockPosition();
            if (TurtleEggBlock.onSand(this.level(), blockPos)) {
                this.level().levelEvent(2001, blockPos, Block.getId(this.level().getBlockState(blockPos.below())));
                this.gameEvent(GameEvent.ENTITY_ACTION);
            }
        }
    }

    @Override
    protected void ageBoundaryReached() {
        ServerLevel serverLevel;
        Level level;
        super.ageBoundaryReached();
        if (!this.isBaby() && (level = this.level()) instanceof ServerLevel && (serverLevel = (ServerLevel)level).getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            this.spawnAtLocation(serverLevel, Items.TURTLE_SCUTE, 1);
        }
    }

    @Override
    public void travel(Vec3 vec3) {
        if (this.isInWater()) {
            this.moveRelative(0.1f, vec3);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
            if (!(this.getTarget() != null || this.goingHome && this.homePos.closerToCenterThan(this.position(), 20.0))) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.005, 0.0));
            }
        } else {
            super.travel(vec3);
        }
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    public void thunderHit(ServerLevel serverLevel, LightningBolt lightningBolt) {
        this.hurtServer(serverLevel, this.damageSources().lightningBolt(), Float.MAX_VALUE);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return this.isBaby() ? BABY_DIMENSIONS : super.getDefaultDimensions(pose);
    }

    static class TurtleMoveControl
    extends MoveControl {
        private final Turtle turtle;

        TurtleMoveControl(Turtle turtle) {
            super(turtle);
            this.turtle = turtle;
        }

        private void updateSpeed() {
            if (this.turtle.isInWater()) {
                this.turtle.setDeltaMovement(this.turtle.getDeltaMovement().add(0.0, 0.005, 0.0));
                if (!this.turtle.homePos.closerToCenterThan(this.turtle.position(), 16.0)) {
                    this.turtle.setSpeed(Math.max(this.turtle.getSpeed() / 2.0f, 0.08f));
                }
                if (this.turtle.isBaby()) {
                    this.turtle.setSpeed(Math.max(this.turtle.getSpeed() / 3.0f, 0.06f));
                }
            } else if (this.turtle.onGround()) {
                this.turtle.setSpeed(Math.max(this.turtle.getSpeed() / 2.0f, 0.06f));
            }
        }

        @Override
        public void tick() {
            double d;
            double d2;
            this.updateSpeed();
            if (this.operation != MoveControl.Operation.MOVE_TO || this.turtle.getNavigation().isDone()) {
                this.turtle.setSpeed(0.0f);
                return;
            }
            double d3 = this.wantedX - this.turtle.getX();
            double d4 = Math.sqrt(d3 * d3 + (d2 = this.wantedY - this.turtle.getY()) * d2 + (d = this.wantedZ - this.turtle.getZ()) * d);
            if (d4 < (double)1.0E-5f) {
                this.mob.setSpeed(0.0f);
                return;
            }
            d2 /= d4;
            float f = (float)(Mth.atan2(d, d3) * 57.2957763671875) - 90.0f;
            this.turtle.setYRot(this.rotlerp(this.turtle.getYRot(), f, 90.0f));
            this.turtle.yBodyRot = this.turtle.getYRot();
            float f2 = (float)(this.speedModifier * this.turtle.getAttributeValue(Attributes.MOVEMENT_SPEED));
            this.turtle.setSpeed(Mth.lerp(0.125f, this.turtle.getSpeed(), f2));
            this.turtle.setDeltaMovement(this.turtle.getDeltaMovement().add(0.0, (double)this.turtle.getSpeed() * d2 * 0.1, 0.0));
        }
    }

    static class TurtlePanicGoal
    extends PanicGoal {
        TurtlePanicGoal(Turtle turtle, double d) {
            super(turtle, d);
        }

        @Override
        public boolean canUse() {
            if (!this.shouldPanic()) {
                return false;
            }
            BlockPos blockPos = this.lookForWater(this.mob.level(), this.mob, 7);
            if (blockPos != null) {
                this.posX = blockPos.getX();
                this.posY = blockPos.getY();
                this.posZ = blockPos.getZ();
                return true;
            }
            return this.findRandomPosition();
        }
    }

    static class TurtleBreedGoal
    extends BreedGoal {
        private final Turtle turtle;

        TurtleBreedGoal(Turtle turtle, double d) {
            super(turtle, d);
            this.turtle = turtle;
        }

        @Override
        public boolean canUse() {
            return super.canUse() && !this.turtle.hasEgg();
        }

        @Override
        protected void breed() {
            ServerPlayer serverPlayer = this.animal.getLoveCause();
            if (serverPlayer == null && this.partner.getLoveCause() != null) {
                serverPlayer = this.partner.getLoveCause();
            }
            if (serverPlayer != null) {
                serverPlayer.awardStat(Stats.ANIMALS_BRED);
                CriteriaTriggers.BRED_ANIMALS.trigger(serverPlayer, this.animal, this.partner, null);
            }
            this.turtle.setHasEgg(true);
            this.animal.setAge(6000);
            this.partner.setAge(6000);
            this.animal.resetLove();
            this.partner.resetLove();
            RandomSource randomSource = this.animal.getRandom();
            if (TurtleBreedGoal.getServerLevel(this.level).getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
                this.level.addFreshEntity(new ExperienceOrb(this.level, this.animal.getX(), this.animal.getY(), this.animal.getZ(), randomSource.nextInt(7) + 1));
            }
        }
    }

    static class TurtleLayEggGoal
    extends MoveToBlockGoal {
        private final Turtle turtle;

        TurtleLayEggGoal(Turtle turtle, double d) {
            super(turtle, d, 16);
            this.turtle = turtle;
        }

        @Override
        public boolean canUse() {
            if (this.turtle.hasEgg() && this.turtle.homePos.closerToCenterThan(this.turtle.position(), 9.0)) {
                return super.canUse();
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && this.turtle.hasEgg() && this.turtle.homePos.closerToCenterThan(this.turtle.position(), 9.0);
        }

        @Override
        public void tick() {
            super.tick();
            BlockPos blockPos = this.turtle.blockPosition();
            if (!this.turtle.isInWater() && this.isReachedTarget()) {
                if (this.turtle.layEggCounter < 1) {
                    this.turtle.setLayingEgg(true);
                } else if (this.turtle.layEggCounter > this.adjustedTickDelay(200)) {
                    Level level = this.turtle.level();
                    level.playSound(null, blockPos, SoundEvents.TURTLE_LAY_EGG, SoundSource.BLOCKS, 0.3f, 0.9f + level.random.nextFloat() * 0.2f);
                    BlockPos blockPos2 = this.blockPos.above();
                    BlockState blockState = (BlockState)Blocks.TURTLE_EGG.defaultBlockState().setValue(TurtleEggBlock.EGGS, this.turtle.random.nextInt(4) + 1);
                    level.setBlock(blockPos2, blockState, 3);
                    level.gameEvent(GameEvent.BLOCK_PLACE, blockPos2, GameEvent.Context.of(this.turtle, blockState));
                    this.turtle.setHasEgg(false);
                    this.turtle.setLayingEgg(false);
                    this.turtle.setInLoveTime(600);
                }
                if (this.turtle.isLayingEgg()) {
                    ++this.turtle.layEggCounter;
                }
            }
        }

        @Override
        protected boolean isValidTarget(LevelReader levelReader, BlockPos blockPos) {
            if (!levelReader.isEmptyBlock(blockPos.above())) {
                return false;
            }
            return TurtleEggBlock.isSand(levelReader, blockPos);
        }
    }

    static class TurtleGoToWaterGoal
    extends MoveToBlockGoal {
        private static final int GIVE_UP_TICKS = 1200;
        private final Turtle turtle;

        TurtleGoToWaterGoal(Turtle turtle, double d) {
            super(turtle, turtle.isBaby() ? 2.0 : d, 24);
            this.turtle = turtle;
            this.verticalSearchStart = -1;
        }

        @Override
        public boolean canContinueToUse() {
            return !this.turtle.isInWater() && this.tryTicks <= 1200 && this.isValidTarget(this.turtle.level(), this.blockPos);
        }

        @Override
        public boolean canUse() {
            if (this.turtle.isBaby() && !this.turtle.isInWater()) {
                return super.canUse();
            }
            if (!(this.turtle.goingHome || this.turtle.isInWater() || this.turtle.hasEgg())) {
                return super.canUse();
            }
            return false;
        }

        @Override
        public boolean shouldRecalculatePath() {
            return this.tryTicks % 160 == 0;
        }

        @Override
        protected boolean isValidTarget(LevelReader levelReader, BlockPos blockPos) {
            return levelReader.getBlockState(blockPos).is(Blocks.WATER);
        }
    }

    static class TurtleGoHomeGoal
    extends Goal {
        private final Turtle turtle;
        private final double speedModifier;
        private boolean stuck;
        private int closeToHomeTryTicks;
        private static final int GIVE_UP_TICKS = 600;

        TurtleGoHomeGoal(Turtle turtle, double d) {
            this.turtle = turtle;
            this.speedModifier = d;
        }

        @Override
        public boolean canUse() {
            if (this.turtle.isBaby()) {
                return false;
            }
            if (this.turtle.hasEgg()) {
                return true;
            }
            if (this.turtle.getRandom().nextInt(TurtleGoHomeGoal.reducedTickDelay(700)) != 0) {
                return false;
            }
            return !this.turtle.homePos.closerToCenterThan(this.turtle.position(), 64.0);
        }

        @Override
        public void start() {
            this.turtle.goingHome = true;
            this.stuck = false;
            this.closeToHomeTryTicks = 0;
        }

        @Override
        public void stop() {
            this.turtle.goingHome = false;
        }

        @Override
        public boolean canContinueToUse() {
            return !this.turtle.homePos.closerToCenterThan(this.turtle.position(), 7.0) && !this.stuck && this.closeToHomeTryTicks <= this.adjustedTickDelay(600);
        }

        @Override
        public void tick() {
            BlockPos blockPos = this.turtle.homePos;
            boolean bl = blockPos.closerToCenterThan(this.turtle.position(), 16.0);
            if (bl) {
                ++this.closeToHomeTryTicks;
            }
            if (this.turtle.getNavigation().isDone()) {
                Vec3 vec3 = Vec3.atBottomCenterOf(blockPos);
                Vec3 vec32 = DefaultRandomPos.getPosTowards(this.turtle, 16, 3, vec3, 0.3141592741012573);
                if (vec32 == null) {
                    vec32 = DefaultRandomPos.getPosTowards(this.turtle, 8, 7, vec3, 1.5707963705062866);
                }
                if (vec32 != null && !bl && !this.turtle.level().getBlockState(BlockPos.containing(vec32)).is(Blocks.WATER)) {
                    vec32 = DefaultRandomPos.getPosTowards(this.turtle, 16, 5, vec3, 1.5707963705062866);
                }
                if (vec32 == null) {
                    this.stuck = true;
                    return;
                }
                this.turtle.getNavigation().moveTo(vec32.x, vec32.y, vec32.z, this.speedModifier);
            }
        }
    }

    static class TurtleTravelGoal
    extends Goal {
        private final Turtle turtle;
        private final double speedModifier;
        private boolean stuck;

        TurtleTravelGoal(Turtle turtle, double d) {
            this.turtle = turtle;
            this.speedModifier = d;
        }

        @Override
        public boolean canUse() {
            return !this.turtle.goingHome && !this.turtle.hasEgg() && this.turtle.isInWater();
        }

        @Override
        public void start() {
            int n = 512;
            int n2 = 4;
            RandomSource randomSource = this.turtle.random;
            int n3 = randomSource.nextInt(1025) - 512;
            int n4 = randomSource.nextInt(9) - 4;
            int n5 = randomSource.nextInt(1025) - 512;
            if ((double)n4 + this.turtle.getY() > (double)(this.turtle.level().getSeaLevel() - 1)) {
                n4 = 0;
            }
            this.turtle.travelPos = BlockPos.containing((double)n3 + this.turtle.getX(), (double)n4 + this.turtle.getY(), (double)n5 + this.turtle.getZ());
            this.stuck = false;
        }

        @Override
        public void tick() {
            if (this.turtle.travelPos == null) {
                this.stuck = true;
                return;
            }
            if (this.turtle.getNavigation().isDone()) {
                Vec3 vec3 = Vec3.atBottomCenterOf(this.turtle.travelPos);
                Vec3 vec32 = DefaultRandomPos.getPosTowards(this.turtle, 16, 3, vec3, 0.3141592741012573);
                if (vec32 == null) {
                    vec32 = DefaultRandomPos.getPosTowards(this.turtle, 8, 7, vec3, 1.5707963705062866);
                }
                if (vec32 != null) {
                    int n = Mth.floor(vec32.x);
                    int n2 = Mth.floor(vec32.z);
                    int n3 = 34;
                    if (!this.turtle.level().hasChunksAt(n - 34, n2 - 34, n + 34, n2 + 34)) {
                        vec32 = null;
                    }
                }
                if (vec32 == null) {
                    this.stuck = true;
                    return;
                }
                this.turtle.getNavigation().moveTo(vec32.x, vec32.y, vec32.z, this.speedModifier);
            }
        }

        @Override
        public boolean canContinueToUse() {
            return !this.turtle.getNavigation().isDone() && !this.stuck && !this.turtle.goingHome && !this.turtle.isInLove() && !this.turtle.hasEgg();
        }

        @Override
        public void stop() {
            this.turtle.travelPos = null;
            super.stop();
        }
    }

    static class TurtleRandomStrollGoal
    extends RandomStrollGoal {
        private final Turtle turtle;

        TurtleRandomStrollGoal(Turtle turtle, double d, int n) {
            super(turtle, d, n);
            this.turtle = turtle;
        }

        @Override
        public boolean canUse() {
            if (!(this.mob.isInWater() || this.turtle.goingHome || this.turtle.hasEgg())) {
                return super.canUse();
            }
            return false;
        }
    }

    static class TurtlePathNavigation
    extends AmphibiousPathNavigation {
        TurtlePathNavigation(Turtle turtle, Level level) {
            super(turtle, level);
        }

        @Override
        public boolean isStableDestination(BlockPos blockPos) {
            Mob mob = this.mob;
            if (mob instanceof Turtle) {
                Turtle turtle = (Turtle)mob;
                if (turtle.travelPos != null) {
                    return this.level.getBlockState(blockPos).is(Blocks.WATER);
                }
            }
            return !this.level.getBlockState(blockPos.below()).isAir();
        }
    }
}

