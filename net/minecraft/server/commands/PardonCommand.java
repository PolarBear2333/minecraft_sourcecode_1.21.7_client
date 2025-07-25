/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.server.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.players.UserBanList;

public class PardonCommand {
    private static final SimpleCommandExceptionType ERROR_NOT_BANNED = new SimpleCommandExceptionType((Message)Component.translatable("commands.pardon.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("pardon").requires(Commands.hasPermission(3))).then(Commands.argument("targets", GameProfileArgument.gameProfile()).suggests((commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(((CommandSourceStack)commandContext.getSource()).getServer().getPlayerList().getBans().getUserList(), suggestionsBuilder)).executes(commandContext -> PardonCommand.pardonPlayers((CommandSourceStack)commandContext.getSource(), GameProfileArgument.getGameProfiles((CommandContext<CommandSourceStack>)commandContext, "targets")))));
    }

    private static int pardonPlayers(CommandSourceStack commandSourceStack, Collection<GameProfile> collection) throws CommandSyntaxException {
        UserBanList userBanList = commandSourceStack.getServer().getPlayerList().getBans();
        int n = 0;
        for (GameProfile gameProfile : collection) {
            if (!userBanList.isBanned(gameProfile)) continue;
            userBanList.remove(gameProfile);
            ++n;
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.pardon.success", Component.literal(gameProfile.getName())), true);
        }
        if (n == 0) {
            throw ERROR_NOT_BANNED.create();
        }
        return n;
    }
}

