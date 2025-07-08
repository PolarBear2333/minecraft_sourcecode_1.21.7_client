/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.item;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class PrimedTnt
extends Entity
implements TraceableEntity {
    private static final EntityDataAccessor<Integer> DATA_FUSE_ID = SynchedEntityData.defineId(PrimedTnt.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<BlockState> DATA_BLOCK_STATE_ID = SynchedEntityData.defineId(PrimedTnt.class, EntityDataSerializers.BLOCK_STATE);
    private static final short DEFAULT_FUSE_TIME = 80;
    private static final float DEFAULT_EXPLOSION_POWER = 4.0f;
    private static final BlockState DEFAULT_BLOCK_STATE = Blocks.TNT.defaultBlockState();
    private static final String TAG_BLOCK_STATE = "block_state";
    public static final String TAG_FUSE = "fuse";
    private static final String TAG_EXPLOSION_POWER = "explosion_power";
    private static final ExplosionDamageCalculator USED_PORTAL_DAMAGE_CALCULATOR = new ExplosionDamageCalculator(){

        @Override
        public boolean shouldBlockExplode(Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, float f) {
            if (blockState.is(Blocks.NETHER_PORTAL)) {
                return false;
            }
            return super.shouldBlockExplode(explosion, blockGetter, blockPos, blockState, f);
        }

        @Override
        public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
            if (blockState.is(Blocks.NETHER_PORTAL)) {
                return Optional.empty();
            }
            return super.getBlockExplosionResistance(explosion, blockGetter, blockPos, blockState, fluidState);
        }
    };
    @Nullable
    private EntityReference<LivingEntity> owner;
    private boolean usedPortal;
    private float explosionPower = 4.0f;

    public PrimedTnt(EntityType<? extends PrimedTnt> entityType, Level level) {
        super(entityType, level);
        this.blocksBuilding = true;
    }

    public PrimedTnt(Level level, double d, double d2, double d3, @Nullable LivingEntity livingEntity) {
        this((EntityType<? extends PrimedTnt>)EntityType.TNT, level);
        this.setPos(d, d2, d3);
        double d4 = level.random.nextDouble() * 6.2831854820251465;
        this.setDeltaMovement(-Math.sin(d4) * 0.02, 0.2f, -Math.cos(d4) * 0.02);
        this.setFuse(80);
        this.xo = d;
        this.yo = d2;
        this.zo = d3;
        this.owner = livingEntity != null ? new EntityReference<LivingEntity>(livingEntity) : null;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_FUSE_ID, 80);
        builder.define(DATA_BLOCK_STATE_ID, DEFAULT_BLOCK_STATE);
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    public boolean isPickable() {
        return !this.isRemoved();
    }

    @Override
    protected double getDefaultGravity() {
        return 0.04;
    }

    @Override
    public void tick() {
        this.handlePortal();
        this.applyGravity();
        this.move(MoverType.SELF, this.getDeltaMovement());
        this.applyEffectsFromBlocks();
        this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
        if (this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.7, -0.5, 0.7));
        }
        int n = this.getFuse() - 1;
        this.setFuse(n);
        if (n <= 0) {
            this.discard();
            if (!this.level().isClientSide) {
                this.explode();
            }
        } else {
            this.updateInWaterStateAndDoFluidPushing();
            if (this.level().isClientSide) {
                this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }

    private void explode() {
        ServerLevel serverLevel;
        Level level = this.level();
        if (level instanceof ServerLevel && (serverLevel = (ServerLevel)level).getGameRules().getBoolean(GameRules.RULE_TNT_EXPLODES)) {
            this.level().explode(this, Explosion.getDefaultDamageSource(this.level(), this), this.usedPortal ? USED_PORTAL_DAMAGE_CALCULATOR : null, this.getX(), this.getY(0.0625), this.getZ(), this.explosionPower, false, Level.ExplosionInteraction.TNT);
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        valueOutput.putShort(TAG_FUSE, (short)this.getFuse());
        valueOutput.store(TAG_BLOCK_STATE, BlockState.CODEC, this.getBlockState());
        if (this.explosionPower != 4.0f) {
            valueOutput.putFloat(TAG_EXPLOSION_POWER, this.explosionPower);
        }
        EntityReference.store(this.owner, valueOutput, "owner");
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        this.setFuse(valueInput.getShortOr(TAG_FUSE, (short)80));
        this.setBlockState(valueInput.read(TAG_BLOCK_STATE, BlockState.CODEC).orElse(DEFAULT_BLOCK_STATE));
        this.explosionPower = Mth.clamp(valueInput.getFloatOr(TAG_EXPLOSION_POWER, 4.0f), 0.0f, 128.0f);
        this.owner = EntityReference.read(valueInput, "owner");
    }

    @Override
    @Nullable
    public LivingEntity getOwner() {
        return EntityReference.get(this.owner, this.level(), LivingEntity.class);
    }

    @Override
    public void restoreFrom(Entity entity) {
        super.restoreFrom(entity);
        if (entity instanceof PrimedTnt) {
            PrimedTnt primedTnt = (PrimedTnt)entity;
            this.owner = primedTnt.owner;
        }
    }

    public void setFuse(int n) {
        this.entityData.set(DATA_FUSE_ID, n);
    }

    public int getFuse() {
        return this.entityData.get(DATA_FUSE_ID);
    }

    public void setBlockState(BlockState blockState) {
        this.entityData.set(DATA_BLOCK_STATE_ID, blockState);
    }

    public BlockState getBlockState() {
        return this.entityData.get(DATA_BLOCK_STATE_ID);
    }

    private void setUsedPortal(boolean bl) {
        this.usedPortal = bl;
    }

    @Override
    @Nullable
    public Entity teleport(TeleportTransition teleportTransition) {
        Entity entity = super.teleport(teleportTransition);
        if (entity instanceof PrimedTnt) {
            PrimedTnt primedTnt = (PrimedTnt)entity;
            primedTnt.setUsedPortal(true);
        }
        return entity;
    }

    @Override
    public final boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        return false;
    }

    @Override
    @Nullable
    public /* synthetic */ Entity getOwner() {
        return this.getOwner();
    }
}

