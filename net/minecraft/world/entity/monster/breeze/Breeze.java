/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Dynamic
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.monster.breeze;

import com.mojang.serialization.Dynamic;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.breeze.BreezeAi;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;

public class Breeze
extends Monster {
    private static final int SLIDE_PARTICLES_AMOUNT = 20;
    private static final int IDLE_PARTICLES_AMOUNT = 1;
    private static final int JUMP_DUST_PARTICLES_AMOUNT = 20;
    private static final int JUMP_TRAIL_PARTICLES_AMOUNT = 3;
    private static final int JUMP_TRAIL_DURATION_TICKS = 5;
    private static final int JUMP_CIRCLE_DISTANCE_Y = 10;
    private static final float FALL_DISTANCE_SOUND_TRIGGER_THRESHOLD = 3.0f;
    private static final int WHIRL_SOUND_FREQUENCY_MIN = 1;
    private static final int WHIRL_SOUND_FREQUENCY_MAX = 80;
    public AnimationState idle = new AnimationState();
    public AnimationState slide = new AnimationState();
    public AnimationState slideBack = new AnimationState();
    public AnimationState longJump = new AnimationState();
    public AnimationState shoot = new AnimationState();
    public AnimationState inhale = new AnimationState();
    private int jumpTrailStartedTick = 0;
    private int soundTick = 0;
    private static final ProjectileDeflection PROJECTILE_DEFLECTION = (projectile, entity, randomSource) -> {
        entity.level().playSound(null, entity, SoundEvents.BREEZE_DEFLECT, entity.getSoundSource(), 1.0f, 1.0f);
        ProjectileDeflection.REVERSE.deflect(projectile, entity, randomSource);
    };

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.63f).add(Attributes.MAX_HEALTH, 30.0).add(Attributes.FOLLOW_RANGE, 24.0).add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    public Breeze(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(PathType.DANGER_TRAPDOOR, -1.0f);
        this.setPathfindingMalus(PathType.DAMAGE_FIRE, -1.0f);
        this.xpReward = 10;
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic) {
        return BreezeAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    public Brain<Breeze> getBrain() {
        return super.getBrain();
    }

    protected Brain.Provider<Breeze> brainProvider() {
        return Brain.provider(BreezeAi.MEMORY_TYPES, BreezeAi.SENSOR_TYPES);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> entityDataAccessor) {
        if (this.level().isClientSide() && DATA_POSE.equals(entityDataAccessor)) {
            this.resetAnimations();
            Pose pose = this.getPose();
            switch (pose) {
                case SHOOTING: {
                    this.shoot.startIfStopped(this.tickCount);
                    break;
                }
                case INHALING: {
                    this.inhale.startIfStopped(this.tickCount);
                    break;
                }
                case SLIDING: {
                    this.slide.startIfStopped(this.tickCount);
                }
            }
        }
        super.onSyncedDataUpdated(entityDataAccessor);
    }

    private void resetAnimations() {
        this.shoot.stop();
        this.idle.stop();
        this.inhale.stop();
        this.longJump.stop();
    }

    @Override
    public void tick() {
        Pose pose = this.getPose();
        switch (pose) {
            case SLIDING: {
                this.emitGroundParticles(20);
                break;
            }
            case SHOOTING: 
            case INHALING: 
            case STANDING: {
                this.resetJumpTrail().emitGroundParticles(1 + this.getRandom().nextInt(1));
                break;
            }
            case LONG_JUMPING: {
                this.longJump.startIfStopped(this.tickCount);
                this.emitJumpTrailParticles();
            }
        }
        this.idle.startIfStopped(this.tickCount);
        if (pose != Pose.SLIDING && this.slide.isStarted()) {
            this.slideBack.start(this.tickCount);
            this.slide.stop();
        }
        int n = this.soundTick = this.soundTick == 0 ? this.random.nextIntBetweenInclusive(1, 80) : this.soundTick - 1;
        if (this.soundTick == 0) {
            this.playWhirlSound();
        }
        super.tick();
    }

    public Breeze resetJumpTrail() {
        this.jumpTrailStartedTick = 0;
        return this;
    }

    public void emitJumpTrailParticles() {
        if (++this.jumpTrailStartedTick > 5) {
            return;
        }
        BlockState blockState = !this.getInBlockState().isAir() ? this.getInBlockState() : this.getBlockStateOn();
        Vec3 vec3 = this.getDeltaMovement();
        Vec3 vec32 = this.position().add(vec3).add(0.0, 0.1f, 0.0);
        for (int i = 0; i < 3; ++i) {
            this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockState), vec32.x, vec32.y, vec32.z, 0.0, 0.0, 0.0);
        }
    }

    public void emitGroundParticles(int n) {
        BlockState blockState;
        if (this.isPassenger()) {
            return;
        }
        Vec3 vec3 = this.getBoundingBox().getCenter();
        Vec3 vec32 = new Vec3(vec3.x, this.position().y, vec3.z);
        BlockState blockState2 = blockState = !this.getInBlockState().isAir() ? this.getInBlockState() : this.getBlockStateOn();
        if (blockState.getRenderShape() == RenderShape.INVISIBLE) {
            return;
        }
        for (int i = 0; i < n; ++i) {
            this.level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, blockState), vec32.x, vec32.y, vec32.z, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public void playAmbientSound() {
        if (this.getTarget() != null && this.onGround()) {
            return;
        }
        this.level().playLocalSound(this, this.getAmbientSound(), this.getSoundSource(), 1.0f, 1.0f);
    }

    public void playWhirlSound() {
        float f = 0.7f + 0.4f * this.random.nextFloat();
        float f2 = 0.8f + 0.2f * this.random.nextFloat();
        this.level().playLocalSound(this, SoundEvents.BREEZE_WHIRL, this.getSoundSource(), f2, f);
    }

    @Override
    public ProjectileDeflection deflection(Projectile projectile) {
        if (projectile.getType() == EntityType.BREEZE_WIND_CHARGE || projectile.getType() == EntityType.WIND_CHARGE) {
            return ProjectileDeflection.NONE;
        }
        return this.getType().is(EntityTypeTags.DEFLECTS_PROJECTILES) ? PROJECTILE_DEFLECTION : ProjectileDeflection.NONE;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.BREEZE_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.BREEZE_HURT;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.onGround() ? SoundEvents.BREEZE_IDLE_GROUND : SoundEvents.BREEZE_IDLE_AIR;
    }

    public Optional<LivingEntity> getHurtBy() {
        return this.getBrain().getMemory(MemoryModuleType.HURT_BY).map(DamageSource::getEntity).filter(entity -> entity instanceof LivingEntity).map(entity -> (LivingEntity)entity);
    }

    public boolean withinInnerCircleRange(Vec3 vec3) {
        Vec3 vec32 = this.blockPosition().getCenter();
        return vec3.closerThan(vec32, 4.0, 10.0);
    }

    @Override
    protected void customServerAiStep(ServerLevel serverLevel) {
        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("breezeBrain");
        this.getBrain().tick(serverLevel, this);
        profilerFiller.popPush("breezeActivityUpdate");
        BreezeAi.updateActivity(this);
        profilerFiller.pop();
        super.customServerAiStep(serverLevel);
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
        DebugPackets.sendBreezeInfo(this);
    }

    @Override
    public boolean canAttackType(EntityType<?> entityType) {
        return entityType == EntityType.PLAYER || entityType == EntityType.IRON_GOLEM;
    }

    @Override
    public int getMaxHeadYRot() {
        return 30;
    }

    @Override
    public int getHeadRotSpeed() {
        return 25;
    }

    public double getFiringYPosition() {
        return this.getY() + (double)(this.getBbHeight() / 2.0f) + (double)0.3f;
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel serverLevel, DamageSource damageSource) {
        return damageSource.getEntity() instanceof Breeze || super.isInvulnerableTo(serverLevel, damageSource);
    }

    @Override
    public double getFluidJumpThreshold() {
        return this.getEyeHeight();
    }

    @Override
    public boolean causeFallDamage(double d, float f, DamageSource damageSource) {
        if (d > 3.0) {
            this.playSound(SoundEvents.BREEZE_LAND, 1.0f, 1.0f);
        }
        return super.causeFallDamage(d, f, damageSource);
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.EVENTS;
    }

    @Override
    @Nullable
    public LivingEntity getTarget() {
        return this.getTargetFromBrain();
    }
}

