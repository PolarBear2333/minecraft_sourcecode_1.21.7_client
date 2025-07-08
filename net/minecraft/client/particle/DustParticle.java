/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3f
 */
package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DustParticleBase;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import org.joml.Vector3f;

public class DustParticle
extends DustParticleBase<DustParticleOptions> {
    protected DustParticle(ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6, DustParticleOptions dustParticleOptions, SpriteSet spriteSet) {
        super(clientLevel, d, d2, d3, d4, d5, d6, dustParticleOptions, spriteSet);
        float f = this.random.nextFloat() * 0.4f + 0.6f;
        Vector3f vector3f = dustParticleOptions.getColor();
        this.rCol = this.randomizeColor(vector3f.x(), f);
        this.gCol = this.randomizeColor(vector3f.y(), f);
        this.bCol = this.randomizeColor(vector3f.z(), f);
    }

    public static class Provider
    implements ParticleProvider<DustParticleOptions> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(DustParticleOptions dustParticleOptions, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return new DustParticle(clientLevel, d, d2, d3, d4, d5, d6, dustParticleOptions, this.sprites);
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return this.createParticle((DustParticleOptions)particleOptions, clientLevel, d, d2, d3, d4, d5, d6);
        }
    }
}

