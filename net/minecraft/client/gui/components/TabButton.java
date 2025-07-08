/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.components.tabs.TabManager;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class TabButton
extends AbstractWidget {
    private static final WidgetSprites SPRITES = new WidgetSprites(ResourceLocation.withDefaultNamespace("widget/tab_selected"), ResourceLocation.withDefaultNamespace("widget/tab"), ResourceLocation.withDefaultNamespace("widget/tab_selected_highlighted"), ResourceLocation.withDefaultNamespace("widget/tab_highlighted"));
    private static final int SELECTED_OFFSET = 3;
    private static final int TEXT_MARGIN = 1;
    private static final int UNDERLINE_HEIGHT = 1;
    private static final int UNDERLINE_MARGIN_X = 4;
    private static final int UNDERLINE_MARGIN_BOTTOM = 2;
    private final TabManager tabManager;
    private final Tab tab;

    public TabButton(TabManager tabManager, Tab tab, int n, int n2) {
        super(0, 0, n, n2, tab.getTabTitle());
        this.tabManager = tabManager;
        this.tab = tab;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int n, int n2, float f) {
        int n3;
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITES.get(this.isSelected(), this.isHoveredOrFocused()), this.getX(), this.getY(), this.width, this.height);
        Font font = Minecraft.getInstance().font;
        int n4 = n3 = this.active ? -1 : -6250336;
        if (this.isSelected()) {
            this.renderMenuBackground(guiGraphics, this.getX() + 2, this.getY() + 2, this.getRight() - 2, this.getBottom());
            this.renderFocusUnderline(guiGraphics, font, n3);
        }
        this.renderString(guiGraphics, font, n3);
    }

    protected void renderMenuBackground(GuiGraphics guiGraphics, int n, int n2, int n3, int n4) {
        Screen.renderMenuBackgroundTexture(guiGraphics, Screen.MENU_BACKGROUND, n, n2, 0.0f, 0.0f, n3 - n, n4 - n2);
    }

    public void renderString(GuiGraphics guiGraphics, Font font, int n) {
        int n2 = this.getX() + 1;
        int n3 = this.getY() + (this.isSelected() ? 0 : 3);
        int n4 = this.getX() + this.getWidth() - 1;
        int n5 = this.getY() + this.getHeight();
        TabButton.renderScrollingString(guiGraphics, font, this.getMessage(), n2, n3, n4, n5, n);
    }

    private void renderFocusUnderline(GuiGraphics guiGraphics, Font font, int n) {
        int n2 = Math.min(font.width(this.getMessage()), this.getWidth() - 4);
        int n3 = this.getX() + (this.getWidth() - n2) / 2;
        int n4 = this.getY() + this.getHeight() - 2;
        guiGraphics.fill(n3, n4, n3 + n2, n4 + 1, n);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, (Component)Component.translatable("gui.narrate.tab", this.tab.getTabTitle()));
        narrationElementOutput.add(NarratedElementType.HINT, this.tab.getTabExtraNarration());
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
    }

    public Tab tab() {
        return this.tab;
    }

    public boolean isSelected() {
        return this.tabManager.getCurrentTab() == this.tab;
    }
}

