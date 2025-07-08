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
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class EntitySpawnerItemVariantComponentFix
extends DataFix {
    public EntitySpawnerItemVariantComponentFix(Schema schema) {
        super(schema, false);
    }

    public final TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder opticFinder = DSL.fieldFinder((String)"id", (Type)DSL.named((String)References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        OpticFinder opticFinder2 = type.findField("components");
        return this.fixTypeEverywhereTyped("ItemStack bucket_entity_data variants to separate components", type, typed2 -> {
            String string;
            return switch (string = typed2.getOptional(opticFinder).map(Pair::getSecond).orElse("")) {
                case "minecraft:salmon_bucket" -> typed2.updateTyped(opticFinder2, EntitySpawnerItemVariantComponentFix::fixSalmonBucket);
                case "minecraft:axolotl_bucket" -> typed2.updateTyped(opticFinder2, EntitySpawnerItemVariantComponentFix::fixAxolotlBucket);
                case "minecraft:tropical_fish_bucket" -> typed2.updateTyped(opticFinder2, EntitySpawnerItemVariantComponentFix::fixTropicalFishBucket);
                case "minecraft:painting" -> typed2.updateTyped(opticFinder2, typed -> Util.writeAndReadTypedOrThrow(typed, typed.getType(), EntitySpawnerItemVariantComponentFix::fixPainting));
                default -> typed2;
            };
        });
    }

    private static String getBaseColor(int n) {
        return ExtraDataFixUtils.dyeColorIdToName(n >> 16 & 0xFF);
    }

    private static String getPatternColor(int n) {
        return ExtraDataFixUtils.dyeColorIdToName(n >> 24 & 0xFF);
    }

    private static String getPattern(int n) {
        return switch (n & 0xFFFF) {
            default -> "kob";
            case 256 -> "sunstreak";
            case 512 -> "snooper";
            case 768 -> "dasher";
            case 1024 -> "brinely";
            case 1280 -> "spotty";
            case 1 -> "flopper";
            case 257 -> "stripey";
            case 513 -> "glitter";
            case 769 -> "blockfish";
            case 1025 -> "betty";
            case 1281 -> "clayfish";
        };
    }

    private static <T> Dynamic<T> fixTropicalFishBucket(Dynamic<T> dynamic2, Dynamic<T> dynamic3) {
        Optional optional = dynamic3.get("BucketVariantTag").asNumber().result();
        if (optional.isEmpty()) {
            return dynamic2;
        }
        int n = ((Number)optional.get()).intValue();
        String string = EntitySpawnerItemVariantComponentFix.getPattern(n);
        String string2 = EntitySpawnerItemVariantComponentFix.getBaseColor(n);
        String string3 = EntitySpawnerItemVariantComponentFix.getPatternColor(n);
        return dynamic2.update("minecraft:bucket_entity_data", dynamic -> dynamic.remove("BucketVariantTag")).set("minecraft:tropical_fish/pattern", dynamic2.createString(string)).set("minecraft:tropical_fish/base_color", dynamic2.createString(string2)).set("minecraft:tropical_fish/pattern_color", dynamic2.createString(string3));
    }

    private static <T> Dynamic<T> fixAxolotlBucket(Dynamic<T> dynamic2, Dynamic<T> dynamic3) {
        Optional optional = dynamic3.get("Variant").asNumber().result();
        if (optional.isEmpty()) {
            return dynamic2;
        }
        String string = switch (((Number)optional.get()).intValue()) {
            default -> "lucy";
            case 1 -> "wild";
            case 2 -> "gold";
            case 3 -> "cyan";
            case 4 -> "blue";
        };
        return dynamic2.update("minecraft:bucket_entity_data", dynamic -> dynamic.remove("Variant")).set("minecraft:axolotl/variant", dynamic2.createString(string));
    }

    private static <T> Dynamic<T> fixSalmonBucket(Dynamic<T> dynamic2, Dynamic<T> dynamic3) {
        Optional optional = dynamic3.get("type").result();
        if (optional.isEmpty()) {
            return dynamic2;
        }
        return dynamic2.update("minecraft:bucket_entity_data", dynamic -> dynamic.remove("type")).set("minecraft:salmon/size", (Dynamic)optional.get());
    }

    private static <T> Dynamic<T> fixPainting(Dynamic<T> dynamic) {
        Optional optional = dynamic.get("minecraft:entity_data").result();
        if (optional.isEmpty()) {
            return dynamic;
        }
        if (((Dynamic)optional.get()).get("id").asString().result().filter(string -> string.equals("minecraft:painting")).isEmpty()) {
            return dynamic;
        }
        Optional optional2 = ((Dynamic)optional.get()).get("variant").result();
        Dynamic dynamic2 = ((Dynamic)optional.get()).remove("variant");
        dynamic = dynamic2.remove("id").equals((Object)dynamic2.emptyMap()) ? dynamic.remove("minecraft:entity_data") : dynamic.set("minecraft:entity_data", dynamic2);
        if (optional2.isPresent()) {
            dynamic = dynamic.set("minecraft:painting/variant", (Dynamic)optional2.get());
        }
        return dynamic;
    }

    @FunctionalInterface
    static interface Fixer
    extends Function<Typed<?>, Typed<?>> {
        @Override
        default public Typed<?> apply(Typed<?> typed) {
            return typed.update(DSL.remainderFinder(), this::fixRemainder);
        }

        default public <T> Dynamic<T> fixRemainder(Dynamic<T> dynamic) {
            return dynamic.get("minecraft:bucket_entity_data").result().map(dynamic2 -> this.fixRemainder(dynamic, (Dynamic)dynamic2)).orElse(dynamic);
        }

        public <T> Dynamic<T> fixRemainder(Dynamic<T> var1, Dynamic<T> var2);
    }
}

