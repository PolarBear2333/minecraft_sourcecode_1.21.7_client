/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 */
package net.minecraft.tags;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.Holder;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RegistryLayer;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;

public class TagNetworkSerialization {
    public static Map<ResourceKey<? extends Registry<?>>, NetworkPayload> serializeTagsToNetwork(LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess) {
        return RegistrySynchronization.networkSafeRegistries(layeredRegistryAccess).map(registryEntry -> Pair.of(registryEntry.key(), (Object)TagNetworkSerialization.serializeToNetwork(registryEntry.value()))).filter(pair -> !((NetworkPayload)pair.getSecond()).isEmpty()).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond));
    }

    private static <T> NetworkPayload serializeToNetwork(Registry<T> registry) {
        HashMap<ResourceLocation, IntList> hashMap = new HashMap<ResourceLocation, IntList>();
        registry.getTags().forEach(named -> {
            IntArrayList intArrayList = new IntArrayList(named.size());
            for (Holder holder : named) {
                if (holder.kind() != Holder.Kind.REFERENCE) {
                    throw new IllegalStateException("Can't serialize unregistered value " + String.valueOf(holder));
                }
                intArrayList.add(registry.getId(holder.value()));
            }
            hashMap.put(named.key().location(), (IntList)intArrayList);
        });
        return new NetworkPayload(hashMap);
    }

    static <T> TagLoader.LoadResult<T> deserializeTagsFromNetwork(Registry<T> registry, NetworkPayload networkPayload) {
        ResourceKey resourceKey = registry.key();
        HashMap hashMap = new HashMap();
        networkPayload.tags.forEach((resourceLocation, intList) -> {
            TagKey tagKey = TagKey.create(resourceKey, resourceLocation);
            List list = intList.intStream().mapToObj(registry::get).flatMap(Optional::stream).collect(Collectors.toUnmodifiableList());
            hashMap.put(tagKey, list);
        });
        return new TagLoader.LoadResult<T>(resourceKey, hashMap);
    }

    public static final class NetworkPayload {
        public static final NetworkPayload EMPTY = new NetworkPayload(Map.of());
        final Map<ResourceLocation, IntList> tags;

        NetworkPayload(Map<ResourceLocation, IntList> map) {
            this.tags = map;
        }

        public void write(FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.writeMap(this.tags, FriendlyByteBuf::writeResourceLocation, FriendlyByteBuf::writeIntIdList);
        }

        public static NetworkPayload read(FriendlyByteBuf friendlyByteBuf) {
            return new NetworkPayload(friendlyByteBuf.readMap(FriendlyByteBuf::readResourceLocation, FriendlyByteBuf::readIntIdList));
        }

        public boolean isEmpty() {
            return this.tags.isEmpty();
        }

        public int size() {
            return this.tags.size();
        }

        public <T> TagLoader.LoadResult<T> resolve(Registry<T> registry) {
            return TagNetworkSerialization.deserializeTagsFromNetwork(registry, this);
        }
    }
}

