/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionf
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
import net.minecraft.core.particles.ShriekParticleOption;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

public class ShriekParticle
extends TextureSheetParticle {
    private static final float MAGICAL_X_ROT = 1.0472f;
    private int delay;

    ShriekParticle(ClientLevel clientLevel, double d, double d2, double d3, int n) {
        super(clientLevel, d, d2, d3, 0.0, 0.0, 0.0);
        this.quadSize = 0.85f;
        this.delay = n;
        this.lifetime = 30;
        this.gravity = 0.0f;
        this.xd = 0.0;
        this.yd = 0.1;
        this.zd = 0.0;
    }

    @Override
    public float getQuadSize(float f) {
        return this.quadSize * Mth.clamp(((float)this.age + f) / (float)this.lifetime * 0.75f, 0.0f, 1.0f);
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
        if (this.delay > 0) {
            return;
        }
        this.alpha = 1.0f - Mth.clamp(((float)this.age + f) / (float)this.lifetime, 0.0f, 1.0f);
        Quaternionf quaternionf = new Quaternionf();
        quaternionf.rotationX(-1.0472f);
        this.renderRotatedQuad(vertexConsumer, camera, quaternionf, f);
        quaternionf.rotationYXZ((float)(-Math.PI), 1.0472f, 0.0f);
        this.renderRotatedQuad(vertexConsumer, camera, quaternionf, f);
    }

    @Override
    public int getLightColor(float f) {
        return 240;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        if (this.delay > 0) {
            --this.delay;
            return;
        }
        super.tick();
    }

    public static class Provider
    implements ParticleProvider<ShriekParticleOption> {
        private final SpriteSet sprite;

        public Provider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(ShriekParticleOption shriekParticleOption, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            ShriekParticle shriekParticle = new ShriekParticle(clientLevel, d, d2, d3, shriekParticleOption.getDelay());
            shriekParticle.pickSprite(this.sprite);
            shriekParticle.setAlpha(1.0f);
            return shriekParticle;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return this.createParticle((ShriekParticleOption)particleOptions, clientLevel, d, d2, d3, d4, d5, d6);
        }
    }
}

