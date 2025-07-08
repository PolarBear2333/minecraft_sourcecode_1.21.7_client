/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Iterables
 *  it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap
 */
package net.minecraft.world.entity.ai.memory;

import com.google.common.collect.Iterables;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.sensing.Sensor;

public class NearestVisibleLivingEntities {
    private static final NearestVisibleLivingEntities EMPTY = new NearestVisibleLivingEntities();
    private final List<LivingEntity> nearbyEntities;
    private final Predicate<LivingEntity> lineOfSightTest;

    private NearestVisibleLivingEntities() {
        this.nearbyEntities = List.of();
        this.lineOfSightTest = livingEntity -> false;
    }

    public NearestVisibleLivingEntities(ServerLevel serverLevel, LivingEntity livingEntity3, List<LivingEntity> list) {
        this.nearbyEntities = list;
        Object2BooleanOpenHashMap object2BooleanOpenHashMap = new Object2BooleanOpenHashMap(list.size());
        Predicate<LivingEntity> predicate = livingEntity2 -> Sensor.isEntityTargetable(serverLevel, livingEntity3, livingEntity2);
        this.lineOfSightTest = livingEntity -> object2BooleanOpenHashMap.computeIfAbsent(livingEntity, predicate);
    }

    public static NearestVisibleLivingEntities empty() {
        return EMPTY;
    }

    public Optional<LivingEntity> findClosest(Predicate<LivingEntity> predicate) {
        for (LivingEntity livingEntity : this.nearbyEntities) {
            if (!predicate.test(livingEntity) || !this.lineOfSightTest.test(livingEntity)) continue;
            return Optional.of(livingEntity);
        }
        return Optional.empty();
    }

    public Iterable<LivingEntity> findAll(Predicate<LivingEntity> predicate) {
        return Iterables.filter(this.nearbyEntities, livingEntity -> predicate.test((LivingEntity)livingEntity) && this.lineOfSightTest.test((LivingEntity)livingEntity));
    }

    public Stream<LivingEntity> find(Predicate<LivingEntity> predicate) {
        return this.nearbyEntities.stream().filter(livingEntity -> predicate.test((LivingEntity)livingEntity) && this.lineOfSightTest.test((LivingEntity)livingEntity));
    }

    public boolean contains(LivingEntity livingEntity) {
        return this.nearbyEntities.contains(livingEntity) && this.lineOfSightTest.test(livingEntity);
    }

    public boolean contains(Predicate<LivingEntity> predicate) {
        for (LivingEntity livingEntity : this.nearbyEntities) {
            if (!predicate.test(livingEntity) || !this.lineOfSightTest.test(livingEntity)) continue;
            return true;
        }
        return false;
    }
}

