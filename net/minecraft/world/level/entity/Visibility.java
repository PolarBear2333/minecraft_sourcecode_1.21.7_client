/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.entity;

import net.minecraft.server.level.FullChunkStatus;

public enum Visibility {
    HIDDEN(false, false),
    TRACKED(true, false),
    TICKING(true, true);

    private final boolean accessible;
    private final boolean ticking;

    private Visibility(boolean bl, boolean bl2) {
        this.accessible = bl;
        this.ticking = bl2;
    }

    public boolean isTicking() {
        return this.ticking;
    }

    public boolean isAccessible() {
        return this.accessible;
    }

    public static Visibility fromFullChunkStatus(FullChunkStatus fullChunkStatus) {
        if (fullChunkStatus.isOrAfter(FullChunkStatus.ENTITY_TICKING)) {
            return TICKING;
        }
        if (fullChunkStatus.isOrAfter(FullChunkStatus.FULL)) {
            return TRACKED;
        }
        return HIDDEN;
    }
}

