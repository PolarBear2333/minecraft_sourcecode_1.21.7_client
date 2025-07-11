/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.worldselection;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.client.gui.screens.worldselection.PresetEditor;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.WorldPresetTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

public class WorldCreationUiState {
    private static final Component DEFAULT_WORLD_NAME = Component.translatable("selectWorld.newWorld");
    private final List<Consumer<WorldCreationUiState>> listeners = new ArrayList<Consumer<WorldCreationUiState>>();
    private String name = DEFAULT_WORLD_NAME.getString();
    private SelectedGameMode gameMode = SelectedGameMode.SURVIVAL;
    private Difficulty difficulty = Difficulty.NORMAL;
    @Nullable
    private Boolean allowCommands;
    private String seed;
    private boolean generateStructures;
    private boolean bonusChest;
    private final Path savesFolder;
    private String targetFolder;
    private WorldCreationContext settings;
    private WorldTypeEntry worldType;
    private final List<WorldTypeEntry> normalPresetList = new ArrayList<WorldTypeEntry>();
    private final List<WorldTypeEntry> altPresetList = new ArrayList<WorldTypeEntry>();
    private GameRules gameRules;

    public WorldCreationUiState(Path path, WorldCreationContext worldCreationContext, Optional<ResourceKey<WorldPreset>> optional, OptionalLong optionalLong) {
        this.savesFolder = path;
        this.settings = worldCreationContext;
        this.worldType = new WorldTypeEntry(WorldCreationUiState.findPreset(worldCreationContext, optional).orElse(null));
        this.updatePresetLists();
        this.seed = optionalLong.isPresent() ? Long.toString(optionalLong.getAsLong()) : "";
        this.generateStructures = worldCreationContext.options().generateStructures();
        this.bonusChest = worldCreationContext.options().generateBonusChest();
        this.targetFolder = this.findResultFolder(this.name);
        this.gameMode = worldCreationContext.initialWorldCreationOptions().selectedGameMode();
        this.gameRules = new GameRules(worldCreationContext.dataConfiguration().enabledFeatures());
        worldCreationContext.initialWorldCreationOptions().disabledGameRules().forEach(key -> ((GameRules.BooleanValue)this.gameRules.getRule(key)).set(false, null));
        Optional.ofNullable(worldCreationContext.initialWorldCreationOptions().flatLevelPreset()).flatMap(resourceKey -> worldCreationContext.worldgenLoadContext().lookup(Registries.FLAT_LEVEL_GENERATOR_PRESET).flatMap(registry -> registry.get(resourceKey))).map(reference -> ((FlatLevelGeneratorPreset)reference.value()).settings()).ifPresent(flatLevelGeneratorSettings -> this.updateDimensions(PresetEditor.flatWorldConfigurator(flatLevelGeneratorSettings)));
    }

    public void addListener(Consumer<WorldCreationUiState> consumer) {
        this.listeners.add(consumer);
    }

    public void onChanged() {
        boolean bl;
        boolean bl2 = this.isBonusChest();
        if (bl2 != this.settings.options().generateBonusChest()) {
            this.settings = this.settings.withOptions(worldOptions -> worldOptions.withBonusChest(bl2));
        }
        if ((bl = this.isGenerateStructures()) != this.settings.options().generateStructures()) {
            this.settings = this.settings.withOptions(worldOptions -> worldOptions.withStructures(bl));
        }
        for (Consumer<WorldCreationUiState> consumer : this.listeners) {
            consumer.accept(this);
        }
    }

    public void setName(String string) {
        this.name = string;
        this.targetFolder = this.findResultFolder(string);
        this.onChanged();
    }

    private String findResultFolder(String string) {
        String string2 = string.trim();
        try {
            return FileUtil.findAvailableName(this.savesFolder, !string2.isEmpty() ? string2 : DEFAULT_WORLD_NAME.getString(), "");
        }
        catch (Exception exception) {
            try {
                return FileUtil.findAvailableName(this.savesFolder, "World", "");
            }
            catch (IOException iOException) {
                throw new RuntimeException("Could not create save folder", iOException);
            }
        }
    }

