/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.npc;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.AABB;

public class CatSpawner
implements CustomSpawner {
    private static final int TICK_DELAY = 1200;
    private int nextTick;

    @Override
    public void tick(ServerLevel serverLevel, boolean bl, boolean bl2) {
        if (!bl2 || !serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
            return;
        }
        --this.nextTick;
        if (this.nextTick > 0) {
            return;
        }
        this.nextTick = 1200;
        ServerPlayer serverPlayer = serverLevel.getRandomPlayer();
        if (serverPlayer == null) {
            return;
        }
        RandomSource randomSource = serverLevel.random;
        int n = (8 + randomSource.nextInt(24)) * (randomSource.nextBoolean() ? -1 : 1);
        int n2 = (8 + randomSource.nextInt(24)) * (randomSource.nextBoolean() ? -1 : 1);
        BlockPos blockPos = serverPlayer.blockPosition().offset(n, 0, n2);
        int n3 = 10;
        if (!serverLevel.hasChunksAt(blockPos.getX() - 10, blockPos.getZ() - 10, blockPos.getX() + 10, blockPos.getZ() + 10)) {
            return;
        }
        if (SpawnPlacements.isSpawnPositionOk(EntityType.CAT, serverLevel, blockPos)) {
            if (serverLevel.isCloseToVillage(blockPos, 2)) {
                this.spawnInVillage(serverLevel, blockPos);
            } else if (serverLevel.structureManager().getStructureWithPieceAt(blockPos, StructureTags.CATS_SPAWN_IN).isValid()) {
                this.spawnInHut(serverLevel, blockPos);
            }
        }
    }

    private void spawnInVillage(ServerLevel serverLevel, BlockPos blockPos) {
        List<Cat> list;
        int n = 48;
        if (serverLevel.getPoiManager().getCountInRange(holder -> holder.is(PoiTypes.HOME), blockPos, 48, PoiManager.Occupancy.IS_OCCUPIED) > 4L && (list = serverLevel.getEntitiesOfClass(Cat.class, new AABB(blockPos).inflate(48.0, 8.0, 48.0))).size() < 5) {
            this.spawnCat(blockPos, serverLevel, false);
        }
    }

    private void spawnInHut(ServerLevel serverLevel, BlockPos blockPos) {
        int n = 16;
        List<Cat> list = serverLevel.getEntitiesOfClass(Cat.class, new AABB(blockPos).inflate(16.0, 8.0, 16.0));
        if (list.isEmpty()) {
            this.spawnCat(blockPos, serverLevel, true);
        }
    }

    private void spawnCat(BlockPos blockPos, ServerLevel serverLevel, boolean bl) {
        Cat cat = EntityType.CAT.create(serverLevel, EntitySpawnReason.NATURAL);
        if (cat == null) {
            return;
        }
        cat.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(blockPos), EntitySpawnReason.NATURAL, null);
        if (bl) {
            cat.setPersistenceRequired();
        }
        cat.snapTo(blockPos, 0.0f, 0.0f);
        serverLevel.addFreshEntityWithPassengers(cat);
    }
}

