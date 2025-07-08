/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SculkShriekerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SculkShriekerBlock
extends BaseEntityBlock
implements SimpleWaterloggedBlock {
    public static final MapCodec<SculkShriekerBlock> CODEC = SculkShriekerBlock.simpleCodec(SculkShriekerBlock::new);
    public static final BooleanProperty SHRIEKING = BlockStateProperties.SHRIEKING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty CAN_SUMMON = BlockStateProperties.CAN_SUMMON;
    private static final VoxelShape SHAPE_COLLISION = Block.column(16.0, 0.0, 8.0);
    public static final double TOP_Y = SHAPE_COLLISION.max(Direction.Axis.Y);

    public MapCodec<SculkShriekerBlock> codec() {
        return CODEC;
    }

    public SculkShriekerBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(SHRIEKING, false)).setValue(WATERLOGGED, false)).setValue(CAN_SUMMON, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SHRIEKING);
        builder.add(WATERLOGGED);
        builder.add(CAN_SUMMON);
    }

    @Override
    public void stepOn(Level level, BlockPos blockPos, BlockState blockState, Entity entity) {
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            ServerPlayer serverPlayer = SculkShriekerBlockEntity.tryGetPlayer(entity);
            if (serverPlayer != null) {
                serverLevel.getBlockEntity(blockPos, BlockEntityType.SCULK_SHRIEKER).ifPresent(sculkShriekerBlockEntity -> sculkShriekerBlockEntity.tryShriek(serverLevel, serverPlayer));
            }
        }
        super.stepOn(level, blockPos, blockState, entity);
    }

    @Override
    protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (blockState.getValue(SHRIEKING).booleanValue()) {
            serverLevel.setBlock(blockPos, (BlockState)blockState.setValue(SHRIEKING, false), 3);
            serverLevel.getBlockEntity(blockPos, BlockEntityType.SCULK_SHRIEKER).ifPresent(sculkShriekerBlockEntity -> sculkShriekerBlockEntity.tryRespond(serverLevel));
        }
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE_COLLISION;
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState blockState) {
        return SHAPE_COLLISION;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState blockState) {
        return true;
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new SculkShriekerBlockEntity(blockPos, blockState);
    }

    @Override
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
        }
        return super.updateShape(blockState, levelReader, scheduledTickAccess, blockPos, direction, blockPos2, blockState2, randomSource);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return (BlockState)this.defaultBlockState().setValue(WATERLOGGED, blockPlaceContext.getLevel().getFluidState(blockPlaceContext.getClickedPos()).getType() == Fluids.WATER);
    }

    @Override
    protected FluidState getFluidState(BlockState blockState) {
        if (blockState.getValue(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getSource(false);
        }
        return super.getFluidState(blockState);
    }

    @Override
    protected void spawnAfterBreak(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack, boolean bl) {
        super.spawnAfterBreak(blockState, serverLevel, blockPos, itemStack, bl);
        if (bl) {
            this.tryDropExperience(serverLevel, blockPos, itemStack, ConstantInt.of(5));
        }
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level2, BlockState blockState2, BlockEntityType<T> blockEntityType) {
        if (!level2.isClientSide) {
            return BaseEntityBlock.createTickerHelper(blockEntityType, BlockEntityType.SCULK_SHRIEKER, (level, blockPos, blockState, sculkShriekerBlockEntity) -> VibrationSystem.Ticker.tick(level, sculkShriekerBlockEntity.getVibrationData(), sculkShriekerBlockEntity.getVibrationUser()));
        }
        return null;
    }
}

