/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.References;

public class LegacyDimensionIdFix
extends DataFix {
    public LegacyDimensionIdFix(Schema schema) {
        super(schema, false);
    }

    public TypeRewriteRule makeRule() {
        TypeRewriteRule typeRewriteRule = this.fixTypeEverywhereTyped("PlayerLegacyDimensionFix", this.getInputSchema().getType(References.PLAYER), typed -> typed.update(DSL.remainderFinder(), this::fixPlayer));
        Type type = this.getInputSchema().getType(References.SAVED_DATA_MAP_DATA);
        OpticFinder opticFinder = type.findField("data");
        TypeRewriteRule typeRewriteRule2 = this.fixTypeEverywhereTyped("MapLegacyDimensionFix", type, typed2 -> typed2.updateTyped(opticFinder, typed -> typed.update(DSL.remainderFinder(), this::fixMap)));
        return TypeRewriteRule.seq((TypeRewriteRule)typeRewriteRule, (TypeRewriteRule)typeRewriteRule2);
    }

    private <T> Dynamic<T> fixMap(Dynamic<T> dynamic) {
        return dynamic.update("dimension", this::fixDimensionId);
    }

    private <T> Dynamic<T> fixPlayer(Dynamic<T> dynamic) {
        return dynamic.update("Dimension", this::fixDimensionId);
    }

    private <T> Dynamic<T> fixDimensionId(Dynamic<T> dynamic) {
        return (Dynamic)DataFixUtils.orElse(dynamic.asNumber().result().map(number -> switch (number.intValue()) {
            case -1 -> dynamic.createString("minecraft:the_nether");
            case 1 -> dynamic.createString("minecraft:the_end");
            default -> dynamic.createString("minecraft:overworld");
        }), dynamic);
    }
}

