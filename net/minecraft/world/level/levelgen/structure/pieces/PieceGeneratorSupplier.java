/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.structure.pieces;

import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

@FunctionalInterface
public interface PieceGeneratorSupplier<C extends FeatureConfiguration> {
    public Optional<PieceGenerator<C>> createGenerator(Context<C> var1);

    public static <C extends FeatureConfiguration> PieceGeneratorSupplier<C> simple(Predicate<Context<C>> predicate, PieceGenerator<C> pieceGenerator) {
        Optional optional = Optional.of(pieceGenerator);
        return context -> predicate.test(context) ? optional : Optional.empty();
    }

    public static <C extends FeatureConfiguration> Predicate<Context<C>> checkForBiomeOnTop(Heightmap.Types types) {
        return context -> context.validBiomeOnTop(types);
    }

    public record Context<C extends FeatureConfiguration>(ChunkGenerator chunkGenerator, BiomeSource biomeSource, RandomState randomState, long seed, ChunkPos chunkPos, C config, LevelHeightAccessor heightAccessor, Predicate<Holder<Biome>> validBiome, StructureTemplateManager structureTemplateManager, RegistryAccess registryAccess) {
        public boolean validBiomeOnTop(Heightmap.Types types) {
            int n = this.chunkPos.getMiddleBlockX();
            int n2 = this.chunkPos.getMiddleBlockZ();
            int n3 = this.chunkGenerator.getFirstOccupiedHeight(n, n2, types, this.heightAccessor, this.randomState);
            Holder<Biome> holder = this.chunkGenerator.getBiomeSource().getNoiseBiome(QuartPos.fromBlock(n), QuartPos.fromBlock(n3), QuartPos.fromBlock(n2), this.randomState.sampler());
            return this.validBiome.test(holder);
        }
    }
}

