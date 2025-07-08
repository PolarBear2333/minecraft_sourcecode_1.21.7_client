/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.client;

import com.mojang.serialization.Codec;
import net.minecraft.util.OptionEnum;
import net.minecraft.util.StringRepresentable;

public enum InactivityFpsLimit implements OptionEnum,
StringRepresentable
{
    MINIMIZED(0, "minimized", "options.inactivityFpsLimit.minimized"),
    AFK(1, "afk", "options.inactivityFpsLimit.afk");

    public static final Codec<InactivityFpsLimit> CODEC;
    private final int id;
    private final String serializedName;
    private final String key;

    private InactivityFpsLimit(int n2, String string2, String string3) {
        this.id = n2;
        this.serializedName = string2;
        this.key = string3;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }

    static {
        CODEC = StringRepresentable.fromEnum(InactivityFpsLimit::values);
    }
}

