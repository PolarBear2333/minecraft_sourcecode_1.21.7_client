/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  org.lwjgl.glfw.GLFW
 *  org.lwjgl.glfw.GLFWVidMode
 *  org.lwjgl.glfw.GLFWVidMode$Buffer
 */
package com.mojang.blaze3d.platform;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.VideoMode;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

public final class Monitor {
    private final long monitor;
    private final List<VideoMode> videoModes;
    private VideoMode currentMode;
    private int x;
    private int y;

    public Monitor(long l) {
        this.monitor = l;
        this.videoModes = Lists.newArrayList();
        this.refreshVideoModes();
    }

    public void refreshVideoModes() {
        Object object;
        this.videoModes.clear();
        GLFWVidMode.Buffer buffer = GLFW.glfwGetVideoModes((long)this.monitor);
        for (int i = buffer.limit() - 1; i >= 0; --i) {
            buffer.position(i);
            object = new VideoMode(buffer);
            if (((VideoMode)object).getRedBits() < 8 || ((VideoMode)object).getGreenBits() < 8 || ((VideoMode)object).getBlueBits() < 8) continue;
            this.videoModes.add((VideoMode)object);
        }
        int[] nArray = new int[1];
        object = new int[1];
        GLFW.glfwGetMonitorPos((long)this.monitor, (int[])nArray, (int[])object);
        this.x = nArray[0];
        this.y = (int)object[0];
        GLFWVidMode gLFWVidMode = GLFW.glfwGetVideoMode((long)this.monitor);
        this.currentMode = new VideoMode(gLFWVidMode);
    }

    public VideoMode getPreferredVidMode(Optional<VideoMode> optional) {
        if (optional.isPresent()) {
            VideoMode videoMode = optional.get();
            for (VideoMode videoMode2 : this.videoModes) {
                if (!videoMode2.equals(videoMode)) continue;
                return videoMode2;
            }
        }
        return this.getCurrentMode();
    }

    public int getVideoModeIndex(VideoMode videoMode) {
        return this.videoModes.indexOf(videoMode);
    }

    public VideoMode getCurrentMode() {
        return this.currentMode;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public VideoMode getMode(int n) {
        return this.videoModes.get(n);
    }

    public int getModeCount() {
        return this.videoModes.size();
    }

    public long getMonitor() {
        return this.monitor;
    }

    public String toString() {
        return String.format(Locale.ROOT, "Monitor[%s %sx%s %s]", this.monitor, this.x, this.y, this.currentMode);
    }
}

