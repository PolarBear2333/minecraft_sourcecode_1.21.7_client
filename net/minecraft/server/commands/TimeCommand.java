/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public class TimeCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("time").requires(Commands.hasPermission(2))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("set").then(Commands.literal("day").executes(commandContext -> TimeCommand.setTime((CommandSourceStack)commandContext.getSource(), 1000)))).then(Commands.literal("noon").executes(commandContext -> TimeCommand.setTime((CommandSourceStack)commandContext.getSource(), 6000)))).then(Commands.literal("night").executes(commandContext -> TimeCommand.setTime((CommandSourceStack)commandContext.getSource(), 13000)))).then(Commands.literal("midnight").executes(commandContext -> TimeCommand.setTime((CommandSourceStack)commandContext.getSource(), 18000)))).then(Commands.argument("time", TimeArgument.time()).executes(commandContext -> TimeCommand.setTime((CommandSourceStack)commandContext.getSource(), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"time")))))).then(Commands.literal("add").then(Commands.argument("time", TimeArgument.time()).executes(commandContext -> TimeCommand.addTime((CommandSourceStack)commandContext.getSource(), IntegerArgumentType.getInteger((CommandContext)commandContext, (String)"time")))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("query").then(Commands.literal("daytime").executes(commandContext -> TimeCommand.queryTime((CommandSourceStack)commandContext.getSource(), TimeCommand.getDayTime(((CommandSourceStack)commandContext.getSource()).getLevel()))))).then(Commands.literal("gametime").executes(commandContext -> TimeCommand.queryTime((CommandSourceStack)commandContext.getSource(), (int)(((CommandSourceStack)commandContext.getSource()).getLevel().getGameTime() % Integer.MAX_VALUE))))).then(Commands.literal("day").executes(commandContext -> TimeCommand.queryTime((CommandSourceStack)commandContext.getSource(), (int)(((CommandSourceStack)commandContext.getSource()).getLevel().getDayTime() / 24000L % Integer.MAX_VALUE))))));
    }

    private static int getDayTime(ServerLevel serverLevel) {
        return (int)(serverLevel.getDayTime() % 24000L);
    }

    private static int queryTime(CommandSourceStack commandSourceStack, int n) {
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.time.query", n), false);
        return n;
    }

    public static int setTime(CommandSourceStack commandSourceStack, int n) {
        for (ServerLevel serverLevel : commandSourceStack.getServer().getAllLevels()) {
            serverLevel.setDayTime(n);
        }
        commandSourceStack.getServer().forceTimeSynchronization();
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.time.set", n), true);
        return TimeCommand.getDayTime(commandSourceStack.getLevel());
    }

    public static int addTime(CommandSourceStack commandSourceStack, int n) {
        for (ServerLevel serverLevel : commandSourceStack.getServer().getAllLevels()) {
            serverLevel.setDayTime(serverLevel.getDayTime() + (long)n);
        }
        commandSourceStack.getServer().forceTimeSynchronization();
        int n2 = TimeCommand.getDayTime(commandSourceStack.getLevel());
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.time.set", n2), true);
        return n2;
    }
}

