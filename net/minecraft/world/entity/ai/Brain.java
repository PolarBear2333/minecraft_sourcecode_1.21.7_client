/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.MapLike
 *  com.mojang.serialization.RecordBuilder
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.mutable.MutableObject
 *  org.slf4j.Logger
 */
package net.minecraft.world.entity.ai;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.ExpirableValue;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;

public class Brain<E extends LivingEntity> {
    static final Logger LOGGER = LogUtils.getLogger();
    private final Supplier<Codec<Brain<E>>> codec;
    private static final int SCHEDULE_UPDATE_DELAY = 20;
    private final Map<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> memories = Maps.newHashMap();
    private final Map<SensorType<? extends Sensor<? super E>>, Sensor<? super E>> sensors = Maps.newLinkedHashMap();
    private final Map<Integer, Map<Activity, Set<BehaviorControl<? super E>>>> availableBehaviorsByPriority = Maps.newTreeMap();
    private Schedule schedule = Schedule.EMPTY;
    private final Map<Activity, Set<Pair<MemoryModuleType<?>, MemoryStatus>>> activityRequirements = Maps.newHashMap();
    private final Map<Activity, Set<MemoryModuleType<?>>> activityMemoriesToEraseWhenStopped = Maps.newHashMap();
    private Set<Activity> coreActivities = Sets.newHashSet();
    private final Set<Activity> activeActivities = Sets.newHashSet();
    private Activity defaultActivity = Activity.IDLE;
    private long lastScheduleUpdate = -9999L;

    public static <E extends LivingEntity> Provider<E> provider(Collection<? extends MemoryModuleType<?>> collection, Collection<? extends SensorType<? extends Sensor<? super E>>> collection2) {
        return new Provider(collection, collection2);
    }

