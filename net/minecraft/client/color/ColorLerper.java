/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 */
package net.minecraft.client.color;

import com.google.common.collect.Maps;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;

public class ColorLerper {
    public static final DyeColor[] MUSIC_NOTE_COLORS = new DyeColor[]{DyeColor.WHITE, DyeColor.LIGHT_GRAY, DyeColor.LIGHT_BLUE, DyeColor.BLUE, DyeColor.CYAN, DyeColor.GREEN, DyeColor.LIME, DyeColor.YELLOW, DyeColor.ORANGE, DyeColor.PINK, DyeColor.RED, DyeColor.MAGENTA};

    public static int getLerpedColor(Type type, float f) {
        int n = Mth.floor(f);
        int n2 = n / type.colorDuration;
        int n3 = type.colors.length;
        int n4 = n2 % n3;
        int n5 = (n2 + 1) % n3;
        float f2 = ((float)(n % type.colorDuration) + Mth.frac(f)) / (float)type.colorDuration;
        int n6 = type.getColor(type.colors[n4]);
        int n7 = type.getColor(type.colors[n5]);
        return ARGB.lerp(f2, n6, n7);
    }

    static int getModifiedColor(DyeColor dyeColor, float f) {
        if (dyeColor == DyeColor.WHITE) {
            return -1644826;
        }
        int n = dyeColor.getTextureDiffuseColor();
        return ARGB.color(255, Mth.floor((float)ARGB.red(n) * f), Mth.floor((float)ARGB.green(n) * f), Mth.floor((float)ARGB.blue(n) * f));
    }

    public static enum Type {
        SHEEP(25, DyeColor.values(), 0.75f),
        MUSIC_NOTE(30, MUSIC_NOTE_COLORS, 1.25f);

        final int colorDuration;
        private final Map<DyeColor, Integer> colorByDye;
        final DyeColor[] colors;

        private Type(int n2, DyeColor[] dyeColorArray, float f) {
            this.colorDuration = n2;
            this.colorByDye = Maps.newHashMap(Arrays.stream(dyeColorArray).collect(Collectors.toMap(dyeColor -> dyeColor, dyeColor -> ColorLerper.getModifiedColor(dyeColor, f))));
            this.colors = dyeColorArray;
        }

        public final int getColor(DyeColor dyeColor) {
            return this.colorByDye.get(dyeColor);
        }
    }
}

