/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.render.state.pip;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.util.profiling.ResultField;

public record GuiProfilerChartRenderState(List<ResultField> chartData, int x0, int y0, int x1, int y1, @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds) implements PictureInPictureRenderState
{
    public GuiProfilerChartRenderState(List<ResultField> list, int n, int n2, int n3, int n4, @Nullable ScreenRectangle screenRectangle) {
        this(list, n, n2, n3, n4, screenRectangle, PictureInPictureRenderState.getBounds(n, n2, n3, n4, screenRectangle));
    }

    @Override
    public float scale() {
        return 1.0f;
    }
}

