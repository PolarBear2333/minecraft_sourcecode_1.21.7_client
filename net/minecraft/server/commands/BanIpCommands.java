/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.net.InetAddresses
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  javax.annotation.Nullable
 */
package net.minecraft.server.commands;

import com.google.common.net.InetAddresses;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.IpBanList;
import net.minecraft.server.players.IpBanListEntry;

public class BanIpCommands {
    private static final SimpleCommandExceptionType ERROR_INVALID_IP = new SimpleCommandExceptionType((Message)Component.translatable("commands.banip.invalid"));
    private static final SimpleCommandExceptionType ERROR_ALREADY_BANNED = new SimpleCommandExceptionType((Message)Component.translatable("commands.banip.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("ban-ip").requires(Commands.hasPermission(3))).then(((RequiredArgumentBuilder)Commands.argument("target", StringArgumentType.word()).executes(commandContext -> BanIpCommands.banIpOrName((CommandSourceStack)commandContext.getSource(), StringArgumentType.getString((CommandContext)commandContext, (String)"target"), null))).then(Commands.argument("reason", MessageArgument.message()).executes(commandContext -> BanIpCommands.banIpOrName((CommandSourceStack)commandContext.getSource(), StringArgumentType.getString((CommandContext)commandContext, (String)"target"), MessageArgument.getMessage((CommandContext<CommandSourceStack>)commandContext, "reason"))))));
    }

    private static int banIpOrName(CommandSourceStack commandSourceStack, String string, @Nullable Component component) throws CommandSyntaxException {
        if (InetAddresses.isInetAddress((String)string)) {
            return BanIpCommands.banIp(commandSourceStack, string, component);
        }
        ServerPlayer serverPlayer = commandSourceStack.getServer().getPlayerList().getPlayerByName(string);
        if (serverPlayer != null) {
            return BanIpCommands.banIp(commandSourceStack, serverPlayer.getIpAddress(), component);
        }
        throw ERROR_INVALID_IP.create();
    }

    private static int banIp(CommandSourceStack commandSourceStack, String string, @Nullable Component component) throws CommandSyntaxException {
        IpBanList ipBanList = commandSourceStack.getServer().getPlayerList().getIpBans();
        if (ipBanList.isBanned(string)) {
            throw ERROR_ALREADY_BANNED.create();
        }
        List<ServerPlayer> list = commandSourceStack.getServer().getPlayerList().getPlayersWithAddress(string);
        IpBanListEntry ipBanListEntry = new IpBanListEntry(string, null, commandSourceStack.getTextName(), null, component == null ? null : component.getString());
        ipBanList.add(ipBanListEntry);
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.banip.success", string, ipBanListEntry.getReason()), true);
        if (!list.isEmpty()) {
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.banip.info", list.size(), EntitySelector.joinNames(list)), true);
        }
        for (ServerPlayer serverPlayer : list) {
            serverPlayer.connection.disconnect(Component.translatable("multiplayer.disconnect.ip_banned"));
        }
        return list.size();
    }
}

