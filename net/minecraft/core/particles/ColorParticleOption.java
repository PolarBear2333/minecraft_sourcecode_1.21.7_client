/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.core.particles;

import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;

public class ColorParticleOption
implements ParticleOptions {
    private final ParticleType<ColorParticleOption> type;
    private final int color;

    public static MapCodec<ColorParticleOption> codec(ParticleType<ColorParticleOption> particleType) {
        return ExtraCodecs.ARGB_COLOR_CODEC.xmap(n -> new ColorParticleOption(particleType, (int)n), colorParticleOption -> colorParticleOption.color).fieldOf("color");
    }

    public static StreamCodec<? super ByteBuf, ColorParticleOption> streamCodec(ParticleType<ColorParticleOption> particleType) {
        return ByteBufCodecs.INT.map(n -> new ColorParticleOption(particleType, (int)n), colorParticleOption -> colorParticleOption.color);
    }

    private ColorParticleOption(ParticleType<ColorParticleOption> particleType, int n) {
        this.type = particleType;
        this.color = n;
    }

    public ParticleType<ColorParticleOption> getType() {
        return this.type;
    }

    public float getRed() {
        return (float)ARGB.red(this.color) / 255.0f;
    }

    public float getGreen() {
        return (float)ARGB.green(this.color) / 255.0f;
    }

    public float getBlue() {
        return (float)ARGB.blue(this.color) / 255.0f;
    }

    public float getAlpha() {
        return (float)ARGB.alpha(this.color) / 255.0f;
    }

    public static ColorParticleOption create(ParticleType<ColorParticleOption> particleType, int n) {
        return new ColorParticleOption(particleType, n);
    }

    public static ColorParticleOption create(ParticleType<ColorParticleOption> particleType, float f, float f2, float f3) {
        return ColorParticleOption.create(particleType, ARGB.colorFromFloat(1.0f, f, f2, f3));
    }
}

