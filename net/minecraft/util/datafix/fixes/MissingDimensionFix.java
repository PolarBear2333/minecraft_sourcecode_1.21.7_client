/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.FieldFinder
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.Typed
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.datafixers.types.templates.CompoundList$CompoundListType
 *  com.mojang.datafixers.util.Either
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.datafixers.util.Unit
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.FieldFinder;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.CompoundList;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Map;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.fixes.WorldGenSettingsFix;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class MissingDimensionFix
extends DataFix {
    public MissingDimensionFix(Schema schema, boolean bl) {
        super(schema, bl);
    }

    protected static <A> Type<Pair<A, Dynamic<?>>> fields(String string, Type<A> type) {
        return DSL.and((Type)DSL.field((String)string, type), (Type)DSL.remainderType());
    }

    protected static <A> Type<Pair<Either<A, Unit>, Dynamic<?>>> optionalFields(String string, Type<A> type) {
        return DSL.and((Type)DSL.optional((Type)DSL.field((String)string, type)), (Type)DSL.remainderType());
    }

    protected static <A1, A2> Type<Pair<Either<A1, Unit>, Pair<Either<A2, Unit>, Dynamic<?>>>> optionalFields(String string, Type<A1> type, String string2, Type<A2> type2) {
        return DSL.and((Type)DSL.optional((Type)DSL.field((String)string, type)), (Type)DSL.optional((Type)DSL.field((String)string2, type2)), (Type)DSL.remainderType());
    }

    protected TypeRewriteRule makeRule() {
        Schema schema = this.getInputSchema();
        Type type = DSL.taggedChoiceType((String)"type", (Type)DSL.string(), (Map)ImmutableMap.of((Object)"minecraft:debug", (Object)DSL.remainderType(), (Object)"minecraft:flat", MissingDimensionFix.flatType(schema), (Object)"minecraft:noise", MissingDimensionFix.optionalFields("biome_source", DSL.taggedChoiceType((String)"type", (Type)DSL.string(), (Map)ImmutableMap.of((Object)"minecraft:fixed", MissingDimensionFix.fields("biome", schema.getType(References.BIOME)), (Object)"minecraft:multi_noise", (Object)DSL.list(MissingDimensionFix.fields("biome", schema.getType(References.BIOME))), (Object)"minecraft:checkerboard", MissingDimensionFix.fields("biomes", DSL.list((Type)schema.getType(References.BIOME))), (Object)"minecraft:vanilla_layered", (Object)DSL.remainderType(), (Object)"minecraft:the_end", (Object)DSL.remainderType())), "settings", DSL.or((Type)DSL.string(), MissingDimensionFix.optionalFields("default_block", schema.getType(References.BLOCK_NAME), "default_fluid", schema.getType(References.BLOCK_NAME))))));
        CompoundList.CompoundListType compoundListType = DSL.compoundList(NamespacedSchema.namespacedString(), MissingDimensionFix.fields("generator", type));
        Type type2 = DSL.and((Type)compoundListType, (Type)DSL.remainderType());
        Type type3 = schema.getType(References.WORLD_GEN_SETTINGS);
        FieldFinder fieldFinder = new FieldFinder("dimensions", type2);
        if (!type3.findFieldType("dimensions").equals((Object)type2)) {
            throw new IllegalStateException();
        }
        OpticFinder opticFinder = compoundListType.finder();
        return this.fixTypeEverywhereTyped("MissingDimensionFix", type3, typed -> typed.updateTyped((OpticFinder)fieldFinder, typed3 -> typed3.updateTyped(opticFinder, typed2 -> {
            if (!(typed2.getValue() instanceof List)) {
                throw new IllegalStateException("List exptected");
            }
            if (((List)typed2.getValue()).isEmpty()) {
                Dynamic dynamic = (Dynamic)typed.get(DSL.remainderFinder());
                Dynamic dynamic2 = this.recreateSettings(dynamic);
                return (Typed)DataFixUtils.orElse(compoundListType.readTyped(dynamic2).result().map(Pair::getFirst), (Object)typed2);
            }
            return typed2;
        })));
    }

    protected static Type<? extends Pair<? extends Either<? extends Pair<? extends Either<?, Unit>, ? extends Pair<? extends Either<? extends List<? extends Pair<? extends Either<?, Unit>, Dynamic<?>>>, Unit>, Dynamic<?>>>, Unit>, Dynamic<?>>> flatType(Schema schema) {
        return MissingDimensionFix.optionalFields("settings", MissingDimensionFix.optionalFields("biome", schema.getType(References.BIOME), "layers", DSL.list(MissingDimensionFix.optionalFields("block", schema.getType(References.BLOCK_NAME)))));
    }

    private <T> Dynamic<T> recreateSettings(Dynamic<T> dynamic) {
        long l = dynamic.get("seed").asLong(0L);
        return new Dynamic(dynamic.getOps(), WorldGenSettingsFix.vanillaLevels(dynamic, l, WorldGenSettingsFix.defaultOverworld(dynamic, l), false));
    }
}

