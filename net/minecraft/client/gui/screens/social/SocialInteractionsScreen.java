/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.social;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.social.SocialInteractionsPlayerList;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonLinks;

public class SocialInteractionsScreen
extends Screen {
    private static final Component TITLE = Component.translatable("gui.socialInteractions.title");
    private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("social_interactions/background");
    private static final ResourceLocation SEARCH_SPRITE = ResourceLocation.withDefaultNamespace("icon/search");
    private static final Component TAB_ALL = Component.translatable("gui.socialInteractions.tab_all");
    private static final Component TAB_HIDDEN = Component.translatable("gui.socialInteractions.tab_hidden");
    private static final Component TAB_BLOCKED = Component.translatable("gui.socialInteractions.tab_blocked");
    private static final Component TAB_ALL_SELECTED = TAB_ALL.plainCopy().withStyle(ChatFormatting.UNDERLINE);
    private static final Component TAB_HIDDEN_SELECTED = TAB_HIDDEN.plainCopy().withStyle(ChatFormatting.UNDERLINE);
    private static final Component TAB_BLOCKED_SELECTED = TAB_BLOCKED.plainCopy().withStyle(ChatFormatting.UNDERLINE);
    private static final Component SEARCH_HINT = Component.translatable("gui.socialInteractions.search_hint").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY);
    static final Component EMPTY_SEARCH = Component.translatable("gui.socialInteractions.search_empty").withStyle(ChatFormatting.GRAY);
    private static final Component EMPTY_HIDDEN = Component.translatable("gui.socialInteractions.empty_hidden").withStyle(ChatFormatting.GRAY);
    private static final Component EMPTY_BLOCKED = Component.translatable("gui.socialInteractions.empty_blocked").withStyle(ChatFormatting.GRAY);
    private static final Component BLOCKING_HINT = Component.translatable("gui.socialInteractions.blocking_hint");
    private static final int BG_BORDER_SIZE = 8;
    private static final int BG_WIDTH = 236;
    private static final int SEARCH_HEIGHT = 16;
    private static final int MARGIN_Y = 64;
    public static final int SEARCH_START = 72;
    public static final int LIST_START = 88;
    private static final int IMAGE_WIDTH = 238;
    private static final int BUTTON_HEIGHT = 20;
    private static final int ITEM_HEIGHT = 36;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    @Nullable
    private final Screen lastScreen;
    @Nullable
    SocialInteractionsPlayerList socialInteractionsPlayerList;
    EditBox searchBox;
    private String lastSearch = "";
    private Page page = Page.ALL;
    private Button allButton;
    private Button hiddenButton;
    private Button blockedButton;
    private Button blockingHintButton;
    @Nullable
    private Component serverLabel;
    private int playerCount;

    public SocialInteractionsScreen() {
        this((Screen)null);
    }

    public SocialInteractionsScreen(@Nullable Screen screen) {
        super(TITLE);
        this.lastScreen = screen;
        this.updateServerLabel(Minecraft.getInstance());
    }

    private int windowHeight() {
        return Math.max(52, this.height - 128 - 16);
    }

    private int listEnd() {
        return 80 + this.windowHeight() - 8;
    }

    private int marginX() {
        return (this.width - 238) / 2;
    }

    @Override
    public Component getNarrationMessage() {
        if (this.serverLabel != null) {
            return CommonComponents.joinForNarration(super.getNarrationMessage(), this.serverLabel);
        }
        return super.getNarrationMessage();
    }

    @Override
    protected void init() {
        this.layout.addTitleHeader(TITLE, this.font);
        this.socialInteractionsPlayerList = new SocialInteractionsPlayerList(this, this.minecraft, this.width, this.listEnd() - 88, 88, 36);
        int n = this.socialInteractionsPlayerList.getRowWidth() / 3;
        int n2 = this.socialInteractionsPlayerList.getRowLeft();
        int n3 = this.socialInteractionsPlayerList.getRowRight();
        this.allButton = this.addRenderableWidget(Button.builder(TAB_ALL, button -> this.showPage(Page.ALL)).bounds(n2, 45, n, 20).build());
        this.hiddenButton = this.addRenderableWidget(Button.builder(TAB_HIDDEN, button -> this.showPage(Page.HIDDEN)).bounds((n2 + n3 - n) / 2 + 1, 45, n, 20).build());
        this.blockedButton = this.addRenderableWidget(Button.builder(TAB_BLOCKED, button -> this.showPage(Page.BLOCKED)).bounds(n3 - n + 1, 45, n, 20).build());
        String string = this.searchBox != null ? this.searchBox.getValue() : "";
        this.searchBox = new EditBox(this.font, this.marginX() + 28, 74, 200, 15, SEARCH_HINT){

            @Override
            protected MutableComponent createNarrationMessage() {
                if (!SocialInteractionsScreen.this.searchBox.getValue().isEmpty() && SocialInteractionsScreen.this.socialInteractionsPlayerList.isEmpty()) {
                    return super.createNarrationMessage().append(", ").append(EMPTY_SEARCH);
                }
                return super.createNarrationMessage();
            }
        };
        this.searchBox.setMaxLength(16);
        this.searchBox.setVisible(true);
        this.searchBox.setTextColor(-1);
        this.searchBox.setValue(string);
        this.searchBox.setHint(SEARCH_HINT);
        this.searchBox.setResponder(this::checkSearchStringUpdate);
        this.addRenderableWidget(this.searchBox);
        this.addWidget(this.socialInteractionsPlayerList);
        this.blockingHintButton = this.addRenderableWidget(Button.builder(BLOCKING_HINT, ConfirmLinkScreen.confirmLink((Screen)this, CommonLinks.BLOCKING_HELP)).bounds(this.width / 2 - 100, 64 + this.windowHeight(), 200, 20).build());
        this.showPage(this.page);
        this.layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).width(200).build());
        this.layout.visitWidgets(guiEventListener -> {
            AbstractWidget cfr_ignored_0 = (AbstractWidget)this.addRenderableWidget(guiEventListener);
        });
        this.repositionElements();
    }

    @Override
    public void added() {
        if (this.socialInteractionsPlayerList != null) {
            this.socialInteractionsPlayerList.refreshHasDraftReport();
        }
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        this.socialInteractionsPlayerList.updateSizeAndPosition(this.width, this.listEnd() - 88, 88);
        this.searchBox.setPosition(this.marginX() + 28, 74);
        int n = this.socialInteractionsPlayerList.getRowLeft();
        int n2 = this.socialInteractionsPlayerList.getRowRight();
        int n3 = this.socialInteractionsPlayerList.getRowWidth() / 3;
        this.allButton.setPosition(n, 45);
        this.hiddenButton.setPosition((n + n2 - n3) / 2 + 1, 45);
        this.blockedButton.setPosition(n2 - n3 + 1, 45);
        this.blockingHintButton.setPosition(this.width / 2 - 100, 64 + this.windowHeight());
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.searchBox);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    private void showPage(Page page) {
        Collection<UUID> collection;
        this.page = page;
        this.allButton.setMessage(TAB_ALL);
        this.hiddenButton.setMessage(TAB_HIDDEN);
        this.blockedButton.setMessage(TAB_BLOCKED);
        boolean bl = false;
        switch (page.ordinal()) {
            case 0: {
                this.allButton.setMessage(TAB_ALL_SELECTED);
                collection = this.minecraft.player.connection.getOnlinePlayerIds();
                this.socialInteractionsPlayerList.updatePlayerList(collection, this.socialInteractionsPlayerList.scrollAmount(), true);
                break;
            }
            case 1: {
                this.hiddenButton.setMessage(TAB_HIDDEN_SELECTED);
                collection = this.minecraft.getPlayerSocialManager().getHiddenPlayers();
                bl = collection.isEmpty();
                this.socialInteractionsPlayerList.updatePlayerList(collection, this.socialInteractionsPlayerList.scrollAmount(), false);
                break;
            }
            case 2: {
                this.blockedButton.setMessage(TAB_BLOCKED_SELECTED);
                collection = this.minecraft.getPlayerSocialManager();
                Set<UUID> set = this.minecraft.player.connection.getOnlinePlayerIds().stream().filter(arg_0 -> collection.isBlocked(arg_0)).collect(Collectors.toSet());
                bl = set.isEmpty();
                this.socialInteractionsPlayerList.updatePlayerList(set, this.socialInteractionsPlayerList.scrollAmount(), false);
            }
        }
        collection = this.minecraft.getNarrator();
        if (!this.searchBox.getValue().isEmpty() && this.socialInteractionsPlayerList.isEmpty() && !this.searchBox.isFocused()) {
            ((GameNarrator)((Object)collection)).saySystemNow(EMPTY_SEARCH);
        } else if (bl) {
            if (page == Page.HIDDEN) {
                ((GameNarrator)((Object)collection)).saySystemNow(EMPTY_HIDDEN);
            } else if (page == Page.BLOCKED) {
                ((GameNarrator)((Object)collection)).saySystemNow(EMPTY_BLOCKED);
            }
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.renderBackground(guiGraphics, n, n2, f);
        int n3 = this.marginX() + 3;
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND_SPRITE, n3, 64, 236, this.windowHeight() + 16);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SEARCH_SPRITE, n3 + 10, 76, 12, 12);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.render(guiGraphics, n, n2, f);
        this.updateServerLabel(this.minecraft);
        if (this.serverLabel != null) {
            guiGraphics.drawString(this.minecraft.font, this.serverLabel, this.marginX() + 8, 35, -1);
        }
        if (!this.socialInteractionsPlayerList.isEmpty()) {
            this.socialInteractionsPlayerList.render(guiGraphics, n, n2, f);
        } else if (!this.searchBox.getValue().isEmpty()) {
            guiGraphics.drawCenteredString(this.minecraft.font, EMPTY_SEARCH, this.width / 2, (72 + this.listEnd()) / 2, -1);
        } else if (this.page == Page.HIDDEN) {
            guiGraphics.drawCenteredString(this.minecraft.font, EMPTY_HIDDEN, this.width / 2, (72 + this.listEnd()) / 2, -1);
        } else if (this.page == Page.BLOCKED) {
            guiGraphics.drawCenteredString(this.minecraft.font, EMPTY_BLOCKED, this.width / 2, (72 + this.listEnd()) / 2, -1);
        }
        this.blockingHintButton.visible = this.page == Page.BLOCKED;
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (!this.searchBox.isFocused() && this.minecraft.options.keySocialInteractions.matches(n, n2)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(n, n2, n3);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void checkSearchStringUpdate(String string) {
        if (!(string = string.toLowerCase(Locale.ROOT)).equals(this.lastSearch)) {
            this.socialInteractionsPlayerList.setFilter(string);
            this.lastSearch = string;
            this.showPage(this.page);
        }
    }

    private void updateServerLabel(Minecraft minecraft) {
        int n = minecraft.getConnection().getOnlinePlayers().size();
        if (this.playerCount != n) {
            String string = "";
            ServerData serverData = minecraft.getCurrentServer();
            if (minecraft.isLocalServer()) {
                string = minecraft.getSingleplayerServer().getMotd();
            } else if (serverData != null) {
                string = serverData.name;
            }
            this.serverLabel = n > 1 ? Component.translatable("gui.socialInteractions.server_label.multiple", string, n) : Component.translatable("gui.socialInteractions.server_label.single", string, n);
            this.playerCount = n;
        }
    }

    public void onAddPlayer(PlayerInfo playerInfo) {
        this.socialInteractionsPlayerList.addPlayer(playerInfo, this.page);
    }

    public void onRemovePlayer(UUID uUID) {
        this.socialInteractionsPlayerList.removePlayer(uUID);
    }

    public static enum Page {
        ALL,
        HIDDEN,
        BLOCKED;

    }
}

