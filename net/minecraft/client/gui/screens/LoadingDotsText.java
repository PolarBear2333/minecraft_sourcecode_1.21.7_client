/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens;

public class LoadingDotsText {
    private static final String[] FRAMES = new String[]{"O o o", "o O o", "o o O", "o O o"};
    private static final long INTERVAL_MS = 300L;

    public static String get(long l) {
        int n = (int)(l / 300L % (long)FRAMES.length);
        return FRAMES[n];
    }
}

