/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.profiling.metrics;

import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.util.profiling.ProfileCollector;
import net.minecraft.util.profiling.metrics.MetricSampler;

public interface MetricsSamplerProvider {
    public Set<MetricSampler> samplers(Supplier<ProfileCollector> var1);
}

