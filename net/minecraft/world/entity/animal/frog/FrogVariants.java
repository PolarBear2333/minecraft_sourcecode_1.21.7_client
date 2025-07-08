/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.animal.frog;

import net.minecraft.core.ClientAsset;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.animal.TemperatureVariants;
import net.minecraft.world.entity.animal.frog.FrogVariant;
import net.minecraft.world.entity.variant.BiomeCheck;
import net.minecraft.world.entity.variant.SpawnPrioritySelectors;
import net.minecraft.world.level.biome.Biome;

public interface FrogVariants {
    public static final ResourceKey<FrogVariant> TEMPERATE = FrogVariants.createKey(TemperatureVariants.TEMPERATE);
    public static final ResourceKey<FrogVariant> WARM = FrogVariants.createKey(TemperatureVariants.WARM);
    public static final ResourceKey<FrogVariant> COLD = FrogVariants.createKey(TemperatureVariants.COLD);

    private static ResourceKey<FrogVariant> createKey(ResourceLocation resourceLocation) {
        return ResourceKey.create(Registries.FROG_VARIANT, resourceLocation);
    }

    public static void bootstrap(BootstrapContext<FrogVariant> bootstrapContext) {
        FrogVariants.register(bootstrapContext, TEMPERATE, "entity/frog/temperate_frog", SpawnPrioritySelectors.fallback(0));
        FrogVariants.register(bootstrapContext, WARM, "entity/frog/warm_frog", BiomeTags.SPAWNS_WARM_VARIANT_FROGS);
        FrogVariants.register(bootstrapContext, COLD, "entity/frog/cold_frog", BiomeTags.SPAWNS_COLD_VARIANT_FROGS);
    }

    private static void register(BootstrapContext<FrogVariant> bootstrapContext, ResourceKey<FrogVariant> resourceKey, String string, TagKey<Biome> tagKey) {
        HolderSet.Named<Biome> named = bootstrapContext.lookup(Registries.BIOME).getOrThrow(tagKey);
        FrogVariants.register(bootstrapContext, resourceKey, string, SpawnPrioritySelectors.single(new BiomeCheck(named), 1));
    }

    private static void register(BootstrapContext<FrogVariant> bootstrapContext, ResourceKey<FrogVariant> resourceKey, String string, SpawnPrioritySelectors spawnPrioritySelectors) {
        bootstrapContext.register(resourceKey, new FrogVariant(new ClientAsset(ResourceLocation.withDefaultNamespace(string)), spawnPrioritySelectors));
    }
}

