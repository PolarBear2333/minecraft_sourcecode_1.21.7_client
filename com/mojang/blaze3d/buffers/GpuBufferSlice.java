/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.buffers;

import com.mojang.blaze3d.DontObfuscate;
import com.mojang.blaze3d.buffers.GpuBuffer;

@DontObfuscate
public record GpuBufferSlice(GpuBuffer buffer, int offset, int length) {
    public GpuBufferSlice slice(int n, int n2) {
        if (n < 0 || n2 < 0 || n + n2 >= this.length) {
            throw new IllegalArgumentException("Offset of " + n + " and length " + n2 + " would put new slice outside existing slice's range (of " + n + "," + n2 + ")");
        }
        return new GpuBufferSlice(this.buffer, this.offset + n, n2);
    }
}

