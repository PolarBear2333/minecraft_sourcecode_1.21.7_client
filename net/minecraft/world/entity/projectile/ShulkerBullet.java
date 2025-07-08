/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.MoreObjects
 *  com.google.common.collect.Lists
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.projectile;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ShulkerBullet
extends Projectile {
    private static final double SPEED = 0.15;
    @Nullable
    private EntityReference<Entity> finalTarget;
    @Nullable
    private Direction currentMoveDirection;
    private int flightSteps;
    private double targetDeltaX;
    private double targetDeltaY;
    private double targetDeltaZ;

    public ShulkerBullet(EntityType<? extends ShulkerBullet> entityType, Level level) {
        super((EntityType<? extends Projectile>)entityType, level);
        this.noPhysics = true;
    }

    public ShulkerBullet(Level level, LivingEntity livingEntity, Entity entity, Direction.Axis axis) {
        this((EntityType<? extends ShulkerBullet>)EntityType.SHULKER_BULLET, level);
        this.setOwner(livingEntity);
        Vec3 vec3 = livingEntity.getBoundingBox().getCenter();
        this.snapTo(vec3.x, vec3.y, vec3.z, this.getYRot(), this.getXRot());
        this.finalTarget = new EntityReference<Entity>(entity);
        this.currentMoveDirection = Direction.UP;
        this.selectNextMoveDirection(axis, entity);
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        if (this.finalTarget != null) {
            valueOutput.store("Target", UUIDUtil.CODEC, this.finalTarget.getUUID());
        }
        valueOutput.storeNullable("Dir", Direction.LEGACY_ID_CODEC, this.currentMoveDirection);
        valueOutput.putInt("Steps", this.flightSteps);
        valueOutput.putDouble("TXD", this.targetDeltaX);
        valueOutput.putDouble("TYD", this.targetDeltaY);
        valueOutput.putDouble("TZD", this.targetDeltaZ);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.flightSteps = valueInput.getIntOr("Steps", 0);
        this.targetDeltaX = valueInput.getDoubleOr("TXD", 0.0);
        this.targetDeltaY = valueInput.getDoubleOr("TYD", 0.0);
        this.targetDeltaZ = valueInput.getDoubleOr("TZD", 0.0);
        this.currentMoveDirection = valueInput.read("Dir", Direction.LEGACY_ID_CODEC).orElse(null);
        this.finalTarget = EntityReference.read(valueInput, "Target");
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Nullable
    private Direction getMoveDirection() {
        return this.currentMoveDirection;
    }

    private void setMoveDirection(@Nullable Direction direction) {
        this.currentMoveDirection = direction;
    }

    private void selectNextMoveDirection(@Nullable Direction.Axis axis, @Nullable Entity entity) {
        BlockPos blockPos;
        double d = 0.5;
        if (entity == null) {
            blockPos = this.blockPosition().below();
        } else {
            d = (double)entity.getBbHeight() * 0.5;
            blockPos = BlockPos.containing(entity.getX(), entity.getY() + d, entity.getZ());
        }
        double d2 = (double)blockPos.getX() + 0.5;
        double d3 = (double)blockPos.getY() + d;
        double d4 = (double)blockPos.getZ() + 0.5;
        Direction direction = null;
        if (!blockPos.closerToCenterThan(this.position(), 2.0)) {
            BlockPos blockPos2 = this.blockPosition();
            ArrayList arrayList = Lists.newArrayList();
            if (axis != Direction.Axis.X) {
                if (blockPos2.getX() < blockPos.getX() && this.level().isEmptyBlock(blockPos2.east())) {
                    arrayList.add(Direction.EAST);
                } else if (blockPos2.getX() > blockPos.getX() && this.level().isEmptyBlock(blockPos2.west())) {
                    arrayList.add(Direction.WEST);
                }
            }
            if (axis != Direction.Axis.Y) {
                if (blockPos2.getY() < blockPos.getY() && this.level().isEmptyBlock(blockPos2.above())) {
                    arrayList.add(Direction.UP);
                } else if (blockPos2.getY() > blockPos.getY() && this.level().isEmptyBlock(blockPos2.below())) {
                    arrayList.add(Direction.DOWN);
                }
            }
            if (axis != Direction.Axis.Z) {
                if (blockPos2.getZ() < blockPos.getZ() && this.level().isEmptyBlock(blockPos2.south())) {
                    arrayList.add(Direction.SOUTH);
                } else if (blockPos2.getZ() > blockPos.getZ() && this.level().isEmptyBlock(blockPos2.north())) {
                    arrayList.add(Direction.NORTH);
                }
            }
            direction = Direction.getRandom(this.random);
            if (arrayList.isEmpty()) {
                for (int i = 5; !this.level().isEmptyBlock(blockPos2.relative(direction)) && i > 0; --i) {
                    direction = Direction.getRandom(this.random);
                }
            } else {
                direction = (Direction)arrayList.get(this.random.nextInt(arrayList.size()));
            }
            d2 = this.getX() + (double)direction.getStepX();
            d3 = this.getY() + (double)direction.getStepY();
            d4 = this.getZ() + (double)direction.getStepZ();
        }
        this.setMoveDirection(direction);
        double d5 = d2 - this.getX();
        double d6 = d3 - this.getY();
        double d7 = d4 - this.getZ();
        double d8 = Math.sqrt(d5 * d5 + d6 * d6 + d7 * d7);
        if (d8 == 0.0) {
            this.targetDeltaX = 0.0;
            this.targetDeltaY = 0.0;
            this.targetDeltaZ = 0.0;
        } else {
            this.targetDeltaX = d5 / d8 * 0.15;
            this.targetDeltaY = d6 / d8 * 0.15;
            this.targetDeltaZ = d7 / d8 * 0.15;
        }
        this.hasImpulse = true;
        this.flightSteps = 10 + this.random.nextInt(5) * 10;
    }

    @Override
    public void checkDespawn() {
        if (this.level().getDifficulty() == Difficulty.PEACEFUL) {
            this.discard();
        }
    }

    @Override
    protected double getDefaultGravity() {
        return 0.04;
    }

    @Override
    public void tick() {
        Vec3 vec3;
        super.tick();
        Entity entity = !this.level().isClientSide() ? EntityReference.get(this.finalTarget, this.level(), Entity.class) : null;
        HitResult hitResult = null;
        if (!this.level().isClientSide) {
            if (entity == null) {
                this.finalTarget = null;
            }
            if (!(entity == null || !entity.isAlive() || entity instanceof Player && entity.isSpectator())) {
                this.targetDeltaX = Mth.clamp(this.targetDeltaX * 1.025, -1.0, 1.0);
                this.targetDeltaY = Mth.clamp(this.targetDeltaY * 1.025, -1.0, 1.0);
                this.targetDeltaZ = Mth.clamp(this.targetDeltaZ * 1.025, -1.0, 1.0);
                vec3 = this.getDeltaMovement();
                this.setDeltaMovement(vec3.add((this.targetDeltaX - vec3.x) * 0.2, (this.targetDeltaY - vec3.y) * 0.2, (this.targetDeltaZ - vec3.z) * 0.2));
            } else {
                this.applyGravity();
            }
            hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        }
        vec3 = this.getDeltaMovement();
        this.setPos(this.position().add(vec3));
        this.applyEffectsFromBlocks();
        if (this.portalProcess != null && this.portalProcess.isInsidePortalThisTick()) {
            this.handlePortal();
        }
        if (hitResult != null && this.isAlive() && hitResult.getType() != HitResult.Type.MISS) {
            this.hitTargetOrDeflectSelf(hitResult);
        }
        ProjectileUtil.rotateTowardsMovement(this, 0.5f);
        if (this.level().isClientSide) {
            this.level().addParticle(ParticleTypes.END_ROD, this.getX() - vec3.x, this.getY() - vec3.y + 0.15, this.getZ() - vec3.z, 0.0, 0.0, 0.0);
        } else if (entity != null) {
            if (this.flightSteps > 0) {
                --this.flightSteps;
                if (this.flightSteps == 0) {
                    this.selectNextMoveDirection(this.currentMoveDirection == null ? null : this.currentMoveDirection.getAxis(), entity);
                }
            }
            if (this.currentMoveDirection != null) {
                BlockPos blockPos = this.blockPosition();
                Direction.Axis axis = this.currentMoveDirection.getAxis();
                if (this.level().loadedAndEntityCanStandOn(blockPos.relative(this.currentMoveDirection), this)) {
                    this.selectNextMoveDirection(axis, entity);
                } else {
                    BlockPos blockPos2 = entity.blockPosition();
                    if (axis == Direction.Axis.X && blockPos.getX() == blockPos2.getX() || axis == Direction.Axis.Z && blockPos.getZ() == blockPos2.getZ() || axis == Direction.Axis.Y && blockPos.getY() == blockPos2.getY()) {
                        this.selectNextMoveDirection(axis, entity);
                    }
                }
            }
        }
    }

    @Override
    protected boolean isAffectedByBlocks() {
        return !this.isRemoved();
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && !entity.noPhysics;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        return d < 16384.0;
    }

    @Override
    public float getLightLevelDependentMagicValue() {
        return 1.0f;
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        Entity entity = entityHitResult.getEntity();
        Entity entity2 = this.getOwner();
        LivingEntity livingEntity = entity2 instanceof LivingEntity ? (LivingEntity)entity2 : null;
        DamageSource damageSource = this.damageSources().mobProjectile(this, livingEntity);
        boolean bl = entity.hurtOrSimulate(damageSource, 4.0f);
        if (bl) {
            Object object;
            Level level = this.level();
            if (level instanceof ServerLevel) {
                object = (ServerLevel)level;
                EnchantmentHelper.doPostAttackEffects((ServerLevel)object, entity, damageSource);
            }
            if (entity instanceof LivingEntity) {
                object = (LivingEntity)entity;
                ((LivingEntity)object).addEffect(new MobEffectInstance(MobEffects.LEVITATION, 200), (Entity)MoreObjects.firstNonNull((Object)entity2, (Object)this));
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        ((ServerLevel)this.level()).sendParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 2, 0.2, 0.2, 0.2, 0.0);
        this.playSound(SoundEvents.SHULKER_BULLET_HIT, 1.0f, 1.0f);
    }

    private void destroy() {
        this.discard();
        this.level().gameEvent(GameEvent.ENTITY_DAMAGE, this.position(), GameEvent.Context.of(this));
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        this.destroy();
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean hurtClient(DamageSource damageSource) {
        return true;
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        this.playSound(SoundEvents.SHULKER_BULLET_HURT, 1.0f, 1.0f);
        serverLevel.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 15, 0.2, 0.2, 0.2, 0.0);
        this.destroy();
        return true;
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket clientboundAddEntityPacket) {
        super.recreateFromPacket(clientboundAddEntityPacket);
        double d = clientboundAddEntityPacket.getXa();
        double d2 = clientboundAddEntityPacket.getYa();
        double d3 = clientboundAddEntityPacket.getZa();
        this.setDeltaMovement(d, d2, d3);
    }
}

