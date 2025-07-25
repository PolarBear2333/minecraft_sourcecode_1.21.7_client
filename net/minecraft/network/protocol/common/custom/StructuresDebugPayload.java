/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.protocol.common.custom;

import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public record StructuresDebugPayload(ResourceKey<Level> dimension, BoundingBox mainBB, List<PieceInfo> pieces) implements CustomPacketPayload
{
    public static final StreamCodec<FriendlyByteBuf, StructuresDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(StructuresDebugPayload::write, StructuresDebugPayload::new);
    public static final CustomPacketPayload.Type<StructuresDebugPayload> TYPE = CustomPacketPayload.createType("debug/structures");

    private StructuresDebugPayload(FriendlyByteBuf friendlyByteBuf) {
        this(friendlyByteBuf.readResourceKey(Registries.DIMENSION), StructuresDebugPayload.readBoundingBox(friendlyByteBuf), friendlyByteBuf.readList(PieceInfo::new));
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeResourceKey(this.dimension);
        StructuresDebugPayload.writeBoundingBox(friendlyByteBuf, this.mainBB);
        friendlyByteBuf.writeCollection(this.pieces, (friendlyByteBuf2, pieceInfo) -> pieceInfo.write(friendlyByteBuf));
    }

    public CustomPacketPayload.Type<StructuresDebugPayload> type() {
        return TYPE;
    }

    static BoundingBox readBoundingBox(FriendlyByteBuf friendlyByteBuf) {
        return new BoundingBox(friendlyByteBuf.readInt(), friendlyByteBuf.readInt(), friendlyByteBuf.readInt(), friendlyByteBuf.readInt(), friendlyByteBuf.readInt(), friendlyByteBuf.readInt());
    }

    static void writeBoundingBox(FriendlyByteBuf friendlyByteBuf, BoundingBox boundingBox) {
        friendlyByteBuf.writeInt(boundingBox.minX());
        friendlyByteBuf.writeInt(boundingBox.minY());
        friendlyByteBuf.writeInt(boundingBox.minZ());
        friendlyByteBuf.writeInt(boundingBox.maxX());
        friendlyByteBuf.writeInt(boundingBox.maxY());
        friendlyByteBuf.writeInt(boundingBox.maxZ());
    }

    public record PieceInfo(BoundingBox boundingBox, boolean isStart) {
        public PieceInfo(FriendlyByteBuf friendlyByteBuf) {
            this(StructuresDebugPayload.readBoundingBox(friendlyByteBuf), friendlyByteBuf.readBoolean());
        }

        public void write(FriendlyByteBuf friendlyByteBuf) {
            StructuresDebugPayload.writeBoundingBox(friendlyByteBuf, this.boundingBox);
            friendlyByteBuf.writeBoolean(this.isStart);
        }
    }
}

