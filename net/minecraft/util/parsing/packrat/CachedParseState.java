/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.util.parsing.packrat;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.parsing.packrat.Atom;
import net.minecraft.util.parsing.packrat.Control;
import net.minecraft.util.parsing.packrat.ErrorCollector;
import net.minecraft.util.parsing.packrat.NamedRule;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Scope;

public abstract class CachedParseState<S>
implements ParseState<S> {
    private PositionCache[] positionCache = new PositionCache[256];
    private final ErrorCollector<S> errorCollector;
    private final Scope scope = new Scope();
    private SimpleControl[] controlCache = new SimpleControl[16];
    private int nextControlToReturn;
    private final Silent silent = new Silent();

    protected CachedParseState(ErrorCollector<S> errorCollector) {
        this.errorCollector = errorCollector;
    }

    @Override
    public Scope scope() {
        return this.scope;
    }

    @Override
    public ErrorCollector<S> errorCollector() {
        return this.errorCollector;
    }

    @Override
    @Nullable
    public <T> T parse(NamedRule<S, T> namedRule) {
        CacheEntry<Object> cacheEntry;
        CacheEntry cacheEntry2;
        int n = this.mark();
        PositionCache positionCache = this.getCacheForPosition(n);
        int n2 = positionCache.findKeyIndex(namedRule.name());
        if (n2 != -1) {
            cacheEntry2 = positionCache.getValue(n2);
            if (cacheEntry2 != null) {
                if (cacheEntry2 == CacheEntry.NEGATIVE) {
                    return null;
                }
                this.restore(cacheEntry2.markAfterParse);
                return cacheEntry2.value;
            }
        } else {
            n2 = positionCache.allocateNewEntry(namedRule.name());
        }
        if ((cacheEntry2 = namedRule.value().parse(this)) == null) {
            cacheEntry = CacheEntry.negativeEntry();
        } else {
            int n3 = this.mark();
            cacheEntry = new CacheEntry(cacheEntry2, n3);
        }
        positionCache.setValue(n2, cacheEntry);
        return (T)cacheEntry2;
    }

    private PositionCache getCacheForPosition(int n) {
        PositionCache positionCache;
        int n2 = this.positionCache.length;
        if (n >= n2) {
            int n3 = Util.growByHalf(n2, n + 1);
            PositionCache[] positionCacheArray = new PositionCache[n3];
            System.arraycopy(this.positionCache, 0, positionCacheArray, 0, n2);
            this.positionCache = positionCacheArray;
        }
        if ((positionCache = this.positionCache[n]) == null) {
            this.positionCache[n] = positionCache = new PositionCache();
        }
        return positionCache;
    }

    @Override
    public Control acquireControl() {
        Object object;
        int n;
        int n2 = this.controlCache.length;
        if (this.nextControlToReturn >= n2) {
            n = Util.growByHalf(n2, this.nextControlToReturn + 1);
            object = new SimpleControl[n];
            System.arraycopy(this.controlCache, 0, object, 0, n2);
            this.controlCache = object;
        }
        if ((object = this.controlCache[n = this.nextControlToReturn++]) == null) {
            this.controlCache[n] = object = new SimpleControl();
        } else {
            ((SimpleControl)object).reset();
        }
        return object;
    }

    @Override
    public void releaseControl() {
        --this.nextControlToReturn;
    }

    @Override
    public ParseState<S> silent() {
        return this.silent;
    }

    static class PositionCache {
        public static final int ENTRY_STRIDE = 2;
        private static final int NOT_FOUND = -1;
        private Object[] atomCache = new Object[16];
        private int nextKey;

        PositionCache() {
        }

        public int findKeyIndex(Atom<?> atom) {
            for (int i = 0; i < this.nextKey; i += 2) {
                if (this.atomCache[i] != atom) continue;
                return i;
            }
            return -1;
        }

        public int allocateNewEntry(Atom<?> atom) {
            int n = this.nextKey;
            this.nextKey += 2;
            int n2 = n + 1;
            int n3 = this.atomCache.length;
            if (n2 >= n3) {
                int n4 = Util.growByHalf(n3, n2 + 1);
                Object[] objectArray = new Object[n4];
                System.arraycopy(this.atomCache, 0, objectArray, 0, n3);
                this.atomCache = objectArray;
            }
            this.atomCache[n] = atom;
            return n;
        }

        @Nullable
        public <T> CacheEntry<T> getValue(int n) {
            return (CacheEntry)this.atomCache[n + 1];
        }

        public void setValue(int n, CacheEntry<?> cacheEntry) {
            this.atomCache[n + 1] = cacheEntry;
        }
    }

    static class SimpleControl
    implements Control {
        private boolean hasCut;

        SimpleControl() {
        }

        @Override
        public void cut() {
            this.hasCut = true;
        }

        @Override
        public boolean hasCut() {
            return this.hasCut;
        }

        public void reset() {
            this.hasCut = false;
        }
    }

    class Silent
    implements ParseState<S> {
        private final ErrorCollector<S> silentCollector = new ErrorCollector.Nop();

        Silent() {
        }

        @Override
        public ErrorCollector<S> errorCollector() {
            return this.silentCollector;
        }

        @Override
        public Scope scope() {
            return CachedParseState.this.scope();
        }

        @Override
        @Nullable
        public <T> T parse(NamedRule<S, T> namedRule) {
            return CachedParseState.this.parse(namedRule);
        }

        @Override
        public S input() {
            return CachedParseState.this.input();
        }

        @Override
        public int mark() {
            return CachedParseState.this.mark();
        }

        @Override
        public void restore(int n) {
            CachedParseState.this.restore(n);
        }

        @Override
        public Control acquireControl() {
            return CachedParseState.this.acquireControl();
        }

        @Override
        public void releaseControl() {
            CachedParseState.this.releaseControl();
        }

        @Override
        public ParseState<S> silent() {
            return this;
        }
    }

    static final class CacheEntry<T>
    extends Record {
        @Nullable
        final T value;
        final int markAfterParse;
        public static final CacheEntry<?> NEGATIVE = new CacheEntry<Object>(null, -1);

        CacheEntry(@Nullable T t, int n) {
            this.value = t;
            this.markAfterParse = n;
        }

        public static <T> CacheEntry<T> negativeEntry() {
            return NEGATIVE;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{CacheEntry.class, "value;markAfterParse", "value", "markAfterParse"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CacheEntry.class, "value;markAfterParse", "value", "markAfterParse"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CacheEntry.class, "value;markAfterParse", "value", "markAfterParse"}, this, object);
        }

        @Nullable
        public T value() {
            return this.value;
        }

        public int markAfterParse() {
            return this.markAfterParse;
        }
    }
}

