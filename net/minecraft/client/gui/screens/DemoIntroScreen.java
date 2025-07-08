/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens;

import net.minecraft.Util;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonLinks;

public class DemoIntroScreen
extends Screen {
    private static final ResourceLocation DEMO_BACKGROUND_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/demo_background.png");
    private static final int BACKGROUND_TEXTURE_WIDTH = 256;
    private static final int BACKGROUND_TEXTURE_HEIGHT = 256;
    private MultiLineLabel movementMessage = MultiLineLabel.EMPTY;
    private MultiLineLabel durationMessage = MultiLineLabel.EMPTY;

    public DemoIntroScreen() {
        super(Component.translatable("demo.help.title"));
    }

    @Override
    protected void init() {
        int n = -16;
        this.addRenderableWidget(Button.builder(Component.translatable("demo.help.buy"), button -> {
            button.active = false;
            Util.getPlatform().openUri(CommonLinks.BUY_MINECRAFT_JAVA);
        }).bounds(this.width / 2 - 116, this.height / 2 + 62 + -16, 114, 20).build());
        this.addRenderableWidget(Button.builder(Component.translatable("demo.help.later"), button -> {
            this.minecraft.setScreen(null);
            this.minecraft.mouseHandler.grabMouse();
        }).bounds(this.width / 2 + 2, this.height / 2 + 62 + -16, 114, 20).build());
        Options options = this.minecraft.options;
        this.movementMessage = MultiLineLabel.create(this.font, Component.translatable("demo.help.movementShort", options.keyUp.getTranslatedKeyMessage(), options.keyLeft.getTranslatedKeyMessage(), options.keyDown.getTranslatedKeyMessage(), options.keyRight.getTranslatedKeyMessage()), Component.translatable("demo.help.movementMouse"), Component.translatable("demo.help.jump", options.keyJump.getTranslatedKeyMessage()), Component.translatable("demo.help.inventory", options.keyInventory.getTranslatedKeyMessage()));
        this.durationMessage = MultiLineLabel.create(this.font, (Component)Component.translatable("demo.help.fullWrapped"), 218);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.renderBackground(guiGraphics, n, n2, f);
        int n3 = (this.width - 248) / 2;
        int n4 = (this.height - 166) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, DEMO_BACKGROUND_LOCATION, n3, n4, 0.0f, 0.0f, 248, 166, 256, 256);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.render(guiGraphics, n, n2, f);
        int n3 = (this.width - 248) / 2 + 10;
        int n4 = (this.height - 166) / 2 + 8;
        guiGraphics.drawString(this.font, this.title, n3, n4, -14737633, false);
        n4 = this.movementMessage.renderLeftAlignedNoShadow(guiGraphics, n3, n4 + 12, 12, -11579569);
        this.durationMessage.renderLeftAlignedNoShadow(guiGraphics, n3, n4 + 20, this.font.lineHeight, -14737633);
    }
}

