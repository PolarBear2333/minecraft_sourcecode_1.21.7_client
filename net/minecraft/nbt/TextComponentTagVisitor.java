/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Strings
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.nbt;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
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
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagVisitor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.slf4j.Logger;

public class TextComponentTagVisitor
implements TagVisitor {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int INLINE_LIST_THRESHOLD = 8;
    private static final int MAX_DEPTH = 64;
    private static final int MAX_LENGTH = 128;
    private static final ChatFormatting SYNTAX_HIGHLIGHTING_KEY = ChatFormatting.AQUA;
    private static final ChatFormatting SYNTAX_HIGHLIGHTING_STRING = ChatFormatting.GREEN;
    private static final ChatFormatting SYNTAX_HIGHLIGHTING_NUMBER = ChatFormatting.GOLD;
    private static final ChatFormatting SYNTAX_HIGHLIGHTING_NUMBER_TYPE = ChatFormatting.RED;
    private static final Pattern SIMPLE_VALUE = Pattern.compile("[A-Za-z0-9._+-]+");
    private static final String LIST_OPEN = "[";
    private static final String LIST_CLOSE = "]";
    private static final String LIST_TYPE_SEPARATOR = ";";
    private static final String ELEMENT_SPACING = " ";
    private static final String STRUCT_OPEN = "{";
    private static final String STRUCT_CLOSE = "}";
    private static final String NEWLINE = "\n";
    private static final String NAME_VALUE_SEPARATOR = ": ";
    private static final String ELEMENT_SEPARATOR = String.valueOf(',');
    private static final String WRAPPED_ELEMENT_SEPARATOR = ELEMENT_SEPARATOR + "\n";
    private static final String SPACED_ELEMENT_SEPARATOR = ELEMENT_SEPARATOR + " ";
    private static final Component FOLDED = Component.literal("<...>").withStyle(ChatFormatting.GRAY);
    private static final Component BYTE_TYPE = Component.literal("b").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
    private static final Component SHORT_TYPE = Component.literal("s").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
    private static final Component INT_TYPE = Component.literal("I").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
    private static final Component LONG_TYPE = Component.literal("L").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
    private static final Component FLOAT_TYPE = Component.literal("f").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
    private static final Component DOUBLE_TYPE = Component.literal("d").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
    private static final Component BYTE_ARRAY_TYPE = Component.literal("B").withStyle(SYNTAX_HIGHLIGHTING_NUMBER_TYPE);
    private final String indentation;
    private int indentDepth;
    private int depth;
    private final MutableComponent result = Component.empty();

    public TextComponentTagVisitor(String string) {
        this.indentation = string;
    }

    public Component visit(Tag tag) {
        tag.accept(this);
        return this.result;
    }

    @Override
    public void visitString(StringTag stringTag) {
        String string = StringTag.quoteAndEscape(stringTag.value());
        String string2 = string.substring(0, 1);
        MutableComponent mutableComponent = Component.literal(string.substring(1, string.length() - 1)).withStyle(SYNTAX_HIGHLIGHTING_STRING);
        this.result.append(string2).append(mutableComponent).append(string2);
    }

    @Override
    public void visitByte(ByteTag byteTag) {
        this.result.append(Component.literal(String.valueOf(byteTag.value())).withStyle(SYNTAX_HIGHLIGHTING_NUMBER)).append(BYTE_TYPE);
    }

    @Override
    public void visitShort(ShortTag shortTag) {
        this.result.append(Component.literal(String.valueOf(shortTag.value())).withStyle(SYNTAX_HIGHLIGHTING_NUMBER)).append(SHORT_TYPE);
    }

    @Override
    public void visitInt(IntTag intTag) {
        this.result.append(Component.literal(String.valueOf(intTag.value())).withStyle(SYNTAX_HIGHLIGHTING_NUMBER));
    }

    @Override
    public void visitLong(LongTag longTag) {
        this.result.append(Component.literal(String.valueOf(longTag.value())).withStyle(SYNTAX_HIGHLIGHTING_NUMBER)).append(LONG_TYPE);
    }

    @Override
    public void visitFloat(FloatTag floatTag) {
        this.result.append(Component.literal(String.valueOf(floatTag.value())).withStyle(SYNTAX_HIGHLIGHTING_NUMBER)).append(FLOAT_TYPE);
    }

    @Override
    public void visitDouble(DoubleTag doubleTag) {
        this.result.append(Component.literal(String.valueOf(doubleTag.value())).withStyle(SYNTAX_HIGHLIGHTING_NUMBER)).append(DOUBLE_TYPE);
    }

    @Override
    public void visitByteArray(ByteArrayTag byteArrayTag) {
        this.result.append(LIST_OPEN).append(BYTE_ARRAY_TYPE).append(LIST_TYPE_SEPARATOR);
        byte[] byArray = byteArrayTag.getAsByteArray();
        for (int i = 0; i < byArray.length && i < 128; ++i) {
            MutableComponent mutableComponent = Component.literal(String.valueOf(byArray[i])).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
            this.result.append(ELEMENT_SPACING).append(mutableComponent).append(BYTE_ARRAY_TYPE);
            if (i == byArray.length - 1) continue;
            this.result.append(ELEMENT_SEPARATOR);
        }
        if (byArray.length > 128) {
            this.result.append(FOLDED);
        }
        this.result.append(LIST_CLOSE);
    }

    @Override
    public void visitIntArray(IntArrayTag intArrayTag) {
        this.result.append(LIST_OPEN).append(INT_TYPE).append(LIST_TYPE_SEPARATOR);
        int[] nArray = intArrayTag.getAsIntArray();
        for (int i = 0; i < nArray.length && i < 128; ++i) {
            this.result.append(ELEMENT_SPACING).append(Component.literal(String.valueOf(nArray[i])).withStyle(SYNTAX_HIGHLIGHTING_NUMBER));
            if (i == nArray.length - 1) continue;
            this.result.append(ELEMENT_SEPARATOR);
        }
        if (nArray.length > 128) {
            this.result.append(FOLDED);
        }
        this.result.append(LIST_CLOSE);
    }

    @Override
    public void visitLongArray(LongArrayTag longArrayTag) {
        this.result.append(LIST_OPEN).append(LONG_TYPE).append(LIST_TYPE_SEPARATOR);
        long[] lArray = longArrayTag.getAsLongArray();
        for (int i = 0; i < lArray.length && i < 128; ++i) {
            MutableComponent mutableComponent = Component.literal(String.valueOf(lArray[i])).withStyle(SYNTAX_HIGHLIGHTING_NUMBER);
            this.result.append(ELEMENT_SPACING).append(mutableComponent).append(LONG_TYPE);
            if (i == lArray.length - 1) continue;
            this.result.append(ELEMENT_SEPARATOR);
        }
        if (lArray.length > 128) {
            this.result.append(FOLDED);
        }
        this.result.append(LIST_CLOSE);
    }

    private static boolean shouldWrapListElements(ListTag listTag) {
        if (listTag.size() >= 8) {
            return false;
        }
        for (Tag tag : listTag) {
            if (tag instanceof NumericTag) continue;
            return true;
        }
        return false;
    }

    @Override
    public void visitList(ListTag listTag) {
        if (listTag.isEmpty()) {
            this.result.append("[]");
            return;
        }
        if (this.depth >= 64) {
            this.result.append(LIST_OPEN).append(FOLDED).append(LIST_CLOSE);
            return;
        }
        if (!TextComponentTagVisitor.shouldWrapListElements(listTag)) {
            this.result.append(LIST_OPEN);
            for (int i = 0; i < listTag.size(); ++i) {
                if (i != 0) {
                    this.result.append(SPACED_ELEMENT_SEPARATOR);
                }
                this.appendSubTag(listTag.get(i), false);
            }
            this.result.append(LIST_CLOSE);
            return;
        }
        this.result.append(LIST_OPEN);
        if (!this.indentation.isEmpty()) {
            this.result.append(NEWLINE);
        }
        String string = Strings.repeat((String)this.indentation, (int)(this.indentDepth + 1));
        for (int i = 0; i < listTag.size() && i < 128; ++i) {
            this.result.append(string);
            this.appendSubTag(listTag.get(i), true);
            if (i == listTag.size() - 1) continue;
            this.result.append(this.indentation.isEmpty() ? SPACED_ELEMENT_SEPARATOR : WRAPPED_ELEMENT_SEPARATOR);
        }
        if (listTag.size() > 128) {
            this.result.append(string).append(FOLDED);
        }
        if (!this.indentation.isEmpty()) {
            this.result.append(NEWLINE + Strings.repeat((String)this.indentation, (int)this.indentDepth));
        }
        this.result.append(LIST_CLOSE);
    }

    @Override
    public void visitCompound(CompoundTag compoundTag) {
        Object object;
        if (compoundTag.isEmpty()) {
            this.result.append("{}");
            return;
        }
        if (this.depth >= 64) {
            this.result.append(STRUCT_OPEN).append(FOLDED).append(STRUCT_CLOSE);
            return;
        }
        this.result.append(STRUCT_OPEN);
        Collection<String> collection = compoundTag.keySet();
        if (LOGGER.isDebugEnabled()) {
            object = Lists.newArrayList(compoundTag.keySet());
            Collections.sort(object);
            collection = object;
        }
        if (!this.indentation.isEmpty()) {
            this.result.append(NEWLINE);
        }
        object = Strings.repeat((String)this.indentation, (int)(this.indentDepth + 1));
        Iterator<String> iterator = collection.iterator();
        while (iterator.hasNext()) {
            String string = iterator.next();
            this.result.append((String)object).append(TextComponentTagVisitor.handleEscapePretty(string)).append(NAME_VALUE_SEPARATOR);
            this.appendSubTag(compoundTag.get(string), true);
            if (!iterator.hasNext()) continue;
            this.result.append(this.indentation.isEmpty() ? SPACED_ELEMENT_SEPARATOR : WRAPPED_ELEMENT_SEPARATOR);
        }
        if (!this.indentation.isEmpty()) {
            this.result.append(NEWLINE + Strings.repeat((String)this.indentation, (int)this.indentDepth));
        }
        this.result.append(STRUCT_CLOSE);
    }

    private void appendSubTag(Tag tag, boolean bl) {
        if (bl) {
            ++this.indentDepth;
        }
        ++this.depth;
        try {
            tag.accept(this);
        }
        finally {
            if (bl) {
                --this.indentDepth;
            }
            --this.depth;
        }
    }

    protected static Component handleEscapePretty(String string) {
        if (SIMPLE_VALUE.matcher(string).matches()) {
            return Component.literal(string).withStyle(SYNTAX_HIGHLIGHTING_KEY);
        }
        String string2 = StringTag.quoteAndEscape(string);
        String string3 = string2.substring(0, 1);
        MutableComponent mutableComponent = Component.literal(string2.substring(1, string2.length() - 1)).withStyle(SYNTAX_HIGHLIGHTING_KEY);
        return Component.literal(string3).append(mutableComponent).append(string3);
    }

    @Override
    public void visitEnd(EndTag endTag) {
    }
}

