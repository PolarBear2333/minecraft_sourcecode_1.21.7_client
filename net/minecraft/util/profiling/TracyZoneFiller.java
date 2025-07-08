/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.jtracy.Plot
 *  com.mojang.jtracy.TracyClient
 *  com.mojang.jtracy.Zone
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.util.profiling;

import com.mojang.jtracy.Plot;
import com.mojang.jtracy.TracyClient;
import com.mojang.jtracy.Zone;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.metrics.MetricCategory;
import org.slf4j.Logger;

public class TracyZoneFiller
implements ProfilerFiller {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(Set.of(StackWalker.Option.RETAIN_CLASS_REFERENCE), 5);
    private final List<Zone> activeZones = new ArrayList<Zone>();
    private final Map<String, PlotAndValue> plots = new HashMap<String, PlotAndValue>();
    private final String name = Thread.currentThread().getName();

    @Override
    public void startTick() {
    }

    @Override
    public void endTick() {
        for (PlotAndValue plotAndValue : this.plots.values()) {
            plotAndValue.set(0);
        }
    }

    @Override
    public void push(String string) {
        Optional optional;
        String string2 = "";
        String string3 = "";
        int n = 0;
        if (SharedConstants.IS_RUNNING_IN_IDE && (optional = STACK_WALKER.walk(stream -> stream.filter(stackFrame -> stackFrame.getDeclaringClass() != TracyZoneFiller.class && stackFrame.getDeclaringClass() != ProfilerFiller.CombinedProfileFiller.class).findFirst())).isPresent()) {
            StackWalker.StackFrame stackFrame = (StackWalker.StackFrame)optional.get();
            string2 = stackFrame.getMethodName();
            string3 = stackFrame.getFileName();
            n = stackFrame.getLineNumber();
        }
        optional = TracyClient.beginZone((String)string, (String)string2, (String)string3, (int)n);
        this.activeZones.add((Zone)optional);
    }

    @Override
    public void push(Supplier<String> supplier) {
        this.push(supplier.get());
    }

    @Override
    public void pop() {
        if (this.activeZones.isEmpty()) {
            LOGGER.error("Tried to pop one too many times! Mismatched push() and pop()?");
            return;
        }
        Zone zone = this.activeZones.removeLast();
        zone.close();
    }

    @Override
    public void popPush(String string) {
        this.pop();
        this.push(string);
    }

    @Override
    public void popPush(Supplier<String> supplier) {
        this.pop();
        this.push(supplier.get());
    }

    @Override
    public void markForCharting(MetricCategory metricCategory) {
    }

    @Override
    public void incrementCounter(String string, int n) {
        this.plots.computeIfAbsent(string, string2 -> new PlotAndValue(this.name + " " + string)).add(n);
    }

    @Override
    public void incrementCounter(Supplier<String> supplier, int n) {
        this.incrementCounter(supplier.get(), n);
    }

    private Zone activeZone() {
        return this.activeZones.getLast();
    }

    @Override
    public void addZoneText(String string) {
        this.activeZone().addText(string);
    }

    @Override
    public void addZoneValue(long l) {
        this.activeZone().addValue(l);
    }

    @Override
    public void setZoneColor(int n) {
        this.activeZone().setColor(n);
    }

    static final class PlotAndValue {
        private final Plot plot;
        private int value;

        PlotAndValue(String string) {
            this.plot = TracyClient.createPlot((String)string);
            this.value = 0;
        }

        void set(int n) {
            this.value = n;
            this.plot.setValue((double)n);
        }

        void add(int n) {
            this.set(this.value + n);
        }
    }
}

