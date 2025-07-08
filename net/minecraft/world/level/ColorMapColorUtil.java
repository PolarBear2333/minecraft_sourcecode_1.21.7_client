/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level;

public interface ColorMapColorUtil {
    public static int get(double d, double d2, int[] nArray, int n) {
        int n2 = (int)((1.0 - (d2 *= d)) * 255.0);
        int n3 = (int)((1.0 - d) * 255.0);
        int n4 = n2 << 8 | n3;
        if (n4 >= nArray.length) {
            return n;
        }
        return nArray[n4];
    }
}

