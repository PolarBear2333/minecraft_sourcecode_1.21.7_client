/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMaps
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.entity;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;

public class EntityTickList {
    private Int2ObjectMap<Entity> active = new Int2ObjectLinkedOpenHashMap();
    private Int2ObjectMap<Entity> passive = new Int2ObjectLinkedOpenHashMap();
    @Nullable
    private Int2ObjectMap<Entity> iterated;

    private void ensureActiveIsNotIterated() {
        if (this.iterated == this.active) {
            this.passive.clear();
            for (Int2ObjectMap.Entry entry : Int2ObjectMaps.fastIterable(this.active)) {
                this.passive.put(entry.getIntKey(), (Object)((Entity)entry.getValue()));
            }
            Int2ObjectMap<Entity> int2ObjectMap = this.active;
            this.active = this.passive;
            this.passive = int2ObjectMap;
        }
    }

    public void add(Entity entity) {
        this.ensureActiveIsNotIterated();
        this.active.put(entity.getId(), (Object)entity);
    }

    public void remove(Entity entity) {
        this.ensureActiveIsNotIterated();
        this.active.remove(entity.getId());
    }

    public boolean contains(Entity entity) {
        return this.active.containsKey(entity.getId());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void forEach(Consumer<Entity> consumer) {
        if (this.iterated != null) {
            throw new UnsupportedOperationException("Only one concurrent iteration supported");
        }
        this.iterated = this.active;
        try {
            for (Entity entity : this.active.values()) {
                consumer.accept(entity);
            }
        }
        finally {
            this.iterated = null;
        }
    }
}

