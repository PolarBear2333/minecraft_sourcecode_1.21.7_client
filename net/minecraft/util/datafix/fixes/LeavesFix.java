/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Lists
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.types.templates.List$ListType
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 *  it.unimi.dsi.fastutil.ints.Int2IntMap
 *  it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.ints.IntIterator
 *  it.unimi.dsi.fastutil.ints.IntOpenHashSet
 *  it.unimi.dsi.fastutil.ints.IntSet
 *  it.unimi.dsi.fastutil.objects.Object2IntMap
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  javax.annotation.Nullable
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.util.datafix.PackedBitStorage;
import net.minecraft.util.datafix.fixes.References;

public class LeavesFix
extends DataFix {
    private static final int NORTH_WEST_MASK = 128;
    private static final int WEST_MASK = 64;
    private static final int SOUTH_WEST_MASK = 32;
    private static final int SOUTH_MASK = 16;
    private static final int SOUTH_EAST_MASK = 8;
    private static final int EAST_MASK = 4;
    private static final int NORTH_EAST_MASK = 2;
    private static final int NORTH_MASK = 1;
    private static final int[][] DIRECTIONS = new int[][]{{-1, 0, 0}, {1, 0, 0}, {0, -1, 0}, {0, 1, 0}, {0, 0, -1}, {0, 0, 1}};
    private static final int DECAY_DISTANCE = 7;
    private static final int SIZE_BITS = 12;
    private static final int SIZE = 4096;
    static final Object2IntMap<String> LEAVES = (Object2IntMap)DataFixUtils.make((Object)new Object2IntOpenHashMap(), object2IntOpenHashMap -> {
        object2IntOpenHashMap.put((Object)"minecraft:acacia_leaves", 0);
        object2IntOpenHashMap.put((Object)"minecraft:birch_leaves", 1);
        object2IntOpenHashMap.put((Object)"minecraft:dark_oak_leaves", 2);
        object2IntOpenHashMap.put((Object)"minecraft:jungle_leaves", 3);
        object2IntOpenHashMap.put((Object)"minecraft:oak_leaves", 4);
        object2IntOpenHashMap.put((Object)"minecraft:spruce_leaves", 5);
    });
    static final Set<String> LOGS = ImmutableSet.of((Object)"minecraft:acacia_bark", (Object)"minecraft:birch_bark", (Object)"minecraft:dark_oak_bark", (Object)"minecraft:jungle_bark", (Object)"minecraft:oak_bark", (Object)"minecraft:spruce_bark", (Object[])new String[]{"minecraft:acacia_log", "minecraft:birch_log", "minecraft:dark_oak_log", "minecraft:jungle_log", "minecraft:oak_log", "minecraft:spruce_log", "minecraft:stripped_acacia_log", "minecraft:stripped_birch_log", "minecraft:stripped_dark_oak_log", "minecraft:stripped_jungle_log", "minecraft:stripped_oak_log", "minecraft:stripped_spruce_log"});

    public LeavesFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(References.CHUNK);
        OpticFinder opticFinder = type.findField("Level");
        OpticFinder opticFinder2 = opticFinder.type().findField("Sections");
        Type type2 = opticFinder2.type();
        if (!(type2 instanceof List.ListType)) {
            throw new IllegalStateException("Expecting sections to be a list.");
        }
        Type type3 = ((List.ListType)type2).getElement();
        OpticFinder opticFinder3 = DSL.typeFinder((Type)type3);
        return this.fixTypeEverywhereTyped("Leaves fix", type, typed2 -> typed2.updateTyped(opticFinder, typed -> {
            int[] nArray = new int[]{0};
            Typed typed3 = typed.updateTyped(opticFinder2, typed2 -> {
                int n;
                int n2;
                Int2ObjectOpenHashMap int2ObjectOpenHashMap = new Int2ObjectOpenHashMap(typed2.getAllTyped(opticFinder3).stream().map(typed -> new LeavesSection((Typed<?>)typed, this.getInputSchema())).collect(Collectors.toMap(Section::getIndex, leavesSection -> leavesSection)));
                if (int2ObjectOpenHashMap.values().stream().allMatch(Section::isSkippable)) {
                    return typed2;
                }
                ArrayList arrayList = Lists.newArrayList();
                for (int i = 0; i < 7; ++i) {
                    arrayList.add(new IntOpenHashSet());
                }
                for (LeavesSection leavesSection2 : int2ObjectOpenHashMap.values()) {
                    if (leavesSection2.isSkippable()) continue;
                    for (int i = 0; i < 4096; ++i) {
                        int n3 = leavesSection2.getBlock(i);
                        if (leavesSection2.isLog(n3)) {
                            ((IntSet)arrayList.get(0)).add(leavesSection2.getIndex() << 12 | i);
                            continue;
                        }
                        if (!leavesSection2.isLeaf(n3)) continue;
                        n2 = this.getX(i);
                        n = this.getZ(i);
                        nArray[0] = nArray[0] | LeavesFix.getSideMask(n2 == 0, n2 == 15, n == 0, n == 15);
                    }
                }
                for (int i = 1; i < 7; ++i) {
                    LeavesSection leavesSection2;
                    leavesSection2 = (IntSet)arrayList.get(i - 1);
                    IntSet intSet = (IntSet)arrayList.get(i);
                    IntIterator intIterator = leavesSection2.iterator();
                    while (intIterator.hasNext()) {
                        n2 = intIterator.nextInt();
                        n = this.getX(n2);
                        int n4 = this.getY(n2);
                        int n5 = this.getZ(n2);
                        for (int[] nArray2 : DIRECTIONS) {
                            int n6;
                            int n7;
                            int n8;
                            LeavesSection leavesSection3;
                            int n9 = n + nArray2[0];
                            int n10 = n4 + nArray2[1];
                            int n11 = n5 + nArray2[2];
                            if (n9 < 0 || n9 > 15 || n11 < 0 || n11 > 15 || n10 < 0 || n10 > 255 || (leavesSection3 = (LeavesSection)int2ObjectOpenHashMap.get(n10 >> 4)) == null || leavesSection3.isSkippable() || !leavesSection3.isLeaf(n8 = leavesSection3.getBlock(n7 = LeavesFix.getIndex(n9, n10 & 0xF, n11))) || (n6 = leavesSection3.getDistance(n8)) <= i) continue;
                            leavesSection3.setDistance(n7, n8, i);
                            intSet.add(LeavesFix.getIndex(n9, n10, n11));
                        }
                    }
                }
                return typed2.updateTyped(opticFinder3, arg_0 -> LeavesFix.lambda$makeRule$3((Int2ObjectMap)int2ObjectOpenHashMap, arg_0));
            });
            if (nArray[0] != 0) {
                typed3 = typed3.update(DSL.remainderFinder(), dynamic -> {
                    Dynamic dynamic2 = (Dynamic)DataFixUtils.orElse((Optional)dynamic.get("UpgradeData").result(), (Object)dynamic.emptyMap());
                    return dynamic.set("UpgradeData", dynamic2.set("Sides", dynamic.createByte((byte)(dynamic2.get("Sides").asByte((byte)0) | nArray[0]))));
                });
            }
            return typed3;
        }));
    }

    public static int getIndex(int n, int n2, int n3) {
        return n2 << 8 | n3 << 4 | n;
    }

    private int getX(int n) {
        return n & 0xF;
    }

    private int getY(int n) {
        return n >> 8 & 0xFF;
    }

    private int getZ(int n) {
        return n >> 4 & 0xF;
    }

    public static int getSideMask(boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        int n = 0;
        if (bl3) {
            n = bl2 ? (n |= 2) : (bl ? (n |= 0x80) : (n |= 1));
        } else if (bl4) {
            n = bl ? (n |= 0x20) : (bl2 ? (n |= 8) : (n |= 0x10));
        } else if (bl2) {
            n |= 4;
        } else if (bl) {
            n |= 0x40;
        }
        return n;
    }

    private static /* synthetic */ Typed lambda$makeRule$3(Int2ObjectMap int2ObjectMap, Typed typed) {
        return ((LeavesSection)int2ObjectMap.get(((Dynamic)typed.get(DSL.remainderFinder())).get("Y").asInt(0))).write(typed);
    }

    public static final class LeavesSection
    extends Section {
        private static final String PERSISTENT = "persistent";
        private static final String DECAYABLE = "decayable";
        private static final String DISTANCE = "distance";
        @Nullable
        private IntSet leaveIds;
        @Nullable
        private IntSet logIds;
        @Nullable
        private Int2IntMap stateToIdMap;

        public LeavesSection(Typed<?> typed, Schema schema) {
            super(typed, schema);
        }

        @Override
        protected boolean skippable() {
            this.leaveIds = new IntOpenHashSet();
            this.logIds = new IntOpenHashSet();
            this.stateToIdMap = new Int2IntOpenHashMap();
            for (int i = 0; i < this.palette.size(); ++i) {
                Dynamic dynamic = (Dynamic)this.palette.get(i);
                String string = dynamic.get("Name").asString("");
                if (LEAVES.containsKey((Object)string)) {
                    boolean bl = Objects.equals(dynamic.get("Properties").get(DECAYABLE).asString(""), "false");
                    this.leaveIds.add(i);
                    this.stateToIdMap.put(this.getStateId(string, bl, 7), i);
                    this.palette.set(i, this.makeLeafTag(dynamic, string, bl, 7));
                }
                if (!LOGS.contains(string)) continue;
                this.logIds.add(i);
            }
            return this.leaveIds.isEmpty() && this.logIds.isEmpty();
        }

        private Dynamic<?> makeLeafTag(Dynamic<?> dynamic, String string, boolean bl, int n) {
            Dynamic dynamic2 = dynamic.emptyMap();
            dynamic2 = dynamic2.set(PERSISTENT, dynamic2.createString(bl ? "true" : "false"));
            dynamic2 = dynamic2.set(DISTANCE, dynamic2.createString(Integer.toString(n)));
            Dynamic dynamic3 = dynamic.emptyMap();
            dynamic3 = dynamic3.set("Properties", dynamic2);
            dynamic3 = dynamic3.set("Name", dynamic3.createString(string));
            return dynamic3;
        }

        public boolean isLog(int n) {
            return this.logIds.contains(n);
        }

        public boolean isLeaf(int n) {
            return this.leaveIds.contains(n);
        }

        int getDistance(int n) {
            if (this.isLog(n)) {
                return 0;
            }
            return Integer.parseInt(((Dynamic)this.palette.get(n)).get("Properties").get(DISTANCE).asString(""));
        }

        void setDistance(int n, int n2, int n3) {
            int n4;
            boolean bl;
            Dynamic dynamic = (Dynamic)this.palette.get(n2);
            String string = dynamic.get("Name").asString("");
            int n5 = this.getStateId(string, bl = Objects.equals(dynamic.get("Properties").get(PERSISTENT).asString(""), "true"), n3);
            if (!this.stateToIdMap.containsKey(n5)) {
                n4 = this.palette.size();
                this.leaveIds.add(n4);
                this.stateToIdMap.put(n5, n4);
                this.palette.add(this.makeLeafTag(dynamic, string, bl, n3));
            }
            n4 = this.stateToIdMap.get(n5);
            if (1 << this.storage.getBits() <= n4) {
                PackedBitStorage packedBitStorage = new PackedBitStorage(this.storage.getBits() + 1, 4096);
                for (int i = 0; i < 4096; ++i) {
                    packedBitStorage.set(i, this.storage.get(i));
                }
                this.storage = packedBitStorage;
            }
            this.storage.set(n, n4);
        }
    }

    public static abstract class Section {
        protected static final String BLOCK_STATES_TAG = "BlockStates";
        protected static final String NAME_TAG = "Name";
        protected static final String PROPERTIES_TAG = "Properties";
        private final Type<Pair<String, Dynamic<?>>> blockStateType = DSL.named((String)References.BLOCK_STATE.typeName(), (Type)DSL.remainderType());
        protected final OpticFinder<List<Pair<String, Dynamic<?>>>> paletteFinder = DSL.fieldFinder((String)"Palette", (Type)DSL.list(this.blockStateType));
        protected final List<Dynamic<?>> palette;
        protected final int index;
        @Nullable
        protected PackedBitStorage storage;

        public Section(Typed<?> typed, Schema schema) {
            if (!Objects.equals(schema.getType(References.BLOCK_STATE), this.blockStateType)) {
                throw new IllegalStateException("Block state type is not what was expected.");
            }
            Optional optional = typed.getOptional(this.paletteFinder);
            this.palette = optional.map(list -> list.stream().map(Pair::getSecond).collect(Collectors.toList())).orElse((List)ImmutableList.of());
            Dynamic dynamic = (Dynamic)typed.get(DSL.remainderFinder());
            this.index = dynamic.get("Y").asInt(0);
            this.readStorage(dynamic);
        }

        protected void readStorage(Dynamic<?> dynamic) {
            if (this.skippable()) {
                this.storage = null;
            } else {
                long[] lArray = dynamic.get(BLOCK_STATES_TAG).asLongStream().toArray();
                int n = Math.max(4, DataFixUtils.ceillog2((int)this.palette.size()));
                this.storage = new PackedBitStorage(n, 4096, lArray);
            }
        }

        public Typed<?> write(Typed<?> typed) {
            if (this.isSkippable()) {
                return typed;
            }
            return typed.update(DSL.remainderFinder(), dynamic -> dynamic.set(BLOCK_STATES_TAG, dynamic.createLongList(Arrays.stream(this.storage.getRaw())))).set(this.paletteFinder, this.palette.stream().map(dynamic -> Pair.of((Object)References.BLOCK_STATE.typeName(), (Object)dynamic)).collect(Collectors.toList()));
        }

        public boolean isSkippable() {
            return this.storage == null;
        }

        public int getBlock(int n) {
            return this.storage.get(n);
        }

        protected int getStateId(String string, boolean bl, int n) {
            return LEAVES.get((Object)string) << 5 | (bl ? 16 : 0) | n;
        }

        int getIndex() {
            return this.index;
        }

        protected abstract boolean skippable();
    }
}

