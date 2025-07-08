/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.datafixers.util.Pair
 *  it.unimi.dsi.fastutil.longs.Long2ByteMap
 *  it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 */
package net.minecraft.world.entity.ai.village.poi;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.SectionTracker;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiSection;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.chunk.storage.ChunkIOErrorReporter;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.chunk.storage.SectionStorage;
import net.minecraft.world.level.chunk.storage.SimpleRegionStorage;

public class PoiManager
extends SectionStorage<PoiSection, PoiSection.Packed> {
    public static final int MAX_VILLAGE_DISTANCE = 6;
    public static final int VILLAGE_SECTION_SIZE = 1;
    private final DistanceTracker distanceTracker;
    private final LongSet loadedChunks = new LongOpenHashSet();

    public PoiManager(RegionStorageInfo regionStorageInfo, Path path, DataFixer dataFixer, boolean bl, RegistryAccess registryAccess, ChunkIOErrorReporter chunkIOErrorReporter, LevelHeightAccessor levelHeightAccessor) {
        super(new SimpleRegionStorage(regionStorageInfo, path, dataFixer, bl, DataFixTypes.POI_CHUNK), PoiSection.Packed.CODEC, PoiSection::pack, PoiSection.Packed::unpack, PoiSection::new, registryAccess, chunkIOErrorReporter, levelHeightAccessor);
        this.distanceTracker = new DistanceTracker();
    }

    public void add(BlockPos blockPos, Holder<PoiType> holder) {
        ((PoiSection)this.getOrCreate(SectionPos.asLong(blockPos))).add(blockPos, holder);
    }

    public void remove(BlockPos blockPos) {
        this.getOrLoad(SectionPos.asLong(blockPos)).ifPresent(poiSection -> poiSection.remove(blockPos));
    }

    public long getCountInRange(Predicate<Holder<PoiType>> predicate, BlockPos blockPos, int n, Occupancy occupancy) {
        return this.getInRange(predicate, blockPos, n, occupancy).count();
    }

    public boolean existsAtPosition(ResourceKey<PoiType> resourceKey, BlockPos blockPos) {
        return this.exists(blockPos, holder -> holder.is(resourceKey));
    }

    public Stream<PoiRecord> getInSquare(Predicate<Holder<PoiType>> predicate, BlockPos blockPos, int n, Occupancy occupancy) {
        int n2 = Math.floorDiv(n, 16) + 1;
        return ChunkPos.rangeClosed(new ChunkPos(blockPos), n2).flatMap(chunkPos -> this.getInChunk(predicate, (ChunkPos)chunkPos, occupancy)).filter(poiRecord -> {
            BlockPos blockPos2 = poiRecord.getPos();
            return Math.abs(blockPos2.getX() - blockPos.getX()) <= n && Math.abs(blockPos2.getZ() - blockPos.getZ()) <= n;
        });
    }

    public Stream<PoiRecord> getInRange(Predicate<Holder<PoiType>> predicate, BlockPos blockPos, int n, Occupancy occupancy) {
        int n2 = n * n;
        return this.getInSquare(predicate, blockPos, n, occupancy).filter(poiRecord -> poiRecord.getPos().distSqr(blockPos) <= (double)n2);
    }

    @VisibleForDebug
    public Stream<PoiRecord> getInChunk(Predicate<Holder<PoiType>> predicate, ChunkPos chunkPos, Occupancy occupancy) {
        return IntStream.rangeClosed(this.levelHeightAccessor.getMinSectionY(), this.levelHeightAccessor.getMaxSectionY()).boxed().map(n -> this.getOrLoad(SectionPos.of(chunkPos, n).asLong())).filter(Optional::isPresent).flatMap(optional -> ((PoiSection)optional.get()).getRecords(predicate, occupancy));
    }

    public Stream<BlockPos> findAll(Predicate<Holder<PoiType>> predicate, Predicate<BlockPos> predicate2, BlockPos blockPos, int n, Occupancy occupancy) {
        return this.getInRange(predicate, blockPos, n, occupancy).map(PoiRecord::getPos).filter(predicate2);
    }

    public Stream<Pair<Holder<PoiType>, BlockPos>> findAllWithType(Predicate<Holder<PoiType>> predicate, Predicate<BlockPos> predicate2, BlockPos blockPos, int n, Occupancy occupancy) {
        return this.getInRange(predicate, blockPos, n, occupancy).filter(poiRecord -> predicate2.test(poiRecord.getPos())).map(poiRecord -> Pair.of(poiRecord.getPoiType(), (Object)poiRecord.getPos()));
    }

    public Stream<Pair<Holder<PoiType>, BlockPos>> findAllClosestFirstWithType(Predicate<Holder<PoiType>> predicate, Predicate<BlockPos> predicate2, BlockPos blockPos, int n, Occupancy occupancy) {
        return this.findAllWithType(predicate, predicate2, blockPos, n, occupancy).sorted(Comparator.comparingDouble(pair -> ((BlockPos)pair.getSecond()).distSqr(blockPos)));
    }

    public Optional<BlockPos> find(Predicate<Holder<PoiType>> predicate, Predicate<BlockPos> predicate2, BlockPos blockPos, int n, Occupancy occupancy) {
        return this.findAll(predicate, predicate2, blockPos, n, occupancy).findFirst();
    }

    public Optional<BlockPos> findClosest(Predicate<Holder<PoiType>> predicate, BlockPos blockPos, int n, Occupancy occupancy) {
        return this.getInRange(predicate, blockPos, n, occupancy).map(PoiRecord::getPos).min(Comparator.comparingDouble(blockPos2 -> blockPos2.distSqr(blockPos)));
    }

    public Optional<Pair<Holder<PoiType>, BlockPos>> findClosestWithType(Predicate<Holder<PoiType>> predicate, BlockPos blockPos, int n, Occupancy occupancy) {
        return this.getInRange(predicate, blockPos, n, occupancy).min(Comparator.comparingDouble(poiRecord -> poiRecord.getPos().distSqr(blockPos))).map(poiRecord -> Pair.of(poiRecord.getPoiType(), (Object)poiRecord.getPos()));
    }

    public Optional<BlockPos> findClosest(Predicate<Holder<PoiType>> predicate, Predicate<BlockPos> predicate2, BlockPos blockPos, int n, Occupancy occupancy) {
        return this.getInRange(predicate, blockPos, n, occupancy).map(PoiRecord::getPos).filter(predicate2).min(Comparator.comparingDouble(blockPos2 -> blockPos2.distSqr(blockPos)));
    }

    public Optional<BlockPos> take(Predicate<Holder<PoiType>> predicate, BiPredicate<Holder<PoiType>, BlockPos> biPredicate, BlockPos blockPos, int n) {
        return this.getInRange(predicate, blockPos, n, Occupancy.HAS_SPACE).filter(poiRecord -> biPredicate.test(poiRecord.getPoiType(), poiRecord.getPos())).findFirst().map(poiRecord -> {
            poiRecord.acquireTicket();
            return poiRecord.getPos();
        });
    }

    public Optional<BlockPos> getRandom(Predicate<Holder<PoiType>> predicate, Predicate<BlockPos> predicate2, Occupancy occupancy, BlockPos blockPos, int n, RandomSource randomSource) {
        List<PoiRecord> list = Util.toShuffledList(this.getInRange(predicate, blockPos, n, occupancy), randomSource);
        return list.stream().filter(poiRecord -> predicate2.test(poiRecord.getPos())).findFirst().map(PoiRecord::getPos);
    }

    public boolean release(BlockPos blockPos) {
        return this.getOrLoad(SectionPos.asLong(blockPos)).map(poiSection -> poiSection.release(blockPos)).orElseThrow(() -> Util.pauseInIde(new IllegalStateException("POI never registered at " + String.valueOf(blockPos))));
    }

    public boolean exists(BlockPos blockPos, Predicate<Holder<PoiType>> predicate) {
        return this.getOrLoad(SectionPos.asLong(blockPos)).map(poiSection -> poiSection.exists(blockPos, predicate)).orElse(false);
    }

    public Optional<Holder<PoiType>> getType(BlockPos blockPos) {
        return this.getOrLoad(SectionPos.asLong(blockPos)).flatMap(poiSection -> poiSection.getType(blockPos));
    }

    @Deprecated
    @VisibleForDebug
    public int getFreeTickets(BlockPos blockPos) {
        return this.getOrLoad(SectionPos.asLong(blockPos)).map(poiSection -> poiSection.getFreeTickets(blockPos)).orElse(0);
    }

    public int sectionsToVillage(SectionPos sectionPos) {
        this.distanceTracker.runAllUpdates();
        return this.distanceTracker.getLevel(sectionPos.asLong());
    }

    boolean isVillageCenter(long l) {
        Optional optional = this.get(l);
        if (optional == null) {
            return false;
        }
        return optional.map(poiSection -> poiSection.getRecords(holder -> holder.is(PoiTypeTags.VILLAGE), Occupancy.IS_OCCUPIED).findAny().isPresent()).orElse(false);
    }

    @Override
    public void tick(BooleanSupplier booleanSupplier) {
        super.tick(booleanSupplier);
        this.distanceTracker.runAllUpdates();
    }

    @Override
    protected void setDirty(long l) {
        super.setDirty(l);
        this.distanceTracker.update(l, this.distanceTracker.getLevelFromSource(l), false);
    }

    @Override
    protected void onSectionLoad(long l) {
        this.distanceTracker.update(l, this.distanceTracker.getLevelFromSource(l), false);
    }

    public void checkConsistencyWithBlocks(SectionPos sectionPos, LevelChunkSection levelChunkSection) {
        Util.ifElse(this.getOrLoad(sectionPos.asLong()), poiSection -> poiSection.refresh(biConsumer -> {
            if (PoiManager.mayHavePoi(levelChunkSection)) {
                this.updateFromSection(levelChunkSection, sectionPos, (BiConsumer<BlockPos, Holder<PoiType>>)biConsumer);
            }
        }), () -> {
            if (PoiManager.mayHavePoi(levelChunkSection)) {
                PoiSection poiSection = (PoiSection)this.getOrCreate(sectionPos.asLong());
                this.updateFromSection(levelChunkSection, sectionPos, poiSection::add);
            }
        });
    }

    private static boolean mayHavePoi(LevelChunkSection levelChunkSection) {
        return levelChunkSection.maybeHas(PoiTypes::hasPoi);
    }

    private void updateFromSection(LevelChunkSection levelChunkSection, SectionPos sectionPos, BiConsumer<BlockPos, Holder<PoiType>> biConsumer) {
        sectionPos.blocksInside().forEach(blockPos -> {
            BlockState blockState = levelChunkSection.getBlockState(SectionPos.sectionRelative(blockPos.getX()), SectionPos.sectionRelative(blockPos.getY()), SectionPos.sectionRelative(blockPos.getZ()));
            PoiTypes.forState(blockState).ifPresent(holder -> biConsumer.accept((BlockPos)blockPos, (Holder<PoiType>)holder));
        });
    }

    public void ensureLoadedAndValid(LevelReader levelReader, BlockPos blockPos, int n) {
        SectionPos.aroundChunk(new ChunkPos(blockPos), Math.floorDiv(n, 16), this.levelHeightAccessor.getMinSectionY(), this.levelHeightAccessor.getMaxSectionY()).map(sectionPos -> Pair.of((Object)sectionPos, this.getOrLoad(sectionPos.asLong()))).filter(pair -> ((Optional)pair.getSecond()).map(PoiSection::isValid).orElse(false) == false).map(pair -> ((SectionPos)pair.getFirst()).chunk()).filter(chunkPos -> this.loadedChunks.add(chunkPos.toLong())).forEach(chunkPos -> levelReader.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.EMPTY));
    }

    final class DistanceTracker
    extends SectionTracker {
        private final Long2ByteMap levels;

        protected DistanceTracker() {
            super(7, 16, 256);
            this.levels = new Long2ByteOpenHashMap();
            this.levels.defaultReturnValue((byte)7);
        }

        @Override
        protected int getLevelFromSource(long l) {
            return PoiManager.this.isVillageCenter(l) ? 0 : 7;
        }

        @Override
        protected int getLevel(long l) {
            return this.levels.get(l);
        }

        @Override
        protected void setLevel(long l, int n) {
            if (n > 6) {
                this.levels.remove(l);
            } else {
                this.levels.put(l, (byte)n);
            }
        }

        public void runAllUpdates() {
            super.runUpdates(Integer.MAX_VALUE);
        }
    }

    public static enum Occupancy {
        HAS_SPACE(PoiRecord::hasSpace),
        IS_OCCUPIED(PoiRecord::isOccupied),
        ANY(poiRecord -> true);

        private final Predicate<? super PoiRecord> test;

        private Occupancy(Predicate<? super PoiRecord> predicate) {
            this.test = predicate;
        }

        public Predicate<? super PoiRecord> getTest() {
            return this.test;
        }
    }
}

