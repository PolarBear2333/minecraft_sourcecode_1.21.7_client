/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonDeserializationContext
 *  com.google.gson.JsonDeserializer
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  javax.annotation.Nullable
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.core.Direction;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public record BlockElement(Vector3fc from, Vector3fc to, Map<Direction, BlockElementFace> faces, @Nullable BlockElementRotation rotation, boolean shade, int lightEmission) {
    private static final boolean DEFAULT_RESCALE = false;
    private static final float MIN_EXTENT = -16.0f;
    private static final float MAX_EXTENT = 32.0f;

    public BlockElement(Vector3fc vector3fc, Vector3fc vector3fc2, Map<Direction, BlockElementFace> map) {
        this(vector3fc, vector3fc2, map, null, true, 0);
    }

    protected static class Deserializer
    implements JsonDeserializer<BlockElement> {
        private static final boolean DEFAULT_SHADE = true;
        private static final int DEFAULT_LIGHT_EMISSION = 0;

        protected Deserializer() {
        }

        public BlockElement deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            Vector3f vector3f = this.getFrom(jsonObject);
            Vector3f vector3f2 = this.getTo(jsonObject);
            BlockElementRotation blockElementRotation = this.getRotation(jsonObject);
            Map<Direction, BlockElementFace> map = this.getFaces(jsonDeserializationContext, jsonObject);
            if (jsonObject.has("shade") && !GsonHelper.isBooleanValue(jsonObject, "shade")) {
                throw new JsonParseException("Expected shade to be a Boolean");
            }
            boolean bl = GsonHelper.getAsBoolean(jsonObject, "shade", true);
            int n = 0;
            if (jsonObject.has("light_emission")) {
                boolean bl2 = GsonHelper.isNumberValue(jsonObject, "light_emission");
                if (bl2) {
                    n = GsonHelper.getAsInt(jsonObject, "light_emission");
                }
                if (!bl2 || n < 0 || n > 15) {
                    throw new JsonParseException("Expected light_emission to be an Integer between (inclusive) 0 and 15");
                }
            }
            return new BlockElement((Vector3fc)vector3f, (Vector3fc)vector3f2, map, blockElementRotation, bl, n);
        }

        @Nullable
        private BlockElementRotation getRotation(JsonObject jsonObject) {
            BlockElementRotation blockElementRotation = null;
            if (jsonObject.has("rotation")) {
                JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "rotation");
                Vector3f vector3f = this.getVector3f(jsonObject2, "origin");
                vector3f.mul(0.0625f);
                Direction.Axis axis = this.getAxis(jsonObject2);
                float f = this.getAngle(jsonObject2);
                boolean bl = GsonHelper.getAsBoolean(jsonObject2, "rescale", false);
                blockElementRotation = new BlockElementRotation(vector3f, axis, f, bl);
            }
            return blockElementRotation;
        }

        private float getAngle(JsonObject jsonObject) {
            float f = GsonHelper.getAsFloat(jsonObject, "angle");
            if (Mth.abs(f) > 45.0f) {
                throw new JsonParseException("Invalid rotation " + f + " found, only values in [-45,45] range allowed");
            }
            return f;
        }

        private Direction.Axis getAxis(JsonObject jsonObject) {
            String string = GsonHelper.getAsString(jsonObject, "axis");
            Direction.Axis axis = Direction.Axis.byName(string.toLowerCase(Locale.ROOT));
            if (axis == null) {
                throw new JsonParseException("Invalid rotation axis: " + string);
            }
            return axis;
        }

        private Map<Direction, BlockElementFace> getFaces(JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObject) {
            Map<Direction, BlockElementFace> map = this.filterNullFromFaces(jsonDeserializationContext, jsonObject);
            if (map.isEmpty()) {
                throw new JsonParseException("Expected between 1 and 6 unique faces, got 0");
            }
            return map;
        }

        private Map<Direction, BlockElementFace> filterNullFromFaces(JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObject) {
            EnumMap enumMap = Maps.newEnumMap(Direction.class);
            JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "faces");
            for (Map.Entry entry : jsonObject2.entrySet()) {
                Direction direction = this.getFacing((String)entry.getKey());
                enumMap.put(direction, (BlockElementFace)jsonDeserializationContext.deserialize((JsonElement)entry.getValue(), BlockElementFace.class));
            }
            return enumMap;
        }

        private Direction getFacing(String string) {
            Direction direction = Direction.byName(string);
            if (direction == null) {
                throw new JsonParseException("Unknown facing: " + string);
            }
            return direction;
        }

        private Vector3f getTo(JsonObject jsonObject) {
            Vector3f vector3f = this.getVector3f(jsonObject, "to");
            if (vector3f.x() < -16.0f || vector3f.y() < -16.0f || vector3f.z() < -16.0f || vector3f.x() > 32.0f || vector3f.y() > 32.0f || vector3f.z() > 32.0f) {
                throw new JsonParseException("'to' specifier exceeds the allowed boundaries: " + String.valueOf(vector3f));
            }
            return vector3f;
        }

        private Vector3f getFrom(JsonObject jsonObject) {
            Vector3f vector3f = this.getVector3f(jsonObject, "from");
            if (vector3f.x() < -16.0f || vector3f.y() < -16.0f || vector3f.z() < -16.0f || vector3f.x() > 32.0f || vector3f.y() > 32.0f || vector3f.z() > 32.0f) {
                throw new JsonParseException("'from' specifier exceeds the allowed boundaries: " + String.valueOf(vector3f));
            }
            return vector3f;
        }

        private Vector3f getVector3f(JsonObject jsonObject, String string) {
            JsonArray jsonArray = GsonHelper.getAsJsonArray(jsonObject, string);
            if (jsonArray.size() != 3) {
                throw new JsonParseException("Expected 3 " + string + " values, found: " + jsonArray.size());
            }
            float[] fArray = new float[3];
            for (int i = 0; i < fArray.length; ++i) {
                fArray[i] = GsonHelper.convertToFloat(jsonArray.get(i), string + "[" + i + "]");
            }
            return new Vector3f(fArray[0], fArray[1], fArray[2]);
        }

        public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return this.deserialize(jsonElement, type, jsonDeserializationContext);
        }
    }
}

