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
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public class FireflyParticle
extends TextureSheetParticle {
    private static final float PARTICLE_FADE_OUT_LIGHT_TIME = 0.3f;
    private static final float PARTICLE_FADE_IN_LIGHT_TIME = 0.1f;
    private static final float PARTICLE_FADE_OUT_ALPHA_TIME = 0.5f;
    private static final float PARTICLE_FADE_IN_ALPHA_TIME = 0.3f;
    private static final int PARTICLE_MIN_LIFETIME = 200;
    private static final int PARTICLE_MAX_LIFETIME = 300;

    FireflyParticle(ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
        super(clientLevel, d, d2, d3, d4, d5, d6);
        this.speedUpWhenYMotionIsBlocked = true;
        this.friction = 0.96f;
        this.quadSize *= 0.75f;
        this.yd *= (double)0.8f;
        this.xd *= (double)0.8f;
        this.zd *= (double)0.8f;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float f) {
        return (int)(255.0f * FireflyParticle.getFadeAmount(this.getLifetimeProgress((float)this.age + f), 0.1f, 0.3f));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level.getBlockState(BlockPos.containing(this.x, this.y, this.z)).isAir()) {
            this.remove();
            return;
        }
        this.setAlpha(FireflyParticle.getFadeAmount(this.getLifetimeProgress(this.age), 0.3f, 0.5f));
        if (Math.random() > 0.95 || this.age == 1) {
            this.setParticleSpeed((double)-0.05f + (double)0.1f * Math.random(), (double)-0.05f + (double)0.1f * Math.random(), (double)-0.05f + (double)0.1f * Math.random());
        }
    }

    private float getLifetimeProgress(float f) {
        return Mth.clamp(f / (float)this.lifetime, 0.0f, 1.0f);
    }

    private static float getFadeAmount(float f, float f2, float f3) {
        if (f >= 1.0f - f2) {
            return (1.0f - f) / f2;
        }
        if (f <= f3) {
            return f / f3;
        }
        return 1.0f;
    }

    public static class FireflyProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public FireflyProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            FireflyParticle fireflyParticle = new FireflyParticle(clientLevel, d, d2, d3, 0.5 - clientLevel.random.nextDouble(), clientLevel.random.nextBoolean() ? d5 : -d5, 0.5 - clientLevel.random.nextDouble());
            fireflyParticle.setLifetime(clientLevel.random.nextIntBetweenInclusive(200, 300));
            fireflyParticle.scale(1.5f);
            fireflyParticle.pickSprite(this.sprite);
            fireflyParticle.setAlpha(0.0f);
            return fireflyParticle;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return this.createParticle((SimpleParticleType)particleOptions, clientLevel, d, d2, d3, d4, d5, d6);
        }
    }
}

