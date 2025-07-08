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
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public class FlyStraightTowardsParticle
extends TextureSheetParticle {
    private final double xStart;
    private final double yStart;
    private final double zStart;
    private final int startColor;
    private final int endColor;

    FlyStraightTowardsParticle(ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6, int n, int n2) {
        super(clientLevel, d, d2, d3);
        this.xd = d4;
        this.yd = d5;
        this.zd = d6;
        this.xStart = d;
        this.yStart = d2;
        this.zStart = d3;
        this.xo = d + d4;
        this.yo = d2 + d5;
        this.zo = d3 + d6;
        this.x = this.xo;
        this.y = this.yo;
        this.z = this.zo;
        this.quadSize = 0.1f * (this.random.nextFloat() * 0.5f + 0.2f);
        this.hasPhysics = false;
        this.lifetime = (int)(Math.random() * 5.0) + 25;
        this.startColor = n;
        this.endColor = n2;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void move(double d, double d2, double d3) {
    }

    @Override
    public int getLightColor(float f) {
        return 240;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        float f = (float)this.age / (float)this.lifetime;
        float f2 = 1.0f - f;
        this.x = this.xStart + this.xd * (double)f2;
        this.y = this.yStart + this.yd * (double)f2;
        this.z = this.zStart + this.zd * (double)f2;
        int n = ARGB.lerp(f, this.startColor, this.endColor);
        this.setColor((float)ARGB.red(n) / 255.0f, (float)ARGB.green(n) / 255.0f, (float)ARGB.blue(n) / 255.0f);
        this.setAlpha((float)ARGB.alpha(n) / 255.0f);
    }

    public static class OminousSpawnProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public OminousSpawnProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            FlyStraightTowardsParticle flyStraightTowardsParticle = new FlyStraightTowardsParticle(clientLevel, d, d2, d3, d4, d5, d6, -12210434, -1);
            flyStraightTowardsParticle.scale(Mth.randomBetween(clientLevel.getRandom(), 3.0f, 5.0f));
            flyStraightTowardsParticle.pickSprite(this.sprite);
            return flyStraightTowardsParticle;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return this.createParticle((SimpleParticleType)particleOptions, clientLevel, d, d2, d3, d4, d5, d6);
        }
    }
}

