/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntOpenHashSet
 *  it.unimi.dsi.fastutil.ints.IntSet
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.font;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.Arrays;
import java.util.function.IntFunction;
import javax.annotation.Nullable;

public class CodepointMap<T> {
    private static final int BLOCK_BITS = 8;
    private static final int BLOCK_SIZE = 256;
    private static final int IN_BLOCK_MASK = 255;
    private static final int MAX_BLOCK = 4351;
    private static final int BLOCK_COUNT = 4352;
    private final T[] empty;
    private final T[][] blockMap;
    private final IntFunction<T[]> blockConstructor;

    public CodepointMap(IntFunction<T[]> intFunction, IntFunction<T[][]> intFunction2) {
        this.empty = intFunction.apply(256);
        this.blockMap = intFunction2.apply(4352);
        Arrays.fill(this.blockMap, this.empty);
        this.blockConstructor = intFunction;
    }

    public void clear() {
        Arrays.fill(this.blockMap, this.empty);
    }

    @Nullable
    public T get(int n) {
        int n2 = n >> 8;
        int n3 = n & 0xFF;
        return this.blockMap[n2][n3];
    }

    @Nullable
    public T put(int n, T t) {
        int n2 = n >> 8;
        int n3 = n & 0xFF;
        T[] TArray = this.blockMap[n2];
        if (TArray == this.empty) {
            TArray = this.blockConstructor.apply(256);
            this.blockMap[n2] = TArray;
            TArray[n3] = t;
            return null;
        }
        T t2 = TArray[n3];
        TArray[n3] = t;
        return t2;
    }

    public T computeIfAbsent(int n, IntFunction<T> intFunction) {
        int n2 = n >> 8;
        T[] TArray = this.blockMap[n2];
        int n3 = n & 0xFF;
        T t = TArray[n3];
        if (t != null) {
            return t;
        }
        if (TArray == this.empty) {
            TArray = this.blockConstructor.apply(256);
            this.blockMap[n2] = TArray;
        }
        T t2 = intFunction.apply(n);
        TArray[n3] = t2;
        return t2;
    }

    @Nullable
    public T remove(int n) {
        int n2 = n >> 8;
        int n3 = n & 0xFF;
        T[] TArray = this.blockMap[n2];
        if (TArray == this.empty) {
            return null;
        }
        T t = TArray[n3];
        TArray[n3] = null;
        return t;
    }

    public void forEach(Output<T> output) {
        for (int i = 0; i < this.blockMap.length; ++i) {
            T[] TArray = this.blockMap[i];
            if (TArray == this.empty) continue;
            for (int j = 0; j < TArray.length; ++j) {
                T t = TArray[j];
                if (t == null) continue;
                int n = i << 8 | j;
                output.accept(n, t);
            }
        }
    }

    public IntSet keySet() {
        IntOpenHashSet intOpenHashSet = new IntOpenHashSet();
        this.forEach((n, object) -> intOpenHashSet.add(n));
        return intOpenHashSet;
    }

    @FunctionalInterface
    public static interface Output<T> {
        public void accept(int var1, T var2);
    }
}

