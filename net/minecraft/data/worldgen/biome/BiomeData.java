/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.worldgen.biome;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.biome.EndBiomes;
import net.minecraft.data.worldgen.biome.NetherBiomes;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public abstract class BiomeData {
    public static void bootstrap(BootstrapContext<Biome> bootstrapContext) {
        HolderGetter<PlacedFeature> holderGetter = bootstrapContext.lookup(Registries.PLACED_FEATURE);
        HolderGetter<ConfiguredWorldCarver<?>> holderGetter2 = bootstrapContext.lookup(Registries.CONFIGURED_CARVER);
        bootstrapContext.register(Biomes.THE_VOID, OverworldBiomes.theVoid(holderGetter, holderGetter2));
        bootstrapContext.register(Biomes.PLAINS, OverworldBiomes.plains(holderGetter, holderGetter2, false, false, false));
        bootstrapContext.register(Biomes.SUNFLOWER_PLAINS, OverworldBiomes.plains(holderGetter, holderGetter2, true, false, false));
        bootstrapContext.register(Biomes.SNOWY_PLAINS, OverworldBiomes.plains(holderGetter, holderGetter2, false, true, false));
        bootstrapContext.register(Biomes.ICE_SPIKES, OverworldBiomes.plains(holderGetter, holderGetter2, false, true, true));
        bootstrapContext.register(Biomes.DESERT, OverworldBiomes.desert(holderGetter, holderGetter2));
        bootstrapContext.register(Biomes.SWAMP, OverworldBiomes.swamp(holderGetter, holderGetter2));
        bootstrapContext.register(Biomes.MANGROVE_SWAMP, OverworldBiomes.mangroveSwamp(holderGetter, holderGetter2));
        bootstrapContext.register(Biomes.FOREST, OverworldBiomes.forest(holderGetter, holderGetter2, false, false, false));
        bootstrapContext.register(Biomes.FLOWER_FOREST, OverworldBiomes.forest(holderGetter, holderGetter2, false, false, true));
        bootstrapContext.register(Biomes.BIRCH_FOREST, OverworldBiomes.forest(holderGetter, holderGetter2, true, false, false));
        bootstrapContext.register(Biomes.DARK_FOREST, OverworldBiomes.darkForest(holderGetter, holderGetter2, false));
        bootstrapContext.register(Biomes.PALE_GARDEN, OverworldBiomes.darkForest(holderGetter, holderGetter2, true));
        bootstrapContext.register(Biomes.OLD_GROWTH_BIRCH_FOREST, OverworldBiomes.forest(holderGetter, holderGetter2, true, true, false));
        bootstrapContext.register(Biomes.OLD_GROWTH_PINE_TAIGA, OverworldBiomes.oldGrowthTaiga(holderGetter, holderGetter2, false));
        bootstrapContext.register(Biomes.OLD_GROWTH_SPRUCE_TAIGA, OverworldBiomes.oldGrowthTaiga(holderGetter, holderGetter2, true));
        bootstrapContext.register(Biomes.TAIGA, OverworldBiomes.taiga(holderGetter, holderGetter2, false));
        bootstrapContext.register(Biomes.SNOWY_TAIGA, OverworldBiomes.taiga(holderGetter, holderGetter2, true));
        bootstrapContext.register(Biomes.SAVANNA, OverworldBiomes.savanna(holderGetter, holderGetter2, false, false));
        bootstrapContext.register(Biomes.SAVANNA_PLATEAU, OverworldBiomes.savanna(holderGetter, holderGetter2, false, true));
        bootstrapContext.register(Biomes.WINDSWEPT_HILLS, OverworldBiomes.windsweptHills(holderGetter, holderGetter2, false));
        bootstrapContext.register(Biomes.WINDSWEPT_GRAVELLY_HILLS, OverworldBiomes.windsweptHills(holderGetter, holderGetter2, false));
        bootstrapContext.register(Biomes.WINDSWEPT_FOREST, OverworldBiomes.windsweptHills(holderGetter, holderGetter2, true));
        bootstrapContext.register(Biomes.WINDSWEPT_SAVANNA, OverworldBiomes.savanna(holderGetter, holderGetter2, true, false));
        bootstrapContext.register(Biomes.JUNGLE, OverworldBiomes.jungle(holderGetter, holderGetter2));
        bootstrapContext.register(Biomes.SPARSE_JUNGLE, OverworldBiomes.sparseJungle(holderGetter, holderGetter2));
        bootstrapContext.register(Biomes.BAMBOO_JUNGLE, OverworldBiomes.bambooJungle(holderGetter, holderGetter2));
        bootstrapContext.register(Biomes.BADLANDS, OverworldBiomes.badlands(holderGetter, holderGetter2, false));
        bootstrapContext.register(Biomes.ERODED_BADLANDS, OverworldBiomes.badlands(holderGetter, holderGetter2, false));
        bootstrapContext.register(Biomes.WOODED_BADLANDS, OverworldBiomes.badlands(holderGetter, holderGetter2, true));
        bootstrapContext.register(Biomes.MEADOW, OverworldBiomes.meadowOrCherryGrove(holderGetter, holderGetter2, false));
        bootstrapContext.register(Biomes.CHERRY_GROVE, OverworldBiomes.meadowOrCherryGrove(holderGetter, holderGetter2, true));
        bootstrapContext.register(Biomes.GROVE, OverworldBiomes.grove(holderGetter, holderGetter2));
        bootstrapContext.register(Biomes.SNOWY_SLOPES, OverworldBiomes.snowySlopes(holderGetter, holderGetter2));
        bootstrapContext.register(Biomes.FROZEN_PEAKS, OverworldBiomes.frozenPeaks(holderGetter, holderGetter2));
        bootstrapContext.register(Biomes.JAGGED_PEAKS, OverworldBiomes.jaggedPeaks(holderGetter, holderGetter2));
        bootstrapContext.register(Biomes.STONY_PEAKS, OverworldBiomes.stonyPeaks(holderGetter, holderGetter2));
        bootstrapContext.register(Biomes.RIVER, OverworldBiomes.river(holderGetter, holderGetter2, false));
        bootstrapContext.register(Biomes.FROZEN_RIVER, OverworldBiomes.river(holderGetter, holderGetter2, true));
        bootstrapContext.register(Biomes.BEACH, OverworldBiomes.beach(holderGetter, holderGetter2, false, false));
        bootstrapContext.register(Biomes.SNOWY_BEACH, OverworldBiomes.beach(holderGetter, holderGetter2, true, false));
        bootstrapContext.register(Biomes.STONY_SHORE, OverworldBiomes.beach(holderGetter, holderGetter2, false, true));
        bootstrapContext.register(Biomes.WARM_OCEAN, OverworldBiomes.warmOcean(holderGetter, holderGetter2));
        bootstrapContext.register(Biomes.LUKEWARM_OCEAN, OverworldBiomes.lukeWarmOcean(holderGetter, holderGetter2, false));
        bootstrapContext.register(Biomes.DEEP_LUKEWARM_OCEAN, OverworldBiomes.lukeWarmOcean(holderGetter, holderGetter2, true));
        bootstrapContext.register(Biomes.OCEAN, OverworldBiomes.ocean(holderGetter, holderGetter2, false));
        bootstrapContext.register(Biomes.DEEP_OCEAN, OverworldBiomes.ocean(holderGetter, holderGetter2, true));
        bootstrapContext.register(Biomes.COLD_OCEAN, OverworldBiomes.coldOcean(holderGetter, holderGetter2, false));
        bootstrapContext.register(Biomes.DEEP_COLD_OCEAN, OverworldBiomes.coldOcean(holderGetter, holderGetter2, true));
        bootstrapContext.register(Biomes.FROZEN_OCEAN, OverworldBiomes.frozenOcean(holderGetter, holderGetter2, false));
        bootstrapContext.register(Biomes.DEEP_FROZEN_OCEAN, OverworldBiomes.frozenOcean(holderGetter, holderGetter2, true));
        bootstrapContext.register(Biomes.MUSHROOM_FIELDS, OverworldBiomes.mushroomFields(holderGetter, holderGetter2));
        bootstrapContext.register(Biomes.DRIPSTONE_CAVES, OverworldBiomes.dripstoneCaves(holderGetter, holderGetter2));
        bootstrapContext.register(Biomes.LUSH_CAVES, OverworldBiomes.lushCaves(holderGetter, holderGetter2));
        bootstrapContext.register(Biomes.DEEP_DARK, OverworldBiomes.deepDark(holderGetter, holderGetter2));
        bootstrapContext.register(Biomes.NETHER_WASTES, NetherBiomes.netherWastes(holderGetter, holderGetter2));
        bootstrapContext.register(Biomes.WARPED_FOREST, NetherBiomes.warpedForest(holderGetter, holderGetter2));
        bootstrapContext.register(Biomes.CRIMSON_FOREST, NetherBiomes.crimsonForest(holderGetter, holderGetter2));
        bootstrapContext.register(Biomes.SOUL_SAND_VALLEY, NetherBiomes.soulSandValley(holderGetter, holderGetter2));
        bootstrapContext.register(Biomes.BASALT_DELTAS, NetherBiomes.basaltDeltas(holderGetter, holderGetter2));
        bootstrapContext.register(Biomes.THE_END, EndBiomes.theEnd(holderGetter, holderGetter2));
        bootstrapContext.register(Biomes.END_HIGHLANDS, EndBiomes.endHighlands(holderGetter, holderGetter2));
        bootstrapContext.register(Biomes.END_MIDLANDS, EndBiomes.endMidlands(holderGetter, holderGetter2));
        bootstrapContext.register(Biomes.SMALL_END_ISLANDS, EndBiomes.smallEndIslands(holderGetter, holderGetter2));
        bootstrapContext.register(Biomes.END_BARRENS, EndBiomes.endBarrens(holderGetter, holderGetter2));
    }
}

