/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 */
package net.minecraft.commands;

import com.mojang.brigadier.StringReader;
import net.minecraft.CharPredicate;

public class ParserUtils {
    public static String readWhile(StringReader stringReader, CharPredicate charPredicate) {
        int n = stringReader.getCursor();
        while (stringReader.canRead() && charPredicate.test(stringReader.peek())) {
            stringReader.skip();
        }
        return stringReader.getString().substring(n, stringReader.getCursor());
    }
}

