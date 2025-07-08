/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.base.Splitter
 *  com.google.common.base.Strings
 *  com.google.common.collect.Comparators
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.Dynamic
 *  it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.PrimitiveTag;
import net.minecraft.nbt.SnbtPrinterTagVisitor;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.nbt.TextComponentTagVisitor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueOutput;
import org.slf4j.Logger;

public final class NbtUtils {
    private static final Comparator<ListTag> YXZ_LISTTAG_INT_COMPARATOR = Comparator.comparingInt(listTag -> listTag.getIntOr(1, 0)).thenComparingInt(listTag -> listTag.getIntOr(0, 0)).thenComparingInt(listTag -> listTag.getIntOr(2, 0));
    private static final Comparator<ListTag> YXZ_LISTTAG_DOUBLE_COMPARATOR = Comparator.comparingDouble(listTag -> listTag.getDoubleOr(1, 0.0)).thenComparingDouble(listTag -> listTag.getDoubleOr(0, 0.0)).thenComparingDouble(listTag -> listTag.getDoubleOr(2, 0.0));
    private static final Codec<ResourceKey<Block>> BLOCK_NAME_CODEC = ResourceKey.codec(Registries.BLOCK);
    public static final String SNBT_DATA_TAG = "data";
    private static final char PROPERTIES_START = '{';
    private static final char PROPERTIES_END = '}';
    private static final String ELEMENT_SEPARATOR = ",";
    private static final char KEY_VALUE_SEPARATOR = ':';
    private static final Splitter COMMA_SPLITTER = Splitter.on((String)",");
    private static final Splitter COLON_SPLITTER = Splitter.on((char)':').limit(2);
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int INDENT = 2;
    private static final int NOT_FOUND = -1;

    private NbtUtils() {
    }

    @VisibleForTesting
    public static boolean compareNbt(@Nullable Tag tag, @Nullable Tag tag2, boolean bl) {
        if (tag == tag2) {
            return true;
        }
        if (tag == null) {
            return true;
        }
        if (tag2 == null) {
            return false;
        }
        if (!tag.getClass().equals(tag2.getClass())) {
            return false;
        }
        if (tag instanceof CompoundTag) {
            CompoundTag compoundTag = (CompoundTag)tag;
            CompoundTag compoundTag2 = (CompoundTag)tag2;
            if (compoundTag2.size() < compoundTag.size()) {
                return false;
            }
            for (Map.Entry<String, Tag> entry : compoundTag.entrySet()) {
                Tag tag3 = entry.getValue();
                if (NbtUtils.compareNbt(tag3, compoundTag2.get(entry.getKey()), bl)) continue;
                return false;
            }
            return true;
        }
        if (tag instanceof ListTag) {
            ListTag listTag = (ListTag)tag;
            if (bl) {
                ListTag listTag2 = (ListTag)tag2;
                if (listTag.isEmpty()) {
                    return listTag2.isEmpty();
                }
                if (listTag2.size() < listTag.size()) {
                    return false;
                }
                for (Tag tag4 : listTag) {
                    boolean bl2 = false;
                    for (Tag tag5 : listTag2) {
                        if (!NbtUtils.compareNbt(tag4, tag5, bl)) continue;
                        bl2 = true;
                        break;
                    }
                    if (bl2) continue;
                    return false;
                }
                return true;
            }
        }
        return tag.equals(tag2);
    }

    public static BlockState readBlockState(HolderGetter<Block> holderGetter, CompoundTag compoundTag) {
        Optional optional = compoundTag.read("Name", BLOCK_NAME_CODEC).flatMap(holderGetter::get);
        if (optional.isEmpty()) {
            return Blocks.AIR.defaultBlockState();
        }
        Block block = (Block)((Holder)optional.get()).value();
        BlockState blockState = block.defaultBlockState();
        Optional<CompoundTag> optional2 = compoundTag.getCompound("Properties");
        if (optional2.isPresent()) {
            StateDefinition<Block, BlockState> stateDefinition = block.getStateDefinition();
            for (String string : optional2.get().keySet()) {
                Property<?> property = stateDefinition.getProperty(string);
                if (property == null) continue;
                blockState = NbtUtils.setValueHelper(blockState, property, string, optional2.get(), compoundTag);
            }
        }
        return blockState;
    }

