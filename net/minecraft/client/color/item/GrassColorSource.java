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
import net.minecraft.world.level.GrassColor;

public record GrassColorSource(float temperature, float downfall) implements ItemTintSource
{
    public static final MapCodec<GrassColorSource> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ExtraCodecs.floatRange(0.0f, 1.0f).fieldOf("temperature").forGetter(GrassColorSource::temperature), (App)ExtraCodecs.floatRange(0.0f, 1.0f).fieldOf("downfall").forGetter(GrassColorSource::downfall)).apply((Applicative)instance, GrassColorSource::new));

    public GrassColorSource() {
        this(0.5f, 1.0f);
    }

    @Override
    public int calculate(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity) {
        return GrassColor.get(this.temperature, this.downfall);
    }

    public MapCodec<GrassColorSource> type() {
        return MAP_CODEC;
    }
}

