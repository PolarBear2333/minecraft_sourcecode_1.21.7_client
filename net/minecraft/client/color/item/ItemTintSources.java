/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.client.color.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.Constant;
import net.minecraft.client.color.item.CustomModelDataSource;
import net.minecraft.client.color.item.Dye;
import net.minecraft.client.color.item.Firework;
import net.minecraft.client.color.item.GrassColorSource;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.MapColor;
import net.minecraft.client.color.item.Potion;
import net.minecraft.client.color.item.TeamColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

public class ItemTintSources {
    private static final ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends ItemTintSource>> ID_MAPPER = new ExtraCodecs.LateBoundIdMapper();
    public static final Codec<ItemTintSource> CODEC = ID_MAPPER.codec(ResourceLocation.CODEC).dispatch(ItemTintSource::type, mapCodec -> mapCodec);

    public static void bootstrap() {
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("custom_model_data"), CustomModelDataSource.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("constant"), Constant.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("dye"), Dye.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("grass"), GrassColorSource.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("firework"), Firework.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("potion"), Potion.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("map_color"), MapColor.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("team"), TeamColor.MAP_CODEC);
    }
}

