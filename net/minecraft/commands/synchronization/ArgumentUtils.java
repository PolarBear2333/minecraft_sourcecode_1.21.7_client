/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.tree.ArgumentCommandNode
 *  com.mojang.brigadier.tree.CommandNode
 *  com.mojang.brigadier.tree.LiteralCommandNode
 *  com.mojang.brigadier.tree.RootCommandNode
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
 *  org.slf4j.Logger
 */
package net.minecraft.commands.synchronization;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.lang.runtime.SwitchBootstraps;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.commands.PermissionCheck;
import org.slf4j.Logger;

public class ArgumentUtils {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final byte NUMBER_FLAG_MIN = 1;
    private static final byte NUMBER_FLAG_MAX = 2;

    public static int createNumberFlags(boolean bl, boolean bl2) {
        int n = 0;
        if (bl) {
            n |= 1;
        }
        if (bl2) {
            n |= 2;
        }
        return n;
    }

    public static boolean numberHasMin(byte by) {
        return (by & 1) != 0;
    }

    public static boolean numberHasMax(byte by) {
        return (by & 2) != 0;
    }

    private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> void serializeArgumentCap(JsonObject jsonObject, ArgumentTypeInfo<A, T> argumentTypeInfo, ArgumentTypeInfo.Template<A> template) {
        argumentTypeInfo.serializeToJson(template, jsonObject);
    }

    private static <T extends ArgumentType<?>> void serializeArgumentToJson(JsonObject jsonObject, T t) {
        ArgumentTypeInfo.Template<T> template = ArgumentTypeInfos.unpack(t);
        jsonObject.addProperty("type", "argument");
        jsonObject.addProperty("parser", String.valueOf(BuiltInRegistries.COMMAND_ARGUMENT_TYPE.getKey(template.type())));
        JsonObject jsonObject2 = new JsonObject();
        ArgumentUtils.serializeArgumentCap(jsonObject2, template.type(), template);
        if (!jsonObject2.isEmpty()) {
            jsonObject.add("properties", (JsonElement)jsonObject2);
        }
    }

    public static <S> JsonObject serializeNodeToJson(CommandDispatcher<S> commandDispatcher, CommandNode<S> commandNode) {
        Collection collection;
        Object object;
        Object object2;
        JsonObject jsonObject = new JsonObject();
        CommandNode<S> commandNode2 = commandNode;
        Objects.requireNonNull(commandNode2);
        Object object3 = commandNode2;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{RootCommandNode.class, LiteralCommandNode.class, ArgumentCommandNode.class}, object3, n)) {
            case 0: {
                object2 = (RootCommandNode)object3;
                jsonObject.addProperty("type", "root");
                break;
            }
            case 1: {
                object = (LiteralCommandNode)object3;
                jsonObject.addProperty("type", "literal");
                break;
            }
            case 2: {
                Object object4 = (ArgumentCommandNode)object3;
                ArgumentUtils.serializeArgumentToJson(jsonObject, object4.getType());
                break;
            }
            default: {
                LOGGER.error("Could not serialize node {} ({})!", commandNode, commandNode.getClass());
                jsonObject.addProperty("type", "unknown");
            }
        }
        object3 = commandNode.getChildren();
        if (!object3.isEmpty()) {
            JsonObject jsonObject2 = new JsonObject();
            object2 = object3.iterator();
            while (object2.hasNext()) {
                object = (CommandNode)object2.next();
                jsonObject2.add(object.getName(), (JsonElement)ArgumentUtils.serializeNodeToJson(commandDispatcher, object));
            }
            jsonObject.add("children", (JsonElement)jsonObject2);
        }
        if (commandNode.getCommand() != null) {
            jsonObject.addProperty("executable", Boolean.valueOf(true));
        }
        if ((object2 = commandNode.getRequirement()) instanceof PermissionCheck) {
            PermissionCheck permissionCheck = (PermissionCheck)object2;
            jsonObject.addProperty("required_level", (Number)permissionCheck.requiredLevel());
        }
        if (commandNode.getRedirect() != null && !(collection = commandDispatcher.getPath(commandNode.getRedirect())).isEmpty()) {
            object2 = new JsonArray();
            for (Object object4 : collection) {
                object2.add((String)object4);
            }
            jsonObject.add("redirect", (JsonElement)object2);
        }
        return jsonObject;
    }

    public static <T> Set<ArgumentType<?>> findUsedArgumentTypes(CommandNode<T> commandNode) {
        ReferenceOpenHashSet referenceOpenHashSet = new ReferenceOpenHashSet();
        HashSet hashSet = new HashSet();
        ArgumentUtils.findUsedArgumentTypes(commandNode, hashSet, referenceOpenHashSet);
        return hashSet;
    }

    private static <T> void findUsedArgumentTypes(CommandNode<T> commandNode2, Set<ArgumentType<?>> set, Set<CommandNode<T>> set2) {
        ArgumentCommandNode argumentCommandNode;
        if (!set2.add(commandNode2)) {
            return;
        }
        if (commandNode2 instanceof ArgumentCommandNode) {
            argumentCommandNode = (ArgumentCommandNode)commandNode2;
            set.add(argumentCommandNode.getType());
        }
        commandNode2.getChildren().forEach(commandNode -> ArgumentUtils.findUsedArgumentTypes(commandNode, set, set2));
        argumentCommandNode = commandNode2.getRedirect();
        if (argumentCommandNode != null) {
            ArgumentUtils.findUsedArgumentTypes(argumentCommandNode, set, set2);
        }
    }
}

