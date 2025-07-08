/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.util.datafix;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.LenientJsonParser;

public class LegacyComponentDataFixUtils {
    private static final String EMPTY_CONTENTS = LegacyComponentDataFixUtils.createTextComponentJson("");

    public static <T> Dynamic<T> createPlainTextComponent(DynamicOps<T> dynamicOps, String string) {
        String string2 = LegacyComponentDataFixUtils.createTextComponentJson(string);
        return new Dynamic(dynamicOps, dynamicOps.createString(string2));
    }

    public static <T> Dynamic<T> createEmptyComponent(DynamicOps<T> dynamicOps) {
        return new Dynamic(dynamicOps, dynamicOps.createString(EMPTY_CONTENTS));
    }

    public static String createTextComponentJson(String string) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("text", string);
        return GsonHelper.toStableString((JsonElement)jsonObject);
    }

    public static String createTranslatableComponentJson(String string) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("translate", string);
        return GsonHelper.toStableString((JsonElement)jsonObject);
    }

    public static <T> Dynamic<T> createTranslatableComponent(DynamicOps<T> dynamicOps, String string) {
        String string2 = LegacyComponentDataFixUtils.createTranslatableComponentJson(string);
        return new Dynamic(dynamicOps, dynamicOps.createString(string2));
    }

    public static String rewriteFromLenient(String string) {
        if (string.isEmpty() || string.equals("null")) {
            return EMPTY_CONTENTS;
        }
        char c = string.charAt(0);
        char c2 = string.charAt(string.length() - 1);
        if (c == '\"' && c2 == '\"' || c == '{' && c2 == '}' || c == '[' && c2 == ']') {
            try {
                JsonElement jsonElement = LenientJsonParser.parse(string);
                if (jsonElement.isJsonPrimitive()) {
                    return LegacyComponentDataFixUtils.createTextComponentJson(jsonElement.getAsString());
                }
                return GsonHelper.toStableString(jsonElement);
            }
            catch (JsonParseException jsonParseException) {
                // empty catch block
            }
        }
        return LegacyComponentDataFixUtils.createTextComponentJson(string);
    }

    public static Optional<String> extractTranslationString(String string) {
        try {
            JsonObject jsonObject;
            JsonElement jsonElement;
            JsonElement jsonElement2 = LenientJsonParser.parse(string);
            if (jsonElement2.isJsonObject() && (jsonElement = (jsonObject = jsonElement2.getAsJsonObject()).get("translate")) != null && jsonElement.isJsonPrimitive()) {
                return Optional.of(jsonElement.getAsString());
            }
        }
        catch (JsonParseException jsonParseException) {
            // empty catch block
        }
        return Optional.empty();
    }
}

