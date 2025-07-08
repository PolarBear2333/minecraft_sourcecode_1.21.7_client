/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.nbt.visitors;

import java.util.ArrayDeque;
import java.util.Deque;
import javax.annotation.Nullable;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
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
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagType;

public class CollectToTag
implements StreamTagVisitor {
    private final Deque<ContainerBuilder> containerStack = new ArrayDeque<ContainerBuilder>();

    public CollectToTag() {
        this.containerStack.addLast(new RootBuilder());
    }

    @Nullable
    public Tag getResult() {
        return this.containerStack.getFirst().build();
    }

    protected int depth() {
        return this.containerStack.size() - 1;
    }

    private void appendEntry(Tag tag) {
        this.containerStack.getLast().acceptValue(tag);
    }

    @Override
    public StreamTagVisitor.ValueResult visitEnd() {
        this.appendEntry(EndTag.INSTANCE);
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(String string) {
        this.appendEntry(StringTag.valueOf(string));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(byte by) {
        this.appendEntry(ByteTag.valueOf(by));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(short s) {
        this.appendEntry(ShortTag.valueOf(s));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(int n) {
        this.appendEntry(IntTag.valueOf(n));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(long l) {
        this.appendEntry(LongTag.valueOf(l));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(float f) {
        this.appendEntry(FloatTag.valueOf(f));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(double d) {
        this.appendEntry(DoubleTag.valueOf(d));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(byte[] byArray) {
        this.appendEntry(new ByteArrayTag(byArray));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(int[] nArray) {
        this.appendEntry(new IntArrayTag(nArray));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visit(long[] lArray) {
        this.appendEntry(new LongArrayTag(lArray));
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visitList(TagType<?> tagType, int n) {
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.EntryResult visitElement(TagType<?> tagType, int n) {
        this.enterContainerIfNeeded(tagType);
        return StreamTagVisitor.EntryResult.ENTER;
    }

    @Override
    public StreamTagVisitor.EntryResult visitEntry(TagType<?> tagType) {
        return StreamTagVisitor.EntryResult.ENTER;
    }

    @Override
    public StreamTagVisitor.EntryResult visitEntry(TagType<?> tagType, String string) {
        this.containerStack.getLast().acceptKey(string);
        this.enterContainerIfNeeded(tagType);
        return StreamTagVisitor.EntryResult.ENTER;
    }

    private void enterContainerIfNeeded(TagType<?> tagType) {
        if (tagType == ListTag.TYPE) {
            this.containerStack.addLast(new ListBuilder());
        } else if (tagType == CompoundTag.TYPE) {
            this.containerStack.addLast(new CompoundBuilder());
        }
    }

    @Override
    public StreamTagVisitor.ValueResult visitContainerEnd() {
        ContainerBuilder containerBuilder = this.containerStack.removeLast();
        Tag tag = containerBuilder.build();
        if (tag != null) {
            this.containerStack.getLast().acceptValue(tag);
        }
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    @Override
    public StreamTagVisitor.ValueResult visitRootEntry(TagType<?> tagType) {
        this.enterContainerIfNeeded(tagType);
        return StreamTagVisitor.ValueResult.CONTINUE;
    }

    static class RootBuilder
    implements ContainerBuilder {
        @Nullable
        private Tag result;

        RootBuilder() {
        }

        @Override
        public void acceptValue(Tag tag) {
            this.result = tag;
        }

        @Override
        @Nullable
        public Tag build() {
            return this.result;
        }
    }

    static interface ContainerBuilder {
        default public void acceptKey(String string) {
        }

        public void acceptValue(Tag var1);

        @Nullable
        public Tag build();
    }

    static class ListBuilder
    implements ContainerBuilder {
        private final ListTag list = new ListTag();

        ListBuilder() {
        }

        @Override
        public void acceptValue(Tag tag) {
            this.list.addAndUnwrap(tag);
        }

        @Override
        public Tag build() {
            return this.list;
        }
    }

    static class CompoundBuilder
    implements ContainerBuilder {
        private final CompoundTag compound = new CompoundTag();
        private String lastId = "";

        CompoundBuilder() {
        }

        @Override
        public void acceptKey(String string) {
            this.lastId = string;
        }

        @Override
        public void acceptValue(Tag tag) {
            this.compound.put(this.lastId, tag);
        }

        @Override
        public Tag build() {
            return this.compound;
        }
    }
}

