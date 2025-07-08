/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.animal;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.BreathAirGoal;
import net.minecraft.world.entity.ai.goal.DolphinJumpGoal;
import net.minecraft.world.entity.ai.goal.FollowBoatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.TryFindWaterGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.AgeableWaterCreature;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class Dolphin
extends AgeableWaterCreature {
    private static final EntityDataAccessor<Boolean> GOT_FISH = SynchedEntityData.defineId(Dolphin.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> MOISTNESS_LEVEL = SynchedEntityData.defineId(Dolphin.class, EntityDataSerializers.INT);
    static final TargetingConditions SWIM_WITH_PLAYER_TARGETING = TargetingConditions.forNonCombat().range(10.0).ignoreLineOfSight();
    public static final int TOTAL_AIR_SUPPLY = 4800;
    private static final int TOTAL_MOISTNESS_LEVEL = 2400;
    public static final Predicate<ItemEntity> ALLOWED_ITEMS = itemEntity -> !itemEntity.hasPickUpDelay() && itemEntity.isAlive() && itemEntity.isInWater();
    public static final float BABY_SCALE = 0.65f;
    private static final boolean DEFAULT_GOT_FISH = false;
    @Nullable
    BlockPos treasurePos;

    public Dolphin(EntityType<? extends Dolphin> entityType, Level level) {
        super((EntityType<? extends AgeableWaterCreature>)entityType, level);
        this.moveControl = new SmoothSwimmingMoveControl(this, 85, 10, 0.02f, 0.1f, true);
        this.lookControl = new SmoothSwimmingLookControl(this, 10);
        this.setCanPickUpLoot(true);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, EntitySpawnReason entitySpawnReason, @Nullable SpawnGroupData spawnGroupData) {
        this.setAirSupply(this.getMaxAirSupply());
        this.setXRot(0.0f);
        SpawnGroupData spawnGroupData2 = Objects.requireNonNullElseGet(spawnGroupData, () -> new AgeableMob.AgeableMobGroupData(0.1f));
        return super.finalizeSpawn(serverLevelAccessor, difficultyInstance, entitySpawnReason, spawnGroupData2);
    }

    @Override
    @Nullable
    public Dolphin getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return EntityType.DOLPHIN.create(serverLevel, EntitySpawnReason.BREEDING);
    }

    @Override
    public float getAgeScale() {
        return this.isBaby() ? 0.65f : 1.0f;
    }

    @Override
    protected void handleAirSupply(int n) {
    }

    public boolean gotFish() {
        return this.entityData.get(GOT_FISH);
    }

    public void setGotFish(boolean bl) {
        this.entityData.set(GOT_FISH, bl);
    }

    public int getMoistnessLevel() {
        return this.entityData.get(MOISTNESS_LEVEL);
    }

    public void setMoisntessLevel(int n) {
        this.entityData.set(MOISTNESS_LEVEL, n);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(GOT_FISH, false);
        builder.define(MOISTNESS_LEVEL, 2400);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putBoolean("GotFish", this.gotFish());
        valueOutput.putInt("Moistness", this.getMoistnessLevel());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.setGotFish(valueInput.getBooleanOr("GotFish", false));
        this.setMoisntessLevel(valueInput.getIntOr("Moistness", 2400));
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new BreathAirGoal(this));
        this.goalSelector.addGoal(0, new TryFindWaterGoal(this));
        this.goalSelector.addGoal(1, new DolphinSwimToTreasureGoal(this));
        this.goalSelector.addGoal(2, new DolphinSwimWithPlayerGoal(this, 4.0));
        this.goalSelector.addGoal(4, new RandomSwimmingGoal(this, 1.0, 10));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6.0f));
        this.goalSelector.addGoal(5, new DolphinJumpGoal(this, 10));
        this.goalSelector.addGoal(6, new MeleeAttackGoal(this, 1.2f, true));
        this.goalSelector.addGoal(8, new PlayWithItemsGoal());
        this.goalSelector.addGoal(8, new FollowBoatGoal(this));
        this.goalSelector.addGoal(9, new AvoidEntityGoal<Guardian>(this, Guardian.class, 8.0f, 1.0, 1.0));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this, Guardian.class).setAlertOthers(new Class[0]));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 10.0).add(Attributes.MOVEMENT_SPEED, 1.2f).add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    @Override
    public void playAttackSound() {
        this.playSound(SoundEvents.DOLPHIN_ATTACK, 1.0f, 1.0f);
    }

    @Override
    public boolean canAttack(LivingEntity livingEntity) {
        return !this.isBaby() && super.canAttack(livingEntity);
    }

    @Override
    public int getMaxAirSupply() {
        return 4800;
    }

    @Override
    protected int increaseAirSupply(int n) {
        return this.getMaxAirSupply();
    }

    @Override
    public int getMaxHeadXRot() {
        return 1;
    }

    @Override
    public int getMaxHeadYRot() {
        return 1;
    }

    @Override
    protected boolean canRide(Entity entity) {
        return true;
    }

    @Override
    protected boolean canDispenserEquipIntoSlot(EquipmentSlot equipmentSlot) {
        return equipmentSlot == EquipmentSlot.MAINHAND && this.canPickUpLoot();
    }

    @Override
    protected void pickUpItem(ServerLevel serverLevel, ItemEntity itemEntity) {
        ItemStack itemStack;
        if (this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty() && this.canHoldItem(itemStack = itemEntity.getItem())) {
            this.onItemPickup(itemEntity);
            this.setItemSlot(EquipmentSlot.MAINHAND, itemStack);
            this.setGuaranteedDrop(EquipmentSlot.MAINHAND);
            this.take(itemEntity, itemStack.getCount());
            itemEntity.discard();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isNoAi()) {
            this.setAirSupply(this.getMaxAirSupply());
            return;
        }
        if (this.isInWaterOrRain()) {
            this.setMoisntessLevel(2400);
        } else {
            this.setMoisntessLevel(this.getMoistnessLevel() - 1);
            if (this.getMoistnessLevel() <= 0) {
                this.hurt(this.damageSources().dryOut(), 1.0f);
            }
            if (this.onGround()) {
                this.setDeltaMovement(this.getDeltaMovement().add((this.random.nextFloat() * 2.0f - 1.0f) * 0.2f, 0.5, (this.random.nextFloat() * 2.0f - 1.0f) * 0.2f));
                this.setYRot(this.random.nextFloat() * 360.0f);
                this.setOnGround(false);
                this.hasImpulse = true;
            }
        }
        if (this.level().isClientSide && this.isInWater() && this.getDeltaMovement().lengthSqr() > 0.03) {
            Vec3 vec3 = this.getViewVector(0.0f);
            float f = Mth.cos(this.getYRot() * ((float)Math.PI / 180)) * 0.3f;
            float f2 = Mth.sin(this.getYRot() * ((float)Math.PI / 180)) * 0.3f;
            float f3 = 1.2f - this.random.nextFloat() * 0.7f;
            for (int i = 0; i < 2; ++i) {
                this.level().addParticle(ParticleTypes.DOLPHIN, this.getX() - vec3.x * (double)f3 + (double)f, this.getY() - vec3.y, this.getZ() - vec3.z * (double)f3 + (double)f2, 0.0, 0.0, 0.0);
                this.level().addParticle(ParticleTypes.DOLPHIN, this.getX() - vec3.x * (double)f3 - (double)f, this.getY() - vec3.y, this.getZ() - vec3.z * (double)f3 - (double)f2, 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    public void handleEntityEvent(byte by) {
        if (by == 38) {
            this.addParticlesAroundSelf(ParticleTypes.HAPPY_VILLAGER);
        } else {
            super.handleEntityEvent(by);
        }
    }

    private void addParticlesAroundSelf(ParticleOptions particleOptions) {
        for (int i = 0; i < 7; ++i) {
            double d = this.random.nextGaussian() * 0.01;
            double d2 = this.random.nextGaussian() * 0.01;
            double d3 = this.random.nextGaussian() * 0.01;
            this.level().addParticle(particleOptions, this.getRandomX(1.0), this.getRandomY() + 0.2, this.getRandomZ(1.0), d, d2, d3);
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (!itemStack.isEmpty() && itemStack.is(ItemTags.FISHES)) {
            if (!this.level().isClientSide) {
                this.playSound(SoundEvents.DOLPHIN_EAT, 1.0f, 1.0f);
            }
            if (this.isBaby()) {
                itemStack.consume(1, player);
                this.ageUp(Dolphin.getSpeedUpSecondsWhenFeeding(-this.age), true);
            } else {
                this.setGotFish(true);
                itemStack.consume(1, player);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, interactionHand);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.DOLPHIN_HURT;
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundEvents.DOLPHIN_DEATH;
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        return this.isInWater() ? SoundEvents.DOLPHIN_AMBIENT_WATER : SoundEvents.DOLPHIN_AMBIENT;
    }

    @Override
    protected SoundEvent getSwimSplashSound() {
        return SoundEvents.DOLPHIN_SPLASH;
    }

    @Override
    protected SoundEvent getSwimSound() {
        return SoundEvents.DOLPHIN_SWIM;
    }

    protected boolean closeToNextPos() {
        BlockPos blockPos = this.getNavigation().getTargetPos();
        if (blockPos != null) {
            return blockPos.closerToCenterThan(this.position(), 12.0);
        }
        return false;
    }

    @Override
    public void travel(Vec3 vec3) {
        if (this.isInWater()) {
            this.moveRelative(this.getSpeed(), vec3);
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
    public boolean canBeLeashed() {
        return true;
    }

    @Override
    @Nullable
    public /* synthetic */ AgeableMob getBreedOffspring(ServerLevel serverLevel, AgeableMob ageableMob) {
        return this.getBreedOffspring(serverLevel, ageableMob);
    }

    static class DolphinSwimToTreasureGoal
    extends Goal {
        private final Dolphin dolphin;
        private boolean stuck;

        DolphinSwimToTreasureGoal(Dolphin dolphin) {
            this.dolphin = dolphin;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean isInterruptable() {
            return false;
        }

        @Override
        public boolean canUse() {
            return this.dolphin.gotFish() && this.dolphin.getAirSupply() >= 100;
        }

        @Override
        public boolean canContinueToUse() {
            BlockPos blockPos = this.dolphin.treasurePos;
            if (blockPos == null) {
                return false;
            }
            return !BlockPos.containing(blockPos.getX(), this.dolphin.getY(), blockPos.getZ()).closerToCenterThan(this.dolphin.position(), 4.0) && !this.stuck && this.dolphin.getAirSupply() >= 100;
        }

        @Override
        public void start() {
            if (!(this.dolphin.level() instanceof ServerLevel)) {
                return;
            }
            ServerLevel serverLevel = (ServerLevel)this.dolphin.level();
            this.stuck = false;
            this.dolphin.getNavigation().stop();
            BlockPos blockPos = this.dolphin.blockPosition();
            BlockPos blockPos2 = serverLevel.findNearestMapStructure(StructureTags.DOLPHIN_LOCATED, blockPos, 50, false);
            if (blockPos2 == null) {
                this.stuck = true;
                return;
            }
            this.dolphin.treasurePos = blockPos2;
            serverLevel.broadcastEntityEvent(this.dolphin, (byte)38);
        }

        @Override
        public void stop() {
            BlockPos blockPos = this.dolphin.treasurePos;
            if (blockPos == null || BlockPos.containing(blockPos.getX(), this.dolphin.getY(), blockPos.getZ()).closerToCenterThan(this.dolphin.position(), 4.0) || this.stuck) {
                this.dolphin.setGotFish(false);
            }
        }

        @Override
        public void tick() {
            if (this.dolphin.treasurePos == null) {
                return;
            }
            Level level = this.dolphin.level();
            if (this.dolphin.closeToNextPos() || this.dolphin.getNavigation().isDone()) {
                BlockPos blockPos;
                Vec3 vec3 = Vec3.atCenterOf(this.dolphin.treasurePos);
                Vec3 vec32 = DefaultRandomPos.getPosTowards(this.dolphin, 16, 1, vec3, 0.3926991f);
                if (vec32 == null) {
                    vec32 = DefaultRandomPos.getPosTowards(this.dolphin, 8, 4, vec3, 1.5707963705062866);
                }
                if (!(vec32 == null || level.getFluidState(blockPos = BlockPos.containing(vec32)).is(FluidTags.WATER) && level.getBlockState(blockPos).isPathfindable(PathComputationType.WATER))) {
                    vec32 = DefaultRandomPos.getPosTowards(this.dolphin, 8, 5, vec3, 1.5707963705062866);
                }
                if (vec32 == null) {
                    this.stuck = true;
                    return;
                }
                this.dolphin.getLookControl().setLookAt(vec32.x, vec32.y, vec32.z, this.dolphin.getMaxHeadYRot() + 20, this.dolphin.getMaxHeadXRot());
                this.dolphin.getNavigation().moveTo(vec32.x, vec32.y, vec32.z, 1.3);
                if (level.random.nextInt(this.adjustedTickDelay(80)) == 0) {
                    level.broadcastEntityEvent(this.dolphin, (byte)38);
                }
            }
        }
    }

    static class DolphinSwimWithPlayerGoal
    extends Goal {
        private final Dolphin dolphin;
        private final double speedModifier;
        @Nullable
        private Player player;

        DolphinSwimWithPlayerGoal(Dolphin dolphin, double d) {
            this.dolphin = dolphin;
            this.speedModifier = d;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            this.player = DolphinSwimWithPlayerGoal.getServerLevel(this.dolphin).getNearestPlayer(SWIM_WITH_PLAYER_TARGETING, this.dolphin);
            if (this.player == null) {
                return false;
            }
            return this.player.isSwimming() && this.dolphin.getTarget() != this.player;
        }

        @Override
        public boolean canContinueToUse() {
            return this.player != null && this.player.isSwimming() && this.dolphin.distanceToSqr(this.player) < 256.0;
        }

        @Override
        public void start() {
            this.player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 100), this.dolphin);
        }

        @Override
        public void stop() {
            this.player = null;
            this.dolphin.getNavigation().stop();
        }

        @Override
        public void tick() {
            this.dolphin.getLookControl().setLookAt(this.player, this.dolphin.getMaxHeadYRot() + 20, this.dolphin.getMaxHeadXRot());
            if (this.dolphin.distanceToSqr(this.player) < 6.25) {
                this.dolphin.getNavigation().stop();
            } else {
                this.dolphin.getNavigation().moveTo(this.player, this.speedModifier);
            }
            if (this.player.isSwimming() && this.player.level().random.nextInt(6) == 0) {
                this.player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 100), this.dolphin);
            }
        }
    }

    class PlayWithItemsGoal
    extends Goal {
        private int cooldown;

        PlayWithItemsGoal() {
        }

        @Override
        public boolean canUse() {
            if (this.cooldown > Dolphin.this.tickCount) {
                return false;
            }
            List<ItemEntity> list = Dolphin.this.level().getEntitiesOfClass(ItemEntity.class, Dolphin.this.getBoundingBox().inflate(8.0, 8.0, 8.0), ALLOWED_ITEMS);
            return !list.isEmpty() || !Dolphin.this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty();
        }

        @Override
        public void start() {
            List<ItemEntity> list = Dolphin.this.level().getEntitiesOfClass(ItemEntity.class, Dolphin.this.getBoundingBox().inflate(8.0, 8.0, 8.0), ALLOWED_ITEMS);
            if (!list.isEmpty()) {
                Dolphin.this.getNavigation().moveTo(list.get(0), (double)1.2f);
                Dolphin.this.playSound(SoundEvents.DOLPHIN_PLAY, 1.0f, 1.0f);
            }
            this.cooldown = 0;
        }

        @Override
        public void stop() {
            ItemStack itemStack = Dolphin.this.getItemBySlot(EquipmentSlot.MAINHAND);
            if (!itemStack.isEmpty()) {
                this.drop(itemStack);
                Dolphin.this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                this.cooldown = Dolphin.this.tickCount + Dolphin.this.random.nextInt(100);
            }
        }

        @Override
        public void tick() {
            List<ItemEntity> list = Dolphin.this.level().getEntitiesOfClass(ItemEntity.class, Dolphin.this.getBoundingBox().inflate(8.0, 8.0, 8.0), ALLOWED_ITEMS);
            ItemStack itemStack = Dolphin.this.getItemBySlot(EquipmentSlot.MAINHAND);
            if (!itemStack.isEmpty()) {
                this.drop(itemStack);
                Dolphin.this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            } else if (!list.isEmpty()) {
                Dolphin.this.getNavigation().moveTo(list.get(0), (double)1.2f);
            }
        }

        private void drop(ItemStack itemStack) {
            if (itemStack.isEmpty()) {
                return;
            }
            double d = Dolphin.this.getEyeY() - (double)0.3f;
            ItemEntity itemEntity = new ItemEntity(Dolphin.this.level(), Dolphin.this.getX(), d, Dolphin.this.getZ(), itemStack);
            itemEntity.setPickUpDelay(40);
            itemEntity.setThrower(Dolphin.this);
            float f = 0.3f;
            float f2 = Dolphin.this.random.nextFloat() * ((float)Math.PI * 2);
            float f3 = 0.02f * Dolphin.this.random.nextFloat();
            itemEntity.setDeltaMovement(0.3f * -Mth.sin(Dolphin.this.getYRot() * ((float)Math.PI / 180)) * Mth.cos(Dolphin.this.getXRot() * ((float)Math.PI / 180)) + Mth.cos(f2) * f3, 0.3f * Mth.sin(Dolphin.this.getXRot() * ((float)Math.PI / 180)) * 1.5f, 0.3f * Mth.cos(Dolphin.this.getYRot() * ((float)Math.PI / 180)) * Mth.cos(Dolphin.this.getXRot() * ((float)Math.PI / 180)) + Mth.sin(f2) * f3);
            Dolphin.this.level().addFreshEntity(itemEntity);
        }
    }
}

