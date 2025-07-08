/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.commands.arguments;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.waypoints.WaypointTransmitter;

public class WaypointArgument {
    public static final SimpleCommandExceptionType ERROR_NOT_A_WAYPOINT = new SimpleCommandExceptionType((Message)Component.translatable("argument.waypoint.invalid"));

    public static WaypointTransmitter getWaypoint(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        Entity entity = ((EntitySelector)commandContext.getArgument(string, EntitySelector.class)).findSingleEntity((CommandSourceStack)commandContext.getSource());
        if (entity instanceof WaypointTransmitter) {
            WaypointTransmitter waypointTransmitter = (WaypointTransmitter)((Object)entity);
            return waypointTransmitter;
        }
        throw ERROR_NOT_A_WAYPOINT.create();
    }
}

