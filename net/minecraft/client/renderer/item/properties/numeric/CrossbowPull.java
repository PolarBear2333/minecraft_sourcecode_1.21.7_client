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
import net.minecraft.client.renderer.item.properties.numeric.UseDuration;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;

public class CrossbowPull
implements RangeSelectItemModelProperty {
    public static final MapCodec<CrossbowPull> MAP_CODEC = MapCodec.unit((Object)new CrossbowPull());

    @Override
    public float get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int n) {
        if (livingEntity == null) {
            return 0.0f;
        }
        if (CrossbowItem.isCharged(itemStack)) {
            return 0.0f;
        }
        int n2 = CrossbowItem.getChargeDuration(itemStack, livingEntity);
        return (float)UseDuration.useDuration(itemStack, livingEntity) / (float)n2;
    }

    public MapCodec<CrossbowPull> type() {
        return MAP_CODEC;
    }
}

