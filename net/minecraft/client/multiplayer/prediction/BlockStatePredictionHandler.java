/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectIterator
 */
package net.minecraft.client.multiplayer.prediction;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BlockStatePredictionHandler
implements AutoCloseable {
    private final Long2ObjectOpenHashMap<ServerVerifiedState> serverVerifiedStates = new Long2ObjectOpenHashMap();
    private int currentSequenceNr;
    private boolean isPredicting;

    public void retainKnownServerState(BlockPos blockPos, BlockState blockState, LocalPlayer localPlayer) {
        this.serverVerifiedStates.compute(blockPos.asLong(), (l, serverVerifiedState) -> {
            if (serverVerifiedState != null) {
                return serverVerifiedState.setSequence(this.currentSequenceNr);
            }
            return new ServerVerifiedState(this.currentSequenceNr, blockState, localPlayer.position());
        });
    }

    public boolean updateKnownServerState(BlockPos blockPos, BlockState blockState) {
        ServerVerifiedState serverVerifiedState = (ServerVerifiedState)this.serverVerifiedStates.get(blockPos.asLong());
        if (serverVerifiedState == null) {
            return false;
        }
        serverVerifiedState.setBlockState(blockState);
        return true;
    }

    public void endPredictionsUpTo(int n, ClientLevel clientLevel) {
        ObjectIterator objectIterator = this.serverVerifiedStates.long2ObjectEntrySet().iterator();
        while (objectIterator.hasNext()) {
            Long2ObjectMap.Entry entry = (Long2ObjectMap.Entry)objectIterator.next();
            ServerVerifiedState serverVerifiedState = (ServerVerifiedState)entry.getValue();
            if (serverVerifiedState.sequence > n) continue;
            BlockPos blockPos = BlockPos.of(entry.getLongKey());
            objectIterator.remove();
            clientLevel.syncBlockState(blockPos, serverVerifiedState.blockState, serverVerifiedState.playerPos);
        }
    }

    public BlockStatePredictionHandler startPredicting() {
        ++this.currentSequenceNr;
        this.isPredicting = true;
        return this;
    }

    @Override
    public void close() {
        this.isPredicting = false;
    }

    public int currentSequence() {
        return this.currentSequenceNr;
    }

    public boolean isPredicting() {
        return this.isPredicting;
    }

    static class ServerVerifiedState {
        final Vec3 playerPos;
        int sequence;
        BlockState blockState;

        ServerVerifiedState(int n, BlockState blockState, Vec3 vec3) {
            this.sequence = n;
            this.blockState = blockState;
            this.playerPos = vec3;
        }

        ServerVerifiedState setSequence(int n) {
            this.sequence = n;
            return this;
        }

        void setBlockState(BlockState blockState) {
            this.blockState = blockState;
        }
    }
}

