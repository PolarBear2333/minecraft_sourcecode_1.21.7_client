/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;

public record ClientboundDeleteChatPacket(MessageSignature.Packed messageSignature) implements Packet<ClientGamePacketListener>
{
    public static final StreamCodec<FriendlyByteBuf, ClientboundDeleteChatPacket> STREAM_CODEC = Packet.codec(ClientboundDeleteChatPacket::write, ClientboundDeleteChatPacket::new);

    private ClientboundDeleteChatPacket(FriendlyByteBuf friendlyByteBuf) {
        this(MessageSignature.Packed.read(friendlyByteBuf));
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        MessageSignature.Packed.write(friendlyByteBuf, this.messageSignature);
    }

    @Override
    public PacketType<ClientboundDeleteChatPacket> type() {
        return GamePacketTypes.CLIENTBOUND_DELETE_CHAT;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleDeleteChat(this);
    }
}

