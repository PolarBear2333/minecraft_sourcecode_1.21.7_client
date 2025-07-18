/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  io.netty.buffer.ByteBuf
 *  javax.annotation.Nullable
 */
package net.minecraft.network.protocol.game;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import java.util.BitSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.lighting.LevelLightEngine;

public class ClientboundLightUpdatePacketData {
    private static final StreamCodec<ByteBuf, byte[]> DATA_LAYER_STREAM_CODEC = ByteBufCodecs.byteArray(2048);
    private final BitSet skyYMask;
    private final BitSet blockYMask;
    private final BitSet emptySkyYMask;
    private final BitSet emptyBlockYMask;
    private final List<byte[]> skyUpdates;
    private final List<byte[]> blockUpdates;

    public ClientboundLightUpdatePacketData(ChunkPos chunkPos, LevelLightEngine levelLightEngine, @Nullable BitSet bitSet, @Nullable BitSet bitSet2) {
        this.skyYMask = new BitSet();
        this.blockYMask = new BitSet();
        this.emptySkyYMask = new BitSet();
        this.emptyBlockYMask = new BitSet();
        this.skyUpdates = Lists.newArrayList();
        this.blockUpdates = Lists.newArrayList();
        for (int i = 0; i < levelLightEngine.getLightSectionCount(); ++i) {
            if (bitSet == null || bitSet.get(i)) {
                this.prepareSectionData(chunkPos, levelLightEngine, LightLayer.SKY, i, this.skyYMask, this.emptySkyYMask, this.skyUpdates);
            }
            if (bitSet2 != null && !bitSet2.get(i)) continue;
            this.prepareSectionData(chunkPos, levelLightEngine, LightLayer.BLOCK, i, this.blockYMask, this.emptyBlockYMask, this.blockUpdates);
        }
    }

    public ClientboundLightUpdatePacketData(FriendlyByteBuf friendlyByteBuf, int n, int n2) {
        this.skyYMask = friendlyByteBuf.readBitSet();
        this.blockYMask = friendlyByteBuf.readBitSet();
        this.emptySkyYMask = friendlyByteBuf.readBitSet();
        this.emptyBlockYMask = friendlyByteBuf.readBitSet();
        this.skyUpdates = friendlyByteBuf.readList(DATA_LAYER_STREAM_CODEC);
        this.blockUpdates = friendlyByteBuf.readList(DATA_LAYER_STREAM_CODEC);
    }

    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeBitSet(this.skyYMask);
        friendlyByteBuf.writeBitSet(this.blockYMask);
        friendlyByteBuf.writeBitSet(this.emptySkyYMask);
        friendlyByteBuf.writeBitSet(this.emptyBlockYMask);
        friendlyByteBuf.writeCollection(this.skyUpdates, DATA_LAYER_STREAM_CODEC);
        friendlyByteBuf.writeCollection(this.blockUpdates, DATA_LAYER_STREAM_CODEC);
    }

    private void prepareSectionData(ChunkPos chunkPos, LevelLightEngine levelLightEngine, LightLayer lightLayer, int n, BitSet bitSet, BitSet bitSet2, List<byte[]> list) {
        DataLayer dataLayer = levelLightEngine.getLayerListener(lightLayer).getDataLayerData(SectionPos.of(chunkPos, levelLightEngine.getMinLightSection() + n));
        if (dataLayer != null) {
            if (dataLayer.isEmpty()) {
                bitSet2.set(n);
            } else {
                bitSet.set(n);
                list.add(dataLayer.copy().getData());
            }
        }
    }

    public BitSet getSkyYMask() {
        return this.skyYMask;
    }

    public BitSet getEmptySkyYMask() {
        return this.emptySkyYMask;
    }

    public List<byte[]> getSkyUpdates() {
        return this.skyUpdates;
    }

    public BitSet getBlockYMask() {
        return this.blockYMask;
    }

    public BitSet getEmptyBlockYMask() {
        return this.emptyBlockYMask;
    }

    public List<byte[]> getBlockUpdates() {
        return this.blockUpdates;
    }
}

