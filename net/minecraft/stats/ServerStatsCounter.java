/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParseException
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  org.apache.commons.io.FileUtils
 *  org.slf4j.Logger
 */
package net.minecraft.stats;

import com.google.common.collect.Sets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatsCounter;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

public class ServerStatsCounter
extends StatsCounter {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Codec<Map<Stat<?>, Integer>> STATS_CODEC = Codec.dispatchedMap(BuiltInRegistries.STAT_TYPE.byNameCodec(), Util.memoize(ServerStatsCounter::createTypedStatsCodec)).xmap(map -> {
        HashMap hashMap = new HashMap();
        map.forEach((statType, map2) -> hashMap.putAll(map2));
        return hashMap;
    }, map -> map.entrySet().stream().collect(Collectors.groupingBy(entry -> ((Stat)entry.getKey()).getType(), Util.toMap())));
    private final MinecraftServer server;
    private final File file;
    private final Set<Stat<?>> dirty = Sets.newHashSet();

    private static <T> Codec<Map<Stat<?>, Integer>> createTypedStatsCodec(StatType<T> statType) {
        Codec<T> codec = statType.getRegistry().byNameCodec();
        Codec codec2 = codec.flatComapMap(statType::get, stat -> {
            if (stat.getType() == statType) {
                return DataResult.success(stat.getValue());
            }
            return DataResult.error(() -> "Expected type " + String.valueOf(statType) + ", but got " + String.valueOf(stat.getType()));
        });
        return Codec.unboundedMap((Codec)codec2, (Codec)Codec.INT);
    }

    public ServerStatsCounter(MinecraftServer minecraftServer, File file) {
        this.server = minecraftServer;
        this.file = file;
        if (file.isFile()) {
            try {
                this.parseLocal(minecraftServer.getFixerUpper(), FileUtils.readFileToString((File)file));
            }
            catch (IOException iOException) {
                LOGGER.error("Couldn't read statistics file {}", (Object)file, (Object)iOException);
            }
            catch (JsonParseException jsonParseException) {
                LOGGER.error("Couldn't parse statistics file {}", (Object)file, (Object)jsonParseException);
            }
        }
    }

    public void save() {
        try {
            FileUtils.writeStringToFile((File)this.file, (String)this.toJson());
        }
        catch (IOException iOException) {
            LOGGER.error("Couldn't save stats", (Throwable)iOException);
        }
    }

    @Override
    public void setValue(Player player, Stat<?> stat, int n) {
        super.setValue(player, stat, n);
        this.dirty.add(stat);
    }

    private Set<Stat<?>> getDirty() {
        HashSet hashSet = Sets.newHashSet(this.dirty);
        this.dirty.clear();
        return hashSet;
    }

    public void parseLocal(DataFixer dataFixer, String string2) {
        try {
            JsonElement jsonElement = StrictJsonParser.parse(string2);
            if (jsonElement.isJsonNull()) {
                LOGGER.error("Unable to parse Stat data from {}", (Object)this.file);
                return;
            }
            Dynamic dynamic = new Dynamic((DynamicOps)JsonOps.INSTANCE, (Object)jsonElement);
            dynamic = DataFixTypes.STATS.updateToCurrentVersion(dataFixer, dynamic, NbtUtils.getDataVersion(dynamic, 1343));
            this.stats.putAll(STATS_CODEC.parse(dynamic.get("stats").orElseEmptyMap()).resultOrPartial(string -> LOGGER.error("Failed to parse statistics for {}: {}", (Object)this.file, string)).orElse(Map.of()));
        }
        catch (JsonParseException jsonParseException) {
            LOGGER.error("Unable to parse Stat data from {}", (Object)this.file, (Object)jsonParseException);
        }
    }

    protected String toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("stats", (JsonElement)STATS_CODEC.encodeStart((DynamicOps)JsonOps.INSTANCE, (Object)this.stats).getOrThrow());
        jsonObject.addProperty("DataVersion", (Number)SharedConstants.getCurrentVersion().dataVersion().version());
        return jsonObject.toString();
    }

    public void markAllDirty() {
        this.dirty.addAll((Collection<Stat<?>>)this.stats.keySet());
    }

    public void sendStats(ServerPlayer serverPlayer) {
        Object2IntOpenHashMap object2IntOpenHashMap = new Object2IntOpenHashMap();
        for (Stat<?> stat : this.getDirty()) {
            object2IntOpenHashMap.put(stat, this.getValue(stat));
        }
        serverPlayer.connection.send(new ClientboundAwardStatsPacket((Object2IntMap<Stat<?>>)object2IntOpenHashMap));
    }
}

