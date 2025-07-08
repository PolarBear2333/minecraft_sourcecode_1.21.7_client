/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.common;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.CommonPacketTypes;

public class ClientboundPingPacket
implements Packet<ClientCommonPacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundPingPacket> STREAM_CODEC = Packet.codec(ClientboundPingPacket::write, ClientboundPingPacket::new);
    private final int id;

    public ClientboundPingPacket(int n) {
        this.id = n;
    }

    private ClientboundPingPacket(FriendlyByteBuf friendlyByteBuf) {
        this.id = friendlyByteBuf.readInt();
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(this.id);
    }

    @Override
    public PacketType<ClientboundPingPacket> type() {
        return CommonPacketTypes.CLIENTBOUND_PING;
    }

    @Override
    public void handle(ClientCommonPacketListener clientCommonPacketListener) {
        clientCommonPacketListener.handlePing(this);
    }

    public int getId() {
        return this.id;
    }
}

