/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.levelgen.structure.templatesystem;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosAlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.PosRuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.Passthrough;
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.RuleBlockEntityModifier;

public class ProcessorRule {
    public static final Passthrough DEFAULT_BLOCK_ENTITY_MODIFIER = Passthrough.INSTANCE;
    public static final Codec<ProcessorRule> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)RuleTest.CODEC.fieldOf("input_predicate").forGetter(processorRule -> processorRule.inputPredicate), (App)RuleTest.CODEC.fieldOf("location_predicate").forGetter(processorRule -> processorRule.locPredicate), (App)PosRuleTest.CODEC.lenientOptionalFieldOf("position_predicate", (Object)PosAlwaysTrueTest.INSTANCE).forGetter(processorRule -> processorRule.posPredicate), (App)BlockState.CODEC.fieldOf("output_state").forGetter(processorRule -> processorRule.outputState), (App)RuleBlockEntityModifier.CODEC.lenientOptionalFieldOf("block_entity_modifier", (Object)DEFAULT_BLOCK_ENTITY_MODIFIER).forGetter(processorRule -> processorRule.blockEntityModifier)).apply((Applicative)instance, ProcessorRule::new));
    private final RuleTest inputPredicate;
    private final RuleTest locPredicate;
    private final PosRuleTest posPredicate;
    private final BlockState outputState;
    private final RuleBlockEntityModifier blockEntityModifier;

    public ProcessorRule(RuleTest ruleTest, RuleTest ruleTest2, BlockState blockState) {
        this(ruleTest, ruleTest2, PosAlwaysTrueTest.INSTANCE, blockState);
    }

    public ProcessorRule(RuleTest ruleTest, RuleTest ruleTest2, PosRuleTest posRuleTest, BlockState blockState) {
        this(ruleTest, ruleTest2, posRuleTest, blockState, DEFAULT_BLOCK_ENTITY_MODIFIER);
    }

    public ProcessorRule(RuleTest ruleTest, RuleTest ruleTest2, PosRuleTest posRuleTest, BlockState blockState, RuleBlockEntityModifier ruleBlockEntityModifier) {
        this.inputPredicate = ruleTest;
        this.locPredicate = ruleTest2;
        this.posPredicate = posRuleTest;
        this.outputState = blockState;
        this.blockEntityModifier = ruleBlockEntityModifier;
    }

    public boolean test(BlockState blockState, BlockState blockState2, BlockPos blockPos, BlockPos blockPos2, BlockPos blockPos3, RandomSource randomSource) {
        return this.inputPredicate.test(blockState, randomSource) && this.locPredicate.test(blockState2, randomSource) && this.posPredicate.test(blockPos, blockPos2, blockPos3, randomSource);
    }

    public BlockState getOutputState() {
        return this.outputState;
    }

    @Nullable
    public CompoundTag getOutputTag(RandomSource randomSource, @Nullable CompoundTag compoundTag) {
        return this.blockEntityModifier.apply(randomSource, compoundTag);
    }
}

