/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.font;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public record GlyphRenderTypes(RenderType normal, RenderType seeThrough, RenderType polygonOffset, RenderPipeline guiPipeline) {
    public static GlyphRenderTypes createForIntensityTexture(ResourceLocation resourceLocation) {
        return new GlyphRenderTypes(RenderType.textIntensity(resourceLocation), RenderType.textIntensitySeeThrough(resourceLocation), RenderType.textIntensityPolygonOffset(resourceLocation), RenderPipelines.TEXT_INTENSITY);
    }

    public static GlyphRenderTypes createForColorTexture(ResourceLocation resourceLocation) {
        return new GlyphRenderTypes(RenderType.text(resourceLocation), RenderType.textSeeThrough(resourceLocation), RenderType.textPolygonOffset(resourceLocation), RenderPipelines.TEXT);
    }

    public RenderType select(Font.DisplayMode displayMode) {
        return switch (displayMode) {
            default -> throw new MatchException(null, null);
            case Font.DisplayMode.NORMAL -> this.normal;
            case Font.DisplayMode.SEE_THROUGH -> this.seeThrough;
            case Font.DisplayMode.POLYGON_OFFSET -> this.polygonOffset;
        };
    }
}

