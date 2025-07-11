/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.serialization.DynamicLike
 */
package net.minecraft.world.level.border;

import com.google.common.collect.Lists;
import com.mojang.serialization.DynamicLike;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.BorderStatus;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WorldBorder {
    public static final double MAX_SIZE = 5.9999968E7;
    public static final double MAX_CENTER_COORDINATE = 2.9999984E7;
    private final List<BorderChangeListener> listeners = Lists.newArrayList();
    private double damagePerBlock = 0.2;
    private double damageSafeZone = 5.0;
    private int warningTime = 15;
    private int warningBlocks = 5;
    private double centerX;
    private double centerZ;
    int absoluteMaxSize = 29999984;
    private BorderExtent extent = new StaticBorderExtent(5.9999968E7);
    public static final Settings DEFAULT_SETTINGS = new Settings(0.0, 0.0, 0.2, 5.0, 5, 15, 5.9999968E7, 0L, 0.0);

    public boolean isWithinBounds(BlockPos blockPos) {
        return this.isWithinBounds(blockPos.getX(), blockPos.getZ());
    }

    public boolean isWithinBounds(Vec3 vec3) {
        return this.isWithinBounds(vec3.x, vec3.z);
    }

    public boolean isWithinBounds(ChunkPos chunkPos) {
        return this.isWithinBounds(chunkPos.getMinBlockX(), chunkPos.getMinBlockZ()) && this.isWithinBounds(chunkPos.getMaxBlockX(), chunkPos.getMaxBlockZ());
    }

    public boolean isWithinBounds(AABB aABB) {
        return this.isWithinBounds(aABB.minX, aABB.minZ, aABB.maxX - (double)1.0E-5f, aABB.maxZ - (double)1.0E-5f);
    }

    private boolean isWithinBounds(double d, double d2, double d3, double d4) {
        return this.isWithinBounds(d, d2) && this.isWithinBounds(d3, d4);
    }

    public boolean isWithinBounds(double d, double d2) {
        return this.isWithinBounds(d, d2, 0.0);
    }

    public boolean isWithinBounds(double d, double d2, double d3) {
        return d >= this.getMinX() - d3 && d < this.getMaxX() + d3 && d2 >= this.getMinZ() - d3 && d2 < this.getMaxZ() + d3;
    }

    public BlockPos clampToBounds(BlockPos blockPos) {
        return this.clampToBounds(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public BlockPos clampToBounds(Vec3 vec3) {
        return this.clampToBounds(vec3.x(), vec3.y(), vec3.z());
    }

    public BlockPos clampToBounds(double d, double d2, double d3) {
        return BlockPos.containing(this.clampVec3ToBound(d, d2, d3));
    }

    public Vec3 clampVec3ToBound(Vec3 vec3) {
        return this.clampVec3ToBound(vec3.x, vec3.y, vec3.z);
    }

    public Vec3 clampVec3ToBound(double d, double d2, double d3) {
        return new Vec3(Mth.clamp(d, this.getMinX(), this.getMaxX() - (double)1.0E-5f), d2, Mth.clamp(d3, this.getMinZ(), this.getMaxZ() - (double)1.0E-5f));
    }

    public double getDistanceToBorder(Entity entity) {
        return this.getDistanceToBorder(entity.getX(), entity.getZ());
    }

    public VoxelShape getCollisionShape() {
        return this.extent.getCollisionShape();
    }

    public double getDistanceToBorder(double d, double d2) {
        double d3 = d2 - this.getMinZ();
        double d4 = this.getMaxZ() - d2;
        double d5 = d - this.getMinX();
        double d6 = this.getMaxX() - d;
        double d7 = Math.min(d5, d6);
        d7 = Math.min(d7, d3);
        return Math.min(d7, d4);
    }

    public List<DistancePerDirection> closestBorder(double d, double d2) {
        DistancePerDirection[] distancePerDirectionArray = new DistancePerDirection[]{new DistancePerDirection(Direction.NORTH, d2 - this.getMinZ()), new DistancePerDirection(Direction.SOUTH, this.getMaxZ() - d2), new DistancePerDirection(Direction.WEST, d - this.getMinX()), new DistancePerDirection(Direction.EAST, this.getMaxX() - d)};
        return Arrays.stream(distancePerDirectionArray).sorted(Comparator.comparingDouble(distancePerDirection -> distancePerDirection.distance)).toList();
    }

    public boolean isInsideCloseToBorder(Entity entity, AABB aABB) {
        double d = Math.max(Mth.absMax(aABB.getXsize(), aABB.getZsize()), 1.0);
        return this.getDistanceToBorder(entity) < d * 2.0 && this.isWithinBounds(entity.getX(), entity.getZ(), d);
    }

    public BorderStatus getStatus() {
        return this.extent.getStatus();
    }

    public double getMinX() {
        return this.extent.getMinX();
    }

    public double getMinZ() {
        return this.extent.getMinZ();
    }

    public double getMaxX() {
        return this.extent.getMaxX();
    }

    public double getMaxZ() {
        return this.extent.getMaxZ();
    }

    public double getCenterX() {
        return this.centerX;
    }

    public double getCenterZ() {
        return this.centerZ;
    }

    public void setCenter(double d, double d2) {
        this.centerX = d;
        this.centerZ = d2;
        this.extent.onCenterChange();
        for (BorderChangeListener borderChangeListener : this.getListeners()) {
            borderChangeListener.onBorderCenterSet(this, d, d2);
        }
    }

    public double getSize() {
        return this.extent.getSize();
    }

    public long getLerpRemainingTime() {
        return this.extent.getLerpRemainingTime();
    }

    public double getLerpTarget() {
        return this.extent.getLerpTarget();
    }

    public void setSize(double d) {
        this.extent = new StaticBorderExtent(d);
        for (BorderChangeListener borderChangeListener : this.getListeners()) {
            borderChangeListener.onBorderSizeSet(this, d);
        }
    }

    public void lerpSizeBetween(double d, double d2, long l) {
        this.extent = d == d2 ? new StaticBorderExtent(d2) : new MovingBorderExtent(d, d2, l);
        for (BorderChangeListener borderChangeListener : this.getListeners()) {
            borderChangeListener.onBorderSizeLerping(this, d, d2, l);
        }
    }

    protected List<BorderChangeListener> getListeners() {
        return Lists.newArrayList(this.listeners);
    }

    public void addListener(BorderChangeListener borderChangeListener) {
        this.listeners.add(borderChangeListener);
    }

    public void removeListener(BorderChangeListener borderChangeListener) {
        this.listeners.remove(borderChangeListener);
    }

    public void setAbsoluteMaxSize(int n) {
        this.absoluteMaxSize = n;
        this.extent.onAbsoluteMaxSizeChange();
    }

    public int getAbsoluteMaxSize() {
        return this.absoluteMaxSize;
    }

    public double getDamageSafeZone() {
        return this.damageSafeZone;
    }

    public void setDamageSafeZone(double d) {
        this.damageSafeZone = d;
        for (BorderChangeListener borderChangeListener : this.getListeners()) {
            borderChangeListener.onBorderSetDamageSafeZOne(this, d);
        }
    }

    public double getDamagePerBlock() {
        return this.damagePerBlock;
    }

    public void setDamagePerBlock(double d) {
        this.damagePerBlock = d;
        for (BorderChangeListener borderChangeListener : this.getListeners()) {
            borderChangeListener.onBorderSetDamagePerBlock(this, d);
        }
    }

    public double getLerpSpeed() {
        return this.extent.getLerpSpeed();
    }

    public int getWarningTime() {
        return this.warningTime;
    }

    public void setWarningTime(int n) {
        this.warningTime = n;
        for (BorderChangeListener borderChangeListener : this.getListeners()) {
            borderChangeListener.onBorderSetWarningTime(this, n);
        }
    }

    public int getWarningBlocks() {
        return this.warningBlocks;
    }

    public void setWarningBlocks(int n) {
        this.warningBlocks = n;
        for (BorderChangeListener borderChangeListener : this.getListeners()) {
            borderChangeListener.onBorderSetWarningBlocks(this, n);
        }
    }

    public void tick() {
        this.extent = this.extent.update();
    }

    public Settings createSettings() {
        return new Settings(this);
    }

    public void applySettings(Settings settings) {
        this.setCenter(settings.getCenterX(), settings.getCenterZ());
        this.setDamagePerBlock(settings.getDamagePerBlock());
        this.setDamageSafeZone(settings.getSafeZone());
        this.setWarningBlocks(settings.getWarningBlocks());
        this.setWarningTime(settings.getWarningTime());
        if (settings.getSizeLerpTime() > 0L) {
            this.lerpSizeBetween(settings.getSize(), settings.getSizeLerpTarget(), settings.getSizeLerpTime());
        } else {
            this.setSize(settings.getSize());
        }
    }

    class StaticBorderExtent
    implements BorderExtent {
        private final double size;
        private double minX;
        private double minZ;
        private double maxX;
        private double maxZ;
        private VoxelShape shape;

        public StaticBorderExtent(double d) {
            this.size = d;
            this.updateBox();
        }

        @Override
        public double getMinX() {
            return this.minX;
        }

        @Override
        public double getMaxX() {
            return this.maxX;
        }

        @Override
        public double getMinZ() {
            return this.minZ;
        }

        @Override
        public double getMaxZ() {
            return this.maxZ;
        }

        @Override
        public double getSize() {
            return this.size;
        }

        @Override
        public BorderStatus getStatus() {
            return BorderStatus.STATIONARY;
        }

        @Override
        public double getLerpSpeed() {
            return 0.0;
        }

        @Override
        public long getLerpRemainingTime() {
            return 0L;
        }

        @Override
        public double getLerpTarget() {
            return this.size;
        }

        private void updateBox() {
            this.minX = Mth.clamp(WorldBorder.this.getCenterX() - this.size / 2.0, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize);
            this.minZ = Mth.clamp(WorldBorder.this.getCenterZ() - this.size / 2.0, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize);
            this.maxX = Mth.clamp(WorldBorder.this.getCenterX() + this.size / 2.0, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize);
            this.maxZ = Mth.clamp(WorldBorder.this.getCenterZ() + this.size / 2.0, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize);
            this.shape = Shapes.join(Shapes.INFINITY, Shapes.box(Math.floor(this.getMinX()), Double.NEGATIVE_INFINITY, Math.floor(this.getMinZ()), Math.ceil(this.getMaxX()), Double.POSITIVE_INFINITY, Math.ceil(this.getMaxZ())), BooleanOp.ONLY_FIRST);
        }

        @Override
        public void onAbsoluteMaxSizeChange() {
            this.updateBox();
        }

        @Override
        public void onCenterChange() {
            this.updateBox();
        }

        @Override
        public BorderExtent update() {
            return this;
        }

        @Override
        public VoxelShape getCollisionShape() {
            return this.shape;
        }
    }

    static interface BorderExtent {
        public double getMinX();

        public double getMaxX();

        public double getMinZ();

        public double getMaxZ();

        public double getSize();

        public double getLerpSpeed();

        public long getLerpRemainingTime();

        public double getLerpTarget();

        public BorderStatus getStatus();

        public void onAbsoluteMaxSizeChange();

        public void onCenterChange();

        public BorderExtent update();

        public VoxelShape getCollisionShape();
    }

    public static final class DistancePerDirection
    extends Record {
        private final Direction direction;
        final double distance;

        public DistancePerDirection(Direction direction, double d) {
            this.direction = direction;
            this.distance = d;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{DistancePerDirection.class, "direction;distance", "direction", "distance"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{DistancePerDirection.class, "direction;distance", "direction", "distance"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{DistancePerDirection.class, "direction;distance", "direction", "distance"}, this, object);
        }

        public Direction direction() {
            return this.direction;
        }

        public double distance() {
            return this.distance;
        }
    }

    class MovingBorderExtent
    implements BorderExtent {
        private final double from;
        private final double to;
        private final long lerpEnd;
        private final long lerpBegin;
        private final double lerpDuration;

        MovingBorderExtent(double d, double d2, long l) {
            this.from = d;
            this.to = d2;
            this.lerpDuration = l;
            this.lerpBegin = Util.getMillis();
            this.lerpEnd = this.lerpBegin + l;
        }

        @Override
        public double getMinX() {
            return Mth.clamp(WorldBorder.this.getCenterX() - this.getSize() / 2.0, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize);
        }

        @Override
        public double getMinZ() {
            return Mth.clamp(WorldBorder.this.getCenterZ() - this.getSize() / 2.0, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize);
        }

        @Override
        public double getMaxX() {
            return Mth.clamp(WorldBorder.this.getCenterX() + this.getSize() / 2.0, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize);
        }

        @Override
        public double getMaxZ() {
            return Mth.clamp(WorldBorder.this.getCenterZ() + this.getSize() / 2.0, (double)(-WorldBorder.this.absoluteMaxSize), (double)WorldBorder.this.absoluteMaxSize);
        }

        @Override
        public double getSize() {
            double d = (double)(Util.getMillis() - this.lerpBegin) / this.lerpDuration;
            return d < 1.0 ? Mth.lerp(d, this.from, this.to) : this.to;
        }

        @Override
        public double getLerpSpeed() {
            return Math.abs(this.from - this.to) / (double)(this.lerpEnd - this.lerpBegin);
        }

        @Override
        public long getLerpRemainingTime() {
            return this.lerpEnd - Util.getMillis();
        }

        @Override
        public double getLerpTarget() {
            return this.to;
        }

        @Override
        public BorderStatus getStatus() {
            return this.to < this.from ? BorderStatus.SHRINKING : BorderStatus.GROWING;
        }

        @Override
        public void onCenterChange() {
        }

        @Override
        public void onAbsoluteMaxSizeChange() {
        }

        @Override
        public BorderExtent update() {
            if (this.getLerpRemainingTime() <= 0L) {
                return new StaticBorderExtent(this.to);
            }
            return this;
        }

        @Override
        public VoxelShape getCollisionShape() {
            return Shapes.join(Shapes.INFINITY, Shapes.box(Math.floor(this.getMinX()), Double.NEGATIVE_INFINITY, Math.floor(this.getMinZ()), Math.ceil(this.getMaxX()), Double.POSITIVE_INFINITY, Math.ceil(this.getMaxZ())), BooleanOp.ONLY_FIRST);
        }
    }

    public static class Settings {
        private final double centerX;
        private final double centerZ;
        private final double damagePerBlock;
        private final double safeZone;
        private final int warningBlocks;
        private final int warningTime;
        private final double size;
        private final long sizeLerpTime;
        private final double sizeLerpTarget;

        Settings(double d, double d2, double d3, double d4, int n, int n2, double d5, long l, double d6) {
            this.centerX = d;
            this.centerZ = d2;
            this.damagePerBlock = d3;
            this.safeZone = d4;
            this.warningBlocks = n;
            this.warningTime = n2;
            this.size = d5;
            this.sizeLerpTime = l;
            this.sizeLerpTarget = d6;
        }

        Settings(WorldBorder worldBorder) {
            this.centerX = worldBorder.getCenterX();
            this.centerZ = worldBorder.getCenterZ();
            this.damagePerBlock = worldBorder.getDamagePerBlock();
            this.safeZone = worldBorder.getDamageSafeZone();
            this.warningBlocks = worldBorder.getWarningBlocks();
            this.warningTime = worldBorder.getWarningTime();
            this.size = worldBorder.getSize();
            this.sizeLerpTime = worldBorder.getLerpRemainingTime();
            this.sizeLerpTarget = worldBorder.getLerpTarget();
        }

        public double getCenterX() {
            return this.centerX;
        }

        public double getCenterZ() {
            return this.centerZ;
        }

        public double getDamagePerBlock() {
            return this.damagePerBlock;
        }

        public double getSafeZone() {
            return this.safeZone;
        }

        public int getWarningBlocks() {
            return this.warningBlocks;
        }

        public int getWarningTime() {
            return this.warningTime;
        }

        public double getSize() {
            return this.size;
        }

        public long getSizeLerpTime() {
            return this.sizeLerpTime;
        }

        public double getSizeLerpTarget() {
            return this.sizeLerpTarget;
        }

        public static Settings read(DynamicLike<?> dynamicLike, Settings settings) {
            double d = Mth.clamp(dynamicLike.get("BorderCenterX").asDouble(settings.centerX), -2.9999984E7, 2.9999984E7);
            double d2 = Mth.clamp(dynamicLike.get("BorderCenterZ").asDouble(settings.centerZ), -2.9999984E7, 2.9999984E7);
            double d3 = dynamicLike.get("BorderSize").asDouble(settings.size);
            long l = dynamicLike.get("BorderSizeLerpTime").asLong(settings.sizeLerpTime);
            double d4 = dynamicLike.get("BorderSizeLerpTarget").asDouble(settings.sizeLerpTarget);
            double d5 = dynamicLike.get("BorderSafeZone").asDouble(settings.safeZone);
            double d6 = dynamicLike.get("BorderDamagePerBlock").asDouble(settings.damagePerBlock);
            int n = dynamicLike.get("BorderWarningBlocks").asInt(settings.warningBlocks);
            int n2 = dynamicLike.get("BorderWarningTime").asInt(settings.warningTime);
            return new Settings(d, d2, d6, d5, n, n2, d3, l, d4);
        }

        public void write(CompoundTag compoundTag) {
            compoundTag.putDouble("BorderCenterX", this.centerX);
            compoundTag.putDouble("BorderCenterZ", this.centerZ);
            compoundTag.putDouble("BorderSize", this.size);
            compoundTag.putLong("BorderSizeLerpTime", this.sizeLerpTime);
            compoundTag.putDouble("BorderSafeZone", this.safeZone);
            compoundTag.putDouble("BorderDamagePerBlock", this.damagePerBlock);
            compoundTag.putDouble("BorderSizeLerpTarget", this.sizeLerpTarget);
            compoundTag.putDouble("BorderWarningBlocks", this.warningBlocks);
            compoundTag.putDouble("BorderWarningTime", this.warningTime);
        }
    }
}

