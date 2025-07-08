/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

public class ServerboundBlockEntityTagQueryPacket
implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ServerboundBlockEntityTagQueryPacket> STREAM_CODEC = Packet.codec(ServerboundBlockEntityTagQueryPacket::write, ServerboundBlockEntityTagQueryPacket::new);
    private final int transactionId;
    private final BlockPos pos;

    public ServerboundBlockEntityTagQueryPacket(int n, BlockPos blockPos) {
        this.transactionId = n;
        this.pos = blockPos;
    }

    private ServerboundBlockEntityTagQueryPacket(FriendlyByteBuf friendlyByteBuf) {
        this.transactionId = friendlyByteBuf.readVarInt();
        this.pos = friendlyByteBuf.readBlockPos();
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.transactionId);
        friendlyByteBuf.writeBlockPos(this.pos);
    }

    @Override
    public PacketType<ServerboundBlockEntityTagQueryPacket> type() {
        return GamePacketTypes.SERVERBOUND_BLOCK_ENTITY_TAG_QUERY;
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handleBlockEntityTagQuery(this);
    }

    public int getTransactionId() {
        return this.transactionId;
    }

    public BlockPos getPos() {
        return this.pos;
    }
}

