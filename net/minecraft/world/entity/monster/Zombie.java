/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.monster;

import com.google.common.annotations.VisibleForTesting;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RemoveBlockGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class Zombie
extends Monster {
    private static final ResourceLocation SPEED_MODIFIER_BABY_ID = ResourceLocation.withDefaultNamespace("baby");
    private static final AttributeModifier SPEED_MODIFIER_BABY = new AttributeModifier(SPEED_MODIFIER_BABY_ID, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    private static final ResourceLocation REINFORCEMENT_CALLER_CHARGE_ID = ResourceLocation.withDefaultNamespace("reinforcement_caller_charge");
    private static final AttributeModifier ZOMBIE_REINFORCEMENT_CALLEE_CHARGE = new AttributeModifier(ResourceLocation.withDefaultNamespace("reinforcement_callee_charge"), -0.05f, AttributeModifier.Operation.ADD_VALUE);
    private static final ResourceLocation LEADER_ZOMBIE_BONUS_ID = ResourceLocation.withDefaultNamespace("leader_zombie_bonus");
    private static final ResourceLocation ZOMBIE_RANDOM_SPAWN_BONUS_ID = ResourceLocation.withDefaultNamespace("zombie_random_spawn_bonus");
    private static final EntityDataAccessor<Boolean> DATA_BABY_ID = SynchedEntityData.defineId(Zombie.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_SPECIAL_TYPE_ID = SynchedEntityData.defineId(Zombie.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_DROWNED_CONVERSION_ID = SynchedEntityData.defineId(Zombie.class, EntityDataSerializers.BOOLEAN);
    public static final float ZOMBIE_LEADER_CHANCE = 0.05f;
    public static final int REINFORCEMENT_ATTEMPTS = 50;
    public static final int REINFORCEMENT_RANGE_MAX = 40;
    public static final int REINFORCEMENT_RANGE_MIN = 7;
    private static final int NOT_CONVERTING = -1;
    private static final EntityDimensions BABY_DIMENSIONS = EntityType.ZOMBIE.getDimensions().scale(0.5f).withEyeHeight(0.93f);
    private static final float BREAK_DOOR_CHANCE = 0.1f;
    private static final Predicate<Difficulty> DOOR_BREAKING_PREDICATE = difficulty -> difficulty == Difficulty.HARD;
    private static final boolean DEFAULT_BABY = false;
    private static final boolean DEFAULT_CAN_BREAK_DOORS = false;
    private static final int DEFAULT_IN_WATER_TIME = 0;
    private final BreakDoorGoal breakDoorGoal = new BreakDoorGoal(this, DOOR_BREAKING_PREDICATE);
    private boolean canBreakDoors = false;
    private int inWaterTime = 0;
    private int conversionTime;

    public Zombie(EntityType<? extends Zombie> entityType, Level level) {
        super((EntityType<? extends Monster>)entityType, level);
    }

    public Zombie(Level level) {
        this((EntityType<? extends Zombie>)EntityType.ZOMBIE, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(4, new ZombieAttackTurtleEggGoal((PathfinderMob)this, 1.0, 3));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.addBehaviourGoals();
    }

    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(2, new ZombieAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 1.0, true, 4, this::canBreakDoors));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, new Class[0]).setAlertOthers(ZombifiedPiglin.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<Player>((Mob)this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<AbstractVillager>((Mob)this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<IronGolem>((Mob)this, IronGolem.class, true));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<Turtle>(this, Turtle.class, 10, true, false, Turtle.BABY_ON_LAND_SELECTOR));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.FOLLOW_RANGE, 35.0).add(Attributes.MOVEMENT_SPEED, 0.23f).add(Attributes.ATTACK_DAMAGE, 3.0).add(Attributes.ARMOR, 2.0).add(Attributes.SPAWN_REINFORCEMENTS_CHANCE);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BABY_ID, false);
        builder.define(DATA_SPECIAL_TYPE_ID, 0);
        builder.define(DATA_DROWNED_CONVERSION_ID, false);
    }

    public boolean isUnderWaterConverting() {
        return this.getEntityData().get(DATA_DROWNED_CONVERSION_ID);
    }

    public boolean canBreakDoors() {
        return this.canBreakDoors;
    }

    public void setCanBreakDoors(boolean bl) {
        if (this.navigation.canNavigateGround()) {
            if (this.canBreakDoors != bl) {
                this.canBreakDoors = bl;
                this.navigation.setCanOpenDoors(bl);
                if (bl) {
                    this.goalSelector.addGoal(1, this.breakDoorGoal);
                } else {
                    this.goalSelector.removeGoal(this.breakDoorGoal);
                }
            }
        } else if (this.canBreakDoors) {
            this.goalSelector.removeGoal(this.breakDoorGoal);
            this.canBreakDoors = false;
        }
    }

    @Override
    public boolean isBaby() {
        return this.getEntityData().get(DATA_BABY_ID);
    }

    @Override
    protected int getBaseExperienceReward(ServerLevel serverLevel) {
        if (this.isBaby()) {
            this.xpReward = (int)((double)this.xpReward * 2.5);
        }
        return super.getBaseExperienceReward(serverLevel);
    }

    @Override
    public void setBaby(boolean bl) {
        this.getEntityData().set(DATA_BABY_ID, bl);
        if (this.level() != null && !this.level().isClientSide) {
            AttributeInstance attributeInstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
            attributeInstance.removeModifier(SPEED_MODIFIER_BABY_ID);
            if (bl) {
                attributeInstance.addTransientModifier(SPEED_MODIFIER_BABY);
            }
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (DATA_BABY_ID.equals(entityDataAccessor)) {
            this.refreshDimensions();
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    protected boolean convertsInWater() {
        return true;
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide && this.isAlive() && !this.isNoAi()) {
            if (this.isUnderWaterConverting()) {
                --this.conversionTime;
                if (this.conversionTime < 0) {
                    this.doUnderWaterConversion();
                }
            } else if (this.convertsInWater()) {
                if (this.isEyeInFluid(FluidTags.WATER)) {
                    ++this.inWaterTime;
                    if (this.inWaterTime >= 600) {
                        this.startUnderWaterConversion(300);
                    }
                } else {
                    this.inWaterTime = -1;
                }
            }
        }
        super.tick();
    }

    @Override
    public void aiStep() {
        if (this.isAlive()) {
            boolean bl;
            boolean bl2 = bl = this.isSunSensitive() && this.isSunBurnTick();
            if (bl) {
                ItemStack itemStack = this.getItemBySlot(EquipmentSlot.HEAD);
                if (!itemStack.isEmpty()) {
                    if (itemStack.isDamageableItem()) {
                        Item item = itemStack.getItem();
                        itemStack.setDamageValue(itemStack.getDamageValue() + this.random.nextInt(2));
                        if (itemStack.getDamageValue() >= itemStack.getMaxDamage()) {
                            this.onEquippedItemBroken(item, EquipmentSlot.HEAD);
                            this.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                        }
                    }
                    bl = false;
                }
                if (bl) {
                    this.igniteForSeconds(8.0f);
                }
            }
        }
        super.aiStep();
    }

    private void startUnderWaterConversion(int n) {
        this.conversionTime = n;
        this.getEntityData().set(DATA_DROWNED_CONVERSION_ID, true);
    }

    protected void doUnderWaterConversion() {
        this.convertToZombieType(EntityType.DROWNED);
        if (!this.isSilent()) {
            this.level().levelEvent(null, 1040, this.blockPosition(), 0);
        }
    }

    protected void convertToZombieType(EntityType<? extends Zombie> entityType) {
        this.convertTo(entityType, ConversionParams.single(this, true, true), zombie -> zombie.handleAttributes(zombie.level().getCurrentDifficultyAt(zombie.blockPosition()).getSpecialMultiplier()));
    }

    @VisibleForTesting
    public boolean convertVillagerToZombieVillager(ServerLevel serverLevel, Villager villager) {
        ZombieVillager zombieVillager2 = villager.convertTo(EntityType.ZOMBIE_VILLAGER, ConversionParams.single(villager, true, true), zombieVillager -> {
            zombieVillager.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(zombieVillager.blockPosition()), EntitySpawnReason.CONVERSION, new ZombieGroupData(false, true));
            zombieVillager.setVillagerData(villager.getVillagerData());
            zombieVillager.setGossips(villager.getGossips().copy());
            zombieVillager.setTradeOffers(villager.getOffers().copy());
            zombieVillager.setVillagerXp(villager.getVillagerXp());
            if (!this.isSilent()) {
                serverLevel.levelEvent(null, 1026, this.blockPosition(), 0);
            }
        });
        return zombieVillager2 != null;
    }

    protected boolean isSunSensitive() {
        return true;
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        if (!super.hurtServer(serverLevel, damageSource, f)) {
            return false;
        }
        LivingEntity livingEntity = this.getTarget();
        if (livingEntity == null && damageSource.getEntity() instanceof LivingEntity) {
            livingEntity = (LivingEntity)damageSource.getEntity();
        }
        if (livingEntity != null && serverLevel.getDifficulty() == Difficulty.HARD && (double)this.random.nextFloat() < this.getAttributeValue(Attributes.SPAWN_REINFORCEMENTS_CHANCE) && serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
            int n = Mth.floor(this.getX());
            int n2 = Mth.floor(this.getY());
            int n3 = Mth.floor(this.getZ());
            EntityType<? extends Zombie> entityType = this.getType();
            Zombie zombie = entityType.create(serverLevel, EntitySpawnReason.REINFORCEMENT);
            if (zombie == null) {
                return true;
            }
            for (int i = 0; i < 50; ++i) {
                int n4;
                int n5;
                int n6 = n + Mth.nextInt(this.random, 7, 40) * Mth.nextInt(this.random, -1, 1);
                BlockPos blockPos = new BlockPos(n6, n5 = n2 + Mth.nextInt(this.random, 7, 40) * Mth.nextInt(this.random, -1, 1), n4 = n3 + Mth.nextInt(this.random, 7, 40) * Mth.nextInt(this.random, -1, 1));
                if (!SpawnPlacements.isSpawnPositionOk(entityType, serverLevel, blockPos) || !SpawnPlacements.checkSpawnRules(entityType, serverLevel, EntitySpawnReason.REINFORCEMENT, blockPos, serverLevel.random)) continue;
                zombie.setPos(n6, n5, n4);
                if (serverLevel.hasNearbyAlivePlayer(n6, n5, n4, 7.0) || !serverLevel.isUnobstructed(zombie) || !serverLevel.noCollision(zombie) || !zombie.canSpawnInLiquids() && serverLevel.containsAnyLiquid(zombie.getBoundingBox())) continue;
                zombie.setTarget(livingEntity);
                zombie.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(zombie.blockPosition()), EntitySpawnReason.REINFORCEMENT, null);
                serverLevel.addFreshEntityWithPassengers(zombie);
                AttributeInstance attributeInstance = this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE);
                AttributeModifier attributeModifier = attributeInstance.getModifier(REINFORCEMENT_CALLER_CHARGE_ID);
                double d = attributeModifier != null ? attributeModifier.amount() : 0.0;
                attributeInstance.removeModifier(REINFORCEMENT_CALLER_CHARGE_ID);
                attributeInstance.addPermanentModifier(new AttributeModifier(REINFORCEMENT_CALLER_CHARGE_ID, d - 0.05, AttributeModifier.Operation.ADD_VALUE));
                zombie.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).addPermanentModifier(ZOMBIE_REINFORCEMENT_CALLEE_CHARGE);
                break;
            }
        }
        return true;
    }

    @Override
    public boolean doHurtTarget(ServerLevel serverLevel, Entity entity) {
        boolean bl = super.doHurtTarget(serverLevel, entity);
        if (bl) {
            float f = this.level().getCurrentDifficultyAt(this.blockPosition()).getEffectiveDifficulty();
            if (this.getMainHandItem().isEmpty() && this.isOnFire() && this.random.nextFloat() < f * 0.3f) {
                entity.igniteForSeconds(2 * (int)f);
            }
        }
        return bl;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.ZOMBIE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_DEATH;
    }

    protected SoundEvent getStepSound() {
        return SoundEvents.ZOMBIE_STEP;
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(this.getStepSound(), 0.15f, 1.0f);
    }

    public EntityType<? extends Zombie> getType() {
        return super.getType();
    }

    protected boolean canSpawnInLiquids() {
        return false;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficultyInstance) {
        super.populateDefaultEquipmentSlots(randomSource, difficultyInstance);
        float f = randomSource.nextFloat();
        float f2 = this.level().getDifficulty() == Difficulty.HARD ? 0.05f : 0.01f;
        if (f < f2) {
            int n = randomSource.nextInt(3);
            if (n == 0) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            } else {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SHOVEL));
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putBoolean("IsBaby", this.isBaby());
        valueOutput.putBoolean("CanBreakDoors", this.canBreakDoors());
        valueOutput.putInt("InWaterTime", this.isInWater() ? this.inWaterTime : -1);
        valueOutput.putInt("DrownedConversionTime", this.isUnderWaterConverting() ? this.conversionTime : -1);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.setBaby(valueInput.getBooleanOr("IsBaby", false));
        this.setCanBreakDoors(valueInput.getBooleanOr("CanBreakDoors", false));
        this.inWaterTime = valueInput.getIntOr("InWaterTime", 0);
        int n = valueInput.getIntOr("DrownedConversionTime", -1);
        if (n != -1) {
            this.startUnderWaterConversion(n);
        } else {
            this.getEntityData().set(DATA_DROWNED_CONVERSION_ID, false);
        }
    }

    @Override
    public boolean killedEntity(ServerLevel serverLevel, LivingEntity livingEntity) {
        boolean bl = super.killedEntity(serverLevel, livingEntity);
        if ((serverLevel.getDifficulty() == Difficulty.NORMAL || serverLevel.getDifficulty() == Difficulty.HARD) && livingEntity instanceof Villager) {
            Villager villager = (Villager)livingEntity;
            if (serverLevel.getDifficulty() != Difficulty.HARD && this.random.nextBoolean()) {
                return bl;
            }
            if (this.convertVillagerToZombieVillager(serverLevel, villager)) {
                bl = false;
            }
        }
        return bl;
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return this.isBaby() ? BABY_DIMENSIONS : super.getDefaultDimensions(pose);
    }

    @Override
    public boolean canHoldItem(ItemStack itemStack) {
        if (itemStack.is(ItemTags.EGGS) && this.isBaby() && this.isPassenger()) {
            return false;
        }
        return super.canHoldItem(itemStack);
    }

    @Override
    public boolean wantsToPickUp(ServerLevel serverLevel, ItemStack itemStack) {
        if (itemStack.is(Items.GLOW_INK_SAC)) {
            return false;
        }
        return super.wantsToPickUp(serverLevel, itemStack);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData) {
        Object object;
        RandomSource randomSource = serverLevelAccessor.getRandom();
        spawnGroupData = super.finalizeSpawn(serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData);
        float f = difficultyInstance.getSpecialMultiplier();
        if (entitySpawnReason != EntitySpawnReason.CONVERSION) {
            this.setCanPickUpLoot(randomSource.nextFloat() < 0.55f * f);
        }
        if (spawnGroupData == null) {
            spawnGroupData = new ZombieGroupData(Zombie.getSpawnAsBabyOdds(randomSource), true);
        }
        if (spawnGroupData instanceof ZombieGroupData) {
            object = (ZombieGroupData)spawnGroupData;
            if (((ZombieGroupData)object).isBaby) {
                this.setBaby(true);
                if (((ZombieGroupData)object).canSpawnJockey) {
                    List<Entity> list;
                    if ((double)randomSource.nextFloat() < 0.05) {
                        list = serverLevelAccessor.getEntitiesOfClass(Chicken.class, this.getBoundingBox().inflate(5.0, 3.0, 5.0), EntitySelector.ENTITY_NOT_BEING_RIDDEN);
                        if (!list.isEmpty()) {
                            Chicken chicken = (Chicken)list.get(0);
                            chicken.setChickenJockey(true);
                            this.startRiding(chicken);
                        }
                    } else if ((double)randomSource.nextFloat() < 0.05 && (list = EntityType.CHICKEN.create(this.level(), EntitySpawnReason.JOCKEY)) != null) {
                        ((Entity)((Object)list)).snapTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0f);
                        ((Chicken)((Object)list)).finalizeSpawn(serverLevelAccessor, difficultyInstance, EntitySpawnReason.JOCKEY, null);
                        ((Chicken)((Object)list)).setChickenJockey(true);
                        this.startRiding((Entity)((Object)list));
                        serverLevelAccessor.addFreshEntity((Entity)((Object)list));
                    }
                }
            }
            this.setCanBreakDoors(randomSource.nextFloat() < f * 0.1f);
            if (entitySpawnReason != EntitySpawnReason.CONVERSION) {
                this.populateDefaultEquipmentSlots(randomSource, difficultyInstance);
                this.populateDefaultEquipmentEnchantments(serverLevelAccessor, randomSource, difficultyInstance);
            }
        }
        if (this.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) {
            object = LocalDate.now();
            int n = ((LocalDate)object).get(ChronoField.DAY_OF_MONTH);
            int n2 = ((LocalDate)object).get(ChronoField.MONTH_OF_YEAR);
            if (n2 == 10 && n == 31 && randomSource.nextFloat() < 0.25f) {
                this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(randomSource.nextFloat() < 0.1f ? Blocks.JACK_O_LANTERN : Blocks.CARVED_PUMPKIN));
                this.setDropChance(EquipmentSlot.HEAD, 0.0f);
            }
        }
        this.handleAttributes(f);
        return spawnGroupData;
    }

    @VisibleForTesting
    public void setInWaterTime(int n) {
        this.inWaterTime = n;
    }

    @VisibleForTesting
    public void setConversionTime(int n) {
        this.conversionTime = n;
    }

    public static boolean getSpawnAsBabyOdds(RandomSource randomSource) {
        return randomSource.nextFloat() < 0.05f;
    }

    protected void handleAttributes(float f) {
        this.randomizeReinforcementsChance();
        this.getAttribute(Attributes.KNOCKBACK_RESISTANCE).addOrReplacePermanentModifier(new AttributeModifier(RANDOM_SPAWN_BONUS_ID, this.random.nextDouble() * (double)0.05f, AttributeModifier.Operation.ADD_VALUE));
        double d = this.random.nextDouble() * 1.5 * (double)f;
        if (d > 1.0) {
            this.getAttribute(Attributes.FOLLOW_RANGE).addOrReplacePermanentModifier(new AttributeModifier(ZOMBIE_RANDOM_SPAWN_BONUS_ID, d, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        if (this.random.nextFloat() < f * 0.05f) {
            this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).addOrReplacePermanentModifier(new AttributeModifier(LEADER_ZOMBIE_BONUS_ID, this.random.nextDouble() * 0.25 + 0.5, AttributeModifier.Operation.ADD_VALUE));
            this.getAttribute(Attributes.MAX_HEALTH).addOrReplacePermanentModifier(new AttributeModifier(LEADER_ZOMBIE_BONUS_ID, this.random.nextDouble() * 3.0 + 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            this.setCanBreakDoors(true);
        }
    }

    protected void randomizeReinforcementsChance() {
        this.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE).setBaseValue(this.random.nextDouble() * (double)0.1f);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel serverLevel, DamageSource damageSource, boolean bl) {
        ItemStack itemStack;
        Creeper creeper;
        super.dropCustomDeathLoot(serverLevel, damageSource, bl);
        Entity entity = damageSource.getEntity();
        if (entity instanceof Creeper && (creeper = (Creeper)entity).canDropMobsSkull() && !(itemStack = this.getSkull()).isEmpty()) {
            creeper.increaseDroppedSkulls();
            this.spawnAtLocation(serverLevel, itemStack);
        }
    }

    protected ItemStack getSkull() {
        return new ItemStack(Items.ZOMBIE_HEAD);
    }

    class ZombieAttackTurtleEggGoal
    extends RemoveBlockGoal {
        ZombieAttackTurtleEggGoal(PathfinderMob pathfinderMob, double d, int n) {
            super(Blocks.TURTLE_EGG, pathfinderMob, d, n);
        }

        @Override
        public void playDestroyProgressSound(LevelAccessor levelAccessor, BlockPos blockPos) {
            levelAccessor.playSound(null, blockPos, SoundEvents.ZOMBIE_DESTROY_EGG, SoundSource.HOSTILE, 0.5f, 0.9f + Zombie.this.random.nextFloat() * 0.2f);
        }

        @Override
        public void playBreakSound(Level level, BlockPos blockPos) {
            level.playSound(null, blockPos, SoundEvents.TURTLE_EGG_BREAK, SoundSource.BLOCKS, 0.7f, 0.9f + level.random.nextFloat() * 0.2f);
        }

        @Override
        public double acceptedDistance() {
            return 1.14;
        }
    }

    public static class ZombieGroupData
    implements SpawnGroupData {
        public final boolean isBaby;
        public final boolean canSpawnJockey;

        public ZombieGroupData(boolean bl, boolean bl2) {
            this.isBaby = bl;
            this.canSpawnJockey = bl2;
        }
    }
}

