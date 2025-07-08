/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.components;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

public class Checkbox
extends AbstractButton {
    private static final ResourceLocation CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("widget/checkbox_selected_highlighted");
    private static final ResourceLocation CHECKBOX_SELECTED_SPRITE = ResourceLocation.withDefaultNamespace("widget/checkbox_selected");
    private static final ResourceLocation CHECKBOX_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("widget/checkbox_highlighted");
    private static final ResourceLocation CHECKBOX_SPRITE = ResourceLocation.withDefaultNamespace("widget/checkbox");
    private static final int TEXT_COLOR = -2039584;
    private static final int SPACING = 4;
    private static final int BOX_PADDING = 8;
    private boolean selected;
    private final OnValueChange onValueChange;
    private final MultiLineTextWidget textWidget;

    Checkbox(int n, int n2, int n3, Component component, Font font, boolean bl, OnValueChange onValueChange) {
        super(n, n2, 0, 0, component);
        this.width = this.getAdjustedWidth(n3, component, font);
        this.textWidget = new MultiLineTextWidget(component, font).setMaxWidth(this.width).setColor(-2039584);
        this.height = this.getAdjustedHeight(font);
        this.selected = bl;
        this.onValueChange = onValueChange;
    }

    private int getAdjustedWidth(int n, Component component, Font font) {
        return Math.min(Checkbox.getDefaultWidth(component, font), n);
    }

    private int getAdjustedHeight(Font font) {
        return Math.max(Checkbox.getBoxSize(font), this.textWidget.getHeight());
    }

    static int getDefaultWidth(Component component, Font font) {
        return Checkbox.getBoxSize(font) + 4 + font.width(component);
    }

    public static Builder builder(Component component, Font font) {
        return new Builder(component, font);
    }

    public static int getBoxSize(Font font) {
        return font.lineHeight + 8;
    }

    @Override
    public void onPress() {
        this.selected = !this.selected;
        this.onValueChange.onValueChange(this, this.selected);
    }

    public boolean selected() {
        return this.selected;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, (Component)this.createNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                narrationElementOutput.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.checkbox.usage.focused"));
            } else {
                narrationElementOutput.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.checkbox.usage.hovered"));
            }
        }
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int n, int n2, float f) {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        ResourceLocation resourceLocation = this.selected ? (this.isFocused() ? CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE : CHECKBOX_SELECTED_SPRITE) : (this.isFocused() ? CHECKBOX_HIGHLIGHTED_SPRITE : CHECKBOX_SPRITE);
        int n3 = Checkbox.getBoxSize(font);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation, this.getX(), this.getY(), n3, n3, ARGB.white(this.alpha));
        int n4 = this.getX() + n3 + 4;
        int n5 = this.getY() + n3 / 2 - this.textWidget.getHeight() / 2;
        this.textWidget.setPosition(n4, n5);
        this.textWidget.renderWidget(guiGraphics, n, n2, f);
    }

    public static interface OnValueChange {
        public static final OnValueChange NOP = (checkbox, bl) -> {};

        public void onValueChange(Checkbox var1, boolean var2);
    }

    public static class Builder {
        private final Component message;
        private final Font font;
        private int maxWidth;
        private int x = 0;
        private int y = 0;
        private OnValueChange onValueChange = OnValueChange.NOP;
        private boolean selected = false;
        @Nullable
        private OptionInstance<Boolean> option = null;
        @Nullable
        private Tooltip tooltip = null;

        Builder(Component component, Font font) {
            this.message = component;
            this.font = font;
            this.maxWidth = Checkbox.getDefaultWidth(component, font);
        }

        public Builder pos(int n, int n2) {
            this.x = n;
            this.y = n2;
            return this;
        }

        public Builder onValueChange(OnValueChange onValueChange) {
            this.onValueChange = onValueChange;
            return this;
        }

        public Builder selected(boolean bl) {
            this.selected = bl;
            this.option = null;
            return this;
        }

        public Builder selected(OptionInstance<Boolean> optionInstance) {
            this.option = optionInstance;
            this.selected = optionInstance.get();
            return this;
        }

        public Builder tooltip(Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public Builder maxWidth(int n) {
            this.maxWidth = n;
            return this;
        }

        public Checkbox build() {
            OnValueChange onValueChange = this.option == null ? this.onValueChange : (checkbox, bl) -> {
                this.option.set(bl);
                this.onValueChange.onValueChange(checkbox, bl);
            };
            Checkbox checkbox2 = new Checkbox(this.x, this.y, this.maxWidth, this.message, this.font, this.selected, onValueChange);
            checkbox2.setTooltip(this.tooltip);
            return checkbox2;
        }
    }
}

