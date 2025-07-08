/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.core.component;

import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;

public interface DataComponentHolder
extends DataComponentGetter {
    public DataComponentMap getComponents();

    @Override
    @Nullable
    default public <T> T get(DataComponentType<? extends T> dataComponentType) {
        return this.getComponents().get(dataComponentType);
    }

    default public <T> Stream<T> getAllOfType(Class<? extends T> clazz) {
        return this.getComponents().stream().map(TypedDataComponent::value).filter(object -> clazz.isAssignableFrom(object.getClass())).map(object -> object);
    }

    @Override
    default public <T> T getOrDefault(DataComponentType<? extends T> dataComponentType, T t) {
        return this.getComponents().getOrDefault(dataComponentType, t);
    }

    default public boolean has(DataComponentType<?> dataComponentType) {
        return this.getComponents().has(dataComponentType);
    }
}

