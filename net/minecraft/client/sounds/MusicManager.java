/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  javax.annotation.Nullable
 */
package net.minecraft.client.sounds;

import com.mojang.serialization.Codec;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.MusicInfo;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.OptionEnum;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;

public class MusicManager {
    private static final int STARTING_DELAY = 100;
    private final RandomSource random = RandomSource.create();
    private final Minecraft minecraft;
    @Nullable
    private SoundInstance currentMusic;
    private MusicFrequency gameMusicFrequency;
    private float currentGain = 1.0f;
    private int nextSongDelay = 100;
    private boolean toastShown = false;

    public MusicManager(Minecraft minecraft) {
        this.minecraft = minecraft;
        this.gameMusicFrequency = minecraft.options.musicFrequency().get();
    }

    public void tick() {
        boolean bl;
        MusicInfo musicInfo = this.minecraft.getSituationalMusic();
        float f = musicInfo.volume();
        if (this.currentMusic != null && this.currentGain != f && !(bl = this.fadePlaying(f))) {
            return;
        }
        Music music = musicInfo.music();
        if (music == null) {
            this.nextSongDelay = Math.max(this.nextSongDelay, 100);
            return;
        }
        if (this.currentMusic != null) {
            if (musicInfo.canReplace(this.currentMusic)) {
                this.minecraft.getSoundManager().stop(this.currentMusic);
                this.nextSongDelay = Mth.nextInt(this.random, 0, music.minDelay() / 2);
            }
            if (!this.minecraft.getSoundManager().isActive(this.currentMusic)) {
                this.currentMusic = null;
                this.nextSongDelay = Math.min(this.nextSongDelay, this.gameMusicFrequency.getNextSongDelay(music, this.random));
            }
        }
        this.nextSongDelay = Math.min(this.nextSongDelay, this.gameMusicFrequency.getNextSongDelay(music, this.random));
        if (this.currentMusic == null && this.nextSongDelay-- <= 0) {
            this.startPlaying(musicInfo);
        }
    }

    public void startPlaying(MusicInfo musicInfo) {
        SoundEvent soundEvent = musicInfo.music().event().value();
        this.currentMusic = SimpleSoundInstance.forMusic(soundEvent, musicInfo.volume());
        switch (this.minecraft.getSoundManager().play(this.currentMusic)) {
            case STARTED: {
                this.minecraft.getToastManager().showNowPlayingToast();
                this.toastShown = true;
                break;
            }
            case STARTED_SILENTLY: {
                this.toastShown = false;
            }
        }
        this.nextSongDelay = Integer.MAX_VALUE;
        this.currentGain = musicInfo.volume();
    }

    public void showNowPlayingToastIfNeeded() {
        if (!this.toastShown) {
            this.minecraft.getToastManager().showNowPlayingToast();
            this.toastShown = true;
        }
    }

    public void stopPlaying(Music music) {
        if (this.isPlayingMusic(music)) {
            this.stopPlaying();
        }
    }

    public void stopPlaying() {
        if (this.currentMusic != null) {
            this.minecraft.getSoundManager().stop(this.currentMusic);
            this.currentMusic = null;
            this.minecraft.getToastManager().hideNowPlayingToast();
        }
        this.nextSongDelay += 100;
    }

    private boolean fadePlaying(float f) {
        if (this.currentMusic == null) {
            return false;
        }
        if (this.currentGain == f) {
            return true;
        }
        if (this.currentGain < f) {
            this.currentGain += Mth.clamp(this.currentGain, 5.0E-4f, 0.005f);
            if (this.currentGain > f) {
                this.currentGain = f;
            }
        } else {
            this.currentGain = 0.03f * f + 0.97f * this.currentGain;
            if (Math.abs(this.currentGain - f) < 1.0E-4f || this.currentGain < f) {
                this.currentGain = f;
            }
        }
        this.currentGain = Mth.clamp(this.currentGain, 0.0f, 1.0f);
        if (this.currentGain <= 1.0E-4f) {
            this.stopPlaying();
            return false;
        }
        this.minecraft.getSoundManager().setVolume(this.currentMusic, this.currentGain);
        return true;
    }

    public boolean isPlayingMusic(Music music) {
        if (this.currentMusic == null) {
            return false;
        }
        return music.event().value().location().equals(this.currentMusic.getLocation());
    }

    @Nullable
    public String getCurrentMusicTranslationKey() {
        Sound sound;
        if (this.currentMusic != null && (sound = this.currentMusic.getSound()) != null) {
            return sound.getLocation().toShortLanguageKey();
        }
        return null;
    }

    public void setMinutesBetweenSongs(MusicFrequency musicFrequency) {
        this.gameMusicFrequency = musicFrequency;
        this.nextSongDelay = this.gameMusicFrequency.getNextSongDelay(this.minecraft.getSituationalMusic().music(), this.random);
    }

    public static enum MusicFrequency implements OptionEnum,
    StringRepresentable
    {
        DEFAULT(20),
        FREQUENT(10),
        CONSTANT(0);

        public static final Codec<MusicFrequency> CODEC;
        private static final String KEY_PREPEND = "options.music_frequency.";
        private final int id;
        private final int maxFrequency;
        private final String key;

        private MusicFrequency(int n2) {
            this.id = n2;
            this.maxFrequency = n2 * 1200;
            this.key = KEY_PREPEND + this.name().toLowerCase();
        }

        int getNextSongDelay(@Nullable Music music, RandomSource randomSource) {
            if (music == null) {
                return this.maxFrequency;
            }
            if (this == CONSTANT) {
                return 100;
            }
            int n = Math.min(music.minDelay(), this.maxFrequency);
            int n2 = Math.min(music.maxDelay(), this.maxFrequency);
            return Mth.nextInt(randomSource, n, n2);
        }

        @Override
        public int getId() {
            return this.id;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public String getSerializedName() {
            return this.name();
        }

        static {
            CODEC = StringRepresentable.fromEnum(MusicFrequency::values);
        }
    }
}

