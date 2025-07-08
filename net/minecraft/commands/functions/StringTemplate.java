/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 */
package net.minecraft.commands.functions;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.commands.functions.CommandFunction;

public record StringTemplate(List<String> segments, List<String> variables) {
    public static StringTemplate fromString(String string) {
        ImmutableList.Builder builder = ImmutableList.builder();
        ImmutableList.Builder builder2 = ImmutableList.builder();
        int n = string.length();
        int n2 = 0;
        int n3 = string.indexOf(36);
        while (n3 != -1) {
            if (n3 == n - 1 || string.charAt(n3 + 1) != '(') {
                n3 = string.indexOf(36, n3 + 1);
                continue;
            }
            builder.add((Object)string.substring(n2, n3));
            int n4 = string.indexOf(41, n3 + 1);
            if (n4 == -1) {
                throw new IllegalArgumentException("Unterminated macro variable");
            }
            String string2 = string.substring(n3 + 2, n4);
            if (!StringTemplate.isValidVariableName(string2)) {
                throw new IllegalArgumentException("Invalid macro variable name '" + string2 + "'");
            }
            builder2.add((Object)string2);
            n2 = n4 + 1;
            n3 = string.indexOf(36, n2);
        }
        if (n2 == 0) {
            throw new IllegalArgumentException("No variables in macro");
        }
        if (n2 != n) {
            builder.add((Object)string.substring(n2));
        }
        return new StringTemplate((List<String>)builder.build(), (List<String>)builder2.build());
    }

    public static boolean isValidVariableName(String string) {
        for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '_') continue;
            return false;
        }
        return true;
    }

    public String substitute(List<String> list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < this.variables.size(); ++i) {
            stringBuilder.append(this.segments.get(i)).append(list.get(i));
            CommandFunction.checkCommandLineLength(stringBuilder);
        }
        if (this.segments.size() > this.variables.size()) {
            stringBuilder.append(this.segments.getLast());
        }
        CommandFunction.checkCommandLineLength(stringBuilder);
        return stringBuilder.toString();
    }
}

