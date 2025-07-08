/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.item.properties.numeric;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public record Damage(boolean normalize) implements RangeSelectItemModelProperty
{
    public static final MapCodec<Damage> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.BOOL.optionalFieldOf("normalize", (Object)true).forGetter(Damage::normalize)).apply((Applicative)instance, Damage::new));

    @Override
    public float get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int n) {
        float f = itemStack.getDamageValue();
        float f2 = itemStack.getMaxDamage();
        if (this.normalize) {
            return Mth.clamp(f / f2, 0.0f, 1.0f);
        }
        return Mth.clamp(f, 0.0f, f2);
    }

    public MapCodec<Damage> type() {
        return MAP_CODEC;
    }
}

