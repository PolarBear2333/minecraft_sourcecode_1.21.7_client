/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.protocol.common.custom;

import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.level.redstone.Orientation;

public record RedstoneWireOrientationsDebugPayload(long time, List<Wire> wires) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<RedstoneWireOrientationsDebugPayload> TYPE = CustomPacketPayload.createType("debug/redstone_update_order");
    public static final StreamCodec<FriendlyByteBuf, RedstoneWireOrientationsDebugPayload> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_LONG, RedstoneWireOrientationsDebugPayload::time, Wire.STREAM_CODEC.apply(ByteBufCodecs.list()), RedstoneWireOrientationsDebugPayload::wires, RedstoneWireOrientationsDebugPayload::new);

    public CustomPacketPayload.Type<RedstoneWireOrientationsDebugPayload> type() {
        return TYPE;
    }

    public record Wire(BlockPos pos, Orientation orientation) {
        public static final StreamCodec<ByteBuf, Wire> STREAM_CODEC = StreamCodec.composite(BlockPos.STREAM_CODEC, Wire::pos, Orientation.STREAM_CODEC, Wire::orientation, Wire::new);
    }
}

