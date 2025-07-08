/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package net.minecraft.client.data.models.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.resources.ResourceLocation;

public class DelegatedModel
implements ModelInstance {
    private final ResourceLocation parent;

    public DelegatedModel(ResourceLocation resourceLocation) {
        this.parent = resourceLocation;
    }

    @Override
    public JsonElement get() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("parent", this.parent.toString());
        return jsonObject;
    }

    @Override
    public /* synthetic */ Object get() {
        return this.get();
    }
}

