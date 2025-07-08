/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.data.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

public class EquipmentAssetProvider
implements DataProvider {
    private final PackOutput.PathProvider pathProvider;

    public EquipmentAssetProvider(PackOutput packOutput) {
        this.pathProvider = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "equipment");
    }

    private static void bootstrap(BiConsumer<ResourceKey<EquipmentAsset>, EquipmentClientInfo> biConsumer) {
        ResourceKey<EquipmentAsset> resourceKey;
        DyeColor dyeColor;
        biConsumer.accept(EquipmentAssets.LEATHER, EquipmentClientInfo.builder().addHumanoidLayers(ResourceLocation.withDefaultNamespace("leather"), true).addHumanoidLayers(ResourceLocation.withDefaultNamespace("leather_overlay"), false).addLayers(EquipmentClientInfo.LayerType.HORSE_BODY, EquipmentClientInfo.Layer.leatherDyeable(ResourceLocation.withDefaultNamespace("leather"), true)).build());
        biConsumer.accept(EquipmentAssets.CHAINMAIL, EquipmentAssetProvider.onlyHumanoid("chainmail"));
        biConsumer.accept(EquipmentAssets.IRON, EquipmentAssetProvider.humanoidAndHorse("iron"));
        biConsumer.accept(EquipmentAssets.GOLD, EquipmentAssetProvider.humanoidAndHorse("gold"));
        biConsumer.accept(EquipmentAssets.DIAMOND, EquipmentAssetProvider.humanoidAndHorse("diamond"));
        biConsumer.accept(EquipmentAssets.TURTLE_SCUTE, EquipmentClientInfo.builder().addMainHumanoidLayer(ResourceLocation.withDefaultNamespace("turtle_scute"), false).build());
        biConsumer.accept(EquipmentAssets.NETHERITE, EquipmentAssetProvider.onlyHumanoid("netherite"));
        biConsumer.accept(EquipmentAssets.ARMADILLO_SCUTE, EquipmentClientInfo.builder().addLayers(EquipmentClientInfo.LayerType.WOLF_BODY, EquipmentClientInfo.Layer.onlyIfDyed(ResourceLocation.withDefaultNamespace("armadillo_scute"), false)).addLayers(EquipmentClientInfo.LayerType.WOLF_BODY, EquipmentClientInfo.Layer.onlyIfDyed(ResourceLocation.withDefaultNamespace("armadillo_scute_overlay"), true)).build());
        biConsumer.accept(EquipmentAssets.ELYTRA, EquipmentClientInfo.builder().addLayers(EquipmentClientInfo.LayerType.WINGS, new EquipmentClientInfo.Layer(ResourceLocation.withDefaultNamespace("elytra"), Optional.empty(), true)).build());
        EquipmentClientInfo.Layer layer = new EquipmentClientInfo.Layer(ResourceLocation.withDefaultNamespace("saddle"));
        biConsumer.accept(EquipmentAssets.SADDLE, EquipmentClientInfo.builder().addLayers(EquipmentClientInfo.LayerType.PIG_SADDLE, layer).addLayers(EquipmentClientInfo.LayerType.STRIDER_SADDLE, layer).addLayers(EquipmentClientInfo.LayerType.CAMEL_SADDLE, layer).addLayers(EquipmentClientInfo.LayerType.HORSE_SADDLE, layer).addLayers(EquipmentClientInfo.LayerType.DONKEY_SADDLE, layer).addLayers(EquipmentClientInfo.LayerType.MULE_SADDLE, layer).addLayers(EquipmentClientInfo.LayerType.SKELETON_HORSE_SADDLE, layer).addLayers(EquipmentClientInfo.LayerType.ZOMBIE_HORSE_SADDLE, layer).build());
        for (Map.Entry<DyeColor, ResourceKey<EquipmentAsset>> entry : EquipmentAssets.HARNESSES.entrySet()) {
            dyeColor = entry.getKey();
            resourceKey = entry.getValue();
            biConsumer.accept(resourceKey, EquipmentClientInfo.builder().addLayers(EquipmentClientInfo.LayerType.HAPPY_GHAST_BODY, EquipmentClientInfo.Layer.onlyIfDyed(ResourceLocation.withDefaultNamespace(dyeColor.getSerializedName() + "_harness"), false)).build());
        }
        for (Map.Entry<DyeColor, ResourceKey<EquipmentAsset>> entry : EquipmentAssets.CARPETS.entrySet()) {
            dyeColor = entry.getKey();
            resourceKey = entry.getValue();
            biConsumer.accept(resourceKey, EquipmentClientInfo.builder().addLayers(EquipmentClientInfo.LayerType.LLAMA_BODY, new EquipmentClientInfo.Layer(ResourceLocation.withDefaultNamespace(dyeColor.getSerializedName()))).build());
        }
        biConsumer.accept(EquipmentAssets.TRADER_LLAMA, EquipmentClientInfo.builder().addLayers(EquipmentClientInfo.LayerType.LLAMA_BODY, new EquipmentClientInfo.Layer(ResourceLocation.withDefaultNamespace("trader_llama"))).build());
    }

    private static EquipmentClientInfo onlyHumanoid(String string) {
        return EquipmentClientInfo.builder().addHumanoidLayers(ResourceLocation.withDefaultNamespace(string)).build();
    }

    private static EquipmentClientInfo humanoidAndHorse(String string) {
        return EquipmentClientInfo.builder().addHumanoidLayers(ResourceLocation.withDefaultNamespace(string)).addLayers(EquipmentClientInfo.LayerType.HORSE_BODY, EquipmentClientInfo.Layer.leatherDyeable(ResourceLocation.withDefaultNamespace(string), false)).build();
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        HashMap hashMap = new HashMap();
        EquipmentAssetProvider.bootstrap((resourceKey, equipmentClientInfo) -> {
            if (hashMap.putIfAbsent(resourceKey, equipmentClientInfo) != null) {
                throw new IllegalStateException("Tried to register equipment asset twice for id: " + String.valueOf(resourceKey));
            }
        });
        return DataProvider.saveAll(cachedOutput, EquipmentClientInfo.CODEC, this.pathProvider::json, hashMap);
    }

    @Override
    public String getName() {
        return "Equipment Asset Definitions";
    }
}

