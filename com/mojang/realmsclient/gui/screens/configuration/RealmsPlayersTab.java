/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.gui.screens.configuration;

import com.google.common.collect.ImmutableList;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.Ops;
import com.mojang.realmsclient.dto.PlayerInfo;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.gui.screens.RealmsConfirmScreen;
import com.mojang.realmsclient.gui.screens.configuration.RealmsConfigurationTab;
import com.mojang.realmsclient.gui.screens.configuration.RealmsConfigureWorldScreen;
import com.mojang.realmsclient.gui.screens.configuration.RealmsInviteScreen;
import com.mojang.realmsclient.util.RealmsUtil;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executor;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.tabs.GridLayoutTab;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

class RealmsPlayersTab
extends GridLayoutTab
implements RealmsConfigurationTab {
    static final Logger LOGGER = LogUtils.getLogger();
    static final Component TITLE = Component.translatable("mco.configure.world.players.title");
    static final Component QUESTION_TITLE = Component.translatable("mco.question");
    private static final int PADDING = 8;
    final RealmsConfigureWorldScreen configurationScreen;
    final Minecraft minecraft;
    RealmsServer serverData;
    private final InvitedObjectSelectionList invitedList;

    RealmsPlayersTab(RealmsConfigureWorldScreen realmsConfigureWorldScreen, Minecraft minecraft, RealmsServer realmsServer) {
        super(TITLE);
        this.configurationScreen = realmsConfigureWorldScreen;
        this.minecraft = minecraft;
        this.serverData = realmsServer;
        GridLayout.RowHelper rowHelper = this.layout.spacing(8).createRowHelper(1);
        this.invitedList = rowHelper.addChild(new InvitedObjectSelectionList(realmsConfigureWorldScreen.width, this.calculateListHeight()), LayoutSettings.defaults().alignVerticallyTop().alignHorizontallyCenter());
        rowHelper.addChild(Button.builder(Component.translatable("mco.configure.world.buttons.invite"), button -> minecraft.setScreen(new RealmsInviteScreen(realmsConfigureWorldScreen, realmsServer))).build(), LayoutSettings.defaults().alignVerticallyBottom().alignHorizontallyCenter());
        this.updateData(realmsServer);
    }

    public int calculateListHeight() {
        return this.configurationScreen.getContentHeight() - 20 - 16;
    }

    @Override
    public void doLayout(ScreenRectangle screenRectangle) {
        this.invitedList.setSize(this.configurationScreen.width, this.calculateListHeight());
        super.doLayout(screenRectangle);
    }

    @Override
    public void updateData(RealmsServer realmsServer) {
        this.serverData = realmsServer;
        this.invitedList.children().clear();
        for (PlayerInfo playerInfo : realmsServer.players) {
            this.invitedList.children().add(new Entry(playerInfo));
        }
    }

    class InvitedObjectSelectionList
    extends ContainerObjectSelectionList<Entry> {
        private static final int ITEM_HEIGHT = 36;

        public InvitedObjectSelectionList(int n, int n2) {
            Minecraft minecraft = Minecraft.getInstance();
            int n3 = RealmsPlayersTab.this.configurationScreen.getHeaderHeight();
            Objects.requireNonNull(RealmsPlayersTab.this.configurationScreen.getFont());
            super(minecraft, n, n2, n3, 36, (int)(9.0f * 1.5f));
        }

        @Override
        protected void renderHeader(GuiGraphics guiGraphics, int n, int n2) {
            String string = RealmsPlayersTab.this.serverData.players != null ? Integer.toString(RealmsPlayersTab.this.serverData.players.size()) : "0";
            MutableComponent mutableComponent = Component.translatable("mco.configure.world.invited.number", string).withStyle(ChatFormatting.UNDERLINE);
            guiGraphics.drawString(RealmsPlayersTab.this.configurationScreen.getFont(), mutableComponent, n + this.getRowWidth() / 2 - RealmsPlayersTab.this.configurationScreen.getFont().width(mutableComponent) / 2, n2, -1);
        }

        @Override
        protected void renderListBackground(GuiGraphics guiGraphics) {
        }

        @Override
        protected void renderListSeparators(GuiGraphics guiGraphics) {
        }

        @Override
        public int getRowWidth() {
            return 300;
        }
    }

    class Entry
    extends ContainerObjectSelectionList.Entry<Entry> {
        protected static final int SKIN_FACE_SIZE = 32;
        private static final Component NORMAL_USER_TEXT = Component.translatable("mco.configure.world.invites.normal.tooltip");
        private static final Component OP_TEXT = Component.translatable("mco.configure.world.invites.ops.tooltip");
        private static final Component REMOVE_TEXT = Component.translatable("mco.configure.world.invites.remove.tooltip");
        private static final ResourceLocation MAKE_OP_SPRITE = ResourceLocation.withDefaultNamespace("player_list/make_operator");
        private static final ResourceLocation REMOVE_OP_SPRITE = ResourceLocation.withDefaultNamespace("player_list/remove_operator");
        private static final ResourceLocation REMOVE_PLAYER_SPRITE = ResourceLocation.withDefaultNamespace("player_list/remove_player");
        private static final int ICON_WIDTH = 8;
        private static final int ICON_HEIGHT = 7;
        private final PlayerInfo playerInfo;
        private final Button removeButton;
        private final Button makeOpButton;
        private final Button removeOpButton;

        public Entry(PlayerInfo playerInfo) {
            this.playerInfo = playerInfo;
            int n = RealmsPlayersTab.this.serverData.players.indexOf(this.playerInfo);
            this.makeOpButton = SpriteIconButton.builder(NORMAL_USER_TEXT, button -> this.op(n), false).sprite(MAKE_OP_SPRITE, 8, 7).width(16 + RealmsPlayersTab.this.configurationScreen.getFont().width(NORMAL_USER_TEXT)).narration(supplier -> CommonComponents.joinForNarration(Component.translatable("mco.invited.player.narration", playerInfo.getName()), (Component)supplier.get(), Component.translatable("narration.cycle_button.usage.focused", OP_TEXT))).build();
            this.removeOpButton = SpriteIconButton.builder(OP_TEXT, button -> this.deop(n), false).sprite(REMOVE_OP_SPRITE, 8, 7).width(16 + RealmsPlayersTab.this.configurationScreen.getFont().width(OP_TEXT)).narration(supplier -> CommonComponents.joinForNarration(Component.translatable("mco.invited.player.narration", playerInfo.getName()), (Component)supplier.get(), Component.translatable("narration.cycle_button.usage.focused", NORMAL_USER_TEXT))).build();
            this.removeButton = SpriteIconButton.builder(REMOVE_TEXT, button -> this.uninvite(n), false).sprite(REMOVE_PLAYER_SPRITE, 8, 7).width(16 + RealmsPlayersTab.this.configurationScreen.getFont().width(REMOVE_TEXT)).narration(supplier -> CommonComponents.joinForNarration(Component.translatable("mco.invited.player.narration", playerInfo.getName()), (Component)supplier.get())).build();
            this.updateOpButtons();
        }

        private void op(int n) {
            UUID uUID = RealmsPlayersTab.this.serverData.players.get(n).getUuid();
            RealmsUtil.supplyAsync(realmsClient -> realmsClient.op(RealmsPlayersTab.this.serverData.id, uUID), realmsServiceException -> LOGGER.error("Couldn't op the user", (Throwable)realmsServiceException)).thenAcceptAsync(ops -> {
                this.updateOps((Ops)ops);
                this.updateOpButtons();
                this.setFocused(this.removeOpButton);
            }, (Executor)RealmsPlayersTab.this.minecraft);
        }

        private void deop(int n) {
            UUID uUID = RealmsPlayersTab.this.serverData.players.get(n).getUuid();
            RealmsUtil.supplyAsync(realmsClient -> realmsClient.deop(RealmsPlayersTab.this.serverData.id, uUID), realmsServiceException -> LOGGER.error("Couldn't deop the user", (Throwable)realmsServiceException)).thenAcceptAsync(ops -> {
                this.updateOps((Ops)ops);
                this.updateOpButtons();
                this.setFocused(this.makeOpButton);
            }, (Executor)RealmsPlayersTab.this.minecraft);
        }

        private void uninvite(int n) {
            if (n >= 0 && n < RealmsPlayersTab.this.serverData.players.size()) {
                PlayerInfo playerInfo = RealmsPlayersTab.this.serverData.players.get(n);
                RealmsConfirmScreen realmsConfirmScreen = new RealmsConfirmScreen(bl -> {
                    if (bl) {
                        RealmsUtil.runAsync(realmsClient -> realmsClient.uninvite(RealmsPlayersTab.this.serverData.id, playerInfo.getUuid()), realmsServiceException -> LOGGER.error("Couldn't uninvite user", (Throwable)realmsServiceException));
                        RealmsPlayersTab.this.serverData.players.remove(n);
                        RealmsPlayersTab.this.updateData(RealmsPlayersTab.this.serverData);
                    }
                    RealmsPlayersTab.this.minecraft.setScreen(RealmsPlayersTab.this.configurationScreen);
                }, QUESTION_TITLE, Component.translatable("mco.configure.world.uninvite.player", playerInfo.getName()));
                RealmsPlayersTab.this.minecraft.setScreen(realmsConfirmScreen);
            }
        }

        private void updateOps(Ops ops) {
            for (PlayerInfo playerInfo : RealmsPlayersTab.this.serverData.players) {
                playerInfo.setOperator(ops.ops.contains(playerInfo.getName()));
            }
        }

        private void updateOpButtons() {
            this.makeOpButton.visible = !this.playerInfo.isOperator();
            this.removeOpButton.visible = !this.makeOpButton.visible;
        }

        private Button activeOpButton() {
            if (this.makeOpButton.visible) {
                return this.makeOpButton;
            }
            return this.removeOpButton;
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of((Object)this.activeOpButton(), (Object)this.removeButton);
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of((Object)this.activeOpButton(), (Object)this.removeButton);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
            int n8 = !this.playerInfo.getAccepted() ? -6250336 : (this.playerInfo.getOnline() ? -16711936 : -1);
            int n9 = n2 + n5 / 2 - 16;
            RealmsUtil.renderPlayerFace(guiGraphics, n3, n9, 32, this.playerInfo.getUuid());
            int n10 = n2 + n5 / 2 - RealmsPlayersTab.this.configurationScreen.getFont().lineHeight / 2;
            guiGraphics.drawString(RealmsPlayersTab.this.configurationScreen.getFont(), this.playerInfo.getName(), n3 + 8 + 32, n10, n8);
            int n11 = n2 + n5 / 2 - 10;
            int n12 = n3 + n4 - this.removeButton.getWidth();
            this.removeButton.setPosition(n12, n11);
            this.removeButton.render(guiGraphics, n6, n7, f);
            int n13 = n12 - this.activeOpButton().getWidth() - 8;
            this.makeOpButton.setPosition(n13, n11);
            this.makeOpButton.render(guiGraphics, n6, n7, f);
            this.removeOpButton.setPosition(n13, n11);
            this.removeOpButton.render(guiGraphics, n6, n7, f);
        }
    }
}

