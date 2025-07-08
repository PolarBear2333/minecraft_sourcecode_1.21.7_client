/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Streams
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonObject
 */
package net.minecraft.client.particle;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public class ParticleDescription {
    private final List<ResourceLocation> textures;

    private ParticleDescription(List<ResourceLocation> list) {
        this.textures = list;
    }

    public List<ResourceLocation> getTextures() {
        return this.textures;
    }

    public static ParticleDescription fromJson(JsonObject jsonObject) {
        JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "textures", null);
        if (jsonArray == null) {
            return new ParticleDescription(List.of());
        }
        List list = (List)Streams.stream((Iterable)jsonArray).map(jsonElement -> GsonHelper.convertToString(jsonElement, "texture")).map(ResourceLocation::parse).collect(ImmutableList.toImmutableList());
        return new ParticleDescription(list);
    }
}

