/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.font.glyphs;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;

public enum SpecialGlyphs implements GlyphInfo
{
    WHITE(() -> SpecialGlyphs.generate(5, 8, (n, n2) -> -1)),
    MISSING(() -> {
        int n3 = 5;
        int n4 = 8;
        return SpecialGlyphs.generate(5, 8, (n, n2) -> {
            boolean bl = n == 0 || n + 1 == 5 || n2 == 0 || n2 + 1 == 8;
            return bl ? -1 : 0;
        });
    });

    final NativeImage image;

    private static NativeImage generate(int n, int n2, PixelProvider pixelProvider) {
        NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, n, n2, false);
        for (int i = 0; i < n2; ++i) {
            for (int j = 0; j < n; ++j) {
                nativeImage.setPixel(j, i, pixelProvider.getColor(j, i));
            }
        }
        nativeImage.untrack();
        return nativeImage;
    }

    private SpecialGlyphs(Supplier<NativeImage> supplier) {
        this.image = supplier.get();
    }

    @Override
    public float getAdvance() {
        return this.image.getWidth() + 1;
    }

    @Override
    public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> function) {
        return function.apply(new SheetGlyphInfo(){

            @Override
            public int getPixelWidth() {
                return SpecialGlyphs.this.image.getWidth();
            }

            @Override
            public int getPixelHeight() {
                return SpecialGlyphs.this.image.getHeight();
            }

            @Override
            public float getOversample() {
                return 1.0f;
            }

            @Override
            public void upload(int n, int n2, GpuTexture gpuTexture) {
                RenderSystem.getDevice().createCommandEncoder().writeToTexture(gpuTexture, SpecialGlyphs.this.image, 0, 0, n, n2, SpecialGlyphs.this.image.getWidth(), SpecialGlyphs.this.image.getHeight(), 0, 0);
            }

            @Override
            public boolean isColored() {
                return true;
            }
        });
    }

    @FunctionalInterface
    static interface PixelProvider {
        public int getColor(int var1, int var2);
    }
}

