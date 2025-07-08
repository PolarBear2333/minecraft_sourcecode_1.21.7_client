/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector2i
 */
package net.minecraft.client;

import org.joml.Vector2i;

public class ScrollWheelHandler {
    private double accumulatedScrollX;
    private double accumulatedScrollY;

    public Vector2i onMouseScroll(double d, double d2) {
        if (this.accumulatedScrollX != 0.0 && Math.signum(d) != Math.signum(this.accumulatedScrollX)) {
            this.accumulatedScrollX = 0.0;
        }
        if (this.accumulatedScrollY != 0.0 && Math.signum(d2) != Math.signum(this.accumulatedScrollY)) {
            this.accumulatedScrollY = 0.0;
        }
        this.accumulatedScrollX += d;
        this.accumulatedScrollY += d2;
        int n = (int)this.accumulatedScrollX;
        int n2 = (int)this.accumulatedScrollY;
        if (n == 0 && n2 == 0) {
            return new Vector2i(0, 0);
        }
        this.accumulatedScrollX -= (double)n;
        this.accumulatedScrollY -= (double)n2;
        return new Vector2i(n, n2);
    }

    public static int getNextScrollWheelSelection(double d, int n, int n2) {
        int n3 = (int)Math.signum(d);
        n -= n3;
        for (n = Math.max(-1, n); n < 0; n += n2) {
        }
        while (n >= n2) {
            n -= n2;
        }
        return n;
    }
}

