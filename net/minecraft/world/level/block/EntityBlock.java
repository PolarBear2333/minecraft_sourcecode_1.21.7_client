/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEventListener;

public interface EntityBlock {
    @Nullable
    public BlockEntity newBlockEntity(BlockPos var1, BlockState var2);

    @Nullable
    default public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return null;
    }

    @Nullable
    default public <T extends BlockEntity> GameEventListener getListener(ServerLevel serverLevel, T t) {
        if (t instanceof GameEventListener.Provider) {
            GameEventListener.Provider provider = (GameEventListener.Provider)((Object)t);
            return provider.getListener();
        }
        return null;
    }
}

