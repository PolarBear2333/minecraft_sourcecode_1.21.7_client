/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.entity.ai.village;

import com.mojang.logging.LogUtils;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class VillageSiege
implements CustomSpawner {
    private static final Logger LOGGER = LogUtils.getLogger();
    private boolean hasSetupSiege;
    private State siegeState = State.SIEGE_DONE;
    private int zombiesToSpawn;
    private int nextSpawnTime;
    private int spawnX;
    private int spawnY;
    private int spawnZ;

    @Override
    public void tick(ServerLevel serverLevel, boolean bl, boolean bl2) {
        if (serverLevel.isBrightOutside() || !bl) {
            this.siegeState = State.SIEGE_DONE;
            this.hasSetupSiege = false;
            return;
        }
        float f = serverLevel.getTimeOfDay(0.0f);
        if ((double)f == 0.5) {
            State state = this.siegeState = serverLevel.random.nextInt(10) == 0 ? State.SIEGE_TONIGHT : State.SIEGE_DONE;
        }
        if (this.siegeState == State.SIEGE_DONE) {
            return;
        }
        if (!this.hasSetupSiege) {
            if (this.tryToSetupSiege(serverLevel)) {
                this.hasSetupSiege = true;
            } else {
                return;
            }
        }
        if (this.nextSpawnTime > 0) {
            --this.nextSpawnTime;
            return;
        }
        this.nextSpawnTime = 2;
        if (this.zombiesToSpawn > 0) {
            this.trySpawn(serverLevel);
            --this.zombiesToSpawn;
        } else {
            this.siegeState = State.SIEGE_DONE;
        }
    }

    private boolean tryToSetupSiege(ServerLevel serverLevel) {
        for (Player player : serverLevel.players()) {
            BlockPos blockPos;
            if (player.isSpectator() || !serverLevel.isVillage(blockPos = player.blockPosition()) || serverLevel.getBiome(blockPos).is(BiomeTags.WITHOUT_ZOMBIE_SIEGES)) continue;
            for (int i = 0; i < 10; ++i) {
                float f = serverLevel.random.nextFloat() * ((float)Math.PI * 2);
                this.spawnX = blockPos.getX() + Mth.floor(Mth.cos(f) * 32.0f);
                this.spawnY = blockPos.getY();
                this.spawnZ = blockPos.getZ() + Mth.floor(Mth.sin(f) * 32.0f);
                if (this.findRandomSpawnPos(serverLevel, new BlockPos(this.spawnX, this.spawnY, this.spawnZ)) == null) continue;
                this.nextSpawnTime = 0;
                this.zombiesToSpawn = 20;
                break;
            }
            return true;
        }
        return false;
    }

    private void trySpawn(ServerLevel serverLevel) {
        Zombie zombie;
        Vec3 vec3 = this.findRandomSpawnPos(serverLevel, new BlockPos(this.spawnX, this.spawnY, this.spawnZ));
        if (vec3 == null) {
            return;
        }
        try {
            zombie = new Zombie(serverLevel);
            zombie.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(zombie.blockPosition()), EntitySpawnReason.EVENT, null);
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to create zombie for village siege at {}", (Object)vec3, (Object)exception);
            return;
        }
        zombie.snapTo(vec3.x, vec3.y, vec3.z, serverLevel.random.nextFloat() * 360.0f, 0.0f);
        serverLevel.addFreshEntityWithPassengers(zombie);
    }

    @Nullable
    private Vec3 findRandomSpawnPos(ServerLevel serverLevel, BlockPos blockPos) {
        for (int i = 0; i < 10; ++i) {
            int n;
            int n2;
            int n3 = blockPos.getX() + serverLevel.random.nextInt(16) - 8;
            BlockPos blockPos2 = new BlockPos(n3, n2 = serverLevel.getHeight(Heightmap.Types.WORLD_SURFACE, n3, n = blockPos.getZ() + serverLevel.random.nextInt(16) - 8), n);
            if (!serverLevel.isVillage(blockPos2) || !Monster.checkMonsterSpawnRules(EntityType.ZOMBIE, serverLevel, EntitySpawnReason.EVENT, blockPos2, serverLevel.random)) continue;
            return Vec3.atBottomCenterOf(blockPos2);
        }
        return null;
    }

    static enum State {
        SIEGE_CAN_ACTIVATE,
        SIEGE_TONIGHT,
        SIEGE_DONE;

    }
}

