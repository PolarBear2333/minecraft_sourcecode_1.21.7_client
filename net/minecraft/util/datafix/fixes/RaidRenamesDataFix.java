/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DSL
 *  com.mojang.datafixers.DataFix
 *  com.mojang.datafixers.TypeRewriteRule
 *  com.mojang.datafixers.schemas.Schema
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.ExtraDataFixUtils;
import net.minecraft.util.datafix.fixes.References;

public class RaidRenamesDataFix
extends DataFix {
    public RaidRenamesDataFix(Schema schema) {
        super(schema, false);
    }

    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("RaidRenamesDataFix", this.getInputSchema().getType(References.SAVED_DATA_RAIDS), typed -> typed.update(DSL.remainderFinder(), dynamic -> dynamic.update("data", RaidRenamesDataFix::fix)));
    }

    private static Dynamic<?> fix(Dynamic<?> dynamic2) {
        return dynamic2.renameAndFixField("Raids", "raids", dynamic -> dynamic.createList(dynamic.asStream().map(RaidRenamesDataFix::fixRaid))).renameField("Tick", "tick").renameField("NextAvailableID", "next_id");
    }

    private static Dynamic<?> fixRaid(Dynamic<?> dynamic) {
        return ExtraDataFixUtils.fixInlineBlockPos(dynamic, "CX", "CY", "CZ", "center").renameField("Id", "id").renameField("Started", "started").renameField("Active", "active").renameField("TicksActive", "ticks_active").renameField("BadOmenLevel", "raid_omen_level").renameField("GroupsSpawned", "groups_spawned").renameField("PreRaidTicks", "cooldown_ticks").renameField("PostRaidTicks", "post_raid_ticks").renameField("TotalHealth", "total_health").renameField("NumGroups", "group_count").renameField("Status", "status").renameField("HeroesOfTheVillage", "heroes_of_the_village");
    }
}

