/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.client.resources.metadata.animation;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.util.ExtraCodecs;

public record AnimationFrame(int index, Optional<Integer> time) {
    public static final Codec<AnimationFrame> FULL_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("index").forGetter(AnimationFrame::index), (App)ExtraCodecs.POSITIVE_INT.optionalFieldOf("time").forGetter(AnimationFrame::time)).apply((Applicative)instance, AnimationFrame::new));
    public static final Codec<AnimationFrame> CODEC = Codec.either(ExtraCodecs.NON_NEGATIVE_INT, FULL_CODEC).xmap(either -> (AnimationFrame)either.map(AnimationFrame::new, animationFrame -> animationFrame), animationFrame -> animationFrame.time.isPresent() ? Either.right((Object)animationFrame) : Either.left((Object)animationFrame.index));

    public AnimationFrame(int n) {
        this(n, Optional.empty());
    }

    public int timeOr(int n) {
        return this.time.orElse(n);
    }
}

