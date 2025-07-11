/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
 *  javax.annotation.Nullable
 */
package net.minecraft.client.resources.sounds;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.AmbientSoundHandler;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.AmbientAdditionsSettings;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;

public class BiomeAmbientSoundsHandler
implements AmbientSoundHandler {
    private static final int LOOP_SOUND_CROSS_FADE_TIME = 40;
    private static final float SKY_MOOD_RECOVERY_RATE = 0.001f;
    private final LocalPlayer player;
    private final SoundManager soundManager;
    private final BiomeManager biomeManager;
    private final RandomSource random;
    private final Object2ObjectArrayMap<Biome, LoopSoundInstance> loopSounds = new Object2ObjectArrayMap();
    private Optional<AmbientMoodSettings> moodSettings = Optional.empty();
    private Optional<AmbientAdditionsSettings> additionsSettings = Optional.empty();
    private float moodiness;
    @Nullable
    private Biome previousBiome;

    public BiomeAmbientSoundsHandler(LocalPlayer localPlayer, SoundManager soundManager, BiomeManager biomeManager) {
        this.random = localPlayer.level().getRandom();
        this.player = localPlayer;
        this.soundManager = soundManager;
        this.biomeManager = biomeManager;
    }

    public float getMoodiness() {
        return this.moodiness;
    }

    @Override
    public void tick() {
        this.loopSounds.values().removeIf(AbstractTickableSoundInstance::isStopped);
        Biome biome = this.biomeManager.getNoiseBiomeAtPosition(this.player.getX(), this.player.getY(), this.player.getZ()).value();
        if (biome != this.previousBiome) {
            this.previousBiome = biome;
            this.moodSettings = biome.getAmbientMood();
            this.additionsSettings = biome.getAmbientAdditions();
            this.loopSounds.values().forEach(LoopSoundInstance::fadeOut);
            biome.getAmbientLoop().ifPresent(holder -> this.loopSounds.compute((Object)biome, (biome, loopSoundInstance) -> {
                if (loopSoundInstance == null) {
                    loopSoundInstance = new LoopSoundInstance((SoundEvent)holder.value());
                    this.soundManager.play((SoundInstance)loopSoundInstance);
                }
                loopSoundInstance.fadeIn();
                return loopSoundInstance;
            }));
        }
        this.additionsSettings.ifPresent(ambientAdditionsSettings -> {
            if (this.random.nextDouble() < ambientAdditionsSettings.getTickChance()) {
                this.soundManager.play(SimpleSoundInstance.forAmbientAddition(ambientAdditionsSettings.getSoundEvent().value()));
            }
        });
        this.moodSettings.ifPresent(ambientMoodSettings -> {
            Level level = this.player.level();
            int n = ambientMoodSettings.getBlockSearchExtent() * 2 + 1;
            BlockPos blockPos = BlockPos.containing(this.player.getX() + (double)this.random.nextInt(n) - (double)ambientMoodSettings.getBlockSearchExtent(), this.player.getEyeY() + (double)this.random.nextInt(n) - (double)ambientMoodSettings.getBlockSearchExtent(), this.player.getZ() + (double)this.random.nextInt(n) - (double)ambientMoodSettings.getBlockSearchExtent());
            int n2 = level.getBrightness(LightLayer.SKY, blockPos);
            this.moodiness = n2 > 0 ? (this.moodiness -= (float)n2 / 15.0f * 0.001f) : (this.moodiness -= (float)(level.getBrightness(LightLayer.BLOCK, blockPos) - 1) / (float)ambientMoodSettings.getTickDelay());
            if (this.moodiness >= 1.0f) {
                double d = (double)blockPos.getX() + 0.5;
                double d2 = (double)blockPos.getY() + 0.5;
                double d3 = (double)blockPos.getZ() + 0.5;
                double d4 = d - this.player.getX();
                double d5 = d2 - this.player.getEyeY();
                double d6 = d3 - this.player.getZ();
                double d7 = Math.sqrt(d4 * d4 + d5 * d5 + d6 * d6);
                double d8 = d7 + ambientMoodSettings.getSoundPositionOffset();
                SimpleSoundInstance simpleSoundInstance = SimpleSoundInstance.forAmbientMood(ambientMoodSettings.getSoundEvent().value(), this.random, this.player.getX() + d4 / d7 * d8, this.player.getEyeY() + d5 / d7 * d8, this.player.getZ() + d6 / d7 * d8);
                this.soundManager.play(simpleSoundInstance);
                this.moodiness = 0.0f;
            } else {
                this.moodiness = Math.max(this.moodiness, 0.0f);
            }
        });
    }

    public static class LoopSoundInstance
    extends AbstractTickableSoundInstance {
        private int fadeDirection;
        private int fade;

        public LoopSoundInstance(SoundEvent soundEvent) {
            super(soundEvent, SoundSource.AMBIENT, SoundInstance.createUnseededRandom());
            this.looping = true;
            this.delay = 0;
            this.volume = 1.0f;
            this.relative = true;
        }

        @Override
        public void tick() {
            if (this.fade < 0) {
                this.stop();
            }
            this.fade += this.fadeDirection;
            this.volume = Mth.clamp((float)this.fade / 40.0f, 0.0f, 1.0f);
        }

        public void fadeOut() {
            this.fade = Math.min(this.fade, 40);
            this.fadeDirection = -1;
        }

        public void fadeIn() {
            this.fade = Math.max(0, this.fade);
            this.fadeDirection = 1;
        }
    }
}

