/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.flag;

import java.util.Set;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;

public interface FeatureElement {
    public static final Set<ResourceKey<? extends Registry<? extends FeatureElement>>> FILTERED_REGISTRIES = Set.of(Registries.ITEM, Registries.BLOCK, Registries.ENTITY_TYPE, Registries.MENU, Registries.POTION, Registries.MOB_EFFECT);

    public FeatureFlagSet requiredFeatures();

    default public boolean isEnabled(FeatureFlagSet featureFlagSet) {
        return this.requiredFeatures().isSubsetOf(featureFlagSet);
    }
}

