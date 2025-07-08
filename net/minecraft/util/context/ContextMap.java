/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.google.common.collect.Sets$SetView
 *  javax.annotation.Nullable
 *  org.jetbrains.annotations.Contract
 */
package net.minecraft.util.context;

import com.google.common.collect.Sets;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import javax.annotation.Nullable;
import net.minecraft.util.context.ContextKey;
import net.minecraft.util.context.ContextKeySet;
import org.jetbrains.annotations.Contract;

public class ContextMap {
    private final Map<ContextKey<?>, Object> params;

    ContextMap(Map<ContextKey<?>, Object> map) {
        this.params = map;
    }

    public boolean has(ContextKey<?> contextKey) {
        return this.params.containsKey(contextKey);
    }

    public <T> T getOrThrow(ContextKey<T> contextKey) {
        Object object = this.params.get(contextKey);
        if (object == null) {
            throw new NoSuchElementException(contextKey.name().toString());
        }
        return (T)object;
    }

    @Nullable
    public <T> T getOptional(ContextKey<T> contextKey) {
        return (T)this.params.get(contextKey);
    }

    @Nullable
    @Contract(value="_,!null->!null; _,_->_")
    public <T> T getOrDefault(ContextKey<T> contextKey, @Nullable T t) {
        return (T)this.params.getOrDefault(contextKey, t);
    }

    public static class Builder {
        private final Map<ContextKey<?>, Object> params = new IdentityHashMap();

        public <T> Builder withParameter(ContextKey<T> contextKey, T t) {
            this.params.put(contextKey, t);
            return this;
        }

        public <T> Builder withOptionalParameter(ContextKey<T> contextKey, @Nullable T t) {
            if (t == null) {
                this.params.remove(contextKey);
            } else {
                this.params.put(contextKey, t);
            }
            return this;
        }

        public <T> T getParameter(ContextKey<T> contextKey) {
            Object object = this.params.get(contextKey);
            if (object == null) {
                throw new NoSuchElementException(contextKey.name().toString());
            }
            return (T)object;
        }

        @Nullable
        public <T> T getOptionalParameter(ContextKey<T> contextKey) {
            return (T)this.params.get(contextKey);
        }

        public ContextMap create(ContextKeySet contextKeySet) {
            Sets.SetView setView = Sets.difference(this.params.keySet(), contextKeySet.allowed());
            if (!setView.isEmpty()) {
                throw new IllegalArgumentException("Parameters not allowed in this parameter set: " + String.valueOf(setView));
            }
            Sets.SetView setView2 = Sets.difference(contextKeySet.required(), this.params.keySet());
            if (!setView2.isEmpty()) {
                throw new IllegalArgumentException("Missing required parameters: " + String.valueOf(setView2));
            }
            return new ContextMap(this.params);
        }
    }
}

