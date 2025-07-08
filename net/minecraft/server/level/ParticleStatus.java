/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.level;

import java.util.function.IntFunction;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.OptionEnum;

public enum ParticleStatus implements OptionEnum
{
    ALL(0, "options.particles.all"),
    DECREASED(1, "options.particles.decreased"),
    MINIMAL(2, "options.particles.minimal");

    private static final IntFunction<ParticleStatus> BY_ID;
    private final int id;
    private final String key;

    private ParticleStatus(int n2, String string2) {
        this.id = n2;
        this.key = string2;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public int getId() {
        return this.id;
    }

    public static ParticleStatus byId(int n) {
        return BY_ID.apply(n);
    }

    static {
        BY_ID = ByIdMap.continuous(ParticleStatus::getId, ParticleStatus.values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    }
}

