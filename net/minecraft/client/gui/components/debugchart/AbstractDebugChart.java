/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.components.debugchart;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.debugchart.SampleStorage;

public abstract class AbstractDebugChart {
    protected static final int COLOR_GREY = -2039584;
    protected static final int CHART_HEIGHT = 60;
    protected static final int LINE_WIDTH = 1;
    protected final Font font;
    protected final SampleStorage sampleStorage;

    protected AbstractDebugChart(Font font, SampleStorage sampleStorage) {
        this.font = font;
        this.sampleStorage = sampleStorage;
    }

    public int getWidth(int n) {
        return Math.min(this.sampleStorage.capacity() + 2, n);
    }

    public int getFullHeight() {
        return 60 + this.font.lineHeight;
    }

    public void drawChart(GuiGraphics guiGraphics, int n, int n2) {
        int n3 = guiGraphics.guiHeight();
        guiGraphics.fill(n, n3 - 60, n + n2, n3, -1873784752);
        long l = 0L;
        long l2 = Integer.MAX_VALUE;
        long l3 = Integer.MIN_VALUE;
        int n4 = Math.max(0, this.sampleStorage.capacity() - (n2 - 2));
        int n5 = this.sampleStorage.size() - n4;
        for (int i = 0; i < n5; ++i) {
            int n6 = n + i + 1;
            int n7 = n4 + i;
            long l4 = this.getValueForAggregation(n7);
            l2 = Math.min(l2, l4);
            l3 = Math.max(l3, l4);
            l += l4;
            this.drawDimensions(guiGraphics, n3, n6, n7);
        }
        guiGraphics.hLine(n, n + n2 - 1, n3 - 60, -1);
        guiGraphics.hLine(n, n + n2 - 1, n3 - 1, -1);
        guiGraphics.vLine(n, n3 - 60, n3, -1);
        guiGraphics.vLine(n + n2 - 1, n3 - 60, n3, -1);
        if (n5 > 0) {
            String string = this.toDisplayString(l2) + " min";
            String string2 = this.toDisplayString((double)l / (double)n5) + " avg";
            String string3 = this.toDisplayString(l3) + " max";
            guiGraphics.drawString(this.font, string, n + 2, n3 - 60 - this.font.lineHeight, -2039584);
            guiGraphics.drawCenteredString(this.font, string2, n + n2 / 2, n3 - 60 - this.font.lineHeight, -2039584);
            guiGraphics.drawString(this.font, string3, n + n2 - this.font.width(string3) - 2, n3 - 60 - this.font.lineHeight, -2039584);
        }
        this.renderAdditionalLinesAndLabels(guiGraphics, n, n2, n3);
    }

    protected void drawDimensions(GuiGraphics guiGraphics, int n, int n2, int n3) {
        this.drawMainDimension(guiGraphics, n, n2, n3);
        this.drawAdditionalDimensions(guiGraphics, n, n2, n3);
    }

    protected void drawMainDimension(GuiGraphics guiGraphics, int n, int n2, int n3) {
        long l = this.sampleStorage.get(n3);
        int n4 = this.getSampleHeight(l);
        int n5 = this.getSampleColor(l);
        guiGraphics.fill(n2, n - n4, n2 + 1, n, n5);
    }

    protected void drawAdditionalDimensions(GuiGraphics guiGraphics, int n, int n2, int n3) {
    }

    protected long getValueForAggregation(int n) {
        return this.sampleStorage.get(n);
    }

    protected void renderAdditionalLinesAndLabels(GuiGraphics guiGraphics, int n, int n2, int n3) {
    }

    protected void drawStringWithShade(GuiGraphics guiGraphics, String string, int n, int n2) {
        guiGraphics.fill(n, n2, n + this.font.width(string) + 1, n2 + this.font.lineHeight, -1873784752);
        guiGraphics.drawString(this.font, string, n + 1, n2 + 1, -2039584, false);
    }

    protected abstract String toDisplayString(double var1);

    protected abstract int getSampleHeight(double var1);

    protected abstract int getSampleColor(long var1);

    protected int getSampleColor(double d, double d2, int n, double d3, int n2, double d4, int n3) {
        if ((d = Mth.clamp(d, d2, d4)) < d3) {
            return ARGB.lerp((float)((d - d2) / (d3 - d2)), n, n2);
        }
        return ARGB.lerp((float)((d - d3) / (d4 - d3)), n2, n3);
    }
}

