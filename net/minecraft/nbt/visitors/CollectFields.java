/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.ImmutableSet$Builder
 */
package net.minecraft.nbt.visitors;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StreamTagVisitor;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.visitors.CollectToTag;
import net.minecraft.nbt.visitors.FieldSelector;
import net.minecraft.nbt.visitors.FieldTree;

public class CollectFields
extends CollectToTag {
    private int fieldsToGetCount;
    private final Set<TagType<?>> wantedTypes;
    private final Deque<FieldTree> stack = new ArrayDeque<FieldTree>();

    public CollectFields(FieldSelector ... fieldSelectorArray) {
        this.fieldsToGetCount = fieldSelectorArray.length;
        ImmutableSet.Builder builder = ImmutableSet.builder();
        FieldTree fieldTree = FieldTree.createRoot();
        for (FieldSelector fieldSelector : fieldSelectorArray) {
            fieldTree.addEntry(fieldSelector);
            builder.add(fieldSelector.type());
        }
        this.stack.push(fieldTree);
        builder.add(CompoundTag.TYPE);
        this.wantedTypes = builder.build();
    }

    @Override
    public StreamTagVisitor.ValueResult visitRootEntry(TagType<?> tagType) {
        if (tagType != CompoundTag.TYPE) {
            return StreamTagVisitor.ValueResult.HALT;
        }
        return super.visitRootEntry(tagType);
    }

    @Override
    public StreamTagVisitor.EntryResult visitEntry(TagType<?> tagType) {
        FieldTree fieldTree = this.stack.element();
        if (this.depth() > fieldTree.depth()) {
            return super.visitEntry(tagType);
        }
        if (this.fieldsToGetCount <= 0) {
            return StreamTagVisitor.EntryResult.BREAK;
        }
        if (!this.wantedTypes.contains(tagType)) {
            return StreamTagVisitor.EntryResult.SKIP;
        }
        return super.visitEntry(tagType);
    }

    @Override
    public StreamTagVisitor.EntryResult visitEntry(TagType<?> tagType, String string) {
        FieldTree fieldTree;
        FieldTree fieldTree2 = this.stack.element();
        if (this.depth() > fieldTree2.depth()) {
            return super.visitEntry(tagType, string);
        }
        if (fieldTree2.selectedFields().remove(string, tagType)) {
            --this.fieldsToGetCount;
            return super.visitEntry(tagType, string);
        }
        if (tagType == CompoundTag.TYPE && (fieldTree = fieldTree2.fieldsToRecurse().get(string)) != null) {
            this.stack.push(fieldTree);
            return super.visitEntry(tagType, string);
        }
        return StreamTagVisitor.EntryResult.SKIP;
    }

    @Override
    public StreamTagVisitor.ValueResult visitContainerEnd() {
        if (this.depth() == this.stack.element().depth()) {
            this.stack.pop();
        }
        return super.visitContainerEnd();
    }

    public int getMissingFieldCount() {
        return this.fieldsToGetCount;
    }
}

