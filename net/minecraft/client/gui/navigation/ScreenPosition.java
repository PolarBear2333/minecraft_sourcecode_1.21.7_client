/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.navigation;

import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;

public record ScreenPosition(int x, int y) {
    public static ScreenPosition of(ScreenAxis screenAxis, int n, int n2) {
        return switch (screenAxis) {
            default -> throw new MatchException(null, null);
            case ScreenAxis.HORIZONTAL -> new ScreenPosition(n, n2);
            case ScreenAxis.VERTICAL -> new ScreenPosition(n2, n);
        };
    }

    public ScreenPosition step(ScreenDirection screenDirection) {
        return switch (screenDirection) {
            default -> throw new MatchException(null, null);
            case ScreenDirection.DOWN -> new ScreenPosition(this.x, this.y + 1);
            case ScreenDirection.UP -> new ScreenPosition(this.x, this.y - 1);
            case ScreenDirection.LEFT -> new ScreenPosition(this.x - 1, this.y);
            case ScreenDirection.RIGHT -> new ScreenPosition(this.x + 1, this.y);
        };
    }

    public int getCoordinate(ScreenAxis screenAxis) {
        return switch (screenAxis) {
            default -> throw new MatchException(null, null);
            case ScreenAxis.HORIZONTAL -> this.x;
            case ScreenAxis.VERTICAL -> this.y;
        };
    }
}

