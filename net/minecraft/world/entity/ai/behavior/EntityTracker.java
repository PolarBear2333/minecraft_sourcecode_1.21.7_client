/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.ai.behavior;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.PositionTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.phys.Vec3;

public class EntityTracker
implements PositionTracker {
    private final Entity entity;
    private final boolean trackEyeHeight;
    private final boolean targetEyeHeight;

    public EntityTracker(Entity entity, boolean bl) {
        this(entity, bl, false);
    }

    public EntityTracker(Entity entity, boolean bl, boolean bl2) {
        this.entity = entity;
        this.trackEyeHeight = bl;
        this.targetEyeHeight = bl2;
    }

    @Override
    public Vec3 currentPosition() {
        return this.trackEyeHeight ? this.entity.position().add(0.0, this.entity.getEyeHeight(), 0.0) : this.entity.position();
    }

    @Override
    public BlockPos currentBlockPosition() {
        return this.targetEyeHeight ? BlockPos.containing(this.entity.getEyePosition()) : this.entity.blockPosition();
    }

    @Override
    public boolean isVisibleBy(LivingEntity livingEntity) {
        Optional<NearestVisibleLivingEntities> optional = this.entity;
        if (!(optional instanceof LivingEntity)) {
            return true;
        }
        LivingEntity livingEntity2 = (LivingEntity)((Object)optional);
        if (!livingEntity2.isAlive()) {
            return false;
        }
        optional = livingEntity.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
        return optional.isPresent() && ((NearestVisibleLivingEntities)optional.get()).contains(livingEntity2);
    }

    public Entity getEntity() {
        return this.entity;
    }

    public String toString() {
        return "EntityTracker for " + String.valueOf(this.entity);
    }
}

