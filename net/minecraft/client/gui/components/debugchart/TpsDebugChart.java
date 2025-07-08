/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.components.debugchart;

import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.debugchart.AbstractDebugChart;
import net.minecraft.util.TimeUtil;
import net.minecraft.util.debugchart.SampleStorage;
import net.minecraft.util.debugchart.TpsDebugDimensions;

public class TpsDebugChart
extends AbstractDebugChart {
    private static final int TICK_METHOD_COLOR = -6745839;
    private static final int TASK_COLOR = -4548257;
    private static final int OTHER_COLOR = -10547572;
    private final Supplier<Float> msptSupplier;

    public TpsDebugChart(Font font, SampleStorage sampleStorage, Supplier<Float> supplier) {
        super(font, sampleStorage);
        this.msptSupplier = supplier;
    }

    @Override
    protected void renderAdditionalLinesAndLabels(GuiGraphics guiGraphics, int n, int n2, int n3) {
        float f = (float)TimeUtil.MILLISECONDS_PER_SECOND / this.msptSupplier.get().floatValue();
        this.drawStringWithShade(guiGraphics, String.format(Locale.ROOT, "%.1f TPS", Float.valueOf(f)), n + 1, n3 - 60 + 1);
    }

    @Override
    protected void drawAdditionalDimensions(GuiGraphics guiGraphics, int n, int n2, int n3) {
        long l = this.sampleStorage.get(n3, TpsDebugDimensions.TICK_SERVER_METHOD.ordinal());
        int n4 = this.getSampleHeight(l);
        guiGraphics.fill(n2, n - n4, n2 + 1, n, -6745839);
        long l2 = this.sampleStorage.get(n3, TpsDebugDimensions.SCHEDULED_TASKS.ordinal());
        int n5 = this.getSampleHeight(l2);
        guiGraphics.fill(n2, n - n4 - n5, n2 + 1, n - n4, -4548257);
        long l3 = this.sampleStorage.get(n3) - this.sampleStorage.get(n3, TpsDebugDimensions.IDLE.ordinal()) - l - l2;
        int n6 = this.getSampleHeight(l3);
        guiGraphics.fill(n2, n - n6 - n5 - n4, n2 + 1, n - n5 - n4, -10547572);
    }

    @Override
    protected long getValueForAggregation(int n) {
        return this.sampleStorage.get(n) - this.sampleStorage.get(n, TpsDebugDimensions.IDLE.ordinal());
    }

    @Override
    protected String toDisplayString(double d) {
        return String.format(Locale.ROOT, "%d ms", (int)Math.round(TpsDebugChart.toMilliseconds(d)));
    }

    @Override
    protected int getSampleHeight(double d) {
        return (int)Math.round(TpsDebugChart.toMilliseconds(d) * 60.0 / (double)this.msptSupplier.get().floatValue());
    }

    @Override
    protected int getSampleColor(long l) {
        float f = this.msptSupplier.get().floatValue();
        return this.getSampleColor(TpsDebugChart.toMilliseconds(l), f, -16711936, (double)f * 1.125, -256, (double)f * 1.25, -65536);
    }

    private static double toMilliseconds(double d) {
        return d / 1000000.0;
    }
}

