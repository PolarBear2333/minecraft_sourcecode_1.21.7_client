/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 */
package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.Objective;

public class ObjectiveArgument
implements ArgumentType<String> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "*", "012");
    private static final DynamicCommandExceptionType ERROR_OBJECTIVE_NOT_FOUND = new DynamicCommandExceptionType(object -> Component.translatableEscape("arguments.objective.notFound", object));
    private static final DynamicCommandExceptionType ERROR_OBJECTIVE_READ_ONLY = new DynamicCommandExceptionType(object -> Component.translatableEscape("arguments.objective.readonly", object));

    public static ObjectiveArgument objective() {
        return new ObjectiveArgument();
    }

    public static Objective getObjective(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        String string2 = (String)commandContext.getArgument(string, String.class);
        ServerScoreboard serverScoreboard = ((CommandSourceStack)commandContext.getSource()).getServer().getScoreboard();
        Objective objective = serverScoreboard.getObjective(string2);
        if (objective == null) {
            throw ERROR_OBJECTIVE_NOT_FOUND.create((Object)string2);
        }
        return objective;
    }

    public static Objective getWritableObjective(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        Objective objective = ObjectiveArgument.getObjective(commandContext, string);
        if (objective.getCriteria().isReadOnly()) {
            throw ERROR_OBJECTIVE_READ_ONLY.create((Object)objective.getName());
        }
        return objective;
    }

    public String parse(StringReader stringReader) throws CommandSyntaxException {
        return stringReader.readUnquotedString();
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
        Object object = commandContext.getSource();
        if (object instanceof CommandSourceStack) {
            CommandSourceStack commandSourceStack = (CommandSourceStack)object;
            return SharedSuggestionProvider.suggest(commandSourceStack.getServer().getScoreboard().getObjectiveNames(), suggestionsBuilder);
        }
        if (object instanceof SharedSuggestionProvider) {
            SharedSuggestionProvider sharedSuggestionProvider = (SharedSuggestionProvider)object;
            return sharedSuggestionProvider.customSuggestion(commandContext);
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

