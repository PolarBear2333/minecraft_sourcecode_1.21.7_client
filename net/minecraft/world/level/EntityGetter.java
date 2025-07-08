/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public interface EntityGetter {
    public List<Entity> getEntities(@Nullable Entity var1, AABB var2, Predicate<? super Entity> var3);

    public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> var1, AABB var2, Predicate<? super T> var3);

    default public <T extends Entity> List<T> getEntitiesOfClass(Class<T> clazz, AABB aABB, Predicate<? super T> predicate) {
        return this.getEntities(EntityTypeTest.forClass(clazz), aABB, predicate);
    }

    public List<? extends Player> players();

    default public List<Entity> getEntities(@Nullable Entity entity, AABB aABB) {
        return this.getEntities(entity, aABB, EntitySelector.NO_SPECTATORS);
    }

    default public boolean isUnobstructed(@Nullable Entity entity, VoxelShape voxelShape) {
        if (voxelShape.isEmpty()) {
            return true;
        }
        for (Entity entity2 : this.getEntities(entity, voxelShape.bounds())) {
            if (entity2.isRemoved() || !entity2.blocksBuilding || entity != null && entity2.isPassengerOfSameVehicle(entity) || !Shapes.joinIsNotEmpty(voxelShape, Shapes.create(entity2.getBoundingBox()), BooleanOp.AND)) continue;
            return false;
        }
        return true;
    }

    default public <T extends Entity> List<T> getEntitiesOfClass(Class<T> clazz, AABB aABB) {
        return this.getEntitiesOfClass(clazz, aABB, EntitySelector.NO_SPECTATORS);
    }

    default public List<VoxelShape> getEntityCollisions(@Nullable Entity entity, AABB aABB) {
        if (aABB.getSize() < 1.0E-7) {
            return List.of();
        }
        Predicate<Entity> predicate = entity == null ? EntitySelector.CAN_BE_COLLIDED_WITH : EntitySelector.NO_SPECTATORS.and(entity::canCollideWith);
        List<Entity> list = this.getEntities(entity, aABB.inflate(1.0E-7), predicate);
        if (list.isEmpty()) {
            return List.of();
        }
        ImmutableList.Builder builder = ImmutableList.builderWithExpectedSize((int)list.size());
        for (Entity entity2 : list) {
            builder.add((Object)Shapes.create(entity2.getBoundingBox()));
        }
        return builder.build();
    }

    @Nullable
    default public Player getNearestPlayer(double d, double d2, double d3, double d4, @Nullable Predicate<Entity> predicate) {
        double d5 = -1.0;
        Player player = null;
        for (Player player2 : this.players()) {
            if (predicate != null && !predicate.test(player2)) continue;
            double d6 = player2.distanceToSqr(d, d2, d3);
            if (!(d4 < 0.0) && !(d6 < d4 * d4) || d5 != -1.0 && !(d6 < d5)) continue;
            d5 = d6;
            player = player2;
        }
        return player;
    }

    @Nullable
    default public Player getNearestPlayer(Entity entity, double d) {
        return this.getNearestPlayer(entity.getX(), entity.getY(), entity.getZ(), d, false);
    }

    @Nullable
    default public Player getNearestPlayer(double d, double d2, double d3, double d4, boolean bl) {
        Predicate<Entity> predicate = bl ? EntitySelector.NO_CREATIVE_OR_SPECTATOR : EntitySelector.NO_SPECTATORS;
        return this.getNearestPlayer(d, d2, d3, d4, predicate);
    }

    default public boolean hasNearbyAlivePlayer(double d, double d2, double d3, double d4) {
        for (Player player : this.players()) {
            if (!EntitySelector.NO_SPECTATORS.test(player) || !EntitySelector.LIVING_ENTITY_STILL_ALIVE.test(player)) continue;
            double d5 = player.distanceToSqr(d, d2, d3);
            if (!(d4 < 0.0) && !(d5 < d4 * d4)) continue;
            return true;
        }
        return false;
    }

    @Nullable
    default public Player getPlayerByUUID(UUID uUID) {
        for (int i = 0; i < this.players().size(); ++i) {
            Player player = this.players().get(i);
            if (!uUID.equals(player.getUUID())) continue;
            return player;
        }
        return null;
    }
}

