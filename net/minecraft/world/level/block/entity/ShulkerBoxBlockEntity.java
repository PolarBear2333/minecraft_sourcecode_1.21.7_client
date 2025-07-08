/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block.entity;

import java.util.List;
import java.util.stream.IntStream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class ShulkerBoxBlockEntity
extends RandomizableContainerBlockEntity
implements WorldlyContainer {
    public static final int COLUMNS = 9;
    public static final int ROWS = 3;
    public static final int CONTAINER_SIZE = 27;
    public static final int EVENT_SET_OPEN_COUNT = 1;
    public static final int OPENING_TICK_LENGTH = 10;
    public static final float MAX_LID_HEIGHT = 0.5f;
    public static final float MAX_LID_ROTATION = 270.0f;
    private static final int[] SLOTS = IntStream.range(0, 27).toArray();
    private NonNullList<ItemStack> itemStacks = NonNullList.withSize(27, ItemStack.EMPTY);
    private int openCount;
    private AnimationStatus animationStatus = AnimationStatus.CLOSED;
    private float progress;
    private float progressOld;
    @Nullable
    private final DyeColor color;

    public ShulkerBoxBlockEntity(@Nullable DyeColor dyeColor, BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.SHULKER_BOX, blockPos, blockState);
        this.color = dyeColor;
    }

    public ShulkerBoxBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(BlockEntityType.SHULKER_BOX, blockPos, blockState);
        DyeColor dyeColor;
        Block block = blockState.getBlock();
        if (block instanceof ShulkerBoxBlock) {
            ShulkerBoxBlock shulkerBoxBlock = (ShulkerBoxBlock)block;
            dyeColor = shulkerBoxBlock.getColor();
        } else {
            dyeColor = null;
        }
        this.color = dyeColor;
    }

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, ShulkerBoxBlockEntity shulkerBoxBlockEntity) {
        shulkerBoxBlockEntity.updateAnimation(level, blockPos, blockState);
    }

    private void updateAnimation(Level level, BlockPos blockPos, BlockState blockState) {
        this.progressOld = this.progress;
        switch (this.animationStatus.ordinal()) {
            case 0: {
                this.progress = 0.0f;
                break;
            }
            case 1: {
                this.progress += 0.1f;
                if (this.progressOld == 0.0f) {
                    ShulkerBoxBlockEntity.doNeighborUpdates(level, blockPos, blockState);
                }
                if (this.progress >= 1.0f) {
                    this.animationStatus = AnimationStatus.OPENED;
                    this.progress = 1.0f;
                    ShulkerBoxBlockEntity.doNeighborUpdates(level, blockPos, blockState);
                }
                this.moveCollidedEntities(level, blockPos, blockState);
                break;
            }
            case 3: {
                this.progress -= 0.1f;
                if (this.progressOld == 1.0f) {
                    ShulkerBoxBlockEntity.doNeighborUpdates(level, blockPos, blockState);
                }
                if (!(this.progress <= 0.0f)) break;
                this.animationStatus = AnimationStatus.CLOSED;
                this.progress = 0.0f;
                ShulkerBoxBlockEntity.doNeighborUpdates(level, blockPos, blockState);
                break;
            }
            case 2: {
                this.progress = 1.0f;
            }
        }
    }

    public AnimationStatus getAnimationStatus() {
        return this.animationStatus;
    }

    public AABB getBoundingBox(BlockState blockState) {
        Vec3 vec3 = new Vec3(0.5, 0.0, 0.5);
        return Shulker.getProgressAabb(1.0f, blockState.getValue(ShulkerBoxBlock.FACING), 0.5f * this.getProgress(1.0f), vec3);
    }

    private void moveCollidedEntities(Level level, BlockPos blockPos, BlockState blockState) {
        if (!(blockState.getBlock() instanceof ShulkerBoxBlock)) {
            return;
        }
        Direction direction = blockState.getValue(ShulkerBoxBlock.FACING);
        AABB aABB = Shulker.getProgressDeltaAabb(1.0f, direction, this.progressOld, this.progress, blockPos.getBottomCenter());
        List<Entity> list = level.getEntities(null, aABB);
        if (list.isEmpty()) {
            return;
        }
        for (Entity entity : list) {
            if (entity.getPistonPushReaction() == PushReaction.IGNORE) continue;
            entity.move(MoverType.SHULKER_BOX, new Vec3((aABB.getXsize() + 0.01) * (double)direction.getStepX(), (aABB.getYsize() + 0.01) * (double)direction.getStepY(), (aABB.getZsize() + 0.01) * (double)direction.getStepZ()));
        }
    }

    @Override
    public int getContainerSize() {
        return this.itemStacks.size();
    }

    @Override
    public boolean triggerEvent(int n, int n2) {
        if (n == 1) {
            this.openCount = n2;
            if (n2 == 0) {
                this.animationStatus = AnimationStatus.CLOSING;
            }
            if (n2 == 1) {
                this.animationStatus = AnimationStatus.OPENING;
            }
            return true;
        }
        return super.triggerEvent(n, n2);
    }

    private static void doNeighborUpdates(Level level, BlockPos blockPos, BlockState blockState) {
        blockState.updateNeighbourShapes(level, blockPos, 3);
        level.updateNeighborsAt(blockPos, blockState.getBlock());
    }

    @Override
    public void preRemoveSideEffects(BlockPos blockPos, BlockState blockState) {
    }

    @Override
    public void startOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            if (this.openCount < 0) {
                this.openCount = 0;
            }
            ++this.openCount;
            this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, this.openCount);
            if (this.openCount == 1) {
                this.level.gameEvent((Entity)player, GameEvent.CONTAINER_OPEN, this.worldPosition);
                this.level.playSound(null, this.worldPosition, SoundEvents.SHULKER_BOX_OPEN, SoundSource.BLOCKS, 0.5f, this.level.random.nextFloat() * 0.1f + 0.9f);
            }
        }
    }

    @Override
    public void stopOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            --this.openCount;
            this.level.blockEvent(this.worldPosition, this.getBlockState().getBlock(), 1, this.openCount);
            if (this.openCount <= 0) {
                this.level.gameEvent((Entity)player, GameEvent.CONTAINER_CLOSE, this.worldPosition);
                this.level.playSound(null, this.worldPosition, SoundEvents.SHULKER_BOX_CLOSE, SoundSource.BLOCKS, 0.5f, this.level.random.nextFloat() * 0.1f + 0.9f);
            }
        }
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.shulkerBox");
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        this.loadFromTag(valueInput);
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        if (!this.trySaveLootTable(valueOutput)) {
            ContainerHelper.saveAllItems(valueOutput, this.itemStacks, false);
        }
    }

    public void loadFromTag(ValueInput valueInput) {
        this.itemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(valueInput)) {
            ContainerHelper.loadAllItems(valueInput, this.itemStacks);
        }
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.itemStacks;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> nonNullList) {
        this.itemStacks = nonNullList;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int n, ItemStack itemStack, @Nullable Direction direction) {
        return !(Block.byItem(itemStack.getItem()) instanceof ShulkerBoxBlock);
    }

    @Override
    public boolean canTakeItemThroughFace(int n, ItemStack itemStack, Direction direction) {
        return true;
    }

    public float getProgress(float f) {
        return Mth.lerp(f, this.progressOld, this.progress);
    }

    @Nullable
    public DyeColor getColor() {
        return this.color;
    }

    @Override
    protected AbstractContainerMenu createMenu(int n, Inventory inventory) {
        return new ShulkerBoxMenu(n, inventory, this);
    }

    public boolean isClosed() {
        return this.animationStatus == AnimationStatus.CLOSED;
    }

    public static enum AnimationStatus {
        CLOSED,
        OPENING,
        OPENED,
        CLOSING;

    }
}

