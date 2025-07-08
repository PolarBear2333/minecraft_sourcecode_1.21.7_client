/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.worldselection;

import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import net.minecraft.client.gui.screens.worldselection.InitialWorldCreationOptions;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.WorldOptions;

public record WorldCreationContext(WorldOptions options, Registry<LevelStem> datapackDimensions, WorldDimensions selectedDimensions, LayeredRegistryAccess<RegistryLayer> worldgenRegistries, ReloadableServerResources dataPackResources, WorldDataConfiguration dataConfiguration, InitialWorldCreationOptions initialWorldCreationOptions) {
    public WorldCreationContext(WorldGenSettings worldGenSettings, LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, ReloadableServerResources reloadableServerResources, WorldDataConfiguration worldDataConfiguration) {
        this(worldGenSettings.options(), worldGenSettings.dimensions(), layeredRegistryAccess, reloadableServerResources, worldDataConfiguration, new InitialWorldCreationOptions(WorldCreationUiState.SelectedGameMode.SURVIVAL, Set.of(), null));
    }

    public WorldCreationContext(WorldOptions worldOptions, WorldDimensions worldDimensions, LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, ReloadableServerResources reloadableServerResources, WorldDataConfiguration worldDataConfiguration, InitialWorldCreationOptions initialWorldCreationOptions) {
        this(worldOptions, (Registry<LevelStem>)layeredRegistryAccess.getLayer(RegistryLayer.DIMENSIONS).lookupOrThrow(Registries.LEVEL_STEM), worldDimensions, layeredRegistryAccess.replaceFrom(RegistryLayer.DIMENSIONS, new RegistryAccess.Frozen[0]), reloadableServerResources, worldDataConfiguration, initialWorldCreationOptions);
    }

    public WorldCreationContext withSettings(WorldOptions worldOptions, WorldDimensions worldDimensions) {
        return new WorldCreationContext(worldOptions, this.datapackDimensions, worldDimensions, this.worldgenRegistries, this.dataPackResources, this.dataConfiguration, this.initialWorldCreationOptions);
    }

    public WorldCreationContext withOptions(OptionsModifier optionsModifier) {
        return new WorldCreationContext((WorldOptions)optionsModifier.apply(this.options), this.datapackDimensions, this.selectedDimensions, this.worldgenRegistries, this.dataPackResources, this.dataConfiguration, this.initialWorldCreationOptions);
    }

    public WorldCreationContext withDimensions(DimensionsUpdater dimensionsUpdater) {
        return new WorldCreationContext(this.options, this.datapackDimensions, (WorldDimensions)dimensionsUpdater.apply(this.worldgenLoadContext(), this.selectedDimensions), this.worldgenRegistries, this.dataPackResources, this.dataConfiguration, this.initialWorldCreationOptions);
    }

    public RegistryAccess.Frozen worldgenLoadContext() {
        return this.worldgenRegistries.compositeAccess();
    }

    public void validate() {
        for (LevelStem levelStem : this.datapackDimensions()) {
            levelStem.generator().validate();
        }
    }

    public static interface OptionsModifier
    extends UnaryOperator<WorldOptions> {
    }

    @FunctionalInterface
    public static interface DimensionsUpdater
    extends BiFunction<RegistryAccess.Frozen, WorldDimensions, WorldDimensions> {
    }
}

