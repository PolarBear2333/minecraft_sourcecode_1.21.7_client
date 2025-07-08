/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.projectile;

import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import java.util.List;
import java.util.OptionalInt;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class FireworkRocketEntity
extends Projectile
implements ItemSupplier {
    private static final EntityDataAccessor<ItemStack> DATA_ID_FIREWORKS_ITEM = SynchedEntityData.defineId(FireworkRocketEntity.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<OptionalInt> DATA_ATTACHED_TO_TARGET = SynchedEntityData.defineId(FireworkRocketEntity.class, EntityDataSerializers.OPTIONAL_UNSIGNED_INT);
    private static final EntityDataAccessor<Boolean> DATA_SHOT_AT_ANGLE = SynchedEntityData.defineId(FireworkRocketEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int DEFAULT_LIFE = 0;
    private static final int DEFAULT_LIFE_TIME = 0;
    private static final boolean DEFAULT_SHOT_AT_ANGLE = false;
    private int life = 0;
    private int lifetime = 0;
    @Nullable
    private LivingEntity attachedToEntity;

    public FireworkRocketEntity(EntityType<? extends FireworkRocketEntity> entityType, Level level) {
        super((EntityType<? extends Projectile>)entityType, level);
    }

    public FireworkRocketEntity(Level level, double d, double d2, double d3, ItemStack itemStack) {
        super((EntityType<? extends Projectile>)EntityType.FIREWORK_ROCKET, level);
        this.life = 0;
        this.setPos(d, d2, d3);
        this.entityData.set(DATA_ID_FIREWORKS_ITEM, itemStack.copy());
        int n = 1;
        Fireworks fireworks = itemStack.get(DataComponents.FIREWORKS);
        if (fireworks != null) {
            n += fireworks.flightDuration();
        }
        this.setDeltaMovement(this.random.triangle(0.0, 0.002297), 0.05, this.random.triangle(0.0, 0.002297));
        this.lifetime = 10 * n + this.random.nextInt(6) + this.random.nextInt(7);
    }

    public FireworkRocketEntity(Level level, @Nullable Entity entity, double d, double d2, double d3, ItemStack itemStack) {
        this(level, d, d2, d3, itemStack);
        this.setOwner(entity);
    }

    public FireworkRocketEntity(Level level, ItemStack itemStack, LivingEntity livingEntity) {
        this(level, livingEntity, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), itemStack);
        this.entityData.set(DATA_ATTACHED_TO_TARGET, OptionalInt.of(livingEntity.getId()));
        this.attachedToEntity = livingEntity;
    }

    public FireworkRocketEntity(Level level, ItemStack itemStack, double d, double d2, double d3, boolean bl) {
        this(level, d, d2, d3, itemStack);
        this.entityData.set(DATA_SHOT_AT_ANGLE, bl);
    }

    public FireworkRocketEntity(Level level, ItemStack itemStack, Entity entity, double d, double d2, double d3, boolean bl) {
        this(level, itemStack, d, d2, d3, bl);
        this.setOwner(entity);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_ID_FIREWORKS_ITEM, FireworkRocketEntity.getDefaultItem());
        builder.define(DATA_ATTACHED_TO_TARGET, OptionalInt.empty());
        builder.define(DATA_SHOT_AT_ANGLE, false);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        return d < 4096.0 && !this.isAttachedToEntity();
    }

    @Override
    public boolean shouldRender(double d, double d2, double d3) {
        return super.shouldRender(d, d2, d3) && !this.isAttachedToEntity();
    }

    @Override
    public void tick() {
        HitResult hitResult;
        Object object;
        Object object2;
        super.tick();
        if (this.isAttachedToEntity()) {
            if (this.attachedToEntity == null) {
                this.entityData.get(DATA_ATTACHED_TO_TARGET).ifPresent(n -> {
                    Entity entity = this.level().getEntity(n);
                    if (entity instanceof LivingEntity) {
                        this.attachedToEntity = (LivingEntity)entity;
                    }
                });
            }
            if (this.attachedToEntity != null) {
                if (this.attachedToEntity.isFallFlying()) {
                    object2 = this.attachedToEntity.getLookAngle();
                    double d = 1.5;
                    double d2 = 0.1;
                    Vec3 vec3 = this.attachedToEntity.getDeltaMovement();
                    this.attachedToEntity.setDeltaMovement(vec3.add(((Vec3)object2).x * 0.1 + (((Vec3)object2).x * 1.5 - vec3.x) * 0.5, ((Vec3)object2).y * 0.1 + (((Vec3)object2).y * 1.5 - vec3.y) * 0.5, ((Vec3)object2).z * 0.1 + (((Vec3)object2).z * 1.5 - vec3.z) * 0.5));
                    object = this.attachedToEntity.getHandHoldingItemAngle(Items.FIREWORK_ROCKET);
                } else {
                    object = Vec3.ZERO;
                }
                this.setPos(this.attachedToEntity.getX() + ((Vec3)object).x, this.attachedToEntity.getY() + ((Vec3)object).y, this.attachedToEntity.getZ() + ((Vec3)object).z);
                this.setDeltaMovement(this.attachedToEntity.getDeltaMovement());
            }
            hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        } else {
            if (!this.isShotAtAngle()) {
                double d = this.horizontalCollision ? 1.0 : 1.15;
                this.setDeltaMovement(this.getDeltaMovement().multiply(d, 1.0, d).add(0.0, 0.04, 0.0));
            }
            object = this.getDeltaMovement();
            hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
            this.move(MoverType.SELF, (Vec3)object);
            this.applyEffectsFromBlocks();
            this.setDeltaMovement((Vec3)object);
        }
        if (!this.noPhysics && this.isAlive() && hitResult.getType() != HitResult.Type.MISS) {
            this.hitTargetOrDeflectSelf(hitResult);
            this.hasImpulse = true;
        }
        this.updateRotation();
        if (this.life == 0 && !this.isSilent()) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.AMBIENT, 3.0f, 1.0f);
        }
        ++this.life;
        if (this.level().isClientSide && this.life % 2 < 2) {
            this.level().addParticle(ParticleTypes.FIREWORK, this.getX(), this.getY(), this.getZ(), this.random.nextGaussian() * 0.05, -this.getDeltaMovement().y * 0.5, this.random.nextGaussian() * 0.05);
        }
        if (this.life > this.lifetime && (object2 = this.level()) instanceof ServerLevel) {
            object = (ServerLevel)object2;
            this.explode((ServerLevel)object);
        }
    }

    private void explode(ServerLevel serverLevel) {
        serverLevel.broadcastEntityEvent(this, (byte)17);
        this.gameEvent(GameEvent.EXPLODE, this.getOwner());
        this.dealExplosionDamage(serverLevel);
        this.discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.explode(serverLevel);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        BlockPos blockPos = new BlockPos(blockHitResult.getBlockPos());
        this.level().getBlockState(blockPos).entityInside(this.level(), blockPos, this, InsideBlockEffectApplier.NOOP);
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (this.hasExplosion()) {
                this.explode(serverLevel);
            }
        }
        super.onHitBlock(blockHitResult);
    }

    private boolean hasExplosion() {
        return !this.getExplosions().isEmpty();
    }

    private void dealExplosionDamage(ServerLevel serverLevel) {
        float f = 0.0f;
        List<FireworkExplosion> list = this.getExplosions();
        if (!list.isEmpty()) {
            f = 5.0f + (float)(list.size() * 2);
        }
        if (f > 0.0f) {
            if (this.attachedToEntity != null) {
                this.attachedToEntity.hurtServer(serverLevel, this.damageSources().fireworks(this, this.getOwner()), 5.0f + (float)(list.size() * 2));
            }
            double d = 5.0;
            Vec3 vec3 = this.position();
            List<LivingEntity> list2 = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(5.0));
            for (LivingEntity livingEntity : list2) {
                if (livingEntity == this.attachedToEntity || this.distanceToSqr(livingEntity) > 25.0) continue;
                boolean bl = false;
                for (int i = 0; i < 2; ++i) {
                    Vec3 vec32 = new Vec3(livingEntity.getX(), livingEntity.getY(0.5 * (double)i), livingEntity.getZ());
                    BlockHitResult blockHitResult = this.level().clip(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
                    if (((HitResult)blockHitResult).getType() != HitResult.Type.MISS) continue;
                    bl = true;
                    break;
                }
                if (!bl) continue;
                float f2 = f * (float)Math.sqrt((5.0 - (double)this.distanceTo(livingEntity)) / 5.0);
                livingEntity.hurtServer(serverLevel, this.damageSources().fireworks(this, this.getOwner()), f2);
            }
        }
    }

    private boolean isAttachedToEntity() {
        return this.entityData.get(DATA_ATTACHED_TO_TARGET).isPresent();
    }

    public boolean isShotAtAngle() {
        return this.entityData.get(DATA_SHOT_AT_ANGLE);
    }

    @Override
    public void handleEntityEvent(byte by) {
        if (by == 17 && this.level().isClientSide) {
            Vec3 vec3 = this.getDeltaMovement();
            this.level().createFireworks(this.getX(), this.getY(), this.getZ(), vec3.x, vec3.y, vec3.z, this.getExplosions());
        }
        super.handleEntityEvent(by);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putInt("Life", this.life);
        valueOutput.putInt("LifeTime", this.lifetime);
        valueOutput.store("FireworksItem", ItemStack.CODEC, this.getItem());
        valueOutput.putBoolean("ShotAtAngle", this.entityData.get(DATA_SHOT_AT_ANGLE));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.life = valueInput.getIntOr("Life", 0);
        this.lifetime = valueInput.getIntOr("LifeTime", 0);
        this.entityData.set(DATA_ID_FIREWORKS_ITEM, valueInput.read("FireworksItem", ItemStack.CODEC).orElse(FireworkRocketEntity.getDefaultItem()));
        this.entityData.set(DATA_SHOT_AT_ANGLE, valueInput.getBooleanOr("ShotAtAngle", false));
    }

    private List<FireworkExplosion> getExplosions() {
        ItemStack itemStack = this.entityData.get(DATA_ID_FIREWORKS_ITEM);
        Fireworks fireworks = itemStack.get(DataComponents.FIREWORKS);
        return fireworks != null ? fireworks.explosions() : List.of();
    }

    @Override
    public ItemStack getItem() {
        return this.entityData.get(DATA_ID_FIREWORKS_ITEM);
    }

    @Override
    public boolean isAttackable() {
        return false;
    }

    private static ItemStack getDefaultItem() {
        return new ItemStack(Items.FIREWORK_ROCKET);
    }

    @Override
    public DoubleDoubleImmutablePair calculateHorizontalHurtKnockbackDirection(LivingEntity livingEntity, DamageSource damageSource) {
        double d = livingEntity.position().x - this.position().x;
        double d2 = livingEntity.position().z - this.position().z;
        return DoubleDoubleImmutablePair.of((double)d, (double)d2);
    }
}

