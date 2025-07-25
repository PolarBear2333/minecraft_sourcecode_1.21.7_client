/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.serialization.Dynamic
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.animal.camel;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Dynamic;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.camel.CamelAi;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public class Camel
extends AbstractHorse {
    public static final float BABY_SCALE = 0.45f;
    public static final int DASH_COOLDOWN_TICKS = 55;
    public static final int MAX_HEAD_Y_ROT = 30;
    private static final float RUNNING_SPEED_BONUS = 0.1f;
    private static final float DASH_VERTICAL_MOMENTUM = 1.4285f;
    private static final float DASH_HORIZONTAL_MOMENTUM = 22.2222f;
    private static final int DASH_MINIMUM_DURATION_TICKS = 5;
    private static final int SITDOWN_DURATION_TICKS = 40;
    private static final int STANDUP_DURATION_TICKS = 52;
    private static final int IDLE_MINIMAL_DURATION_TICKS = 80;
    private static final float SITTING_HEIGHT_DIFFERENCE = 1.43f;
    private static final long DEFAULT_LAST_POSE_CHANGE_TICK = 0L;
    public static final EntityDataAccessor<Boolean> DASH = SynchedEntityData.defineId(Camel.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Long> LAST_POSE_CHANGE_TICK = SynchedEntityData.defineId(Camel.class, EntityDataSerializers.LONG);
    public final AnimationState sitAnimationState = new AnimationState();
    public final AnimationState sitPoseAnimationState = new AnimationState();
    public final AnimationState sitUpAnimationState = new AnimationState();
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState dashAnimationState = new AnimationState();
    private static final EntityDimensions SITTING_DIMENSIONS = EntityDimensions.scalable(EntityType.CAMEL.getWidth(), EntityType.CAMEL.getHeight() - 1.43f).withEyeHeight(0.845f);
    private int dashCooldown = 0;
    private int idleAnimationTimeout = 0;

    public Camel(EntityType<? extends Camel> entityType, Level level) {
        super((EntityType<? extends AbstractHorse>)entityType, level);
        this.moveControl = new CamelMoveControl();
        this.lookControl = new CamelLookControl();
        GroundPathNavigation groundPathNavigation = (GroundPathNavigation)this.getNavigation();
        groundPathNavigation.setCanFloat(true);
        groundPathNavigation.setCanWalkOverFences(true);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putLong("LastPoseTick", this.entityData.get(LAST_POSE_CHANGE_TICK));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        long l = valueInput.getLongOr("LastPoseTick", 0L);
        if (l < 0L) {
            this.setPose(Pose.SITTING);
        }
        this.resetLastPoseChangeTick(l);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Camel.createBaseHorseAttributes().add(Attributes.MAX_HEALTH, 32.0).add(Attributes.MOVEMENT_SPEED, 0.09f).add(Attributes.JUMP_STRENGTH, 0.42f).add(Attributes.STEP_HEIGHT, 1.5);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DASH, false);
        builder.define(LAST_POSE_CHANGE_TICK, 0L);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData) {
        CamelAi.initMemories(this, serverLevelAccessor.getRandom());
        this.resetLastPoseChangeTickToFullStand(serverLevelAccessor.getLevel().getGameTime());
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData);
    }

    public static boolean checkCamelSpawnRules(EntityType<Camel> entityType, LevelAccessor levelAccessor, EntitySpawnReason entitySpawnReason, BlockPos blockPos, RandomSource randomSource) {
        return levelAccessor.getBlockState(blockPos.below()).is(BlockTags.CAMELS_SPAWNABLE_ON) && Camel.isBrightEnoughToSpawn(levelAccessor, blockPos);
    }

    protected Brain.Provider<Camel> brainProvider() {
        return CamelAi.brainProvider();
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return CamelAi.makeBrain(this.brainProvider().makeBrain(dynamic));
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return pose == Pose.SITTING ? SITTING_DIMENSIONS.scale(this.getAgeScale()) : super.getDefaultDimensions(pose);
    }

    @Override
    protected void customServerAiStep(ServerLevel serverLevel) {
        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("camelBrain");
        Brain<?> brain = this.getBrain();
        brain.tick(serverLevel, this);
        profilerFiller.pop();
        profilerFiller.push("camelActivityUpdate");
        CamelAi.updateActivity(this);
        profilerFiller.pop();
        super.customServerAiStep(serverLevel);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isDashing() && this.dashCooldown < 50 && (this.onGround() || this.isInLiquid() || this.isPassenger())) {
            this.setDashing(false);
        }
        if (this.dashCooldown > 0) {
            --this.dashCooldown;
            if (this.dashCooldown == 0) {
                this.level().playSound(null, this.blockPosition(), SoundEvents.CAMEL_DASH_READY, SoundSource.NEUTRAL, 1.0f, 1.0f);
            }
        }
        if (this.level().isClientSide()) {
            this.setupAnimationStates();
        }
        if (this.refuseToMove()) {
            this.clampHeadRotationToBody();
        }
        if (this.isCamelSitting() && this.isInWater()) {
            this.standUpInstantly();
        }
    }

    private void setupAnimationStates() {
        if (this.idleAnimationTimeout <= 0) {
            this.idleAnimationTimeout = this.random.nextInt(40) + 80;
            this.idleAnimationState.start(this.tickCount);
        } else {
            --this.idleAnimationTimeout;
        }
        if (this.isCamelVisuallySitting()) {
            this.sitUpAnimationState.stop();
            this.dashAnimationState.stop();
            if (this.isVisuallySittingDown()) {
                this.sitAnimationState.startIfStopped(this.tickCount);
                this.sitPoseAnimationState.stop();
            } else {
                this.sitAnimationState.stop();
                this.sitPoseAnimationState.startIfStopped(this.tickCount);
            }
        } else {
            this.sitAnimationState.stop();
            this.sitPoseAnimationState.stop();
            this.dashAnimationState.animateWhen(this.isDashing(), this.tickCount);
            this.sitUpAnimationState.animateWhen(this.isInPoseTransition() && this.getPoseTime() >= 0L, this.tickCount);
        }
    }

    @Override
    protected void updateWalkAnimation(float f) {
        float f2 = this.getPose() == Pose.STANDING && !this.dashAnimationState.isStarted() ? Math.min(f * 6.0f, 1.0f) : 0.0f;
        this.walkAnimation.update(f2, 0.2f, this.isBaby() ? 3.0f : 1.0f);
    }

    @Override
    public void travel(Vec3 vec3) {
        if (this.refuseToMove() && this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.0, 1.0, 0.0));
            vec3 = vec3.multiply(0.0, 1.0, 0.0);
        }
        super.travel(vec3);
    }

    @Override
    protected void tickRidden(Player player, Vec3 vec3) {
        super.tickRidden(player, vec3);
        if (player.zza > 0.0f && this.isCamelSitting() && !this.isInPoseTransition()) {
            this.standUp();
        }
    }

    public boolean refuseToMove() {
        return this.isCamelSitting() || this.isInPoseTransition();
    }

    @Override
    protected float getRiddenSpeed(Player player) {
        float f = player.isSprinting() && this.getJumpCooldown() == 0 ? 0.1f : 0.0f;
        return (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) + f;
    }

    @Override
    protected Vec2 getRiddenRotation(LivingEntity livingEntity) {
        if (this.refuseToMove()) {
            return new Vec2(this.getXRot(), this.getYRot());
        }
        return super.getRiddenRotation(livingEntity);
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 vec3) {
        if (this.refuseToMove()) {
            return Vec3.ZERO;
        }
        return super.getRiddenInput(player, vec3);
    }

    @Override
    public boolean canJump() {
        return !this.refuseToMove() && super.canJump();
    }

    @Override
    public void onPlayerJump(int n) {
        if (!this.isSaddled() || this.dashCooldown > 0 || !this.onGround()) {
            return;
        }
        super.onPlayerJump(n);
    }

    @Override
    public boolean canSprint() {
        return true;
    }

    @Override
    protected void executeRidersJump(float f, Vec3 vec3) {
        double d = this.getJumpPower();
        this.addDeltaMovement(this.getLookAngle().multiply(1.0, 0.0, 1.0).normalize().scale((double)(22.2222f * f) * this.getAttributeValue(Attributes.MOVEMENT_SPEED) * (double)this.getBlockSpeedFactor()).add(0.0, (double)(1.4285f * f) * d, 0.0));
        this.dashCooldown = 55;
        this.setDashing(true);
        this.hasImpulse = true;
    }

    public boolean isDashing() {
        return this.entityData.get(DASH);
    }

    public void setDashing(boolean bl) {
        this.entityData.set(DASH, bl);
    }

    @Override
    public void handleStartJump(int n) {
        this.makeSound(SoundEvents.CAMEL_DASH);
        this.gameEvent(GameEvent.ENTITY_ACTION);
        this.setDashing(true);
    }

    @Override
    public void handleStopJump() {
    }

    @Override
    public int getJumpCooldown() {
        return this.dashCooldown;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.CAMEL_AMBIENT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CAMEL_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.CAMEL_HURT;
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        if (blockState.is(BlockTags.CAMEL_SAND_STEP_SOUND_BLOCKS)) {
            this.playSound(SoundEvents.CAMEL_STEP_SAND, 1.0f, 1.0f);
        } else {
            this.playSound(SoundEvents.CAMEL_STEP, 1.0f, 1.0f);
        }
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(ItemTags.CAMEL_FOOD);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (player.isSecondaryUseActive() && !this.isBaby()) {
            this.openCustomInventoryScreen(player);
            return InteractionResult.SUCCESS;
        }
        InteractionResult interactionResult = itemStack.interactLivingEntity(player, this, interactionHand);
        if (interactionResult.consumesAction()) {
            return interactionResult;
        }
        if (this.isFood(itemStack)) {
            return this.fedFood(player, itemStack);
        }
        if (this.getPassengers().size() < 2 && !this.isBaby()) {
            this.doPlayerRide(player);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onElasticLeashPull() {
        super.onElasticLeashPull();
        if (this.isCamelSitting() && !this.isInPoseTransition() && this.canCamelChangePose()) {
            this.standUp();
        }
    }

    @Override
    public Vec3[] getQuadLeashOffsets() {
        return Leashable.createQuadLeashOffsets(this, 0.02, 0.48, 0.25, 0.82);
    }

    public boolean canCamelChangePose() {
        return this.wouldNotSuffocateAtTargetPose(this.isCamelSitting() ? Pose.STANDING : Pose.SITTING);
    }

    @Override
    protected boolean handleEating(Player player, ItemStack itemStack) {
        boolean bl;
        boolean bl2;
        boolean bl3;
        if (!this.isFood(itemStack)) {
            return false;
        }
        boolean bl4 = bl3 = this.getHealth() < this.getMaxHealth();
        if (bl3) {
            this.heal(2.0f);
        }
        boolean bl5 = bl2 = this.isTamed() && this.getAge() == 0 && this.canFallInLove();
        if (bl2) {
            this.setInLove(player);
        }
        if (bl = this.isBaby()) {
            this.level().addParticle(ParticleTypes.HAPPY_VILLAGER, this.getRandomX(1.0), this.getRandomY() + 0.5, this.getRandomZ(1.0), 0.0, 0.0, 0.0);
            if (!this.level().isClientSide) {
                this.ageUp(10);
            }
        }
        if (bl3 || bl2 || bl) {
            SoundEvent soundEvent;
            if (!this.isSilent() && (soundEvent = this.getEatingSound()) != null) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), soundEvent, this.getSoundSource(), 1.0f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
            }
            this.gameEvent(GameEvent.EAT);
            return true;
        }
        return false;
    }

    @Override
    protected boolean canPerformRearing() {
        return false;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public boolean canMate(Animal animal) {
        if (animal == this) return false;
        if (!(animal instanceof Camel)) return false;
        Camel camel = (Camel)animal;
        if (!this.canParent()) return false;
        if (!camel.canParent()) return false;
        return true;
    }

    @Override
    @Nullable
    public Camel getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return EntityType.CAMEL.create(serverLevel, EntitySpawnReason.BREEDING);
    }

    @Override
    @Nullable
    protected SoundEvent getEatingSound() {
        return SoundEvents.CAMEL_EAT;
    }

    @Override
    protected void actuallyHurt(ServerLevel serverLevel, DamageSource damageSource, float f) {
        this.standUpInstantly();
        super.actuallyHurt(serverLevel, damageSource, f);
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity entity, EntityDimensions entityDimensions, float f) {
        int n = Math.max(this.getPassengers().indexOf(entity), 0);
        boolean bl = n == 0;
        float f2 = 0.5f;
        float f3 = (float)(this.isRemoved() ? (double)0.01f : this.getBodyAnchorAnimationYOffset(bl, 0.0f, entityDimensions, f));
        if (this.getPassengers().size() > 1) {
            if (!bl) {
                f2 = -0.7f;
            }
            if (entity instanceof Animal) {
                f2 += 0.2f;
            }
        }
        return new Vec3(0.0, f3, f2 * f).yRot(-this.getYRot() * ((float)Math.PI / 180));
    }

    @Override
    public float getAgeScale() {
        return this.isBaby() ? 0.45f : 1.0f;
    }

    private double getBodyAnchorAnimationYOffset(boolean bl, float f, EntityDimensions entityDimensions, float f2) {
        double d = entityDimensions.height() - 0.375f * f2;
        float f3 = f2 * 1.43f;
        float f4 = f3 - f2 * 0.2f;
        float f5 = f3 - f4;
        boolean bl2 = this.isInPoseTransition();
        boolean bl3 = this.isCamelSitting();
        if (bl2) {
            float f6;
            int n;
            int n2;
            int n3 = n2 = bl3 ? 40 : 52;
            if (bl3) {
                n = 28;
                f6 = bl ? 0.5f : 0.1f;
            } else {
                n = bl ? 24 : 32;
                f6 = bl ? 0.6f : 0.35f;
            }
            float f7 = Mth.clamp((float)this.getPoseTime() + f, 0.0f, (float)n2);
            boolean bl4 = f7 < (float)n;
            float f8 = bl4 ? f7 / (float)n : (f7 - (float)n) / (float)(n2 - n);
            float f9 = f3 - f6 * f4;
            d += bl3 ? (double)Mth.lerp(f8, bl4 ? f3 : f9, bl4 ? f9 : f5) : (double)Mth.lerp(f8, bl4 ? f5 - f3 : f5 - f9, bl4 ? f5 - f9 : 0.0f);
        }
        if (bl3 && !bl2) {
            d += (double)f5;
        }
        return d;
    }

    @Override
    public Vec3 getLeashOffset(float f) {
        EntityDimensions entityDimensions = this.getDimensions(this.getPose());
        float f2 = this.getAgeScale();
        return new Vec3(0.0, this.getBodyAnchorAnimationYOffset(true, f, entityDimensions, f2) - (double)(0.2f * f2), entityDimensions.width() * 0.56f);
    }

    @Override
    public int getMaxHeadYRot() {
        return 30;
    }

    @Override
    protected boolean canAddPassenger(Entity entity) {
        return this.getPassengers().size() <= 2;
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }

    public boolean isCamelSitting() {
        return this.entityData.get(LAST_POSE_CHANGE_TICK) < 0L;
    }

    public boolean isCamelVisuallySitting() {
        return this.getPoseTime() < 0L != this.isCamelSitting();
    }

    public boolean isInPoseTransition() {
        long l = this.getPoseTime();
        return l < (long)(this.isCamelSitting() ? 40 : 52);
    }

    private boolean isVisuallySittingDown() {
        return this.isCamelSitting() && this.getPoseTime() < 40L && this.getPoseTime() >= 0L;
    }

    public void sitDown() {
        if (this.isCamelSitting()) {
            return;
        }
        this.makeSound(SoundEvents.CAMEL_SIT);
        this.setPose(Pose.SITTING);
        this.gameEvent(GameEvent.ENTITY_ACTION);
        this.resetLastPoseChangeTick(-this.level().getGameTime());
    }

    public void standUp() {
        if (!this.isCamelSitting()) {
            return;
        }
        this.makeSound(SoundEvents.CAMEL_STAND);
        this.setPose(Pose.STANDING);
        this.gameEvent(GameEvent.ENTITY_ACTION);
        this.resetLastPoseChangeTick(this.level().getGameTime());
    }

    public void standUpInstantly() {
        this.setPose(Pose.STANDING);
        this.gameEvent(GameEvent.ENTITY_ACTION);
        this.resetLastPoseChangeTickToFullStand(this.level().getGameTime());
    }

    @VisibleForTesting
    public void resetLastPoseChangeTick(long l) {
        this.entityData.set(LAST_POSE_CHANGE_TICK, l);
    }

    private void resetLastPoseChangeTickToFullStand(long l) {
        this.resetLastPoseChangeTick(Math.max(0L, l - 52L - 1L));
    }

    public long getPoseTime() {
        return this.level().getGameTime() - Math.abs(this.entityData.get(LAST_POSE_CHANGE_TICK));
    }

    @Override
    protected Holder<SoundEvent> getEquipSound(EquipmentSlot equipmentSlot, ItemStack itemStack, Equippable equippable) {
        if (equipmentSlot == EquipmentSlot.SADDLE) {
            return SoundEvents.CAMEL_SADDLE;
        }
        return super.getEquipSound(equipmentSlot, itemStack, equippable);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (!this.firstTick && DASH.equals(entityDataAccessor)) {
            this.dashCooldown = this.dashCooldown == 0 ? 55 : this.dashCooldown;
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    @Override
    public boolean isTamed() {
        return true;
    }

    @Override
    public void openCustomInventoryScreen(Player player) {
        if (!this.level().isClientSide) {
            player.openHorseInventory(this, this.inventory);
        }
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        return new CamelBodyRotationControl(this);
    }

    @Override
    @Nullable
    public /* synthetic */ AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return this.getBreedOffspring(serverLevel, ageableMob);
    }

    class CamelMoveControl
    extends MoveControl {
        public CamelMoveControl() {
            super(Camel.this);
        }

        @Override
        public void tick() {
            if (this.operation == MoveControl.Operation.MOVE_TO && !Camel.this.isLeashed() && Camel.this.isCamelSitting() && !Camel.this.isInPoseTransition() && Camel.this.canCamelChangePose()) {
                Camel.this.standUp();
            }
            super.tick();
        }
    }

    class CamelLookControl
    extends LookControl {
        CamelLookControl() {
            super(Camel.this);
        }

        @Override
        public void tick() {
            if (!Camel.this.hasControllingPassenger()) {
                super.tick();
            }
        }
    }

    class CamelBodyRotationControl
    extends BodyRotationControl {
        public CamelBodyRotationControl(Camel camel2) {
            super(camel2);
        }

        @Override
        public void clientTick() {
            if (!Camel.this.refuseToMove()) {
                super.clientTick();
            }
        }
    }
}

