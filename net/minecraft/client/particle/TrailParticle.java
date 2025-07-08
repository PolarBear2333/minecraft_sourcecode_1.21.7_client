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
import net.minecraft.core.particles.TrailParticleOption;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class TrailParticle
extends TextureSheetParticle {
    private final Vec3 target;

    TrailParticle(ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6, Vec3 vec3, int n) {
        super(clientLevel, d, d2, d3, d4, d5, d6);
        n = ARGB.scaleRGB(n, 0.875f + this.random.nextFloat() * 0.25f, 0.875f + this.random.nextFloat() * 0.25f, 0.875f + this.random.nextFloat() * 0.25f);
        this.rCol = (float)ARGB.red(n) / 255.0f;
        this.gCol = (float)ARGB.green(n) / 255.0f;
        this.bCol = (float)ARGB.blue(n) / 255.0f;
        this.quadSize = 0.26f;
        this.target = vec3;
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
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        int n = this.lifetime - this.age;
        double d = 1.0 / (double)n;
        this.x = Mth.lerp(d, this.x, this.target.x());
        this.y = Mth.lerp(d, this.y, this.target.y());
        this.z = Mth.lerp(d, this.z, this.target.z());
    }

    @Override
    public int getLightColor(float f) {
        return 0xF000F0;
    }

    public static class Provider
    implements ParticleProvider<TrailParticleOption> {
        private final SpriteSet sprite;

        public Provider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(TrailParticleOption trailParticleOption, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            TrailParticle trailParticle = new TrailParticle(clientLevel, d, d2, d3, d4, d5, d6, trailParticleOption.target(), trailParticleOption.color());
            trailParticle.pickSprite(this.sprite);
            trailParticle.setLifetime(trailParticleOption.duration());
            return trailParticle;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return this.createParticle((TrailParticleOption)particleOptions, clientLevel, d, d2, d3, d4, d5, d6);
        }
    }
}

