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
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class CritParticle
extends TextureSheetParticle {
    CritParticle(ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
        super(clientLevel, d, d2, d3, 0.0, 0.0, 0.0);
        float f;
        this.friction = 0.7f;
        this.gravity = 0.5f;
        this.xd *= (double)0.1f;
        this.yd *= (double)0.1f;
        this.zd *= (double)0.1f;
        this.xd += d4 * 0.4;
        this.yd += d5 * 0.4;
        this.zd += d6 * 0.4;
        this.rCol = f = (float)(Math.random() * (double)0.3f + (double)0.6f);
        this.gCol = f;
        this.bCol = f;
        this.quadSize *= 0.75f;
        this.lifetime = Math.max((int)(6.0 / (Math.random() * 0.8 + 0.6)), 1);
        this.hasPhysics = false;
        this.tick();
    }

    @Override
    public float getQuadSize(float f) {
        return this.quadSize * Mth.clamp(((float)this.age + f) / (float)this.lifetime * 32.0f, 0.0f, 1.0f);
    }

    @Override
    public void tick() {
        super.tick();
        this.gCol *= 0.96f;
        this.bCol *= 0.9f;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static class DamageIndicatorProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public DamageIndicatorProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            CritParticle critParticle = new CritParticle(clientLevel, d, d2, d3, d4, d5 + 1.0, d6);
            critParticle.setLifetime(20);
            critParticle.pickSprite(this.sprite);
            return critParticle;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return this.createParticle((SimpleParticleType)particleOptions, clientLevel, d, d2, d3, d4, d5, d6);
        }
    }

    public static class MagicProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public MagicProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            CritParticle critParticle = new CritParticle(clientLevel, d, d2, d3, d4, d5, d6);
            critParticle.rCol *= 0.3f;
            critParticle.gCol *= 0.8f;
            critParticle.pickSprite(this.sprite);
            return critParticle;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return this.createParticle((SimpleParticleType)particleOptions, clientLevel, d, d2, d3, d4, d5, d6);
        }
    }

    public static class Provider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public Provider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            CritParticle critParticle = new CritParticle(clientLevel, d, d2, d3, d4, d5, d6);
            critParticle.pickSprite(this.sprite);
            return critParticle;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return this.createParticle((SimpleParticleType)particleOptions, clientLevel, d, d2, d3, d4, d5, d6);
        }
    }
}

