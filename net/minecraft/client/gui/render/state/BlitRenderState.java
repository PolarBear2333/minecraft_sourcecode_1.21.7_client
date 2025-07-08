/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.joml.Matrix3x2f
 */
package net.minecraft.client.gui.render.state;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import javax.annotation.Nullable;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import org.joml.Matrix3x2f;

public record BlitRenderState(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2f pose, int x0, int y0, int x1, int y1, float u0, float u1, float v0, float v1, int color, @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds) implements GuiElementRenderState
{
    public BlitRenderState(RenderPipeline renderPipeline, TextureSetup textureSetup, Matrix3x2f matrix3x2f, int n, int n2, int n3, int n4, float f, float f2, float f3, float f4, int n5, @Nullable ScreenRectangle screenRectangle) {
        this(renderPipeline, textureSetup, matrix3x2f, n, n2, n3, n4, f, f2, f3, f4, n5, screenRectangle, BlitRenderState.getBounds(n, n2, n3, n4, matrix3x2f, screenRectangle));
    }

    @Override
    public void buildVertices(VertexConsumer vertexConsumer, float f) {
        vertexConsumer.addVertexWith2DPose(this.pose(), this.x0(), this.y0(), f).setUv(this.u0(), this.v0()).setColor(this.color());
        vertexConsumer.addVertexWith2DPose(this.pose(), this.x0(), this.y1(), f).setUv(this.u0(), this.v1()).setColor(this.color());
        vertexConsumer.addVertexWith2DPose(this.pose(), this.x1(), this.y1(), f).setUv(this.u1(), this.v1()).setColor(this.color());
        vertexConsumer.addVertexWith2DPose(this.pose(), this.x1(), this.y0(), f).setUv(this.u1(), this.v0()).setColor(this.color());
    }

    @Nullable
    private static ScreenRectangle getBounds(int n, int n2, int n3, int n4, Matrix3x2f matrix3x2f, @Nullable ScreenRectangle screenRectangle) {
        ScreenRectangle screenRectangle2 = new ScreenRectangle(n, n2, n3 - n, n4 - n2).transformMaxBounds(matrix3x2f);
        return screenRectangle != null ? screenRectangle.intersection(screenRectangle2) : screenRectangle2;
    }
}

