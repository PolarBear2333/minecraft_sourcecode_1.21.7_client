/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.nbt.visitors;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.nbt.TagType;
import net.minecraft.nbt.visitors.FieldSelector;

public record FieldTree(int depth, Map<String, TagType<?>> selectedFields, Map<String, FieldTree> fieldsToRecurse) {
    private FieldTree(int n) {
        this(n, new HashMap(), new HashMap<String, FieldTree>());
    }

    public static FieldTree createRoot() {
        return new FieldTree(1);
    }

    public void addEntry(FieldSelector fieldSelector) {
        if (this.depth <= fieldSelector.path().size()) {
            this.fieldsToRecurse.computeIfAbsent(fieldSelector.path().get(this.depth - 1), string -> new FieldTree(this.depth + 1)).addEntry(fieldSelector);
        } else {
            this.selectedFields.put(fieldSelector.name(), fieldSelector.type());
        }
    }

    public boolean isSelected(TagType<?> tagType, String string) {
        return tagType.equals(this.selectedFields().get(string));
    }
}

