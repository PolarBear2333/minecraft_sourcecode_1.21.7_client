/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 */
package com.mojang.blaze3d;

import com.mojang.blaze3d.vertex.VertexSorting;
import org.joml.Matrix4f;

public enum ProjectionType {
    PERSPECTIVE(VertexSorting.DISTANCE_TO_ORIGIN, (matrix4f, f) -> matrix4f.scale(1.0f - f / 4096.0f)),
    ORTHOGRAPHIC(VertexSorting.ORTHOGRAPHIC_Z, (matrix4f, f) -> matrix4f.translate(0.0f, 0.0f, f / 512.0f));

    private final VertexSorting vertexSorting;
    private final LayeringTransform layeringTransform;

    private ProjectionType(VertexSorting vertexSorting, LayeringTransform layeringTransform) {
        this.vertexSorting = vertexSorting;
        this.layeringTransform = layeringTransform;
    }

    public VertexSorting vertexSorting() {
        return this.vertexSorting;
    }

    public void applyLayeringTransform(Matrix4f matrix4f, float f) {
        this.layeringTransform.apply(matrix4f, f);
    }

    @FunctionalInterface
    static interface LayeringTransform {
        public void apply(Matrix4f var1, float var2);
    }
}

