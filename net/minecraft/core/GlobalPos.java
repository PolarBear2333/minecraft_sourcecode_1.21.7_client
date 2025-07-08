/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.core;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record GlobalPos(ResourceKey<Level> dimension, BlockPos pos) {
    public static final MapCodec<GlobalPos> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(GlobalPos::dimension), (App)BlockPos.CODEC.fieldOf("pos").forGetter(GlobalPos::pos)).apply((Applicative)instance, GlobalPos::of));
    public static final Codec<GlobalPos> CODEC = MAP_CODEC.codec();
    public static final StreamCodec<ByteBuf, GlobalPos> STREAM_CODEC = StreamCodec.composite(ResourceKey.streamCodec(Registries.DIMENSION), GlobalPos::dimension, BlockPos.STREAM_CODEC, GlobalPos::pos, GlobalPos::of);

    public static GlobalPos of(ResourceKey<Level> resourceKey, BlockPos blockPos) {
        return new GlobalPos(resourceKey, blockPos);
    }

    @Override
    public String toString() {
        return String.valueOf(this.dimension) + " " + String.valueOf(this.pos);
    }

    public boolean isCloseEnough(ResourceKey<Level> resourceKey, BlockPos blockPos, int n) {
        return this.dimension.equals(resourceKey) && this.pos.distChessboard(blockPos) <= n;
    }
}

