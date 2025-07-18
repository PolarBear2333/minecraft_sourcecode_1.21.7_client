/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 */
package net.minecraft.commands.arguments;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.ParserUtils;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.inventory.SlotRanges;

public class SlotsArgument
implements ArgumentType<SlotRange> {
    private static final Collection<String> EXAMPLES = List.of("container.*", "container.5", "weapon");
    private static final DynamicCommandExceptionType ERROR_UNKNOWN_SLOT = new DynamicCommandExceptionType(object -> Component.translatableEscape("slot.unknown", object));

    public static SlotsArgument slots() {
        return new SlotsArgument();
    }

    public static SlotRange getSlots(CommandContext<CommandSourceStack> commandContext, String string) {
        return (SlotRange)commandContext.getArgument(string, SlotRange.class);
    }

    public SlotRange parse(StringReader stringReader) throws CommandSyntaxException {
        String string = ParserUtils.readWhile(stringReader, c -> c != ' ');
        SlotRange slotRange = SlotRanges.nameToIds(string);
        if (slotRange == null) {
            throw ERROR_UNKNOWN_SLOT.createWithContext((ImmutableStringReader)stringReader, (Object)string);
        }
        return slotRange;
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
        return SharedSuggestionProvider.suggest(SlotRanges.allNames(), suggestionsBuilder);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }
}

