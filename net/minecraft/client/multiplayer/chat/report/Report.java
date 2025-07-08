/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.minecraft.report.AbuseReport
 *  com.mojang.authlib.minecraft.report.AbuseReportLimits
 *  com.mojang.datafixers.util.Either
 *  javax.annotation.Nullable
 */
package net.minecraft.client.multiplayer.chat.report;

import com.mojang.authlib.minecraft.report.AbuseReport;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.datafixers.util.Either;
import java.time.Instant;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.chat.report.ReportReason;
import net.minecraft.client.multiplayer.chat.report.ReportType;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.network.chat.Component;

public abstract class Report {
    protected final UUID reportId;
    protected final Instant createdAt;
    protected final UUID reportedProfileId;
    protected String comments = "";
    @Nullable
    protected ReportReason reason;
    protected boolean attested;

    public Report(UUID uUID, Instant instant, UUID uUID2) {
        this.reportId = uUID;
        this.createdAt = instant;
        this.reportedProfileId = uUID2;
    }

    public boolean isReportedPlayer(UUID uUID) {
        return uUID.equals(this.reportedProfileId);
    }

    public abstract Report copy();

    public abstract Screen createScreen(Screen var1, ReportingContext var2);

    public record CannotBuildReason(Component message) {
        public static final CannotBuildReason NO_REASON = new CannotBuildReason(Component.translatable("gui.abuseReport.send.no_reason"));
        public static final CannotBuildReason NO_REPORTED_MESSAGES = new CannotBuildReason(Component.translatable("gui.chatReport.send.no_reported_messages"));
        public static final CannotBuildReason TOO_MANY_MESSAGES = new CannotBuildReason(Component.translatable("gui.chatReport.send.too_many_messages"));
        public static final CannotBuildReason COMMENT_TOO_LONG = new CannotBuildReason(Component.translatable("gui.abuseReport.send.comment_too_long"));
        public static final CannotBuildReason NOT_ATTESTED = new CannotBuildReason(Component.translatable("gui.abuseReport.send.not_attested"));

        public Tooltip tooltip() {
            return Tooltip.create(this.message);
        }
    }

    public record Result(UUID id, ReportType reportType, AbuseReport report) {
    }

    public static abstract class Builder<R extends Report> {
        protected final R report;
        protected final AbuseReportLimits limits;

        protected Builder(R r, AbuseReportLimits abuseReportLimits) {
            this.report = r;
            this.limits = abuseReportLimits;
        }

        public R report() {
            return this.report;
        }

        public UUID reportedProfileId() {
            return ((Report)this.report).reportedProfileId;
        }

        public String comments() {
            return ((Report)this.report).comments;
        }

        public boolean attested() {
            return ((Report)this.report()).attested;
        }

        public void setComments(String string) {
            ((Report)this.report).comments = string;
        }

        @Nullable
        public ReportReason reason() {
            return ((Report)this.report).reason;
        }

        public void setReason(ReportReason reportReason) {
            ((Report)this.report).reason = reportReason;
        }

        public void setAttested(boolean bl) {
            ((Report)this.report).attested = bl;
        }

        public abstract boolean hasContent();

        @Nullable
        public CannotBuildReason checkBuildable() {
            if (!((Report)this.report()).attested) {
                return CannotBuildReason.NOT_ATTESTED;
            }
            return null;
        }

        public abstract Either<Result, CannotBuildReason> build(ReportingContext var1);
    }
}

