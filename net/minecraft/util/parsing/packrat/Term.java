/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.parsing.packrat;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.parsing.packrat.Atom;
import net.minecraft.util.parsing.packrat.Control;
import net.minecraft.util.parsing.packrat.NamedRule;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Scope;

public interface Term<S> {
    public boolean parse(ParseState<S> var1, Scope var2, Control var3);

    public static <S, T> Term<S> marker(Atom<T> atom, T t) {
        return new Marker(atom, t);
    }

    @SafeVarargs
    public static <S> Term<S> sequence(Term<S> ... termArray) {
        return new Sequence<S>(termArray);
    }

    @SafeVarargs
    public static <S> Term<S> alternative(Term<S> ... termArray) {
        return new Alternative<S>(termArray);
    }

    public static <S> Term<S> optional(Term<S> term) {
        return new Maybe<S>(term);
    }

    public static <S, T> Term<S> repeated(NamedRule<S, T> namedRule, Atom<List<T>> atom) {
        return Term.repeated(namedRule, atom, 0);
    }

    public static <S, T> Term<S> repeated(NamedRule<S, T> namedRule, Atom<List<T>> atom, int n) {
        return new Repeated<S, T>(namedRule, atom, n);
    }

    public static <S, T> Term<S> repeatedWithTrailingSeparator(NamedRule<S, T> namedRule, Atom<List<T>> atom, Term<S> term) {
        return Term.repeatedWithTrailingSeparator(namedRule, atom, term, 0);
    }

    public static <S, T> Term<S> repeatedWithTrailingSeparator(NamedRule<S, T> namedRule, Atom<List<T>> atom, Term<S> term, int n) {
        return new RepeatedWithSeparator<S, T>(namedRule, atom, term, n, true);
    }

    public static <S, T> Term<S> repeatedWithoutTrailingSeparator(NamedRule<S, T> namedRule, Atom<List<T>> atom, Term<S> term) {
        return Term.repeatedWithoutTrailingSeparator(namedRule, atom, term, 0);
    }

    public static <S, T> Term<S> repeatedWithoutTrailingSeparator(NamedRule<S, T> namedRule, Atom<List<T>> atom, Term<S> term, int n) {
        return new RepeatedWithSeparator<S, T>(namedRule, atom, term, n, false);
    }

    public static <S> Term<S> positiveLookahead(Term<S> term) {
        return new LookAhead<S>(term, true);
    }

    public static <S> Term<S> negativeLookahead(Term<S> term) {
        return new LookAhead<S>(term, false);
    }

    public static <S> Term<S> cut() {
        return new Term<S>(){

            @Override
            public boolean parse(ParseState<S> parseState, Scope scope, Control control) {
                control.cut();
                return true;
            }

            public String toString() {
                return "\u2191";
            }
        };
    }

    public static <S> Term<S> empty() {
        return new Term<S>(){

            @Override
            public boolean parse(ParseState<S> parseState, Scope scope, Control control) {
                return true;
            }

            public String toString() {
                return "\u03b5";
            }
        };
    }

    public static <S> Term<S> fail(final Object object) {
        return new Term<S>(){

            @Override
            public boolean parse(ParseState<S> parseState, Scope scope, Control control) {
                parseState.errorCollector().store(parseState.mark(), object);
                return false;
            }

            public String toString() {
                return "fail";
            }
        };
    }

    public record Marker<S, T>(Atom<T> name, T value) implements Term<S>
    {
        @Override
        public boolean parse(ParseState<S> parseState, Scope scope, Control control) {
            scope.put(this.name, this.value);
            return true;
        }
    }

    public record Sequence<S>(Term<S>[] elements) implements Term<S>
    {
        @Override
        public boolean parse(ParseState<S> parseState, Scope scope, Control control) {
            int n = parseState.mark();
            for (Term<S> term : this.elements) {
                if (term.parse(parseState, scope, control)) continue;
                parseState.restore(n);
                return false;
            }
            return true;
        }
    }

    public record Alternative<S>(Term<S>[] elements) implements Term<S>
    {
        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public boolean parse(ParseState<S> parseState, Scope scope, Control control) {
            Control control2 = parseState.acquireControl();
            try {
                int n = parseState.mark();
                scope.splitFrame();
                for (Term<S> term : this.elements) {
                    if (term.parse(parseState, scope, control2)) {
                        scope.mergeFrame();
                        boolean bl = true;
                        return bl;
                    }
                    scope.clearFrameValues();
                    parseState.restore(n);
                    if (control2.hasCut()) break;
                }
                scope.popFrame();
                boolean bl = false;
                return bl;
            }
            finally {
                parseState.releaseControl();
            }
        }
    }

    public record Maybe<S>(Term<S> term) implements Term<S>
    {
        @Override
        public boolean parse(ParseState<S> parseState, Scope scope, Control control) {
            int n = parseState.mark();
            if (!this.term.parse(parseState, scope, control)) {
                parseState.restore(n);
            }
            return true;
        }
    }

    public record Repeated<S, T>(NamedRule<S, T> element, Atom<List<T>> listName, int minRepetitions) implements Term<S>
    {
        @Override
        public boolean parse(ParseState<S> parseState, Scope scope, Control control) {
            int n;
            int n2 = parseState.mark();
            ArrayList<T> arrayList = new ArrayList<T>(this.minRepetitions);
            while (true) {
                n = parseState.mark();
                T t = parseState.parse(this.element);
                if (t == null) break;
                arrayList.add(t);
            }
            parseState.restore(n);
            if (arrayList.size() < this.minRepetitions) {
                parseState.restore(n2);
                return false;
            }
            scope.put(this.listName, arrayList);
            return true;
        }
    }

    public record RepeatedWithSeparator<S, T>(NamedRule<S, T> element, Atom<List<T>> listName, Term<S> separator, int minRepetitions, boolean allowTrailingSeparator) implements Term<S>
    {
        @Override
        public boolean parse(ParseState<S> parseState, Scope scope, Control control) {
            int n = parseState.mark();
            ArrayList<T> arrayList = new ArrayList<T>(this.minRepetitions);
            boolean bl = true;
            while (true) {
                int n2 = parseState.mark();
                if (!bl && !this.separator.parse(parseState, scope, control)) {
                    parseState.restore(n2);
                    break;
                }
                int n3 = parseState.mark();
                T t = parseState.parse(this.element);
                if (t == null) {
                    if (bl) {
                        parseState.restore(n3);
                        break;
                    }
                    if (this.allowTrailingSeparator) {
                        parseState.restore(n3);
                        break;
                    }
                    parseState.restore(n);
                    return false;
                }
                arrayList.add(t);
                bl = false;
            }
            if (arrayList.size() < this.minRepetitions) {
                parseState.restore(n);
                return false;
            }
            scope.put(this.listName, arrayList);
            return true;
        }
    }

    public record LookAhead<S>(Term<S> term, boolean positive) implements Term<S>
    {
        @Override
        public boolean parse(ParseState<S> parseState, Scope scope, Control control) {
            int n = parseState.mark();
            boolean bl = this.term.parse(parseState.silent(), scope, control);
            parseState.restore(n);
            return this.positive == bl;
        }
    }
}

