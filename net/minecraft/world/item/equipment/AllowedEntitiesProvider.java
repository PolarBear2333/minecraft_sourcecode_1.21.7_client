/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.equipment;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.EntityType;

@FunctionalInterface
public interface AllowedEntitiesProvider {
    public HolderSet<EntityType<?>> get(HolderGetter<EntityType<?>> var1);
}

