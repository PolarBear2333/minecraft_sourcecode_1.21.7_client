/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.resources.sounds;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.HappyGhast;
import net.minecraft.world.entity.player.Player;

public class RidingHappyGhastSoundInstance
extends AbstractTickableSoundInstance {
    private static final float VOLUME_MIN = 0.0f;
    private static final float VOLUME_MAX = 1.0f;
    private final Player player;
    private final HappyGhast happyGhast;

    public RidingHappyGhastSoundInstance(Player player, HappyGhast happyGhast) {
        super(SoundEvents.HAPPY_GHAST_RIDING, happyGhast.getSoundSource(), SoundInstance.createUnseededRandom());
        this.player = player;
        this.happyGhast = happyGhast;
        this.attenuation = SoundInstance.Attenuation.NONE;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0f;
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        if (this.happyGhast.isRemoved() || !this.player.isPassenger() || this.player.getVehicle() != this.happyGhast) {
            this.stop();
            return;
        }
        float f = (float)this.happyGhast.getDeltaMovement().length();
        this.volume = f >= 0.01f ? 5.0f * Mth.clampedLerp(0.0f, 1.0f, f) : 0.0f;
    }
}

