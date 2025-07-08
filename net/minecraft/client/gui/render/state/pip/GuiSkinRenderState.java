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
import net.minecraft.client.model.PlayerModel;
import net.minecraft.resources.ResourceLocation;

public record GuiSkinRenderState(PlayerModel playerModel, ResourceLocation texture, float rotationX, float rotationY, float pivotY, int x0, int y0, int x1, int y1, float scale, @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds) implements PictureInPictureRenderState
{
    public GuiSkinRenderState(PlayerModel playerModel, ResourceLocation resourceLocation, float f, float f2, float f3, int n, int n2, int n3, int n4, float f4, @Nullable ScreenRectangle screenRectangle) {
        this(playerModel, resourceLocation, f, f2, f3, n, n2, n3, n4, f4, screenRectangle, PictureInPictureRenderState.getBounds(n, n2, n3, n4, screenRectangle));
    }
}

