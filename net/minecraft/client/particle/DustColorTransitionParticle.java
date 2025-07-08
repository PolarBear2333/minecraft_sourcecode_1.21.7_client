/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DustParticleBase;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.ParticleOptions;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class DustColorTransitionParticle
extends DustParticleBase<DustColorTransitionOptions> {
    private final Vector3f fromColor;
    private final Vector3f toColor;

    protected DustColorTransitionParticle(ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6, DustColorTransitionOptions dustColorTransitionOptions, SpriteSet spriteSet) {
        super(clientLevel, d, d2, d3, d4, d5, d6, dustColorTransitionOptions, spriteSet);
        float f = this.random.nextFloat() * 0.4f + 0.6f;
        this.fromColor = this.randomizeColor(dustColorTransitionOptions.getFromColor(), f);
        this.toColor = this.randomizeColor(dustColorTransitionOptions.getToColor(), f);
    }

    private Vector3f randomizeColor(Vector3f vector3f, float f) {
        return new Vector3f(this.randomizeColor(vector3f.x(), f), this.randomizeColor(vector3f.y(), f), this.randomizeColor(vector3f.z(), f));
    }

    private void lerpColors(float f) {
        float f2 = ((float)this.age + f) / ((float)this.lifetime + 1.0f);
        Vector3f vector3f = new Vector3f((Vector3fc)this.fromColor).lerp((Vector3fc)this.toColor, f2);
        this.rCol = vector3f.x();
        this.gCol = vector3f.y();
        this.bCol = vector3f.z();
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
        this.lerpColors(f);
        super.render(vertexConsumer, camera, f);
    }

    public static class Provider
    implements ParticleProvider<DustColorTransitionOptions> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(DustColorTransitionOptions dustColorTransitionOptions, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return new DustColorTransitionParticle(clientLevel, d, d2, d3, d4, d5, d6, dustColorTransitionOptions, this.sprites);
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return this.createParticle((DustColorTransitionOptions)particleOptions, clientLevel, d, d2, d3, d4, d5, d6);
        }
    }
}

