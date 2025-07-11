/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.animal;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractFish
extends WaterAnimal
implements Bucketable {
    private static final EntityDataAccessor<Boolean> FROM_BUCKET = SynchedEntityData.defineId(AbstractFish.class, EntityDataSerializers.BOOLEAN);
    private static final boolean DEFAULT_FROM_BUCKET = false;

    public AbstractFish(EntityType<? extends AbstractFish> entityType, Level level) {
        super((EntityType<? extends WaterAnimal>)entityType, level);
        this.moveControl = new FishMoveControl(this);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 3.0);
    }

    @Override
    public boolean requiresCustomPersistence() {
        return super.requiresCustomPersistence() || this.fromBucket();
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        return !this.fromBucket() && !this.hasCustomName();
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 8;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(FROM_BUCKET, false);
    }

    @Override
    public boolean fromBucket() {
        return this.entityData.get(FROM_BUCKET);
    }

    @Override
    public void setFromBucket(boolean bl) {
        this.entityData.set(FROM_BUCKET, bl);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putBoolean("FromBucket", this.fromBucket());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.setFromBucket(valueInput.getBooleanOr("FromBucket", false));
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new PanicGoal(this, 1.25));
        this.goalSelector.addGoal(2, new AvoidEntityGoal<Player>(this, Player.class, 8.0f, 1.6, 1.4, EntitySelector.NO_SPECTATORS::test));
        this.goalSelector.addGoal(4, new FishSwimGoal(this));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    @Override
    public void travel(Vec3 vec3) {
        if (this.isInWater()) {
            this.moveRelative(0.01f, vec3);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9));
            if (this.getTarget() == null) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.005, 0.0));
            }
        } else {
            super.travel(vec3);
        }
    }

    @Override
    public void aiStep() {
        if (!this.isInWater() && this.onGround() && this.verticalCollision) {
            this.setDeltaMovement(this.getDeltaMovement().add((this.random.nextFloat() * 2.0f - 1.0f) * 0.05f, 0.4f, (this.random.nextFloat() * 2.0f - 1.0f) * 0.05f));
            this.setOnGround(false);
            this.hasImpulse = true;
            this.makeSound(this.getFlopSound());
        }
        super.aiStep();
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        return Bucketable.bucketMobPickup(player, interactionHand, this).orElse(super.mobInteract(player, interactionHand));
    }

    @Override
    public void saveToBucketTag(ItemStack itemStack) {
        Bucketable.saveDefaultDataToBucketTag(this, itemStack);
    }

    @Override
    public void loadFromBucketTag(CompoundTag compoundTag) {
        Bucketable.loadDefaultDataFromBucketTag(this, compoundTag);
    }

    @Override
    public SoundEvent getPickupSound() {
        return SoundEvents.BUCKET_FILL_FISH;
    }

    protected boolean canRandomSwim() {
        return true;
    }

    protected abstract SoundEvent getFlopSound();

    @Override
    protected SoundEvent getSwimSound() {
        return SoundEvents.FISH_SWIM;
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
    }

    static class FishMoveControl
    extends MoveControl {
        private final AbstractFish fish;

        FishMoveControl(AbstractFish abstractFish) {
            super(abstractFish);
            this.fish = abstractFish;
        }

        @Override
        public void tick() {
            if (this.fish.isEyeInFluid(FluidTags.WATER)) {
                this.fish.setDeltaMovement(this.fish.getDeltaMovement().add(0.0, 0.005, 0.0));
            }
            if (this.operation != MoveControl.Operation.MOVE_TO || this.fish.getNavigation().isDone()) {
                this.fish.setSpeed(0.0f);
                return;
            }
            float f = (float)(this.speedModifier * this.fish.getAttributeValue(Attributes.MOVEMENT_SPEED));
            this.fish.setSpeed(Mth.lerp(0.125f, this.fish.getSpeed(), f));
            double d = this.wantedX - this.fish.getX();
            double d2 = this.wantedY - this.fish.getY();
            double d3 = this.wantedZ - this.fish.getZ();
            if (d2 != 0.0) {
                double d4 = Math.sqrt(d * d + d2 * d2 + d3 * d3);
                this.fish.setDeltaMovement(this.fish.getDeltaMovement().add(0.0, (double)this.fish.getSpeed() * (d2 / d4) * 0.1, 0.0));
            }
            if (d != 0.0 || d3 != 0.0) {
                float f2 = (float)(Mth.atan2(d3, d) * 57.2957763671875) - 90.0f;
                this.fish.setYRot(this.rotlerp(this.fish.getYRot(), f2, 90.0f));
                this.fish.yBodyRot = this.fish.getYRot();
            }
        }
    }

    static class FishSwimGoal
    extends RandomSwimmingGoal {
        private final AbstractFish fish;

        public FishSwimGoal(AbstractFish abstractFish) {
            super(abstractFish, 1.0, 40);
            this.fish = abstractFish;
        }

        @Override
        public boolean canUse() {
            return this.fish.canRandomSwim() && super.canUse();
        }
    }
}

