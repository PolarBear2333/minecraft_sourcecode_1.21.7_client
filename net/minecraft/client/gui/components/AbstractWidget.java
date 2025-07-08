/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.components;

import java.time.Duration;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetTooltipHolder;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

public abstract class AbstractWidget
implements Renderable,
GuiEventListener,
LayoutElement,
NarratableEntry {
    private static final double PERIOD_PER_SCROLLED_PIXEL = 0.5;
    private static final double MIN_SCROLL_PERIOD = 3.0;
    protected int width;
    protected int height;
    private int x;
    private int y;
    private Component message;
    protected boolean isHovered;
    public boolean active = true;
    public boolean visible = true;
    protected float alpha = 1.0f;
    private int tabOrderGroup;
    private boolean focused;
    private final WidgetTooltipHolder tooltip = new WidgetTooltipHolder();

    public AbstractWidget(int n, int n2, int n3, int n4, Component component) {
        this.x = n;
        this.y = n2;
        this.width = n3;
        this.height = n4;
        this.message = component;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public final void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        if (!this.visible) {
            return;
        }
        this.isHovered = guiGraphics.containsPointInScissor(n, n2) && this.areCoordinatesInRectangle(n, n2);
        this.renderWidget(guiGraphics, n, n2, f);
        this.tooltip.refreshTooltipForNextRenderPass(guiGraphics, n, n2, this.isHovered(), this.isFocused(), this.getRectangle());
    }

    public void setTooltip(@Nullable Tooltip tooltip) {
        this.tooltip.set(tooltip);
    }

    public void setTooltipDelay(Duration duration) {
        this.tooltip.setDelay(duration);
    }

    protected MutableComponent createNarrationMessage() {
        return AbstractWidget.wrapDefaultNarrationMessage(this.getMessage());
    }

    public static MutableComponent wrapDefaultNarrationMessage(Component component) {
        return Component.translatable("gui.narrate.button", component);
    }

    protected abstract void renderWidget(GuiGraphics var1, int var2, int var3, float var4);

    protected static void renderScrollingString(GuiGraphics guiGraphics, Font font, Component component, int n, int n2, int n3, int n4, int n5) {
        AbstractWidget.renderScrollingString(guiGraphics, font, component, (n + n3) / 2, n, n2, n3, n4, n5);
    }

    protected static void renderScrollingString(GuiGraphics guiGraphics, Font font, Component component, int n, int n2, int n3, int n4, int n5, int n6) {
        int n7 = font.width(component);
        int n8 = (n3 + n5 - font.lineHeight) / 2 + 1;
        int n9 = n4 - n2;
        if (n7 > n9) {
            int n10 = n7 - n9;
            double d = (double)Util.getMillis() / 1000.0;
            double d2 = Math.max((double)n10 * 0.5, 3.0);
            double d3 = Math.sin(1.5707963267948966 * Math.cos(Math.PI * 2 * d / d2)) / 2.0 + 0.5;
            double d4 = Mth.lerp(d3, 0.0, (double)n10);
            guiGraphics.enableScissor(n2, n3, n4, n5);
            guiGraphics.drawString(font, component, n2 - (int)d4, n8, n6);
            guiGraphics.disableScissor();
        } else {
            int n11 = Mth.clamp(n, n2 + n7 / 2, n4 - n7 / 2);
            guiGraphics.drawCenteredString(font, component, n11, n8, n6);
        }
    }

    protected void renderScrollingString(GuiGraphics guiGraphics, Font font, int n, int n2) {
        int n3 = this.getX() + n;
        int n4 = this.getX() + this.getWidth() - n;
        AbstractWidget.renderScrollingString(guiGraphics, font, this.getMessage(), n3, this.getY(), n4, this.getY() + this.getHeight(), n2);
    }

    public void onClick(double d, double d2) {
    }

    public void onRelease(double d, double d2) {
    }

    protected void onDrag(double d, double d2, double d3, double d4) {
    }

    @Override
    public boolean mouseClicked(double d, double d2, int n) {
        boolean bl;
        if (!this.active || !this.visible) {
            return false;
        }
        if (this.isValidClickButton(n) && (bl = this.isMouseOver(d, d2))) {
            this.playDownSound(Minecraft.getInstance().getSoundManager());
            this.onClick(d, d2);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double d, double d2, int n) {
        if (this.isValidClickButton(n)) {
            this.onRelease(d, d2);
            return true;
        }
        return false;
    }

    protected boolean isValidClickButton(int n) {
        return n == 0;
    }

    @Override
    public boolean mouseDragged(double d, double d2, int n, double d3, double d4) {
        if (this.isValidClickButton(n)) {
            this.onDrag(d, d2, d3, d4);
            return true;
        }
        return false;
    }

    @Override
    @Nullable
    public ComponentPath nextFocusPath(FocusNavigationEvent focusNavigationEvent) {
        if (!this.active || !this.visible) {
            return null;
        }
        if (!this.isFocused()) {
            return ComponentPath.leaf(this);
        }
        return null;
    }

    @Override
    public boolean isMouseOver(double d, double d2) {
        return this.active && this.visible && this.areCoordinatesInRectangle(d, d2);
    }

    public void playDownSound(SoundManager soundManager) {
        AbstractWidget.playButtonClickSound(soundManager);
    }

    public static void playButtonClickSound(SoundManager soundManager) {
        soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    public void setWidth(int n) {
        this.width = n;
    }

    public void setHeight(int n) {
        this.height = n;
    }

    public void setAlpha(float f) {
        this.alpha = f;
    }

    public void setMessage(Component component) {
        this.message = component;
    }

    public Component getMessage() {
        return this.message;
    }

    @Override
    public boolean isFocused() {
        return this.focused;
    }

    public boolean isHovered() {
        return this.isHovered;
    }

    public boolean isHoveredOrFocused() {
        return this.isHovered() || this.isFocused();
    }

    @Override
    public boolean isActive() {
        return this.visible && this.active;
    }

    @Override
    public void setFocused(boolean bl) {
        this.focused = bl;
    }

    @Override
    public NarratableEntry.NarrationPriority narrationPriority() {
        if (this.isFocused()) {
            return NarratableEntry.NarrationPriority.FOCUSED;
        }
        if (this.isHovered) {
            return NarratableEntry.NarrationPriority.HOVERED;
        }
        return NarratableEntry.NarrationPriority.NONE;
    }

    @Override
    public final void updateNarration(NarrationElementOutput narrationElementOutput) {
        this.updateWidgetNarration(narrationElementOutput);
        this.tooltip.updateNarration(narrationElementOutput);
    }

    protected abstract void updateWidgetNarration(NarrationElementOutput var1);

    protected void defaultButtonNarrationText(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, (Component)this.createNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                narrationElementOutput.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.button.usage.focused"));
            } else {
                narrationElementOutput.add(NarratedElementType.USAGE, (Component)Component.translatable("narration.button.usage.hovered"));
            }
        }
    }

    @Override
    public int getX() {
        return this.x;
    }

    @Override
    public void setX(int n) {
        this.x = n;
    }

    @Override
    public int getY() {
        return this.y;
    }

    @Override
    public void setY(int n) {
        this.y = n;
    }

    public int getRight() {
        return this.getX() + this.getWidth();
    }

    public int getBottom() {
        return this.getY() + this.getHeight();
    }

    @Override
    public void visitWidgets(Consumer<AbstractWidget> consumer) {
        consumer.accept(this);
    }

    public void setSize(int n, int n2) {
        this.width = n;
        this.height = n2;
    }

    @Override
    public ScreenRectangle getRectangle() {
        return LayoutElement.super.getRectangle();
    }

    private boolean areCoordinatesInRectangle(double d, double d2) {
        return d >= (double)this.getX() && d2 >= (double)this.getY() && d < (double)this.getRight() && d2 < (double)this.getBottom();
    }

    public void setRectangle(int n, int n2, int n3, int n4) {
        this.setSize(n, n2);
        this.setPosition(n3, n4);
    }

    @Override
    public int getTabOrderGroup() {
        return this.tabOrderGroup;
    }

    public void setTabOrderGroup(int n) {
        this.tabOrderGroup = n;
    }
}

