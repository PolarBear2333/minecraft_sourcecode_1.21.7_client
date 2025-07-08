/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.item.properties.conditional;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public record BundleHasSelectedItem() implements ConditionalItemModelProperty
{
    public static final MapCodec<BundleHasSelectedItem> MAP_CODEC = MapCodec.unit((Object)new BundleHasSelectedItem());

    @Override
    public boolean get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int n, ItemDisplayContext itemDisplayContext) {
        return BundleItem.hasSelectedItem(itemStack);
    }

    public MapCodec<BundleHasSelectedItem> type() {
        return MAP_CODEC;
    }
}

