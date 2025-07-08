/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.client.gui.components;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Whence;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.util.StringUtil;
import org.slf4j.Logger;

public class MultilineTextField {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int NO_LIMIT = Integer.MAX_VALUE;
    private static final int LINE_SEEK_PIXEL_BIAS = 2;
    private final Font font;
    private final List<StringView> displayLines = Lists.newArrayList();
    private String value;
    private int cursor;
    private int selectCursor;
    private boolean selecting;
    private int characterLimit = Integer.MAX_VALUE;
    private int lineLimit = Integer.MAX_VALUE;
    private final int width;
    private Consumer<String> valueListener = string -> {};
    private Runnable cursorListener = () -> {};

    public MultilineTextField(Font font, int n) {
        this.font = font;
        this.width = n;
        this.setValue("");
    }

    public int characterLimit() {
        return this.characterLimit;
    }

    public void setCharacterLimit(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Character limit cannot be negative");
        }
        this.characterLimit = n;
    }

    public void setLineLimit(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("Character limit cannot be negative");
        }
        this.lineLimit = n;
    }

    public boolean hasCharacterLimit() {
        return this.characterLimit != Integer.MAX_VALUE;
    }

    public boolean hasLineLimit() {
        return this.lineLimit != Integer.MAX_VALUE;
    }

    public void setValueListener(Consumer<String> consumer) {
        this.valueListener = consumer;
    }

    public void setCursorListener(Runnable runnable) {
        this.cursorListener = runnable;
    }

    public void setValue(String string) {
        this.setValue(string, false);
    }

    public void setValue(String string, boolean bl) {
        String string2 = this.truncateFullText(string);
        if (!bl && this.overflowsLineLimit(string2)) {
            return;
        }
        this.value = string2;
        this.selectCursor = this.cursor = this.value.length();
        this.onValueChange();
    }

    public String value() {
        return this.value;
    }

    public void insertText(String string) {
        if (string.isEmpty() && !this.hasSelection()) {
            return;
        }
        String string2 = this.truncateInsertionText(StringUtil.filterText(string, true));
        StringView stringView = this.getSelected();
        String string3 = new StringBuilder(this.value).replace(stringView.beginIndex, stringView.endIndex, string2).toString();
        if (this.overflowsLineLimit(string3)) {
            return;
        }
        this.value = string3;
        this.selectCursor = this.cursor = stringView.beginIndex + string2.length();
        this.onValueChange();
    }

    public void deleteText(int n) {
        if (!this.hasSelection()) {
            this.selectCursor = Mth.clamp(this.cursor + n, 0, this.value.length());
        }
        this.insertText("");
    }

    public int cursor() {
        return this.cursor;
    }

    public void setSelecting(boolean bl) {
        this.selecting = bl;
    }

    public StringView getSelected() {
        return new StringView(Math.min(this.selectCursor, this.cursor), Math.max(this.selectCursor, this.cursor));
    }

    public int getLineCount() {
        return this.displayLines.size();
    }

    public int getLineAtCursor() {
        for (int i = 0; i < this.displayLines.size(); ++i) {
            StringView stringView = this.displayLines.get(i);
            if (this.cursor < stringView.beginIndex || this.cursor > stringView.endIndex) continue;
            return i;
        }
        return -1;
    }

    public StringView getLineView(int n) {
        return this.displayLines.get(Mth.clamp(n, 0, this.displayLines.size() - 1));
    }

    public void seekCursor(Whence whence, int n) {
        switch (whence) {
            case ABSOLUTE: {
                this.cursor = n;
                break;
            }
            case RELATIVE: {
                this.cursor += n;
                break;
            }
            case END: {
                this.cursor = this.value.length() + n;
            }
        }
        this.cursor = Mth.clamp(this.cursor, 0, this.value.length());
        this.cursorListener.run();
        if (!this.selecting) {
            this.selectCursor = this.cursor;
        }
    }

    public void seekCursorLine(int n) {
        if (n == 0) {
            return;
        }
        int n2 = this.font.width(this.value.substring(this.getCursorLineView().beginIndex, this.cursor)) + 2;
        StringView stringView = this.getCursorLineView(n);
        int n3 = this.font.plainSubstrByWidth(this.value.substring(stringView.beginIndex, stringView.endIndex), n2).length();
        this.seekCursor(Whence.ABSOLUTE, stringView.beginIndex + n3);
    }

    public void seekCursorToPoint(double d, double d2) {
        int n = Mth.floor(d);
        int n2 = Mth.floor(d2 / (double)this.font.lineHeight);
        StringView stringView = this.displayLines.get(Mth.clamp(n2, 0, this.displayLines.size() - 1));
        int n3 = this.font.plainSubstrByWidth(this.value.substring(stringView.beginIndex, stringView.endIndex), n).length();
        this.seekCursor(Whence.ABSOLUTE, stringView.beginIndex + n3);
    }

    public boolean keyPressed(int n) {
        this.selecting = Screen.hasShiftDown();
        if (Screen.isSelectAll(n)) {
            this.cursor = this.value.length();
            this.selectCursor = 0;
            return true;
        }
        if (Screen.isCopy(n)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.getSelectedText());
            return true;
        }
        if (Screen.isPaste(n)) {
            this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
            return true;
        }
        if (Screen.isCut(n)) {
            Minecraft.getInstance().keyboardHandler.setClipboard(this.getSelectedText());
            this.insertText("");
            return true;
        }
        switch (n) {
            case 263: {
                if (Screen.hasControlDown()) {
                    StringView stringView = this.getPreviousWord();
                    this.seekCursor(Whence.ABSOLUTE, stringView.beginIndex);
                } else {
                    this.seekCursor(Whence.RELATIVE, -1);
                }
                return true;
            }
            case 262: {
                if (Screen.hasControlDown()) {
                    StringView stringView = this.getNextWord();
                    this.seekCursor(Whence.ABSOLUTE, stringView.beginIndex);
                } else {
                    this.seekCursor(Whence.RELATIVE, 1);
                }
                return true;
            }
            case 265: {
                if (!Screen.hasControlDown()) {
                    this.seekCursorLine(-1);
                }
                return true;
            }
            case 264: {
                if (!Screen.hasControlDown()) {
                    this.seekCursorLine(1);
                }
                return true;
            }
            case 266: {
                this.seekCursor(Whence.ABSOLUTE, 0);
                return true;
            }
            case 267: {
                this.seekCursor(Whence.END, 0);
                return true;
            }
            case 268: {
                if (Screen.hasControlDown()) {
                    this.seekCursor(Whence.ABSOLUTE, 0);
                } else {
                    this.seekCursor(Whence.ABSOLUTE, this.getCursorLineView().beginIndex);
                }
                return true;
            }
            case 269: {
                if (Screen.hasControlDown()) {
                    this.seekCursor(Whence.END, 0);
                } else {
                    this.seekCursor(Whence.ABSOLUTE, this.getCursorLineView().endIndex);
                }
                return true;
            }
            case 259: {
                if (Screen.hasControlDown()) {
                    StringView stringView = this.getPreviousWord();
                    this.deleteText(stringView.beginIndex - this.cursor);
                } else {
                    this.deleteText(-1);
                }
                return true;
            }
            case 261: {
                if (Screen.hasControlDown()) {
                    StringView stringView = this.getNextWord();
                    this.deleteText(stringView.beginIndex - this.cursor);
                } else {
                    this.deleteText(1);
                }
                return true;
            }
            case 257: 
            case 335: {
                this.insertText("\n");
                return true;
            }
        }
        return false;
    }

    public Iterable<StringView> iterateLines() {
        return this.displayLines;
    }

    public boolean hasSelection() {
        return this.selectCursor != this.cursor;
    }

    @VisibleForTesting
    public String getSelectedText() {
        StringView stringView = this.getSelected();
        return this.value.substring(stringView.beginIndex, stringView.endIndex);
    }

    private StringView getCursorLineView() {
        return this.getCursorLineView(0);
    }

    private StringView getCursorLineView(int n) {
        int n2 = this.getLineAtCursor();
        if (n2 < 0) {
            LOGGER.error("Cursor is not within text (cursor = {}, length = {})", (Object)this.cursor, (Object)this.value.length());
            return this.displayLines.getLast();
        }
        return this.displayLines.get(Mth.clamp(n2 + n, 0, this.displayLines.size() - 1));
    }

    @VisibleForTesting
    public StringView getPreviousWord() {
        int n;
        if (this.value.isEmpty()) {
            return StringView.EMPTY;
        }
        for (n = Mth.clamp(this.cursor, 0, this.value.length() - 1); n > 0 && Character.isWhitespace(this.value.charAt(n - 1)); --n) {
        }
        while (n > 0 && !Character.isWhitespace(this.value.charAt(n - 1))) {
            --n;
        }
        return new StringView(n, this.getWordEndPosition(n));
    }

    @VisibleForTesting
    public StringView getNextWord() {
        int n;
        if (this.value.isEmpty()) {
            return StringView.EMPTY;
        }
        for (n = Mth.clamp(this.cursor, 0, this.value.length() - 1); n < this.value.length() && !Character.isWhitespace(this.value.charAt(n)); ++n) {
        }
        while (n < this.value.length() && Character.isWhitespace(this.value.charAt(n))) {
            ++n;
        }
        return new StringView(n, this.getWordEndPosition(n));
    }

    private int getWordEndPosition(int n) {
        int n2;
        for (n2 = n; n2 < this.value.length() && !Character.isWhitespace(this.value.charAt(n2)); ++n2) {
        }
        return n2;
    }

    private void onValueChange() {
        this.reflowDisplayLines();
        this.valueListener.accept(this.value);
        this.cursorListener.run();
    }

    private void reflowDisplayLines() {
        this.displayLines.clear();
        if (this.value.isEmpty()) {
            this.displayLines.add(StringView.EMPTY);
            return;
        }
        this.font.getSplitter().splitLines(this.value, this.width, Style.EMPTY, false, (style, n, n2) -> this.displayLines.add(new StringView(n, n2)));
        if (this.value.charAt(this.value.length() - 1) == '\n') {
            this.displayLines.add(new StringView(this.value.length(), this.value.length()));
        }
    }

    private String truncateFullText(String string) {
        if (this.hasCharacterLimit()) {
            return StringUtil.truncateStringIfNecessary(string, this.characterLimit, false);
        }
        return string;
    }

    private String truncateInsertionText(String string) {
        String string2 = string;
        if (this.hasCharacterLimit()) {
            int n = this.characterLimit - this.value.length();
            string2 = StringUtil.truncateStringIfNecessary(string, n, false);
        }
        return string2;
    }

    private boolean overflowsLineLimit(String string) {
        return this.hasLineLimit() && this.font.getSplitter().splitLines(string, this.width, Style.EMPTY).size() + (StringUtil.endsWithNewLine(string) ? 1 : 0) > this.lineLimit;
    }

    protected static final class StringView
    extends Record {
        final int beginIndex;
        final int endIndex;
        static final StringView EMPTY = new StringView(0, 0);

        protected StringView(int n, int n2) {
            this.beginIndex = n;
            this.endIndex = n2;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{StringView.class, "beginIndex;endIndex", "beginIndex", "endIndex"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{StringView.class, "beginIndex;endIndex", "beginIndex", "endIndex"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{StringView.class, "beginIndex;endIndex", "beginIndex", "endIndex"}, this, object);
        }

        public int beginIndex() {
            return this.beginIndex;
        }

        public int endIndex() {
            return this.endIndex;
        }
    }
}

