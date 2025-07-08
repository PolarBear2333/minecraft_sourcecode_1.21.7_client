/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.phys.shapes;

import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EntityCollisionContext
implements CollisionContext {
    protected static final CollisionContext EMPTY = new EntityCollisionContext(false, false, -1.7976931348623157E308, ItemStack.EMPTY, fluidState -> false, null){

        @Override
        public boolean isAbove(VoxelShape voxelShape, BlockPos blockPos, boolean bl) {
            return bl;
        }
    };
    private final boolean descending;
    private final double entityBottom;
    private final boolean placement;
    private final ItemStack heldItem;
    private final Predicate<FluidState> canStandOnFluid;
    @Nullable
    private final Entity entity;

    protected EntityCollisionContext(boolean bl, boolean bl2, double d, ItemStack itemStack, Predicate<FluidState> predicate, @Nullable Entity entity) {
        this.descending = bl;
        this.placement = bl2;
        this.entityBottom = d;
        this.heldItem = itemStack;
        this.canStandOnFluid = predicate;
        this.entity = entity;
    }

    @Deprecated
    protected EntityCollisionContext(Entity entity, boolean bl, boolean bl2) {
        Predicate<FluidState> predicate;
        ItemStack itemStack;
        LivingEntity livingEntity;
        boolean bl3 = entity.isDescending();
        double d = entity.getY();
        if (entity instanceof LivingEntity) {
            livingEntity = (LivingEntity)entity;
            itemStack = livingEntity.getMainHandItem();
        } else {
            itemStack = ItemStack.EMPTY;
        }
        if (bl) {
            predicate = fluidState -> true;
        } else if (entity instanceof LivingEntity) {
            livingEntity = (LivingEntity)entity;
            predicate = fluidState -> livingEntity.canStandOnFluid((FluidState)fluidState);
        } else {
            predicate = fluidState -> false;
        }
        this(bl3, bl2, d, itemStack, predicate, entity);
    }

    @Override
    public boolean isHoldingItem(Item item) {
        return this.heldItem.is(item);
    }

    @Override
    public boolean canStandOnFluid(FluidState fluidState, FluidState fluidState2) {
        return this.canStandOnFluid.test(fluidState2) && !fluidState.getType().isSame(fluidState2.getType());
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState, CollisionGetter collisionGetter, BlockPos blockPos) {
        return blockState.getCollisionShape(collisionGetter, blockPos, this);
    }

    @Override
    public boolean isDescending() {
        return this.descending;
    }

    @Override
    public boolean isAbove(VoxelShape voxelShape, BlockPos blockPos, boolean bl) {
        return this.entityBottom > (double)blockPos.getY() + voxelShape.max(Direction.Axis.Y) - (double)1.0E-5f;
    }

    @Nullable
    public Entity getEntity() {
        return this.entity;
    }

    @Override
    public boolean isPlacement() {
        return this.placement;
    }
}

