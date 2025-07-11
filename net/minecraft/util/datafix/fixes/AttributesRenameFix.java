/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.function.UnaryOperator;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.fixes.References;

public class AttributesRenameFix
extends DataFix {
    private final String name;
    private final UnaryOperator<String> renames;

    public AttributesRenameFix(Schema schema, String string, UnaryOperator<String> unaryOperator) {
        super(schema, false);
        this.name = string;
        this.renames = unaryOperator;
    }

    protected TypeRewriteRule makeRule() {
        return TypeRewriteRule.seq((TypeRewriteRule)this.fixTypeEverywhereTyped(this.name + " (Components)", this.getInputSchema().getType(References.DATA_COMPONENTS), this::fixDataComponents), (TypeRewriteRule[])new TypeRewriteRule[]{this.fixTypeEverywhereTyped(this.name + " (Entity)", this.getInputSchema().getType(References.ENTITY), this::fixEntity), this.fixTypeEverywhereTyped(this.name + " (Player)", this.getInputSchema().getType(References.PLAYER), this::fixEntity)});
    }

    private Typed<?> fixDataComponents(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), dynamic -> dynamic.update("minecraft:attribute_modifiers", dynamic2 -> dynamic2.update("modifiers", dynamic -> (Dynamic)DataFixUtils.orElse(dynamic.asStreamOpt().result().map(stream -> stream.map(this::fixTypeField)).map(arg_0 -> ((Dynamic)dynamic).createList(arg_0)), (Object)dynamic))));
    }

    private Typed<?> fixEntity(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), dynamic2 -> dynamic2.update("attributes", dynamic -> (Dynamic)DataFixUtils.orElse(dynamic.asStreamOpt().result().map(stream -> stream.map(this::fixIdField)).map(arg_0 -> ((Dynamic)dynamic).createList(arg_0)), (Object)dynamic)));
    }

    private Dynamic<?> fixIdField(Dynamic<?> dynamic) {
        return ExtraDataFixUtils.fixStringField(dynamic, "id", this.renames);
    }

    private Dynamic<?> fixTypeField(Dynamic<?> dynamic) {
        return ExtraDataFixUtils.fixStringField(dynamic, "type", this.renames);
    }
}

