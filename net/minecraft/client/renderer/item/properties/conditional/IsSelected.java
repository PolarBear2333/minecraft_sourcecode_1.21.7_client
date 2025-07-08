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
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public record IsSelected() implements ConditionalItemModelProperty
{
    public static final MapCodec<IsSelected> MAP_CODEC = MapCodec.unit((Object)new IsSelected());

    @Override
    public boolean get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int n, ItemDisplayContext itemDisplayContext) {
        LocalPlayer localPlayer;
        return livingEntity instanceof LocalPlayer && (localPlayer = (LocalPlayer)livingEntity).getInventory().getSelectedItem() == itemStack;
    }

    public MapCodec<IsSelected> type() {
        return MAP_CODEC;
    }
}

