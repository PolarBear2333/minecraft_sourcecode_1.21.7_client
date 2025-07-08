/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens;

import com.mojang.realmsclient.RealmsMainScreen;
import java.net.URI;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.toasts.NowPlayingToast;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ShareToLanScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ServerLinks;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.dialog.Dialogs;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DialogTags;
import net.minecraft.util.CommonLinks;

public class PauseScreen
extends Screen {
    private static final ResourceLocation DRAFT_REPORT_SPRITE = ResourceLocation.withDefaultNamespace("icon/draft_report");
    private static final int COLUMNS = 2;
    private static final int MENU_PADDING_TOP = 50;
    private static final int BUTTON_PADDING = 4;
    private static final int BUTTON_WIDTH_FULL = 204;
    private static final int BUTTON_WIDTH_HALF = 98;
    private static final Component RETURN_TO_GAME = Component.translatable("menu.returnToGame");
    private static final Component ADVANCEMENTS = Component.translatable("gui.advancements");
    private static final Component STATS = Component.translatable("gui.stats");
    private static final Component SEND_FEEDBACK = Component.translatable("menu.sendFeedback");
    private static final Component REPORT_BUGS = Component.translatable("menu.reportBugs");
    private static final Component FEEDBACK_SUBSCREEN = Component.translatable("menu.feedback");
    private static final Component OPTIONS = Component.translatable("menu.options");
    private static final Component SHARE_TO_LAN = Component.translatable("menu.shareToLan");
    private static final Component PLAYER_REPORTING = Component.translatable("menu.playerReporting");
    private static final Component GAME = Component.translatable("menu.game");
    private static final Component PAUSED = Component.translatable("menu.paused");
    private static final Tooltip CUSTOM_OPTIONS_TOOLTIP = Tooltip.create(Component.translatable("menu.custom_options.tooltip"));
    private final boolean showPauseMenu;
    @Nullable
    private Button disconnectButton;

    public PauseScreen(boolean bl) {
        super(bl ? GAME : PAUSED);
        this.showPauseMenu = bl;
    }

    public boolean showsPauseMenu() {
        return this.showPauseMenu;
    }

    @Override
    protected void init() {
        if (this.showPauseMenu) {
            this.createPauseMenu();
        }
        this.addRenderableWidget(new StringWidget(0, this.showPauseMenu ? 40 : 10, this.width, this.font.lineHeight, this.title, this.font));
    }

    private void createPauseMenu() {
        GridLayout gridLayout = new GridLayout();
        gridLayout.defaultCellSetting().padding(4, 4, 4, 0);
        GridLayout.RowHelper rowHelper = gridLayout.createRowHelper(2);
        rowHelper.addChild(Button.builder(RETURN_TO_GAME, button -> {
            this.minecraft.setScreen(null);
            this.minecraft.mouseHandler.grabMouse();
        }).width(204).build(), 2, gridLayout.newCellSettings().paddingTop(50));
        rowHelper.addChild(this.openScreenButton(ADVANCEMENTS, () -> new AdvancementsScreen(this.minecraft.player.connection.getAdvancements(), this)));
        rowHelper.addChild(this.openScreenButton(STATS, () -> new StatsScreen(this, this.minecraft.player.getStats())));
        Optional<? extends Holder<Dialog>> optional = this.getCustomAdditions();
        if (optional.isEmpty()) {
            PauseScreen.addFeedbackButtons(this, rowHelper);
        } else {
            this.addFeedbackSubscreenAndCustomDialogButtons(this.minecraft, optional.get(), rowHelper);
        }
        rowHelper.addChild(this.openScreenButton(OPTIONS, () -> new OptionsScreen(this, this.minecraft.options)));
        if (this.minecraft.hasSingleplayerServer() && !this.minecraft.getSingleplayerServer().isPublished()) {
            rowHelper.addChild(this.openScreenButton(SHARE_TO_LAN, () -> new ShareToLanScreen(this)));
        } else {
            rowHelper.addChild(this.openScreenButton(PLAYER_REPORTING, () -> new SocialInteractionsScreen(this)));
        }
        this.disconnectButton = rowHelper.addChild(Button.builder(CommonComponents.disconnectButtonLabel(this.minecraft.isLocalServer()), button -> {
            button.active = false;
            this.minecraft.getReportingContext().draftReportHandled(this.minecraft, this, () -> PauseScreen.disconnectFromWorld(this.minecraft, ClientLevel.DEFAULT_QUIT_MESSAGE), true);
        }).width(204).build(), 2);
        gridLayout.arrangeElements();
        FrameLayout.alignInRectangle(gridLayout, 0, 0, this.width, this.height, 0.5f, 0.25f);
        gridLayout.visitWidgets(this::addRenderableWidget);
    }

    private Optional<? extends Holder<Dialog>> getCustomAdditions() {
        Object object;
        HolderLookup.RegistryLookup registryLookup = this.minecraft.player.connection.registryAccess().lookupOrThrow(Registries.DIALOG);
        Optional optional = registryLookup.get(DialogTags.PAUSE_SCREEN_ADDITIONS);
        if (optional.isPresent() && (object = (HolderSet)optional.get()).size() > 0) {
            if (object.size() == 1) {
                return Optional.of(object.get(0));
            }
            return registryLookup.get(Dialogs.CUSTOM_OPTIONS);
        }
        object = this.minecraft.player.connection.serverLinks();
        if (!((ServerLinks)object).isEmpty()) {
            return registryLookup.get(Dialogs.SERVER_LINKS);
        }
        return Optional.empty();
    }

    static void addFeedbackButtons(Screen screen, GridLayout.RowHelper rowHelper) {
        rowHelper.addChild(PauseScreen.openLinkButton(screen, SEND_FEEDBACK, SharedConstants.getCurrentVersion().stable() ? CommonLinks.RELEASE_FEEDBACK : CommonLinks.SNAPSHOT_FEEDBACK));
        rowHelper.addChild(PauseScreen.openLinkButton((Screen)screen, (Component)PauseScreen.REPORT_BUGS, (URI)CommonLinks.SNAPSHOT_BUGS_FEEDBACK)).active = !SharedConstants.getCurrentVersion().dataVersion().isSideSeries();
    }

    private void addFeedbackSubscreenAndCustomDialogButtons(Minecraft minecraft, Holder<Dialog> holder, GridLayout.RowHelper rowHelper) {
        rowHelper.addChild(this.openScreenButton(FEEDBACK_SUBSCREEN, () -> new FeedbackSubScreen(this)));
        rowHelper.addChild(Button.builder(holder.value().common().computeExternalTitle(), button -> minecraft.player.connection.showDialog(holder, this)).width(98).tooltip(CUSTOM_OPTIONS_TOOLTIP).build());
    }

    public static void disconnectFromWorld(Minecraft minecraft, Component component) {
        boolean bl = minecraft.isLocalServer();
        ServerData serverData = minecraft.getCurrentServer();
        if (minecraft.level != null) {
            minecraft.level.disconnect(component);
        }
        if (bl) {
            minecraft.disconnectWithSavingScreen();
        } else {
            minecraft.disconnectWithProgressScreen();
        }
        TitleScreen titleScreen = new TitleScreen();
        if (bl) {
            minecraft.setScreen(titleScreen);
        } else if (serverData != null && serverData.isRealm()) {
            minecraft.setScreen(new RealmsMainScreen(titleScreen));
        } else {
            minecraft.setScreen(new JoinMultiplayerScreen(titleScreen));
        }
    }

    @Override
    public void tick() {
        if (this.rendersNowPlayingToast()) {
            NowPlayingToast.tickMusicNotes();
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.render(guiGraphics, n, n2, f);
        if (this.rendersNowPlayingToast()) {
            NowPlayingToast.renderToast(guiGraphics, this.font);
        }
        if (this.showPauseMenu && this.minecraft != null && this.minecraft.getReportingContext().hasDraftReport() && this.disconnectButton != null) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, DRAFT_REPORT_SPRITE, this.disconnectButton.getX() + this.disconnectButton.getWidth() - 17, this.disconnectButton.getY() + 3, 15, 15);
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int n, int n2, float f) {
        if (this.showPauseMenu) {
            super.renderBackground(guiGraphics, n, n2, f);
        }
    }

    public boolean rendersNowPlayingToast() {
        Options options = this.minecraft.options;
        return options.showNowPlayingToast().get() != false && options.getFinalSoundSourceVolume(SoundSource.MUSIC) > 0.0f && this.showPauseMenu;
    }

    private Button openScreenButton(Component component, Supplier<Screen> supplier) {
        return Button.builder(component, button -> this.minecraft.setScreen((Screen)supplier.get())).width(98).build();
    }

    private static Button openLinkButton(Screen screen, Component component, URI uRI) {
        return Button.builder(component, ConfirmLinkScreen.confirmLink(screen, uRI)).width(98).build();
    }

    static class FeedbackSubScreen
    extends Screen {
        private static final Component TITLE = Component.translatable("menu.feedback.title");
        public final Screen parent;
        private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);

        protected FeedbackSubScreen(Screen screen) {
            super(TITLE);
            this.parent = screen;
        }

        @Override
        protected void init() {
            this.layout.addTitleHeader(TITLE, this.font);
            GridLayout gridLayout = this.layout.addToContents(new GridLayout());
            gridLayout.defaultCellSetting().padding(4, 4, 4, 0);
            GridLayout.RowHelper rowHelper = gridLayout.createRowHelper(2);
            PauseScreen.addFeedbackButtons(this, rowHelper);
            this.layout.addToFooter(Button.builder(CommonComponents.GUI_BACK, button -> this.onClose()).width(200).build());
            this.layout.visitWidgets(this::addRenderableWidget);
            this.repositionElements();
        }

        @Override
        protected void repositionElements() {
            this.layout.arrangeElements();
        }

        @Override
        public void onClose() {
            this.minecraft.setScreen(this.parent);
        }
    }
}

