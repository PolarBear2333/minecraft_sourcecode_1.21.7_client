/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  it.unimi.dsi.fastutil.chars.CharList
 */
package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.chars.CharList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.util.parsing.packrat.Control;
import net.minecraft.util.parsing.packrat.DelayedException;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Scope;
import net.minecraft.util.parsing.packrat.SuggestionSupplier;
import net.minecraft.util.parsing.packrat.Term;

public interface StringReaderTerms {
    public static Term<StringReader> word(String string) {
        return new TerminalWord(string);
    }

    public static Term<StringReader> character(final char c) {
        return new TerminalCharacters(CharList.of((char)c)){

            @Override
            protected boolean isAccepted(char c2) {
                return c == c2;
            }
        };
    }

    public static Term<StringReader> characters(final char c, final char c2) {
        return new TerminalCharacters(CharList.of((char)c, (char)c2)){

            @Override
            protected boolean isAccepted(char c3) {
                return c3 == c || c3 == c2;
            }
        };
    }

    public static StringReader createReader(String string, int n) {
        StringReader stringReader = new StringReader(string);
        stringReader.setCursor(n);
        return stringReader;
    }

    public static final class TerminalWord
    implements Term<StringReader> {
        private final String value;
        private final DelayedException<CommandSyntaxException> error;
        private final SuggestionSupplier<StringReader> suggestions;

        public TerminalWord(String string) {
            this.value = string;
            this.error = DelayedException.create(CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect(), string);
            this.suggestions = parseState -> Stream.of(string);
        }

        @Override
        public boolean parse(ParseState<StringReader> parseState, Scope scope, Control control) {
            parseState.input().skipWhitespace();
            int n = parseState.mark();
            String string = parseState.input().readUnquotedString();
            if (!string.equals(this.value)) {
                parseState.errorCollector().store(n, this.suggestions, this.error);
                return false;
            }
            return true;
        }

        public String toString() {
            return "terminal[" + this.value + "]";
        }
    }

    public static abstract class TerminalCharacters
    implements Term<StringReader> {
        private final DelayedException<CommandSyntaxException> error;
        private final SuggestionSupplier<StringReader> suggestions;

        public TerminalCharacters(CharList charList) {
            String string = charList.intStream().mapToObj(Character::toString).collect(Collectors.joining("|"));
            this.error = DelayedException.create(CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect(), String.valueOf(string));
            this.suggestions = parseState -> charList.intStream().mapToObj(Character::toString);
        }

        @Override
        public boolean parse(ParseState<StringReader> parseState, Scope scope, Control control) {
            parseState.input().skipWhitespace();
            int n = parseState.mark();
            if (!parseState.input().canRead() || !this.isAccepted(parseState.input().read())) {
                parseState.errorCollector().store(n, this.suggestions, this.error);
                return false;
            }
            return true;
        }

        protected abstract boolean isAccepted(char var1);
    }
}

