/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MudBlock
extends Block {
    public static final MapCodec<MudBlock> CODEC = MudBlock.simpleCodec(MudBlock::new);
    private static final VoxelShape SHAPE = Block.column(16.0, 0.0, 14.0);

    public MapCodec<MudBlock> codec() {
        return CODEC;
    }

    public MudBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return Shapes.block();
    }

    @Override
    protected VoxelShape getVisualShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
        return Shapes.block();
    }

    @Override
    protected boolean isPathfindable(BlockState blockState, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    protected float getShadeBrightness(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos) {
        return 0.2f;
    }
}

