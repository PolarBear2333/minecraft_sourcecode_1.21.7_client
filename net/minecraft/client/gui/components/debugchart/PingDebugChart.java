/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.components.debugchart;

import java.util.Locale;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.debugchart.AbstractDebugChart;
import net.minecraft.util.debugchart.SampleStorage;

public class PingDebugChart
extends AbstractDebugChart {
    private static final int CHART_TOP_VALUE = 500;

    public PingDebugChart(Font font, SampleStorage sampleStorage) {
        super(font, sampleStorage);
    }

    @Override
    protected void renderAdditionalLinesAndLabels(GuiGraphics guiGraphics, int n, int n2, int n3) {
        this.drawStringWithShade(guiGraphics, "500 ms", n + 1, n3 - 60 + 1);
    }

    @Override
    protected String toDisplayString(double d) {
        return String.format(Locale.ROOT, "%d ms", (int)Math.round(d));
    }

    @Override
    protected int getSampleHeight(double d) {
        return (int)Math.round(d * 60.0 / 500.0);
    }

    @Override
    protected int getSampleColor(long l) {
        return this.getSampleColor(l, 0.0, -16711936, 250.0, -256, 500.0, -65536);
    }
}

