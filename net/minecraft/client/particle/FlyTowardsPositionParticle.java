/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;

public class FlyTowardsPositionParticle
extends TextureSheetParticle {
    private final double xStart;
    private final double yStart;
    private final double zStart;
    private final boolean isGlowing;
    private final Particle.LifetimeAlpha lifetimeAlpha;

    FlyTowardsPositionParticle(ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
        this(clientLevel, d, d2, d3, d4, d5, d6, false, Particle.LifetimeAlpha.ALWAYS_OPAQUE);
    }

    FlyTowardsPositionParticle(ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6, boolean bl, Particle.LifetimeAlpha lifetimeAlpha) {
        super(clientLevel, d, d2, d3);
        this.isGlowing = bl;
        this.lifetimeAlpha = lifetimeAlpha;
        this.setAlpha(lifetimeAlpha.startAlpha());
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
        float f = this.random.nextFloat() * 0.6f + 0.4f;
        this.rCol = 0.9f * f;
        this.gCol = 0.9f * f;
        this.bCol = f;
        this.hasPhysics = false;
        this.lifetime = (int)(Math.random() * 10.0) + 30;
    }

    @Override
    public ParticleRenderType getRenderType() {
        if (this.lifetimeAlpha.isOpaque()) {
            return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
        }
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void move(double d, double d2, double d3) {
        this.setBoundingBox(this.getBoundingBox().move(d, d2, d3));
        this.setLocationFromBoundingbox();
    }

    @Override
    public int getLightColor(float f) {
        if (this.isGlowing) {
            return 240;
        }
        int n = super.getLightColor(f);
        float f2 = (float)this.age / (float)this.lifetime;
        f2 *= f2;
        f2 *= f2;
        int n2 = n & 0xFF;
        int n3 = n >> 16 & 0xFF;
        if ((n3 += (int)(f2 * 15.0f * 16.0f)) > 240) {
            n3 = 240;
        }
        return n2 | n3 << 16;
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
        f = 1.0f - f;
        float f2 = 1.0f - f;
        f2 *= f2;
        f2 *= f2;
        this.x = this.xStart + this.xd * (double)f;
        this.y = this.yStart + this.yd * (double)f - (double)(f2 * 1.2f);
        this.z = this.zStart + this.zd * (double)f;
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
        this.setAlpha(this.lifetimeAlpha.currentAlphaForAge(this.age, this.lifetime, f));
        super.render(vertexConsumer, camera, f);
    }

    public static class VaultConnectionProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public VaultConnectionProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            FlyTowardsPositionParticle flyTowardsPositionParticle = new FlyTowardsPositionParticle(clientLevel, d, d2, d3, d4, d5, d6, true, new Particle.LifetimeAlpha(0.0f, 0.6f, 0.25f, 1.0f));
            flyTowardsPositionParticle.scale(1.5f);
            flyTowardsPositionParticle.pickSprite(this.sprite);
            return flyTowardsPositionParticle;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return this.createParticle((SimpleParticleType)particleOptions, clientLevel, d, d2, d3, d4, d5, d6);
        }
    }

    public static class NautilusProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public NautilusProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            FlyTowardsPositionParticle flyTowardsPositionParticle = new FlyTowardsPositionParticle(clientLevel, d, d2, d3, d4, d5, d6);
            flyTowardsPositionParticle.pickSprite(this.sprite);
            return flyTowardsPositionParticle;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return this.createParticle((SimpleParticleType)particleOptions, clientLevel, d, d2, d3, d4, d5, d6);
        }
    }

    public static class EnchantProvider
    implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprite;

        public EnchantProvider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(SimpleParticleType simpleParticleType, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            FlyTowardsPositionParticle flyTowardsPositionParticle = new FlyTowardsPositionParticle(clientLevel, d, d2, d3, d4, d5, d6);
            flyTowardsPositionParticle.pickSprite(this.sprite);
            return flyTowardsPositionParticle;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return this.createParticle((SimpleParticleType)particleOptions, clientLevel, d, d2, d3, d4, d5, d6);
        }
    }
}

