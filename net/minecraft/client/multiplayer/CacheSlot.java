/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.multiplayer;

import java.util.function.Function;
import javax.annotation.Nullable;

public class CacheSlot<C extends Cleaner<C>, D> {
    private final Function<C, D> operation;
    @Nullable
    private C context;
    @Nullable
    private D value;

    public CacheSlot(Function<C, D> function) {
        this.operation = function;
    }

    public D compute(C c) {
        if (c == this.context && this.value != null) {
            return this.value;
        }
        D d = this.operation.apply(c);
        this.value = d;
        this.context = c;
        c.registerForCleaning(this);
        return d;
    }

    public void clear() {
        this.value = null;
        this.context = null;
    }

    @FunctionalInterface
    public static interface Cleaner<C extends Cleaner<C>> {
        public void registerForCleaning(CacheSlot<C, ?> var1);
    }
}

