/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SculkSensorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class SculkSensorBlockEntity
extends BlockEntity
implements GameEventListener.Provider<VibrationSystem.Listener>,
VibrationSystem {
    private static final int DEFAULT_LAST_VIBRATION_FREQUENCY = 0;
    private VibrationSystem.Data vibrationData;
    private final VibrationSystem.Listener vibrationListener;
    private final VibrationSystem.User vibrationUser = this.createVibrationUser();
    private int lastVibrationFrequency = 0;

    protected SculkSensorBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
        this.vibrationData = new VibrationSystem.Data();
        this.vibrationListener = new VibrationSystem.Listener(this);
    }

    public SculkSensorBlockEntity(BlockPos blockPos, BlockState blockState) {
        this(BlockEntityType.SCULK_SENSOR, blockPos, blockState);
    }

    public VibrationSystem.User createVibrationUser() {
        return new VibrationUser(this.getBlockPos());
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        this.lastVibrationFrequency = valueInput.getIntOr("last_vibration_frequency", 0);
        this.vibrationData = valueInput.read("listener", VibrationSystem.Data.CODEC).orElseGet(VibrationSystem.Data::new);
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        valueOutput.putInt("last_vibration_frequency", this.lastVibrationFrequency);
        valueOutput.store("listener", VibrationSystem.Data.CODEC, this.vibrationData);
    }

    @Override
    public VibrationSystem.Data getVibrationData() {
        return this.vibrationData;
    }

    @Override
    public VibrationSystem.User getVibrationUser() {
        return this.vibrationUser;
    }

    public int getLastVibrationFrequency() {
        return this.lastVibrationFrequency;
    }

    public void setLastVibrationFrequency(int n) {
        this.lastVibrationFrequency = n;
    }

    @Override
    public VibrationSystem.Listener getListener() {
        return this.vibrationListener;
    }

    @Override
    public /* synthetic */ GameEventListener getListener() {
        return this.getListener();
    }

    protected class VibrationUser
    implements VibrationSystem.User {
        public static final int LISTENER_RANGE = 8;
        protected final BlockPos blockPos;
        private final PositionSource positionSource;

        public VibrationUser(BlockPos blockPos) {
            this.blockPos = blockPos;
            this.positionSource = new BlockPositionSource(blockPos);
        }

        @Override
        public int getListenerRadius() {
            return 8;
        }

        @Override
        public PositionSource getPositionSource() {
            return this.positionSource;
        }

        @Override
        public boolean canTriggerAvoidVibration() {
            return true;
        }

        @Override
        public boolean canReceiveVibration(ServerLevel serverLevel, BlockPos blockPos, Holder<GameEvent> holder, @Nullable GameEvent.Context context) {
            if (blockPos.equals(this.blockPos) && (holder.is(GameEvent.BLOCK_DESTROY) || holder.is(GameEvent.BLOCK_PLACE))) {
                return false;
            }
            if (VibrationSystem.getGameEventFrequency(holder) == 0) {
                return false;
            }
            return SculkSensorBlock.canActivate(SculkSensorBlockEntity.this.getBlockState());
        }

        @Override
        public void onReceiveVibration(ServerLevel serverLevel, BlockPos blockPos, Holder<GameEvent> holder, @Nullable Entity entity, @Nullable Entity entity2, float f) {
            BlockState blockState = SculkSensorBlockEntity.this.getBlockState();
            if (SculkSensorBlock.canActivate(blockState)) {
                int n = VibrationSystem.getGameEventFrequency(holder);
                SculkSensorBlockEntity.this.setLastVibrationFrequency(n);
                int n2 = VibrationSystem.getRedstoneStrengthForDistance(f, this.getListenerRadius());
                Block block = blockState.getBlock();
                if (block instanceof SculkSensorBlock) {
                    SculkSensorBlock sculkSensorBlock = (SculkSensorBlock)block;
                    sculkSensorBlock.activate(entity, serverLevel, this.blockPos, blockState, n2, n);
                }
            }
        }

        @Override
        public void onDataChanged() {
            SculkSensorBlockEntity.this.setChanged();
        }

        @Override
        public boolean requiresAdjacentChunksToBeTicking() {
            return true;
        }
    }
}

