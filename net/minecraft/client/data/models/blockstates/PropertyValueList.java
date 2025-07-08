/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 */
package net.minecraft.client.data.models.blockstates;

import com.google.common.collect.ImmutableList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.Util;
import net.minecraft.world.level.block.state.properties.Property;

public record PropertyValueList(List<Property.Value<?>> values) {
    public static final PropertyValueList EMPTY = new PropertyValueList(List.of());
    private static final Comparator<Property.Value<?>> COMPARE_BY_NAME = Comparator.comparing(value -> value.property().getName());

    public PropertyValueList extend(Property.Value<?> value) {
        return new PropertyValueList(Util.copyAndAdd(this.values, value));
    }

    public PropertyValueList extend(PropertyValueList propertyValueList) {
        return new PropertyValueList((List<Property.Value<?>>)ImmutableList.builder().addAll(this.values).addAll(propertyValueList.values).build());
    }

    public static PropertyValueList of(Property.Value<?> ... valueArray) {
        return new PropertyValueList(List.of(valueArray));
    }

    public String getKey() {
        return this.values.stream().sorted(COMPARE_BY_NAME).map(Property.Value::toString).collect(Collectors.joining(","));
    }

    @Override
    public String toString() {
        return this.getKey();
    }
}

