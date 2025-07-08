/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.DontObfuscate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;

@DontObfuscate
public record VertexFormatElement(int id, int index, Type type, Usage usage, int count) {
    public static final int MAX_COUNT = 32;
    private static final VertexFormatElement[] BY_ID = new VertexFormatElement[32];
    private static final List<VertexFormatElement> ELEMENTS = new ArrayList<VertexFormatElement>(32);
    public static final VertexFormatElement POSITION = VertexFormatElement.register(0, 0, Type.FLOAT, Usage.POSITION, 3);
    public static final VertexFormatElement COLOR = VertexFormatElement.register(1, 0, Type.UBYTE, Usage.COLOR, 4);
    public static final VertexFormatElement UV0;
    public static final VertexFormatElement UV;
    public static final VertexFormatElement UV1;
    public static final VertexFormatElement UV2;
    public static final VertexFormatElement NORMAL;

    public VertexFormatElement(int n, int n2, Type type, Usage usage, int n3) {
        if (n < 0 || n >= BY_ID.length) {
            throw new IllegalArgumentException("Element ID must be in range [0; " + BY_ID.length + ")");
        }
        if (!this.supportsUsage(n2, usage)) {
            throw new IllegalStateException("Multiple vertex elements of the same type other than UVs are not supported");
        }
        this.id = n;
        this.index = n2;
        this.type = type;
        this.usage = usage;
        this.count = n3;
    }

    public static VertexFormatElement register(int n, int n2, Type type, Usage usage, int n3) {
        VertexFormatElement vertexFormatElement = new VertexFormatElement(n, n2, type, usage, n3);
        if (BY_ID[n] != null) {
            throw new IllegalArgumentException("Duplicate element registration for: " + n);
        }
        VertexFormatElement.BY_ID[n] = vertexFormatElement;
        ELEMENTS.add(vertexFormatElement);
        return vertexFormatElement;
    }

    private boolean supportsUsage(int n, Usage usage) {
        return n == 0 || usage == Usage.UV;
    }

    @Override
    public String toString() {
        return this.count + "," + String.valueOf((Object)this.usage) + "," + String.valueOf((Object)this.type) + " (" + this.id + ")";
    }

    public int mask() {
        return 1 << this.id;
    }

    public int byteSize() {
        return this.type.size() * this.count;
    }

    @Nullable
    public static VertexFormatElement byId(int n) {
        return BY_ID[n];
    }

    public static Stream<VertexFormatElement> elementsFromMask(int n) {
        return ELEMENTS.stream().filter(vertexFormatElement -> vertexFormatElement != null && (n & vertexFormatElement.mask()) != 0);
    }

    static {
        UV = UV0 = VertexFormatElement.register(2, 0, Type.FLOAT, Usage.UV, 2);
        UV1 = VertexFormatElement.register(3, 1, Type.SHORT, Usage.UV, 2);
        UV2 = VertexFormatElement.register(4, 2, Type.SHORT, Usage.UV, 2);
        NORMAL = VertexFormatElement.register(5, 0, Type.BYTE, Usage.NORMAL, 3);
    }

    @DontObfuscate
    public static enum Type {
        FLOAT(4, "Float"),
        UBYTE(1, "Unsigned Byte"),
        BYTE(1, "Byte"),
        USHORT(2, "Unsigned Short"),
        SHORT(2, "Short"),
        UINT(4, "Unsigned Int"),
        INT(4, "Int");

        private final int size;
        private final String name;

        private Type(int n2, String string2) {
            this.size = n2;
            this.name = string2;
        }

        public int size() {
            return this.size;
        }

        public String toString() {
            return this.name;
        }
    }

    @DontObfuscate
    public static enum Usage {
        POSITION("Position"),
        NORMAL("Normal"),
        COLOR("Vertex Color"),
        UV("UV"),
        GENERIC("Generic");

        private final String name;

        private Usage(String string2) {
            this.name = string2;
        }

        public String toString() {
            return this.name;
        }
    }
}

