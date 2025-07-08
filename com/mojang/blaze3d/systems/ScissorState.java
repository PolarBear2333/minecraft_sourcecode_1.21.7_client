/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.systems;

public class ScissorState {
    private boolean enabled;
    private int x;
    private int y;
    private int width;
    private int height;

    public void enable(int n, int n2, int n3, int n4) {
        this.enabled = true;
        this.x = n;
        this.y = n2;
        this.width = n3;
        this.height = n4;
    }

    public void disable() {
        this.enabled = false;
    }

    public boolean enabled() {
        return this.enabled;
    }

    public int x() {
        return this.x;
    }

    public int y() {
        return this.y;
    }

    public int width() {
        return this.width;
    }

    public int height() {
        return this.height;
    }
}

