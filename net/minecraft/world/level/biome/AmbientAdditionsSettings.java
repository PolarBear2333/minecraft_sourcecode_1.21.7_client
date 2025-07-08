/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.biome;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;

public class AmbientAdditionsSettings {
    public static final Codec<AmbientAdditionsSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)SoundEvent.CODEC.fieldOf("sound").forGetter(ambientAdditionsSettings -> ambientAdditionsSettings.soundEvent), (App)Codec.DOUBLE.fieldOf("tick_chance").forGetter(ambientAdditionsSettings -> ambientAdditionsSettings.tickChance)).apply((Applicative)instance, AmbientAdditionsSettings::new));
    private final Holder<SoundEvent> soundEvent;
    private final double tickChance;

    public AmbientAdditionsSettings(Holder<SoundEvent> holder, double d) {
        this.soundEvent = holder;
        this.tickChance = d;
    }

    public Holder<SoundEvent> getSoundEvent() {
        return this.soundEvent;
    }

    public double getTickChance() {
        return this.tickChance;
    }
}

