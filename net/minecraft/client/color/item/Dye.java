/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.client.color.item;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

public record Dye(int defaultColor) implements ItemTintSource
{
    public static final MapCodec<Dye> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ExtraCodecs.RGB_COLOR_CODEC.fieldOf("default").forGetter(Dye::defaultColor)).apply((Applicative)instance, Dye::new));

    @Override
    public int calculate(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity) {
        return DyedItemColor.getOrDefault(itemStack, this.defaultColor);
    }

    public MapCodec<Dye> type() {
        return MAP_CODEC;
    }
}

