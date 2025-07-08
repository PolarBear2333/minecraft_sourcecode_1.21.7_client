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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public record DisplayContext() implements SelectItemModelProperty<ItemDisplayContext>
{
    public static final Codec<ItemDisplayContext> VALUE_CODEC = ItemDisplayContext.CODEC;
    public static final SelectItemModelProperty.Type<DisplayContext, ItemDisplayContext> TYPE = SelectItemModelProperty.Type.create(MapCodec.unit((Object)new DisplayContext()), VALUE_CODEC);

    @Override
    public ItemDisplayContext get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int n, ItemDisplayContext itemDisplayContext) {
        return itemDisplayContext;
    }

    @Override
    public SelectItemModelProperty.Type<DisplayContext, ItemDisplayContext> type() {
        return TYPE;
    }

    @Override
    public Codec<ItemDisplayContext> valueCodec() {
        return VALUE_CODEC;
    }

    @Override
    public /* synthetic */ Object get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int n, ItemDisplayContext itemDisplayContext) {
        return this.get(itemStack, clientLevel, livingEntity, n, itemDisplayContext);
    }
}

