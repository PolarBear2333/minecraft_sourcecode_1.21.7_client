/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.buffers;

import com.mojang.blaze3d.DontObfuscate;

@DontObfuscate
public interface GpuFence
extends AutoCloseable {
    @Override
    public void close();

    public boolean awaitCompletion(long var1);
}

