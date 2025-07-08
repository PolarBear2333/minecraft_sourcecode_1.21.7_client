/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 */
package com.mojang.realmsclient.dto;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.realmsclient.dto.ValueObject;
import java.util.Set;
import net.minecraft.util.LenientJsonParser;

public class Ops
extends ValueObject {
    public Set<String> ops = Sets.newHashSet();

    public static Ops parse(String string) {
        Ops ops = new Ops();
        try {
            JsonObject jsonObject = LenientJsonParser.parse(string).getAsJsonObject();
            JsonElement jsonElement = jsonObject.get("ops");
            if (jsonElement.isJsonArray()) {
                for (JsonElement jsonElement2 : jsonElement.getAsJsonArray()) {
                    ops.ops.add(jsonElement2.getAsString());
                }
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return ops;
    }
}

