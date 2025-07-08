/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.social;

import com.google.common.collect.ImmutableList;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.reporting.ReportPlayerScreen;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

public class PlayerEntry
extends ContainerObjectSelectionList.Entry<PlayerEntry> {
    private static final ResourceLocation DRAFT_REPORT_SPRITE = ResourceLocation.withDefaultNamespace("icon/draft_report");
    private static final Duration TOOLTIP_DELAY = Duration.ofMillis(500L);
    private static final WidgetSprites REPORT_BUTTON_SPRITES = new WidgetSprites(ResourceLocation.withDefaultNamespace("social_interactions/report_button"), ResourceLocation.withDefaultNamespace("social_interactions/report_button_disabled"), ResourceLocation.withDefaultNamespace("social_interactions/report_button_highlighted"));
    private static final WidgetSprites MUTE_BUTTON_SPRITES = new WidgetSprites(ResourceLocation.withDefaultNamespace("social_interactions/mute_button"), ResourceLocation.withDefaultNamespace("social_interactions/mute_button_highlighted"));
    private static final WidgetSprites UNMUTE_BUTTON_SPRITES = new WidgetSprites(ResourceLocation.withDefaultNamespace("social_interactions/unmute_button"), ResourceLocation.withDefaultNamespace("social_interactions/unmute_button_highlighted"));
    private final Minecraft minecraft;
    private final List<AbstractWidget> children;
    private final UUID id;
    private final String playerName;
    private final Supplier<PlayerSkin> skinGetter;
    private boolean isRemoved;
    private boolean hasRecentMessages;
    private final boolean reportingEnabled;
    private boolean hasDraftReport;
    private final boolean chatReportable;
    @Nullable
    private Button hideButton;
    @Nullable
    private Button showButton;
    @Nullable
    private Button reportButton;
    private float tooltipHoverTime;
    private static final Component HIDDEN = Component.translatable("gui.socialInteractions.status_hidden").withStyle(ChatFormatting.ITALIC);
    private static final Component BLOCKED = Component.translatable("gui.socialInteractions.status_blocked").withStyle(ChatFormatting.ITALIC);
    private static final Component OFFLINE = Component.translatable("gui.socialInteractions.status_offline").withStyle(ChatFormatting.ITALIC);
    private static final Component HIDDEN_OFFLINE = Component.translatable("gui.socialInteractions.status_hidden_offline").withStyle(ChatFormatting.ITALIC);
    private static final Component BLOCKED_OFFLINE = Component.translatable("gui.socialInteractions.status_blocked_offline").withStyle(ChatFormatting.ITALIC);
    private static final Component REPORT_DISABLED_TOOLTIP = Component.translatable("gui.socialInteractions.tooltip.report.disabled");
    private static final Component HIDE_TEXT_TOOLTIP = Component.translatable("gui.socialInteractions.tooltip.hide");
    private static final Component SHOW_TEXT_TOOLTIP = Component.translatable("gui.socialInteractions.tooltip.show");
    private static final Component REPORT_PLAYER_TOOLTIP = Component.translatable("gui.socialInteractions.tooltip.report");
    private static final int SKIN_SIZE = 24;
    private static final int PADDING = 4;
    public static final int SKIN_SHADE = ARGB.color(190, 0, 0, 0);
    private static final int CHAT_TOGGLE_ICON_SIZE = 20;
    public static final int BG_FILL = ARGB.color(255, 74, 74, 74);
    public static final int BG_FILL_REMOVED = ARGB.color(255, 48, 48, 48);
    public static final int PLAYERNAME_COLOR = ARGB.color(255, 255, 255, 255);
    public static final int PLAYER_STATUS_COLOR = ARGB.color(140, 255, 255, 255);

    public PlayerEntry(Minecraft minecraft, SocialInteractionsScreen socialInteractionsScreen, UUID uUID, String string, Supplier<PlayerSkin> supplier, boolean bl) {
        boolean bl2;
        this.minecraft = minecraft;
        this.id = uUID;
        this.playerName = string;
        this.skinGetter = supplier;
        ReportingContext reportingContext = minecraft.getReportingContext();
        this.reportingEnabled = reportingContext.sender().isEnabled();
        this.chatReportable = bl;
        this.refreshHasDraftReport(reportingContext);
        MutableComponent mutableComponent = Component.translatable("gui.socialInteractions.narration.hide", string);
        MutableComponent mutableComponent2 = Component.translatable("gui.socialInteractions.narration.show", string);
        PlayerSocialManager playerSocialManager = minecraft.getPlayerSocialManager();
        boolean bl3 = minecraft.getChatStatus().isChatAllowed(minecraft.isLocalServer());
        boolean bl4 = bl2 = !minecraft.player.getUUID().equals(uUID);
        if (bl2 && bl3 && !playerSocialManager.isBlocked(uUID)) {
            this.reportButton = new ImageButton(0, 0, 20, 20, REPORT_BUTTON_SPRITES, button -> reportingContext.draftReportHandled(minecraft, socialInteractionsScreen, () -> minecraft.setScreen(new ReportPlayerScreen(socialInteractionsScreen, reportingContext, this)), false), Component.translatable("gui.socialInteractions.report")){

                @Override
                protected MutableComponent createNarrationMessage() {
                    return PlayerEntry.this.getEntryNarationMessage(super.createNarrationMessage());
                }
            };
            this.reportButton.active = this.reportingEnabled;
            this.reportButton.setTooltip(this.createReportButtonTooltip());
            this.reportButton.setTooltipDelay(TOOLTIP_DELAY);
            this.hideButton = new ImageButton(0, 0, 20, 20, MUTE_BUTTON_SPRITES, button -> {
                playerSocialManager.hidePlayer(uUID);
                this.onHiddenOrShown(true, Component.translatable("gui.socialInteractions.hidden_in_chat", string));
            }, Component.translatable("gui.socialInteractions.hide")){

                @Override
                protected MutableComponent createNarrationMessage() {
                    return PlayerEntry.this.getEntryNarationMessage(super.createNarrationMessage());
                }
            };
            this.hideButton.setTooltip(Tooltip.create(HIDE_TEXT_TOOLTIP, mutableComponent));
            this.hideButton.setTooltipDelay(TOOLTIP_DELAY);
            this.showButton = new ImageButton(0, 0, 20, 20, UNMUTE_BUTTON_SPRITES, button -> {
                playerSocialManager.showPlayer(uUID);
                this.onHiddenOrShown(false, Component.translatable("gui.socialInteractions.shown_in_chat", string));
            }, Component.translatable("gui.socialInteractions.show")){

                @Override
                protected MutableComponent createNarrationMessage() {
                    return PlayerEntry.this.getEntryNarationMessage(super.createNarrationMessage());
                }
            };
            this.showButton.setTooltip(Tooltip.create(SHOW_TEXT_TOOLTIP, mutableComponent2));
            this.showButton.setTooltipDelay(TOOLTIP_DELAY);
            this.children = new ArrayList<AbstractWidget>();
            this.children.add(this.hideButton);
            this.children.add(this.reportButton);
            this.updateHideAndShowButton(playerSocialManager.isHidden(this.id));
        } else {
            this.children = ImmutableList.of();
        }
    }

    public void refreshHasDraftReport(ReportingContext reportingContext) {
        this.hasDraftReport = reportingContext.hasDraftReportFor(this.id);
    }

    private Tooltip createReportButtonTooltip() {
        if (!this.reportingEnabled) {
            return Tooltip.create(REPORT_DISABLED_TOOLTIP);
        }
        return Tooltip.create(REPORT_PLAYER_TOOLTIP, Component.translatable("gui.socialInteractions.narration.report", this.playerName));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
        int n8;
        int n9 = n3 + 4;
        int n10 = n2 + (n5 - 24) / 2;
        int n11 = n9 + 24 + 4;
        Component component = this.getStatusComponent();
        if (component == CommonComponents.EMPTY) {
            guiGraphics.fill(n3, n2, n3 + n4, n2 + n5, BG_FILL);
            n8 = n2 + (n5 - this.minecraft.font.lineHeight) / 2;
        } else {
            guiGraphics.fill(n3, n2, n3 + n4, n2 + n5, BG_FILL_REMOVED);
            n8 = n2 + (n5 - (this.minecraft.font.lineHeight + this.minecraft.font.lineHeight)) / 2;
            guiGraphics.drawString(this.minecraft.font, component, n11, n8 + 12, PLAYER_STATUS_COLOR);
        }
        PlayerFaceRenderer.draw(guiGraphics, this.skinGetter.get(), n9, n10, 24);
        guiGraphics.drawString(this.minecraft.font, this.playerName, n11, n8, PLAYERNAME_COLOR);
        if (this.isRemoved) {
            guiGraphics.fill(n9, n10, n9 + 24, n10 + 24, SKIN_SHADE);
        }
        if (this.hideButton != null && this.showButton != null && this.reportButton != null) {
            float f2 = this.tooltipHoverTime;
            this.hideButton.setX(n3 + (n4 - this.hideButton.getWidth() - 4) - 20 - 4);
            this.hideButton.setY(n2 + (n5 - this.hideButton.getHeight()) / 2);
            this.hideButton.render(guiGraphics, n6, n7, f);
            this.showButton.setX(n3 + (n4 - this.showButton.getWidth() - 4) - 20 - 4);
            this.showButton.setY(n2 + (n5 - this.showButton.getHeight()) / 2);
            this.showButton.render(guiGraphics, n6, n7, f);
            this.reportButton.setX(n3 + (n4 - this.showButton.getWidth() - 4));
            this.reportButton.setY(n2 + (n5 - this.showButton.getHeight()) / 2);
            this.reportButton.render(guiGraphics, n6, n7, f);
            if (f2 == this.tooltipHoverTime) {
                this.tooltipHoverTime = 0.0f;
            }
        }
        if (this.hasDraftReport && this.reportButton != null) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, DRAFT_REPORT_SPRITE, this.reportButton.getX() + 5, this.reportButton.getY() + 1, 15, 15);
        }
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return this.children;
    }

    @Override
    public List<? extends NarratableEntry> narratables() {
        return this.children;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public UUID getPlayerId() {
        return this.id;
    }

    public Supplier<PlayerSkin> getSkinGetter() {
        return this.skinGetter;
    }

    public void setRemoved(boolean bl) {
        this.isRemoved = bl;
    }

    public boolean isRemoved() {
        return this.isRemoved;
    }

    public void setHasRecentMessages(boolean bl) {
        this.hasRecentMessages = bl;
    }

    public boolean hasRecentMessages() {
        return this.hasRecentMessages;
    }

    public boolean isChatReportable() {
        return this.chatReportable;
    }

    private void onHiddenOrShown(boolean bl, Component component) {
        this.updateHideAndShowButton(bl);
        this.minecraft.gui.getChat().addMessage(component);
        this.minecraft.getNarrator().saySystemNow(component);
    }

    private void updateHideAndShowButton(boolean bl) {
        this.showButton.visible = bl;
        this.hideButton.visible = !bl;
        this.children.set(0, bl ? this.showButton : this.hideButton);
    }

    MutableComponent getEntryNarationMessage(MutableComponent mutableComponent) {
        Component component = this.getStatusComponent();
        if (component == CommonComponents.EMPTY) {
            return Component.literal(this.playerName).append(", ").append(mutableComponent);
        }
        return Component.literal(this.playerName).append(", ").append(component).append(", ").append(mutableComponent);
    }

    private Component getStatusComponent() {
        boolean bl = this.minecraft.getPlayerSocialManager().isHidden(this.id);
        boolean bl2 = this.minecraft.getPlayerSocialManager().isBlocked(this.id);
        if (bl2 && this.isRemoved) {
            return BLOCKED_OFFLINE;
        }
        if (bl && this.isRemoved) {
            return HIDDEN_OFFLINE;
        }
        if (bl2) {
            return BLOCKED;
        }
        if (bl) {
            return HIDDEN;
        }
        if (this.isRemoved) {
            return OFFLINE;
        }
        return CommonComponents.EMPTY;
    }
}

