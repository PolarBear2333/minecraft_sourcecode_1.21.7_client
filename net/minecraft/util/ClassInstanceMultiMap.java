/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Iterators
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 */
package net.minecraft.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.Util;

public class ClassInstanceMultiMap<T>
extends AbstractCollection<T> {
    private final Map<Class<?>, List<T>> byClass = Maps.newHashMap();
    private final Class<T> baseClass;
    private final List<T> allInstances = Lists.newArrayList();

    public ClassInstanceMultiMap(Class<T> clazz) {
        this.baseClass = clazz;
        this.byClass.put(clazz, this.allInstances);
    }

    @Override
    public boolean add(T t) {
        boolean bl = false;
        for (Map.Entry<Class<?>, List<T>> entry : this.byClass.entrySet()) {
            if (!entry.getKey().isInstance(t)) continue;
            bl |= entry.getValue().add(t);
        }
        return bl;
    }

    @Override
    public boolean remove(Object object) {
        boolean bl = false;
        for (Map.Entry<Class<?>, List<T>> entry : this.byClass.entrySet()) {
            if (!entry.getKey().isInstance(object)) continue;
            List<T> list = entry.getValue();
            bl |= list.remove(object);
        }
        return bl;
    }

    @Override
    public boolean contains(Object object) {
        return this.find(object.getClass()).contains(object);
    }

    public <S> Collection<S> find(Class<S> clazz2) {
        if (!this.baseClass.isAssignableFrom(clazz2)) {
            throw new IllegalArgumentException("Don't know how to search for " + String.valueOf(clazz2));
        }
        List list = this.byClass.computeIfAbsent(clazz2, clazz -> this.allInstances.stream().filter(clazz::isInstance).collect(Util.toMutableList()));
        return Collections.unmodifiableCollection(list);
    }

    @Override
    public Iterator<T> iterator() {
        if (this.allInstances.isEmpty()) {
            return Collections.emptyIterator();
        }
        return Iterators.unmodifiableIterator(this.allInstances.iterator());
    }

    public List<T> getAllInstances() {
        return ImmutableList.copyOf(this.allInstances);
    }

    @Override
    public int size() {
        return this.allInstances.size();
    }
}

