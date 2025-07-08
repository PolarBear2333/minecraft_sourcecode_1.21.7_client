/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.datafixers.OpticFinder
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.datafixers.types.Type
 *  com.mojang.serialization.Dynamic
 *  javax.annotation.Nullable
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.util.datafix.fixes.References;

public class ScoreboardDisplaySlotFix
extends DataFix {
    private static final Map<String, String> SLOT_RENAMES = ImmutableMap.builder().put((Object)"slot_0", (Object)"list").put((Object)"slot_1", (Object)"sidebar").put((Object)"slot_2", (Object)"below_name").put((Object)"slot_3", (Object)"sidebar.team.black").put((Object)"slot_4", (Object)"sidebar.team.dark_blue").put((Object)"slot_5", (Object)"sidebar.team.dark_green").put((Object)"slot_6", (Object)"sidebar.team.dark_aqua").put((Object)"slot_7", (Object)"sidebar.team.dark_red").put((Object)"slot_8", (Object)"sidebar.team.dark_purple").put((Object)"slot_9", (Object)"sidebar.team.gold").put((Object)"slot_10", (Object)"sidebar.team.gray").put((Object)"slot_11", (Object)"sidebar.team.dark_gray").put((Object)"slot_12", (Object)"sidebar.team.blue").put((Object)"slot_13", (Object)"sidebar.team.green").put((Object)"slot_14", (Object)"sidebar.team.aqua").put((Object)"slot_15", (Object)"sidebar.team.red").put((Object)"slot_16", (Object)"sidebar.team.light_purple").put((Object)"slot_17", (Object)"sidebar.team.yellow").put((Object)"slot_18", (Object)"sidebar.team.white").build();

    public ScoreboardDisplaySlotFix(Schema schema) {
        super(schema, false);
    }

    @Nullable
    private static String rename(String string) {
        return SLOT_RENAMES.get(string);
    }

    protected TypeRewriteRule makeRule() {
        Type type = this.getInputSchema().getType(References.SAVED_DATA_SCOREBOARD);
        OpticFinder opticFinder = type.findField("data");
        return this.fixTypeEverywhereTyped("Scoreboard DisplaySlot rename", type, typed2 -> typed2.updateTyped(opticFinder, typed -> typed.update(DSL.remainderFinder(), dynamic2 -> dynamic2.update("DisplaySlots", dynamic -> dynamic.updateMapValues(pair -> pair.mapFirst(dynamic -> (Dynamic)DataFixUtils.orElse(dynamic.asString().result().map(ScoreboardDisplaySlotFix::rename).map(arg_0 -> ((Dynamic)dynamic).createString(arg_0)), (Object)dynamic)))))));
    }
}

