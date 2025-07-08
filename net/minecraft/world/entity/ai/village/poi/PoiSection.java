/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.shorts.Short2ObjectMap
 *  it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap
 *  org.slf4j.Logger
 */
package net.minecraft.world.entity.ai.village.poi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import org.slf4j.Logger;

public class PoiSection {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Short2ObjectMap<PoiRecord> records = new Short2ObjectOpenHashMap();
    private final Map<Holder<PoiType>, Set<PoiRecord>> byType = Maps.newHashMap();
    private final Runnable setDirty;
    private boolean isValid;

    public PoiSection(Runnable runnable) {
        this(runnable, true, (List<PoiRecord>)ImmutableList.of());
    }

    PoiSection(Runnable runnable, boolean bl, List<PoiRecord> list) {
        this.setDirty = runnable;
        this.isValid = bl;
        list.forEach(this::add);
    }

    public Packed pack() {
        return new Packed(this.isValid, this.records.values().stream().map(PoiRecord::pack).toList());
    }

    public Stream<PoiRecord> getRecords(Predicate<Holder<PoiType>> predicate, PoiManager.Occupancy occupancy) {
        return this.byType.entrySet().stream().filter(entry -> predicate.test((Holder)entry.getKey())).flatMap(entry -> ((Set)entry.getValue()).stream()).filter(occupancy.getTest());
    }

    public void add(BlockPos blockPos, Holder<PoiType> holder) {
        if (this.add(new PoiRecord(blockPos, holder, this.setDirty))) {
            LOGGER.debug("Added POI of type {} @ {}", (Object)holder.getRegisteredName(), (Object)blockPos);
            this.setDirty.run();
        }
    }

    private boolean add(PoiRecord poiRecord) {
        BlockPos blockPos = poiRecord.getPos();
        Holder<PoiType> holder2 = poiRecord.getPoiType();
        short s = SectionPos.sectionRelativePos(blockPos);
        PoiRecord poiRecord2 = (PoiRecord)this.records.get(s);
        if (poiRecord2 != null) {
            if (holder2.equals(poiRecord2.getPoiType())) {
                return false;
            }
            Util.logAndPauseIfInIde("POI data mismatch: already registered at " + String.valueOf(blockPos));
        }
        this.records.put(s, (Object)poiRecord);
        this.byType.computeIfAbsent(holder2, holder -> Sets.newHashSet()).add(poiRecord);
        return true;
    }

    public void remove(BlockPos blockPos) {
        PoiRecord poiRecord = (PoiRecord)this.records.remove(SectionPos.sectionRelativePos(blockPos));
        if (poiRecord == null) {
            LOGGER.error("POI data mismatch: never registered at {}", (Object)blockPos);
            return;
        }
        this.byType.get(poiRecord.getPoiType()).remove(poiRecord);
        LOGGER.debug("Removed POI of type {} @ {}", LogUtils.defer(poiRecord::getPoiType), LogUtils.defer(poiRecord::getPos));
        this.setDirty.run();
    }

    @Deprecated
    @VisibleForDebug
    public int getFreeTickets(BlockPos blockPos) {
        return this.getPoiRecord(blockPos).map(PoiRecord::getFreeTickets).orElse(0);
    }

    public boolean release(BlockPos blockPos) {
        PoiRecord poiRecord = (PoiRecord)this.records.get(SectionPos.sectionRelativePos(blockPos));
        if (poiRecord == null) {
            throw Util.pauseInIde(new IllegalStateException("POI never registered at " + String.valueOf(blockPos)));
        }
        boolean bl = poiRecord.releaseTicket();
        this.setDirty.run();
        return bl;
    }

    public boolean exists(BlockPos blockPos, Predicate<Holder<PoiType>> predicate) {
        return this.getType(blockPos).filter(predicate).isPresent();
    }

    public Optional<Holder<PoiType>> getType(BlockPos blockPos) {
        return this.getPoiRecord(blockPos).map(PoiRecord::getPoiType);
    }

    private Optional<PoiRecord> getPoiRecord(BlockPos blockPos) {
        return Optional.ofNullable((PoiRecord)this.records.get(SectionPos.sectionRelativePos(blockPos)));
    }

    public void refresh(Consumer<BiConsumer<BlockPos, Holder<PoiType>>> consumer) {
        if (!this.isValid) {
            Short2ObjectOpenHashMap short2ObjectOpenHashMap = new Short2ObjectOpenHashMap(this.records);
            this.clear();
            consumer.accept((arg_0, arg_1) -> this.lambda$refresh$4((Short2ObjectMap)short2ObjectOpenHashMap, arg_0, arg_1));
            this.isValid = true;
            this.setDirty.run();
        }
    }

    private void clear() {
        this.records.clear();
        this.byType.clear();
    }

    boolean isValid() {
        return this.isValid;
    }

    private /* synthetic */ void lambda$refresh$4(Short2ObjectMap short2ObjectMap, BlockPos blockPos, Holder holder) {
        short s2 = SectionPos.sectionRelativePos(blockPos);
        PoiRecord poiRecord = (PoiRecord)short2ObjectMap.computeIfAbsent(s2, s -> new PoiRecord(blockPos, holder, this.setDirty));
        this.add(poiRecord);
    }

    public record Packed(boolean isValid, List<PoiRecord.Packed> records) {
        public static final Codec<Packed> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.BOOL.lenientOptionalFieldOf("Valid", (Object)false).forGetter(Packed::isValid), (App)PoiRecord.Packed.CODEC.listOf().fieldOf("Records").forGetter(Packed::records)).apply((Applicative)instance, Packed::new));

        public PoiSection unpack(Runnable runnable) {
            return new PoiSection(runnable, this.isValid, this.records.stream().map(packed -> packed.unpack(runnable)).toList());
        }
    }
}

