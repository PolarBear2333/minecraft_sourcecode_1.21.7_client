/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.boss.enderdragon;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class EndCrystal
extends Entity {
    private static final EntityDataAccessor<Optional<BlockPos>> DATA_BEAM_TARGET = SynchedEntityData.defineId(EndCrystal.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);
    private static final EntityDataAccessor<Boolean> DATA_SHOW_BOTTOM = SynchedEntityData.defineId(EndCrystal.class, EntityDataSerializers.BOOLEAN);
    private static final boolean DEFAULT_SHOW_BOTTOM = true;
    public int time;

    public EndCrystal(EntityType<? extends EndCrystal> entityType, Level level) {
        super(entityType, level);
        this.blocksBuilding = true;
        this.time = this.random.nextInt(100000);
    }

    public EndCrystal(Level level, double d, double d2, double d3) {
        this((EntityType<? extends EndCrystal>)EntityType.END_CRYSTAL, level);
        this.setPos(d, d2, d3);
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_BEAM_TARGET, Optional.empty());
        builder.define(DATA_SHOW_BOTTOM, true);
    }

    @Override
    public void tick() {
        ++this.time;
        this.applyEffectsFromBlocks();
        this.handlePortal();
        if (this.level() instanceof ServerLevel) {
            BlockPos blockPos = this.blockPosition();
            if (((ServerLevel)this.level()).getDragonFight() != null && this.level().getBlockState(blockPos).isAir()) {
                this.level().setBlockAndUpdate(blockPos, BaseFireBlock.getState(this.level(), blockPos));
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        valueOutput.storeNullable("beam_target", BlockPos.CODEC, this.getBeamTarget());
        valueOutput.putBoolean("ShowBottom", this.showsBottom());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        this.setBeamTarget(valueInput.read("beam_target", BlockPos.CODEC).orElse(null));
        this.setShowBottom(valueInput.getBooleanOr("ShowBottom", true));
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public final boolean hurtClient(DamageSource damageSource) {
        if (this.isInvulnerableToBase(damageSource)) {
            return false;
        }
        return !(damageSource.getEntity() instanceof EnderDragon);
    }

    @Override
    public final boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        if (this.isInvulnerableToBase(damageSource)) {
            return false;
        }
        if (damageSource.getEntity() instanceof EnderDragon) {
            return false;
        }
        if (!this.isRemoved()) {
            this.remove(Entity.RemovalReason.KILLED);
            if (!damageSource.is(DamageTypeTags.IS_EXPLOSION)) {
                DamageSource damageSource2 = damageSource.getEntity() != null ? this.damageSources().explosion(this, damageSource.getEntity()) : null;
                serverLevel.explode(this, damageSource2, null, this.getX(), this.getY(), this.getZ(), 6.0f, false, Level.ExplosionInteraction.BLOCK);
            }
            this.onDestroyedBy(serverLevel, damageSource);
        }
        return true;
    }

    @Override
    public void kill(ServerLevel serverLevel) {
        this.onDestroyedBy(serverLevel, this.damageSources().generic());
        super.kill(serverLevel);
    }

    private void onDestroyedBy(ServerLevel serverLevel, DamageSource damageSource) {
        EndDragonFight endDragonFight = serverLevel.getDragonFight();
        if (endDragonFight != null) {
            endDragonFight.onCrystalDestroyed(this, damageSource);
        }
    }

    public void setBeamTarget(@Nullable BlockPos blockPos) {
        this.getEntityData().set(DATA_BEAM_TARGET, Optional.ofNullable(blockPos));
    }

    @Nullable
    public BlockPos getBeamTarget() {
        return this.getEntityData().get(DATA_BEAM_TARGET).orElse(null);
    }

    public void setShowBottom(boolean bl) {
        this.getEntityData().set(DATA_SHOW_BOTTOM, bl);
    }

    public boolean showsBottom() {
        return this.getEntityData().get(DATA_SHOW_BOTTOM);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        return super.shouldRenderAtSqrDistance(d) || this.getBeamTarget() != null;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.END_CRYSTAL);
    }
}

