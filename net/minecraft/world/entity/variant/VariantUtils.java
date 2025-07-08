/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.variant;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.variant.PriorityProvider;
import net.minecraft.world.entity.variant.SpawnContext;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class VariantUtils {
    public static final String TAG_VARIANT = "variant";

    public static <T> Holder<T> getDefaultOrAny(RegistryAccess registryAccess, ResourceKey<T> resourceKey) {
        HolderLookup.RegistryLookup registryLookup = registryAccess.lookupOrThrow(resourceKey.registryKey());
        return (Holder)registryLookup.get(resourceKey).or(((Registry)registryLookup)::getAny).orElseThrow();
    }

    public static <T> Holder<T> getAny(RegistryAccess registryAccess, ResourceKey<? extends Registry<T>> resourceKey) {
        return registryAccess.lookupOrThrow(resourceKey).getAny().orElseThrow();
    }

    public static <T> void writeVariant(ValueOutput valueOutput, Holder<T> holder) {
        holder.unwrapKey().ifPresent(resourceKey -> valueOutput.store(TAG_VARIANT, ResourceLocation.CODEC, resourceKey.location()));
    }

    public static <T> Optional<Holder<T>> readVariant(ValueInput valueInput, ResourceKey<? extends Registry<T>> resourceKey) {
        return valueInput.read(TAG_VARIANT, ResourceLocation.CODEC).map(resourceLocation -> ResourceKey.create(resourceKey, resourceLocation)).flatMap(valueInput.lookup()::get);
    }

    public static <T extends PriorityProvider<SpawnContext, ?>> Optional<Holder.Reference<T>> selectVariantToSpawn(SpawnContext spawnContext, ResourceKey<Registry<T>> resourceKey) {
        ServerLevelAccessor serverLevelAccessor = spawnContext.level();
        Stream stream = serverLevelAccessor.registryAccess().lookupOrThrow(resourceKey).listElements();
        return PriorityProvider.pick(stream, Holder::value, serverLevelAccessor.getRandom(), spawnContext);
    }
}

