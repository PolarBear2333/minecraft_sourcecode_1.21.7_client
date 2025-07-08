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
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.StringTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagVisitor;
import org.apache.commons.lang3.ArrayUtils;

public final class IntArrayTag
implements CollectionTag {
    private static final int SELF_SIZE_IN_BYTES = 24;
    public static final TagType<IntArrayTag> TYPE = new TagType.VariableSize<IntArrayTag>(){

        @Override
        public IntArrayTag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            return new IntArrayTag(1.readAccounted(dataInput, nbtAccounter));
        }

        @Override
        public StreamTagVisitor.ValueResult parse(DataInput dataInput, StreamTagVisitor streamTagVisitor, NbtAccounter nbtAccounter) throws IOException {
            return streamTagVisitor.visit(1.readAccounted(dataInput, nbtAccounter));
        }

        private static int[] readAccounted(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.accountBytes(24L);
            int n = dataInput.readInt();
            nbtAccounter.accountBytes(4L, n);
            int[] nArray = new int[n];
            for (int i = 0; i < n; ++i) {
                nArray[i] = dataInput.readInt();
            }
            return nArray;
        }

        @Override
        public void skip(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            dataInput.skipBytes(dataInput.readInt() * 4);
        }

        @Override
        public String getName() {
            return "INT[]";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Int_Array";
        }

        @Override
        public /* synthetic */ Tag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            return this.load(dataInput, nbtAccounter);
        }
    };
    private int[] data;

    public IntArrayTag(int[] nArray) {
        this.data = nArray;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(this.data.length);
        for (int n : this.data) {
            dataOutput.writeInt(n);
        }
    }

    @Override
    public int sizeInBytes() {
        return 24 + 4 * this.data.length;
    }

    @Override
    public byte getId() {
        return 11;
    }

    public TagType<IntArrayTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        StringTagVisitor stringTagVisitor = new StringTagVisitor();
        stringTagVisitor.visitIntArray(this);
        return stringTagVisitor.build();
    }

    @Override
    public IntArrayTag copy() {
        int[] nArray = new int[this.data.length];
        System.arraycopy(this.data, 0, nArray, 0, this.data.length);
        return new IntArrayTag(nArray);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof IntArrayTag && Arrays.equals(this.data, ((IntArrayTag)object).data);
    }

    public int hashCode() {
        return Arrays.hashCode(this.data);
    }

    public int[] getAsIntArray() {
        return this.data;
    }

    @Override
    public void accept(TagVisitor tagVisitor) {
        tagVisitor.visitIntArray(this);
    }

    @Override
    public int size() {
        return this.data.length;
    }

    @Override
    public IntTag get(int n) {
        return IntTag.valueOf(this.data[n]);
    }

    @Override
    public boolean setTag(int n, Tag tag) {
        if (tag instanceof NumericTag) {
            NumericTag numericTag = (NumericTag)tag;
            this.data[n] = numericTag.intValue();
            return true;
        }
        return false;
    }

    @Override
    public boolean addTag(int n, Tag tag) {
        if (tag instanceof NumericTag) {
            NumericTag numericTag = (NumericTag)tag;
            this.data = ArrayUtils.add((int[])this.data, (int)n, (int)numericTag.intValue());
            return true;
        }
        return false;
    }

    @Override
    public IntTag remove(int n) {
        int n2 = this.data[n];
        this.data = ArrayUtils.remove((int[])this.data, (int)n);
        return IntTag.valueOf(n2);
    }

    @Override
    public void clear() {
        this.data = new int[0];
    }

    @Override
    public Optional<int[]> asIntArray() {
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

