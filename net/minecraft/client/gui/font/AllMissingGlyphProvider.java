/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntSet
 *  it.unimi.dsi.fastutil.ints.IntSets
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.font;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.GlyphProvider;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import javax.annotation.Nullable;
import net.minecraft.client.gui.font.glyphs.SpecialGlyphs;

public class AllMissingGlyphProvider
implements GlyphProvider {
    @Override
    @Nullable
    public GlyphInfo getGlyph(int n) {
        return SpecialGlyphs.MISSING;
    }

    @Override
    public IntSet getSupportedGlyphs() {
        return IntSets.EMPTY_SET;
    }
}

