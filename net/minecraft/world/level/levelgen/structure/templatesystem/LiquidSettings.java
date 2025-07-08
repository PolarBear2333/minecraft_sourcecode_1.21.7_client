/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum LiquidSettings implements StringRepresentable
{
    IGNORE_WATERLOGGING("ignore_waterlogging"),
    APPLY_WATERLOGGING("apply_waterlogging");

    public static Codec<LiquidSettings> CODEC;
    private final String name;

    private LiquidSettings(String string2) {
        this.name = string2;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    static {
        CODEC = StringRepresentable.fromValues(LiquidSettings::values);
    }
}

