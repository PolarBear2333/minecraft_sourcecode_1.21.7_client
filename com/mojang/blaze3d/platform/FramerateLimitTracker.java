/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.platform;

import net.minecraft.Util;
import net.minecraft.client.InactivityFpsLimit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;

public class FramerateLimitTracker {
    private static final int OUT_OF_LEVEL_MENU_LIMIT = 60;
    private static final int ICONIFIED_WINDOW_LIMIT = 10;
    private static final int AFK_LIMIT = 30;
    private static final int LONG_AFK_LIMIT = 10;
    private static final long AFK_THRESHOLD_MS = 60000L;
    private static final long LONG_AFK_THRESHOLD_MS = 600000L;
    private final Options options;
    private final Minecraft minecraft;
    private int framerateLimit;
    private long latestInputTime;

    public FramerateLimitTracker(Options options, Minecraft minecraft) {
        this.options = options;
        this.minecraft = minecraft;
        this.framerateLimit = options.framerateLimit().get();
    }

    public int getFramerateLimit() {
        return switch (this.getThrottleReason().ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> this.framerateLimit;
            case 1 -> 10;
            case 2 -> 10;
            case 3 -> Math.min(this.framerateLimit, 30);
            case 4 -> 60;
        };
    }

    public FramerateThrottleReason getThrottleReason() {
        InactivityFpsLimit inactivityFpsLimit = this.options.inactivityFpsLimit().get();
        if (this.minecraft.getWindow().isIconified()) {
            return FramerateThrottleReason.WINDOW_ICONIFIED;
        }
        if (inactivityFpsLimit == InactivityFpsLimit.AFK) {
            long l = Util.getMillis() - this.latestInputTime;
            if (l > 600000L) {
                return FramerateThrottleReason.LONG_AFK;
            }
            if (l > 60000L) {
                return FramerateThrottleReason.SHORT_AFK;
            }
        }
        if (this.minecraft.level == null && (this.minecraft.screen != null || this.minecraft.getOverlay() != null)) {
            return FramerateThrottleReason.OUT_OF_LEVEL_MENU;
        }
        return FramerateThrottleReason.NONE;
    }

    public boolean isHeavilyThrottled() {
        FramerateThrottleReason framerateThrottleReason = this.getThrottleReason();
        return framerateThrottleReason == FramerateThrottleReason.WINDOW_ICONIFIED || framerateThrottleReason == FramerateThrottleReason.LONG_AFK;
    }

    public void setFramerateLimit(int n) {
        this.framerateLimit = n;
    }

    public void onInputReceived() {
        this.latestInputTime = Util.getMillis();
    }

    public static enum FramerateThrottleReason {
        NONE,
        WINDOW_ICONIFIED,
        LONG_AFK,
        SHORT_AFK,
        OUT_OF_LEVEL_MENU;

    }
}

