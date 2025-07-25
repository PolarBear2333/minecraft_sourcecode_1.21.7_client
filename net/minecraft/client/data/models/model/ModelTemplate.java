/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Streams
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package net.minecraft.client.data.models.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class ModelTemplate {
    private final Optional<ResourceLocation> model;
    private final Set<TextureSlot> requiredSlots;
    private final Optional<String> suffix;

    public ModelTemplate(Optional<ResourceLocation> optional, Optional<String> optional2, TextureSlot ... textureSlotArray) {
        this.model = optional;
        this.suffix = optional2;
        this.requiredSlots = ImmutableSet.copyOf((Object[])textureSlotArray);
    }

    public ResourceLocation getDefaultModelLocation(Block block) {
        return ModelLocationUtils.getModelLocation(block, this.suffix.orElse(""));
    }

    public ResourceLocation create(Block block, TextureMapping textureMapping, BiConsumer<ResourceLocation, ModelInstance> biConsumer) {
        return this.create(ModelLocationUtils.getModelLocation(block, this.suffix.orElse("")), textureMapping, biConsumer);
    }

    public ResourceLocation createWithSuffix(Block block, String string, TextureMapping textureMapping, BiConsumer<ResourceLocation, ModelInstance> biConsumer) {
        return this.create(ModelLocationUtils.getModelLocation(block, string + this.suffix.orElse("")), textureMapping, biConsumer);
    }

    public ResourceLocation createWithOverride(Block block, String string, TextureMapping textureMapping, BiConsumer<ResourceLocation, ModelInstance> biConsumer) {
        return this.create(ModelLocationUtils.getModelLocation(block, string), textureMapping, biConsumer);
    }

    public ResourceLocation create(Item item, TextureMapping textureMapping, BiConsumer<ResourceLocation, ModelInstance> biConsumer) {
        return this.create(ModelLocationUtils.getModelLocation(item, this.suffix.orElse("")), textureMapping, biConsumer);
    }

    public ResourceLocation create(ResourceLocation resourceLocation, TextureMapping textureMapping, BiConsumer<ResourceLocation, ModelInstance> biConsumer) {
        Map<TextureSlot, ResourceLocation> map = this.createMap(textureMapping);
        biConsumer.accept(resourceLocation, () -> {
            JsonObject jsonObject = new JsonObject();
            this.model.ifPresent(resourceLocation -> jsonObject.addProperty("parent", resourceLocation.toString()));
            if (!map.isEmpty()) {
                JsonObject jsonObject2 = new JsonObject();
                map.forEach((textureSlot, resourceLocation) -> jsonObject2.addProperty(textureSlot.getId(), resourceLocation.toString()));
                jsonObject.add("textures", (JsonElement)jsonObject2);
            }
            return jsonObject;
        });
        return resourceLocation;
    }

    private Map<TextureSlot, ResourceLocation> createMap(TextureMapping textureMapping) {
        return (Map)Streams.concat((Stream[])new Stream[]{this.requiredSlots.stream(), textureMapping.getForced()}).collect(ImmutableMap.toImmutableMap(Function.identity(), textureMapping::get));
    }
}

