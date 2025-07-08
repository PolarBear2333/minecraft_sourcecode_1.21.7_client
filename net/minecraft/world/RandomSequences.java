/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 */
package net.minecraft.world;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.RandomSequence;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class RandomSequences
extends SavedData {
    public static final SavedDataType<RandomSequences> TYPE = new SavedDataType<RandomSequences>("random_sequences", context -> new RandomSequences(context.worldSeed()), context -> RandomSequences.codec(context.worldSeed()), DataFixTypes.SAVED_DATA_RANDOM_SEQUENCES);
    private final long worldSeed;
    private int salt;
    private boolean includeWorldSeed = true;
    private boolean includeSequenceId = true;
    private final Map<ResourceLocation, RandomSequence> sequences = new Object2ObjectOpenHashMap();

    public RandomSequences(long l) {
        this.worldSeed = l;
    }

    private RandomSequences(long l, int n, boolean bl, boolean bl2, Map<ResourceLocation, RandomSequence> map) {
        this.worldSeed = l;
        this.salt = n;
        this.includeWorldSeed = bl;
        this.includeSequenceId = bl2;
        this.sequences.putAll(map);
    }

    public static Codec<RandomSequences> codec(long l) {
        return RecordCodecBuilder.create(instance -> instance.group((App)RecordCodecBuilder.point((Object)l), (App)Codec.INT.fieldOf("salt").forGetter(randomSequences -> randomSequences.salt), (App)Codec.BOOL.optionalFieldOf("include_world_seed", (Object)true).forGetter(randomSequences -> randomSequences.includeWorldSeed), (App)Codec.BOOL.optionalFieldOf("include_sequence_id", (Object)true).forGetter(randomSequences -> randomSequences.includeSequenceId), (App)Codec.unboundedMap(ResourceLocation.CODEC, RandomSequence.CODEC).fieldOf("sequences").forGetter(randomSequences -> randomSequences.sequences)).apply((Applicative)instance, RandomSequences::new));
    }

    public RandomSource get(ResourceLocation resourceLocation) {
        RandomSource randomSource = this.sequences.computeIfAbsent(resourceLocation, this::createSequence).random();
        return new DirtyMarkingRandomSource(randomSource);
    }

    private RandomSequence createSequence(ResourceLocation resourceLocation) {
        return this.createSequence(resourceLocation, this.salt, this.includeWorldSeed, this.includeSequenceId);
    }

    private RandomSequence createSequence(ResourceLocation resourceLocation, int n, boolean bl, boolean bl2) {
        long l = (bl ? this.worldSeed : 0L) ^ (long)n;
        return new RandomSequence(l, bl2 ? Optional.of(resourceLocation) : Optional.empty());
    }

    public void forAllSequences(BiConsumer<ResourceLocation, RandomSequence> biConsumer) {
        this.sequences.forEach(biConsumer);
    }

    public void setSeedDefaults(int n, boolean bl, boolean bl2) {
        this.salt = n;
        this.includeWorldSeed = bl;
        this.includeSequenceId = bl2;
    }

    public int clear() {
        int n = this.sequences.size();
        this.sequences.clear();
        return n;
    }

    public void reset(ResourceLocation resourceLocation) {
        this.sequences.put(resourceLocation, this.createSequence(resourceLocation));
    }

    public void reset(ResourceLocation resourceLocation, int n, boolean bl, boolean bl2) {
        this.sequences.put(resourceLocation, this.createSequence(resourceLocation, n, bl, bl2));
    }

    class DirtyMarkingRandomSource
    implements RandomSource {
        private final RandomSource random;

        DirtyMarkingRandomSource(RandomSource randomSource) {
            this.random = randomSource;
        }

        @Override
        public RandomSource fork() {
            RandomSequences.this.setDirty();
            return this.random.fork();
        }

        @Override
        public PositionalRandomFactory forkPositional() {
            RandomSequences.this.setDirty();
            return this.random.forkPositional();
        }

        @Override
        public void setSeed(long l) {
            RandomSequences.this.setDirty();
            this.random.setSeed(l);
        }

        @Override
        public int nextInt() {
            RandomSequences.this.setDirty();
            return this.random.nextInt();
        }

        @Override
        public int nextInt(int n) {
            RandomSequences.this.setDirty();
            return this.random.nextInt(n);
        }

        @Override
        public long nextLong() {
            RandomSequences.this.setDirty();
            return this.random.nextLong();
        }

        @Override
        public boolean nextBoolean() {
            RandomSequences.this.setDirty();
            return this.random.nextBoolean();
        }

        @Override
        public float nextFloat() {
            RandomSequences.this.setDirty();
            return this.random.nextFloat();
        }

        @Override
        public double nextDouble() {
            RandomSequences.this.setDirty();
            return this.random.nextDouble();
        }

        @Override
        public double nextGaussian() {
            RandomSequences.this.setDirty();
            return this.random.nextGaussian();
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof DirtyMarkingRandomSource) {
                DirtyMarkingRandomSource dirtyMarkingRandomSource = (DirtyMarkingRandomSource)object;
                return this.random.equals(dirtyMarkingRandomSource.random);
            }
            return false;
        }
    }
}

