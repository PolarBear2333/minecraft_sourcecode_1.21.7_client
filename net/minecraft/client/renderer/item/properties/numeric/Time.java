/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.client.renderer.item.properties.numeric;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.NeedleDirectionHelper;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

public class Time
extends NeedleDirectionHelper
implements RangeSelectItemModelProperty {
    public static final MapCodec<Time> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.BOOL.optionalFieldOf("wobble", (Object)true).forGetter(NeedleDirectionHelper::wobble), (App)TimeSource.CODEC.fieldOf("source").forGetter(time -> time.source)).apply((Applicative)instance, Time::new));
    private final TimeSource source;
    private final RandomSource randomSource = RandomSource.create();
    private final NeedleDirectionHelper.Wobbler wobbler;

    public Time(boolean bl, TimeSource timeSource) {
        super(bl);
        this.source = timeSource;
        this.wobbler = this.newWobbler(0.9f);
    }

    @Override
    protected float calculate(ItemStack itemStack, ClientLevel clientLevel, int n, Entity entity) {
        float f = this.source.get(clientLevel, itemStack, entity, this.randomSource);
        long l = clientLevel.getGameTime();
        if (this.wobbler.shouldUpdate(l)) {
            this.wobbler.update(l, f);
        }
        return this.wobbler.rotation();
    }

    public MapCodec<Time> type() {
        return MAP_CODEC;
    }

    public static enum TimeSource implements StringRepresentable
    {
        RANDOM("random"){

            @Override
            public float get(ClientLevel clientLevel, ItemStack itemStack, Entity entity, RandomSource randomSource) {
                return randomSource.nextFloat();
            }
        }
        ,
        DAYTIME("daytime"){

            @Override
            public float get(ClientLevel clientLevel, ItemStack itemStack, Entity entity, RandomSource randomSource) {
                return clientLevel.getTimeOfDay(1.0f);
            }
        }
        ,
        MOON_PHASE("moon_phase"){

            @Override
            public float get(ClientLevel clientLevel, ItemStack itemStack, Entity entity, RandomSource randomSource) {
                return (float)clientLevel.getMoonPhase() / 8.0f;
            }
        };

        public static final Codec<TimeSource> CODEC;
        private final String name;

        TimeSource(String string2) {
            this.name = string2;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        abstract float get(ClientLevel var1, ItemStack var2, Entity var3, RandomSource var4);

        static {
            CODEC = StringRepresentable.fromEnum(TimeSource::values);
        }
    }
}

