/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.minecraft.BanDetails
 *  it.unimi.dsi.fastutil.booleans.BooleanConsumer
 *  org.apache.commons.lang3.StringUtils
 */
package net.minecraft.client.gui.screens;

import com.mojang.authlib.minecraft.BanDetails;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.multiplayer.chat.report.BanReason;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.CommonLinks;
import org.apache.commons.lang3.StringUtils;

public class BanNoticeScreens {
    private static final Component TEMPORARY_BAN_TITLE = Component.translatable("gui.banned.title.temporary").withStyle(ChatFormatting.BOLD);
    private static final Component PERMANENT_BAN_TITLE = Component.translatable("gui.banned.title.permanent").withStyle(ChatFormatting.BOLD);
    public static final Component NAME_BAN_TITLE = Component.translatable("gui.banned.name.title").withStyle(ChatFormatting.BOLD);
    private static final Component SKIN_BAN_TITLE = Component.translatable("gui.banned.skin.title").withStyle(ChatFormatting.BOLD);
    private static final Component SKIN_BAN_DESCRIPTION = Component.translatable("gui.banned.skin.description", Component.translationArg(CommonLinks.SUSPENSION_HELP));

    public static ConfirmLinkScreen create(BooleanConsumer booleanConsumer, BanDetails banDetails) {
        return new ConfirmLinkScreen(booleanConsumer, BanNoticeScreens.getBannedTitle(banDetails), BanNoticeScreens.getBannedScreenText(banDetails), CommonLinks.SUSPENSION_HELP, CommonComponents.GUI_ACKNOWLEDGE, true);
    }

    public static ConfirmLinkScreen createSkinBan(Runnable runnable) {
        URI uRI = CommonLinks.SUSPENSION_HELP;
        return new ConfirmLinkScreen(bl -> {
            if (bl) {
                Util.getPlatform().openUri(uRI);
            }
            runnable.run();
        }, SKIN_BAN_TITLE, SKIN_BAN_DESCRIPTION, uRI, CommonComponents.GUI_ACKNOWLEDGE, true);
    }

    public static ConfirmLinkScreen createNameBan(String string, Runnable runnable) {
        URI uRI = CommonLinks.SUSPENSION_HELP;
        return new ConfirmLinkScreen(bl -> {
            if (bl) {
                Util.getPlatform().openUri(uRI);
            }
            runnable.run();
        }, NAME_BAN_TITLE, (Component)Component.translatable("gui.banned.name.description", Component.literal(string).withStyle(ChatFormatting.YELLOW), Component.translationArg(CommonLinks.SUSPENSION_HELP)), uRI, CommonComponents.GUI_ACKNOWLEDGE, true);
    }

    private static Component getBannedTitle(BanDetails banDetails) {
        return BanNoticeScreens.isTemporaryBan(banDetails) ? TEMPORARY_BAN_TITLE : PERMANENT_BAN_TITLE;
    }

    private static Component getBannedScreenText(BanDetails banDetails) {
        return Component.translatable("gui.banned.description", BanNoticeScreens.getBanReasonText(banDetails), BanNoticeScreens.getBanStatusText(banDetails), Component.translationArg(CommonLinks.SUSPENSION_HELP));
    }

    private static Component getBanReasonText(BanDetails banDetails) {
        String string = banDetails.reason();
        String string2 = banDetails.reasonMessage();
        if (StringUtils.isNumeric((CharSequence)string)) {
            int n = Integer.parseInt(string);
            BanReason banReason = BanReason.byId(n);
            MutableComponent mutableComponent = banReason != null ? ComponentUtils.mergeStyles(banReason.title().copy(), Style.EMPTY.withBold(true)) : (string2 != null ? Component.translatable("gui.banned.description.reason_id_message", n, string2).withStyle(ChatFormatting.BOLD) : Component.translatable("gui.banned.description.reason_id", n).withStyle(ChatFormatting.BOLD));
            return Component.translatable("gui.banned.description.reason", mutableComponent);
        }
        return Component.translatable("gui.banned.description.unknownreason");
    }

    private static Component getBanStatusText(BanDetails banDetails) {
        if (BanNoticeScreens.isTemporaryBan(banDetails)) {
            Component component = BanNoticeScreens.getBanDurationText(banDetails);
            return Component.translatable("gui.banned.description.temporary", Component.translatable("gui.banned.description.temporary.duration", component).withStyle(ChatFormatting.BOLD));
        }
        return Component.translatable("gui.banned.description.permanent").withStyle(ChatFormatting.BOLD);
    }

    private static Component getBanDurationText(BanDetails banDetails) {
        Duration duration = Duration.between(Instant.now(), banDetails.expires());
        long l = duration.toHours();
        if (l > 72L) {
            return CommonComponents.days(duration.toDays());
        }
        if (l < 1L) {
            return CommonComponents.minutes(duration.toMinutes());
        }
        return CommonComponents.hours(duration.toHours());
    }

    private static boolean isTemporaryBan(BanDetails banDetails) {
        return banDetails.expires() != null;
    }
}

