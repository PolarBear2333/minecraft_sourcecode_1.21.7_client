/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.templates.TypeTemplate
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class V4312
extends NamespacedSchema {
    public V4312(int n, Schema schema) {
        super(n, schema);
    }

    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> map, Map<String, Supplier<TypeTemplate>> map2) {
        super.registerTypes(schema, map, map2);
        schema.registerType(false, References.PLAYER, () -> DSL.and((TypeTemplate)References.ENTITY_EQUIPMENT.in(schema), (TypeTemplate)DSL.optionalFields((Pair[])new Pair[]{Pair.of((Object)"RootVehicle", (Object)DSL.optionalFields((String)"Entity", (TypeTemplate)References.ENTITY_TREE.in(schema))), Pair.of((Object)"ender_pearls", (Object)DSL.list((TypeTemplate)References.ENTITY_TREE.in(schema))), Pair.of((Object)"Inventory", (Object)DSL.list((TypeTemplate)References.ITEM_STACK.in(schema))), Pair.of((Object)"EnderItems", (Object)DSL.list((TypeTemplate)References.ITEM_STACK.in(schema))), Pair.of((Object)"ShoulderEntityLeft", (Object)References.ENTITY_TREE.in(schema)), Pair.of((Object)"ShoulderEntityRight", (Object)References.ENTITY_TREE.in(schema)), Pair.of((Object)"recipeBook", (Object)DSL.optionalFields((String)"recipes", (TypeTemplate)DSL.list((TypeTemplate)References.RECIPE.in(schema)), (String)"toBeDisplayed", (TypeTemplate)DSL.list((TypeTemplate)References.RECIPE.in(schema))))})));
    }
}

