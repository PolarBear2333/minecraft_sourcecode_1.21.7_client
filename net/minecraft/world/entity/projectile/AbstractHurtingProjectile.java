/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.projectile;

import javax.annotation.Nullable;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractHurtingProjectile
extends Projectile {
    public static final double INITAL_ACCELERATION_POWER = 0.1;
    public static final double DEFLECTION_SCALE = 0.5;
    public double accelerationPower = 0.1;

    protected AbstractHurtingProjectile(EntityType<? extends AbstractHurtingProjectile> entityType, Level level) {
        super((EntityType<? extends Projectile>)entityType, level);
    }

    protected AbstractHurtingProjectile(EntityType<? extends AbstractHurtingProjectile> entityType, double d, double d2, double d3, Level level) {
        this(entityType, level);
        this.setPos(d, d2, d3);
    }

    public AbstractHurtingProjectile(EntityType<? extends AbstractHurtingProjectile> entityType, double d, double d2, double d3, Vec3 vec3, Level level) {
        this(entityType, level);
        this.snapTo(d, d2, d3, this.getYRot(), this.getXRot());
        this.reapplyPosition();
        this.assignDirectionalMovement(vec3, this.accelerationPower);
    }

    public AbstractHurtingProjectile(EntityType<? extends AbstractHurtingProjectile> entityType, LivingEntity livingEntity, Vec3 vec3, Level level) {
        this(entityType, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), vec3, level);
        this.setOwner(livingEntity);
        this.setRot(livingEntity.getYRot(), livingEntity.getXRot());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        double d2 = this.getBoundingBox().getSize() * 4.0;
        if (Double.isNaN(d2)) {
            d2 = 4.0;
        }
        return d < (d2 *= 64.0) * d2;
    }

    protected ClipContext.Block getClipType() {
        return ClipContext.Block.COLLIDER;
    }

    @Override
    public void tick() {
        Entity entity = this.getOwner();
        this.applyInertia();
        if (!this.level().isClientSide && (entity != null && entity.isRemoved() || !this.level().hasChunkAt(this.blockPosition()))) {
            this.discard();
            return;
        }
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity, this.getClipType());
        Vec3 vec3 = hitResult.getType() != HitResult.Type.MISS ? hitResult.getLocation() : this.position().add(this.getDeltaMovement());
        ProjectileUtil.rotateTowardsMovement(this, 0.2f);
        this.setPos(vec3);
        this.applyEffectsFromBlocks();
        super.tick();
        if (this.shouldBurn()) {
            this.igniteForSeconds(1.0f);
        }
        if (hitResult.getType() != HitResult.Type.MISS && this.isAlive()) {
            this.hitTargetOrDeflectSelf(hitResult);
        }
        this.createParticleTrail();
    }

    private void applyInertia() {
        float f;
        Vec3 vec3 = this.getDeltaMovement();
        Vec3 vec32 = this.position();
        if (this.isInWater()) {
            for (int i = 0; i < 4; ++i) {
                float f2 = 0.25f;
                this.level().addParticle(ParticleTypes.BUBBLE, vec32.x - vec3.x * 0.25, vec32.y - vec3.y * 0.25, vec32.z - vec3.z * 0.25, vec3.x, vec3.y, vec3.z);
            }
            f = this.getLiquidInertia();
        } else {
            f = this.getInertia();
        }
        this.setDeltaMovement(vec3.add(vec3.normalize().scale(this.accelerationPower)).scale(f));
    }

    private void createParticleTrail() {
        ParticleOptions particleOptions = this.getTrailParticle();
        Vec3 vec3 = this.position();
        if (particleOptions != null) {
            this.level().addParticle(particleOptions, vec3.x, vec3.y + 0.5, vec3.z, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        return false;
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && !entity.noPhysics;
    }

    protected boolean shouldBurn() {
        return true;
    }

    @Nullable
    protected ParticleOptions getTrailParticle() {
        return ParticleTypes.SMOKE;
    }

    protected float getInertia() {
        return 0.95f;
    }

    protected float getLiquidInertia() {
        return 0.8f;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putDouble("acceleration_power", this.accelerationPower);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.accelerationPower = valueInput.getDoubleOr("acceleration_power", 0.1);
    }

    @Override
    public float getLightLevelDependentMagicValue() {
        return 1.0f;
    }

    private void assignDirectionalMovement(Vec3 vec3, double d) {
        this.setDeltaMovement(vec3.normalize().scale(d));
        this.hasImpulse = true;
    }

    @Override
    protected void onDeflection(@Nullable Entity entity, boolean bl) {
        super.onDeflection(entity, bl);
        this.accelerationPower = bl ? 0.1 : (this.accelerationPower *= 0.5);
    }
}

