/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.animal.horse;

import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityAttachments;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Donkey;
import net.minecraft.world.entity.animal.horse.Markings;
import net.minecraft.world.entity.animal.horse.Mule;
import net.minecraft.world.entity.animal.horse.Variant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class Horse
extends AbstractHorse {
    private static final EntityDataAccessor<Integer> DATA_ID_TYPE_VARIANT = SynchedEntityData.defineId(Horse.class, EntityDataSerializers.INT);
    private static final EntityDimensions BABY_DIMENSIONS = EntityType.HORSE.getDimensions().withAttachments(EntityAttachments.builder().attach(EntityAttachment.PASSENGER, 0.0f, EntityType.HORSE.getHeight() + 0.125f, 0.0f)).scale(0.5f);
    private static final int DEFAULT_VARIANT = 0;

    public Horse(EntityType<? extends Horse> entityType, Level level) {
        super((EntityType<? extends AbstractHorse>)entityType, level);
    }

    @Override
    protected void randomizeAttributes(RandomSource randomSource) {
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(Horse.generateMaxHealth(randomSource::nextInt));
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(Horse.generateSpeed(randomSource::nextDouble));
        this.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(Horse.generateJumpStrength(randomSource::nextDouble));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ID_TYPE_VARIANT, 0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putInt("Variant", this.getTypeVariant());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.setTypeVariant(valueInput.getIntOr("Variant", 0));
    }

    private void setTypeVariant(int n) {
        this.entityData.set(DATA_ID_TYPE_VARIANT, n);
    }

    private int getTypeVariant() {
        return this.entityData.get(DATA_ID_TYPE_VARIANT);
    }

    private void setVariantAndMarkings(Variant variant, Markings markings) {
        this.setTypeVariant(variant.getId() & 0xFF | markings.getId() << 8 & 0xFF00);
    }

    public Variant getVariant() {
        return Variant.byId(this.getTypeVariant() & 0xFF);
    }

    private void setVariant(Variant variant) {
        this.setTypeVariant(variant.getId() & 0xFF | this.getTypeVariant() & 0xFFFFFF00);
    }

    @Override
    @Nullable
    public <T> T get(DataComponentType<? extends T> dataComponentType) {
        if (dataComponentType == DataComponents.HORSE_VARIANT) {
            return Horse.castComponentValue(dataComponentType, this.getVariant());
        }
        return super.get(dataComponentType);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter dataComponentGetter) {
        this.applyImplicitComponentIfPresent(dataComponentGetter, DataComponents.HORSE_VARIANT);
        super.applyImplicitComponents(dataComponentGetter);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> dataComponentType, T t) {
        if (dataComponentType == DataComponents.HORSE_VARIANT) {
            this.setVariant(Horse.castComponentValue(DataComponents.HORSE_VARIANT, t));
            return true;
        }
        return super.applyImplicitComponent(dataComponentType, t);
    }

    public Markings getMarkings() {
        return Markings.byId((this.getTypeVariant() & 0xFF00) >> 8);
    }

    @Override
    protected void playGallopSound(SoundType soundType) {
        super.playGallopSound(soundType);
        if (this.random.nextInt(10) == 0) {
            this.playSound(SoundEvents.HORSE_BREATHE, soundType.getVolume() * 0.6f, soundType.getPitch());
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.HORSE_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.HORSE_DEATH;
    }

    @Override
    @Nullable
    protected SoundEvent getEatingSound() {
        return SoundEvents.HORSE_EAT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.HORSE_HURT;
    }

    @Override
    protected SoundEvent getAngrySound() {
        return SoundEvents.HORSE_ANGRY;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        boolean bl;
        boolean bl2 = bl = !this.isBaby() && this.isTamed() && player.isSecondaryUseActive();
        if (this.isVehicle() || bl) {
            return super.mobInteract(player, interactionHand);
        }
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (!itemStack.isEmpty()) {
            if (this.isFood(itemStack)) {
                return this.fedFood(player, itemStack);
            }
            if (!this.isTamed()) {
                this.makeMad();
                return InteractionResult.SUCCESS;
            }
        }
        return super.mobInteract(player, interactionHand);
    }

    @Override
    public boolean canMate(Animal animal) {
        if (animal == this) {
            return false;
        }
        if (animal instanceof Donkey || animal instanceof Horse) {
            return this.canParent() && ((AbstractHorse)animal).canParent();
        }
        return false;
    }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        if (ageableMob instanceof Donkey) {
            Mule mule = EntityType.MULE.create(serverLevel, EntitySpawnReason.BREEDING);
            if (mule != null) {
                this.setOffspringAttributes(ageableMob, mule);
            }
            return mule;
        }
        Horse horse = (Horse)ageableMob;
        Horse horse2 = EntityType.HORSE.create(serverLevel, EntitySpawnReason.BREEDING);
        if (horse2 != null) {
            int n = this.random.nextInt(9);
            Variant variant = n < 4 ? this.getVariant() : (n < 8 ? horse.getVariant() : Util.getRandom(Variant.values(), this.random));
            int n2 = this.random.nextInt(5);
            Markings markings = n2 < 2 ? this.getMarkings() : (n2 < 4 ? horse.getMarkings() : Util.getRandom(Markings.values(), this.random));
            horse2.setVariantAndMarkings(variant, markings);
            this.setOffspringAttributes(ageableMob, horse2);
        }
        return horse2;
    }

    @Override
    public boolean canUseSlot(EquipmentSlot equipmentSlot) {
        return true;
    }

    @Override
    protected void hurtArmor(DamageSource damageSource, float f) {
        this.doHurtEquipment(damageSource, f, EquipmentSlot.BODY);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData) {
        Variant variant;
        RandomSource randomSource = serverLevelAccessor.getRandom();
        if (spawnGroupData instanceof HorseGroupData) {
            variant = ((HorseGroupData)spawnGroupData).variant;
        } else {
            variant = Util.getRandom(Variant.values(), randomSource);
            spawnGroupData = new HorseGroupData(variant);
        }
        this.setVariantAndMarkings(variant, Util.getRandom(Markings.values(), randomSource));
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData);
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return this.isBaby() ? BABY_DIMENSIONS : super.getDefaultDimensions(pose);
    }

    public static class HorseGroupData
    extends AgeableMob.AgeableMobGroupData {
        public final Variant variant;

        public HorseGroupData(Variant variant) {
            super(true);
            this.variant = variant;
        }
    }
}

