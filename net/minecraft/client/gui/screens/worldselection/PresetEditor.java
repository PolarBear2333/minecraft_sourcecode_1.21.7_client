/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.worldselection;

import java.util.Map;
import java.util.Optional;
import net.minecraft.client.gui.screens.CreateBuffetWorldScreen;
import net.minecraft.client.gui.screens.CreateFlatWorldScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

public interface PresetEditor {
    public static final Map<Optional<ResourceKey<WorldPreset>>, PresetEditor> EDITORS = Map.of(Optional.of(WorldPresets.FLAT), (createWorldScreen, worldCreationContext) -> {
        ChunkGenerator chunkGenerator = worldCreationContext.selectedDimensions().overworld();
        RegistryAccess.Frozen frozen = worldCreationContext.worldgenLoadContext();
        HolderLookup.RegistryLookup registryLookup = frozen.lookupOrThrow(Registries.BIOME);
        HolderLookup.RegistryLookup registryLookup2 = frozen.lookupOrThrow(Registries.STRUCTURE_SET);
        HolderLookup.RegistryLookup registryLookup3 = frozen.lookupOrThrow(Registries.PLACED_FEATURE);
        return new CreateFlatWorldScreen(createWorldScreen, flatLevelGeneratorSettings -> createWorldScreen.getUiState().updateDimensions(PresetEditor.flatWorldConfigurator(flatLevelGeneratorSettings)), chunkGenerator instanceof FlatLevelSource ? ((FlatLevelSource)chunkGenerator).settings() : FlatLevelGeneratorSettings.getDefault(registryLookup, registryLookup2, registryLookup3));
    }, Optional.of(WorldPresets.SINGLE_BIOME_SURFACE), (createWorldScreen, worldCreationContext) -> new CreateBuffetWorldScreen(createWorldScreen, worldCreationContext, holder -> createWorldScreen.getUiState().updateDimensions(PresetEditor.fixedBiomeConfigurator(holder))));

    public Screen createEditScreen(CreateWorldScreen var1, WorldCreationContext var2);

    public static WorldCreationContext.DimensionsUpdater flatWorldConfigurator(FlatLevelGeneratorSettings flatLevelGeneratorSettings) {
        return (frozen, worldDimensions) -> {
            FlatLevelSource flatLevelSource = new FlatLevelSource(flatLevelGeneratorSettings);
            return worldDimensions.replaceOverworldGenerator((HolderLookup.Provider)frozen, flatLevelSource);
        };
    }

    private static WorldCreationContext.DimensionsUpdater fixedBiomeConfigurator(Holder<Biome> holder) {
        return (frozen, worldDimensions) -> {
            HolderLookup.RegistryLookup registryLookup = frozen.lookupOrThrow(Registries.NOISE_SETTINGS);
            Holder.Reference reference = registryLookup.getOrThrow(NoiseGeneratorSettings.OVERWORLD);
            FixedBiomeSource fixedBiomeSource = new FixedBiomeSource(holder);
            NoiseBasedChunkGenerator noiseBasedChunkGenerator = new NoiseBasedChunkGenerator((BiomeSource)fixedBiomeSource, reference);
            return worldDimensions.replaceOverworldGenerator((HolderLookup.Provider)frozen, noiseBasedChunkGenerator);
        };
    }
}

