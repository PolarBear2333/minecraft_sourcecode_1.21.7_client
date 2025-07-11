/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.world.level.block.entity.CommandBlockEntity;

public class ServerboundSetCommandBlockPacket
implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ServerboundSetCommandBlockPacket> STREAM_CODEC = Packet.codec(ServerboundSetCommandBlockPacket::write, ServerboundSetCommandBlockPacket::new);
    private static final int FLAG_TRACK_OUTPUT = 1;
    private static final int FLAG_CONDITIONAL = 2;
    private static final int FLAG_AUTOMATIC = 4;
    private final BlockPos pos;
    private final String command;
    private final boolean trackOutput;
    private final boolean conditional;
    private final boolean automatic;
    private final CommandBlockEntity.Mode mode;

    public ServerboundSetCommandBlockPacket(BlockPos blockPos, String string, CommandBlockEntity.Mode mode, boolean bl, boolean bl2, boolean bl3) {
        this.pos = blockPos;
        this.command = string;
        this.trackOutput = bl;
        this.conditional = bl2;
        this.automatic = bl3;
        this.mode = mode;
    }

    private ServerboundSetCommandBlockPacket(FriendlyByteBuf friendlyByteBuf) {
        this.pos = friendlyByteBuf.readBlockPos();
        this.command = friendlyByteBuf.readUtf();
        this.mode = friendlyByteBuf.readEnum(CommandBlockEntity.Mode.class);
        byte by = friendlyByteBuf.readByte();
        this.trackOutput = (by & 1) != 0;
        this.conditional = (by & 2) != 0;
        this.automatic = (by & 4) != 0;
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBlockPos(this.pos);
        friendlyByteBuf.writeUtf(this.command);
        friendlyByteBuf.writeEnum(this.mode);
        int n = 0;
        if (this.trackOutput) {
            n |= 1;
        }
        if (this.conditional) {
            n |= 2;
        }
        if (this.automatic) {
            n |= 4;
        }
        friendlyByteBuf.writeByte(n);
    }

    @Override
    public PacketType<ServerboundSetCommandBlockPacket> type() {
        return GamePacketTypes.SERVERBOUND_SET_COMMAND_BLOCK;
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handleSetCommandBlock(this);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public String getCommand() {
        return this.command;
    }

    public boolean isTrackOutput() {
        return this.trackOutput;
    }

    public boolean isConditional() {
        return this.conditional;
    }

    public boolean isAutomatic() {
        return this.automatic;
    }

    public CommandBlockEntity.Mode getMode() {
        return this.mode;
    }
}

