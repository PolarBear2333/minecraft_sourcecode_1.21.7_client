/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.network.protocol.common.custom;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.DebugEntityNameGenerator;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public record BeeDebugPayload(BeeInfo beeInfo) implements CustomPacketPayload
{
    public static final StreamCodec<FriendlyByteBuf, BeeDebugPayload> STREAM_CODEC = CustomPacketPayload.codec(BeeDebugPayload::write, BeeDebugPayload::new);
    public static final CustomPacketPayload.Type<BeeDebugPayload> TYPE = CustomPacketPayload.createType("debug/bee");

    private BeeDebugPayload(FriendlyByteBuf friendlyByteBuf) {
        this(new BeeInfo(friendlyByteBuf));
    }

    private void write(FriendlyByteBuf friendlyByteBuf) {
        this.beeInfo.write(friendlyByteBuf);
    }

    public CustomPacketPayload.Type<BeeDebugPayload> type() {
        return TYPE;
    }

    public record BeeInfo(UUID uuid, int id, Vec3 pos, @Nullable Path path, @Nullable BlockPos hivePos, @Nullable BlockPos flowerPos, int travelTicks, Set<String> goals, List<BlockPos> blacklistedHives) {
        public BeeInfo(FriendlyByteBuf friendlyByteBuf) {
            this(friendlyByteBuf.readUUID(), friendlyByteBuf.readInt(), friendlyByteBuf.readVec3(), friendlyByteBuf.readNullable(Path::createFromStream), friendlyByteBuf.readNullable(BlockPos.STREAM_CODEC), friendlyByteBuf.readNullable(BlockPos.STREAM_CODEC), friendlyByteBuf.readInt(), friendlyByteBuf.readCollection(HashSet::new, FriendlyByteBuf::readUtf), friendlyByteBuf.readList(BlockPos.STREAM_CODEC));
        }

        public void write(FriendlyByteBuf friendlyByteBuf2) {
            friendlyByteBuf2.writeUUID(this.uuid);
            friendlyByteBuf2.writeInt(this.id);
            friendlyByteBuf2.writeVec3(this.pos);
            friendlyByteBuf2.writeNullable(this.path, (friendlyByteBuf, path) -> path.writeToStream((FriendlyByteBuf)((Object)friendlyByteBuf)));
            friendlyByteBuf2.writeNullable(this.hivePos, BlockPos.STREAM_CODEC);
            friendlyByteBuf2.writeNullable(this.flowerPos, BlockPos.STREAM_CODEC);
            friendlyByteBuf2.writeInt(this.travelTicks);
            friendlyByteBuf2.writeCollection(this.goals, FriendlyByteBuf::writeUtf);
            friendlyByteBuf2.writeCollection(this.blacklistedHives, BlockPos.STREAM_CODEC);
        }

        public boolean hasHive(BlockPos blockPos) {
            return Objects.equals(blockPos, this.hivePos);
        }

        public String generateName() {
            return DebugEntityNameGenerator.getEntityName(this.uuid);
        }

        @Override
        public String toString() {
            return this.generateName();
        }
    }
}

