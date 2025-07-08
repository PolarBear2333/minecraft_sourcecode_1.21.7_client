/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens;

import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.FocusableTextWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class GenericMessageScreen
extends Screen {
    @Nullable
    private FocusableTextWidget textWidget;

    public GenericMessageScreen(Component component) {
        super(component);
    }

    @Override
    protected void init() {
        this.textWidget = this.addRenderableWidget(new FocusableTextWidget(this.width, this.title, this.font, 12));
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        if (this.textWidget != null) {
            this.textWidget.containWithin(this.width);
            this.textWidget.setPosition(this.width / 2 - this.textWidget.getWidth() / 2, this.height / 2 - this.font.lineHeight / 2);
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected boolean shouldNarrateNavigation() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int n, int n2, float f) {
        this.renderPanorama(guiGraphics, f);
        this.renderBlurredBackground(guiGraphics);
        this.renderMenuBackground(guiGraphics);
    }
}

