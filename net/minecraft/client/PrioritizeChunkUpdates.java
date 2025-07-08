/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client;

import java.util.function.IntFunction;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.OptionEnum;

public enum PrioritizeChunkUpdates implements OptionEnum
{
    NONE(0, "options.prioritizeChunkUpdates.none"),
    PLAYER_AFFECTED(1, "options.prioritizeChunkUpdates.byPlayer"),
    NEARBY(2, "options.prioritizeChunkUpdates.nearby");

    private static final IntFunction<PrioritizeChunkUpdates> BY_ID;
    private final int id;
    private final String key;

    private PrioritizeChunkUpdates(int n2, String string2) {
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

    public static PrioritizeChunkUpdates byId(int n) {
        return BY_ID.apply(n);
    }

    static {
        BY_ID = ByIdMap.continuous(PrioritizeChunkUpdates::getId, PrioritizeChunkUpdates.values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    }
}

