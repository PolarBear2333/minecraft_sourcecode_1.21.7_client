/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.animal.horse;

import java.util.function.DoubleSupplier;
import java.util.function.IntUnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStandGoal;
import net.minecraft.world.entity.ai.goal.RunAroundLikeCrazyGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractHorse
extends Animal
implements HasCustomInventoryScreen,
OwnableEntity,
PlayerRideableJumping {
    public static final int CHEST_SLOT_OFFSET = 499;
    public static final int INVENTORY_SLOT_OFFSET = 500;
    public static final double BREEDING_CROSS_FACTOR = 0.15;
    private static final float MIN_MOVEMENT_SPEED = (float)AbstractHorse.generateSpeed(() -> 0.0);
    private static final float MAX_MOVEMENT_SPEED = (float)AbstractHorse.generateSpeed(() -> 1.0);
    private static final float MIN_JUMP_STRENGTH = (float)AbstractHorse.generateJumpStrength(() -> 0.0);
    private static final float MAX_JUMP_STRENGTH = (float)AbstractHorse.generateJumpStrength(() -> 1.0);
    private static final float MIN_HEALTH = AbstractHorse.generateMaxHealth(n -> 0);
    private static final float MAX_HEALTH = AbstractHorse.generateMaxHealth(n -> n - 1);
    private static final float BACKWARDS_MOVE_SPEED_FACTOR = 0.25f;
    private static final float SIDEWAYS_MOVE_SPEED_FACTOR = 0.5f;
    private static final TargetingConditions.Selector PARENT_HORSE_SELECTOR = (livingEntity, serverLevel) -> {
        AbstractHorse abstractHorse;
        return livingEntity instanceof AbstractHorse && (abstractHorse = (AbstractHorse)livingEntity).isBred();
    };
    private static final TargetingConditions MOMMY_TARGETING = TargetingConditions.forNonCombat().range(16.0).ignoreLineOfSight().selector(PARENT_HORSE_SELECTOR);
    private static final EntityDataAccessor<Byte> DATA_ID_FLAGS = SynchedEntityData.defineId(AbstractHorse.class, EntityDataSerializers.BYTE);
    private static final int FLAG_TAME = 2;
    private static final int FLAG_BRED = 8;
    private static final int FLAG_EATING = 16;
    private static final int FLAG_STANDING = 32;
    private static final int FLAG_OPEN_MOUTH = 64;
    public static final int INVENTORY_ROWS = 3;
    private static final int DEFAULT_TEMPER = 0;
    private static final boolean DEFAULT_EATING_HAYSTACK = false;
    private static final boolean DEFAULT_BRED = false;
    private static final boolean DEFAULT_TAME = false;
    private int eatingCounter;
    private int mouthCounter;
    private int standCounter;
    public int tailCounter;
    public int sprintCounter;
    protected SimpleContainer inventory;
    protected int temper = 0;
    protected float playerJumpPendingScale;
    protected boolean allowStandSliding;
    private float eatAnim;
    private float eatAnimO;
    private float standAnim;
    private float standAnimO;
    private float mouthAnim;
    private float mouthAnimO;
    protected boolean canGallop = true;
    protected int gallopSoundCounter;
    @Nullable
    private EntityReference<LivingEntity> owner;

    protected AbstractHorse(EntityType<? extends AbstractHorse> entityType, Level level) {
        super((EntityType<? extends Animal>)entityType, level);
        this.createInventory();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.2));
        this.goalSelector.addGoal(1, new RunAroundLikeCrazyGoal(this, 1.2));
        this.goalSelector.addGoal(2, new BreedGoal(this, 1.0, AbstractHorse.class));
        this.goalSelector.addGoal(4, new FollowParentGoal(this, 1.0));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        if (this.canPerformRearing()) {
            this.goalSelector.addGoal(9, new RandomStandGoal(this));
        }
        this.addBehaviourGoals();
    }

    protected void addBehaviourGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(3, new TemptGoal(this, 1.25, itemStack -> itemStack.is(ItemTags.HORSE_TEMPT_ITEMS), false));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ID_FLAGS, (byte)0);
    }

    protected boolean getFlag(int n) {
        return (this.entityData.get(DATA_ID_FLAGS) & n) != 0;
    }

    protected void setFlag(int n, boolean bl) {
        byte by = this.entityData.get(DATA_ID_FLAGS);
        if (bl) {
            this.entityData.set(DATA_ID_FLAGS, (byte)(by | n));
        } else {
            this.entityData.set(DATA_ID_FLAGS, (byte)(by & ~n));
        }
    }

    public boolean isTamed() {
        return this.getFlag(2);
    }

    @Override
    @Nullable
    public EntityReference<LivingEntity> getOwnerReference() {
        return this.owner;
    }

    public void setOwner(@Nullable LivingEntity livingEntity) {
        this.owner = livingEntity != null ? new EntityReference<LivingEntity>(livingEntity) : null;
    }

    public void setTamed(boolean bl) {
        this.setFlag(2, bl);
    }

    @Override
    public void onElasticLeashPull() {
        super.onElasticLeashPull();
        if (this.isEating()) {
            this.setEating(false);
        }
    }

    @Override
    public boolean supportQuadLeash() {
        return true;
    }

    @Override
    public Vec3[] getQuadLeashOffsets() {
        return Leashable.createQuadLeashOffsets(this, 0.04, 0.52, 0.23, 0.87);
    }

    public boolean isEating() {
        return this.getFlag(16);
    }

    public boolean isStanding() {
        return this.getFlag(32);
    }

    public boolean isBred() {
        return this.getFlag(8);
    }

    public void setBred(boolean bl) {
        this.setFlag(8, bl);
    }

    @Override
    public boolean canUseSlot(EquipmentSlot equipmentSlot) {
        if (equipmentSlot == EquipmentSlot.SADDLE) {
            return this.isAlive() && !this.isBaby() && this.isTamed();
        }
        return super.canUseSlot(equipmentSlot);
    }

    public void equipBodyArmor(Player player, ItemStack itemStack) {
        if (this.isEquippableInSlot(itemStack, EquipmentSlot.BODY)) {
            this.setBodyArmorItem(itemStack.consumeAndReturn(1, player));
        }
    }

    @Override
    protected boolean canDispenserEquipIntoSlot(EquipmentSlot equipmentSlot) {
        return (equipmentSlot == EquipmentSlot.BODY || equipmentSlot == EquipmentSlot.SADDLE) && this.isTamed() || super.canDispenserEquipIntoSlot(equipmentSlot);
    }

    public int getTemper() {
        return this.temper;
    }

    public void setTemper(int n) {
        this.temper = n;
    }

    public int modifyTemper(int n) {
        int n2 = Mth.clamp(this.getTemper() + n, 0, this.getMaxTemper());
        this.setTemper(n2);
        return n2;
    }

    @Override
    public boolean isPushable() {
        return !this.isVehicle();
    }

    private void eating() {
        SoundEvent soundEvent;
        this.openMouth();
        if (!this.isSilent() && (soundEvent = this.getEatingSound()) != null) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), soundEvent, this.getSoundSource(), 1.0f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
        }
    }

    @Override
    public boolean causeFallDamage(double d, float f, DamageSource damageSource) {
        int n;
        if (d > 1.0) {
            this.playSound(SoundEvents.HORSE_LAND, 0.4f, 1.0f);
        }
        if ((n = this.calculateFallDamage(d, f)) <= 0) {
            return false;
        }
        this.hurt(damageSource, n);
        this.propagateFallToPassengers(d, f, damageSource);
        this.playBlockFallSound();
        return true;
    }

    public final int getInventorySize() {
        return AbstractHorse.getInventorySize(this.getInventoryColumns());
    }

    public static int getInventorySize(int n) {
        return n * 3;
    }

    protected void createInventory() {
        SimpleContainer simpleContainer = this.inventory;
        this.inventory = new SimpleContainer(this.getInventorySize());
        if (simpleContainer != null) {
            int n = Math.min(simpleContainer.getContainerSize(), this.inventory.getContainerSize());
            for (int i = 0; i < n; ++i) {
                ItemStack itemStack = simpleContainer.getItem(i);
                if (itemStack.isEmpty()) continue;
                this.inventory.setItem(i, itemStack.copy());
            }
        }
    }

    @Override
    protected Holder<SoundEvent> getEquipSound(EquipmentSlot equipmentSlot, ItemStack itemStack, Equippable equippable) {
        if (equipmentSlot == EquipmentSlot.SADDLE) {
            return SoundEvents.HORSE_SADDLE;
        }
        return super.getEquipSound(equipmentSlot, itemStack, equippable);
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        boolean bl = super.hurtServer(serverLevel, damageSource, f);
        if (bl && this.random.nextInt(3) == 0) {
            this.standIfPossible();
        }
        return bl;
    }

    protected boolean canPerformRearing() {
        return true;
    }

    @Nullable
    protected SoundEvent getEatingSound() {
        return null;
    }

    @Nullable
    protected SoundEvent getAngrySound() {
        return null;
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        if (blockState.liquid()) {
            return;
        }
        BlockState blockState2 = this.level().getBlockState(blockPos.above());
        SoundType soundType = blockState.getSoundType();
        if (blockState2.is(Blocks.SNOW)) {
            soundType = blockState2.getSoundType();
        }
        if (this.isVehicle() && this.canGallop) {
            ++this.gallopSoundCounter;
            if (this.gallopSoundCounter > 5 && this.gallopSoundCounter % 3 == 0) {
                this.playGallopSound(soundType);
            } else if (this.gallopSoundCounter <= 5) {
                this.playSound(SoundEvents.HORSE_STEP_WOOD, soundType.getVolume() * 0.15f, soundType.getPitch());
            }
        } else if (this.isWoodSoundType(soundType)) {
            this.playSound(SoundEvents.HORSE_STEP_WOOD, soundType.getVolume() * 0.15f, soundType.getPitch());
        } else {
            this.playSound(SoundEvents.HORSE_STEP, soundType.getVolume() * 0.15f, soundType.getPitch());
        }
    }

    private boolean isWoodSoundType(SoundType soundType) {
        return soundType == SoundType.WOOD || soundType == SoundType.NETHER_WOOD || soundType == SoundType.STEM || soundType == SoundType.CHERRY_WOOD || soundType == SoundType.BAMBOO_WOOD;
    }

    protected void playGallopSound(SoundType soundType) {
        this.playSound(SoundEvents.HORSE_GALLOP, soundType.getVolume() * 0.15f, soundType.getPitch());
    }

    public static AttributeSupplier.Builder createBaseHorseAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.JUMP_STRENGTH, 0.7).add(Attributes.MAX_HEALTH, 53.0).add(Attributes.MOVEMENT_SPEED, 0.225f).add(Attributes.STEP_HEIGHT, 1.0).add(Attributes.SAFE_FALL_DISTANCE, 6.0).add(Attributes.FALL_DAMAGE_MULTIPLIER, 0.5);
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 6;
    }

    public int getMaxTemper() {
        return 100;
    }

    @Override
    protected float getSoundVolume() {
        return 0.8f;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 400;
    }

    @Override
    public void openCustomInventoryScreen(Player player) {
        if (!this.level().isClientSide && (!this.isVehicle() || this.hasPassenger(player)) && this.isTamed()) {
            player.openHorseInventory(this, this.inventory);
        }
    }

    public InteractionResult fedFood(Player player, ItemStack itemStack) {
        boolean bl = this.handleEating(player, itemStack);
        if (bl) {
            itemStack.consume(1, player);
        }
        return bl || this.level().isClientSide ? InteractionResult.SUCCESS_SERVER : InteractionResult.PASS;
    }

    protected boolean handleEating(Player player, ItemStack itemStack) {
        boolean bl = false;
        float f = 0.0f;
        int n = 0;
        int n2 = 0;
        if (itemStack.is(Items.WHEAT)) {
            f = 2.0f;
            n = 20;
            n2 = 3;
        } else if (itemStack.is(Items.SUGAR)) {
            f = 1.0f;
            n = 30;
            n2 = 3;
        } else if (itemStack.is(Blocks.HAY_BLOCK.asItem())) {
            f = 20.0f;
            n = 180;
        } else if (itemStack.is(Items.APPLE)) {
            f = 3.0f;
            n = 60;
            n2 = 3;
        } else if (itemStack.is(Items.CARROT)) {
            f = 3.0f;
            n = 60;
            n2 = 3;
        } else if (itemStack.is(Items.GOLDEN_CARROT)) {
            f = 4.0f;
            n = 60;
            n2 = 5;
            if (!this.level().isClientSide && this.isTamed() && this.getAge() == 0 && !this.isInLove()) {
                bl = true;
                this.setInLove(player);
            }
        } else if (itemStack.is(Items.GOLDEN_APPLE) || itemStack.is(Items.ENCHANTED_GOLDEN_APPLE)) {
            f = 10.0f;
            n = 240;
            n2 = 10;
            if (!this.level().isClientSide && this.isTamed() && this.getAge() == 0 && !this.isInLove()) {
                bl = true;
                this.setInLove(player);
            }
        }
        if (this.getHealth() < this.getMaxHealth() && f > 0.0f) {
            this.heal(f);
            bl = true;
        }
        if (this.isBaby() && n > 0) {
            this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), 0.0, 0.0, 0.0);
            if (!this.level().isClientSide) {
                this.ageUp(n);
                bl = true;
            }
        }
        if (!(n2 <= 0 || !bl && this.isTamed() || this.getTemper() >= this.getMaxTemper() || this.level().isClientSide)) {
            this.modifyTemper(n2);
            bl = true;
        }
        if (bl) {
            this.eating();
            this.gameEvent(GameEvent.EAT);
        }
        return bl;
    }

    protected void doPlayerRide(Player player) {
        this.setEating(false);
        this.clearStanding();
        if (!this.level().isClientSide) {
            player.setYRot(this.getYRot());
            player.setXRot(this.getXRot());
            player.startRiding(this);
        }
    }

    @Override
    public boolean isImmobile() {
        return super.isImmobile() && this.isVehicle() && this.isSaddled() || this.isEating() || this.isStanding();
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(ItemTags.HORSE_FOOD);
    }

    private void moveTail() {
        this.tailCounter = 1;
    }

    @Override
    protected void dropEquipment(ServerLevel serverLevel) {
        super.dropEquipment(serverLevel);
        if (this.inventory == null) {
            return;
        }
        for (int i = 0; i < this.inventory.getContainerSize(); ++i) {
            ItemStack itemStack = this.inventory.getItem(i);
            if (itemStack.isEmpty() || EnchantmentHelper.has(itemStack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) continue;
            this.spawnAtLocation(serverLevel, itemStack);
        }
    }

    @Override
    public void aiStep() {
        ServerLevel serverLevel;
        block9: {
            block8: {
                if (this.random.nextInt(200) == 0) {
                    this.moveTail();
                }
                super.aiStep();
                Level level = this.level();
                if (!(level instanceof ServerLevel)) break block8;
                serverLevel = (ServerLevel)level;
                if (this.isAlive()) break block9;
            }
            return;
        }
        if (this.random.nextInt(900) == 0 && this.deathTime == 0) {
            this.heal(1.0f);
        }
        if (this.canEatGrass()) {
            if (!this.isEating() && !this.isVehicle() && this.random.nextInt(300) == 0 && serverLevel.getBlockState(this.blockPosition().below()).is(Blocks.GRASS_BLOCK)) {
                this.setEating(true);
            }
            if (this.isEating() && ++this.eatingCounter > 50) {
                this.eatingCounter = 0;
                this.setEating(false);
            }
        }
        this.followMommy(serverLevel);
    }

    protected void followMommy(ServerLevel serverLevel) {
        AbstractHorse abstractHorse;
        if (this.isBred() && this.isBaby() && !this.isEating() && (abstractHorse = serverLevel.getNearestEntity(AbstractHorse.class, MOMMY_TARGETING, this, this.getX(), this.getY(), this.getZ(), this.getBoundingBox().inflate(16.0))) != null && this.distanceToSqr(abstractHorse) > 4.0) {
            this.navigation.createPath(abstractHorse, 0);
        }
    }

    public boolean canEatGrass() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.mouthCounter > 0 && ++this.mouthCounter > 30) {
            this.mouthCounter = 0;
            this.setFlag(64, false);
        }
        if (this.standCounter > 0 && --this.standCounter <= 0) {
            this.clearStanding();
        }
        if (this.tailCounter > 0 && ++this.tailCounter > 8) {
            this.tailCounter = 0;
        }
        if (this.sprintCounter > 0) {
            ++this.sprintCounter;
            if (this.sprintCounter > 300) {
                this.sprintCounter = 0;
            }
        }
        this.eatAnimO = this.eatAnim;
        if (this.isEating()) {
            this.eatAnim += (1.0f - this.eatAnim) * 0.4f + 0.05f;
            if (this.eatAnim > 1.0f) {
                this.eatAnim = 1.0f;
            }
        } else {
            this.eatAnim += (0.0f - this.eatAnim) * 0.4f - 0.05f;
            if (this.eatAnim < 0.0f) {
                this.eatAnim = 0.0f;
            }
        }
        this.standAnimO = this.standAnim;
        if (this.isStanding()) {
            this.eatAnimO = this.eatAnim = 0.0f;
            this.standAnim += (1.0f - this.standAnim) * 0.4f + 0.05f;
            if (this.standAnim > 1.0f) {
                this.standAnim = 1.0f;
            }
        } else {
            this.allowStandSliding = false;
            this.standAnim += (0.8f * this.standAnim * this.standAnim * this.standAnim - this.standAnim) * 0.6f - 0.05f;
            if (this.standAnim < 0.0f) {
                this.standAnim = 0.0f;
            }
        }
        this.mouthAnimO = this.mouthAnim;
        if (this.getFlag(64)) {
            this.mouthAnim += (1.0f - this.mouthAnim) * 0.7f + 0.05f;
            if (this.mouthAnim > 1.0f) {
                this.mouthAnim = 1.0f;
            }
        } else {
            this.mouthAnim += (0.0f - this.mouthAnim) * 0.7f - 0.05f;
            if (this.mouthAnim < 0.0f) {
                this.mouthAnim = 0.0f;
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        if (this.isVehicle() || this.isBaby()) {
            return super.mobInteract(player, interactionHand);
        }
        if (this.isTamed() && player.isSecondaryUseActive()) {
            this.openCustomInventoryScreen(player);
            return InteractionResult.SUCCESS;
        }
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (!itemStack.isEmpty()) {
            InteractionResult interactionResult = itemStack.interactLivingEntity(player, this, interactionHand);
            if (interactionResult.consumesAction()) {
                return interactionResult;
            }
            if (this.isEquippableInSlot(itemStack, EquipmentSlot.BODY) && !this.isWearingBodyArmor()) {
                this.equipBodyArmor(player, itemStack);
                return InteractionResult.SUCCESS;
            }
        }
        this.doPlayerRide(player);
        return InteractionResult.SUCCESS;
    }

    private void openMouth() {
        if (!this.level().isClientSide) {
            this.mouthCounter = 1;
            this.setFlag(64, true);
        }
    }

    public void setEating(boolean bl) {
        this.setFlag(16, bl);
    }

    public void setStanding(int n) {
        this.setEating(false);
        this.setFlag(32, true);
        this.standCounter = n;
    }

    public void clearStanding() {
        this.setFlag(32, false);
        this.standCounter = 0;
    }

    @Nullable
    public SoundEvent getAmbientStandSound() {
        return this.getAmbientSound();
    }

    public void standIfPossible() {
        if (this.canPerformRearing() && (this.isEffectiveAi() || !this.level().isClientSide)) {
            this.setStanding(20);
        }
    }

    public void makeMad() {
        if (!this.isStanding() && !this.level().isClientSide) {
            this.standIfPossible();
            this.makeSound(this.getAngrySound());
        }
    }

    public boolean tameWithName(Player player) {
        this.setOwner(player);
        this.setTamed(true);
        if (player instanceof ServerPlayer) {
            CriteriaTriggers.TAME_ANIMAL.trigger((ServerPlayer)player, this);
        }
        this.level().broadcastEntityEvent(this, (byte)7);
        return true;
    }

    @Override
    protected void tickRidden(Player player, Vec3 vec3) {
        super.tickRidden(player, vec3);
        Vec2 vec2 = this.getRiddenRotation(player);
        this.setRot(vec2.y, vec2.x);
        this.yBodyRot = this.yHeadRot = this.getYRot();
        this.yRotO = this.yHeadRot;
        if (this.isLocalInstanceAuthoritative()) {
            if (vec3.z <= 0.0) {
                this.gallopSoundCounter = 0;
            }
            if (this.onGround()) {
                if (this.playerJumpPendingScale > 0.0f && !this.isJumping()) {
                    this.executeRidersJump(this.playerJumpPendingScale, vec3);
                }
                this.playerJumpPendingScale = 0.0f;
            }
        }
    }

    protected Vec2 getRiddenRotation(LivingEntity livingEntity) {
        return new Vec2(livingEntity.getXRot() * 0.5f, livingEntity.getYRot());
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 vec3) {
        if (this.onGround() && this.playerJumpPendingScale == 0.0f && this.isStanding() && !this.allowStandSliding) {
            return Vec3.ZERO;
        }
        float f = player.xxa * 0.5f;
        float f2 = player.zza;
        if (f2 <= 0.0f) {
            f2 *= 0.25f;
        }
        return new Vec3(f, 0.0, f2);
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        return (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED);
    }

    protected void executeRidersJump(float f, Vec3 vec3) {
        double d = this.getJumpPower(f);
        Vec3 vec32 = this.getDeltaMovement();
        this.setDeltaMovement(vec32.x, d, vec32.z);
        this.hasImpulse = true;
        if (vec3.z > 0.0) {
            float f2 = Mth.sin(this.getYRot() * ((float)Math.PI / 180));
            float f3 = Mth.cos(this.getYRot() * ((float)Math.PI / 180));
            this.setDeltaMovement(this.getDeltaMovement().add(-0.4f * f2 * f, 0.0, 0.4f * f3 * f));
        }
    }

    protected void playJumpSound() {
        this.playSound(SoundEvents.HORSE_JUMP, 0.4f, 1.0f);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putBoolean("EatingHaystack", this.isEating());
        valueOutput.putBoolean("Bred", this.isBred());
        valueOutput.putInt("Temper", this.getTemper());
        valueOutput.putBoolean("Tame", this.isTamed());
        EntityReference.store(this.owner, valueOutput, "Owner");
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.setEating(valueInput.getBooleanOr("EatingHaystack", false));
        this.setBred(valueInput.getBooleanOr("Bred", false));
        this.setTemper(valueInput.getIntOr("Temper", 0));
        this.setTamed(valueInput.getBooleanOr("Tame", false));
        this.owner = EntityReference.readWithOldOwnerConversion(valueInput, "Owner", this.level());
    }

    @Override
    public boolean canMate(Animal animal) {
        return false;
    }

    protected boolean canParent() {
        return !this.isVehicle() && !this.isPassenger() && this.isTamed() && !this.isBaby() && this.getHealth() >= this.getMaxHealth() && this.isInLove();
    }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return null;
    }

    protected void setOffspringAttributes(AgeableMob ageableMob, AbstractHorse abstractHorse) {
        this.setOffspringAttribute(ageableMob, abstractHorse, Attributes.MAX_HEALTH, MIN_HEALTH, MAX_HEALTH);
        this.setOffspringAttribute(ageableMob, abstractHorse, Attributes.JUMP_STRENGTH, MIN_JUMP_STRENGTH, MAX_JUMP_STRENGTH);
        this.setOffspringAttribute(ageableMob, abstractHorse, Attributes.MOVEMENT_SPEED, MIN_MOVEMENT_SPEED, MAX_MOVEMENT_SPEED);
    }

    private void setOffspringAttribute(AgeableMob ageableMob, AbstractHorse abstractHorse, Holder<Attribute> holder, double d, double d2) {
        double d3 = AbstractHorse.createOffspringAttribute(this.getAttributeBaseValue(holder), ageableMob.getAttributeBaseValue(holder), d, d2, this.random);
        abstractHorse.getAttribute(holder).setBaseValue(d3);
    }

    static double createOffspringAttribute(double d, double d2, double d3, double d4, RandomSource randomSource) {
        double d5;
        if (d4 <= d3) {
            throw new IllegalArgumentException("Incorrect range for an attribute");
        }
        d = Mth.clamp(d, d3, d4);
        d2 = Mth.clamp(d2, d3, d4);
        double d6 = 0.15 * (d4 - d3);
        double d7 = (d + d2) / 2.0;
        double d8 = Math.abs(d - d2) + d6 * 2.0;
        double d9 = d7 + d8 * (d5 = (randomSource.nextDouble() + randomSource.nextDouble() + randomSource.nextDouble()) / 3.0 - 0.5);
        if (d9 > d4) {
            double d10 = d9 - d4;
            return d4 - d10;
        }
        if (d9 < d3) {
            double d11 = d3 - d9;
            return d3 + d11;
        }
        return d9;
    }

    public float getEatAnim(float f) {
        return Mth.lerp(f, this.eatAnimO, this.eatAnim);
    }

    public float getStandAnim(float f) {
        return Mth.lerp(f, this.standAnimO, this.standAnim);
    }

    public float getMouthAnim(float f) {
        return Mth.lerp(f, this.mouthAnimO, this.mouthAnim);
    }

    @Override
    public void onPlayerJump(int n) {
        if (!this.isSaddled()) {
            return;
        }
        if (n < 0) {
            n = 0;
        } else {
            this.allowStandSliding = true;
            this.standIfPossible();
        }
        this.playerJumpPendingScale = n >= 90 ? 1.0f : 0.4f + 0.4f * (float)n / 90.0f;
    }

    @Override
    public boolean canJump() {
        return this.isSaddled();
    }

    @Override
    public void handleStartJump(int n) {
        this.allowStandSliding = true;
        this.standIfPossible();
        this.playJumpSound();
    }

    @Override
    public void handleStopJump() {
    }

    protected void spawnTamingParticles(boolean bl) {
        SimpleParticleType simpleParticleType = bl ? ParticleTypes.HEART : ParticleTypes.SMOKE;
        for (int i = 0; i < 7; ++i) {
            double d = this.random.nextGaussian() * 0.02;
            double d2 = this.random.nextGaussian() * 0.02;
            double d3 = this.random.nextGaussian() * 0.02;
            this.level().addParticle(simpleParticleType, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), d, d2, d3);
        }
    }

    @Override
    public void handleEntityEvent(byte by) {
        if (by == 7) {
            this.spawnTamingParticles(true);
        } else if (by == 6) {
            this.spawnTamingParticles(false);
        } else {
            super.handleEntityEvent(by);
        }
    }

    @Override
    protected void positionRider(Entity entity, Entity.MoveFunction moveFunction) {
        super.positionRider(entity, moveFunction);
        if (entity instanceof LivingEntity) {
            ((LivingEntity)entity).yBodyRot = this.yBodyRot;
        }
    }

    protected static float generateMaxHealth(IntUnaryOperator intUnaryOperator) {
        return 15.0f + (float)intUnaryOperator.applyAsInt(8) + (float)intUnaryOperator.applyAsInt(9);
    }

    protected static double generateJumpStrength(DoubleSupplier doubleSupplier) {
        return (double)0.4f + doubleSupplier.getAsDouble() * 0.2 + doubleSupplier.getAsDouble() * 0.2 + doubleSupplier.getAsDouble() * 0.2;
    }

    protected static double generateSpeed(DoubleSupplier doubleSupplier) {
        return ((double)0.45f + doubleSupplier.getAsDouble() * 0.3 + doubleSupplier.getAsDouble() * 0.3 + doubleSupplier.getAsDouble() * 0.3) * 0.25;
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public SlotAccess getSlot(int n) {
        int n2 = n - 500;
        if (n2 >= 0 && n2 < this.inventory.getContainerSize()) {
            return SlotAccess.forContainer(this.inventory, n2);
        }
        return super.getSlot(n);
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        Entity entity;
        if (this.isSaddled() && (entity = this.getFirstPassenger()) instanceof Player) {
            Player player = (Player)entity;
            return player;
        }
        return super.getControllingPassenger();
    }

    @Nullable
    private Vec3 getDismountLocationInDirection(Vec3 vec3, LivingEntity livingEntity) {
        double d = this.getX() + vec3.x;
        double d2 = this.getBoundingBox().minY;
        double d3 = this.getZ() + vec3.z;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        block0: for (Pose pose : livingEntity.getDismountPoses()) {
            mutableBlockPos.set(d, d2, d3);
            double d4 = this.getBoundingBox().maxY + 0.75;
            do {
                double d5 = this.level().getBlockFloorHeight(mutableBlockPos);
                if ((double)mutableBlockPos.getY() + d5 > d4) continue block0;
                if (DismountHelper.isBlockFloorValid(d5)) {
                    AABB aABB = livingEntity.getLocalBoundsForPose(pose);
                    Vec3 vec32 = new Vec3(d, (double)mutableBlockPos.getY() + d5, d3);
                    if (DismountHelper.canDismountTo(this.level(), livingEntity, aABB.move(vec32))) {
                        livingEntity.setPose(pose);
                        return vec32;
                    }
                }
                mutableBlockPos.move(Direction.UP);
            } while ((double)mutableBlockPos.getY() < d4);
        }
        return null;
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity livingEntity) {
        Vec3 vec3 = AbstractHorse.getCollisionHorizontalEscapeVector(this.getBbWidth(), livingEntity.getBbWidth(), this.getYRot() + (livingEntity.getMainArm() == HumanoidArm.RIGHT ? 90.0f : -90.0f));
        Vec3 vec32 = this.getDismountLocationInDirection(vec3, livingEntity);
        if (vec32 != null) {
            return vec32;
        }
        Vec3 vec33 = AbstractHorse.getCollisionHorizontalEscapeVector(this.getBbWidth(), livingEntity.getBbWidth(), this.getYRot() + (livingEntity.getMainArm() == HumanoidArm.LEFT ? 90.0f : -90.0f));
        Vec3 vec34 = this.getDismountLocationInDirection(vec33, livingEntity);
        if (vec34 != null) {
            return vec34;
        }
        return this.position();
    }

    protected void randomizeAttributes(RandomSource randomSource) {
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData) {
        if (spawnGroupData == null) {
            spawnGroupData = new AgeableMob.AgeableMobGroupData(0.2f);
        }
        this.randomizeAttributes(serverLevelAccessor.getRandom());
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData);
    }

    public boolean hasInventoryChanged(Container container) {
        return this.inventory != container;
    }

    public int getAmbientStandInterval() {
        return this.getAmbientSoundInterval();
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions entityDimensions, float f) {
        return super.getPassengerAttachmentPoint(entity, entityDimensions, f).add(new Vec3(0.0, 0.15 * (double)this.standAnimO * (double)f, -0.7 * (double)this.standAnimO * (double)f).yRot(-this.getYRot() * ((float)Math.PI / 180)));
    }

    public int getInventoryColumns() {
        return 0;
    }
}

