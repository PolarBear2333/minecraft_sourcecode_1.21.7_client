/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.joml.Matrix4f
 */
package net.minecraft.client.gui.font.glyphs;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import javax.annotation.Nullable;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Style;
import org.joml.Matrix4f;

public class BakedGlyph {
    public static final float Z_FIGHTER = 0.001f;
    private final GlyphRenderTypes renderTypes;
    @Nullable
    private final GpuTextureView textureView;
    private final float u0;
    private final float u1;
    private final float v0;
    private final float v1;
    private final float left;
    private final float right;
    private final float up;
    private final float down;

    public BakedGlyph(GlyphRenderTypes glyphRenderTypes, @Nullable GpuTextureView gpuTextureView, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8) {
        this.renderTypes = glyphRenderTypes;
        this.textureView = gpuTextureView;
        this.u0 = f;
        this.u1 = f2;
        this.v0 = f3;
        this.v1 = f4;
        this.left = f5;
        this.right = f6;
        this.up = f7;
        this.down = f8;
    }

    public float left(GlyphInstance glyphInstance) {
        return glyphInstance.x + this.left + (glyphInstance.style.isItalic() ? Math.min(this.shearTop(), this.shearBottom()) : 0.0f) - BakedGlyph.extraThickness(glyphInstance.style.isBold());
    }

    public float top(GlyphInstance glyphInstance) {
        return glyphInstance.y + this.up - BakedGlyph.extraThickness(glyphInstance.style.isBold());
    }

    public float right(GlyphInstance glyphInstance) {
        return glyphInstance.x + this.right + (glyphInstance.hasShadow() ? glyphInstance.shadowOffset : 0.0f) + (glyphInstance.style.isItalic() ? Math.max(this.shearTop(), this.shearBottom()) : 0.0f) + BakedGlyph.extraThickness(glyphInstance.style.isBold());
    }

    public float bottom(GlyphInstance glyphInstance) {
        return glyphInstance.y + this.down + (glyphInstance.hasShadow() ? glyphInstance.shadowOffset : 0.0f) + BakedGlyph.extraThickness(glyphInstance.style.isBold());
    }

    public void renderChar(GlyphInstance glyphInstance, Matrix4f matrix4f, VertexConsumer vertexConsumer, int n, boolean bl) {
        float f;
        float f2;
        Style style = glyphInstance.style();
        boolean bl2 = style.isItalic();
        float f3 = glyphInstance.x();
        float f4 = glyphInstance.y();
        int n2 = glyphInstance.color();
        boolean bl3 = style.isBold();
        float f5 = f2 = bl ? 0.0f : 0.001f;
        if (glyphInstance.hasShadow()) {
            int n3 = glyphInstance.shadowColor();
            this.render(bl2, f3 + glyphInstance.shadowOffset(), f4 + glyphInstance.shadowOffset(), 0.0f, matrix4f, vertexConsumer, n3, bl3, n);
            if (bl3) {
                this.render(bl2, f3 + glyphInstance.boldOffset() + glyphInstance.shadowOffset(), f4 + glyphInstance.shadowOffset(), f2, matrix4f, vertexConsumer, n3, true, n);
            }
            f = bl ? 0.0f : 0.03f;
        } else {
            f = 0.0f;
        }
        this.render(bl2, f3, f4, f, matrix4f, vertexConsumer, n2, bl3, n);
        if (bl3) {
            this.render(bl2, f3 + glyphInstance.boldOffset(), f4, f + f2, matrix4f, vertexConsumer, n2, true, n);
        }
    }

    private void render(boolean bl, float f, float f2, float f3, Matrix4f matrix4f, VertexConsumer vertexConsumer, int n, boolean bl2, int n2) {
        float f4 = f + this.left;
        float f5 = f + this.right;
        float f6 = f2 + this.up;
        float f7 = f2 + this.down;
        float f8 = bl ? this.shearTop() : 0.0f;
        float f9 = bl ? this.shearBottom() : 0.0f;
        float f10 = BakedGlyph.extraThickness(bl2);
        vertexConsumer.addVertex(matrix4f, f4 + f8 - f10, f6 - f10, f3).setColor(n).setUv(this.u0, this.v0).setLight(n2);
        vertexConsumer.addVertex(matrix4f, f4 + f9 - f10, f7 + f10, f3).setColor(n).setUv(this.u0, this.v1).setLight(n2);
        vertexConsumer.addVertex(matrix4f, f5 + f9 + f10, f7 + f10, f3).setColor(n).setUv(this.u1, this.v1).setLight(n2);
        vertexConsumer.addVertex(matrix4f, f5 + f8 + f10, f6 - f10, f3).setColor(n).setUv(this.u1, this.v0).setLight(n2);
    }

    private static float extraThickness(boolean bl) {
        return bl ? 0.1f : 0.0f;
    }

    private float shearBottom() {
        return 1.0f - 0.25f * this.down;
    }

    private float shearTop() {
        return 1.0f - 0.25f * this.up;
    }

    public void renderEffect(Effect effect, Matrix4f matrix4f, VertexConsumer vertexConsumer, int n, boolean bl) {
        float f;
        float f2 = f = bl ? 0.0f : effect.depth;
        if (effect.hasShadow()) {
            this.buildEffect(effect, effect.shadowOffset(), f, effect.shadowColor(), vertexConsumer, n, matrix4f);
            f += bl ? 0.0f : 0.03f;
        }
        this.buildEffect(effect, 0.0f, f, effect.color, vertexConsumer, n, matrix4f);
    }

