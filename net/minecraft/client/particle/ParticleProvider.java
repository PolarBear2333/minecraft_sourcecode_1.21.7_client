/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.particle;

import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.ParticleOptions;

public interface ParticleProvider<T extends ParticleOptions> {
    @Nullable
    public Particle createParticle(T var1, ClientLevel var2, double var3, double var5, double var7, double var9, double var11, double var13);

    public static interface Sprite<T extends ParticleOptions> {
        @Nullable
        public TextureSheetParticle createParticle(T var1, ClientLevel var2, double var3, double var5, double var7, double var9, double var11, double var13);
    }
}

