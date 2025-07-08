/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.control;

import net.minecraft.util.Mth;

public interface Control {
    default public float rotateTowards(float f, float f2, float f3) {
        float f4 = Mth.degreesDifference(f, f2);
        float f5 = Mth.clamp(f4, -f3, f3);
        return f + f5;
    }
}

