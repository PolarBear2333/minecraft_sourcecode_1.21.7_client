/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.commands.arguments;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class UuidArgument
implements ArgumentType<UUID> {
    public static final SimpleCommandExceptionType ERROR_INVALID_UUID = new SimpleCommandExceptionType((Message)Component.translatable("argument.uuid.invalid"));
    private static final Collection<String> EXAMPLES = Arrays.asList("dd12be42-52a9-4a91-a8a1-11c01849e498");
    private static final Pattern ALLOWED_CHARACTERS = Pattern.compile("^([-A-Fa-f0-9]+)");

    public static UUID getUuid(CommandContext<CommandSourceStack> commandContext, String string) {
        return (UUID)commandContext.getArgument(string, UUID.class);
    }

    public static UuidArgument uuid() {
        return new UuidArgument();
    }

    public UUID parse(StringReader stringReader) throws CommandSyntaxException {
        String string = stringReader.getRemaining();
        Matcher matcher = ALLOWED_CHARACTERS.matcher(string);
        if (matcher.find()) {
            String string2 = matcher.group(1);
            try {
                UUID uUID = UUID.fromString(string2);
                stringReader.setCursor(stringReader.getCursor() + string2.length());
                return uUID;
            }
            catch (IllegalArgumentException illegalArgumentException) {
                // empty catch block
            }
        }
        throw ERROR_INVALID_UUID.createWithContext((ImmutableStringReader)stringReader);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public /* synthetic */ Object parse(StringReader stringReader) throws CommandSyntaxException {
        return this.parse(stringReader);
    }
}

