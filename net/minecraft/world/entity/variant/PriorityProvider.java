/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.entity.variant;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.util.RandomSource;

public interface PriorityProvider<Context, Condition extends SelectorCondition<Context>> {
    public List<Selector<Context, Condition>> selectors();

    public static <C, T> Stream<T> select(Stream<T> stream, Function<T, PriorityProvider<C, ?>> function, C c) {
        ArrayList arrayList = new ArrayList();
        stream.forEach(object -> {
            PriorityProvider priorityProvider = (PriorityProvider)function.apply(object);
            for (Selector selector : priorityProvider.selectors()) {
                arrayList.add(new UnpackedEntry(object, selector.priority(), (SelectorCondition)DataFixUtils.orElseGet(selector.condition(), SelectorCondition::alwaysTrue)));
            }
        });
        arrayList.sort(UnpackedEntry.HIGHEST_PRIORITY_FIRST);
        Iterator iterator = arrayList.iterator();
        int n = Integer.MIN_VALUE;
        while (iterator.hasNext()) {
            UnpackedEntry unpackedEntry = (UnpackedEntry)iterator.next();
            if (unpackedEntry.priority < n) {
                iterator.remove();
                continue;
            }
            if (unpackedEntry.condition.test(c)) {
                n = unpackedEntry.priority;
                continue;
            }
            iterator.remove();
        }
        return arrayList.stream().map(UnpackedEntry::entry);
    }

    public static <C, T> Optional<T> pick(Stream<T> stream, Function<T, PriorityProvider<C, ?>> function, RandomSource randomSource, C c) {
        List<T> list = PriorityProvider.select(stream, function, c).toList();
        return Util.getRandomSafe(list, randomSource);
    }

    public static <Context, Condition extends SelectorCondition<Context>> List<Selector<Context, Condition>> single(Condition Condition2, int n) {
        return List.of(new Selector(Condition2, n));
    }

    public static <Context, Condition extends SelectorCondition<Context>> List<Selector<Context, Condition>> alwaysTrue(int n) {
        return List.of(new Selector(Optional.empty(), n));
    }

    public static final class UnpackedEntry<C, T>
    extends Record {
        private final T entry;
        final int priority;
        final SelectorCondition<C> condition;
        public static final Comparator<UnpackedEntry<?, ?>> HIGHEST_PRIORITY_FIRST = Comparator.comparingInt(UnpackedEntry::priority).reversed();

        public UnpackedEntry(T t, int n, SelectorCondition<C> selectorCondition) {
            this.entry = t;
            this.priority = n;
            this.condition = selectorCondition;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{UnpackedEntry.class, "entry;priority;condition", "entry", "priority", "condition"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{UnpackedEntry.class, "entry;priority;condition", "entry", "priority", "condition"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{UnpackedEntry.class, "entry;priority;condition", "entry", "priority", "condition"}, this, object);
        }

        public T entry() {
            return this.entry;
        }

        public int priority() {
            return this.priority;
        }

        public SelectorCondition<C> condition() {
            return this.condition;
        }
    }

    @FunctionalInterface
    public static interface SelectorCondition<C>
    extends Predicate<C> {
        public static <C> SelectorCondition<C> alwaysTrue() {
            return object -> true;
        }
    }

    public record Selector<Context, Condition extends SelectorCondition<Context>>(Optional<Condition> condition, int priority) {
        public Selector(Condition Condition2, int n) {
            this(Optional.of(Condition2), n);
        }

        public Selector(int n) {
            this(Optional.empty(), n);
        }

        public static <Context, Condition extends SelectorCondition<Context>> Codec<Selector<Context, Condition>> codec(Codec<Condition> codec) {
            return RecordCodecBuilder.create(instance -> instance.group((App)codec.optionalFieldOf("condition").forGetter(Selector::condition), (App)Codec.INT.fieldOf("priority").forGetter(Selector::priority)).apply((Applicative)instance, Selector::new));
        }
    }
}

