/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Splitter
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.gui.screens;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.CreateFlatWorldScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FlatLevelGeneratorPresetTags;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.flat.FlatLayerInfo;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import org.slf4j.Logger;

public class PresetFlatWorldScreen
extends Screen {
    static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int SLOT_BG_SIZE = 18;
    private static final int SLOT_STAT_HEIGHT = 20;
    private static final int SLOT_BG_X = 1;
    private static final int SLOT_BG_Y = 1;
    private static final int SLOT_FG_X = 2;
    private static final int SLOT_FG_Y = 2;
    private static final ResourceKey<Biome> DEFAULT_BIOME = Biomes.PLAINS;
    public static final Component UNKNOWN_PRESET = Component.translatable("flat_world_preset.unknown");
    private final CreateFlatWorldScreen parent;
    private Component shareText;
    private Component listText;
    private PresetsList list;
    private Button selectButton;
    EditBox export;
    FlatLevelGeneratorSettings settings;

    public PresetFlatWorldScreen(CreateFlatWorldScreen createFlatWorldScreen) {
        super(Component.translatable("createWorld.customize.presets.title"));
        this.parent = createFlatWorldScreen;
    }

    @Nullable
    private static FlatLayerInfo getLayerInfoFromString(HolderGetter<Block> holderGetter, String string, int n) {
        Optional<Holder.Reference<Block>> optional;
        int n2;
        String string2;
        List list = Splitter.on((char)'*').limit(2).splitToList((CharSequence)string);
        if (list.size() == 2) {
            string2 = (String)list.get(1);
            try {
                n2 = Math.max(Integer.parseInt((String)list.get(0)), 0);
            }
            catch (NumberFormatException numberFormatException) {
                LOGGER.error("Error while parsing flat world string", (Throwable)numberFormatException);
                return null;
            }
        } else {
            string2 = (String)list.get(0);
            n2 = 1;
        }
        int n3 = Math.min(n + n2, DimensionType.Y_SIZE);
        int n4 = n3 - n;
        try {
            optional = holderGetter.get(ResourceKey.create(Registries.BLOCK, ResourceLocation.parse(string2)));
        }
        catch (Exception exception) {
            LOGGER.error("Error while parsing flat world string", (Throwable)exception);
            return null;
        }
        if (optional.isEmpty()) {
            LOGGER.error("Error while parsing flat world string => Unknown block, {}", (Object)string2);
            return null;
        }
        return new FlatLayerInfo(n4, optional.get().value());
    }

    private static List<FlatLayerInfo> getLayersInfoFromString(HolderGetter<Block> holderGetter, String string) {
        ArrayList arrayList = Lists.newArrayList();
        String[] stringArray = string.split(",");
        int n = 0;
        for (String string2 : stringArray) {
            FlatLayerInfo flatLayerInfo = PresetFlatWorldScreen.getLayerInfoFromString(holderGetter, string2, n);
            if (flatLayerInfo == null) {
                return Collections.emptyList();
            }
            int n2 = DimensionType.Y_SIZE - n;
            if (n2 <= 0) continue;
            arrayList.add(flatLayerInfo.heightLimited(n2));
            n += flatLayerInfo.getHeight();
        }
        return arrayList;
    }

    public static FlatLevelGeneratorSettings fromString(HolderGetter<Block> holderGetter, HolderGetter<Biome> holderGetter2, HolderGetter<StructureSet> holderGetter3, HolderGetter<PlacedFeature> holderGetter4, String string, FlatLevelGeneratorSettings flatLevelGeneratorSettings) {
        Holder.Reference<Biome> reference;
        Iterator iterator = Splitter.on((char)';').split((CharSequence)string).iterator();
        if (!iterator.hasNext()) {
            return FlatLevelGeneratorSettings.getDefault(holderGetter2, holderGetter3, holderGetter4);
        }
        List<FlatLayerInfo> list = PresetFlatWorldScreen.getLayersInfoFromString(holderGetter, (String)iterator.next());
        if (list.isEmpty()) {
            return FlatLevelGeneratorSettings.getDefault(holderGetter2, holderGetter3, holderGetter4);
        }
        Holder<Biome> holder = reference = holderGetter2.getOrThrow(DEFAULT_BIOME);
        if (iterator.hasNext()) {
            String string2 = (String)iterator.next();
            holder = Optional.ofNullable(ResourceLocation.tryParse(string2)).map(resourceLocation -> ResourceKey.create(Registries.BIOME, resourceLocation)).flatMap(holderGetter2::get).orElseGet(() -> {
                LOGGER.warn("Invalid biome: {}", (Object)string2);
                return reference;
            });
        }
        return flatLevelGeneratorSettings.withBiomeAndLayers(list, flatLevelGeneratorSettings.structureOverrides(), holder);
    }

    static String save(FlatLevelGeneratorSettings flatLevelGeneratorSettings) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < flatLevelGeneratorSettings.getLayersInfo().size(); ++i) {
            if (i > 0) {
                stringBuilder.append(",");
            }
            stringBuilder.append(flatLevelGeneratorSettings.getLayersInfo().get(i));
        }
        stringBuilder.append(";");
        stringBuilder.append(flatLevelGeneratorSettings.getBiome().unwrapKey().map(ResourceKey::location).orElseThrow(() -> new IllegalStateException("Biome not registered")));
        return stringBuilder.toString();
    }

    @Override
    protected void init() {
        this.shareText = Component.translatable("createWorld.customize.presets.share");
        this.listText = Component.translatable("createWorld.customize.presets.list");
        this.export = new EditBox(this.font, 50, 40, this.width - 100, 20, this.shareText);
        this.export.setMaxLength(1230);
        WorldCreationContext worldCreationContext = this.parent.parent.getUiState().getSettings();
        RegistryAccess.Frozen frozen = worldCreationContext.worldgenLoadContext();
        FeatureFlagSet featureFlagSet = worldCreationContext.dataConfiguration().enabledFeatures();
        HolderLookup.RegistryLookup registryLookup = frozen.lookupOrThrow(Registries.BIOME);
        HolderLookup.RegistryLookup registryLookup2 = frozen.lookupOrThrow(Registries.STRUCTURE_SET);
        HolderLookup.RegistryLookup registryLookup3 = frozen.lookupOrThrow(Registries.PLACED_FEATURE);
        HolderLookup.RegistryLookup registryLookup4 = frozen.lookupOrThrow(Registries.BLOCK).filterFeatures(featureFlagSet);
        this.export.setValue(PresetFlatWorldScreen.save(this.parent.settings()));
        this.settings = this.parent.settings();
        this.addWidget(this.export);
        this.list = this.addRenderableWidget(new PresetsList(frozen, featureFlagSet));
        this.selectButton = this.addRenderableWidget(Button.builder(Component.translatable("createWorld.customize.presets.select"), button -> {
            FlatLevelGeneratorSettings flatLevelGeneratorSettings = PresetFlatWorldScreen.fromString(registryLookup4, registryLookup, registryLookup2, registryLookup3, this.export.getValue(), this.settings);
            this.parent.setConfig(flatLevelGeneratorSettings);
            this.minecraft.setScreen(this.parent);
        }).bounds(this.width / 2 - 155, this.height - 28, 150, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.minecraft.setScreen(this.parent)).bounds(this.width / 2 + 5, this.height - 28, 150, 20).build());
        this.updateButtonValidity(this.list.getSelected() != null);
    }

    @Override
    public boolean mouseScrolled(double d, double d2, double d3, double d4) {
        return this.list.mouseScrolled(d, d2, d3, d4);
    }

    @Override
    public void resize(Minecraft minecraft, int n, int n2) {
        String string = this.export.getValue();
        this.init(minecraft, n, n2);
        this.export.setValue(string);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.render(guiGraphics, n, n2, f);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 8, -1);
        guiGraphics.drawString(this.font, this.shareText, 51, 30, -6250336);
        guiGraphics.drawString(this.font, this.listText, 51, 68, -6250336);
        this.export.render(guiGraphics, n, n2, f);
    }

    public void updateButtonValidity(boolean bl) {
        this.selectButton.active = bl || this.export.getValue().length() > 1;
    }

    class PresetsList
    extends ObjectSelectionList<Entry> {
        public PresetsList(RegistryAccess registryAccess, FeatureFlagSet featureFlagSet) {
            super(PresetFlatWorldScreen.this.minecraft, PresetFlatWorldScreen.this.width, PresetFlatWorldScreen.this.height - 117, 80, 24);
            for (Holder<FlatLevelGeneratorPreset> holder : registryAccess.lookupOrThrow(Registries.FLAT_LEVEL_GENERATOR_PRESET).getTagOrEmpty(FlatLevelGeneratorPresetTags.VISIBLE)) {
                Set set = holder.value().settings().getLayersInfo().stream().map(flatLayerInfo -> flatLayerInfo.getBlockState().getBlock()).filter(block -> !block.isEnabled(featureFlagSet)).collect(Collectors.toSet());
                if (!set.isEmpty()) {
                    LOGGER.info("Discarding flat world preset {} since it contains experimental blocks {}", (Object)holder.unwrapKey().map(resourceKey -> resourceKey.location().toString()).orElse("<unknown>"), set);
                    continue;
                }
                this.addEntry(new Entry(holder));
            }
        }

        @Override
        public void setSelected(@Nullable Entry entry) {
            super.setSelected(entry);
            PresetFlatWorldScreen.this.updateButtonValidity(entry != null);
        }

        @Override
        public boolean keyPressed(int n, int n2, int n3) {
            if (super.keyPressed(n, n2, n3)) {
                return true;
            }
            if (CommonInputs.selected(n) && this.getSelected() != null) {
                ((Entry)this.getSelected()).select();
            }
            return false;
        }

        public class Entry
        extends ObjectSelectionList.Entry<Entry> {
            private static final ResourceLocation STATS_ICON_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/container/stats_icons.png");
            private final FlatLevelGeneratorPreset preset;
            private final Component name;

            public Entry(Holder<FlatLevelGeneratorPreset> holder) {
                this.preset = holder.value();
                this.name = holder.unwrapKey().map(resourceKey -> Component.translatable(resourceKey.location().toLanguageKey("flat_world_preset"))).orElse(UNKNOWN_PRESET);
            }

            @Override
            public void render(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
                this.blitSlot(guiGraphics, n3, n2, this.preset.displayItem().value());
                guiGraphics.drawString(PresetFlatWorldScreen.this.font, this.name, n3 + 18 + 5, n2 + 6, -1);
            }

            @Override
            public boolean mouseClicked(double d, double d2, int n) {
                this.select();
                return super.mouseClicked(d, d2, n);
            }

            void select() {
                PresetsList.this.setSelected(this);
                PresetFlatWorldScreen.this.settings = this.preset.settings();
                PresetFlatWorldScreen.this.export.setValue(PresetFlatWorldScreen.save(PresetFlatWorldScreen.this.settings));
                PresetFlatWorldScreen.this.export.moveCursorToStart(false);
            }

            private void blitSlot(GuiGraphics guiGraphics, int n, int n2, Item item) {
                this.blitSlotBg(guiGraphics, n + 1, n2 + 1);
                guiGraphics.renderFakeItem(new ItemStack(item), n + 2, n2 + 2);
            }

            private void blitSlotBg(GuiGraphics guiGraphics, int n, int n2) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SPRITE, n, n2, 18, 18);
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", this.name);
            }
        }
    }
}

