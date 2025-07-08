/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity;

import java.util.function.Consumer;

public class AnimationState {
    private static final int STOPPED = Integer.MIN_VALUE;
    private int startTick = Integer.MIN_VALUE;

    public void start(int n) {
        this.startTick = n;
    }

    public void startIfStopped(int n) {
        if (!this.isStarted()) {
            this.start(n);
        }
    }

    public void animateWhen(boolean bl, int n) {
        if (bl) {
            this.startIfStopped(n);
        } else {
            this.stop();
        }
    }

    public void stop() {
        this.startTick = Integer.MIN_VALUE;
    }

    public void ifStarted(Consumer<AnimationState> consumer) {
        if (this.isStarted()) {
            consumer.accept(this);
        }
    }

    public void fastForward(int n, float f) {
        if (!this.isStarted()) {
            return;
        }
        this.startTick -= (int)((float)n * f);
    }

    public long getTimeInMillis(float f) {
        float f2 = f - (float)this.startTick;
        return (long)(f2 * 50.0f);
    }

    public boolean isStarted() {
        return this.startTick != Integer.MIN_VALUE;
    }

    public void copyFrom(AnimationState animationState) {
        this.startTick = animationState.startTick;
    }
}

