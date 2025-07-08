/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Stopwatch
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.server.packs.resources;

import com.google.common.base.Stopwatch;
import com.mojang.logging.LogUtils;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import net.minecraft.Util;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadInstance;
import net.minecraft.util.Unit;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

public class ProfiledReloadInstance
extends SimpleReloadInstance<State> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Stopwatch total = Stopwatch.createUnstarted();

    public static ReloadInstance of(ResourceManager resourceManager2, List<PreparableReloadListener> list, Executor executor, Executor executor4, CompletableFuture<Unit> completableFuture) {
        ProfiledReloadInstance profiledReloadInstance = new ProfiledReloadInstance(list);
        profiledReloadInstance.startTasks(executor, executor4, resourceManager2, list, (preparationBarrier, resourceManager, preparableReloadListener, executor2, executor3) -> {
            AtomicLong atomicLong = new AtomicLong();
            AtomicLong atomicLong2 = new AtomicLong();
            AtomicLong atomicLong3 = new AtomicLong();
            AtomicLong atomicLong4 = new AtomicLong();
            CompletableFuture<Void> completableFuture = preparableReloadListener.reload(preparationBarrier, resourceManager, ProfiledReloadInstance.profiledExecutor(executor2, atomicLong, atomicLong2, preparableReloadListener.getName()), ProfiledReloadInstance.profiledExecutor(executor3, atomicLong3, atomicLong4, preparableReloadListener.getName()));
            return completableFuture.thenApplyAsync(void_ -> {
                LOGGER.debug("Finished reloading {}", (Object)preparableReloadListener.getName());
                return new State(preparableReloadListener.getName(), atomicLong, atomicLong2, atomicLong3, atomicLong4);
            }, executor4);
        }, completableFuture);
        return profiledReloadInstance;
    }

    private ProfiledReloadInstance(List<PreparableReloadListener> list) {
        super(list);
        this.total.start();
    }

    @Override
    protected CompletableFuture<List<State>> prepareTasks(Executor executor, Executor executor2, ResourceManager resourceManager, List<PreparableReloadListener> list, SimpleReloadInstance.StateFactory<State> stateFactory, CompletableFuture<?> completableFuture) {
        return super.prepareTasks(executor, executor2, resourceManager, list, stateFactory, completableFuture).thenApplyAsync(this::finish, executor2);
    }

    private static Executor profiledExecutor(Executor executor, AtomicLong atomicLong, AtomicLong atomicLong2, String string) {
        return runnable -> executor.execute(() -> {
            ProfilerFiller profilerFiller = Profiler.get();
            profilerFiller.push(string);
            long l = Util.getNanos();
            runnable.run();
            atomicLong.addAndGet(Util.getNanos() - l);
            atomicLong2.incrementAndGet();
            profilerFiller.pop();
        });
    }

    private List<State> finish(List<State> list) {
        this.total.stop();
        long l = 0L;
        LOGGER.info("Resource reload finished after {} ms", (Object)this.total.elapsed(TimeUnit.MILLISECONDS));
        for (State state : list) {
            long l2 = TimeUnit.NANOSECONDS.toMillis(state.preparationNanos.get());
            long l3 = state.preparationCount.get();
            long l4 = TimeUnit.NANOSECONDS.toMillis(state.reloadNanos.get());
            long l5 = state.reloadCount.get();
            long l6 = l2 + l4;
            long l7 = l3 + l5;
            String string = state.name;
            LOGGER.info("{} took approximately {} tasks/{} ms ({} tasks/{} ms preparing, {} tasks/{} ms applying)", new Object[]{string, l7, l6, l3, l2, l5, l4});
            l += l4;
        }
        LOGGER.info("Total blocking time: {} ms", (Object)l);
        return list;
    }

    public static final class State
    extends Record {
        final String name;
        final AtomicLong preparationNanos;
        final AtomicLong preparationCount;
        final AtomicLong reloadNanos;
        final AtomicLong reloadCount;

        public State(String string, AtomicLong atomicLong, AtomicLong atomicLong2, AtomicLong atomicLong3, AtomicLong atomicLong4) {
            this.name = string;
            this.preparationNanos = atomicLong;
            this.preparationCount = atomicLong2;
            this.reloadNanos = atomicLong3;
            this.reloadCount = atomicLong4;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{State.class, "name;preparationNanos;preparationCount;reloadNanos;reloadCount", "name", "preparationNanos", "preparationCount", "reloadNanos", "reloadCount"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{State.class, "name;preparationNanos;preparationCount;reloadNanos;reloadCount", "name", "preparationNanos", "preparationCount", "reloadNanos", "reloadCount"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{State.class, "name;preparationNanos;preparationCount;reloadNanos;reloadCount", "name", "preparationNanos", "preparationCount", "reloadNanos", "reloadCount"}, this, object);
        }

        public String name() {
            return this.name;
        }

        public AtomicLong preparationNanos() {
            return this.preparationNanos;
        }

        public AtomicLong preparationCount() {
            return this.preparationCount;
        }

        public AtomicLong reloadNanos() {
            return this.reloadNanos;
        }

        public AtomicLong reloadCount() {
            return this.reloadCount;
        }
    }
}

