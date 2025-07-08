/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.buffers;

import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import java.nio.ByteBuffer;

@DontObfuscate
public abstract class GpuBuffer
implements AutoCloseable {
    public static final int USAGE_MAP_READ = 1;
    public static final int USAGE_MAP_WRITE = 2;
    public static final int USAGE_HINT_CLIENT_STORAGE = 4;
    public static final int USAGE_COPY_DST = 8;
    public static final int USAGE_COPY_SRC = 16;
    public static final int USAGE_VERTEX = 32;
    public static final int USAGE_INDEX = 64;
    public static final int USAGE_UNIFORM = 128;
    public static final int USAGE_UNIFORM_TEXEL_BUFFER = 256;
    private final int usage;
    public int size;

    public GpuBuffer(int n, int n2) {
        this.size = n2;
        this.usage = n;
    }

    public int size() {
        return this.size;
    }

    public int usage() {
        return this.usage;
    }

    public abstract boolean isClosed();

    @Override
    public abstract void close();

    public GpuBufferSlice slice(int n, int n2) {
        if (n < 0 || n2 < 0 || n + n2 > this.size) {
            throw new IllegalArgumentException("Offset of " + n + " and length " + n2 + " would put new slice outside buffer's range (of 0," + n2 + ")");
        }
        return new GpuBufferSlice(this, n, n2);
    }

    public GpuBufferSlice slice() {
        return new GpuBufferSlice(this, 0, this.size);
    }

    @DontObfuscate
    public static interface MappedView
    extends AutoCloseable {
        public ByteBuffer data();

        @Override
        public void close();
    }
}

