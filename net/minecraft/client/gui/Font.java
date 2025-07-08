/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.ibm.icu.text.ArabicShaping
 *  com.ibm.icu.text.ArabicShapingException
 *  com.ibm.icu.text.Bidi
 *  javax.annotation.Nullable
 *  org.joml.Matrix4f
 */
package net.minecraft.client.gui;

import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringDecomposer;
import org.joml.Matrix4f;

public class Font {
    private static final float EFFECT_DEPTH = 0.01f;
    private static final float OVER_EFFECT_DEPTH = 0.01f;
    private static final float UNDER_EFFECT_DEPTH = -0.01f;
    public static final float SHADOW_DEPTH = 0.03f;
    public static final int NO_SHADOW = 0;
    public final int lineHeight = 9;
    public final RandomSource random = RandomSource.create();
    private final Function<ResourceLocation, FontSet> fonts;
    final boolean filterFishyGlyphs;
    private final StringSplitter splitter;

    public Font(Function<ResourceLocation, FontSet> function, boolean bl) {
        this.fonts = function;
        this.filterFishyGlyphs = bl;
        this.splitter = new StringSplitter((n, style) -> this.getFontSet(style.getFont()).getGlyphInfo(n, this.filterFishyGlyphs).getAdvance(style.isBold()));
    }

    FontSet getFontSet(ResourceLocation resourceLocation) {
        return this.fonts.apply(resourceLocation);
    }

    public String bidirectionalShaping(String string) {
        try {
            Bidi bidi = new Bidi(new ArabicShaping(8).shape(string), 127);
            bidi.setReorderingMode(0);
            return bidi.writeReordered(2);
        }
        catch (ArabicShapingException arabicShapingException) {
            return string;
        }
    }

    public void drawInBatch(String string, float f, float f2, int n, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, DisplayMode displayMode, int n2, int n3) {
        PreparedText preparedText = this.prepareText(string, f, f2, n, bl, n2);
        preparedText.visit(GlyphVisitor.forMultiBufferSource(multiBufferSource, matrix4f, displayMode, n3));
    }

    public void drawInBatch(Component component, float f, float f2, int n, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, DisplayMode displayMode, int n2, int n3) {
        PreparedText preparedText = this.prepareText(component.getVisualOrderText(), f, f2, n, bl, n2);
        preparedText.visit(GlyphVisitor.forMultiBufferSource(multiBufferSource, matrix4f, displayMode, n3));
    }

    public void drawInBatch(FormattedCharSequence formattedCharSequence, float f, float f2, int n, boolean bl, Matrix4f matrix4f, MultiBufferSource multiBufferSource, DisplayMode displayMode, int n2, int n3) {
        PreparedText preparedText = this.prepareText(formattedCharSequence, f, f2, n, bl, n2);
        preparedText.visit(GlyphVisitor.forMultiBufferSource(multiBufferSource, matrix4f, displayMode, n3));
    }

