/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.multiplayer.chat.report;

import java.util.Locale;

public enum ReportType {
    CHAT("chat"),
    SKIN("skin"),
    USERNAME("username");

    private final String backendName;

    private ReportType(String string2) {
        this.backendName = string2.toUpperCase(Locale.ROOT);
    }

    public String backendName() {
        return this.backendName;
    }
}

