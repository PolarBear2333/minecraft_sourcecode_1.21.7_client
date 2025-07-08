/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.resources.sounds;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.NewMinecartBehavior;

public class RidingMinecartSoundInstance
extends AbstractTickableSoundInstance {
    private static final float VOLUME_MIN = 0.0f;
    private static final float VOLUME_MAX = 0.75f;
    private final Player player;
    private final AbstractMinecart minecart;
    private final boolean underwaterSound;

    public RidingMinecartSoundInstance(Player player, AbstractMinecart abstractMinecart, boolean bl) {
        super(bl ? SoundEvents.MINECART_INSIDE_UNDERWATER : SoundEvents.MINECART_INSIDE, SoundSource.NEUTRAL, SoundInstance.createUnseededRandom());
        this.player = player;
        this.minecart = abstractMinecart;
        this.underwaterSound = bl;
        this.attenuation = SoundInstance.Attenuation.NONE;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0f;
    }

    @Override
    public boolean canPlaySound() {
        return !this.minecart.isSilent();
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public void tick() {
        if (this.minecart.isRemoved() || !this.player.isPassenger() || this.player.getVehicle() != this.minecart) {
            this.stop();
            return;
        }
        if (this.underwaterSound != this.player.isUnderWater()) {
            this.volume = 0.0f;
            return;
        }
        float f = (float)this.minecart.getDeltaMovement().horizontalDistance();
        boolean bl = !this.minecart.isOnRails() && this.minecart.getBehavior() instanceof NewMinecartBehavior;
        this.volume = f >= 0.01f && !bl ? Mth.clampedLerp(0.0f, 0.75f, f) : 0.0f;
    }
}

