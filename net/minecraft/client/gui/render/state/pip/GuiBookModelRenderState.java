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
import net.minecraft.client.model.BookModel;
import net.minecraft.resources.ResourceLocation;

public record GuiBookModelRenderState(BookModel bookModel, ResourceLocation texture, float open, float flip, int x0, int y0, int x1, int y1, float scale, @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds) implements PictureInPictureRenderState
{
    public GuiBookModelRenderState(BookModel bookModel, ResourceLocation resourceLocation, float f, float f2, int n, int n2, int n3, int n4, float f3, @Nullable ScreenRectangle screenRectangle) {
        this(bookModel, resourceLocation, f, f2, n, n2, n3, n4, f3, screenRectangle, PictureInPictureRenderState.getBounds(n, n2, n3, n4, screenRectangle));
    }
}

