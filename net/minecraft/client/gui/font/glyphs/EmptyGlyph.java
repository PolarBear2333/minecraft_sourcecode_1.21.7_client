/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 */
package net.minecraft.client.gui.font.glyphs;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.font.GlyphRenderTypes;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class EmptyGlyph
extends BakedGlyph {
    public static final EmptyGlyph INSTANCE = new EmptyGlyph();

    public EmptyGlyph() {
        super(GlyphRenderTypes.createForColorTexture(ResourceLocation.withDefaultNamespace("")), null, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
    }

    @Override
    public void renderChar(BakedGlyph.GlyphInstance glyphInstance, Matrix4f matrix4f, VertexConsumer vertexConsumer, int n, boolean bl) {
    }
}

