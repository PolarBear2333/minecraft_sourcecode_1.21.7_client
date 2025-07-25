/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.serialization.Dynamic
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.monster.piglin;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class Piglin
extends AbstractPiglin
implements CrossbowAttackMob,
InventoryCarrier {
    private static final EntityDataAccessor<Boolean> DATA_BABY_ID = SynchedEntityData.defineId(Piglin.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING_CROSSBOW = SynchedEntityData.defineId(Piglin.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_DANCING = SynchedEntityData.defineId(Piglin.class, EntityDataSerializers.BOOLEAN);
    private static final ResourceLocation SPEED_MODIFIER_BABY_ID = ResourceLocation.withDefaultNamespace("baby");
    private static final AttributeModifier SPEED_MODIFIER_BABY = new AttributeModifier(SPEED_MODIFIER_BABY_ID, 0.2f, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    private static final int MAX_HEALTH = 16;
    private static final float MOVEMENT_SPEED_WHEN_FIGHTING = 0.35f;
    private static final int ATTACK_DAMAGE = 5;
    private static final float CHANCE_OF_WEARING_EACH_ARMOUR_ITEM = 0.1f;
    private static final int MAX_PASSENGERS_ON_ONE_HOGLIN = 3;
    private static final float PROBABILITY_OF_SPAWNING_AS_BABY = 0.2f;
    private static final EntityDimensions BABY_DIMENSIONS = EntityType.PIGLIN.getDimensions().scale(0.5f).withEyeHeight(0.97f);
    private static final double PROBABILITY_OF_SPAWNING_WITH_CROSSBOW_INSTEAD_OF_SWORD = 0.5;
    private static final boolean DEFAULT_IS_BABY = false;
    private static final boolean DEFAULT_CANNOT_HUNT = false;
    private final SimpleContainer inventory = new SimpleContainer(8);
    private boolean cannotHunt = false;
    protected static final ImmutableList<SensorType<? extends Sensor<? super Piglin>>> SENSOR_TYPES = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS, SensorType.HURT_BY, SensorType.PIGLIN_SPECIFIC_SENSOR);
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.DOORS_TO_CLOSE, MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS, MemoryModuleType.NEARBY_ADULT_PIGLINS, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, (Object[])new MemoryModuleType[]{MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN, MemoryModuleType.INTERACTION_TARGET, MemoryModuleType.PATH, MemoryModuleType.ANGRY_AT, MemoryModuleType.UNIVERSAL_ANGER, MemoryModuleType.AVOID_TARGET, MemoryModuleType.ADMIRING_ITEM, MemoryModuleType.TIME_TRYING_TO_REACH_ADMIRE_ITEM, MemoryModuleType.ADMIRING_DISABLED, MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM, MemoryModuleType.CELEBRATE_LOCATION, MemoryModuleType.DANCING, MemoryModuleType.HUNTED_RECENTLY, MemoryModuleType.NEAREST_VISIBLE_BABY_HOGLIN, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.NEAREST_VISIBLE_ZOMBIFIED, MemoryModuleType.RIDE_TARGET, MemoryModuleType.VISIBLE_ADULT_PIGLIN_COUNT, MemoryModuleType.VISIBLE_ADULT_HOGLIN_COUNT, MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN, MemoryModuleType.NEAREST_TARGETABLE_PLAYER_NOT_WEARING_GOLD, MemoryModuleType.NEAREST_PLAYER_HOLDING_WANTED_ITEM, MemoryModuleType.ATE_RECENTLY, MemoryModuleType.NEAREST_REPELLENT});

    public Piglin(EntityType<? extends AbstractPiglin> entityType, Level level) {
        super(entityType, level);
        this.xpReward = 5;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putBoolean("IsBaby", this.isBaby());
        valueOutput.putBoolean("CannotHunt", this.cannotHunt);
        this.writeInventoryToTag(valueOutput);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.setBaby(valueInput.getBooleanOr("IsBaby", false));
        this.setCannotHunt(valueInput.getBooleanOr("CannotHunt", false));
        this.readInventoryFromTag(valueInput);
    }

    @Override
    @VisibleForDebug
    public SimpleContainer getInventory() {
        return this.inventory;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel serverLevel, DamageSource damageSource, boolean bl) {
        Creeper creeper;
        super.dropCustomDeathLoot(serverLevel, damageSource, bl);
        Entity entity = damageSource.getEntity();
        if (entity instanceof Creeper && (creeper = (Creeper)entity).canDropMobsSkull()) {
            ItemStack itemStack2 = new ItemStack(Items.PIGLIN_HEAD);
            creeper.increaseDroppedSkulls();
            this.spawnAtLocation(serverLevel, itemStack2);
        }
        this.inventory.removeAllItems().forEach(itemStack -> this.spawnAtLocation(serverLevel, (ItemStack)itemStack));
    }

    protected ItemStack addToInventory(ItemStack itemStack) {
        return this.inventory.addItem(itemStack);
    }

    protected boolean canAddToInventory(ItemStack itemStack) {
        return this.inventory.canAddItem(itemStack);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BABY_ID, false);
        builder.define(DATA_IS_CHARGING_CROSSBOW, false);
        builder.define(DATA_IS_DANCING, false);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        super.onSyncedDataUpdated(entityDataAccessor);
        if (DATA_BABY_ID.equals(entityDataAccessor)) {
            this.refreshDimensions();
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 16.0).add(Attributes.MOVEMENT_SPEED, 0.35f).add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    public static boolean checkPiglinSpawnRules(EntityType<Piglin> entityType, LevelAccessor levelAccessor, EntitySpawnReason entitySpawnReason, BlockPos blockPos, RandomSource randomSource) {
        return !levelAccessor.getBlockState(blockPos.below()).is(Blocks.NETHER_WART_BLOCK);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData) {
        RandomSource randomSource = serverLevelAccessor.getRandom();
        if (entitySpawnReason != EntitySpawnReason.STRUCTURE) {
            if (randomSource.nextFloat() < 0.2f) {
                this.setBaby(true);
            } else if (this.isAdult()) {
                this.setItemSlot(EquipmentSlot.MAINHAND, this.createSpawnWeapon());
            }
        }
        PiglinAi.initMemories(this, serverLevelAccessor.getRandom());
        this.populateDefaultEquipmentSlots(randomSource, difficultyInstance);
        this.populateDefaultEquipmentEnchantments(serverLevelAccessor, randomSource, difficultyInstance);
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData);
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        return !this.isPersistenceRequired();
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficultyInstance) {
        if (this.isAdult()) {
            this.maybeWearArmor(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET), randomSource);
            this.maybeWearArmor(EquipmentSlot.CHEST, new ItemStack(Items.GOLDEN_CHESTPLATE), randomSource);
            this.maybeWearArmor(EquipmentSlot.LEGS, new ItemStack(Items.GOLDEN_LEGGINGS), randomSource);
            this.maybeWearArmor(EquipmentSlot.FEET, new ItemStack(Items.GOLDEN_BOOTS), randomSource);
        }
    }

    private void maybeWearArmor(EquipmentSlot equipmentSlot, ItemStack itemStack, RandomSource randomSource) {
        if (randomSource.nextFloat() < 0.1f) {
            this.setItemSlot(equipmentSlot, itemStack);
        }
    }

    protected Brain.Provider<Piglin> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return PiglinAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    public Brain<Piglin> getBrain() {
        return super.getBrain();
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        InteractionResult interactionResult = super.mobInteract(player, interactionHand);
        if (interactionResult.consumesAction()) {
            return interactionResult;
        }
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            return PiglinAi.mobInteract(serverLevel, this, player, interactionHand);
        }
        boolean bl = PiglinAi.canAdmire(this, player.getItemInHand(interactionHand)) && this.getArmPose() != PiglinArmPose.ADMIRING_ITEM;
        return bl ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return this.isBaby() ? BABY_DIMENSIONS : super.getDefaultDimensions(pose);
    }

    @Override
    public void setBaby(boolean bl) {
        this.getEntityData().set(DATA_BABY_ID, bl);
        if (!this.level().isClientSide) {
            AttributeInstance attributeInstance = this.getAttribute(Attributes.MOVEMENT_SPEED);
            attributeInstance.removeModifier(SPEED_MODIFIER_BABY.id());
            if (bl) {
                attributeInstance.addTransientModifier(SPEED_MODIFIER_BABY);
            }
        }
    }

    @Override
    public boolean isBaby() {
        return this.getEntityData().get(DATA_BABY_ID);
    }

    private void setCannotHunt(boolean bl) {
        this.cannotHunt = bl;
    }

    @Override
    protected boolean canHunt() {
        return !this.cannotHunt;
    }

    @Override
    protected void customServerAiStep(ServerLevel serverLevel) {
        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("piglinBrain");
        this.getBrain().tick(serverLevel, this);
        profilerFiller.pop();
        PiglinAi.updateActivity(this);
        super.customServerAiStep(serverLevel);
    }

    @Override
    protected int getBaseExperienceReward(ServerLevel serverLevel) {
        return this.xpReward;
    }

    @Override
    protected void finishConversion(ServerLevel serverLevel) {
        PiglinAi.cancelAdmiring(serverLevel, this);
        this.inventory.removeAllItems().forEach(itemStack -> this.spawnAtLocation(serverLevel, (ItemStack)itemStack));
        super.finishConversion(serverLevel);
    }

    private ItemStack createSpawnWeapon() {
        if ((double)this.random.nextFloat() < 0.5) {
            return new ItemStack(Items.CROSSBOW);
        }
        return new ItemStack(Items.GOLDEN_SWORD);
    }

    @Override
    @Nullable
    public TagKey<Item> getPreferredWeaponType() {
        if (this.isBaby()) {
            return null;
        }
        return ItemTags.PIGLIN_PREFERRED_WEAPONS;
    }

    private boolean isChargingCrossbow() {
        return this.entityData.get(DATA_IS_CHARGING_CROSSBOW);
    }

    @Override
    public void setChargingCrossbow(boolean bl) {
        this.entityData.set(DATA_IS_CHARGING_CROSSBOW, bl);
    }

    @Override
    public void onCrossbowAttackPerformed() {
        this.noActionTime = 0;
    }

    @Override
    public PiglinArmPose getArmPose() {
        if (this.isDancing()) {
            return PiglinArmPose.DANCING;
        }
        if (PiglinAi.isLovedItem(this.getOffhandItem())) {
            return PiglinArmPose.ADMIRING_ITEM;
        }
        if (this.isAggressive() && this.isHoldingMeleeWeapon()) {
            return PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON;
        }
        if (this.isChargingCrossbow()) {
            return PiglinArmPose.CROSSBOW_CHARGE;
        }
        if (this.isHolding(Items.CROSSBOW) && CrossbowItem.isCharged(this.getWeaponItem())) {
            return PiglinArmPose.CROSSBOW_HOLD;
        }
        return PiglinArmPose.DEFAULT;
    }

    public boolean isDancing() {
        return this.entityData.get(DATA_IS_DANCING);
    }

    public void setDancing(boolean bl) {
        this.entityData.set(DATA_IS_DANCING, bl);
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        Entity entity;
        boolean bl = super.hurtServer(serverLevel, damageSource, f);
        if (bl && (entity = damageSource.getEntity()) instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            PiglinAi.wasHurtBy(serverLevel, this, livingEntity);
        }
        return bl;
    }

    @Override
    public void performRangedAttack(LivingEntity livingEntity, float f) {
        this.performCrossbowAttack(this, 1.6f);
    }

    @Override
    public boolean canFireProjectileWeapon(ProjectileWeaponItem projectileWeaponItem) {
        return projectileWeaponItem == Items.CROSSBOW;
    }

    protected void holdInMainHand(ItemStack itemStack) {
        this.setItemSlotAndDropWhenKilled(EquipmentSlot.MAINHAND, itemStack);
    }

    protected void holdInOffHand(ItemStack itemStack) {
        if (itemStack.is(PiglinAi.BARTERING_ITEM)) {
            this.setItemSlot(EquipmentSlot.OFFHAND, itemStack);
            this.setGuaranteedDrop(EquipmentSlot.OFFHAND);
        } else {
            this.setItemSlotAndDropWhenKilled(EquipmentSlot.OFFHAND, itemStack);
        }
    }

    @Override
    public boolean wantsToPickUp(ServerLevel serverLevel, ItemStack itemStack) {
        return serverLevel.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING) && this.canPickUpLoot() && PiglinAi.wantsToPickup(this, itemStack);
    }

    protected boolean canReplaceCurrentItem(ItemStack itemStack) {
        EquipmentSlot equipmentSlot = this.getEquipmentSlotForItem(itemStack);
        ItemStack itemStack2 = this.getItemBySlot(equipmentSlot);
        return this.canReplaceCurrentItem(itemStack, itemStack2, equipmentSlot);
    }

    @Override
    protected boolean canReplaceCurrentItem(ItemStack itemStack, ItemStack itemStack2, EquipmentSlot equipmentSlot) {
        boolean bl;
        if (EnchantmentHelper.has(itemStack2, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)) {
            return false;
        }
        TagKey<Item> tagKey = this.getPreferredWeaponType();
        boolean bl2 = PiglinAi.isLovedItem(itemStack) || tagKey != null && itemStack.is(tagKey);
        boolean bl3 = bl = PiglinAi.isLovedItem(itemStack2) || tagKey != null && itemStack2.is(tagKey);
        if (bl2 && !bl) {
            return true;
        }
        if (!bl2 && bl) {
            return false;
        }
        return super.canReplaceCurrentItem(itemStack, itemStack2, equipmentSlot);
    }

    @Override
    protected void pickUpItem(ServerLevel serverLevel, ItemEntity itemEntity) {
        this.onItemPickup(itemEntity);
        PiglinAi.pickUpItem(serverLevel, this, itemEntity);
    }

    @Override
    public boolean startRiding(Entity entity, boolean bl) {
        if (this.isBaby() && entity.getType() == EntityType.HOGLIN) {
            entity = this.getTopPassenger(entity, 3);
        }
        return super.startRiding(entity, bl);
    }

    private Entity getTopPassenger(Entity entity, int n) {
        List<Entity> list = entity.getPassengers();
        if (n == 1 || list.isEmpty()) {
            return entity;
        }
        return this.getTopPassenger(list.getFirst(), n - 1);
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        if (this.level().isClientSide) {
            return null;
        }
        return PiglinAi.getSoundForCurrentActivity(this).orElse(null);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PIGLIN_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PIGLIN_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(SoundEvents.PIGLIN_STEP, 0.15f, 1.0f);
    }

    @Override
    protected void playConvertedSound() {
        this.makeSound(SoundEvents.PIGLIN_CONVERTED_TO_ZOMBIFIED);
    }
}

