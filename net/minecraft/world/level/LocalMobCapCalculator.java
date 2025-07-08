/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 */
package net.minecraft.world.level;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;

public class LocalMobCapCalculator {
    private final Long2ObjectMap<List<ServerPlayer>> playersNearChunk = new Long2ObjectOpenHashMap();
    private final Map<ServerPlayer, MobCounts> playerMobCounts = Maps.newHashMap();
    private final ChunkMap chunkMap;

    public LocalMobCapCalculator(ChunkMap chunkMap) {
        this.chunkMap = chunkMap;
    }

    private List<ServerPlayer> getPlayersNear(ChunkPos chunkPos) {
        return (List)this.playersNearChunk.computeIfAbsent(chunkPos.toLong(), l -> this.chunkMap.getPlayersCloseForSpawning(chunkPos));
    }

    public void addMob(ChunkPos chunkPos, MobCategory mobCategory) {
        for (ServerPlayer serverPlayer2 : this.getPlayersNear(chunkPos)) {
            this.playerMobCounts.computeIfAbsent(serverPlayer2, serverPlayer -> new MobCounts()).add(mobCategory);
        }
    }

    public boolean canSpawn(MobCategory mobCategory, ChunkPos chunkPos) {
        for (ServerPlayer serverPlayer : this.getPlayersNear(chunkPos)) {
            MobCounts mobCounts = this.playerMobCounts.get(serverPlayer);
            if (mobCounts != null && !mobCounts.canSpawn(mobCategory)) continue;
            return true;
        }
        return false;
    }

    static class MobCounts {
        private final Object2IntMap<MobCategory> counts = new Object2IntOpenHashMap(MobCategory.values().length);

        MobCounts() {
        }

        public void add(MobCategory mobCategory2) {
            this.counts.computeInt((Object)mobCategory2, (mobCategory, n) -> n == null ? 1 : n + 1);
        }

        public boolean canSpawn(MobCategory mobCategory) {
            return this.counts.getOrDefault((Object)mobCategory, 0) < mobCategory.getMaxInstancesPerChunk();
        }
    }
}

