/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.material;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.InsideBlockEffectType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public abstract class LavaFluid
extends FlowingFluid {
    public static final float MIN_LEVEL_CUTOFF = 0.44444445f;

    @Override
    public Fluid getFlowing() {
        return Fluids.FLOWING_LAVA;
    }

    @Override
    public Fluid getSource() {
        return Fluids.LAVA;
    }

    @Override
    public Item getBucket() {
        return Items.LAVA_BUCKET;
    }

    @Override
    public void animateTick(Level level, BlockPos blockPos, FluidState fluidState, RandomSource randomSource) {
        BlockPos blockPos2 = blockPos.above();
        if (level.getBlockState(blockPos2).isAir() && !level.getBlockState(blockPos2).isSolidRender()) {
            if (randomSource.nextInt(100) == 0) {
                double d = (double)blockPos.getX() + randomSource.nextDouble();
                double d2 = (double)blockPos.getY() + 1.0;
                double d3 = (double)blockPos.getZ() + randomSource.nextDouble();
                level.addParticle(ParticleTypes.LAVA, d, d2, d3, 0.0, 0.0, 0.0);
                level.playLocalSound(d, d2, d3, SoundEvents.LAVA_POP, SoundSource.AMBIENT, 0.2f + randomSource.nextFloat() * 0.2f, 0.9f + randomSource.nextFloat() * 0.15f, false);
            }
            if (randomSource.nextInt(200) == 0) {
                level.playLocalSound(blockPos.getX(), blockPos.getY(), blockPos.getZ(), SoundEvents.LAVA_AMBIENT, SoundSource.AMBIENT, 0.2f + randomSource.nextFloat() * 0.2f, 0.9f + randomSource.nextFloat() * 0.15f, false);
            }
        }
    }

    @Override
    public void randomTick(ServerLevel serverLevel, BlockPos blockPos, FluidState fluidState, RandomSource randomSource) {
        if (!serverLevel.getGameRules().getBoolean(GameRules.RULE_DOFIRETICK)) {
            return;
        }
        if (!serverLevel.getGameRules().getBoolean(GameRules.RULE_ALLOWFIRETICKAWAYFROMPLAYERS) && !serverLevel.anyPlayerCloseEnoughForSpawning(blockPos)) {
            return;
        }
        int n = randomSource.nextInt(3);
        if (n > 0) {
            BlockPos blockPos2 = blockPos;
            for (int i = 0; i < n; ++i) {
                if (!serverLevel.isLoaded(blockPos2 = blockPos2.offset(randomSource.nextInt(3) - 1, 1, randomSource.nextInt(3) - 1))) {
                    return;
                }
                BlockState blockState = serverLevel.getBlockState(blockPos2);
                if (blockState.isAir()) {
                    if (!this.hasFlammableNeighbours(serverLevel, blockPos2)) continue;
                    serverLevel.setBlockAndUpdate(blockPos2, BaseFireBlock.getState(serverLevel, blockPos2));
                    return;
                }
                if (!blockState.blocksMotion()) continue;
                return;
            }
        } else {
            for (int i = 0; i < 3; ++i) {
                BlockPos blockPos3 = blockPos.offset(randomSource.nextInt(3) - 1, 0, randomSource.nextInt(3) - 1);
                if (!serverLevel.isLoaded(blockPos3)) {
                    return;
                }
                if (!serverLevel.isEmptyBlock(blockPos3.above()) || !this.isFlammable(serverLevel, blockPos3)) continue;
                serverLevel.setBlockAndUpdate(blockPos3.above(), BaseFireBlock.getState(serverLevel, blockPos3));
            }
        }
    }

    @Override
    protected void entityInside(Level level, BlockPos blockPos, Entity entity, InsideBlockEffectApplier insideBlockEffectApplier) {
        insideBlockEffectApplier.apply(InsideBlockEffectType.LAVA_IGNITE);
        insideBlockEffectApplier.runAfter(InsideBlockEffectType.LAVA_IGNITE, Entity::lavaHurt);
    }

    private boolean hasFlammableNeighbours(LevelReader levelReader, BlockPos blockPos) {
        for (Direction direction : Direction.values()) {
            if (!this.isFlammable(levelReader, blockPos.relative(direction))) continue;
            return true;
        }
        return false;
    }

    private boolean isFlammable(LevelReader levelReader, BlockPos blockPos) {
        if (levelReader.isInsideBuildHeight(blockPos.getY()) && !levelReader.hasChunkAt(blockPos)) {
            return false;
        }
        return levelReader.getBlockState(blockPos).ignitedByLava();
    }

    @Override
    @Nullable
    public ParticleOptions getDripParticle() {
        return ParticleTypes.DRIPPING_LAVA;
    }

    @Override
    protected void beforeDestroyingBlock(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
        this.fizz(levelAccessor, blockPos);
    }

    @Override
    public int getSlopeFindDistance(LevelReader levelReader) {
        return levelReader.dimensionType().ultraWarm() ? 4 : 2;
    }

    @Override
    public BlockState createLegacyBlock(FluidState fluidState) {
        return (BlockState)Blocks.LAVA.defaultBlockState().setValue(LiquidBlock.LEVEL, LavaFluid.getLegacyLevel(fluidState));
    }

    @Override
    public boolean isSame(Fluid fluid) {
        return fluid == Fluids.LAVA || fluid == Fluids.FLOWING_LAVA;
    }

    @Override
    public int getDropOff(LevelReader levelReader) {
        return levelReader.dimensionType().ultraWarm() ? 1 : 2;
    }

    @Override
    public boolean canBeReplacedWith(FluidState fluidState, BlockGetter blockGetter, BlockPos blockPos, Fluid fluid, Direction direction) {
        return fluidState.getHeight(blockGetter, blockPos) >= 0.44444445f && fluid.is(FluidTags.WATER);
    }

    @Override
    public int getTickDelay(LevelReader levelReader) {
        return levelReader.dimensionType().ultraWarm() ? 10 : 30;
    }

    @Override
    public int getSpreadDelay(Level level, BlockPos blockPos, FluidState fluidState, FluidState fluidState2) {
        int n = this.getTickDelay(level);
        if (!(fluidState.isEmpty() || fluidState2.isEmpty() || fluidState.getValue(FALLING).booleanValue() || fluidState2.getValue(FALLING).booleanValue() || !(fluidState2.getHeight(level, blockPos) > fluidState.getHeight(level, blockPos)) || level.getRandom().nextInt(4) == 0)) {
            n *= 4;
        }
        return n;
    }

    private void fizz(LevelAccessor levelAccessor, BlockPos blockPos) {
        levelAccessor.levelEvent(1501, blockPos, 0);
    }

    @Override
    protected boolean canConvertToSource(ServerLevel serverLevel) {
        return serverLevel.getGameRules().getBoolean(GameRules.RULE_LAVA_SOURCE_CONVERSION);
    }

    @Override
    protected void spreadTo(LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState, Direction direction, FluidState fluidState) {
        if (direction == Direction.DOWN) {
            FluidState fluidState2 = levelAccessor.getFluidState(blockPos);
            if (this.is(FluidTags.LAVA) && fluidState2.is(FluidTags.WATER)) {
                if (blockState.getBlock() instanceof LiquidBlock) {
                    levelAccessor.setBlock(blockPos, Blocks.STONE.defaultBlockState(), 3);
                }
                this.fizz(levelAccessor, blockPos);
                return;
            }
        }
        super.spreadTo(levelAccessor, blockPos, blockState, direction, fluidState);
    }

    @Override
    protected boolean isRandomlyTicking() {
        return true;
    }

    @Override
    protected float getExplosionResistance() {
        return 100.0f;
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return Optional.of(SoundEvents.BUCKET_FILL_LAVA);
    }

    public static class Flowing
    extends LavaFluid {
        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState fluidState) {
            return fluidState.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState fluidState) {
            return false;
        }
    }

    public static class Source
    extends LavaFluid {
        @Override
        public int getAmount(FluidState fluidState) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState fluidState) {
            return true;
        }
    }
}