    public static <E extends LivingEntity> Codec<Brain<E>> codec(final Collection<? extends MemoryModuleType<?>> collection, final Collection<? extends SensorType<? extends Sensor<? super E>>> collection2) {
        final MutableObject mutableObject = new MutableObject();
        mutableObject.setValue((Object)new MapCodec<Brain<E>>(){

            public <T> Stream<T> keys(DynamicOps<T> dynamicOps) {
                return collection.stream().flatMap(memoryModuleType -> memoryModuleType.getCodec().map(codec -> BuiltInRegistries.MEMORY_MODULE_TYPE.getKey((MemoryModuleType<?>)memoryModuleType)).stream()).map(resourceLocation -> dynamicOps.createString(resourceLocation.toString()));
            }

            public <T> DataResult<Brain<E>> decode(DynamicOps<T> dynamicOps, MapLike<T> mapLike) {
                MutableObject mutableObject2 = new MutableObject((Object)DataResult.success((Object)ImmutableList.builder()));
                mapLike.entries().forEach(pair -> {
                    DataResult dataResult = BuiltInRegistries.MEMORY_MODULE_TYPE.byNameCodec().parse(dynamicOps, pair.getFirst());
                    DataResult dataResult2 = dataResult.flatMap(memoryModuleType -> this.captureRead((MemoryModuleType)memoryModuleType, dynamicOps, (Object)pair.getSecond()));
                    mutableObject2.setValue((Object)((DataResult)mutableObject2.getValue()).apply2(ImmutableList.Builder::add, dataResult2));
                });
                ImmutableList immutableList = ((DataResult)mutableObject2.getValue()).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).map(ImmutableList.Builder::build).orElseGet(ImmutableList::of);
                return DataResult.success(new Brain(collection, collection2, immutableList, () -> ((MutableObject)mutableObject).getValue()));
            }

            private <T, U> DataResult<MemoryValue<U>> captureRead(MemoryModuleType<U> memoryModuleType, DynamicOps<T> dynamicOps, T t) {
                return memoryModuleType.getCodec().map(DataResult::success).orElseGet(() -> DataResult.error(() -> "No codec for memory: " + String.valueOf(memoryModuleType))).flatMap(codec -> codec.parse(dynamicOps, t)).map(expirableValue -> new MemoryValue(memoryModuleType, Optional.of(expirableValue)));
            }

            public <T> RecordBuilder<T> encode(Brain<E> brain, DynamicOps<T> dynamicOps, RecordBuilder<T> recordBuilder) {
                brain.memories().forEach(memoryValue -> memoryValue.serialize(dynamicOps, recordBuilder));
                return recordBuilder;
            }

            public /* synthetic */ RecordBuilder encode(Object object, DynamicOps dynamicOps, RecordBuilder recordBuilder) {
                return this.encode((Brain)object, dynamicOps, recordBuilder);
            }
        }.fieldOf("memories").codec());
        return (Codec)mutableObject.getValue();
    }

    public Brain(Collection<? extends MemoryModuleType<?>> collection, Collection<? extends SensorType<? extends Sensor<? super E>>> collection2, ImmutableList<MemoryValue<?>> immutableList, Supplier<Codec<Brain<E>>> supplier) {
        this.codec = supplier;
        for (MemoryModuleType<?> memoryModuleType : collection) {
            this.memories.put(memoryModuleType, Optional.empty());
        }
        for (SensorType sensorType : collection2) {
            this.sensors.put(sensorType, (Sensor<E>)sensorType.create());
        }
        for (Sensor sensor : this.sensors.values()) {
            for (MemoryModuleType<?> memoryModuleType : sensor.requires()) {
                this.memories.put(memoryModuleType, Optional.empty());
            }
        }
        for (MemoryValue memoryValue : immutableList) {
            memoryValue.setMemoryInternal(this);
        }
    }

    public <T> DataResult<T> serializeStart(DynamicOps<T> dynamicOps) {
        return this.codec.get().encodeStart(dynamicOps, (Object)this);
    }

    Stream<MemoryValue<?>> memories() {
        return this.memories.entrySet().stream().map(entry -> MemoryValue.createUnchecked((MemoryModuleType)entry.getKey(), (Optional)entry.getValue()));
    }

    public boolean hasMemoryValue(MemoryModuleType<?> memoryModuleType) {
        return this.checkMemory(memoryModuleType, MemoryStatus.VALUE_PRESENT);
    }

    public void clearMemories() {
        this.memories.keySet().forEach(memoryModuleType -> this.memories.put((MemoryModuleType<?>)memoryModuleType, Optional.empty()));
    }

    public <U> void eraseMemory(MemoryModuleType<U> memoryModuleType) {
        this.setMemory(memoryModuleType, Optional.empty());
    }

    public <U> void setMemory(MemoryModuleType<U> memoryModuleType, @Nullable U u) {
        this.setMemory(memoryModuleType, Optional.ofNullable(u));
    }

    public <U> void setMemoryWithExpiry(MemoryModuleType<U> memoryModuleType, U u, long l) {
        this.setMemoryInternal(memoryModuleType, Optional.of(ExpirableValue.of(u, l)));
    }

    public <U> void setMemory(MemoryModuleType<U> memoryModuleType, Optional<? extends U> optional) {
        this.setMemoryInternal(memoryModuleType, optional.map(ExpirableValue::of));
    }

    <U> void setMemoryInternal(MemoryModuleType<U> memoryModuleType, Optional<? extends ExpirableValue<?>> optional) {
        if (this.memories.containsKey(memoryModuleType)) {
            if (optional.isPresent() && this.isEmptyCollection(optional.get().getValue())) {
                this.eraseMemory(memoryModuleType);
            } else {
                this.memories.put(memoryModuleType, optional);
            }
        }
    }

    public <U> Optional<U> getMemory(MemoryModuleType<U> memoryModuleType) {
        Optional<ExpirableValue<?>> optional = this.memories.get(memoryModuleType);
        if (optional == null) {
            throw new IllegalStateException("Unregistered memory fetched: " + String.valueOf(memoryModuleType));
        }
        return optional.map(ExpirableValue::getValue);
    }

    @Nullable
    public <U> Optional<U> getMemoryInternal(MemoryModuleType<U> memoryModuleType) {
        Optional<ExpirableValue<?>> optional = this.memories.get(memoryModuleType);
        if (optional == null) {
            return null;
        }
        return optional.map(ExpirableValue::getValue);
    }

    public <U> long getTimeUntilExpiry(MemoryModuleType<U> memoryModuleType) {
        Optional<ExpirableValue<?>> optional = this.memories.get(memoryModuleType);
        return optional.map(ExpirableValue::getTimeToLive).orElse(0L);
    }

    @Deprecated
    @VisibleForDebug
    public Map<MemoryModuleType<?>, Optional<? extends ExpirableValue<?>>> getMemories() {
        return this.memories;
    }

    public <U> boolean isMemoryValue(MemoryModuleType<U> memoryModuleType, U u) {
        if (!this.hasMemoryValue(memoryModuleType)) {
            return false;
        }
        return this.getMemory(memoryModuleType).filter(object2 -> object2.equals(u)).isPresent();
    }

    public boolean checkMemory(MemoryModuleType<?> memoryModuleType, MemoryStatus memoryStatus) {
        Optional<ExpirableValue<?>> optional = this.memories.get(memoryModuleType);
        if (optional == null) {
            return false;
        }
        return memoryStatus == MemoryStatus.REGISTERED || memoryStatus == MemoryStatus.VALUE_PRESENT && optional.isPresent() || memoryStatus == MemoryStatus.VALUE_ABSENT && optional.isEmpty();
    }

    public Schedule getSchedule() {
        return this.schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public void setCoreActivities(Set<Activity> set) {
        this.coreActivities = set;
    }

    @Deprecated
    @VisibleForDebug
    public Set<Activity> getActiveActivities() {
        return this.activeActivities;
    }

    @Deprecated
    @VisibleForDebug
    public List<BehaviorControl<? super E>> getRunningBehaviors() {
        ObjectArrayList objectArrayList = new ObjectArrayList();
        for (Map<Activity, Set<BehaviorControl<E>>> map : this.availableBehaviorsByPriority.values()) {
            for (Set<BehaviorControl<E>> set : map.values()) {
                for (BehaviorControl<E> behaviorControl : set) {
                    if (behaviorControl.getStatus() != Behavior.Status.RUNNING) continue;
                    objectArrayList.add(behaviorControl);
                }
            }
        }
        return objectArrayList;
    }

    public void useDefaultActivity() {
        this.setActiveActivity(this.defaultActivity);
    }

    public Optional<Activity> getActiveNonCoreActivity() {
        for (Activity activity : this.activeActivities) {
            if (this.coreActivities.contains(activity)) continue;
            return Optional.of(activity);
        }
        return Optional.empty();
    }

    public void setActiveActivityIfPossible(Activity activity) {
        if (this.activityRequirementsAreMet(activity)) {
            this.setActiveActivity(activity);
        } else {
            this.useDefaultActivity();
        }
    }

    private void setActiveActivity(Activity activity) {
        if (this.isActive(activity)) {
            return;
        }
        this.eraseMemoriesForOtherActivitesThan(activity);
        this.activeActivities.clear();
        this.activeActivities.addAll(this.coreActivities);
        this.activeActivities.add(activity);
    }

    private void eraseMemoriesForOtherActivitesThan(Activity activity) {
        for (Activity activity2 : this.activeActivities) {
            Set<MemoryModuleType<?>> set;
            if (activity2 == activity || (set = this.activityMemoriesToEraseWhenStopped.get(activity2)) == null) continue;
            for (MemoryModuleType<?> memoryModuleType : set) {
                this.eraseMemory(memoryModuleType);
            }
        }
    }

    public void updateActivityFromSchedule(long l, long l2) {
        if (l2 - this.lastScheduleUpdate > 20L) {
            this.lastScheduleUpdate = l2;
            Activity activity = this.getSchedule().getActivityAt((int)(l % 24000L));
            if (!this.activeActivities.contains(activity)) {
                this.setActiveActivityIfPossible(activity);
            }
        }
    }

    public void setActiveActivityToFirstValid(List<Activity> list) {
        for (Activity activity : list) {
            if (!this.activityRequirementsAreMet(activity)) continue;
            this.setActiveActivity(activity);
            break;
        }
    }

    public void setDefaultActivity(Activity activity) {
        this.defaultActivity = activity;
    }

    public void addActivity(Activity activity, int n, ImmutableList<? extends BehaviorControl<? super E>> immutableList) {
        this.addActivity(activity, this.createPriorityPairs(n, immutableList));
    }

    public void addActivityAndRemoveMemoryWhenStopped(Activity activity, int n, ImmutableList<? extends BehaviorControl<? super E>> immutableList, MemoryModuleType<?> memoryModuleType) {
        ImmutableSet immutableSet = ImmutableSet.of((Object)Pair.of(memoryModuleType, (Object)((Object)MemoryStatus.VALUE_PRESENT)));
        ImmutableSet immutableSet2 = ImmutableSet.of(memoryModuleType);
        this.addActivityAndRemoveMemoriesWhenStopped(activity, (ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super E>>>)this.createPriorityPairs(n, immutableList), (Set<Pair<MemoryModuleType<?>, MemoryStatus>>)immutableSet, (Set<MemoryModuleType<?>>)immutableSet2);
    }

    public void addActivity(Activity activity, ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super E>>> immutableList) {
        this.addActivityAndRemoveMemoriesWhenStopped(activity, immutableList, (Set<Pair<MemoryModuleType<?>, MemoryStatus>>)ImmutableSet.of(), Sets.newHashSet());
    }

    public void addActivityWithConditions(Activity activity, int n, ImmutableList<? extends BehaviorControl<? super E>> immutableList, Set<Pair<MemoryModuleType<?>, MemoryStatus>> set) {
        this.addActivityWithConditions(activity, this.createPriorityPairs(n, immutableList), set);
    }

    public void addActivityWithConditions(Activity activity, ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super E>>> immutableList, Set<Pair<MemoryModuleType<?>, MemoryStatus>> set) {
        this.addActivityAndRemoveMemoriesWhenStopped(activity, immutableList, set, Sets.newHashSet());
    }

    public void addActivityAndRemoveMemoriesWhenStopped(Activity activity2, ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super E>>> immutableList, Set<Pair<MemoryModuleType<?>, MemoryStatus>> set, Set<MemoryModuleType<?>> set2) {
        this.activityRequirements.put(activity2, set);
        if (!set2.isEmpty()) {
            this.activityMemoriesToEraseWhenStopped.put(activity2, set2);
        }
        for (Pair pair : immutableList) {
            this.availableBehaviorsByPriority.computeIfAbsent((Integer)pair.getFirst(), n -> Maps.newHashMap()).computeIfAbsent(activity2, activity -> Sets.newLinkedHashSet()).add((BehaviorControl)pair.getSecond());
        }
    }

    @VisibleForTesting
    public void removeAllBehaviors() {
        this.availableBehaviorsByPriority.clear();
    }

    public boolean isActive(Activity activity) {
        return this.activeActivities.contains(activity);
    }

    public Brain<E> copyWithoutBehaviors() {
        Brain<E> brain = new Brain<E>(this.memories.keySet(), this.sensors.keySet(), ImmutableList.of(), this.codec);
        for (Map.Entry<MemoryModuleType<?>, Optional<ExpirableValue<?>>> entry : this.memories.entrySet()) {
            MemoryModuleType<?> memoryModuleType = entry.getKey();
            if (!entry.getValue().isPresent()) continue;
            brain.memories.put(memoryModuleType, entry.getValue());
        }
        return brain;
    }

    public void tick(ServerLevel serverLevel, E e) {
        this.forgetOutdatedMemories();
        this.tickSensors(serverLevel, e);
        this.startEachNonRunningBehavior(serverLevel, e);
        this.tickEachRunningBehavior(serverLevel, e);
    }

    private void tickSensors(ServerLevel serverLevel, E e) {
        for (Sensor<E> sensor : this.sensors.values()) {
            sensor.tick(serverLevel, e);
        }
    }

    private void forgetOutdatedMemories() {
        for (Map.Entry<MemoryModuleType<?>, Optional<ExpirableValue<?>>> entry : this.memories.entrySet()) {
            if (!entry.getValue().isPresent()) continue;
            ExpirableValue<?> expirableValue = entry.getValue().get();
            if (expirableValue.hasExpired()) {
                this.eraseMemory(entry.getKey());
            }
            expirableValue.tick();
        }
    }

    public void stopAll(ServerLevel serverLevel, E e) {
        long l = ((Entity)e).level().getGameTime();
        for (BehaviorControl<E> behaviorControl : this.getRunningBehaviors()) {
            behaviorControl.doStop(serverLevel, e, l);
        }
    }

    private void startEachNonRunningBehavior(ServerLevel serverLevel, E e) {
        long l = serverLevel.getGameTime();
        for (Map<Activity, Set<BehaviorControl<E>>> map : this.availableBehaviorsByPriority.values()) {
            for (Map.Entry<Activity, Set<BehaviorControl<E>>> entry : map.entrySet()) {
                Activity activity = entry.getKey();
                if (!this.activeActivities.contains(activity)) continue;
                Set<BehaviorControl<E>> set = entry.getValue();
                for (BehaviorControl<E> behaviorControl : set) {
                    if (behaviorControl.getStatus() != Behavior.Status.STOPPED) continue;
                    behaviorControl.tryStart(serverLevel, e, l);
                }
            }
        }
    }

    private void tickEachRunningBehavior(ServerLevel serverLevel, E e) {
        long l = serverLevel.getGameTime();
        for (BehaviorControl<E> behaviorControl : this.getRunningBehaviors()) {
            behaviorControl.tickOrStop(serverLevel, e, l);
        }
    }

    private boolean activityRequirementsAreMet(Activity activity) {
        if (!this.activityRequirements.containsKey(activity)) {
            return false;
        }
        for (Pair<MemoryModuleType<?>, MemoryStatus> pair : this.activityRequirements.get(activity)) {
            MemoryStatus memoryStatus;
            MemoryModuleType memoryModuleType = (MemoryModuleType)pair.getFirst();
            if (this.checkMemory(memoryModuleType, memoryStatus = (MemoryStatus)((Object)pair.getSecond()))) continue;
            return false;
        }
        return true;
    }

    private boolean isEmptyCollection(Object object) {
        return object instanceof Collection && ((Collection)object).isEmpty();
    }

    ImmutableList<? extends Pair<Integer, ? extends BehaviorControl<? super E>>> createPriorityPairs(int n, ImmutableList<? extends BehaviorControl<? super E>> immutableList) {
        int n2 = n;
        ImmutableList.Builder builder = ImmutableList.builder();
        for (BehaviorControl behaviorControl : immutableList) {
            builder.add((Object)Pair.of((Object)n2++, (Object)behaviorControl));
        }
        return builder.build();
    }

    public static final class Provider<E extends LivingEntity> {
        private final Collection<? extends MemoryModuleType<?>> memoryTypes;
        private final Collection<? extends SensorType<? extends Sensor<? super E>>> sensorTypes;
        private final Codec<Brain<E>> codec;

        Provider(Collection<? extends MemoryModuleType<?>> collection, Collection<? extends SensorType<? extends Sensor<? super E>>> collection2) {
            this.memoryTypes = collection;
            this.sensorTypes = collection2;
            this.codec = Brain.codec(collection, collection2);
        }

        public Brain<E> makeBrain(Dynamic<?> dynamic) {
            return this.codec.parse(dynamic).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).orElseGet(() -> new Brain(this.memoryTypes, this.sensorTypes, ImmutableList.of(), () -> this.codec));
        }
    }

    static final class MemoryValue<U> {
        private final MemoryModuleType<U> type;
        private final Optional<? extends ExpirableValue<U>> value;

        static <U> MemoryValue<U> createUnchecked(MemoryModuleType<U> memoryModuleType, Optional<? extends ExpirableValue<?>> optional) {
            return new MemoryValue<U>(memoryModuleType, optional);
        }

        MemoryValue(MemoryModuleType<U> memoryModuleType, Optional<? extends ExpirableValue<U>> optional) {
            this.type = memoryModuleType;
            this.value = optional;
        }

        void setMemoryInternal(Brain<?> brain) {
            brain.setMemoryInternal(this.type, this.value);
        }

        public <T> void serialize(DynamicOps<T> dynamicOps, RecordBuilder<T> recordBuilder) {
            this.type.getCodec().ifPresent(codec -> this.value.ifPresent(expirableValue -> recordBuilder.add(BuiltInRegistries.MEMORY_MODULE_TYPE.byNameCodec().encodeStart(dynamicOps, this.type), codec.encodeStart(dynamicOps, expirableValue))));
        }
    }
}

