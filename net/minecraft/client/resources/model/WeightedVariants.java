/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.resources.model;

import java.util.List;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;

public class WeightedVariants
implements BlockStateModel {
    private final WeightedList<BlockStateModel> list;
    private final TextureAtlasSprite particleIcon;

    public WeightedVariants(WeightedList<BlockStateModel> weightedList) {
        this.list = weightedList;
        BlockStateModel blockStateModel = weightedList.unwrap().getFirst().value();
        this.particleIcon = blockStateModel.particleIcon();
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return this.particleIcon;
    }

    @Override
    public void collectParts(RandomSource randomSource, List<BlockModelPart> list) {
        this.list.getRandomOrThrow(randomSource).collectParts(randomSource, list);
    }

    public record Unbaked(WeightedList<BlockStateModel.Unbaked> entries) implements BlockStateModel.Unbaked
    {
        @Override
        public BlockStateModel bake(ModelBaker modelBaker) {
            return new WeightedVariants(this.entries.map(unbaked -> unbaked.bake(modelBaker)));
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            this.entries.unwrap().forEach(weighted -> ((BlockStateModel.Unbaked)weighted.value()).resolveDependencies(resolver));
        }
    }
}

