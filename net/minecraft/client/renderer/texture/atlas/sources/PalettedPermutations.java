/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Supplier
 *  com.google.common.base.Suppliers
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.ints.Int2IntMap
 *  it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer.texture.atlas.sources;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntUnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.sources.LazyLoadedImage;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.util.ARGB;
import org.slf4j.Logger;

public record PalettedPermutations(List<ResourceLocation> textures, ResourceLocation paletteKey, Map<String, ResourceLocation> permutations, String separator) implements SpriteSource
{
    static final Logger LOGGER = LogUtils.getLogger();
    public static final String DEFAULT_SEPARATOR = "_";
    public static final MapCodec<PalettedPermutations> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.list(ResourceLocation.CODEC).fieldOf("textures").forGetter(PalettedPermutations::textures), (App)ResourceLocation.CODEC.fieldOf("palette_key").forGetter(PalettedPermutations::paletteKey), (App)Codec.unboundedMap((Codec)Codec.STRING, ResourceLocation.CODEC).fieldOf("permutations").forGetter(PalettedPermutations::permutations), (App)Codec.STRING.optionalFieldOf("separator", (Object)DEFAULT_SEPARATOR).forGetter(PalettedPermutations::separator)).apply((Applicative)instance, PalettedPermutations::new));

    public PalettedPermutations(List<ResourceLocation> list, ResourceLocation resourceLocation, Map<String, ResourceLocation> map) {
        this(list, resourceLocation, map, DEFAULT_SEPARATOR);
    }

    @Override
    public void run(ResourceManager resourceManager, SpriteSource.Output output) {
        Supplier supplier = Suppliers.memoize(() -> PalettedPermutations.loadPaletteEntryFromImage(resourceManager, this.paletteKey));
        HashMap hashMap = new HashMap();
        this.permutations.forEach((arg_0, arg_1) -> PalettedPermutations.lambda$run$3(hashMap, (java.util.function.Supplier)supplier, resourceManager, arg_0, arg_1));
        for (ResourceLocation resourceLocation : this.textures) {
            ResourceLocation resourceLocation2 = TEXTURE_ID_CONVERTER.idToFile(resourceLocation);
            Optional<Resource> optional = resourceManager.getResource(resourceLocation2);
            if (optional.isEmpty()) {
                LOGGER.warn("Unable to find texture {}", (Object)resourceLocation2);
                continue;
            }
            LazyLoadedImage lazyLoadedImage = new LazyLoadedImage(resourceLocation2, optional.get(), hashMap.size());
            for (Map.Entry entry : hashMap.entrySet()) {
                ResourceLocation resourceLocation3 = resourceLocation.withSuffix(this.separator + (String)entry.getKey());
                output.add(resourceLocation3, new PalettedSpriteSupplier(lazyLoadedImage, (java.util.function.Supplier)entry.getValue(), resourceLocation3));
            }
        }
    }

    private static IntUnaryOperator createPaletteMapping(int[] nArray, int[] nArray2) {
        if (nArray2.length != nArray.length) {
            LOGGER.warn("Palette mapping has different sizes: {} and {}", (Object)nArray.length, (Object)nArray2.length);
            throw new IllegalArgumentException();
        }
        Int2IntOpenHashMap int2IntOpenHashMap = new Int2IntOpenHashMap(nArray2.length);
        for (int i = 0; i < nArray.length; ++i) {
            int n = nArray[i];
            if (ARGB.alpha(n) == 0) continue;
            int2IntOpenHashMap.put(ARGB.transparent(n), nArray2[i]);
        }
        return arg_0 -> PalettedPermutations.lambda$createPaletteMapping$4((Int2IntMap)int2IntOpenHashMap, arg_0);
    }

    /*
     * Enabled aggressive exception aggregation
     */
    private static int[] loadPaletteEntryFromImage(ResourceManager resourceManager, ResourceLocation resourceLocation) {
        Optional<Resource> optional = resourceManager.getResource(TEXTURE_ID_CONVERTER.idToFile(resourceLocation));
        if (optional.isEmpty()) {
            LOGGER.error("Failed to load palette image {}", (Object)resourceLocation);
            throw new IllegalArgumentException();
        }
        try (InputStream inputStream = optional.get().open();){
            NativeImage nativeImage = NativeImage.read(inputStream);
            try {
                int[] nArray = nativeImage.getPixels();
                if (nativeImage != null) {
                    nativeImage.close();
                }
                return nArray;
            }
            catch (Throwable throwable) {
                if (nativeImage != null) {
                    try {
                        nativeImage.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                }
                throw throwable;
            }
        }
        catch (Exception exception) {
            LOGGER.error("Couldn't load texture {}", (Object)resourceLocation, (Object)exception);
            throw new IllegalArgumentException();
        }
    }

    public MapCodec<PalettedPermutations> codec() {
        return MAP_CODEC;
    }

    private static /* synthetic */ int lambda$createPaletteMapping$4(Int2IntMap int2IntMap, int n) {
        int n2 = ARGB.alpha(n);
        if (n2 == 0) {
            return n;
        }
        int n3 = ARGB.transparent(n);
        int n4 = int2IntMap.getOrDefault(n3, ARGB.opaque(n3));
        int n5 = ARGB.alpha(n4);
        return ARGB.color(n2 * n5 / 255, n4);
    }

    private static /* synthetic */ void lambda$run$3(Map map, java.util.function.Supplier supplier, ResourceManager resourceManager, String string, ResourceLocation resourceLocation) {
        map.put(string, Suppliers.memoize(() -> PalettedPermutations.lambda$run$2((java.util.function.Supplier)supplier, resourceManager, resourceLocation)));
    }

    private static /* synthetic */ IntUnaryOperator lambda$run$2(java.util.function.Supplier supplier, ResourceManager resourceManager, ResourceLocation resourceLocation) {
        return PalettedPermutations.createPaletteMapping((int[])supplier.get(), PalettedPermutations.loadPaletteEntryFromImage(resourceManager, resourceLocation));
    }

    record PalettedSpriteSupplier(LazyLoadedImage baseImage, java.util.function.Supplier<IntUnaryOperator> palette, ResourceLocation permutationLocation) implements SpriteSource.SpriteSupplier
    {
        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        @Nullable
        public SpriteContents apply(SpriteResourceLoader spriteResourceLoader) {
            try {
                NativeImage nativeImage = this.baseImage.get().mappedCopy(this.palette.get());
                SpriteContents spriteContents = new SpriteContents(this.permutationLocation, new FrameSize(nativeImage.getWidth(), nativeImage.getHeight()), nativeImage, ResourceMetadata.EMPTY);
                return spriteContents;
            }
            catch (IOException | IllegalArgumentException exception) {
                LOGGER.error("unable to apply palette to {}", (Object)this.permutationLocation, (Object)exception);
                SpriteContents spriteContents = null;
                return spriteContents;
            }
            finally {
                this.baseImage.release();
            }
        }

        @Override
        public void discard() {
            this.baseImage.release();
        }

        @Override
        @Nullable
        public /* synthetic */ Object apply(Object object) {
            return this.apply((SpriteResourceLoader)object);
        }
    }
}

