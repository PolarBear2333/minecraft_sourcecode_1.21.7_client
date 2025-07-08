/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.render.state.pip;

import javax.annotation.Nullable;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

public record GuiBannerResultRenderState(ModelPart flag, DyeColor baseColor, BannerPatternLayers resultBannerPatterns, int x0, int y0, int x1, int y1, @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds) implements PictureInPictureRenderState
{
    public GuiBannerResultRenderState(ModelPart modelPart, DyeColor dyeColor, BannerPatternLayers bannerPatternLayers, int n, int n2, int n3, int n4, @Nullable ScreenRectangle screenRectangle) {
        this(modelPart, dyeColor, bannerPatternLayers, n, n2, n3, n4, screenRectangle, PictureInPictureRenderState.getBounds(n, n2, n3, n4, screenRectangle));
    }

    @Override
    public float scale() {
        return 16.0f;
    }
}

