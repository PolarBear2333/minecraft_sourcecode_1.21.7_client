/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.world.item.ItemStack;

public class ClientboundContainerSetSlotPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundContainerSetSlotPacket> STREAM_CODEC = Packet.codec(ClientboundContainerSetSlotPacket::write, ClientboundContainerSetSlotPacket::new);
    private final int containerId;
    private final int stateId;
    private final int slot;
    private final ItemStack itemStack;

    public ClientboundContainerSetSlotPacket(int n, int n2, int n3, ItemStack itemStack) {
        this.containerId = n;
        this.stateId = n2;
        this.slot = n3;
        this.itemStack = itemStack.copy();
    }

    private ClientboundContainerSetSlotPacket(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        this.containerId = registryFriendlyByteBuf.readContainerId();
        this.stateId = registryFriendlyByteBuf.readVarInt();
        this.slot = registryFriendlyByteBuf.readShort();
        this.itemStack = (ItemStack)ItemStack.OPTIONAL_STREAM_CODEC.decode(registryFriendlyByteBuf);
    }

    private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        registryFriendlyByteBuf.writeContainerId(this.containerId);
        registryFriendlyByteBuf.writeVarInt(this.stateId);
        registryFriendlyByteBuf.writeShort(this.slot);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(registryFriendlyByteBuf, this.itemStack);
    }

    @Override
    public PacketType<ClientboundContainerSetSlotPacket> type() {
        return GamePacketTypes.CLIENTBOUND_CONTAINER_SET_SLOT;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleContainerSetSlot(this);
    }

    public int getContainerId() {
        return this.containerId;
    }

    public int getSlot() {
        return this.slot;
    }

    public ItemStack getItem() {
        return this.itemStack;
    }

    public int getStateId() {
        return this.stateId;
    }
}

