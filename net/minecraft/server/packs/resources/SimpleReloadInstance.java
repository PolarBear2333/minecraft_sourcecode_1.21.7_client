/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.server.packs.resources;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ProfiledReloadInstance;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Unit;

public class SimpleReloadInstance<S>
implements ReloadInstance {
    private static final int PREPARATION_PROGRESS_WEIGHT = 2;
    private static final int EXTRA_RELOAD_PROGRESS_WEIGHT = 2;
    private static final int LISTENER_PROGRESS_WEIGHT = 1;
    final CompletableFuture<Unit> allPreparations = new CompletableFuture();
    @Nullable
    private CompletableFuture<List<S>> allDone;
    final Set<PreparableReloadListener> preparingListeners;
    private final int listenerCount;
    private final AtomicInteger startedTasks = new AtomicInteger();
    private final AtomicInteger finishedTasks = new AtomicInteger();
    private final AtomicInteger startedReloads = new AtomicInteger();
    private final AtomicInteger finishedReloads = new AtomicInteger();

    public static ReloadInstance of(ResourceManager resourceManager, List<PreparableReloadListener> list, Executor executor, Executor executor2, CompletableFuture<Unit> completableFuture) {
        SimpleReloadInstance<Void> simpleReloadInstance = new SimpleReloadInstance<Void>(list);
        simpleReloadInstance.startTasks(executor, executor2, resourceManager, list, StateFactory.SIMPLE, completableFuture);
        return simpleReloadInstance;
    }

    protected SimpleReloadInstance(List<PreparableReloadListener> list) {
        this.listenerCount = list.size();
        this.preparingListeners = new HashSet<PreparableReloadListener>(list);
    }

    protected void startTasks(Executor executor, Executor executor2, ResourceManager resourceManager, List<PreparableReloadListener> list, StateFactory<S> stateFactory, CompletableFuture<?> completableFuture) {
        this.allDone = this.prepareTasks(executor, executor2, resourceManager, list, stateFactory, completableFuture);
    }

    protected CompletableFuture<List<S>> prepareTasks(Executor executor, Executor executor2, ResourceManager resourceManager, List<PreparableReloadListener> list, StateFactory<S> stateFactory, CompletableFuture<?> completableFuture) {
        Executor executor3 = runnable -> {
            this.startedTasks.incrementAndGet();
            executor.execute(() -> {
                runnable.run();
                this.finishedTasks.incrementAndGet();
            });
        };
        Executor executor4 = runnable -> {
            this.startedReloads.incrementAndGet();
            executor2.execute(() -> {
                runnable.run();
                this.finishedReloads.incrementAndGet();
            });
        };
        this.startedTasks.incrementAndGet();
        completableFuture.thenRun(this.finishedTasks::incrementAndGet);
        CompletableFuture<Object> completableFuture2 = completableFuture;
        ArrayList<CompletableFuture<S>> arrayList = new ArrayList<CompletableFuture<S>>();
        for (PreparableReloadListener preparableReloadListener : list) {
            PreparableReloadListener.PreparationBarrier preparationBarrier = this.createBarrierForListener(preparableReloadListener, completableFuture2, executor2);
            CompletableFuture<S> completableFuture3 = stateFactory.create(preparationBarrier, resourceManager, preparableReloadListener, executor3, executor4);
            arrayList.add(completableFuture3);
            completableFuture2 = completableFuture3;
        }
        return Util.sequenceFailFast(arrayList);
    }

    private PreparableReloadListener.PreparationBarrier createBarrierForListener(final PreparableReloadListener preparableReloadListener, final CompletableFuture<?> completableFuture, final Executor executor) {
        return new PreparableReloadListener.PreparationBarrier(){

            @Override
            public <T> CompletableFuture<T> wait(T t) {
                executor.execute(() -> {
                    SimpleReloadInstance.this.preparingListeners.remove(preparableReloadListener);
                    if (SimpleReloadInstance.this.preparingListeners.isEmpty()) {
                        SimpleReloadInstance.this.allPreparations.complete(Unit.INSTANCE);
                    }
                });
                return SimpleReloadInstance.this.allPreparations.thenCombine((CompletionStage)completableFuture, (unit, object2) -> t);
            }
        };
    }

    @Override
    public CompletableFuture<?> done() {
        return Objects.requireNonNull(this.allDone, "not started");
    }

    @Override
    public float getActualProgress() {
        int n = this.listenerCount - this.preparingListeners.size();
        float f = SimpleReloadInstance.weightProgress(this.finishedTasks.get(), this.finishedReloads.get(), n);
        float f2 = SimpleReloadInstance.weightProgress(this.startedTasks.get(), this.startedReloads.get(), this.listenerCount);
        return f / f2;
    }

    private static int weightProgress(int n, int n2, int n3) {
        return n * 2 + n2 * 2 + n3 * 1;
    }

    public static ReloadInstance create(ResourceManager resourceManager, List<PreparableReloadListener> list, Executor executor, Executor executor2, CompletableFuture<Unit> completableFuture, boolean bl) {
        if (bl) {
            return ProfiledReloadInstance.of(resourceManager, list, executor, executor2, completableFuture);
        }
        return SimpleReloadInstance.of(resourceManager, list, executor, executor2, completableFuture);
    }

    @FunctionalInterface
    protected static interface StateFactory<S> {
        public static final StateFactory<Void> SIMPLE = (preparationBarrier, resourceManager, preparableReloadListener, executor, executor2) -> preparableReloadListener.reload(preparationBarrier, resourceManager, executor, executor2);

        public CompletableFuture<S> create(PreparableReloadListener.PreparationBarrier var1, ResourceManager var2, PreparableReloadListener var3, Executor var4, Executor var5);
    }
}

