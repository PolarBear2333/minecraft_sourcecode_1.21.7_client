/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SculkChargeParticleOptions;

public class SculkChargeParticle
extends TextureSheetParticle {
    private final SpriteSet sprites;

    SculkChargeParticle(ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6, SpriteSet spriteSet) {
        super(clientLevel, d, d2, d3, d4, d5, d6);
        this.friction = 0.96f;
        this.sprites = spriteSet;
        this.scale(1.5f);
        this.hasPhysics = false;
        this.setSpriteFromAge(spriteSet);
    }

    @Override
    public int getLightColor(float f) {
        return 240;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
    }

    public record Provider(SpriteSet sprite) implements ParticleProvider<SculkChargeParticleOptions>
    {
        @Override
        public Particle createParticle(SculkChargeParticleOptions sculkChargeParticleOptions, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            SculkChargeParticle sculkChargeParticle = new SculkChargeParticle(clientLevel, d, d2, d3, d4, d5, d6, this.sprite);
            sculkChargeParticle.setAlpha(1.0f);
            sculkChargeParticle.setParticleSpeed(d4, d5, d6);
            sculkChargeParticle.oRoll = sculkChargeParticleOptions.roll();
            sculkChargeParticle.roll = sculkChargeParticleOptions.roll();
            sculkChargeParticle.setLifetime(clientLevel.random.nextInt(12) + 8);
            return sculkChargeParticle;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return this.createParticle((SculkChargeParticleOptions)particleOptions, clientLevel, d, d2, d3, d4, d5, d6);
        }
    }
}

