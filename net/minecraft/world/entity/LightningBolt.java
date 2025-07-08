/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class LightningBolt
extends Entity {
    private static final int START_LIFE = 2;
    private static final double DAMAGE_RADIUS = 3.0;
    private static final double DETECTION_RADIUS = 15.0;
    private int life = 2;
    public long seed;
    private int flashes;
    private boolean visualOnly;
    @Nullable
    private ServerPlayer cause;
    private final Set<Entity> hitEntities = Sets.newHashSet();
    private int blocksSetOnFire;

    public LightningBolt(EntityType<? extends LightningBolt> entityType, Level level) {
        super(entityType, level);
        this.seed = this.random.nextLong();
        this.flashes = this.random.nextInt(3) + 1;
    }

    public void setVisualOnly(boolean bl) {
        this.visualOnly = bl;
    }

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.WEATHER;
    }

    @Nullable
    public ServerPlayer getCause() {
        return this.cause;
    }

    public void setCause(@Nullable ServerPlayer serverPlayer) {
        this.cause = serverPlayer;
    }

    private void powerLightningRod() {
        BlockPos blockPos = this.getStrikePosition();
        BlockState blockState = this.level().getBlockState(blockPos);
        if (blockState.is(Blocks.LIGHTNING_ROD)) {
            ((LightningRodBlock)blockState.getBlock()).onLightningStrike(blockState, this.level(), blockPos);
        }
    }

    @Override
    public void tick() {
        Object object;
        super.tick();
        if (this.life == 2) {
            if (this.level().isClientSide()) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 10000.0f, 0.8f + this.random.nextFloat() * 0.2f, false);
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.WEATHER, 2.0f, 0.5f + this.random.nextFloat() * 0.2f, false);
            } else {
                object = this.level().getDifficulty();
                if (object == Difficulty.NORMAL || object == Difficulty.HARD) {
                    this.spawnFire(4);
                }
                this.powerLightningRod();
                LightningBolt.clearCopperOnLightningStrike(this.level(), this.getStrikePosition());
                this.gameEvent(GameEvent.LIGHTNING_STRIKE);
            }
        }
        --this.life;
        if (this.life < 0) {
            if (this.flashes == 0) {
                if (this.level() instanceof ServerLevel) {
                    object = this.level().getEntities(this, new AABB(this.getX() - 15.0, this.getY() - 15.0, this.getZ() - 15.0, this.getX() + 15.0, this.getY() + 6.0 + 15.0, this.getZ() + 15.0), entity -> entity.isAlive() && !this.hitEntities.contains(entity));
                    for (ServerPlayer entity2 : ((ServerLevel)this.level()).getPlayers(serverPlayer -> serverPlayer.distanceTo(this) < 256.0f)) {
                        CriteriaTriggers.LIGHTNING_STRIKE.trigger(entity2, this, (List<Entity>)object);
                    }
                }
                this.discard();
            } else if (this.life < -this.random.nextInt(10)) {
                --this.flashes;
                this.life = 1;
                this.seed = this.random.nextLong();
                this.spawnFire(0);
            }
        }
        if (this.life >= 0) {
            if (!(this.level() instanceof ServerLevel)) {
                this.level().setSkyFlashTime(2);
            } else if (!this.visualOnly) {
                object = this.level().getEntities(this, new AABB(this.getX() - 3.0, this.getY() - 3.0, this.getZ() - 3.0, this.getX() + 3.0, this.getY() + 6.0 + 3.0, this.getZ() + 3.0), Entity::isAlive);
                Iterator<Entity> iterator = object.iterator();
                while (iterator.hasNext()) {
                    Entity entity2 = iterator.next();
                    entity2.thunderHit((ServerLevel)this.level(), this);
                }
                this.hitEntities.addAll((Collection<Entity>)object);
                if (this.cause != null) {
                    CriteriaTriggers.CHANNELED_LIGHTNING.trigger(this.cause, (Collection<? extends Entity>)object);
                }
            }
        }
    }

    private BlockPos getStrikePosition() {
        Vec3 vec3 = this.position();
        return BlockPos.containing(vec3.x, vec3.y - 1.0E-6, vec3.z);
    }

    private void spawnFire(int n) {
        ServerLevel serverLevel;
        Object object;
        if (this.visualOnly || !((object = this.level()) instanceof ServerLevel) || !(serverLevel = (ServerLevel)object).getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
            return;
        }
        object = this.blockPosition();
        BlockState blockState = BaseFireBlock.getState(this.level(), (BlockPos)object);
        if (this.level().getBlockState((BlockPos)object).isAir() && blockState.canSurvive(this.level(), (BlockPos)object)) {
            this.level().setBlockAndUpdate((BlockPos)object, blockState);
            ++this.blocksSetOnFire;
        }
        for (int i = 0; i < n; ++i) {
            BlockPos blockPos = ((BlockPos)object).offset(this.random.nextInt(3) - 1, this.random.nextInt(3) - 1, this.random.nextInt(3) - 1);
            blockState = BaseFireBlock.getState(this.level(), blockPos);
            if (!this.level().getBlockState(blockPos).isAir() || !blockState.canSurvive(this.level(), blockPos)) continue;
            this.level().setBlockAndUpdate(blockPos, blockState);
            ++this.blocksSetOnFire;
        }
    }

    private static void clearCopperOnLightningStrike(Level level, BlockPos blockPos) {
        BlockState blockState;
        BlockPos blockPos2;
        BlockState blockState2 = level.getBlockState(blockPos);
        if (blockState2.is(Blocks.LIGHTNING_ROD)) {
            blockPos2 = blockPos.relative(((Direction)blockState2.getValue(LightningRodBlock.FACING)).getOpposite());
            blockState = level.getBlockState(blockPos2);
        } else {
            blockPos2 = blockPos;
            blockState = blockState2;
        }
        if (!(blockState.getBlock() instanceof WeatheringCopper)) {
            return;
        }
        level.setBlockAndUpdate(blockPos2, WeatheringCopper.getFirst(level.getBlockState(blockPos2)));
        BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable();
        int n = level.random.nextInt(3) + 3;
        for (int i = 0; i < n; ++i) {
            int n2 = level.random.nextInt(8) + 1;
            LightningBolt.randomWalkCleaningCopper(level, blockPos2, mutableBlockPos, n2);
        }
    }

    private static void randomWalkCleaningCopper(Level level, BlockPos blockPos, BlockPos.MutableBlockPos mutableBlockPos, int n) {
        Optional<BlockPos> optional;
        mutableBlockPos.set(blockPos);
        for (int i = 0; i < n && !(optional = LightningBolt.randomStepCleaningCopper(level, mutableBlockPos)).isEmpty(); ++i) {
            mutableBlockPos.set(optional.get());
        }
    }

    private static Optional<BlockPos> randomStepCleaningCopper(Level level, BlockPos blockPos) {
        for (BlockPos blockPos2 : BlockPos.randomInCube(level.random, 10, blockPos, 1)) {
            BlockState blockState2 = level.getBlockState(blockPos2);
            if (!(blockState2.getBlock() instanceof WeatheringCopper)) continue;
            WeatheringCopper.getPrevious(blockState2).ifPresent(blockState -> level.setBlockAndUpdate(blockPos2, (BlockState)blockState));
            level.levelEvent(3002, blockPos2, -1);
            return Optional.of(blockPos2);
        }
        return Optional.empty();
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double d) {
        double d2 = 64.0 * LightningBolt.getViewScale();
        return d < d2 * d2;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
    }

    public int getBlocksSetOnFire() {
        return this.blocksSetOnFire;
    }

    public Stream<Entity> getHitEntities() {
        return this.hitEntities.stream().filter(Entity::isAlive);
    }

    @Override
    public final boolean hurtServer(ServerLevel serverLevel, DamageSource damageSource, float f) {
        return false;
    }
}

