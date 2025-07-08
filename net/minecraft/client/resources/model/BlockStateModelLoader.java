/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonParseException
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  org.slf4j.Logger
 */
package net.minecraft.client.resources.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.Reader;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.resources.model.BlockStateDefinitions;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.slf4j.Logger;

public class BlockStateModelLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final FileToIdConverter BLOCKSTATE_LISTER = FileToIdConverter.json("blockstates");

    public static CompletableFuture<LoadedModels> loadBlockStates(ResourceManager resourceManager, Executor executor) {
        Function<ResourceLocation, StateDefinition<Block, BlockState>> function = BlockStateDefinitions.definitionLocationToBlockStateMapper();
        return CompletableFuture.supplyAsync(() -> BLOCKSTATE_LISTER.listMatchingResourceStacks(resourceManager), executor).thenCompose(map -> {
            ArrayList<CompletableFuture<LoadedModels>> arrayList = new ArrayList<CompletableFuture<LoadedModels>>(map.size());
            for (Map.Entry entry : map.entrySet()) {
                arrayList.add(CompletableFuture.supplyAsync(() -> {
                    ResourceLocation resourceLocation = BLOCKSTATE_LISTER.fileToId((ResourceLocation)entry.getKey());
                    StateDefinition stateDefinition = (StateDefinition)function.apply(resourceLocation);
                    if (stateDefinition == null) {
                        LOGGER.debug("Discovered unknown block state definition {}, ignoring", (Object)resourceLocation);
                        return null;
                    }
                    List list = (List)entry.getValue();
                    ArrayList<LoadedBlockModelDefinition> arrayList = new ArrayList<LoadedBlockModelDefinition>(list.size());
                    for (Resource resource : list) {
                        try {
                            BufferedReader bufferedReader = resource.openAsReader();
                            try {
                                JsonElement jsonElement = StrictJsonParser.parse(bufferedReader);
                                BlockModelDefinition blockModelDefinition = (BlockModelDefinition)BlockModelDefinition.CODEC.parse((DynamicOps)JsonOps.INSTANCE, (Object)jsonElement).getOrThrow(JsonParseException::new);
                                arrayList.add(new LoadedBlockModelDefinition(resource.sourcePackId(), blockModelDefinition));
                            }
                            finally {
                                if (bufferedReader == null) continue;
                                ((Reader)bufferedReader).close();
                            }
                        }
                        catch (Exception exception) {
                            LOGGER.error("Failed to load blockstate definition {} from pack {}", new Object[]{resourceLocation, resource.sourcePackId(), exception});
                        }
                    }
                    try {
                        return BlockStateModelLoader.loadBlockStateDefinitionStack(resourceLocation, stateDefinition, arrayList);
                    }
                    catch (Exception exception) {
                        LOGGER.error("Failed to load blockstate definition {}", (Object)resourceLocation, (Object)exception);
                        return null;
                    }
                }, executor));
            }
            return Util.sequence(arrayList).thenApply(list -> {
                IdentityHashMap<BlockState, BlockStateModel.UnbakedRoot> identityHashMap = new IdentityHashMap<BlockState, BlockStateModel.UnbakedRoot>();
                for (LoadedModels loadedModels : list) {
                    if (loadedModels == null) continue;
                    identityHashMap.putAll(loadedModels.models());
                }
                return new LoadedModels(identityHashMap);
            });
        });
    }

    private static LoadedModels loadBlockStateDefinitionStack(ResourceLocation resourceLocation, StateDefinition<Block, BlockState> stateDefinition, List<LoadedBlockModelDefinition> list) {
        IdentityHashMap<BlockState, BlockStateModel.UnbakedRoot> identityHashMap = new IdentityHashMap<BlockState, BlockStateModel.UnbakedRoot>();
        for (LoadedBlockModelDefinition loadedBlockModelDefinition : list) {
            identityHashMap.putAll(loadedBlockModelDefinition.contents.instantiate(stateDefinition, () -> String.valueOf(resourceLocation) + "/" + loadedBlockModelDefinition.source));
        }
        return new LoadedModels(identityHashMap);
    }

    static final class LoadedBlockModelDefinition
    extends Record {
        final String source;
        final BlockModelDefinition contents;

        LoadedBlockModelDefinition(String string, BlockModelDefinition blockModelDefinition) {
            this.source = string;
            this.contents = blockModelDefinition;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{LoadedBlockModelDefinition.class, "source;contents", "source", "contents"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{LoadedBlockModelDefinition.class, "source;contents", "source", "contents"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{LoadedBlockModelDefinition.class, "source;contents", "source", "contents"}, this, object);
        }

        public String source() {
            return this.source;
        }

        public BlockModelDefinition contents() {
            return this.contents;
        }
    }

    public record LoadedModels(Map<BlockState, BlockStateModel.UnbakedRoot> models) {
    }
}

