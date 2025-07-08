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
import net.minecraft.util.RandomSource;

public class WhiteAshParticle
extends BaseAshSmokeParticle {
    private static final int COLOR_RGB24 = 12235202;

    protected WhiteAshParticle(ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6, float f, SpriteSet spriteSet) {
        super(clientLevel, d, d2, d3, 0.1f, -0.1f, 0.1f, d4, d5, d6, f, spriteSet, 0.0f, 20, 0.0125f, false);
        this.rCol = (float)ARGB.red(12235202) / 255.0f;
        this.gCol = (float)ARGB.green(12235202) / 255.0f;
        this.bCol = (float)ARGB.blue(12235202) / 255.0f;
    }

    public static class Provider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            RandomSource randomSource = clientLevel.random;
            double d7 = (double)randomSource.nextFloat() * -1.9 * (double)randomSource.nextFloat() * 0.1;
            double d8 = (double)randomSource.nextFloat() * -0.5 * (double)randomSource.nextFloat() * 0.1 * 5.0;
            double d9 = (double)randomSource.nextFloat() * -1.9 * (double)randomSource.nextFloat() * 0.1;
            return new WhiteAshParticle(clientLevel, d, d2, d3, d7, d8, d9, 1.0f, this.sprites);
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return this.createParticle((SimpleParticleType)particleOptions, clientLevel, d, d2, d3, d4, d5, d6);
        }
    }
}

