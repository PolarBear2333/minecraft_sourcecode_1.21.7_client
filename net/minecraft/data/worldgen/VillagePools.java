/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.worldgen;

import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.DesertVillagePools;
import net.minecraft.data.worldgen.PlainVillagePools;
import net.minecraft.data.worldgen.SavannaVillagePools;
import net.minecraft.data.worldgen.SnowyVillagePools;
import net.minecraft.data.worldgen.TaigaVillagePools;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class VillagePools {
    public static void bootstrap(BootstrapContext<StructureTemplatePool> bootstrapContext) {
        PlainVillagePools.bootstrap(bootstrapContext);
        SnowyVillagePools.bootstrap(bootstrapContext);
        SavannaVillagePools.bootstrap(bootstrapContext);
        DesertVillagePools.bootstrap(bootstrapContext);
        TaigaVillagePools.bootstrap(bootstrapContext);
    }
}

