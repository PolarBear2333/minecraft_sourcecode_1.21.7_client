/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;

public class GustSeedParticle
extends NoRenderParticle {
    private final double scale;
    private final int tickDelayInBetween;

    GustSeedParticle(ClientLevel clientLevel, double d, double d2, double d3, double d4, int n, int n2) {
        super(clientLevel, d, d2, d3, 0.0, 0.0, 0.0);
        this.scale = d4;
        this.lifetime = n;
        this.tickDelayInBetween = n2;
    }

    @Override
    public void tick() {
        if (this.age % (this.tickDelayInBetween + 1) == 0) {
            for (int i = 0; i < 3; ++i) {
                double d = this.x + (this.random.nextDouble() - this.random.nextDouble()) * this.scale;
                double d2 = this.y + (this.random.nextDouble() - this.random.nextDouble()) * this.scale;
                double d3 = this.z + (this.random.nextDouble() - this.random.nextDouble()) * this.scale;
                this.level.addParticle(ParticleTypes.GUST, d, d2, d3, (float)this.age / (float)this.lifetime, 0.0, 0.0);
            }
        }
        if (this.age++ == this.lifetime) {
            this.remove();
        }
    }

    public static class Provider
    implements ParticleProvider<SimpleParticleType> {
        private final double scale;
        private final int lifetime;
        private final int tickDelayInBetween;

        public Provider(double d, int n, int n2) {
            this.scale = d;
            this.lifetime = n;
            this.tickDelayInBetween = n2;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return new GustSeedParticle(clientLevel, d, d2, d3, this.scale, this.lifetime, this.tickDelayInBetween);
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return this.createParticle((SimpleParticleType)particleOptions, clientLevel, d, d2, d3, d4, d5, d6);
        }
    }
}

