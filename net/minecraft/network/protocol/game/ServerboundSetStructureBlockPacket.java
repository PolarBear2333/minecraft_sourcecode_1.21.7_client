/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.game;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.game.GamePacketTypes;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.properties.StructureMode;

public class ServerboundSetStructureBlockPacket
implements Packet<ServerGamePacketListener> {
    public static final StreamCodec<FriendlyByteBuf, ServerboundSetStructureBlockPacket> STREAM_CODEC = Packet.codec(ServerboundSetStructureBlockPacket::write, ServerboundSetStructureBlockPacket::new);
    private static final int FLAG_IGNORE_ENTITIES = 1;
    private static final int FLAG_SHOW_AIR = 2;
    private static final int FLAG_SHOW_BOUNDING_BOX = 4;
    private static final int FLAG_STRICT = 8;
    private final BlockPos pos;
    private final StructureBlockEntity.UpdateType updateType;
    private final StructureMode mode;
    private final String name;
    private final BlockPos offset;
    private final Vec3i size;
    private final Mirror mirror;
    private final Rotation rotation;
    private final String data;
    private final boolean ignoreEntities;
    private final boolean strict;
    private final boolean showAir;
    private final boolean showBoundingBox;
    private final float integrity;
    private final long seed;

    public ServerboundSetStructureBlockPacket(BlockPos blockPos, StructureBlockEntity.UpdateType updateType, StructureMode structureMode, String string, BlockPos blockPos2, Vec3i vec3i, Mirror mirror, Rotation rotation, String string2, boolean bl, boolean bl2, boolean bl3, boolean bl4, float f, long l) {
        this.pos = blockPos;
        this.updateType = updateType;
        this.mode = structureMode;
        this.name = string;
        this.offset = blockPos2;
        this.size = vec3i;
        this.mirror = mirror;
        this.rotation = rotation;
        this.data = string2;
        this.ignoreEntities = bl;
        this.strict = bl2;
        this.showAir = bl3;
        this.showBoundingBox = bl4;
        this.integrity = f;
        this.seed = l;
    }

    private ServerboundSetStructureBlockPacket(FriendlyByteBuf friendlyByteBuf) {
        this.pos = friendlyByteBuf.readBlockPos();
        this.updateType = friendlyByteBuf.readEnum(StructureBlockEntity.UpdateType.class);
        this.mode = friendlyByteBuf.readEnum(StructureMode.class);
        this.name = friendlyByteBuf.readUtf();
        int n = 48;
        this.offset = new BlockPos(Mth.clamp(friendlyByteBuf.readByte(), -48, 48), Mth.clamp(friendlyByteBuf.readByte(), -48, 48), Mth.clamp(friendlyByteBuf.readByte(), -48, 48));
        int n2 = 48;
        this.size = new Vec3i(Mth.clamp(friendlyByteBuf.readByte(), 0, 48), Mth.clamp(friendlyByteBuf.readByte(), 0, 48), Mth.clamp(friendlyByteBuf.readByte(), 0, 48));
        this.mirror = friendlyByteBuf.readEnum(Mirror.class);
        this.rotation = friendlyByteBuf.readEnum(Rotation.class);
        this.data = friendlyByteBuf.readUtf(128);
        this.integrity = Mth.clamp(friendlyByteBuf.readFloat(), 0.0f, 1.0f);
        this.seed = friendlyByteBuf.readVarLong();
        byte by = friendlyByteBuf.readByte();
        this.ignoreEntities = (by & 1) != 0;
        this.strict = (by & 8) != 0;
        this.showAir = (by & 2) != 0;
        this.showBoundingBox = (by & 4) != 0;
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBlockPos(this.pos);
        friendlyByteBuf.writeEnum(this.updateType);
        friendlyByteBuf.writeEnum(this.mode);
        friendlyByteBuf.writeUtf(this.name);
        friendlyByteBuf.writeByte(this.offset.getX());
        friendlyByteBuf.writeByte(this.offset.getY());
        friendlyByteBuf.writeByte(this.offset.getZ());
        friendlyByteBuf.writeByte(this.size.getX());
        friendlyByteBuf.writeByte(this.size.getY());
        friendlyByteBuf.writeByte(this.size.getZ());
        friendlyByteBuf.writeEnum(this.mirror);
        friendlyByteBuf.writeEnum(this.rotation);
        friendlyByteBuf.writeUtf(this.data);
        friendlyByteBuf.writeFloat(this.integrity);
        friendlyByteBuf.writeVarLong(this.seed);
        int n = 0;
        if (this.ignoreEntities) {
            n |= 1;
        }
        if (this.showAir) {
            n |= 2;
        }
        if (this.showBoundingBox) {
            n |= 4;
        }
        if (this.strict) {
            n |= 8;
        }
        friendlyByteBuf.writeByte(n);
    }

    @Override
    public PacketType<ServerboundSetStructureBlockPacket> type() {
        return GamePacketTypes.SERVERBOUND_SET_STRUCTURE_BLOCK;
    }

    @Override
    public void handle(ServerGamePacketListener serverGamePacketListener) {
        serverGamePacketListener.handleSetStructureBlock(this);
    }

    public BlockPos getPos() {
        return this.pos;
    }

    public StructureBlockEntity.UpdateType getUpdateType() {
        return this.updateType;
    }

    public StructureMode getMode() {
        return this.mode;
    }

    public String getName() {
        return this.name;
    }

    public BlockPos getOffset() {
        return this.offset;
    }

    public Vec3i getSize() {
        return this.size;
    }

    public Mirror getMirror() {
        return this.mirror;
    }

    public Rotation getRotation() {
        return this.rotation;
    }

    public String getData() {
        return this.data;
    }

    public boolean isIgnoreEntities() {
        return this.ignoreEntities;
    }

    public boolean isStrict() {
        return this.strict;
    }

    public boolean isShowAir() {
        return this.showAir;
    }

    public boolean isShowBoundingBox() {
        return this.showBoundingBox;
    }

    public float getIntegrity() {
        return this.integrity;
    }

    public long getSeed() {
        return this.seed;
    }
}

