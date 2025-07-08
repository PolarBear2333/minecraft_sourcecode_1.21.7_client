/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.npc;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.animal.horse.TraderLlama;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ServerLevelData;

public class WanderingTraderSpawner
implements CustomSpawner {
    private static final int DEFAULT_TICK_DELAY = 1200;
    public static final int DEFAULT_SPAWN_DELAY = 24000;
    private static final int MIN_SPAWN_CHANCE = 25;
    private static final int MAX_SPAWN_CHANCE = 75;
    private static final int SPAWN_CHANCE_INCREASE = 25;
    private static final int SPAWN_ONE_IN_X_CHANCE = 10;
    private static final int NUMBER_OF_SPAWN_ATTEMPTS = 10;
    private final RandomSource random = RandomSource.create();
    private final ServerLevelData serverLevelData;
    private int tickDelay;
    private int spawnDelay;
    private int spawnChance;

    public WanderingTraderSpawner(ServerLevelData serverLevelData) {
        this.serverLevelData = serverLevelData;
        this.tickDelay = 1200;
        this.spawnDelay = serverLevelData.getWanderingTraderSpawnDelay();
        this.spawnChance = serverLevelData.getWanderingTraderSpawnChance();
        if (this.spawnDelay == 0 && this.spawnChance == 0) {
            this.spawnDelay = 24000;
            serverLevelData.setWanderingTraderSpawnDelay(this.spawnDelay);
            this.spawnChance = 25;
            serverLevelData.setWanderingTraderSpawnChance(this.spawnChance);
        }
    }

    @Override
    public void tick(ServerLevel serverLevel, boolean bl, boolean bl2) {
        if (!serverLevel.getGameRules().getBoolean(GameRules.RULE_DO_TRADER_SPAWNING)) {
            return;
        }
        if (--this.tickDelay > 0) {
            return;
        }
        this.tickDelay = 1200;
        this.spawnDelay -= 1200;
        this.serverLevelData.setWanderingTraderSpawnDelay(this.spawnDelay);
        if (this.spawnDelay > 0) {
            return;
        }
        this.spawnDelay = 24000;
        if (!serverLevel.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)) {
            return;
        }
        int n = this.spawnChance;
        this.spawnChance = Mth.clamp(this.spawnChance + 25, 25, 75);
        this.serverLevelData.setWanderingTraderSpawnChance(this.spawnChance);
        if (this.random.nextInt(100) > n) {
            return;
        }
        if (this.spawn(serverLevel)) {
            this.spawnChance = 25;
        }
    }

    private boolean spawn(ServerLevel serverLevel) {
        ServerPlayer serverPlayer = serverLevel.getRandomPlayer();
        if (serverPlayer == null) {
            return true;
        }
        if (this.random.nextInt(10) != 0) {
            return false;
        }
        BlockPos blockPos2 = serverPlayer.blockPosition();
        int n = 48;
        PoiManager poiManager = serverLevel.getPoiManager();
        Optional<BlockPos> optional = poiManager.find(holder -> holder.is(PoiTypes.MEETING), blockPos -> true, blockPos2, 48, PoiManager.Occupancy.ANY);
        BlockPos blockPos3 = optional.orElse(blockPos2);
        BlockPos blockPos4 = this.findSpawnPositionNear(serverLevel, blockPos3, 48);
        if (blockPos4 != null && this.hasEnoughSpace(serverLevel, blockPos4)) {
            if (serverLevel.getBiome(blockPos4).is(BiomeTags.WITHOUT_WANDERING_TRADER_SPAWNS)) {
                return false;
            }
            WanderingTrader wanderingTrader = EntityType.WANDERING_TRADER.spawn(serverLevel, blockPos4, EntitySpawnReason.EVENT);
            if (wanderingTrader != null) {
                for (int i = 0; i < 2; ++i) {
                    this.tryToSpawnLlamaFor(serverLevel, wanderingTrader, 4);
                }
                this.serverLevelData.setWanderingTraderId(wanderingTrader.getUUID());
                wanderingTrader.setDespawnDelay(48000);
                wanderingTrader.setWanderTarget(blockPos3);
                wanderingTrader.setHomeTo(blockPos3, 16);
                return true;
            }
        }
        return false;
    }

    private void tryToSpawnLlamaFor(ServerLevel serverLevel, WanderingTrader wanderingTrader, int n) {
        BlockPos blockPos = this.findSpawnPositionNear(serverLevel, wanderingTrader.blockPosition(), n);
        if (blockPos == null) {
            return;
        }
        TraderLlama traderLlama = EntityType.TRADER_LLAMA.spawn(serverLevel, blockPos, EntitySpawnReason.EVENT);
        if (traderLlama == null) {
            return;
        }
        traderLlama.setLeashedTo(wanderingTrader, true);
    }

    @Nullable
    private BlockPos findSpawnPositionNear(LevelReader levelReader, BlockPos blockPos, int n) {
        BlockPos blockPos2 = null;
        SpawnPlacementType spawnPlacementType = SpawnPlacements.getPlacementType(EntityType.WANDERING_TRADER);
        for (int i = 0; i < 10; ++i) {
            int n2;
            int n3;
            int n4 = blockPos.getX() + this.random.nextInt(n * 2) - n;
            BlockPos blockPos3 = new BlockPos(n4, n3 = levelReader.getHeight(Heightmap.Types.WORLD_SURFACE, n4, n2 = blockPos.getZ() + this.random.nextInt(n * 2) - n), n2);
            if (!spawnPlacementType.isSpawnPositionOk(levelReader, blockPos3, EntityType.WANDERING_TRADER)) continue;
            blockPos2 = blockPos3;
            break;
        }
        return blockPos2;
    }

    private boolean hasEnoughSpace(BlockGetter blockGetter, BlockPos blockPos) {
        for (BlockPos blockPos2 : BlockPos.betweenClosed(blockPos, blockPos.offset(1, 2, 1))) {
            if (blockGetter.getBlockState(blockPos2).getCollisionShape(blockGetter, blockPos2).isEmpty()) continue;
            return false;
        }
        return true;
    }
}

