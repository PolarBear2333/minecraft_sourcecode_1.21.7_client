/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.vehicle;

import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class MinecartFurnace
extends AbstractMinecart {
    private static final EntityDataAccessor<Boolean> DATA_ID_FUEL = SynchedEntityData.defineId(MinecartFurnace.class, EntityDataSerializers.BOOLEAN);
    private static final int FUEL_TICKS_PER_ITEM = 3600;
    private static final int MAX_FUEL_TICKS = 32000;
    private static final short DEFAULT_FUEL = 0;
    private static final Vec3 DEFAULT_PUSH = Vec3.ZERO;
    private int fuel = 0;
    public Vec3 push = DEFAULT_PUSH;

    public MinecartFurnace(EntityType<? extends MinecartFurnace> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean isFurnace() {
        return true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ID_FUEL, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide()) {
            if (this.fuel > 0) {
                --this.fuel;
            }
            if (this.fuel <= 0) {
                this.push = Vec3.ZERO;
            }
            this.setHasFuel(this.fuel > 0);
        }
        if (this.hasFuel() && this.random.nextInt(4) == 0) {
            this.level().addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 0.8, this.getZ(), 0.0, 0.0, 0.0);
        }
    }

    @Override
    protected double getMaxSpeed(ServerLevel serverLevel) {
        return this.isInWater() ? super.getMaxSpeed(serverLevel) * 0.75 : super.getMaxSpeed(serverLevel) * 0.5;
    }

    @Override
    protected Item getDropItem() {
        return Items.FURNACE_MINECART;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(Items.FURNACE_MINECART);
    }

    @Override
    protected Vec3 applyNaturalSlowdown(Vec3 vec3) {
        Vec3 vec32;
        if (this.push.lengthSqr() > 1.0E-7) {
            this.push = this.calculateNewPushAlong(vec3);
            vec32 = vec3.multiply(0.8, 0.0, 0.8).add(this.push);
            if (this.isInWater()) {
                vec32 = vec32.scale(0.1);
            }
        } else {
            vec32 = vec3.multiply(0.98, 0.0, 0.98);
        }
        return super.applyNaturalSlowdown(vec32);
    }

    private Vec3 calculateNewPushAlong(Vec3 vec3) {
        double d = 1.0E-4;
        double d2 = 0.001;
        if (this.push.horizontalDistanceSqr() > 1.0E-4 && vec3.horizontalDistanceSqr() > 0.001) {
            return this.push.projectedOn(vec3).normalize().scale(this.push.length());
        }
        return this.push;
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand interactionHand) {
        ItemStack itemStack = player.getItemInHand(interactionHand);
        if (itemStack.is(ItemTags.FURNACE_MINECART_FUEL) && this.fuel + 3600 <= 32000) {
            itemStack.consume(1, player);
            this.fuel += 3600;
        }
        if (this.fuel > 0) {
            this.push = this.position().subtract(player.position()).horizontal();
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput valueOutput) {
        super.addAdditionalSaveData(valueOutput);
        valueOutput.putDouble("PushX", this.push.x);
        valueOutput.putDouble("PushZ", this.push.z);
        valueOutput.putShort("Fuel", (short)this.fuel);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput valueInput) {
        super.readAdditionalSaveData(valueInput);
        double d = valueInput.getDoubleOr("PushX", MinecartFurnace.DEFAULT_PUSH.x);
        double d2 = valueInput.getDoubleOr("PushZ", MinecartFurnace.DEFAULT_PUSH.z);
        this.push = new Vec3(d, 0.0, d2);
        this.fuel = valueInput.getShortOr("Fuel", (short)0);
    }

    protected boolean hasFuel() {
        return this.entityData.get(DATA_ID_FUEL);
    }

    protected void setHasFuel(boolean bl) {
        this.entityData.set(DATA_ID_FUEL, bl);
    }

    @Override
    public BlockState getDefaultDisplayBlockState() {
        return (BlockState)((BlockState)Blocks.FURNACE.defaultBlockState().setValue(FurnaceBlock.FACING, Direction.NORTH)).setValue(FurnaceBlock.LIT, this.hasFuel());
    }
}

