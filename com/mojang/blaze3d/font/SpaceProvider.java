/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.ints.IntSet
 *  it.unimi.dsi.fastutil.ints.IntSets
 *  javax.annotation.Nullable
 */
package com.mojang.blaze3d.font;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.gui.font.providers.GlyphProviderDefinition;
import net.minecraft.client.gui.font.providers.GlyphProviderType;
import net.minecraft.util.ExtraCodecs;

public class SpaceProvider
implements GlyphProvider {
    private final Int2ObjectMap<GlyphInfo.SpaceGlyphInfo> glyphs;

    public SpaceProvider(Map<Integer, Float> map) {
        this.glyphs = new Int2ObjectOpenHashMap(map.size());
        map.forEach((n, f) -> this.glyphs.put(n.intValue(), () -> f.floatValue()));
    }

    @Override
    @Nullable
    public GlyphInfo getGlyph(int n) {
        return (GlyphInfo)this.glyphs.get(n);
    }

    @Override
    public IntSet getSupportedGlyphs() {
        return IntSets.unmodifiable((IntSet)this.glyphs.keySet());
    }

    public record Definition(Map<Integer, Float> advances) implements GlyphProviderDefinition
    {
        public static final MapCodec<Definition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.unboundedMap(ExtraCodecs.CODEPOINT, (Codec)Codec.FLOAT).fieldOf("advances").forGetter(Definition::advances)).apply((Applicative)instance, Definition::new));

        @Override
        public GlyphProviderType type() {
            return GlyphProviderType.SPACE;
        }

        @Override
        public Either<GlyphProviderDefinition.Loader, GlyphProviderDefinition.Reference> unpack() {
            GlyphProviderDefinition.Loader loader = resourceManager -> new SpaceProvider(this.advances);
            return Either.left((Object)loader);
        }
    }
}

