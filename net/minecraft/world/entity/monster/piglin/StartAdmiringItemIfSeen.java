/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.monster.piglin;

import com.mojang.datafixers.kinds.Applicative;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;

public class StartAdmiringItemIfSeen {
    public static BehaviorControl<LivingEntity> create(int n) {
        return BehaviorBuilder.create(instance -> instance.group(instance.present(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM), instance.absent(MemoryModuleType.ADMIRING_ITEM), instance.absent(MemoryModuleType.ADMIRING_DISABLED), instance.absent(MemoryModuleType.DISABLE_WALK_TO_ADMIRE_ITEM)).apply((Applicative)instance, (memoryAccessor, memoryAccessor2, memoryAccessor3, memoryAccessor4) -> (serverLevel, livingEntity, l) -> {
            ItemEntity itemEntity = (ItemEntity)instance.get(memoryAccessor);
            if (!PiglinAi.isLovedItem(itemEntity.getItem())) {
                return false;
            }
            memoryAccessor2.setWithExpiry(true, n);
            return true;
        }));
    }
}

