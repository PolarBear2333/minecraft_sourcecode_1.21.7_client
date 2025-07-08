/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 */
package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestEvent;
import net.minecraft.gametest.framework.GameTestException;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.network.chat.Component;

public class GameTestSequence {
    final GameTestInfo parent;
    private final List<GameTestEvent> events = Lists.newArrayList();
    private int lastTick;

    GameTestSequence(GameTestInfo gameTestInfo) {
        this.parent = gameTestInfo;
        this.lastTick = gameTestInfo.getTick();
    }

    public GameTestSequence thenWaitUntil(Runnable runnable) {
        this.events.add(GameTestEvent.create(runnable));
        return this;
    }

    public GameTestSequence thenWaitUntil(long l, Runnable runnable) {
        this.events.add(GameTestEvent.create(l, runnable));
        return this;
    }

    public GameTestSequence thenIdle(int n) {
        return this.thenExecuteAfter(n, () -> {});
    }

    public GameTestSequence thenExecute(Runnable runnable) {
        this.events.add(GameTestEvent.create(() -> this.executeWithoutFail(runnable)));
        return this;
    }

    public GameTestSequence thenExecuteAfter(int n, Runnable runnable) {
        this.events.add(GameTestEvent.create(() -> {
            if (this.parent.getTick() < this.lastTick + n) {
                throw new GameTestAssertException(Component.translatable("test.error.sequence.not_completed"), this.parent.getTick());
            }
            this.executeWithoutFail(runnable);
        }));
        return this;
    }

    public GameTestSequence thenExecuteFor(int n, Runnable runnable) {
        this.events.add(GameTestEvent.create(() -> {
            if (this.parent.getTick() < this.lastTick + n) {
                this.executeWithoutFail(runnable);
                throw new GameTestAssertException(Component.translatable("test.error.sequence.not_completed"), this.parent.getTick());
            }
        }));
        return this;
    }

    public void thenSucceed() {
        this.events.add(GameTestEvent.create(this.parent::succeed));
    }

    public void thenFail(Supplier<GameTestException> supplier) {
        this.events.add(GameTestEvent.create(() -> this.parent.fail((GameTestException)supplier.get())));
    }

    public Condition thenTrigger() {
        Condition condition = new Condition();
        this.events.add(GameTestEvent.create(() -> condition.trigger(this.parent.getTick())));
        return condition;
    }

    public void tickAndContinue(int n) {
        try {
            this.tick(n);
        }
        catch (GameTestAssertException gameTestAssertException) {
            // empty catch block
        }
    }

    public void tickAndFailIfNotComplete(int n) {
        try {
            this.tick(n);
        }
        catch (GameTestAssertException gameTestAssertException) {
            this.parent.fail(gameTestAssertException);
        }
    }

    private void executeWithoutFail(Runnable runnable) {
        try {
            runnable.run();
        }
        catch (GameTestAssertException gameTestAssertException) {
            this.parent.fail(gameTestAssertException);
        }
    }

    private void tick(int n) {
        Iterator<GameTestEvent> iterator = this.events.iterator();
        while (iterator.hasNext()) {
            GameTestEvent gameTestEvent = iterator.next();
            gameTestEvent.assertion.run();
            iterator.remove();
            int n2 = n - this.lastTick;
            int n3 = this.lastTick;
            this.lastTick = n;
            if (gameTestEvent.expectedDelay == null || gameTestEvent.expectedDelay == (long)n2) continue;
            this.parent.fail(new GameTestAssertException(Component.translatable("test.error.sequence.invalid_tick", (long)n3 + gameTestEvent.expectedDelay), n));
            break;
        }
    }

    public class Condition {
        private static final int NOT_TRIGGERED = -1;
        private int triggerTime = -1;

        void trigger(int n) {
            if (this.triggerTime != -1) {
                throw new IllegalStateException("Condition already triggered at " + this.triggerTime);
            }
            this.triggerTime = n;
        }

        public void assertTriggeredThisTick() {
            int n = GameTestSequence.this.parent.getTick();
            if (this.triggerTime != n) {
                if (this.triggerTime == -1) {
                    throw new GameTestAssertException(Component.translatable("test.error.sequence.condition_not_triggered"), n);
                }
                throw new GameTestAssertException(Component.translatable("test.error.sequence.condition_already_triggered", this.triggerTime), n);
            }
        }
    }
}

