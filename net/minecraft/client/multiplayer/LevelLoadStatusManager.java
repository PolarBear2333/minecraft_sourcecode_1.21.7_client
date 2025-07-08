/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.multiplayer;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;

public class LevelLoadStatusManager {
    private final LocalPlayer player;
    private final ClientLevel level;
    private final LevelRenderer levelRenderer;
    private Status status = Status.WAITING_FOR_SERVER;

    public LevelLoadStatusManager(LocalPlayer localPlayer, ClientLevel clientLevel, LevelRenderer levelRenderer) {
        this.player = localPlayer;
        this.level = clientLevel;
        this.levelRenderer = levelRenderer;
    }

    public void tick() {
        switch (this.status.ordinal()) {
            case 0: 
            case 2: {
                break;
            }
            case 1: {
                BlockPos blockPos = this.player.blockPosition();
                boolean bl = this.level.isOutsideBuildHeight(blockPos.getY());
                if (!bl && !this.levelRenderer.isSectionCompiled(blockPos) && !this.player.isSpectator() && this.player.isAlive()) break;
                this.status = Status.LEVEL_READY;
            }
        }
    }

    public boolean levelReady() {
        return this.status == Status.LEVEL_READY;
    }

    public void loadingPacketsReceived() {
        if (this.status == Status.WAITING_FOR_SERVER) {
            this.status = Status.WAITING_FOR_PLAYER_CHUNK;
        }
    }

    static enum Status {
        WAITING_FOR_SERVER,
        WAITING_FOR_PLAYER_CHUNK,
        LEVEL_READY;

    }
}

