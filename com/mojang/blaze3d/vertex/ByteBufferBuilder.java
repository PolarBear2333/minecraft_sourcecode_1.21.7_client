/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.jtracy.MemoryPool
 *  com.mojang.jtracy.TracyClient
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.lwjgl.system.MemoryUtil
 *  org.lwjgl.system.MemoryUtil$MemoryAllocator
 *  org.slf4j.Logger
 */
package com.mojang.blaze3d.vertex;

import com.mojang.jtracy.MemoryPool;
import com.mojang.jtracy.TracyClient;
import com.mojang.logging.LogUtils;
import java.nio.ByteBuffer;
import javax.annotation.Nullable;
import net.minecraft.util.Mth;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

public class ByteBufferBuilder
implements AutoCloseable {
    private static final MemoryPool MEMORY_POOL = TracyClient.createMemoryPool((String)"ByteBufferBuilder");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final MemoryUtil.MemoryAllocator ALLOCATOR = MemoryUtil.getAllocator((boolean)false);
    private static final long DEFAULT_MAX_CAPACITY = 0xFFFFFFFFL;
    private static final int MAX_GROWTH_SIZE = 0x200000;
    private static final int BUFFER_FREED_GENERATION = -1;
    long pointer;
    private long capacity;
    private final long maxCapacity;
    private long writeOffset;
    private long nextResultOffset;
    private int resultCount;
    private int generation;

    public ByteBufferBuilder(int n, long l) {
        this.capacity = n;
        this.maxCapacity = l;
        this.pointer = ALLOCATOR.malloc((long)n);
        MEMORY_POOL.malloc(this.pointer, n);
        if (this.pointer == 0L) {
            throw new OutOfMemoryError("Failed to allocate " + n + " bytes");
        }
    }

    public ByteBufferBuilder(int n) {
        this(n, 0xFFFFFFFFL);
    }

    public static ByteBufferBuilder exactlySized(int n) {
        return new ByteBufferBuilder(n, n);
    }

    public long reserve(int n) {
        long l = this.writeOffset;
        long l2 = Math.addExact(l, (long)n);
        this.ensureCapacity(l2);
        this.writeOffset = l2;
        return Math.addExact(this.pointer, l);
    }

    private void ensureCapacity(long l) {
        if (l > this.capacity) {
            if (l > this.maxCapacity) {
                throw new IllegalArgumentException("Maximum capacity of ByteBufferBuilder (" + this.maxCapacity + ") exceeded, required " + l);
            }
            long l2 = Math.min(this.capacity, 0x200000L);
            long l3 = Mth.clamp(this.capacity + l2, l, this.maxCapacity);
            this.resize(l3);
        }
    }

    private void resize(long l) {
        MEMORY_POOL.free(this.pointer);
        this.pointer = ALLOCATOR.realloc(this.pointer, l);
        MEMORY_POOL.malloc(this.pointer, (int)Math.min(l, Integer.MAX_VALUE));
        LOGGER.debug("Needed to grow BufferBuilder buffer: Old size {} bytes, new size {} bytes.", (Object)this.capacity, (Object)l);
        if (this.pointer == 0L) {
            throw new OutOfMemoryError("Failed to resize buffer from " + this.capacity + " bytes to " + l + " bytes");
        }
        this.capacity = l;
    }

    @Nullable
    public Result build() {
        this.checkOpen();
        long l = this.nextResultOffset;
        long l2 = this.writeOffset - l;
        if (l2 == 0L) {
            return null;
        }
        if (l2 > Integer.MAX_VALUE) {
            throw new IllegalStateException("Cannot build buffer larger than 2147483647 bytes (was " + l2 + ")");
        }
        this.nextResultOffset = this.writeOffset;
        ++this.resultCount;
        return new Result(l, (int)l2, this.generation);
    }

    public void clear() {
        if (this.resultCount > 0) {
            LOGGER.warn("Clearing BufferBuilder with unused batches");
        }
        this.discard();
    }

    public void discard() {
        this.checkOpen();
        if (this.resultCount > 0) {
            this.discardResults();
            this.resultCount = 0;
        }
    }

    boolean isValid(int n) {
        return n == this.generation;
    }

    void freeResult() {
        if (--this.resultCount <= 0) {
            this.discardResults();
        }
    }

    private void discardResults() {
        long l = this.writeOffset - this.nextResultOffset;
        if (l > 0L) {
            MemoryUtil.memCopy((long)(this.pointer + this.nextResultOffset), (long)this.pointer, (long)l);
        }
        this.writeOffset = l;
        this.nextResultOffset = 0L;
        ++this.generation;
    }

    @Override
    public void close() {
        if (this.pointer != 0L) {
            MEMORY_POOL.free(this.pointer);
            ALLOCATOR.free(this.pointer);
            this.pointer = 0L;
            this.generation = -1;
        }
    }

    private void checkOpen() {
        if (this.pointer == 0L) {
            throw new IllegalStateException("Buffer has been freed");
        }
    }

    public class Result
    implements AutoCloseable {
        private final long offset;
        private final int capacity;
        private final int generation;
        private boolean closed;

        Result(long l, int n, int n2) {
            this.offset = l;
            this.capacity = n;
            this.generation = n2;
        }

        public ByteBuffer byteBuffer() {
            if (!ByteBufferBuilder.this.isValid(this.generation)) {
                throw new IllegalStateException("Buffer is no longer valid");
            }
            return MemoryUtil.memByteBuffer((long)(ByteBufferBuilder.this.pointer + this.offset), (int)this.capacity);
        }

        @Override
        public void close() {
            if (this.closed) {
                return;
            }
            this.closed = true;
            if (ByteBufferBuilder.this.isValid(this.generation)) {
                ByteBufferBuilder.this.freeResult();
            }
        }
    }
}

