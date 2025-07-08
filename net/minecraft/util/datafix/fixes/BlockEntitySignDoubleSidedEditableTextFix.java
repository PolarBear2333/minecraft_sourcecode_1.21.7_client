/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Streams
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Streams;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;
import net.minecraft.util.datafix.fixes.NamedEntityWriteReadFix;
import net.minecraft.util.datafix.fixes.References;

public class BlockEntitySignDoubleSidedEditableTextFix
extends NamedEntityWriteReadFix {
    public static final List<String> FIELDS_TO_DROP = List.of("Text1", "Text2", "Text3", "Text4", "FilteredText1", "FilteredText2", "FilteredText3", "FilteredText4", "Color", "GlowingText");
    public static final String FILTERED_CORRECT = "_filtered_correct";
    private static final String DEFAULT_COLOR = "black";

    public BlockEntitySignDoubleSidedEditableTextFix(Schema schema, String string, String string2) {
        super(schema, true, string, References.BLOCK_ENTITY, string2);
    }

    @Override
    protected <T> Dynamic<T> fix(Dynamic<T> dynamic) {
        dynamic = dynamic.set("front_text", BlockEntitySignDoubleSidedEditableTextFix.fixFrontTextTag(dynamic)).set("back_text", BlockEntitySignDoubleSidedEditableTextFix.createDefaultText(dynamic)).set("is_waxed", dynamic.createBoolean(false)).set(FILTERED_CORRECT, dynamic.createBoolean(true));
        for (String string : FIELDS_TO_DROP) {
            dynamic = dynamic.remove(string);
        }
        return dynamic;
    }

    private static <T> Dynamic<T> fixFrontTextTag(Dynamic<T> dynamic) {
        Dynamic dynamic2 = LegacyComponentDataFixUtils.createEmptyComponent(dynamic.getOps());
        List<Dynamic> list = BlockEntitySignDoubleSidedEditableTextFix.getLines(dynamic, "Text").map(optional -> optional.orElse(dynamic2)).toList();
        Dynamic dynamic3 = dynamic.emptyMap().set("messages", dynamic.createList(list.stream())).set("color", dynamic.get("Color").result().orElse(dynamic.createString(DEFAULT_COLOR))).set("has_glowing_text", dynamic.get("GlowingText").result().orElse(dynamic.createBoolean(false)));
        List<Optional<Dynamic<T>>> list2 = BlockEntitySignDoubleSidedEditableTextFix.getLines(dynamic, "FilteredText").toList();
        if (list2.stream().anyMatch(Optional::isPresent)) {
            dynamic3 = dynamic3.set("filtered_messages", dynamic.createList(Streams.mapWithIndex(list2.stream(), (optional, l) -> {
                Dynamic dynamic = (Dynamic)list.get((int)l);
                return optional.orElse(dynamic);
            })));
        }
        return dynamic3;
    }

    private static <T> Stream<Optional<Dynamic<T>>> getLines(Dynamic<T> dynamic, String string) {
        return Stream.of(dynamic.get(string + "1").result(), dynamic.get(string + "2").result(), dynamic.get(string + "3").result(), dynamic.get(string + "4").result());
    }

    private static <T> Dynamic<T> createDefaultText(Dynamic<T> dynamic) {
        return dynamic.emptyMap().set("messages", BlockEntitySignDoubleSidedEditableTextFix.createEmptyLines(dynamic)).set("color", dynamic.createString(DEFAULT_COLOR)).set("has_glowing_text", dynamic.createBoolean(false));
    }

    private static <T> Dynamic<T> createEmptyLines(Dynamic<T> dynamic) {
        Dynamic dynamic2 = LegacyComponentDataFixUtils.createEmptyComponent(dynamic.getOps());
        return dynamic.createList(Stream.of(dynamic2, dynamic2, dynamic2, dynamic2));
    }
}

