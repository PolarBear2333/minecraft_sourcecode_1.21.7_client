/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.resources.model;

import java.util.Map;
import java.util.stream.Collectors;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

public class EquipmentAssetManager
extends SimpleJsonResourceReloadListener<EquipmentClientInfo> {
    public static final EquipmentClientInfo MISSING = new EquipmentClientInfo(Map.of());
    private static final FileToIdConverter ASSET_LISTER = FileToIdConverter.json("equipment");
    private Map<ResourceKey<EquipmentAsset>, EquipmentClientInfo> equipmentAssets = Map.of();

    public EquipmentAssetManager() {
        super(EquipmentClientInfo.CODEC, ASSET_LISTER);
    }

    @Override
    protected void apply(Map<ResourceLocation, EquipmentClientInfo> map, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        this.equipmentAssets = map.entrySet().stream().collect(Collectors.toUnmodifiableMap(entry -> ResourceKey.create(EquipmentAssets.ROOT_ID, (ResourceLocation)entry.getKey()), Map.Entry::getValue));
    }

    public EquipmentClientInfo get(ResourceKey<EquipmentAsset> resourceKey) {
        return this.equipmentAssets.getOrDefault(resourceKey, MISSING);
    }
}

