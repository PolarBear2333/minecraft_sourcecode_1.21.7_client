/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.StructureSet;

public class FlatLevelSource
extends ChunkGenerator {
    public static final MapCodec<FlatLevelSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)FlatLevelGeneratorSettings.CODEC.fieldOf("settings").forGetter(FlatLevelSource::settings)).apply((Applicative)instance, instance.stable(FlatLevelSource::new)));
    private final FlatLevelGeneratorSettings settings;

    public FlatLevelSource(FlatLevelGeneratorSettings flatLevelGeneratorSettings) {
        super(new FixedBiomeSource(flatLevelGeneratorSettings.getBiome()), Util.memoize(flatLevelGeneratorSettings::adjustGenerationSettings));
        this.settings = flatLevelGeneratorSettings;
    }

    @Override
    public ChunkGeneratorStructureState createState(HolderLookup<StructureSet> holderLookup, RandomState randomState, long l) {
        Stream stream = this.settings.structureOverrides().map(HolderSet::stream).orElseGet(() -> holderLookup.listElements().map(reference -> reference));
        return ChunkGeneratorStructureState.createForFlat(randomState, l, this.biomeSource, stream);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    public FlatLevelGeneratorSettings settings() {
        return this.settings;
    }

    @Override
    public void buildSurface(WorldGenRegion worldGenRegion, StructureManager structureManager, RandomState randomState, ChunkAccess chunkAccess) {
    }

    @Override
    public int getSpawnHeight(LevelHeightAccessor levelHeightAccessor) {
        return levelHeightAccessor.getMinY() + Math.min(levelHeightAccessor.getHeight(), this.settings.getLayers().size());
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunkAccess) {
        List<BlockState> list = this.settings.getLayers();
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        Heightmap heightmap = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        Heightmap heightmap2 = chunkAccess.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        for (int i = 0; i < Math.min(chunkAccess.getHeight(), list.size()); ++i) {
            BlockState blockState = list.get(i);
            if (blockState == null) continue;
            int n = chunkAccess.getMinY() + i;
            for (int j = 0; j < 16; ++j) {
                for (int k = 0; k < 16; ++k) {
                    chunkAccess.setBlockState(mutableBlockPos.set(j, n, k), blockState);
                    heightmap.update(j, n, k, blockState);
                    heightmap2.update(j, n, k, blockState);
                }
            }
        }
        return CompletableFuture.completedFuture(chunkAccess);
    }

    @Override
    public int getBaseHeight(int n, int n2, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        List<BlockState> list = this.settings.getLayers();
        for (int i = Math.min(list.size() - 1, levelHeightAccessor.getMaxY()); i >= 0; --i) {
            BlockState blockState = list.get(i);
            if (blockState == null || !types.isOpaque().test(blockState)) continue;
            return levelHeightAccessor.getMinY() + i + 1;
        }
        return levelHeightAccessor.getMinY();
    }

    @Override
    public NoiseColumn getBaseColumn(int n, int n2, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        return new NoiseColumn(levelHeightAccessor.getMinY(), (BlockState[])this.settings.getLayers().stream().limit(levelHeightAccessor.getHeight()).map(blockState -> blockState == null ? Blocks.AIR.defaultBlockState() : blockState).toArray(BlockState[]::new));
    }

    @Override
    public void addDebugScreenInfo(List<String> list, RandomState randomState, BlockPos blockPos) {
    }

    @Override
    public void applyCarvers(WorldGenRegion worldGenRegion, long l, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunkAccess) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion worldGenRegion) {
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public int getGenDepth() {
        return 384;
    }

    @Override
    public int getSeaLevel() {
        return -63;
    }
}

