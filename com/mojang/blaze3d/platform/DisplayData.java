/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.platform;

import java.util.OptionalInt;

public record DisplayData(int width, int height, OptionalInt fullscreenWidth, OptionalInt fullscreenHeight, boolean isFullscreen) {
    public DisplayData withSize(int n, int n2) {
        return new DisplayData(n, n2, this.fullscreenWidth, this.fullscreenHeight, this.isFullscreen);
    }

    public DisplayData withFullscreen(boolean bl) {
        return new DisplayData(this.width, this.height, this.fullscreenWidth, this.fullscreenHeight, bl);
    }
}

