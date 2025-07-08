/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.packs.resources;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.server.packs.resources.ResourceManager;

@FunctionalInterface
public interface PreparableReloadListener {
    public CompletableFuture<Void> reload(PreparationBarrier var1, ResourceManager var2, Executor var3, Executor var4);

    default public String getName() {
        return this.getClass().getSimpleName();
    }

    @FunctionalInterface
    public static interface PreparationBarrier {
        public <T> CompletableFuture<T> wait(T var1);
    }
}

