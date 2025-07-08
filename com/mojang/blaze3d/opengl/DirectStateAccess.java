/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.lwjgl.opengl.ARBBufferStorage
 *  org.lwjgl.opengl.ARBDirectStateAccess
 *  org.lwjgl.opengl.GL30
 *  org.lwjgl.opengl.GL31
 *  org.lwjgl.opengl.GLCapabilities
 */
package com.mojang.blaze3d.opengl;

import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlStateManager;
import java.nio.ByteBuffer;
import java.util.Set;
import javax.annotation.Nullable;
import org.lwjgl.opengl.ARBBufferStorage;
import org.lwjgl.opengl.ARBDirectStateAccess;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GLCapabilities;

public abstract class DirectStateAccess {
    public static DirectStateAccess create(GLCapabilities gLCapabilities, Set<String> set) {
        if (gLCapabilities.GL_ARB_direct_state_access && GlDevice.USE_GL_ARB_direct_state_access) {
            set.add("GL_ARB_direct_state_access");
            return new Core();
        }
        return new Emulated();
    }

    abstract int createBuffer();

    abstract void bufferData(int var1, long var2, int var4);

    abstract void bufferData(int var1, ByteBuffer var2, int var3);

    abstract void bufferSubData(int var1, int var2, ByteBuffer var3);

    abstract void bufferStorage(int var1, long var2, int var4);

    abstract void bufferStorage(int var1, ByteBuffer var2, int var3);

    @Nullable
    abstract ByteBuffer mapBufferRange(int var1, int var2, int var3, int var4);

    abstract void unmapBuffer(int var1);

    abstract int createFrameBufferObject();

    abstract void bindFrameBufferTextures(int var1, int var2, int var3, int var4, int var5);

    abstract void blitFrameBuffers(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, int var11, int var12);

    abstract void flushMappedBufferRange(int var1, int var2, int var3);

    abstract void copyBufferSubData(int var1, int var2, int var3, int var4, int var5);

    static class Core
    extends DirectStateAccess {
        Core() {
        }

        @Override
        int createBuffer() {
            return ARBDirectStateAccess.glCreateBuffers();
        }

        @Override
        void bufferData(int n, long l, int n2) {
            ARBDirectStateAccess.glNamedBufferData((int)n, (long)l, (int)n2);
        }

        @Override
        void bufferData(int n, ByteBuffer byteBuffer, int n2) {
            ARBDirectStateAccess.glNamedBufferData((int)n, (ByteBuffer)byteBuffer, (int)n2);
        }

        @Override
        void bufferSubData(int n, int n2, ByteBuffer byteBuffer) {
            ARBDirectStateAccess.glNamedBufferSubData((int)n, (long)n2, (ByteBuffer)byteBuffer);
        }

        @Override
        void bufferStorage(int n, long l, int n2) {
            ARBDirectStateAccess.glNamedBufferStorage((int)n, (long)l, (int)n2);
        }

        @Override
        void bufferStorage(int n, ByteBuffer byteBuffer, int n2) {
            ARBDirectStateAccess.glNamedBufferStorage((int)n, (ByteBuffer)byteBuffer, (int)n2);
        }

        @Override
        @Nullable
        ByteBuffer mapBufferRange(int n, int n2, int n3, int n4) {
            return ARBDirectStateAccess.glMapNamedBufferRange((int)n, (long)n2, (long)n3, (int)n4);
        }

        @Override
        void unmapBuffer(int n) {
            ARBDirectStateAccess.glUnmapNamedBuffer((int)n);
        }

        @Override
        public int createFrameBufferObject() {
            return ARBDirectStateAccess.glCreateFramebuffers();
        }

        @Override
        public void bindFrameBufferTextures(int n, int n2, int n3, int n4, int n5) {
            ARBDirectStateAccess.glNamedFramebufferTexture((int)n, (int)36064, (int)n2, (int)n4);
            ARBDirectStateAccess.glNamedFramebufferTexture((int)n, (int)36096, (int)n3, (int)n4);
            if (n5 != 0) {
                GlStateManager._glBindFramebuffer(n5, n);
            }
        }

        @Override
        public void blitFrameBuffers(int n, int n2, int n3, int n4, int n5, int n6, int n7, int n8, int n9, int n10, int n11, int n12) {
            ARBDirectStateAccess.glBlitNamedFramebuffer((int)n, (int)n2, (int)n3, (int)n4, (int)n5, (int)n6, (int)n7, (int)n8, (int)n9, (int)n10, (int)n11, (int)n12);
        }

        @Override
        void flushMappedBufferRange(int n, int n2, int n3) {
            ARBDirectStateAccess.glFlushMappedNamedBufferRange((int)n, (long)n2, (long)n3);
        }

        @Override
        void copyBufferSubData(int n, int n2, int n3, int n4, int n5) {
            ARBDirectStateAccess.glCopyNamedBufferSubData((int)n, (int)n2, (long)n3, (long)n4, (long)n5);
        }
    }

