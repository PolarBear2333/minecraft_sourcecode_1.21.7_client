/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.storage;

public record DataVersion(int version, String series) {
    public static final String MAIN_SERIES = "main";

    public boolean isSideSeries() {
        return !this.series.equals(MAIN_SERIES);
    }

    public boolean isCompatible(DataVersion dataVersion) {
        return this.series().equals(dataVersion.series());
    }
}

