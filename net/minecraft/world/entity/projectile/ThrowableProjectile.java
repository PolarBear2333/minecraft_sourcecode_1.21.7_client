/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.projectile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public abstract class ThrowableProjectile
extends Projectile {
    private static final float MIN_CAMERA_DISTANCE_SQUARED = 12.25f;

    protected ThrowableProjectile(EntityType<? extends ThrowableProjectile> entityType, Level level) {
        super((EntityType<? extends Projectile>)entityType, level);
    }

    protected ThrowableProjectile(EntityType<? extends ThrowableProjectile> entityType, double d, double d2, double d3, Level level) {
        this(entityType, level);
        this.setPos(d, d2, d3);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        if (this.tickCount < 2 && d < 12.25) {
            return false;
        }
        double d2 = this.getBoundingBox().getSize() * 4.0;
        if (Double.isNaN(d2)) {
            d2 = 4.0;
        }
        return d < (d2 *= 64.0) * d2;
    }

    @Override
    public boolean canUsePortal(boolean bl) {
        return true;
    }

    @Override
    public void tick() {
        this.handleFirstTickBubbleColumn();
        this.applyGravity();
        this.applyInertia();
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        Vec3 vec3 = hitResult.getType() != HitResult.Type.MISS ? hitResult.getLocation() : this.position().add(this.getDeltaMovement());
        this.setPos(vec3);
        this.updateRotation();
        this.applyEffectsFromBlocks();
        super.tick();
        if (hitResult.getType() != HitResult.Type.MISS && this.isAlive()) {
            this.hitTargetOrDeflectSelf(hitResult);
        }
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
            f = 0.8f;
        } else {
            f = 0.99f;
        }
        this.setDeltaMovement(vec3.scale(f));
    }

    private void handleFirstTickBubbleColumn() {
        if (this.firstTick) {
            for (BlockPos blockPos : BlockPos.betweenClosed(this.getBoundingBox())) {
                BlockState blockState = this.level().getBlockState(blockPos);
                if (!blockState.is(Blocks.BUBBLE_COLUMN)) continue;
                blockState.entityInside(this.level(), blockPos, this, InsideBlockEffectApplier.NOOP);
            }
        }
    }

    @Override
    protected double getDefaultGravity() {
        return 0.03;
    }
}

