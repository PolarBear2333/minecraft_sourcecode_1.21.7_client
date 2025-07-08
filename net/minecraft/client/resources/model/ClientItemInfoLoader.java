/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.JsonOps
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.resources.model;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.Reader;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.multiplayer.ClientRegistryLayer;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.PlaceholderLookupProvider;
import net.minecraft.util.StrictJsonParser;
import org.slf4j.Logger;

public class ClientItemInfoLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final FileToIdConverter LISTER = FileToIdConverter.json("items");

    public static CompletableFuture<LoadedClientInfos> scheduleLoad(ResourceManager resourceManager, Executor executor) {
        RegistryAccess.Frozen frozen = ClientRegistryLayer.createRegistryAccess().compositeAccess();
        return CompletableFuture.supplyAsync(() -> LISTER.listMatchingResources(resourceManager), executor).thenCompose(map -> {
            ArrayList arrayList = new ArrayList(map.size());
            map.forEach((resourceLocation, resource) -> arrayList.add(CompletableFuture.supplyAsync(() -> {
                PendingLoad pendingLoad;
                block8: {
                    ResourceLocation resourceLocation2 = LISTER.fileToId((ResourceLocation)resourceLocation);
                    BufferedReader bufferedReader = resource.openAsReader();
                    try {
                        PlaceholderLookupProvider placeholderLookupProvider = new PlaceholderLookupProvider(frozen);
                        RegistryOps registryOps = placeholderLookupProvider.createSerializationContext(JsonOps.INSTANCE);
                        ClientItem clientItem2 = ClientItem.CODEC.parse(registryOps, (Object)StrictJsonParser.parse(bufferedReader)).ifError(error -> LOGGER.error("Couldn't parse item model '{}' from pack '{}': {}", new Object[]{resourceLocation2, resource.sourcePackId(), error.message()})).result().map(clientItem -> {
                            if (placeholderLookupProvider.hasRegisteredPlaceholders()) {
                                return clientItem.withRegistrySwapper(placeholderLookupProvider.createSwapper());
                            }
                            return clientItem;
                        }).orElse(null);
                        pendingLoad = new PendingLoad(resourceLocation2, clientItem2);
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
                            LOGGER.error("Failed to open item model {} from pack '{}'", new Object[]{resourceLocation, resource.sourcePackId(), exception});
                            return new PendingLoad(resourceLocation2, null);
                        }
                    }
                    ((Reader)bufferedReader).close();
                }
                return pendingLoad;
            }, executor)));
            return Util.sequence(arrayList).thenApply(list -> {
                HashMap<ResourceLocation, ClientItem> hashMap = new HashMap<ResourceLocation, ClientItem>();
                for (PendingLoad pendingLoad : list) {
                    if (pendingLoad.clientItemInfo == null) continue;
                    hashMap.put(pendingLoad.id, pendingLoad.clientItemInfo);
                }
                return new LoadedClientInfos(hashMap);
            });
        });
    }

    static final class PendingLoad
    extends Record {
        final ResourceLocation id;
        @Nullable
        final ClientItem clientItemInfo;

        PendingLoad(ResourceLocation resourceLocation, @Nullable ClientItem clientItem) {
            this.id = resourceLocation;
            this.clientItemInfo = clientItem;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{PendingLoad.class, "id;clientItemInfo", "id", "clientItemInfo"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PendingLoad.class, "id;clientItemInfo", "id", "clientItemInfo"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PendingLoad.class, "id;clientItemInfo", "id", "clientItemInfo"}, this, object);
        }

        public ResourceLocation id() {
            return this.id;
        }

        @Nullable
        public ClientItem clientItemInfo() {
            return this.clientItemInfo;
        }
    }

    public record LoadedClientInfos(Map<ResourceLocation, ClientItem> contents) {
    }
}

