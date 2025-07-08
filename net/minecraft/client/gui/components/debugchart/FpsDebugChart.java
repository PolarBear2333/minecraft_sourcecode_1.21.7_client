/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.components.debugchart;

import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.debugchart.AbstractDebugChart;
import net.minecraft.util.debugchart.SampleStorage;

public class FpsDebugChart
extends AbstractDebugChart {
    private static final int CHART_TOP_FPS = 30;
    private static final double CHART_TOP_VALUE = 33.333333333333336;

    public FpsDebugChart(Font font, SampleStorage sampleStorage) {
        super(font, sampleStorage);
    }

    @Override
    protected void renderAdditionalLinesAndLabels(GuiGraphics guiGraphics, int n, int n2, int n3) {
        this.drawStringWithShade(guiGraphics, "30 FPS", n + 1, n3 - 60 + 1);
        this.drawStringWithShade(guiGraphics, "60 FPS", n + 1, n3 - 30 + 1);
        guiGraphics.hLine(n, n + n2 - 1, n3 - 30, -1);
        int n4 = Minecraft.getInstance().options.framerateLimit().get();
        if (n4 > 0 && n4 <= 250) {
            guiGraphics.hLine(n, n + n2 - 1, n3 - this.getSampleHeight(1.0E9 / (double)n4) - 1, -16711681);
        }
    }

    @Override
    protected String toDisplayString(double d) {
        return String.format(Locale.ROOT, "%d ms", (int)Math.round(FpsDebugChart.toMilliseconds(d)));
    }

    @Override
    protected int getSampleHeight(double d) {
        return (int)Math.round(FpsDebugChart.toMilliseconds(d) * 60.0 / 33.333333333333336);
    }

    @Override
    protected int getSampleColor(long l) {
        return this.getSampleColor(FpsDebugChart.toMilliseconds(l), 0.0, -16711936, 28.0, -256, 56.0, -65536);
    }

    private static double toMilliseconds(double d) {
        return d / 1000000.0;
    }
}

