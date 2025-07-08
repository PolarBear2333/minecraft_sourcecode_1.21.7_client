/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;

public class ClientboundContainerClosePacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundContainerClosePacket> STREAM_CODEC = Packet.codec(ClientboundContainerClosePacket::write, ClientboundContainerClosePacket::new);
    private final int containerId;

    public ClientboundContainerClosePacket(int n) {
        this.containerId = n;
    }

    private ClientboundContainerClosePacket(FriendlyByteBuf friendlyByteBuf) {
        this.containerId = friendlyByteBuf.readContainerId();
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeContainerId(this.containerId);
    }

    @Override
    public PacketType<ClientboundContainerClosePacket> type() {
        return GamePacketTypes.CLIENTBOUND_CONTAINER_CLOSE;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleContainerClose(this);
    }

    public int getContainerId() {
        return this.containerId;
    }
}

