/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import javax.annotation.Nullable;

public class Tesselator {
    private static final int MAX_BYTES = 786432;
    private final ByteBufferBuilder buffer;
    @Nullable
    private static Tesselator instance;

    public static void init() {
        if (instance != null) {
            throw new IllegalStateException("Tesselator has already been initialized");
        }
        instance = new Tesselator();
    }

    public static Tesselator getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Tesselator has not been initialized");
        }
        return instance;
    }

    public Tesselator(int n) {
        this.buffer = new ByteBufferBuilder(n);
    }

    public Tesselator() {
        this(786432);
    }

    public BufferBuilder begin(VertexFormat.Mode mode, VertexFormat vertexFormat) {
        return new BufferBuilder(this.buffer, mode, vertexFormat);
    }

    public void clear() {
        this.buffer.clear();
    }
}

