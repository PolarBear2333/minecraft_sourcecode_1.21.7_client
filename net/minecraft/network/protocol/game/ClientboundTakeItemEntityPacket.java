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

public class ClientboundTakeItemEntityPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundTakeItemEntityPacket> STREAM_CODEC = Packet.codec(ClientboundTakeItemEntityPacket::write, ClientboundTakeItemEntityPacket::new);
    private final int itemId;
    private final int playerId;
    private final int amount;

    public ClientboundTakeItemEntityPacket(int n, int n2, int n3) {
        this.itemId = n;
        this.playerId = n2;
        this.amount = n3;
    }

    private ClientboundTakeItemEntityPacket(FriendlyByteBuf friendlyByteBuf) {
        this.itemId = friendlyByteBuf.readVarInt();
        this.playerId = friendlyByteBuf.readVarInt();
        this.amount = friendlyByteBuf.readVarInt();
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.itemId);
        friendlyByteBuf.writeVarInt(this.playerId);
        friendlyByteBuf.writeVarInt(this.amount);
    }

    @Override
    public PacketType<ClientboundTakeItemEntityPacket> type() {
        return GamePacketTypes.CLIENTBOUND_TAKE_ITEM_ENTITY;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleTakeItemEntity(this);
    }

    public int getItemId() {
        return this.itemId;
    }

    public int getPlayerId() {
        return this.playerId;
    }

    public int getAmount() {
        return this.amount;
    }
}

