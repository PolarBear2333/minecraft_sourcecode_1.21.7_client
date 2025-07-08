/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.Encoder
 *  com.mojang.serialization.JsonOps
 *  com.mojang.serialization.MapCodec
 *  org.slf4j.Logger
 */
package net.minecraft.data.info;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.MultiNoiseBiomeSourceParameterList;
import org.slf4j.Logger;

public class BiomeParametersDumpReport
implements DataProvider {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Path topPath;
    private final CompletableFuture<HolderLookup.Provider> registries;
    private static final MapCodec<ResourceKey<Biome>> ENTRY_CODEC = ResourceKey.codec(Registries.BIOME).fieldOf("biome");
    private static final Codec<Climate.ParameterList<ResourceKey<Biome>>> CODEC = Climate.ParameterList.codec(ENTRY_CODEC).fieldOf("biomes").codec();

    public BiomeParametersDumpReport(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> completableFuture) {
        this.topPath = packOutput.getOutputFolder(PackOutput.Target.REPORTS).resolve("biome_parameters");
        this.registries = completableFuture;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        return this.registries.thenCompose(provider -> {
            RegistryOps registryOps = provider.createSerializationContext(JsonOps.INSTANCE);
            ArrayList arrayList = new ArrayList();
            MultiNoiseBiomeSourceParameterList.knownPresets().forEach((preset, parameterList) -> arrayList.add(BiomeParametersDumpReport.dumpValue(this.createPath(preset.id()), cachedOutput, registryOps, CODEC, parameterList)));
            return CompletableFuture.allOf((CompletableFuture[])arrayList.toArray(CompletableFuture[]::new));
        });
    }

    private static <E> CompletableFuture<?> dumpValue(Path path, CachedOutput cachedOutput, DynamicOps<JsonElement> dynamicOps, Encoder<E> encoder, E e) {
        Optional optional = encoder.encodeStart(dynamicOps, e).resultOrPartial(string -> LOGGER.error("Couldn't serialize element {}: {}", (Object)path, string));
        if (optional.isPresent()) {
            return DataProvider.saveStable(cachedOutput, (JsonElement)optional.get(), path);
        }
        return CompletableFuture.completedFuture(null);
    }

    private Path createPath(ResourceLocation resourceLocation) {
        return this.topPath.resolve(resourceLocation.getNamespace()).resolve(resourceLocation.getPath() + ".json");
    }

    @Override
    public final String getName() {
        return "Biome Parameters";
    }
}

