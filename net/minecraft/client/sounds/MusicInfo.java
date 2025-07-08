/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.sounds;

import javax.annotation.Nullable;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.Music;

public record MusicInfo(@Nullable Music music, float volume) {
    public MusicInfo(Music music) {
        this(music, 1.0f);
    }

    public boolean canReplace(SoundInstance soundInstance) {
        if (this.music == null) {
            return false;
        }
        return this.music.replaceCurrentMusic() && !this.music.event().value().location().equals(soundInstance.getLocation());
    }
}

