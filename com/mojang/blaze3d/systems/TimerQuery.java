/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.lwjgl.opengl.ARBTimerQuery
 *  org.lwjgl.opengl.GL
 *  org.lwjgl.opengl.GL32C
 */
package com.mojang.blaze3d.systems;

import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Optional;
import javax.annotation.Nullable;
import org.lwjgl.opengl.ARBTimerQuery;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL32C;

public class TimerQuery {
    private int nextQueryName;

    public static Optional<TimerQuery> getInstance() {
        return TimerQueryLazyLoader.INSTANCE;
    }

    public void beginProfile() {
        RenderSystem.assertOnRenderThread();
        if (this.nextQueryName != 0) {
            throw new IllegalStateException("Current profile not ended");
        }
        this.nextQueryName = GL32C.glGenQueries();
        GL32C.glBeginQuery((int)35007, (int)this.nextQueryName);
    }

    public FrameProfile endProfile() {
        RenderSystem.assertOnRenderThread();
        if (this.nextQueryName == 0) {
            throw new IllegalStateException("endProfile called before beginProfile");
        }
        GL32C.glEndQuery((int)35007);
        FrameProfile frameProfile = new FrameProfile(this.nextQueryName);
        this.nextQueryName = 0;
        return frameProfile;
    }

    static class TimerQueryLazyLoader {
        static final Optional<TimerQuery> INSTANCE = Optional.ofNullable(TimerQueryLazyLoader.instantiate());

        private TimerQueryLazyLoader() {
        }

        @Nullable
        private static TimerQuery instantiate() {
            if (!GL.getCapabilities().GL_ARB_timer_query) {
                return null;
            }
            return new TimerQuery();
        }
    }

    public static class FrameProfile {
        private static final long NO_RESULT = 0L;
        private static final long CANCELLED_RESULT = -1L;
        private final int queryName;
        private long result;

        FrameProfile(int n) {
            this.queryName = n;
        }

        public void cancel() {
            RenderSystem.assertOnRenderThread();
            if (this.result != 0L) {
                return;
            }
            this.result = -1L;
            GL32C.glDeleteQueries((int)this.queryName);
        }

        public boolean isDone() {
            RenderSystem.assertOnRenderThread();
            if (this.result != 0L) {
                return true;
            }
            if (1 == GL32C.glGetQueryObjecti((int)this.queryName, (int)34919)) {
                this.result = ARBTimerQuery.glGetQueryObjecti64((int)this.queryName, (int)34918);
                GL32C.glDeleteQueries((int)this.queryName);
                return true;
            }
            return false;
        }

        public long get() {
            RenderSystem.assertOnRenderThread();
            if (this.result == 0L) {
                this.result = ARBTimerQuery.glGetQueryObjecti64((int)this.queryName, (int)34918);
                GL32C.glDeleteQueries((int)this.queryName);
            }
            return this.result;
        }
    }
}

