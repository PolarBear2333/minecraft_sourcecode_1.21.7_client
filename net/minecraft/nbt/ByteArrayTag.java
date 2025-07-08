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
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.StringTagVisitor;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.TagVisitor;
import org.apache.commons.lang3.ArrayUtils;

public final class ByteArrayTag
implements CollectionTag {
    private static final int SELF_SIZE_IN_BYTES = 24;
    public static final TagType<ByteArrayTag> TYPE = new TagType.VariableSize<ByteArrayTag>(){

        @Override
        public ByteArrayTag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            return new ByteArrayTag(1.readAccounted(dataInput, nbtAccounter));
        }

        @Override
        public StreamTagVisitor.ValueResult parse(DataInput dataInput, StreamTagVisitor streamTagVisitor, NbtAccounter nbtAccounter) throws IOException {
            return streamTagVisitor.visit(1.readAccounted(dataInput, nbtAccounter));
        }

        private static byte[] readAccounted(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            nbtAccounter.accountBytes(24L);
            int n = dataInput.readInt();
            nbtAccounter.accountBytes(1L, n);
            byte[] byArray = new byte[n];
            dataInput.readFully(byArray);
            return byArray;
        }

        @Override
        public void skip(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            dataInput.skipBytes(dataInput.readInt() * 1);
        }

        @Override
        public String getName() {
            return "BYTE[]";
        }

        @Override
        public String getPrettyName() {
            return "TAG_Byte_Array";
        }

        @Override
        public /* synthetic */ Tag load(DataInput dataInput, NbtAccounter nbtAccounter) throws IOException {
            return this.load(dataInput, nbtAccounter);
        }
    };
    private byte[] data;

    public ByteArrayTag(byte[] byArray) {
        this.data = byArray;
    }

    @Override
    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeInt(this.data.length);
        dataOutput.write(this.data);
    }

    @Override
    public int sizeInBytes() {
        return 24 + 1 * this.data.length;
    }

    @Override
    public byte getId() {
        return 7;
    }

    public TagType<ByteArrayTag> getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        StringTagVisitor stringTagVisitor = new StringTagVisitor();
        stringTagVisitor.visitByteArray(this);
        return stringTagVisitor.build();
    }

    @Override
    public Tag copy() {
        byte[] byArray = new byte[this.data.length];
        System.arraycopy(this.data, 0, byArray, 0, this.data.length);
        return new ByteArrayTag(byArray);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        return object instanceof ByteArrayTag && Arrays.equals(this.data, ((ByteArrayTag)object).data);
    }

    public int hashCode() {
        return Arrays.hashCode(this.data);
    }

    @Override
    public void accept(TagVisitor tagVisitor) {
        tagVisitor.visitByteArray(this);
    }

    public byte[] getAsByteArray() {
        return this.data;
    }

    @Override
    public int size() {
        return this.data.length;
    }

    @Override
    public ByteTag get(int n) {
        return ByteTag.valueOf(this.data[n]);
    }

    @Override
    public boolean setTag(int n, Tag tag) {
        if (tag instanceof NumericTag) {
            NumericTag numericTag = (NumericTag)tag;
            this.data[n] = numericTag.byteValue();
            return true;
        }
        return false;
    }

    @Override
    public boolean addTag(int n, Tag tag) {
        if (tag instanceof NumericTag) {
            NumericTag numericTag = (NumericTag)tag;
            this.data = ArrayUtils.add((byte[])this.data, (int)n, (byte)numericTag.byteValue());
            return true;
        }
        return false;
    }

    @Override
    public ByteTag remove(int n) {
        byte by = this.data[n];
        this.data = ArrayUtils.remove((byte[])this.data, (int)n);
        return ByteTag.valueOf(by);
    }

    @Override
    public void clear() {
        this.data = new byte[0];
    }

    @Override
    public Optional<byte[]> asByteArray() {
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
}

