/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.ArrayUtils
 */
package net.minecraft.nbt;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.StringTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagVisitor;
import org.apache.commons.lang3.ArrayUtils;

public final class LongArrayTag
implements CollectionTag {
    private static final int SELF_SIZE_IN_BYTES = 24;
    public static final TagType<LongArrayTag> TYPE = new TagType.VariableSize<LongArrayTag>(){

        @Override
        public LongArrayTag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            return new LongArrayTag(1.readAccounted(dataInput, nbtAccounter));
        }

        @Override
        public StreamTagVisitor.ValueResult parse(DataInput dataInput, StreamTagVisitor streamTagVisitor, NbtAccounter nbtAccounter) throws IOException {
            return streamTagVisitor.visit(1.readAccounted(dataInput, nbtAccounter));
        }

        private static long[] readAccounted(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.accountBytes(24L);
            int n = dataInput.readInt();
            nbtAccounter.accountBytes(8L, n);
            long[] lArray = new long[n];
            for (int i = 0; i < n; ++i) {
                lArray[i] = dataInput.readLong();
            }
            return lArray;
        }

        @Override
        public void skip(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            dataInput.skipBytes(dataInput.readInt() * 8);
        }

        @Override
        public String getName() {
            return "LONG[]";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Long_Array";
        }

        @Override
        public /* synthetic */ Tag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            return this.load(dataInput, nbtAccounter);
        }
    };
    private long[] data;

    public LongArrayTag(long[] lArray) {
        this.data = lArray;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(this.data.length);
        for (long l : this.data) {
            dataOutput.writeLong(l);
        }
    }

    @Override
    public int sizeInBytes() {
        return 24 + 8 * this.data.length;
    }

    @Override
    public byte getId() {
        return 12;
    }

    public TagType<LongArrayTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        StringTagVisitor stringTagVisitor = new StringTagVisitor();
        stringTagVisitor.visitLongArray(this);
        return stringTagVisitor.build();
    }

    @Override
    public LongArrayTag copy() {
        long[] lArray = new long[this.data.length];
        System.arraycopy(this.data, 0, lArray, 0, this.data.length);
        return new LongArrayTag(lArray);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof LongArrayTag && Arrays.equals(this.data, ((LongArrayTag)object).data);
    }

    public int hashCode() {
        return Arrays.hashCode(this.data);
    }

    @Override
    public void accept(TagVisitor tagVisitor) {
        tagVisitor.visitLongArray(this);
    }

    public long[] getAsLongArray() {
        return this.data;
    }

    @Override
    public int size() {
        return this.data.length;
    }

    @Override
    public LongTag get(int n) {
        return LongTag.valueOf(this.data[n]);
    }

    @Override
    public boolean setTag(int n, Tag tag) {
        if (tag instanceof NumericTag) {
            NumericTag numericTag = (NumericTag)tag;
            this.data[n] = numericTag.longValue();
            return true;
        }
        return false;
    }

    @Override
    public boolean addTag(int n, Tag tag) {
        if (tag instanceof NumericTag) {
            NumericTag numericTag = (NumericTag)tag;
            this.data = ArrayUtils.add((long[])this.data, (int)n, (long)numericTag.longValue());
            return true;
        }
        return false;
    }

    @Override
    public LongTag remove(int n) {
        long l = this.data[n];
        this.data = ArrayUtils.remove((long[])this.data, (int)n);
        return LongTag.valueOf(l);
    }

    @Override
    public void clear() {
        this.data = new long[0];
    }

    @Override
    public Optional<long[]> asLongArray() {
        return Optional.of(this.data);
    }

    @Override
    public StreamTagVisitor.ValueResult accept(StreamTagVisitor streamTagVisitor) {
        return streamTagVisitor.visit(this.data);
    }

    @Override
    public /* synthetic */ Tag get(int n) {
        return this.get(n);
    }

    @Override
    public /* synthetic */ Tag remove(int n) {
        return this.remove(n);
    }

    @Override
    public /* synthetic */ Tag copy() {
        return this.copy();
    }
}

