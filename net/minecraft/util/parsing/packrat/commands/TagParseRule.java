/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  javax.annotation.Nullable
 */
package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import javax.annotation.Nullable;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Rule;

public class TagParseRule<T>
implements Rule<StringReader, Dynamic<?>> {
    private final TagParser<T> parser;

    public TagParseRule(DynamicOps<T> dynamicOps) {
        this.parser = TagParser.create(dynamicOps);
    }

    @Override
    @Nullable
    public Dynamic<T> parse(ParseState<StringReader> parseState) {
        parseState.input().skipWhitespace();
        int n = parseState.mark();
        try {
            return new Dynamic(this.parser.getOps(), this.parser.parseAsArgument(parseState.input()));
        }
        catch (Exception exception) {
            parseState.errorCollector().store(n, exception);
            return null;
        }
    }

    @Override
    @Nullable
    public /* synthetic */ Object parse(ParseState parseState) {
        return this.parse((ParseState<StringReader>)parseState);
    }
}

