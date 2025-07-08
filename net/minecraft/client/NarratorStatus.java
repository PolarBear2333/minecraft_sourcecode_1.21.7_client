/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client;

import java.util.function.IntFunction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ByIdMap;

public enum NarratorStatus {
    OFF(0, "options.narrator.off"),
    ALL(1, "options.narrator.all"),
    CHAT(2, "options.narrator.chat"),
    SYSTEM(3, "options.narrator.system");

    private static final IntFunction<NarratorStatus> BY_ID;
    private final int id;
    private final Component name;

    private NarratorStatus(int n2, String string2) {
        this.id = n2;
        this.name = Component.translatable(string2);
    }

    public int getId() {
        return this.id;
    }

    public Component getName() {
        return this.name;
    }

    public static NarratorStatus byId(int n) {
        return BY_ID.apply(n);
    }

    public boolean shouldNarrateChat() {
        return this == ALL || this == CHAT;
    }

    public boolean shouldNarrateSystem() {
        return this == ALL || this == SYSTEM;
    }

    public boolean shouldNarrateSystemOrChat() {
        return this == ALL || this == SYSTEM || this == CHAT;
    }

    static {
        BY_ID = ByIdMap.continuous(NarratorStatus::getId, NarratorStatus.values(), ByIdMap.OutOfBoundsStrategy.WRAP);
    }
}

