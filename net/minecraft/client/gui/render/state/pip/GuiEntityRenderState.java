/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 */
package net.minecraft.client.gui.render.state.pip;

import javax.annotation.Nullable;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public record GuiEntityRenderState(EntityRenderState renderState, Vector3f translation, Quaternionf rotation, @Nullable Quaternionf overrideCameraAngle, int x0, int y0, int x1, int y1, float scale, @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds) implements PictureInPictureRenderState
{
    public GuiEntityRenderState(EntityRenderState entityRenderState, Vector3f vector3f, Quaternionf quaternionf, @Nullable Quaternionf quaternionf2, int n, int n2, int n3, int n4, float f, @Nullable ScreenRectangle screenRectangle) {
        this(entityRenderState, vector3f, quaternionf, quaternionf2, n, n2, n3, n4, f, screenRectangle, PictureInPictureRenderState.getBounds(n, n2, n3, n4, screenRectangle));
    }
}

