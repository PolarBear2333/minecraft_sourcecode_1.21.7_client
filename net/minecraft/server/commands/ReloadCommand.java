/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.server.commands;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.WorldData;
import org.slf4j.Logger;

public class ReloadCommand {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void reloadPacks(Collection<String> collection, CommandSourceStack commandSourceStack) {
        commandSourceStack.getServer().reloadResources(collection).exceptionally(throwable -> {
            LOGGER.warn("Failed to execute reload", throwable);
            commandSourceStack.sendFailure(Component.translatable("commands.reload.failure"));
            return null;
        });
    }

    private static Collection<String> discoverNewPacks(PackRepository packRepository, WorldData worldData, Collection<String> collection) {
        packRepository.reload();
        ArrayList arrayList = Lists.newArrayList(collection);
        List<String> list = worldData.getDataConfiguration().dataPacks().getDisabled();
        for (String string : packRepository.getAvailableIds()) {
            if (list.contains(string) || arrayList.contains(string)) continue;
            arrayList.add(string);
        }
        return arrayList;
    }

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("reload").requires(Commands.hasPermission(2))).executes(commandContext -> {
            CommandSourceStack commandSourceStack = (CommandSourceStack)commandContext.getSource();
            MinecraftServer minecraftServer = commandSourceStack.getServer();
            PackRepository packRepository = minecraftServer.getPackRepository();
            WorldData worldData = minecraftServer.getWorldData();
            Collection<String> collection = packRepository.getSelectedIds();
            Collection<String> collection2 = ReloadCommand.discoverNewPacks(packRepository, worldData, collection);
            commandSourceStack.sendSuccess(() -> Component.translatable("commands.reload.success"), true);
            ReloadCommand.reloadPacks(collection2, commandSourceStack);
            return 0;
        }));
    }
}

