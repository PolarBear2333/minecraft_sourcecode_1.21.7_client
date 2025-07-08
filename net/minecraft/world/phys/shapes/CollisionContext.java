/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.world.phys.shapes;

import java.lang.runtime.SwitchBootstraps;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.MinecartCollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface CollisionContext {
    public static CollisionContext empty() {
        return EntityCollisionContext.EMPTY;
    }

    public static CollisionContext of(Entity entity) {
        Entity entity2 = entity;
        Objects.requireNonNull(entity2);
        Entity entity3 = entity2;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{AbstractMinecart.class}, (Object)entity3, n)) {
            case 0 -> {
                AbstractMinecart var3_3 = (AbstractMinecart)entity3;
                if (AbstractMinecart.useExperimentalMovement(var3_3.level())) {
                    yield new MinecartCollisionContext(var3_3, false);
                }
                yield new EntityCollisionContext(entity, false, false);
            }
            default -> new EntityCollisionContext(entity, false, false);
        };
    }

    public static CollisionContext of(Entity entity, boolean bl) {
        return new EntityCollisionContext(entity, bl, false);
    }

    public static CollisionContext placementContext(@Nullable Player player) {
        Predicate<FluidState> predicate;
        ItemStack itemStack;
        Player player2;
        boolean bl = player != null ? player.isDescending() : false;
        double d = player != null ? player.getY() : -1.7976931348623157E308;
        if (player instanceof LivingEntity) {
            player2 = player;
            itemStack = player2.getMainHandItem();
        } else {
            itemStack = ItemStack.EMPTY;
        }
        if (player instanceof LivingEntity) {
            player2 = player;
            predicate = fluidState -> player2.canStandOnFluid((FluidState)fluidState);
        } else {
            predicate = fluidState -> false;
        }
        return new EntityCollisionContext(bl, true, d, itemStack, predicate, player);
    }

    public static CollisionContext withPosition(@Nullable Entity entity, double d) {
        Predicate<FluidState> predicate;
        ItemStack itemStack;
        LivingEntity livingEntity;
        boolean bl = entity != null ? entity.isDescending() : false;
        double d2 = entity != null ? d : -1.7976931348623157E308;
        if (entity instanceof LivingEntity) {
            livingEntity = (LivingEntity)entity;
            itemStack = livingEntity.getMainHandItem();
        } else {
            itemStack = ItemStack.EMPTY;
        }
        if (entity instanceof LivingEntity) {
            livingEntity = (LivingEntity)entity;
            predicate = fluidState -> livingEntity.canStandOnFluid((FluidState)fluidState);
        } else {
            predicate = fluidState -> false;
        }
        return new EntityCollisionContext(bl, true, d2, itemStack, predicate, entity);
    }

    public boolean isDescending();

    public boolean isAbove(VoxelShape var1, BlockPos var2, boolean var3);

    public boolean isHoldingItem(Item var1);

    public boolean canStandOnFluid(FluidState var1, FluidState var2);

    public VoxelShape getCollisionShape(BlockState var1, CollisionGetter var2, BlockPos var3);

    default public boolean isPlacement() {
        return false;
    }
}

