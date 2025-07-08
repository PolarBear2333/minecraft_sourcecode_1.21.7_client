/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.jtracy.TracyClient
 *  com.mojang.logging.LogListeners
 *  org.slf4j.event.Level
 */
package com.mojang.blaze3d;

import com.mojang.jtracy.TracyClient;
import com.mojang.logging.LogListeners;
import org.slf4j.event.Level;

public class TracyBootstrap {
    private static boolean setup;

    public static void setup() {
        if (setup) {
            return;
        }
        TracyClient.load();
        if (!TracyClient.isAvailable()) {
            return;
        }
        LogListeners.addListener((String)"Tracy", (string, level) -> TracyClient.message((String)string, (int)TracyBootstrap.messageColor(level)));
        setup = true;
    }

    private static int messageColor(Level level) {
        return switch (level) {
            default -> 0xFFFFFF;
            case Level.DEBUG -> 0xAAAAAA;
            case Level.WARN -> 0xFFFFAA;
            case Level.ERROR -> 0xFFAAAA;
        };
    }
}

