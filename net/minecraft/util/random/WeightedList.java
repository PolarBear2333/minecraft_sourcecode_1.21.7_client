/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.google.common.collect.Lists
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 */
package net.minecraft.util.random;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedRandom;

public final class WeightedList<E> {
    private static final int FLAT_THRESHOLD = 64;
    private final int totalWeight;
    private final List<Weighted<E>> items;
    @Nullable
    private final Selector<E> selector;

    WeightedList(List<? extends Weighted<E>> list) {
        this.items = List.copyOf(list);
        this.totalWeight = WeightedRandom.getTotalWeight(list, Weighted::weight);
        this.selector = this.totalWeight == 0 ? null : (this.totalWeight < 64 ? new Flat<E>(this.items, this.totalWeight) : new Compact<E>(this.items));
    }

    public static <E> WeightedList<E> of() {
        return new WeightedList<E>(List.of());
    }

    public static <E> WeightedList<E> of(E e) {
        return new WeightedList<E>(List.of(new Weighted<E>(e, 1)));
    }

    @SafeVarargs
    public static <E> WeightedList<E> of(Weighted<E> ... weightedArray) {
        return new WeightedList<E>(List.of(weightedArray));
    }

    public static <E> WeightedList<E> of(List<Weighted<E>> list) {
        return new WeightedList<E>(list);
    }

    public static <E> Builder<E> builder() {
        return new Builder();
    }

    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    public <T> WeightedList<T> map(Function<E, T> function) {
        return new WeightedList<E>(Lists.transform(this.items, weighted -> weighted.map(function)));
    }

    public Optional<E> getRandom(RandomSource randomSource) {
        if (this.selector == null) {
            return Optional.empty();
        }
        int n = randomSource.nextInt(this.totalWeight);
        return Optional.of(this.selector.get(n));
    }

    public E getRandomOrThrow(RandomSource randomSource) {
        if (this.selector == null) {
            throw new IllegalStateException("Weighted list has no elements");
        }
        int n = randomSource.nextInt(this.totalWeight);
        return this.selector.get(n);
    }

    public List<Weighted<E>> unwrap() {
        return this.items;
    }

    public static <E> Codec<WeightedList<E>> codec(Codec<E> codec) {
        return Weighted.codec(codec).listOf().xmap(WeightedList::of, WeightedList::unwrap);
    }

    public static <E> Codec<WeightedList<E>> codec(MapCodec<E> mapCodec) {
        return Weighted.codec(mapCodec).listOf().xmap(WeightedList::of, WeightedList::unwrap);
    }

    public static <E> Codec<WeightedList<E>> nonEmptyCodec(Codec<E> codec) {
        return ExtraCodecs.nonEmptyList(Weighted.codec(codec).listOf()).xmap(WeightedList::of, WeightedList::unwrap);
    }

    public static <E> Codec<WeightedList<E>> nonEmptyCodec(MapCodec<E> mapCodec) {
        return ExtraCodecs.nonEmptyList(Weighted.codec(mapCodec).listOf()).xmap(WeightedList::of, WeightedList::unwrap);
    }

    public boolean contains(E e) {
        for (Weighted<E> weighted : this.items) {
            if (!weighted.value().equals(e)) continue;
            return true;
        }
        return false;
    }

    public boolean equals(@Nullable Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof WeightedList) {
            WeightedList weightedList = (WeightedList)object;
            return this.totalWeight == weightedList.totalWeight && Objects.equals(this.items, weightedList.items);
        }
        return false;
    }

    public int hashCode() {
        int n = this.totalWeight;
        n = 31 * n + this.items.hashCode();
        return n;
    }

    static interface Selector<E> {
        public E get(int var1);
    }

    static class Flat<E>
    implements Selector<E> {
        private final Object[] entries;

        Flat(List<Weighted<E>> list, int n) {
            this.entries = new Object[n];
            int n2 = 0;
            for (Weighted<E> weighted : list) {
                int n3 = weighted.weight();
                Arrays.fill(this.entries, n2, n2 + n3, weighted.value());
                n2 += n3;
            }
        }

        @Override
        public E get(int n) {
            return (E)this.entries[n];
        }
    }

    static class Compact<E>
    implements Selector<E> {
        private final Weighted<?>[] entries;

        Compact(List<Weighted<E>> list) {
            this.entries = (Weighted[])list.toArray(Weighted[]::new);
        }

        @Override
        public E get(int n) {
            for (Weighted<?> weighted : this.entries) {
                if ((n -= weighted.weight()) >= 0) continue;
                return (E)weighted.value();
            }
            throw new IllegalStateException(n + " exceeded total weight");
        }
    }

    public static class Builder<E> {
        private final ImmutableList.Builder<Weighted<E>> result = ImmutableList.builder();

        public Builder<E> add(E e) {
            return this.add(e, 1);
        }

        public Builder<E> add(E e, int n) {
            this.result.add(new Weighted<E>(e, n));
            return this;
        }

        public WeightedList<E> build() {
            return new WeightedList(this.result.build());
        }
    }
}

