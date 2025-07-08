/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.types.templates.TaggedChoice$TaggedChoiceType
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice;
import com.mojang.datafixers.util.Pair;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class OminousBannerRarityFix
extends DataFix {
    public OminousBannerRarityFix(Schema schema) {
        super(schema, false);
    }

    public TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(References.BLOCK_ENTITY);
        Type type2 = this.getInputSchema().getType(References.ITEM_STACK);
        TaggedChoice.TaggedChoiceType taggedChoiceType = this.getInputSchema().findChoiceType(References.BLOCK_ENTITY);
        OpticFinder opticFinder = DSL.fieldFinder((String)"id", (Type)DSL.named((String)References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        OpticFinder opticFinder2 = type.findField("components");
        OpticFinder opticFinder3 = type2.findField("components");
        OpticFinder opticFinder4 = opticFinder2.type().findField("minecraft:item_name");
        OpticFinder opticFinder5 = DSL.typeFinder((Type)this.getInputSchema().getType(References.TEXT_COMPONENT));
        return TypeRewriteRule.seq((TypeRewriteRule)this.fixTypeEverywhereTyped("Ominous Banner block entity common rarity to uncommon rarity fix", type, typed -> {
            Object object = ((Pair)typed.get(taggedChoiceType.finder())).getFirst();
            return object.equals("minecraft:banner") ? this.fix((Typed<?>)typed, (OpticFinder<?>)opticFinder2, (OpticFinder<?>)opticFinder4, (OpticFinder<Pair<String, String>>)opticFinder5) : typed;
        }), (TypeRewriteRule)this.fixTypeEverywhereTyped("Ominous Banner item stack common rarity to uncommon rarity fix", type2, typed -> {
            String string = typed.getOptional(opticFinder).map(Pair::getSecond).orElse("");
            return string.equals("minecraft:white_banner") ? this.fix((Typed<?>)typed, (OpticFinder<?>)opticFinder3, (OpticFinder<?>)opticFinder4, (OpticFinder<Pair<String, String>>)opticFinder5) : typed;
        }));
    }

    private Typed<?> fix(Typed<?> typed, OpticFinder<?> opticFinder, OpticFinder<?> opticFinder2, OpticFinder<Pair<String, String>> opticFinder3) {
        return typed.updateTyped(opticFinder, typed2 -> {
            boolean bl = typed2.getOptionalTyped(opticFinder2).flatMap(typed -> typed.getOptional(opticFinder3)).map(Pair::getSecond).flatMap(LegacyComponentDataFixUtils::extractTranslationString).filter(string -> string.equals("block.minecraft.ominous_banner")).isPresent();
            if (bl) {
                return typed2.updateTyped(opticFinder2, typed -> typed.set(opticFinder3, (Object)Pair.of((Object)References.TEXT_COMPONENT.typeName(), (Object)LegacyComponentDataFixUtils.createTranslatableComponentJson("block.minecraft.ominous_banner")))).update(DSL.remainderFinder(), dynamic -> dynamic.set("minecraft:rarity", dynamic.createString("uncommon")));
            }
            return typed2;
        });
    }
}

