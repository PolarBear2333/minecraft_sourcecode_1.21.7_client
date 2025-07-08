/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.client.resources.metadata.animation;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.util.ExtraCodecs;

public record AnimationMetadataSection(Optional<List<AnimationFrame>> frames, Optional<Integer> frameWidth, Optional<Integer> frameHeight, int defaultFrameTime, boolean interpolatedFrames) {
    public static final Codec<AnimationMetadataSection> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)AnimationFrame.CODEC.listOf().optionalFieldOf("frames").forGetter(AnimationMetadataSection::frames), (App)ExtraCodecs.POSITIVE_INT.optionalFieldOf("width").forGetter(AnimationMetadataSection::frameWidth), (App)ExtraCodecs.POSITIVE_INT.optionalFieldOf("height").forGetter(AnimationMetadataSection::frameHeight), (App)ExtraCodecs.POSITIVE_INT.optionalFieldOf("frametime", (Object)1).forGetter(AnimationMetadataSection::defaultFrameTime), (App)Codec.BOOL.optionalFieldOf("interpolate", (Object)false).forGetter(AnimationMetadataSection::interpolatedFrames)).apply((Applicative)instance, AnimationMetadataSection::new));
    public static final MetadataSectionType<AnimationMetadataSection> TYPE = new MetadataSectionType<AnimationMetadataSection>("animation", CODEC);

    public FrameSize calculateFrameSize(int n, int n2) {
        if (this.frameWidth.isPresent()) {
            if (this.frameHeight.isPresent()) {
                return new FrameSize(this.frameWidth.get(), this.frameHeight.get());
            }
            return new FrameSize(this.frameWidth.get(), n2);
        }
        if (this.frameHeight.isPresent()) {
            return new FrameSize(n, this.frameHeight.get());
        }
        int n3 = Math.min(n, n2);
        return new FrameSize(n3, n3);
    }
}

