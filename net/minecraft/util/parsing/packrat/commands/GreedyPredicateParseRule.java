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

public abstract class GreedyPredicateParseRule
implements Rule<StringReader, String> {
    private final int minSize;
    private final int maxSize;
    private final DelayedException<CommandSyntaxException> error;

    public GreedyPredicateParseRule(int n, DelayedException<CommandSyntaxException> delayedException) {
        this(n, Integer.MAX_VALUE, delayedException);
    }

    public GreedyPredicateParseRule(int n, int n2, DelayedException<CommandSyntaxException> delayedException) {
        this.minSize = n;
        this.maxSize = n2;
        this.error = delayedException;
    }

    @Override
    @Nullable
    public String parse(ParseState<StringReader> parseState) {
        int n;
        int n2;
        StringReader stringReader = parseState.input();
        String string = stringReader.getString();
        for (n2 = n = stringReader.getCursor(); n2 < string.length() && this.isAccepted(string.charAt(n2)) && n2 - n < this.maxSize; ++n2) {
        }
        int n3 = n2 - n;
        if (n3 < this.minSize) {
            parseState.errorCollector().store(parseState.mark(), this.error);
            return null;
        }
        stringReader.setCursor(n2);
        return string.substring(n, n2);
    }

    protected abstract boolean isAccepted(char var1);

    @Override
    @Nullable
    public /* synthetic */ Object parse(ParseState parseState) {
        return this.parse(parseState);
    }
}

