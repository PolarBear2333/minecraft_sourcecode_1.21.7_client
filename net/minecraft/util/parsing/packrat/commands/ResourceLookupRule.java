/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  javax.annotation.Nullable
 */
package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.parsing.packrat.DelayedException;
import net.minecraft.util.parsing.packrat.NamedRule;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Rule;
import net.minecraft.util.parsing.packrat.commands.ResourceSuggestion;

public abstract class ResourceLookupRule<C, V>
implements Rule<StringReader, V>,
ResourceSuggestion {
    private final NamedRule<StringReader, ResourceLocation> idParser;
    protected final C context;
    private final DelayedException<CommandSyntaxException> error;

    protected ResourceLookupRule(NamedRule<StringReader, ResourceLocation> namedRule, C c) {
        this.idParser = namedRule;
        this.context = c;
        this.error = DelayedException.create(ResourceLocation.ERROR_INVALID);
    }

    @Override
    @Nullable
    public V parse(ParseState<StringReader> parseState) {
        parseState.input().skipWhitespace();
        int n = parseState.mark();
        ResourceLocation resourceLocation = parseState.parse(this.idParser);
        if (resourceLocation != null) {
            try {
                return this.validateElement((ImmutableStringReader)parseState.input(), resourceLocation);
            }
            catch (Exception exception) {
                parseState.errorCollector().store(n, this, exception);
                return null;
            }
        }
        parseState.errorCollector().store(n, this, this.error);
        return null;
    }

    protected abstract V validateElement(ImmutableStringReader var1, ResourceLocation var2) throws Exception;
}

