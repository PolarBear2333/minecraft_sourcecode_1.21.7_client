/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.behavior;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.schedule.Activity;

public class SetRaidStatus {
    public static BehaviorControl<LivingEntity> create() {
        return BehaviorBuilder.create(instance -> instance.point((serverLevel, livingEntity, l) -> {
            if (serverLevel.random.nextInt(20) != 0) {
                return false;
            }
            Brain<?> brain = livingEntity.getBrain();
            Raid raid = serverLevel.getRaidAt(livingEntity.blockPosition());
            if (raid != null) {
                if (!raid.hasFirstWaveSpawned() || raid.isBetweenWaves()) {
                    brain.setDefaultActivity(Activity.PRE_RAID);
                    brain.setActiveActivityIfPossible(Activity.PRE_RAID);
                } else {
                    brain.setDefaultActivity(Activity.RAID);
                    brain.setActiveActivityIfPossible(Activity.RAID);
                }
            }
            return true;
        }));
    }
}

