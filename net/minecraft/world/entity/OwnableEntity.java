/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectArraySet
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity;

import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import javax.annotation.Nullable;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public interface OwnableEntity {
    @Nullable
    public EntityReference<LivingEntity> getOwnerReference();

    public Level level();

    @Nullable
    default public LivingEntity getOwner() {
        return EntityReference.get(this.getOwnerReference(), this.level(), LivingEntity.class);
    }

    @Nullable
    default public LivingEntity getRootOwner() {
        ObjectArraySet objectArraySet = new ObjectArraySet();
        LivingEntity livingEntity = this.getOwner();
        objectArraySet.add(this);
        while (livingEntity instanceof OwnableEntity) {
            OwnableEntity ownableEntity = (OwnableEntity)((Object)livingEntity);
            LivingEntity livingEntity2 = ownableEntity.getOwner();
            if (objectArraySet.contains(livingEntity2)) {
                return null;
            }
            objectArraySet.add(livingEntity);
            livingEntity = ownableEntity.getOwner();
        }
        return livingEntity;
    }
}

