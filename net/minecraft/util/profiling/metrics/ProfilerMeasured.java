/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.profiling.metrics;

import java.util.List;
import net.minecraft.util.profiling.metrics.MetricSampler;

public interface ProfilerMeasured {
    public List<MetricSampler> profiledMetrics();
}

