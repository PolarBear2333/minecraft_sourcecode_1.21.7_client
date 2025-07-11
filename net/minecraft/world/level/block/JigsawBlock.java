/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.BlockHitResult;

public class JigsawBlock
extends Block
implements EntityBlock,
GameMasterBlock {
    public static final MapCodec<JigsawBlock> CODEC = JigsawBlock.simpleCodec(JigsawBlock::new);
    public static final EnumProperty<FrontAndTop> ORIENTATION = BlockStateProperties.ORIENTATION;

    public MapCodec<JigsawBlock> codec() {
        return CODEC;
    }

    protected JigsawBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(ORIENTATION, FrontAndTop.NORTH_UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ORIENTATION);
    }

    @Override
    protected BlockState rotate(BlockState blockState, Rotation rotation) {
        return (BlockState)blockState.setValue(ORIENTATION, rotation.rotation().rotate(blockState.getValue(ORIENTATION)));
    }

    @Override
    protected BlockState mirror(BlockState blockState, Mirror mirror) {
        return (BlockState)blockState.setValue(ORIENTATION, mirror.rotation().rotate(blockState.getValue(ORIENTATION)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
        Direction direction = blockPlaceContext.getClickedFace();
        Direction direction2 = direction.getAxis() == Direction.Axis.Y ? blockPlaceContext.getHorizontalDirection().getOpposite() : Direction.UP;
        return (BlockState)this.defaultBlockState().setValue(ORIENTATION, FrontAndTop.fromFrontAndTop(direction, direction2));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new JigsawBlockEntity(blockPos, blockState);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (blockEntity instanceof JigsawBlockEntity && player.canUseGameMasterBlocks()) {
            player.openJigsawBlock((JigsawBlockEntity)blockEntity);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    public static boolean canAttach(StructureTemplate.JigsawBlockInfo jigsawBlockInfo, StructureTemplate.JigsawBlockInfo jigsawBlockInfo2) {
        Direction direction = JigsawBlock.getFrontFacing(jigsawBlockInfo.info().state());
        Direction direction2 = JigsawBlock.getFrontFacing(jigsawBlockInfo2.info().state());
        Direction direction3 = JigsawBlock.getTopFacing(jigsawBlockInfo.info().state());
        Direction direction4 = JigsawBlock.getTopFacing(jigsawBlockInfo2.info().state());
        JigsawBlockEntity.JointType jointType = jigsawBlockInfo.jointType();
        boolean bl = jointType == JigsawBlockEntity.JointType.ROLLABLE;
        return direction == direction2.getOpposite() && (bl || direction3 == direction4) && jigsawBlockInfo.target().equals(jigsawBlockInfo2.name());
    }

    public static Direction getFrontFacing(BlockState blockState) {
        return blockState.getValue(ORIENTATION).front();
    }

    public static Direction getTopFacing(BlockState blockState) {
        return blockState.getValue(ORIENTATION).top();
    }
}

