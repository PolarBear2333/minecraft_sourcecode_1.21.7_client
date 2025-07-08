/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.kinds.Applicative;
import java.util.Optional;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;

public class AssignProfessionFromJobSite {
    public static BehaviorControl<Villager> create() {
        return BehaviorBuilder.create(instance -> instance.group(instance.present(MemoryModuleType.POTENTIAL_JOB_SITE), instance.registered(MemoryModuleType.JOB_SITE)).apply((Applicative)instance, (memoryAccessor, memoryAccessor2) -> (serverLevel2, villager, l) -> {
            GlobalPos globalPos = (GlobalPos)instance.get(memoryAccessor);
            if (!globalPos.pos().closerToCenterThan(villager.position(), 2.0) && !villager.assignProfessionWhenSpawned()) {
                return false;
            }
            memoryAccessor.erase();
            memoryAccessor2.set(globalPos);
            serverLevel2.broadcastEntityEvent(villager, (byte)14);
            if (!villager.getVillagerData().profession().is(VillagerProfession.NONE)) {
                return true;
            }
            MinecraftServer minecraftServer = serverLevel2.getServer();
            Optional.ofNullable(minecraftServer.getLevel(globalPos.dimension())).flatMap(serverLevel -> serverLevel.getPoiManager().getType(globalPos.pos())).flatMap(holder -> BuiltInRegistries.VILLAGER_PROFESSION.listElements().filter(reference -> ((VillagerProfession)reference.value()).heldJobSite().test((Holder<PoiType>)holder)).findFirst()).ifPresent(reference -> {
                villager.setVillagerData(villager.getVillagerData().withProfession((Holder<VillagerProfession>)reference));
                villager.refreshBrain(serverLevel2);
            });
            return true;
        }));
    }
}

