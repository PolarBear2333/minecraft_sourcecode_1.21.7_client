/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 */
package net.minecraft.commands.arguments.coordinates;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.commands.arguments.coordinates.WorldCoordinate;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ColumnPos;

public class ColumnPosArgument
implements ArgumentType<Coordinates> {
    private static final Collection<String> EXAMPLES = Arrays.asList("0 0", "~ ~", "~1 ~-2", "^ ^", "^-1 ^0");
    public static final SimpleCommandExceptionType ERROR_NOT_COMPLETE = new SimpleCommandExceptionType((Message)Component.translatable("argument.pos2d.incomplete"));

    public static ColumnPosArgument columnPos() {
        return new ColumnPosArgument();
    }

    public static ColumnPos getColumnPos(CommandContext<CommandSourceStack> commandContext, String string) {
        BlockPos blockPos = ((Coordinates)commandContext.getArgument(string, Coordinates.class)).getBlockPos((CommandSourceStack)commandContext.getSource());
        return new ColumnPos(blockPos.getX(), blockPos.getZ());
    }

    public Coordinates parse(StringReader stringReader) throws CommandSyntaxException {
        int n = stringReader.getCursor();
        if (!stringReader.canRead()) {
            throw ERROR_NOT_COMPLETE.createWithContext((ImmutableStringReader)stringReader);
        }
        WorldCoordinate worldCoordinate = WorldCoordinate.parseInt(stringReader);
        if (!stringReader.canRead() || stringReader.peek() != ' ') {
            stringReader.setCursor(n);
            throw ERROR_NOT_COMPLETE.createWithContext((ImmutableStringReader)stringReader);
        }
        stringReader.skip();
        WorldCoordinate worldCoordinate2 = WorldCoordinate.parseInt(stringReader);
        return new WorldCoordinates(worldCoordinate, new WorldCoordinate(true, 0.0), worldCoordinate2);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
        if (commandContext.getSource() instanceof SharedSuggestionProvider) {
            String string = suggestionsBuilder.getRemaining();
            Collection<SharedSuggestionProvider.TextCoordinates> collection = !string.isEmpty() && string.charAt(0) == '^' ? Collections.singleton(SharedSuggestionProvider.TextCoordinates.DEFAULT_LOCAL) : ((SharedSuggestionProvider)commandContext.getSource()).getRelevantCoordinates();
            return SharedSuggestionProvider.suggest2DCoordinates(string, collection, suggestionsBuilder, Commands.createValidator(this::parse));
        }
        return Suggestions.empty();
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }
}

