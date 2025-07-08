/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Sets
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntCollection
 *  it.unimi.dsi.fastutil.ints.IntList
 *  it.unimi.dsi.fastutil.ints.IntOpenHashSet
 *  org.slf4j.Logger
 */
package net.minecraft.client.gui.font;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntFunction;
import net.minecraft.client.gui.font.CodepointMap;
import net.minecraft.client.gui.font.FontOption;
import net.minecraft.client.gui.font.FontTexture;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.SpecialGlyphs;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.slf4j.Logger;

public class FontSet
implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final RandomSource RANDOM = RandomSource.create();
    private static final float LARGE_FORWARD_ADVANCE = 32.0f;
    private final TextureManager textureManager;
    private final ResourceLocation name;
    private BakedGlyph missingGlyph;
    private BakedGlyph whiteGlyph;
    private List<GlyphProvider.Conditional> allProviders = List.of();
    private List<GlyphProvider> activeProviders = List.of();
    private final CodepointMap<BakedGlyph> glyphs = new CodepointMap(BakedGlyph[]::new, n -> new BakedGlyph[n][]);
    private final CodepointMap<GlyphInfoFilter> glyphInfos = new CodepointMap(GlyphInfoFilter[]::new, n -> new GlyphInfoFilter[n][]);
    private final Int2ObjectMap<IntList> glyphsByWidth = new Int2ObjectOpenHashMap();
    private final List<FontTexture> textures = Lists.newArrayList();
    private final IntFunction<GlyphInfoFilter> glyphInfoGetter = this::computeGlyphInfo;
    private final IntFunction<BakedGlyph> glyphGetter = this::computeBakedGlyph;

    public FontSet(TextureManager textureManager, ResourceLocation resourceLocation) {
        this.textureManager = textureManager;
        this.name = resourceLocation;
    }

    public void reload(List<GlyphProvider.Conditional> list, Set<FontOption> set) {
        this.allProviders = list;
        this.reload(set);
    }

    public void reload(Set<FontOption> set) {
        this.activeProviders = List.of();
        this.resetTextures();
        this.activeProviders = this.selectProviders(this.allProviders, set);
    }

    private void resetTextures() {
        this.textures.clear();
        this.glyphs.clear();
        this.glyphInfos.clear();
        this.glyphsByWidth.clear();
        this.missingGlyph = SpecialGlyphs.MISSING.bake(this::stitch);
        this.whiteGlyph = SpecialGlyphs.WHITE.bake(this::stitch);
    }

    private List<GlyphProvider> selectProviders(List<GlyphProvider.Conditional> list, Set<FontOption> set) {
        IntOpenHashSet intOpenHashSet = new IntOpenHashSet();
        ArrayList<GlyphProvider> arrayList = new ArrayList<GlyphProvider>();
        for (GlyphProvider.Conditional conditional : list) {
            if (!conditional.filter().apply(set)) continue;
            arrayList.add(conditional.provider());
            intOpenHashSet.addAll((IntCollection)conditional.provider().getSupportedGlyphs());
        }
        HashSet hashSet = Sets.newHashSet();
        intOpenHashSet.forEach(n2 -> {
            for (GlyphProvider glyphProvider : arrayList) {
                GlyphInfo glyphInfo = glyphProvider.getGlyph(n2);
                if (glyphInfo == null) continue;
                hashSet.add(glyphProvider);
                if (glyphInfo == SpecialGlyphs.MISSING) break;
                ((IntList)this.glyphsByWidth.computeIfAbsent(Mth.ceil(glyphInfo.getAdvance(false)), n -> new IntArrayList())).add(n2);
                break;
            }
        });
        return arrayList.stream().filter(hashSet::contains).toList();
    }

    @Override
    public void close() {
        this.textures.clear();
    }

    private static boolean hasFishyAdvance(GlyphInfo glyphInfo) {
        float f = glyphInfo.getAdvance(false);
        if (f < 0.0f || f > 32.0f) {
            return true;
        }
        float f2 = glyphInfo.getAdvance(true);
        return f2 < 0.0f || f2 > 32.0f;
    }

    private GlyphInfoFilter computeGlyphInfo(int n) {
        GlyphInfo glyphInfo = null;
        for (GlyphProvider glyphProvider : this.activeProviders) {
            GlyphInfo glyphInfo2 = glyphProvider.getGlyph(n);
            if (glyphInfo2 == null) continue;
            if (glyphInfo == null) {
                glyphInfo = glyphInfo2;
            }
            if (FontSet.hasFishyAdvance(glyphInfo2)) continue;
            return new GlyphInfoFilter(glyphInfo, glyphInfo2);
        }
        if (glyphInfo != null) {
            return new GlyphInfoFilter(glyphInfo, SpecialGlyphs.MISSING);
        }
        return GlyphInfoFilter.MISSING;
    }

    public GlyphInfo getGlyphInfo(int n, boolean bl) {
        return this.glyphInfos.computeIfAbsent(n, this.glyphInfoGetter).select(bl);
    }

    private BakedGlyph computeBakedGlyph(int n) {
        for (GlyphProvider glyphProvider : this.activeProviders) {
            GlyphInfo glyphInfo = glyphProvider.getGlyph(n);
            if (glyphInfo == null) continue;
            return glyphInfo.bake(this::stitch);
        }
        LOGGER.warn("Couldn't find glyph for character {} (\\u{})", (Object)Character.toString(n), (Object)String.format("%04x", n));
        return this.missingGlyph;
    }

    public BakedGlyph getGlyph(int n) {
        return this.glyphs.computeIfAbsent(n, this.glyphGetter);
    }

    private BakedGlyph stitch(SheetGlyphInfo sheetGlyphInfo) {
        Object object;
        for (FontTexture fontTexture : this.textures) {
            object = fontTexture.add(sheetGlyphInfo);
            if (object == null) continue;
            return object;
        }
        ResourceLocation resourceLocation = this.name.withSuffix("/" + this.textures.size());
        boolean bl = sheetGlyphInfo.isColored();
        object = bl ? GlyphRenderTypes.createForColorTexture(resourceLocation) : GlyphRenderTypes.createForIntensityTexture(resourceLocation);
        FontTexture fontTexture = new FontTexture(resourceLocation::toString, (GlyphRenderTypes)object, bl);
        this.textures.add(fontTexture);
        this.textureManager.register(resourceLocation, fontTexture);
        BakedGlyph bakedGlyph = fontTexture.add(sheetGlyphInfo);
        return bakedGlyph == null ? this.missingGlyph : bakedGlyph;
    }

    public BakedGlyph getRandomGlyph(GlyphInfo glyphInfo) {
        IntList intList = (IntList)this.glyphsByWidth.get(Mth.ceil(glyphInfo.getAdvance(false)));
        if (intList != null && !intList.isEmpty()) {
            return this.getGlyph(intList.getInt(RANDOM.nextInt(intList.size())));
        }
        return this.missingGlyph;
    }

    public ResourceLocation name() {
        return this.name;
    }

    public BakedGlyph whiteGlyph() {
        return this.whiteGlyph;
    }

    record GlyphInfoFilter(GlyphInfo glyphInfo, GlyphInfo glyphInfoNotFishy) {
        static final GlyphInfoFilter MISSING = new GlyphInfoFilter(SpecialGlyphs.MISSING, SpecialGlyphs.MISSING);

        GlyphInfo select(boolean bl) {
            return bl ? this.glyphInfoNotFishy : this.glyphInfo;
        }
    }
}

