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

public enum CloudStatus implements OptionEnum,
StringRepresentable
{
    OFF(0, "false", "options.off"),
    FAST(1, "fast", "options.clouds.fast"),
    FANCY(2, "true", "options.clouds.fancy");

    public static final Codec<CloudStatus> CODEC;
    private final int id;
    private final String legacyName;
    private final String key;

    private CloudStatus(int n2, String string2, String string3) {
        this.id = n2;
        this.legacyName = string2;
        this.key = string3;
    }

    @Override
    public String getSerializedName() {
        return this.legacyName;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    static {
        CODEC = StringRepresentable.fromEnum(CloudStatus::values);
    }
}

