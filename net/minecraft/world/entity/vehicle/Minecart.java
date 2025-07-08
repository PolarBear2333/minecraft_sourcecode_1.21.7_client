/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.vehicle;

import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Minecart
extends AbstractMinecart {
    private float rotationOffset;
    private float playerRotationOffset;

    public Minecart(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand interactionHand) {
        if (!player.isSecondaryUseActive() && !this.isVehicle() && (this.level().isClientSide || player.startRiding(this))) {
            this.playerRotationOffset = this.rotationOffset;
            if (!this.level().isClientSide) {
                return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected Item getDropItem() {
        return Items.MINECART;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.MINECART);
    }

    @Override
    public void activateMinecart(int n, int n2, int n3, boolean bl) {
        if (bl) {
            if (this.isVehicle()) {
                this.ejectPassengers();
            }
            if (this.getHurtTime() == 0) {
                this.setHurtDir(-this.getHurtDir());
                this.setHurtTime(10);
                this.setDamage(50.0f);
                this.markHurt();
            }
        }
    }

    @Override
    public boolean isRideable() {
        return true;
    }

    @Override
    public void tick() {
        double d = this.getYRot();
        Vec3 vec3 = this.position();
        super.tick();
        double d2 = ((double)this.getYRot() - d) % 360.0;
        if (this.level().isClientSide && vec3.distanceTo(this.position()) > 0.01) {
            this.rotationOffset += (float)d2;
            this.rotationOffset %= 360.0f;
        }
    }

    @Override
    protected void positionRider(Entity entity, Entity.MoveFunction moveFunction) {
        Player player;
        super.positionRider(entity, moveFunction);
        if (this.level().isClientSide && entity instanceof Player && (player = (Player)entity).shouldRotateWithMinecart() && Minecart.useExperimentalMovement(this.level())) {
            float f = (float)Mth.rotLerp(0.5, (double)this.playerRotationOffset, (double)this.rotationOffset);
            player.setYRot(player.getYRot() - (f - this.playerRotationOffset));
            this.playerRotationOffset = f;
        }
    }
}

