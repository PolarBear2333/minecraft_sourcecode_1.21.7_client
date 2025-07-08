/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  javax.annotation.Nullable
 */
package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.util.parsing.packrat.DelayedException;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Rule;

public class UnquotedStringParseRule
implements Rule<StringReader, String> {
    private final int minSize;
    private final DelayedException<CommandSyntaxException> error;

    public UnquotedStringParseRule(int n, DelayedException<CommandSyntaxException> delayedException) {
        this.minSize = n;
        this.error = delayedException;
    }

    @Override
    @Nullable
    public String parse(ParseState<StringReader> parseState) {
        parseState.input().skipWhitespace();
        int n = parseState.mark();
        String string = parseState.input().readUnquotedString();
        if (string.length() < this.minSize) {
            parseState.errorCollector().store(n, this.error);
            return null;
        }
        return string;
    }

    @Override
    @Nullable
    public /* synthetic */ Object parse(ParseState parseState) {
        return this.parse(parseState);
    }
}

