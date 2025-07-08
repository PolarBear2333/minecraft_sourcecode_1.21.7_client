/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 */
package net.minecraft.commands.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Arrays;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.ResourceLocation;

public class ResourceLocationArgument
implements ArgumentType<ResourceLocation> {
    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar", "012");

    public static ResourceLocationArgument id() {
        return new ResourceLocationArgument();
    }

    public static ResourceLocation getId(CommandContext<CommandSourceStack> commandContext, String string) {
        return (ResourceLocation)commandContext.getArgument(string, ResourceLocation.class);
    }

    public ResourceLocation parse(StringReader stringReader) throws CommandSyntaxException {
        return ResourceLocation.read(stringReader);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }
}

