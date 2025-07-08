/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.animal;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ItemBasedSteering;
import net.minecraft.world.entity.ItemSteerable;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.PigVariant;
import net.minecraft.world.entity.animal.PigVariants;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.variant.SpawnContext;
import net.minecraft.world.entity.variant.VariantUtils;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Pig
extends Animal
implements ItemSteerable {
    private static final EntityDataAccessor<Integer> DATA_BOOST_TIME = SynchedEntityData.defineId(Pig.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Holder<PigVariant>> DATA_VARIANT_ID = SynchedEntityData.defineId(Pig.class, EntityDataSerializers.PIG_VARIANT);
    private final ItemBasedSteering steering;

    public Pig(EntityType<? extends Pig> entityType, Level level) {
        super((EntityType<? extends Animal>)entityType, level);
        this.steering = new ItemBasedSteering(this.entityData, DATA_BOOST_TIME);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.25));
        this.goalSelector.addGoal(3, new BreedGoal(this, 1.0));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.2, itemStack -> itemStack.is(Items.CARROT_ON_A_STICK), false));
        this.goalSelector.addGoal(4, new TemptGoal(this, 1.2, itemStack -> itemStack.is(ItemTags.PIG_FOOD), false));
        this.goalSelector.addGoal(5, new FollowParentGoal(this, 1.1));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 0.25);
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        Player player;
        Entity entity;
        if (this.isSaddled() && (entity = this.getFirstPassenger()) instanceof Player && (player = (Player)entity).isHolding(Items.CARROT_ON_A_STICK)) {
            return player;
        }
        return super.getControllingPassenger();
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
        builder.define(DATA_VARIANT_ID, VariantUtils.getDefaultOrAny(this.registryAccess(), PigVariants.DEFAULT));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        VariantUtils.writeVariant(valueOutput, this.getVariant());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        VariantUtils.readVariant(valueInput, Registries.PIG_VARIANT).ifPresent(this::setVariant);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PIG_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PIG_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PIG_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(SoundEvents.PIG_STEP, 0.15f, 1.0f);
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
        return interactionResult;
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
            return SoundEvents.PIG_SADDLE;
        }
        return super.getEquipSound(equipmentSlot, itemStack, equippable);
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity livingEntity) {
        Direction direction = this.getMotionDirection();
        if (direction.getAxis() == Direction.Axis.Y) {
            return super.getDismountLocationForPassenger(livingEntity);
        }
        int[][] nArray = DismountHelper.offsetsForDirection(direction);
        BlockPos blockPos = this.blockPosition();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (Pose pose : livingEntity.getDismountPoses()) {
            AABB aABB = livingEntity.getLocalBoundsForPose(pose);
            for (int[] nArray2 : nArray) {
                mutableBlockPos.set(blockPos.getX() + nArray2[0], blockPos.getY(), blockPos.getZ() + nArray2[1]);
                double d = this.level().getBlockFloorHeight(mutableBlockPos);
                if (!DismountHelper.isBlockFloorValid(d)) continue;
                Vec3 vec3 = Vec3.upFromBottomCenterOf(mutableBlockPos, d);
                if (!DismountHelper.canDismountTo(this.level(), livingEntity, aABB.move(vec3))) continue;
                livingEntity.setPose(pose);
                return vec3;
            }
        }
        return super.getDismountLocationForPassenger(livingEntity);
    }

    @Override
    public void thunderHit(ServerLevel serverLevel, LightningBolt lightningBolt) {
        if (serverLevel.getDifficulty() != Difficulty.PEACEFUL) {
            ZombifiedPiglin zombifiedPiglin2 = this.convertTo(EntityType.ZOMBIFIED_PIGLIN, ConversionParams.single(this, false, true), zombifiedPiglin -> {
                if (this.getMainHandItem().isEmpty()) {
                    zombifiedPiglin.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
                }
                zombifiedPiglin.setPersistenceRequired();
            });
            if (zombifiedPiglin2 == null) {
                super.thunderHit(serverLevel, lightningBolt);
            }
        } else {
            super.thunderHit(serverLevel, lightningBolt);
        }
    }

    @Override
    protected void tickRidden(Player player, Vec3 vec3) {
        super.tickRidden(player, vec3);
        this.setRot(player.getYRot(), player.getXRot() * 0.5f);
        this.yBodyRot = this.yHeadRot = this.getYRot();
        this.yRotO = this.yHeadRot;
        this.steering.tickBoost();
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 vec3) {
        return new Vec3(0.0, 0.0, 1.0);
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        return (float)(this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.225 * (double)this.steering.boostFactor());
    }

    @Override
    public boolean boost() {
        return this.steering.boost(this.getRandom());
    }

    @Override
    @Nullable
    public Pig getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        Pig pig = EntityType.PIG.create(serverLevel, EntitySpawnReason.BREEDING);
        if (pig != null && ageableMob instanceof Pig) {
            Pig pig2 = (Pig)ageableMob;
            pig.setVariant(this.random.nextBoolean() ? this.getVariant() : pig2.getVariant());
        }
        return pig;
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(ItemTags.PIG_FOOD);
    }

    @Override
    public Vec3 getLeashOffset() {
        return new Vec3(0.0, 0.6f * this.getEyeHeight(), this.getBbWidth() * 0.4f);
    }

    private void setVariant(Holder<PigVariant> holder) {
        this.entityData.set(DATA_VARIANT_ID, holder);
    }

    public Holder<PigVariant> getVariant() {
        return this.entityData.get(DATA_VARIANT_ID);
    }

    @Override
    @Nullable
    public <T> T get(DataComponentType<? extends T> dataComponentType) {
        if (dataComponentType == DataComponents.PIG_VARIANT) {
            return Pig.castComponentValue(dataComponentType, this.getVariant());
        }
        return super.get(dataComponentType);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter dataComponentGetter) {
        this.applyImplicitComponentIfPresent(dataComponentGetter, DataComponents.PIG_VARIANT);
        super.applyImplicitComponents(dataComponentGetter);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> dataComponentType, T t) {
        if (dataComponentType == DataComponents.PIG_VARIANT) {
            this.setVariant(Pig.castComponentValue(DataComponents.PIG_VARIANT, t));
            return true;
        }
        return super.applyImplicitComponent(dataComponentType, t);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData) {
        VariantUtils.selectVariantToSpawn(SpawnContext.create(serverLevelAccessor, this.blockPosition()), Registries.PIG_VARIANT).ifPresent(this::setVariant);
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData);
    }

    @Override
    @Nullable
    public /* synthetic */ AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return this.getBreedOffspring(serverLevel, ageableMob);
    }
}

