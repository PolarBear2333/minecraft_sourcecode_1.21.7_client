/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  it.unimi.dsi.fastutil.floats.FloatConsumer
 *  org.lwjgl.BufferUtils
 */
package net.minecraft.client.sounds;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import java.nio.ByteBuffer;
import java.util.List;
import net.minecraft.util.Mth;
import org.lwjgl.BufferUtils;

public class ChunkedSampleByteBuf
implements FloatConsumer {
    private final List<ByteBuffer> buffers = Lists.newArrayList();
    private final int bufferSize;
    private int byteCount;
    private ByteBuffer currentBuffer;

    public ChunkedSampleByteBuf(int n) {
        this.bufferSize = n + 1 & 0xFFFFFFFE;
        this.currentBuffer = BufferUtils.createByteBuffer((int)n);
    }

    public void accept(float f) {
        if (this.currentBuffer.remaining() == 0) {
            this.currentBuffer.flip();
            this.buffers.add(this.currentBuffer);
            this.currentBuffer = BufferUtils.createByteBuffer((int)this.bufferSize);
        }
        int n = Mth.clamp((int)(f * 32767.5f - 0.5f), Short.MIN_VALUE, Short.MAX_VALUE);
        this.currentBuffer.putShort((short)n);
        this.byteCount += 2;
    }

    public ByteBuffer get() {
        this.currentBuffer.flip();
        if (this.buffers.isEmpty()) {
            return this.currentBuffer;
        }
        ByteBuffer byteBuffer = BufferUtils.createByteBuffer((int)this.byteCount);
        this.buffers.forEach(byteBuffer::put);
        byteBuffer.put(this.currentBuffer);
        byteBuffer.flip();
        return byteBuffer;
    }

    public int size() {
        return this.byteCount;
    }
}

