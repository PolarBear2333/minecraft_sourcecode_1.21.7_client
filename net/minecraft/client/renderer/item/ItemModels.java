/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.client.renderer.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.item.BundleSelectedItemSpecialRenderer;
import net.minecraft.client.renderer.item.CompositeModel;
import net.minecraft.client.renderer.item.ConditionalItemModel;
import net.minecraft.client.renderer.item.EmptyModel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.RangeSelectItemModel;
import net.minecraft.client.renderer.item.SelectItemModel;
import net.minecraft.client.renderer.item.SpecialModelWrapper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

public class ItemModels {
    private static final ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends ItemModel.Unbaked>> ID_MAPPER = new ExtraCodecs.LateBoundIdMapper();
    public static final Codec<ItemModel.Unbaked> CODEC = ID_MAPPER.codec(ResourceLocation.CODEC).dispatch(ItemModel.Unbaked::type, mapCodec -> mapCodec);

    public static void bootstrap() {
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("empty"), EmptyModel.Unbaked.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("model"), BlockModelWrapper.Unbaked.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("range_dispatch"), RangeSelectItemModel.Unbaked.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("special"), SpecialModelWrapper.Unbaked.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("composite"), CompositeModel.Unbaked.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("bundle/selected_item"), BundleSelectedItemSpecialRenderer.Unbaked.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("select"), SelectItemModel.Unbaked.MAP_CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("condition"), ConditionalItemModel.Unbaked.MAP_CODEC);
    }
}

