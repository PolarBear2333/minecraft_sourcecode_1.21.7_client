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

public class ClientboundSetChunkCacheCenterPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundSetChunkCacheCenterPacket> STREAM_CODEC = Packet.codec(ClientboundSetChunkCacheCenterPacket::write, ClientboundSetChunkCacheCenterPacket::new);
    private final int x;
    private final int z;

    public ClientboundSetChunkCacheCenterPacket(int n, int n2) {
        this.x = n;
        this.z = n2;
    }

    private ClientboundSetChunkCacheCenterPacket(FriendlyByteBuf friendlyByteBuf) {
        this.x = friendlyByteBuf.readVarInt();
        this.z = friendlyByteBuf.readVarInt();
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.x);
        friendlyByteBuf.writeVarInt(this.z);
    }

    @Override
    public PacketType<ClientboundSetChunkCacheCenterPacket> type() {
        return GamePacketTypes.CLIENTBOUND_SET_CHUNK_CACHE_CENTER;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSetChunkCacheCenter(this);
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }
}

