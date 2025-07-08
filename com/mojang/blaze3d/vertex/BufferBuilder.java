/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.lwjgl.system.MemoryUtil
 */
package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import java.nio.ByteOrder;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.lwjgl.system.MemoryUtil;

public class BufferBuilder
implements VertexConsumer {
    private static final int MAX_VERTEX_COUNT = 0xFFFFFF;
    private static final long NOT_BUILDING = -1L;
    private static final long UNKNOWN_ELEMENT = -1L;
    private static final boolean IS_LITTLE_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;
    private final ByteBufferBuilder buffer;
    private long vertexPointer = -1L;
    private int vertices;
    private final VertexFormat format;
    private final VertexFormat.Mode mode;
    private final boolean fastFormat;
    private final boolean fullFormat;
    private final int vertexSize;
    private final int initialElementsToFill;
    private final int[] offsetsByElement;
    private int elementsToFill;
    private boolean building = true;

    public BufferBuilder(ByteBufferBuilder byteBufferBuilder, VertexFormat.Mode mode, VertexFormat vertexFormat) {
        if (!vertexFormat.contains(VertexFormatElement.POSITION)) {
            throw new IllegalArgumentException("Cannot build mesh with no position element");
        }
        this.buffer = byteBufferBuilder;
        this.mode = mode;
        this.format = vertexFormat;
        this.vertexSize = vertexFormat.getVertexSize();
        this.initialElementsToFill = vertexFormat.getElementsMask() & ~VertexFormatElement.POSITION.mask();
        this.offsetsByElement = vertexFormat.getOffsetsByElement();
        boolean bl = vertexFormat == DefaultVertexFormat.NEW_ENTITY;
        boolean bl2 = vertexFormat == DefaultVertexFormat.BLOCK;
        this.fastFormat = bl || bl2;
        this.fullFormat = bl;
    }

    @Nullable
    public MeshData build() {
        this.ensureBuilding();
        this.endLastVertex();
        MeshData meshData = this.storeMesh();
        this.building = false;
        this.vertexPointer = -1L;
        return meshData;
    }

    public MeshData buildOrThrow() {
        MeshData meshData = this.build();
        if (meshData == null) {
            throw new IllegalStateException("BufferBuilder was empty");
        }
        return meshData;
    }

    private void ensureBuilding() {
        if (!this.building) {
            throw new IllegalStateException("Not building!");
        }
    }

    @Nullable
    private MeshData storeMesh() {
        if (this.vertices == 0) {
            return null;
        }
        ByteBufferBuilder.Result result = this.buffer.build();
        if (result == null) {
            return null;
        }
        int n = this.mode.indexCount(this.vertices);
        VertexFormat.IndexType indexType = VertexFormat.IndexType.least(this.vertices);
        return new MeshData(result, new MeshData.DrawState(this.format, this.vertices, n, this.mode, indexType));
    }

    private long beginVertex() {
        long l;
        this.ensureBuilding();
        this.endLastVertex();
        if (this.vertices >= 0xFFFFFF) {
            throw new IllegalStateException("Trying to write too many vertices (>16777215) into BufferBuilder");
        }
        ++this.vertices;
        this.vertexPointer = l = this.buffer.reserve(this.vertexSize);
        return l;
    }

    private long beginElement(VertexFormatElement vertexFormatElement) {
        int n = this.elementsToFill;
        int n2 = n & ~vertexFormatElement.mask();
        if (n2 == n) {
            return -1L;
        }
        this.elementsToFill = n2;
        long l = this.vertexPointer;
        if (l == -1L) {
            throw new IllegalArgumentException("Not currently building vertex");
        }
        return l + (long)this.offsetsByElement[vertexFormatElement.id()];
    }

    private void endLastVertex() {
        if (this.vertices == 0) {
            return;
        }
        if (this.elementsToFill != 0) {
            String string = VertexFormatElement.elementsFromMask(this.elementsToFill).map(this.format::getElementName).collect(Collectors.joining(", "));
            throw new IllegalStateException("Missing elements in vertex: " + string);
        }
        if (this.mode == VertexFormat.Mode.LINES || this.mode == VertexFormat.Mode.LINE_STRIP) {
            long l = this.buffer.reserve(this.vertexSize);
            MemoryUtil.memCopy((long)(l - (long)this.vertexSize), (long)l, (long)this.vertexSize);
            ++this.vertices;
        }
    }

    private static void putRgba(long l, int n) {
        int n2 = ARGB.toABGR(n);
        MemoryUtil.memPutInt((long)l, (int)(IS_LITTLE_ENDIAN ? n2 : Integer.reverseBytes(n2)));
    }

    private static void putPackedUv(long l, int n) {
        if (IS_LITTLE_ENDIAN) {
            MemoryUtil.memPutInt((long)l, (int)n);
        } else {
            MemoryUtil.memPutShort((long)l, (short)((short)(n & 0xFFFF)));
            MemoryUtil.memPutShort((long)(l + 2L), (short)((short)(n >> 16 & 0xFFFF)));
        }
    }

    @Override
    public VertexConsumer addVertex(float f, float f2, float f3) {
        long l = this.beginVertex() + (long)this.offsetsByElement[VertexFormatElement.POSITION.id()];
        this.elementsToFill = this.initialElementsToFill;
        MemoryUtil.memPutFloat((long)l, (float)f);
        MemoryUtil.memPutFloat((long)(l + 4L), (float)f2);
        MemoryUtil.memPutFloat((long)(l + 8L), (float)f3);
        return this;
    }

    @Override
    public VertexConsumer setColor(int n, int n2, int n3, int n4) {
        long l = this.beginElement(VertexFormatElement.COLOR);
        if (l != -1L) {
            MemoryUtil.memPutByte((long)l, (byte)((byte)n));
            MemoryUtil.memPutByte((long)(l + 1L), (byte)((byte)n2));
            MemoryUtil.memPutByte((long)(l + 2L), (byte)((byte)n3));
            MemoryUtil.memPutByte((long)(l + 3L), (byte)((byte)n4));
        }
        return this;
    }

    @Override
    public VertexConsumer setColor(int n) {
        long l = this.beginElement(VertexFormatElement.COLOR);
        if (l != -1L) {
            BufferBuilder.putRgba(l, n);
        }
        return this;
    }

    @Override
    public VertexConsumer setUv(float f, float f2) {
        long l = this.beginElement(VertexFormatElement.UV0);
        if (l != -1L) {
            MemoryUtil.memPutFloat((long)l, (float)f);
            MemoryUtil.memPutFloat((long)(l + 4L), (float)f2);
        }
        return this;
    }

    @Override
    public VertexConsumer setUv1(int n, int n2) {
        return this.uvShort((short)n, (short)n2, VertexFormatElement.UV1);
    }

    @Override
    public VertexConsumer setOverlay(int n) {
        long l = this.beginElement(VertexFormatElement.UV1);
        if (l != -1L) {
            BufferBuilder.putPackedUv(l, n);
        }
        return this;
    }

    @Override
    public VertexConsumer setUv2(int n, int n2) {
        return this.uvShort((short)n, (short)n2, VertexFormatElement.UV2);
    }

    @Override
    public VertexConsumer setLight(int n) {
        long l = this.beginElement(VertexFormatElement.UV2);
        if (l != -1L) {
            BufferBuilder.putPackedUv(l, n);
        }
        return this;
    }

    private VertexConsumer uvShort(short s, short s2, VertexFormatElement vertexFormatElement) {
        long l = this.beginElement(vertexFormatElement);
        if (l != -1L) {
            MemoryUtil.memPutShort((long)l, (short)s);
            MemoryUtil.memPutShort((long)(l + 2L), (short)s2);
        }
        return this;
    }

    @Override
    public VertexConsumer setNormal(float f, float f2, float f3) {
        long l = this.beginElement(VertexFormatElement.NORMAL);
        if (l != -1L) {
            MemoryUtil.memPutByte((long)l, (byte)BufferBuilder.normalIntValue(f));
            MemoryUtil.memPutByte((long)(l + 1L), (byte)BufferBuilder.normalIntValue(f2));
            MemoryUtil.memPutByte((long)(l + 2L), (byte)BufferBuilder.normalIntValue(f3));
        }
        return this;
    }

    private static byte normalIntValue(float f) {
        return (byte)((int)(Mth.clamp(f, -1.0f, 1.0f) * 127.0f) & 0xFF);
    }

    @Override
    public void addVertex(float f, float f2, float f3, int n, float f4, float f5, int n2, int n3, float f6, float f7, float f8) {
        if (this.fastFormat) {
            long l;
            long l2 = this.beginVertex();
            MemoryUtil.memPutFloat((long)(l2 + 0L), (float)f);
            MemoryUtil.memPutFloat((long)(l2 + 4L), (float)f2);
            MemoryUtil.memPutFloat((long)(l2 + 8L), (float)f3);
            BufferBuilder.putRgba(l2 + 12L, n);
            MemoryUtil.memPutFloat((long)(l2 + 16L), (float)f4);
            MemoryUtil.memPutFloat((long)(l2 + 20L), (float)f5);
            if (this.fullFormat) {
                BufferBuilder.putPackedUv(l2 + 24L, n2);
                l = l2 + 28L;
            } else {
                l = l2 + 24L;
            }
            BufferBuilder.putPackedUv(l + 0L, n3);
            MemoryUtil.memPutByte((long)(l + 4L), (byte)BufferBuilder.normalIntValue(f6));
            MemoryUtil.memPutByte((long)(l + 5L), (byte)BufferBuilder.normalIntValue(f7));
            MemoryUtil.memPutByte((long)(l + 6L), (byte)BufferBuilder.normalIntValue(f8));
            return;
        }
        VertexConsumer.super.addVertex(f, f2, f3, n, f4, f5, n2, n3, f6, f7, f8);
    }
}

