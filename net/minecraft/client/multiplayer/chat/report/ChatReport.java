/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.authlib.minecraft.report.AbuseReport
 *  com.mojang.authlib.minecraft.report.AbuseReportLimits
 *  com.mojang.authlib.minecraft.report.ReportChatMessage
 *  com.mojang.authlib.minecraft.report.ReportEvidence
 *  com.mojang.authlib.minecraft.report.ReportedEntity
 *  com.mojang.datafixers.util.Either
 *  it.unimi.dsi.fastutil.ints.IntCollection
 *  it.unimi.dsi.fastutil.ints.IntOpenHashSet
 *  it.unimi.dsi.fastutil.ints.IntSet
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.StringUtils
 */
package net.minecraft.client.multiplayer.chat.report;

import com.google.common.collect.Lists;
import com.mojang.authlib.minecraft.report.AbuseReport;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.authlib.minecraft.report.ReportChatMessage;
import com.mojang.authlib.minecraft.report.ReportEvidence;
import com.mojang.authlib.minecraft.report.ReportedEntity;
import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Optionull;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.reporting.ChatReportScreen;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraft.client.multiplayer.chat.report.ChatReportContextBuilder;
import net.minecraft.client.multiplayer.chat.report.Report;
import net.minecraft.client.multiplayer.chat.report.ReportType;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.chat.SignedMessageLink;
import org.apache.commons.lang3.StringUtils;

public class ChatReport
extends Report {
    final IntSet reportedMessages = new IntOpenHashSet();

    ChatReport(UUID uUID, Instant instant, UUID uUID2) {
        super(uUID, instant, uUID2);
    }

    public void toggleReported(int n, AbuseReportLimits abuseReportLimits) {
        if (this.reportedMessages.contains(n)) {
            this.reportedMessages.remove(n);
        } else if (this.reportedMessages.size() < abuseReportLimits.maxReportedMessageCount()) {
            this.reportedMessages.add(n);
        }
    }

    @Override
    public ChatReport copy() {
        ChatReport chatReport = new ChatReport(this.reportId, this.createdAt, this.reportedProfileId);
        chatReport.reportedMessages.addAll((IntCollection)this.reportedMessages);
        chatReport.comments = this.comments;
        chatReport.reason = this.reason;
        chatReport.attested = this.attested;
        return chatReport;
    }

    @Override
    public Screen createScreen(Screen screen, ReportingContext reportingContext) {
        return new ChatReportScreen(screen, reportingContext, this);
    }

    @Override
    public /* synthetic */ Report copy() {
        return this.copy();
    }

    public static class Builder
    extends Report.Builder<ChatReport> {
        public Builder(ChatReport chatReport, AbuseReportLimits abuseReportLimits) {
            super(chatReport, abuseReportLimits);
        }

        public Builder(UUID uUID, AbuseReportLimits abuseReportLimits) {
            super(new ChatReport(UUID.randomUUID(), Instant.now(), uUID), abuseReportLimits);
        }

        public IntSet reportedMessages() {
            return ((ChatReport)this.report).reportedMessages;
        }

        public void toggleReported(int n) {
            ((ChatReport)this.report).toggleReported(n, this.limits);
        }

        public boolean isReported(int n) {
            return ((ChatReport)this.report).reportedMessages.contains(n);
        }

        @Override
        public boolean hasContent() {
            return StringUtils.isNotEmpty((CharSequence)this.comments()) || !this.reportedMessages().isEmpty() || this.reason() != null;
        }

        @Override
        @Nullable
        public Report.CannotBuildReason checkBuildable() {
            if (((ChatReport)this.report).reportedMessages.isEmpty()) {
                return Report.CannotBuildReason.NO_REPORTED_MESSAGES;
            }
            if (((ChatReport)this.report).reportedMessages.size() > this.limits.maxReportedMessageCount()) {
                return Report.CannotBuildReason.TOO_MANY_MESSAGES;
            }
            if (((ChatReport)this.report).reason == null) {
                return Report.CannotBuildReason.NO_REASON;
            }
            if (((ChatReport)this.report).comments.length() > this.limits.maxOpinionCommentsLength()) {
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
            String string = Objects.requireNonNull(((ChatReport)this.report).reason).backendName();
            ReportEvidence reportEvidence = this.buildEvidence(reportingContext);
            ReportedEntity reportedEntity = new ReportedEntity(((ChatReport)this.report).reportedProfileId);
            AbuseReport abuseReport = AbuseReport.chat((String)((ChatReport)this.report).comments, (String)string, (ReportEvidence)reportEvidence, (ReportedEntity)reportedEntity, (Instant)((ChatReport)this.report).createdAt);
            return Either.left((Object)new Report.Result(((ChatReport)this.report).reportId, ReportType.CHAT, abuseReport));
        }

        private ReportEvidence buildEvidence(ReportingContext reportingContext) {
            ArrayList arrayList = new ArrayList();
            ChatReportContextBuilder chatReportContextBuilder = new ChatReportContextBuilder(this.limits.leadingContextMessageCount());
            chatReportContextBuilder.collectAllContext(reportingContext.chatLog(), (IntCollection)((ChatReport)this.report).reportedMessages, (n, player) -> arrayList.add(this.buildReportedChatMessage(player, this.isReported(n))));
            return new ReportEvidence(Lists.reverse(arrayList));
        }

        private ReportChatMessage buildReportedChatMessage(LoggedChatMessage.Player player, boolean bl) {
            SignedMessageLink signedMessageLink = player.message().link();
            SignedMessageBody signedMessageBody = player.message().signedBody();
            List<ByteBuffer> list = signedMessageBody.lastSeen().entries().stream().map(MessageSignature::asByteBuffer).toList();
            ByteBuffer byteBuffer = Optionull.map(player.message().signature(), MessageSignature::asByteBuffer);
            return new ReportChatMessage(signedMessageLink.index(), signedMessageLink.sender(), signedMessageLink.sessionId(), signedMessageBody.timeStamp(), signedMessageBody.salt(), list, signedMessageBody.content(), byteBuffer, bl);
        }

        public Builder copy() {
            return new Builder(((ChatReport)this.report).copy(), this.limits);
        }
    }
}

