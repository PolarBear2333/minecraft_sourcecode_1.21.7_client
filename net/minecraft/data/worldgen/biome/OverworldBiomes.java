/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.data.worldgen.biome;

import javax.annotation.Nullable;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.Carvers;
import net.minecraft.data.worldgen.placement.AquaticPlacements;
import net.minecraft.data.worldgen.placement.MiscOverworldPlacements;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class OverworldBiomes {
    protected static final int NORMAL_WATER_COLOR = 4159204;
    protected static final int NORMAL_WATER_FOG_COLOR = 329011;
    private static final int OVERWORLD_FOG_COLOR = 12638463;
    private static final int DARK_DRY_FOLIAGE_COLOR = 8082228;
    @Nullable
    private static final Music NORMAL_MUSIC = null;
    public static final int SWAMP_SKELETON_WEIGHT = 70;

    protected static int calculateSkyColor(float f) {
        float f2 = f;
        f2 /= 3.0f;
        f2 = Mth.clamp(f2, -1.0f, 1.0f);
        return Mth.hsvToRgb(0.62222224f - f2 * 0.05f, 0.5f + f2 * 0.1f, 1.0f);
    }

    private static Biome biome(boolean bl, float f, float f2, MobSpawnSettings.Builder builder, BiomeGenerationSettings.Builder builder2, @Nullable Music music) {
        return OverworldBiomes.biome(bl, f, f2, 4159204, 329011, null, null, null, builder, builder2, music);
    }

    private static Biome biome(boolean bl, float f, float f2, int n, int n2, @Nullable Integer n3, @Nullable Integer n4, @Nullable Integer n5, MobSpawnSettings.Builder builder, BiomeGenerationSettings.Builder builder2, @Nullable Music music) {
        BiomeSpecialEffects.Builder builder3 = new BiomeSpecialEffects.Builder().waterColor(n).waterFogColor(n2).fogColor(12638463).skyColor(OverworldBiomes.calculateSkyColor(f)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).backgroundMusic(music);
        if (n3 != null) {
            builder3.grassColorOverride(n3);
        }
        if (n4 != null) {
            builder3.foliageColorOverride(n4);
        }
        if (n5 != null) {
            builder3.dryFoliageColorOverride(n5);
        }
        return new Biome.BiomeBuilder().hasPrecipitation(bl).temperature(f).downfall(f2).specialEffects(builder3.build()).mobSpawnSettings(builder.build()).generationSettings(builder2.build()).build();
    }

    private static void globalOverworldGeneration(BiomeGenerationSettings.Builder builder) {
        BiomeDefaultFeatures.addDefaultCarversAndLakes(builder);
        BiomeDefaultFeatures.addDefaultCrystalFormations(builder);
        BiomeDefaultFeatures.addDefaultMonsterRoom(builder);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(builder);
        BiomeDefaultFeatures.addDefaultSprings(builder);
        BiomeDefaultFeatures.addSurfaceFreezing(builder);
    }

    public static Biome oldGrowthTaiga(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2, boolean bl) {
        MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(builder);
        builder.addSpawn(MobCategory.CREATURE, 8, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 4, 4));
        builder.addSpawn(MobCategory.CREATURE, 4, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 2, 3));
        builder.addSpawn(MobCategory.CREATURE, 8, new MobSpawnSettings.SpawnerData(EntityType.FOX, 2, 4));
        if (bl) {
            BiomeDefaultFeatures.commonSpawns(builder);
        } else {
            BiomeDefaultFeatures.caveSpawns(builder);
            BiomeDefaultFeatures.monsters(builder, 100, 25, 100, false);
        }
        BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        OverworldBiomes.globalOverworldGeneration(builder2);
        BiomeDefaultFeatures.addMossyStoneBlock(builder2);
        BiomeDefaultFeatures.addFerns(builder2);
        BiomeDefaultFeatures.addDefaultOres(builder2);
        BiomeDefaultFeatures.addDefaultSoftDisks(builder2);
        builder2.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, bl ? VegetationPlacements.TREES_OLD_GROWTH_SPRUCE_TAIGA : VegetationPlacements.TREES_OLD_GROWTH_PINE_TAIGA);
        BiomeDefaultFeatures.addDefaultFlowers(builder2);
        BiomeDefaultFeatures.addGiantTaigaVegetation(builder2);
        BiomeDefaultFeatures.addDefaultMushrooms(builder2);
        BiomeDefaultFeatures.addDefaultExtraVegetation(builder2, true);
        BiomeDefaultFeatures.addCommonBerryBushes(builder2);
        Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_OLD_GROWTH_TAIGA);
        return OverworldBiomes.biome(true, bl ? 0.25f : 0.3f, 0.8f, builder, builder2, music);
    }

    public static Biome sparseJungle(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.baseJungleSpawns(builder);
        builder.addSpawn(MobCategory.CREATURE, 8, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 2, 4));
        return OverworldBiomes.baseJungle(holderGetter, holderGetter2, 0.8f, false, true, false, builder, Musics.createGameMusic(SoundEvents.MUSIC_BIOME_SPARSE_JUNGLE));
    }

    public static Biome jungle(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.baseJungleSpawns(builder);
        builder.addSpawn(MobCategory.CREATURE, 40, new MobSpawnSettings.SpawnerData(EntityType.PARROT, 1, 2)).addSpawn(MobCategory.MONSTER, 2, new MobSpawnSettings.SpawnerData(EntityType.OCELOT, 1, 3)).addSpawn(MobCategory.CREATURE, 1, new MobSpawnSettings.SpawnerData(EntityType.PANDA, 1, 2));
        return OverworldBiomes.baseJungle(holderGetter, holderGetter2, 0.9f, false, false, true, builder, Musics.createGameMusic(SoundEvents.MUSIC_BIOME_JUNGLE));
    }

    public static Biome bambooJungle(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.baseJungleSpawns(builder);
        builder.addSpawn(MobCategory.CREATURE, 40, new MobSpawnSettings.SpawnerData(EntityType.PARROT, 1, 2)).addSpawn(MobCategory.CREATURE, 80, new MobSpawnSettings.SpawnerData(EntityType.PANDA, 1, 2)).addSpawn(MobCategory.MONSTER, 2, new MobSpawnSettings.SpawnerData(EntityType.OCELOT, 1, 1));
        return OverworldBiomes.baseJungle(holderGetter, holderGetter2, 0.9f, true, false, true, builder, Musics.createGameMusic(SoundEvents.MUSIC_BIOME_BAMBOO_JUNGLE));
    }

    private static Biome baseJungle(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2, float f, boolean bl, boolean bl2, boolean bl3, MobSpawnSettings.Builder builder, Music music) {
        BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        OverworldBiomes.globalOverworldGeneration(builder2);
        BiomeDefaultFeatures.addDefaultOres(builder2);
        BiomeDefaultFeatures.addDefaultSoftDisks(builder2);
        if (bl) {
            BiomeDefaultFeatures.addBambooVegetation(builder2);
        } else {
            if (bl3) {
                BiomeDefaultFeatures.addLightBambooVegetation(builder2);
            }
            if (bl2) {
                BiomeDefaultFeatures.addSparseJungleTrees(builder2);
            } else {
                BiomeDefaultFeatures.addJungleTrees(builder2);
            }
        }
        BiomeDefaultFeatures.addWarmFlowers(builder2);
        BiomeDefaultFeatures.addJungleGrass(builder2);
        BiomeDefaultFeatures.addDefaultMushrooms(builder2);
        BiomeDefaultFeatures.addDefaultExtraVegetation(builder2, true);
        BiomeDefaultFeatures.addJungleVines(builder2);
        if (bl2) {
            BiomeDefaultFeatures.addSparseJungleMelons(builder2);
        } else {
            BiomeDefaultFeatures.addJungleMelons(builder2);
        }
        return OverworldBiomes.biome(true, 0.95f, f, builder, builder2, music);
    }

    public static Biome windsweptHills(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2, boolean bl) {
        MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(builder);
        builder.addSpawn(MobCategory.CREATURE, 5, new MobSpawnSettings.SpawnerData(EntityType.LLAMA, 4, 6));
        BiomeDefaultFeatures.commonSpawns(builder);
        BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        OverworldBiomes.globalOverworldGeneration(builder2);
        BiomeDefaultFeatures.addDefaultOres(builder2);
        BiomeDefaultFeatures.addDefaultSoftDisks(builder2);
        if (bl) {
            BiomeDefaultFeatures.addMountainForestTrees(builder2);
        } else {
            BiomeDefaultFeatures.addMountainTrees(builder2);
        }
        BiomeDefaultFeatures.addBushes(builder2);
        BiomeDefaultFeatures.addDefaultFlowers(builder2);
        BiomeDefaultFeatures.addDefaultGrass(builder2);
        BiomeDefaultFeatures.addDefaultMushrooms(builder2);
        BiomeDefaultFeatures.addDefaultExtraVegetation(builder2, true);
        BiomeDefaultFeatures.addExtraEmeralds(builder2);
        BiomeDefaultFeatures.addInfestedStone(builder2);
        return OverworldBiomes.biome(true, 0.2f, 0.3f, builder, builder2, NORMAL_MUSIC);
    }

    public static Biome desert(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.desertSpawns(builder);
        BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        BiomeDefaultFeatures.addFossilDecoration(builder2);
        OverworldBiomes.globalOverworldGeneration(builder2);
        BiomeDefaultFeatures.addDefaultOres(builder2);
        BiomeDefaultFeatures.addDefaultSoftDisks(builder2);
        BiomeDefaultFeatures.addDefaultFlowers(builder2);
        BiomeDefaultFeatures.addDefaultGrass(builder2);
        BiomeDefaultFeatures.addDesertVegetation(builder2);
        BiomeDefaultFeatures.addDefaultMushrooms(builder2);
        BiomeDefaultFeatures.addDesertExtraVegetation(builder2);
        BiomeDefaultFeatures.addDesertExtraDecoration(builder2);
        return OverworldBiomes.biome(false, 2.0f, 0.0f, builder, builder2, Musics.createGameMusic(SoundEvents.MUSIC_BIOME_DESERT));
    }

    public static Biome plains(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2, boolean bl, boolean bl2, boolean bl3) {
        MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
        BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        OverworldBiomes.globalOverworldGeneration(builder2);
        if (bl2) {
            builder.creatureGenerationProbability(0.07f);
            BiomeDefaultFeatures.snowySpawns(builder);
            if (bl3) {
                builder2.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, MiscOverworldPlacements.ICE_SPIKE);
                builder2.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, MiscOverworldPlacements.ICE_PATCH);
            }
        } else {
            BiomeDefaultFeatures.plainsSpawns(builder);
            BiomeDefaultFeatures.addPlainGrass(builder2);
            if (bl) {
                builder2.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_SUNFLOWER);
            } else {
                BiomeDefaultFeatures.addBushes(builder2);
            }
        }
        BiomeDefaultFeatures.addDefaultOres(builder2);
        BiomeDefaultFeatures.addDefaultSoftDisks(builder2);
        if (bl2) {
            BiomeDefaultFeatures.addSnowyTrees(builder2);
            BiomeDefaultFeatures.addDefaultFlowers(builder2);
            BiomeDefaultFeatures.addDefaultGrass(builder2);
        } else {
            BiomeDefaultFeatures.addPlainVegetation(builder2);
        }
        BiomeDefaultFeatures.addDefaultMushrooms(builder2);
        BiomeDefaultFeatures.addDefaultExtraVegetation(builder2, true);
        float f = bl2 ? 0.0f : 0.8f;
        return OverworldBiomes.biome(true, f, bl2 ? 0.5f : 0.4f, builder, builder2, NORMAL_MUSIC);
    }

    public static Biome mushroomFields(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.mooshroomSpawns(builder);
        BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        OverworldBiomes.globalOverworldGeneration(builder2);
        BiomeDefaultFeatures.addDefaultOres(builder2);
        BiomeDefaultFeatures.addDefaultSoftDisks(builder2);
        BiomeDefaultFeatures.addMushroomFieldVegetation(builder2);
        BiomeDefaultFeatures.addDefaultExtraVegetation(builder2, true);
        return OverworldBiomes.biome(true, 0.9f, 1.0f, builder, builder2, NORMAL_MUSIC);
    }

    public static Biome savanna(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2, boolean bl, boolean bl2) {
        BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        OverworldBiomes.globalOverworldGeneration(builder);
        if (!bl) {
            BiomeDefaultFeatures.addSavannaGrass(builder);
        }
        BiomeDefaultFeatures.addDefaultOres(builder);
        BiomeDefaultFeatures.addDefaultSoftDisks(builder);
        if (bl) {
            BiomeDefaultFeatures.addShatteredSavannaTrees(builder);
            BiomeDefaultFeatures.addDefaultFlowers(builder);
            BiomeDefaultFeatures.addShatteredSavannaGrass(builder);
        } else {
            BiomeDefaultFeatures.addSavannaTrees(builder);
            BiomeDefaultFeatures.addWarmFlowers(builder);
            BiomeDefaultFeatures.addSavannaExtraGrass(builder);
        }
        BiomeDefaultFeatures.addDefaultMushrooms(builder);
        BiomeDefaultFeatures.addDefaultExtraVegetation(builder, true);
        MobSpawnSettings.Builder builder2 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(builder2);
        builder2.addSpawn(MobCategory.CREATURE, 1, new MobSpawnSettings.SpawnerData(EntityType.HORSE, 2, 6)).addSpawn(MobCategory.CREATURE, 1, new MobSpawnSettings.SpawnerData(EntityType.DONKEY, 1, 1)).addSpawn(MobCategory.CREATURE, 10, new MobSpawnSettings.SpawnerData(EntityType.ARMADILLO, 2, 3));
        BiomeDefaultFeatures.commonSpawns(builder2);
        if (bl2) {
            builder2.addSpawn(MobCategory.CREATURE, 8, new MobSpawnSettings.SpawnerData(EntityType.LLAMA, 4, 4));
            builder2.addSpawn(MobCategory.CREATURE, 8, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 4, 8));
        }
        return OverworldBiomes.biome(false, 2.0f, 0.0f, builder2, builder, NORMAL_MUSIC);
    }

    public static Biome badlands(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2, boolean bl) {
        MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(builder);
        BiomeDefaultFeatures.commonSpawns(builder);
        builder.addSpawn(MobCategory.CREATURE, 6, new MobSpawnSettings.SpawnerData(EntityType.ARMADILLO, 1, 2));
        builder.creatureGenerationProbability(0.03f);
        if (bl) {
            builder.addSpawn(MobCategory.CREATURE, 2, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 4, 8));
            builder.creatureGenerationProbability(0.04f);
        }
        BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        OverworldBiomes.globalOverworldGeneration(builder2);
        BiomeDefaultFeatures.addDefaultOres(builder2);
        BiomeDefaultFeatures.addExtraGold(builder2);
        BiomeDefaultFeatures.addDefaultSoftDisks(builder2);
        if (bl) {
            BiomeDefaultFeatures.addBadlandsTrees(builder2);
        }
        BiomeDefaultFeatures.addBadlandGrass(builder2);
        BiomeDefaultFeatures.addDefaultMushrooms(builder2);
        BiomeDefaultFeatures.addBadlandExtraVegetation(builder2);
        return new Biome.BiomeBuilder().hasPrecipitation(false).temperature(2.0f).downfall(0.0f).specialEffects(new BiomeSpecialEffects.Builder().waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(OverworldBiomes.calculateSkyColor(2.0f)).foliageColorOverride(10387789).grassColorOverride(9470285).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_BADLANDS)).build()).mobSpawnSettings(builder.build()).generationSettings(builder2.build()).build();
    }

    private static Biome baseOcean(MobSpawnSettings.Builder builder, int n, int n2, BiomeGenerationSettings.Builder builder2) {
        return OverworldBiomes.biome(true, 0.5f, 0.5f, n, n2, null, null, null, builder, builder2, NORMAL_MUSIC);
    }

    private static BiomeGenerationSettings.Builder baseOceanGeneration(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        OverworldBiomes.globalOverworldGeneration(builder);
        BiomeDefaultFeatures.addDefaultOres(builder);
        BiomeDefaultFeatures.addDefaultSoftDisks(builder);
        BiomeDefaultFeatures.addWaterTrees(builder);
        BiomeDefaultFeatures.addDefaultFlowers(builder);
        BiomeDefaultFeatures.addDefaultGrass(builder);
        BiomeDefaultFeatures.addDefaultMushrooms(builder);
        BiomeDefaultFeatures.addDefaultExtraVegetation(builder, true);
        return builder;
    }

    public static Biome coldOcean(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2, boolean bl) {
        MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.oceanSpawns(builder, 3, 4, 15);
        builder.addSpawn(MobCategory.WATER_AMBIENT, 15, new MobSpawnSettings.SpawnerData(EntityType.SALMON, 1, 5));
        BiomeGenerationSettings.Builder builder2 = OverworldBiomes.baseOceanGeneration(holderGetter, holderGetter2);
        builder2.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, bl ? AquaticPlacements.SEAGRASS_DEEP_COLD : AquaticPlacements.SEAGRASS_COLD);
        BiomeDefaultFeatures.addColdOceanExtraVegetation(builder2);
        return OverworldBiomes.baseOcean(builder, 4020182, 329011, builder2);
    }

    public static Biome ocean(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2, boolean bl) {
        MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.oceanSpawns(builder, 1, 4, 10);
        builder.addSpawn(MobCategory.WATER_CREATURE, 1, new MobSpawnSettings.SpawnerData(EntityType.DOLPHIN, 1, 2));
        BiomeGenerationSettings.Builder builder2 = OverworldBiomes.baseOceanGeneration(holderGetter, holderGetter2);
        builder2.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, bl ? AquaticPlacements.SEAGRASS_DEEP : AquaticPlacements.SEAGRASS_NORMAL);
        BiomeDefaultFeatures.addColdOceanExtraVegetation(builder2);
        return OverworldBiomes.baseOcean(builder, 4159204, 329011, builder2);
    }

    public static Biome lukeWarmOcean(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2, boolean bl) {
        MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
        if (bl) {
            BiomeDefaultFeatures.oceanSpawns(builder, 8, 4, 8);
        } else {
            BiomeDefaultFeatures.oceanSpawns(builder, 10, 2, 15);
        }
        builder.addSpawn(MobCategory.WATER_AMBIENT, 5, new MobSpawnSettings.SpawnerData(EntityType.PUFFERFISH, 1, 3)).addSpawn(MobCategory.WATER_AMBIENT, 25, new MobSpawnSettings.SpawnerData(EntityType.TROPICAL_FISH, 8, 8)).addSpawn(MobCategory.WATER_CREATURE, 2, new MobSpawnSettings.SpawnerData(EntityType.DOLPHIN, 1, 2));
        BiomeGenerationSettings.Builder builder2 = OverworldBiomes.baseOceanGeneration(holderGetter, holderGetter2);
        builder2.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, bl ? AquaticPlacements.SEAGRASS_DEEP_WARM : AquaticPlacements.SEAGRASS_WARM);
        BiomeDefaultFeatures.addLukeWarmKelp(builder2);
        return OverworldBiomes.baseOcean(builder, 4566514, 267827, builder2);
    }

    public static Biome warmOcean(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder().addSpawn(MobCategory.WATER_AMBIENT, 15, new MobSpawnSettings.SpawnerData(EntityType.PUFFERFISH, 1, 3));
        BiomeDefaultFeatures.warmOceanSpawns(builder, 10, 4);
        BiomeGenerationSettings.Builder builder2 = OverworldBiomes.baseOceanGeneration(holderGetter, holderGetter2).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.WARM_OCEAN_VEGETATION).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEAGRASS_WARM).addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEA_PICKLE);
        return OverworldBiomes.baseOcean(builder, 4445678, 270131, builder2);
    }

    public static Biome frozenOcean(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2, boolean bl) {
        MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder().addSpawn(MobCategory.WATER_CREATURE, 1, new MobSpawnSettings.SpawnerData(EntityType.SQUID, 1, 4)).addSpawn(MobCategory.WATER_AMBIENT, 15, new MobSpawnSettings.SpawnerData(EntityType.SALMON, 1, 5)).addSpawn(MobCategory.CREATURE, 1, new MobSpawnSettings.SpawnerData(EntityType.POLAR_BEAR, 1, 2));
        BiomeDefaultFeatures.commonSpawns(builder);
        builder.addSpawn(MobCategory.MONSTER, 5, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, 1, 1));
        float f = bl ? 0.5f : 0.0f;
        BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        BiomeDefaultFeatures.addIcebergs(builder2);
        OverworldBiomes.globalOverworldGeneration(builder2);
        BiomeDefaultFeatures.addBlueIce(builder2);
        BiomeDefaultFeatures.addDefaultOres(builder2);
        BiomeDefaultFeatures.addDefaultSoftDisks(builder2);
        BiomeDefaultFeatures.addWaterTrees(builder2);
        BiomeDefaultFeatures.addDefaultFlowers(builder2);
        BiomeDefaultFeatures.addDefaultGrass(builder2);
        BiomeDefaultFeatures.addDefaultMushrooms(builder2);
        BiomeDefaultFeatures.addDefaultExtraVegetation(builder2, true);
        return new Biome.BiomeBuilder().hasPrecipitation(true).temperature(f).temperatureAdjustment(Biome.TemperatureModifier.FROZEN).downfall(0.5f).specialEffects(new BiomeSpecialEffects.Builder().waterColor(3750089).waterFogColor(329011).fogColor(12638463).skyColor(OverworldBiomes.calculateSkyColor(f)).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).build()).mobSpawnSettings(builder.build()).generationSettings(builder2.build()).build();
    }

    public static Biome forest(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2, boolean bl, boolean bl2, boolean bl3) {
        Music music;
        BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        OverworldBiomes.globalOverworldGeneration(builder);
        if (bl3) {
            music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_FLOWER_FOREST);
            builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_FOREST_FLOWERS);
        } else {
            music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_FOREST);
            BiomeDefaultFeatures.addForestFlowers(builder);
        }
        BiomeDefaultFeatures.addDefaultOres(builder);
        BiomeDefaultFeatures.addDefaultSoftDisks(builder);
        if (bl3) {
            builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.TREES_FLOWER_FOREST);
            builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_FLOWER_FOREST);
            BiomeDefaultFeatures.addDefaultGrass(builder);
        } else {
            if (bl) {
                BiomeDefaultFeatures.addBirchForestFlowers(builder);
                if (bl2) {
                    BiomeDefaultFeatures.addTallBirchTrees(builder);
                } else {
                    BiomeDefaultFeatures.addBirchTrees(builder);
                }
            } else {
                BiomeDefaultFeatures.addOtherBirchTrees(builder);
            }
            BiomeDefaultFeatures.addBushes(builder);
            BiomeDefaultFeatures.addDefaultFlowers(builder);
            BiomeDefaultFeatures.addForestGrass(builder);
        }
        BiomeDefaultFeatures.addDefaultMushrooms(builder);
        BiomeDefaultFeatures.addDefaultExtraVegetation(builder, true);
        MobSpawnSettings.Builder builder2 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(builder2);
        BiomeDefaultFeatures.commonSpawns(builder2);
        if (bl3) {
            builder2.addSpawn(MobCategory.CREATURE, 4, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 2, 3));
        } else if (!bl) {
            builder2.addSpawn(MobCategory.CREATURE, 5, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 4, 4));
        }
        float f = bl ? 0.6f : 0.7f;
        return OverworldBiomes.biome(true, f, bl ? 0.6f : 0.8f, builder2, builder, music);
    }

    public static Biome taiga(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2, boolean bl) {
        MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(builder);
        builder.addSpawn(MobCategory.CREATURE, 8, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 4, 4)).addSpawn(MobCategory.CREATURE, 4, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 2, 3)).addSpawn(MobCategory.CREATURE, 8, new MobSpawnSettings.SpawnerData(EntityType.FOX, 2, 4));
        BiomeDefaultFeatures.commonSpawns(builder);
        float f = bl ? -0.5f : 0.25f;
        BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        OverworldBiomes.globalOverworldGeneration(builder2);
        BiomeDefaultFeatures.addFerns(builder2);
        BiomeDefaultFeatures.addDefaultOres(builder2);
        BiomeDefaultFeatures.addDefaultSoftDisks(builder2);
        BiomeDefaultFeatures.addTaigaTrees(builder2);
        BiomeDefaultFeatures.addDefaultFlowers(builder2);
        BiomeDefaultFeatures.addTaigaGrass(builder2);
        BiomeDefaultFeatures.addDefaultExtraVegetation(builder2, true);
        if (bl) {
            BiomeDefaultFeatures.addRareBerryBushes(builder2);
        } else {
            BiomeDefaultFeatures.addCommonBerryBushes(builder2);
        }
        return OverworldBiomes.biome(true, f, bl ? 0.4f : 0.8f, bl ? 4020182 : 4159204, 329011, null, null, null, builder, builder2, NORMAL_MUSIC);
    }

    public static Biome darkForest(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2, boolean bl) {
        MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
        if (!bl) {
            BiomeDefaultFeatures.farmAnimals(builder);
        }
        BiomeDefaultFeatures.commonSpawns(builder);
        BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        OverworldBiomes.globalOverworldGeneration(builder2);
        builder2.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, bl ? VegetationPlacements.PALE_GARDEN_VEGETATION : VegetationPlacements.DARK_FOREST_VEGETATION);
        if (!bl) {
            BiomeDefaultFeatures.addForestFlowers(builder2);
        } else {
            builder2.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PALE_MOSS_PATCH);
            builder2.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PALE_GARDEN_FLOWERS);
        }
        BiomeDefaultFeatures.addDefaultOres(builder2);
        BiomeDefaultFeatures.addDefaultSoftDisks(builder2);
        if (!bl) {
            BiomeDefaultFeatures.addDefaultFlowers(builder2);
        } else {
            builder2.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_PALE_GARDEN);
        }
        BiomeDefaultFeatures.addForestGrass(builder2);
        if (!bl) {
            BiomeDefaultFeatures.addDefaultMushrooms(builder2);
            BiomeDefaultFeatures.addLeafLitterPatch(builder2);
        }
        BiomeDefaultFeatures.addDefaultExtraVegetation(builder2, true);
        return new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.7f).downfall(0.8f).specialEffects(bl ? new BiomeSpecialEffects.Builder().waterColor(7768221).waterFogColor(5597568).fogColor(8484720).skyColor(0xB9B9B9).grassColorOverride(0x778272).foliageColorOverride(8883574).dryFoliageColorOverride(10528412).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).silenceAllBackgroundMusic().build() : new BiomeSpecialEffects.Builder().waterColor(4159204).waterFogColor(329011).fogColor(12638463).skyColor(OverworldBiomes.calculateSkyColor(0.7f)).dryFoliageColorOverride(8082228).grassColorModifier(BiomeSpecialEffects.GrassColorModifier.DARK_FOREST).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).backgroundMusic(Musics.createGameMusic(SoundEvents.MUSIC_BIOME_FOREST)).build()).mobSpawnSettings(builder.build()).generationSettings(builder2.build()).build();
    }

    public static Biome swamp(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.farmAnimals(builder);
        BiomeDefaultFeatures.commonSpawns(builder, 70);
        builder.addSpawn(MobCategory.MONSTER, 1, new MobSpawnSettings.SpawnerData(EntityType.SLIME, 1, 1));
        builder.addSpawn(MobCategory.MONSTER, 30, new MobSpawnSettings.SpawnerData(EntityType.BOGGED, 4, 4));
        builder.addSpawn(MobCategory.CREATURE, 10, new MobSpawnSettings.SpawnerData(EntityType.FROG, 2, 5));
        BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        BiomeDefaultFeatures.addFossilDecoration(builder2);
        OverworldBiomes.globalOverworldGeneration(builder2);
        BiomeDefaultFeatures.addDefaultOres(builder2);
        BiomeDefaultFeatures.addSwampClayDisk(builder2);
        BiomeDefaultFeatures.addSwampVegetation(builder2);
        BiomeDefaultFeatures.addDefaultMushrooms(builder2);
        BiomeDefaultFeatures.addSwampExtraVegetation(builder2);
        builder2.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEAGRASS_SWAMP);
        Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_SWAMP);
        return new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.8f).downfall(0.9f).specialEffects(new BiomeSpecialEffects.Builder().waterColor(6388580).waterFogColor(2302743).fogColor(12638463).skyColor(OverworldBiomes.calculateSkyColor(0.8f)).foliageColorOverride(6975545).dryFoliageColorOverride(8082228).grassColorModifier(BiomeSpecialEffects.GrassColorModifier.SWAMP).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).backgroundMusic(music).build()).mobSpawnSettings(builder.build()).generationSettings(builder2.build()).build();
    }

    public static Biome mangroveSwamp(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.commonSpawns(builder, 70);
        builder.addSpawn(MobCategory.MONSTER, 1, new MobSpawnSettings.SpawnerData(EntityType.SLIME, 1, 1));
        builder.addSpawn(MobCategory.MONSTER, 30, new MobSpawnSettings.SpawnerData(EntityType.BOGGED, 4, 4));
        builder.addSpawn(MobCategory.CREATURE, 10, new MobSpawnSettings.SpawnerData(EntityType.FROG, 2, 5));
        builder.addSpawn(MobCategory.WATER_AMBIENT, 25, new MobSpawnSettings.SpawnerData(EntityType.TROPICAL_FISH, 8, 8));
        BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        BiomeDefaultFeatures.addFossilDecoration(builder2);
        OverworldBiomes.globalOverworldGeneration(builder2);
        BiomeDefaultFeatures.addDefaultOres(builder2);
        BiomeDefaultFeatures.addMangroveSwampDisks(builder2);
        BiomeDefaultFeatures.addMangroveSwampVegetation(builder2);
        BiomeDefaultFeatures.addMangroveSwampExtraVegetation(builder2);
        Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_SWAMP);
        return new Biome.BiomeBuilder().hasPrecipitation(true).temperature(0.8f).downfall(0.9f).specialEffects(new BiomeSpecialEffects.Builder().waterColor(3832426).waterFogColor(5077600).fogColor(12638463).skyColor(OverworldBiomes.calculateSkyColor(0.8f)).foliageColorOverride(9285927).dryFoliageColorOverride(8082228).grassColorModifier(BiomeSpecialEffects.GrassColorModifier.SWAMP).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS).backgroundMusic(music).build()).mobSpawnSettings(builder.build()).generationSettings(builder2.build()).build();
    }

    public static Biome river(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2, boolean bl) {
        MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder().addSpawn(MobCategory.WATER_CREATURE, 2, new MobSpawnSettings.SpawnerData(EntityType.SQUID, 1, 4)).addSpawn(MobCategory.WATER_AMBIENT, 5, new MobSpawnSettings.SpawnerData(EntityType.SALMON, 1, 5));
        BiomeDefaultFeatures.commonSpawns(builder);
        builder.addSpawn(MobCategory.MONSTER, bl ? 1 : 100, new MobSpawnSettings.SpawnerData(EntityType.DROWNED, 1, 1));
        BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        OverworldBiomes.globalOverworldGeneration(builder2);
        BiomeDefaultFeatures.addDefaultOres(builder2);
        BiomeDefaultFeatures.addDefaultSoftDisks(builder2);
        BiomeDefaultFeatures.addWaterTrees(builder2);
        BiomeDefaultFeatures.addBushes(builder2);
        BiomeDefaultFeatures.addDefaultFlowers(builder2);
        BiomeDefaultFeatures.addDefaultGrass(builder2);
        BiomeDefaultFeatures.addDefaultMushrooms(builder2);
        BiomeDefaultFeatures.addDefaultExtraVegetation(builder2, true);
        if (!bl) {
            builder2.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, AquaticPlacements.SEAGRASS_RIVER);
        }
        float f = bl ? 0.0f : 0.5f;
        return OverworldBiomes.biome(true, f, 0.5f, bl ? 3750089 : 4159204, 329011, null, null, null, builder, builder2, NORMAL_MUSIC);
    }

    public static Biome beach(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2, boolean bl, boolean bl2) {
        boolean bl3;
        MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
        boolean bl4 = bl3 = !bl2 && !bl;
        if (bl3) {
            builder.addSpawn(MobCategory.CREATURE, 5, new MobSpawnSettings.SpawnerData(EntityType.TURTLE, 2, 5));
        }
        BiomeDefaultFeatures.commonSpawns(builder);
        BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        OverworldBiomes.globalOverworldGeneration(builder2);
        BiomeDefaultFeatures.addDefaultOres(builder2);
        BiomeDefaultFeatures.addDefaultSoftDisks(builder2);
        BiomeDefaultFeatures.addDefaultFlowers(builder2);
        BiomeDefaultFeatures.addDefaultGrass(builder2);
        BiomeDefaultFeatures.addDefaultMushrooms(builder2);
        BiomeDefaultFeatures.addDefaultExtraVegetation(builder2, true);
        float f = bl ? 0.05f : (bl2 ? 0.2f : 0.8f);
        return OverworldBiomes.biome(true, f, bl3 ? 0.4f : 0.3f, bl ? 4020182 : 4159204, 329011, null, null, null, builder, builder2, NORMAL_MUSIC);
    }

    public static Biome theVoid(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        builder.addFeature(GenerationStep.Decoration.TOP_LAYER_MODIFICATION, MiscOverworldPlacements.VOID_START_PLATFORM);
        return OverworldBiomes.biome(false, 0.5f, 0.5f, new MobSpawnSettings.Builder(), builder, NORMAL_MUSIC);
    }

    public static Biome meadowOrCherryGrove(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2, boolean bl) {
        BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        MobSpawnSettings.Builder builder2 = new MobSpawnSettings.Builder();
        builder2.addSpawn(MobCategory.CREATURE, 1, new MobSpawnSettings.SpawnerData(bl ? EntityType.PIG : EntityType.DONKEY, 1, 2)).addSpawn(MobCategory.CREATURE, 2, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 2, 6)).addSpawn(MobCategory.CREATURE, 2, new MobSpawnSettings.SpawnerData(EntityType.SHEEP, 2, 4));
        BiomeDefaultFeatures.commonSpawns(builder2);
        OverworldBiomes.globalOverworldGeneration(builder);
        BiomeDefaultFeatures.addPlainGrass(builder);
        BiomeDefaultFeatures.addDefaultOres(builder);
        BiomeDefaultFeatures.addDefaultSoftDisks(builder);
        if (bl) {
            BiomeDefaultFeatures.addCherryGroveVegetation(builder);
        } else {
            BiomeDefaultFeatures.addMeadowVegetation(builder);
        }
        BiomeDefaultFeatures.addExtraEmeralds(builder);
        BiomeDefaultFeatures.addInfestedStone(builder);
        Music music = Musics.createGameMusic(bl ? SoundEvents.MUSIC_BIOME_CHERRY_GROVE : SoundEvents.MUSIC_BIOME_MEADOW);
        if (bl) {
            return OverworldBiomes.biome(true, 0.5f, 0.8f, 6141935, 6141935, 11983713, 11983713, null, builder2, builder, music);
        }
        return OverworldBiomes.biome(true, 0.5f, 0.8f, 937679, 329011, null, null, null, builder2, builder, music);
    }

    public static Biome frozenPeaks(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        MobSpawnSettings.Builder builder2 = new MobSpawnSettings.Builder();
        builder2.addSpawn(MobCategory.CREATURE, 5, new MobSpawnSettings.SpawnerData(EntityType.GOAT, 1, 3));
        BiomeDefaultFeatures.commonSpawns(builder2);
        OverworldBiomes.globalOverworldGeneration(builder);
        BiomeDefaultFeatures.addFrozenSprings(builder);
        BiomeDefaultFeatures.addDefaultOres(builder);
        BiomeDefaultFeatures.addDefaultSoftDisks(builder);
        BiomeDefaultFeatures.addExtraEmeralds(builder);
        BiomeDefaultFeatures.addInfestedStone(builder);
        Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_FROZEN_PEAKS);
        return OverworldBiomes.biome(true, -0.7f, 0.9f, builder2, builder, music);
    }

    public static Biome jaggedPeaks(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        MobSpawnSettings.Builder builder2 = new MobSpawnSettings.Builder();
        builder2.addSpawn(MobCategory.CREATURE, 5, new MobSpawnSettings.SpawnerData(EntityType.GOAT, 1, 3));
        BiomeDefaultFeatures.commonSpawns(builder2);
        OverworldBiomes.globalOverworldGeneration(builder);
        BiomeDefaultFeatures.addFrozenSprings(builder);
        BiomeDefaultFeatures.addDefaultOres(builder);
        BiomeDefaultFeatures.addDefaultSoftDisks(builder);
        BiomeDefaultFeatures.addExtraEmeralds(builder);
        BiomeDefaultFeatures.addInfestedStone(builder);
        Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_JAGGED_PEAKS);
        return OverworldBiomes.biome(true, -0.7f, 0.9f, builder2, builder, music);
    }

    public static Biome stonyPeaks(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        MobSpawnSettings.Builder builder2 = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.commonSpawns(builder2);
        OverworldBiomes.globalOverworldGeneration(builder);
        BiomeDefaultFeatures.addDefaultOres(builder);
        BiomeDefaultFeatures.addDefaultSoftDisks(builder);
        BiomeDefaultFeatures.addExtraEmeralds(builder);
        BiomeDefaultFeatures.addInfestedStone(builder);
        Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_STONY_PEAKS);
        return OverworldBiomes.biome(true, 1.0f, 0.3f, builder2, builder, music);
    }

    public static Biome snowySlopes(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        MobSpawnSettings.Builder builder2 = new MobSpawnSettings.Builder();
        builder2.addSpawn(MobCategory.CREATURE, 4, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 2, 3)).addSpawn(MobCategory.CREATURE, 5, new MobSpawnSettings.SpawnerData(EntityType.GOAT, 1, 3));
        BiomeDefaultFeatures.commonSpawns(builder2);
        OverworldBiomes.globalOverworldGeneration(builder);
        BiomeDefaultFeatures.addFrozenSprings(builder);
        BiomeDefaultFeatures.addDefaultOres(builder);
        BiomeDefaultFeatures.addDefaultSoftDisks(builder);
        BiomeDefaultFeatures.addDefaultExtraVegetation(builder, false);
        BiomeDefaultFeatures.addExtraEmeralds(builder);
        BiomeDefaultFeatures.addInfestedStone(builder);
        Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_SNOWY_SLOPES);
        return OverworldBiomes.biome(true, -0.3f, 0.9f, builder2, builder, music);
    }

    public static Biome grove(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        BiomeGenerationSettings.Builder builder = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        MobSpawnSettings.Builder builder2 = new MobSpawnSettings.Builder();
        builder2.addSpawn(MobCategory.CREATURE, 1, new MobSpawnSettings.SpawnerData(EntityType.WOLF, 1, 1)).addSpawn(MobCategory.CREATURE, 8, new MobSpawnSettings.SpawnerData(EntityType.RABBIT, 2, 3)).addSpawn(MobCategory.CREATURE, 4, new MobSpawnSettings.SpawnerData(EntityType.FOX, 2, 4));
        BiomeDefaultFeatures.commonSpawns(builder2);
        OverworldBiomes.globalOverworldGeneration(builder);
        BiomeDefaultFeatures.addFrozenSprings(builder);
        BiomeDefaultFeatures.addDefaultOres(builder);
        BiomeDefaultFeatures.addDefaultSoftDisks(builder);
        BiomeDefaultFeatures.addGroveTrees(builder);
        BiomeDefaultFeatures.addDefaultExtraVegetation(builder, false);
        BiomeDefaultFeatures.addExtraEmeralds(builder);
        BiomeDefaultFeatures.addInfestedStone(builder);
        Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_GROVE);
        return OverworldBiomes.biome(true, -0.2f, 0.8f, builder2, builder, music);
    }

    public static Biome lushCaves(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
        builder.addSpawn(MobCategory.AXOLOTLS, 10, new MobSpawnSettings.SpawnerData(EntityType.AXOLOTL, 4, 6));
        builder.addSpawn(MobCategory.WATER_AMBIENT, 25, new MobSpawnSettings.SpawnerData(EntityType.TROPICAL_FISH, 8, 8));
        BiomeDefaultFeatures.commonSpawns(builder);
        BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        OverworldBiomes.globalOverworldGeneration(builder2);
        BiomeDefaultFeatures.addPlainGrass(builder2);
        BiomeDefaultFeatures.addDefaultOres(builder2);
        BiomeDefaultFeatures.addLushCavesSpecialOres(builder2);
        BiomeDefaultFeatures.addDefaultSoftDisks(builder2);
        BiomeDefaultFeatures.addLushCavesVegetationFeatures(builder2);
        Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_LUSH_CAVES);
        return OverworldBiomes.biome(true, 0.5f, 0.5f, builder, builder2, music);
    }

    public static Biome dripstoneCaves(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
        BiomeDefaultFeatures.dripstoneCavesSpawns(builder);
        BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        OverworldBiomes.globalOverworldGeneration(builder2);
        BiomeDefaultFeatures.addPlainGrass(builder2);
        BiomeDefaultFeatures.addDefaultOres(builder2, true);
        BiomeDefaultFeatures.addDefaultSoftDisks(builder2);
        BiomeDefaultFeatures.addPlainVegetation(builder2);
        BiomeDefaultFeatures.addDefaultMushrooms(builder2);
        BiomeDefaultFeatures.addDefaultExtraVegetation(builder2, false);
        BiomeDefaultFeatures.addDripstone(builder2);
        Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_DRIPSTONE_CAVES);
        return OverworldBiomes.biome(true, 0.8f, 0.4f, builder, builder2, music);
    }

    public static Biome deepDark(HolderGetter<PlacedFeature> holderGetter, HolderGetter<ConfiguredWorldCarver<?>> holderGetter2) {
        MobSpawnSettings.Builder builder = new MobSpawnSettings.Builder();
        BiomeGenerationSettings.Builder builder2 = new BiomeGenerationSettings.Builder(holderGetter, holderGetter2);
        builder2.addCarver(Carvers.CAVE);
        builder2.addCarver(Carvers.CAVE_EXTRA_UNDERGROUND);
        builder2.addCarver(Carvers.CANYON);
        BiomeDefaultFeatures.addDefaultCrystalFormations(builder2);
        BiomeDefaultFeatures.addDefaultMonsterRoom(builder2);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(builder2);
        BiomeDefaultFeatures.addSurfaceFreezing(builder2);
        BiomeDefaultFeatures.addPlainGrass(builder2);
        BiomeDefaultFeatures.addDefaultOres(builder2);
        BiomeDefaultFeatures.addDefaultSoftDisks(builder2);
        BiomeDefaultFeatures.addPlainVegetation(builder2);
        BiomeDefaultFeatures.addDefaultMushrooms(builder2);
        BiomeDefaultFeatures.addDefaultExtraVegetation(builder2, false);
        BiomeDefaultFeatures.addSculk(builder2);
        Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_DEEP_DARK);
        return OverworldBiomes.biome(true, 0.8f, 0.4f, builder, builder2, music);
    }
}

