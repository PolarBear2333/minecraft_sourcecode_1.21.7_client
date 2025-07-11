/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonDeserializationContext
 *  com.google.gson.JsonDeserializer
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  org.apache.commons.lang3.Validate
 */
package net.minecraft.client.resources.sounds;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundEventRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.FloatProvider;
import org.apache.commons.lang3.Validate;

public class SoundEventRegistrationSerializer
implements JsonDeserializer<SoundEventRegistration> {
    private static final FloatProvider DEFAULT_FLOAT = ConstantFloat.of(1.0f);

    public SoundEventRegistration deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = GsonHelper.convertToJsonObject(jsonElement, "entry");
        boolean bl = GsonHelper.getAsBoolean(jsonObject, "replace", false);
        String string = GsonHelper.getAsString(jsonObject, "subtitle", null);
        List<Sound> list = this.getSounds(jsonObject);
        return new SoundEventRegistration(list, bl, string);
    }

    private List<Sound> getSounds(JsonObject jsonObject) {
        ArrayList arrayList = Lists.newArrayList();
        if (jsonObject.has("sounds")) {
            JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, "sounds");
            for (int i = 0; i < jsonArray.size(); ++i) {
                JsonElement jsonElement = jsonArray.get(i);
                if (GsonHelper.isStringValue(jsonElement)) {
                    ResourceLocation resourceLocation = ResourceLocation.parse(GsonHelper.convertToString(jsonElement, "sound"));
                    arrayList.add(new Sound(resourceLocation, DEFAULT_FLOAT, DEFAULT_FLOAT, 1, Sound.Type.FILE, false, false, 16));
                    continue;
                }
                arrayList.add(this.getSound(GsonHelper.convertToJsonObject(jsonElement, "sound")));
            }
        }
        return arrayList;
    }

    private Sound getSound(JsonObject jsonObject) {
        ResourceLocation resourceLocation = ResourceLocation.parse(GsonHelper.getAsString(jsonObject, "name"));
        Sound.Type type = this.getType(jsonObject, Sound.Type.FILE);
        float f = GsonHelper.getAsFloat(jsonObject, "volume", 1.0f);
        Validate.isTrue((f > 0.0f ? 1 : 0) != 0, (String)"Invalid volume", (Object[])new Object[0]);
        float f2 = GsonHelper.getAsFloat(jsonObject, "pitch", 1.0f);
        Validate.isTrue((f2 > 0.0f ? 1 : 0) != 0, (String)"Invalid pitch", (Object[])new Object[0]);
        int n = GsonHelper.getAsInt(jsonObject, "weight", 1);
        Validate.isTrue((n > 0 ? 1 : 0) != 0, (String)"Invalid weight", (Object[])new Object[0]);
        boolean bl = GsonHelper.getAsBoolean(jsonObject, "preload", false);
        boolean bl2 = GsonHelper.getAsBoolean(jsonObject, "stream", false);
        int n2 = GsonHelper.getAsInt(jsonObject, "attenuation_distance", 16);
        return new Sound(resourceLocation, ConstantFloat.of(f), ConstantFloat.of(f2), n, type, bl2, bl, n2);
    }

    private Sound.Type getType(JsonObject jsonObject, Sound.Type type) {
        Sound.Type type2 = type;
        if (jsonObject.has("type")) {
            type2 = Sound.Type.getByName(GsonHelper.getAsString(jsonObject, "type"));
            Validate.notNull((Object)((Object)type2), (String)"Invalid type", (Object[])new Object[0]);
        }
        return type2;
    }

    public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        return this.deserialize(jsonElement, type, jsonDeserializationContext);
    }
}

