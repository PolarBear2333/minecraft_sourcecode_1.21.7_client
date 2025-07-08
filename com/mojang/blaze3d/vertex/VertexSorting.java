/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.primitives.Floats
 *  it.unimi.dsi.fastutil.ints.IntArrays
 *  org.joml.Vector3f
 */
package com.mojang.blaze3d.vertex;

import com.google.common.primitives.Floats;
import it.unimi.dsi.fastutil.ints.IntArrays;
import org.joml.Vector3f;

public interface VertexSorting {
    public static final VertexSorting DISTANCE_TO_ORIGIN = VertexSorting.byDistance(0.0f, 0.0f, 0.0f);
    public static final VertexSorting ORTHOGRAPHIC_Z = VertexSorting.byDistance((Vector3f vector3f) -> -vector3f.z());

    public static VertexSorting byDistance(float f, float f2, float f3) {
        return VertexSorting.byDistance(new Vector3f(f, f2, f3));
    }

    public static VertexSorting byDistance(Vector3f vector3f) {
        return VertexSorting.byDistance(arg_0 -> ((Vector3f)vector3f).distanceSquared(arg_0));
    }

    public static VertexSorting byDistance(DistanceFunction distanceFunction) {
        return vector3fArray -> {
            float[] fArray = new float[vector3fArray.length];
            int[] nArray = new int[vector3fArray.length];
            for (int i = 0; i < vector3fArray.length; ++i) {
                fArray[i] = distanceFunction.apply(vector3fArray[i]);
                nArray[i] = i;
            }
            IntArrays.mergeSort((int[])nArray, (n, n2) -> Floats.compare((float)fArray[n2], (float)fArray[n]));
            return nArray;
        };
    }

    public int[] sort(Vector3f[] var1);

    public static interface DistanceFunction {
        public float apply(Vector3f var1);
    }
}

