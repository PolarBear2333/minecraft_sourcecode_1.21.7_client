/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Splitter
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.block.model;

import com.google.common.base.Splitter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;

public class VariantSelector {
    private static final Splitter COMMA_SPLITTER = Splitter.on((char)',');
    private static final Splitter EQUAL_SPLITTER = Splitter.on((char)'=').limit(2);

    public static <O, S extends StateHolder<O, S>> Predicate<StateHolder<O, S>> predicate(StateDefinition<O, S> stateDefinition, String string) {
        HashMap hashMap = new HashMap();
        for (String string2 : COMMA_SPLITTER.split((CharSequence)string)) {
            Iterator iterator = EQUAL_SPLITTER.split((CharSequence)string2).iterator();
            if (!iterator.hasNext()) continue;
            String string3 = (String)iterator.next();
            Property<?> property = stateDefinition.getProperty(string3);
            if (property != null && iterator.hasNext()) {
                String string4 = (String)iterator.next();
                Object obj = VariantSelector.getValueHelper(property, string4);
                if (obj != null) {
                    hashMap.put(property, obj);
                    continue;
                }
                throw new RuntimeException("Unknown value: '" + string4 + "' for blockstate property: '" + string3 + "' " + String.valueOf(property.getPossibleValues()));
            }
            if (string3.isEmpty()) continue;
            throw new RuntimeException("Unknown blockstate property: '" + string3 + "'");
        }
        return stateHolder -> {
            for (Map.Entry entry : hashMap.entrySet()) {
                if (Objects.equals(stateHolder.getValue((Property)entry.getKey()), entry.getValue())) continue;
                return false;
            }
            return true;
        };
    }

    @Nullable
    private static <T extends Comparable<T>> T getValueHelper(Property<T> property, String string) {
        return (T)((Comparable)property.getValue(string).orElse(null));
    }
}

