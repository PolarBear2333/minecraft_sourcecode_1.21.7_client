/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionf
 */
package net.minecraft.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Optional;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.util.Mth;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

public class VibrationSignalParticle
extends TextureSheetParticle {
    private final PositionSource target;
    private float rot;
    private float rotO;
    private float pitch;
    private float pitchO;

    VibrationSignalParticle(ClientLevel clientLevel, double d, double d2, double d3, PositionSource positionSource, int n) {
        super(clientLevel, d, d2, d3, 0.0, 0.0, 0.0);
        this.quadSize = 0.3f;
        this.target = positionSource;
        this.lifetime = n;
        Optional<Vec3> optional = positionSource.getPosition(clientLevel);
        if (optional.isPresent()) {
            Vec3 vec3 = optional.get();
            double d4 = d - vec3.x();
            double d5 = d2 - vec3.y();
            double d6 = d3 - vec3.z();
            this.rotO = this.rot = (float)Mth.atan2(d4, d6);
            this.pitchO = this.pitch = (float)Mth.atan2(d5, Math.sqrt(d4 * d4 + d6 * d6));
        }
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float f) {
        float f2 = Mth.sin(((float)this.age + f - (float)Math.PI * 2) * 0.05f) * 2.0f;
        float f3 = Mth.lerp(f, this.rotO, this.rot);
        float f4 = Mth.lerp(f, this.pitchO, this.pitch) + 1.5707964f;
        Quaternionf quaternionf = new Quaternionf();
        quaternionf.rotationY(f3).rotateX(-f4).rotateY(f2);
        this.renderRotatedQuad(vertexConsumer, camera, quaternionf, f);
        quaternionf.rotationY((float)(-Math.PI) + f3).rotateX(f4).rotateY(f2);
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
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        Optional<Vec3> optional = this.target.getPosition(this.level);
        if (optional.isEmpty()) {
            this.remove();
            return;
        }
        int n = this.lifetime - this.age;
        double d = 1.0 / (double)n;
        Vec3 vec3 = optional.get();
        this.x = Mth.lerp(d, this.x, vec3.x());
        this.y = Mth.lerp(d, this.y, vec3.y());
        this.z = Mth.lerp(d, this.z, vec3.z());
        double d2 = this.x - vec3.x();
        double d3 = this.y - vec3.y();
        double d4 = this.z - vec3.z();
        this.rotO = this.rot;
        this.rot = (float)Mth.atan2(d2, d4);
        this.pitchO = this.pitch;
        this.pitch = (float)Mth.atan2(d3, Math.sqrt(d2 * d2 + d4 * d4));
    }

    public static class Provider
    implements ParticleProvider<VibrationParticleOption> {
        private final SpriteSet sprite;

        public Provider(SpriteSet spriteSet) {
            this.sprite = spriteSet;
        }

        @Override
        public Particle createParticle(VibrationParticleOption vibrationParticleOption, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            VibrationSignalParticle vibrationSignalParticle = new VibrationSignalParticle(clientLevel, d, d2, d3, vibrationParticleOption.getDestination(), vibrationParticleOption.getArrivalInTicks());
            vibrationSignalParticle.pickSprite(this.sprite);
            vibrationSignalParticle.setAlpha(1.0f);
            return vibrationSignalParticle;
        }

        @Override
        public /* synthetic */ Particle createParticle(ParticleOptions particleOptions, ClientLevel clientLevel, double d, double d2, double d3, double d4, double d5, double d6) {
            return this.createParticle((VibrationParticleOption)particleOptions, clientLevel, d, d2, d3, d4, d5, d6);
        }
    }
}

