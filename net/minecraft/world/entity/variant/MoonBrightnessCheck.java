/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.entity.variant;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.world.entity.variant.SpawnCondition;
import net.minecraft.world.entity.variant.SpawnContext;

public record MoonBrightnessCheck(MinMaxBounds.Doubles range) implements SpawnCondition
{
    public static final MapCodec<MoonBrightnessCheck> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)MinMaxBounds.Doubles.CODEC.fieldOf("range").forGetter(MoonBrightnessCheck::range)).apply((Applicative)instance, MoonBrightnessCheck::new));

    @Override
    public boolean test(SpawnContext spawnContext) {
        return this.range.matches(spawnContext.level().getLevel().getMoonBrightness());
    }

    public MapCodec<MoonBrightnessCheck> codec() {
        return MAP_CODEC;
    }

    @Override
    public /* synthetic */ boolean test(Object object) {
        return this.test((SpawnContext)object);
    }
}

