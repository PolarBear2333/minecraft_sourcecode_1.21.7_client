/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.core.dispenser;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.block.DispenserBlock;

public class ProjectileDispenseBehavior
extends DefaultDispenseItemBehavior {
    private final ProjectileItem projectileItem;
    private final ProjectileItem.DispenseConfig dispenseConfig;

    public ProjectileDispenseBehavior(Item item) {
        if (!(item instanceof ProjectileItem)) {
            throw new IllegalArgumentException(String.valueOf(item) + " not instance of " + ProjectileItem.class.getSimpleName());
        }
        ProjectileItem projectileItem = (ProjectileItem)((Object)item);
        this.projectileItem = projectileItem;
        this.dispenseConfig = projectileItem.createDispenseConfig();
    }

    @Override
    public ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
        ServerLevel serverLevel = blockSource.level();
        Direction direction = blockSource.state().getValue(DispenserBlock.FACING);
        Position position = this.dispenseConfig.positionFunction().getDispensePosition(blockSource, direction);
        Projectile.spawnProjectileUsingShoot(this.projectileItem.asProjectile(serverLevel, position, itemStack, direction), serverLevel, itemStack, direction.getStepX(), direction.getStepY(), direction.getStepZ(), this.dispenseConfig.power(), this.dispenseConfig.uncertainty());
        itemStack.shrink(1);
        return itemStack;
    }

    @Override
    protected void playSound(BlockSource blockSource) {
        blockSource.level().levelEvent(this.dispenseConfig.overrideDispenseEvent().orElse(1002), blockSource.pos(), 0);
    }
}

