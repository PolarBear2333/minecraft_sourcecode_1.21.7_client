/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 */
package net.minecraft.world.level.block.state.properties;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.Property;

public final class EnumProperty<T extends Enum<T>>
extends Property<T> {
    private final List<T> values;
    private final Map<String, T> names;
    private final int[] ordinalToIndex;

    private EnumProperty(String string, Class<T> clazz, List<T> list) {
        super(string, clazz);
        if (list.isEmpty()) {
            throw new IllegalArgumentException("Trying to make empty EnumProperty '" + string + "'");
        }
        this.values = List.copyOf(list);
        ImmutableMap.Builder builder = (ImmutableMap.Builder)clazz.getEnumConstants();
        this.ordinalToIndex = new int[((Enum[])builder).length];
        for (Object object : builder) {
            this.ordinalToIndex[object.ordinal()] = list.indexOf(object);
        }
        ImmutableMap.Builder builder2 = ImmutableMap.builder();
        for (Enum enum_ : list) {
            Object object;
            object = ((StringRepresentable)((Object)enum_)).getSerializedName();
            builder2.put(object, (Object)enum_);
        }
        this.names = builder2.buildOrThrow();
    }

    @Override
    public List<T> getPossibleValues() {
        return this.values;
    }

    @Override
    public Optional<T> getValue(String string) {
        return Optional.ofNullable((Enum)this.names.get(string));
    }

    @Override
    public String getName(T t) {
        return ((StringRepresentable)t).getSerializedName();
    }

    @Override
    public int getInternalIndex(T t) {
        return this.ordinalToIndex[((Enum)t).ordinal()];
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof EnumProperty) {
            EnumProperty enumProperty = (EnumProperty)object;
            if (super.equals(object)) {
                return this.values.equals(enumProperty.values);
            }
        }
        return false;
    }

    @Override
    public int generateHashCode() {
        int n = super.generateHashCode();
        n = 31 * n + this.values.hashCode();
        return n;
    }

    public static <T extends Enum<T>> EnumProperty<T> create(String string, Class<T> clazz) {
        return EnumProperty.create(string, clazz, (T enum_) -> true);
    }

    public static <T extends Enum<T>> EnumProperty<T> create(String string, Class<T> clazz, Predicate<T> predicate) {
        return EnumProperty.create(string, clazz, Arrays.stream((Enum[])clazz.getEnumConstants()).filter(predicate).collect(Collectors.toList()));
    }

    @SafeVarargs
    public static <T extends Enum<T>> EnumProperty<T> create(String string, Class<T> clazz, T ... TArray) {
        return EnumProperty.create(string, clazz, List.of(TArray));
    }

    public static <T extends Enum<T>> EnumProperty<T> create(String string, Class<T> clazz, List<T> list) {
        return new EnumProperty<T>(string, clazz, list);
    }

    @Override
    public /* synthetic */ int getInternalIndex(Comparable comparable) {
        return this.getInternalIndex((Enum)((Object)comparable));
    }

    @Override
    public /* synthetic */ String getName(Comparable comparable) {
        return this.getName((Enum)((Object)comparable));
    }
}

