/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.AncientCityStructurePools;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public class AncientCityStructurePieces {
    public static final ResourceKey<StructureTemplatePool> START = Pools.createKey("ancient_city/city_center");

    public static void bootstrap(BootstrapContext<StructureTemplatePool> bootstrapContext) {
        HolderGetter<StructureProcessorList> holderGetter = bootstrapContext.lookup(Registries.PROCESSOR_LIST);
        Holder.Reference<StructureProcessorList> reference = holderGetter.getOrThrow(ProcessorLists.ANCIENT_CITY_START_DEGRADATION);
        HolderGetter<StructureTemplatePool> holderGetter2 = bootstrapContext.lookup(Registries.TEMPLATE_POOL);
        Holder.Reference<StructureTemplatePool> reference2 = holderGetter2.getOrThrow(Pools.EMPTY);
        bootstrapContext.register(START, new StructureTemplatePool(reference2, (List<Pair<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>, Integer>>)ImmutableList.of((Object)Pair.of(StructurePoolElement.single("ancient_city/city_center/city_center_1", reference), (Object)1), (Object)Pair.of(StructurePoolElement.single("ancient_city/city_center/city_center_2", reference), (Object)1), (Object)Pair.of(StructurePoolElement.single("ancient_city/city_center/city_center_3", reference), (Object)1)), StructureTemplatePool.Projection.RIGID));
        AncientCityStructurePools.bootstrap(bootstrapContext);
    }
}

