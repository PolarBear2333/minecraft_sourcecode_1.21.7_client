/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.advancements.critereon;

import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.predicates.DataComponentPredicate;

public interface SingleComponentItemPredicate<T>
extends DataComponentPredicate {
    @Override
    default public boolean matches(DataComponentGetter dataComponentGetter) {
        T t = dataComponentGetter.get(this.componentType());
        return t != null && this.matches(t);
    }

    public DataComponentType<T> componentType();

    public boolean matches(T var1);
}

