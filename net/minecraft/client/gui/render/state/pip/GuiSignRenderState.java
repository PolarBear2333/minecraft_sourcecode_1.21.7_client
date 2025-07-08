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
import net.minecraft.client.model.Model;
import net.minecraft.world.level.block.state.properties.WoodType;

public record GuiSignRenderState(Model signModel, WoodType woodType, int x0, int y0, int x1, int y1, float scale, @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds) implements PictureInPictureRenderState
{
    public GuiSignRenderState(Model model, WoodType woodType, int n, int n2, int n3, int n4, float f, @Nullable ScreenRectangle screenRectangle) {
        this(model, woodType, n, n2, n3, n4, f, screenRectangle, PictureInPictureRenderState.getBounds(n, n2, n3, n4, screenRectangle));
    }
}

