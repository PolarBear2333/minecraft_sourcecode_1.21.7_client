/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

public abstract class AbstractButton
extends AbstractWidget {
    protected static final int TEXT_MARGIN = 2;
    private static final WidgetSprites SPRITES = new WidgetSprites(ResourceLocation.withDefaultNamespace("widget/button"), ResourceLocation.withDefaultNamespace("widget/button_disabled"), ResourceLocation.withDefaultNamespace("widget/button_highlighted"));

    public AbstractButton(int n, int n2, int n3, int n4, Component component) {
        super(n, n2, n3, n4, component);
    }

    public abstract void onPress();

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int n, int n2, float f) {
        Minecraft minecraft = Minecraft.getInstance();
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SPRITES.get(this.active, this.isHoveredOrFocused()), this.getX(), this.getY(), this.getWidth(), this.getHeight(), ARGB.white(this.alpha));
        int n3 = ARGB.color(this.alpha, this.active ? -1 : -6250336);
        this.renderString(guiGraphics, minecraft.font, n3);
    }

    public void renderString(GuiGraphics guiGraphics, Font font, int n) {
        this.renderScrollingString(guiGraphics, font, 2, n);
    }

    @Override
    public void onClick(double d, double d2) {
        this.onPress();
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (!this.active || !this.visible) {
            return false;
        }
        if (CommonInputs.selected(n)) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            this.onPress();
            return true;
        }
        return false;
    }
}

