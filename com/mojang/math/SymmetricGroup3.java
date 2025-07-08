/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix3f
 *  org.joml.Matrix3fc
 */
package com.mojang.math;

import java.util.Arrays;
import net.minecraft.Util;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;

public enum SymmetricGroup3 {
    P123(0, 1, 2),
    P213(1, 0, 2),
    P132(0, 2, 1),
    P231(1, 2, 0),
    P312(2, 0, 1),
    P321(2, 1, 0);

    private final int[] permutation;
    private final Matrix3fc transformation;
    private static final int ORDER = 3;
    private static final SymmetricGroup3[][] CAYLEY_TABLE;

    private SymmetricGroup3(int n2, int n3, int n4) {
        this.permutation = new int[]{n2, n3, n4};
        Matrix3f matrix3f = new Matrix3f().zero();
        matrix3f.set(this.permutation(0), 0, 1.0f);
        matrix3f.set(this.permutation(1), 1, 1.0f);
        matrix3f.set(this.permutation(2), 2, 1.0f);
        this.transformation = matrix3f;
    }

    public SymmetricGroup3 compose(SymmetricGroup3 symmetricGroup3) {
        return CAYLEY_TABLE[this.ordinal()][symmetricGroup3.ordinal()];
    }

    public int permutation(int n) {
        return this.permutation[n];
    }

    public Matrix3fc transformation() {
        return this.transformation;
    }

    static {
        CAYLEY_TABLE = Util.make(new SymmetricGroup3[SymmetricGroup3.values().length][SymmetricGroup3.values().length], symmetricGroup3Array -> {
            for (SymmetricGroup3 symmetricGroup32 : SymmetricGroup3.values()) {
                for (SymmetricGroup3 symmetricGroup33 : SymmetricGroup3.values()) {
                    SymmetricGroup3 symmetricGroup34;
                    int[] nArray = new int[3];
                    for (int i = 0; i < 3; ++i) {
                        nArray[i] = symmetricGroup32.permutation[symmetricGroup33.permutation[i]];
                    }
                    symmetricGroup3Array[symmetricGroup32.ordinal()][symmetricGroup33.ordinal()] = symmetricGroup34 = Arrays.stream(SymmetricGroup3.values()).filter(symmetricGroup3 -> Arrays.equals(symmetricGroup3.permutation, nArray)).findFirst().get();
                }
            }
        });
    }
}

