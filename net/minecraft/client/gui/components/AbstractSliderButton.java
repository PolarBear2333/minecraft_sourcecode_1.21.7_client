/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.components;

import net.minecraft.client.InputType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public abstract class AbstractSliderButton
extends AbstractWidget {
    private static final ResourceLocation SLIDER_SPRITE = ResourceLocation.withDefaultNamespace("widget/slider");
    private static final ResourceLocation HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("widget/slider_highlighted");
    private static final ResourceLocation SLIDER_HANDLE_SPRITE = ResourceLocation.withDefaultNamespace("widget/slider_handle");
    private static final ResourceLocation SLIDER_HANDLE_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("widget/slider_handle_highlighted");
    protected static final int TEXT_MARGIN = 2;
    public static final int DEFAULT_HEIGHT = 20;
    private static final int HANDLE_WIDTH = 8;
    private static final int HANDLE_HALF_WIDTH = 4;
    protected double value;
    private boolean canChangeValue;

    public AbstractSliderButton(int n, int n2, int n3, int n4, Component component, double d) {
        super(n, n2, n3, n4, component);
        this.value = d;
    }

    private ResourceLocation getSprite() {
        if (this.isActive() && this.isFocused() && !this.canChangeValue) {
            return HIGHLIGHTED_SPRITE;
        }
        return SLIDER_SPRITE;
    }

    private ResourceLocation getHandleSprite() {
        if (this.isActive() && (this.isHovered || this.canChangeValue)) {
            return SLIDER_HANDLE_HIGHLIGHTED_SPRITE;
        }
        return SLIDER_HANDLE_SPRITE;
    }

    @Override
    protected MutableComponent createNarrationMessage() {
        return Component.translatable("gui.narrate.slider", this.getMessage());
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, (Component)this.createNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                narrationElementOutput.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.slider.usage.focused"));
            } else {
                narrationElementOutput.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.slider.usage.hovered"));
            }
        }
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int n, int n2, float f) {
        Minecraft minecraft = Minecraft.getInstance();
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.getSprite(), this.getX(), this.getY(), this.getWidth(), this.getHeight(), ARGB.white(this.alpha));
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.getHandleSprite(), this.getX() + (int)(this.value * (double)(this.width - 8)), this.getY(), 8, this.getHeight(), ARGB.white(this.alpha));
        int n3 = ARGB.color(this.alpha, this.active ? -1 : -6250336);
        this.renderScrollingString(guiGraphics, minecraft.font, 2, n3);
    }

    @Override
    public void onClick(double d, double d2) {
        this.setValueFromMouse(d);
    }

    @Override
    public void setFocused(boolean bl) {
        super.setFocused(bl);
        if (!bl) {
            this.canChangeValue = false;
            return;
        }
        InputType inputType = Minecraft.getInstance().getLastInputType();
        if (inputType == InputType.MOUSE || inputType == InputType.KEYBOARD_TAB) {
            this.canChangeValue = true;
        }
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (CommonInputs.selected(n)) {
            this.canChangeValue = !this.canChangeValue;
            return true;
        }
        if (this.canChangeValue) {
            boolean bl;
            boolean bl2 = bl = n == 263;
            if (bl || n == 262) {
                float f = bl ? -1.0f : 1.0f;
                this.setValue(this.value + (double)(f / (float)(this.width - 8)));
                return true;
            }
        }
        return false;
    }

    private void setValueFromMouse(double d) {
        this.setValue((d - (double)(this.getX() + 4)) / (double)(this.width - 8));
    }

    private void setValue(double d) {
        double d2 = this.value;
        this.value = Mth.clamp(d, 0.0, 1.0);
        if (d2 != this.value) {
            this.applyValue();
        }
        this.updateMessage();
    }

    @Override
    protected void onDrag(double d, double d2, double d3, double d4) {
        this.setValueFromMouse(d);
        super.onDrag(d, d2, d3, d4);
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
    }

    @Override
    public void onRelease(double d, double d2) {
        super.playDownSound(Minecraft.getInstance().getSoundManager());
    }

    protected abstract void updateMessage();

    protected abstract void applyValue();
}

