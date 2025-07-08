/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 */
package net.minecraft.client.gui.render.pip;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.pip.GuiProfilerChartRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ResultField;
import org.joml.Matrix4f;

public class GuiProfilerChartRenderer
extends PictureInPictureRenderer<GuiProfilerChartRenderState> {
    public GuiProfilerChartRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    @Override
    public Class<GuiProfilerChartRenderState> getRenderStateClass() {
        return GuiProfilerChartRenderState.class;
    }

    @Override
    protected void renderToTexture(GuiProfilerChartRenderState guiProfilerChartRenderState, PoseStack poseStack) {
        double d = 0.0;
        poseStack.translate(0.0f, -5.0f, 0.0f);
        Matrix4f matrix4f = poseStack.last().pose();
        for (ResultField resultField : guiProfilerChartRenderState.chartData()) {
            float f;
            float f2;
            float f3;
            int n;
            int n2 = Mth.floor(resultField.percentage / 4.0) + 1;
            VertexConsumer vertexConsumer = this.bufferSource.getBuffer(RenderType.debugTriangleFan());
            int n3 = ARGB.opaque(resultField.getColor());
            int n4 = ARGB.multiply(n3, -8355712);
            vertexConsumer.addVertex(matrix4f, 0.0f, 0.0f, 0.0f).setColor(n3);
            for (n = n2; n >= 0; --n) {
                f3 = (float)((d + resultField.percentage * (double)n / (double)n2) * 6.2831854820251465 / 100.0);
                f2 = Mth.sin(f3) * 105.0f;
                f = Mth.cos(f3) * 105.0f * 0.5f;
                vertexConsumer.addVertex(matrix4f, f2, f, 0.0f).setColor(n3);
            }
            vertexConsumer = this.bufferSource.getBuffer(RenderType.debugQuads());
            for (n = n2; n > 0; --n) {
                f3 = (float)((d + resultField.percentage * (double)n / (double)n2) * 6.2831854820251465 / 100.0);
                f2 = Mth.sin(f3) * 105.0f;
                f = Mth.cos(f3) * 105.0f * 0.5f;
                float f4 = (float)((d + resultField.percentage * (double)(n - 1) / (double)n2) * 6.2831854820251465 / 100.0);
                float f5 = Mth.sin(f4) * 105.0f;
                float f6 = Mth.cos(f4) * 105.0f * 0.5f;
                if ((f + f6) / 2.0f < 0.0f) continue;
                vertexConsumer.addVertex(matrix4f, f2, f, 0.0f).setColor(n4);
                vertexConsumer.addVertex(matrix4f, f2, f + 10.0f, 0.0f).setColor(n4);
                vertexConsumer.addVertex(matrix4f, f5, f6 + 10.0f, 0.0f).setColor(n4);
                vertexConsumer.addVertex(matrix4f, f5, f6, 0.0f).setColor(n4);
            }
            d += resultField.percentage;
        }
    }

    @Override
    protected float getTranslateY(int n, int n2) {
        return (float)n / 2.0f;
    }

    @Override
    protected String getTextureLabel() {
        return "profiler chart";
    }
}

