/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  javax.annotation.Nullable
 */
package net.minecraft.server.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.HashSet;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceOrIdArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.ClientboundShowDialogPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;

public class DebugConfigCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("debugconfig").requires(Commands.hasPermission(3))).then(Commands.literal("config").then(Commands.argument("target", EntityArgument.player()).executes(commandContext -> DebugConfigCommand.config((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayer((CommandContext<CommandSourceStack>)commandContext, "target")))))).then(Commands.literal("unconfig").then(Commands.argument("target", UuidArgument.uuid()).suggests((commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(DebugConfigCommand.getUuidsInConfig(((CommandSourceStack)commandContext.getSource()).getServer()), suggestionsBuilder)).executes(commandContext -> DebugConfigCommand.unconfig((CommandSourceStack)commandContext.getSource(), UuidArgument.getUuid((CommandContext<CommandSourceStack>)commandContext, "target")))))).then(Commands.literal("dialog").then(Commands.argument("target", UuidArgument.uuid()).suggests((commandContext, suggestionsBuilder) -> SharedSuggestionProvider.suggest(DebugConfigCommand.getUuidsInConfig(((CommandSourceStack)commandContext.getSource()).getServer()), suggestionsBuilder)).then(Commands.argument("dialog", ResourceOrIdArgument.dialog(commandBuildContext)).executes(commandContext -> DebugConfigCommand.showDialog((CommandSourceStack)commandContext.getSource(), UuidArgument.getUuid((CommandContext<CommandSourceStack>)commandContext, "target"), ResourceOrIdArgument.getDialog((CommandContext<CommandSourceStack>)commandContext, "dialog")))))));
    }

    private static Iterable<String> getUuidsInConfig(MinecraftServer minecraftServer) {
        HashSet<String> hashSet = new HashSet<String>();
        for (Connection connection : minecraftServer.getConnection().getConnections()) {
            PacketListener packetListener = connection.getPacketListener();
            if (!(packetListener instanceof ServerConfigurationPacketListenerImpl)) continue;
            ServerConfigurationPacketListenerImpl serverConfigurationPacketListenerImpl = (ServerConfigurationPacketListenerImpl)packetListener;
            hashSet.add(serverConfigurationPacketListenerImpl.getOwner().getId().toString());
        }
        return hashSet;
    }

    private static int config(CommandSourceStack commandSourceStack, ServerPlayer serverPlayer) {
        GameProfile gameProfile = serverPlayer.getGameProfile();
        serverPlayer.connection.switchToConfig();
        commandSourceStack.sendSuccess(() -> Component.literal("Switched player " + gameProfile.getName() + "(" + String.valueOf(gameProfile.getId()) + ") to config mode"), false);
        return 1;
    }

    @Nullable
    private static ServerConfigurationPacketListenerImpl findConfigPlayer(MinecraftServer minecraftServer, UUID uUID) {
        for (Connection connection : minecraftServer.getConnection().getConnections()) {
            ServerConfigurationPacketListenerImpl serverConfigurationPacketListenerImpl;
            PacketListener packetListener = connection.getPacketListener();
            if (!(packetListener instanceof ServerConfigurationPacketListenerImpl) || !(serverConfigurationPacketListenerImpl = (ServerConfigurationPacketListenerImpl)packetListener).getOwner().getId().equals(uUID)) continue;
            return serverConfigurationPacketListenerImpl;
        }
        return null;
    }

    private static int unconfig(CommandSourceStack commandSourceStack, UUID uUID) {
        ServerConfigurationPacketListenerImpl serverConfigurationPacketListenerImpl = DebugConfigCommand.findConfigPlayer(commandSourceStack.getServer(), uUID);
        if (serverConfigurationPacketListenerImpl != null) {
            serverConfigurationPacketListenerImpl.returnToWorld();
            return 1;
        }
        commandSourceStack.sendFailure(Component.literal("Can't find player to unconfig"));
        return 0;
    }

    private static int showDialog(CommandSourceStack commandSourceStack, UUID uUID, Holder<Dialog> holder) {
        ServerConfigurationPacketListenerImpl serverConfigurationPacketListenerImpl = DebugConfigCommand.findConfigPlayer(commandSourceStack.getServer(), uUID);
        if (serverConfigurationPacketListenerImpl != null) {
            serverConfigurationPacketListenerImpl.send(new ClientboundShowDialogPacket(holder));
            return 1;
        }
        commandSourceStack.sendFailure(Component.literal("Can't find player to talk to"));
        return 0;
    }
}

