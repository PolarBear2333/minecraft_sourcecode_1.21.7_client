/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.minecraft.report.AbuseReport
 *  com.mojang.authlib.minecraft.report.AbuseReportLimits
 *  com.mojang.authlib.minecraft.report.ReportedEntity
 *  com.mojang.datafixers.util.Either
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.StringUtils
 */
package net.minecraft.client.multiplayer.chat.report;

import com.mojang.authlib.minecraft.report.AbuseReport;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.authlib.minecraft.report.ReportedEntity;
import com.mojang.datafixers.util.Either;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.reporting.SkinReportScreen;
import net.minecraft.client.multiplayer.chat.report.Report;
import net.minecraft.client.multiplayer.chat.report.ReportType;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.client.resources.PlayerSkin;
import org.apache.commons.lang3.StringUtils;

public class SkinReport
extends Report {
    final Supplier<PlayerSkin> skinGetter;

    SkinReport(UUID uUID, Instant instant, UUID uUID2, Supplier<PlayerSkin> supplier) {
        super(uUID, instant, uUID2);
        this.skinGetter = supplier;
    }

    public Supplier<PlayerSkin> getSkinGetter() {
        return this.skinGetter;
    }

    @Override
    public SkinReport copy() {
        SkinReport skinReport = new SkinReport(this.reportId, this.createdAt, this.reportedProfileId, this.skinGetter);
        skinReport.comments = this.comments;
        skinReport.reason = this.reason;
        skinReport.attested = this.attested;
        return skinReport;
    }

    @Override
    public Screen createScreen(Screen screen, ReportingContext reportingContext) {
        return new SkinReportScreen(screen, reportingContext, this);
    }

    @Override
    public /* synthetic */ Report copy() {
        return this.copy();
    }

    public static class Builder
    extends Report.Builder<SkinReport> {
        public Builder(SkinReport skinReport, AbuseReportLimits abuseReportLimits) {
            super(skinReport, abuseReportLimits);
        }

        public Builder(UUID uUID, Supplier<PlayerSkin> supplier, AbuseReportLimits abuseReportLimits) {
            super(new SkinReport(UUID.randomUUID(), Instant.now(), uUID, supplier), abuseReportLimits);
        }

        @Override
        public boolean hasContent() {
            return StringUtils.isNotEmpty((CharSequence)this.comments()) || this.reason() != null;
        }

        @Override
        @Nullable
        public Report.CannotBuildReason checkBuildable() {
            if (((SkinReport)this.report).reason == null) {
                return Report.CannotBuildReason.NO_REASON;
            }
            if (((SkinReport)this.report).comments.length() > this.limits.maxOpinionCommentsLength()) {
                return Report.CannotBuildReason.COMMENT_TOO_LONG;
            }
            return super.checkBuildable();
        }

        @Override
        public Either<Report.Result, Report.CannotBuildReason> build(ReportingContext reportingContext) {
            Report.CannotBuildReason cannotBuildReason = this.checkBuildable();
            if (cannotBuildReason != null) {
                return Either.right((Object)cannotBuildReason);
            }
            String string = Objects.requireNonNull(((SkinReport)this.report).reason).backendName();
            ReportedEntity reportedEntity = new ReportedEntity(((SkinReport)this.report).reportedProfileId);
            PlayerSkin playerSkin = ((SkinReport)this.report).skinGetter.get();
            String string2 = playerSkin.textureUrl();
            AbuseReport abuseReport = AbuseReport.skin((String)((SkinReport)this.report).comments, (String)string, (String)string2, (ReportedEntity)reportedEntity, (Instant)((SkinReport)this.report).createdAt);
            return Either.left((Object)new Report.Result(((SkinReport)this.report).reportId, ReportType.SKIN, abuseReport));
        }
    }
}

