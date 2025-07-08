/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.entity.variant;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.variant.SpawnCondition;
import net.minecraft.world.entity.variant.SpawnContext;
import net.minecraft.world.level.levelgen.structure.Structure;

public record StructureCheck(HolderSet<Structure> requiredStructures) implements SpawnCondition
{
    public static final MapCodec<StructureCheck> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)RegistryCodecs.homogeneousList(Registries.STRUCTURE).fieldOf("structures").forGetter(StructureCheck::requiredStructures)).apply((Applicative)instance, StructureCheck::new));

    @Override
    public boolean test(SpawnContext spawnContext) {
        return spawnContext.level().getLevel().structureManager().getStructureWithPieceAt(spawnContext.pos(), this.requiredStructures).isValid();
    }

    public MapCodec<StructureCheck> codec() {
        return MAP_CODEC;
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((SpawnContext)object);
    }
}

