/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
 */
package net.minecraft.world.level.block.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockState
extends BlockBehaviour.BlockStateBase {
    public static final Codec<BlockState> CODEC = BlockState.codec(BuiltInRegistries.BLOCK.byNameCodec(), Block::defaultBlockState).stable();

    public BlockState(Block block, Reference2ObjectArrayMap<Property<?>, Comparable<?>> reference2ObjectArrayMap, MapCodec<BlockState> mapCodec) {
        super(block, reference2ObjectArrayMap, mapCodec);
    }

    @Override
    protected BlockState asState() {
        return this;
    }
}

