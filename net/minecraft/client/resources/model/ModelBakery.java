/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.client.resources.model;

import com.mojang.logging.LogUtils;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.block.model.SingleVariant;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.MissingItemModel;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SpriteGetter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.thread.ParallelMapTransform;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;

public class ModelBakery {
    public static final Material FIRE_0 = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("block/fire_0"));
    public static final Material FIRE_1 = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("block/fire_1"));
    public static final Material LAVA_FLOW = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("block/lava_flow"));
    public static final Material WATER_FLOW = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("block/water_flow"));
    public static final Material WATER_OVERLAY = new Material(TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("block/water_overlay"));
    public static final Material BANNER_BASE = new Material(Sheets.BANNER_SHEET, ResourceLocation.withDefaultNamespace("entity/banner_base"));
    public static final Material SHIELD_BASE = new Material(Sheets.SHIELD_SHEET, ResourceLocation.withDefaultNamespace("entity/shield_base"));
    public static final Material NO_PATTERN_SHIELD = new Material(Sheets.SHIELD_SHEET, ResourceLocation.withDefaultNamespace("entity/shield_base_nopattern"));
    public static final int DESTROY_STAGE_COUNT = 10;
    public static final List<ResourceLocation> DESTROY_STAGES = IntStream.range(0, 10).mapToObj(n -> ResourceLocation.withDefaultNamespace("block/destroy_stage_" + n)).collect(Collectors.toList());
    public static final List<ResourceLocation> BREAKING_LOCATIONS = DESTROY_STAGES.stream().map(resourceLocation -> resourceLocation.withPath(string -> "textures/" + string + ".png")).collect(Collectors.toList());
    public static final List<RenderType> DESTROY_TYPES = BREAKING_LOCATIONS.stream().map(RenderType::crumbling).collect(Collectors.toList());
    static final Logger LOGGER = LogUtils.getLogger();
    private final EntityModelSet entityModelSet;
    private final Map<BlockState, BlockStateModel.UnbakedRoot> unbakedBlockStateModels;
    private final Map<ResourceLocation, ClientItem> clientInfos;
    final Map<ResourceLocation, ResolvedModel> resolvedModels;
    final ResolvedModel missingModel;

    public ModelBakery(EntityModelSet entityModelSet, Map<BlockState, BlockStateModel.UnbakedRoot> map, Map<ResourceLocation, ClientItem> map2, Map<ResourceLocation, ResolvedModel> map3, ResolvedModel resolvedModel) {
        this.entityModelSet = entityModelSet;
        this.unbakedBlockStateModels = map;
        this.clientInfos = map2;
        this.resolvedModels = map3;
        this.missingModel = resolvedModel;
    }

    public CompletableFuture<BakingResult> bakeModels(SpriteGetter spriteGetter, Executor executor) {
        MissingModels missingModels = MissingModels.bake(this.missingModel, spriteGetter);
        ModelBakerImpl modelBakerImpl = new ModelBakerImpl(spriteGetter);
        CompletableFuture<Map<BlockState, BlockStateModel>> completableFuture = ParallelMapTransform.schedule(this.unbakedBlockStateModels, (blockState, unbakedRoot) -> {
            try {
                return unbakedRoot.bake((BlockState)blockState, modelBakerImpl);
            }
            catch (Exception exception) {
                LOGGER.warn("Unable to bake model: '{}': {}", blockState, (Object)exception);
                return null;
            }
        }, executor);
        CompletableFuture<Map<ResourceLocation, ItemModel>> completableFuture2 = ParallelMapTransform.schedule(this.clientInfos, (resourceLocation, clientItem) -> {
            try {
                return clientItem.model().bake(new ItemModel.BakingContext(modelBakerImpl, this.entityModelSet, missingModels.item, clientItem.registrySwapper()));
            }
            catch (Exception exception) {
                LOGGER.warn("Unable to bake item model: '{}'", resourceLocation, (Object)exception);
                return null;
            }
        }, executor);
        HashMap hashMap = new HashMap(this.clientInfos.size());
        this.clientInfos.forEach((resourceLocation, clientItem) -> {
            ClientItem.Properties properties = clientItem.properties();
            if (!properties.equals(ClientItem.Properties.DEFAULT)) {
                hashMap.put(resourceLocation, properties);
            }
        });
        return completableFuture.thenCombine(completableFuture2, (map2, map3) -> new BakingResult(missingModels, (Map<BlockState, BlockStateModel>)map2, (Map<ResourceLocation, ItemModel>)map3, hashMap));
    }

    public static final class MissingModels
    extends Record {
        private final BlockStateModel block;
        final ItemModel item;

        public MissingModels(BlockStateModel blockStateModel, ItemModel itemModel) {
            this.block = blockStateModel;
            this.item = itemModel;
        }

        public static MissingModels bake(ResolvedModel resolvedModel, final SpriteGetter spriteGetter) {
            ModelBaker modelBaker = new ModelBaker(){

                @Override
                public ResolvedModel getModel(ResourceLocation resourceLocation) {
                    throw new IllegalStateException("Missing model can't have dependencies, but asked for " + String.valueOf(resourceLocation));
                }

                @Override
                public <T> T compute(ModelBaker.SharedOperationKey<T> sharedOperationKey) {
                    return sharedOperationKey.compute(this);
                }

                @Override
                public SpriteGetter sprites() {
                    return spriteGetter;
                }
            };
            TextureSlots textureSlots = resolvedModel.getTopTextureSlots();
            boolean bl = resolvedModel.getTopAmbientOcclusion();
            boolean bl2 = resolvedModel.getTopGuiLight().lightLikeBlock();
            ItemTransforms itemTransforms = resolvedModel.getTopTransforms();
            QuadCollection quadCollection = resolvedModel.bakeTopGeometry(textureSlots, modelBaker, BlockModelRotation.X0_Y0);
            TextureAtlasSprite textureAtlasSprite = resolvedModel.resolveParticleSprite(textureSlots, modelBaker);
            SingleVariant singleVariant = new SingleVariant(new SimpleModelWrapper(quadCollection, bl, textureAtlasSprite));
            MissingItemModel missingItemModel = new MissingItemModel(quadCollection.getAll(), new ModelRenderProperties(bl2, textureAtlasSprite, itemTransforms));
            return new MissingModels(singleVariant, missingItemModel);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{MissingModels.class, "block;item", "block", "item"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{MissingModels.class, "block;item", "block", "item"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{MissingModels.class, "block;item", "block", "item"}, this, object);
        }

        public BlockStateModel block() {
            return this.block;
        }

        public ItemModel item() {
            return this.item;
        }
    }

    class ModelBakerImpl
    implements ModelBaker {
        private final SpriteGetter sprites;
        private final Map<ModelBaker.SharedOperationKey<Object>, Object> operationCache = new ConcurrentHashMap<ModelBaker.SharedOperationKey<Object>, Object>();
        private final Function<ModelBaker.SharedOperationKey<Object>, Object> cacheComputeFunction = sharedOperationKey -> sharedOperationKey.compute(this);

        ModelBakerImpl(SpriteGetter spriteGetter) {
            this.sprites = spriteGetter;
        }

        @Override
        public SpriteGetter sprites() {
            return this.sprites;
        }

        @Override
        public ResolvedModel getModel(ResourceLocation resourceLocation) {
            ResolvedModel resolvedModel = ModelBakery.this.resolvedModels.get(resourceLocation);
            if (resolvedModel == null) {
                LOGGER.warn("Requested a model that was not discovered previously: {}", (Object)resourceLocation);
                return ModelBakery.this.missingModel;
            }
            return resolvedModel;
        }

        @Override
        public <T> T compute(ModelBaker.SharedOperationKey<T> sharedOperationKey) {
            return (T)this.operationCache.computeIfAbsent(sharedOperationKey, this.cacheComputeFunction);
        }
    }

    public record BakingResult(MissingModels missingModels, Map<BlockState, BlockStateModel> blockStateModels, Map<ResourceLocation, ItemModel> itemStackModels, Map<ResourceLocation, ClientItem.Properties> itemProperties) {
    }
}

