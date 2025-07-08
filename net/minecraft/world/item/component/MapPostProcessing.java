/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.world.item.component;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;

public enum MapPostProcessing {
    LOCK(0),
    SCALE(1);

    public static final IntFunction<MapPostProcessing> ID_MAP;
    public static final StreamCodec<ByteBuf, MapPostProcessing> STREAM_CODEC;
    private final int id;

    private MapPostProcessing(int n2) {
        this.id = n2;
    }

    public int id() {
        return this.id;
    }

    static {
        ID_MAP = ByIdMap.continuous(MapPostProcessing::id, MapPostProcessing.values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        STREAM_CODEC = ByteBufCodecs.idMapper(ID_MAP, MapPostProcessing::id);
    }
}

