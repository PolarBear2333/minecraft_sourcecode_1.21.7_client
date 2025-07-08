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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record Cooldown() implements RangeSelectItemModelProperty
{
    public static final MapCodec<Cooldown> MAP_CODEC = MapCodec.unit((Object)new Cooldown());

    @Override
    public float get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int n) {
        float f;
        if (livingEntity instanceof Player) {
            Player player = (Player)livingEntity;
            f = player.getCooldowns().getCooldownPercent(itemStack, 0.0f);
        } else {
            f = 0.0f;
        }
        return f;
    }

    public MapCodec<Cooldown> type() {
        return MAP_CODEC;
    }
}