    public String getName() {
        return this.name;
    }

    public String getTargetFolder() {
        return this.targetFolder;
    }

    public void setGameMode(SelectedGameMode selectedGameMode) {
        this.gameMode = selectedGameMode;
        this.onChanged();
    }

    public SelectedGameMode getGameMode() {
        if (this.isDebug()) {
            return SelectedGameMode.DEBUG;
        }
        return this.gameMode;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
        this.onChanged();
    }

    public Difficulty getDifficulty() {
        if (this.isHardcore()) {
            return Difficulty.HARD;
        }
        return this.difficulty;
    }

    public boolean isHardcore() {
        return this.getGameMode() == SelectedGameMode.HARDCORE;
    }

    public void setAllowCommands(boolean bl) {
        this.allowCommands = bl;
        this.onChanged();
    }

    public boolean isAllowCommands() {
        if (this.isDebug()) {
            return true;
        }
        if (this.isHardcore()) {
            return false;
        }
        if (this.allowCommands == null) {
            return this.getGameMode() == SelectedGameMode.CREATIVE;
        }
        return this.allowCommands;
    }

    public void setSeed(String string) {
        this.seed = string;
        this.settings = this.settings.withOptions(worldOptions -> worldOptions.withSeed(WorldOptions.parseSeed(this.getSeed())));
        this.onChanged();
    }

    public String getSeed() {
        return this.seed;
    }

    public void setGenerateStructures(boolean bl) {
        this.generateStructures = bl;
        this.onChanged();
    }

    public boolean isGenerateStructures() {
        if (this.isDebug()) {
            return false;
        }
        return this.generateStructures;
    }

    public void setBonusChest(boolean bl) {
        this.bonusChest = bl;
        this.onChanged();
    }

    public boolean isBonusChest() {
        if (this.isDebug() || this.isHardcore()) {
            return false;
        }
        return this.bonusChest;
    }

    public void setSettings(WorldCreationContext worldCreationContext) {
        this.settings = worldCreationContext;
        this.updatePresetLists();
        this.onChanged();
    }

    public WorldCreationContext getSettings() {
        return this.settings;
    }

    public void updateDimensions(WorldCreationContext.DimensionsUpdater dimensionsUpdater) {
        this.settings = this.settings.withDimensions(dimensionsUpdater);
        this.onChanged();
    }

    protected boolean tryUpdateDataConfiguration(WorldDataConfiguration worldDataConfiguration) {
        WorldDataConfiguration worldDataConfiguration2 = this.settings.dataConfiguration();
        if (worldDataConfiguration2.dataPacks().getEnabled().equals(worldDataConfiguration.dataPacks().getEnabled()) && worldDataConfiguration2.enabledFeatures().equals(worldDataConfiguration.enabledFeatures())) {
            this.settings = new WorldCreationContext(this.settings.options(), this.settings.datapackDimensions(), this.settings.selectedDimensions(), this.settings.worldgenRegistries(), this.settings.dataPackResources(), worldDataConfiguration, this.settings.initialWorldCreationOptions());
            return true;
        }
        return false;
    }

    public boolean isDebug() {
        return this.settings.selectedDimensions().isDebug();
    }

    public void setWorldType(WorldTypeEntry worldTypeEntry) {
        this.worldType = worldTypeEntry;
        Holder<WorldPreset> holder = worldTypeEntry.preset();
        if (holder != null) {
            this.updateDimensions((frozen, worldDimensions) -> ((WorldPreset)holder.value()).createWorldDimensions());
        }
    }

    public WorldTypeEntry getWorldType() {
        return this.worldType;
    }

    @Nullable
    public PresetEditor getPresetEditor() {
        Holder<WorldPreset> holder = this.getWorldType().preset();
        return holder != null ? PresetEditor.EDITORS.get(holder.unwrapKey()) : null;
    }

