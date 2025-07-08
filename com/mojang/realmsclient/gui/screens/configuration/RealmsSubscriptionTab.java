/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.gui.screens.configuration;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.Subscription;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.gui.screens.RealmsPopups;
import com.mojang.realmsclient.gui.screens.configuration.RealmsConfigurationTab;
import com.mojang.realmsclient.gui.screens.configuration.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.util.RealmsUtil;
import java.text.DateFormat;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.FocusableTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonLinks;
import org.slf4j.Logger;

class RealmsSubscriptionTab
extends GridLayoutTab
implements RealmsConfigurationTab {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int DEFAULT_COMPONENT_WIDTH = 200;
    private static final int EXTRA_SPACING = 2;
    private static final int DEFAULT_SPACING = 6;
    static final Component TITLE = Component.translatable("mco.configure.world.subscription.tab");
    private static final Component SUBSCRIPTION_START_LABEL = Component.translatable("mco.configure.world.subscription.start");
    private static final Component TIME_LEFT_LABEL = Component.translatable("mco.configure.world.subscription.timeleft");
    private static final Component DAYS_LEFT_LABEL = Component.translatable("mco.configure.world.subscription.recurring.daysleft");
    private static final Component SUBSCRIPTION_EXPIRED_TEXT = Component.translatable("mco.configure.world.subscription.expired").withStyle(ChatFormatting.GRAY);
    private static final Component SUBSCRIPTION_LESS_THAN_A_DAY_TEXT = Component.translatable("mco.configure.world.subscription.less_than_a_day").withStyle(ChatFormatting.GRAY);
    private static final Component UNKNOWN = Component.translatable("mco.configure.world.subscription.unknown");
    private static final Component RECURRING_INFO = Component.translatable("mco.configure.world.subscription.recurring.info");
    private final RealmsConfigureWorldScreen configurationScreen;
    private final Minecraft minecraft;
    private final Button deleteButton;
    private final FocusableTextWidget subscriptionInfo;
    private final StringWidget startDateWidget;
    private final StringWidget daysLeftLabelWidget;
    private final StringWidget daysLeftWidget;
    private RealmsServer serverData;
    private Component daysLeft = UNKNOWN;
    private Component startDate = UNKNOWN;
    @Nullable
    private Subscription.SubscriptionType type;

    RealmsSubscriptionTab(RealmsConfigureWorldScreen realmsConfigureWorldScreen, Minecraft minecraft, RealmsServer realmsServer) {
        super(TITLE);
        this.configurationScreen = realmsConfigureWorldScreen;
        this.minecraft = minecraft;
        this.serverData = realmsServer;
        GridLayout.RowHelper rowHelper = this.layout.rowSpacing(6).createRowHelper(1);
        Font font = realmsConfigureWorldScreen.getFont();
        rowHelper.addChild(new StringWidget(200, font.lineHeight, SUBSCRIPTION_START_LABEL, font).alignLeft());
        this.startDateWidget = rowHelper.addChild(new StringWidget(200, font.lineHeight, this.startDate, font).alignLeft());
        rowHelper.addChild(SpacerElement.height(2));
        this.daysLeftLabelWidget = rowHelper.addChild(new StringWidget(200, font.lineHeight, TIME_LEFT_LABEL, font).alignLeft());
        this.daysLeftWidget = rowHelper.addChild(new StringWidget(200, font.lineHeight, this.daysLeft, font).alignLeft());
        rowHelper.addChild(SpacerElement.height(2));
        rowHelper.addChild(Button.builder(Component.translatable("mco.configure.world.subscription.extend"), button -> ConfirmLinkScreen.confirmLinkNow((Screen)realmsConfigureWorldScreen, CommonLinks.extendRealms(realmsServer.remoteSubscriptionId, minecraft.getUser().getProfileId()))).bounds(0, 0, 200, 20).build());
        rowHelper.addChild(SpacerElement.height(2));
        this.deleteButton = rowHelper.addChild(Button.builder(Component.translatable("mco.configure.world.delete.button"), button -> minecraft.setScreen(RealmsPopups.warningPopupScreen(realmsConfigureWorldScreen, Component.translatable("mco.configure.world.delete.question.line1"), popupScreen -> this.deleteRealm()))).bounds(0, 0, 200, 20).build());
        rowHelper.addChild(SpacerElement.height(2));
        this.subscriptionInfo = rowHelper.addChild(new FocusableTextWidget(200, Component.empty(), font, true, true, 4), LayoutSettings.defaults().alignHorizontallyCenter());
        this.subscriptionInfo.setMaxWidth(200);
        this.subscriptionInfo.setCentered(false);
        this.updateData(realmsServer);
    }

    private void deleteRealm() {
        RealmsUtil.runAsync(realmsClient -> realmsClient.deleteRealm(this.serverData.id), RealmsUtil.openScreenAndLogOnFailure(this.configurationScreen::createErrorScreen, "Couldn't delete world")).thenRunAsync(() -> this.minecraft.setScreen(this.configurationScreen.getLastScreen()), this.minecraft);
        this.minecraft.setScreen(this.configurationScreen);
    }

    private void getSubscription(long l) {
        RealmsClient realmsClient = RealmsClient.getOrCreate();
        try {
            Subscription subscription = realmsClient.subscriptionFor(l);
            this.daysLeft = this.daysLeftPresentation(subscription.daysLeft);
            this.startDate = RealmsSubscriptionTab.localPresentation(subscription.startDate);
            this.type = subscription.type;
        }
        catch (RealmsServiceException realmsServiceException) {
            LOGGER.error("Couldn't get subscription", (Throwable)realmsServiceException);
            this.minecraft.setScreen(this.configurationScreen.createErrorScreen(realmsServiceException));
        }
    }

    private static Component localPresentation(long l) {
        GregorianCalendar gregorianCalendar = new GregorianCalendar(TimeZone.getDefault());
        gregorianCalendar.setTimeInMillis(l);
        return Component.literal(DateFormat.getDateTimeInstance().format(gregorianCalendar.getTime())).withStyle(ChatFormatting.GRAY);
    }

    private Component daysLeftPresentation(int n) {
        boolean bl;
        if (n < 0 && this.serverData.expired) {
            return SUBSCRIPTION_EXPIRED_TEXT;
        }
        if (n <= 1) {
            return SUBSCRIPTION_LESS_THAN_A_DAY_TEXT;
        }
        int n2 = n / 30;
        int n3 = n % 30;
        boolean bl2 = n2 > 0;
        boolean bl3 = bl = n3 > 0;
        if (bl2 && bl) {
            return Component.translatable("mco.configure.world.subscription.remaining.months.days", n2, n3).withStyle(ChatFormatting.GRAY);
        }
        if (bl2) {
            return Component.translatable("mco.configure.world.subscription.remaining.months", n2).withStyle(ChatFormatting.GRAY);
        }
        if (bl) {
            return Component.translatable("mco.configure.world.subscription.remaining.days", n3).withStyle(ChatFormatting.GRAY);
        }
        return Component.empty();
    }

    @Override
    public void updateData(RealmsServer realmsServer) {
        this.serverData = realmsServer;
        this.getSubscription(realmsServer.id);
        this.startDateWidget.setMessage(this.startDate);
        if (this.type == Subscription.SubscriptionType.NORMAL) {
            this.daysLeftLabelWidget.setMessage(TIME_LEFT_LABEL);
        } else if (this.type == Subscription.SubscriptionType.RECURRING) {
            this.daysLeftLabelWidget.setMessage(DAYS_LEFT_LABEL);
        }
        this.daysLeftWidget.setMessage(this.daysLeft);
        boolean bl = RealmsMainScreen.isSnapshot() && realmsServer.parentWorldName != null;
        this.deleteButton.active = realmsServer.expired;
        if (bl) {
            this.subscriptionInfo.setMessage(Component.translatable("mco.snapshot.subscription.info", realmsServer.parentWorldName));
        } else {
            this.subscriptionInfo.setMessage(RECURRING_INFO);
        }
        this.layout.arrangeElements();
    }

    @Override
    public Component getTabExtraNarration() {
        return CommonComponents.joinLines(TITLE, SUBSCRIPTION_START_LABEL, this.startDate, TIME_LEFT_LABEL, this.daysLeft);
    }
}

