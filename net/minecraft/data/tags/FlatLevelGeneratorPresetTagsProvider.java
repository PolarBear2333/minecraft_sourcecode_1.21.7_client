/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.tags;

import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.KeyTagProvider;
import net.minecraft.tags.FlatLevelGeneratorPresetTags;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPresets;

public class FlatLevelGeneratorPresetTagsProvider
extends KeyTagProvider<FlatLevelGeneratorPreset> {
    public FlatLevelGeneratorPresetTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(packOutput, Registries.FLAT_LEVEL_GENERATOR_PRESET, completableFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(FlatLevelGeneratorPresetTags.VISIBLE).add(FlatLevelGeneratorPresets.CLASSIC_FLAT).add(FlatLevelGeneratorPresets.TUNNELERS_DREAM).add(FlatLevelGeneratorPresets.WATER_WORLD).add(FlatLevelGeneratorPresets.OVERWORLD).add(FlatLevelGeneratorPresets.SNOWY_KINGDOM).add(FlatLevelGeneratorPresets.BOTTOMLESS_PIT).add(FlatLevelGeneratorPresets.DESERT).add(FlatLevelGeneratorPresets.REDSTONE_READY).add(FlatLevelGeneratorPresets.THE_VOID);
    }
}

