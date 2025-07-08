/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonParseException
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 */
package com.mojang.math;

import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.util.Mth;

public enum Quadrant {
    R0(0),
    R90(1),
    R180(2),
    R270(3);

    public static final Codec<Quadrant> CODEC;
    public final int shift;

    private Quadrant(int n2) {
        this.shift = n2;
    }

    @Deprecated
    public static Quadrant parseJson(int n) {
        return switch (Mth.positiveModulo(n, 360)) {
            case 0 -> R0;
            case 90 -> R90;
            case 180 -> R180;
            case 270 -> R270;
            default -> throw new JsonParseException("Invalid rotation " + n + " found, only 0/90/180/270 allowed");
        };
    }

    public int rotateVertexIndex(int n) {
        return (n + this.shift) % 4;
    }

    static {
        CODEC = Codec.INT.comapFlatMap(n -> switch (Mth.positiveModulo(n, 360)) {
            case 0 -> DataResult.success((Object)((Object)R0));
            case 90 -> DataResult.success((Object)((Object)R90));
            case 180 -> DataResult.success((Object)((Object)R180));
            case 270 -> DataResult.success((Object)((Object)R270));
            default -> DataResult.error(() -> "Invalid rotation " + n + " found, only 0/90/180/270 allowed");
        }, quadrant -> switch (quadrant.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> 0;
            case 1 -> 90;
            case 2 -> 180;
            case 3 -> 270;
        });
    }
}

