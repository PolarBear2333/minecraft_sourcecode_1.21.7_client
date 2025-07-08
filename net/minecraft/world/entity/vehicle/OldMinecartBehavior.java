/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  javax.annotation.Nullable
 */
package net.minecraft.world.entity.vehicle;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.InterpolationHandler;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartBehavior;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class OldMinecartBehavior
extends MinecartBehavior {
    private static final double MINECART_RIDABLE_THRESHOLD = 0.01;
    private static final double MAX_SPEED_IN_WATER = 0.2;
    private static final double MAX_SPEED_ON_LAND = 0.4;
    private static final double ABSOLUTE_MAX_SPEED = 0.4;
    private final InterpolationHandler interpolation;
    private Vec3 targetDeltaMovement = Vec3.ZERO;

    public OldMinecartBehavior(AbstractMinecart abstractMinecart) {
        super(abstractMinecart);
        this.interpolation = new InterpolationHandler((Entity)abstractMinecart, this::onInterpolation);
    }

    @Override
    public InterpolationHandler getInterpolation() {
        return this.interpolation;
    }

    public void onInterpolation(InterpolationHandler interpolationHandler) {
        this.setDeltaMovement(this.targetDeltaMovement);
    }

    @Override
    public void lerpMotion(double d, double d2, double d3) {
        this.targetDeltaMovement = new Vec3(d, d2, d3);
        this.setDeltaMovement(this.targetDeltaMovement);
    }

    @Override
    public void tick() {
        double d;
        Object object = this.level();
        if (!(object instanceof ServerLevel)) {
            if (this.interpolation.hasActiveInterpolation()) {
                this.interpolation.interpolate();
            } else {
                this.minecart.reapplyPosition();
                this.setXRot(this.getXRot() % 360.0f);
                this.setYRot(this.getYRot() % 360.0f);
            }
            return;
        }
        ServerLevel serverLevel = (ServerLevel)object;
        this.minecart.applyGravity();
        object = this.minecart.getCurrentBlockPosOrRailBelow();
        BlockState blockState = this.level().getBlockState((BlockPos)object);
        boolean bl = BaseRailBlock.isRail(blockState);
        this.minecart.setOnRails(bl);
        if (bl) {
            this.moveAlongTrack(serverLevel);
            if (blockState.is(Blocks.ACTIVATOR_RAIL)) {
                this.minecart.activateMinecart(((Vec3i)object).getX(), ((Vec3i)object).getY(), ((Vec3i)object).getZ(), blockState.getValue(PoweredRailBlock.POWERED));
            }
        } else {
            this.minecart.comeOffTrack(serverLevel);
        }
        this.minecart.applyEffectsFromBlocks();
        this.setXRot(0.0f);
        double d2 = this.minecart.xo - this.getX();
        double d3 = this.minecart.zo - this.getZ();
        if (d2 * d2 + d3 * d3 > 0.001) {
            this.setYRot((float)(Mth.atan2(d3, d2) * 180.0 / Math.PI));
            if (this.minecart.isFlipped()) {
                this.setYRot(this.getYRot() + 180.0f);
            }
        }
        if ((d = (double)Mth.wrapDegrees(this.getYRot() - this.minecart.yRotO)) < -170.0 || d >= 170.0) {
            this.setYRot(this.getYRot() + 180.0f);
            this.minecart.setFlipped(!this.minecart.isFlipped());
        }
        this.setXRot(this.getXRot() % 360.0f);
        this.setYRot(this.getYRot() % 360.0f);
        this.pushAndPickupEntities();
    }

    @Override
    public void moveAlongTrack(ServerLevel serverLevel) {
        double d;
        Vec3 vec3;
        double d2;
        double d3;
        double d4;
        Vec3 vec32;
        Object object;
        BlockPos blockPos = this.minecart.getCurrentBlockPosOrRailBelow();
        BlockState blockState = this.level().getBlockState(blockPos);
        this.minecart.resetFallDistance();
        double d5 = this.minecart.getX();
        double d6 = this.minecart.getY();
        double d7 = this.minecart.getZ();
        Vec3 vec33 = this.getPos(d5, d6, d7);
        d6 = blockPos.getY();
        boolean bl = false;
        boolean bl2 = false;
        if (blockState.is(Blocks.POWERED_RAIL)) {
            bl = blockState.getValue(PoweredRailBlock.POWERED);
            bl2 = !bl;
        }
        double d8 = 0.0078125;
        if (this.minecart.isInWater()) {
            d8 *= 0.2;
        }
        Vec3 vec34 = this.getDeltaMovement();
        RailShape railShape = blockState.getValue(((BaseRailBlock)blockState.getBlock()).getShapeProperty());
        switch (railShape) {
            case ASCENDING_EAST: {
                this.setDeltaMovement(vec34.add(-d8, 0.0, 0.0));
                d6 += 1.0;
                break;
            }
            case ASCENDING_WEST: {
                this.setDeltaMovement(vec34.add(d8, 0.0, 0.0));
                d6 += 1.0;
                break;
            }
            case ASCENDING_NORTH: {
                this.setDeltaMovement(vec34.add(0.0, 0.0, d8));
                d6 += 1.0;
                break;
            }
            case ASCENDING_SOUTH: {
                this.setDeltaMovement(vec34.add(0.0, 0.0, -d8));
                d6 += 1.0;
            }
        }
        vec34 = this.getDeltaMovement();
        Pair<Vec3i, Vec3i> pair = AbstractMinecart.exits(railShape);
        Vec3i vec3i = (Vec3i)pair.getFirst();
        Vec3i vec3i2 = (Vec3i)pair.getSecond();
        double d9 = vec3i2.getX() - vec3i.getX();
        double d10 = vec3i2.getZ() - vec3i.getZ();
        double d11 = Math.sqrt(d9 * d9 + d10 * d10);
        double d12 = vec34.x * d9 + vec34.z * d10;
        if (d12 < 0.0) {
            d9 = -d9;
            d10 = -d10;
        }
        double d13 = Math.min(2.0, vec34.horizontalDistance());
        vec34 = new Vec3(d13 * d9 / d11, vec34.y, d13 * d10 / d11);
        this.setDeltaMovement(vec34);
        Entity entity = this.minecart.getFirstPassenger();
        Entity entity2 = this.minecart.getFirstPassenger();
        if (entity2 instanceof ServerPlayer) {
            object = (ServerPlayer)entity2;
            vec32 = ((ServerPlayer)object).getLastClientMoveIntent();
        } else {
            vec32 = Vec3.ZERO;
        }
        if (entity instanceof Player && vec32.lengthSqr() > 0.0) {
            object = vec32.normalize();
            double d14 = this.getDeltaMovement().horizontalDistanceSqr();
            if (((Vec3)object).lengthSqr() > 0.0 && d14 < 0.01) {
                this.setDeltaMovement(this.getDeltaMovement().add(vec32.x * 0.001, 0.0, vec32.z * 0.001));
                bl2 = false;
            }
        }
        if (bl2) {
            double d15 = this.getDeltaMovement().horizontalDistance();
            if (d15 < 0.03) {
                this.setDeltaMovement(Vec3.ZERO);
            } else {
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.5, 0.0, 0.5));
            }
        }
        double d16 = (double)blockPos.getX() + 0.5 + (double)vec3i.getX() * 0.5;
        double d17 = (double)blockPos.getZ() + 0.5 + (double)vec3i.getZ() * 0.5;
        double d18 = (double)blockPos.getX() + 0.5 + (double)vec3i2.getX() * 0.5;
        double d19 = (double)blockPos.getZ() + 0.5 + (double)vec3i2.getZ() * 0.5;
        d9 = d18 - d16;
        d10 = d19 - d17;
        if (d9 == 0.0) {
            d4 = d7 - (double)blockPos.getZ();
        } else if (d10 == 0.0) {
            d4 = d5 - (double)blockPos.getX();
        } else {
            d3 = d5 - d16;
            d2 = d7 - d17;
            d4 = (d3 * d9 + d2 * d10) * 2.0;
        }
        d5 = d16 + d9 * d4;
        d7 = d17 + d10 * d4;
        this.setPos(d5, d6, d7);
        d3 = this.minecart.isVehicle() ? 0.75 : 1.0;
        d2 = this.minecart.getMaxSpeed(serverLevel);
        vec34 = this.getDeltaMovement();
        this.minecart.move(MoverType.SELF, new Vec3(Mth.clamp(d3 * vec34.x, -d2, d2), 0.0, Mth.clamp(d3 * vec34.z, -d2, d2)));
        if (vec3i.getY() != 0 && Mth.floor(this.minecart.getX()) - blockPos.getX() == vec3i.getX() && Mth.floor(this.minecart.getZ()) - blockPos.getZ() == vec3i.getZ()) {
            this.setPos(this.minecart.getX(), this.minecart.getY() + (double)vec3i.getY(), this.minecart.getZ());
        } else if (vec3i2.getY() != 0 && Mth.floor(this.minecart.getX()) - blockPos.getX() == vec3i2.getX() && Mth.floor(this.minecart.getZ()) - blockPos.getZ() == vec3i2.getZ()) {
            this.setPos(this.minecart.getX(), this.minecart.getY() + (double)vec3i2.getY(), this.minecart.getZ());
        }
        this.setDeltaMovement(this.minecart.applyNaturalSlowdown(this.getDeltaMovement()));
        Vec3 vec35 = this.getPos(this.minecart.getX(), this.minecart.getY(), this.minecart.getZ());
        if (vec35 != null && vec33 != null) {
            double d20 = (vec33.y - vec35.y) * 0.05;
            vec3 = this.getDeltaMovement();
            d = vec3.horizontalDistance();
            if (d > 0.0) {
                this.setDeltaMovement(vec3.multiply((d + d20) / d, 1.0, (d + d20) / d));
            }
            this.setPos(this.minecart.getX(), vec35.y, this.minecart.getZ());
        }
        int n = Mth.floor(this.minecart.getX());
        int n2 = Mth.floor(this.minecart.getZ());
        if (n != blockPos.getX() || n2 != blockPos.getZ()) {
            vec3 = this.getDeltaMovement();
            d = vec3.horizontalDistance();
            this.setDeltaMovement(d * (double)(n - blockPos.getX()), vec3.y, d * (double)(n2 - blockPos.getZ()));
        }
        if (bl) {
            vec3 = this.getDeltaMovement();
            d = vec3.horizontalDistance();
            if (d > 0.01) {
                double d21 = 0.06;
                this.setDeltaMovement(vec3.add(vec3.x / d * 0.06, 0.0, vec3.z / d * 0.06));
            } else {
                Vec3 vec36 = this.getDeltaMovement();
                double d22 = vec36.x;
                double d23 = vec36.z;
                if (railShape == RailShape.EAST_WEST) {
                    if (this.minecart.isRedstoneConductor(blockPos.west())) {
                        d22 = 0.02;
                    } else if (this.minecart.isRedstoneConductor(blockPos.east())) {
                        d22 = -0.02;
                    }
                } else if (railShape == RailShape.NORTH_SOUTH) {
                    if (this.minecart.isRedstoneConductor(blockPos.north())) {
                        d23 = 0.02;
                    } else if (this.minecart.isRedstoneConductor(blockPos.south())) {
                        d23 = -0.02;
                    }
                } else {
                    return;
                }
                this.setDeltaMovement(d22, vec36.y, d23);
            }
        }
    }

    @Nullable
    public Vec3 getPosOffs(double d, double d2, double d3, double d4) {
        BlockState blockState;
        int n = Mth.floor(d);
        int n2 = Mth.floor(d2);
        int n3 = Mth.floor(d3);
        if (this.level().getBlockState(new BlockPos(n, n2 - 1, n3)).is(BlockTags.RAILS)) {
            --n2;
        }
        if (BaseRailBlock.isRail(blockState = this.level().getBlockState(new BlockPos(n, n2, n3)))) {
            RailShape railShape = blockState.getValue(((BaseRailBlock)blockState.getBlock()).getShapeProperty());
            d2 = n2;
            if (railShape.isSlope()) {
                d2 = n2 + 1;
            }
            Pair<Vec3i, Vec3i> pair = AbstractMinecart.exits(railShape);
            Vec3i vec3i = (Vec3i)pair.getFirst();
            Vec3i vec3i2 = (Vec3i)pair.getSecond();
            double d5 = vec3i2.getX() - vec3i.getX();
            double d6 = vec3i2.getZ() - vec3i.getZ();
            double d7 = Math.sqrt(d5 * d5 + d6 * d6);
            if (vec3i.getY() != 0 && Mth.floor(d += (d5 /= d7) * d4) - n == vec3i.getX() && Mth.floor(d3 += (d6 /= d7) * d4) - n3 == vec3i.getZ()) {
                d2 += (double)vec3i.getY();
            } else if (vec3i2.getY() != 0 && Mth.floor(d) - n == vec3i2.getX() && Mth.floor(d3) - n3 == vec3i2.getZ()) {
                d2 += (double)vec3i2.getY();
            }
            return this.getPos(d, d2, d3);
        }
        return null;
    }

    @Nullable
    public Vec3 getPos(double d, double d2, double d3) {
        BlockState blockState;
        int n = Mth.floor(d);
        int n2 = Mth.floor(d2);
        int n3 = Mth.floor(d3);
        if (this.level().getBlockState(new BlockPos(n, n2 - 1, n3)).is(BlockTags.RAILS)) {
            --n2;
        }
        if (BaseRailBlock.isRail(blockState = this.level().getBlockState(new BlockPos(n, n2, n3)))) {
            double d4;
            RailShape railShape = blockState.getValue(((BaseRailBlock)blockState.getBlock()).getShapeProperty());
            Pair<Vec3i, Vec3i> pair = AbstractMinecart.exits(railShape);
            Vec3i vec3i = (Vec3i)pair.getFirst();
            Vec3i vec3i2 = (Vec3i)pair.getSecond();
            double d5 = (double)n + 0.5 + (double)vec3i.getX() * 0.5;
            double d6 = (double)n2 + 0.0625 + (double)vec3i.getY() * 0.5;
            double d7 = (double)n3 + 0.5 + (double)vec3i.getZ() * 0.5;
            double d8 = (double)n + 0.5 + (double)vec3i2.getX() * 0.5;
            double d9 = (double)n2 + 0.0625 + (double)vec3i2.getY() * 0.5;
            double d10 = (double)n3 + 0.5 + (double)vec3i2.getZ() * 0.5;
            double d11 = d8 - d5;
            double d12 = (d9 - d6) * 2.0;
            double d13 = d10 - d7;
            if (d11 == 0.0) {
                d4 = d3 - (double)n3;
            } else if (d13 == 0.0) {
                d4 = d - (double)n;
            } else {
                double d14 = d - d5;
                double d15 = d3 - d7;
                d4 = (d14 * d11 + d15 * d13) * 2.0;
            }
            d = d5 + d11 * d4;
            d2 = d6 + d12 * d4;
            d3 = d7 + d13 * d4;
            if (d12 < 0.0) {
                d2 += 1.0;
            } else if (d12 > 0.0) {
                d2 += 0.5;
            }
            return new Vec3(d, d2, d3);
        }
        return null;
    }

    @Override
    public double stepAlongTrack(BlockPos blockPos, RailShape railShape, double d) {
        return 0.0;
    }

    @Override
    public boolean pushAndPickupEntities() {
        block4: {
            AABB aABB;
            block3: {
                aABB = this.minecart.getBoundingBox().inflate(0.2f, 0.0, 0.2f);
                if (!this.minecart.isRideable() || !(this.getDeltaMovement().horizontalDistanceSqr() >= 0.01)) break block3;
                List<Entity> list = this.level().getEntities(this.minecart, aABB, EntitySelector.pushableBy(this.minecart));
                if (list.isEmpty()) break block4;
                for (Entity entity : list) {
                    if (entity instanceof Player || entity instanceof IronGolem || entity instanceof AbstractMinecart || this.minecart.isVehicle() || entity.isPassenger()) {
                        entity.push(this.minecart);
                        continue;
                    }
                    entity.startRiding(this.minecart);
                }
                break block4;
            }
            for (Entity entity : this.level().getEntities(this.minecart, aABB)) {
                if (this.minecart.hasPassenger(entity) || !entity.isPushable() || !(entity instanceof AbstractMinecart)) continue;
                entity.push(this.minecart);
            }
        }
        return false;
    }

    @Override
    public Direction getMotionDirection() {
        return this.minecart.isFlipped() ? this.minecart.getDirection().getOpposite().getClockWise() : this.minecart.getDirection().getClockWise();
    }

    @Override
    public Vec3 getKnownMovement(Vec3 vec3) {
        if (Double.isNaN(vec3.x) || Double.isNaN(vec3.y) || Double.isNaN(vec3.z)) {
            return Vec3.ZERO;
        }
        return new Vec3(Mth.clamp(vec3.x, -0.4, 0.4), vec3.y, Mth.clamp(vec3.z, -0.4, 0.4));
    }

    @Override
    public double getMaxSpeed(ServerLevel serverLevel) {
        return this.minecart.isInWater() ? 0.2 : 0.4;
    }

    @Override
    public double getSlowdownFactor() {
        return this.minecart.isVehicle() ? 0.997 : 0.96;
    }
}

