/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BaseAshSmokeParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.ARGB;

public class DustPlumeParticle
extends BaseAshSmokeParticle {
    private static final int COLOR_RGB24 = 12235202;

    protected DustPlumeParticle(ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6, float f, SpriteSet spriteSet) {
        super(clientLevel, d, d2, d3, 0.7f, 0.6f, 0.7f, d4, d5 + (double)0.15f, d6, f, spriteSet, 0.5f, 7, 0.5f, false);
        float f2 = (float)Math.random() * 0.2f;
        this.rCol = (float)ARGB.red(12235202) / 255.0f - f2;
        this.gCol = (float)ARGB.green(12235202) / 255.0f - f2;
        this.bCol = (float)ARGB.blue(12235202) / 255.0f - f2;
    }

    @Override
    public void tick() {
        this.gravity = 0.88f * this.gravity;
        this.friction = 0.92f * this.friction;
        super.tick();
    }

    public static class Provider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return new DustPlumeParticle(clientLevel, d, d2, d3, d4, d5, d6, 1.0f, this.sprites);
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return this.createParticle((SimpleParticleType)particleOptions, clientLevel, d, d2, d3, d4, d5, d6);
        }
    }
}

