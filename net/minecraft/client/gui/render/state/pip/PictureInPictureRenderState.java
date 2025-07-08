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
import net.minecraft.client.gui.render.state.ScreenArea;
import org.joml.Matrix3x2f;

public interface PictureInPictureRenderState
extends ScreenArea {
    public static final Matrix3x2f IDENTITY_POSE = new Matrix3x2f();

    public int x0();

    public int x1();

    public int y0();

    public int y1();

    public float scale();

    default public Matrix3x2f pose() {
        return IDENTITY_POSE;
    }

    @Nullable
    public ScreenRectangle scissorArea();

    @Nullable
    public static ScreenRectangle getBounds(int n, int n2, int n3, int n4, @Nullable ScreenRectangle screenRectangle) {
        ScreenRectangle screenRectangle2 = new ScreenRectangle(n, n2, n3 - n, n4 - n2);
        return screenRectangle != null ? screenRectangle.intersection(screenRectangle2) : screenRectangle2;
    }
}

