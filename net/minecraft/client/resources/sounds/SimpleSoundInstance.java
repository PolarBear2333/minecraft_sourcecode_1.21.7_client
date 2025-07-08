/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.resources.sounds;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public class SimpleSoundInstance
extends AbstractSoundInstance {
    public SimpleSoundInstance(SoundEvent soundEvent, SoundSource soundSource, float f, float f2, RandomSource randomSource, BlockPos blockPos) {
        this(soundEvent, soundSource, f, f2, randomSource, (double)blockPos.getX() + 0.5, (double)blockPos.getY() + 0.5, (double)blockPos.getZ() + 0.5);
    }

    public static SimpleSoundInstance forUI(SoundEvent soundEvent, float f) {
        return SimpleSoundInstance.forUI(soundEvent, f, 0.25f);
    }

    public static SimpleSoundInstance forUI(Holder<SoundEvent> holder, float f) {
        return SimpleSoundInstance.forUI(holder.value(), f);
    }

    public static SimpleSoundInstance forUI(SoundEvent soundEvent, float f, float f2) {
        return new SimpleSoundInstance(soundEvent.location(), SoundSource.UI, f2, f, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE, 0.0, 0.0, 0.0, true);
    }

    public static SimpleSoundInstance forMusic(SoundEvent soundEvent, float f) {
        return new SimpleSoundInstance(soundEvent.location(), SoundSource.MUSIC, f, 1.0f, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE, 0.0, 0.0, 0.0, true);
    }

    public static SimpleSoundInstance forJukeboxSong(SoundEvent soundEvent, Vec3 vec3) {
        return new SimpleSoundInstance(soundEvent, SoundSource.RECORDS, 4.0f, 1.0f, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.LINEAR, vec3.x, vec3.y, vec3.z);
    }

    public static SimpleSoundInstance forLocalAmbience(SoundEvent soundEvent, float f, float f2) {
        return new SimpleSoundInstance(soundEvent.location(), SoundSource.AMBIENT, f2, f, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE, 0.0, 0.0, 0.0, true);
    }

    public static SimpleSoundInstance forAmbientAddition(SoundEvent soundEvent) {
        return SimpleSoundInstance.forLocalAmbience(soundEvent, 1.0f, 1.0f);
    }

    public static SimpleSoundInstance forAmbientMood(SoundEvent soundEvent, RandomSource randomSource, double d, double d2, double d3) {
        return new SimpleSoundInstance(soundEvent, SoundSource.AMBIENT, 1.0f, 1.0f, randomSource, false, 0, SoundInstance.Attenuation.LINEAR, d, d2, d3);
    }

    public SimpleSoundInstance(SoundEvent soundEvent, SoundSource soundSource, float f, float f2, RandomSource randomSource, double d, double d2, double d3) {
        this(soundEvent, soundSource, f, f2, randomSource, false, 0, SoundInstance.Attenuation.LINEAR, d, d2, d3);
    }

    private SimpleSoundInstance(SoundEvent soundEvent, SoundSource soundSource, float f, float f2, RandomSource randomSource, boolean bl, int n, SoundInstance.Attenuation attenuation, double d, double d2, double d3) {
        this(soundEvent.location(), soundSource, f, f2, randomSource, bl, n, attenuation, d, d2, d3, false);
    }

    public SimpleSoundInstance(ResourceLocation resourceLocation, SoundSource soundSource, float f, float f2, RandomSource randomSource, boolean bl, int n, SoundInstance.Attenuation attenuation, double d, double d2, double d3, boolean bl2) {
        super(resourceLocation, soundSource, randomSource);
        this.volume = f;
        this.pitch = f2;
        this.x = d;
        this.y = d2;
        this.z = d3;
        this.looping = bl;
        this.delay = n;
        this.attenuation = attenuation;
        this.relative = bl2;
    }
}

