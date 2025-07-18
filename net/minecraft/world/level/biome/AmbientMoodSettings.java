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
import net.minecraft.sounds.SoundEvents;

public class AmbientMoodSettings {
    public static final Codec<AmbientMoodSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)SoundEvent.CODEC.fieldOf("sound").forGetter(ambientMoodSettings -> ambientMoodSettings.soundEvent), (App)Codec.INT.fieldOf("tick_delay").forGetter(ambientMoodSettings -> ambientMoodSettings.tickDelay), (App)Codec.INT.fieldOf("block_search_extent").forGetter(ambientMoodSettings -> ambientMoodSettings.blockSearchExtent), (App)Codec.DOUBLE.fieldOf("offset").forGetter(ambientMoodSettings -> ambientMoodSettings.soundPositionOffset)).apply((Applicative)instance, AmbientMoodSettings::new));
    public static final AmbientMoodSettings LEGACY_CAVE_SETTINGS = new AmbientMoodSettings(SoundEvents.AMBIENT_CAVE, 6000, 8, 2.0);
    private final Holder<SoundEvent> soundEvent;
    private final int tickDelay;
    private final int blockSearchExtent;
    private final double soundPositionOffset;

    public AmbientMoodSettings(Holder<SoundEvent> holder, int n, int n2, double d) {
        this.soundEvent = holder;
        this.tickDelay = n;
        this.blockSearchExtent = n2;
        this.soundPositionOffset = d;
    }

    public Holder<SoundEvent> getSoundEvent() {
        return this.soundEvent;
    }

    public int getTickDelay() {
        return this.tickDelay;
    }

    public int getBlockSearchExtent() {
        return this.blockSearchExtent;
    }

    public double getSoundPositionOffset() {
        return this.soundPositionOffset;
    }
}

