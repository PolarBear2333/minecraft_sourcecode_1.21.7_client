/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 *  javax.annotation.Nullable
 */
package com.mojang.blaze3d.vertex;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;

@DontObfuscate
public class VertexFormat {
    public static final int UNKNOWN_ELEMENT = -1;
    private static final boolean USE_STAGING_BUFFER_WORKAROUND = Util.getPlatform() == Util.OS.WINDOWS && Util.isAarch64();
    @Nullable
    private static GpuBuffer UPLOAD_STAGING_BUFFER;
    private final List<VertexFormatElement> elements;
    private final List<String> names;
    private final int vertexSize;
    private final int elementsMask;
    private final int[] offsetsByElement = new int[32];
    @Nullable
    private GpuBuffer immediateDrawVertexBuffer;
    @Nullable
    private GpuBuffer immediateDrawIndexBuffer;

    VertexFormat(List<VertexFormatElement> list, List<String> list2, IntList intList, int n3) {
        this.elements = list;
        this.names = list2;
        this.vertexSize = n3;
        this.elementsMask = list.stream().mapToInt(VertexFormatElement::mask).reduce(0, (n, n2) -> n | n2);
        for (int i = 0; i < this.offsetsByElement.length; ++i) {
            VertexFormatElement vertexFormatElement = VertexFormatElement.byId(i);
            int n4 = vertexFormatElement != null ? list.indexOf(vertexFormatElement) : -1;
            this.offsetsByElement[i] = n4 != -1 ? intList.getInt(n4) : -1;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public String toString() {
        return "VertexFormat" + String.valueOf(this.names);
    }

    public int getVertexSize() {
        return this.vertexSize;
    }

    public List<VertexFormatElement> getElements() {
        return this.elements;
    }

    public List<String> getElementAttributeNames() {
        return this.names;
    }

    public int[] getOffsetsByElement() {
        return this.offsetsByElement;
    }

    public int getOffset(VertexFormatElement vertexFormatElement) {
        return this.offsetsByElement[vertexFormatElement.id()];
    }

    public boolean contains(VertexFormatElement vertexFormatElement) {
        return (this.elementsMask & vertexFormatElement.mask()) != 0;
    }

    public int getElementsMask() {
        return this.elementsMask;
    }

    public String getElementName(VertexFormatElement vertexFormatElement) {
        int n = this.elements.indexOf(vertexFormatElement);
        if (n == -1) {
            throw new IllegalArgumentException(String.valueOf(vertexFormatElement) + " is not contained in format");
        }
        return this.names.get(n);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof VertexFormat)) return false;
        VertexFormat vertexFormat = (VertexFormat)object;
        if (this.elementsMask != vertexFormat.elementsMask) return false;
        if (this.vertexSize != vertexFormat.vertexSize) return false;
        if (!this.names.equals(vertexFormat.names)) return false;
        if (!Arrays.equals(this.offsetsByElement, vertexFormat.offsetsByElement)) return false;
        return true;
    }

    public int hashCode() {
        return this.elementsMask * 31 + Arrays.hashCode(this.offsetsByElement);
    }

    private static GpuBuffer uploadToBuffer(@Nullable GpuBuffer gpuBuffer, ByteBuffer byteBuffer, int n, Supplier<String> supplier) {
        GpuDevice gpuDevice = RenderSystem.getDevice();
        if (gpuBuffer == null) {
            gpuBuffer = gpuDevice.createBuffer(supplier, n, byteBuffer);
        } else {
            CommandEncoder commandEncoder = gpuDevice.createCommandEncoder();
            if (gpuBuffer.size() < byteBuffer.remaining()) {
                gpuBuffer.close();
                gpuBuffer = gpuDevice.createBuffer(supplier, n, byteBuffer);
            } else {
                commandEncoder.writeToBuffer(gpuBuffer.slice(), byteBuffer);
            }
        }
        return gpuBuffer;
    }

