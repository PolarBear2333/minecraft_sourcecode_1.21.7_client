/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 */
package net.minecraft.client.gui.screens.debug;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundChangeGameModePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;

public class GameModeSwitcherScreen
extends Screen {
    static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("gamemode_switcher/slot");
    static final ResourceLocation SELECTION_SPRITE = ResourceLocation.withDefaultNamespace("gamemode_switcher/selection");
    private static final ResourceLocation GAMEMODE_SWITCHER_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/container/gamemode_switcher.png");
    private static final int SPRITE_SHEET_WIDTH = 128;
    private static final int SPRITE_SHEET_HEIGHT = 128;
    private static final int SLOT_AREA = 26;
    private static final int SLOT_PADDING = 5;
    private static final int SLOT_AREA_PADDED = 31;
    private static final int HELP_TIPS_OFFSET_Y = 5;
    private static final int ALL_SLOTS_WIDTH = GameModeIcon.values().length * 31 - 5;
    private static final Component SELECT_KEY = Component.translatable("debug.gamemodes.select_next", Component.translatable("debug.gamemodes.press_f4").withStyle(ChatFormatting.AQUA));
    private final GameModeIcon previousHovered;
    private GameModeIcon currentlyHovered;
    private int firstMouseX;
    private int firstMouseY;
    private boolean setFirstMousePos;
    private final List<GameModeSlot> slots = Lists.newArrayList();

    public GameModeSwitcherScreen() {
        super(GameNarrator.NO_TITLE);
        this.currentlyHovered = this.previousHovered = GameModeIcon.getFromGameType(this.getDefaultSelected());
    }

    private GameType getDefaultSelected() {
        MultiPlayerGameMode multiPlayerGameMode = Minecraft.getInstance().gameMode;
        GameType gameType = multiPlayerGameMode.getPreviousPlayerMode();
        if (gameType != null) {
            return gameType;
        }
        return multiPlayerGameMode.getPlayerMode() == GameType.CREATIVE ? GameType.SURVIVAL : GameType.CREATIVE;
    }

    @Override
    protected void init() {
        super.init();
        this.currentlyHovered = this.previousHovered;
        for (int i = 0; i < GameModeIcon.VALUES.length; ++i) {
            GameModeIcon gameModeIcon = GameModeIcon.VALUES[i];
            this.slots.add(new GameModeSlot(gameModeIcon, this.width / 2 - ALL_SLOTS_WIDTH / 2 + i * 31, this.height / 2 - 31));
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        if (this.checkToClose()) {
            return;
        }
        guiGraphics.drawCenteredString(this.font, this.currentlyHovered.name, this.width / 2, this.height / 2 - 31 - 20, -1);
        guiGraphics.drawCenteredString(this.font, SELECT_KEY, this.width / 2, this.height / 2 + 5, -1);
        if (!this.setFirstMousePos) {
            this.firstMouseX = n;
            this.firstMouseY = n2;
            this.setFirstMousePos = true;
        }
        boolean bl = this.firstMouseX == n && this.firstMouseY == n2;
        for (GameModeSlot gameModeSlot : this.slots) {
            gameModeSlot.render(guiGraphics, n, n2, f);
            gameModeSlot.setSelected(this.currentlyHovered == gameModeSlot.icon);
            if (bl || !gameModeSlot.isHoveredOrFocused()) continue;
            this.currentlyHovered = gameModeSlot.icon;
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int n, int n2, float f) {
        int n3 = this.width / 2 - 62;
        int n4 = this.height / 2 - 31 - 27;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, GAMEMODE_SWITCHER_LOCATION, n3, n4, 0.0f, 0.0f, 125, 75, 128, 128);
    }

    private void switchToHoveredGameMode() {
        GameModeSwitcherScreen.switchToHoveredGameMode(this.minecraft, this.currentlyHovered);
    }

    private static void switchToHoveredGameMode(Minecraft minecraft, GameModeIcon gameModeIcon) {
        if (minecraft.gameMode == null || minecraft.player == null) {
            return;
        }
        GameModeIcon gameModeIcon2 = GameModeIcon.getFromGameType(minecraft.gameMode.getPlayerMode());
        if (minecraft.player.hasPermissions(2) && gameModeIcon != gameModeIcon2) {
            minecraft.player.connection.send(new ServerboundChangeGameModePacket(gameModeIcon.mode));
        }
    }

    private boolean checkToClose() {
        if (!InputConstants.isKeyDown(this.minecraft.getWindow().getWindow(), 292)) {
            this.switchToHoveredGameMode();
            this.minecraft.setScreen(null);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (n == 293) {
            this.setFirstMousePos = false;
            this.currentlyHovered = this.currentlyHovered.getNext();
            return true;
        }
        return super.keyPressed(n, n2, n3);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    static enum GameModeIcon {
        CREATIVE(Component.translatable("gameMode.creative"), GameType.CREATIVE, new ItemStack(Blocks.GRASS_BLOCK)),
        SURVIVAL(Component.translatable("gameMode.survival"), GameType.SURVIVAL, new ItemStack(Items.IRON_SWORD)),
        ADVENTURE(Component.translatable("gameMode.adventure"), GameType.ADVENTURE, new ItemStack(Items.MAP)),
        SPECTATOR(Component.translatable("gameMode.spectator"), GameType.SPECTATOR, new ItemStack(Items.ENDER_EYE));

        static final GameModeIcon[] VALUES;
        private static final int ICON_AREA = 16;
        private static final int ICON_TOP_LEFT = 5;
        final Component name;
        final GameType mode;
        private final ItemStack renderStack;

        private GameModeIcon(Component component, GameType gameType, ItemStack itemStack) {
            this.name = component;
            this.mode = gameType;
            this.renderStack = itemStack;
        }

        void drawIcon(GuiGraphics guiGraphics, int n, int n2) {
            guiGraphics.renderItem(this.renderStack, n, n2);
        }

        GameModeIcon getNext() {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> SURVIVAL;
                case 1 -> ADVENTURE;
                case 2 -> SPECTATOR;
                case 3 -> CREATIVE;
            };
        }

        static GameModeIcon getFromGameType(GameType gameType) {
            return switch (gameType) {
                default -> throw new MatchException(null, null);
                case GameType.SPECTATOR -> SPECTATOR;
                case GameType.SURVIVAL -> SURVIVAL;
                case GameType.CREATIVE -> CREATIVE;
                case GameType.ADVENTURE -> ADVENTURE;
            };
        }

        static {
            VALUES = GameModeIcon.values();
        }
    }

    public static class GameModeSlot
    extends AbstractWidget {
        final GameModeIcon icon;
        private boolean isSelected;

        public GameModeSlot(GameModeIcon gameModeIcon, int n, int n2) {
            super(n, n2, 26, 26, gameModeIcon.name);
            this.icon = gameModeIcon;
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int n, int n2, float f) {
            this.drawSlot(guiGraphics);
            if (this.isSelected) {
                this.drawSelection(guiGraphics);
            }
            this.icon.drawIcon(guiGraphics, this.getX() + 5, this.getY() + 5);
        }

        @Override
        public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
            this.defaultButtonNarrationText(narrationElementOutput);
        }

        @Override
        public boolean isHoveredOrFocused() {
            return super.isHoveredOrFocused() || this.isSelected;
        }

        public void setSelected(boolean bl) {
            this.isSelected = bl;
        }

        private void drawSlot(GuiGraphics guiGraphics) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SPRITE, this.getX(), this.getY(), 26, 26);
        }

        private void drawSelection(GuiGraphics guiGraphics) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SELECTION_SPRITE, this.getX(), this.getY(), 26, 26);
        }
    }
}

