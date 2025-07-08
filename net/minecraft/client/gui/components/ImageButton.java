/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ImageButton
extends Button {
    protected final WidgetSprites sprites;

    public ImageButton(int n, int n2, int n3, int n4, WidgetSprites widgetSprites, Button.OnPress onPress) {
        this(n, n2, n3, n4, widgetSprites, onPress, CommonComponents.EMPTY);
    }

    public ImageButton(int n, int n2, int n3, int n4, WidgetSprites widgetSprites, Button.OnPress onPress, Component component) {
        super(n, n2, n3, n4, component, onPress, DEFAULT_NARRATION);
        this.sprites = widgetSprites;
    }

    public ImageButton(int n, int n2, WidgetSprites widgetSprites, Button.OnPress onPress, Component component) {
        this(0, 0, n, n2, widgetSprites, onPress, component);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int n, int n2, float f) {
        ResourceLocation resourceLocation = this.sprites.get(this.isActive(), this.isHoveredOrFocused());
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation, this.getX(), this.getY(), this.width, this.height);
    }
}

