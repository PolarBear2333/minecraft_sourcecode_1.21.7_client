/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.joml.Matrix3x2f
 */
package net.minecraft.client.gui.render.state.pip;

import javax.annotation.Nullable;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.GuiItemRenderState;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import org.joml.Matrix3x2f;

public record OversizedItemRenderState(GuiItemRenderState guiItemRenderState, int x0, int y0, int x1, int y1) implements PictureInPictureRenderState
{
    @Override
    public float scale() {
        return 16.0f;
    }

    @Override
    public Matrix3x2f pose() {
        return this.guiItemRenderState.pose();
    }

    @Override
    @Nullable
    public ScreenRectangle scissorArea() {
        return this.guiItemRenderState.scissorArea();
    }

    @Override
    @Nullable
    public ScreenRectangle bounds() {
        return this.guiItemRenderState.bounds();
    }
}

