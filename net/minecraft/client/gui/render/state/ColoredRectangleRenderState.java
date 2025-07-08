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

public record ColoredRectangleRenderState(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2f pose, int x0, int y0, int x1, int y1, int col1, int col2, @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds) implements GuiElementRenderState
{
    public ColoredRectangleRenderState(RenderPipeline renderPipeline, TextureSetup textureSetup, Matrix3x2f matrix3x2f, int n, int n2, int n3, int n4, int n5, int n6, @Nullable ScreenRectangle screenRectangle) {
        this(renderPipeline, textureSetup, matrix3x2f, n, n2, n3, n4, n5, n6, screenRectangle, ColoredRectangleRenderState.getBounds(n, n2, n3, n4, matrix3x2f, screenRectangle));
    }

    @Override
    public void buildVertices(VertexConsumer vertexConsumer, float f) {
        vertexConsumer.addVertexWith2DPose(this.pose(), this.x0(), this.y0(), f).setColor(this.col1());
        vertexConsumer.addVertexWith2DPose(this.pose(), this.x0(), this.y1(), f).setColor(this.col2());
        vertexConsumer.addVertexWith2DPose(this.pose(), this.x1(), this.y1(), f).setColor(this.col2());
        vertexConsumer.addVertexWith2DPose(this.pose(), this.x1(), this.y0(), f).setColor(this.col1());
    }

    @Nullable
    private static ScreenRectangle getBounds(int n, int n2, int n3, int n4, Matrix3x2f matrix3x2f, @Nullable ScreenRectangle screenRectangle) {
        ScreenRectangle screenRectangle2 = new ScreenRectangle(n, n2, n3 - n, n4 - n2).transformMaxBounds(matrix3x2f);
        return screenRectangle != null ? screenRectangle.intersection(screenRectangle2) : screenRectangle2;
    }
}

