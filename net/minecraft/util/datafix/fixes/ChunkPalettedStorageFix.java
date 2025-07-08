/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Dynamic
 *  it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.ints.IntList
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.util.CrudeIncrementalIntIdentityHashBiMap;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.PackedBitStorage;
import net.minecraft.util.datafix.fixes.BlockStateData;
import net.minecraft.util.datafix.fixes.References;
import org.slf4j.Logger;

public class ChunkPalettedStorageFix
extends DataFix {
    private static final int NORTH_WEST_MASK = 128;
    private static final int WEST_MASK = 64;
    private static final int SOUTH_WEST_MASK = 32;
    private static final int SOUTH_MASK = 16;
    private static final int SOUTH_EAST_MASK = 8;
    private static final int EAST_MASK = 4;
    private static final int NORTH_EAST_MASK = 2;
    private static final int NORTH_MASK = 1;
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int SIZE = 4096;

    public ChunkPalettedStorageFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    public static String getName(Dynamic<?> dynamic) {
        return dynamic.get("Name").asString("");
    }

    public static String getProperty(Dynamic<?> dynamic, String string) {
        return dynamic.get("Properties").get(string).asString("");
    }

    public static int idFor(CrudeIncrementalIntIdentityHashBiMap<Dynamic<?>> crudeIncrementalIntIdentityHashBiMap, Dynamic<?> dynamic) {
        int n = crudeIncrementalIntIdentityHashBiMap.getId(dynamic);
        if (n == -1) {
            n = crudeIncrementalIntIdentityHashBiMap.add(dynamic);
        }
        return n;
    }

    private Dynamic<?> fix(Dynamic<?> dynamic) {
        Optional optional = dynamic.get("Level").result();
        if (optional.isPresent() && ((Dynamic)optional.get()).get("Sections").asStreamOpt().result().isPresent()) {
            return dynamic.set("Level", new UpgradeChunk((Dynamic)optional.get()).write());
        }
        return dynamic;
    }

    public TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(References.CHUNK);
        Type type2 = this.getOutputSchema().getType(References.CHUNK);
        return this.writeFixAndRead("ChunkPalettedStorageFix", type, type2, this::fix);
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

    static final class UpgradeChunk {
        private int sides;
        private final Section[] sections;
        private final Dynamic<?> level;
        private final int x;
        private final int z;
        private final Int2ObjectMap<Dynamic<?>> blockEntities;

        /*
         * Exception decompiling
         */
        public UpgradeChunk(Dynamic<?> var1_1) {
            /*
             * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
             * 
             * org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.SwitchStringRewriter$TooOptimisticMatchException
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.SwitchStringRewriter.getString(SwitchStringRewriter.java:404)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.SwitchStringRewriter.access$600(SwitchStringRewriter.java:53)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.SwitchStringRewriter$SwitchStringMatchResultCollector.collectMatches(SwitchStringRewriter.java:368)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.ResetAfterTest.match(ResetAfterTest.java:24)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.KleeneN.match(KleeneN.java:24)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.MatchSequence.match(MatchSequence.java:26)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.matchutil.ResetAfterTest.match(ResetAfterTest.java:23)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.SwitchStringRewriter.rewriteComplex(SwitchStringRewriter.java:201)
             *     at org.benf.cfr.reader.bytecode.analysis.opgraph.op4rewriters.SwitchStringRewriter.rewrite(SwitchStringRewriter.java:73)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:881)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
             *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
             *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
             *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseInnerClassesPass1(ClassFile.java:923)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1035)
             *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
             *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
             *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
             *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
             *     at org.benf.cfr.reader.Main.main(Main.java:54)
             */
            throw new IllegalStateException("Decompilation failed");
        }

        @Nullable
        private Dynamic<?> getBlockEntity(int n) {
            return (Dynamic)this.blockEntities.get(n);
        }

        @Nullable
        private Dynamic<?> removeBlockEntity(int n) {
            return (Dynamic)this.blockEntities.remove(n);
        }

        public static int relative(int n, Direction direction) {
            return switch (direction.getAxis().ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> {
                    int var2_2 = (n & 0xF) + direction.getAxisDirection().getStep();
                    if (var2_2 < 0 || var2_2 > 15) {
                        yield -1;
                    }
                    yield n & 0xFFFFFFF0 | var2_2;
                }
                case 1 -> {
                    int var2_3 = (n >> 8) + direction.getAxisDirection().getStep();
                    if (var2_3 < 0 || var2_3 > 255) {
                        yield -1;
                    }
                    yield n & 0xFF | var2_3 << 8;
                }
                case 2 -> {
                    int var2_4 = (n >> 4 & 0xF) + direction.getAxisDirection().getStep();
                    if (var2_4 < 0 || var2_4 > 15) {
                        yield -1;
                    }
                    yield n & 0xFFFFFF0F | var2_4 << 4;
                }
            };
        }

        private void setBlock(int n, Dynamic<?> dynamic) {
            if (n < 0 || n > 65535) {
                return;
            }
            Section section = this.getSection(n);
            if (section == null) {
                return;
            }
            section.setBlock(n & 0xFFF, dynamic);
        }

        @Nullable
        private Section getSection(int n) {
            int n2 = n >> 12;
            return n2 < this.sections.length ? this.sections[n2] : null;
        }

        public Dynamic<?> getBlock(int n) {
            if (n < 0 || n > 65535) {
                return MappingConstants.AIR;
            }
            Section section = this.getSection(n);
            if (section == null) {
                return MappingConstants.AIR;
            }
            return section.getBlock(n & 0xFFF);
        }

        public Dynamic<?> write() {
            Dynamic dynamic = this.level;
            dynamic = this.blockEntities.isEmpty() ? dynamic.remove("TileEntities") : dynamic.set("TileEntities", dynamic.createList(this.blockEntities.values().stream()));
            Dynamic dynamic2 = dynamic.emptyMap();
            ArrayList arrayList = Lists.newArrayList();
            for (Section section : this.sections) {
                if (section == null) continue;
                arrayList.add(section.write());
                dynamic2 = dynamic2.set(String.valueOf(section.y), dynamic2.createIntList(Arrays.stream(section.update.toIntArray())));
            }
            Dynamic dynamic3 = dynamic.emptyMap();
            dynamic3 = dynamic3.set("Sides", dynamic3.createByte((byte)this.sides));
            dynamic3 = dynamic3.set("Indices", dynamic2);
            return dynamic.set("UpgradeData", dynamic3).set("Sections", dynamic3.createList(arrayList.stream()));
        }

        private /* synthetic */ void lambda$new$3(Stream stream) {
            stream.forEach(dynamic -> {
                Section section = new Section((Dynamic<?>)dynamic);
                this.sides = section.upgrade(this.sides);
                this.sections[section.y] = section;
            });
        }

        private /* synthetic */ void lambda$new$1(Stream stream) {
            stream.forEach(dynamic -> {
                int n;
                int n2 = dynamic.get("x").asInt(0) - this.x & 0xF;
                int n3 = dynamic.get("y").asInt(0);
                int n4 = n3 << 8 | (n = dynamic.get("z").asInt(0) - this.z & 0xF) << 4 | n2;
                if (this.blockEntities.put(n4, dynamic) != null) {
                    LOGGER.warn("In chunk: {}x{} found a duplicate block entity at position: [{}, {}, {}]", new Object[]{this.x, this.z, n2, n3, n});
                }
            });
        }
    }

    public static enum Direction {
        DOWN(AxisDirection.NEGATIVE, Axis.Y),
        UP(AxisDirection.POSITIVE, Axis.Y),
        NORTH(AxisDirection.NEGATIVE, Axis.Z),
        SOUTH(AxisDirection.POSITIVE, Axis.Z),
        WEST(AxisDirection.NEGATIVE, Axis.X),
        EAST(AxisDirection.POSITIVE, Axis.X);

        private final Axis axis;
        private final AxisDirection axisDirection;

        private Direction(AxisDirection axisDirection, Axis axis) {
            this.axis = axis;
            this.axisDirection = axisDirection;
        }

        public AxisDirection getAxisDirection() {
            return this.axisDirection;
        }

        public Axis getAxis() {
            return this.axis;
        }

        public static enum Axis {
            X,
            Y,
            Z;

        }

        public static enum AxisDirection {
            POSITIVE(1),
            NEGATIVE(-1);

            private final int step;

            private AxisDirection(int n2) {
                this.step = n2;
            }

            public int getStep() {
                return this.step;
            }
        }
    }

    static class DataLayer {
        private static final int SIZE = 2048;
        private static final int NIBBLE_SIZE = 4;
        private final byte[] data;

        public DataLayer() {
            this.data = new byte[2048];
        }

        public DataLayer(byte[] byArray) {
            this.data = byArray;
            if (byArray.length != 2048) {
                throw new IllegalArgumentException("ChunkNibbleArrays should be 2048 bytes not: " + byArray.length);
            }
        }

        public int get(int n, int n2, int n3) {
            int n4 = this.getPosition(n2 << 8 | n3 << 4 | n);
            if (this.isFirst(n2 << 8 | n3 << 4 | n)) {
                return this.data[n4] & 0xF;
            }
            return this.data[n4] >> 4 & 0xF;
        }

        private boolean isFirst(int n) {
            return (n & 1) == 0;
        }

        private int getPosition(int n) {
            return n >> 1;
        }
    }

    static class Section {
        private final CrudeIncrementalIntIdentityHashBiMap<Dynamic<?>> palette = CrudeIncrementalIntIdentityHashBiMap.create(32);
        private final List<Dynamic<?>> listTag;
        private final Dynamic<?> section;
        private final boolean hasData;
        final Int2ObjectMap<IntList> toFix = new Int2ObjectLinkedOpenHashMap();
        final IntList update = new IntArrayList();
        public final int y;
        private final Set<Dynamic<?>> seen = Sets.newIdentityHashSet();
        private final int[] buffer = new int[4096];

        public Section(Dynamic<?> dynamic) {
            this.listTag = Lists.newArrayList();
            this.section = dynamic;
            this.y = dynamic.get("Y").asInt(0);
            this.hasData = dynamic.get("Blocks").result().isPresent();
        }

        public Dynamic<?> getBlock(int n) {
            if (n < 0 || n > 4095) {
                return MappingConstants.AIR;
            }
            Dynamic<?> dynamic = this.palette.byId(this.buffer[n]);
            return dynamic == null ? MappingConstants.AIR : dynamic;
        }

        public void setBlock(int n, Dynamic<?> dynamic) {
            if (this.seen.add(dynamic)) {
                this.listTag.add("%%FILTER_ME%%".equals(ChunkPalettedStorageFix.getName(dynamic)) ? MappingConstants.AIR : dynamic);
            }
            this.buffer[n] = ChunkPalettedStorageFix.idFor(this.palette, dynamic);
        }

        public int upgrade(int n) {
            if (!this.hasData) {
                return n;
            }
            ByteBuffer byteBuffer2 = (ByteBuffer)this.section.get("Blocks").asByteBufferOpt().result().get();
            DataLayer dataLayer = this.section.get("Data").asByteBufferOpt().map(byteBuffer -> new DataLayer(DataFixUtils.toArray((ByteBuffer)byteBuffer))).result().orElseGet(DataLayer::new);
            DataLayer dataLayer2 = this.section.get("Add").asByteBufferOpt().map(byteBuffer -> new DataLayer(DataFixUtils.toArray((ByteBuffer)byteBuffer))).result().orElseGet(DataLayer::new);
            this.seen.add(MappingConstants.AIR);
            ChunkPalettedStorageFix.idFor(this.palette, MappingConstants.AIR);
            this.listTag.add(MappingConstants.AIR);
            for (int i = 0; i < 4096; ++i) {
                int n2 = i & 0xF;
                int n3 = i >> 8 & 0xF;
                int n4 = i >> 4 & 0xF;
                int n5 = dataLayer2.get(n2, n3, n4) << 12 | (byteBuffer2.get(i) & 0xFF) << 4 | dataLayer.get(n2, n3, n4);
                if (MappingConstants.FIX.get(n5 >> 4)) {
                    this.addFix(n5 >> 4, i);
                }
                if (MappingConstants.VIRTUAL.get(n5 >> 4)) {
                    int n6 = ChunkPalettedStorageFix.getSideMask(n2 == 0, n2 == 15, n4 == 0, n4 == 15);
                    if (n6 == 0) {
                        this.update.add(i);
                    } else {
                        n |= n6;
                    }
                }
                this.setBlock(i, BlockStateData.getTag(n5));
            }
            return n;
        }

        private void addFix(int n, int n2) {
            IntList intList = (IntList)this.toFix.get(n);
            if (intList == null) {
                intList = new IntArrayList();
                this.toFix.put(n, (Object)intList);
            }
            intList.add(n2);
        }

        public Dynamic<?> write() {
            Dynamic dynamic = this.section;
            if (!this.hasData) {
                return dynamic;
            }
            dynamic = dynamic.set("Palette", dynamic.createList(this.listTag.stream()));
            int n = Math.max(4, DataFixUtils.ceillog2((int)this.seen.size()));
            PackedBitStorage packedBitStorage = new PackedBitStorage(n, 4096);
            for (int i = 0; i < this.buffer.length; ++i) {
                packedBitStorage.set(i, this.buffer[i]);
            }
            dynamic = dynamic.set("BlockStates", dynamic.createLongList(Arrays.stream(packedBitStorage.getRaw())));
            dynamic = dynamic.remove("Blocks");
            dynamic = dynamic.remove("Data");
            dynamic = dynamic.remove("Add");
            return dynamic;
        }
    }

    static class MappingConstants {
        static final BitSet VIRTUAL = new BitSet(256);
        static final BitSet FIX = new BitSet(256);
        static final Dynamic<?> PUMPKIN = ExtraDataFixUtils.blockState("minecraft:pumpkin");
        static final Dynamic<?> SNOWY_PODZOL = ExtraDataFixUtils.blockState("minecraft:podzol", Map.of("snowy", "true"));
        static final Dynamic<?> SNOWY_GRASS = ExtraDataFixUtils.blockState("minecraft:grass_block", Map.of("snowy", "true"));
        static final Dynamic<?> SNOWY_MYCELIUM = ExtraDataFixUtils.blockState("minecraft:mycelium", Map.of("snowy", "true"));
        static final Dynamic<?> UPPER_SUNFLOWER = ExtraDataFixUtils.blockState("minecraft:sunflower", Map.of("half", "upper"));
        static final Dynamic<?> UPPER_LILAC = ExtraDataFixUtils.blockState("minecraft:lilac", Map.of("half", "upper"));
        static final Dynamic<?> UPPER_TALL_GRASS = ExtraDataFixUtils.blockState("minecraft:tall_grass", Map.of("half", "upper"));
        static final Dynamic<?> UPPER_LARGE_FERN = ExtraDataFixUtils.blockState("minecraft:large_fern", Map.of("half", "upper"));
        static final Dynamic<?> UPPER_ROSE_BUSH = ExtraDataFixUtils.blockState("minecraft:rose_bush", Map.of("half", "upper"));
        static final Dynamic<?> UPPER_PEONY = ExtraDataFixUtils.blockState("minecraft:peony", Map.of("half", "upper"));
        static final Map<String, Dynamic<?>> FLOWER_POT_MAP = (Map)DataFixUtils.make((Object)Maps.newHashMap(), hashMap -> {
            hashMap.put("minecraft:air0", ExtraDataFixUtils.blockState("minecraft:flower_pot"));
            hashMap.put("minecraft:red_flower0", ExtraDataFixUtils.blockState("minecraft:potted_poppy"));
            hashMap.put("minecraft:red_flower1", ExtraDataFixUtils.blockState("minecraft:potted_blue_orchid"));
            hashMap.put("minecraft:red_flower2", ExtraDataFixUtils.blockState("minecraft:potted_allium"));
            hashMap.put("minecraft:red_flower3", ExtraDataFixUtils.blockState("minecraft:potted_azure_bluet"));
            hashMap.put("minecraft:red_flower4", ExtraDataFixUtils.blockState("minecraft:potted_red_tulip"));
            hashMap.put("minecraft:red_flower5", ExtraDataFixUtils.blockState("minecraft:potted_orange_tulip"));
            hashMap.put("minecraft:red_flower6", ExtraDataFixUtils.blockState("minecraft:potted_white_tulip"));
            hashMap.put("minecraft:red_flower7", ExtraDataFixUtils.blockState("minecraft:potted_pink_tulip"));
            hashMap.put("minecraft:red_flower8", ExtraDataFixUtils.blockState("minecraft:potted_oxeye_daisy"));
            hashMap.put("minecraft:yellow_flower0", ExtraDataFixUtils.blockState("minecraft:potted_dandelion"));
            hashMap.put("minecraft:sapling0", ExtraDataFixUtils.blockState("minecraft:potted_oak_sapling"));
            hashMap.put("minecraft:sapling1", ExtraDataFixUtils.blockState("minecraft:potted_spruce_sapling"));
            hashMap.put("minecraft:sapling2", ExtraDataFixUtils.blockState("minecraft:potted_birch_sapling"));
            hashMap.put("minecraft:sapling3", ExtraDataFixUtils.blockState("minecraft:potted_jungle_sapling"));
            hashMap.put("minecraft:sapling4", ExtraDataFixUtils.blockState("minecraft:potted_acacia_sapling"));
            hashMap.put("minecraft:sapling5", ExtraDataFixUtils.blockState("minecraft:potted_dark_oak_sapling"));
            hashMap.put("minecraft:red_mushroom0", ExtraDataFixUtils.blockState("minecraft:potted_red_mushroom"));
            hashMap.put("minecraft:brown_mushroom0", ExtraDataFixUtils.blockState("minecraft:potted_brown_mushroom"));
            hashMap.put("minecraft:deadbush0", ExtraDataFixUtils.blockState("minecraft:potted_dead_bush"));
            hashMap.put("minecraft:tallgrass2", ExtraDataFixUtils.blockState("minecraft:potted_fern"));
            hashMap.put("minecraft:cactus0", ExtraDataFixUtils.blockState("minecraft:potted_cactus"));
        });
        static final Map<String, Dynamic<?>> SKULL_MAP = (Map)DataFixUtils.make((Object)Maps.newHashMap(), hashMap -> {
            MappingConstants.mapSkull(hashMap, 0, "skeleton", "skull");
            MappingConstants.mapSkull(hashMap, 1, "wither_skeleton", "skull");
            MappingConstants.mapSkull(hashMap, 2, "zombie", "head");
            MappingConstants.mapSkull(hashMap, 3, "player", "head");
            MappingConstants.mapSkull(hashMap, 4, "creeper", "head");
            MappingConstants.mapSkull(hashMap, 5, "dragon", "head");
        });
        static final Map<String, Dynamic<?>> DOOR_MAP = (Map)DataFixUtils.make((Object)Maps.newHashMap(), hashMap -> {
            MappingConstants.mapDoor(hashMap, "oak_door");
            MappingConstants.mapDoor(hashMap, "iron_door");
            MappingConstants.mapDoor(hashMap, "spruce_door");
            MappingConstants.mapDoor(hashMap, "birch_door");
            MappingConstants.mapDoor(hashMap, "jungle_door");
            MappingConstants.mapDoor(hashMap, "acacia_door");
            MappingConstants.mapDoor(hashMap, "dark_oak_door");
        });
        static final Map<String, Dynamic<?>> NOTE_BLOCK_MAP = (Map)DataFixUtils.make((Object)Maps.newHashMap(), hashMap -> {
            for (int i = 0; i < 26; ++i) {
                hashMap.put("true" + i, ExtraDataFixUtils.blockState("minecraft:note_block", Map.of("powered", "true", "note", String.valueOf(i))));
                hashMap.put("false" + i, ExtraDataFixUtils.blockState("minecraft:note_block", Map.of("powered", "false", "note", String.valueOf(i))));
            }
        });
        private static final Int2ObjectMap<String> DYE_COLOR_MAP = (Int2ObjectMap)DataFixUtils.make((Object)new Int2ObjectOpenHashMap(), int2ObjectOpenHashMap -> {
            int2ObjectOpenHashMap.put(0, (Object)"white");
            int2ObjectOpenHashMap.put(1, (Object)"orange");
            int2ObjectOpenHashMap.put(2, (Object)"magenta");
            int2ObjectOpenHashMap.put(3, (Object)"light_blue");
            int2ObjectOpenHashMap.put(4, (Object)"yellow");
            int2ObjectOpenHashMap.put(5, (Object)"lime");
            int2ObjectOpenHashMap.put(6, (Object)"pink");
            int2ObjectOpenHashMap.put(7, (Object)"gray");
            int2ObjectOpenHashMap.put(8, (Object)"light_gray");
            int2ObjectOpenHashMap.put(9, (Object)"cyan");
            int2ObjectOpenHashMap.put(10, (Object)"purple");
            int2ObjectOpenHashMap.put(11, (Object)"blue");
            int2ObjectOpenHashMap.put(12, (Object)"brown");
            int2ObjectOpenHashMap.put(13, (Object)"green");
            int2ObjectOpenHashMap.put(14, (Object)"red");
            int2ObjectOpenHashMap.put(15, (Object)"black");
        });
        static final Map<String, Dynamic<?>> BED_BLOCK_MAP = (Map)DataFixUtils.make((Object)Maps.newHashMap(), hashMap -> {
            for (Int2ObjectMap.Entry entry : DYE_COLOR_MAP.int2ObjectEntrySet()) {
                if (Objects.equals(entry.getValue(), "red")) continue;
                MappingConstants.addBeds(hashMap, entry.getIntKey(), (String)entry.getValue());
            }
        });
        static final Map<String, Dynamic<?>> BANNER_BLOCK_MAP = (Map)DataFixUtils.make((Object)Maps.newHashMap(), hashMap -> {
            for (Int2ObjectMap.Entry entry : DYE_COLOR_MAP.int2ObjectEntrySet()) {
                if (Objects.equals(entry.getValue(), "white")) continue;
                MappingConstants.addBanners(hashMap, 15 - entry.getIntKey(), (String)entry.getValue());
            }
        });
        static final Dynamic<?> AIR;

        private MappingConstants() {
        }

        private static void mapSkull(Map<String, Dynamic<?>> map, int n, String string, String string2) {
            map.put(n + "north", ExtraDataFixUtils.blockState("minecraft:" + string + "_wall_" + string2, Map.of("facing", "north")));
            map.put(n + "east", ExtraDataFixUtils.blockState("minecraft:" + string + "_wall_" + string2, Map.of("facing", "east")));
            map.put(n + "south", ExtraDataFixUtils.blockState("minecraft:" + string + "_wall_" + string2, Map.of("facing", "south")));
            map.put(n + "west", ExtraDataFixUtils.blockState("minecraft:" + string + "_wall_" + string2, Map.of("facing", "west")));
            for (int i = 0; i < 16; ++i) {
                map.put("" + n + i, ExtraDataFixUtils.blockState("minecraft:" + string + "_" + string2, Map.of("rotation", String.valueOf(i))));
            }
        }

        private static void mapDoor(Map<String, Dynamic<?>> map, String string) {
            String string2 = "minecraft:" + string;
            map.put("minecraft:" + string + "eastlowerleftfalsefalse", ExtraDataFixUtils.blockState(string2, Map.of("facing", "east", "half", "lower", "hinge", "left", "open", "false", "powered", "false")));
            map.put("minecraft:" + string + "eastlowerleftfalsetrue", ExtraDataFixUtils.blockState(string2, Map.of("facing", "east", "half", "lower", "hinge", "left", "open", "false", "powered", "true")));
            map.put("minecraft:" + string + "eastlowerlefttruefalse", ExtraDataFixUtils.blockState(string2, Map.of("facing", "east", "half", "lower", "hinge", "left", "open", "true", "powered", "false")));
            map.put("minecraft:" + string + "eastlowerlefttruetrue", ExtraDataFixUtils.blockState(string2, Map.of("facing", "east", "half", "lower", "hinge", "left", "open", "true", "powered", "true")));
            map.put("minecraft:" + string + "eastlowerrightfalsefalse", ExtraDataFixUtils.blockState(string2, Map.of("facing", "east", "half", "lower", "hinge", "right", "open", "false", "powered", "false")));
            map.put("minecraft:" + string + "eastlowerrightfalsetrue", ExtraDataFixUtils.blockState(string2, Map.of("facing", "east", "half", "lower", "hinge", "right", "open", "false", "powered", "true")));
            map.put("minecraft:" + string + "eastlowerrighttruefalse", ExtraDataFixUtils.blockState(string2, Map.of("facing", "east", "half", "lower", "hinge", "right", "open", "true", "powered", "false")));
            map.put("minecraft:" + string + "eastlowerrighttruetrue", ExtraDataFixUtils.blockState(string2, Map.of("facing", "east", "half", "lower", "hinge", "right", "open", "true", "powered", "true")));
            map.put("minecraft:" + string + "eastupperleftfalsefalse", ExtraDataFixUtils.blockState(string2, Map.of("facing", "east", "half", "upper", "hinge", "left", "open", "false", "powered", "false")));
            map.put("minecraft:" + string + "eastupperleftfalsetrue", ExtraDataFixUtils.blockState(string2, Map.of("facing", "east", "half", "upper", "hinge", "left", "open", "false", "powered", "true")));
            map.put("minecraft:" + string + "eastupperlefttruefalse", ExtraDataFixUtils.blockState(string2, Map.of("facing", "east", "half", "upper", "hinge", "left", "open", "true", "powered", "false")));
            map.put("minecraft:" + string + "eastupperlefttruetrue", ExtraDataFixUtils.blockState(string2, Map.of("facing", "east", "half", "upper", "hinge", "left", "open", "true", "powered", "true")));
            map.put("minecraft:" + string + "eastupperrightfalsefalse", ExtraDataFixUtils.blockState(string2, Map.of("facing", "east", "half", "upper", "hinge", "right", "open", "false", "powered", "false")));
            map.put("minecraft:" + string + "eastupperrightfalsetrue", ExtraDataFixUtils.blockState(string2, Map.of("facing", "east", "half", "upper", "hinge", "right", "open", "false", "powered", "true")));
            map.put("minecraft:" + string + "eastupperrighttruefalse", ExtraDataFixUtils.blockState(string2, Map.of("facing", "east", "half", "upper", "hinge", "right", "open", "true", "powered", "false")));
            map.put("minecraft:" + string + "eastupperrighttruetrue", ExtraDataFixUtils.blockState(string2, Map.of("facing", "east", "half", "upper", "hinge", "right", "open", "true", "powered", "true")));
            map.put("minecraft:" + string + "northlowerleftfalsefalse", ExtraDataFixUtils.blockState(string2, Map.of("facing", "north", "half", "lower", "hinge", "left", "open", "false", "powered", "false")));
            map.put("minecraft:" + string + "northlowerleftfalsetrue", ExtraDataFixUtils.blockState(string2, Map.of("facing", "north", "half", "lower", "hinge", "left", "open", "false", "powered", "true")));
            map.put("minecraft:" + string + "northlowerlefttruefalse", ExtraDataFixUtils.blockState(string2, Map.of("facing", "north", "half", "lower", "hinge", "left", "open", "true", "powered", "false")));
            map.put("minecraft:" + string + "northlowerlefttruetrue", ExtraDataFixUtils.blockState(string2, Map.of("facing", "north", "half", "lower", "hinge", "left", "open", "true", "powered", "true")));
            map.put("minecraft:" + string + "northlowerrightfalsefalse", ExtraDataFixUtils.blockState(string2, Map.of("facing", "north", "half", "lower", "hinge", "right", "open", "false", "powered", "false")));
            map.put("minecraft:" + string + "northlowerrightfalsetrue", ExtraDataFixUtils.blockState(string2, Map.of("facing", "north", "half", "lower", "hinge", "right", "open", "false", "powered", "true")));
            map.put("minecraft:" + string + "northlowerrighttruefalse", ExtraDataFixUtils.blockState(string2, Map.of("facing", "north", "half", "lower", "hinge", "right", "open", "true", "powered", "false")));
            map.put("minecraft:" + string + "northlowerrighttruetrue", ExtraDataFixUtils.blockState(string2, Map.of("facing", "north", "half", "lower", "hinge", "right", "open", "true", "powered", "true")));
            map.put("minecraft:" + string + "northupperleftfalsefalse", ExtraDataFixUtils.blockState(string2, Map.of("facing", "north", "half", "upper", "hinge", "left", "open", "false", "powered", "false")));
            map.put("minecraft:" + string + "northupperleftfalsetrue", ExtraDataFixUtils.blockState(string2, Map.of("facing", "north", "half", "upper", "hinge", "left", "open", "false", "powered", "true")));
            map.put("minecraft:" + string + "northupperlefttruefalse", ExtraDataFixUtils.blockState(string2, Map.of("facing", "north", "half", "upper", "hinge", "left", "open", "true", "powered", "false")));
            map.put("minecraft:" + string + "northupperlefttruetrue", ExtraDataFixUtils.blockState(string2, Map.of("facing", "north", "half", "upper", "hinge", "left", "open", "true", "powered", "true")));
            map.put("minecraft:" + string + "northupperrightfalsefalse", ExtraDataFixUtils.blockState(string2, Map.of("facing", "north", "half", "upper", "hinge", "right", "open", "false", "powered", "false")));
            map.put("minecraft:" + string + "northupperrightfalsetrue", ExtraDataFixUtils.blockState(string2, Map.of("facing", "north", "half", "upper", "hinge", "right", "open", "false", "powered", "true")));
            map.put("minecraft:" + string + "northupperrighttruefalse", ExtraDataFixUtils.blockState(string2, Map.of("facing", "north", "half", "upper", "hinge", "right", "open", "true", "powered", "false")));
            map.put("minecraft:" + string + "northupperrighttruetrue", ExtraDataFixUtils.blockState(string2, Map.of("facing", "north", "half", "upper", "hinge", "right", "open", "true", "powered", "true")));
            map.put("minecraft:" + string + "southlowerleftfalsefalse", ExtraDataFixUtils.blockState(string2, Map.of("facing", "south", "half", "lower", "hinge", "left", "open", "false", "powered", "false")));
            map.put("minecraft:" + string + "southlowerleftfalsetrue", ExtraDataFixUtils.blockState(string2, Map.of("facing", "south", "half", "lower", "hinge", "left", "open", "false", "powered", "true")));
            map.put("minecraft:" + string + "southlowerlefttruefalse", ExtraDataFixUtils.blockState(string2, Map.of("facing", "south", "half", "lower", "hinge", "left", "open", "true", "powered", "false")));
            map.put("minecraft:" + string + "southlowerlefttruetrue", ExtraDataFixUtils.blockState(string2, Map.of("facing", "south", "half", "lower", "hinge", "left", "open", "true", "powered", "true")));
            map.put("minecraft:" + string + "southlowerrightfalsefalse", ExtraDataFixUtils.blockState(string2, Map.of("facing", "south", "half", "lower", "hinge", "right", "open", "false", "powered", "false")));
            map.put("minecraft:" + string + "southlowerrightfalsetrue", ExtraDataFixUtils.blockState(string2, Map.of("facing", "south", "half", "lower", "hinge", "right", "open", "false", "powered", "true")));
            map.put("minecraft:" + string + "southlowerrighttruefalse", ExtraDataFixUtils.blockState(string2, Map.of("facing", "south", "half", "lower", "hinge", "right", "open", "true", "powered", "false")));
            map.put("minecraft:" + string + "southlowerrighttruetrue", ExtraDataFixUtils.blockState(string2, Map.of("facing", "south", "half", "lower", "hinge", "right", "open", "true", "powered", "true")));
            map.put("minecraft:" + string + "southupperleftfalsefalse", ExtraDataFixUtils.blockState(string2, Map.of("facing", "south", "half", "upper", "hinge", "left", "open", "false", "powered", "false")));
            map.put("minecraft:" + string + "southupperleftfalsetrue", ExtraDataFixUtils.blockState(string2, Map.of("facing", "south", "half", "upper", "hinge", "left", "open", "false", "powered", "true")));
            map.put("minecraft:" + string + "southupperlefttruefalse", ExtraDataFixUtils.blockState(string2, Map.of("facing", "south", "half", "upper", "hinge", "left", "open", "true", "powered", "false")));
            map.put("minecraft:" + string + "southupperlefttruetrue", ExtraDataFixUtils.blockState(string2, Map.of("facing", "south", "half", "upper", "hinge", "left", "open", "true", "powered", "true")));
            map.put("minecraft:" + string + "southupperrightfalsefalse", ExtraDataFixUtils.blockState(string2, Map.of("facing", "south", "half", "upper", "hinge", "right", "open", "false", "powered", "false")));
            map.put("minecraft:" + string + "southupperrightfalsetrue", ExtraDataFixUtils.blockState(string2, Map.of("facing", "south", "half", "upper", "hinge", "right", "open", "false", "powered", "true")));
            map.put("minecraft:" + string + "southupperrighttruefalse", ExtraDataFixUtils.blockState(string2, Map.of("facing", "south", "half", "upper", "hinge", "right", "open", "true", "powered", "false")));
            map.put("minecraft:" + string + "southupperrighttruetrue", ExtraDataFixUtils.blockState(string2, Map.of("facing", "south", "half", "upper", "hinge", "right", "open", "true", "powered", "true")));
            map.put("minecraft:" + string + "westlowerleftfalsefalse", ExtraDataFixUtils.blockState(string2, Map.of("facing", "west", "half", "lower", "hinge", "left", "open", "false", "powered", "false")));
            map.put("minecraft:" + string + "westlowerleftfalsetrue", ExtraDataFixUtils.blockState(string2, Map.of("facing", "west", "half", "lower", "hinge", "left", "open", "false", "powered", "true")));
            map.put("minecraft:" + string + "westlowerlefttruefalse", ExtraDataFixUtils.blockState(string2, Map.of("facing", "west", "half", "lower", "hinge", "left", "open", "true", "powered", "false")));
            map.put("minecraft:" + string + "westlowerlefttruetrue", ExtraDataFixUtils.blockState(string2, Map.of("facing", "west", "half", "lower", "hinge", "left", "open", "true", "powered", "true")));
            map.put("minecraft:" + string + "westlowerrightfalsefalse", ExtraDataFixUtils.blockState(string2, Map.of("facing", "west", "half", "lower", "hinge", "right", "open", "false", "powered", "false")));
            map.put("minecraft:" + string + "westlowerrightfalsetrue", ExtraDataFixUtils.blockState(string2, Map.of("facing", "west", "half", "lower", "hinge", "right", "open", "false", "powered", "true")));
            map.put("minecraft:" + string + "westlowerrighttruefalse", ExtraDataFixUtils.blockState(string2, Map.of("facing", "west", "half", "lower", "hinge", "right", "open", "true", "powered", "false")));
            map.put("minecraft:" + string + "westlowerrighttruetrue", ExtraDataFixUtils.blockState(string2, Map.of("facing", "west", "half", "lower", "hinge", "right", "open", "true", "powered", "true")));
            map.put("minecraft:" + string + "westupperleftfalsefalse", ExtraDataFixUtils.blockState(string2, Map.of("facing", "west", "half", "upper", "hinge", "left", "open", "false", "powered", "false")));
            map.put("minecraft:" + string + "westupperleftfalsetrue", ExtraDataFixUtils.blockState(string2, Map.of("facing", "west", "half", "upper", "hinge", "left", "open", "false", "powered", "true")));
            map.put("minecraft:" + string + "westupperlefttruefalse", ExtraDataFixUtils.blockState(string2, Map.of("facing", "west", "half", "upper", "hinge", "left", "open", "true", "powered", "false")));
            map.put("minecraft:" + string + "westupperlefttruetrue", ExtraDataFixUtils.blockState(string2, Map.of("facing", "west", "half", "upper", "hinge", "left", "open", "true", "powered", "true")));
            map.put("minecraft:" + string + "westupperrightfalsefalse", ExtraDataFixUtils.blockState(string2, Map.of("facing", "west", "half", "upper", "hinge", "right", "open", "false", "powered", "false")));
            map.put("minecraft:" + string + "westupperrightfalsetrue", ExtraDataFixUtils.blockState(string2, Map.of("facing", "west", "half", "upper", "hinge", "right", "open", "false", "powered", "true")));
            map.put("minecraft:" + string + "westupperrighttruefalse", ExtraDataFixUtils.blockState(string2, Map.of("facing", "west", "half", "upper", "hinge", "right", "open", "true", "powered", "false")));
            map.put("minecraft:" + string + "westupperrighttruetrue", ExtraDataFixUtils.blockState(string2, Map.of("facing", "west", "half", "upper", "hinge", "right", "open", "true", "powered", "true")));
        }

        private static void addBeds(Map<String, Dynamic<?>> map, int n, String string) {
            map.put("southfalsefoot" + n, ExtraDataFixUtils.blockState("minecraft:" + string + "_bed", Map.of("facing", "south", "occupied", "false", "part", "foot")));
            map.put("westfalsefoot" + n, ExtraDataFixUtils.blockState("minecraft:" + string + "_bed", Map.of("facing", "west", "occupied", "false", "part", "foot")));
            map.put("northfalsefoot" + n, ExtraDataFixUtils.blockState("minecraft:" + string + "_bed", Map.of("facing", "north", "occupied", "false", "part", "foot")));
            map.put("eastfalsefoot" + n, ExtraDataFixUtils.blockState("minecraft:" + string + "_bed", Map.of("facing", "east", "occupied", "false", "part", "foot")));
            map.put("southfalsehead" + n, ExtraDataFixUtils.blockState("minecraft:" + string + "_bed", Map.of("facing", "south", "occupied", "false", "part", "head")));
            map.put("westfalsehead" + n, ExtraDataFixUtils.blockState("minecraft:" + string + "_bed", Map.of("facing", "west", "occupied", "false", "part", "head")));
            map.put("northfalsehead" + n, ExtraDataFixUtils.blockState("minecraft:" + string + "_bed", Map.of("facing", "north", "occupied", "false", "part", "head")));
            map.put("eastfalsehead" + n, ExtraDataFixUtils.blockState("minecraft:" + string + "_bed", Map.of("facing", "east", "occupied", "false", "part", "head")));
            map.put("southtruehead" + n, ExtraDataFixUtils.blockState("minecraft:" + string + "_bed", Map.of("facing", "south", "occupied", "true", "part", "head")));
            map.put("westtruehead" + n, ExtraDataFixUtils.blockState("minecraft:" + string + "_bed", Map.of("facing", "west", "occupied", "true", "part", "head")));
            map.put("northtruehead" + n, ExtraDataFixUtils.blockState("minecraft:" + string + "_bed", Map.of("facing", "north", "occupied", "true", "part", "head")));
            map.put("easttruehead" + n, ExtraDataFixUtils.blockState("minecraft:" + string + "_bed", Map.of("facing", "east", "occupied", "true", "part", "head")));
        }

        private static void addBanners(Map<String, Dynamic<?>> map, int n, String string) {
            for (int i = 0; i < 16; ++i) {
                map.put(i + "_" + n, ExtraDataFixUtils.blockState("minecraft:" + string + "_banner", Map.of("rotation", String.valueOf(i))));
            }
            map.put("north_" + n, ExtraDataFixUtils.blockState("minecraft:" + string + "_wall_banner", Map.of("facing", "north")));
            map.put("south_" + n, ExtraDataFixUtils.blockState("minecraft:" + string + "_wall_banner", Map.of("facing", "south")));
            map.put("west_" + n, ExtraDataFixUtils.blockState("minecraft:" + string + "_wall_banner", Map.of("facing", "west")));
            map.put("east_" + n, ExtraDataFixUtils.blockState("minecraft:" + string + "_wall_banner", Map.of("facing", "east")));
        }

        static {
            FIX.set(2);
            FIX.set(3);
            FIX.set(110);
            FIX.set(140);
            FIX.set(144);
            FIX.set(25);
            FIX.set(86);
            FIX.set(26);
            FIX.set(176);
            FIX.set(177);
            FIX.set(175);
            FIX.set(64);
            FIX.set(71);
            FIX.set(193);
            FIX.set(194);
            FIX.set(195);
            FIX.set(196);
            FIX.set(197);
            VIRTUAL.set(54);
            VIRTUAL.set(146);
            VIRTUAL.set(25);
            VIRTUAL.set(26);
            VIRTUAL.set(51);
            VIRTUAL.set(53);
            VIRTUAL.set(67);
            VIRTUAL.set(108);
            VIRTUAL.set(109);
            VIRTUAL.set(114);
            VIRTUAL.set(128);
            VIRTUAL.set(134);
            VIRTUAL.set(135);
            VIRTUAL.set(136);
            VIRTUAL.set(156);
            VIRTUAL.set(163);
            VIRTUAL.set(164);
            VIRTUAL.set(180);
            VIRTUAL.set(203);
            VIRTUAL.set(55);
            VIRTUAL.set(85);
            VIRTUAL.set(113);
            VIRTUAL.set(188);
            VIRTUAL.set(189);
            VIRTUAL.set(190);
            VIRTUAL.set(191);
            VIRTUAL.set(192);
            VIRTUAL.set(93);
            VIRTUAL.set(94);
            VIRTUAL.set(101);
            VIRTUAL.set(102);
            VIRTUAL.set(160);
            VIRTUAL.set(106);
            VIRTUAL.set(107);
            VIRTUAL.set(183);
            VIRTUAL.set(184);
            VIRTUAL.set(185);
            VIRTUAL.set(186);
            VIRTUAL.set(187);
            VIRTUAL.set(132);
            VIRTUAL.set(139);
            VIRTUAL.set(199);
            AIR = ExtraDataFixUtils.blockState("minecraft:air");
        }
    }
}

