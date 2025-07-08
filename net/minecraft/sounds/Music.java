/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.sounds;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;

public record Music(Holder<SoundEvent> event, int minDelay, int maxDelay, boolean replaceCurrentMusic) {
    public static final Codec<Music> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)SoundEvent.CODEC.fieldOf("sound").forGetter(music -> music.event), (App)Codec.INT.fieldOf("min_delay").forGetter(music -> music.minDelay), (App)Codec.INT.fieldOf("max_delay").forGetter(music -> music.maxDelay), (App)Codec.BOOL.fieldOf("replace_current_music").forGetter(music -> music.replaceCurrentMusic)).apply((Applicative)instance, Music::new));
}

