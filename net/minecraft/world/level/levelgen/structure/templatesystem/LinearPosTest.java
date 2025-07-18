/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosRuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosRuleTestType;

public class LinearPosTest
extends PosRuleTest {
    public static final MapCodec<LinearPosTest> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.FLOAT.fieldOf("min_chance").orElse((Object)Float.valueOf(0.0f)).forGetter(linearPosTest -> Float.valueOf(linearPosTest.minChance)), (App)Codec.FLOAT.fieldOf("max_chance").orElse((Object)Float.valueOf(0.0f)).forGetter(linearPosTest -> Float.valueOf(linearPosTest.maxChance)), (App)Codec.INT.fieldOf("min_dist").orElse((Object)0).forGetter(linearPosTest -> linearPosTest.minDist), (App)Codec.INT.fieldOf("max_dist").orElse((Object)0).forGetter(linearPosTest -> linearPosTest.maxDist)).apply((Applicative)instance, LinearPosTest::new));
    private final float minChance;
    private final float maxChance;
    private final int minDist;
    private final int maxDist;

    public LinearPosTest(float f, float f2, int n, int n2) {
        if (n >= n2) {
            throw new IllegalArgumentException("Invalid range: [" + n + "," + n2 + "]");
        }
        this.minChance = f;
        this.maxChance = f2;
        this.minDist = n;
        this.maxDist = n2;
    }

    @Override
    public boolean test(BlockPos blockPos, BlockPos blockPos2, BlockPos blockPos3, RandomSource randomSource) {
        int n = blockPos2.distManhattan(blockPos3);
        float f = randomSource.nextFloat();
        return f <= Mth.clampedLerp(this.minChance, this.maxChance, Mth.inverseLerp(n, this.minDist, this.maxDist));
    }

    @Override
    protected PosRuleTestType<?> getType() {
        return PosRuleTestType.LINEAR_POS_TEST;
    }
}

