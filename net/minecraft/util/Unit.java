/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.util;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;

public enum Unit {
    INSTANCE;

    public static final Codec<Unit> CODEC;
    public static final StreamCodec<ByteBuf, Unit> STREAM_CODEC;

    static {
        CODEC = Codec.unit((Object)((Object)INSTANCE));
        STREAM_CODEC = StreamCodec.unit(INSTANCE);
    }
}

