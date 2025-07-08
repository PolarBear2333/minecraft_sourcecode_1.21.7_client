/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.components;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public interface MultiLineLabel {
    public static final MultiLineLabel EMPTY = new MultiLineLabel(){

        @Override
        public void renderCentered(GuiGraphics guiGraphics, int n, int n2) {
        }

        @Override
        public void renderCentered(GuiGraphics guiGraphics, int n, int n2, int n3, int n4) {
        }

        @Override
        public void renderLeftAligned(GuiGraphics guiGraphics, int n, int n2, int n3, int n4) {
        }

        @Override
        public int renderLeftAlignedNoShadow(GuiGraphics guiGraphics, int n, int n2, int n3, int n4) {
            return n2;
        }

        @Override
        @Nullable
        public Style getStyleAtCentered(int n, int n2, int n3, double d, double d2) {
            return null;
        }

        @Override
        @Nullable
        public Style getStyleAtLeftAligned(int n, int n2, int n3, double d, double d2) {
            return null;
        }

        @Override
        public int getLineCount() {
            return 0;
        }

        @Override
        public int getWidth() {
            return 0;
        }
    };

    public static MultiLineLabel create(Font font, Component ... componentArray) {
        return MultiLineLabel.create(font, Integer.MAX_VALUE, Integer.MAX_VALUE, componentArray);
    }

    public static MultiLineLabel create(Font font, int n, Component ... componentArray) {
        return MultiLineLabel.create(font, n, Integer.MAX_VALUE, componentArray);
    }

    public static MultiLineLabel create(Font font, Component component, int n) {
        return MultiLineLabel.create(font, n, Integer.MAX_VALUE, component);
    }

    public static MultiLineLabel create(final Font font, final int n, final int n2, final Component ... componentArray) {
        if (componentArray.length == 0) {
            return EMPTY;
        }
        return new MultiLineLabel(){
            @Nullable
            private List<TextAndWidth> cachedTextAndWidth;
            @Nullable
            private Language splitWithLanguage;

            @Override
            public void renderCentered(GuiGraphics guiGraphics, int n3, int n22) {
                this.renderCentered(guiGraphics, n3, n22, font.lineHeight, -1);
            }

            @Override
            public void renderCentered(GuiGraphics guiGraphics, int n6, int n22, int n3, int n4) {
                int n5 = n22;
                for (TextAndWidth textAndWidth : this.getSplitMessage()) {
                    guiGraphics.drawString(font, textAndWidth.text, n6 - textAndWidth.width / 2, n5, n4);
                    n5 += n3;
                }
            }

            @Override
            public void renderLeftAligned(GuiGraphics guiGraphics, int n6, int n22, int n3, int n4) {
                int n5 = n22;
                for (TextAndWidth textAndWidth : this.getSplitMessage()) {
                    guiGraphics.drawString(font, textAndWidth.text, n6, n5, n4);
                    n5 += n3;
                }
            }

            @Override
            public int renderLeftAlignedNoShadow(GuiGraphics guiGraphics, int n6, int n22, int n3, int n4) {
                int n5 = n22;
                for (TextAndWidth textAndWidth : this.getSplitMessage()) {
                    guiGraphics.drawString(font, textAndWidth.text, n6, n5, n4, false);
                    n5 += n3;
                }
                return n5;
            }

            @Override
            @Nullable
            public Style getStyleAtCentered(int n7, int n22, int n3, double d, double d2) {
                List<TextAndWidth> list = this.getSplitMessage();
                int n4 = Mth.floor((d2 - (double)n22) / (double)n3);
                if (n4 < 0 || n4 >= list.size()) {
                    return null;
                }
                TextAndWidth textAndWidth = list.get(n4);
                int n5 = n7 - textAndWidth.width / 2;
                if (d < (double)n5) {
                    return null;
                }
                int n6 = Mth.floor(d - (double)n5);
                return font.getSplitter().componentStyleAtWidth(textAndWidth.text, n6);
            }

            @Override
            @Nullable
            public Style getStyleAtLeftAligned(int n6, int n22, int n3, double d, double d2) {
                if (d < (double)n6) {
                    return null;
                }
                List<TextAndWidth> list = this.getSplitMessage();
                int n4 = Mth.floor((d2 - (double)n22) / (double)n3);
                if (n4 < 0 || n4 >= list.size()) {
                    return null;
                }
                TextAndWidth textAndWidth = list.get(n4);
                int n5 = Mth.floor(d - (double)n6);
                return font.getSplitter().componentStyleAtWidth(textAndWidth.text, n5);
            }

            private List<TextAndWidth> getSplitMessage() {
                FormattedText formattedText;
                int n4;
                Language language = Language.getInstance();
                if (this.cachedTextAndWidth != null && language == this.splitWithLanguage) {
                    return this.cachedTextAndWidth;
                }
                this.splitWithLanguage = language;
                ArrayList<FormattedText> arrayList = new ArrayList<FormattedText>();
                Component[] componentArray2 = componentArray;
                int n22 = componentArray2.length;
                for (n4 = 0; n4 < n22; ++n4) {
                    formattedText = componentArray2[n4];
                    arrayList.addAll(font.splitIgnoringLanguage(formattedText, n));
                }
                this.cachedTextAndWidth = new ArrayList<TextAndWidth>();
                int n3 = Math.min(arrayList.size(), n2);
                List list = arrayList.subList(0, n3);
                for (n4 = 0; n4 < list.size(); ++n4) {
                    formattedText = (FormattedText)list.get(n4);
                    FormattedCharSequence formattedCharSequence = Language.getInstance().getVisualOrder(formattedText);
                    if (n4 == list.size() - 1 && n3 == n2 && n3 != arrayList.size()) {
                        FormattedText formattedText2 = font.substrByWidth(formattedText, font.width(formattedText) - font.width(CommonComponents.ELLIPSIS));
                        FormattedText formattedText3 = FormattedText.composite(formattedText2, CommonComponents.ELLIPSIS);
                        this.cachedTextAndWidth.add(new TextAndWidth(Language.getInstance().getVisualOrder(formattedText3), font.width(formattedText3)));
                        continue;
                    }
                    this.cachedTextAndWidth.add(new TextAndWidth(formattedCharSequence, font.width(formattedCharSequence)));
                }
                return this.cachedTextAndWidth;
            }

            @Override
            public int getLineCount() {
                return this.getSplitMessage().size();
            }

            @Override
            public int getWidth() {
                return Math.min(n, this.getSplitMessage().stream().mapToInt(TextAndWidth::width).max().orElse(0));
            }
        };
    }

    public void renderCentered(GuiGraphics var1, int var2, int var3);

    public void renderCentered(GuiGraphics var1, int var2, int var3, int var4, int var5);

    public void renderLeftAligned(GuiGraphics var1, int var2, int var3, int var4, int var5);

    public int renderLeftAlignedNoShadow(GuiGraphics var1, int var2, int var3, int var4, int var5);

    @Nullable
    public Style getStyleAtCentered(int var1, int var2, int var3, double var4, double var6);

    @Nullable
    public Style getStyleAtLeftAligned(int var1, int var2, int var3, double var4, double var6);

    public int getLineCount();

    public int getWidth();

    public static final class TextAndWidth
    extends Record {
        final FormattedCharSequence text;
        final int width;

        public TextAndWidth(FormattedCharSequence formattedCharSequence, int n) {
            this.text = formattedCharSequence;
            this.width = n;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{TextAndWidth.class, "text;width", "text", "width"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{TextAndWidth.class, "text;width", "text", "width"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{TextAndWidth.class, "text;width", "text", "width"}, this, object);
        }

        public FormattedCharSequence text() {
            return this.text;
        }

        public int width() {
            return this.width;
        }
    }
}