    private GpuBuffer uploadToBufferWithWorkaround(@Nullable GpuBuffer gpuBuffer, ByteBuffer byteBuffer, int n, Supplier<String> supplier) {
        if (USE_STAGING_BUFFER_WORKAROUND) {
            GpuDevice gpuDevice = RenderSystem.getDevice();
            if (gpuBuffer == null) {
                gpuBuffer = gpuDevice.createBuffer(supplier, n, byteBuffer);
            } else {
                CommandEncoder commandEncoder = gpuDevice.createCommandEncoder();
                if (gpuBuffer.size() < byteBuffer.remaining()) {
                    gpuBuffer.close();
                    gpuBuffer = gpuDevice.createBuffer(supplier, n, byteBuffer);
                } else {
                    UPLOAD_STAGING_BUFFER = VertexFormat.uploadToBuffer(UPLOAD_STAGING_BUFFER, byteBuffer, n, supplier);
                    commandEncoder.copyToBuffer(UPLOAD_STAGING_BUFFER.slice(0, byteBuffer.remaining()), gpuBuffer.slice(0, byteBuffer.remaining()));
                }
            }
            return gpuBuffer;
        }
        return VertexFormat.uploadToBuffer(gpuBuffer, byteBuffer, n, supplier);
    }

    public GpuBuffer uploadImmediateVertexBuffer(ByteBuffer byteBuffer) {
        this.immediateDrawVertexBuffer = this.uploadToBufferWithWorkaround(this.immediateDrawVertexBuffer, byteBuffer, 40, () -> "Immediate vertex buffer for " + String.valueOf(this));
        return this.immediateDrawVertexBuffer;
    }

    public GpuBuffer uploadImmediateIndexBuffer(ByteBuffer byteBuffer) {
        this.immediateDrawIndexBuffer = this.uploadToBufferWithWorkaround(this.immediateDrawIndexBuffer, byteBuffer, 72, () -> "Immediate index buffer for " + String.valueOf(this));
        return this.immediateDrawIndexBuffer;
    }

    @DontObfuscate
    public static class Builder {
        private final ImmutableMap.Builder<String, VertexFormatElement> elements = ImmutableMap.builder();
        private final IntList offsets = new IntArrayList();
        private int offset;

        Builder() {
        }

        public Builder add(String string, VertexFormatElement vertexFormatElement) {
            this.elements.put((Object)string, (Object)vertexFormatElement);
            this.offsets.add(this.offset);
            this.offset += vertexFormatElement.byteSize();
            return this;
        }

        public Builder padding(int n) {
            this.offset += n;
            return this;
        }

        public VertexFormat build() {
            ImmutableMap immutableMap = this.elements.buildOrThrow();
            ImmutableList immutableList = immutableMap.values().asList();
            ImmutableList immutableList2 = immutableMap.keySet().asList();
            return new VertexFormat((List<VertexFormatElement>)immutableList, (List<String>)immutableList2, this.offsets, this.offset);
        }
    }

    public static enum Mode {
        LINES(2, 2, false),
        LINE_STRIP(2, 1, true),
        DEBUG_LINES(2, 2, false),
        DEBUG_LINE_STRIP(2, 1, true),
        TRIANGLES(3, 3, false),
        TRIANGLE_STRIP(3, 1, true),
        TRIANGLE_FAN(3, 1, true),
        QUADS(4, 4, false);

        public final int primitiveLength;
        public final int primitiveStride;
        public final boolean connectedPrimitives;

        private Mode(int n2, int n3, boolean bl) {
            this.primitiveLength = n2;
            this.primitiveStride = n3;
            this.connectedPrimitives = bl;
        }

        public int indexCount(int n) {
            return switch (this.ordinal()) {
                case 1, 2, 3, 4, 5, 6 -> n;
                case 0, 7 -> n / 4 * 6;
                default -> 0;
            };
        }
    }

    public static enum IndexType {
        SHORT(2),
        INT(4);

        public final int bytes;

        private IndexType(int n2) {
            this.bytes = n2;
        }

        public static IndexType least(int n) {
            if ((n & 0xFFFF0000) != 0) {
                return INT;
            }
            return SHORT;
        }
    }
}

