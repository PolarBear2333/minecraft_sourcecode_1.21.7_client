/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.world.entity.ai.behavior;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class Swim<T extends Mob>
extends Behavior<T> {
    private final float chance;

    public Swim(float f) {
        super((Map<MemoryModuleType<?>, MemoryStatus>)ImmutableMap.of());
        this.chance = f;
    }

    public static <T extends Mob> boolean shouldSwim(T t) {
        return t.isInWater() && t.getFluidHeight(FluidTags.WATER) > t.getFluidJumpThreshold() || t.isInLava();
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel serverLevel, Mob mob) {
        return Swim.shouldSwim(mob);
    }

    @Override
    protected boolean canStillUse(ServerLevel serverLevel, Mob mob, long l) {
        return this.checkExtraStartConditions(serverLevel, mob);
    }

    @Override
    protected void tick(ServerLevel serverLevel, Mob mob, long l) {
        if (mob.getRandom().nextFloat() < this.chance) {
            mob.getJumpControl().jump();
        }
    }

    @Override
    protected /* synthetic */ void tick(ServerLevel serverLevel, LivingEntity livingEntity, long l) {
        this.tick(serverLevel, (Mob)livingEntity, l);
    }
}

