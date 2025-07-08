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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class ClientboundRotateHeadPacket
implements Packet<ClientGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ClientboundRotateHeadPacket> STREAM_CODEC = Packet.codec(ClientboundRotateHeadPacket::write, ClientboundRotateHeadPacket::new);
    private final int entityId;
    private final byte yHeadRot;

    public ClientboundRotateHeadPacket(Entity entity, byte by) {
        this.entityId = entity.getId();
        this.yHeadRot = by;
    }

    private ClientboundRotateHeadPacket(FriendlyByteBuf friendlyByteBuf) {
        this.entityId = friendlyByteBuf.readVarInt();
        this.yHeadRot = friendlyByteBuf.readByte();
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeVarInt(this.entityId);
        friendlyByteBuf.writeByte(this.yHeadRot);
    }

    @Override
    public PacketType<ClientboundRotateHeadPacket> type() {
        return GamePacketTypes.CLIENTBOUND_ROTATE_HEAD;
    }

    @Override
    public void handle(ClientGamePacketListener clientGamePacketListener) {
        clientGamePacketListener.handleRotateMob(this);
    }

    public Entity getEntity(Level level) {
        return level.getEntity(this.entityId);
    }

    public float getYHeadRot() {
        return Mth.unpackDegrees(this.yHeadRot);
    }
}

