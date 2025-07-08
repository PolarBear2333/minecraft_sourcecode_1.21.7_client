/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.item.properties.numeric;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;

public record BundleFullness() implements RangeSelectItemModelProperty
{
    public static final MapCodec<BundleFullness> MAP_CODEC = MapCodec.unit((Object)new BundleFullness());

    @Override
    public float get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int n) {
        return BundleItem.getFullnessDisplay(itemStack);
    }

    public MapCodec<BundleFullness> type() {
        return MAP_CODEC;
    }
}

