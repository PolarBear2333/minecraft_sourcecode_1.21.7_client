/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonDeserializationContext
 *  com.google.gson.JsonDeserializer
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.block.model;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.SimpleUnbakedGeometry;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.UnbakedGeometry;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

public record BlockModel(@Nullable UnbakedGeometry geometry, @Nullable UnbakedModel.GuiLight guiLight, @Nullable Boolean ambientOcclusion, @Nullable ItemTransforms transforms, TextureSlots.Data textureSlots, @Nullable ResourceLocation parent) implements UnbakedModel
{
    @VisibleForTesting
    static final Gson GSON = new GsonBuilder().registerTypeAdapter(BlockModel.class, (Object)new Deserializer()).registerTypeAdapter(BlockElement.class, (Object)new BlockElement.Deserializer()).registerTypeAdapter(BlockElementFace.class, (Object)new BlockElementFace.Deserializer()).registerTypeAdapter(ItemTransform.class, (Object)new ItemTransform.Deserializer()).registerTypeAdapter(ItemTransforms.class, (Object)new ItemTransforms.Deserializer()).create();

    public static BlockModel fromStream(Reader reader) {
        return GsonHelper.fromJson(GSON, reader, BlockModel.class);
    }

    public static class Deserializer
    implements JsonDeserializer<BlockModel> {
        public BlockModel deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            Object object;
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            UnbakedGeometry unbakedGeometry = this.getElements(jsonDeserializationContext, jsonObject);
            String string = this.getParentName(jsonObject);
            TextureSlots.Data data = this.getTextureMap(jsonObject);
            Boolean bl = this.getAmbientOcclusion(jsonObject);
            ItemTransforms itemTransforms = null;
            if (jsonObject.has("display")) {
                object = GsonHelper.getAsJsonObject(jsonObject, "display");
                itemTransforms = (ItemTransforms)jsonDeserializationContext.deserialize((JsonElement)object, ItemTransforms.class);
            }
            object = null;
            if (jsonObject.has("gui_light")) {
                object = UnbakedModel.GuiLight.getByName(GsonHelper.getAsString(jsonObject, "gui_light"));
            }
            ResourceLocation resourceLocation = string.isEmpty() ? null : ResourceLocation.parse(string);
            return new BlockModel(unbakedGeometry, (UnbakedModel.GuiLight)((Object)object), bl, itemTransforms, data, resourceLocation);
        }

        private TextureSlots.Data getTextureMap(JsonObject jsonObject) {
            if (jsonObject.has("textures")) {
                JsonObject jsonObject2 = GsonHelper.getAsJsonObject(jsonObject, "textures");
                return TextureSlots.parseTextureMap(jsonObject2, TextureAtlas.LOCATION_BLOCKS);
            }
            return TextureSlots.Data.EMPTY;
        }

        private String getParentName(JsonObject jsonObject) {
            return GsonHelper.getAsString(jsonObject, "parent", "");
        }

        @Nullable
        protected Boolean getAmbientOcclusion(JsonObject jsonObject) {
            if (jsonObject.has("ambientocclusion")) {
                return GsonHelper.getAsBoolean(jsonObject, "ambientocclusion");
            }
            return null;
        }

        @Nullable
        protected UnbakedGeometry getElements(JsonDeserializationContext jsonDeserializationContext, JsonObject jsonObject) {
            if (jsonObject.has("elements")) {
                ArrayList<BlockElement> arrayList = new ArrayList<BlockElement>();
                for (JsonElement jsonElement : GsonHelper.getAsJsonArray(jsonObject, "elements")) {
                    arrayList.add((BlockElement)jsonDeserializationContext.deserialize(jsonElement, BlockElement.class));
                }
                return new SimpleUnbakedGeometry(arrayList);
            }
            return null;
        }

        public /* synthetic */ Object deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return this.deserialize(jsonElement, type, jsonDeserializationContext);
        }
    }
}

