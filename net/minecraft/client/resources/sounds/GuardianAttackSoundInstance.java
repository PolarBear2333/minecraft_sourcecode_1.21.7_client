/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.resources.sounds;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.monster.Guardian;

public class GuardianAttackSoundInstance
extends AbstractTickableSoundInstance {
    private static final float VOLUME_MIN = 0.0f;
    private static final float VOLUME_SCALE = 1.0f;
    private static final float PITCH_MIN = 0.7f;
    private static final float PITCH_SCALE = 0.5f;
    private final Guardian guardian;

    public GuardianAttackSoundInstance(Guardian guardian) {
        super(SoundEvents.GUARDIAN_ATTACK, SoundSource.HOSTILE, SoundInstance.createUnseededRandom());
        this.guardian = guardian;
        this.attenuation = SoundInstance.Attenuation.NONE;
        this.looping = true;
        this.delay = 0;
    }

    @Override
    public boolean canPlaySound() {
        return !this.guardian.isSilent();
    }

    @Override
    public void tick() {
        if (this.guardian.isRemoved() || this.guardian.getTarget() != null) {
            this.stop();
            return;
        }
        this.x = (float)this.guardian.getX();
        this.y = (float)this.guardian.getY();
        this.z = (float)this.guardian.getZ();
        float f = this.guardian.getAttackAnimationScale(0.0f);
        this.volume = 0.0f + 1.0f * f * f;
        this.pitch = 0.7f + 0.5f * f;
    }
}

