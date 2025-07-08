/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level;

import net.minecraft.world.level.ColorMapColorUtil;

public class DryFoliageColor {
    public static final int FOLIAGE_DRY_DEFAULT = -10732494;
    private static int[] pixels = new int[65536];

    public static void init(int[] nArray) {
        pixels = nArray;
    }

    public static int get(double d, double d2) {
        return ColorMapColorUtil.get(d, d2, pixels, -10732494);
    }
}

