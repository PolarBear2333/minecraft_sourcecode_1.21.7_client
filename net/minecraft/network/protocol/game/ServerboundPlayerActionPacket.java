/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

public class ServerboundPlayerActionPacket
implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ServerboundPlayerActionPacket> STREAM_CODEC = Packet.codec(ServerboundPlayerActionPacket::write, ServerboundPlayerActionPacket::new);
    private final BlockPos pos;
    private final Direction direction;
    private final Action action;
    private final int sequence;

    public ServerboundPlayerActionPacket(Action action, BlockPos blockPos, Direction direction, int n) {
        this.action = action;
        this.pos = blockPos.immutable();
        this.direction = direction;
        this.sequence = n;
    }

    public ServerboundPlayerActionPacket(Action action, BlockPos blockPos, Direction direction) {
        this(action, blockPos, direction, 0);
    }

    private ServerboundPlayerActionPacket(FriendlyByteBuf friendlyByteBuf) {
        this.action = friendlyByteBuf.readEnum(Action.class);
        this.pos = friendlyByteBuf.readBlockPos();
        this.direction = Direction.from3DDataValue(friendlyByteBuf.readUnsignedByte());
        this.sequence = friendlyByteBuf.readVarInt();
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeEnum(this.action);
        friendlyByteBuf.writeBlockPos(this.pos);
        friendlyByteBuf.writeByte(this.direction.get3DDataValue());
        friendlyByteBuf.writeVarInt(this.sequence);
    }

    @Override
    public PacketType<ServerboundPlayerActionPacket> type() {
        return GamePacketTypes.SERVERBOUND_PLAYER_ACTION;
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handlePlayerAction(this);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public Direction getDirection() {
        return this.direction;
    }

    public Action getAction() {
        return this.action;
    }

    public int getSequence() {
        return this.sequence;
    }

    public static enum Action {
        START_DESTROY_BLOCK,
        ABORT_DESTROY_BLOCK,
        STOP_DESTROY_BLOCK,
        DROP_ALL_ITEMS,
        DROP_ITEM,
        RELEASE_USE_ITEM,
        SWAP_ITEM_WITH_OFFHAND;

    }
}

