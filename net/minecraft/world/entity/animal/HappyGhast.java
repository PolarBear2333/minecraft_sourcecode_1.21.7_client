/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Dynamic
 *  org.jetbrains.annotations.Nullable
 */
package net.minecraft.world.entity.animal;

import com.mojang.serialization.Dynamic;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Leashable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.HappyGhastAi;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class HappyGhast
extends Animal {
    public static final float BABY_SCALE = 0.2375f;
    public static final int WANDER_GROUND_DISTANCE = 16;
    public static final int SMALL_RESTRICTION_RADIUS = 32;
    public static final int LARGE_RESTRICTION_RADIUS = 64;
    public static final int RESTRICTION_RADIUS_BUFFER = 16;
    public static final int FAST_HEALING_TICKS = 20;
    public static final int SLOW_HEALING_TICKS = 600;
    public static final int MAX_PASSANGERS = 4;
    private static final int STILL_TIMEOUT_ON_LOAD_GRACE_PERIOD = 60;
    private static final int MAX_STILL_TIMEOUT = 10;
    public static final float SPEED_MULTIPLIER_WHEN_PANICKING = 2.0f;
    public static final Predicate<ItemStack> IS_FOOD = itemStack -> itemStack.is(ItemTags.HAPPY_GHAST_FOOD);
    private int leashHolderTime = 0;
    private int serverStillTimeout;
    private static final EntityDataAccessor<Boolean> IS_LEASH_HOLDER = SynchedEntityData.defineId(HappyGhast.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> STAYS_STILL = SynchedEntityData.defineId(HappyGhast.class, EntityDataSerializers.BOOLEAN);
    private static final float MAX_SCALE = 1.0f;

    public HappyGhast(EntityType<? extends HappyGhast> entityType, Level level) {
        super((EntityType<? extends Animal>)entityType, level);
        this.moveControl = new Ghast.GhastMoveControl(this, true, this::isOnStillTimeout);
        this.lookControl = new HappyGhastLookControl();
    }

    private void setServerStillTimeout(int n) {
        Level level;
        if (this.serverStillTimeout <= 0 && n > 0 && (level = this.level()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());
            serverLevel.getChunkSource().chunkMap.broadcast(this, ClientboundEntityPositionSyncPacket.of(this));
        }
        this.serverStillTimeout = n;
        this.syncStayStillFlag();
    }

    private PathNavigation createBabyNavigation(Level level) {
        return new BabyFlyingPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(3, new HappyGhastFloatGoal());
        this.goalSelector.addGoal(4, new TemptGoal.ForNonPathfinders((Mob)this, 1.0, itemStack -> this.isWearingBodyArmor() || this.isBaby() ? IS_FOOD.test((ItemStack)itemStack) : itemStack.is(ItemTags.HAPPY_GHAST_TEMPT_ITEMS), false, 7.0));
        this.goalSelector.addGoal(5, new Ghast.RandomFloatAroundGoal(this, 16));
    }

    private void adultGhastSetup() {
        this.moveControl = new Ghast.GhastMoveControl(this, true, this::isOnStillTimeout);
        this.lookControl = new HappyGhastLookControl();
        this.navigation = this.createNavigation(this.level());
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.removeAllGoals(goal -> true);
            this.registerGoals();
            this.brain.stopAll(serverLevel, this);
            this.brain.clearMemories();
        }
    }

    private void babyGhastSetup() {
        this.moveControl = new FlyingMoveControl(this, 180, true);
        this.lookControl = new LookControl(this);
        this.navigation = this.createBabyNavigation(this.level());
        this.setServerStillTimeout(0);
        this.removeAllGoals(goal -> true);
    }

    @Override
    protected void ageBoundaryReached() {
        if (this.isBaby()) {
            this.babyGhastSetup();
        } else {
            this.adultGhastSetup();
        }
        super.ageBoundaryReached();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.TEMPT_RANGE, 16.0).add(Attributes.FLYING_SPEED, 0.05).add(Attributes.MOVEMENT_SPEED, 0.05).add(Attributes.FOLLOW_RANGE, 16.0).add(Attributes.CAMERA_DISTANCE, 8.0);
    }

    @Override
    protected float sanitizeScale(float f) {
        return Math.min(f, 1.0f);
    }

    @Override
    protected void checkFallDamage(double d, boolean bl, BlockState blockState, BlockPos blockPos) {
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public void travel(Vec3 vec3) {
        float f = (float)this.getAttributeValue(Attributes.FLYING_SPEED) * 5.0f / 3.0f;
        this.travelFlying(vec3, f, f, f);
    }

    @Override
    public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
        if (!levelReader.isEmptyBlock(blockPos)) {
            return 0.0f;
        }
        if (levelReader.isEmptyBlock(blockPos.below()) && !levelReader.isEmptyBlock(blockPos.below(2))) {
            return 10.0f;
        }
        return 5.0f;
    }

    @Override
    public boolean canBreatheUnderwater() {
        if (this.isBaby()) {
            return true;
        }
        return super.canBreatheUnderwater();
    }

    @Override
    protected boolean shouldStayCloseToLeashHolder() {
        return false;
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
    }

    @Override
    public float getVoicePitch() {
        return 1.0f;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.NEUTRAL;
    }

    @Override
    public int getAmbientSoundInterval() {
        int n = super.getAmbientSoundInterval();
        if (this.isVehicle()) {
            return n * 6;
        }
        return n;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isBaby() ? SoundEvents.GHASTLING_AMBIENT : SoundEvents.HAPPY_GHAST_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return this.isBaby() ? SoundEvents.GHASTLING_HURT : SoundEvents.HAPPY_GHAST_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.isBaby() ? SoundEvents.GHASTLING_DEATH : SoundEvents.HAPPY_GHAST_DEATH;
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return 1;
    }

    @Override
    @Nullable
    public AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return EntityType.HAPPY_GHAST.create(serverLevel, EntitySpawnReason.BREEDING);
    }

    @Override
    public boolean canFallInLove() {
        return false;
    }

    @Override
    public float getAgeScale() {
        return this.isBaby() ? 0.2375f : 1.0f;
    }

    @Override
    public boolean isFood(ItemStack itemStack) {
        return IS_FOOD.test(itemStack);
    }

    @Override
    public boolean canUseSlot(EquipmentSlot equipmentSlot) {
        if (equipmentSlot == EquipmentSlot.BODY) {
            return this.isAlive() && !this.isBaby();
        }
        return super.canUseSlot(equipmentSlot);
    }

    @Override
    protected boolean canDispenserEquipIntoSlot(EquipmentSlot equipmentSlot) {
        return equipmentSlot == EquipmentSlot.BODY;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        InteractionResult interactionResult;
        if (this.isBaby()) {
            return super.mobInteract(player, interactionHand);
        }
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (!itemStack.isEmpty() && (interactionResult = itemStack.interactLivingEntity(player, this, interactionHand)).consumesAction()) {
            return interactionResult;
        }
        if (this.isWearingBodyArmor() && !player.isSecondaryUseActive()) {
            this.doPlayerRide(player);
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, interactionHand);
    }

    private void doPlayerRide(Player player) {
        if (!this.level().isClientSide) {
            player.startRiding(this);
        }
    }

    @Override
    protected void addPassenger(Entity entity) {
        if (!this.isVehicle()) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.HARNESS_GOGGLES_DOWN, this.getSoundSource(), 1.0f, 1.0f);
        }
        super.addPassenger(entity);
        if (!this.level().isClientSide) {
            if (!this.scanPlayerAboveGhast()) {
                this.setServerStillTimeout(0);
            } else if (this.serverStillTimeout > 10) {
                this.setServerStillTimeout(10);
            }
        }
    }

    @Override
    protected void removePassenger(Entity entity) {
        super.removePassenger(entity);
        if (!this.level().isClientSide) {
            this.setServerStillTimeout(10);
        }
        if (!this.isVehicle()) {
            this.clearHome();
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.HARNESS_GOGGLES_UP, this.getSoundSource(), 1.0f, 1.0f);
        }
    }

    @Override
    protected boolean canAddPassenger(Entity entity) {
        return this.getPassengers().size() < 4;
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        Entity entity = this.getFirstPassenger();
        if (this.isWearingBodyArmor() && !this.isOnStillTimeout() && entity instanceof Player) {
            Player player = (Player)entity;
            return player;
        }
        return super.getControllingPassenger();
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 vec3) {
        float f = player.xxa;
        float f2 = 0.0f;
        float f3 = 0.0f;
        if (player.zza != 0.0f) {
            float f4 = Mth.cos(player.getXRot() * ((float)Math.PI / 180));
            float f5 = -Mth.sin(player.getXRot() * ((float)Math.PI / 180));
            if (player.zza < 0.0f) {
                f4 *= -0.5f;
                f5 *= -0.5f;
            }
            f3 = f5;
            f2 = f4;
        }
        if (player.isJumping()) {
            f3 += 0.5f;
        }
        return new Vec3(f, f3, f2).scale((double)3.9f * this.getAttributeValue(Attributes.FLYING_SPEED));
    }

    protected Vec2 getRiddenRotation(LivingEntity livingEntity) {
        return new Vec2(livingEntity.getXRot() * 0.5f, livingEntity.getYRot());
    }

    @Override
    protected void tickRidden(Player player, Vec3 vec3) {
        super.tickRidden(player, vec3);
        Vec2 vec2 = this.getRiddenRotation(player);
        float f = this.getYRot();
        float f2 = Mth.wrapDegrees(vec2.y - f);
        float f3 = 0.08f;
        this.setRot(f += f2 * 0.08f, vec2.x);
        this.yBodyRot = this.yHeadRot = f;
        this.yRotO = this.yHeadRot;
    }

    protected Brain.Provider<HappyGhast> brainProvider() {
        return HappyGhastAi.brainProvider();
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return HappyGhastAi.makeBrain(this.brainProvider().makeBrain(dynamic));
    }

    @Override
    protected void customServerAiStep(ServerLevel serverLevel) {
        if (this.isBaby()) {
            ProfilerFiller profilerFiller = Profiler.get();
            profilerFiller.push("happyGhastBrain");
            this.brain.tick(serverLevel, this);
            profilerFiller.pop();
            profilerFiller.push("happyGhastActivityUpdate");
            HappyGhastAi.updateActivity(this);
            profilerFiller.pop();
        }
        this.checkRestriction();
        super.customServerAiStep(serverLevel);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }
        if (this.leashHolderTime > 0) {
            --this.leashHolderTime;
        }
        this.setLeashHolder(this.leashHolderTime > 0);
        if (this.serverStillTimeout > 0) {
            if (this.tickCount > 60) {
                --this.serverStillTimeout;
            }
            this.setServerStillTimeout(this.serverStillTimeout);
        }
        if (this.scanPlayerAboveGhast()) {
            this.setServerStillTimeout(10);
        }
    }

    @Override
    public void aiStep() {
        if (!this.level().isClientSide) {
            this.setRequiresPrecisePosition(this.isOnStillTimeout());
        }
        super.aiStep();
        this.continuousHeal();
    }

    private int getHappyGhastRestrictionRadius() {
        if (!this.isBaby() && this.getItemBySlot(EquipmentSlot.BODY).isEmpty()) {
            return 64;
        }
        return 32;
    }

    private void checkRestriction() {
        if (this.isLeashed() || this.isVehicle()) {
            return;
        }
        int n = this.getHappyGhastRestrictionRadius();
        if (this.hasHome() && this.getHomePosition().closerThan(this.blockPosition(), n + 16) && n == this.getHomeRadius()) {
            return;
        }
        this.setHomeTo(this.blockPosition(), n);
    }

    private void continuousHeal() {
        ServerLevel serverLevel;
        block5: {
            block4: {
                Level level = this.level();
                if (!(level instanceof ServerLevel)) break block4;
                serverLevel = (ServerLevel)level;
                if (this.isAlive() && this.deathTime == 0 && this.getMaxHealth() != this.getHealth()) break block5;
            }
            return;
        }
        boolean bl = serverLevel.dimensionType().natural() && (this.isInClouds() || serverLevel.precipitationAt(this.blockPosition()) != Biome.Precipitation.NONE);
        if (this.tickCount % (bl ? 20 : 600) == 0) {
            this.heal(1.0f);
        }
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(IS_LEASH_HOLDER, false);
        builder.define(STAYS_STILL, false);
    }

    private void setLeashHolder(boolean bl) {
        this.entityData.set(IS_LEASH_HOLDER, bl);
    }

    public boolean isLeashHolder() {
        return this.entityData.get(IS_LEASH_HOLDER);
    }

    private void syncStayStillFlag() {
        this.entityData.set(STAYS_STILL, this.serverStillTimeout > 0);
    }

    public boolean staysStill() {
        return this.entityData.get(STAYS_STILL);
    }

    @Override
    public boolean supportQuadLeashAsHolder() {
        return true;
    }

    @Override
    public Vec3[] getQuadLeashHolderOffsets() {
        return Leashable.createQuadLeashOffsets(this, -0.03125, 0.4375, 0.46875, 0.03125);
    }

    @Override
    public Vec3 getLeashOffset() {
        return Vec3.ZERO;
    }

    @Override
    public double leashElasticDistance() {
        return 10.0;
    }

    @Override
    public double leashSnapDistance() {
        return 16.0;
    }

    @Override
    public void onElasticLeashPull() {
        super.onElasticLeashPull();
        this.getMoveControl().setWait();
    }

    @Override
    public void notifyLeashHolder(Leashable leashable) {
        if (leashable.supportQuadLeash()) {
            this.leashHolderTime = 5;
        }
    }

    @Override
    public void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putInt("still_timeout", this.serverStillTimeout);
    }

    @Override
    public void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.setServerStillTimeout(valueInput.getIntOr("still_timeout", 0));
    }

    public boolean isOnStillTimeout() {
        return this.staysStill() || this.serverStillTimeout > 0;
    }

    private boolean scanPlayerAboveGhast() {
        AABB aABB = this.getBoundingBox();
        AABB aABB2 = new AABB(aABB.minX - 1.0, aABB.maxY - (double)1.0E-5f, aABB.minZ - 1.0, aABB.maxX + 1.0, aABB.maxY + aABB.getYsize() / 2.0, aABB.maxZ + 1.0);
        for (Player player : this.level().players()) {
            Entity entity;
            if (player.isSpectator() || (entity = player.getRootVehicle()) instanceof HappyGhast || !aABB2.contains(entity.position())) continue;
            return true;
        }
        return false;
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        return new HappyGhastBodyRotationControl();
    }

    @Override
    public boolean canBeCollidedWith(@Nullable Entity entity) {
        if (this.isBaby() || !this.isAlive()) {
            return false;
        }
        if (this.level().isClientSide() && entity instanceof Player && entity.position().y >= this.getBoundingBox().maxY) {
            return true;
        }
        if (this.isVehicle() && entity instanceof HappyGhast) {
            return true;
        }
        return this.isOnStillTimeout();
    }

    @Override
    public boolean isFlyingVehicle() {
        return !this.isBaby();
    }

    class HappyGhastLookControl
    extends LookControl {
        HappyGhastLookControl() {
            super(HappyGhast.this);
        }

        @Override
        public void tick() {
            if (HappyGhast.this.isOnStillTimeout()) {
                float f = HappyGhastLookControl.wrapDegrees90(HappyGhast.this.getYRot());
                HappyGhast.this.setYRot(HappyGhast.this.getYRot() - f);
                HappyGhast.this.setYHeadRot(HappyGhast.this.getYRot());
                return;
            }
            if (this.lookAtCooldown > 0) {
                --this.lookAtCooldown;
                double d = this.wantedX - HappyGhast.this.getX();
                double d2 = this.wantedZ - HappyGhast.this.getZ();
                HappyGhast.this.setYRot(-((float)Mth.atan2(d, d2)) * 57.295776f);
                HappyGhast.this.yHeadRot = HappyGhast.this.yBodyRot = HappyGhast.this.getYRot();
                return;
            }
            Ghast.faceMovementDirection(this.mob);
        }

        public static float wrapDegrees90(float f) {
            float f2 = f % 90.0f;
            if (f2 >= 45.0f) {
                f2 -= 90.0f;
            }
            if (f2 < -45.0f) {
                f2 += 90.0f;
            }
            return f2;
        }
    }

    static class BabyFlyingPathNavigation
    extends FlyingPathNavigation {
        public BabyFlyingPathNavigation(HappyGhast happyGhast, Level level) {
            super(happyGhast, level);
            this.setCanOpenDoors(false);
            this.setCanFloat(true);
            this.setRequiredPathLength(48.0f);
        }

        @Override
        protected boolean canMoveDirectly(Vec3 vec3, Vec3 vec32) {
            return BabyFlyingPathNavigation.isClearForMovementBetween(this.mob, vec3, vec32, false);
        }
    }

    class HappyGhastFloatGoal
    extends FloatGoal {
        public HappyGhastFloatGoal() {
            super(HappyGhast.this);
        }

        @Override
        public boolean canUse() {
            return !HappyGhast.this.isOnStillTimeout() && super.canUse();
        }
    }

    class HappyGhastBodyRotationControl
    extends BodyRotationControl {
        public HappyGhastBodyRotationControl() {
            super(HappyGhast.this);
        }

        @Override
        public void clientTick() {
            if (HappyGhast.this.isVehicle()) {
                HappyGhast.this.yBodyRot = HappyGhast.this.yHeadRot = HappyGhast.this.getYRot();
            }
            super.clientTick();
        }
    }
}

