/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.resources.sounds;

import javax.annotation.Nullable;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.SampledFloat;

public class Sound
implements Weighted<Sound> {
    public static final FileToIdConverter SOUND_LISTER = new FileToIdConverter("sounds", ".ogg");
    private final ResourceLocation location;
    private final SampledFloat volume;
    private final SampledFloat pitch;
    private final int weight;
    private final Type type;
    private final boolean stream;
    private final boolean preload;
    private final int attenuationDistance;

    public Sound(ResourceLocation resourceLocation, SampledFloat sampledFloat, SampledFloat sampledFloat2, int n, Type type, boolean bl, boolean bl2, int n2) {
        this.location = resourceLocation;
        this.volume = sampledFloat;
        this.pitch = sampledFloat2;
        this.weight = n;
        this.type = type;
        this.stream = bl;
        this.preload = bl2;
        this.attenuationDistance = n2;
    }

    public ResourceLocation getLocation() {
        return this.location;
    }

    public ResourceLocation getPath() {
        return SOUND_LISTER.idToFile(this.location);
    }

    public SampledFloat getVolume() {
        return this.volume;
    }

    public SampledFloat getPitch() {
        return this.pitch;
    }

    @Override
    public int getWeight() {
        return this.weight;
    }

    @Override
    public Sound getSound(RandomSource randomSource) {
        return this;
    }

    @Override
    public void preloadIfRequired(SoundEngine soundEngine) {
        if (this.preload) {
            soundEngine.requestPreload(this);
        }
    }

    public Type getType() {
        return this.type;
    }

    public boolean shouldStream() {
        return this.stream;
    }

    public boolean shouldPreload() {
        return this.preload;
    }

    public int getAttenuationDistance() {
        return this.attenuationDistance;
    }

    public String toString() {
        return "Sound[" + String.valueOf(this.location) + "]";
    }

    @Override
    public /* synthetic */ Object getSound(RandomSource randomSource) {
        return this.getSound(randomSource);
    }

    public static enum Type {
        FILE("file"),
        SOUND_EVENT("event");

        private final String name;

        private Type(String string2) {
            this.name = string2;
        }

        @Nullable
        public static Type getByName(String string) {
            for (Type type : Type.values()) {
                if (!type.name.equals(string)) continue;
                return type;
            }
            return null;
        }
    }
}

