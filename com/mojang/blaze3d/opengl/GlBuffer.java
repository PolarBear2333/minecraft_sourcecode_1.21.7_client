/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.jtracy.MemoryPool
 *  com.mojang.jtracy.TracyClient
 *  javax.annotation.Nullable
 */
package com.mojang.blaze3d.opengl;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.DirectStateAccess;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.jtracy.MemoryPool;
import com.mojang.jtracy.TracyClient;
import java.nio.ByteBuffer;
import java.util.function.Supplier;
import javax.annotation.Nullable;

public class GlBuffer
extends GpuBuffer {
    protected static final MemoryPool MEMORY_POOl = TracyClient.createMemoryPool((String)"GPU Buffers");
    protected boolean closed;
    @Nullable
    protected final Supplier<String> label;
    private final DirectStateAccess dsa;
    protected final int handle;
    @Nullable
    protected ByteBuffer persistentBuffer;

    protected GlBuffer(@Nullable Supplier<String> supplier, DirectStateAccess directStateAccess, int n, int n2, int n3, @Nullable ByteBuffer byteBuffer) {
        super(n, n2);
        this.label = supplier;
        this.dsa = directStateAccess;
        this.handle = n3;
        this.persistentBuffer = byteBuffer;
        MEMORY_POOl.malloc((long)n3, n2);
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    @Override
    public void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        if (this.persistentBuffer != null) {
            this.dsa.unmapBuffer(this.handle);
            this.persistentBuffer = null;
        }
        GlStateManager._glDeleteBuffers(this.handle);
        MEMORY_POOl.free((long)this.handle);
    }

    public static class GlMappedView
    implements GpuBuffer.MappedView {
        private final Runnable unmap;
        private final GlBuffer buffer;
        private final ByteBuffer data;
        private boolean closed;

        protected GlMappedView(Runnable runnable, GlBuffer glBuffer, ByteBuffer byteBuffer) {
            this.unmap = runnable;
            this.buffer = glBuffer;
            this.data = byteBuffer;
        }

        @Override
        public ByteBuffer data() {
            return this.data;
        }

        @Override
        public void close() {
            if (this.closed) {
                return;
            }
            this.closed = true;
            this.unmap.run();
        }
    }
}

