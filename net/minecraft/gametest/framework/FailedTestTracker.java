/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 */
package net.minecraft.gametest.framework;

import com.google.common.collect.Sets;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestInstance;

public class FailedTestTracker {
    private static final Set<Holder.Reference<GameTestInstance>> LAST_FAILED_TESTS = Sets.newHashSet();

    public static Stream<Holder.Reference<GameTestInstance>> getLastFailedTests() {
        return LAST_FAILED_TESTS.stream();
    }

    public static void rememberFailedTest(Holder.Reference<GameTestInstance> reference) {
        LAST_FAILED_TESTS.add(reference);
    }

    public static void forgetFailedTests() {
        LAST_FAILED_TESTS.clear();
    }
}

