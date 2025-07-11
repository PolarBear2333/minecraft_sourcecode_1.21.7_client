/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.monster;

import com.google.common.collect.Sets;
import java.util.LinkedHashSet;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ItemBasedSteering;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

public class Strider
extends Animal
implements ItemSteerable {
    private static final ResourceLocation SUFFOCATING_MODIFIER_ID = ResourceLocation.withDefaultNamespace("suffocating");
    private static final AttributeModifier SUFFOCATING_MODIFIER = new AttributeModifier(SUFFOCATING_MODIFIER_ID, -0.34f, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    private static final float SUFFOCATE_STEERING_MODIFIER = 0.35f;
    private static final float STEERING_MODIFIER = 0.55f;
    private static final EntityDataAccessor<Integer> DATA_BOOST_TIME = SynchedEntityData.defineId(Strider.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_SUFFOCATING = SynchedEntityData.defineId(Strider.class, EntityDataSerializers.BOOLEAN);
    private final ItemBasedSteering steering;
    @Nullable
    private TemptGoal temptGoal;

    public Strider(EntityType<? extends Strider> entityType, Level level) {
        super((EntityType<? extends Animal>)entityType, level);
        this.steering = new ItemBasedSteering(this.entityData, DATA_BOOST_TIME);
        this.blocksBuilding = true;
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.setPathfindingMalus(PathType.LAVA, 0.0f);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0f);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0f);
    }

    public static boolean checkStriderSpawnRules(EntityType<Strider> entityType, LevelAccessor levelAccessor, EntitySpawnReason entitySpawnReason, BlockPos blockPos, RandomSource randomSource) {
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        do {
            mutableBlockPos.move(Direction.UP);
        } while (levelAccessor.getFluidState(mutableBlockPos).is(FluidTags.LAVA));
        return levelAccessor.getBlockState(mutableBlockPos).isAir();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (DATA_BOOST_TIME.equals(entityDataAccessor) && this.level().isClientSide) {
            this.steering.onSynced();
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BOOST_TIME, 0);
        builder.define(DATA_SUFFOCATING, false);
    }

    @Override
    public boolean canUseSlot(EquipmentSlot equipmentSlot) {
        if (equipmentSlot == EquipmentSlot.SADDLE) {
            return this.isAlive() && !this.isBaby();
        }
        return super.canUseSlot(equipmentSlot);
    }

    @Override
    protected boolean canDispenserEquipIntoSlot(EquipmentSlot equipmentSlot) {
        return equipmentSlot == EquipmentSlot.SADDLE || super.canDispenserEquipIntoSlot(equipmentSlot);
    }

    @Override
    protected Holder<SoundEvent> getEquipSound(EquipmentSlot equipmentSlot, ItemStack itemStack, Equippable equippable) {
        if (equipmentSlot == EquipmentSlot.SADDLE) {
            return SoundEvents.STRIDER_SADDLE;
        }
        return super.getEquipSound(equipmentSlot, itemStack, equippable);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.65));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.temptGoal = new TemptGoal(this, 1.4, itemStack -> itemStack.is(ItemTags.STRIDER_TEMPT_ITEMS), false);
        this.goalSelector.addGoal(3, this.temptGoal);
        this.goalSelector.addGoal(4, new StriderGoToLavaGoal(this, 1.0));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.0));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0, 60));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Strider.class, 8.0f));
    }

    public void setSuffocating(boolean bl) {
        this.entityData.set(DATA_SUFFOCATING, bl);
        AttributeInstance attributeInstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attributeInstance != null) {
            if (bl) {
                attributeInstance.addOrUpdateTransientModifier(SUFFOCATING_MODIFIER);
            } else {
                attributeInstance.removeModifier(SUFFOCATING_MODIFIER_ID);
            }
        }
    }

    public boolean isSuffocating() {
        return this.entityData.get(DATA_SUFFOCATING);
    }

    @Override
    public boolean canStandOnFluid(FluidState fluidState) {
        return fluidState.is(FluidTags.LAVA);
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions entityDimensions, float f) {
        if (!this.level().isClientSide()) {
            return super.getPassengerAttachmentPoint(entity, entityDimensions, f);
        }
        float f2 = Math.min(0.25f, this.walkAnimation.speed());
        float f3 = this.walkAnimation.position();
        float f4 = 0.12f * Mth.cos(f3 * 1.5f) * 2.0f * f2;
        return super.getPassengerAttachmentPoint(entity, entityDimensions, f).add(0.0, f4 * f, 0.0);
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader levelReader) {
        return levelReader.isUnobstructed(this);
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        Player player;
        Entity entity;
        if (this.isSaddled() && (entity = this.getFirstPassenger()) instanceof Player && (player = (Player)entity).isHolding(Items.WARPED_FUNGUS_ON_A_STICK)) {
            return player;
        }
        return super.getControllingPassenger();
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity livingEntity) {
        Vec3[] vec3Array = new Vec3[]{Strider.getCollisionHorizontalEscapeVector(this.getBbWidth(), livingEntity.getBbWidth(), livingEntity.getYRot()), Strider.getCollisionHorizontalEscapeVector(this.getBbWidth(), livingEntity.getBbWidth(), livingEntity.getYRot() - 22.5f), Strider.getCollisionHorizontalEscapeVector(this.getBbWidth(), livingEntity.getBbWidth(), livingEntity.getYRot() + 22.5f), Strider.getCollisionHorizontalEscapeVector(this.getBbWidth(), livingEntity.getBbWidth(), livingEntity.getYRot() - 45.0f), Strider.getCollisionHorizontalEscapeVector(this.getBbWidth(), livingEntity.getBbWidth(), livingEntity.getYRot() + 45.0f)};
        LinkedHashSet linkedHashSet = Sets.newLinkedHashSet();
        double d = this.getBoundingBox().maxY;
        double d2 = this.getBoundingBox().minY - 0.5;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (Vec3 vec3 : vec3Array) {
            mutableBlockPos.set(this.getX() + vec3.x, d, this.getZ() + vec3.z);
            for (double d3 = d; d3 > d2; d3 -= 1.0) {
                linkedHashSet.add(mutableBlockPos.immutable());
                mutableBlockPos.move(Direction.DOWN);
            }
        }
        for (BlockPos blockPos : linkedHashSet) {
            double d4;
            if (this.level().getFluidState(blockPos).is(FluidTags.LAVA) || !DismountHelper.isBlockFloorValid(d4 = this.level().getBlockFloorHeight(blockPos))) continue;
            Vec3 vec3 = Vec3.upFromBottomCenterOf(blockPos, d4);
            for (Pose pose : livingEntity.getDismountPoses()) {
                AABB aABB = livingEntity.getLocalBoundsForPose(pose);
                if (!DismountHelper.canDismountTo(this.level(), livingEntity, aABB.move(vec3))) continue;
                livingEntity.setPose(pose);
                return vec3;
            }
        }
        return new Vec3(this.getX(), this.getBoundingBox().maxY, this.getZ());
    }

    @Override
    protected void tickRidden(Player player, Vec3 vec3) {
        this.setRot(player.getYRot(), player.getXRot() * 0.5f);
        this.yBodyRot = this.yHeadRot = this.getYRot();
        this.yRotO = this.yHeadRot;
        this.steering.tickBoost();
        super.tickRidden(player, vec3);
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 vec3) {
        return new Vec3(0.0, 0.0, 1.0);
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        return (float)(this.getAttributeValue(Attributes.MOVEMENT_SPEED) * (double)(this.isSuffocating() ? 0.35f : 0.55f) * (double)this.steering.boostFactor());
    }

    @Override
    protected float nextStep() {
        return this.moveDist + 0.6f;
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(this.isInLava() ? SoundEvents.STRIDER_STEP_LAVA : SoundEvents.STRIDER_STEP, 1.0f, 1.0f);
    }

    @Override
    public boolean boost() {
        return this.steering.boost(this.getRandom());
    }

    @Override
    protected void checkFallDamage(double d, boolean bl, BlockState blockState, BlockPos blockPos) {
        if (this.isInLava()) {
            this.resetFallDistance();
            return;
        }
        super.checkFallDamage(d, bl, blockState, blockPos);
    }

    @Override
    public void tick() {
        if (this.isBeingTempted() && this.random.nextInt(140) == 0) {
            this.makeSound(SoundEvents.STRIDER_HAPPY);
        } else if (this.isPanicking() && this.random.nextInt(60) == 0) {
            this.makeSound(SoundEvents.STRIDER_RETREAT);
        }
        if (!this.isNoAi()) {
            Strider strider;
            BlockState blockState = this.level().getBlockState(this.blockPosition());
            BlockState blockState2 = this.getBlockStateOnLegacy();
            boolean bl = blockState.is(BlockTags.STRIDER_WARM_BLOCKS) || blockState2.is(BlockTags.STRIDER_WARM_BLOCKS) || this.getFluidHeight(FluidTags.LAVA) > 0.0;
            Entity entity = this.getVehicle();
            boolean bl2 = entity instanceof Strider && (strider = (Strider)entity).isSuffocating();
            this.setSuffocating(!bl || bl2);
        }
        super.tick();
        this.floatStrider();
    }

    private boolean isBeingTempted() {
        return this.temptGoal != null && this.temptGoal.isRunning();
    }

    @Override
    protected boolean shouldPassengersInheritMalus() {
        return true;
    }

    private void floatStrider() {
        if (this.isInLava()) {
            CollisionContext collisionContext = CollisionContext.of(this);
            if (!collisionContext.isAbove(LiquidBlock.SHAPE_STABLE, this.blockPosition(), true) || this.level().getFluidState(this.blockPosition().above()).is(FluidTags.LAVA)) {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.5).add(0.0, 0.05, 0.0));
            } else {
                this.setOnGround(true);
            }
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MOVEMENT_SPEED, 0.175f);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isPanicking() || this.isBeingTempted()) {
            return null;
        }
        return SoundEvents.STRIDER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.STRIDER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.STRIDER_DEATH;
    }

    @Override
    protected boolean canAddPassenger(Entity entity) {
        return !this.isVehicle() && !this.isEyeInFluid(FluidTags.LAVA);
    }

    @Override
    public boolean isSensitiveToWater() {
        return true;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new StriderPathNavigation(this, level);
    }

    @Override
    public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
        if (levelReader.getBlockState(blockPos).getFluidState().is(FluidTags.LAVA)) {
            return 10.0f;
        }
        return this.isInLava() ? Float.NEGATIVE_INFINITY : 0.0f;
    }

    @Override
    @Nullable
    public Strider getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return EntityType.STRIDER.create(serverLevel, EntitySpawnReason.BREEDING);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(ItemTags.STRIDER_FOOD);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        boolean bl = this.isFood(player.getItemInHand(interactionHand));
        if (!bl && this.isSaddled() && !this.isVehicle() && !player.isSecondaryUseActive()) {
            if (!this.level().isClientSide) {
                player.startRiding(this);
            }
            return InteractionResult.SUCCESS;
        }
        InteractionResult interactionResult = super.mobInteract(player, interactionHand);
        if (!interactionResult.consumesAction()) {
            ItemStack itemStack = player.getItemInHand(interactionHand);
            if (this.isEquippableInSlot(itemStack, EquipmentSlot.SADDLE)) {
                return itemStack.interactLivingEntity(player, this, interactionHand);
            }
            return InteractionResult.PASS;
        }
        if (bl && !this.isSilent()) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.STRIDER_EAT, this.getSoundSource(), 1.0f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
        }
        return interactionResult;
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, 0.6f * this.getEyeHeight(), this.getBbWidth() * 0.4f);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData) {
        if (this.isBaby()) {
            return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData);
        }
        RandomSource randomSource = serverLevelAccessor.getRandom();
        if (randomSource.nextInt(30) == 0) {
            Mob mob = EntityType.ZOMBIFIED_PIGLIN.create(serverLevelAccessor.getLevel(), EntitySpawnReason.JOCKEY);
            if (mob != null) {
                spawnGroupData = this.spawnJockey(serverLevelAccessor, difficultyInstance, mob, new Zombie.ZombieGroupData(Zombie.getSpawnAsBabyOdds(randomSource), false));
                mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.WARPED_FUNGUS_ON_A_STICK));
                this.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
                this.setGuaranteedDrop(EquipmentSlot.SADDLE);
            }
        } else if (randomSource.nextInt(10) == 0) {
            AgeableMob ageableMob = EntityType.STRIDER.create(serverLevelAccessor.getLevel(), EntitySpawnReason.JOCKEY);
            if (ageableMob != null) {
                ageableMob.setAge(-24000);
                spawnGroupData = this.spawnJockey(serverLevelAccessor, difficultyInstance, ageableMob, null);
            }
        } else {
            spawnGroupData = new AgeableMob.AgeableMobGroupData(0.5f);
        }
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData);
    }

    private SpawnGroupData spawnJockey(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, Mob mob, @Nullable SpawnGroupData spawnGroupData) {
        mob.snapTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0f);
        mob.finalizeSpawn(serverLevelAccessor, difficultyInstance, EntitySpawnReason.JOCKEY, spawnGroupData);
        mob.startRiding(this, true);
        return new AgeableMob.AgeableMobGroupData(0.0f);
    }

    @Override
    @Nullable
    public /* synthetic */ AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return this.getBreedOffspring(serverLevel, ageableMob);
    }

    static class StriderGoToLavaGoal
    extends MoveToBlockGoal {
        private final Strider strider;

        StriderGoToLavaGoal(Strider strider, double d) {
            super(strider, d, 8, 2);
            this.strider = strider;
        }

        @Override
        public BlockPos getMoveToTarget() {
            return this.blockPos;
        }

        @Override
        public boolean canContinueToUse() {
            return !this.strider.isInLava() && this.isValidTarget(this.strider.level(), this.blockPos);
        }

        @Override
        public boolean canUse() {
            return !this.strider.isInLava() && super.canUse();
        }

        @Override
        public boolean shouldRecalculatePath() {
            return this.tryTicks % 20 == 0;
        }

        @Override
        protected boolean isValidTarget(LevelReader levelReader, BlockPos blockPos) {
            return levelReader.getBlockState(blockPos).is(Blocks.LAVA) && levelReader.getBlockState(blockPos.above()).isPathfindable(PathComputationType.LAND);
        }
    }

    static class StriderPathNavigation
    extends GroundPathNavigation {
        StriderPathNavigation(Strider strider, Level level) {
            super(strider, level);
        }

        @Override
        protected PathFinder createPathFinder(int n) {
            this.nodeEvaluator = new WalkNodeEvaluator();
            return new PathFinder(this.nodeEvaluator, n);
        }

        @Override
        protected boolean hasValidPathType(PathType pathType) {
            if (pathType == PathType.LAVA || pathType == PathType.DAMAGE_FIRE || pathType == PathType.DANGER_FIRE) {
                return true;
            }
            return super.hasValidPathType(pathType);
        }

        @Override
        public boolean isStableDestination(BlockPos blockPos) {
            return this.level.getBlockState(blockPos).is(Blocks.LAVA) || super.isStableDestination(blockPos);
        }
    }
}

