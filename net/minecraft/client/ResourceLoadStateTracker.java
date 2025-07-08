/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.server.packs.PackResources;
import org.slf4j.Logger;

public class ResourceLoadStateTracker {
    private static final Logger LOGGER = LogUtils.getLogger();
    @Nullable
    private ReloadState reloadState;
    private int reloadCount;

    public void startReload(ReloadReason reloadReason, List<PackResources> list) {
        ++this.reloadCount;
        if (this.reloadState != null && !this.reloadState.finished) {
            LOGGER.warn("Reload already ongoing, replacing");
        }
        this.reloadState = new ReloadState(reloadReason, (List)list.stream().map(PackResources::packId).collect(ImmutableList.toImmutableList()));
    }

    public void startRecovery(Throwable throwable) {
        if (this.reloadState == null) {
            LOGGER.warn("Trying to signal reload recovery, but nothing was started");
            this.reloadState = new ReloadState(ReloadReason.UNKNOWN, (List<String>)ImmutableList.of());
        }
        this.reloadState.recoveryReloadInfo = new RecoveryInfo(throwable);
    }

    public void finishReload() {
        if (this.reloadState == null) {
            LOGGER.warn("Trying to finish reload, but nothing was started");
        } else {
            this.reloadState.finished = true;
        }
    }

    public void fillCrashReport(CrashReport crashReport) {
        CrashReportCategory crashReportCategory = crashReport.addCategory("Last reload");
        crashReportCategory.setDetail("Reload number", this.reloadCount);
        if (this.reloadState != null) {
            this.reloadState.fillCrashInfo(crashReportCategory);
        }
    }

    static class ReloadState {
        private final ReloadReason reloadReason;
        private final List<String> packs;
        @Nullable
        RecoveryInfo recoveryReloadInfo;
        boolean finished;

        ReloadState(ReloadReason reloadReason, List<String> list) {
            this.reloadReason = reloadReason;
            this.packs = list;
        }

        public void fillCrashInfo(CrashReportCategory crashReportCategory) {
            crashReportCategory.setDetail("Reload reason", this.reloadReason.name);
            crashReportCategory.setDetail("Finished", this.finished ? "Yes" : "No");
            crashReportCategory.setDetail("Packs", () -> String.join((CharSequence)", ", this.packs));
            if (this.recoveryReloadInfo != null) {
                this.recoveryReloadInfo.fillCrashInfo(crashReportCategory);
            }
        }
    }

    public static enum ReloadReason {
        INITIAL("initial"),
        MANUAL("manual"),
        UNKNOWN("unknown");

        final String name;

        private ReloadReason(String string2) {
            this.name = string2;
        }
    }

    static class RecoveryInfo {
        private final Throwable error;

        RecoveryInfo(Throwable throwable) {
            this.error = throwable;
        }

        public void fillCrashInfo(CrashReportCategory crashReportCategory) {
            crashReportCategory.setDetail("Recovery", "Yes");
            crashReportCategory.setDetail("Recovery reason", () -> {
                StringWriter stringWriter = new StringWriter();
                this.error.printStackTrace(new PrintWriter(stringWriter));
                return stringWriter.toString();
            });
        }
    }
}

