/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.vehicle;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class MinecartTNT
extends AbstractMinecart {
    private static final byte EVENT_PRIME = 10;
    private static final String TAG_EXPLOSION_POWER = "explosion_power";
    private static final String TAG_EXPLOSION_SPEED_FACTOR = "explosion_speed_factor";
    private static final String TAG_FUSE = "fuse";
    private static final float DEFAULT_EXPLOSION_POWER_BASE = 4.0f;
    private static final float DEFAULT_EXPLOSION_SPEED_FACTOR = 1.0f;
    private static final int NO_FUSE = -1;
    @Nullable
    private DamageSource ignitionSource;
    private int fuse = -1;
    private float explosionPowerBase = 4.0f;
    private float explosionSpeedFactor = 1.0f;

    public MinecartTNT(EntityType<? extends MinecartTNT> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public BlockState getDefaultDisplayBlockState() {
        return Blocks.TNT.defaultBlockState();
    }

    @Override
    public void tick() {
        double d;
        super.tick();
        if (this.fuse > 0) {
            --this.fuse;
            this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
        } else if (this.fuse == 0) {
            this.explode(this.ignitionSource, this.getDeltaMovement().horizontalDistanceSqr());
        }
        if (this.horizontalCollision && (d = this.getDeltaMovement().horizontalDistanceSqr()) >= (double)0.01f) {
            this.explode(d);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        AbstractArrow abstractArrow;
        Entity entity = damageSource.getDirectEntity();
        if (entity instanceof AbstractArrow && (abstractArrow = (AbstractArrow)entity).isOnFire()) {
            DamageSource damageSource2 = this.damageSources().explosion(this, damageSource.getEntity());
            this.explode(damageSource2, abstractArrow.getDeltaMovement().lengthSqr());
        }
        return super.hurtServer(serverLevel, damageSource, f);
    }

    @Override
    public void destroy(ServerLevel serverLevel, DamageSource damageSource) {
        double d = this.getDeltaMovement().horizontalDistanceSqr();
        if (MinecartTNT.damageSourceIgnitesTnt(damageSource) || d >= (double)0.01f) {
            if (this.fuse < 0) {
                this.primeFuse(damageSource);
                this.fuse = this.random.nextInt(20) + this.random.nextInt(20);
            }
            return;
        }
        this.destroy(serverLevel, this.getDropItem());
    }

    @Override
    protected Item getDropItem() {
        return Items.TNT_MINECART;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.TNT_MINECART);
    }

    protected void explode(double d) {
        this.explode(null, d);
    }

    protected void explode(@Nullable DamageSource damageSource, double d) {
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (serverLevel.getGameRules().getBoolean(GameRules.RULE_TNT_EXPLODES)) {
                double d2 = Math.min(Math.sqrt(d), 5.0);
                serverLevel.explode(this, damageSource, null, this.getX(), this.getY(), this.getZ(), (float)((double)this.explosionPowerBase + (double)this.explosionSpeedFactor * this.random.nextDouble() * 1.5 * d2), false, Level.ExplosionInteraction.TNT);
                this.discard();
            } else if (this.isPrimed()) {
                this.discard();
            }
        }
    }

    @Override
    public boolean causeFallDamage(double d, float f, DamageSource damageSource) {
        if (d >= 3.0) {
            double d2 = d / 10.0;
            this.explode(d2 * d2);
        }
        return super.causeFallDamage(d, f, damageSource);
    }

    @Override
    public void activateMinecart(int n, int n2, int n3, boolean bl) {
        if (bl && this.fuse < 0) {
            this.primeFuse(null);
        }
    }

    @Override
    public void handleEntityEvent(byte by) {
        if (by == 10) {
            this.primeFuse(null);
        } else {
            super.handleEntityEvent(by);
        }
    }

    public void primeFuse(@Nullable DamageSource damageSource) {
        ServerLevel serverLevel;
        Level level = this.level();
        if (level instanceof ServerLevel && !(serverLevel = (ServerLevel)level).getGameRules().getBoolean(GameRules.RULE_TNT_EXPLODES)) {
            return;
        }
        this.fuse = 80;
        if (!this.level().isClientSide) {
            if (damageSource != null && this.ignitionSource == null) {
                this.ignitionSource = this.damageSources().explosion(this, damageSource.getEntity());
            }
            this.level().broadcastEntityEvent(this, (byte)10);
            if (!this.isSilent()) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
        }
    }

    public int getFuse() {
        return this.fuse;
    }

    public boolean isPrimed() {
        return this.fuse > -1;
    }

    @Override
    public float getBlockExplosionResistance(Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, FluidState fluidState, float f) {
        if (this.isPrimed() && (blockState.is(BlockTags.RAILS) || blockGetter.getBlockState(blockPos.above()).is(BlockTags.RAILS))) {
            return 0.0f;
        }
        return super.getBlockExplosionResistance(explosion, blockGetter, blockPos, blockState, fluidState, f);
    }

    @Override
    public boolean shouldBlockExplode(Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, float f) {
        if (this.isPrimed() && (blockState.is(BlockTags.RAILS) || blockGetter.getBlockState(blockPos.above()).is(BlockTags.RAILS))) {
            return false;
        }
        return super.shouldBlockExplode(explosion, blockGetter, blockPos, blockState, f);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        this.fuse = valueInput.getIntOr(TAG_FUSE, -1);
        this.explosionPowerBase = Mth.clamp(valueInput.getFloatOr(TAG_EXPLOSION_POWER, 4.0f), 0.0f, 128.0f);
        this.explosionSpeedFactor = Mth.clamp(valueInput.getFloatOr(TAG_EXPLOSION_SPEED_FACTOR, 1.0f), 0.0f, 128.0f);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putInt(TAG_FUSE, this.fuse);
        if (this.explosionPowerBase != 4.0f) {
            valueOutput.putFloat(TAG_EXPLOSION_POWER, this.explosionPowerBase);
        }
        if (this.explosionSpeedFactor != 1.0f) {
            valueOutput.putFloat(TAG_EXPLOSION_SPEED_FACTOR, this.explosionSpeedFactor);
        }
    }

    @Override
    boolean shouldSourceDestroy(DamageSource damageSource) {
        return MinecartTNT.damageSourceIgnitesTnt(damageSource);
    }

    private static boolean damageSourceIgnitesTnt(DamageSource damageSource) {
        Entity entity = damageSource.getDirectEntity();
        if (entity instanceof Projectile) {
            Projectile projectile = (Projectile)entity;
            return projectile.isOnFire();
        }
        return damageSource.is(DamageTypeTags.IS_FIRE) || damageSource.is(DamageTypeTags.IS_EXPLOSION);
    }
}

