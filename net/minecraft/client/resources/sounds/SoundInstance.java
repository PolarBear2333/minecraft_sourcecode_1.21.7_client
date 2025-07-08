/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.resources.sounds;

import javax.annotation.Nullable;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public interface SoundInstance {
    public ResourceLocation getLocation();

    @Nullable
    public WeighedSoundEvents resolve(SoundManager var1);

    @Nullable
    public Sound getSound();

    public SoundSource getSource();

    public boolean isLooping();

    public boolean isRelative();

    public int getDelay();

    public float getVolume();

    public float getPitch();

    public double getX();

    public double getY();

    public double getZ();

    public Attenuation getAttenuation();

    default public boolean canStartSilent() {
        return false;
    }

    default public boolean canPlaySound() {
        return true;
    }

    public static RandomSource createUnseededRandom() {
        return RandomSource.create();
    }

    public static enum Attenuation {
        NONE,
        LINEAR;

    }
}

