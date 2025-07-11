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

public class ClientboundSetHealthPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundSetHealthPacket> STREAM_CODEC = Packet.codec(ClientboundSetHealthPacket::write, ClientboundSetHealthPacket::new);
    private final float health;
    private final int food;
    private final float saturation;

    public ClientboundSetHealthPacket(float f, int n, float f2) {
        this.health = f;
        this.food = n;
        this.saturation = f2;
    }

    private ClientboundSetHealthPacket(FriendlyByteBuf friendlyByteBuf) {
        this.health = friendlyByteBuf.readFloat();
        this.food = friendlyByteBuf.readVarInt();
        this.saturation = friendlyByteBuf.readFloat();
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeFloat(this.health);
        friendlyByteBuf.writeVarInt(this.food);
        friendlyByteBuf.writeFloat(this.saturation);
    }

    @Override
    public PacketType<ClientboundSetHealthPacket> type() {
        return GamePacketTypes.CLIENTBOUND_SET_HEALTH;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleSetHealth(this);
    }

    public float getHealth() {
        return this.health;
    }

    public int getFood() {
        return this.food;
    }

    public float getSaturation() {
        return this.saturation;
    }
}

