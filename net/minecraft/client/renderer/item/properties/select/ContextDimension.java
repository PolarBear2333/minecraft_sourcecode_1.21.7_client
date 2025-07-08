/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.item.properties.select;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public record ContextDimension() implements SelectItemModelProperty<ResourceKey<Level>>
{
    public static final Codec<ResourceKey<Level>> VALUE_CODEC = ResourceKey.codec(Registries.DIMENSION);
    public static final SelectItemModelProperty.Type<ContextDimension, ResourceKey<Level>> TYPE = SelectItemModelProperty.Type.create(MapCodec.unit((Object)new ContextDimension()), VALUE_CODEC);

    @Override
    @Nullable
    public ResourceKey<Level> get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int n, ItemDisplayContext itemDisplayContext) {
        return clientLevel != null ? clientLevel.dimension() : null;
    }

    @Override
    public SelectItemModelProperty.Type<ContextDimension, ResourceKey<Level>> type() {
        return TYPE;
    }

    @Override
    public Codec<ResourceKey<Level>> valueCodec() {
        return VALUE_CODEC;
    }

    @Override
    @Nullable
    public /* synthetic */ Object get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int n, ItemDisplayContext itemDisplayContext) {
        return this.get(itemStack, clientLevel, livingEntity, n, itemDisplayContext);
    }
}

