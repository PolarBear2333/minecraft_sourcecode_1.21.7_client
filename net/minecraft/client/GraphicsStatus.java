/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client;

import java.util.function.IntFunction;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.OptionEnum;

public enum GraphicsStatus implements OptionEnum
{
    FAST(0, "options.graphics.fast"),
    FANCY(1, "options.graphics.fancy"),
    FABULOUS(2, "options.graphics.fabulous");

    private static final IntFunction<GraphicsStatus> BY_ID;
    private final int id;
    private final String key;

    private GraphicsStatus(int n2, String string2) {
        this.id = n2;
        this.key = string2;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    public String toString() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> "fast";
            case 1 -> "fancy";
            case 2 -> "fabulous";
        };
    }

    public static GraphicsStatus byId(int n) {
        return BY_ID.apply(n);
    }

    static {
        BY_ID = ByIdMap.continuous(GraphicsStatus::getId, GraphicsStatus.values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    }
}

