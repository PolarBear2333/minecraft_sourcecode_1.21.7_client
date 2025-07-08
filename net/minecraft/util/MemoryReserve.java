/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.util;

import javax.annotation.Nullable;

public class MemoryReserve {
    @Nullable
    private static byte[] reserve;

    public static void allocate() {
        reserve = new byte[0xA00000];
    }

    public static void release() {
        if (reserve != null) {
            reserve = null;
            try {
                System.gc();
                System.gc();
                System.gc();
            }
            catch (Throwable throwable) {
                // empty catch block
            }
        }
    }
}

