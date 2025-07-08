/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.block;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class UntintedParticleLeavesBlock
extends LeavesBlock {
    public static final MapCodec<UntintedParticleLeavesBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ExtraCodecs.floatRange(0.0f, 1.0f).fieldOf("leaf_particle_chance").forGetter(untintedParticleLeavesBlock -> Float.valueOf(untintedParticleLeavesBlock.leafParticleChance)), (App)ParticleTypes.CODEC.fieldOf("leaf_particle").forGetter(untintedParticleLeavesBlock -> untintedParticleLeavesBlock.leafParticle), UntintedParticleLeavesBlock.propertiesCodec()).apply((Applicative)instance, UntintedParticleLeavesBlock::new));
    protected final ParticleOptions leafParticle;

    public UntintedParticleLeavesBlock(float f, ParticleOptions particleOptions, BlockBehaviour.Properties properties) {
        super(f, properties);
        this.leafParticle = particleOptions;
    }

    @Override
    protected void spawnFallingLeavesParticle(Level level, BlockPos blockPos, RandomSource randomSource) {
        ParticleUtils.spawnParticleBelow(level, blockPos, randomSource, this.leafParticle);
    }

    public MapCodec<UntintedParticleLeavesBlock> codec() {
        return CODEC;
    }
}

