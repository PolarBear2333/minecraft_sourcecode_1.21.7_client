/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Sets
 */
package net.minecraft.world.level.gameevent;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.GameEventListenerRegistry;
import net.minecraft.world.phys.Vec3;

public class EuclideanGameEventListenerRegistry
implements GameEventListenerRegistry {
    private final List<GameEventListener> listeners = Lists.newArrayList();
    private final Set<GameEventListener> listenersToRemove = Sets.newHashSet();
    private final List<GameEventListener> listenersToAdd = Lists.newArrayList();
    private boolean processing;
    private final ServerLevel level;
    private final int sectionY;
    private final OnEmptyAction onEmptyAction;

    public EuclideanGameEventListenerRegistry(ServerLevel serverLevel, int n, OnEmptyAction onEmptyAction) {
        this.level = serverLevel;
        this.sectionY = n;
        this.onEmptyAction = onEmptyAction;
    }

    @Override
    public boolean isEmpty() {
        return this.listeners.isEmpty();
    }

    @Override
    public void register(GameEventListener gameEventListener) {
        if (this.processing) {
            this.listenersToAdd.add(gameEventListener);
        } else {
            this.listeners.add(gameEventListener);
        }
        DebugPackets.sendGameEventListenerInfo(this.level, gameEventListener);
    }

    @Override
    public void unregister(GameEventListener gameEventListener) {
        if (this.processing) {
            this.listenersToRemove.add(gameEventListener);
        } else {
            this.listeners.remove(gameEventListener);
        }
        if (this.listeners.isEmpty()) {
            this.onEmptyAction.apply(this.sectionY);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean visitInRangeListeners(Holder<GameEvent> holder, Vec3 vec3, GameEvent.Context context, GameEventListenerRegistry.ListenerVisitor listenerVisitor) {
        this.processing = true;
        boolean bl = false;
        try {
            Iterator<GameEventListener> iterator = this.listeners.iterator();
            while (iterator.hasNext()) {
                GameEventListener gameEventListener = iterator.next();
                if (this.listenersToRemove.remove(gameEventListener)) {
                    iterator.remove();
                    continue;
                }
                Optional<Vec3> optional = EuclideanGameEventListenerRegistry.getPostableListenerPosition(this.level, vec3, gameEventListener);
                if (!optional.isPresent()) continue;
                listenerVisitor.visit(gameEventListener, optional.get());
                bl = true;
            }
        }
        finally {
            this.processing = false;
        }
        if (!this.listenersToAdd.isEmpty()) {
            this.listeners.addAll(this.listenersToAdd);
            this.listenersToAdd.clear();
        }
        if (!this.listenersToRemove.isEmpty()) {
            this.listeners.removeAll(this.listenersToRemove);
            this.listenersToRemove.clear();
        }
        return bl;
    }

    private static Optional<Vec3> getPostableListenerPosition(ServerLevel serverLevel, Vec3 vec3, GameEventListener gameEventListener) {
        int n;
        Optional<Vec3> optional = gameEventListener.getListenerSource().getPosition(serverLevel);
        if (optional.isEmpty()) {
            return Optional.empty();
        }
        double d = BlockPos.containing(optional.get()).distSqr(BlockPos.containing(vec3));
        if (d > (double)(n = gameEventListener.getListenerRadius() * gameEventListener.getListenerRadius())) {
            return Optional.empty();
        }
        return optional;
    }

    @FunctionalInterface
    public static interface OnEmptyAction {
        public void apply(int var1);
    }
}

