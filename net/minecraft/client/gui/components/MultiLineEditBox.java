/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.components;

import java.util.function.Consumer;
import net.minecraft.Util;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractTextAreaWidget;
import net.minecraft.client.gui.components.MultilineTextField;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringUtil;

public class MultiLineEditBox
extends AbstractTextAreaWidget {
    private static final int CURSOR_INSERT_WIDTH = 1;
    private static final int CURSOR_COLOR = -3092272;
    private static final String CURSOR_APPEND_CHARACTER = "_";
    private static final int TEXT_COLOR = -2039584;
    private static final int PLACEHOLDER_TEXT_COLOR = -857677600;
    private static final int CURSOR_BLINK_INTERVAL_MS = 300;
    private final Font font;
    private final Component placeholder;
    private final MultilineTextField textField;
    private final int textColor;
    private final boolean textShadow;
    private final int cursorColor;
    private long focusedTime = Util.getMillis();

    MultiLineEditBox(Font font, int n, int n2, int n3, int n4, Component component, Component component2, int n5, boolean bl, int n6, boolean bl2, boolean bl3) {
        super(n, n2, n3, n4, component2, bl2, bl3);
        this.font = font;
        this.textShadow = bl;
        this.textColor = n5;
        this.cursorColor = n6;
        this.placeholder = component;
        this.textField = new MultilineTextField(font, n3 - this.totalInnerPadding());
        this.textField.setCursorListener(this::scrollToCursor);
    }

    public void setCharacterLimit(int n) {
        this.textField.setCharacterLimit(n);
    }

    public void setLineLimit(int n) {
        this.textField.setLineLimit(n);
    }

    public void setValueListener(Consumer<String> consumer) {
        this.textField.setValueListener(consumer);
    }

    public void setValue(String string) {
        this.setValue(string, false);
    }

    public void setValue(String string, boolean bl) {
        this.textField.setValue(string, bl);
    }

    public String getValue() {
        return this.textField.value();
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, (Component)Component.translatable("gui.narrate.editBox", this.getMessage(), this.getValue()));
    }

    @Override
    public void onClick(double d, double d2) {
        this.textField.setSelecting(Screen.hasShiftDown());
        this.seekCursorScreen(d, d2);
    }

    @Override
    protected void onDrag(double d, double d2, double d3, double d4) {
        this.textField.setSelecting(true);
        this.seekCursorScreen(d, d2);
        this.textField.setSelecting(Screen.hasShiftDown());
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        return this.textField.keyPressed(n);
    }

    @Override
    public boolean charTyped(char c, int n) {
        if (!(this.visible && this.isFocused() && StringUtil.isAllowedChatCharacter(c))) {
            return false;
        }
        this.textField.insertText(Character.toString(c));
        return true;
    }

    @Override
    protected void renderContents(GuiGraphics guiGraphics, int n, int n2, float f) {
        String string = this.textField.value();
        if (string.isEmpty() && !this.isFocused()) {
            guiGraphics.drawWordWrap(this.font, this.placeholder, this.getInnerLeft(), this.getInnerTop(), this.width - this.totalInnerPadding(), -857677600);
            return;
        }
        int n3 = this.textField.cursor();
        boolean bl = this.isFocused() && (Util.getMillis() - this.focusedTime) / 300L % 2L == 0L;
        boolean bl2 = n3 < string.length();
        int n4 = 0;
        int n5 = 0;
        int n6 = this.getInnerTop();
        for (MultilineTextField.StringView stringView : this.textField.iterateLines()) {
            boolean bl3 = this.withinContentAreaTopBottom(n6, n6 + this.font.lineHeight);
            int n7 = this.getInnerLeft();
            if (bl && bl2 && n3 >= stringView.beginIndex() && n3 < stringView.endIndex()) {
                if (bl3) {
                    var16_19 = string.substring(stringView.beginIndex(), n3);
                    guiGraphics.drawString(this.font, var16_19, n7, n6, this.textColor, this.textShadow);
                    n4 = n7 + this.font.width(var16_19);
                    guiGraphics.fill(n4, n6 - 1, n4 + 1, n6 + 1 + this.font.lineHeight, this.cursorColor);
                    guiGraphics.drawString(this.font, string.substring(n3, stringView.endIndex()), n4, n6, this.textColor, this.textShadow);
                }
            } else {
                if (bl3) {
                    var16_19 = string.substring(stringView.beginIndex(), stringView.endIndex());
                    guiGraphics.drawString(this.font, var16_19, n7, n6, this.textColor, this.textShadow);
                    n4 = n7 + this.font.width(var16_19) - 1;
                }
                n5 = n6;
            }
            n6 += this.font.lineHeight;
        }
        if (bl && !bl2 && this.withinContentAreaTopBottom(n5, n5 + this.font.lineHeight)) {
            guiGraphics.drawString(this.font, CURSOR_APPEND_CHARACTER, n4, n5, this.cursorColor, this.textShadow);
        }
        if (this.textField.hasSelection()) {
            MultilineTextField.StringView stringView = this.textField.getSelected();
            int n8 = this.getInnerLeft();
            n6 = this.getInnerTop();
            for (MultilineTextField.StringView stringView2 : this.textField.iterateLines()) {
                if (stringView.beginIndex() > stringView2.endIndex()) {
                    n6 += this.font.lineHeight;
                    continue;
                }
                if (stringView2.beginIndex() > stringView.endIndex()) break;
                if (this.withinContentAreaTopBottom(n6, n6 + this.font.lineHeight)) {
                    int n9 = this.font.width(string.substring(stringView2.beginIndex(), Math.max(stringView.beginIndex(), stringView2.beginIndex())));
                    int n10 = stringView.endIndex() > stringView2.endIndex() ? this.width - this.innerPadding() : this.font.width(string.substring(stringView2.beginIndex(), stringView.endIndex()));
                    guiGraphics.textHighlight(n8 + n9, n6, n8 + n10, n6 + this.font.lineHeight);
                }
                n6 += this.font.lineHeight;
            }
        }
    }

    @Override
    protected void renderDecorations(GuiGraphics guiGraphics) {
        super.renderDecorations(guiGraphics);
        if (this.textField.hasCharacterLimit()) {
            int n = this.textField.characterLimit();
            MutableComponent mutableComponent = Component.translatable("gui.multiLineEditBox.character_limit", this.textField.value().length(), n);
            guiGraphics.drawString(this.font, mutableComponent, this.getX() + this.width - this.font.width(mutableComponent), this.getY() + this.height + 4, -6250336);
        }
    }

    @Override
    public int getInnerHeight() {
        return this.font.lineHeight * this.textField.getLineCount();
    }

    @Override
    protected double scrollRate() {
        return (double)this.font.lineHeight / 2.0;
    }

    private void scrollToCursor() {
        double d = this.scrollAmount();
        MultilineTextField.StringView stringView = this.textField.getLineView((int)(d / (double)this.font.lineHeight));
        if (this.textField.cursor() <= stringView.beginIndex()) {
            d = this.textField.getLineAtCursor() * this.font.lineHeight;
        } else {
            MultilineTextField.StringView stringView2 = this.textField.getLineView((int)((d + (double)this.height) / (double)this.font.lineHeight) - 1);
            if (this.textField.cursor() > stringView2.endIndex()) {
                d = this.textField.getLineAtCursor() * this.font.lineHeight - this.height + this.font.lineHeight + this.totalInnerPadding();
            }
        }
        this.setScrollAmount(d);
    }

    private void seekCursorScreen(double d, double d2) {
        double d3 = d - (double)this.getX() - (double)this.innerPadding();
        double d4 = d2 - (double)this.getY() - (double)this.innerPadding() + this.scrollAmount();
        this.textField.seekCursorToPoint(d3, d4);
    }

    @Override
    public void setFocused(boolean bl) {
        super.setFocused(bl);
        if (bl) {
            this.focusedTime = Util.getMillis();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int x;
        private int y;
        private Component placeholder = CommonComponents.EMPTY;
        private int textColor = -2039584;
        private boolean textShadow = true;
        private int cursorColor = -3092272;
        private boolean showBackground = true;
        private boolean showDecorations = true;

        public Builder setX(int n) {
            this.x = n;
            return this;
        }

        public Builder setY(int n) {
            this.y = n;
            return this;
        }

        public Builder setPlaceholder(Component component) {
            this.placeholder = component;
            return this;
        }

        public Builder setTextColor(int n) {
            this.textColor = n;
            return this;
        }

        public Builder setTextShadow(boolean bl) {
            this.textShadow = bl;
            return this;
        }

        public Builder setCursorColor(int n) {
            this.cursorColor = n;
            return this;
        }

        public Builder setShowBackground(boolean bl) {
            this.showBackground = bl;
            return this;
        }

        public Builder setShowDecorations(boolean bl) {
            this.showDecorations = bl;
            return this;
        }

        public MultiLineEditBox build(Font font, int n, int n2, Component component) {
            return new MultiLineEditBox(font, this.x, this.y, n, n2, this.placeholder, component, this.textColor, this.textShadow, this.cursorColor, this.showBackground, this.showDecorations);
        }
    }
}

