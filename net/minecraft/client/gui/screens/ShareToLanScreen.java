/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens;

import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.commands.PublishCommand;
import net.minecraft.util.HttpUtil;
import net.minecraft.world.level.GameType;

public class ShareToLanScreen
extends Screen {
    private static final int PORT_LOWER_BOUND = 1024;
    private static final int PORT_HIGHER_BOUND = 65535;
    private static final Component ALLOW_COMMANDS_LABEL = Component.translatable("selectWorld.allowCommands");
    private static final Component GAME_MODE_LABEL = Component.translatable("selectWorld.gameMode");
    private static final Component INFO_TEXT = Component.translatable("lanServer.otherPlayers");
    private static final Component PORT_INFO_TEXT = Component.translatable("lanServer.port");
    private static final Component PORT_UNAVAILABLE = Component.translatable("lanServer.port.unavailable", 1024, 65535);
    private static final Component INVALID_PORT = Component.translatable("lanServer.port.invalid", 1024, 65535);
    private static final int INVALID_PORT_COLOR = -43691;
    private final Screen lastScreen;
    private GameType gameMode = GameType.SURVIVAL;
    private boolean commands;
    private int port = HttpUtil.getAvailablePort();
    @Nullable
    private EditBox portEdit;

    public ShareToLanScreen(Screen screen) {
        super(Component.translatable("lanServer.title"));
        this.lastScreen = screen;
    }

    @Override
    protected void init() {
        IntegratedServer integratedServer = this.minecraft.getSingleplayerServer();
        this.gameMode = integratedServer.getDefaultGameType();
        this.commands = integratedServer.getWorldData().isAllowCommands();
        this.addRenderableWidget(CycleButton.builder(GameType::getShortDisplayName).withValues((GameType[])new GameType[]{GameType.SURVIVAL, GameType.SPECTATOR, GameType.CREATIVE, GameType.ADVENTURE}).withInitialValue(this.gameMode).create(this.width / 2 - 155, 100, 150, 20, GAME_MODE_LABEL, (cycleButton, gameType) -> {
            this.gameMode = gameType;
        }));
        this.addRenderableWidget(CycleButton.onOffBuilder(this.commands).create(this.width / 2 + 5, 100, 150, 20, ALLOW_COMMANDS_LABEL, (cycleButton, bl) -> {
            this.commands = bl;
        }));
        Button button2 = Button.builder(Component.translatable("lanServer.start"), button -> {
            this.minecraft.setScreen(null);
            MutableComponent mutableComponent = integratedServer.publishServer(this.gameMode, this.commands, this.port) ? PublishCommand.getSuccessMessage(this.port) : Component.translatable("commands.publish.failed");
            this.minecraft.gui.getChat().addMessage(mutableComponent);
            this.minecraft.getNarrator().saySystemQueued(mutableComponent);
            this.minecraft.updateTitle();
        }).bounds(this.width / 2 - 155, this.height - 28, 150, 20).build();
        this.portEdit = new EditBox(this.font, this.width / 2 - 75, 160, 150, 20, Component.translatable("lanServer.port"));
        this.portEdit.setResponder(string -> {
            Component component = this.tryParsePort((String)string);
            this.portEdit.setHint(Component.literal("" + this.port).withStyle(ChatFormatting.DARK_GRAY));
            if (component == null) {
                this.portEdit.setTextColor(-2039584);
                this.portEdit.setTooltip(null);
                button.active = true;
            } else {
                this.portEdit.setTextColor(-43691);
                this.portEdit.setTooltip(Tooltip.create(component));
                button.active = false;
            }
        });
        this.portEdit.setHint(Component.literal("" + this.port).withStyle(ChatFormatting.DARK_GRAY));
        this.addRenderableWidget(this.portEdit);
        this.addRenderableWidget(button2);
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose()).bounds(this.width / 2 + 5, this.height - 28, 150, 20).build());
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    @Nullable
    private Component tryParsePort(String string) {
        if (string.isBlank()) {
            this.port = HttpUtil.getAvailablePort();
            return null;
        }
        try {
            this.port = Integer.parseInt(string);
            if (this.port < 1024 || this.port > 65535) {
                return INVALID_PORT;
            }
            if (!HttpUtil.isPortAvailable(this.port)) {
                return PORT_UNAVAILABLE;
            }
            return null;
        }
        catch (NumberFormatException numberFormatException) {
            this.port = HttpUtil.getAvailablePort();
            return INVALID_PORT;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.render(guiGraphics, n, n2, f);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 50, -1);
        guiGraphics.drawCenteredString(this.font, INFO_TEXT, this.width / 2, 82, -1);
        guiGraphics.drawCenteredString(this.font, PORT_INFO_TEXT, this.width / 2, 142, -1);
    }
}

