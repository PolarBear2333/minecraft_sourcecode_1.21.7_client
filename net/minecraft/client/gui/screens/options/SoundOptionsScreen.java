/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.options;

import java.util.Arrays;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;

public class SoundOptionsScreen
extends OptionsSubScreen {
    private static final Component TITLE = Component.translatable("options.sounds.title");

    public SoundOptionsScreen(Screen screen, Options options) {
        super(screen, options, TITLE);
    }

    @Override
    protected void addOptions() {
        this.list.addBig(this.options.getSoundSourceOptionInstance(SoundSource.MASTER));
        this.list.addSmall(this.getAllSoundOptionsExceptMaster());
        this.list.addBig(this.options.soundDevice());
        this.list.addSmall(this.options.showSubtitles(), this.options.directionalAudio());
        this.list.addSmall(this.options.musicFrequency(), this.options.showNowPlayingToast());
    }

    private OptionInstance<?>[] getAllSoundOptionsExceptMaster() {
        return (OptionInstance[])Arrays.stream(SoundSource.values()).filter(soundSource -> soundSource != SoundSource.MASTER).map(this.options::getSoundSourceOptionInstance).toArray(OptionInstance[]::new);
    }
}

