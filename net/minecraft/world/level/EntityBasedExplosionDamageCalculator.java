/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class EntityBasedExplosionDamageCalculator
extends ExplosionDamageCalculator {
    private final Entity source;

    public EntityBasedExplosionDamageCalculator(Entity entity) {
        this.source = entity;
    }

    @Override
    public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
        return super.getBlockExplosionResistance(explosion, blockGetter, blockPos, blockState, fluidState).map(f -> Float.valueOf(this.source.getBlockExplosionResistance(explosion, blockGetter, blockPos, blockState, fluidState, f.floatValue())));
    }

    @Override
    public boolean shouldBlockExplode(Explosion explosion, BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, float f) {
        return this.source.shouldBlockExplode(explosion, blockGetter, blockPos, blockState, f);
    }
}

