/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntConsumer
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.mutable.MutableLong
 *  org.joml.Vector3f
 *  org.lwjgl.system.MemoryUtil
 */
package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.blaze3d.vertex.VertexSorting;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import javax.annotation.Nullable;
import org.apache.commons.lang3.mutable.MutableLong;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

public class MeshData
implements AutoCloseable {
    private final ByteBufferBuilder.Result vertexBuffer;
    @Nullable
    private ByteBufferBuilder.Result indexBuffer;
    private final DrawState drawState;

    public MeshData(ByteBufferBuilder.Result result, DrawState drawState) {
        this.vertexBuffer = result;
        this.drawState = drawState;
    }

    private static Vector3f[] unpackQuadCentroids(ByteBuffer byteBuffer, int n, VertexFormat vertexFormat) {
        int n2 = vertexFormat.getOffset(VertexFormatElement.POSITION);
        if (n2 == -1) {
            throw new IllegalArgumentException("Cannot identify quad centers with no position element");
        }
        FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
        int n3 = vertexFormat.getVertexSize() / 4;
        int n4 = n3 * 4;
        int n5 = n / 4;
        Vector3f[] vector3fArray = new Vector3f[n5];
        for (int i = 0; i < n5; ++i) {
            int n6 = i * n4 + n2;
            int n7 = n6 + n3 * 2;
            float f = floatBuffer.get(n6 + 0);
            float f2 = floatBuffer.get(n6 + 1);
            float f3 = floatBuffer.get(n6 + 2);
            float f4 = floatBuffer.get(n7 + 0);
            float f5 = floatBuffer.get(n7 + 1);
            float f6 = floatBuffer.get(n7 + 2);
            vector3fArray[i] = new Vector3f((f + f4) / 2.0f, (f2 + f5) / 2.0f, (f3 + f6) / 2.0f);
        }
        return vector3fArray;
    }

    public ByteBuffer vertexBuffer() {
        return this.vertexBuffer.byteBuffer();
    }

    @Nullable
    public ByteBuffer indexBuffer() {
        return this.indexBuffer != null ? this.indexBuffer.byteBuffer() : null;
    }

    public DrawState drawState() {
        return this.drawState;
    }

    @Nullable
    public SortState sortQuads(ByteBufferBuilder byteBufferBuilder, VertexSorting vertexSorting) {
        if (this.drawState.mode() != VertexFormat.Mode.QUADS) {
            return null;
        }
        Vector3f[] vector3fArray = MeshData.unpackQuadCentroids(this.vertexBuffer.byteBuffer(), this.drawState.vertexCount(), this.drawState.format());
        SortState sortState = new SortState(vector3fArray, this.drawState.indexType());
        this.indexBuffer = sortState.buildSortedIndexBuffer(byteBufferBuilder, vertexSorting);
        return sortState;
    }

    @Override
    public void close() {
        this.vertexBuffer.close();
        if (this.indexBuffer != null) {
            this.indexBuffer.close();
        }
    }

    public record DrawState(VertexFormat format, int vertexCount, int indexCount, VertexFormat.Mode mode, VertexFormat.IndexType indexType) {
    }

    public record SortState(Vector3f[] centroids, VertexFormat.IndexType indexType) {
        @Nullable
        public ByteBufferBuilder.Result buildSortedIndexBuffer(ByteBufferBuilder byteBufferBuilder, VertexSorting vertexSorting) {
            int[] nArray = vertexSorting.sort(this.centroids);
            long l = byteBufferBuilder.reserve(nArray.length * 6 * this.indexType.bytes);
            IntConsumer intConsumer = this.indexWriter(l, this.indexType);
            for (int n : nArray) {
                intConsumer.accept(n * 4 + 0);
                intConsumer.accept(n * 4 + 1);
                intConsumer.accept(n * 4 + 2);
                intConsumer.accept(n * 4 + 2);
                intConsumer.accept(n * 4 + 3);
                intConsumer.accept(n * 4 + 0);
            }
            return byteBufferBuilder.build();
        }

        private IntConsumer indexWriter(long l, VertexFormat.IndexType indexType) {
            MutableLong mutableLong = new MutableLong(l);
            return switch (indexType) {
                default -> throw new MatchException(null, null);
                case VertexFormat.IndexType.SHORT -> n -> MemoryUtil.memPutShort((long)mutableLong.getAndAdd(2L), (short)((short)n));
                case VertexFormat.IndexType.INT -> n -> MemoryUtil.memPutInt((long)mutableLong.getAndAdd(4L), (int)n);
            };
        }
    }
}

