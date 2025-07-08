/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public class PatrolSpawner
implements CustomSpawner {
    private int nextTick;

    @Override
    public void tick(ServerLevel serverLevel, boolean bl, boolean bl2) {
        if (!bl) {
            return;
        }
        if (!serverLevel.getGameRules().getBoolean(GameRules.RULE_DO_PATROL_SPAWNING)) {
            return;
        }
        RandomSource randomSource = serverLevel.random;
        --this.nextTick;
        if (this.nextTick > 0) {
            return;
        }
        this.nextTick += 12000 + randomSource.nextInt(1200);
        long l = serverLevel.getDayTime() / 24000L;
        if (l < 5L || !serverLevel.isBrightOutside()) {
            return;
        }
        if (randomSource.nextInt(5) != 0) {
            return;
        }
        int n = serverLevel.players().size();
        if (n < 1) {
            return;
        }
        Player player = serverLevel.players().get(randomSource.nextInt(n));
        if (player.isSpectator()) {
            return;
        }
        if (serverLevel.isCloseToVillage(player.blockPosition(), 2)) {
            return;
        }
        int n2 = (24 + randomSource.nextInt(24)) * (randomSource.nextBoolean() ? -1 : 1);
        int n3 = (24 + randomSource.nextInt(24)) * (randomSource.nextBoolean() ? -1 : 1);
        BlockPos.MutableBlockPos mutableBlockPos = player.blockPosition().mutable().move(n2, 0, n3);
        int n4 = 10;
        if (!serverLevel.hasChunksAt(mutableBlockPos.getX() - 10, mutableBlockPos.getZ() - 10, mutableBlockPos.getX() + 10, mutableBlockPos.getZ() + 10)) {
            return;
        }
        Holder<Biome> holder = serverLevel.getBiome(mutableBlockPos);
        if (holder.is(BiomeTags.WITHOUT_PATROL_SPAWNS)) {
            return;
        }
        int n5 = (int)Math.ceil(serverLevel.getCurrentDifficultyAt(mutableBlockPos).getEffectiveDifficulty()) + 1;
        for (int i = 0; i < n5; ++i) {
            mutableBlockPos.setY(serverLevel.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, mutableBlockPos).getY());
            if (i == 0) {
                if (!this.spawnPatrolMember(serverLevel, mutableBlockPos, randomSource, true)) {
                    break;
                }
            } else {
                this.spawnPatrolMember(serverLevel, mutableBlockPos, randomSource, false);
            }
            mutableBlockPos.setX(mutableBlockPos.getX() + randomSource.nextInt(5) - randomSource.nextInt(5));
            mutableBlockPos.setZ(mutableBlockPos.getZ() + randomSource.nextInt(5) - randomSource.nextInt(5));
        }
    }

    private boolean spawnPatrolMember(ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource, boolean bl) {
        BlockState blockState = serverLevel.getBlockState(blockPos);
        if (!NaturalSpawner.isValidEmptySpawnBlock(serverLevel, blockPos, blockState, blockState.getFluidState(), EntityType.PILLAGER)) {
            return false;
        }
        if (!PatrollingMonster.checkPatrollingMonsterSpawnRules(EntityType.PILLAGER, serverLevel, EntitySpawnReason.PATROL, blockPos, randomSource)) {
            return false;
        }
        PatrollingMonster patrollingMonster = EntityType.PILLAGER.create(serverLevel, EntitySpawnReason.PATROL);
        if (patrollingMonster != null) {
            if (bl) {
                patrollingMonster.setPatrolLeader(true);
                patrollingMonster.findPatrolTarget();
            }
            patrollingMonster.setPos(blockPos.getX(), blockPos.getY(), blockPos.getZ());
            patrollingMonster.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(blockPos), EntitySpawnReason.PATROL, null);
            serverLevel.addFreshEntityWithPassengers(patrollingMonster);
            return true;
        }
        return false;
    }
}

