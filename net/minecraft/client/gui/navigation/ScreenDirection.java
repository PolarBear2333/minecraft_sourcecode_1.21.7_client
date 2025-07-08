/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntComparator
 */
package net.minecraft.client.gui.navigation;

import it.unimi.dsi.fastutil.ints.IntComparator;
import net.minecraft.client.gui.navigation.ScreenAxis;

public enum ScreenDirection {
    UP,
    DOWN,
    LEFT,
    RIGHT;

    private final IntComparator coordinateValueComparator = (n, n2) -> n == n2 ? 0 : (this.isBefore(n, n2) ? -1 : 1);

    public ScreenAxis getAxis() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0, 1 -> ScreenAxis.VERTICAL;
            case 2, 3 -> ScreenAxis.HORIZONTAL;
        };
    }

    public ScreenDirection getOpposite() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> DOWN;
            case 1 -> UP;
            case 2 -> RIGHT;
            case 3 -> LEFT;
        };
    }

    public boolean isPositive() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0, 2 -> false;
            case 1, 3 -> true;
        };
    }

    public boolean isAfter(int n, int n2) {
        if (this.isPositive()) {
            return n > n2;
        }
        return n2 > n;
    }

    public boolean isBefore(int n, int n2) {
        if (this.isPositive()) {
            return n < n2;
        }
        return n2 < n;
    }

    public IntComparator coordinateValueComparator() {
        return this.coordinateValueComparator;
    }
}

