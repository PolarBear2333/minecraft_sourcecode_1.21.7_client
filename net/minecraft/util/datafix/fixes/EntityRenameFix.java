/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.types.templates.TaggedChoice$TaggedChoiceType
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DynamicOps;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.fixes.References;

public abstract class EntityRenameFix
extends DataFix {
    protected final String name;

    public EntityRenameFix(String string, Schema schema, boolean bl) {
        super(schema, bl);
        this.name = string;
    }

    public TypeRewriteRule makeRule() {
        TaggedChoice.TaggedChoiceType taggedChoiceType = this.getInputSchema().findChoiceType(References.ENTITY);
        TaggedChoice.TaggedChoiceType taggedChoiceType2 = this.getOutputSchema().findChoiceType(References.ENTITY);
        Function<String, Type> function = Util.memoize(string -> {
            Type type = (Type)taggedChoiceType.types().get(string);
            return ExtraDataFixUtils.patchSubType(type, taggedChoiceType, taggedChoiceType2);
        });
        return this.fixTypeEverywhere(this.name, (Type)taggedChoiceType, (Type)taggedChoiceType2, dynamicOps -> pair -> {
            String string = (String)pair.getFirst();
            Type type = (Type)function.apply(string);
            Pair<String, Typed<?>> pair2 = this.fix(string, this.getEntity(pair.getSecond(), (DynamicOps<?>)dynamicOps, (Type)type));
            Type type2 = (Type)taggedChoiceType2.types().get(pair2.getFirst());
            if (!type2.equals((Object)((Typed)pair2.getSecond()).getType(), true, true)) {
                throw new IllegalStateException(String.format(Locale.ROOT, "Dynamic type check failed: %s not equal to %s", type2, ((Typed)pair2.getSecond()).getType()));
            }
            return Pair.of((Object)((String)pair2.getFirst()), (Object)((Typed)pair2.getSecond()).getValue());
        });
    }

    private <A> Typed<A> getEntity(Object object, DynamicOps<?> dynamicOps, Type<A> type) {
        return new Typed(type, dynamicOps, object);
    }

    protected abstract Pair<String, Typed<?>> fix(String var1, Typed<?> var2);
}

