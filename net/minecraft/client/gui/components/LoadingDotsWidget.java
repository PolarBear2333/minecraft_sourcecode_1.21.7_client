/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.components;

import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;

public class LoadingDotsWidget
extends AbstractWidget {
    private final Font font;

    public LoadingDotsWidget(Font font, Component component) {
        super(0, 0, font.width(component), font.lineHeight * 3, component);
        this.font = font;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int n, int n2, float f) {
        int n3 = this.getX() + this.getWidth() / 2;
        int n4 = this.getY() + this.getHeight() / 2;
        Component component = this.getMessage();
        guiGraphics.drawString(this.font, component, n3 - this.font.width(component) / 2, n4 - this.font.lineHeight, -1);
        String string = LoadingDotsText.get(Util.getMillis());
        guiGraphics.drawString(this.font, string, n3 - this.font.width(string) / 2, n4 + this.font.lineHeight, -8355712);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    @Nullable
    public ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
        return null;
    }
}

