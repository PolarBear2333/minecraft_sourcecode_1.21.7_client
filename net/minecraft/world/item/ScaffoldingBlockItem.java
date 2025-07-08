/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.item;

import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ScaffoldingBlock;
import net.minecraft.world.level.block.state.BlockState;

public class ScaffoldingBlockItem
extends BlockItem {
    public ScaffoldingBlockItem(Block block, Item.Properties properties) {
        super(block, properties);
    }

    @Override
    @Nullable
    public BlockPlaceContext updatePlacementContext(BlockPlaceContext blockPlaceContext) {
        Block block;
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        Level level = blockPlaceContext.getLevel();
        BlockState blockState = level.getBlockState(blockPos);
        if (blockState.is(block = this.getBlock())) {
            Direction direction = blockPlaceContext.isSecondaryUseActive() ? (blockPlaceContext.isInside() ? blockPlaceContext.getClickedFace().getOpposite() : blockPlaceContext.getClickedFace()) : (blockPlaceContext.getClickedFace() == Direction.UP ? blockPlaceContext.getHorizontalDirection() : Direction.UP);
            int n = 0;
            BlockPos.MutableBlockPos mutableBlockPos = blockPos.mutable().move(direction);
            while (n < 7) {
                if (!level.isClientSide && !level.isInWorldBounds(mutableBlockPos)) {
                    Player player = blockPlaceContext.getPlayer();
                    int n2 = level.getMaxY();
                    if (!(player instanceof ServerPlayer) || mutableBlockPos.getY() <= n2) break;
                    ((ServerPlayer)player).sendSystemMessage(Component.translatable("build.tooHigh", n2).withStyle(ChatFormatting.RED), true);
                    break;
                }
                blockState = level.getBlockState(mutableBlockPos);
                if (!blockState.is(this.getBlock())) {
                    if (!blockState.canBeReplaced(blockPlaceContext)) break;
                    return BlockPlaceContext.at(blockPlaceContext, mutableBlockPos, direction);
                }
                mutableBlockPos.move(direction);
                if (!direction.getAxis().isHorizontal()) continue;
                ++n;
            }
            return null;
        }
        if (ScaffoldingBlock.getDistance(level, blockPos) == 7) {
            return null;
        }
        return blockPlaceContext;
    }

    @Override
    protected boolean mustSurvive() {
        return false;
    }
}

