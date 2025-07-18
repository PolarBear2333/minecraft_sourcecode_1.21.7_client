/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.packs.repository;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.InclusiveRange;

public enum PackCompatibility {
    TOO_OLD("old"),
    TOO_NEW("new"),
    COMPATIBLE("compatible");

    private final Component description;
    private final Component confirmation;

    private PackCompatibility(String string2) {
        this.description = Component.translatable("pack.incompatible." + string2).withStyle(ChatFormatting.GRAY);
        this.confirmation = Component.translatable("pack.incompatible.confirm." + string2);
    }

    public boolean isCompatible() {
        return this == COMPATIBLE;
    }

    public static PackCompatibility forVersion(InclusiveRange<Integer> inclusiveRange, int n) {
        if (inclusiveRange.maxInclusive() < n) {
            return TOO_OLD;
        }
        if (n < inclusiveRange.minInclusive()) {
            return TOO_NEW;
        }
        return COMPATIBLE;
    }

    public Component getDescription() {
        return this.description;
    }

    public Component getConfirmation() {
        return this.confirmation;
    }
}