    public void drawInBatch8xOutline(FormattedCharSequence formattedCharSequence, float f, float f2, int n, int n2, Matrix4f matrix4f, MultiBufferSource multiBufferSource, int n3) {
        PreparedTextBuilder preparedTextBuilder = new PreparedTextBuilder(0.0f, 0.0f, n2, false);
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                if (i == 0 && j == 0) continue;
                float[] object = new float[]{f};
                int n6 = i;
                int n7 = j;
                formattedCharSequence.accept((n4, style, n5) -> {
                    boolean bl = style.isBold();
                    FontSet fontSet = this.getFontSet(style.getFont());
                    GlyphInfo glyphInfo = fontSet.getGlyphInfo(n5, this.filterFishyGlyphs);
                    preparedTextBuilder.x = object[0] + (float)n6 * glyphInfo.getShadowOffset();
                    preparedTextBuilder.y = f2 + (float)n7 * glyphInfo.getShadowOffset();
                    fArray[0] = object[0] + glyphInfo.getAdvance(bl);
                    return preparedTextBuilder.accept(n4, style.withColor(n2), n5);
                });
            }
        }
        GlyphVisitor glyphVisitor = GlyphVisitor.forMultiBufferSource(multiBufferSource, matrix4f, DisplayMode.NORMAL, n3);
        for (BakedGlyph.GlyphInstance glyphInstance : preparedTextBuilder.glyphs) {
            glyphVisitor.acceptGlyph(glyphInstance);
        }
        PreparedTextBuilder preparedTextBuilder2 = new PreparedTextBuilder(f, f2, n, false);
        formattedCharSequence.accept(preparedTextBuilder2);
        preparedTextBuilder2.visit(GlyphVisitor.forMultiBufferSource(multiBufferSource, matrix4f, DisplayMode.POLYGON_OFFSET, n3));
    }

    public PreparedText prepareText(String string, float f, float f2, int n, boolean bl, int n2) {
        if (this.isBidirectional()) {
            string = this.bidirectionalShaping(string);
        }
        PreparedTextBuilder preparedTextBuilder = new PreparedTextBuilder(f, f2, n, n2, bl);
        StringDecomposer.iterateFormatted(string, Style.EMPTY, (FormattedCharSink)preparedTextBuilder);
        return preparedTextBuilder;
    }

    public PreparedText prepareText(FormattedCharSequence formattedCharSequence, float f, float f2, int n, boolean bl, int n2) {
        PreparedTextBuilder preparedTextBuilder = new PreparedTextBuilder(f, f2, n, n2, bl);
        formattedCharSequence.accept(preparedTextBuilder);
        return preparedTextBuilder;
    }

    public int width(String string) {
        return Mth.ceil(this.splitter.stringWidth(string));
    }

    public int width(FormattedText formattedText) {
        return Mth.ceil(this.splitter.stringWidth(formattedText));
    }

    public int width(FormattedCharSequence formattedCharSequence) {
        return Mth.ceil(this.splitter.stringWidth(formattedCharSequence));
    }

    public String plainSubstrByWidth(String string, int n, boolean bl) {
        return bl ? this.splitter.plainTailByWidth(string, n, Style.EMPTY) : this.splitter.plainHeadByWidth(string, n, Style.EMPTY);
    }

    public String plainSubstrByWidth(String string, int n) {
        return this.splitter.plainHeadByWidth(string, n, Style.EMPTY);
    }

    public FormattedText substrByWidth(FormattedText formattedText, int n) {
        return this.splitter.headByWidth(formattedText, n, Style.EMPTY);
    }

    public int wordWrapHeight(String string, int n) {
        return 9 * this.splitter.splitLines(string, n, Style.EMPTY).size();
    }

    public int wordWrapHeight(FormattedText formattedText, int n) {
        return 9 * this.splitter.splitLines(formattedText, n, Style.EMPTY).size();
    }

    public List<FormattedCharSequence> split(FormattedText formattedText, int n) {
        return Language.getInstance().getVisualOrder(this.splitter.splitLines(formattedText, n, Style.EMPTY));
    }

    public List<FormattedText> splitIgnoringLanguage(FormattedText formattedText, int n) {
        return this.splitter.splitLines(formattedText, n, Style.EMPTY);
    }

    public boolean isBidirectional() {
        return Language.getInstance().isDefaultRightToLeft();
    }

    public StringSplitter getSplitter() {
        return this.splitter;
    }

    public static interface PreparedText {
        public void visit(GlyphVisitor var1);

        @Nullable
        public ScreenRectangle bounds();
    }

    public static interface GlyphVisitor {
        public static GlyphVisitor forMultiBufferSource(final MultiBufferSource multiBufferSource, final Matrix4f matrix4f, final DisplayMode displayMode, final int n) {
            return new GlyphVisitor(){

                @Override
                public void acceptGlyph(BakedGlyph.GlyphInstance glyphInstance) {
                    BakedGlyph bakedGlyph = glyphInstance.glyph();
                    VertexConsumer vertexConsumer = multiBufferSource.getBuffer(bakedGlyph.renderType(displayMode));
                    bakedGlyph.renderChar(glyphInstance, matrix4f, vertexConsumer, n, false);
                }

                @Override
                public void acceptEffect(BakedGlyph bakedGlyph, BakedGlyph.Effect effect) {
                    VertexConsumer vertexConsumer = multiBufferSource.getBuffer(bakedGlyph.renderType(displayMode));
                    bakedGlyph.renderEffect(effect, matrix4f, vertexConsumer, n, false);
                }
            };
        }

        public void acceptGlyph(BakedGlyph.GlyphInstance var1);

        public void acceptEffect(BakedGlyph var1, BakedGlyph.Effect var2);
    }

    public static enum DisplayMode {
        NORMAL,
        SEE_THROUGH,
        POLYGON_OFFSET;

    }

    class PreparedTextBuilder
    implements FormattedCharSink,
    PreparedText {
        private final boolean drawShadow;
        private final int color;
        private final int backgroundColor;
        float x;
        float y;
        private float left = Float.MAX_VALUE;
        private float top = Float.MAX_VALUE;
        private float right = -3.4028235E38f;
        private float bottom = -3.4028235E38f;
        private float backgroundLeft = Float.MAX_VALUE;
        private float backgroundTop = Float.MAX_VALUE;
        private float backgroundRight = -3.4028235E38f;
        private float backgroundBottom = -3.4028235E38f;
        final List<BakedGlyph.GlyphInstance> glyphs = new ArrayList<BakedGlyph.GlyphInstance>();
        @Nullable
        private List<BakedGlyph.Effect> effects;

        public PreparedTextBuilder(float f, float f2, int n, boolean bl) {
            this(f, f2, n, 0, bl);
        }

        public PreparedTextBuilder(float f, float f2, int n, int n2, boolean bl) {
            this.x = f;
            this.y = f2;
            this.drawShadow = bl;
            this.color = n;
            this.backgroundColor = n2;
            this.markBackground(f, f2, 0.0f);
        }

        private void markSize(float f, float f2, float f3, float f4) {
            this.left = Math.min(this.left, f);
            this.top = Math.min(this.top, f2);
            this.right = Math.max(this.right, f3);
            this.bottom = Math.max(this.bottom, f4);
        }

        private void markBackground(float f, float f2, float f3) {
            if (ARGB.alpha(this.backgroundColor) == 0) {
                return;
            }
            this.backgroundLeft = Math.min(this.backgroundLeft, f - 1.0f);
            this.backgroundTop = Math.min(this.backgroundTop, f2 - 1.0f);
            this.backgroundRight = Math.max(this.backgroundRight, f + f3);
            this.backgroundBottom = Math.max(this.backgroundBottom, f2 + 9.0f);
            this.markSize(this.backgroundLeft, this.backgroundTop, this.backgroundRight, this.backgroundBottom);
        }

        private void addGlyph(BakedGlyph.GlyphInstance glyphInstance) {
            this.glyphs.add(glyphInstance);
            this.markSize(glyphInstance.left(), glyphInstance.top(), glyphInstance.right(), glyphInstance.bottom());
        }

        private void addEffect(BakedGlyph.Effect effect) {
            if (this.effects == null) {
                this.effects = new ArrayList<BakedGlyph.Effect>();
            }
            this.effects.add(effect);
            this.markSize(effect.left(), effect.top(), effect.right(), effect.bottom());
        }

        @Override
        public boolean accept(int n, Style style, int n2) {
            FontSet fontSet = Font.this.getFontSet(style.getFont());
            GlyphInfo glyphInfo = fontSet.getGlyphInfo(n2, Font.this.filterFishyGlyphs);
            BakedGlyph bakedGlyph = style.isObfuscated() && n2 != 32 ? fontSet.getRandomGlyph(glyphInfo) : fontSet.getGlyph(n2);
            boolean bl = style.isBold();
            TextColor textColor = style.getColor();
            int n3 = this.getTextColor(textColor);
            int n4 = this.getShadowColor(style, n3);
            float f = glyphInfo.getAdvance(bl);
            float f2 = n == 0 ? this.x - 1.0f : this.x;
            float f3 = glyphInfo.getShadowOffset();
            if (!(bakedGlyph instanceof EmptyGlyph)) {
                float f4 = bl ? glyphInfo.getBoldOffset() : 0.0f;
                this.addGlyph(new BakedGlyph.GlyphInstance(this.x, this.y, n3, n4, bakedGlyph, style, f4, f3));
            }
            this.markBackground(this.x, this.y, f);
            if (style.isStrikethrough()) {
                this.addEffect(new BakedGlyph.Effect(f2, this.y + 4.5f - 1.0f, this.x + f, this.y + 4.5f, 0.01f, n3, n4, f3));
            }
            if (style.isUnderlined()) {
                this.addEffect(new BakedGlyph.Effect(f2, this.y + 9.0f - 1.0f, this.x + f, this.y + 9.0f, 0.01f, n3, n4, f3));
            }
            this.x += f;
            return true;
        }

        @Override
        public void visit(GlyphVisitor glyphVisitor) {
            BakedGlyph bakedGlyph = null;
            if (ARGB.alpha(this.backgroundColor) != 0) {
                Iterator<BakedGlyph.Effect> iterator = new BakedGlyph.Effect(this.backgroundLeft, this.backgroundTop, this.backgroundRight, this.backgroundBottom, -0.01f, this.backgroundColor);
                bakedGlyph = Font.this.getFontSet(Style.DEFAULT_FONT).whiteGlyph();
                glyphVisitor.acceptEffect(bakedGlyph, (BakedGlyph.Effect)((Object)iterator));
            }
            for (BakedGlyph.GlyphInstance record : this.glyphs) {
                glyphVisitor.acceptGlyph(record);
            }
            if (this.effects != null) {
                if (bakedGlyph == null) {
                    bakedGlyph = Font.this.getFontSet(Style.DEFAULT_FONT).whiteGlyph();
                }
                for (BakedGlyph.Effect effect : this.effects) {
                    glyphVisitor.acceptEffect(bakedGlyph, effect);
                }
            }
        }

        private int getTextColor(@Nullable TextColor textColor) {
            if (textColor != null) {
                int n = ARGB.alpha(this.color);
                int n2 = textColor.getValue();
                return ARGB.color(n, n2);
            }
            return this.color;
        }

        private int getShadowColor(Style style, int n) {
            Integer n2 = style.getShadowColor();
            if (n2 != null) {
                float f = ARGB.alphaFloat(n);
                float f2 = ARGB.alphaFloat(n2);
                if (f != 1.0f) {
                    return ARGB.color(ARGB.as8BitChannel(f * f2), (int)n2);
                }
                return n2;
            }
            if (this.drawShadow) {
                return ARGB.scaleRGB(n, 0.25f);
            }
            return 0;
        }

        @Override
        @Nullable
        public ScreenRectangle bounds() {
            if (this.left >= this.right || this.top >= this.bottom) {
                return null;
            }
            int n = Mth.floor(this.left);
            int n2 = Mth.floor(this.top);
            int n3 = Mth.ceil(this.right);
            int n4 = Mth.ceil(this.bottom);
            return new ScreenRectangle(n, n2, n3 - n, n4 - n2);
        }
    }
}

