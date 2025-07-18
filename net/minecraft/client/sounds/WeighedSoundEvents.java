/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  javax.annotation.Nullable
 */
package net.minecraft.client.sounds;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.Weighted;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

public class WeighedSoundEvents
implements Weighted<Sound> {
    private final List<Weighted<Sound>> list = Lists.newArrayList();
    @Nullable
    private final Component subtitle;

    public WeighedSoundEvents(ResourceLocation resourceLocation, @Nullable String string) {
        this.subtitle = string == null ? null : Component.translatable(string);
    }

    @Override
    public int getWeight() {
        int n = 0;
        for (Weighted<Sound> weighted : this.list) {
            n += weighted.getWeight();
        }
        return n;
    }

    @Override
    public Sound getSound(RandomSource randomSource) {
        int n = this.getWeight();
        if (this.list.isEmpty() || n == 0) {
            return SoundManager.EMPTY_SOUND;
        }
        int n2 = randomSource.nextInt(n);
        for (Weighted<Sound> weighted : this.list) {
            if ((n2 -= weighted.getWeight()) >= 0) continue;
            return weighted.getSound(randomSource);
        }
        return SoundManager.EMPTY_SOUND;
    }

    public void addSound(Weighted<Sound> weighted) {
        this.list.add(weighted);
    }

    @Nullable
    public Component getSubtitle() {
        return this.subtitle;
    }

    @Override
    public void preloadIfRequired(SoundEngine soundEngine) {
        for (Weighted<Sound> weighted : this.list) {
            weighted.preloadIfRequired(soundEngine);
        }
    }

    @Override
    public /* synthetic */ Object getSound(RandomSource randomSource) {
        return this.getSound(randomSource);
    }
}

