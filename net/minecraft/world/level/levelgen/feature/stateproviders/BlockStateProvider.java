/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.feature.stateproviders;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;
import net.minecraft.world.level.levelgen.feature.stateproviders.SimpleStateProvider;

public abstract class BlockStateProvider {
    public static final Codec<BlockStateProvider> CODEC = BuiltInRegistries.BLOCKSTATE_PROVIDER_TYPE.byNameCodec().dispatch(BlockStateProvider::type, BlockStateProviderType::codec);

    public static SimpleStateProvider simple(BlockState blockState) {
        return new SimpleStateProvider(blockState);
    }

    public static SimpleStateProvider simple(Block block) {
        return new SimpleStateProvider(block.defaultBlockState());
    }

    protected abstract BlockStateProviderType<?> type();

    public abstract BlockState getState(RandomSource var1, BlockPos var2);
}

