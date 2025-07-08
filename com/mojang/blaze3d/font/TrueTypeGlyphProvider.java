/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntArraySet
 *  it.unimi.dsi.fastutil.ints.IntSet
 *  javax.annotation.Nullable
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.system.MemoryUtil
 *  org.lwjgl.util.freetype.FT_Bitmap
 *  org.lwjgl.util.freetype.FT_Face
 *  org.lwjgl.util.freetype.FT_GlyphSlot
 *  org.lwjgl.util.freetype.FT_Vector
 *  org.lwjgl.util.freetype.FreeType
 */
package com.mojang.blaze3d.font;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Locale;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.gui.font.CodepointMap;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.providers.FreeTypeUtil;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.FT_Bitmap;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FT_GlyphSlot;
import org.lwjgl.util.freetype.FT_Vector;
import org.lwjgl.util.freetype.FreeType;

public class TrueTypeGlyphProvider
implements GlyphProvider {
    @Nullable
    private ByteBuffer fontMemory;
    @Nullable
    private FT_Face face;
    final float oversample;
    private final CodepointMap<GlyphEntry> glyphs = new CodepointMap(GlyphEntry[]::new, n -> new GlyphEntry[n][]);

    public TrueTypeGlyphProvider(ByteBuffer byteBuffer, FT_Face fT_Face, float f, float f2, float f3, float f4, String string) {
        this.fontMemory = byteBuffer;
        this.face = fT_Face;
        this.oversample = f2;
        IntArraySet intArraySet = new IntArraySet();
        string.codePoints().forEach(arg_0 -> ((IntSet)intArraySet).add(arg_0));
        int n2 = Math.round(f * f2);
        FreeType.FT_Set_Pixel_Sizes((FT_Face)fT_Face, (int)n2, (int)n2);
        float f5 = f3 * f2;
        float f6 = -f4 * f2;
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            int n3;
            FT_Vector fT_Vector = FreeTypeUtil.setVector(FT_Vector.malloc((MemoryStack)memoryStack), f5, f6);
            FreeType.FT_Set_Transform((FT_Face)fT_Face, null, (FT_Vector)fT_Vector);
            IntBuffer intBuffer = memoryStack.mallocInt(1);
            int n4 = (int)FreeType.FT_Get_First_Char((FT_Face)fT_Face, (IntBuffer)intBuffer);
            while ((n3 = intBuffer.get(0)) != 0) {
                if (!intArraySet.contains(n4)) {
                    this.glyphs.put(n4, new GlyphEntry(n3));
                }
                n4 = (int)FreeType.FT_Get_Next_Char((FT_Face)fT_Face, (long)n4, (IntBuffer)intBuffer);
            }
        }
    }

    @Override
    @Nullable
    public GlyphInfo getGlyph(int n) {
        GlyphEntry glyphEntry = this.glyphs.get(n);
        return glyphEntry != null ? this.getOrLoadGlyphInfo(n, glyphEntry) : null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private GlyphInfo getOrLoadGlyphInfo(int n, GlyphEntry glyphEntry) {
        GlyphInfo glyphInfo = glyphEntry.glyph;
        if (glyphInfo == null) {
            FT_Face fT_Face;
            FT_Face fT_Face2 = fT_Face = this.validateFontOpen();
            synchronized (fT_Face2) {
                glyphInfo = glyphEntry.glyph;
                if (glyphInfo == null) {
                    glyphEntry.glyph = glyphInfo = this.loadGlyph(n, fT_Face, glyphEntry.index);
                }
            }
        }
        return glyphInfo;
    }

    private GlyphInfo loadGlyph(int n, FT_Face fT_Face, int n2) {
        FT_GlyphSlot fT_GlyphSlot;
        int n3 = FreeType.FT_Load_Glyph((FT_Face)fT_Face, (int)n2, (int)0x400008);
        if (n3 != 0) {
            FreeTypeUtil.assertError(n3, String.format(Locale.ROOT, "Loading glyph U+%06X", n));
        }
        if ((fT_GlyphSlot = fT_Face.glyph()) == null) {
            throw new NullPointerException(String.format(Locale.ROOT, "Glyph U+%06X not initialized", n));
        }
        float f = FreeTypeUtil.x(fT_GlyphSlot.advance());
        FT_Bitmap fT_Bitmap = fT_GlyphSlot.bitmap();
        int n4 = fT_GlyphSlot.bitmap_left();
        int n5 = fT_GlyphSlot.bitmap_top();
        int n6 = fT_Bitmap.width();
        int n7 = fT_Bitmap.rows();
        if (n6 <= 0 || n7 <= 0) {
            return () -> f / this.oversample;
        }
        return new Glyph(n4, n5, n6, n7, f, n2);
    }

    FT_Face validateFontOpen() {
        if (this.fontMemory == null || this.face == null) {
            throw new IllegalStateException("Provider already closed");
        }
        return this.face;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void close() {
        if (this.face != null) {
            Object object = FreeTypeUtil.LIBRARY_LOCK;
            synchronized (object) {
                FreeTypeUtil.checkError(FreeType.FT_Done_Face((FT_Face)this.face), "Deleting face");
            }
            this.face = null;
        }
        MemoryUtil.memFree((Buffer)this.fontMemory);
        this.fontMemory = null;
    }

    @Override
    public IntSet getSupportedGlyphs() {
        return this.glyphs.keySet();
    }

    static class GlyphEntry {
        final int index;
        @Nullable
        volatile GlyphInfo glyph;

        GlyphEntry(int n) {
            this.index = n;
        }
    }

    class Glyph
    implements GlyphInfo {
        final int width;
        final int height;
        final float bearingX;
        final float bearingY;
        private final float advance;
        final int index;

        Glyph(float f, float f2, int n, int n2, float f3, int n3) {
            this.width = n;
            this.height = n2;
            this.advance = f3 / TrueTypeGlyphProvider.this.oversample;
            this.bearingX = f / TrueTypeGlyphProvider.this.oversample;
            this.bearingY = f2 / TrueTypeGlyphProvider.this.oversample;
            this.index = n3;
        }

        @Override
        public float getAdvance() {
            return this.advance;
        }

        @Override
        public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> function) {
            return function.apply(new SheetGlyphInfo(){

                @Override
                public int getPixelWidth() {
                    return Glyph.this.width;
                }

                @Override
                public int getPixelHeight() {
                    return Glyph.this.height;
                }

                @Override
                public float getOversample() {
                    return TrueTypeGlyphProvider.this.oversample;
                }

                @Override
                public float getBearingLeft() {
                    return Glyph.this.bearingX;
                }

                @Override
                public float getBearingTop() {
                    return Glyph.this.bearingY;
                }

                @Override
                public void upload(int n, int n2, GpuTexture gpuTexture) {
                    FT_Face fT_Face = TrueTypeGlyphProvider.this.validateFontOpen();
                    try (NativeImage nativeImage = new NativeImage(NativeImage.Format.LUMINANCE, Glyph.this.width, Glyph.this.height, false);){
                        if (nativeImage.copyFromFont(fT_Face, Glyph.this.index)) {
                            RenderSystem.getDevice().createCommandEncoder().writeToTexture(gpuTexture, nativeImage, 0, 0, n, n2, Glyph.this.width, Glyph.this.height, 0, 0);
                        }
                    }
                }

                @Override
                public boolean isColored() {
                    return false;
                }
            });
        }
    }
}

