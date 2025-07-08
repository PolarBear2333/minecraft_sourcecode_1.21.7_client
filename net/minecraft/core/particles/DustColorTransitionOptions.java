/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.joml.Vector3f
 */
package net.minecraft.core.particles;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.ScalableParticleOptionsBase;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import org.joml.Vector3f;

public class DustColorTransitionOptions
extends ScalableParticleOptionsBase {
    public static final int SCULK_PARTICLE_COLOR = 3790560;
    public static final DustColorTransitionOptions SCULK_TO_REDSTONE = new DustColorTransitionOptions(3790560, 0xFF0000, 1.0f);
    public static final MapCodec<DustColorTransitionOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)ExtraCodecs.RGB_COLOR_CODEC.fieldOf("from_color").forGetter(dustColorTransitionOptions -> dustColorTransitionOptions.fromColor), (App)ExtraCodecs.RGB_COLOR_CODEC.fieldOf("to_color").forGetter(dustColorTransitionOptions -> dustColorTransitionOptions.toColor), (App)SCALE.fieldOf("scale").forGetter(ScalableParticleOptionsBase::getScale)).apply((Applicative)instance, DustColorTransitionOptions::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, DustColorTransitionOptions> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.INT, dustColorTransitionOptions -> dustColorTransitionOptions.fromColor, ByteBufCodecs.INT, dustColorTransitionOptions -> dustColorTransitionOptions.toColor, ByteBufCodecs.FLOAT, ScalableParticleOptionsBase::getScale, DustColorTransitionOptions::new);
    private final int fromColor;
    private final int toColor;

    public DustColorTransitionOptions(int n, int n2, float f) {
        super(f);
        this.fromColor = n;
        this.toColor = n2;
    }

    public Vector3f getFromColor() {
        return ARGB.vector3fFromRGB24(this.fromColor);
    }

    public Vector3f getToColor() {
        return ARGB.vector3fFromRGB24(this.toColor);
    }

    public ParticleType<DustColorTransitionOptions> getType() {
        return ParticleTypes.DUST_COLOR_TRANSITION;
    }
}

