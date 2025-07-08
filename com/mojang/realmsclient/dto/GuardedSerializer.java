/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.ExclusionStrategy
 *  com.google.gson.FieldAttributes
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonElement
 *  javax.annotation.Nullable
 */
package com.mojang.realmsclient.dto;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.realmsclient.dto.Exclude;
import com.mojang.realmsclient.dto.ReflectionBasedSerialization;
import javax.annotation.Nullable;

public class GuardedSerializer {
    ExclusionStrategy strategy = new ExclusionStrategy(this){

        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }

        public boolean shouldSkipField(FieldAttributes fieldAttributes) {
            return fieldAttributes.getAnnotation(Exclude.class) != null;
        }
    };
    private final Gson gson = new GsonBuilder().addSerializationExclusionStrategy(this.strategy).addDeserializationExclusionStrategy(this.strategy).create();

    public String toJson(ReflectionBasedSerialization reflectionBasedSerialization) {
        return this.gson.toJson((Object)reflectionBasedSerialization);
    }

    public String toJson(JsonElement jsonElement) {
        return this.gson.toJson(jsonElement);
    }

    @Nullable
    public <T extends ReflectionBasedSerialization> T fromJson(String string, Class<T> clazz) {
        return (T)((ReflectionBasedSerialization)this.gson.fromJson(string, clazz));
    }
}

