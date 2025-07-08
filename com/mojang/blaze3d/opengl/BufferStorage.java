/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.lwjgl.opengl.GLCapabilities
 *  org.lwjgl.system.MemoryUtil
 */
package com.mojang.blaze3d.opengl;

import com.mojang.blaze3d.opengl.DirectStateAccess;
import com.mojang.blaze3d.opengl.GlBuffer;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlStateManager;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryUtil;

public abstract class BufferStorage {
    public static BufferStorage create(GLCapabilities gLCapabilities, Set<String> set) {
        if (gLCapabilities.GL_ARB_buffer_storage && GlDevice.USE_GL_ARB_buffer_storage) {
            set.add("GL_ARB_buffer_storage");
            return new Immutable();
        }
        return new Mutable();
    }

    public abstract GlBuffer createBuffer(DirectStateAccess var1, @Nullable Supplier<String> var2, int var3, int var4);

    public abstract GlBuffer createBuffer(DirectStateAccess var1, @Nullable Supplier<String> var2, int var3, ByteBuffer var4);

    public abstract GlBuffer.GlMappedView mapBuffer(DirectStateAccess var1, GlBuffer var2, int var3, int var4, int var5);

    static class Immutable
    extends BufferStorage {
        Immutable() {
        }

        @Override
        public GlBuffer createBuffer(DirectStateAccess directStateAccess, @Nullable Supplier<String> supplier, int n, int n2) {
            int n3 = directStateAccess.createBuffer();
            directStateAccess.bufferStorage(n3, n2, GlConst.bufferUsageToGlFlag(n));
            ByteBuffer byteBuffer = this.tryMapBufferPersistent(directStateAccess, n, n3, n2);
            return new GlBuffer(supplier, directStateAccess, n, n2, n3, byteBuffer);
        }

        @Override
        public GlBuffer createBuffer(DirectStateAccess directStateAccess, @Nullable Supplier<String> supplier, int n, ByteBuffer byteBuffer) {
            int n2 = directStateAccess.createBuffer();
            int n3 = byteBuffer.remaining();
            directStateAccess.bufferStorage(n2, byteBuffer, GlConst.bufferUsageToGlFlag(n));
            ByteBuffer byteBuffer2 = this.tryMapBufferPersistent(directStateAccess, n, n2, n3);
            return new GlBuffer(supplier, directStateAccess, n, n3, n2, byteBuffer2);
        }

        @Nullable
        private ByteBuffer tryMapBufferPersistent(DirectStateAccess directStateAccess, int n, int n2, int n3) {
            ByteBuffer byteBuffer;
            int n4 = 0;
            if ((n & 1) != 0) {
                n4 |= 1;
            }
            if ((n & 2) != 0) {
                n4 |= 0x12;
            }
            if (n4 != 0) {
                GlStateManager.clearGlErrors();
                byteBuffer = directStateAccess.mapBufferRange(n2, 0, n3, n4 | 0x40);
                if (byteBuffer == null) {
                    throw new IllegalStateException("Can't persistently map buffer, opengl error " + GlStateManager._getError());
                }
            } else {
                byteBuffer = null;
            }
            return byteBuffer;
        }

        @Override
        public GlBuffer.GlMappedView mapBuffer(DirectStateAccess directStateAccess, GlBuffer glBuffer, int n, int n2, int n3) {
            if (glBuffer.persistentBuffer == null) {
                throw new IllegalStateException("Somehow trying to map an unmappable buffer");
            }
            return new GlBuffer.GlMappedView(() -> {
                if ((n3 & 2) != 0) {
                    directStateAccess.flushMappedBufferRange(glBuffer.handle, n, n2);
                }
            }, glBuffer, MemoryUtil.memSlice((ByteBuffer)glBuffer.persistentBuffer, (int)n, (int)n2));
        }
    }

    static class Mutable
    extends BufferStorage {
        Mutable() {
        }

        @Override
        public GlBuffer createBuffer(DirectStateAccess directStateAccess, @Nullable Supplier<String> supplier, int n, int n2) {
            int n3 = directStateAccess.createBuffer();
            directStateAccess.bufferData(n3, n2, GlConst.bufferUsageToGlEnum(n));
            return new GlBuffer(supplier, directStateAccess, n, n2, n3, null);
        }

        @Override
        public GlBuffer createBuffer(DirectStateAccess directStateAccess, @Nullable Supplier<String> supplier, int n, ByteBuffer byteBuffer) {
            int n2 = directStateAccess.createBuffer();
            int n3 = byteBuffer.remaining();
            directStateAccess.bufferData(n2, byteBuffer, GlConst.bufferUsageToGlEnum(n));
            return new GlBuffer(supplier, directStateAccess, n, n3, n2, null);
        }

        @Override
        public GlBuffer.GlMappedView mapBuffer(DirectStateAccess directStateAccess, GlBuffer glBuffer, int n, int n2, int n3) {
            GlStateManager.clearGlErrors();
            ByteBuffer byteBuffer = directStateAccess.mapBufferRange(glBuffer.handle, n, n2, n3);
            if (byteBuffer == null) {
                throw new IllegalStateException("Can't map buffer, opengl error " + GlStateManager._getError());
            }
            return new GlBuffer.GlMappedView(() -> directStateAccess.unmapBuffer(glBuffer.handle), glBuffer, byteBuffer);
        }
    }
}

