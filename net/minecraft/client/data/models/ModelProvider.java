/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 */
package net.minecraft.client.data.models;

import com.google.common.collect.Maps;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModelProvider
implements DataProvider {
    private final PackOutput.PathProvider blockStatePathProvider;
    private final PackOutput.PathProvider itemInfoPathProvider;
    private final PackOutput.PathProvider modelPathProvider;

    public ModelProvider(PackOutput packOutput) {
        this.blockStatePathProvider = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
        this.itemInfoPathProvider = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
        this.modelPathProvider = packOutput.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        ItemInfoCollector itemInfoCollector = new ItemInfoCollector();
        BlockStateGeneratorCollector blockStateGeneratorCollector = new BlockStateGeneratorCollector();
        SimpleModelCollector simpleModelCollector = new SimpleModelCollector();
        new BlockModelGenerators(blockStateGeneratorCollector, itemInfoCollector, simpleModelCollector).run();
        new ItemModelGenerators(itemInfoCollector, simpleModelCollector).run();
        blockStateGeneratorCollector.validate();
        itemInfoCollector.finalizeAndValidate();
        return CompletableFuture.allOf(blockStateGeneratorCollector.save(cachedOutput, this.blockStatePathProvider), simpleModelCollector.save(cachedOutput, this.modelPathProvider), itemInfoCollector.save(cachedOutput, this.itemInfoPathProvider));
    }

    @Override
    public final String getName() {
        return "Model Definitions";
    }

    static class ItemInfoCollector
    implements ItemModelOutput {
        private final Map<Item, ClientItem> itemInfos = new HashMap<Item, ClientItem>();
        private final Map<Item, Item> copies = new HashMap<Item, Item>();

        ItemInfoCollector() {
        }

        @Override
        public void accept(Item item, ItemModel.Unbaked unbaked) {
            this.register(item, new ClientItem(unbaked, ClientItem.Properties.DEFAULT));
        }

        private void register(Item item, ClientItem clientItem) {
            ClientItem clientItem2 = this.itemInfos.put(item, clientItem);
            if (clientItem2 != null) {
                throw new IllegalStateException("Duplicate item model definition for " + String.valueOf(item));
            }
        }

        @Override
        public void copy(Item item, Item item2) {
            this.copies.put(item2, item);
        }

        public void finalizeAndValidate() {
            BuiltInRegistries.ITEM.forEach(item -> {
                BlockItem blockItem;
                if (this.copies.containsKey(item)) {
                    return;
                }
                if (item instanceof BlockItem && !this.itemInfos.containsKey(blockItem = (BlockItem)item)) {
                    ResourceLocation resourceLocation = ModelLocationUtils.getModelLocation(blockItem.getBlock());
                    this.accept(blockItem, ItemModelUtils.plainModel(resourceLocation));
                }
            });
            this.copies.forEach((item, item2) -> {
                ClientItem clientItem = this.itemInfos.get(item2);
                if (clientItem == null) {
                    throw new IllegalStateException("Missing donor: " + String.valueOf(item2) + " -> " + String.valueOf(item));
                }
                this.register((Item)item, clientItem);
            });
            List<ResourceLocation> list = BuiltInRegistries.ITEM.listElements().filter(reference -> !this.itemInfos.containsKey(reference.value())).map(reference -> reference.key().location()).toList();
            if (!list.isEmpty()) {
                throw new IllegalStateException("Missing item model definitions for: " + String.valueOf(list));
            }
        }

        public CompletableFuture<?> save(CachedOutput cachedOutput, PackOutput.PathProvider pathProvider) {
            return DataProvider.saveAll(cachedOutput, ClientItem.CODEC, item -> pathProvider.json(item.builtInRegistryHolder().key().location()), this.itemInfos);
        }
    }

    static class BlockStateGeneratorCollector
    implements Consumer<BlockModelDefinitionGenerator> {
        private final Map<Block, BlockModelDefinitionGenerator> generators = new HashMap<Block, BlockModelDefinitionGenerator>();

        BlockStateGeneratorCollector() {
        }

        @Override
        public void accept(BlockModelDefinitionGenerator blockModelDefinitionGenerator) {
            Block block = blockModelDefinitionGenerator.block();
            BlockModelDefinitionGenerator blockModelDefinitionGenerator2 = this.generators.put(block, blockModelDefinitionGenerator);
            if (blockModelDefinitionGenerator2 != null) {
                throw new IllegalStateException("Duplicate blockstate definition for " + String.valueOf(block));
            }
        }

        public void validate() {
            Stream<Holder.Reference> stream = BuiltInRegistries.BLOCK.listElements().filter(reference -> true);
            List<ResourceLocation> list = stream.filter(reference -> !this.generators.containsKey(reference.value())).map(reference -> reference.key().location()).toList();
            if (!list.isEmpty()) {
                throw new IllegalStateException("Missing blockstate definitions for: " + String.valueOf(list));
            }
        }

        public CompletableFuture<?> save(CachedOutput cachedOutput, PackOutput.PathProvider pathProvider) {
            Map map = Maps.transformValues(this.generators, BlockModelDefinitionGenerator::create);
            Function<Block, Path> function = block -> pathProvider.json(block.builtInRegistryHolder().key().location());
            return DataProvider.saveAll(cachedOutput, BlockModelDefinition.CODEC, function, map);
        }

        @Override
        public /* synthetic */ void accept(Object object) {
            this.accept((BlockModelDefinitionGenerator)object);
        }
    }

    static class SimpleModelCollector
    implements BiConsumer<ResourceLocation, ModelInstance> {
        private final Map<ResourceLocation, ModelInstance> models = new HashMap<ResourceLocation, ModelInstance>();

        SimpleModelCollector() {
        }

        @Override
        public void accept(ResourceLocation resourceLocation, ModelInstance modelInstance) {
            Supplier supplier = this.models.put(resourceLocation, modelInstance);
            if (supplier != null) {
                throw new IllegalStateException("Duplicate model definition for " + String.valueOf(resourceLocation));
            }
        }

        public CompletableFuture<?> save(CachedOutput cachedOutput, PackOutput.PathProvider pathProvider) {
            return DataProvider.saveAll(cachedOutput, Supplier::get, pathProvider::json, this.models);
        }

        @Override
        public /* synthetic */ void accept(Object object, Object object2) {
            this.accept((ResourceLocation)object, (ModelInstance)object2);
        }
    }
}

