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
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;

public class FallingLeavesParticle
extends TextureSheetParticle {
    private static final float ACCELERATION_SCALE = 0.0025f;
    private static final int INITIAL_LIFETIME = 300;
    private static final int CURVE_ENDPOINT_TIME = 300;
    private float rotSpeed;
    private final float particleRandom;
    private final float spinAcceleration;
    private final float windBig;
    private boolean swirl;
    private boolean flowAway;
    private double xaFlowScale;
    private double zaFlowScale;
    private double swirlPeriod;

    protected FallingLeavesParticle(ClientLevel clientLevel, double d, double d2, double d3, SpriteSet spriteSet, float f, float f2, boolean bl, boolean bl2, float f3, float f4) {
        super(clientLevel, d, d2, d3);
        float f5;
        this.setSprite(spriteSet.get(this.random.nextInt(12), 12));
        this.rotSpeed = (float)Math.toRadians(this.random.nextBoolean() ? -30.0 : 30.0);
        this.particleRandom = this.random.nextFloat();
        this.spinAcceleration = (float)Math.toRadians(this.random.nextBoolean() ? -5.0 : 5.0);
        this.windBig = f2;
        this.swirl = bl;
        this.flowAway = bl2;
        this.lifetime = 300;
        this.gravity = f * 1.2f * 0.0025f;
        this.quadSize = f5 = f3 * (this.random.nextBoolean() ? 0.05f : 0.075f);
        this.setSize(f5, f5);
        this.friction = 1.0f;
        this.yd = -f4;
        this.xaFlowScale = Math.cos(Math.toRadians(this.particleRandom * 60.0f)) * (double)this.windBig;
        this.zaFlowScale = Math.sin(Math.toRadians(this.particleRandom * 60.0f)) * (double)this.windBig;
        this.swirlPeriod = Math.toRadians(1000.0f + this.particleRandom * 3000.0f);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.lifetime-- <= 0) {
            this.remove();
        }
        if (this.removed) {
            return;
        }
        float f = 300 - this.lifetime;
        float f2 = Math.min(f / 300.0f, 1.0f);
        double d = 0.0;
        double d2 = 0.0;
        if (this.flowAway) {
            d += this.xaFlowScale * Math.pow(f2, 1.25);
            d2 += this.zaFlowScale * Math.pow(f2, 1.25);
        }
        if (this.swirl) {
            d += (double)f2 * Math.cos((double)f2 * this.swirlPeriod) * (double)this.windBig;
            d2 += (double)f2 * Math.sin((double)f2 * this.swirlPeriod) * (double)this.windBig;
        }
        this.xd += d * (double)0.0025f;
        this.zd += d2 * (double)0.0025f;
        this.yd -= (double)this.gravity;
        this.rotSpeed += this.spinAcceleration / 20.0f;
        this.oRoll = this.roll;
        this.roll += this.rotSpeed / 20.0f;
        this.move(this.xd, this.yd, this.zd);
        if (this.onGround || this.lifetime < 299 && (this.xd == 0.0 || this.zd == 0.0)) {
            this.remove();
        }
        if (this.removed) {
            return;
        }
        this.xd *= (double)this.friction;
        this.yd *= (double)this.friction;
        this.zd *= (double)this.friction;
    }

    public static class TintedLeavesProvider
    implements ParticleProvider<ColorParticleOption> {
        private final SpriteSet sprites;

        public TintedLeavesProvider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(ColorParticleOption colorParticleOption, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            FallingLeavesParticle fallingLeavesParticle = new FallingLeavesParticle(clientLevel, d, d2, d3, this.sprites, 0.07f, 10.0f, true, false, 2.0f, 0.021f);
            fallingLeavesParticle.setColor(colorParticleOption.getRed(), colorParticleOption.getGreen(), colorParticleOption.getBlue());
            return fallingLeavesParticle;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return this.createParticle((ColorParticleOption)particleOptions, clientLevel, d, d2, d3, d4, d5, d6);
        }
    }

    public static class PaleOakProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public PaleOakProvider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return new FallingLeavesParticle(clientLevel, d, d2, d3, this.sprites, 0.07f, 10.0f, true, false, 2.0f, 0.021f);
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return this.createParticle((SimpleParticleType)particleOptions, clientLevel, d, d2, d3, d4, d5, d6);
        }
    }

    public static class CherryProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public CherryProvider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return new FallingLeavesParticle(clientLevel, d, d2, d3, this.sprites, 0.25f, 2.0f, false, true, 1.0f, 0.0f);
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return this.createParticle((SimpleParticleType)particleOptions, clientLevel, d, d2, d3, d4, d5, d6);
        }
    }
}

