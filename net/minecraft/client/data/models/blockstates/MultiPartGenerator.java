/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.data.models.blockstates;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.blockstates.ConditionBuilder;
import net.minecraft.client.renderer.block.model.BlockModelDefinition;
import net.minecraft.client.renderer.block.model.multipart.Condition;
import net.minecraft.client.renderer.block.model.multipart.Selector;
import net.minecraft.world.level.block.Block;

public class MultiPartGenerator
implements BlockModelDefinitionGenerator {
    private final Block block;
    private final List<Entry> parts = new ArrayList<Entry>();

    private MultiPartGenerator(Block block) {
        this.block = block;
    }

    @Override
    public Block block() {
        return this.block;
    }

    public static MultiPartGenerator multiPart(Block block) {
        return new MultiPartGenerator(block);
    }

    public MultiPartGenerator with(MultiVariant multiVariant) {
        this.parts.add(new Entry(Optional.empty(), multiVariant));
        return this;
    }

    private void validateCondition(Condition condition) {
        condition.instantiate(this.block.getStateDefinition());
    }

    public MultiPartGenerator with(Condition condition, MultiVariant multiVariant) {
        this.validateCondition(condition);
        this.parts.add(new Entry(Optional.of(condition), multiVariant));
        return this;
    }

    public MultiPartGenerator with(ConditionBuilder conditionBuilder, MultiVariant multiVariant) {
        return this.with(conditionBuilder.build(), multiVariant);
    }

    @Override
    public BlockModelDefinition create() {
        return new BlockModelDefinition(Optional.empty(), Optional.of(new BlockModelDefinition.MultiPartDefinition(this.parts.stream().map(Entry::toUnbaked).toList())));
    }

    record Entry(Optional<Condition> condition, MultiVariant variants) {
        public Selector toUnbaked() {
            return new Selector(this.condition, this.variants.toUnbaked());
        }
    }
}