    public List<WorldTypeEntry> getNormalPresetList() {
        return this.normalPresetList;
    }

    public List<WorldTypeEntry> getAltPresetList() {
        return this.altPresetList;
    }

    private void updatePresetLists() {
        HolderLookup.RegistryLookup registryLookup = this.getSettings().worldgenLoadContext().lookupOrThrow(Registries.WORLD_PRESET);
        this.normalPresetList.clear();
        this.normalPresetList.addAll(WorldCreationUiState.getNonEmptyList((Registry<WorldPreset>)registryLookup, WorldPresetTags.NORMAL).orElseGet(() -> WorldCreationUiState.lambda$updatePresetLists$9((Registry)registryLookup)));
        this.altPresetList.clear();
        this.altPresetList.addAll((Collection<WorldTypeEntry>)WorldCreationUiState.getNonEmptyList((Registry<WorldPreset>)registryLookup, WorldPresetTags.EXTENDED).orElse(this.normalPresetList));
        Holder<WorldPreset> holder = this.worldType.preset();
        if (holder != null) {
            boolean bl;
            WorldTypeEntry worldTypeEntry = WorldCreationUiState.findPreset(this.getSettings(), holder.unwrapKey()).map(WorldTypeEntry::new).orElse(this.normalPresetList.getFirst());
            boolean bl2 = bl = PresetEditor.EDITORS.get(holder.unwrapKey()) != null;
            if (bl) {
                this.worldType = worldTypeEntry;
            } else {
                this.setWorldType(worldTypeEntry);
            }
        }
    }

    private static Optional<Holder<WorldPreset>> findPreset(WorldCreationContext worldCreationContext, Optional<ResourceKey<WorldPreset>> optional) {
        return optional.flatMap(resourceKey -> worldCreationContext.worldgenLoadContext().lookupOrThrow(Registries.WORLD_PRESET).get((ResourceKey)resourceKey));
    }

    private static Optional<List<WorldTypeEntry>> getNonEmptyList(Registry<WorldPreset> registry, TagKey<WorldPreset> tagKey) {
        return registry.get(tagKey).map(named -> named.stream().map(WorldTypeEntry::new).toList()).filter(list -> !list.isEmpty());
    }

    public void setGameRules(GameRules gameRules) {
        this.gameRules = gameRules;
        this.onChanged();
    }

    public GameRules getGameRules() {
        return this.gameRules;
    }

    private static /* synthetic */ List lambda$updatePresetLists$9(Registry registry) {
        return registry.listElements().map(WorldTypeEntry::new).toList();
    }

    public static enum SelectedGameMode {
        SURVIVAL("survival", GameType.SURVIVAL),
        HARDCORE("hardcore", GameType.SURVIVAL),
        CREATIVE("creative", GameType.CREATIVE),
        DEBUG("spectator", GameType.SPECTATOR);

        public final GameType gameType;
        public final Component displayName;
        private final Component info;

        private SelectedGameMode(String string2, GameType gameType) {
            this.gameType = gameType;
            this.displayName = Component.translatable("selectWorld.gameMode." + string2);
            this.info = Component.translatable("selectWorld.gameMode." + string2 + ".info");
        }

        public Component getInfo() {
            return this.info;
        }
    }

    public record WorldTypeEntry(@Nullable Holder<WorldPreset> preset) {
        private static final Component CUSTOM_WORLD_DESCRIPTION = Component.translatable("generator.custom");

        public Component describePreset() {
            return Optional.ofNullable(this.preset).flatMap(Holder::unwrapKey).map(resourceKey -> Component.translatable(resourceKey.location().toLanguageKey("generator"))).orElse(CUSTOM_WORLD_DESCRIPTION);
        }

        public boolean isAmplified() {
            return Optional.ofNullable(this.preset).flatMap(Holder::unwrapKey).filter(resourceKey -> resourceKey.equals(WorldPresets.AMPLIFIED)).isPresent();
        }
    }
}

