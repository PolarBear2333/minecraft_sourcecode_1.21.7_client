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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Rule;

public class ResourceLocationParseRule
implements Rule<StringReader, ResourceLocation> {
    public static final Rule<StringReader, ResourceLocation> INSTANCE = new ResourceLocationParseRule();

    private ResourceLocationParseRule() {
    }

    @Override
    @Nullable
    public ResourceLocation parse(ParseState<StringReader> parseState) {
        parseState.input().skipWhitespace();
        try {
            return ResourceLocation.readNonEmpty(parseState.input());
        }
        catch (CommandSyntaxException commandSyntaxException) {
            return null;
        }
    }

    @Override
    @Nullable
    public /* synthetic */ Object parse(ParseState parseState) {
        return this.parse(parseState);
    }
}

