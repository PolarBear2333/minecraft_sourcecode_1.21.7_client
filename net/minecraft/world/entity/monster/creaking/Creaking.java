/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Dynamic
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.monster.creaking;

import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.creaking.CreakingAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CreakingHeartBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.CreakingHeartBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.CreakingHeartState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class Creaking
extends Monster {
    private static final EntityDataAccessor<Boolean> CAN_MOVE = SynchedEntityData.defineId(Creaking.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_ACTIVE = SynchedEntityData.defineId(Creaking.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_TEARING_DOWN = SynchedEntityData.defineId(Creaking.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Optional<BlockPos>> HOME_POS = SynchedEntityData.defineId(Creaking.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final int ATTACK_ANIMATION_DURATION = 15;
    private static final int MAX_HEALTH = 1;
    private static final float ATTACK_DAMAGE = 3.0f;
    private static final float FOLLOW_RANGE = 32.0f;
    private static final float ACTIVATION_RANGE_SQ = 144.0f;
    public static final int ATTACK_INTERVAL = 40;
    private static final float MOVEMENT_SPEED_WHEN_FIGHTING = 0.4f;
    public static final float SPEED_MULTIPLIER_WHEN_IDLING = 0.3f;
    public static final int CREAKING_ORANGE = 16545810;
    public static final int CREAKING_GRAY = 0x5F5F5F;
    public static final int INVULNERABILITY_ANIMATION_DURATION = 8;
    public static final int TWITCH_DEATH_DURATION = 45;
    private static final int MAX_PLAYER_STUCK_COUNTER = 4;
    private int attackAnimationRemainingTicks;
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState invulnerabilityAnimationState = new AnimationState();
    public final AnimationState deathAnimationState = new AnimationState();
    private int invulnerabilityAnimationRemainingTicks;
    private boolean eyesGlowing;
    private int nextFlickerTime;
    private int playerStuckCounter;

    public Creaking(EntityType<? extends Creaking> entityType, Level level) {
        super((EntityType<? extends Monster>)entityType, level);
        this.lookControl = new CreakingLookControl(this);
        this.moveControl = new CreakingMoveControl(this);
        this.jumpControl = new CreakingJumpControl(this);
        GroundPathNavigation groundPathNavigation = (GroundPathNavigation)this.getNavigation();
        groundPathNavigation.setCanFloat(true);
        this.xpReward = 0;
    }

    public void setTransient(BlockPos blockPos) {
        this.setHomePos(blockPos);
        this.setPathfindingMalus(PathType.DAMAGE_OTHER, 8.0f);
        this.setPathfindingMalus(PathType.POWDER_SNOW, 8.0f);
        this.setPathfindingMalus(PathType.LAVA, 8.0f);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, 0.0f);
        this.setPathfindingMalus(PathType.DANGER_FIRE, 0.0f);
    }

    public boolean isHeartBound() {
        return this.getHomePos() != null;
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        return new CreakingBodyRotationControl(this);
    }

    protected Brain.Provider<Creaking> brainProvider() {
        return CreakingAi.brainProvider();
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return CreakingAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(CAN_MOVE, true);
        builder.define(IS_ACTIVE, false);
        builder.define(IS_TEARING_DOWN, false);
        builder.define(HOME_POS, Optional.empty());
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1.0).add(Attributes.MOVEMENT_SPEED, 0.4f).add(Attributes.ATTACK_DAMAGE, 3.0).add(Attributes.FOLLOW_RANGE, 32.0).add(Attributes.STEP_HEIGHT, 1.0625);
    }

    public boolean canMove() {
        return this.entityData.get(CAN_MOVE);
    }

    @Override
    public boolean doHurtTarget(ServerLevel serverLevel, Entity entity) {
        if (!(entity instanceof LivingEntity)) {
            return false;
        }
        this.attackAnimationRemainingTicks = 15;
        this.level().broadcastEntityEvent(this, (byte)4);
        return super.doHurtTarget(serverLevel, entity);
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        CreakingHeartBlockEntity creakingHeartBlockEntity;
        BlockPos blockPos = this.getHomePos();
        if (blockPos == null || damageSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurtServer(serverLevel, damageSource, f);
        }
        if (this.isInvulnerableTo(serverLevel, damageSource) || this.invulnerabilityAnimationRemainingTicks > 0 || this.isDeadOrDying()) {
            return false;
        }
        Player player = this.blameSourceForDamage(damageSource);
        Entity entity = damageSource.getDirectEntity();
        if (!(entity instanceof LivingEntity) && !(entity instanceof Projectile) && player == null) {
            return false;
        }
        this.invulnerabilityAnimationRemainingTicks = 8;
        this.level().broadcastEntityEvent(this, (byte)66);
        this.gameEvent(GameEvent.ENTITY_ACTION);
        BlockEntity blockEntity = this.level().getBlockEntity(blockPos);
        if (blockEntity instanceof CreakingHeartBlockEntity && (creakingHeartBlockEntity = (CreakingHeartBlockEntity)blockEntity).isProtector(this)) {
            if (player != null) {
                creakingHeartBlockEntity.creakingHurt();
            }
            this.playHurtSound(damageSource);
        }
        return true;
    }

    public Player blameSourceForDamage(DamageSource damageSource) {
        this.resolveMobResponsibleForDamage(damageSource);
        return this.resolvePlayerResponsibleForDamage(damageSource);
    }

    @Override
    public boolean isPushable() {
        return super.isPushable() && this.canMove();
    }

    @Override
    public void push(double d, double d2, double d3) {
        if (!this.canMove()) {
            return;
        }
        super.push(d, d2, d3);
    }

    public Brain<Creaking> getBrain() {
        return super.getBrain();
    }

    @Override
    protected void customServerAiStep(ServerLevel serverLevel) {
        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("creakingBrain");
        this.getBrain().tick((ServerLevel)this.level(), this);
        profilerFiller.pop();
        CreakingAi.updateActivity(this);
    }

    @Override
    public void aiStep() {
        if (this.invulnerabilityAnimationRemainingTicks > 0) {
            --this.invulnerabilityAnimationRemainingTicks;
        }
        if (this.attackAnimationRemainingTicks > 0) {
            --this.attackAnimationRemainingTicks;
        }
        if (!this.level().isClientSide) {
            boolean bl = this.entityData.get(CAN_MOVE);
            boolean bl2 = this.checkCanMove();
            if (bl2 != bl) {
                this.gameEvent(GameEvent.ENTITY_ACTION);
                if (bl2) {
                    this.makeSound(SoundEvents.CREAKING_UNFREEZE);
                } else {
                    this.stopInPlace();
                    this.makeSound(SoundEvents.CREAKING_FREEZE);
                }
            }
            this.entityData.set(CAN_MOVE, bl2);
        }
        super.aiStep();
    }

    @Override
    public void tick() {
        BlockPos blockPos;
        if (!this.level().isClientSide && (blockPos = this.getHomePos()) != null) {
            CreakingHeartBlockEntity creakingHeartBlockEntity;
            boolean bl;
            BlockEntity blockEntity = this.level().getBlockEntity(blockPos);
            boolean bl2 = bl = blockEntity instanceof CreakingHeartBlockEntity && (creakingHeartBlockEntity = (CreakingHeartBlockEntity)blockEntity).isProtector(this);
            if (!bl) {
                this.setHealth(0.0f);
            }
        }
        super.tick();
        if (this.level().isClientSide) {
            this.setupAnimationStates();
            this.checkEyeBlink();
        }
    }

    @Override
    protected void tickDeath() {
        if (this.isHeartBound() && this.isTearingDown()) {
            ++this.deathTime;
            if (!this.level().isClientSide() && this.deathTime > 45 && !this.isRemoved()) {
                this.tearDown();
            }
        } else {
            super.tickDeath();
        }
    }

    @Override
    protected void updateWalkAnimation(float f) {
        float f2 = Math.min(f * 25.0f, 3.0f);
        this.walkAnimation.update(f2, 0.4f, 1.0f);
    }

    private void setupAnimationStates() {
        this.attackAnimationState.animateWhen(this.attackAnimationRemainingTicks > 0, this.tickCount);
        this.invulnerabilityAnimationState.animateWhen(this.invulnerabilityAnimationRemainingTicks > 0, this.tickCount);
        this.deathAnimationState.animateWhen(this.isTearingDown(), this.tickCount);
    }

    public void tearDown() {
        Object object = this.level();
        if (object instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)object;
            object = this.getBoundingBox();
            Vec3 vec3 = ((AABB)object).getCenter();
            double d = ((AABB)object).getXsize() * 0.3;
            double d2 = ((AABB)object).getYsize() * 0.3;
            double d3 = ((AABB)object).getZsize() * 0.3;
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK_CRUMBLE, Blocks.PALE_OAK_WOOD.defaultBlockState()), vec3.x, vec3.y, vec3.z, 100, d, d2, d3, 0.0);
            serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK_CRUMBLE, (BlockState)Blocks.CREAKING_HEART.defaultBlockState().setValue(CreakingHeartBlock.STATE, CreakingHeartState.AWAKE)), vec3.x, vec3.y, vec3.z, 10, d, d2, d3, 0.0);
        }
        this.makeSound(this.getDeathSound());
        this.remove(Entity.RemovalReason.DISCARDED);
    }

    public void creakingDeathEffects(DamageSource damageSource) {
        this.blameSourceForDamage(damageSource);
        this.die(damageSource);
        this.makeSound(SoundEvents.CREAKING_TWITCH);
    }

    @Override
    public void handleEntityEvent(byte by) {
        if (by == 66) {
            this.invulnerabilityAnimationRemainingTicks = 8;
            this.playHurtSound(this.damageSources().generic());
        } else if (by == 4) {
            this.attackAnimationRemainingTicks = 15;
            this.playAttackSound();
        } else {
            super.handleEntityEvent(by);
        }
    }

    @Override
    public boolean fireImmune() {
        return this.isHeartBound() || super.fireImmune();
    }

    @Override
    protected boolean canAddPassenger(Entity entity) {
        return !this.isHeartBound() && super.canAddPassenger(entity);
    }

    @Override
    protected boolean couldAcceptPassenger() {
        return !this.isHeartBound() && super.couldAcceptPassenger();
    }

    @Override
    protected void addPassenger(Entity entity) {
        if (this.isHeartBound()) {
            throw new IllegalStateException("Should never addPassenger without checking couldAcceptPassenger()");
        }
    }

    @Override
    public boolean canUsePortal(boolean bl) {
        return !this.isHeartBound() && super.canUsePortal(bl);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new CreakingPathNavigation(this, level);
    }

    public boolean playerIsStuckInYou() {
        List list = this.brain.getMemory(MemoryModuleType.NEAREST_PLAYERS).orElse(List.of());
        if (list.isEmpty()) {
            this.playerStuckCounter = 0;
            return false;
        }
        AABB aABB = this.getBoundingBox();
        for (Player player : list) {
            if (!aABB.contains(player.getEyePosition())) continue;
            ++this.playerStuckCounter;
            return this.playerStuckCounter > 4;
        }
        this.playerStuckCounter = 0;
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        valueInput.read("home_pos", BlockPos.CODEC).ifPresent(this::setTransient);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.storeNullable("home_pos", BlockPos.CODEC, this.getHomePos());
    }

    public void setHomePos(BlockPos blockPos) {
        this.entityData.set(HOME_POS, Optional.of(blockPos));
    }

    @Nullable
    public BlockPos getHomePos() {
        return this.entityData.get(HOME_POS).orElse(null);
    }

    public void setTearingDown() {
        this.entityData.set(IS_TEARING_DOWN, true);
    }

    public boolean isTearingDown() {
        return this.entityData.get(IS_TEARING_DOWN);
    }

    public boolean hasGlowingEyes() {
        return this.eyesGlowing;
    }

    public void checkEyeBlink() {
        if (this.deathTime > this.nextFlickerTime) {
            this.nextFlickerTime = this.deathTime + this.getRandom().nextIntBetweenInclusive(this.eyesGlowing ? 2 : this.deathTime / 4, this.eyesGlowing ? 8 : this.deathTime / 2);
            this.eyesGlowing = !this.eyesGlowing;
        }
    }

    @Override
    public void playAttackSound() {
        this.makeSound(SoundEvents.CREAKING_ATTACK);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isActive()) {
            return null;
        }
        return SoundEvents.CREAKING_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return this.isHeartBound() ? SoundEvents.CREAKING_SWAY : super.getHurtSound(damageSource);
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.CREAKING_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos blockPos, BlockState blockState) {
        this.playSound(SoundEvents.CREAKING_STEP, 0.15f, 1.0f);
    }

    @Override
    @Nullable
    public LivingEntity getTarget() {
        return this.getTargetFromBrain();
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }

    @Override
    public void knockback(double d, double d2, double d3) {
        if (!this.canMove()) {
            return;
        }
        super.knockback(d, d2, d3);
    }

    public boolean checkCanMove() {
        List list = this.brain.getMemory(MemoryModuleType.NEAREST_PLAYERS).orElse(List.of());
        boolean bl = this.isActive();
        if (list.isEmpty()) {
            if (bl) {
                this.deactivate();
            }
            return true;
        }
        boolean bl2 = false;
        for (Player player : list) {
            if (!this.canAttack(player) || this.isAlliedTo(player)) continue;
            bl2 = true;
            if (bl && !LivingEntity.PLAYER_NOT_WEARING_DISGUISE_ITEM.test(player) || !this.isLookingAtMe(player, 0.5, false, true, this.getEyeY(), this.getY() + 0.5 * (double)this.getScale(), (this.getEyeY() + this.getY()) / 2.0)) continue;
            if (bl) {
                return false;
            }
            if (!(player.distanceToSqr(this) < 144.0)) continue;
            this.activate(player);
            return false;
        }
        if (!bl2 && bl) {
            this.deactivate();
        }
        return true;
    }

    public void activate(Player player) {
        this.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, player);
        this.gameEvent(GameEvent.ENTITY_ACTION);
        this.makeSound(SoundEvents.CREAKING_ACTIVATE);
        this.setIsActive(true);
    }

    public void deactivate() {
        this.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
        this.gameEvent(GameEvent.ENTITY_ACTION);
        this.makeSound(SoundEvents.CREAKING_DEACTIVATE);
        this.setIsActive(false);
    }

    public void setIsActive(boolean bl) {
        this.entityData.set(IS_ACTIVE, bl);
    }

    public boolean isActive() {
        return this.entityData.get(IS_ACTIVE);
    }

    @Override
    public float getWalkTargetValue(BlockPos blockPos, LevelReader levelReader) {
        return 0.0f;
    }

    class CreakingLookControl
    extends LookControl {
        public CreakingLookControl(Creaking creaking2) {
            super(creaking2);
        }

        @Override
        public void tick() {
            if (Creaking.this.canMove()) {
                super.tick();
            }
        }
    }

    class CreakingMoveControl
    extends MoveControl {
        public CreakingMoveControl(Creaking creaking2) {
            super(creaking2);
        }

        @Override
        public void tick() {
            if (Creaking.this.canMove()) {
                super.tick();
            }
        }
    }

    class CreakingJumpControl
    extends JumpControl {
        public CreakingJumpControl(Creaking creaking2) {
            super(creaking2);
        }

        @Override
        public void tick() {
            if (Creaking.this.canMove()) {
                super.tick();
            } else {
                Creaking.this.setJumping(false);
            }
        }
    }

    class CreakingBodyRotationControl
    extends BodyRotationControl {
        public CreakingBodyRotationControl(Creaking creaking2) {
            super(creaking2);
        }

        @Override
        public void clientTick() {
            if (Creaking.this.canMove()) {
                super.clientTick();
            }
        }
    }

    class CreakingPathNavigation
    extends GroundPathNavigation {
        CreakingPathNavigation(Creaking creaking2, Level level) {
            super(creaking2, level);
        }

        @Override
        public void tick() {
            if (Creaking.this.canMove()) {
                super.tick();
            }
        }

        @Override
        protected PathFinder createPathFinder(int n) {
            this.nodeEvaluator = new HomeNodeEvaluator();
            this.nodeEvaluator.setCanPassDoors(true);
            return new PathFinder(this.nodeEvaluator, n);
        }
    }

    class HomeNodeEvaluator
    extends WalkNodeEvaluator {
        private static final int MAX_DISTANCE_TO_HOME_SQ = 1024;

        HomeNodeEvaluator() {
        }

        @Override
        public PathType getPathType(PathfindingContext pathfindingContext, int n, int n2, int n3) {
            BlockPos blockPos = Creaking.this.getHomePos();
            if (blockPos == null) {
                return super.getPathType(pathfindingContext, n, n2, n3);
            }
            double d = blockPos.distSqr(new Vec3i(n, n2, n3));
            if (d > 1024.0 && d >= blockPos.distSqr(pathfindingContext.mobPosition())) {
                return PathType.BLOCKED;
            }
            return super.getPathType(pathfindingContext, n, n2, n3);
        }
    }
}

