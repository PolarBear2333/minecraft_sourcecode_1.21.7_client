/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 */
package net.minecraft.world.item;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;

public record EitherHolder<T>(Either<Holder<T>, ResourceKey<T>> contents) {
    public EitherHolder(Holder<T> holder) {
        this(Either.left(holder));
    }

    public EitherHolder(ResourceKey<T> resourceKey) {
        this(Either.right(resourceKey));
    }

    public static <T> Codec<EitherHolder<T>> codec(ResourceKey<Registry<T>> resourceKey2, Codec<Holder<T>> codec) {
        return Codec.either(codec, (Codec)ResourceKey.codec(resourceKey2).comapFlatMap(resourceKey -> DataResult.error(() -> "Cannot parse as key without registry"), Function.identity())).xmap(EitherHolder::new, EitherHolder::contents);
    }

    public static <T> StreamCodec<RegistryFriendlyByteBuf, EitherHolder<T>> streamCodec(ResourceKey<Registry<T>> resourceKey, StreamCodec<RegistryFriendlyByteBuf, Holder<T>> streamCodec) {
        return StreamCodec.composite(ByteBufCodecs.either(streamCodec, ResourceKey.streamCodec(resourceKey)), EitherHolder::contents, EitherHolder::new);
    }

    public Optional<T> unwrap(Registry<T> registry) {
        return (Optional)this.contents.map(holder -> Optional.of(holder.value()), registry::getOptional);
    }

    public Optional<Holder<T>> unwrap(HolderLookup.Provider provider) {
        return (Optional)this.contents.map(Optional::of, resourceKey -> provider.get(resourceKey).map(reference -> reference));
    }

    public Optional<ResourceKey<T>> key() {
        return (Optional)this.contents.map(Holder::unwrapKey, Optional::of);
    }
}

