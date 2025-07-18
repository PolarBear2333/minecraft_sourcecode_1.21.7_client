/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.animal;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.AirRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class Bee
extends Animal
implements NeutralMob,
FlyingAnimal {
    public static final float FLAP_DEGREES_PER_TICK = 120.32113f;
    public static final int TICKS_PER_FLAP = Mth.ceil(1.4959966f);
    private static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(Bee.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME = SynchedEntityData.defineId(Bee.class, EntityDataSerializers.INT);
    private static final int FLAG_ROLL = 2;
    private static final int FLAG_HAS_STUNG = 4;
    private static final int FLAG_HAS_NECTAR = 8;
    private static final int STING_DEATH_COUNTDOWN = 1200;
    private static final int TICKS_BEFORE_GOING_TO_KNOWN_FLOWER = 600;
    private static final int TICKS_WITHOUT_NECTAR_BEFORE_GOING_HOME = 3600;
    private static final int MIN_ATTACK_DIST = 4;
    private static final int MAX_CROPS_GROWABLE = 10;
    private static final int POISON_SECONDS_NORMAL = 10;
    private static final int POISON_SECONDS_HARD = 18;
    private static final int TOO_FAR_DISTANCE = 48;
    private static final int HIVE_CLOSE_ENOUGH_DISTANCE = 2;
    private static final int RESTRICTED_WANDER_DISTANCE_REDUCTION = 24;
    private static final int DEFAULT_WANDER_DISTANCE_REDUCTION = 16;
    private static final int PATHFIND_TO_HIVE_WHEN_CLOSER_THAN = 16;
    private static final int HIVE_SEARCH_DISTANCE = 20;
    public static final String TAG_CROPS_GROWN_SINCE_POLLINATION = "CropsGrownSincePollination";
    public static final String TAG_CANNOT_ENTER_HIVE_TICKS = "CannotEnterHiveTicks";
    public static final String TAG_TICKS_SINCE_POLLINATION = "TicksSincePollination";
    public static final String TAG_HAS_STUNG = "HasStung";
    public static final String TAG_HAS_NECTAR = "HasNectar";
    public static final String TAG_FLOWER_POS = "flower_pos";
    public static final String TAG_HIVE_POS = "hive_pos";
    public static final boolean DEFAULT_HAS_NECTAR = false;
    private static final boolean DEFAULT_HAS_STUNG = false;
    private static final int DEFAULT_TICKS_SINCE_POLLINATION = 0;
    private static final int DEFAULT_CANNOT_ENTER_HIVE_TICKS = 0;
    private static final int DEFAULT_CROPS_GROWN_SINCE_POLLINATION = 0;
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    @Nullable
    private UUID persistentAngerTarget;
    private float rollAmount;
    private float rollAmountO;
    private int timeSinceSting;
    int ticksWithoutNectarSinceExitingHive = 0;
    private int stayOutOfHiveCountdown = 0;
    private int numCropsGrownSincePollination = 0;
    private static final int COOLDOWN_BEFORE_LOCATING_NEW_HIVE = 200;
    int remainingCooldownBeforeLocatingNewHive;
    private static final int COOLDOWN_BEFORE_LOCATING_NEW_FLOWER = 200;
    private static final int MIN_FIND_FLOWER_RETRY_COOLDOWN = 20;
    private static final int MAX_FIND_FLOWER_RETRY_COOLDOWN = 60;
    int remainingCooldownBeforeLocatingNewFlower = Mth.nextInt(this.random, 20, 60);
    @Nullable
    BlockPos savedFlowerPos;
    @Nullable
    BlockPos hivePos;
    BeePollinateGoal beePollinateGoal;
    BeeGoToHiveGoal goToHiveGoal;
    private BeeGoToKnownFlowerGoal goToKnownFlowerGoal;
    private int underWaterTicks;

    public Bee(EntityType<? extends Bee> entityType, Level level) {
        super((EntityType<? extends Animal>)entityType, level);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.lookControl = new BeeLookControl(this);
        this.setPathfindingMalus(PathType.DANGER_FIRE, -1.0f);
        this.setPathfindingMalus(PathType.WATER, -1.0f);
        this.setPathfindingMalus(PathType.WATER_BORDER, 16.0f);
        this.setPathfindingMalus(PathType.COCOA, -1.0f);
        this.setPathfindingMalus(PathType.FENCE, -1.0f);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_FLAGS_ID, (byte)0);
        builder.define(DATA_REMAINING_ANGER_TIME, 0);
    }

    @Override
    public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
        if (levelReader.getBlockState(blockPos).isAir()) {
            return 10.0f;
        }
        return 0.0f;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new BeeAttackGoal(this, 1.4f, true));
        this.goalSelector.addGoal(1, new BeeEnterHiveGoal());
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.25, itemStack -> itemStack.is(ItemTags.BEE_FOOD), false));
        this.goalSelector.addGoal(3, new ValidateHiveGoal());
        this.goalSelector.addGoal(3, new ValidateFlowerGoal());
        this.beePollinateGoal = new BeePollinateGoal();
        this.goalSelector.addGoal(4, this.beePollinateGoal);
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.25));
        this.goalSelector.addGoal(5, new BeeLocateHiveGoal());
        this.goToHiveGoal = new BeeGoToHiveGoal();
        this.goalSelector.addGoal(5, this.goToHiveGoal);
        this.goToKnownFlowerGoal = new BeeGoToKnownFlowerGoal();
        this.goalSelector.addGoal(6, this.goToKnownFlowerGoal);
        this.goalSelector.addGoal(7, new BeeGrowCropGoal());
        this.goalSelector.addGoal(8, new BeeWanderGoal());
        this.goalSelector.addGoal(9, new FloatGoal(this));
        this.targetSelector.addGoal(1, new BeeHurtByOtherGoal(this).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(2, new BeeBecomeAngryTargetGoal(this));
        this.targetSelector.addGoal(3, new ResetUniversalAngerTargetGoal<Bee>(this, true));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.storeNullable(TAG_HIVE_POS, BlockPos.CODEC, this.hivePos);
        valueOutput.storeNullable(TAG_FLOWER_POS, BlockPos.CODEC, this.savedFlowerPos);
        valueOutput.putBoolean(TAG_HAS_NECTAR, this.hasNectar());
        valueOutput.putBoolean(TAG_HAS_STUNG, this.hasStung());
        valueOutput.putInt(TAG_TICKS_SINCE_POLLINATION, this.ticksWithoutNectarSinceExitingHive);
        valueOutput.putInt(TAG_CANNOT_ENTER_HIVE_TICKS, this.stayOutOfHiveCountdown);
        valueOutput.putInt(TAG_CROPS_GROWN_SINCE_POLLINATION, this.numCropsGrownSincePollination);
        this.addPersistentAngerSaveData(valueOutput);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.setHasNectar(valueInput.getBooleanOr(TAG_HAS_NECTAR, false));
        this.setHasStung(valueInput.getBooleanOr(TAG_HAS_STUNG, false));
        this.ticksWithoutNectarSinceExitingHive = valueInput.getIntOr(TAG_TICKS_SINCE_POLLINATION, 0);
        this.stayOutOfHiveCountdown = valueInput.getIntOr(TAG_CANNOT_ENTER_HIVE_TICKS, 0);
        this.numCropsGrownSincePollination = valueInput.getIntOr(TAG_CROPS_GROWN_SINCE_POLLINATION, 0);
        this.hivePos = valueInput.read(TAG_HIVE_POS, BlockPos.CODEC).orElse(null);
        this.savedFlowerPos = valueInput.read(TAG_FLOWER_POS, BlockPos.CODEC).orElse(null);
        this.readPersistentAngerSaveData(this.level(), valueInput);
    }

    @Override
    public boolean doHurtTarget(ServerLevel serverLevel, Entity entity) {
        DamageSource damageSource = this.damageSources().sting(this);
        boolean bl = entity.hurtServer(serverLevel, damageSource, (int)this.getAttributeValue(Attributes.ATTACK_DAMAGE));
        if (bl) {
            EnchantmentHelper.doPostAttackEffects(serverLevel, entity, damageSource);
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity)entity;
                livingEntity.setStingerCount(livingEntity.getStingerCount() + 1);
                int n = 0;
                if (this.level().getDifficulty() == Difficulty.NORMAL) {
                    n = 10;
                } else if (this.level().getDifficulty() == Difficulty.HARD) {
                    n = 18;
                }
                if (n > 0) {
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.POISON, n * 20, 0), this);
                }
            }
            this.setHasStung(true);
            this.stopBeingAngry();
            this.playSound(SoundEvents.BEE_STING, 1.0f, 1.0f);
        }
        return bl;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.hasNectar() && this.getCropsGrownSincePollination() < 10 && this.random.nextFloat() < 0.05f) {
            for (int i = 0; i < this.random.nextInt(2) + 1; ++i) {
                this.spawnFluidParticle(this.level(), this.getX() - (double)0.3f, this.getX() + (double)0.3f, this.getZ() - (double)0.3f, this.getZ() + (double)0.3f, this.getY(0.5), ParticleTypes.FALLING_NECTAR);
            }
        }
        this.updateRollAmount();
    }

    private void spawnFluidParticle(Level level, double d, double d2, double d3, double d4, double d5, ParticleOptions particleOptions) {
        level.addParticle(particleOptions, Mth.lerp(level.random.nextDouble(), d, d2), d5, Mth.lerp(level.random.nextDouble(), d3, d4), 0.0, 0.0, 0.0);
    }

    void pathfindRandomlyTowards(BlockPos blockPos) {
        Vec3 vec3;
        Vec3 vec32 = Vec3.atBottomCenterOf(blockPos);
        int n = 0;
        BlockPos blockPos2 = this.blockPosition();
        int n2 = (int)vec32.y - blockPos2.getY();
        if (n2 > 2) {
            n = 4;
        } else if (n2 < -2) {
            n = -4;
        }
        int n3 = 6;
        int n4 = 8;
        int n5 = blockPos2.distManhattan(blockPos);
        if (n5 < 15) {
            n3 = n5 / 2;
            n4 = n5 / 2;
        }
        if ((vec3 = AirRandomPos.getPosTowards(this, n3, n4, n, vec32, 0.3141592741012573)) == null) {
            return;
        }
        this.navigation.setMaxVisitedNodesMultiplier(0.5f);
        this.navigation.moveTo(vec3.x, vec3.y, vec3.z, 1.0);
    }

    @Nullable
    public BlockPos getSavedFlowerPos() {
        return this.savedFlowerPos;
    }

    public boolean hasSavedFlowerPos() {
        return this.savedFlowerPos != null;
    }

    public void setSavedFlowerPos(BlockPos blockPos) {
        this.savedFlowerPos = blockPos;
    }

    @VisibleForDebug
    public int getTravellingTicks() {
        return Math.max(this.goToHiveGoal.travellingTicks, this.goToKnownFlowerGoal.travellingTicks);
    }

    @VisibleForDebug
    public List<BlockPos> getBlacklistedHives() {
        return this.goToHiveGoal.blacklistedTargets;
    }

    private boolean isTiredOfLookingForNectar() {
        return this.ticksWithoutNectarSinceExitingHive > 3600;
    }

    void dropHive() {
        this.hivePos = null;
        this.remainingCooldownBeforeLocatingNewHive = 200;
    }

    void dropFlower() {
        this.savedFlowerPos = null;
        this.remainingCooldownBeforeLocatingNewFlower = Mth.nextInt(this.random, 20, 60);
    }

    boolean wantsToEnterHive() {
        if (this.stayOutOfHiveCountdown > 0 || this.beePollinateGoal.isPollinating() || this.hasStung() || this.getTarget() != null) {
            return false;
        }
        boolean bl = this.isTiredOfLookingForNectar() || Bee.isNightOrRaining(this.level()) || this.hasNectar();
        return bl && !this.isHiveNearFire();
    }

    public static boolean isNightOrRaining(Level level) {
        return level.dimensionType().hasSkyLight() && (level.isDarkOutside() || level.isRaining());
    }

    public void setStayOutOfHiveCountdown(int n) {
        this.stayOutOfHiveCountdown = n;
    }

    public float getRollAmount(float f) {
        return Mth.lerp(f, this.rollAmountO, this.rollAmount);
    }

    private void updateRollAmount() {
        this.rollAmountO = this.rollAmount;
        this.rollAmount = this.isRolling() ? Math.min(1.0f, this.rollAmount + 0.2f) : Math.max(0.0f, this.rollAmount - 0.24f);
    }

    @Override
    protected void customServerAiStep(ServerLevel serverLevel) {
        boolean bl = this.hasStung();
        this.underWaterTicks = this.isInWater() ? ++this.underWaterTicks : 0;
        if (this.underWaterTicks > 20) {
            this.hurtServer(serverLevel, this.damageSources().drown(), 1.0f);
        }
        if (bl) {
            ++this.timeSinceSting;
            if (this.timeSinceSting % 5 == 0 && this.random.nextInt(Mth.clamp(1200 - this.timeSinceSting, 1, 1200)) == 0) {
                this.hurtServer(serverLevel, this.damageSources().generic(), this.getHealth());
            }
        }
        if (!this.hasNectar()) {
            ++this.ticksWithoutNectarSinceExitingHive;
        }
        this.updatePersistentAnger(serverLevel, false);
    }

    public void resetTicksWithoutNectarSinceExitingHive() {
        this.ticksWithoutNectarSinceExitingHive = 0;
    }

    private boolean isHiveNearFire() {
        BeehiveBlockEntity beehiveBlockEntity = this.getBeehiveBlockEntity();
        return beehiveBlockEntity != null && beehiveBlockEntity.isFireNearby();
    }

    @Override
    public int getRemainingPersistentAngerTime() {
        return this.entityData.get(DATA_REMAINING_ANGER_TIME);
    }

    @Override
    public void setRemainingPersistentAngerTime(int n) {
        this.entityData.set(DATA_REMAINING_ANGER_TIME, n);
    }

    @Override
    @Nullable
    public UUID getPersistentAngerTarget() {
        return this.persistentAngerTarget;
    }

    @Override
    public void setPersistentAngerTarget(@Nullable UUID uUID) {
        this.persistentAngerTarget = uUID;
    }

    @Override
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
    }

    private boolean doesHiveHaveSpace(BlockPos blockPos) {
        BlockEntity blockEntity = this.level().getBlockEntity(blockPos);
        if (blockEntity instanceof BeehiveBlockEntity) {
            return !((BeehiveBlockEntity)blockEntity).isFull();
        }
        return false;
    }

    @VisibleForDebug
    public boolean hasHive() {
        return this.hivePos != null;
    }

    @Nullable
    @VisibleForDebug
    public BlockPos getHivePos() {
        return this.hivePos;
    }

    @VisibleForDebug
    public GoalSelector getGoalSelector() {
        return this.goalSelector;
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendBeeInfo(this);
    }

    int getCropsGrownSincePollination() {
        return this.numCropsGrownSincePollination;
    }

    private void resetNumCropsGrownSincePollination() {
        this.numCropsGrownSincePollination = 0;
    }

    void incrementNumCropsGrownSincePollination() {
        ++this.numCropsGrownSincePollination;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide) {
            if (this.stayOutOfHiveCountdown > 0) {
                --this.stayOutOfHiveCountdown;
            }
            if (this.remainingCooldownBeforeLocatingNewHive > 0) {
                --this.remainingCooldownBeforeLocatingNewHive;
            }
            if (this.remainingCooldownBeforeLocatingNewFlower > 0) {
                --this.remainingCooldownBeforeLocatingNewFlower;
            }
            boolean bl = this.isAngry() && !this.hasStung() && this.getTarget() != null && this.getTarget().distanceToSqr(this) < 4.0;
            this.setRolling(bl);
            if (this.tickCount % 20 == 0 && !this.isHiveValid()) {
                this.hivePos = null;
            }
        }
    }

    @Nullable
    BeehiveBlockEntity getBeehiveBlockEntity() {
        if (this.hivePos == null) {
            return null;
        }
        if (this.isTooFarAway(this.hivePos)) {
            return null;
        }
        return this.level().getBlockEntity(this.hivePos, BlockEntityType.BEEHIVE).orElse(null);
    }

    boolean isHiveValid() {
        return this.getBeehiveBlockEntity() != null;
    }

    public boolean hasNectar() {
        return this.getFlag(8);
    }

    void setHasNectar(boolean bl) {
        if (bl) {
            this.resetTicksWithoutNectarSinceExitingHive();
        }
        this.setFlag(8, bl);
    }

    public boolean hasStung() {
        return this.getFlag(4);
    }

    private void setHasStung(boolean bl) {
        this.setFlag(4, bl);
    }

    private boolean isRolling() {
        return this.getFlag(2);
    }

    private void setRolling(boolean bl) {
        this.setFlag(2, bl);
    }

    boolean isTooFarAway(BlockPos blockPos) {
        return !this.closerThan(blockPos, 48);
    }

    private void setFlag(int n, boolean bl) {
        if (bl) {
            this.entityData.set(DATA_FLAGS_ID, (byte)(this.entityData.get(DATA_FLAGS_ID) | n));
        } else {
            this.entityData.set(DATA_FLAGS_ID, (byte)(this.entityData.get(DATA_FLAGS_ID) & ~n));
        }
    }

    private boolean getFlag(int n) {
        return (this.entityData.get(DATA_FLAGS_ID) & n) != 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.FLYING_SPEED, 0.6f).add(Attributes.MOVEMENT_SPEED, 0.3f).add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation flyingPathNavigation = new FlyingPathNavigation(this, level){

            @Override
            public boolean isStableDestination(BlockPos blockPos) {
                return !this.level.getBlockState(blockPos.below()).isAir();
            }

            @Override
            public void tick() {
                if (Bee.this.beePollinateGoal.isPollinating()) {
                    return;
                }
                super.tick();
            }
        };
        flyingPathNavigation.setCanOpenDoors(false);
        flyingPathNavigation.setCanFloat(false);
        flyingPathNavigation.setRequiredPathLength(48.0f);
        return flyingPathNavigation;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        FlowerBlock flowerBlock;
        BlockItem blockItem;
        Object object;
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (this.isFood(itemStack) && (object = itemStack.getItem()) instanceof BlockItem && (object = (blockItem = (BlockItem)object).getBlock()) instanceof FlowerBlock && (object = (flowerBlock = (FlowerBlock)object).getBeeInteractionEffect()) != null) {
            this.usePlayerItem(player, interactionHand, itemStack);
            if (!this.level().isClientSide) {
                this.addEffect((MobEffectInstance)object);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, interactionHand);
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(ItemTags.BEE_FOOD);
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.BEE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BEE_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    @Override
    @Nullable
    public Bee getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return EntityType.BEE.create(serverLevel, EntitySpawnReason.BREEDING);
    }

    @Override
    protected void checkFallDamage(double d, boolean bl, BlockState blockState, BlockPos blockPos) {
    }

    @Override
    public boolean isFlapping() {
        return this.isFlying() && this.tickCount % TICKS_PER_FLAP == 0;
    }

    @Override
    public boolean isFlying() {
        return !this.onGround();
    }

    public void dropOffNectar() {
        this.setHasNectar(false);
        this.resetNumCropsGrownSincePollination();
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        if (this.isInvulnerableTo(serverLevel, damageSource)) {
            return false;
        }
        this.beePollinateGoal.stopPollinating();
        return super.hurtServer(serverLevel, damageSource, f);
    }

    @Override
    protected void jumpInLiquid(TagKey<Fluid> tagKey) {
        this.setDeltaMovement(this.getDeltaMovement().add(0.0, 0.01, 0.0));
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, 0.5f * this.getEyeHeight(), this.getBbWidth() * 0.2f);
    }

    boolean closerThan(BlockPos blockPos, int n) {
        return blockPos.closerThan(this.blockPosition(), n);
    }

    public void setHivePos(BlockPos blockPos) {
        this.hivePos = blockPos;
    }

    public static boolean attractsBees(BlockState blockState) {
        if (blockState.is(BlockTags.BEE_ATTRACTIVE)) {
            if (blockState.getValueOrElse(BlockStateProperties.WATERLOGGED, false).booleanValue()) {
                return false;
            }
            if (blockState.is(Blocks.SUNFLOWER)) {
                return blockState.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER;
            }
            return true;
        }
        return false;
    }

    @Override
    @Nullable
    public /* synthetic */ AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return this.getBreedOffspring(serverLevel, ageableMob);
    }

    class BeeLookControl
    extends LookControl {
        BeeLookControl(Mob mob) {
            super(mob);
        }

        @Override
        public void tick() {
            if (Bee.this.isAngry()) {
                return;
            }
            super.tick();
        }

        @Override
        protected boolean resetXRotOnTick() {
            return !Bee.this.beePollinateGoal.isPollinating();
        }
    }

    class BeeAttackGoal
    extends MeleeAttackGoal {
        BeeAttackGoal(PathfinderMob pathfinderMob, double d, boolean bl) {
            super(pathfinderMob, d, bl);
        }

        @Override
        public boolean canUse() {
            return super.canUse() && Bee.this.isAngry() && !Bee.this.hasStung();
        }

        @Override
        public boolean canContinueToUse() {
            return super.canContinueToUse() && Bee.this.isAngry() && !Bee.this.hasStung();
        }
    }

    class BeeEnterHiveGoal
    extends BaseBeeGoal {
        BeeEnterHiveGoal() {
        }

        @Override
        public boolean canBeeUse() {
            BeehiveBlockEntity beehiveBlockEntity;
            if (Bee.this.hivePos != null && Bee.this.wantsToEnterHive() && Bee.this.hivePos.closerToCenterThan(Bee.this.position(), 2.0) && (beehiveBlockEntity = Bee.this.getBeehiveBlockEntity()) != null) {
                if (beehiveBlockEntity.isFull()) {
                    Bee.this.hivePos = null;
                } else {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean canBeeContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            BeehiveBlockEntity beehiveBlockEntity = Bee.this.getBeehiveBlockEntity();
            if (beehiveBlockEntity != null) {
                beehiveBlockEntity.addOccupant(Bee.this);
            }
        }
    }

    class ValidateHiveGoal
    extends BaseBeeGoal {
        private final int VALIDATE_HIVE_COOLDOWN;
        private long lastValidateTick;

        ValidateHiveGoal() {
            this.VALIDATE_HIVE_COOLDOWN = Mth.nextInt(Bee.this.random, 20, 40);
            this.lastValidateTick = -1L;
        }

        @Override
        public void start() {
            if (Bee.this.hivePos != null && Bee.this.level().isLoaded(Bee.this.hivePos) && !Bee.this.isHiveValid()) {
                Bee.this.dropHive();
            }
            this.lastValidateTick = Bee.this.level().getGameTime();
        }

        @Override
        public boolean canBeeUse() {
            return Bee.this.level().getGameTime() > this.lastValidateTick + (long)this.VALIDATE_HIVE_COOLDOWN;
        }

        @Override
        public boolean canBeeContinueToUse() {
            return false;
        }
    }

    class ValidateFlowerGoal
    extends BaseBeeGoal {
        private final int validateFlowerCooldown;
        private long lastValidateTick;

        ValidateFlowerGoal() {
            this.validateFlowerCooldown = Mth.nextInt(Bee.this.random, 20, 40);
            this.lastValidateTick = -1L;
        }

        @Override
        public void start() {
            if (Bee.this.savedFlowerPos != null && Bee.this.level().isLoaded(Bee.this.savedFlowerPos) && !this.isFlower(Bee.this.savedFlowerPos)) {
                Bee.this.dropFlower();
            }
            this.lastValidateTick = Bee.this.level().getGameTime();
        }

        @Override
        public boolean canBeeUse() {
            return Bee.this.level().getGameTime() > this.lastValidateTick + (long)this.validateFlowerCooldown;
        }

        @Override
        public boolean canBeeContinueToUse() {
            return false;
        }

        private boolean isFlower(BlockPos blockPos) {
            return Bee.attractsBees(Bee.this.level().getBlockState(blockPos));
        }
    }

    class BeePollinateGoal
    extends BaseBeeGoal {
        private static final int MIN_POLLINATION_TICKS = 400;
        private static final double ARRIVAL_THRESHOLD = 0.1;
        private static final int POSITION_CHANGE_CHANCE = 25;
        private static final float SPEED_MODIFIER = 0.35f;
        private static final float HOVER_HEIGHT_WITHIN_FLOWER = 0.6f;
        private static final float HOVER_POS_OFFSET = 0.33333334f;
        private static final int FLOWER_SEARCH_RADIUS = 5;
        private int successfulPollinatingTicks;
        private int lastSoundPlayedTick;
        private boolean pollinating;
        @Nullable
        private Vec3 hoverPos;
        private int pollinatingTicks;
        private static final int MAX_POLLINATING_TICKS = 600;
        private Long2LongOpenHashMap unreachableFlowerCache;

        BeePollinateGoal() {
            this.unreachableFlowerCache = new Long2LongOpenHashMap();
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canBeeUse() {
            if (Bee.this.remainingCooldownBeforeLocatingNewFlower > 0) {
                return false;
            }
            if (Bee.this.hasNectar()) {
                return false;
            }
            if (Bee.this.level().isRaining()) {
                return false;
            }
            Optional<BlockPos> optional = this.findNearbyFlower();
            if (optional.isPresent()) {
                Bee.this.savedFlowerPos = optional.get();
                Bee.this.navigation.moveTo((double)Bee.this.savedFlowerPos.getX() + 0.5, (double)Bee.this.savedFlowerPos.getY() + 0.5, (double)Bee.this.savedFlowerPos.getZ() + 0.5, 1.2f);
                return true;
            }
            Bee.this.remainingCooldownBeforeLocatingNewFlower = Mth.nextInt(Bee.this.random, 20, 60);
            return false;
        }

        @Override
        public boolean canBeeContinueToUse() {
            if (!this.pollinating) {
                return false;
            }
            if (!Bee.this.hasSavedFlowerPos()) {
                return false;
            }
            if (Bee.this.level().isRaining()) {
                return false;
            }
            if (this.hasPollinatedLongEnough()) {
                return Bee.this.random.nextFloat() < 0.2f;
            }
            return true;
        }

        private boolean hasPollinatedLongEnough() {
            return this.successfulPollinatingTicks > 400;
        }

        boolean isPollinating() {
            return this.pollinating;
        }

        void stopPollinating() {
            this.pollinating = false;
        }

        @Override
        public void start() {
            this.successfulPollinatingTicks = 0;
            this.pollinatingTicks = 0;
            this.lastSoundPlayedTick = 0;
            this.pollinating = true;
            Bee.this.resetTicksWithoutNectarSinceExitingHive();
        }

        @Override
        public void stop() {
            if (this.hasPollinatedLongEnough()) {
                Bee.this.setHasNectar(true);
            }
            this.pollinating = false;
            Bee.this.navigation.stop();
            Bee.this.remainingCooldownBeforeLocatingNewFlower = 200;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (!Bee.this.hasSavedFlowerPos()) {
                return;
            }
            ++this.pollinatingTicks;
            if (this.pollinatingTicks > 600) {
                Bee.this.dropFlower();
                this.pollinating = false;
                Bee.this.remainingCooldownBeforeLocatingNewFlower = 200;
                return;
            }
            Vec3 vec3 = Vec3.atBottomCenterOf(Bee.this.savedFlowerPos).add(0.0, 0.6f, 0.0);
            if (vec3.distanceTo(Bee.this.position()) > 1.0) {
                this.hoverPos = vec3;
                this.setWantedPos();
                return;
            }
            if (this.hoverPos == null) {
                this.hoverPos = vec3;
            }
            boolean bl = Bee.this.position().distanceTo(this.hoverPos) <= 0.1;
            boolean bl2 = true;
            if (!bl && this.pollinatingTicks > 600) {
                Bee.this.dropFlower();
                return;
            }
            if (bl) {
                boolean bl3;
                boolean bl4 = bl3 = Bee.this.random.nextInt(25) == 0;
                if (bl3) {
                    this.hoverPos = new Vec3(vec3.x() + (double)this.getOffset(), vec3.y(), vec3.z() + (double)this.getOffset());
                    Bee.this.navigation.stop();
                } else {
                    bl2 = false;
                }
                Bee.this.getLookControl().setLookAt(vec3.x(), vec3.y(), vec3.z());
            }
            if (bl2) {
                this.setWantedPos();
            }
            ++this.successfulPollinatingTicks;
            if (Bee.this.random.nextFloat() < 0.05f && this.successfulPollinatingTicks > this.lastSoundPlayedTick + 60) {
                this.lastSoundPlayedTick = this.successfulPollinatingTicks;
                Bee.this.playSound(SoundEvents.BEE_POLLINATE, 1.0f, 1.0f);
            }
        }

        private void setWantedPos() {
            Bee.this.getMoveControl().setWantedPosition(this.hoverPos.x(), this.hoverPos.y(), this.hoverPos.z(), 0.35f);
        }

        private float getOffset() {
            return (Bee.this.random.nextFloat() * 2.0f - 1.0f) * 0.33333334f;
        }

        private Optional<BlockPos> findNearbyFlower() {
            Iterable<BlockPos> iterable = BlockPos.withinManhattan(Bee.this.blockPosition(), 5, 5, 5);
            Long2LongOpenHashMap long2LongOpenHashMap = new Long2LongOpenHashMap();
            for (BlockPos blockPos : iterable) {
                long l = this.unreachableFlowerCache.getOrDefault(blockPos.asLong(), Long.MIN_VALUE);
                if (Bee.this.level().getGameTime() < l) {
                    long2LongOpenHashMap.put(blockPos.asLong(), l);
                    continue;
                }
                if (!Bee.attractsBees(Bee.this.level().getBlockState(blockPos))) continue;
                Path path = Bee.this.navigation.createPath(blockPos, 1);
                if (path != null && path.canReach()) {
                    return Optional.of(blockPos);
                }
                long2LongOpenHashMap.put(blockPos.asLong(), Bee.this.level().getGameTime() + 600L);
            }
            this.unreachableFlowerCache = long2LongOpenHashMap;
            return Optional.empty();
        }
    }

    class BeeLocateHiveGoal
    extends BaseBeeGoal {
        BeeLocateHiveGoal() {
        }

        @Override
        public boolean canBeeUse() {
            return Bee.this.remainingCooldownBeforeLocatingNewHive == 0 && !Bee.this.hasHive() && Bee.this.wantsToEnterHive();
        }

        @Override
        public boolean canBeeContinueToUse() {
            return false;
        }

        @Override
        public void start() {
            Bee.this.remainingCooldownBeforeLocatingNewHive = 200;
            List<BlockPos> list = this.findNearbyHivesWithSpace();
            if (list.isEmpty()) {
                return;
            }
            for (BlockPos blockPos : list) {
                if (Bee.this.goToHiveGoal.isTargetBlacklisted(blockPos)) continue;
                Bee.this.hivePos = blockPos;
                return;
            }
            Bee.this.goToHiveGoal.clearBlacklist();
            Bee.this.hivePos = list.get(0);
        }

        private List<BlockPos> findNearbyHivesWithSpace() {
            BlockPos blockPos = Bee.this.blockPosition();
            PoiManager poiManager = ((ServerLevel)Bee.this.level()).getPoiManager();
            Stream<PoiRecord> stream = poiManager.getInRange(holder -> holder.is(PoiTypeTags.BEE_HOME), blockPos, 20, PoiManager.Occupancy.ANY);
            return stream.map(PoiRecord::getPos).filter(Bee.this::doesHiveHaveSpace).sorted(Comparator.comparingDouble(blockPos2 -> blockPos2.distSqr(blockPos))).collect(Collectors.toList());
        }
    }

    @VisibleForDebug
    public class BeeGoToHiveGoal
    extends BaseBeeGoal {
        public static final int MAX_TRAVELLING_TICKS = 2400;
        int travellingTicks;
        private static final int MAX_BLACKLISTED_TARGETS = 3;
        final List<BlockPos> blacklistedTargets;
        @Nullable
        private Path lastPath;
        private static final int TICKS_BEFORE_HIVE_DROP = 60;
        private int ticksStuck;

        BeeGoToHiveGoal() {
            this.blacklistedTargets = Lists.newArrayList();
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canBeeUse() {
            return Bee.this.hivePos != null && !Bee.this.isTooFarAway(Bee.this.hivePos) && !Bee.this.hasHome() && Bee.this.wantsToEnterHive() && !this.hasReachedTarget(Bee.this.hivePos) && Bee.this.level().getBlockState(Bee.this.hivePos).is(BlockTags.BEEHIVES);
        }

        @Override
        public boolean canBeeContinueToUse() {
            return this.canBeeUse();
        }

        @Override
        public void start() {
            this.travellingTicks = 0;
            this.ticksStuck = 0;
            super.start();
        }

        @Override
        public void stop() {
            this.travellingTicks = 0;
            this.ticksStuck = 0;
            Bee.this.navigation.stop();
            Bee.this.navigation.resetMaxVisitedNodesMultiplier();
        }

        @Override
        public void tick() {
            if (Bee.this.hivePos == null) {
                return;
            }
            ++this.travellingTicks;
            if (this.travellingTicks > this.adjustedTickDelay(2400)) {
                this.dropAndBlacklistHive();
                return;
            }
            if (Bee.this.navigation.isInProgress()) {
                return;
            }
            if (Bee.this.closerThan(Bee.this.hivePos, 16)) {
                boolean bl = this.pathfindDirectlyTowards(Bee.this.hivePos);
                if (!bl) {
                    this.dropAndBlacklistHive();
                } else if (this.lastPath != null && Bee.this.navigation.getPath().sameAs(this.lastPath)) {
                    ++this.ticksStuck;
                    if (this.ticksStuck > 60) {
                        Bee.this.dropHive();
                        this.ticksStuck = 0;
                    }
                } else {
                    this.lastPath = Bee.this.navigation.getPath();
                }
                return;
            }
            if (Bee.this.isTooFarAway(Bee.this.hivePos)) {
                Bee.this.dropHive();
                return;
            }
            Bee.this.pathfindRandomlyTowards(Bee.this.hivePos);
        }

        private boolean pathfindDirectlyTowards(BlockPos blockPos) {
            int n = Bee.this.closerThan(blockPos, 3) ? 1 : 2;
            Bee.this.navigation.setMaxVisitedNodesMultiplier(10.0f);
            Bee.this.navigation.moveTo(blockPos.getX(), blockPos.getY(), blockPos.getZ(), n, 1.0);
            return Bee.this.navigation.getPath() != null && Bee.this.navigation.getPath().canReach();
        }

        boolean isTargetBlacklisted(BlockPos blockPos) {
            return this.blacklistedTargets.contains(blockPos);
        }

        private void blacklistTarget(BlockPos blockPos) {
            this.blacklistedTargets.add(blockPos);
            while (this.blacklistedTargets.size() > 3) {
                this.blacklistedTargets.remove(0);
            }
        }

        void clearBlacklist() {
            this.blacklistedTargets.clear();
        }

        private void dropAndBlacklistHive() {
            if (Bee.this.hivePos != null) {
                this.blacklistTarget(Bee.this.hivePos);
            }
            Bee.this.dropHive();
        }

        private boolean hasReachedTarget(BlockPos blockPos) {
            if (Bee.this.closerThan(blockPos, 2)) {
                return true;
            }
            Path path = Bee.this.navigation.getPath();
            return path != null && path.getTarget().equals(blockPos) && path.canReach() && path.isDone();
        }
    }

    public class BeeGoToKnownFlowerGoal
    extends BaseBeeGoal {
        private static final int MAX_TRAVELLING_TICKS = 2400;
        int travellingTicks;

        BeeGoToKnownFlowerGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canBeeUse() {
            return Bee.this.savedFlowerPos != null && !Bee.this.hasHome() && this.wantsToGoToKnownFlower() && !Bee.this.closerThan(Bee.this.savedFlowerPos, 2);
        }

        @Override
        public boolean canBeeContinueToUse() {
            return this.canBeeUse();
        }

        @Override
        public void start() {
            this.travellingTicks = 0;
            super.start();
        }

        @Override
        public void stop() {
            this.travellingTicks = 0;
            Bee.this.navigation.stop();
            Bee.this.navigation.resetMaxVisitedNodesMultiplier();
        }

        @Override
        public void tick() {
            if (Bee.this.savedFlowerPos == null) {
                return;
            }
            ++this.travellingTicks;
            if (this.travellingTicks > this.adjustedTickDelay(2400)) {
                Bee.this.dropFlower();
                return;
            }
            if (Bee.this.navigation.isInProgress()) {
                return;
            }
            if (Bee.this.isTooFarAway(Bee.this.savedFlowerPos)) {
                Bee.this.dropFlower();
                return;
            }
            Bee.this.pathfindRandomlyTowards(Bee.this.savedFlowerPos);
        }

        private boolean wantsToGoToKnownFlower() {
            return Bee.this.ticksWithoutNectarSinceExitingHive > 600;
        }
    }

    class BeeGrowCropGoal
    extends BaseBeeGoal {
        static final int GROW_CHANCE = 30;

        BeeGrowCropGoal() {
        }

        @Override
        public boolean canBeeUse() {
            if (Bee.this.getCropsGrownSincePollination() >= 10) {
                return false;
            }
            if (Bee.this.random.nextFloat() < 0.3f) {
                return false;
            }
            return Bee.this.hasNectar() && Bee.this.isHiveValid();
        }

        @Override
        public boolean canBeeContinueToUse() {
            return this.canBeeUse();
        }

        @Override
        public void tick() {
            if (Bee.this.random.nextInt(this.adjustedTickDelay(30)) != 0) {
                return;
            }
            for (int i = 1; i <= 2; ++i) {
                BonemealableBlock bonemealableBlock;
                BlockPos blockPos = Bee.this.blockPosition().below(i);
                BlockState blockState = Bee.this.level().getBlockState(blockPos);
                Block block = blockState.getBlock();
                BlockState blockState2 = null;
                if (!blockState.is(BlockTags.BEE_GROWABLES)) continue;
                if (block instanceof CropBlock) {
                    CropBlock cropBlock = (CropBlock)block;
                    if (!cropBlock.isMaxAge(blockState)) {
                        blockState2 = cropBlock.getStateForAge(cropBlock.getAge(blockState) + 1);
                    }
                } else if (block instanceof StemBlock) {
                    int n = blockState.getValue(StemBlock.AGE);
                    if (n < 7) {
                        blockState2 = (BlockState)blockState.setValue(StemBlock.AGE, n + 1);
                    }
                } else if (blockState.is(Blocks.SWEET_BERRY_BUSH)) {
                    int n = blockState.getValue(SweetBerryBushBlock.AGE);
                    if (n < 3) {
                        blockState2 = (BlockState)blockState.setValue(SweetBerryBushBlock.AGE, n + 1);
                    }
                } else if ((blockState.is(Blocks.CAVE_VINES) || blockState.is(Blocks.CAVE_VINES_PLANT)) && (bonemealableBlock = (BonemealableBlock)((Object)blockState.getBlock())).isValidBonemealTarget(Bee.this.level(), blockPos, blockState)) {
                    bonemealableBlock.performBonemeal((ServerLevel)Bee.this.level(), Bee.this.random, blockPos, blockState);
                    blockState2 = Bee.this.level().getBlockState(blockPos);
                }
                if (blockState2 == null) continue;
                Bee.this.level().levelEvent(2011, blockPos, 15);
                Bee.this.level().setBlockAndUpdate(blockPos, blockState2);
                Bee.this.incrementNumCropsGrownSincePollination();
            }
        }
    }

    class BeeWanderGoal
    extends Goal {
        BeeWanderGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return Bee.this.navigation.isDone() && Bee.this.random.nextInt(10) == 0;
        }

        @Override
        public boolean canContinueToUse() {
            return Bee.this.navigation.isInProgress();
        }

        @Override
        public void start() {
            Vec3 vec3 = this.findPos();
            if (vec3 != null) {
                Bee.this.navigation.moveTo(Bee.this.navigation.createPath(BlockPos.containing(vec3), 1), 1.0);
            }
        }

        @Nullable
        private Vec3 findPos() {
            Vec3 vec3;
            if (Bee.this.isHiveValid() && !Bee.this.closerThan(Bee.this.hivePos, this.getWanderThreshold())) {
                Vec3 vec32 = Vec3.atCenterOf(Bee.this.hivePos);
                vec3 = vec32.subtract(Bee.this.position()).normalize();
            } else {
                vec3 = Bee.this.getViewVector(0.0f);
            }
            int n = 8;
            Vec3 vec33 = HoverRandomPos.getPos(Bee.this, 8, 7, vec3.x, vec3.z, 1.5707964f, 3, 1);
            if (vec33 != null) {
                return vec33;
            }
            return AirAndWaterRandomPos.getPos(Bee.this, 8, 4, -2, vec3.x, vec3.z, 1.5707963705062866);
        }

        private int getWanderThreshold() {
            int n = Bee.this.hasHive() || Bee.this.hasSavedFlowerPos() ? 24 : 16;
            return 48 - n;
        }
    }

    class BeeHurtByOtherGoal
    extends HurtByTargetGoal {
        BeeHurtByOtherGoal(Bee bee2) {
            super(bee2, new Class[0]);
        }

        @Override
        public boolean canContinueToUse() {
            return Bee.this.isAngry() && super.canContinueToUse();
        }

        @Override
        protected void alertOther(Mob mob, LivingEntity livingEntity) {
            if (mob instanceof Bee && this.mob.hasLineOfSight(livingEntity)) {
                mob.setTarget(livingEntity);
            }
        }
    }

    static class BeeBecomeAngryTargetGoal
    extends NearestAttackableTargetGoal<Player> {
        BeeBecomeAngryTargetGoal(Bee bee) {
            super(bee, Player.class, 10, true, false, bee::isAngryAt);
        }

        @Override
        public boolean canUse() {
            return this.beeCanTarget() && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            boolean bl = this.beeCanTarget();
            if (!bl || this.mob.getTarget() == null) {
                this.targetMob = null;
                return false;
            }
            return super.canContinueToUse();
        }

        private boolean beeCanTarget() {
            Bee bee = (Bee)this.mob;
            return bee.isAngry() && !bee.hasStung();
        }
    }

    abstract class BaseBeeGoal
    extends Goal {
        BaseBeeGoal() {
        }

        public abstract boolean canBeeUse();

        public abstract boolean canBeeContinueToUse();

        @Override
        public boolean canUse() {
            return this.canBeeUse() && !Bee.this.isAngry();
        }

        @Override
        public boolean canContinueToUse() {
            return this.canBeeContinueToUse() && !Bee.this.isAngry();
        }
    }
}

