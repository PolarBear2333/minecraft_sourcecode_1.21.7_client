/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.gametest;

import net.minecraft.SharedConstants;
import net.minecraft.gametest.framework.GameTestMainUtil;
import net.minecraft.obfuscate.DontObfuscate;

public class Main {
    @DontObfuscate
    public static void main(String[] stringArray) throws Exception {
        SharedConstants.tryDetectVersion();
        GameTestMainUtil.runGameTestServer(stringArray, string -> {});
    }
}

