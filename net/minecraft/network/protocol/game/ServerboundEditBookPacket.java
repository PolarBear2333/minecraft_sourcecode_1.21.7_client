/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import java.util.List;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

public record ServerboundEditBookPacket(int slot, List<String> pages, Optional<String> title) implements Packet<ServerGamePacketListener>
{
    public static final StreamCodec<FriendlyByteBuf, ServerboundEditBookPacket> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, ServerboundEditBookPacket::slot, ByteBufCodecs.stringUtf8(1024).apply(ByteBufCodecs.list(100)), ServerboundEditBookPacket::pages, ByteBufCodecs.stringUtf8(32).apply(ByteBufCodecs::optional), ServerboundEditBookPacket::title, ServerboundEditBookPacket::new);

    public ServerboundEditBookPacket {
        list = List.copyOf(list);
    }

    @Override
    public PacketType<ServerboundEditBookPacket> type() {
        return GamePacketTypes.SERVERBOUND_EDIT_BOOK;
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handleEditBook(this);
    }
}

