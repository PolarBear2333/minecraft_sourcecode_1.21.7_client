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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface RangeSelectItemModelProperty {
    public float get(ItemStack var1, @Nullable ClientLevel var2, @Nullable LivingEntity var3, int var4);

    public MapCodec<? extends RangeSelectItemModelProperty> type();
}

