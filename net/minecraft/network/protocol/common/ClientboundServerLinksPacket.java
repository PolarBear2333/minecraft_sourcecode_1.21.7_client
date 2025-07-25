/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.protocol.common;

import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.CommonPacketTypes;
import net.minecraft.server.ServerLinks;

public record ClientboundServerLinksPacket(List<ServerLinks.UntrustedEntry> links) implements Packet<ClientCommonPacketListener>
{
    public static final StreamCodec<ByteBuf, ClientboundServerLinksPacket> STREAM_CODEC = StreamCodec.composite(ServerLinks.UNTRUSTED_LINKS_STREAM_CODEC, ClientboundServerLinksPacket::links, ClientboundServerLinksPacket::new);

    @Override
    public PacketType<ClientboundServerLinksPacket> type() {
        return CommonPacketTypes.CLIENTBOUND_SERVER_LINKS;
    }

    @Override
    public void handle(ClientCommonPacketListener clientCommonPacketListener) {
        clientCommonPacketListener.handleServerLinks(this);
    }
}

