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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public record IsUsingItem() implements ConditionalItemModelProperty
{
    public static final MapCodec<IsUsingItem> MAP_CODEC = MapCodec.unit((Object)new IsUsingItem());

    @Override
    public boolean get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int n, ItemDisplayContext itemDisplayContext) {
        if (livingEntity == null) {
            return false;
        }
        return livingEntity.isUsingItem() && livingEntity.getUseItem() == itemStack;
    }

    public MapCodec<IsUsingItem> type() {
        return MAP_CODEC;
    }
}

