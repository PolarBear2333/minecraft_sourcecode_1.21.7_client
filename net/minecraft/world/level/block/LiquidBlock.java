/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LiquidBlock
extends Block
implements BucketPickup {
    private static final Codec<FlowingFluid> FLOWING_FLUID = BuiltInRegistries.FLUID.byNameCodec().comapFlatMap(fluid -> {
        DataResult dataResult;
        if (fluid instanceof FlowingFluid) {
            FlowingFluid flowingFluid = (FlowingFluid)fluid;
            dataResult = DataResult.success((Object)flowingFluid);
        } else {
            dataResult = DataResult.error(() -> "Not a flowing fluid: " + String.valueOf(fluid));
        }
        return dataResult;
    }, flowingFluid -> flowingFluid);
    public static final MapCodec<LiquidBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)FLOWING_FLUID.fieldOf("fluid").forGetter(liquidBlock -> liquidBlock.fluid), LiquidBlock.propertiesCodec()).apply((Applicative)instance, LiquidBlock::new));
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL;
    protected final FlowingFluid fluid;
    private final List<FluidState> stateCache;
    public static final VoxelShape SHAPE_STABLE = Block.column(16.0, 0.0, 8.0);
    public static final ImmutableList<Direction> POSSIBLE_FLOW_DIRECTIONS = ImmutableList.of((Object)Direction.DOWN, (Object)Direction.SOUTH, (Object)Direction.NORTH, (Object)Direction.EAST, (Object)Direction.WEST);

    public MapCodec<LiquidBlock> codec() {
        return CODEC;
    }

    protected LiquidBlock(FlowingFluid flowingFluid, BlockBehaviour.Properties properties) {
        super(properties);
        this.fluid = flowingFluid;
        this.stateCache = Lists.newArrayList();
        this.stateCache.add(flowingFluid.getSource(false));
        for (int i = 1; i < 8; ++i) {
            this.stateCache.add(flowingFluid.getFlowing(8 - i, false));
        }
        this.stateCache.add(flowingFluid.getFlowing(8, true));
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(LEVEL, 0));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        if (collisionContext.isAbove(SHAPE_STABLE, blockPos, true) && blockState.getValue(LEVEL) == 0 && collisionContext.canStandOnFluid(blockGetter.getFluidState(blockPos.above()), blockState.getFluidState())) {
            return SHAPE_STABLE;
        }
        return Shapes.empty();
    }

    @Override
    protected boolean isRandomlyTicking(BlockState blockState) {
        return blockState.getFluidState().isRandomlyTicking();
    }

    @Override
    protected void randomTick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        blockState.getFluidState().randomTick(serverLevel, blockPos, randomSource);
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState blockState) {
        return false;
    }

    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return !this.fluid.is(FluidTags.LAVA);
    }

    @Override
    protected FluidState getFluidState(BlockState blockState) {
        int n = blockState.getValue(LEVEL);
        return this.stateCache.get(Math.min(n, 8));
    }

    @Override
    protected boolean skipRendering(BlockState blockState, BlockState blockState2, Direction direction) {
        return blockState2.getFluidState().getType().isSame(this.fluid);
    }

    @Override
    protected RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState blockState, LootParams.Builder builder) {
        return Collections.emptyList();
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return Shapes.empty();
    }

    @Override
    protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (this.shouldSpreadLiquid(level, blockPos, blockState)) {
            level.scheduleTick(blockPos, blockState.getFluidState().getType(), this.fluid.getTickDelay(level));
        }
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (blockState.getFluidState().isSource() || blockState2.getFluidState().isSource()) {
            scheduledTickAccess.scheduleTick(blockPos, blockState.getFluidState().getType(), this.fluid.getTickDelay(levelReader));
        }
        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, @Nullable Orientation orientation, boolean bl) {
        if (this.shouldSpreadLiquid(level, blockPos, blockState)) {
            level.scheduleTick(blockPos, blockState.getFluidState().getType(), this.fluid.getTickDelay(level));
        }
    }

    private boolean shouldSpreadLiquid(Level level, BlockPos blockPos, BlockState blockState) {
        if (this.fluid.is(FluidTags.LAVA)) {
            boolean bl = level.getBlockState(blockPos.below()).is(Blocks.SOUL_SOIL);
            for (Direction direction : POSSIBLE_FLOW_DIRECTIONS) {
                BlockPos blockPos2 = blockPos.relative(direction.getOpposite());
                if (level.getFluidState(blockPos2).is(FluidTags.WATER)) {
                    Block block = level.getFluidState(blockPos).isSource() ? Blocks.OBSIDIAN : Blocks.COBBLESTONE;
                    level.setBlockAndUpdate(blockPos, block.defaultBlockState());
                    this.fizz(level, blockPos);
                    return false;
                }
                if (!bl || !level.getBlockState(blockPos2).is(Blocks.BLUE_ICE)) continue;
                level.setBlockAndUpdate(blockPos, Blocks.BASALT.defaultBlockState());
                this.fizz(level, blockPos);
                return false;
            }
        }
        return true;
    }

    private void fizz(LevelAccessor levelAccessor, BlockPos blockPos) {
        levelAccessor.levelEvent(1501, blockPos, 0);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    public ItemStack pickupBlock(@Nullable LivingEntity livingEntity, LevelAccessor levelAccessor, BlockPos blockPos, BlockState blockState) {
        if (blockState.getValue(LEVEL) == 0) {
            levelAccessor.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 11);
            return new ItemStack(this.fluid.getBucket());
        }
        return ItemStack.EMPTY;
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return this.fluid.getPickupSound();
    }
}

