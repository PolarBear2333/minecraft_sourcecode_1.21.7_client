/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server;

public final class RunningOnDifferentThreadException
extends RuntimeException {
    public static final RunningOnDifferentThreadException RUNNING_ON_DIFFERENT_THREAD = new RunningOnDifferentThreadException();

    private RunningOnDifferentThreadException() {
        this.setStackTrace(new StackTraceElement[0]);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        this.setStackTrace(new StackTraceElement[0]);
        return this;
    }
}

