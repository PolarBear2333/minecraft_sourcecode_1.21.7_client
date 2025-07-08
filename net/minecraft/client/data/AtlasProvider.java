/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.renderer.MaterialMapper;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BellRenderer;
import net.minecraft.client.renderer.blockentity.ConduitRenderer;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSources;
import net.minecraft.client.renderer.texture.atlas.sources.DirectoryLister;
import net.minecraft.client.renderer.texture.atlas.sources.PalettedPermutations;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.client.resources.model.AtlasIds;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.equipment.trim.MaterialAssetGroup;
import net.minecraft.world.item.equipment.trim.TrimPattern;
import net.minecraft.world.item.equipment.trim.TrimPatterns;

public class AtlasProvider
implements DataProvider {
    private static final ResourceLocation TRIM_PALETTE_KEY = ResourceLocation.withDefaultNamespace("trims/color_palettes/trim_palette");
    private static final Map<String, ResourceLocation> TRIM_PALETTE_VALUES = AtlasProvider.extractAllMaterialAssets().collect(Collectors.toMap(MaterialAssetGroup.AssetInfo::suffix, assetInfo -> ResourceLocation.withDefaultNamespace("trims/color_palettes/" + assetInfo.suffix())));
    private static final List<ResourceKey<TrimPattern>> VANILLA_PATTERNS = List.of(TrimPatterns.SENTRY, TrimPatterns.DUNE, TrimPatterns.COAST, TrimPatterns.WILD, TrimPatterns.WARD, TrimPatterns.EYE, TrimPatterns.VEX, TrimPatterns.TIDE, TrimPatterns.SNOUT, TrimPatterns.RIB, TrimPatterns.SPIRE, TrimPatterns.WAYFINDER, TrimPatterns.SHAPER, TrimPatterns.SILENCE, TrimPatterns.RAISER, TrimPatterns.HOST, TrimPatterns.FLOW, TrimPatterns.BOLT);
    private static final List<EquipmentClientInfo.LayerType> HUMANOID_LAYERS = List.of(EquipmentClientInfo.LayerType.HUMANOID, EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS);
    private final PackOutput.PathProvider pathProvider;

    public AtlasProvider(PackOutput packOutput) {
        this.pathProvider = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "atlases");
    }

    private static List<ResourceLocation> patternTextures() {
        ArrayList<ResourceLocation> arrayList = new ArrayList<ResourceLocation>(VANILLA_PATTERNS.size() * HUMANOID_LAYERS.size());
        for (ResourceKey<TrimPattern> resourceKey : VANILLA_PATTERNS) {
            ResourceLocation resourceLocation = TrimPatterns.defaultAssetId(resourceKey);
            for (EquipmentClientInfo.LayerType layerType : HUMANOID_LAYERS) {
                arrayList.add(resourceLocation.withPath(string -> layerType.trimAssetPrefix() + "/" + string));
            }
        }
        return arrayList;
    }

    private static SpriteSource forMaterial(Material material) {
        return new SingleFile(material.texture());
    }

    private static SpriteSource forMapper(MaterialMapper materialMapper) {
        return new DirectoryLister(materialMapper.prefix(), materialMapper.prefix() + "/");
    }

    private static List<SpriteSource> simpleMapper(MaterialMapper materialMapper) {
        return List.of(AtlasProvider.forMapper(materialMapper));
    }

    private static List<SpriteSource> noPrefixMapper(String string) {
        return List.of(new DirectoryLister(string, ""));
    }

    private static Stream<MaterialAssetGroup.AssetInfo> extractAllMaterialAssets() {
        return ItemModelGenerators.TRIM_MATERIAL_MODELS.stream().map(ItemModelGenerators.TrimMaterialData::assets).flatMap(materialAssetGroup -> Stream.concat(Stream.of(materialAssetGroup.base()), materialAssetGroup.overrides().values().stream())).sorted(Comparator.comparing(MaterialAssetGroup.AssetInfo::suffix));
    }

    private static List<SpriteSource> armorTrims() {
        return List.of(new PalettedPermutations(AtlasProvider.patternTextures(), TRIM_PALETTE_KEY, TRIM_PALETTE_VALUES));
    }

    private static List<SpriteSource> blocksList() {
        return List.of(AtlasProvider.forMapper(Sheets.BLOCKS_MAPPER), AtlasProvider.forMapper(Sheets.ITEMS_MAPPER), AtlasProvider.forMapper(ConduitRenderer.MAPPER), AtlasProvider.forMaterial(BellRenderer.BELL_RESOURCE_LOCATION), AtlasProvider.forMaterial(Sheets.DECORATED_POT_SIDE), AtlasProvider.forMaterial(EnchantTableRenderer.BOOK_LOCATION), new PalettedPermutations(List.of(ItemModelGenerators.TRIM_PREFIX_HELMET, ItemModelGenerators.TRIM_PREFIX_CHESTPLATE, ItemModelGenerators.TRIM_PREFIX_LEGGINGS, ItemModelGenerators.TRIM_PREFIX_BOOTS), TRIM_PALETTE_KEY, TRIM_PALETTE_VALUES));
    }

    private static List<SpriteSource> bannerPatterns() {
        return List.of(AtlasProvider.forMaterial(ModelBakery.BANNER_BASE), AtlasProvider.forMapper(Sheets.BANNER_MAPPER));
    }

    private static List<SpriteSource> shieldPatterns() {
        return List.of(AtlasProvider.forMaterial(ModelBakery.SHIELD_BASE), AtlasProvider.forMaterial(ModelBakery.NO_PATTERN_SHIELD), AtlasProvider.forMapper(Sheets.SHIELD_MAPPER));
    }

    private static List<SpriteSource> guiSprites() {
        return List.of(new DirectoryLister("gui/sprites", ""), new DirectoryLister("mob_effect", "mob_effect/"));
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        return CompletableFuture.allOf(this.storeAtlas(cachedOutput, AtlasIds.ARMOR_TRIMS, AtlasProvider.armorTrims()), this.storeAtlas(cachedOutput, AtlasIds.BANNER_PATTERNS, AtlasProvider.bannerPatterns()), this.storeAtlas(cachedOutput, AtlasIds.BEDS, AtlasProvider.simpleMapper(Sheets.BED_MAPPER)), this.storeAtlas(cachedOutput, AtlasIds.BLOCKS, AtlasProvider.blocksList()), this.storeAtlas(cachedOutput, AtlasIds.CHESTS, AtlasProvider.simpleMapper(Sheets.CHEST_MAPPER)), this.storeAtlas(cachedOutput, AtlasIds.DECORATED_POT, AtlasProvider.simpleMapper(Sheets.DECORATED_POT_MAPPER)), this.storeAtlas(cachedOutput, AtlasIds.GUI, AtlasProvider.guiSprites()), this.storeAtlas(cachedOutput, AtlasIds.MAP_DECORATIONS, AtlasProvider.noPrefixMapper("map/decorations")), this.storeAtlas(cachedOutput, AtlasIds.PAINTINGS, AtlasProvider.noPrefixMapper("painting")), this.storeAtlas(cachedOutput, AtlasIds.PARTICLES, AtlasProvider.noPrefixMapper("particle")), this.storeAtlas(cachedOutput, AtlasIds.SHIELD_PATTERNS, AtlasProvider.shieldPatterns()), this.storeAtlas(cachedOutput, AtlasIds.SHULKER_BOXES, AtlasProvider.simpleMapper(Sheets.SHULKER_MAPPER)), this.storeAtlas(cachedOutput, AtlasIds.SIGNS, AtlasProvider.simpleMapper(Sheets.SIGN_MAPPER)));
    }

    private CompletableFuture<?> storeAtlas(CachedOutput cachedOutput, ResourceLocation resourceLocation, List<SpriteSource> list) {
        return DataProvider.saveStable(cachedOutput, SpriteSources.FILE_CODEC, list, this.pathProvider.json(resourceLocation));
    }

    @Override
    public String getName() {
        return "Atlas Definitions";
    }
}

