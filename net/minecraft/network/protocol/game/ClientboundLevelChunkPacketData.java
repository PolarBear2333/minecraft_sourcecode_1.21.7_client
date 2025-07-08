/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  io.netty.buffer.ByteBuf
 *  io.netty.buffer.Unpooled
 *  javax.annotation.Nullable
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;

public class ClientboundLevelChunkPacketData {
    private static final StreamCodec<ByteBuf, Map<Heightmap.Types, long[]>> HEIGHTMAPS_STREAM_CODEC = ByteBufCodecs.map(n -> new EnumMap(Heightmap.Types.class), Heightmap.Types.STREAM_CODEC, ByteBufCodecs.LONG_ARRAY);
    private static final int TWO_MEGABYTES = 0x200000;
    private final Map<Heightmap.Types, long[]> heightmaps;
    private final byte[] buffer;
    private final List<BlockEntityInfo> blockEntitiesData;

    public ClientboundLevelChunkPacketData(LevelChunk levelChunk) {
        this.heightmaps = levelChunk.getHeightmaps().stream().filter(entry -> ((Heightmap.Types)entry.getKey()).sendToClient()).collect(Collectors.toMap(Map.Entry::getKey, entry -> (long[])((Heightmap)entry.getValue()).getRawData().clone()));
        this.buffer = new byte[ClientboundLevelChunkPacketData.calculateChunkSize(levelChunk)];
        ClientboundLevelChunkPacketData.extractChunkData(new FriendlyByteBuf(this.getWriteBuffer()), levelChunk);
        this.blockEntitiesData = Lists.newArrayList();
        for (Map.Entry<BlockPos, BlockEntity> entry2 : levelChunk.getBlockEntities().entrySet()) {
            this.blockEntitiesData.add(BlockEntityInfo.create(entry2.getValue()));
        }
    }

    public ClientboundLevelChunkPacketData(RegistryFriendlyByteBuf registryFriendlyByteBuf, int n, int n2) {
        this.heightmaps = (Map)HEIGHTMAPS_STREAM_CODEC.decode(registryFriendlyByteBuf);
        int n3 = registryFriendlyByteBuf.readVarInt();
        if (n3 > 0x200000) {
            throw new RuntimeException("Chunk Packet trying to allocate too much memory on read.");
        }
        this.buffer = new byte[n3];
        registryFriendlyByteBuf.readBytes(this.buffer);
        this.blockEntitiesData = (List)BlockEntityInfo.LIST_STREAM_CODEC.decode(registryFriendlyByteBuf);
    }

    public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        HEIGHTMAPS_STREAM_CODEC.encode(registryFriendlyByteBuf, this.heightmaps);
        registryFriendlyByteBuf.writeVarInt(this.buffer.length);
        registryFriendlyByteBuf.writeBytes(this.buffer);
        BlockEntityInfo.LIST_STREAM_CODEC.encode(registryFriendlyByteBuf, this.blockEntitiesData);
    }

    private static int calculateChunkSize(LevelChunk levelChunk) {
        int n = 0;
        for (LevelChunkSection levelChunkSection : levelChunk.getSections()) {
            n += levelChunkSection.getSerializedSize();
        }
        return n;
    }

    private ByteBuf getWriteBuffer() {
        ByteBuf byteBuf = Unpooled.wrappedBuffer((byte[])this.buffer);
        byteBuf.writerIndex(0);
        return byteBuf;
    }

    public static void extractChunkData(FriendlyByteBuf friendlyByteBuf, LevelChunk levelChunk) {
        for (LevelChunkSection levelChunkSection : levelChunk.getSections()) {
            levelChunkSection.write(friendlyByteBuf);
        }
        if (friendlyByteBuf.writerIndex() != friendlyByteBuf.capacity()) {
            throw new IllegalStateException("Didn't fill chunk buffer: expected " + friendlyByteBuf.capacity() + " bytes, got " + friendlyByteBuf.writerIndex());
        }
    }

    public Consumer<BlockEntityTagOutput> getBlockEntitiesTagsConsumer(int n, int n2) {
        return blockEntityTagOutput -> this.getBlockEntitiesTags((BlockEntityTagOutput)blockEntityTagOutput, n, n2);
    }

    private void getBlockEntitiesTags(BlockEntityTagOutput blockEntityTagOutput, int n, int n2) {
        int n3 = 16 * n;
        int n4 = 16 * n2;
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        for (BlockEntityInfo blockEntityInfo : this.blockEntitiesData) {
            int n5 = n3 + SectionPos.sectionRelative(blockEntityInfo.packedXZ >> 4);
            int n6 = n4 + SectionPos.sectionRelative(blockEntityInfo.packedXZ);
            mutableBlockPos.set(n5, blockEntityInfo.y, n6);
            blockEntityTagOutput.accept(mutableBlockPos, blockEntityInfo.type, blockEntityInfo.tag);
        }
    }

    public FriendlyByteBuf getReadBuffer() {
        return new FriendlyByteBuf(Unpooled.wrappedBuffer((byte[])this.buffer));
    }

    public Map<Heightmap.Types, long[]> getHeightmaps() {
        return this.heightmaps;
    }

    static class BlockEntityInfo {
        public static final StreamCodec<RegistryFriendlyByteBuf, BlockEntityInfo> STREAM_CODEC = StreamCodec.ofMember(BlockEntityInfo::write, BlockEntityInfo::new);
        public static final StreamCodec<RegistryFriendlyByteBuf, List<BlockEntityInfo>> LIST_STREAM_CODEC = STREAM_CODEC.apply(ByteBufCodecs.list());
        final int packedXZ;
        final int y;
        final BlockEntityType<?> type;
        @Nullable
        final CompoundTag tag;

        private BlockEntityInfo(int n, int n2, BlockEntityType<?> blockEntityType, @Nullable CompoundTag compoundTag) {
            this.packedXZ = n;
            this.y = n2;
            this.type = blockEntityType;
            this.tag = compoundTag;
        }

        private BlockEntityInfo(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            this.packedXZ = registryFriendlyByteBuf.readByte();
            this.y = registryFriendlyByteBuf.readShort();
            this.type = (BlockEntityType)ByteBufCodecs.registry(Registries.BLOCK_ENTITY_TYPE).decode(registryFriendlyByteBuf);
            this.tag = registryFriendlyByteBuf.readNbt();
        }

        private void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
            registryFriendlyByteBuf.writeByte(this.packedXZ);
            registryFriendlyByteBuf.writeShort(this.y);
            ByteBufCodecs.registry(Registries.BLOCK_ENTITY_TYPE).encode(registryFriendlyByteBuf, this.type);
            registryFriendlyByteBuf.writeNbt(this.tag);
        }

        static BlockEntityInfo create(BlockEntity blockEntity) {
            CompoundTag compoundTag = blockEntity.getUpdateTag(blockEntity.getLevel().registryAccess());
            BlockPos blockPos = blockEntity.getBlockPos();
            int n = SectionPos.sectionRelative(blockPos.getX()) << 4 | SectionPos.sectionRelative(blockPos.getZ());
            return new BlockEntityInfo(n, blockPos.getY(), blockEntity.getType(), compoundTag.isEmpty() ? null : compoundTag);
        }
    }

    @FunctionalInterface
    public static interface BlockEntityTagOutput {
        public void accept(BlockPos var1, BlockEntityType<?> var2, @Nullable CompoundTag var3);
    }
}

