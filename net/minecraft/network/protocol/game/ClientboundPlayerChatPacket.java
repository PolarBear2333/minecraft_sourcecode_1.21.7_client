/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.network.protocol.game;

import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.FilterMask;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;

public record ClientboundPlayerChatPacket(int globalIndex, UUID sender, int index, @Nullable MessageSignature signature, SignedMessageBody.Packed body, @Nullable Component unsignedContent, FilterMask filterMask, ChatType.Bound chatType) implements Packet<ClientGamePacketListener>
{
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundPlayerChatPacket> STREAM_CODEC = Packet.codec(ClientboundPlayerChatPacket::write, ClientboundPlayerChatPacket::new);

    private ClientboundPlayerChatPacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        this(registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readUUID(), registryFriendlyByteBuf.readVarInt(), registryFriendlyByteBuf.readNullable(MessageSignature::read), new SignedMessageBody.Packed(registryFriendlyByteBuf), FriendlyByteBuf.readNullable(registryFriendlyByteBuf, ComponentSerialization.TRUSTED_STREAM_CODEC), FilterMask.read(registryFriendlyByteBuf), (ChatType.Bound)ChatType.Bound.STREAM_CODEC.decode(registryFriendlyByteBuf));
    }

    private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        registryFriendlyByteBuf.writeVarInt(this.globalIndex);
        registryFriendlyByteBuf.writeUUID(this.sender);
        registryFriendlyByteBuf.writeVarInt(this.index);
        registryFriendlyByteBuf.writeNullable(this.signature, MessageSignature::write);
        this.body.write(registryFriendlyByteBuf);
        FriendlyByteBuf.writeNullable(registryFriendlyByteBuf, this.unsignedContent, ComponentSerialization.TRUSTED_STREAM_CODEC);
        FilterMask.write(registryFriendlyByteBuf, this.filterMask);
        ChatType.Bound.STREAM_CODEC.encode(registryFriendlyByteBuf, this.chatType);
    }

    @Override
    public PacketType<ClientboundPlayerChatPacket> type() {
        return GamePacketTypes.CLIENTBOUND_PLAYER_CHAT;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handlePlayerChat(this);
    }

    @Override
    public boolean isSkippable() {
        return true;
    }
}

