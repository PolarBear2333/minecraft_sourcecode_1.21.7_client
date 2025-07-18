/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.reporting;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.reporting.ChatReportScreen;
import net.minecraft.client.gui.screens.reporting.NameReportScreen;
import net.minecraft.client.gui.screens.reporting.SkinReportScreen;
import net.minecraft.client.gui.screens.social.PlayerEntry;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class ReportPlayerScreen
extends Screen {
    private static final Component TITLE = Component.translatable("gui.abuseReport.title");
    private static final Component MESSAGE = Component.translatable("gui.abuseReport.message");
    private static final Component REPORT_CHAT = Component.translatable("gui.abuseReport.type.chat");
    private static final Component REPORT_SKIN = Component.translatable("gui.abuseReport.type.skin");
    private static final Component REPORT_NAME = Component.translatable("gui.abuseReport.type.name");
    private static final int SPACING = 6;
    private final Screen lastScreen;
    private final ReportingContext context;
    private final PlayerEntry player;
    private final LinearLayout layout = LinearLayout.vertical().spacing(6);

    public ReportPlayerScreen(Screen screen, ReportingContext reportingContext, PlayerEntry playerEntry) {
        super(TITLE);
        this.lastScreen = screen;
        this.context = reportingContext;
        this.player = playerEntry;
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinForNarration(super.getNarrationMessage(), MESSAGE);
    }

    @Override
    protected void init() {
        this.layout.defaultCellSetting().alignHorizontallyCenter();
        this.layout.addChild(new StringWidget(this.title, this.font), this.layout.newCellSettings().paddingBottom(6));
        this.layout.addChild(new MultiLineTextWidget(MESSAGE, this.font).setCentered(true), this.layout.newCellSettings().paddingBottom(6));
        Button button2 = this.layout.addChild(Button.builder(REPORT_CHAT, button -> this.minecraft.setScreen(new ChatReportScreen(this.lastScreen, this.context, this.player.getPlayerId()))).build());
        if (!this.player.isChatReportable()) {
            button2.active = false;
            button2.setTooltip(Tooltip.create(Component.translatable("gui.socialInteractions.tooltip.report.not_reportable")));
        } else if (!this.player.hasRecentMessages()) {
            button2.active = false;
            button2.setTooltip(Tooltip.create(Component.translatable("gui.socialInteractions.tooltip.report.no_messages", this.player.getPlayerName())));
        }
        this.layout.addChild(Button.builder(REPORT_SKIN, button -> this.minecraft.setScreen(new SkinReportScreen(this.lastScreen, this.context, this.player.getPlayerId(), this.player.getSkinGetter()))).build());
        this.layout.addChild(Button.builder(REPORT_NAME, button -> this.minecraft.setScreen(new NameReportScreen(this.lastScreen, this.context, this.player.getPlayerId(), this.player.getPlayerName()))).build());
        this.layout.addChild(SpacerElement.height(20));
        this.layout.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose()).build());
        this.layout.visitWidgets(guiEventListener -> {
            AbstractWidget cfr_ignored_0 = (AbstractWidget)this.addRenderableWidget(guiEventListener);
        });
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        FrameLayout.centerInRectangle(this.layout, this.getRectangle());
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }
}

