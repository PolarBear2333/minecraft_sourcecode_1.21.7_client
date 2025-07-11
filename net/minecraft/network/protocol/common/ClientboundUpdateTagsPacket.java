/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.common;

import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;
import net.minecraft.network.protocol.common.CommonPacketTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagNetworkSerialization;

public class ClientboundUpdateTagsPacket
implements Packet<ClientCommonPacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundUpdateTagsPacket> STREAM_CODEC = Packet.codec(ClientboundUpdateTagsPacket::write, ClientboundUpdateTagsPacket::new);
    private final Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> tags;

    public ClientboundUpdateTagsPacket(Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> map) {
        this.tags = map;
    }

    private ClientboundUpdateTagsPacket(FriendlyByteBuf friendlyByteBuf) {
        this.tags = friendlyByteBuf.readMap(FriendlyByteBuf::readRegistryKey, TagNetworkSerialization.NetworkPayload::read);
    }

    private void write(FriendlyByteBuf friendlyByteBuf2) {
        friendlyByteBuf2.writeMap(this.tags, FriendlyByteBuf::writeResourceKey, (friendlyByteBuf, networkPayload) -> networkPayload.write((FriendlyByteBuf)((Object)friendlyByteBuf)));
    }

    @Override
    public PacketType<ClientboundUpdateTagsPacket> type() {
        return CommonPacketTypes.CLIENTBOUND_UPDATE_TAGS;
    }

    @Override
    public void handle(ClientCommonPacketListener clientCommonPacketListener) {
        clientCommonPacketListener.handleUpdateTags(this);
    }

    public Map<ResourceKey<? extends Registry<?>>, TagNetworkSerialization.NetworkPayload> getTags() {
        return this.tags;
    }
}

