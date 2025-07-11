/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 */
package net.minecraft.world.level.levelgen.feature.treedecorators;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Comparator;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public abstract class TreeDecorator {
    public static final Codec<TreeDecorator> CODEC = BuiltInRegistries.TREE_DECORATOR_TYPE.byNameCodec().dispatch(TreeDecorator::type, TreeDecoratorType::codec);

    protected abstract TreeDecoratorType<?> type();

    public abstract void place(Context var1);

    public static final class Context {
        private final LevelSimulatedReader level;
        private final BiConsumer<BlockPos, BlockState> decorationSetter;
        private final RandomSource random;
        private final ObjectArrayList<BlockPos> logs;
        private final ObjectArrayList<BlockPos> leaves;
        private final ObjectArrayList<BlockPos> roots;

        public Context(LevelSimulatedReader levelSimulatedReader, BiConsumer<BlockPos, BlockState> biConsumer, RandomSource randomSource, Set<BlockPos> set, Set<BlockPos> set2, Set<BlockPos> set3) {
            this.level = levelSimulatedReader;
            this.decorationSetter = biConsumer;
            this.random = randomSource;
            this.roots = new ObjectArrayList(set3);
            this.logs = new ObjectArrayList(set);
            this.leaves = new ObjectArrayList(set2);
            this.logs.sort(Comparator.comparingInt(Vec3i::getY));
            this.leaves.sort(Comparator.comparingInt(Vec3i::getY));
            this.roots.sort(Comparator.comparingInt(Vec3i::getY));
        }

        public void placeVine(BlockPos blockPos, BooleanProperty booleanProperty) {
            this.setBlock(blockPos, (BlockState)Blocks.VINE.defaultBlockState().setValue(booleanProperty, true));
        }

        public void setBlock(BlockPos blockPos, BlockState blockState) {
            this.decorationSetter.accept(blockPos, blockState);
        }

        public boolean isAir(BlockPos blockPos) {
            return this.level.isStateAtPosition(blockPos, BlockBehaviour.BlockStateBase::isAir);
        }

        public boolean checkBlock(BlockPos blockPos, Predicate<BlockState> predicate) {
            return this.level.isStateAtPosition(blockPos, predicate);
        }

        public LevelSimulatedReader level() {
            return this.level;
        }

        public RandomSource random() {
            return this.random;
        }

        public ObjectArrayList<BlockPos> logs() {
            return this.logs;
        }

        public ObjectArrayList<BlockPos> leaves() {
            return this.leaves;
        }

        public ObjectArrayList<BlockPos> roots() {
            return this.roots;
        }
    }
}

