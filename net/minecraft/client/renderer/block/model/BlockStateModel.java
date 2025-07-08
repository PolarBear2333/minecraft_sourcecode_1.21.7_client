/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 */
package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.SingleVariant;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.WeightedVariants;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockStateModel {
    public void collectParts(RandomSource var1, List<BlockModelPart> var2);

    default public List<BlockModelPart> collectParts(RandomSource randomSource) {
        ObjectArrayList objectArrayList = new ObjectArrayList();
        this.collectParts(randomSource, (List<BlockModelPart>)objectArrayList);
        return objectArrayList;
    }

    public TextureAtlasSprite particleIcon();

    public static class SimpleCachedUnbakedRoot
    implements UnbakedRoot {
        final Unbaked contents;
        private final ModelBaker.SharedOperationKey<BlockStateModel> bakingKey = new ModelBaker.SharedOperationKey<BlockStateModel>(){

            @Override
            public BlockStateModel compute(ModelBaker modelBaker) {
                return contents.bake(modelBaker);
            }

            @Override
            public /* synthetic */ Object compute(ModelBaker modelBaker) {
                return this.compute(modelBaker);
            }
        };

        public SimpleCachedUnbakedRoot(Unbaked unbaked) {
            this.contents = unbaked;
        }

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            this.contents.resolveDependencies(resolver);
        }

        @Override
        public BlockStateModel bake(BlockState blockState, ModelBaker modelBaker) {
            return modelBaker.compute(this.bakingKey);
        }

        @Override
        public Object visualEqualityGroup(BlockState blockState) {
            return this;
        }
    }

    public static interface Unbaked
    extends ResolvableModel {
        public static final Codec<Weighted<Variant>> ELEMENT_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Variant.MAP_CODEC.forGetter(Weighted::value), (App)ExtraCodecs.POSITIVE_INT.optionalFieldOf("weight", (Object)1).forGetter(Weighted::weight)).apply((Applicative)instance, Weighted::new));
        public static final Codec<WeightedVariants.Unbaked> HARDCODED_WEIGHTED_CODEC = ExtraCodecs.nonEmptyList(ELEMENT_CODEC.listOf()).flatComapMap(list -> new WeightedVariants.Unbaked(WeightedList.of(Lists.transform((List)list, weighted -> weighted.map(SingleVariant.Unbaked::new)))), unbaked -> {
            List<Weighted<Unbaked>> list = unbaked.entries().unwrap();
            ArrayList<Weighted<Variant>> arrayList = new ArrayList<Weighted<Variant>>(list.size());
            for (Weighted<Unbaked> weighted : list) {
                Unbaked unbaked2 = weighted.value();
                if (unbaked2 instanceof SingleVariant.Unbaked) {
                    SingleVariant.Unbaked unbaked3 = (SingleVariant.Unbaked)unbaked2;
                    arrayList.add(new Weighted<Variant>(unbaked3.variant(), weighted.weight()));
                    continue;
                }
                return DataResult.error(() -> "Only single variants are supported");
            }
            return DataResult.success(arrayList);
        });
        public static final Codec<Unbaked> CODEC = Codec.either(HARDCODED_WEIGHTED_CODEC, SingleVariant.Unbaked.CODEC).flatComapMap(either -> (Unbaked)either.map(unbaked -> unbaked, unbaked -> unbaked), unbaked -> {
            Unbaked unbaked2 = unbaked;
            Objects.requireNonNull(unbaked2);
            Unbaked unbaked3 = unbaked2;
            int n = 0;
            return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{SingleVariant.Unbaked.class, WeightedVariants.Unbaked.class}, (Object)unbaked3, n)) {
                case 0 -> {
                    SingleVariant.Unbaked var3_3 = (SingleVariant.Unbaked)unbaked3;
                    yield DataResult.success((Object)Either.right((Object)var3_3));
                }
                case 1 -> {
                    WeightedVariants.Unbaked var4_4 = (WeightedVariants.Unbaked)unbaked3;
                    yield DataResult.success((Object)Either.left((Object)var4_4));
                }
                default -> DataResult.error(() -> "Only a single variant or a list of variants are supported");
            };
        });

        public BlockStateModel bake(ModelBaker var1);

        default public UnbakedRoot asRoot() {
            return new SimpleCachedUnbakedRoot(this);
        }
    }

    public static interface UnbakedRoot
    extends ResolvableModel {
        public BlockStateModel bake(BlockState var1, ModelBaker var2);

        public Object visualEqualityGroup(BlockState var1);
    }
}

