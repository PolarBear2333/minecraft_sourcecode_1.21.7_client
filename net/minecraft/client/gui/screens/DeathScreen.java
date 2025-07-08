/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  it.unimi.dsi.fastutil.booleans.BooleanConsumer
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

public class DeathScreen
extends Screen {
    private static final ResourceLocation DRAFT_REPORT_SPRITE = ResourceLocation.withDefaultNamespace("icon/draft_report");
    private int delayTicker;
    private final Component causeOfDeath;
    private final boolean hardcore;
    private Component deathScore;
    private final List<Button> exitButtons = Lists.newArrayList();
    @Nullable
    private Button exitToTitleButton;

    public DeathScreen(@Nullable Component component, boolean bl) {
        super(Component.translatable(bl ? "deathScreen.title.hardcore" : "deathScreen.title"));
        this.causeOfDeath = component;
        this.hardcore = bl;
    }

    @Override
    protected void init() {
        this.delayTicker = 0;
        this.exitButtons.clear();
        MutableComponent mutableComponent = this.hardcore ? Component.translatable("deathScreen.spectate") : Component.translatable("deathScreen.respawn");
        this.exitButtons.add(this.addRenderableWidget(Button.builder(mutableComponent, button -> {
            this.minecraft.player.respawn();
            button.active = false;
        }).bounds(this.width / 2 - 100, this.height / 4 + 72, 200, 20).build()));
        this.exitToTitleButton = this.addRenderableWidget(Button.builder(Component.translatable("deathScreen.titleScreen"), button -> this.minecraft.getReportingContext().draftReportHandled(this.minecraft, this, this::handleExitToTitleScreen, true)).bounds(this.width / 2 - 100, this.height / 4 + 96, 200, 20).build());
        this.exitButtons.add(this.exitToTitleButton);
        this.setButtonsActive(false);
        this.deathScore = Component.translatable("deathScreen.score.value", Component.literal(Integer.toString(this.minecraft.player.getScore())).withStyle(ChatFormatting.YELLOW));
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private void handleExitToTitleScreen() {
        if (this.hardcore) {
            this.exitToTitleScreen();
            return;
        }
        TitleConfirmScreen titleConfirmScreen = new TitleConfirmScreen(bl -> {
            if (bl) {
                this.exitToTitleScreen();
            } else {
                this.minecraft.player.respawn();
                this.minecraft.setScreen(null);
            }
        }, Component.translatable("deathScreen.quit.confirm"), CommonComponents.EMPTY, Component.translatable("deathScreen.titleScreen"), Component.translatable("deathScreen.respawn"));
        this.minecraft.setScreen(titleConfirmScreen);
        titleConfirmScreen.setDelay(20);
    }

    private void exitToTitleScreen() {
        if (this.minecraft.level != null) {
            this.minecraft.level.disconnect(ClientLevel.DEFAULT_QUIT_MESSAGE);
        }
        this.minecraft.disconnectWithSavingScreen();
        this.minecraft.setScreen(new TitleScreen());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.render(guiGraphics, n, n2, f);
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().scale(2.0f, 2.0f);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2 / 2, 30, -1);
        guiGraphics.pose().popMatrix();
        if (this.causeOfDeath != null) {
            guiGraphics.drawCenteredString(this.font, this.causeOfDeath, this.width / 2, 85, -1);
        }
        guiGraphics.drawCenteredString(this.font, this.deathScore, this.width / 2, 100, -1);
        if (this.causeOfDeath != null && n2 > 85 && n2 < 85 + this.font.lineHeight) {
            Style style = this.getClickedComponentStyleAt(n);
            guiGraphics.renderComponentHoverEffect(this.font, style, n, n2);
        }
        if (this.exitToTitleButton != null && this.minecraft.getReportingContext().hasDraftReport()) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, DRAFT_REPORT_SPRITE, this.exitToTitleButton.getX() + this.exitToTitleButton.getWidth() - 17, this.exitToTitleButton.getY() + 3, 15, 15);
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int n, int n2, float f) {
        DeathScreen.renderDeathBackground(guiGraphics, this.width, this.height);
    }

    static void renderDeathBackground(GuiGraphics guiGraphics, int n, int n2) {
        guiGraphics.fillGradient(0, 0, n, n2, 0x60500000, -1602211792);
    }

    @Nullable
    private Style getClickedComponentStyleAt(int n) {
        if (this.causeOfDeath == null) {
            return null;
        }
        int n2 = this.minecraft.font.width(this.causeOfDeath);
        int n3 = this.width / 2 - n2 / 2;
        int n4 = this.width / 2 + n2 / 2;
        if (n < n3 || n > n4) {
            return null;
        }
        return this.minecraft.font.getSplitter().componentStyleAtWidth(this.causeOfDeath, n - n3);
    }

    @Override
    public boolean mouseClicked(double d, double d2, int n) {
        ClickEvent clickEvent;
        Style style;
        if (this.causeOfDeath != null && d2 > 85.0 && d2 < (double)(85 + this.font.lineHeight) && (style = this.getClickedComponentStyleAt((int)d)) != null && (clickEvent = style.getClickEvent()) instanceof ClickEvent.OpenUrl) {
            ClickEvent.OpenUrl openUrl = (ClickEvent.OpenUrl)clickEvent;
            return DeathScreen.clickUrlAction(this.minecraft, this, openUrl.uri());
        }
        return super.mouseClicked(d, d2, n);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        ++this.delayTicker;
        if (this.delayTicker == 20) {
            this.setButtonsActive(true);
        }
    }

    private void setButtonsActive(boolean bl) {
        for (Button button : this.exitButtons) {
            button.active = bl;
        }
    }

    public static class TitleConfirmScreen
    extends ConfirmScreen {
        public TitleConfirmScreen(BooleanConsumer booleanConsumer, Component component, Component component2, Component component3, Component component4) {
            super(booleanConsumer, component, component2, component3, component4);
        }

        @Override
        public void renderBackground(GuiGraphics guiGraphics, int n, int n2, float f) {
            DeathScreen.renderDeathBackground(guiGraphics, this.width, this.height);
        }
    }
}

