/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.ints.IntList
 *  javax.annotation.Nullable
 */
package net.minecraft.client.color.item;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntList;
import javax.annotation.Nullable;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.FireworkExplosion;

public record Firework(int defaultColor) implements ItemTintSource
{
    public static final MapCodec<Firework> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ExtraCodecs.RGB_COLOR_CODEC.fieldOf("default").forGetter(Firework::defaultColor)).apply((Applicative)instance, Firework::new));

    public Firework() {
        this(-7697782);
    }

    @Override
    public int calculate(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity) {
        FireworkExplosion fireworkExplosion = itemStack.get(DataComponents.FIREWORK_EXPLOSION);
        IntList intList = fireworkExplosion != null ? fireworkExplosion.colors() : IntList.of();
        int n = intList.size();
        if (n == 0) {
            return this.defaultColor;
        }
        if (n == 1) {
            return ARGB.opaque(intList.getInt(0));
        }
        int n2 = 0;
        int n3 = 0;
        int n4 = 0;
        for (int i = 0; i < n; ++i) {
            int n5 = intList.getInt(i);
            n2 += ARGB.red(n5);
            n3 += ARGB.green(n5);
            n4 += ARGB.blue(n5);
        }
        return ARGB.color(n2 / n, n3 / n, n4 / n);
    }

    public MapCodec<Firework> type() {
        return MAP_CODEC;
    }
}

