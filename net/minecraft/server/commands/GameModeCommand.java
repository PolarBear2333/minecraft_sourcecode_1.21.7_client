/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameModeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;

public class GameModeCommand {
    public static final int PERMISSION_LEVEL = 2;

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("gamemode").requires(Commands.hasPermission(2))).then(((RequiredArgumentBuilder)Commands.argument("gamemode", GameModeArgument.gameMode()).executes(commandContext -> GameModeCommand.setMode((CommandContext<CommandSourceStack>)commandContext, Collections.singleton(((CommandSourceStack)commandContext.getSource()).getPlayerOrException()), GameModeArgument.getGameMode((CommandContext<CommandSourceStack>)commandContext, "gamemode")))).then(Commands.argument("target", EntityArgument.players()).executes(commandContext -> GameModeCommand.setMode((CommandContext<CommandSourceStack>)commandContext, EntityArgument.getPlayers((CommandContext<CommandSourceStack>)commandContext, "target"), GameModeArgument.getGameMode((CommandContext<CommandSourceStack>)commandContext, "gamemode"))))));
    }

    private static void logGamemodeChange(CommandSourceStack commandSourceStack, ServerPlayer serverPlayer, GameType gameType) {
        MutableComponent mutableComponent = Component.translatable("gameMode." + gameType.getName());
        if (commandSourceStack.getEntity() == serverPlayer) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.gamemode.success.self", mutableComponent), true);
        } else {
            if (commandSourceStack.getLevel().getGameRules().getBoolean(GameRules.RULE_SENDCOMMANDFEEDBACK)) {
                serverPlayer.sendSystemMessage(Component.translatable("gameMode.changed", mutableComponent));
            }
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.gamemode.success.other", serverPlayer.getDisplayName(), mutableComponent), true);
        }
    }

    private static int setMode(CommandContext<CommandSourceStack> commandContext, Collection<ServerPlayer> collection, GameType gameType) {
        int n = 0;
        for (ServerPlayer serverPlayer : collection) {
            if (!GameModeCommand.setGameMode((CommandSourceStack)commandContext.getSource(), serverPlayer, gameType)) continue;
            ++n;
        }
        return n;
    }

    public static void setGameMode(ServerPlayer serverPlayer, GameType gameType) {
        GameModeCommand.setGameMode(serverPlayer.createCommandSourceStack(), serverPlayer, gameType);
    }

    private static boolean setGameMode(CommandSourceStack commandSourceStack, ServerPlayer serverPlayer, GameType gameType) {
        if (serverPlayer.setGameMode(gameType)) {
            GameModeCommand.logGamemodeChange(commandSourceStack, serverPlayer, gameType);
            return true;
        }
        return false;
    }
}

