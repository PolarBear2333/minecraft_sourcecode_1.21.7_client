/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.components.debugchart;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ResultField;

public class ProfilerPieChart {
    public static final int RADIUS = 105;
    public static final int PIE_CHART_THICKNESS = 10;
    private static final int MARGIN = 5;
    private final Font font;
    @Nullable
    private ProfileResults profilerPieChartResults;
    private String profilerTreePath = "root";
    private int bottomOffset = 0;

    public ProfilerPieChart(Font font) {
        this.font = font;
    }

    public void setPieChartResults(@Nullable ProfileResults profileResults) {
        this.profilerPieChartResults = profileResults;
    }

    public void setBottomOffset(int n) {
        this.bottomOffset = n;
    }

    public void render(GuiGraphics guiGraphics) {
        if (this.profilerPieChartResults == null) {
            return;
        }
        List<ResultField> list = this.profilerPieChartResults.getTimes(this.profilerTreePath);
        ResultField resultField = list.removeFirst();
        int n = guiGraphics.guiWidth() - 105 - 10;
        int n2 = n - 105;
        int n3 = n + 105;
        int n4 = list.size() * this.font.lineHeight;
        int n5 = guiGraphics.guiHeight() - this.bottomOffset - 5;
        int n6 = n5 - n4;
        int n7 = 62;
        int n8 = n6 - 62 - 5;
        guiGraphics.fill(n2 - 5, n8 - 62 - 5, n3 + 5, n5 + 5, -1873784752);
        guiGraphics.submitProfilerChartRenderState(list, n2, n8 - 62 + 10, n3, n8 + 62);
        DecimalFormat decimalFormat = new DecimalFormat("##0.00");
        decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
        String string = ProfileResults.demanglePath(resultField.name);
        Object object = "";
        if (!"unspecified".equals(string)) {
            object = (String)object + "[0] ";
        }
        object = string.isEmpty() ? (String)object + "ROOT " : (String)object + string + " ";
        int n9 = -1;
        int n10 = n8 - 62;
        guiGraphics.drawString(this.font, (String)object, n2, n10, -1);
        object = decimalFormat.format(resultField.globalPercentage) + "%";
        guiGraphics.drawString(this.font, (String)object, n3 - this.font.width((String)object), n10, -1);
        for (int i = 0; i < list.size(); ++i) {
            ResultField resultField2 = list.get(i);
            StringBuilder stringBuilder = new StringBuilder();
            if ("unspecified".equals(resultField2.name)) {
                stringBuilder.append("[?] ");
            } else {
                stringBuilder.append("[").append(i + 1).append("] ");
            }
            Object object2 = stringBuilder.append(resultField2.name).toString();
            int n11 = n6 + i * this.font.lineHeight;
            guiGraphics.drawString(this.font, (String)object2, n2, n11, resultField2.getColor());
            object2 = decimalFormat.format(resultField2.percentage) + "%";
            guiGraphics.drawString(this.font, (String)object2, n3 - 50 - this.font.width((String)object2), n11, resultField2.getColor());
            object2 = decimalFormat.format(resultField2.globalPercentage) + "%";
            guiGraphics.drawString(this.font, (String)object2, n3 - this.font.width((String)object2), n11, resultField2.getColor());
        }
    }

    public void profilerPieChartKeyPress(int n) {
        if (this.profilerPieChartResults == null) {
            return;
        }
        List<ResultField> list = this.profilerPieChartResults.getTimes(this.profilerTreePath);
        if (list.isEmpty()) {
            return;
        }
        ResultField resultField = list.remove(0);
        if (n == 0) {
            int n2;
            if (!resultField.name.isEmpty() && (n2 = this.profilerTreePath.lastIndexOf(30)) >= 0) {
                this.profilerTreePath = this.profilerTreePath.substring(0, n2);
            }
        } else if (--n < list.size() && !"unspecified".equals(list.get((int)n).name)) {
            if (!this.profilerTreePath.isEmpty()) {
                this.profilerTreePath = this.profilerTreePath + "\u001e";
            }
            this.profilerTreePath = this.profilerTreePath + list.get((int)n).name;
        }
    }
}

