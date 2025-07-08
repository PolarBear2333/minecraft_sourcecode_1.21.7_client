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
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;

public record CustomModelDataSource(int index, int defaultColor) implements ItemTintSource
{
    public static final MapCodec<CustomModelDataSource> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("index", (Object)0).forGetter(CustomModelDataSource::index), (App)ExtraCodecs.RGB_COLOR_CODEC.fieldOf("default").forGetter(CustomModelDataSource::defaultColor)).apply((Applicative)instance, CustomModelDataSource::new));

    @Override
    public int calculate(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity) {
        Integer n;
        CustomModelData customModelData = itemStack.get(DataComponents.CUSTOM_MODEL_DATA);
        if (customModelData != null && (n = customModelData.getColor(this.index)) != null) {
            return ARGB.opaque(n);
        }
        return ARGB.opaque(this.defaultColor);
    }

    public MapCodec<CustomModelDataSource> type() {
        return MAP_CODEC;
    }
}

