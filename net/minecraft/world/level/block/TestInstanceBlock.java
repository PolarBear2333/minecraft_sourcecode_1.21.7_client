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
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.GameMasterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.TestInstanceBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class TestInstanceBlock
extends BaseEntityBlock
implements GameMasterBlock {
    public static final MapCodec<TestInstanceBlock> CODEC = TestInstanceBlock.simpleCodec(TestInstanceBlock::new);

    public TestInstanceBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new TestInstanceBlockEntity(blockPos, blockState);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState blockState, Level level, BlockPos blockPos, Player player, BlockHitResult blockHitResult) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        if (!(blockEntity instanceof TestInstanceBlockEntity)) {
            return InteractionResult.PASS;
        }
        TestInstanceBlockEntity testInstanceBlockEntity = (TestInstanceBlockEntity)blockEntity;
        if (!player.canUseGameMasterBlocks()) {
            return InteractionResult.PASS;
        }
        if (player.level().isClientSide) {
            player.openTestInstanceBlock(testInstanceBlockEntity);
        }
        return InteractionResult.SUCCESS;
    }

    protected MapCodec<TestInstanceBlock> codec() {
        return CODEC;
    }
}

