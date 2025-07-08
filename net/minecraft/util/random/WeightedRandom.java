/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.random;

import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;
import net.minecraft.Util;
import net.minecraft.util.RandomSource;

public class WeightedRandom {
    private WeightedRandom() {
    }

    public static <T> int getTotalWeight(List<T> list, ToIntFunction<T> toIntFunction) {
        long l = 0L;
        for (T t : list) {
            l += (long)toIntFunction.applyAsInt(t);
        }
        if (l > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Sum of weights must be <= 2147483647");
        }
        return (int)l;
    }

    public static <T> Optional<T> getRandomItem(RandomSource randomSource, List<T> list, int n, ToIntFunction<T> toIntFunction) {
        if (n < 0) {
            throw Util.pauseInIde(new IllegalArgumentException("Negative total weight in getRandomItem"));
        }
        if (n == 0) {
            return Optional.empty();
        }
        int n2 = randomSource.nextInt(n);
        return WeightedRandom.getWeightedItem(list, n2, toIntFunction);
    }

    public static <T> Optional<T> getWeightedItem(List<T> list, int n, ToIntFunction<T> toIntFunction) {
        for (T t : list) {
            if ((n -= toIntFunction.applyAsInt(t)) >= 0) continue;
            return Optional.of(t);
        }
        return Optional.empty();
    }

    public static <T> Optional<T> getRandomItem(RandomSource randomSource, List<T> list, ToIntFunction<T> toIntFunction) {
        return WeightedRandom.getRandomItem(randomSource, list, WeightedRandom.getTotalWeight(list, toIntFunction), toIntFunction);
    }
}

