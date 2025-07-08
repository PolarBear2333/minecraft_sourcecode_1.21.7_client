/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMaps
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  it.unimi.dsi.fastutil.objects.ObjectIterator
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkLevel;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.slf4j.Logger;

public class TicketStorage
extends SavedData {
    private static final int INITIAL_TICKET_LIST_CAPACITY = 4;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Codec<Pair<ChunkPos, Ticket>> TICKET_ENTRY = Codec.mapPair((MapCodec)ChunkPos.CODEC.fieldOf("chunk_pos"), Ticket.CODEC).codec();
    public static final Codec<TicketStorage> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)TICKET_ENTRY.listOf().optionalFieldOf("tickets", List.of()).forGetter(TicketStorage::packTickets)).apply((Applicative)instance, TicketStorage::fromPacked));
    public static final SavedDataType<TicketStorage> TYPE = new SavedDataType<TicketStorage>("chunks", TicketStorage::new, CODEC, DataFixTypes.SAVED_DATA_FORCED_CHUNKS);
    private final Long2ObjectOpenHashMap<List<Ticket>> tickets;
    private final Long2ObjectOpenHashMap<List<Ticket>> deactivatedTickets;
    private LongSet chunksWithForcedTickets = new LongOpenHashSet();
    @Nullable
    private ChunkUpdated loadingChunkUpdatedListener;
    @Nullable
    private ChunkUpdated simulationChunkUpdatedListener;

    private TicketStorage(Long2ObjectOpenHashMap<List<Ticket>> long2ObjectOpenHashMap, Long2ObjectOpenHashMap<List<Ticket>> long2ObjectOpenHashMap2) {
        this.tickets = long2ObjectOpenHashMap;
        this.deactivatedTickets = long2ObjectOpenHashMap2;
        this.updateForcedChunks();
    }

    public TicketStorage() {
        this((Long2ObjectOpenHashMap<List<Ticket>>)new Long2ObjectOpenHashMap(4), (Long2ObjectOpenHashMap<List<Ticket>>)new Long2ObjectOpenHashMap());
    }

    private static TicketStorage fromPacked(List<Pair<ChunkPos, Ticket>> list) {
        Long2ObjectOpenHashMap long2ObjectOpenHashMap = new Long2ObjectOpenHashMap();
        for (Pair<ChunkPos, Ticket> pair : list) {
            ChunkPos chunkPos = (ChunkPos)pair.getFirst();
            List list2 = (List)long2ObjectOpenHashMap.computeIfAbsent(chunkPos.toLong(), l -> new ObjectArrayList(4));
            list2.add((Ticket)pair.getSecond());
        }
        return new TicketStorage((Long2ObjectOpenHashMap<List<Ticket>>)new Long2ObjectOpenHashMap(4), (Long2ObjectOpenHashMap<List<Ticket>>)long2ObjectOpenHashMap);
    }

    private List<Pair<ChunkPos, Ticket>> packTickets() {
        ArrayList<Pair<ChunkPos, Ticket>> arrayList = new ArrayList<Pair<ChunkPos, Ticket>>();
        this.forEachTicket((chunkPos, ticket) -> {
            if (ticket.getType().persist()) {
                arrayList.add(new Pair(chunkPos, ticket));
            }
        });
        return arrayList;
    }

    private void forEachTicket(BiConsumer<ChunkPos, Ticket> biConsumer) {
        TicketStorage.forEachTicket(biConsumer, this.tickets);
        TicketStorage.forEachTicket(biConsumer, this.deactivatedTickets);
    }

    private static void forEachTicket(BiConsumer<ChunkPos, Ticket> biConsumer, Long2ObjectOpenHashMap<List<Ticket>> long2ObjectOpenHashMap) {
        for (Long2ObjectMap.Entry entry : Long2ObjectMaps.fastIterable(long2ObjectOpenHashMap)) {
            ChunkPos chunkPos = new ChunkPos(entry.getLongKey());
            for (Ticket ticket : (List)entry.getValue()) {
                biConsumer.accept(chunkPos, ticket);
            }
        }
    }

    public void activateAllDeactivatedTickets() {
        for (Long2ObjectMap.Entry entry : Long2ObjectMaps.fastIterable(this.deactivatedTickets)) {
            for (Ticket ticket : (List)entry.getValue()) {
                this.addTicket(entry.getLongKey(), ticket);
            }
        }
        this.deactivatedTickets.clear();
    }

    public void setLoadingChunkUpdatedListener(@Nullable ChunkUpdated chunkUpdated) {
        this.loadingChunkUpdatedListener = chunkUpdated;
    }

    public void setSimulationChunkUpdatedListener(@Nullable ChunkUpdated chunkUpdated) {
        this.simulationChunkUpdatedListener = chunkUpdated;
    }

    public boolean hasTickets() {
        return !this.tickets.isEmpty();
    }

    public List<Ticket> getTickets(long l) {
        return (List)this.tickets.getOrDefault(l, List.of());
    }

    private List<Ticket> getOrCreateTickets(long l2) {
        return (List)this.tickets.computeIfAbsent(l2, l -> new ObjectArrayList(4));
    }

    public void addTicketWithRadius(TicketType ticketType, ChunkPos chunkPos, int n) {
        Ticket ticket = new Ticket(ticketType, ChunkLevel.byStatus(FullChunkStatus.FULL) - n);
        this.addTicket(chunkPos.toLong(), ticket);
    }

    public void addTicket(Ticket ticket, ChunkPos chunkPos) {
        this.addTicket(chunkPos.toLong(), ticket);
    }

    public boolean addTicket(long l, Ticket ticket) {
        List<Ticket> list = this.getOrCreateTickets(l);
        for (Ticket ticket2 : list) {
            if (!TicketStorage.isTicketSameTypeAndLevel(ticket, ticket2)) continue;
            ticket2.resetTicksLeft();
            this.setDirty();
            return false;
        }
        int n = TicketStorage.getTicketLevelAt(list, true);
        int n2 = TicketStorage.getTicketLevelAt(list, false);
        list.add(ticket);
        if (ticket.getType().doesSimulate() && ticket.getTicketLevel() < n && this.simulationChunkUpdatedListener != null) {
            this.simulationChunkUpdatedListener.update(l, ticket.getTicketLevel(), true);
        }
        if (ticket.getType().doesLoad() && ticket.getTicketLevel() < n2 && this.loadingChunkUpdatedListener != null) {
            this.loadingChunkUpdatedListener.update(l, ticket.getTicketLevel(), true);
        }
        if (ticket.getType().equals(TicketType.FORCED)) {
            this.chunksWithForcedTickets.add(l);
        }
        this.setDirty();
        return true;
    }

    private static boolean isTicketSameTypeAndLevel(Ticket ticket, Ticket ticket2) {
        return ticket2.getType() == ticket.getType() && ticket2.getTicketLevel() == ticket.getTicketLevel();
    }

    public int getTicketLevelAt(long l, boolean bl) {
        return TicketStorage.getTicketLevelAt(this.getTickets(l), bl);
    }

    private static int getTicketLevelAt(List<Ticket> list, boolean bl) {
        Ticket ticket = TicketStorage.getLowestTicket(list, bl);
        return ticket == null ? ChunkLevel.MAX_LEVEL + 1 : ticket.getTicketLevel();
    }

    @Nullable
    private static Ticket getLowestTicket(@Nullable List<Ticket> list, boolean bl) {
        if (list == null) {
            return null;
        }
        Ticket ticket = null;
        for (Ticket ticket2 : list) {
            if (ticket != null && ticket2.getTicketLevel() >= ticket.getTicketLevel()) continue;
            if (bl && ticket2.getType().doesSimulate()) {
                ticket = ticket2;
                continue;
            }
            if (bl || !ticket2.getType().doesLoad()) continue;
            ticket = ticket2;
        }
        return ticket;
    }

    public void removeTicketWithRadius(TicketType ticketType, ChunkPos chunkPos, int n) {
        Ticket ticket = new Ticket(ticketType, ChunkLevel.byStatus(FullChunkStatus.FULL) - n);
        this.removeTicket(chunkPos.toLong(), ticket);
    }

    public void removeTicket(Ticket ticket, ChunkPos chunkPos) {
        this.removeTicket(chunkPos.toLong(), ticket);
    }

    public boolean removeTicket(long l, Ticket ticket) {
        List list = (List)this.tickets.get(l);
        if (list == null) {
            return false;
        }
        boolean bl = false;
        Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            Ticket ticket2 = (Ticket)iterator.next();
            if (!TicketStorage.isTicketSameTypeAndLevel(ticket, ticket2)) continue;
            iterator.remove();
            bl = true;
            break;
        }
        if (!bl) {
            return false;
        }
        if (list.isEmpty()) {
            this.tickets.remove(l);
        }
        if (ticket.getType().doesSimulate() && this.simulationChunkUpdatedListener != null) {
            this.simulationChunkUpdatedListener.update(l, TicketStorage.getTicketLevelAt(list, true), false);
        }
        if (ticket.getType().doesLoad() && this.loadingChunkUpdatedListener != null) {
            this.loadingChunkUpdatedListener.update(l, TicketStorage.getTicketLevelAt(list, false), false);
        }
        if (ticket.getType().equals(TicketType.FORCED)) {
            this.updateForcedChunks();
        }
        this.setDirty();
        return true;
    }

    private void updateForcedChunks() {
        this.chunksWithForcedTickets = this.getAllChunksWithTicketThat(ticket -> ticket.getType().equals(TicketType.FORCED));
    }

    public String getTicketDebugString(long l, boolean bl) {
        List<Ticket> list = this.getTickets(l);
        Ticket ticket = TicketStorage.getLowestTicket(list, bl);
        return ticket == null ? "no_ticket" : ticket.toString();
    }

    public void purgeStaleTickets(ChunkMap chunkMap) {
        this.removeTicketIf((l, ticket) -> {
            boolean bl;
            ChunkHolder chunkHolder = chunkMap.getUpdatingChunkIfPresent((long)l);
            boolean bl2 = bl = chunkHolder != null && !chunkHolder.isReadyForSaving() && ticket.getType().doesSimulate();
            if (bl) {
                return false;
            }
            ticket.decreaseTicksLeft();
            return ticket.isTimedOut();
        }, null);
        this.setDirty();
    }

    public void deactivateTicketsOnClosing() {
        this.removeTicketIf((l, ticket) -> ticket.getType() != TicketType.UNKNOWN, this.deactivatedTickets);
    }

    public void removeTicketIf(BiPredicate<Long, Ticket> biPredicate, @Nullable Long2ObjectOpenHashMap<List<Ticket>> long2ObjectOpenHashMap) {
        ObjectIterator objectIterator = this.tickets.long2ObjectEntrySet().fastIterator();
        boolean bl = false;
        while (objectIterator.hasNext()) {
            Long2ObjectMap.Entry entry = (Long2ObjectMap.Entry)objectIterator.next();
            Iterator iterator = ((List)entry.getValue()).iterator();
            long l2 = entry.getLongKey();
            boolean bl2 = false;
            boolean bl3 = false;
            while (iterator.hasNext()) {
                Ticket ticket = (Ticket)iterator.next();
                if (!biPredicate.test(l2, ticket)) continue;
                if (long2ObjectOpenHashMap != null) {
                    List list = (List)long2ObjectOpenHashMap.computeIfAbsent(l2, l -> new ObjectArrayList(((List)entry.getValue()).size()));
                    list.add(ticket);
                }
                iterator.remove();
                if (ticket.getType().doesLoad()) {
                    bl3 = true;
                }
                if (ticket.getType().doesSimulate()) {
                    bl2 = true;
                }
                if (!ticket.getType().equals(TicketType.FORCED)) continue;
                bl = true;
            }
            if (!bl3 && !bl2) continue;
            if (bl3 && this.loadingChunkUpdatedListener != null) {
                this.loadingChunkUpdatedListener.update(l2, TicketStorage.getTicketLevelAt((List)entry.getValue(), false), false);
            }
            if (bl2 && this.simulationChunkUpdatedListener != null) {
                this.simulationChunkUpdatedListener.update(l2, TicketStorage.getTicketLevelAt((List)entry.getValue(), true), false);
            }
            this.setDirty();
            if (!((List)entry.getValue()).isEmpty()) continue;
            objectIterator.remove();
        }
        if (bl) {
            this.updateForcedChunks();
        }
    }

    public void replaceTicketLevelOfType(int n, TicketType ticketType) {
        ArrayList<Pair> arrayList = new ArrayList<Pair>();
        for (Long2ObjectMap.Entry entry : this.tickets.long2ObjectEntrySet()) {
            for (Ticket ticket : (List)entry.getValue()) {
                if (ticket.getType() != ticketType) continue;
                arrayList.add(Pair.of((Object)ticket, (Object)entry.getLongKey()));
            }
        }
        for (Pair pair : arrayList) {
            Ticket ticket;
            Long l = (Long)pair.getSecond();
            ticket = (Ticket)pair.getFirst();
            this.removeTicket(l, ticket);
            TicketType ticketType2 = ticket.getType();
            this.addTicket(l, new Ticket(ticketType2, n));
        }
    }

    public boolean updateChunkForced(ChunkPos chunkPos, boolean bl) {
        Ticket ticket = new Ticket(TicketType.FORCED, ChunkMap.FORCED_TICKET_LEVEL);
        if (bl) {
            return this.addTicket(chunkPos.toLong(), ticket);
        }
        return this.removeTicket(chunkPos.toLong(), ticket);
    }

    public LongSet getForceLoadedChunks() {
        return this.chunksWithForcedTickets;
    }

    private LongSet getAllChunksWithTicketThat(Predicate<Ticket> predicate) {
        LongOpenHashSet longOpenHashSet = new LongOpenHashSet();
        block0: for (Long2ObjectMap.Entry entry : Long2ObjectMaps.fastIterable(this.tickets)) {
            for (Ticket ticket : (List)entry.getValue()) {
                if (!predicate.test(ticket)) continue;
                longOpenHashSet.add(entry.getLongKey());
                continue block0;
            }
        }
        return longOpenHashSet;
    }

    @FunctionalInterface
    public static interface ChunkUpdated {
        public void update(long var1, int var3, boolean var4);
    }
}

