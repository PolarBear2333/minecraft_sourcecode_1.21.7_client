/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.phys;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.util.Mth;

public class Vec2 {
    public static final Vec2 ZERO = new Vec2(0.0f, 0.0f);
    public static final Vec2 ONE = new Vec2(1.0f, 1.0f);
    public static final Vec2 UNIT_X = new Vec2(1.0f, 0.0f);
    public static final Vec2 NEG_UNIT_X = new Vec2(-1.0f, 0.0f);
    public static final Vec2 UNIT_Y = new Vec2(0.0f, 1.0f);
    public static final Vec2 NEG_UNIT_Y = new Vec2(0.0f, -1.0f);
    public static final Vec2 MAX = new Vec2(Float.MAX_VALUE, Float.MAX_VALUE);
    public static final Vec2 MIN = new Vec2(Float.MIN_VALUE, Float.MIN_VALUE);
    public static final Codec<Vec2> CODEC = Codec.FLOAT.listOf().comapFlatMap(list2 -> Util.fixedSize(list2, 2).map(list -> new Vec2(((Float)list.get(0)).floatValue(), ((Float)list.get(1)).floatValue())), vec2 -> List.of(Float.valueOf(vec2.x), Float.valueOf(vec2.y)));
    public final float x;
    public final float y;

    public Vec2(float f, float f2) {
        this.x = f;
        this.y = f2;
    }

    public Vec2 scale(float f) {
        return new Vec2(this.x * f, this.y * f);
    }

    public float dot(Vec2 vec2) {
        return this.x * vec2.x + this.y * vec2.y;
    }

    public Vec2 add(Vec2 vec2) {
        return new Vec2(this.x + vec2.x, this.y + vec2.y);
    }

    public Vec2 add(float f) {
        return new Vec2(this.x + f, this.y + f);
    }

    public boolean equals(Vec2 vec2) {
        return this.x == vec2.x && this.y == vec2.y;
    }

    public Vec2 normalized() {
        float f = Mth.sqrt(this.x * this.x + this.y * this.y);
        return f < 1.0E-4f ? ZERO : new Vec2(this.x / f, this.y / f);
    }

    public float length() {
        return Mth.sqrt(this.x * this.x + this.y * this.y);
    }

    public float lengthSquared() {
        return this.x * this.x + this.y * this.y;
    }

    public float distanceToSqr(Vec2 vec2) {
        float f = vec2.x - this.x;
        float f2 = vec2.y - this.y;
        return f * f + f2 * f2;
    }

    public Vec2 negated() {
        return new Vec2(-this.x, -this.y);
    }
}

