/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.common.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record PoiRemovedDebugPayload(BlockPos pos) implements CustomPacketPayload
{
    public static final StreamCodec<FriendlyByteBuf, PoiRemovedDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(PoiRemovedDebugPayload::write, PoiRemovedDebugPayload::new);
    public static final CustomPacketPayload.Type<PoiRemovedDebugPayload> TYPE = CustomPacketPayload.createType("debug/poi_removed");

    private PoiRemovedDebugPayload(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readBlockPos());
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBlockPos(this.pos);
    }

    public CustomPacketPayload.Type<PoiRemovedDebugPayload> type() {
        return TYPE;
    }
}