    private static <S extends StateHolder<?, S>, T extends Comparable<T>> S setValueHelper(S s, Property<T> property, String string, CompoundTag compoundTag, CompoundTag compoundTag2) {
        Optional optional = compoundTag.getString(string).flatMap(property::getValue);
        if (optional.isPresent()) {
            return (S)((StateHolder)s.setValue(property, (Comparable)((Comparable)optional.get())));
        }
        LOGGER.warn("Unable to read property: {} with value: {} for blockstate: {}", new Object[]{string, compoundTag.get(string), compoundTag2});
        return s;
    }

    public static CompoundTag writeBlockState(BlockState blockState) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("Name", BuiltInRegistries.BLOCK.getKey(blockState.getBlock()).toString());
        Map<Property<?>, Comparable<?>> map = blockState.getValues();
        if (!map.isEmpty()) {
            CompoundTag compoundTag2 = new CompoundTag();
            for (Map.Entry<Property<?>, Comparable<?>> entry : map.entrySet()) {
                Property<?> property = entry.getKey();
                compoundTag2.putString(property.getName(), NbtUtils.getName(property, entry.getValue()));
            }
            compoundTag.put("Properties", compoundTag2);
        }
        return compoundTag;
    }

    public static CompoundTag writeFluidState(FluidState fluidState) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("Name", BuiltInRegistries.FLUID.getKey(fluidState.getType()).toString());
        Map<Property<?>, Comparable<?>> map = fluidState.getValues();
        if (!map.isEmpty()) {
            CompoundTag compoundTag2 = new CompoundTag();
            for (Map.Entry<Property<?>, Comparable<?>> entry : map.entrySet()) {
                Property<?> property = entry.getKey();
                compoundTag2.putString(property.getName(), NbtUtils.getName(property, entry.getValue()));
            }
            compoundTag.put("Properties", compoundTag2);
        }
        return compoundTag;
    }

    private static <T extends Comparable<T>> String getName(Property<T> property, Comparable<?> comparable) {
        return property.getName(comparable);
    }

    public static String prettyPrint(Tag tag) {
        return NbtUtils.prettyPrint(tag, false);
    }

    public static String prettyPrint(Tag tag, boolean bl) {
        return NbtUtils.prettyPrint(new StringBuilder(), tag, 0, bl).toString();
    }

    public static StringBuilder prettyPrint(StringBuilder stringBuilder, Tag tag, int n, boolean bl) {
        Tag tag2 = tag;
        Objects.requireNonNull(tag2);
        Tag tag3 = tag2;
        int n2 = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{PrimitiveTag.class, EndTag.class, ByteArrayTag.class, ListTag.class, IntArrayTag.class, CompoundTag.class, LongArrayTag.class}, (Object)tag3, n2)) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                PrimitiveTag var6_6 = (PrimitiveTag)tag3;
                yield stringBuilder.append(var6_6);
            }
            case 1 -> {
                EndTag var7_7 = (EndTag)tag3;
                yield stringBuilder;
            }
            case 2 -> {
                ByteArrayTag var8_8 = (ByteArrayTag)tag3;
                byte[] var9_9 = var8_8.getAsByteArray();
                int var10_11 = var9_9.length;
                NbtUtils.indent(n, stringBuilder).append("byte[").append(var10_11).append("] {\n");
                if (bl) {
                    NbtUtils.indent(n + 1, stringBuilder);
                    for (int var11_14 = 0; var11_14 < var9_9.length; ++var11_14) {
                        if (var11_14 != 0) {
                            stringBuilder.append(',');
                        }
                        if (var11_14 % 16 == 0 && var11_14 / 16 > 0) {
                            stringBuilder.append('\n');
                            if (var11_14 < var9_9.length) {
                                NbtUtils.indent(n + 1, stringBuilder);
                            }
                        } else if (var11_14 != 0) {
                            stringBuilder.append(' ');
                        }
                        stringBuilder.append(String.format(Locale.ROOT, "0x%02X", var9_9[var11_14] & 0xFF));
                    }
                } else {
                    NbtUtils.indent(n + 1, stringBuilder).append(" // Skipped, supply withBinaryBlobs true");
                }
                stringBuilder.append('\n');
                NbtUtils.indent(n, stringBuilder).append('}');
                yield stringBuilder;
            }
            case 3 -> {
                ListTag var9_10 = (ListTag)tag3;
                int var10_12 = var9_10.size();
                NbtUtils.indent(n, stringBuilder).append("list").append("[").append(var10_12).append("] [");
                if (var10_12 != 0) {
                    stringBuilder.append('\n');
                }
                for (int var11_15 = 0; var11_15 < var10_12; ++var11_15) {
                    if (var11_15 != 0) {
                        stringBuilder.append(",\n");
                    }
                    NbtUtils.indent(n + 1, stringBuilder);
                    NbtUtils.prettyPrint(stringBuilder, var9_10.get(var11_15), n + 1, bl);
                }
                if (var10_12 != 0) {
                    stringBuilder.append('\n');
                }
                NbtUtils.indent(n, stringBuilder).append(']');
                yield stringBuilder;
            }
            case 4 -> {
                IntArrayTag var10_13 = (IntArrayTag)tag3;
                int[] var11_16 = var10_13.getAsIntArray();
                int var12_18 = 0;
                int[] var13_21 = var11_16;
                int var14_25 = var13_21.length;
                for (int var15_28 = 0; var15_28 < var14_25; ++var15_28) {
                    int var16_30 = var13_21[var15_28];
                    var12_18 = Math.max(var12_18, String.format(Locale.ROOT, "%X", var16_30).length());
                }
                int var13_22 = var11_16.length;
                NbtUtils.indent(n, stringBuilder).append("int[").append(var13_22).append("] {\n");
                if (bl) {
                    NbtUtils.indent(n + 1, stringBuilder);
                    for (var14_25 = 0; var14_25 < var11_16.length; ++var14_25) {
                        if (var14_25 != 0) {
                            stringBuilder.append(',');
                        }
                        if (var14_25 % 16 == 0 && var14_25 / 16 > 0) {
                            stringBuilder.append('\n');
                            if (var14_25 < var11_16.length) {
                                NbtUtils.indent(n + 1, stringBuilder);
                            }
                        } else if (var14_25 != 0) {
                            stringBuilder.append(' ');
                        }
                        stringBuilder.append(String.format(Locale.ROOT, "0x%0" + var12_18 + "X", var11_16[var14_25]));
                    }
                } else {
                    NbtUtils.indent(n + 1, stringBuilder).append(" // Skipped, supply withBinaryBlobs true");
                }
                stringBuilder.append('\n');
                NbtUtils.indent(n, stringBuilder).append('}');
                yield stringBuilder;
            }
            case 5 -> {
                CompoundTag var11_17 = (CompoundTag)tag3;
                ArrayList var12_19 = Lists.newArrayList(var11_17.keySet());
                Collections.sort(var12_19);
                NbtUtils.indent(n, stringBuilder).append('{');
                if (stringBuilder.length() - stringBuilder.lastIndexOf("\n") > 2 * (n + 1)) {
                    stringBuilder.append('\n');
                    NbtUtils.indent(n + 1, stringBuilder);
                }
                int var13_23 = var12_19.stream().mapToInt(String::length).max().orElse(0);
                String var14_26 = Strings.repeat((String)" ", (int)var13_23);
                for (int var15_29 = 0; var15_29 < var12_19.size(); ++var15_29) {
                    if (var15_29 != 0) {
                        stringBuilder.append(",\n");
                    }
                    String var16_31 = (String)var12_19.get(var15_29);
                    NbtUtils.indent(n + 1, stringBuilder).append('\"').append(var16_31).append('\"').append(var14_26, 0, var14_26.length() - var16_31.length()).append(": ");
                    NbtUtils.prettyPrint(stringBuilder, var11_17.get(var16_31), n + 1, bl);
                }
                if (!var12_19.isEmpty()) {
                    stringBuilder.append('\n');
                }
                NbtUtils.indent(n, stringBuilder).append('}');
                yield stringBuilder;
            }
            case 6 -> {
                int var18_35;
                LongArrayTag var12_20 = (LongArrayTag)tag3;
                long[] var13_24 = var12_20.getAsLongArray();
                long var14_27 = 0L;
                long[] var16_32 = var13_24;
                int var17_34 = var16_32.length;
                for (var18_35 = 0; var18_35 < var17_34; ++var18_35) {
                    long var19_36 = var16_32[var18_35];
                    var14_27 = Math.max(var14_27, (long)String.format(Locale.ROOT, "%X", var19_36).length());
                }
                long var16_33 = var13_24.length;
                NbtUtils.indent(n, stringBuilder).append("long[").append(var16_33).append("] {\n");
                if (bl) {
                    NbtUtils.indent(n + 1, stringBuilder);
                    for (var18_35 = 0; var18_35 < var13_24.length; ++var18_35) {
                        if (var18_35 != 0) {
                            stringBuilder.append(',');
                        }
                        if (var18_35 % 16 == 0 && var18_35 / 16 > 0) {
                            stringBuilder.append('\n');
                            if (var18_35 < var13_24.length) {
                                NbtUtils.indent(n + 1, stringBuilder);
                            }
                        } else if (var18_35 != 0) {
                            stringBuilder.append(' ');
                        }
                        stringBuilder.append(String.format(Locale.ROOT, "0x%0" + var14_27 + "X", var13_24[var18_35]));
                    }
                } else {
                    NbtUtils.indent(n + 1, stringBuilder).append(" // Skipped, supply withBinaryBlobs true");
                }
                stringBuilder.append('\n');
                NbtUtils.indent(n, stringBuilder).append('}');
                yield stringBuilder;
            }
        };
    }

    private static StringBuilder indent(int n, StringBuilder stringBuilder) {
        int n2 = stringBuilder.lastIndexOf("\n") + 1;
        int n3 = stringBuilder.length() - n2;
        for (int i = 0; i < 2 * n - n3; ++i) {
            stringBuilder.append(' ');
        }
        return stringBuilder;
    }

    public static Component toPrettyComponent(Tag tag) {
        return new TextComponentTagVisitor("").visit(tag);
    }

    public static String structureToSnbt(CompoundTag compoundTag) {
        return new SnbtPrinterTagVisitor().visit(NbtUtils.packStructureTemplate(compoundTag));
    }

    public static CompoundTag snbtToStructure(String string) throws CommandSyntaxException {
        return NbtUtils.unpackStructureTemplate(TagParser.parseCompoundFully(string));
    }

    @VisibleForTesting
    static CompoundTag packStructureTemplate(CompoundTag compoundTag2) {
        ListTag listTag;
        Object object;
        Optional<ListTag> optional = compoundTag2.getList("palettes");
        ListTag listTag2 = optional.isPresent() ? optional.get().getListOrEmpty(0) : compoundTag2.getListOrEmpty("palette");
        ListTag listTag3 = listTag2.compoundStream().map(NbtUtils::packBlockState).map(StringTag::valueOf).collect(Collectors.toCollection(ListTag::new));
        compoundTag2.put("palette", listTag3);
        if (optional.isPresent()) {
            object = new ListTag();
            optional.get().stream().flatMap(tag -> tag.asList().stream()).forEach(arg_0 -> NbtUtils.lambda$packStructureTemplate$7(listTag3, (ListTag)object, arg_0));
            compoundTag2.put("palettes", (Tag)object);
        }
        if (((Optional)(object = compoundTag2.getList("entities"))).isPresent()) {
            listTag = ((ListTag)((Optional)object).get()).compoundStream().sorted(Comparator.comparing(compoundTag -> compoundTag.getList("pos"), Comparators.emptiesLast(YXZ_LISTTAG_DOUBLE_COMPARATOR))).collect(Collectors.toCollection(ListTag::new));
            compoundTag2.put("entities", listTag);
        }
        listTag = compoundTag2.getList("blocks").stream().flatMap(ListTag::compoundStream).sorted(Comparator.comparing(compoundTag -> compoundTag.getList("pos"), Comparators.emptiesLast(YXZ_LISTTAG_INT_COMPARATOR))).peek(compoundTag -> compoundTag.putString("state", listTag3.getString(compoundTag.getIntOr("state", 0)).orElseThrow())).collect(Collectors.toCollection(ListTag::new));
        compoundTag2.put(SNBT_DATA_TAG, listTag);
        compoundTag2.remove("blocks");
        return compoundTag2;
    }

    @VisibleForTesting
    static CompoundTag unpackStructureTemplate(CompoundTag compoundTag2) {
        ListTag listTag = compoundTag2.getListOrEmpty("palette");
        Map map = (Map)listTag.stream().flatMap(tag -> tag.asString().stream()).collect(ImmutableMap.toImmutableMap(Function.identity(), NbtUtils::unpackBlockState));
        Optional<ListTag> optional = compoundTag2.getList("palettes");
        if (optional.isPresent()) {
            compoundTag2.put("palettes", optional.get().compoundStream().map(compoundTag -> map.keySet().stream().map(string -> compoundTag.getString((String)string).orElseThrow()).map(NbtUtils::unpackBlockState).collect(Collectors.toCollection(ListTag::new))).collect(Collectors.toCollection(ListTag::new)));
            compoundTag2.remove("palette");
        } else {
            compoundTag2.put("palette", map.values().stream().collect(Collectors.toCollection(ListTag::new)));
        }
        Optional<ListTag> optional2 = compoundTag2.getList(SNBT_DATA_TAG);
        if (optional2.isPresent()) {
            Object2IntOpenHashMap object2IntOpenHashMap = new Object2IntOpenHashMap();
            object2IntOpenHashMap.defaultReturnValue(-1);
            for (int i = 0; i < listTag.size(); ++i) {
                object2IntOpenHashMap.put((Object)listTag.getString(i).orElseThrow(), i);
            }
            ListTag listTag2 = optional2.get();
            for (int i = 0; i < listTag2.size(); ++i) {
                CompoundTag compoundTag3 = listTag2.getCompound(i).orElseThrow();
                String string = compoundTag3.getString("state").orElseThrow();
                int n = object2IntOpenHashMap.getInt((Object)string);
                if (n == -1) {
                    throw new IllegalStateException("Entry " + string + " missing from palette");
                }
                compoundTag3.putInt("state", n);
            }
            compoundTag2.put("blocks", listTag2);
            compoundTag2.remove(SNBT_DATA_TAG);
        }
        return compoundTag2;
    }

    @VisibleForTesting
    static String packBlockState(CompoundTag compoundTag2) {
        StringBuilder stringBuilder = new StringBuilder(compoundTag2.getString("Name").orElseThrow());
        compoundTag2.getCompound("Properties").ifPresent(compoundTag -> {
            String string = compoundTag.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(entry -> (String)entry.getKey() + ":" + ((Tag)entry.getValue()).asString().orElseThrow()).collect(Collectors.joining(ELEMENT_SEPARATOR));
            stringBuilder.append('{').append(string).append('}');
        });
        return stringBuilder.toString();
    }

    @VisibleForTesting
    static CompoundTag unpackBlockState(String string) {
        String string3;
        CompoundTag compoundTag = new CompoundTag();
        int n = string.indexOf(123);
        if (n >= 0) {
            string3 = string.substring(0, n);
            CompoundTag compoundTag2 = new CompoundTag();
            if (n + 2 <= string.length()) {
                String string4 = string.substring(n + 1, string.indexOf(125, n));
                COMMA_SPLITTER.split((CharSequence)string4).forEach(string2 -> {
                    List list = COLON_SPLITTER.splitToList((CharSequence)string2);
                    if (list.size() == 2) {
                        compoundTag2.putString((String)list.get(0), (String)list.get(1));
                    } else {
                        LOGGER.error("Something went wrong parsing: '{}' -- incorrect gamedata!", (Object)string);
                    }
                });
                compoundTag.put("Properties", compoundTag2);
            }
        } else {
            string3 = string;
        }
        compoundTag.putString("Name", string3);
        return compoundTag;
    }

    public static CompoundTag addCurrentDataVersion(CompoundTag compoundTag) {
        int n = SharedConstants.getCurrentVersion().dataVersion().version();
        return NbtUtils.addDataVersion(compoundTag, n);
    }

    public static CompoundTag addDataVersion(CompoundTag compoundTag, int n) {
        compoundTag.putInt("DataVersion", n);
        return compoundTag;
    }

    public static void addCurrentDataVersion(ValueOutput valueOutput) {
        int n = SharedConstants.getCurrentVersion().dataVersion().version();
        NbtUtils.addDataVersion(valueOutput, n);
    }

    public static void addDataVersion(ValueOutput valueOutput, int n) {
        valueOutput.putInt("DataVersion", n);
    }

    public static int getDataVersion(CompoundTag compoundTag, int n) {
        return compoundTag.getIntOr("DataVersion", n);
    }

    public static int getDataVersion(Dynamic<?> dynamic, int n) {
        return dynamic.get("DataVersion").asInt(n);
    }

    private static /* synthetic */ void lambda$packStructureTemplate$7(ListTag listTag, ListTag listTag2, ListTag listTag3) {
        CompoundTag compoundTag = new CompoundTag();
        for (int i = 0; i < listTag3.size(); ++i) {
            compoundTag.putString(listTag.getString(i).orElseThrow(), NbtUtils.packBlockState(listTag3.getCompound(i).orElseThrow()));
        }
        listTag2.add(compoundTag);
    }
}

