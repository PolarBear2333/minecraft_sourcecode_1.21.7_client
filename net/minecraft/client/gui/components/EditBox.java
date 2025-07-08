/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.components;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;

public class EditBox
extends AbstractWidget {
    private static final WidgetSprites SPRITES = new WidgetSprites(ResourceLocation.withDefaultNamespace("widget/text_field"), ResourceLocation.withDefaultNamespace("widget/text_field_highlighted"));
    public static final int BACKWARDS = -1;
    public static final int FORWARDS = 1;
    private static final int CURSOR_INSERT_WIDTH = 1;
    private static final int CURSOR_INSERT_COLOR = -3092272;
    private static final String CURSOR_APPEND_CHARACTER = "_";
    public static final int DEFAULT_TEXT_COLOR = -2039584;
    private static final int CURSOR_BLINK_INTERVAL_MS = 300;
    private final Font font;
    private String value = "";
    private int maxLength = 32;
    private boolean bordered = true;
    private boolean canLoseFocus = true;
    private boolean isEditable = true;
    private boolean centered = false;
    private boolean textShadow = true;
    private int displayPos;
    private int cursorPos;
    private int highlightPos;
    private int textColor = -2039584;
    private int textColorUneditable = -9408400;
    @Nullable
    private String suggestion;
    @Nullable
    private Consumer<String> responder;
    private Predicate<String> filter = Objects::nonNull;
    private BiFunction<String, Integer, FormattedCharSequence> formatter = (string, n) -> FormattedCharSequence.forward(string, Style.EMPTY);
    @Nullable
    private Component hint;
    private long focusedTime = Util.getMillis();
    private int textX;
    private int textY;

    public EditBox(Font font, int n, int n2, Component component) {
        this(font, 0, 0, n, n2, component);
    }

    public EditBox(Font font, int n, int n2, int n3, int n4, Component component) {
        this(font, n, n2, n3, n4, null, component);
    }

    public EditBox(Font font, int n2, int n3, int n4, int n5, @Nullable EditBox editBox, Component component) {
        super(n2, n3, n4, n5, component);
        this.font = font;
        if (editBox != null) {
            this.setValue(editBox.getValue());
        }
        this.updateTextPosition();
    }

    public void setResponder(Consumer<String> consumer) {
        this.responder = consumer;
    }

    public void setFormatter(BiFunction<String, Integer, FormattedCharSequence> biFunction) {
        this.formatter = biFunction;
    }

    @Override
    protected MutableComponent createNarrationMessage() {
        Component component = this.getMessage();
        return Component.translatable("gui.narrate.editBox", component, this.value);
    }

    public void setValue(String string) {
        if (!this.filter.test(string)) {
            return;
        }
        this.value = string.length() > this.maxLength ? string.substring(0, this.maxLength) : string;
        this.moveCursorToEnd(false);
        this.setHighlightPos(this.cursorPos);
        this.onValueChange(string);
    }

    public String getValue() {
        return this.value;
    }

    public String getHighlighted() {
        int n = Math.min(this.cursorPos, this.highlightPos);
        int n2 = Math.max(this.cursorPos, this.highlightPos);
        return this.value.substring(n, n2);
    }

    @Override
    public void setX(int n) {
        super.setX(n);
        this.updateTextPosition();
    }

    @Override
    public void setY(int n) {
        super.setY(n);
        this.updateTextPosition();
    }

    public void setFilter(Predicate<String> predicate) {
        this.filter = predicate;
    }

    public void insertText(String string) {
        String string2;
        int n = Math.min(this.cursorPos, this.highlightPos);
        int n2 = Math.max(this.cursorPos, this.highlightPos);
        int n3 = this.maxLength - this.value.length() - (n - n2);
        if (n3 <= 0) {
            return;
        }
        String string3 = StringUtil.filterText(string);
        int n4 = string3.length();
        if (n3 < n4) {
            if (Character.isHighSurrogate(string3.charAt(n3 - 1))) {
                --n3;
            }
            string3 = string3.substring(0, n3);
            n4 = n3;
        }
        if (!this.filter.test(string2 = new StringBuilder(this.value).replace(n, n2, string3).toString())) {
            return;
        }
        this.value = string2;
        this.setCursorPosition(n + n4);
        this.setHighlightPos(this.cursorPos);
        this.onValueChange(this.value);
    }

    private void onValueChange(String string) {
        if (this.responder != null) {
            this.responder.accept(string);
        }
        this.updateTextPosition();
    }

    private void deleteText(int n) {
        if (Screen.hasControlDown()) {
            this.deleteWords(n);
        } else {
            this.deleteChars(n);
        }
    }

    public void deleteWords(int n) {
        if (this.value.isEmpty()) {
            return;
        }
        if (this.highlightPos != this.cursorPos) {
            this.insertText("");
            return;
        }
        this.deleteCharsToPos(this.getWordPosition(n));
    }

    public void deleteChars(int n) {
        this.deleteCharsToPos(this.getCursorPos(n));
    }

    public void deleteCharsToPos(int n) {
        int n2;
        if (this.value.isEmpty()) {
            return;
        }
        if (this.highlightPos != this.cursorPos) {
            this.insertText("");
            return;
        }
        int n3 = Math.min(n, this.cursorPos);
        if (n3 == (n2 = Math.max(n, this.cursorPos))) {
            return;
        }
        String string = new StringBuilder(this.value).delete(n3, n2).toString();
        if (!this.filter.test(string)) {
            return;
        }
        this.value = string;
        this.moveCursorTo(n3, false);
    }

    public int getWordPosition(int n) {
        return this.getWordPosition(n, this.getCursorPosition());
    }

    private int getWordPosition(int n, int n2) {
        return this.getWordPosition(n, n2, true);
    }

    private int getWordPosition(int n, int n2, boolean bl) {
        int n3 = n2;
        boolean bl2 = n < 0;
        int n4 = Math.abs(n);
        for (int i = 0; i < n4; ++i) {
            if (bl2) {
                while (bl && n3 > 0 && this.value.charAt(n3 - 1) == ' ') {
                    --n3;
                }
                while (n3 > 0 && this.value.charAt(n3 - 1) != ' ') {
                    --n3;
                }
                continue;
            }
            int n5 = this.value.length();
            if ((n3 = this.value.indexOf(32, n3)) == -1) {
                n3 = n5;
                continue;
            }
            while (bl && n3 < n5 && this.value.charAt(n3) == ' ') {
                ++n3;
            }
        }
        return n3;
    }

    public void moveCursor(int n, boolean bl) {
        this.moveCursorTo(this.getCursorPos(n), bl);
    }

    private int getCursorPos(int n) {
        return Util.offsetByCodepoints(this.value, this.cursorPos, n);
    }

    public void moveCursorTo(int n, boolean bl) {
        this.setCursorPosition(n);
        if (!bl) {
            this.setHighlightPos(this.cursorPos);
        }
        this.onValueChange(this.value);
    }

    public void setCursorPosition(int n) {
        this.cursorPos = Mth.clamp(n, 0, this.value.length());
        this.scrollTo(this.cursorPos);
    }

    public void moveCursorToStart(boolean bl) {
        this.moveCursorTo(0, bl);
    }

    public void moveCursorToEnd(boolean bl) {
        this.moveCursorTo(this.value.length(), bl);
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (!this.isActive() || !this.isFocused()) {
            return false;
        }
        switch (n) {
            case 263: {
                if (Screen.hasControlDown()) {
                    this.moveCursorTo(this.getWordPosition(-1), Screen.hasShiftDown());
                } else {
                    this.moveCursor(-1, Screen.hasShiftDown());
                }
                return true;
            }
            case 262: {
                if (Screen.hasControlDown()) {
                    this.moveCursorTo(this.getWordPosition(1), Screen.hasShiftDown());
                } else {
                    this.moveCursor(1, Screen.hasShiftDown());
                }
                return true;
            }
            case 259: {
                if (this.isEditable) {
                    this.deleteText(-1);
                }
                return true;
            }
            case 261: {
                if (this.isEditable) {
                    this.deleteText(1);
                }
                return true;
            }
            case 268: {
                this.moveCursorToStart(Screen.hasShiftDown());
                return true;
            }
            case 269: {
                this.moveCursorToEnd(Screen.hasShiftDown());
                return true;
            }
        }
        if (Screen.isSelectAll(n)) {
            this.moveCursorToEnd(false);
            this.setHighlightPos(0);
            return true;
        }
        if (Screen.isCopy(n)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
            return true;
        }
        if (Screen.isPaste(n)) {
            if (this.isEditable()) {
                this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
            }
            return true;
        }
        if (Screen.isCut(n)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
            if (this.isEditable()) {
                this.insertText("");
            }
            return true;
        }
        return false;
    }

    public boolean canConsumeInput() {
        return this.isActive() && this.isFocused() && this.isEditable();
    }

    @Override
    public boolean charTyped(char c, int n) {
        if (!this.canConsumeInput()) {
            return false;
        }
        if (StringUtil.isAllowedChatCharacter(c)) {
            if (this.isEditable) {
                this.insertText(Character.toString(c));
            }
            return true;
        }
        return false;
    }

    @Override
    public void onClick(double d, double d2) {
        int n = Mth.floor(d) - this.textX;
        String string = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
        this.moveCursorTo(this.font.plainSubstrByWidth(string, n).length() + this.displayPos, Screen.hasShiftDown());
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int n, int n2, float f) {
        if (!this.isVisible()) {
            return;
        }
        if (this.isBordered()) {
            ResourceLocation resourceLocation = SPRITES.get(this.isActive(), this.isFocused());
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        }
        int n3 = this.isEditable ? this.textColor : this.textColorUneditable;
        int n4 = this.cursorPos - this.displayPos;
        String string = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
        boolean bl = n4 >= 0 && n4 <= string.length();
        boolean bl2 = this.isFocused() && (Util.getMillis() - this.focusedTime) / 300L % 2L == 0L && bl;
        int n5 = this.textX;
        int n6 = Mth.clamp(this.highlightPos - this.displayPos, 0, string.length());
        if (!string.isEmpty()) {
            String string2 = bl ? string.substring(0, n4) : string;
            FormattedCharSequence formattedCharSequence = this.formatter.apply(string2, this.displayPos);
            guiGraphics.drawString(this.font, formattedCharSequence, n5, this.textY, n3, this.textShadow);
            n5 += this.font.width(formattedCharSequence) + 1;
        }
        boolean bl3 = this.cursorPos < this.value.length() || this.value.length() >= this.getMaxLength();
        int n7 = n5;
        if (!bl) {
            n7 = n4 > 0 ? this.textX + this.width : this.textX;
        } else if (bl3) {
            --n7;
            --n5;
        }
        if (!string.isEmpty() && bl && n4 < string.length()) {
            guiGraphics.drawString(this.font, this.formatter.apply(string.substring(n4), this.cursorPos), n5, this.textY, n3, this.textShadow);
        }
        if (this.hint != null && string.isEmpty() && !this.isFocused()) {
            guiGraphics.drawString(this.font, this.hint, n5, this.textY, n3);
        }
        if (!bl3 && this.suggestion != null) {
            guiGraphics.drawString(this.font, this.suggestion, n7 - 1, this.textY, -8355712, this.textShadow);
        }
        if (n6 != n4) {
            int n8 = this.textX + this.font.width(string.substring(0, n6));
            guiGraphics.textHighlight(Math.min(n7, this.getX() + this.width), this.textY - 1, Math.min(n8 - 1, this.getX() + this.width), this.textY + 1 + this.font.lineHeight);
        }
        if (bl2) {
            if (bl3) {
                guiGraphics.fill(n7, this.textY - 1, n7 + 1, this.textY + 1 + this.font.lineHeight, -3092272);
            } else {
                guiGraphics.drawString(this.font, CURSOR_APPEND_CHARACTER, n7, this.textY, n3, this.textShadow);
            }
        }
    }

    private void updateTextPosition() {
        if (this.font == null) {
            return;
        }
        String string = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
        this.textX = this.getX() + (this.isCentered() ? (this.getWidth() - this.font.width(string)) / 2 : (this.bordered ? 4 : 0));
        this.textY = this.bordered ? this.getY() + (this.height - 8) / 2 : this.getY();
    }

    public void setMaxLength(int n) {
        this.maxLength = n;
        if (this.value.length() > n) {
            this.value = this.value.substring(0, n);
            this.onValueChange(this.value);
        }
    }

    private int getMaxLength() {
        return this.maxLength;
    }

    public int getCursorPosition() {
        return this.cursorPos;
    }

    public boolean isBordered() {
        return this.bordered;
    }

    public void setBordered(boolean bl) {
        this.bordered = bl;
        this.updateTextPosition();
    }

    public void setTextColor(int n) {
        this.textColor = n;
    }

    public void setTextColorUneditable(int n) {
        this.textColorUneditable = n;
    }

    @Override
    public void setFocused(boolean bl) {
        if (!this.canLoseFocus && !bl) {
            return;
        }
        super.setFocused(bl);
        if (bl) {
            this.focusedTime = Util.getMillis();
        }
    }

    private boolean isEditable() {
        return this.isEditable;
    }

    public void setEditable(boolean bl) {
        this.isEditable = bl;
    }

    private boolean isCentered() {
        return this.centered;
    }

    public void setCentered(boolean bl) {
        this.centered = bl;
        this.updateTextPosition();
    }

    public void setTextShadow(boolean bl) {
        this.textShadow = bl;
    }

    public int getInnerWidth() {
        return this.isBordered() ? this.width - 8 : this.width;
    }

    public void setHighlightPos(int n) {
        this.highlightPos = Mth.clamp(n, 0, this.value.length());
        this.scrollTo(this.highlightPos);
    }

    private void scrollTo(int n) {
        if (this.font == null) {
            return;
        }
        this.displayPos = Math.min(this.displayPos, this.value.length());
        int n2 = this.getInnerWidth();
        String string = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), n2);
        int n3 = string.length() + this.displayPos;
        if (n == this.displayPos) {
            this.displayPos -= this.font.plainSubstrByWidth(this.value, n2, true).length();
        }
        if (n > n3) {
            this.displayPos += n - n3;
        } else if (n <= this.displayPos) {
            this.displayPos -= this.displayPos - n;
        }
        this.displayPos = Mth.clamp(this.displayPos, 0, this.value.length());
    }

    public void setCanLoseFocus(boolean bl) {
        this.canLoseFocus = bl;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean bl) {
        this.visible = bl;
    }

    public void setSuggestion(@Nullable String string) {
        this.suggestion = string;
    }

    public int getScreenX(int n) {
        if (n > this.value.length()) {
            return this.getX();
        }
        return this.getX() + this.font.width(this.value.substring(0, n));
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, (Component)this.createNarrationMessage());
    }

    public void setHint(Component component) {
        this.hint = component;
    }
}

