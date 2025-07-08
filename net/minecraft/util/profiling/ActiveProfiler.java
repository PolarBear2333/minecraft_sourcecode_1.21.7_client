/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.longs.LongArrayList
 *  it.unimi.dsi.fastutil.longs.LongList
 *  it.unimi.dsi.fastutil.objects.Object2LongMap
 *  it.unimi.dsi.fastutil.objects.Object2LongMaps
 *  it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectArraySet
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.tuple.Pair
 *  org.slf4j.Logger
 */
package net.minecraft.util.profiling;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.profiling.FilledProfileResults;
import net.minecraft.util.profiling.ProfileCollector;
import net.minecraft.util.profiling.ProfileResults;
import net.minecraft.util.profiling.ProfilerPathEntry;
import net.minecraft.util.profiling.metrics.MetricCategory;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;

public class ActiveProfiler
implements ProfileCollector {
    private static final long WARNING_TIME_NANOS = Duration.ofMillis(100L).toNanos();
    private static final Logger LOGGER = LogUtils.getLogger();
    private final List<String> paths = Lists.newArrayList();
    private final LongList startTimes = new LongArrayList();
    private final Map<String, PathEntry> entries = Maps.newHashMap();
    private final IntSupplier getTickTime;
    private final LongSupplier getRealTime;
    private final long startTimeNano;
    private final int startTimeTicks;
    private String path = "";
    private boolean started;
    @Nullable
    private PathEntry currentEntry;
    private final BooleanSupplier suppressWarnings;
    private final Set<Pair<String, MetricCategory>> chartedPaths = new ObjectArraySet();

    public ActiveProfiler(LongSupplier longSupplier, IntSupplier intSupplier, BooleanSupplier booleanSupplier) {
        this.startTimeNano = longSupplier.getAsLong();
        this.getRealTime = longSupplier;
        this.startTimeTicks = intSupplier.getAsInt();
        this.getTickTime = intSupplier;
        this.suppressWarnings = booleanSupplier;
    }

    @Override
    public void startTick() {
        if (this.started) {
            LOGGER.error("Profiler tick already started - missing endTick()?");
            return;
        }
        this.started = true;
        this.path = "";
        this.paths.clear();
        this.push("root");
    }

    @Override
    public void endTick() {
        if (!this.started) {
            LOGGER.error("Profiler tick already ended - missing startTick()?");
            return;
        }
        this.pop();
        this.started = false;
        if (!this.path.isEmpty()) {
            LOGGER.error("Profiler tick ended before path was fully popped (remainder: '{}'). Mismatched push/pop?", LogUtils.defer(() -> ProfileResults.demanglePath(this.path)));
        }
    }

    @Override
    public void push(String string) {
        if (!this.started) {
            LOGGER.error("Cannot push '{}' to profiler if profiler tick hasn't started - missing startTick()?", (Object)string);
            return;
        }
        if (!this.path.isEmpty()) {
            this.path = this.path + "\u001e";
        }
        this.path = this.path + string;
        this.paths.add(this.path);
        this.startTimes.add(Util.getNanos());
        this.currentEntry = null;
    }

    @Override
    public void push(Supplier<String> supplier) {
        this.push(supplier.get());
    }

    @Override
    public void markForCharting(MetricCategory metricCategory) {
        this.chartedPaths.add((Pair<String, MetricCategory>)Pair.of((Object)this.path, (Object)((Object)metricCategory)));
    }

    @Override
    public void pop() {
        if (!this.started) {
            LOGGER.error("Cannot pop from profiler if profiler tick hasn't started - missing startTick()?");
            return;
        }
        if (this.startTimes.isEmpty()) {
            LOGGER.error("Tried to pop one too many times! Mismatched push() and pop()?");
            return;
        }
        long l = Util.getNanos();
        long l2 = this.startTimes.removeLong(this.startTimes.size() - 1);
        this.paths.remove(this.paths.size() - 1);
        long l3 = l - l2;
        PathEntry pathEntry = this.getCurrentEntry();
        pathEntry.accumulatedDuration += l3;
        ++pathEntry.count;
        pathEntry.maxDuration = Math.max(pathEntry.maxDuration, l3);
        pathEntry.minDuration = Math.min(pathEntry.minDuration, l3);
        if (l3 > WARNING_TIME_NANOS && !this.suppressWarnings.getAsBoolean()) {
            LOGGER.warn("Something's taking too long! '{}' took aprox {} ms", LogUtils.defer(() -> ProfileResults.demanglePath(this.path)), LogUtils.defer(() -> (double)l3 / 1000000.0));
        }
        this.path = this.paths.isEmpty() ? "" : this.paths.get(this.paths.size() - 1);
        this.currentEntry = null;
    }

    @Override
    public void popPush(String string) {
        this.pop();
        this.push(string);
    }

    @Override
    public void popPush(Supplier<String> supplier) {
        this.pop();
        this.push(supplier);
    }

    private PathEntry getCurrentEntry() {
        if (this.currentEntry == null) {
            this.currentEntry = this.entries.computeIfAbsent(this.path, string -> new PathEntry());
        }
        return this.currentEntry;
    }

    @Override
    public void incrementCounter(String string, int n) {
        this.getCurrentEntry().counters.addTo((Object)string, (long)n);
    }

    @Override
    public void incrementCounter(Supplier<String> supplier, int n) {
        this.getCurrentEntry().counters.addTo((Object)supplier.get(), (long)n);
    }

    @Override
    public ProfileResults getResults() {
        return new FilledProfileResults(this.entries, this.startTimeNano, this.startTimeTicks, this.getRealTime.getAsLong(), this.getTickTime.getAsInt());
    }

    @Override
    @Nullable
    public PathEntry getEntry(String string) {
        return this.entries.get(string);
    }

    @Override
    public Set<Pair<String, MetricCategory>> getChartedPaths() {
        return this.chartedPaths;
    }

    public static class PathEntry
    implements ProfilerPathEntry {
        long maxDuration = Long.MIN_VALUE;
        long minDuration = Long.MAX_VALUE;
        long accumulatedDuration;
        long count;
        final Object2LongOpenHashMap<String> counters = new Object2LongOpenHashMap();

        @Override
        public long getDuration() {
            return this.accumulatedDuration;
        }

        @Override
        public long getMaxDuration() {
            return this.maxDuration;
        }

        @Override
        public long getCount() {
            return this.count;
        }

        @Override
        public Object2LongMap<String> getCounters() {
            return Object2LongMaps.unmodifiable(this.counters);
        }
    }
}

