/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.Keyable
 *  com.mojang.serialization.Lifecycle
 *  javax.annotation.Nullable
 */
package net.minecraft.core;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.Lifecycle;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.IdMap;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;

public interface Registry<T>
extends Keyable,
HolderLookup.RegistryLookup<T>,
IdMap<T> {
    @Override
    public ResourceKey<? extends Registry<T>> key();

    default public Codec<T> byNameCodec() {
        return this.referenceHolderWithLifecycle().flatComapMap(Holder.Reference::value, object -> this.safeCastToReference(this.wrapAsHolder(object)));
    }

    default public Codec<Holder<T>> holderByNameCodec() {
        return this.referenceHolderWithLifecycle().flatComapMap(reference -> reference, this::safeCastToReference);
    }

    private Codec<Holder.Reference<T>> referenceHolderWithLifecycle() {
        Codec codec = ResourceLocation.CODEC.comapFlatMap(resourceLocation -> this.get((ResourceLocation)resourceLocation).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown registry key in " + String.valueOf(this.key()) + ": " + String.valueOf(resourceLocation))), reference -> reference.key().location());
        return ExtraCodecs.overrideLifecycle(codec, reference -> this.registrationInfo(reference.key()).map(RegistrationInfo::lifecycle).orElse(Lifecycle.experimental()));
    }

    private DataResult<Holder.Reference<T>> safeCastToReference(Holder<T> holder) {
        DataResult dataResult;
        if (holder instanceof Holder.Reference) {
            Holder.Reference reference = (Holder.Reference)holder;
            dataResult = DataResult.success((Object)reference);
        } else {
            dataResult = DataResult.error(() -> "Unregistered holder in " + String.valueOf(this.key()) + ": " + String.valueOf(holder));
        }
        return dataResult;
    }

    default public <U> Stream<U> keys(DynamicOps<U> dynamicOps) {
        return this.keySet().stream().map(resourceLocation -> dynamicOps.createString(resourceLocation.toString()));
    }

    @Nullable
    public ResourceLocation getKey(T var1);

    public Optional<ResourceKey<T>> getResourceKey(T var1);

    @Override
    public int getId(@Nullable T var1);

    @Nullable
    public T getValue(@Nullable ResourceKey<T> var1);

    @Nullable
    public T getValue(@Nullable ResourceLocation var1);

    public Optional<RegistrationInfo> registrationInfo(ResourceKey<T> var1);

    default public Optional<T> getOptional(@Nullable ResourceLocation resourceLocation) {
        return Optional.ofNullable(this.getValue(resourceLocation));
    }

    default public Optional<T> getOptional(@Nullable ResourceKey<T> resourceKey) {
        return Optional.ofNullable(this.getValue(resourceKey));
    }

    public Optional<Holder.Reference<T>> getAny();

    default public T getValueOrThrow(ResourceKey<T> resourceKey) {
        T t = this.getValue(resourceKey);
        if (t == null) {
            throw new IllegalStateException("Missing key in " + String.valueOf(this.key()) + ": " + String.valueOf(resourceKey));
        }
        return t;
    }

    public Set<ResourceLocation> keySet();

    public Set<Map.Entry<ResourceKey<T>, T>> entrySet();

    public Set<ResourceKey<T>> registryKeySet();

    public Optional<Holder.Reference<T>> getRandom(RandomSource var1);

    default public Stream<T> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    public boolean containsKey(ResourceLocation var1);

    public boolean containsKey(ResourceKey<T> var1);

    public static <T> T register(Registry<? super T> registry, String string, T t) {
        return Registry.register(registry, ResourceLocation.parse(string), t);
    }

    public static <V, T extends V> T register(Registry<V> registry, ResourceLocation resourceLocation, T t) {
        return Registry.register(registry, ResourceKey.create(registry.key(), resourceLocation), t);
    }

    public static <V, T extends V> T register(Registry<V> registry, ResourceKey<V> resourceKey, T t) {
        ((WritableRegistry)registry).register(resourceKey, t, RegistrationInfo.BUILT_IN);
        return t;
    }

    public static <T> Holder.Reference<T> registerForHolder(Registry<T> registry, ResourceKey<T> resourceKey, T t) {
        return ((WritableRegistry)registry).register(resourceKey, t, RegistrationInfo.BUILT_IN);
    }

    public static <T> Holder.Reference<T> registerForHolder(Registry<T> registry, ResourceLocation resourceLocation, T t) {
        return Registry.registerForHolder(registry, ResourceKey.create(registry.key(), resourceLocation), t);
    }

    public Registry<T> freeze();

    public Holder.Reference<T> createIntrusiveHolder(T var1);

    public Optional<Holder.Reference<T>> get(int var1);

    public Optional<Holder.Reference<T>> get(ResourceLocation var1);

    public Holder<T> wrapAsHolder(T var1);

    default public Iterable<Holder<T>> getTagOrEmpty(TagKey<T> tagKey) {
        return (Iterable)DataFixUtils.orElse((Optional)this.get(tagKey), List.of());
    }

    default public Optional<Holder<T>> getRandomElementOf(TagKey<T> tagKey, RandomSource randomSource) {
        return this.get(tagKey).flatMap(named -> named.getRandomElement(randomSource));
    }

    public Stream<HolderSet.Named<T>> getTags();

    default public IdMap<Holder<T>> asHolderIdMap() {
        return new IdMap<Holder<T>>(){

            @Override
            public int getId(Holder<T> holder) {
                return Registry.this.getId(holder.value());
            }

            @Override
            @Nullable
            public Holder<T> byId(int n) {
                return Registry.this.get(n).orElse(null);
            }

            @Override
            public int size() {
                return Registry.this.size();
            }

            @Override
            public Iterator<Holder<T>> iterator() {
                return Registry.this.listElements().map(reference -> reference).iterator();
            }

            @Override
            @Nullable
            public /* synthetic */ Object byId(int n) {
                return this.byId(n);
            }
        };
    }

    public PendingTags<T> prepareTagReload(TagLoader.LoadResult<T> var1);

    public static interface PendingTags<T> {
        public ResourceKey<? extends Registry<? extends T>> key();

        public HolderLookup.RegistryLookup<T> lookup();

        public void apply();

        public int size();
    }
}

