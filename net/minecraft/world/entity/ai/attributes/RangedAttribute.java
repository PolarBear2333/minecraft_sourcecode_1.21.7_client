/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.attributes;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attribute;

public class RangedAttribute
extends Attribute {
    private final double minValue;
    private final double maxValue;

    public RangedAttribute(String string, double d, double d2, double d3) {
        super(string, d);
        this.minValue = d2;
        this.maxValue = d3;
        if (d2 > d3) {
            throw new IllegalArgumentException("Minimum value cannot be bigger than maximum value!");
        }
        if (d < d2) {
            throw new IllegalArgumentException("Default value cannot be lower than minimum value!");
        }
        if (d > d3) {
            throw new IllegalArgumentException("Default value cannot be bigger than maximum value!");
        }
    }

    public double getMinValue() {
        return this.minValue;
    }

    public double getMaxValue() {
        return this.maxValue;
    }

    @Override
    public double sanitizeValue(double d) {
        if (Double.isNaN(d)) {
            return this.minValue;
        }
        return Mth.clamp(d, this.minValue, this.maxValue);
    }
}

