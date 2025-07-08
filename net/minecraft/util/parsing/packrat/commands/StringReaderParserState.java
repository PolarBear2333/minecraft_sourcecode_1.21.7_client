/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 */
package net.minecraft.util.parsing.packrat.commands;

import com.mojang.brigadier.StringReader;
import net.minecraft.util.parsing.packrat.CachedParseState;
import net.minecraft.util.parsing.packrat.ErrorCollector;

public class StringReaderParserState
extends CachedParseState<StringReader> {
    private final StringReader input;

    public StringReaderParserState(ErrorCollector<StringReader> errorCollector, StringReader stringReader) {
        super(errorCollector);
        this.input = stringReader;
    }

    @Override
    public StringReader input() {
        return this.input;
    }

    @Override
    public int mark() {
        return this.input.getCursor();
    }

    @Override
    public void restore(int n) {
        this.input.setCursor(n);
    }

    @Override
    public /* synthetic */ Object input() {
        return this.input();
    }
}

