/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.ai.goal;

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.Vec3;

public class GolemRandomStrollInVillageGoal
extends RandomStrollGoal {
    private static final int POI_SECTION_SCAN_RADIUS = 2;
    private static final int VILLAGER_SCAN_RADIUS = 32;
    private static final int RANDOM_POS_XY_DISTANCE = 10;
    private static final int RANDOM_POS_Y_DISTANCE = 7;

    public GolemRandomStrollInVillageGoal(PathfinderMob pathfinderMob, double d) {
        super(pathfinderMob, d, 240, false);
    }

    @Override
    @Nullable
    protected Vec3 getPosition() {
        Vec3 vec3;
        float f = this.mob.level().random.nextFloat();
        if (this.mob.level().random.nextFloat() < 0.3f) {
            return this.getPositionTowardsAnywhere();
        }
        if (f < 0.7f) {
            vec3 = this.getPositionTowardsVillagerWhoWantsGolem();
            if (vec3 == null) {
                vec3 = this.getPositionTowardsPoi();
            }
        } else {
            vec3 = this.getPositionTowardsPoi();
            if (vec3 == null) {
                vec3 = this.getPositionTowardsVillagerWhoWantsGolem();
            }
        }
        return vec3 == null ? this.getPositionTowardsAnywhere() : vec3;
    }

    @Nullable
    private Vec3 getPositionTowardsAnywhere() {
        return LandRandomPos.getPos(this.mob, 10, 7);
    }

    @Nullable
    private Vec3 getPositionTowardsVillagerWhoWantsGolem() {
        ServerLevel serverLevel = (ServerLevel)this.mob.level();
        List<Villager> list = serverLevel.getEntities(EntityType.VILLAGER, this.mob.getBoundingBox().inflate(32.0), this::doesVillagerWantGolem);
        if (list.isEmpty()) {
            return null;
        }
        Villager villager = list.get(this.mob.level().random.nextInt(list.size()));
        Vec3 vec3 = villager.position();
        return LandRandomPos.getPosTowards(this.mob, 10, 7, vec3);
    }

    @Nullable
    private Vec3 getPositionTowardsPoi() {
        SectionPos sectionPos = this.getRandomVillageSection();
        if (sectionPos == null) {
            return null;
        }
        BlockPos blockPos = this.getRandomPoiWithinSection(sectionPos);
        if (blockPos == null) {
            return null;
        }
        return LandRandomPos.getPosTowards(this.mob, 10, 7, Vec3.atBottomCenterOf(blockPos));
    }

    @Nullable
    private SectionPos getRandomVillageSection() {
        ServerLevel serverLevel = (ServerLevel)this.mob.level();
        List list = SectionPos.cube(SectionPos.of(this.mob), 2).filter(sectionPos -> serverLevel.sectionsToVillage((SectionPos)sectionPos) == 0).collect(Collectors.toList());
        if (list.isEmpty()) {
            return null;
        }
        return (SectionPos)list.get(serverLevel.random.nextInt(list.size()));
    }

    @Nullable
    private BlockPos getRandomPoiWithinSection(SectionPos sectionPos) {
        ServerLevel serverLevel = (ServerLevel)this.mob.level();
        PoiManager poiManager = serverLevel.getPoiManager();
        List list = poiManager.getInRange(holder -> true, sectionPos.center(), 8, PoiManager.Occupancy.IS_OCCUPIED).map(PoiRecord::getPos).collect(Collectors.toList());
        if (list.isEmpty()) {
            return null;
        }
        return (BlockPos)list.get(serverLevel.random.nextInt(list.size()));
    }

    private boolean doesVillagerWantGolem(Villager villager) {
        return villager.wantsToSpawnGolem(this.mob.level().getGameTime());
    }
}

