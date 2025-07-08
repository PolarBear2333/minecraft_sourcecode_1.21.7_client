/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.status;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.status.ClientStatusPacketListener;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatusPacketListener;
import net.minecraft.network.protocol.status.ServerboundStatusRequestPacket;
import net.minecraft.resources.ResourceLocation;

public class StatusPacketTypes {
    public static final PacketType<ClientboundStatusResponsePacket> CLIENTBOUND_STATUS_RESPONSE = StatusPacketTypes.createClientbound("status_response");
    public static final PacketType<ServerboundStatusRequestPacket> SERVERBOUND_STATUS_REQUEST = StatusPacketTypes.createServerbound("status_request");

    private static <T extends Packet<ClientStatusPacketListener>> PacketType<T> createClientbound(String string) {
        return new PacketType(PacketFlow.CLIENTBOUND, ResourceLocation.withDefaultNamespace(string));
    }

    private static <T extends Packet<ServerStatusPacketListener>> PacketType<T> createServerbound(String string) {
        return new PacketType(PacketFlow.SERVERBOUND, ResourceLocation.withDefaultNamespace(string));
    }
}

