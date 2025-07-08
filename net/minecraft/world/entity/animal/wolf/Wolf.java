/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.animal.wolf;

import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Crackiness;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BegGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.goal.target.ResetUniversalAngerTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Llama;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariant;
import net.minecraft.world.entity.animal.wolf.WolfSoundVariants;
import net.minecraft.world.entity.animal.wolf.WolfVariant;
import net.minecraft.world.entity.animal.wolf.WolfVariants;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.variant.SpawnContext;
import net.minecraft.world.entity.variant.VariantUtils;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class Wolf
extends TamableAnimal
implements NeutralMob {
    private static final EntityDataAccessor<Boolean> DATA_INTERESTED_ID = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_COLLAR_COLOR = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_REMAINING_ANGER_TIME = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Holder<WolfVariant>> DATA_VARIANT_ID = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.WOLF_VARIANT);
    private static final EntityDataAccessor<Holder<WolfSoundVariant>> DATA_SOUND_VARIANT_ID = SynchedEntityData.defineId(Wolf.class, EntityDataSerializers.WOLF_SOUND_VARIANT);
    public static final TargetingConditions.Selector PREY_SELECTOR = (livingEntity, serverLevel) -> {
        EntityType<?> entityType = livingEntity.getType();
        return entityType == EntityType.SHEEP || entityType == EntityType.RABBIT || entityType == EntityType.FOX;
    };
    private static final float START_HEALTH = 8.0f;
    private static final float TAME_HEALTH = 40.0f;
    private static final float ARMOR_REPAIR_UNIT = 0.125f;
    public static final float DEFAULT_TAIL_ANGLE = 0.62831855f;
    private static final DyeColor DEFAULT_COLLAR_COLOR = DyeColor.RED;
    private float interestedAngle;
    private float interestedAngleO;
    private boolean isWet;
    private boolean isShaking;
    private float shakeAnim;
    private float shakeAnimO;
    private static final UniformInt PERSISTENT_ANGER_TIME = TimeUtil.rangeOfSeconds(20, 39);
    @Nullable
    private UUID persistentAngerTarget;

    public Wolf(EntityType<? extends Wolf> entityType, Level level) {
        super((EntityType<? extends TamableAnimal>)entityType, level);
        this.setTame(false, false);
        this.setPathfindingMalus(PathType.POWDER_SNOW, -1.0f);
        this.setPathfindingMalus(PathType.DANGER_POWDER_SNOW, -1.0f);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TamableAnimal.TamableAnimalPanicGoal(1.5, DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES));
        this.goalSelector.addGoal(2, new SitWhenOrderedToGoal(this));
        this.goalSelector.addGoal(3, new WolfAvoidEntityGoal<Llama>(this, Llama.class, 24.0f, 1.5, 1.5));
        this.goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.4f));
        this.goalSelector.addGoal(5, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(6, new FollowOwnerGoal(this, 1.0, 10.0f, 2.0f));
        this.goalSelector.addGoal(7, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(8, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(9, new BegGoal(this, 8.0f));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Player.class, 8.0f));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this, new Class[0]).setAlertOthers(new Class[0]));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<Player>(this, Player.class, 10, true, false, this::isAngryAt));
        this.targetSelector.addGoal(5, new NonTameRandomTargetGoal<Animal>(this, Animal.class, false, PREY_SELECTOR));
        this.targetSelector.addGoal(6, new NonTameRandomTargetGoal<Turtle>(this, Turtle.class, false, Turtle.BABY_ON_LAND_SELECTOR));
        this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<AbstractSkeleton>((Mob)this, AbstractSkeleton.class, false));
        this.targetSelector.addGoal(8, new ResetUniversalAngerTargetGoal<Wolf>(this, true));
    }

    public ResourceLocation getTexture() {
        WolfVariant wolfVariant = this.getVariant().value();
        if (this.isTame()) {
            return wolfVariant.assetInfo().tame().texturePath();
        }
        if (this.isAngry()) {
            return wolfVariant.assetInfo().angry().texturePath();
        }
        return wolfVariant.assetInfo().wild().texturePath();
    }

    private Holder<WolfVariant> getVariant() {
        return this.entityData.get(DATA_VARIANT_ID);
    }

    private void setVariant(Holder<WolfVariant> holder) {
        this.entityData.set(DATA_VARIANT_ID, holder);
    }

    private Holder<WolfSoundVariant> getSoundVariant() {
        return this.entityData.get(DATA_SOUND_VARIANT_ID);
    }

    private void setSoundVariant(Holder<WolfSoundVariant> holder) {
        this.entityData.set(DATA_SOUND_VARIANT_ID, holder);
    }

    @Override
    @Nullable
    public <T> T get(DataComponentType<? extends T> dataComponentType) {
        if (dataComponentType == DataComponents.WOLF_VARIANT) {
            return Wolf.castComponentValue(dataComponentType, this.getVariant());
        }
        if (dataComponentType == DataComponents.WOLF_SOUND_VARIANT) {
            return Wolf.castComponentValue(dataComponentType, this.getSoundVariant());
        }
        if (dataComponentType == DataComponents.WOLF_COLLAR) {
            return Wolf.castComponentValue(dataComponentType, this.getCollarColor());
        }
        return super.get(dataComponentType);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter dataComponentGetter) {
        this.applyImplicitComponentIfPresent(dataComponentGetter, DataComponents.WOLF_VARIANT);
        this.applyImplicitComponentIfPresent(dataComponentGetter, DataComponents.WOLF_SOUND_VARIANT);
        this.applyImplicitComponentIfPresent(dataComponentGetter, DataComponents.WOLF_COLLAR);
        super.applyImplicitComponents(dataComponentGetter);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> dataComponentType, T t) {
        if (dataComponentType == DataComponents.WOLF_VARIANT) {
            this.setVariant(Wolf.castComponentValue(DataComponents.WOLF_VARIANT, t));
            return true;
        }
        if (dataComponentType == DataComponents.WOLF_SOUND_VARIANT) {
            this.setSoundVariant(Wolf.castComponentValue(DataComponents.WOLF_SOUND_VARIANT, t));
            return true;
        }
        if (dataComponentType == DataComponents.WOLF_COLLAR) {
            this.setCollarColor(Wolf.castComponentValue(DataComponents.WOLF_COLLAR, t));
            return true;
        }
        return super.applyImplicitComponent(dataComponentType, t);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MOVEMENT_SPEED, 0.3f).add(Attributes.MAX_HEALTH, 8.0).add(Attributes.ATTACK_DAMAGE, 4.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        HolderLookup.RegistryLookup registryLookup = this.registryAccess().lookupOrThrow(Registries.WOLF_SOUND_VARIANT);
        builder.define(DATA_VARIANT_ID, VariantUtils.getDefaultOrAny(this.registryAccess(), WolfVariants.DEFAULT));
        builder.define(DATA_SOUND_VARIANT_ID, (Holder)registryLookup.get(WolfSoundVariants.CLASSIC).or(((Registry)registryLookup)::getAny).orElseThrow());
        builder.define(DATA_INTERESTED_ID, false);
        builder.define(DATA_COLLAR_COLOR, DEFAULT_COLLAR_COLOR.getId());
        builder.define(DATA_REMAINING_ANGER_TIME, 0);
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(SoundEvents.WOLF_STEP, 0.15f, 1.0f);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.store("CollarColor", DyeColor.LEGACY_ID_CODEC, this.getCollarColor());
        VariantUtils.writeVariant(valueOutput, this.getVariant());
        this.addPersistentAngerSaveData(valueOutput);
        this.getSoundVariant().unwrapKey().ifPresent(resourceKey -> valueOutput.store("sound_variant", ResourceKey.codec(Registries.WOLF_SOUND_VARIANT), resourceKey));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        VariantUtils.readVariant(valueInput, Registries.WOLF_VARIANT).ifPresent(this::setVariant);
        this.setCollarColor(valueInput.read("CollarColor", DyeColor.LEGACY_ID_CODEC).orElse(DEFAULT_COLLAR_COLOR));
        this.readPersistentAngerSaveData(this.level(), valueInput);
        valueInput.read("sound_variant", ResourceKey.codec(Registries.WOLF_SOUND_VARIANT)).flatMap(resourceKey -> this.registryAccess().lookupOrThrow(Registries.WOLF_SOUND_VARIANT).get((ResourceKey)resourceKey)).ifPresent(this::setSoundVariant);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData) {
        if (spawnGroupData instanceof WolfPackData) {
            WolfPackData wolfPackData = (WolfPackData)spawnGroupData;
            this.setVariant(wolfPackData.type);
        } else {
            Optional optional = VariantUtils.selectVariantToSpawn(SpawnContext.create(serverLevelAccessor, this.blockPosition()), Registries.WOLF_VARIANT);
            if (optional.isPresent()) {
                this.setVariant(optional.get());
                spawnGroupData = new WolfPackData(optional.get());
            }
        }
        this.setSoundVariant(WolfSoundVariants.pickRandomSoundVariant(this.registryAccess(), serverLevelAccessor.getRandom()));
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isAngry()) {
            return this.getSoundVariant().value().growlSound().value();
        }
        if (this.random.nextInt(3) == 0) {
            if (this.isTame() && this.getHealth() < 20.0f) {
                return this.getSoundVariant().value().whineSound().value();
            }
            return this.getSoundVariant().value().pantSound().value();
        }
        return this.getSoundVariant().value().ambientSound().value();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        if (this.canArmorAbsorb(damageSource)) {
            return SoundEvents.WOLF_ARMOR_DAMAGE;
        }
        return this.getSoundVariant().value().hurtSound().value();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.getSoundVariant().value().deathSound().value();
    }

    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level().isClientSide && this.isWet && !this.isShaking && !this.isPathFinding() && this.onGround()) {
            this.isShaking = true;
            this.shakeAnim = 0.0f;
            this.shakeAnimO = 0.0f;
            this.level().broadcastEntityEvent(this, (byte)8);
        }
        if (!this.level().isClientSide) {
            this.updatePersistentAnger((ServerLevel)this.level(), true);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.isAlive()) {
            return;
        }
        this.interestedAngleO = this.interestedAngle;
        this.interestedAngle = this.isInterested() ? (this.interestedAngle += (1.0f - this.interestedAngle) * 0.4f) : (this.interestedAngle += (0.0f - this.interestedAngle) * 0.4f);
        if (this.isInWaterOrRain()) {
            this.isWet = true;
            if (this.isShaking && !this.level().isClientSide) {
                this.level().broadcastEntityEvent(this, (byte)56);
                this.cancelShake();
            }
        } else if ((this.isWet || this.isShaking) && this.isShaking) {
            if (this.shakeAnim == 0.0f) {
                this.playSound(SoundEvents.WOLF_SHAKE, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
                this.gameEvent(GameEvent.ENTITY_ACTION);
            }
            this.shakeAnimO = this.shakeAnim;
            this.shakeAnim += 0.05f;
            if (this.shakeAnimO >= 2.0f) {
                this.isWet = false;
                this.isShaking = false;
                this.shakeAnimO = 0.0f;
                this.shakeAnim = 0.0f;
            }
            if (this.shakeAnim > 0.4f) {
                float f = (float)this.getY();
                int n = (int)(Mth.sin((this.shakeAnim - 0.4f) * (float)Math.PI) * 7.0f);
                Vec3 vec3 = this.getDeltaMovement();
                for (int i = 0; i < n; ++i) {
                    float f2 = (this.random.nextFloat() * 2.0f - 1.0f) * this.getBbWidth() * 0.5f;
                    float f3 = (this.random.nextFloat() * 2.0f - 1.0f) * this.getBbWidth() * 0.5f;
                    this.level().addParticle(ParticleTypes.SPLASH, this.getX() + (double)f2, f + 0.8f, this.getZ() + (double)f3, vec3.x, vec3.y, vec3.z);
                }
            }
        }
    }

    private void cancelShake() {
        this.isShaking = false;
        this.shakeAnim = 0.0f;
        this.shakeAnimO = 0.0f;
    }

    @Override
    public void die(DamageSource damageSource) {
        this.isWet = false;
        this.isShaking = false;
        this.shakeAnimO = 0.0f;
        this.shakeAnim = 0.0f;
        super.die(damageSource);
    }

    public float getWetShade(float f) {
        if (!this.isWet) {
            return 1.0f;
        }
        return Math.min(0.75f + Mth.lerp(f, this.shakeAnimO, this.shakeAnim) / 2.0f * 0.25f, 1.0f);
    }

    public float getShakeAnim(float f) {
        return Mth.lerp(f, this.shakeAnimO, this.shakeAnim);
    }

    public float getHeadRollAngle(float f) {
        return Mth.lerp(f, this.interestedAngleO, this.interestedAngle) * 0.15f * (float)Math.PI;
    }

    @Override
    public int getMaxHeadXRot() {
        if (this.isInSittingPose()) {
            return 20;
        }
        return super.getMaxHeadXRot();
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        if (this.isInvulnerableTo(serverLevel, damageSource)) {
            return false;
        }
        this.setOrderedToSit(false);
        return super.hurtServer(serverLevel, damageSource, f);
    }

    @Override
    protected void actuallyHurt(ServerLevel serverLevel, DamageSource damageSource, float f) {
        if (!this.canArmorAbsorb(damageSource)) {
            super.actuallyHurt(serverLevel, damageSource, f);
            return;
        }
        ItemStack itemStack = this.getBodyArmorItem();
        int n = itemStack.getDamageValue();
        int n2 = itemStack.getMaxDamage();
        itemStack.hurtAndBreak(Mth.ceil(f), (LivingEntity)this, EquipmentSlot.BODY);
        if (Crackiness.WOLF_ARMOR.byDamage(n, n2) != Crackiness.WOLF_ARMOR.byDamage(this.getBodyArmorItem())) {
            this.playSound(SoundEvents.WOLF_ARMOR_CRACK);
            serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, Items.ARMADILLO_SCUTE.getDefaultInstance()), this.getX(), this.getY() + 1.0, this.getZ(), 20, 0.2, 0.1, 0.2, 0.1);
        }
    }

    private boolean canArmorAbsorb(DamageSource damageSource) {
        return this.getBodyArmorItem().is(Items.WOLF_ARMOR) && !damageSource.is(DamageTypeTags.BYPASSES_WOLF_ARMOR);
    }

    @Override
    protected void applyTamingSideEffects() {
        if (this.isTame()) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(40.0);
            this.setHealth(40.0f);
        } else {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(8.0);
        }
    }

    @Override
    protected void hurtArmor(DamageSource damageSource, float f) {
        this.doHurtEquipment(damageSource, f, EquipmentSlot.BODY);
    }

    @Override
    protected boolean canShearEquipment(Player player) {
        return this.isOwnedBy(player);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        Item item = itemStack.getItem();
        if (this.isTame()) {
            if (this.isFood(itemStack) && this.getHealth() < this.getMaxHealth()) {
                this.usePlayerItem(player, interactionHand, itemStack);
                FoodProperties foodProperties = itemStack.get(DataComponents.FOOD);
                float f = foodProperties != null ? (float)foodProperties.nutrition() : 1.0f;
                this.heal(2.0f * f);
                return InteractionResult.SUCCESS;
            }
            if (item instanceof DyeItem) {
                DyeItem dyeItem = (DyeItem)item;
                if (this.isOwnedBy(player)) {
                    DyeColor dyeColor = dyeItem.getDyeColor();
                    if (dyeColor == this.getCollarColor()) return super.mobInteract(player, interactionHand);
                    this.setCollarColor(dyeColor);
                    itemStack.consume(1, player);
                    return InteractionResult.SUCCESS;
                }
            }
            if (this.isEquippableInSlot(itemStack, EquipmentSlot.BODY) && !this.isWearingBodyArmor() && this.isOwnedBy(player) && !this.isBaby()) {
                this.setBodyArmorItem(itemStack.copyWithCount(1));
                itemStack.consume(1, player);
                return InteractionResult.SUCCESS;
            }
            if (this.isInSittingPose() && this.isWearingBodyArmor() && this.isOwnedBy(player) && this.getBodyArmorItem().isDamaged() && this.getBodyArmorItem().isValidRepairItem(itemStack)) {
                itemStack.shrink(1);
                this.playSound(SoundEvents.WOLF_ARMOR_REPAIR);
                ItemStack itemStack2 = this.getBodyArmorItem();
                int n = (int)((float)itemStack2.getMaxDamage() * 0.125f);
                itemStack2.setDamageValue(Math.max(0, itemStack2.getDamageValue() - n));
                return InteractionResult.SUCCESS;
            }
            InteractionResult interactionResult = super.mobInteract(player, interactionHand);
            if (interactionResult.consumesAction() || !this.isOwnedBy(player)) return interactionResult;
            this.setOrderedToSit(!this.isOrderedToSit());
            this.jumping = false;
            this.navigation.stop();
            this.setTarget(null);
            return InteractionResult.SUCCESS.withoutItem();
        }
        if (this.level().isClientSide || !itemStack.is(Items.BONE) || this.isAngry()) return super.mobInteract(player, interactionHand);
        itemStack.consume(1, player);
        this.tryToTame(player);
        return InteractionResult.SUCCESS_SERVER;
    }

    private void tryToTame(Player player) {
        if (this.random.nextInt(3) == 0) {
            this.tame(player);
            this.navigation.stop();
            this.setTarget(null);
            this.setOrderedToSit(true);
            this.level().broadcastEntityEvent(this, (byte)7);
        } else {
            this.level().broadcastEntityEvent(this, (byte)6);
        }
    }

    @Override
    public void handleEntityEvent(byte by) {
        if (by == 8) {
            this.isShaking = true;
            this.shakeAnim = 0.0f;
            this.shakeAnimO = 0.0f;
        } else if (by == 56) {
            this.cancelShake();
        } else {
            super.handleEntityEvent(by);
        }
    }

    public float getTailAngle() {
        if (this.isAngry()) {
            return 1.5393804f;
        }
        if (this.isTame()) {
            float f = this.getMaxHealth();
            float f2 = (f - this.getHealth()) / f;
            return (0.55f - f2 * 0.4f) * (float)Math.PI;
        }
        return 0.62831855f;
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(ItemTags.WOLF_FOOD);
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 8;
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
    public void startPersistentAngerTimer() {
        this.setRemainingPersistentAngerTime(PERSISTENT_ANGER_TIME.sample(this.random));
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

    public DyeColor getCollarColor() {
        return DyeColor.byId(this.entityData.get(DATA_COLLAR_COLOR));
    }

    private void setCollarColor(DyeColor dyeColor) {
        this.entityData.set(DATA_COLLAR_COLOR, dyeColor.getId());
    }

    @Override
    @Nullable
    public Wolf getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        Wolf wolf = EntityType.WOLF.create(serverLevel, EntitySpawnReason.BREEDING);
        if (wolf != null && ageableMob instanceof Wolf) {
            Wolf wolf2 = (Wolf)ageableMob;
            if (this.random.nextBoolean()) {
                wolf.setVariant(this.getVariant());
            } else {
                wolf.setVariant(wolf2.getVariant());
            }
            if (this.isTame()) {
                wolf.setOwnerReference(this.getOwnerReference());
                wolf.setTame(true, true);
                DyeColor dyeColor = this.getCollarColor();
                DyeColor dyeColor2 = wolf2.getCollarColor();
                wolf.setCollarColor(DyeColor.getMixedColor(serverLevel, dyeColor, dyeColor2));
            }
            wolf.setSoundVariant(WolfSoundVariants.pickRandomSoundVariant(this.registryAccess(), this.random));
        }
        return wolf;
    }

    public void setIsInterested(boolean bl) {
        this.entityData.set(DATA_INTERESTED_ID, bl);
    }

    @Override
    public boolean canMate(Animal animal) {
        if (animal == this) {
            return false;
        }
        if (!this.isTame()) {
            return false;
        }
        if (!(animal instanceof Wolf)) {
            return false;
        }
        Wolf wolf = (Wolf)animal;
        if (!wolf.isTame()) {
            return false;
        }
        if (wolf.isInSittingPose()) {
            return false;
        }
        return this.isInLove() && wolf.isInLove();
    }

    public boolean isInterested() {
        return this.entityData.get(DATA_INTERESTED_ID);
    }

    @Override
    public boolean wantsToAttack(LivingEntity livingEntity, LivingEntity livingEntity2) {
        LivingEntity livingEntity3;
        if (livingEntity instanceof Creeper || livingEntity instanceof Ghast || livingEntity instanceof ArmorStand) {
            return false;
        }
        if (livingEntity instanceof Wolf) {
            Wolf wolf = (Wolf)livingEntity;
            return !wolf.isTame() || wolf.getOwner() != livingEntity2;
        }
        if (livingEntity instanceof Player) {
            Player player;
            livingEntity3 = (Player)livingEntity;
            if (livingEntity2 instanceof Player && !(player = (Player)livingEntity2).canHarmPlayer((Player)livingEntity3)) {
                return false;
            }
        }
        if (livingEntity instanceof AbstractHorse && ((AbstractHorse)(livingEntity3 = (AbstractHorse)livingEntity)).isTamed()) {
            return false;
        }
        return !(livingEntity instanceof TamableAnimal) || !((TamableAnimal)(livingEntity3 = (TamableAnimal)livingEntity)).isTame();
    }

    @Override
    public boolean canBeLeashed() {
        return !this.isAngry();
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, 0.6f * this.getEyeHeight(), this.getBbWidth() * 0.4f);
    }

    public static boolean checkWolfSpawnRules(EntityType<Wolf> entityType, LevelAccessor levelAccessor, EntitySpawnReason entitySpawnReason, BlockPos blockPos, RandomSource randomSource) {
        return levelAccessor.getBlockState(blockPos.below()).is(BlockTags.WOLVES_SPAWNABLE_ON) && Wolf.isBrightEnoughToSpawn(levelAccessor, blockPos);
    }

    @Override
    @Nullable
    public /* synthetic */ AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return this.getBreedOffspring(serverLevel, ageableMob);
    }

    class WolfAvoidEntityGoal<T extends LivingEntity>
    extends AvoidEntityGoal<T> {
        private final Wolf wolf;

        public WolfAvoidEntityGoal(Wolf wolf2, Class<T> clazz, float f, double d, double d2) {
            super(wolf2, clazz, f, d, d2);
            this.wolf = wolf2;
        }

        @Override
        public boolean canUse() {
            if (super.canUse() && this.toAvoid instanceof Llama) {
                return !this.wolf.isTame() && this.avoidLlama((Llama)this.toAvoid);
            }
            return false;
        }

        private boolean avoidLlama(Llama llama) {
            return llama.getStrength() >= Wolf.this.random.nextInt(5);
        }

        @Override
        public void start() {
            Wolf.this.setTarget(null);
            super.start();
        }

        @Override
        public void tick() {
            Wolf.this.setTarget(null);
            super.tick();
        }
    }

    public static class WolfPackData
    extends AgeableMob.AgeableMobGroupData {
        public final Holder<WolfVariant> type;

        public WolfPackData(Holder<WolfVariant> holder) {
            super(false);
            this.type = holder;
        }
    }
}

