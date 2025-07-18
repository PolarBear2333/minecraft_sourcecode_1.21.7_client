/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.status;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.game.ServerPacketListener;
import net.minecraft.network.protocol.ping.ServerPingPacketListener;
import net.minecraft.network.protocol.status.ServerboundStatusRequestPacket;

public interface ServerStatusPacketListener
extends ServerPacketListener,
ServerPingPacketListener {
    @Override
    default public ConnectionProtocol protocol() {
        return ConnectionProtocol.STATUS;
    }

    public void handleStatusRequest(ServerboundStatusRequestPacket var1);
}

