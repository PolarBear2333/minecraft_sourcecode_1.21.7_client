/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.world.entity.ai.behavior;

import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.ShufflingList;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class GateBehavior<E extends LivingEntity>
implements BehaviorControl<E> {
    private final Map<MemoryModuleType<?>, MemoryStatus> entryCondition;
    private final Set<MemoryModuleType<?>> exitErasedMemories;
    private final OrderPolicy orderPolicy;
    private final RunningPolicy runningPolicy;
    private final ShufflingList<BehaviorControl<? super E>> behaviors = new ShufflingList();
    private Behavior.Status status = Behavior.Status.STOPPED;

    public GateBehavior(Map<MemoryModuleType<?>, MemoryStatus> map, Set<MemoryModuleType<?>> set, OrderPolicy orderPolicy, RunningPolicy runningPolicy, List<Pair<? extends BehaviorControl<? super E>, Integer>> list) {
        this.entryCondition = map;
        this.exitErasedMemories = set;
        this.orderPolicy = orderPolicy;
        this.runningPolicy = runningPolicy;
        list.forEach(pair -> this.behaviors.add((BehaviorControl)pair.getFirst(), (Integer)pair.getSecond()));
    }

    @Override
    public Behavior.Status getStatus() {
        return this.status;
    }

    private boolean hasRequiredMemories(E e) {
        for (Map.Entry<MemoryModuleType<?>, MemoryStatus> entry : this.entryCondition.entrySet()) {
            MemoryModuleType<?> memoryModuleType = entry.getKey();
            MemoryStatus memoryStatus = entry.getValue();
            if (((LivingEntity)e).getBrain().checkMemory(memoryModuleType, memoryStatus)) continue;
            return false;
        }
        return true;
    }

    @Override
    public final boolean tryStart(ServerLevel serverLevel, E e, long l) {
        if (this.hasRequiredMemories(e)) {
            this.status = Behavior.Status.RUNNING;
            this.orderPolicy.apply(this.behaviors);
            this.runningPolicy.apply(this.behaviors.stream(), serverLevel, e, l);
            return true;
        }
        return false;
    }

    @Override
    public final void tickOrStop(ServerLevel serverLevel, E e, long l) {
        this.behaviors.stream().filter(behaviorControl -> behaviorControl.getStatus() == Behavior.Status.RUNNING).forEach(behaviorControl -> behaviorControl.tickOrStop(serverLevel, e, l));
        if (this.behaviors.stream().noneMatch(behaviorControl -> behaviorControl.getStatus() == Behavior.Status.RUNNING)) {
            this.doStop(serverLevel, e, l);
        }
    }

    @Override
    public final void doStop(ServerLevel serverLevel, E e, long l) {
        this.status = Behavior.Status.STOPPED;
        this.behaviors.stream().filter(behaviorControl -> behaviorControl.getStatus() == Behavior.Status.RUNNING).forEach(behaviorControl -> behaviorControl.doStop(serverLevel, e, l));
        this.exitErasedMemories.forEach(((LivingEntity)e).getBrain()::eraseMemory);
    }

    @Override
    public String debugString() {
        return this.getClass().getSimpleName();
    }

    public String toString() {
        Set set = this.behaviors.stream().filter(behaviorControl -> behaviorControl.getStatus() == Behavior.Status.RUNNING).collect(Collectors.toSet());
        return "(" + this.getClass().getSimpleName() + "): " + String.valueOf(set);
    }

    public static enum OrderPolicy {
        ORDERED(shufflingList -> {}),
        SHUFFLED(ShufflingList::shuffle);

        private final Consumer<ShufflingList<?>> consumer;

        private OrderPolicy(Consumer<ShufflingList<?>> consumer) {
            this.consumer = consumer;
        }

        public void apply(ShufflingList<?> shufflingList) {
            this.consumer.accept(shufflingList);
        }
    }

    public static enum RunningPolicy {
        RUN_ONE{

            @Override
            public <E extends LivingEntity> void apply(Stream<BehaviorControl<? super E>> stream, ServerLevel serverLevel, E e, long l) {
                stream.filter(behaviorControl -> behaviorControl.getStatus() == Behavior.Status.STOPPED).filter(behaviorControl -> behaviorControl.tryStart(serverLevel, e, l)).findFirst();
            }
        }
        ,
        TRY_ALL{

            @Override
            public <E extends LivingEntity> void apply(Stream<BehaviorControl<? super E>> stream, ServerLevel serverLevel, E e, long l) {
                stream.filter(behaviorControl -> behaviorControl.getStatus() == Behavior.Status.STOPPED).forEach(behaviorControl -> behaviorControl.tryStart(serverLevel, e, l));
            }
        };


        public abstract <E extends LivingEntity> void apply(Stream<BehaviorControl<? super E>> var1, ServerLevel var2, E var3, long var4);
    }
}

