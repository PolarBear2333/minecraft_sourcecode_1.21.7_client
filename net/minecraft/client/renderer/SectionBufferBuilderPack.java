/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import java.util.Arrays;
import java.util.Map;
import net.minecraft.Util;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;

public class SectionBufferBuilderPack
implements AutoCloseable {
    public static final int TOTAL_BUFFERS_SIZE = Arrays.stream(ChunkSectionLayer.values()).mapToInt(ChunkSectionLayer::bufferSize).sum();
    private final Map<ChunkSectionLayer, ByteBufferBuilder> buffers = Util.makeEnumMap(ChunkSectionLayer.class, chunkSectionLayer -> new ByteBufferBuilder(chunkSectionLayer.bufferSize()));

    public ByteBufferBuilder buffer(ChunkSectionLayer chunkSectionLayer) {
        return this.buffers.get((Object)chunkSectionLayer);
    }

    public void clearAll() {
        this.buffers.values().forEach(ByteBufferBuilder::clear);
    }

    public void discardAll() {
        this.buffers.values().forEach(ByteBufferBuilder::discard);
    }

    @Override
    public void close() {
        this.buffers.values().forEach(ByteBufferBuilder::close);
    }
}

