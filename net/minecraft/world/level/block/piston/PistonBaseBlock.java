/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block.piston;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.piston.PistonMovingBlockEntity;
import net.minecraft.world.level.block.piston.PistonStructureResolver;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.ExperimentalRedstoneUtils;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PistonBaseBlock
extends DirectionalBlock {
    public static final MapCodec<PistonBaseBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.BOOL.fieldOf("sticky").forGetter(pistonBaseBlock -> pistonBaseBlock.isSticky), PistonBaseBlock.propertiesCodec()).apply((Applicative)instance, PistonBaseBlock::new));
    public static final BooleanProperty EXTENDED = BlockStateProperties.EXTENDED;
    public static final int TRIGGER_EXTEND = 0;
    public static final int TRIGGER_CONTRACT = 1;
    public static final int TRIGGER_DROP = 2;
    public static final int PLATFORM_THICKNESS = 4;
    private static final Map<Direction, VoxelShape> SHAPES = Shapes.rotateAll(Block.boxZ(16.0, 4.0, 16.0));
    private final boolean isSticky;

    public MapCodec<PistonBaseBlock> codec() {
        return CODEC;
    }

    public PistonBaseBlock(boolean bl, BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.NORTH)).setValue(EXTENDED, false));
        this.isSticky = bl;
    }

    @Override
    protected VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        if (blockState.getValue(EXTENDED).booleanValue()) {
            return SHAPES.get(blockState.getValue(FACING));
        }
        return Shapes.block();
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack itemStack) {
        if (!level.isClientSide) {
            this.checkIfExtend(level, blockPos, blockState);
        }
    }

    @Override
    protected void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block block, @Nullable Orientation orientation, boolean bl) {
        if (!level.isClientSide) {
            this.checkIfExtend(level, blockPos, blockState);
        }
    }

    @Override
    protected void onPlace(BlockState blockState, Level level, BlockPos blockPos, BlockState blockState2, boolean bl) {
        if (blockState2.is(blockState.getBlock())) {
            return;
        }
        if (!level.isClientSide && level.getBlockEntity(blockPos) == null) {
            this.checkIfExtend(level, blockPos, blockState);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        return (BlockState)((BlockState)this.defaultBlockState().setValue(FACING, blockPlaceContext.getNearestLookingDirection().getOpposite())).setValue(EXTENDED, false);
    }

    private void checkIfExtend(Level level, BlockPos blockPos, BlockState blockState) {
        Direction direction = (Direction)blockState.getValue(FACING);
        boolean bl = this.getNeighborSignal(level, blockPos, direction);
        if (bl && !blockState.getValue(EXTENDED).booleanValue()) {
            if (new PistonStructureResolver(level, blockPos, direction, true).resolve()) {
                level.blockEvent(blockPos, this, 0, direction.get3DDataValue());
            }
        } else if (!bl && blockState.getValue(EXTENDED).booleanValue()) {
            PistonMovingBlockEntity pistonMovingBlockEntity;
            BlockEntity blockEntity;
            BlockPos blockPos2 = blockPos.relative(direction, 2);
            BlockState blockState2 = level.getBlockState(blockPos2);
            int n = 1;
            if (blockState2.is(Blocks.MOVING_PISTON) && blockState2.getValue(FACING) == direction && (blockEntity = level.getBlockEntity(blockPos2)) instanceof PistonMovingBlockEntity && (pistonMovingBlockEntity = (PistonMovingBlockEntity)blockEntity).isExtending() && (pistonMovingBlockEntity.getProgress(0.0f) < 0.5f || level.getGameTime() == pistonMovingBlockEntity.getLastTicked() || ((ServerLevel)level).isHandlingTick())) {
                n = 2;
            }
            level.blockEvent(blockPos, this, n, direction.get3DDataValue());
        }
    }

    private boolean getNeighborSignal(SignalGetter signalGetter, BlockPos blockPos, Direction direction) {
        for (Direction direction2 : Direction.values()) {
            if (direction2 == direction || !signalGetter.hasSignal(blockPos.relative(direction2), direction2)) continue;
            return true;
        }
        if (signalGetter.hasSignal(blockPos, Direction.DOWN)) {
            return true;
        }
        BlockPos blockPos2 = blockPos.above();
        for (Direction direction3 : Direction.values()) {
            if (direction3 == Direction.DOWN || !signalGetter.hasSignal(blockPos2.relative(direction3), direction3)) continue;
            return true;
        }
        return false;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    protected boolean triggerEvent(BlockState blockState, Level level, BlockPos blockPos, int n, int n2) {
        Direction direction = (Direction)blockState.getValue(FACING);
        BlockState blockState2 = (BlockState)blockState.setValue(EXTENDED, true);
        if (!level.isClientSide) {
            boolean bl = this.getNeighborSignal(level, blockPos, direction);
            if (bl && (n == 1 || n == 2)) {
                level.setBlock(blockPos, blockState2, 2);
                return false;
            }
            if (!bl && n == 0) {
                return false;
            }
        }
        if (n == 0) {
            if (!this.moveBlocks(level, blockPos, direction, true)) return false;
            level.setBlock(blockPos, blockState2, 67);
            level.playSound(null, blockPos, SoundEvents.PISTON_EXTEND, SoundSource.BLOCKS, 0.5f, level.random.nextFloat() * 0.25f + 0.6f);
            level.gameEvent(GameEvent.BLOCK_ACTIVATE, blockPos, GameEvent.Context.of(blockState2));
            return true;
        } else {
            if (n != 1 && n != 2) return true;
            BlockEntity blockEntity = level.getBlockEntity(blockPos.relative(direction));
            if (blockEntity instanceof PistonMovingBlockEntity) {
                ((PistonMovingBlockEntity)blockEntity).finalTick();
            }
            BlockState blockState3 = (BlockState)((BlockState)Blocks.MOVING_PISTON.defaultBlockState().setValue(MovingPistonBlock.FACING, direction)).setValue(MovingPistonBlock.TYPE, this.isSticky ? PistonType.STICKY : PistonType.DEFAULT);
            level.setBlock(blockPos, blockState3, 276);
            level.setBlockEntity(MovingPistonBlock.newMovingBlockEntity(blockPos, blockState3, (BlockState)this.defaultBlockState().setValue(FACING, Direction.from3DDataValue(n2 & 7)), direction, false, true));
            level.updateNeighborsAt(blockPos, blockState3.getBlock());
            blockState3.updateNeighbourShapes(level, blockPos, 2);
            if (this.isSticky) {
                PistonMovingBlockEntity pistonMovingBlockEntity;
                BlockEntity blockEntity2;
                BlockPos blockPos2 = blockPos.offset(direction.getStepX() * 2, direction.getStepY() * 2, direction.getStepZ() * 2);
                BlockState blockState4 = level.getBlockState(blockPos2);
                boolean bl = false;
                if (blockState4.is(Blocks.MOVING_PISTON) && (blockEntity2 = level.getBlockEntity(blockPos2)) instanceof PistonMovingBlockEntity && (pistonMovingBlockEntity = (PistonMovingBlockEntity)blockEntity2).getDirection() == direction && pistonMovingBlockEntity.isExtending()) {
                    pistonMovingBlockEntity.finalTick();
                    bl = true;
                }
                if (!bl) {
                    if (n == 1 && !blockState4.isAir() && PistonBaseBlock.isPushable(blockState4, level, blockPos2, direction.getOpposite(), false, direction) && (blockState4.getPistonPushReaction() == PushReaction.NORMAL || blockState4.is(Blocks.PISTON) || blockState4.is(Blocks.STICKY_PISTON))) {
                        this.moveBlocks(level, blockPos, direction, false);
                    } else {
                        level.removeBlock(blockPos.relative(direction), false);
                    }
                }
            } else {
                level.removeBlock(blockPos.relative(direction), false);
            }
            level.playSound(null, blockPos, SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.5f, level.random.nextFloat() * 0.15f + 0.6f);
            level.gameEvent(GameEvent.BLOCK_DEACTIVATE, blockPos, GameEvent.Context.of(blockState3));
        }
        return true;
    }

    public static boolean isPushable(BlockState blockState, Level level, BlockPos blockPos, Direction direction, boolean bl, Direction direction2) {
        if (blockPos.getY() < level.getMinY() || blockPos.getY() > level.getMaxY() || !level.getWorldBorder().isWithinBounds(blockPos)) {
            return false;
        }
        if (blockState.isAir()) {
            return true;
        }
        if (blockState.is(Blocks.OBSIDIAN) || blockState.is(Blocks.CRYING_OBSIDIAN) || blockState.is(Blocks.RESPAWN_ANCHOR) || blockState.is(Blocks.REINFORCED_DEEPSLATE)) {
            return false;
        }
        if (direction == Direction.DOWN && blockPos.getY() == level.getMinY()) {
            return false;
        }
        if (direction == Direction.UP && blockPos.getY() == level.getMaxY()) {
            return false;
        }
        if (blockState.is(Blocks.PISTON) || blockState.is(Blocks.STICKY_PISTON)) {
            if (blockState.getValue(EXTENDED).booleanValue()) {
                return false;
            }
        } else {
            if (blockState.getDestroySpeed(level, blockPos) == -1.0f) {
                return false;
            }
            switch (blockState.getPistonPushReaction()) {
                case BLOCK: {
                    return false;
                }
                case DESTROY: {
                    return bl;
                }
                case PUSH_ONLY: {
                    return direction == direction2;
                }
            }
        }
        return !blockState.hasBlockEntity();
    }

    /*
     * WARNING - void declaration
     */
    private boolean moveBlocks(Level level, BlockPos blockPos, Direction direction, boolean bl) {
        void var16_31;
        void var16_29;
        Object object;
        Object object2;
        Object object3;
        int pistonType;
        Object object4;
        PistonStructureResolver pistonStructureResolver;
        BlockPos blockPos2 = blockPos.relative(direction);
        if (!bl && level.getBlockState(blockPos2).is(Blocks.PISTON_HEAD)) {
            level.setBlock(blockPos2, Blocks.AIR.defaultBlockState(), 276);
        }
        if (!(pistonStructureResolver = new PistonStructureResolver(level, blockPos, direction, bl)).resolve()) {
            return false;
        }
        HashMap hashMap = Maps.newHashMap();
        List<BlockPos> list = pistonStructureResolver.getToPush();
        ArrayList arrayList = Lists.newArrayList();
        for (BlockPos blockStateArray2 : list) {
            object4 = level.getBlockState(blockStateArray2);
            arrayList.add(object4);
            hashMap.put(blockStateArray2, object4);
        }
        List<BlockPos> list2 = pistonStructureResolver.getToDestroy();
        BlockState[] blockStateArray = new BlockState[list.size() + list2.size()];
        object4 = bl ? direction : direction.getOpposite();
        int n = 0;
        for (pistonType = list2.size() - 1; pistonType >= 0; --pistonType) {
            object3 = (BlockPos)list2.get(pistonType);
            BlockState n2 = level.getBlockState((BlockPos)object3);
            object2 = n2.hasBlockEntity() ? level.getBlockEntity((BlockPos)object3) : null;
            PistonBaseBlock.dropResources(n2, level, (BlockPos)object3, (BlockEntity)object2);
            if (!n2.is(BlockTags.FIRE) && level.isClientSide()) {
                level.levelEvent(2001, (BlockPos)object3, PistonBaseBlock.getId(n2));
            }
            level.setBlock((BlockPos)object3, Blocks.AIR.defaultBlockState(), 18);
            level.gameEvent(GameEvent.BLOCK_DESTROY, (BlockPos)object3, GameEvent.Context.of(n2));
            blockStateArray[n++] = n2;
        }
        for (pistonType = list.size() - 1; pistonType >= 0; --pistonType) {
            object3 = list.get(pistonType);
            BlockState blockState = level.getBlockState((BlockPos)object3);
            object3 = ((BlockPos)object3).relative((Direction)object4);
            hashMap.remove(object3);
            object2 = (BlockState)Blocks.MOVING_PISTON.defaultBlockState().setValue(FACING, direction);
            level.setBlock((BlockPos)object3, (BlockState)object2, 324);
            level.setBlockEntity(MovingPistonBlock.newMovingBlockEntity((BlockPos)object3, (BlockState)object2, (BlockState)arrayList.get(pistonType), direction, bl, false));
            blockStateArray[n++] = blockState;
        }
        if (bl) {
            PistonType blockState = this.isSticky ? PistonType.STICKY : PistonType.DEFAULT;
            object3 = (BlockState)((BlockState)Blocks.PISTON_HEAD.defaultBlockState().setValue(PistonHeadBlock.FACING, direction)).setValue(PistonHeadBlock.TYPE, blockState);
            BlockState blockState2 = (BlockState)((BlockState)Blocks.MOVING_PISTON.defaultBlockState().setValue(MovingPistonBlock.FACING, direction)).setValue(MovingPistonBlock.TYPE, this.isSticky ? PistonType.STICKY : PistonType.DEFAULT);
            hashMap.remove(blockPos2);
            level.setBlock(blockPos2, blockState2, 324);
            level.setBlockEntity(MovingPistonBlock.newMovingBlockEntity(blockPos2, blockState2, (BlockState)object3, direction, true, true));
        }
        BlockState blockState = Blocks.AIR.defaultBlockState();
        for (BlockPos blockPos3 : hashMap.keySet()) {
            level.setBlock(blockPos3, blockState, 82);
        }
        for (Map.Entry entry : hashMap.entrySet()) {
            object2 = (BlockPos)entry.getKey();
            object = (BlockState)entry.getValue();
            ((BlockBehaviour.BlockStateBase)object).updateIndirectNeighbourShapes(level, (BlockPos)object2, 2);
            blockState.updateNeighbourShapes(level, (BlockPos)object2, 2);
            blockState.updateIndirectNeighbourShapes(level, (BlockPos)object2, 2);
        }
        object3 = ExperimentalRedstoneUtils.initialOrientation(level, pistonStructureResolver.getPushDirection(), null);
        n = 0;
        int n3 = list2.size() - 1;
        while (var16_29 >= 0) {
            object2 = blockStateArray[n++];
            object = (BlockPos)list2.get((int)var16_29);
            if (level instanceof ServerLevel) {
                ServerLevel serverLevel = (ServerLevel)level;
                ((BlockBehaviour.BlockStateBase)object2).affectNeighborsAfterRemoval(serverLevel, (BlockPos)object, false);
            }
            ((BlockBehaviour.BlockStateBase)object2).updateIndirectNeighbourShapes(level, (BlockPos)object, 2);
            level.updateNeighborsAt((BlockPos)object, ((BlockBehaviour.BlockStateBase)object2).getBlock(), (Orientation)object3);
            --var16_29;
        }
        int n4 = list.size() - 1;
        while (var16_31 >= 0) {
            level.updateNeighborsAt(list.get((int)var16_31), blockStateArray[n++].getBlock(), (Orientation)object3);
            --var16_31;
        }
        if (bl) {
            level.updateNeighborsAt(blockPos2, Blocks.PISTON_HEAD, (Orientation)object3);
        }
        return true;
    }

    @Override
    protected BlockState rotate(BlockState blockState, Rotation rotation) {
        return (BlockState)blockState.setValue(FACING, rotation.rotate((Direction)blockState.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState blockState, Mirror mirror) {
        return blockState.rotate(mirror.getRotation((Direction)blockState.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, EXTENDED);
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState blockState) {
        return blockState.getValue(EXTENDED);
    }

    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return false;
    }
}

