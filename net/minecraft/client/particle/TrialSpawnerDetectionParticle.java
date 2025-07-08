/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class TrialSpawnerDetectionParticle
extends TextureSheetParticle {
    private final SpriteSet sprites;
    private static final int BASE_LIFETIME = 8;

    protected TrialSpawnerDetectionParticle(ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6, float f, SpriteSet spriteSet) {
        super(clientLevel, d, d2, d3, 0.0, 0.0, 0.0);
        this.sprites = spriteSet;
        this.friction = 0.96f;
        this.gravity = -0.1f;
        this.speedUpWhenYMotionIsBlocked = true;
        this.xd *= 0.0;
        this.yd *= 0.9;
        this.zd *= 0.0;
        this.xd += d4;
        this.yd += d5;
        this.zd += d6;
        this.quadSize *= 0.75f * f;
        this.lifetime = (int)(8.0f / Mth.randomBetween(this.random, 0.5f, 1.0f) * f);
        this.lifetime = Math.max(this.lifetime, 1);
        this.setSpriteFromAge(spriteSet);
        this.hasPhysics = true;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public int getLightColor(float f) {
        return 240;
    }

    @Override
    public SingleQuadParticle.FacingCameraMode getFacingCameraMode() {
        return SingleQuadParticle.FacingCameraMode.LOOKAT_Y;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public float getQuadSize(float f) {
        return this.quadSize * Mth.clamp(((float)this.age + f) / (float)this.lifetime * 32.0f, 0.0f, 1.0f);
    }

    public static class Provider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return new TrialSpawnerDetectionParticle(clientLevel, d, d2, d3, d4, d5, d6, 1.5f, this.sprites);
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return this.createParticle((SimpleParticleType)particleOptions, clientLevel, d, d2, d3, d4, d5, d6);
        }
    }
}