    static class Emulated
    extends DirectStateAccess {
        Emulated() {
        }

        @Override
        int createBuffer() {
            return GlStateManager._glGenBuffers();
        }

        @Override
        void bufferData(int n, long l, int n2) {
            GlStateManager._glBindBuffer(36663, n);
            GlStateManager._glBufferData(36663, l, GlConst.bufferUsageToGlEnum(n2));
            GlStateManager._glBindBuffer(36663, 0);
        }

        @Override
        void bufferData(int n, ByteBuffer byteBuffer, int n2) {
            GlStateManager._glBindBuffer(36663, n);
            GlStateManager._glBufferData(36663, byteBuffer, GlConst.bufferUsageToGlEnum(n2));
            GlStateManager._glBindBuffer(36663, 0);
        }

        @Override
        void bufferSubData(int n, int n2, ByteBuffer byteBuffer) {
            GlStateManager._glBindBuffer(36663, n);
            GlStateManager._glBufferSubData(36663, n2, byteBuffer);
            GlStateManager._glBindBuffer(36663, 0);
        }

        @Override
        void bufferStorage(int n, long l, int n2) {
            GlStateManager._glBindBuffer(36663, n);
            ARBBufferStorage.glBufferStorage((int)36663, (long)l, (int)n2);
            GlStateManager._glBindBuffer(36663, 0);
        }

        @Override
        void bufferStorage(int n, ByteBuffer byteBuffer, int n2) {
            GlStateManager._glBindBuffer(36663, n);
            ARBBufferStorage.glBufferStorage((int)36663, (ByteBuffer)byteBuffer, (int)n2);
            GlStateManager._glBindBuffer(36663, 0);
        }

        @Override
        @Nullable
        ByteBuffer mapBufferRange(int n, int n2, int n3, int n4) {
            GlStateManager._glBindBuffer(36663, n);
            ByteBuffer byteBuffer = GlStateManager._glMapBufferRange(36663, n2, n3, n4);
            GlStateManager._glBindBuffer(36663, 0);
            return byteBuffer;
        }

        @Override
        void unmapBuffer(int n) {
            GlStateManager._glBindBuffer(36663, n);
            GlStateManager._glUnmapBuffer(36663);
            GlStateManager._glBindBuffer(36663, 0);
        }

        @Override
        void flushMappedBufferRange(int n, int n2, int n3) {
            GlStateManager._glBindBuffer(36663, n);
            GL30.glFlushMappedBufferRange((int)36663, (long)n2, (long)n3);
            GlStateManager._glBindBuffer(36663, 0);
        }

        @Override
        void copyBufferSubData(int n, int n2, int n3, int n4, int n5) {
            GlStateManager._glBindBuffer(36662, n);
            GlStateManager._glBindBuffer(36663, n2);
            GL31.glCopyBufferSubData((int)36662, (int)36663, (long)n3, (long)n4, (long)n5);
            GlStateManager._glBindBuffer(36662, 0);
            GlStateManager._glBindBuffer(36663, 0);
        }

        @Override
        public int createFrameBufferObject() {
            return GlStateManager.glGenFramebuffers();
        }

        @Override
        public void bindFrameBufferTextures(int n, int n2, int n3, int n4, int n5) {
            int n6 = n5 == 0 ? 36009 : n5;
            int n7 = GlStateManager.getFrameBuffer(n6);
            GlStateManager._glBindFramebuffer(n6, n);
            GlStateManager._glFramebufferTexture2D(n6, 36064, 3553, n2, n4);
            GlStateManager._glFramebufferTexture2D(n6, 36096, 3553, n3, n4);
            if (n5 == 0) {
                GlStateManager._glBindFramebuffer(n6, n7);
            }
        }

        @Override
        public void blitFrameBuffers(int n, int n2, int n3, int n4, int n5, int n6, int n7, int n8, int n9, int n10, int n11, int n12) {
            int n13 = GlStateManager.getFrameBuffer(36008);
            int n14 = GlStateManager.getFrameBuffer(36009);
            GlStateManager._glBindFramebuffer(36008, n);
            GlStateManager._glBindFramebuffer(36009, n2);
            GlStateManager._glBlitFrameBuffer(n3, n4, n5, n6, n7, n8, n9, n10, n11, n12);
            GlStateManager._glBindFramebuffer(36008, n13);
            GlStateManager._glBindFramebuffer(36009, n14);
        }
    }
}

