/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.floats.FloatConsumer
 */
package net.minecraft.client.sounds;

import it.unimi.dsi.fastutil.floats.FloatConsumer;
import java.io.IOException;
import java.nio.ByteBuffer;
import net.minecraft.client.sounds.ChunkedSampleByteBuf;
import net.minecraft.client.sounds.FiniteAudioStream;

public interface FloatSampleSource
extends FiniteAudioStream {
    public static final int EXPECTED_MAX_FRAME_SIZE = 8192;

    public boolean readChunk(FloatConsumer var1) throws IOException;

    @Override
    default public ByteBuffer read(int n) throws IOException {
        ChunkedSampleByteBuf chunkedSampleByteBuf = new ChunkedSampleByteBuf(n + 8192);
        while (this.readChunk(chunkedSampleByteBuf) && chunkedSampleByteBuf.size() < n) {
        }
        return chunkedSampleByteBuf.get();
    }

    @Override
    default public ByteBuffer readAll() throws IOException {
        ChunkedSampleByteBuf chunkedSampleByteBuf = new ChunkedSampleByteBuf(16384);
        while (this.readChunk(chunkedSampleByteBuf)) {
        }
        return chunkedSampleByteBuf.get();
    }
}

