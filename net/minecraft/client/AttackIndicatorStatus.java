/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client;

import java.util.function.IntFunction;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.OptionEnum;

public enum AttackIndicatorStatus implements OptionEnum
{
    OFF(0, "options.off"),
    CROSSHAIR(1, "options.attack.crosshair"),
    HOTBAR(2, "options.attack.hotbar");

    private static final IntFunction<AttackIndicatorStatus> BY_ID;
    private final int id;
    private final String key;

    private AttackIndicatorStatus(int n2, String string2) {
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

    public static AttackIndicatorStatus byId(int n) {
        return BY_ID.apply(n);
    }

    static {
        BY_ID = ByIdMap.continuous(AttackIndicatorStatus::getId, AttackIndicatorStatus.values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    }
}

