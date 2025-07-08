/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Joiner
 *  com.google.common.base.Splitter
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer.block.model.multipart;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.Util;
import net.minecraft.client.renderer.block.model.multipart.Condition;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.slf4j.Logger;

public record KeyValueCondition(Map<String, Terms> tests) implements Condition
{
    static final Logger LOGGER = LogUtils.getLogger();
    public static final Codec<KeyValueCondition> CODEC = ExtraCodecs.nonEmptyMap(Codec.unboundedMap((Codec)Codec.STRING, Terms.CODEC)).xmap(KeyValueCondition::new, KeyValueCondition::tests);

    @Override
    public <O, S extends StateHolder<O, S>> Predicate<S> instantiate(StateDefinition<O, S> stateDefinition) {
        ArrayList arrayList = new ArrayList(this.tests.size());
        this.tests.forEach((string, terms) -> arrayList.add(KeyValueCondition.instantiate(stateDefinition, string, terms)));
        return Util.allOf(arrayList);
    }

    private static <O, S extends StateHolder<O, S>> Predicate<S> instantiate(StateDefinition<O, S> stateDefinition, String string, Terms terms) {
        Property<?> property = stateDefinition.getProperty(string);
        if (property == null) {
            throw new IllegalArgumentException(String.format(Locale.ROOT, "Unknown property '%s' on '%s'", string, stateDefinition.getOwner()));
        }
        return terms.instantiate(stateDefinition.getOwner(), property);
    }

    public record Terms(List<Term> entries) {
        private static final char SEPARATOR = '|';
        private static final Joiner JOINER = Joiner.on((char)'|');
        private static final Splitter SPLITTER = Splitter.on((char)'|');
        private static final Codec<String> LEGACY_REPRESENTATION_CODEC = Codec.either((Codec)Codec.INT, (Codec)Codec.BOOL).flatComapMap(either -> (String)either.map(String::valueOf, String::valueOf), string -> DataResult.error(() -> "This codec can't be used for encoding"));
        public static final Codec<Terms> CODEC = Codec.withAlternative((Codec)Codec.STRING, LEGACY_REPRESENTATION_CODEC).comapFlatMap(Terms::parse, Terms::toString);

        public Terms {
            if (list.isEmpty()) {
                throw new IllegalArgumentException("Empty value for property");
            }
        }

        public static DataResult<Terms> parse(String string) {
            List<Term> list = SPLITTER.splitToStream((CharSequence)string).map(Term::parse).toList();
            if (list.isEmpty()) {
                return DataResult.error(() -> "Empty value for property");
            }
            for (Term term : list) {
                if (!term.value.isEmpty()) continue;
                return DataResult.error(() -> "Empty term in value '" + string + "'");
            }
            return DataResult.success((Object)new Terms(list));
        }

        @Override
        public String toString() {
            return JOINER.join(this.entries);
        }

        public <O, S extends StateHolder<O, S>, T extends Comparable<T>> Predicate<S> instantiate(O o, Property<T> property) {
            Object object;
            Object object2;
            boolean bl;
            Predicate predicate = Util.anyOf(Lists.transform(this.entries, term -> this.instantiate(o, property, (Term)term)));
            ArrayList<T> arrayList = new ArrayList<T>(property.getPossibleValues());
            int n = arrayList.size();
            arrayList.removeIf(predicate.negate());
            int n2 = arrayList.size();
            if (n2 == 0) {
                LOGGER.warn("Condition {} for property {} on {} is always false", new Object[]{this, property.getName(), o});
                return stateHolder -> false;
            }
            int n3 = n - n2;
            if (n3 == 0) {
                LOGGER.warn("Condition {} for property {} on {} is always true", new Object[]{this, property.getName(), o});
                return stateHolder -> true;
            }
            if (n2 <= n3) {
                bl = false;
                object2 = arrayList;
            } else {
                bl = true;
                object = new ArrayList<T>(property.getPossibleValues());
                object.removeIf(predicate);
                object2 = object;
            }
            if (object2.size() == 1) {
                object = (Comparable)object2.getFirst();
                return arg_0 -> Terms.lambda$instantiate$8(property, (Comparable)object, bl, arg_0);
            }
            return stateHolder -> {
                Object t = stateHolder.getValue(property);
                return object2.contains(t) ^ bl;
            };
        }

        private <T extends Comparable<T>> T getValueOrThrow(Object object, Property<T> property, String string) {
            Optional<T> optional = property.getValue(string);
            if (optional.isEmpty()) {
                throw new RuntimeException(String.format(Locale.ROOT, "Unknown value '%s' for property '%s' on '%s' in '%s'", string, property, object, this));
            }
            return (T)((Comparable)optional.get());
        }

        private <T extends Comparable<T>> Predicate<T> instantiate(Object object, Property<T> property, Term term) {
            Object t = this.getValueOrThrow(object, property, term.value);
            if (term.negated) {
                return comparable2 -> !comparable2.equals(t);
            }
            return comparable2 -> comparable2.equals(t);
        }

        private static /* synthetic */ boolean lambda$instantiate$8(Property property, Comparable comparable, boolean bl, StateHolder stateHolder) {
            Object t = stateHolder.getValue(property);
            return comparable.equals(t) ^ bl;
        }
    }

    public static final class Term
    extends Record {
        final String value;
        final boolean negated;
        private static final String NEGATE = "!";

        public Term(String string, boolean bl) {
            if (string.isEmpty()) {
                throw new IllegalArgumentException("Empty term");
            }
            this.value = string;
            this.negated = bl;
        }

        public static Term parse(String string) {
            if (string.startsWith(NEGATE)) {
                return new Term(string.substring(1), true);
            }
            return new Term(string, false);
        }

        @Override
        public String toString() {
            return this.negated ? NEGATE + this.value : this.value;
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Term.class, "value;negated", "value", "negated"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Term.class, "value;negated", "value", "negated"}, this, object);
        }

        public String value() {
            return this.value;
        }

        public boolean negated() {
            return this.negated;
        }
    }
}

