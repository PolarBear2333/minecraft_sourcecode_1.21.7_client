/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.util.parsing.packrat;

import javax.annotation.Nullable;
import net.minecraft.util.parsing.packrat.Control;
import net.minecraft.util.parsing.packrat.ParseState;
import net.minecraft.util.parsing.packrat.Scope;
import net.minecraft.util.parsing.packrat.Term;

public interface Rule<S, T> {
    @Nullable
    public T parse(ParseState<S> var1);

    public static <S, T> Rule<S, T> fromTerm(Term<S> term, RuleAction<S, T> ruleAction) {
        return new WrappedTerm<S, T>(ruleAction, term);
    }

    public static <S, T> Rule<S, T> fromTerm(Term<S> term, SimpleRuleAction<S, T> simpleRuleAction) {
        return new WrappedTerm<S, T>(simpleRuleAction, term);
    }

    public record WrappedTerm<S, T>(RuleAction<S, T> action, Term<S> child) implements Rule<S, T>
    {
        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        @Nullable
        public T parse(ParseState<S> parseState) {
            Scope scope = parseState.scope();
            scope.pushFrame();
            try {
                if (this.child.parse(parseState, scope, Control.UNBOUND)) {
                    T t = this.action.run(parseState);
                    return t;
                }
                T t = null;
                return t;
            }
            finally {
                scope.popFrame();
            }
        }
    }

    @FunctionalInterface
    public static interface RuleAction<S, T> {
        @Nullable
        public T run(ParseState<S> var1);
    }

    @FunctionalInterface
    public static interface SimpleRuleAction<S, T>
    extends RuleAction<S, T> {
        public T run(Scope var1);

        @Override
        default public T run(ParseState<S> parseState) {
            return this.run(parseState.scope());
        }
    }
}

