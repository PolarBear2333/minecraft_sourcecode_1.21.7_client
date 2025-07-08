/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.MapLike
 *  com.mojang.serialization.RecordBuilder
 *  com.mojang.serialization.RecordBuilder$AbstractStringBuilder
 *  it.unimi.dsi.fastutil.bytes.ByteArrayList
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.longs.LongArrayList
 *  javax.annotation.Nullable
 */
package net.minecraft.nbt;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.lang.runtime.SwitchBootstraps;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.EndTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

public class NbtOps
implements DynamicOps<Tag> {
    public static final NbtOps INSTANCE = new NbtOps();

    private NbtOps() {
    }

    public Tag empty() {
        return EndTag.INSTANCE;
    }

    /*
     * WARNING - Removed back jump from a try to a catch block - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public <U> U convertTo(DynamicOps<U> dynamicOps, Tag tag) {
        Object object;
        Tag tag2 = tag;
        Objects.requireNonNull(tag2);
        Tag tag3 = tag2;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{EndTag.class, ByteTag.class, ShortTag.class, IntTag.class, LongTag.class, FloatTag.class, DoubleTag.class, ByteArrayTag.class, StringTag.class, ListTag.class, CompoundTag.class, IntArrayTag.class, LongArrayTag.class}, (Object)tag3, n)) {
            default: {
                throw new MatchException(null, null);
            }
            case 0: {
                EndTag endTag = (EndTag)tag3;
                object = dynamicOps.empty();
                return (U)object;
            }
            case 1: {
                byte by2;
                ByteTag byteTag = (ByteTag)tag3;
                try {
                    byte by;
                    by2 = by = byteTag.value();
                }
                catch (Throwable throwable) {
                    throw new MatchException(throwable.toString(), throwable);
                }
                object = dynamicOps.createByte(by2);
                return (U)object;
            }
            case 2: {
                short s2;
                ShortTag shortTag = (ShortTag)tag3;
                {
                    short s;
                    s2 = s = shortTag.value();
                }
                object = dynamicOps.createShort(s2);
                return (U)object;
            }
            case 3: {
                int n3;
                IntTag intTag = (IntTag)tag3;
                {
                    int n2;
                    n3 = n2 = intTag.value();
                }
                object = dynamicOps.createInt(n3);
                return (U)object;
            }
            case 4: {
                long l2;
                LongTag longTag = (LongTag)tag3;
                {
                    long l;
                    l2 = l = longTag.value();
                }
                object = dynamicOps.createLong(l2);
                return (U)object;
            }
            case 5: {
                float f2;
                FloatTag floatTag = (FloatTag)tag3;
                {
                    float f;
                    f2 = f = floatTag.value();
                }
                object = dynamicOps.createFloat(f2);
                return (U)object;
            }
            case 6: {
                double d2;
                DoubleTag doubleTag = (DoubleTag)tag3;
                {
                    double d;
                    d2 = d = doubleTag.value();
                }
                object = dynamicOps.createDouble(d2);
                return (U)object;
            }
            case 7: {
                ByteArrayTag byteArrayTag = (ByteArrayTag)tag3;
                object = dynamicOps.createByteList(ByteBuffer.wrap(byteArrayTag.getAsByteArray()));
                return (U)object;
            }
            case 8: {
                String string2;
                StringTag stringTag = (StringTag)tag3;
                {
                    String string;
                    string2 = string = stringTag.value();
                }
                object = dynamicOps.createString(string2);
                return (U)object;
            }
            case 9: {
                ListTag listTag = (ListTag)tag3;
                object = this.convertList(dynamicOps, listTag);
                return (U)object;
            }
            case 10: {
                CompoundTag compoundTag = (CompoundTag)tag3;
                object = this.convertMap(dynamicOps, compoundTag);
                return (U)object;
            }
            case 11: {
                IntArrayTag intArrayTag = (IntArrayTag)tag3;
                object = dynamicOps.createIntList(Arrays.stream(intArrayTag.getAsIntArray()));
                return (U)object;
            }
            case 12: 
        }
        LongArrayTag longArrayTag = (LongArrayTag)tag3;
        object = dynamicOps.createLongList(Arrays.stream(longArrayTag.getAsLongArray()));
        return (U)object;
    }

    public DataResult<Number> getNumberValue(Tag tag) {
        return tag.asNumber().map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Not a number"));
    }

    public Tag createNumeric(Number number) {
        return DoubleTag.valueOf(number.doubleValue());
    }

    public Tag createByte(byte by) {
        return ByteTag.valueOf(by);
    }

    public Tag createShort(short s) {
        return ShortTag.valueOf(s);
    }

    public Tag createInt(int n) {
        return IntTag.valueOf(n);
    }

    public Tag createLong(long l) {
        return LongTag.valueOf(l);
    }

    public Tag createFloat(float f) {
        return FloatTag.valueOf(f);
    }

    public Tag createDouble(double d) {
        return DoubleTag.valueOf(d);
    }

    public Tag createBoolean(boolean bl) {
        return ByteTag.valueOf(bl);
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public DataResult<String> getStringValue(Tag tag) {
        String string2;
        if (!(tag instanceof StringTag)) return DataResult.error(() -> "Not a string");
        StringTag stringTag = (StringTag)tag;
        try {
            String string;
            string2 = string = stringTag.value();
        }
        catch (Throwable throwable) {
            throw new MatchException(throwable.toString(), throwable);
        }
        return DataResult.success((Object)string2);
    }

    public Tag createString(String string) {
        return StringTag.valueOf(string);
    }

    public DataResult<Tag> mergeToList(Tag tag, Tag tag2) {
        return NbtOps.createCollector(tag).map(listCollector -> DataResult.success((Object)listCollector.accept(tag2).result())).orElseGet(() -> DataResult.error(() -> "mergeToList called with not a list: " + String.valueOf(tag), (Object)tag));
    }

    public DataResult<Tag> mergeToList(Tag tag, List<Tag> list) {
        return NbtOps.createCollector(tag).map(listCollector -> DataResult.success((Object)listCollector.acceptAll(list).result())).orElseGet(() -> DataResult.error(() -> "mergeToList called with not a list: " + String.valueOf(tag), (Object)tag));
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public DataResult<Tag> mergeToMap(Tag tag, Tag tag2, Tag tag3) {
        CompoundTag compoundTag;
        String string;
        Object object;
        if (!(tag instanceof CompoundTag) && !(tag instanceof EndTag)) {
            return DataResult.error(() -> "mergeToMap called with not a map: " + String.valueOf(tag), (Object)tag);
        }
        if (!(tag2 instanceof StringTag)) return DataResult.error(() -> "key is not a string: " + String.valueOf(tag2), (Object)tag);
        Tag tag4 = (StringTag)tag2;
        try {
            object = ((StringTag)tag4).value();
            string = object;
        }
        catch (Throwable throwable) {
            throw new MatchException(throwable.toString(), throwable);
        }
        if (tag instanceof CompoundTag) {
            object = (CompoundTag)tag;
            compoundTag = ((CompoundTag)object).shallowCopy();
        } else {
            compoundTag = new CompoundTag();
        }
        tag4 = compoundTag;
        ((CompoundTag)tag4).put(string, tag3);
        return DataResult.success((Object)tag4);
    }

    public DataResult<Tag> mergeToMap(Tag tag, MapLike<Tag> mapLike) {
        CompoundTag compoundTag;
        Object object;
        if (!(tag instanceof CompoundTag) && !(tag instanceof EndTag)) {
            return DataResult.error(() -> "mergeToMap called with not a map: " + String.valueOf(tag), (Object)tag);
        }
        if (tag instanceof CompoundTag) {
            object = (CompoundTag)tag;
            compoundTag = ((CompoundTag)object).shallowCopy();
        } else {
            compoundTag = new CompoundTag();
        }
        CompoundTag compoundTag2 = compoundTag;
        object = new ArrayList();
        mapLike.entries().forEach(arg_0 -> NbtOps.lambda$mergeToMap$12((List)object, compoundTag2, arg_0));
        if (!object.isEmpty()) {
            return DataResult.error(() -> NbtOps.lambda$mergeToMap$13((List)object), (Object)compoundTag2);
        }
        return DataResult.success((Object)compoundTag2);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public DataResult<Tag> mergeToMap(Tag tag, Map<Tag, Tag> map) {
        CompoundTag compoundTag;
        Object object;
        if (!(tag instanceof CompoundTag) && !(tag instanceof EndTag)) {
            return DataResult.error(() -> "mergeToMap called with not a map: " + String.valueOf(tag), (Object)tag);
        }
        if (tag instanceof CompoundTag) {
            object = (CompoundTag)tag;
            compoundTag = ((CompoundTag)object).shallowCopy();
        } else {
            compoundTag = new CompoundTag();
        }
        CompoundTag compoundTag2 = compoundTag;
        object = new ArrayList();
        for (Map.Entry<Tag, Tag> entry : map.entrySet()) {
            Tag tag2 = entry.getKey();
            if (tag2 instanceof StringTag) {
                StringTag stringTag = (StringTag)tag2;
                try {
                    String string;
                    String string2 = string = stringTag.value();
                    compoundTag2.put(string2, entry.getValue());
                    continue;
                }
                catch (Throwable throwable) {
                    throw new MatchException(throwable.toString(), throwable);
                }
            }
            object.add(tag2);
        }
        if (!object.isEmpty()) {
            return DataResult.error(() -> NbtOps.lambda$mergeToMap$15((List)object), (Object)compoundTag2);
        }
        return DataResult.success((Object)compoundTag2);
    }

    public DataResult<Stream<Pair<Tag, Tag>>> getMapValues(Tag tag) {
        if (tag instanceof CompoundTag) {
            CompoundTag compoundTag = (CompoundTag)tag;
            return DataResult.success(compoundTag.entrySet().stream().map(entry -> Pair.of((Object)this.createString((String)entry.getKey()), (Object)((Tag)entry.getValue()))));
        }
        return DataResult.error(() -> "Not a map: " + String.valueOf(tag));
    }

    public DataResult<Consumer<BiConsumer<Tag, Tag>>> getMapEntries(Tag tag) {
        if (tag instanceof CompoundTag) {
            CompoundTag compoundTag = (CompoundTag)tag;
            return DataResult.success(biConsumer -> {
                for (Map.Entry<String, Tag> entry : compoundTag.entrySet()) {
                    biConsumer.accept(this.createString(entry.getKey()), entry.getValue());
                }
            });
        }
        return DataResult.error(() -> "Not a map: " + String.valueOf(tag));
    }

    public DataResult<MapLike<Tag>> getMap(Tag tag) {
        if (tag instanceof CompoundTag) {
            final CompoundTag compoundTag = (CompoundTag)tag;
            return DataResult.success((Object)new MapLike<Tag>(){

                /*
                 * Enabled force condition propagation
                 * Lifted jumps to return sites
                 */
                @Nullable
                public Tag get(Tag tag) {
                    if (!(tag instanceof StringTag)) throw new UnsupportedOperationException("Cannot get map entry with non-string key: " + String.valueOf(tag));
                    StringTag stringTag = (StringTag)tag;
                    try {
                        String string;
                        String string2 = string = stringTag.value();
                        return compoundTag.get(string2);
                    }
                    catch (Throwable throwable) {
                        throw new MatchException(throwable.toString(), throwable);
                    }
                }

                @Nullable
                public Tag get(String string) {
                    return compoundTag.get(string);
                }

                public Stream<Pair<Tag, Tag>> entries() {
                    return compoundTag.entrySet().stream().map(entry -> Pair.of((Object)NbtOps.this.createString((String)entry.getKey()), (Object)((Tag)entry.getValue())));
                }

                public String toString() {
                    return "MapLike[" + String.valueOf(compoundTag) + "]";
                }

                @Nullable
                public /* synthetic */ Object get(String string) {
                    return this.get(string);
                }

                @Nullable
                public /* synthetic */ Object get(Object object) {
                    return this.get((Tag)object);
                }
            });
        }
        return DataResult.error(() -> "Not a map: " + String.valueOf(tag));
    }

    public Tag createMap(Stream<Pair<Tag, Tag>> stream) {
        CompoundTag compoundTag = new CompoundTag();
        stream.forEach(pair -> {
            Tag tag = (Tag)pair.getFirst();
            Tag tag2 = (Tag)pair.getSecond();
            if (!(tag instanceof StringTag)) throw new UnsupportedOperationException("Cannot create map with non-string key: " + String.valueOf(tag));
            StringTag stringTag = (StringTag)tag;
            try {
                String string;
                String string2 = string = stringTag.value();
                compoundTag.put(string2, tag2);
            }
            catch (Throwable throwable) {
                throw new MatchException(throwable.toString(), throwable);
            }
        });
        return compoundTag;
    }

    public DataResult<Stream<Tag>> getStream(Tag tag) {
        if (tag instanceof CollectionTag) {
            CollectionTag collectionTag = (CollectionTag)tag;
            return DataResult.success(collectionTag.stream());
        }
        return DataResult.error(() -> "Not a list");
    }

    public DataResult<Consumer<Consumer<Tag>>> getList(Tag tag) {
        if (tag instanceof CollectionTag) {
            CollectionTag collectionTag = (CollectionTag)tag;
            return DataResult.success(collectionTag::forEach);
        }
        return DataResult.error(() -> "Not a list: " + String.valueOf(tag));
    }

    public DataResult<ByteBuffer> getByteBuffer(Tag tag) {
        if (tag instanceof ByteArrayTag) {
            ByteArrayTag byteArrayTag = (ByteArrayTag)tag;
            return DataResult.success((Object)ByteBuffer.wrap(byteArrayTag.getAsByteArray()));
        }
        return super.getByteBuffer((Object)tag);
    }

    public Tag createByteList(ByteBuffer byteBuffer) {
        ByteBuffer byteBuffer2 = byteBuffer.duplicate().clear();
        byte[] byArray = new byte[byteBuffer.capacity()];
        byteBuffer2.get(0, byArray, 0, byArray.length);
        return new ByteArrayTag(byArray);
    }

    public DataResult<IntStream> getIntStream(Tag tag) {
        if (tag instanceof IntArrayTag) {
            IntArrayTag intArrayTag = (IntArrayTag)tag;
            return DataResult.success((Object)Arrays.stream(intArrayTag.getAsIntArray()));
        }
        return super.getIntStream((Object)tag);
    }

    public Tag createIntList(IntStream intStream) {
        return new IntArrayTag(intStream.toArray());
    }

    public DataResult<LongStream> getLongStream(Tag tag) {
        if (tag instanceof LongArrayTag) {
            LongArrayTag longArrayTag = (LongArrayTag)tag;
            return DataResult.success((Object)Arrays.stream(longArrayTag.getAsLongArray()));
        }
        return super.getLongStream((Object)tag);
    }

    public Tag createLongList(LongStream longStream) {
        return new LongArrayTag(longStream.toArray());
    }

    public Tag createList(Stream<Tag> stream) {
        return new ListTag(stream.collect(Util.toMutableList()));
    }

    public Tag remove(Tag tag, String string) {
        if (tag instanceof CompoundTag) {
            CompoundTag compoundTag = (CompoundTag)tag;
            CompoundTag compoundTag2 = compoundTag.shallowCopy();
            compoundTag2.remove(string);
            return compoundTag2;
        }
        return tag;
    }

    public String toString() {
        return "NBT";
    }

    public RecordBuilder<Tag> mapBuilder() {
        return new NbtRecordBuilder(this);
    }

    private static Optional<ListCollector> createCollector(Tag tag) {
        if (tag instanceof EndTag) {
            return Optional.of(new GenericListCollector());
        }
        if (tag instanceof CollectionTag) {
            CollectionTag collectionTag = (CollectionTag)tag;
            if (collectionTag.isEmpty()) {
                return Optional.of(new GenericListCollector());
            }
            CollectionTag collectionTag2 = collectionTag;
            Objects.requireNonNull(collectionTag2);
            CollectionTag collectionTag3 = collectionTag2;
            int n = 0;
            return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{ListTag.class, ByteArrayTag.class, IntArrayTag.class, LongArrayTag.class}, (Object)collectionTag3, n)) {
                default -> throw new MatchException(null, null);
                case 0 -> {
                    ListTag var4_4 = (ListTag)collectionTag3;
                    yield Optional.of(new GenericListCollector(var4_4));
                }
                case 1 -> {
                    ByteArrayTag var5_5 = (ByteArrayTag)collectionTag3;
                    yield Optional.of(new ByteListCollector(var5_5.getAsByteArray()));
                }
                case 2 -> {
                    IntArrayTag var6_6 = (IntArrayTag)collectionTag3;
                    yield Optional.of(new IntListCollector(var6_6.getAsIntArray()));
                }
                case 3 -> {
                    LongArrayTag var7_7 = (LongArrayTag)collectionTag3;
                    yield Optional.of(new LongListCollector(var7_7.getAsLongArray()));
                }
            };
        }
        return Optional.empty();
    }

    public /* synthetic */ Object remove(Object object, String string) {
        return this.remove((Tag)object, string);
    }

    public /* synthetic */ Object createLongList(LongStream longStream) {
        return this.createLongList(longStream);
    }

    public /* synthetic */ DataResult getLongStream(Object object) {
        return this.getLongStream((Tag)object);
    }

    public /* synthetic */ Object createIntList(IntStream intStream) {
        return this.createIntList(intStream);
    }

    public /* synthetic */ DataResult getIntStream(Object object) {
        return this.getIntStream((Tag)object);
    }

    public /* synthetic */ Object createByteList(ByteBuffer byteBuffer) {
        return this.createByteList(byteBuffer);
    }

    public /* synthetic */ DataResult getByteBuffer(Object object) {
        return this.getByteBuffer((Tag)object);
    }

    public /* synthetic */ Object createList(Stream stream) {
        return this.createList(stream);
    }

    public /* synthetic */ DataResult getList(Object object) {
        return this.getList((Tag)object);
    }

    public /* synthetic */ DataResult getStream(Object object) {
        return this.getStream((Tag)object);
    }

    public /* synthetic */ DataResult getMap(Object object) {
        return this.getMap((Tag)object);
    }

    public /* synthetic */ Object createMap(Stream stream) {
        return this.createMap(stream);
    }

    public /* synthetic */ DataResult getMapEntries(Object object) {
        return this.getMapEntries((Tag)object);
    }

    public /* synthetic */ DataResult getMapValues(Object object) {
        return this.getMapValues((Tag)object);
    }

    public /* synthetic */ DataResult mergeToMap(Object object, MapLike mapLike) {
        return this.mergeToMap((Tag)object, (MapLike<Tag>)mapLike);
    }

    public /* synthetic */ DataResult mergeToMap(Object object, Map map) {
        return this.mergeToMap((Tag)object, (Map<Tag, Tag>)map);
    }

    public /* synthetic */ DataResult mergeToMap(Object object, Object object2, Object object3) {
        return this.mergeToMap((Tag)object, (Tag)object2, (Tag)object3);
    }

    public /* synthetic */ DataResult mergeToList(Object object, List list) {
        return this.mergeToList((Tag)object, (List<Tag>)list);
    }

    public /* synthetic */ DataResult mergeToList(Object object, Object object2) {
        return this.mergeToList((Tag)object, (Tag)object2);
    }

    public /* synthetic */ Object createString(String string) {
        return this.createString(string);
    }

    public /* synthetic */ DataResult getStringValue(Object object) {
        return this.getStringValue((Tag)object);
    }

    public /* synthetic */ Object createBoolean(boolean bl) {
        return this.createBoolean(bl);
    }

    public /* synthetic */ Object createDouble(double d) {
        return this.createDouble(d);
    }

    public /* synthetic */ Object createFloat(float f) {
        return this.createFloat(f);
    }

    public /* synthetic */ Object createLong(long l) {
        return this.createLong(l);
    }

    public /* synthetic */ Object createInt(int n) {
        return this.createInt(n);
    }

    public /* synthetic */ Object createShort(short s) {
        return this.createShort(s);
    }

    public /* synthetic */ Object createByte(byte by) {
        return this.createByte(by);
    }

    public /* synthetic */ Object createNumeric(Number number) {
        return this.createNumeric(number);
    }

    public /* synthetic */ DataResult getNumberValue(Object object) {
        return this.getNumberValue((Tag)object);
    }

    public /* synthetic */ Object convertTo(DynamicOps dynamicOps, Object object) {
        return this.convertTo(dynamicOps, (Tag)object);
    }

    public /* synthetic */ Object empty() {
        return this.empty();
    }

    private static /* synthetic */ String lambda$mergeToMap$15(List list) {
        return "some keys are not strings: " + String.valueOf(list);
    }

    private static /* synthetic */ String lambda$mergeToMap$13(List list) {
        return "some keys are not strings: " + String.valueOf(list);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static /* synthetic */ void lambda$mergeToMap$12(List list, CompoundTag compoundTag, Pair pair) {
        String string;
        Tag tag = (Tag)pair.getFirst();
        if (!(tag instanceof StringTag)) {
            list.add(tag);
            return;
        }
        StringTag stringTag = (StringTag)tag;
        try {
            String string2;
            string = string2 = stringTag.value();
        }
        catch (Throwable throwable) {
            throw new MatchException(throwable.toString(), throwable);
        }
        compoundTag.put(string, (Tag)pair.getSecond());
    }

    class NbtRecordBuilder
    extends RecordBuilder.AbstractStringBuilder<Tag, CompoundTag> {
        protected NbtRecordBuilder(NbtOps nbtOps) {
            super((DynamicOps)nbtOps);
        }

        protected CompoundTag initBuilder() {
            return new CompoundTag();
        }

        protected CompoundTag append(String string, Tag tag, CompoundTag compoundTag) {
            compoundTag.put(string, tag);
            return compoundTag;
        }

        protected DataResult<Tag> build(CompoundTag compoundTag, Tag tag) {
            if (tag == null || tag == EndTag.INSTANCE) {
                return DataResult.success((Object)compoundTag);
            }
            if (tag instanceof CompoundTag) {
                CompoundTag compoundTag2 = (CompoundTag)tag;
                CompoundTag compoundTag3 = compoundTag2.shallowCopy();
                for (Map.Entry<String, Tag> entry : compoundTag.entrySet()) {
                    compoundTag3.put(entry.getKey(), entry.getValue());
                }
                return DataResult.success((Object)compoundTag3);
            }
            return DataResult.error(() -> "mergeToMap called with not a map: " + String.valueOf(tag), (Object)tag);
        }

        protected /* synthetic */ Object append(String string, Object object, Object object2) {
            return this.append(string, (Tag)object, (CompoundTag)object2);
        }

        protected /* synthetic */ DataResult build(Object object, Object object2) {
            return this.build((CompoundTag)object, (Tag)object2);
        }

        protected /* synthetic */ Object initBuilder() {
            return this.initBuilder();
        }
    }

    static class GenericListCollector
    implements ListCollector {
        private final ListTag result = new ListTag();

        GenericListCollector() {
        }

        GenericListCollector(ListTag listTag) {
            this.result.addAll(listTag);
        }

        public GenericListCollector(IntArrayList intArrayList) {
            intArrayList.forEach(n -> this.result.add(IntTag.valueOf(n)));
        }

        public GenericListCollector(ByteArrayList byteArrayList) {
            byteArrayList.forEach(by -> this.result.add(ByteTag.valueOf(by)));
        }

        public GenericListCollector(LongArrayList longArrayList) {
            longArrayList.forEach(l -> this.result.add(LongTag.valueOf(l)));
        }

        @Override
        public ListCollector accept(Tag tag) {
            this.result.add(tag);
            return this;
        }

        @Override
        public Tag result() {
            return this.result;
        }
    }

    static class ByteListCollector
    implements ListCollector {
        private final ByteArrayList values = new ByteArrayList();

        public ByteListCollector(byte[] byArray) {
            this.values.addElements(0, byArray);
        }

        @Override
        public ListCollector accept(Tag tag) {
            if (tag instanceof ByteTag) {
                ByteTag byteTag = (ByteTag)tag;
                this.values.add(byteTag.byteValue());
                return this;
            }
            return new GenericListCollector(this.values).accept(tag);
        }

        @Override
        public Tag result() {
            return new ByteArrayTag(this.values.toByteArray());
        }
    }

    static class IntListCollector
    implements ListCollector {
        private final IntArrayList values = new IntArrayList();

        public IntListCollector(int[] nArray) {
            this.values.addElements(0, nArray);
        }

        @Override
        public ListCollector accept(Tag tag) {
            if (tag instanceof IntTag) {
                IntTag intTag = (IntTag)tag;
                this.values.add(intTag.intValue());
                return this;
            }
            return new GenericListCollector(this.values).accept(tag);
        }

        @Override
        public Tag result() {
            return new IntArrayTag(this.values.toIntArray());
        }
    }

    static class LongListCollector
    implements ListCollector {
        private final LongArrayList values = new LongArrayList();

        public LongListCollector(long[] lArray) {
            this.values.addElements(0, lArray);
        }

        @Override
        public ListCollector accept(Tag tag) {
            if (tag instanceof LongTag) {
                LongTag longTag = (LongTag)tag;
                this.values.add(longTag.longValue());
                return this;
            }
            return new GenericListCollector(this.values).accept(tag);
        }

        @Override
        public Tag result() {
            return new LongArrayTag(this.values.toLongArray());
        }
    }

    static interface ListCollector {
        public ListCollector accept(Tag var1);

        default public ListCollector acceptAll(Iterable<Tag> iterable) {
            ListCollector listCollector = this;
            for (Tag tag : iterable) {
                listCollector = listCollector.accept(tag);
            }
            return listCollector;
        }

        default public ListCollector acceptAll(Stream<Tag> stream) {
            return this.acceptAll(stream::iterator);
        }

        public Tag result();
    }
}