    private void buildEffect(Effect effect, float f, float f2, int n, VertexConsumer vertexConsumer, int n2, Matrix4f matrix4f) {
        vertexConsumer.addVertex(matrix4f, effect.x0 + f, effect.y1 + f, f2).setColor(n).setUv(this.u0, this.v0).setLight(n2);
        vertexConsumer.addVertex(matrix4f, effect.x1 + f, effect.y1 + f, f2).setColor(n).setUv(this.u0, this.v1).setLight(n2);
        vertexConsumer.addVertex(matrix4f, effect.x1 + f, effect.y0 + f, f2).setColor(n).setUv(this.u1, this.v1).setLight(n2);
        vertexConsumer.addVertex(matrix4f, effect.x0 + f, effect.y0 + f, f2).setColor(n).setUv(this.u1, this.v0).setLight(n2);
    }

    @Nullable
    public GpuTextureView textureView() {
        return this.textureView;
    }

    public RenderPipeline guiPipeline() {
        return this.renderTypes.guiPipeline();
    }

    public RenderType renderType(Font.DisplayMode displayMode) {
        return this.renderTypes.select(displayMode);
    }

    public static final class GlyphInstance
    extends Record {
        final float x;
        final float y;
        private final int color;
        private final int shadowColor;
        private final BakedGlyph glyph;
        final Style style;
        private final float boldOffset;
        final float shadowOffset;

        public GlyphInstance(float f, float f2, int n, int n2, BakedGlyph bakedGlyph, Style style, float f3, float f4) {
            this.x = f;
            this.y = f2;
            this.color = n;
            this.shadowColor = n2;
            this.glyph = bakedGlyph;
            this.style = style;
            this.boldOffset = f3;
            this.shadowOffset = f4;
        }

        public float left() {
            return this.glyph.left(this);
        }

        public float top() {
            return this.glyph.top(this);
        }

        public float right() {
            return this.glyph.right(this);
        }

        public float bottom() {
            return this.glyph.bottom(this);
        }

        boolean hasShadow() {
            return this.shadowColor() != 0;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{GlyphInstance.class, "x;y;color;shadowColor;glyph;style;boldOffset;shadowOffset", "x", "y", "color", "shadowColor", "glyph", "style", "boldOffset", "shadowOffset"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{GlyphInstance.class, "x;y;color;shadowColor;glyph;style;boldOffset;shadowOffset", "x", "y", "color", "shadowColor", "glyph", "style", "boldOffset", "shadowOffset"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{GlyphInstance.class, "x;y;color;shadowColor;glyph;style;boldOffset;shadowOffset", "x", "y", "color", "shadowColor", "glyph", "style", "boldOffset", "shadowOffset"}, this, object);
        }

        public float x() {
            return this.x;
        }

        public float y() {
            return this.y;
        }

        public int color() {
            return this.color;
        }

        public int shadowColor() {
            return this.shadowColor;
        }

        public BakedGlyph glyph() {
            return this.glyph;
        }

        public Style style() {
            return this.style;
        }

        public float boldOffset() {
            return this.boldOffset;
        }

        public float shadowOffset() {
            return this.shadowOffset;
        }
    }

    public static final class Effect
    extends Record {
        final float x0;
        final float y0;
        final float x1;
        final float y1;
        final float depth;
        final int color;
        private final int shadowColor;
        private final float shadowOffset;

        public Effect(float f, float f2, float f3, float f4, float f5, int n) {
            this(f, f2, f3, f4, f5, n, 0, 0.0f);
        }

        public Effect(float f, float f2, float f3, float f4, float f5, int n, int n2, float f6) {
            this.x0 = f;
            this.y0 = f2;
            this.x1 = f3;
            this.y1 = f4;
            this.depth = f5;
            this.color = n;
            this.shadowColor = n2;
            this.shadowOffset = f6;
        }

        public float left() {
            return this.x0;
        }

        public float top() {
            return this.y0;
        }

        public float right() {
            return this.x1 + (this.hasShadow() ? this.shadowOffset : 0.0f);
        }

        public float bottom() {
            return this.y1 + (this.hasShadow() ? this.shadowOffset : 0.0f);
        }

        boolean hasShadow() {
            return this.shadowColor() != 0;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Effect.class, "x0;y0;x1;y1;depth;color;shadowColor;shadowOffset", "x0", "y0", "x1", "y1", "depth", "color", "shadowColor", "shadowOffset"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Effect.class, "x0;y0;x1;y1;depth;color;shadowColor;shadowOffset", "x0", "y0", "x1", "y1", "depth", "color", "shadowColor", "shadowOffset"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Effect.class, "x0;y0;x1;y1;depth;color;shadowColor;shadowOffset", "x0", "y0", "x1", "y1", "depth", "color", "shadowColor", "shadowOffset"}, this, object);
        }

        public float x0() {
            return this.x0;
        }

        public float y0() {
            return this.y0;
        }

        public float x1() {
            return this.x1;
        }

        public float y1() {
            return this.y1;
        }

        public float depth() {
            return this.depth;
        }

        public int color() {
            return this.color;
        }

        public int shadowColor() {
            return this.shadowColor;
        }

        public float shadowOffset() {
            return this.shadowOffset;
        }
    }
}

