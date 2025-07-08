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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public record UseDuration(boolean remaining) implements RangeSelectItemModelProperty
{
    public static final MapCodec<UseDuration> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.BOOL.optionalFieldOf("remaining", (Object)false).forGetter(UseDuration::remaining)).apply((Applicative)instance, UseDuration::new));

    @Override
    public float get(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity, int n) {
        if (livingEntity == null || livingEntity.getUseItem() != itemStack) {
            return 0.0f;
        }
        return this.remaining ? (float)livingEntity.getUseItemRemainingTicks() : (float)UseDuration.useDuration(itemStack, livingEntity);
    }

    public MapCodec<UseDuration> type() {
        return MAP_CODEC;
    }

    public static int useDuration(ItemStack itemStack, LivingEntity livingEntity) {
        return itemStack.getUseDuration(livingEntity) - livingEntity.getUseItemRemainingTicks();
    }
}

