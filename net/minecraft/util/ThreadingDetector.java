/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.util;

import com.mojang.logging.LogUtils;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import org.slf4j.Logger;

public class ThreadingDetector {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String name;
    private final Semaphore lock = new Semaphore(1);
    private final Lock stackTraceLock = new ReentrantLock();
    @Nullable
    private volatile Thread threadThatFailedToAcquire;
    @Nullable
    private volatile ReportedException fullException;

    public ThreadingDetector(String string) {
        this.name = string;
    }

    public void checkAndLock() {
        block6: {
            boolean bl = false;
            try {
                this.stackTraceLock.lock();
                if (this.lock.tryAcquire()) break block6;
                this.threadThatFailedToAcquire = Thread.currentThread();
                bl = true;
                this.stackTraceLock.unlock();
                try {
                    this.lock.acquire();
                }
                catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                }
                throw this.fullException;
            }
            finally {
                if (!bl) {
                    this.stackTraceLock.unlock();
                }
            }
        }
    }

    public void checkAndUnlock() {
        try {
            this.stackTraceLock.lock();
            Thread thread = this.threadThatFailedToAcquire;
            if (thread != null) {
                ReportedException reportedException;
                this.fullException = reportedException = ThreadingDetector.makeThreadingException(this.name, thread);
                this.lock.release();
                throw reportedException;
            }
            this.lock.release();
        }
        finally {
            this.stackTraceLock.unlock();
        }
    }

    public static ReportedException makeThreadingException(String string, @Nullable Thread thread) {
        String string2 = Stream.of(Thread.currentThread(), thread).filter(Objects::nonNull).map(ThreadingDetector::stackTrace).collect(Collectors.joining("\n"));
        String string3 = "Accessing " + string + " from multiple threads";
        CrashReport crashReport = new CrashReport(string3, new IllegalStateException(string3));
        CrashReportCategory crashReportCategory = crashReport.addCategory("Thread dumps");
        crashReportCategory.setDetail("Thread dumps", string2);
        LOGGER.error("Thread dumps: \n" + string2);
        return new ReportedException(crashReport);
    }

    private static String stackTrace(Thread thread) {
        return thread.getName() + ": \n\tat " + Arrays.stream(thread.getStackTrace()).map(Object::toString).collect(Collectors.joining("\n\tat "));
    }
}

