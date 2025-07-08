/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;

public class DebugMobSpawningCommand {
    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        LiteralArgumentBuilder literalArgumentBuilder = (LiteralArgumentBuilder)Commands.literal("debugmobspawning").requires(Commands.hasPermission(2));
        for (MobCategory mobCategory : MobCategory.values()) {
            literalArgumentBuilder.then(Commands.literal(mobCategory.getName()).then(Commands.argument("at", BlockPosArgument.blockPos()).executes(commandContext -> DebugMobSpawningCommand.spawnMobs((CommandSourceStack)commandContext.getSource(), mobCategory, BlockPosArgument.getLoadedBlockPos((CommandContext<CommandSourceStack>)commandContext, "at")))));
        }
        commandDispatcher.register(literalArgumentBuilder);
    }

    private static int spawnMobs(CommandSourceStack commandSourceStack, MobCategory mobCategory, BlockPos blockPos) {
        NaturalSpawner.spawnCategoryForPosition(mobCategory, commandSourceStack.getLevel(), blockPos);
        return 1;
    }
}

