/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block.state;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.state.properties.Property;

public abstract class StateHolder<O, S> {
    public static final String NAME_TAG = "Name";
    public static final String PROPERTIES_TAG = "Properties";
    private static final Function<Map.Entry<Property<?>, Comparable<?>>, String> PROPERTY_ENTRY_TO_STRING_FUNCTION = new Function<Map.Entry<Property<?>, Comparable<?>>, String>(){

        @Override
        public String apply(@Nullable Map.Entry<Property<?>, Comparable<?>> entry) {
            if (entry == null) {
                return "<NULL>";
            }
            Property<?> property = entry.getKey();
            return property.getName() + "=" + this.getName(property, entry.getValue());
        }

        private <T extends Comparable<T>> String getName(Property<T> property, Comparable<?> comparable) {
            return property.getName(comparable);
        }

        @Override
        public /* synthetic */ Object apply(@Nullable Object object) {
            return this.apply((Map.Entry)object);
        }
    };
    protected final O owner;
    private final Reference2ObjectArrayMap<Property<?>, Comparable<?>> values;
    private Map<Property<?>, S[]> neighbours;
    protected final MapCodec<S> propertiesCodec;

    protected StateHolder(O o, Reference2ObjectArrayMap<Property<?>, Comparable<?>> reference2ObjectArrayMap, MapCodec<S> mapCodec) {
        this.owner = o;
        this.values = reference2ObjectArrayMap;
        this.propertiesCodec = mapCodec;
    }

    public <T extends Comparable<T>> S cycle(Property<T> property) {
        return this.setValue(property, (Comparable)StateHolder.findNextInCollection(property.getPossibleValues(), this.getValue(property)));
    }

    protected static <T> T findNextInCollection(List<T> list, T t) {
        int n = list.indexOf(t) + 1;
        return n == list.size() ? list.getFirst() : list.get(n);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.owner);
        if (!this.getValues().isEmpty()) {
            stringBuilder.append('[');
            stringBuilder.append(this.getValues().entrySet().stream().map(PROPERTY_ENTRY_TO_STRING_FUNCTION).collect(Collectors.joining(",")));
            stringBuilder.append(']');
        }
        return stringBuilder.toString();
    }

    public final boolean equals(Object object) {
        return super.equals(object);
    }

    public int hashCode() {
        return super.hashCode();
    }

    public Collection<Property<?>> getProperties() {
        return Collections.unmodifiableCollection(this.values.keySet());
    }

    public boolean hasProperty(Property<?> property) {
        return this.values.containsKey(property);
    }

    public <T extends Comparable<T>> T getValue(Property<T> property) {
        Comparable comparable = (Comparable)this.values.get(property);
        if (comparable == null) {
            throw new IllegalArgumentException("Cannot get property " + String.valueOf(property) + " as it does not exist in " + String.valueOf(this.owner));
        }
        return (T)((Comparable)property.getValueClass().cast(comparable));
    }

    public <T extends Comparable<T>> Optional<T> getOptionalValue(Property<T> property) {
        return Optional.ofNullable(this.getNullableValue(property));
    }

    public <T extends Comparable<T>> T getValueOrElse(Property<T> property, T t) {
        return (T)((Comparable)Objects.requireNonNullElse(this.getNullableValue(property), t));
    }

    @Nullable
    private <T extends Comparable<T>> T getNullableValue(Property<T> property) {
        Comparable comparable = (Comparable)this.values.get(property);
        if (comparable == null) {
            return null;
        }
        return (T)((Comparable)property.getValueClass().cast(comparable));
    }

    public <T extends Comparable<T>, V extends T> S setValue(Property<T> property, V v) {
        Comparable comparable = (Comparable)this.values.get(property);
        if (comparable == null) {
            throw new IllegalArgumentException("Cannot set property " + String.valueOf(property) + " as it does not exist in " + String.valueOf(this.owner));
        }
        return this.setValueInternal(property, v, comparable);
    }

    public <T extends Comparable<T>, V extends T> S trySetValue(Property<T> property, V v) {
        Comparable comparable = (Comparable)this.values.get(property);
        if (comparable == null) {
            return (S)this;
        }
        return this.setValueInternal(property, v, comparable);
    }

    private <T extends Comparable<T>, V extends T> S setValueInternal(Property<T> property, V v, Comparable<?> comparable) {
        if (comparable.equals(v)) {
            return (S)this;
        }
        int n = property.getInternalIndex(v);
        if (n < 0) {
            throw new IllegalArgumentException("Cannot set property " + String.valueOf(property) + " to " + String.valueOf(v) + " on " + String.valueOf(this.owner) + ", it is not an allowed value");
        }
        return this.neighbours.get(property)[n];
    }

    public void populateNeighbours(Map<Map<Property<?>, Comparable<?>>, S> map) {
        if (this.neighbours != null) {
            throw new IllegalStateException();
        }
        Reference2ObjectArrayMap reference2ObjectArrayMap = new Reference2ObjectArrayMap(this.values.size());
        for (Map.Entry entry : this.values.entrySet()) {
            Property property = (Property)entry.getKey();
            reference2ObjectArrayMap.put(property, property.getPossibleValues().stream().map(comparable -> map.get(this.makeNeighbourValues(property, (Comparable<?>)comparable))).toArray());
        }
        this.neighbours = reference2ObjectArrayMap;
    }

    private Map<Property<?>, Comparable<?>> makeNeighbourValues(Property<?> property, Comparable<?> comparable) {
        Reference2ObjectArrayMap reference2ObjectArrayMap = new Reference2ObjectArrayMap(this.values);
        reference2ObjectArrayMap.put(property, comparable);
        return reference2ObjectArrayMap;
    }

    public Map<Property<?>, Comparable<?>> getValues() {
        return this.values;
    }

    protected static <O, S extends StateHolder<O, S>> Codec<S> codec(Codec<O> codec, Function<O, S> function) {
        return codec.dispatch(NAME_TAG, stateHolder -> stateHolder.owner, object -> {
            StateHolder stateHolder = (StateHolder)function.apply(object);
            if (stateHolder.getValues().isEmpty()) {
                return MapCodec.unit((Object)stateHolder);
            }
            return stateHolder.propertiesCodec.codec().lenientOptionalFieldOf(PROPERTIES_TAG).xmap(optional -> optional.orElse(stateHolder), Optional::of);
        });
    }
}

