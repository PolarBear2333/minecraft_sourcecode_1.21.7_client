/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.core.dispenser;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

public class MinecartDispenseItemBehavior
extends DefaultDispenseItemBehavior {
    private final DefaultDispenseItemBehavior defaultDispenseItemBehavior = new DefaultDispenseItemBehavior();
    private final EntityType<? extends AbstractMinecart> entityType;

    public MinecartDispenseItemBehavior(EntityType<? extends AbstractMinecart> entityType) {
        this.entityType = entityType;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
        Object object;
        double d;
        Direction direction = blockSource.state().getValue(DispenserBlock.FACING);
        ServerLevel serverLevel = blockSource.level();
        Vec3 vec3 = blockSource.center();
        double d2 = vec3.x() + (double)direction.getStepX() * 1.125;
        double d3 = Math.floor(vec3.y()) + (double)direction.getStepY();
        double d4 = vec3.z() + (double)direction.getStepZ() * 1.125;
        BlockPos blockPos = blockSource.pos().relative(direction);
        BlockState blockState = serverLevel.getBlockState(blockPos);
        if (blockState.is(BlockTags.RAILS)) {
            d = MinecartDispenseItemBehavior.getRailShape(blockState).isSlope() ? 0.6 : 0.1;
        } else {
            if (!blockState.isAir()) return this.defaultDispenseItemBehavior.dispense(blockSource, itemStack);
            object = serverLevel.getBlockState(blockPos.below());
            if (!((BlockBehaviour.BlockStateBase)object).is(BlockTags.RAILS)) return this.defaultDispenseItemBehavior.dispense(blockSource, itemStack);
            d = direction == Direction.DOWN || !MinecartDispenseItemBehavior.getRailShape((BlockState)object).isSlope() ? -0.9 : -0.4;
        }
        object = new Vec3(d2, d3 + d, d4);
        AbstractMinecart abstractMinecart = AbstractMinecart.createMinecart(serverLevel, ((Vec3)object).x, ((Vec3)object).y, ((Vec3)object).z, this.entityType, EntitySpawnReason.DISPENSER, itemStack, null);
        if (abstractMinecart == null) return itemStack;
        serverLevel.addFreshEntity(abstractMinecart);
        itemStack.shrink(1);
        return itemStack;
    }

    private static RailShape getRailShape(BlockState blockState) {
        RailShape railShape;
        Block block = blockState.getBlock();
        if (block instanceof BaseRailBlock) {
            BaseRailBlock baseRailBlock = (BaseRailBlock)block;
            railShape = blockState.getValue(baseRailBlock.getShapeProperty());
        } else {
            railShape = RailShape.NORTH_SOUTH;
        }
        return railShape;
    }

    @Override
    protected void playSound(BlockSource blockSource) {
        blockSource.level().levelEvent(1000, blockSource.pos(), 0);
    }
}

