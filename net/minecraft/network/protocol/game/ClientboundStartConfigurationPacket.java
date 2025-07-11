/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.protocol.game;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;

public class ClientboundStartConfigurationPacket
implements Packet<ClientGamePacketListener> {
    public static final ClientboundStartConfigurationPacket INSTANCE = new ClientboundStartConfigurationPacket();
    public static final StreamCodec<ByteBuf, ClientboundStartConfigurationPacket> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    private ClientboundStartConfigurationPacket() {
    }

    @Override
    public PacketType<ClientboundStartConfigurationPacket> type() {
        return GamePacketTypes.CLIENTBOUND_START_CONFIGURATION;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleConfigurationStart(this);
    }

    @Override
    public boolean isTerminal() {
        return true;
    }
}

