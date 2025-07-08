/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.ints.IntSet
 *  it.unimi.dsi.fastutil.ints.IntSets
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.gui.font.providers;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.gui.font.CodepointMap;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.providers.GlyphProviderDefinition;
import net.minecraft.client.gui.font.providers.GlyphProviderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

public class BitmapProvider
implements GlyphProvider {
    static final Logger LOGGER = LogUtils.getLogger();
    private final NativeImage image;
    private final CodepointMap<Glyph> glyphs;

    BitmapProvider(NativeImage nativeImage, CodepointMap<Glyph> codepointMap) {
        this.image = nativeImage;
        this.glyphs = codepointMap;
    }

    @Override
    public void close() {
        this.image.close();
    }

    @Override
    @Nullable
    public GlyphInfo getGlyph(int n) {
        return this.glyphs.get(n);
    }

    @Override
    public IntSet getSupportedGlyphs() {
        return IntSets.unmodifiable((IntSet)this.glyphs.keySet());
    }

    static final class Glyph
    extends Record
    implements GlyphInfo {
        final float scale;
        final NativeImage image;
        final int offsetX;
        final int offsetY;
        final int width;
        final int height;
        private final int advance;
        final int ascent;

        Glyph(float f, NativeImage nativeImage, int n, int n2, int n3, int n4, int n5, int n6) {
            this.scale = f;
            this.image = nativeImage;
            this.offsetX = n;
            this.offsetY = n2;
            this.width = n3;
            this.height = n4;
            this.advance = n5;
            this.ascent = n6;
        }

        @Override
        public float getAdvance() {
            return this.advance;
        }

        @Override
        public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> function) {
            return function.apply(new SheetGlyphInfo(){

                @Override
                public float getOversample() {
                    return 1.0f / scale;
                }

                @Override
                public int getPixelWidth() {
                    return width;
                }

                @Override
                public int getPixelHeight() {
                    return height;
                }

                @Override
                public float getBearingTop() {
                    return ascent;
                }

                @Override
                public void upload(int n, int n2, GpuTexture gpuTexture) {
                    RenderSystem.getDevice().createCommandEncoder().writeToTexture(gpuTexture, image, 0, 0, n, n2, width, height, offsetX, offsetY);
                }

                @Override
                public boolean isColored() {
                    return image.format().components() > 1;
                }
            });
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Glyph.class, "scale;image;offsetX;offsetY;width;height;advance;ascent", "scale", "image", "offsetX", "offsetY", "width", "height", "advance", "ascent"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Glyph.class, "scale;image;offsetX;offsetY;width;height;advance;ascent", "scale", "image", "offsetX", "offsetY", "width", "height", "advance", "ascent"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Glyph.class, "scale;image;offsetX;offsetY;width;height;advance;ascent", "scale", "image", "offsetX", "offsetY", "width", "height", "advance", "ascent"}, this, object);
        }

        public float scale() {
            return this.scale;
        }

        public NativeImage image() {
            return this.image;
        }

        public int offsetX() {
            return this.offsetX;
        }

        public int offsetY() {
            return this.offsetY;
        }

        public int width() {
            return this.width;
        }

        public int height() {
            return this.height;
        }

        public int advance() {
            return this.advance;
        }

        public int ascent() {
            return this.ascent;
        }
    }

    public record Definition(ResourceLocation file, int height, int ascent, int[][] codepointGrid) implements GlyphProviderDefinition
    {
        private static final Codec<int[][]> CODEPOINT_GRID_CODEC = Codec.STRING.listOf().xmap(list -> {
            int n = list.size();
            int[][] nArrayArray = new int[n][];
            for (int i = 0; i < n; ++i) {
                nArrayArray[i] = ((String)list.get(i)).codePoints().toArray();
            }
            return nArrayArray;
        }, nArray -> {
            ArrayList<String> arrayList = new ArrayList<String>(((int[][])nArray).length);
            for (int[] nArray2 : nArray) {
                arrayList.add(new String(nArray2, 0, nArray2.length));
            }
            return arrayList;
        }).validate(Definition::validateDimensions);
        public static final MapCodec<Definition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ResourceLocation.CODEC.fieldOf("file").forGetter(Definition::file), (App)Codec.INT.optionalFieldOf("height", (Object)8).forGetter(Definition::height), (App)Codec.INT.fieldOf("ascent").forGetter(Definition::ascent), (App)CODEPOINT_GRID_CODEC.fieldOf("chars").forGetter(Definition::codepointGrid)).apply((Applicative)instance, Definition::new)).validate(Definition::validate);

        private static DataResult<int[][]> validateDimensions(int[][] nArray) {
            int n = nArray.length;
            if (n == 0) {
                return DataResult.error(() -> "Expected to find data in codepoint grid");
            }
            int[] nArray2 = nArray[0];
            int n2 = nArray2.length;
            if (n2 == 0) {
                return DataResult.error(() -> "Expected to find data in codepoint grid");
            }
            for (int i = 1; i < n; ++i) {
                int[] nArray3 = nArray[i];
                if (nArray3.length == n2) continue;
                return DataResult.error(() -> "Lines in codepoint grid have to be the same length (found: " + nArray3.length + " codepoints, expected: " + n2 + "), pad with \\u0000");
            }
            return DataResult.success((Object)nArray);
        }

        private static DataResult<Definition> validate(Definition definition) {
            if (definition.ascent > definition.height) {
                return DataResult.error(() -> "Ascent " + definition.ascent + " higher than height " + definition.height);
            }
            return DataResult.success((Object)definition);
        }

        @Override
        public GlyphProviderType type() {
            return GlyphProviderType.BITMAP;
        }

        @Override
        public Either<GlyphProviderDefinition.Loader, GlyphProviderDefinition.Reference> unpack() {
            return Either.left(this::load);
        }

        private GlyphProvider load(ResourceManager resourceManager) throws IOException {
            ResourceLocation resourceLocation = this.file.withPrefix("textures/");
            try (InputStream inputStream = resourceManager.open(resourceLocation);){
                NativeImage nativeImage = NativeImage.read(NativeImage.Format.RGBA, inputStream);
                int n2 = nativeImage.getWidth();
                int n3 = nativeImage.getHeight();
                int n4 = n2 / this.codepointGrid[0].length;
                int n5 = n3 / this.codepointGrid.length;
                float f = (float)this.height / (float)n5;
                CodepointMap<Glyph> codepointMap = new CodepointMap<Glyph>(Glyph[]::new, n -> new Glyph[n][]);
                for (int i = 0; i < this.codepointGrid.length; ++i) {
                    int n6 = 0;
                    for (int n7 : this.codepointGrid[i]) {
                        int n8;
                        Glyph glyph;
                        int n9 = n6++;
                        if (n7 == 0 || (glyph = codepointMap.put(n7, new Glyph(f, nativeImage, n9 * n4, i * n5, n4, n5, (int)(0.5 + (double)((float)(n8 = this.getActualGlyphWidth(nativeImage, n4, n5, n9, i)) * f)) + 1, this.ascent))) == null) continue;
                        LOGGER.warn("Codepoint '{}' declared multiple times in {}", (Object)Integer.toHexString(n7), (Object)resourceLocation);
                    }
                }
                BitmapProvider bitmapProvider = new BitmapProvider(nativeImage, codepointMap);
                return bitmapProvider;
            }
        }

        private int getActualGlyphWidth(NativeImage nativeImage, int n, int n2, int n3, int n4) {
            int n5;
            for (n5 = n - 1; n5 >= 0; --n5) {
                int n6 = n3 * n + n5;
                for (int i = 0; i < n2; ++i) {
                    int n7 = n4 * n2 + i;
                    if (nativeImage.getLuminanceOrAlpha(n6, n7) == 0) continue;
                    return n5 + 1;
                }
            }
            return n5 + 1;
        }
    }
}

