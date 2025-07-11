/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 */
package net.minecraft.data.metadata;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.minecraft.DetectedVersion;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FeatureFlagsMetadataSection;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.world.flag.FeatureFlagSet;

public class PackMetadataGenerator
implements DataProvider {
    private final PackOutput output;
    private final Map<String, Supplier<JsonElement>> elements = new HashMap<String, Supplier<JsonElement>>();

    public PackMetadataGenerator(PackOutput packOutput) {
        this.output = packOutput;
    }

    public <T> PackMetadataGenerator add(MetadataSectionType<T> metadataSectionType, T t) {
        this.elements.put(metadataSectionType.name(), () -> ((JsonElement)metadataSectionType.codec().encodeStart((DynamicOps)JsonOps.INSTANCE, t).getOrThrow(IllegalArgumentException::new)).getAsJsonObject());
        return this;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        JsonObject jsonObject = new JsonObject();
        this.elements.forEach((string, supplier) -> jsonObject.add(string, (JsonElement)supplier.get()));
        return DataProvider.saveStable(cachedOutput, (JsonElement)jsonObject, this.output.getOutputFolder().resolve("pack.mcmeta"));
    }

    @Override
    public final String getName() {
        return "Pack Metadata";
    }

    public static PackMetadataGenerator forFeaturePack(PackOutput packOutput, Component component) {
        return new PackMetadataGenerator(packOutput).add(PackMetadataSection.TYPE, new PackMetadataSection(component, DetectedVersion.BUILT_IN.packVersion(PackType.SERVER_DATA), Optional.empty()));
    }

    public static PackMetadataGenerator forFeaturePack(PackOutput packOutput, Component component, FeatureFlagSet featureFlagSet) {
        return PackMetadataGenerator.forFeaturePack(packOutput, component).add(FeatureFlagsMetadataSection.TYPE, new FeatureFlagsMetadataSection(featureFlagSet));
    }
}

