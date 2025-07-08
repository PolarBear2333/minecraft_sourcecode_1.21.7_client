/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level;

import net.minecraft.world.level.ColorMapColorUtil;

public class FoliageColor {
    public static final int FOLIAGE_EVERGREEN = -10380959;
    public static final int FOLIAGE_BIRCH = -8345771;
    public static final int FOLIAGE_DEFAULT = -12012264;
    public static final int FOLIAGE_MANGROVE = -7158200;
    private static int[] pixels = new int[65536];

    public static void init(int[] nArray) {
        pixels = nArray;
    }

    public static int get(double d, double d2) {
        return ColorMapColorUtil.get(d, d2, pixels, -12012264);
    }
}

