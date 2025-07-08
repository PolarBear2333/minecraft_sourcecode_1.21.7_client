/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.world.scores;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.NumberFormatTypes;
import net.minecraft.world.scores.ReadOnlyScoreInfo;

public class Score
implements ReadOnlyScoreInfo {
    public static final MapCodec<Score> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.INT.optionalFieldOf("Score", (Object)0).forGetter(Score::value), (App)Codec.BOOL.optionalFieldOf("Locked", (Object)false).forGetter(Score::isLocked), (App)ComponentSerialization.CODEC.optionalFieldOf("display").forGetter(score -> Optional.ofNullable(score.display)), (App)NumberFormatTypes.CODEC.optionalFieldOf("format").forGetter(score -> Optional.ofNullable(score.numberFormat))).apply((Applicative)instance, Score::new));
    private int value;
    private boolean locked = true;
    @Nullable
    private Component display;
    @Nullable
    private NumberFormat numberFormat;

    public Score() {
    }

    private Score(int n, boolean bl, Optional<Component> optional, Optional<NumberFormat> optional2) {
        this.value = n;
        this.locked = bl;
        this.display = optional.orElse(null);
        this.numberFormat = optional2.orElse(null);
    }

    @Override
    public int value() {
        return this.value;
    }

    public void value(int n) {
        this.value = n;
    }

    @Override
    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean bl) {
        this.locked = bl;
    }

    @Nullable
    public Component display() {
        return this.display;
    }

    public void display(@Nullable Component component) {
        this.display = component;
    }

    @Override
    @Nullable
    public NumberFormat numberFormat() {
        return this.numberFormat;
    }

    public void numberFormat(@Nullable NumberFormat numberFormat) {
        this.numberFormat = numberFormat;
    }
}

