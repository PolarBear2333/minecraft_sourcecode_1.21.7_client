/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SummonCommand {
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType((Message)Component.translatable("commands.summon.failed"));
    private static final SimpleCommandExceptionType ERROR_DUPLICATE_UUID = new SimpleCommandExceptionType((Message)Component.translatable("commands.summon.failed.uuid"));
    private static final SimpleCommandExceptionType INVALID_POSITION = new SimpleCommandExceptionType((Message)Component.translatable("commands.summon.invalidPosition"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("summon").requires(Commands.hasPermission(2))).then(((RequiredArgumentBuilder)Commands.argument("entity", ResourceArgument.resource(commandBuildContext, Registries.ENTITY_TYPE)).suggests(SuggestionProviders.cast(SuggestionProviders.SUMMONABLE_ENTITIES)).executes(commandContext -> SummonCommand.spawnEntity((CommandSourceStack)commandContext.getSource(), ResourceArgument.getSummonableEntityType((CommandContext<CommandSourceStack>)commandContext, "entity"), ((CommandSourceStack)commandContext.getSource()).getPosition(), new CompoundTag(), true))).then(((RequiredArgumentBuilder)Commands.argument("pos", Vec3Argument.vec3()).executes(commandContext -> SummonCommand.spawnEntity((CommandSourceStack)commandContext.getSource(), ResourceArgument.getSummonableEntityType((CommandContext<CommandSourceStack>)commandContext, "entity"), Vec3Argument.getVec3((CommandContext<CommandSourceStack>)commandContext, "pos"), new CompoundTag(), true))).then(Commands.argument("nbt", CompoundTagArgument.compoundTag()).executes(commandContext -> SummonCommand.spawnEntity((CommandSourceStack)commandContext.getSource(), ResourceArgument.getSummonableEntityType((CommandContext<CommandSourceStack>)commandContext, "entity"), Vec3Argument.getVec3((CommandContext<CommandSourceStack>)commandContext, "pos"), CompoundTagArgument.getCompoundTag(commandContext, "nbt"), false))))));
    }

    public static Entity createEntity(CommandSourceStack commandSourceStack, Holder.Reference<EntityType<?>> reference, Vec3 vec3, CompoundTag compoundTag, boolean bl) throws CommandSyntaxException {
        BlockPos blockPos = BlockPos.containing(vec3);
        if (!Level.isInSpawnableBounds(blockPos)) {
            throw INVALID_POSITION.create();
        }
        CompoundTag compoundTag2 = compoundTag.copy();
        compoundTag2.putString("id", reference.key().location().toString());
        ServerLevel serverLevel = commandSourceStack.getLevel();
        Entity entity2 = EntityType.loadEntityRecursive(compoundTag2, (Level)serverLevel, EntitySpawnReason.COMMAND, entity -> {
            entity.snapTo(vec3.x, vec3.y, vec3.z, entity.getYRot(), entity.getXRot());
            return entity;
        });
        if (entity2 == null) {
            throw ERROR_FAILED.create();
        }
        if (bl && entity2 instanceof Mob) {
            ((Mob)entity2).finalizeSpawn(commandSourceStack.getLevel(), commandSourceStack.getLevel().getCurrentDifficultyAt(entity2.blockPosition()), EntitySpawnReason.COMMAND, null);
        }
        if (!serverLevel.tryAddFreshEntityWithPassengers(entity2)) {
            throw ERROR_DUPLICATE_UUID.create();
        }
        return entity2;
    }

    private static int spawnEntity(CommandSourceStack commandSourceStack, Holder.Reference<EntityType<?>> reference, Vec3 vec3, CompoundTag compoundTag, boolean bl) throws CommandSyntaxException {
        Entity entity = SummonCommand.createEntity(commandSourceStack, reference, vec3, compoundTag, bl);
        commandSourceStack.sendSuccess(() -> Component.translatable("commands.summon.success", entity.getDisplayName()), true);
        return 1;
    }
}

