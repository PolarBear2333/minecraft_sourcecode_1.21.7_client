/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Splitter
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.gson.JsonElement
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  javax.annotation.Nullable
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.util.LenientJsonParser;
import net.minecraft.util.datafix.fixes.References;

public class LevelDataGeneratorOptionsFix
extends DataFix {
    static final Map<String, String> MAP = Util.make(Maps.newHashMap(), hashMap -> {
        hashMap.put("0", "minecraft:ocean");
        hashMap.put("1", "minecraft:plains");
        hashMap.put("2", "minecraft:desert");
        hashMap.put("3", "minecraft:mountains");
        hashMap.put("4", "minecraft:forest");
        hashMap.put("5", "minecraft:taiga");
        hashMap.put("6", "minecraft:swamp");
        hashMap.put("7", "minecraft:river");
        hashMap.put("8", "minecraft:nether");
        hashMap.put("9", "minecraft:the_end");
        hashMap.put("10", "minecraft:frozen_ocean");
        hashMap.put("11", "minecraft:frozen_river");
        hashMap.put("12", "minecraft:snowy_tundra");
        hashMap.put("13", "minecraft:snowy_mountains");
        hashMap.put("14", "minecraft:mushroom_fields");
        hashMap.put("15", "minecraft:mushroom_field_shore");
        hashMap.put("16", "minecraft:beach");
        hashMap.put("17", "minecraft:desert_hills");
        hashMap.put("18", "minecraft:wooded_hills");
        hashMap.put("19", "minecraft:taiga_hills");
        hashMap.put("20", "minecraft:mountain_edge");
        hashMap.put("21", "minecraft:jungle");
        hashMap.put("22", "minecraft:jungle_hills");
        hashMap.put("23", "minecraft:jungle_edge");
        hashMap.put("24", "minecraft:deep_ocean");
        hashMap.put("25", "minecraft:stone_shore");
        hashMap.put("26", "minecraft:snowy_beach");
        hashMap.put("27", "minecraft:birch_forest");
        hashMap.put("28", "minecraft:birch_forest_hills");
        hashMap.put("29", "minecraft:dark_forest");
        hashMap.put("30", "minecraft:snowy_taiga");
        hashMap.put("31", "minecraft:snowy_taiga_hills");
        hashMap.put("32", "minecraft:giant_tree_taiga");
        hashMap.put("33", "minecraft:giant_tree_taiga_hills");
        hashMap.put("34", "minecraft:wooded_mountains");
        hashMap.put("35", "minecraft:savanna");
        hashMap.put("36", "minecraft:savanna_plateau");
        hashMap.put("37", "minecraft:badlands");
        hashMap.put("38", "minecraft:wooded_badlands_plateau");
        hashMap.put("39", "minecraft:badlands_plateau");
        hashMap.put("40", "minecraft:small_end_islands");
        hashMap.put("41", "minecraft:end_midlands");
        hashMap.put("42", "minecraft:end_highlands");
        hashMap.put("43", "minecraft:end_barrens");
        hashMap.put("44", "minecraft:warm_ocean");
        hashMap.put("45", "minecraft:lukewarm_ocean");
        hashMap.put("46", "minecraft:cold_ocean");
        hashMap.put("47", "minecraft:deep_warm_ocean");
        hashMap.put("48", "minecraft:deep_lukewarm_ocean");
        hashMap.put("49", "minecraft:deep_cold_ocean");
        hashMap.put("50", "minecraft:deep_frozen_ocean");
        hashMap.put("127", "minecraft:the_void");
        hashMap.put("129", "minecraft:sunflower_plains");
        hashMap.put("130", "minecraft:desert_lakes");
        hashMap.put("131", "minecraft:gravelly_mountains");
        hashMap.put("132", "minecraft:flower_forest");
        hashMap.put("133", "minecraft:taiga_mountains");
        hashMap.put("134", "minecraft:swamp_hills");
        hashMap.put("140", "minecraft:ice_spikes");
        hashMap.put("149", "minecraft:modified_jungle");
        hashMap.put("151", "minecraft:modified_jungle_edge");
        hashMap.put("155", "minecraft:tall_birch_forest");
        hashMap.put("156", "minecraft:tall_birch_hills");
        hashMap.put("157", "minecraft:dark_forest_hills");
        hashMap.put("158", "minecraft:snowy_taiga_mountains");
        hashMap.put("160", "minecraft:giant_spruce_taiga");
        hashMap.put("161", "minecraft:giant_spruce_taiga_hills");
        hashMap.put("162", "minecraft:modified_gravelly_mountains");
        hashMap.put("163", "minecraft:shattered_savanna");
        hashMap.put("164", "minecraft:shattered_savanna_plateau");
        hashMap.put("165", "minecraft:eroded_badlands");
        hashMap.put("166", "minecraft:modified_wooded_badlands_plateau");
        hashMap.put("167", "minecraft:modified_badlands_plateau");
    });
    public static final String GENERATOR_OPTIONS = "generatorOptions";

    public LevelDataGeneratorOptionsFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getOutputSchema().getType(References.LEVEL);
        return this.fixTypeEverywhereTyped("LevelDataGeneratorOptionsFix", this.getInputSchema().getType(References.LEVEL), type, typed -> Util.writeAndReadTypedOrThrow(typed, type, dynamic -> {
            Optional optional = dynamic.get(GENERATOR_OPTIONS).asString().result();
            if ("flat".equalsIgnoreCase(dynamic.get("generatorName").asString(""))) {
                String string = optional.orElse("");
                return dynamic.set(GENERATOR_OPTIONS, LevelDataGeneratorOptionsFix.convert(string, dynamic.getOps()));
            }
            if ("buffet".equalsIgnoreCase(dynamic.get("generatorName").asString("")) && optional.isPresent()) {
                JsonElement jsonElement = LenientJsonParser.parse((String)optional.get());
                return dynamic.set(GENERATOR_OPTIONS, new Dynamic((DynamicOps)JsonOps.INSTANCE, (Object)jsonElement).convert(dynamic.getOps()));
            }
            return dynamic;
        }));
    }

    private static <T> Dynamic<T> convert(String string, DynamicOps<T> dynamicOps) {
        String[] stringArray;
        List<Object> list;
        Iterator iterator = Splitter.on((char)';').split((CharSequence)string).iterator();
        String string2 = "minecraft:plains";
        HashMap hashMap = Maps.newHashMap();
        if (!string.isEmpty() && iterator.hasNext()) {
            list = LevelDataGeneratorOptionsFix.getLayersInfoFromString((String)iterator.next());
            if (!list.isEmpty()) {
                if (iterator.hasNext()) {
                    string2 = MAP.getOrDefault(iterator.next(), "minecraft:plains");
                }
                if (iterator.hasNext()) {
                    for (String string3 : stringArray = ((String)iterator.next()).toLowerCase(Locale.ROOT).split(",")) {
                        String[] stringArray2;
                        String[] stringArray3 = string3.split("\\(", 2);
                        if (stringArray3[0].isEmpty()) continue;
                        hashMap.put(stringArray3[0], Maps.newHashMap());
                        if (stringArray3.length <= 1 || !stringArray3[1].endsWith(")") || stringArray3[1].length() <= 1) continue;
                        for (String string4 : stringArray2 = stringArray3[1].substring(0, stringArray3[1].length() - 1).split(" ")) {
                            String[] stringArray4 = string4.split("=", 2);
                            if (stringArray4.length != 2) continue;
                            ((Map)hashMap.get(stringArray3[0])).put(stringArray4[0], stringArray4[1]);
                        }
                    }
                } else {
                    hashMap.put("village", Maps.newHashMap());
                }
            }
        } else {
            list = Lists.newArrayList();
            list.add(Pair.of((Object)1, (Object)"minecraft:bedrock"));
            list.add(Pair.of((Object)2, (Object)"minecraft:dirt"));
            list.add(Pair.of((Object)1, (Object)"minecraft:grass_block"));
            hashMap.put("village", Maps.newHashMap());
        }
        stringArray = dynamicOps.createList(list.stream().map(pair -> dynamicOps.createMap((Map)ImmutableMap.of((Object)dynamicOps.createString("height"), (Object)dynamicOps.createInt(((Integer)pair.getFirst()).intValue()), (Object)dynamicOps.createString("block"), (Object)dynamicOps.createString((String)pair.getSecond())))));
        Object object = dynamicOps.createMap(hashMap.entrySet().stream().map(entry2 -> Pair.of((Object)dynamicOps.createString(((String)entry2.getKey()).toLowerCase(Locale.ROOT)), (Object)dynamicOps.createMap(((Map)entry2.getValue()).entrySet().stream().map(entry -> Pair.of((Object)dynamicOps.createString((String)entry.getKey()), (Object)dynamicOps.createString((String)entry.getValue()))).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond))))).collect(Collectors.toMap(Pair::getFirst, Pair::getSecond)));
        return new Dynamic(dynamicOps, dynamicOps.createMap((Map)ImmutableMap.of((Object)dynamicOps.createString("layers"), (Object)stringArray, (Object)dynamicOps.createString("biome"), (Object)dynamicOps.createString(string2), (Object)dynamicOps.createString("structures"), (Object)object)));
    }

    @Nullable
    private static Pair<Integer, String> getLayerInfoFromString(String string) {
        int n;
        String[] stringArray = string.split("\\*", 2);
        if (stringArray.length == 2) {
            try {
                n = Integer.parseInt(stringArray[0]);
            }
            catch (NumberFormatException numberFormatException) {
                return null;
            }
        } else {
            n = 1;
        }
        String string2 = stringArray[stringArray.length - 1];
        return Pair.of((Object)n, (Object)string2);
    }

    private static List<Pair<Integer, String>> getLayersInfoFromString(String string) {
        String[] stringArray;
        ArrayList arrayList = Lists.newArrayList();
        for (String string2 : stringArray = string.split(",")) {
            Pair<Integer, String> pair = LevelDataGeneratorOptionsFix.getLayerInfoFromString(string2);
            if (pair == null) {
                return Collections.emptyList();
            }
            arrayList.add(pair);
        }
        return arrayList;
    }
}

