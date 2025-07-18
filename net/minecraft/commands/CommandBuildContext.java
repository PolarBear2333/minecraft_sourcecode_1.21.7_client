/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.commands;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;

public interface CommandBuildContext
extends HolderLookup.Provider {
    public static CommandBuildContext simple(final HolderLookup.Provider provider, final FeatureFlagSet featureFlagSet) {
        return new CommandBuildContext(){

            @Override
            public Stream<ResourceKey<? extends Registry<?>>> listRegistryKeys() {
                return provider.listRegistryKeys();
            }

            public <T> Optional<HolderLookup.RegistryLookup<T>> lookup(ResourceKey<? extends Registry<? extends T>> resourceKey) {
                return provider.lookup(resourceKey).map(registryLookup -> registryLookup.filterFeatures(featureFlagSet));
            }

            @Override
            public FeatureFlagSet enabledFeatures() {
                return featureFlagSet;
            }
        };
    }

    public FeatureFlagSet enabledFeatures();
}

