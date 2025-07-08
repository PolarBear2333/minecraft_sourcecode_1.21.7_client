/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

public enum TriState {
    TRUE,
    FALSE,
    DEFAULT;


    public boolean toBoolean(boolean bl) {
        return switch (this.ordinal()) {
            case 0 -> true;
            case 1 -> false;
            default -> bl;
        };
    }
}

