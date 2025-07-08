/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.monster.piglin;

import com.mojang.datafixers.kinds.Applicative;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.piglin.PiglinAi;

public class RememberIfHoglinWasKilled {
    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create(instance -> instance.group(instance.present(MemoryModuleType.ATTACK_TARGET), instance.registered(MemoryModuleType.HUNTED_RECENTLY)).apply((Applicative)instance, (memoryAccessor, memoryAccessor2) -> (serverLevel, livingEntity, l) -> {
            LivingEntity livingEntity2 = (LivingEntity)instance.get(memoryAccessor);
            if (livingEntity2.getType() == EntityType.HOGLIN && livingEntity2.isDeadOrDying()) {
                memoryAccessor2.setWithExpiry(true, PiglinAi.TIME_BETWEEN_HUNTS.sample(livingEntity.level().random));
            }
            return true;
        }));
    }
}

