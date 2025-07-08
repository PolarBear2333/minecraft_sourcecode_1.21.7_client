/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.HashMultimap
 *  com.google.common.collect.Multimap
 *  com.google.common.collect.Multimaps
 *  com.google.common.collect.Sets
 *  com.google.common.collect.Sets$SetView
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMaps
 *  org.slf4j.Logger
 */
package net.minecraft.client.resources.model;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import java.io.BufferedReader;
import java.io.Reader;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SpecialBlockModelRenderer;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.AtlasIds;
import net.minecraft.client.resources.model.AtlasSet;
import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.client.resources.model.ClientItemInfoLoader;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.MissingBlockModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ModelDiscovery;
import net.minecraft.client.resources.model.ModelGroupCollector;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SpriteGetter;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.util.profiling.Zone;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.slf4j.Logger;

public class ModelManager
implements PreparableReloadListener,
AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final FileToIdConverter MODEL_LISTER = FileToIdConverter.json("models");
    private static final Map<ResourceLocation, ResourceLocation> VANILLA_ATLASES = Map.of(Sheets.BANNER_SHEET, AtlasIds.BANNER_PATTERNS, Sheets.BED_SHEET, AtlasIds.BEDS, Sheets.CHEST_SHEET, AtlasIds.CHESTS, Sheets.SHIELD_SHEET, AtlasIds.SHIELD_PATTERNS, Sheets.SIGN_SHEET, AtlasIds.SIGNS, Sheets.SHULKER_SHEET, AtlasIds.SHULKER_BOXES, Sheets.ARMOR_TRIMS_SHEET, AtlasIds.ARMOR_TRIMS, Sheets.DECORATED_POT_SHEET, AtlasIds.DECORATED_POT, TextureAtlas.LOCATION_BLOCKS, AtlasIds.BLOCKS);
    private Map<ResourceLocation, ItemModel> bakedItemStackModels = Map.of();
    private Map<ResourceLocation, ClientItem.Properties> itemProperties = Map.of();
    private final AtlasSet atlases;
    private final BlockModelShaper blockModelShaper;
    private final BlockColors blockColors;
    private EntityModelSet entityModelSet = EntityModelSet.EMPTY;
    private SpecialBlockModelRenderer specialBlockModelRenderer = SpecialBlockModelRenderer.EMPTY;
    private int maxMipmapLevels;
    private ModelBakery.MissingModels missingModels;
    private Object2IntMap<BlockState> modelGroups = Object2IntMaps.emptyMap();

    public ModelManager(TextureManager textureManager, BlockColors blockColors, int n) {
        this.blockColors = blockColors;
        this.maxMipmapLevels = n;
        this.blockModelShaper = new BlockModelShaper(this);
        this.atlases = new AtlasSet(VANILLA_ATLASES, textureManager);
    }

    public BlockStateModel getMissingBlockStateModel() {
        return this.missingModels.block();
    }

    public ItemModel getItemModel(ResourceLocation resourceLocation) {
        return this.bakedItemStackModels.getOrDefault(resourceLocation, this.missingModels.item());
    }

    public ClientItem.Properties getItemProperties(ResourceLocation resourceLocation) {
        return this.itemProperties.getOrDefault(resourceLocation, ClientItem.Properties.DEFAULT);
    }

    public BlockModelShaper getBlockModelShaper() {
        return this.blockModelShaper;
    }

    @Override
    public final CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier preparationBarrier, ResourceManager resourceManager, Executor executor, Executor executor2) {
        CompletableFuture<EntityModelSet> completableFuture = CompletableFuture.supplyAsync(EntityModelSet::vanilla, executor);
        CompletionStage completionStage = completableFuture.thenApplyAsync(SpecialBlockModelRenderer::vanilla, executor);
        CompletableFuture<Map<ResourceLocation, UnbakedModel>> completableFuture2 = ModelManager.loadBlockModels(resourceManager, executor);
        CompletableFuture<BlockStateModelLoader.LoadedModels> completableFuture3 = BlockStateModelLoader.loadBlockStates(resourceManager, executor);
        CompletableFuture<ClientItemInfoLoader.LoadedClientInfos> completableFuture4 = ClientItemInfoLoader.scheduleLoad(resourceManager, executor);
        CompletionStage completionStage2 = CompletableFuture.allOf(completableFuture2, completableFuture3, completableFuture4).thenApplyAsync(void_ -> ModelManager.discoverModelDependencies((Map)completableFuture2.join(), (BlockStateModelLoader.LoadedModels)completableFuture3.join(), (ClientItemInfoLoader.LoadedClientInfos)completableFuture4.join()), executor);
        CompletionStage completionStage3 = completableFuture3.thenApplyAsync(loadedModels -> ModelManager.buildModelGroups(this.blockColors, loadedModels), executor);
        Map<ResourceLocation, CompletableFuture<AtlasSet.StitchResult>> map = this.atlases.scheduleLoad(resourceManager, this.maxMipmapLevels, executor);
        return ((CompletableFuture)((CompletableFuture)((CompletableFuture)CompletableFuture.allOf((CompletableFuture[])Stream.concat(map.values().stream(), Stream.of(completionStage2, completionStage3, completableFuture3, completableFuture4, completableFuture, completionStage, completableFuture2)).toArray(CompletableFuture[]::new)).thenComposeAsync(arg_0 -> ModelManager.lambda$reload$4(map, (CompletableFuture)completionStage2, (CompletableFuture)completionStage3, completableFuture2, completableFuture, completableFuture3, completableFuture4, (CompletableFuture)completionStage, executor, arg_0), executor)).thenCompose(reloadState -> reloadState.readyForUpload.thenApply(void_ -> reloadState))).thenCompose(preparationBarrier::wait)).thenAcceptAsync(reloadState -> this.apply((ReloadState)reloadState, Profiler.get()), executor2);
    }

    private static CompletableFuture<Map<ResourceLocation, UnbakedModel>> loadBlockModels(ResourceManager resourceManager, Executor executor) {
        return CompletableFuture.supplyAsync(() -> MODEL_LISTER.listMatchingResources(resourceManager), executor).thenCompose(map -> {
            ArrayList<CompletableFuture<Pair>> arrayList = new ArrayList<CompletableFuture<Pair>>(map.size());
            for (Map.Entry entry : map.entrySet()) {
                arrayList.add(CompletableFuture.supplyAsync(() -> {
                    Pair pair;
                    block8: {
                        ResourceLocation resourceLocation = MODEL_LISTER.fileToId((ResourceLocation)entry.getKey());
                        BufferedReader bufferedReader = ((Resource)entry.getValue()).openAsReader();
                        try {
                            pair = Pair.of((Object)resourceLocation, (Object)BlockModel.fromStream(bufferedReader));
                            if (bufferedReader == null) break block8;
                        }
                        catch (Throwable throwable) {
                            try {
                                if (bufferedReader != null) {
                                    try {
                                        ((Reader)bufferedReader).close();
                                    }
                                    catch (Throwable throwable2) {
                                        throwable.addSuppressed(throwable2);
                                    }
                                }
                                throw throwable;
                            }
                            catch (Exception exception) {
                                LOGGER.error("Failed to load model {}", entry.getKey(), (Object)exception);
                                return null;
                            }
                        }
                        ((Reader)bufferedReader).close();
                    }
                    return pair;
                }, executor));
            }
            return Util.sequence(arrayList).thenApply(list -> list.stream().filter(Objects::nonNull).collect(Collectors.toUnmodifiableMap(Pair::getFirst, Pair::getSecond)));
        });
    }

    private static ResolvedModels discoverModelDependencies(Map<ResourceLocation, UnbakedModel> map, BlockStateModelLoader.LoadedModels loadedModels, ClientItemInfoLoader.LoadedClientInfos loadedClientInfos) {
        try (Zone zone = Profiler.get().zone("dependencies");){
            ModelDiscovery modelDiscovery = new ModelDiscovery(map, MissingBlockModel.missingModel());
            modelDiscovery.addSpecialModel(ItemModelGenerator.GENERATED_ITEM_MODEL_ID, new ItemModelGenerator());
            loadedModels.models().values().forEach(modelDiscovery::addRoot);
            loadedClientInfos.contents().values().forEach(clientItem -> modelDiscovery.addRoot(clientItem.model()));
            ResolvedModels resolvedModels = new ResolvedModels(modelDiscovery.missingModel(), modelDiscovery.resolve());
            return resolvedModels;
        }
    }

    private static CompletableFuture<ReloadState> loadModels(final Map<ResourceLocation, AtlasSet.StitchResult> map, ModelBakery modelBakery, Object2IntMap<BlockState> object2IntMap, EntityModelSet entityModelSet, SpecialBlockModelRenderer specialBlockModelRenderer, Executor executor) {
        CompletableFuture<Void> completableFuture = CompletableFuture.allOf((CompletableFuture[])map.values().stream().map(AtlasSet.StitchResult::readyForUpload).toArray(CompletableFuture[]::new));
        final Multimap multimap = Multimaps.synchronizedMultimap((Multimap)HashMultimap.create());
        final Multimap multimap2 = Multimaps.synchronizedMultimap((Multimap)HashMultimap.create());
        return modelBakery.bakeModels(new SpriteGetter(){
            private final TextureAtlasSprite missingSprite;
            {
                this.missingSprite = ((AtlasSet.StitchResult)map.get(TextureAtlas.LOCATION_BLOCKS)).missing();
            }

            @Override
            public TextureAtlasSprite get(Material material, ModelDebugName modelDebugName) {
                AtlasSet.StitchResult stitchResult = (AtlasSet.StitchResult)map.get(material.atlasLocation());
                TextureAtlasSprite textureAtlasSprite = stitchResult.getSprite(material.texture());
                if (textureAtlasSprite != null) {
                    return textureAtlasSprite;
                }
                multimap.put((Object)modelDebugName.debugName(), (Object)material);
                return stitchResult.missing();
            }

            @Override
            public TextureAtlasSprite reportMissingReference(String string, ModelDebugName modelDebugName) {
                multimap2.put((Object)modelDebugName.debugName(), (Object)string);
                return this.missingSprite;
            }
        }, executor).thenApply(bakingResult -> {
            multimap.asMap().forEach((string, collection) -> LOGGER.warn("Missing textures in model {}:\n{}", string, (Object)collection.stream().sorted(Material.COMPARATOR).map(material -> "    " + String.valueOf(material.atlasLocation()) + ":" + String.valueOf(material.texture())).collect(Collectors.joining("\n"))));
            multimap2.asMap().forEach((string2, collection) -> LOGGER.warn("Missing texture references in model {}:\n{}", string2, (Object)collection.stream().sorted().map(string -> "    " + string).collect(Collectors.joining("\n"))));
            Map<BlockState, BlockStateModel> map2 = ModelManager.createBlockStateToModelDispatch(bakingResult.blockStateModels(), bakingResult.missingModels().block());
            return new ReloadState((ModelBakery.BakingResult)bakingResult, object2IntMap, map2, map, entityModelSet, specialBlockModelRenderer, completableFuture);
        });
    }

    private static Map<BlockState, BlockStateModel> createBlockStateToModelDispatch(Map<BlockState, BlockStateModel> map, BlockStateModel blockStateModel) {
        try (Zone zone = Profiler.get().zone("block state dispatch");){
            IdentityHashMap<BlockState, BlockStateModel> identityHashMap = new IdentityHashMap<BlockState, BlockStateModel>(map);
            for (Block block : BuiltInRegistries.BLOCK) {
                block.getStateDefinition().getPossibleStates().forEach(blockState -> {
                    if (map.putIfAbsent((BlockState)blockState, blockStateModel) == null) {
                        LOGGER.warn("Missing model for variant: '{}'", blockState);
                    }
                });
            }
            IdentityHashMap<BlockState, BlockStateModel> identityHashMap2 = identityHashMap;
            return identityHashMap2;
        }
    }

    private static Object2IntMap<BlockState> buildModelGroups(BlockColors blockColors, BlockStateModelLoader.LoadedModels loadedModels) {
        try (Zone zone = Profiler.get().zone("block groups");){
            Object2IntMap<BlockState> object2IntMap = ModelGroupCollector.build(blockColors, loadedModels);
            return object2IntMap;
        }
    }

    private void apply(ReloadState reloadState, ProfilerFiller profilerFiller) {
        profilerFiller.push("upload");
        reloadState.atlasPreparations.values().forEach(AtlasSet.StitchResult::upload);
        ModelBakery.BakingResult bakingResult = reloadState.bakedModels;
        this.bakedItemStackModels = bakingResult.itemStackModels();
        this.itemProperties = bakingResult.itemProperties();
        this.modelGroups = reloadState.modelGroups;
        this.missingModels = bakingResult.missingModels();
        profilerFiller.popPush("cache");
        this.blockModelShaper.replaceCache(reloadState.modelCache);
        this.specialBlockModelRenderer = reloadState.specialBlockModelRenderer;
        this.entityModelSet = reloadState.entityModelSet;
        profilerFiller.pop();
    }

    public boolean requiresRender(BlockState blockState, BlockState blockState2) {
        int n;
        if (blockState == blockState2) {
            return false;
        }
        int n2 = this.modelGroups.getInt((Object)blockState);
        if (n2 != -1 && n2 == (n = this.modelGroups.getInt((Object)blockState2))) {
            FluidState fluidState;
            FluidState fluidState2 = blockState.getFluidState();
            return fluidState2 != (fluidState = blockState2.getFluidState());
        }
        return true;
    }

    public TextureAtlas getAtlas(ResourceLocation resourceLocation) {
        return this.atlases.getAtlas(resourceLocation);
    }

    @Override
    public void close() {
        this.atlases.close();
    }

    public void updateMaxMipLevel(int n) {
        this.maxMipmapLevels = n;
    }

    public Supplier<SpecialBlockModelRenderer> specialBlockModelRenderer() {
        return () -> this.specialBlockModelRenderer;
    }

    public Supplier<EntityModelSet> entityModels() {
        return () -> this.entityModelSet;
    }

    private static /* synthetic */ CompletionStage lambda$reload$4(Map map, CompletableFuture completableFuture, CompletableFuture completableFuture2, CompletableFuture completableFuture3, CompletableFuture completableFuture4, CompletableFuture completableFuture5, CompletableFuture completableFuture6, CompletableFuture completableFuture7, Executor executor, Void void_) {
        Map<ResourceLocation, AtlasSet.StitchResult> map2 = Util.mapValues(map, CompletableFuture::join);
        ResolvedModels resolvedModels = (ResolvedModels)completableFuture.join();
        Object2IntMap object2IntMap = (Object2IntMap)completableFuture2.join();
        Sets.SetView setView = Sets.difference(((Map)completableFuture3.join()).keySet(), resolvedModels.models.keySet());
        if (!setView.isEmpty()) {
            LOGGER.debug("Unreferenced models: \n{}", (Object)setView.stream().sorted().map(resourceLocation -> "\t" + String.valueOf(resourceLocation) + "\n").collect(Collectors.joining()));
        }
        ModelBakery modelBakery = new ModelBakery((EntityModelSet)completableFuture4.join(), ((BlockStateModelLoader.LoadedModels)completableFuture5.join()).models(), ((ClientItemInfoLoader.LoadedClientInfos)completableFuture6.join()).contents(), resolvedModels.models(), resolvedModels.missing());
        return ModelManager.loadModels(map2, modelBakery, (Object2IntMap<BlockState>)object2IntMap, (EntityModelSet)completableFuture4.join(), (SpecialBlockModelRenderer)completableFuture7.join(), executor);
    }

    static final class ResolvedModels
    extends Record {
        private final ResolvedModel missing;
        final Map<ResourceLocation, ResolvedModel> models;

        ResolvedModels(ResolvedModel resolvedModel, Map<ResourceLocation, ResolvedModel> map) {
            this.missing = resolvedModel;
            this.models = map;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ResolvedModels.class, "missing;models", "missing", "models"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ResolvedModels.class, "missing;models", "missing", "models"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ResolvedModels.class, "missing;models", "missing", "models"}, this, object);
        }

        public ResolvedModel missing() {
            return this.missing;
        }

        public Map<ResourceLocation, ResolvedModel> models() {
            return this.models;
        }
    }

    static final class ReloadState
    extends Record {
        final ModelBakery.BakingResult bakedModels;
        final Object2IntMap<BlockState> modelGroups;
        final Map<BlockState, BlockStateModel> modelCache;
        final Map<ResourceLocation, AtlasSet.StitchResult> atlasPreparations;
        final EntityModelSet entityModelSet;
        final SpecialBlockModelRenderer specialBlockModelRenderer;
        final CompletableFuture<Void> readyForUpload;

        ReloadState(ModelBakery.BakingResult bakingResult, Object2IntMap<BlockState> object2IntMap, Map<BlockState, BlockStateModel> map, Map<ResourceLocation, AtlasSet.StitchResult> map2, EntityModelSet entityModelSet, SpecialBlockModelRenderer specialBlockModelRenderer, CompletableFuture<Void> completableFuture) {
            this.bakedModels = bakingResult;
            this.modelGroups = object2IntMap;
            this.modelCache = map;
            this.atlasPreparations = map2;
            this.entityModelSet = entityModelSet;
            this.specialBlockModelRenderer = specialBlockModelRenderer;
            this.readyForUpload = completableFuture;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ReloadState.class, "bakedModels;modelGroups;modelCache;atlasPreparations;entityModelSet;specialBlockModelRenderer;readyForUpload", "bakedModels", "modelGroups", "modelCache", "atlasPreparations", "entityModelSet", "specialBlockModelRenderer", "readyForUpload"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ReloadState.class, "bakedModels;modelGroups;modelCache;atlasPreparations;entityModelSet;specialBlockModelRenderer;readyForUpload", "bakedModels", "modelGroups", "modelCache", "atlasPreparations", "entityModelSet", "specialBlockModelRenderer", "readyForUpload"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ReloadState.class, "bakedModels;modelGroups;modelCache;atlasPreparations;entityModelSet;specialBlockModelRenderer;readyForUpload", "bakedModels", "modelGroups", "modelCache", "atlasPreparations", "entityModelSet", "specialBlockModelRenderer", "readyForUpload"}, this, object);
        }

        public ModelBakery.BakingResult bakedModels() {
            return this.bakedModels;
        }

        public Object2IntMap<BlockState> modelGroups() {
            return this.modelGroups;
        }

        public Map<BlockState, BlockStateModel> modelCache() {
            return this.modelCache;
        }

        public Map<ResourceLocation, AtlasSet.StitchResult> atlasPreparations() {
            return this.atlasPreparations;
        }

        public EntityModelSet entityModelSet() {
            return this.entityModelSet;
        }

        public SpecialBlockModelRenderer specialBlockModelRenderer() {
            return this.specialBlockModelRenderer;
        }

        public CompletableFuture<Void> readyForUpload() {
            return this.readyForUpload;
        }
    }
}

