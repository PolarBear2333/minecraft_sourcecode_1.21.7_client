/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.monster.piglin;

import com.mojang.datafixers.kinds.Applicative;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinAi;

public class StartHuntingHoglin {
    public static OneShot<Piglin> create() {
        return BehaviorBuilder.create(instance -> instance.group(instance.present(MemoryModuleType.NEAREST_VISIBLE_HUNTABLE_HOGLIN), instance.absent(MemoryModuleType.ANGRY_AT), instance.absent(MemoryModuleType.HUNTED_RECENTLY), instance.registered(MemoryModuleType.NEAREST_VISIBLE_ADULT_PIGLINS)).apply((Applicative)instance, (memoryAccessor, memoryAccessor2, memoryAccessor3, memoryAccessor4) -> (serverLevel, piglin, l) -> {
            if (piglin.isBaby() || instance.tryGet(memoryAccessor4).map(list -> list.stream().anyMatch(StartHuntingHoglin::hasHuntedRecently)).isPresent()) {
                return false;
            }
            Hoglin hoglin = (Hoglin)instance.get(memoryAccessor);
            PiglinAi.setAngerTarget(serverLevel, piglin, hoglin);
            PiglinAi.dontKillAnyMoreHoglinsForAWhile(piglin);
            PiglinAi.broadcastAngerTarget(serverLevel, piglin, hoglin);
            instance.tryGet(memoryAccessor4).ifPresent(list -> list.forEach(PiglinAi::dontKillAnyMoreHoglinsForAWhile));
            return true;
        }));
    }

    private static boolean hasHuntedRecently(AbstractPiglin abstractPiglin) {
        return abstractPiglin.getBrain().hasMemoryValue(MemoryModuleType.HUNTED_RECENTLY);
    }
}

