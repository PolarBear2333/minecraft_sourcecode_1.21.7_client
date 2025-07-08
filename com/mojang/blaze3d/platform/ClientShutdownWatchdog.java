/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.platform;

import java.io.File;
import java.time.Duration;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import net.minecraft.server.dedicated.ServerWatchdog;

public class ClientShutdownWatchdog {
    private static final Duration CRASH_REPORT_PRELOAD_LOAD = Duration.ofSeconds(15L);

    public static void startShutdownWatchdog(File file, long l) {
        Thread thread = new Thread(() -> {
            try {
                Thread.sleep(CRASH_REPORT_PRELOAD_LOAD);
            }
            catch (InterruptedException interruptedException) {
                return;
            }
            CrashReport crashReport = ServerWatchdog.createWatchdogCrashReport("Client shutdown", l);
            Minecraft.saveReport(file, crashReport);
        });
        thread.setDaemon(true);
        thread.setName("Client shutdown watchdog");
        thread.start();
    }
}

