/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.function.Predicate;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class CopyMemoryWithExpiry {
    public static <E extends LivingEntity, T> BehaviorControl<E> create(Predicate<E> predicate, MemoryModuleType<? extends T> memoryModuleType, MemoryModuleType<T> memoryModuleType2, UniformInt uniformInt) {
        return BehaviorBuilder.create(instance -> instance.group(instance.present(memoryModuleType), instance.absent(memoryModuleType2)).apply((Applicative)instance, (memoryAccessor, memoryAccessor2) -> (serverLevel, livingEntity, l) -> {
            if (!predicate.test(livingEntity)) {
                return false;
            }
            memoryAccessor2.setWithExpiry(instance.get(memoryAccessor), uniformInt.sample(serverLevel.random));
            return true;
        }));
    }
}

