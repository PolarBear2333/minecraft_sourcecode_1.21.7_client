/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Queues
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.mutable.MutableBoolean
 */
package net.minecraft.client.gui.components.toasts;

import com.google.common.collect.Queues;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.NowPlayingToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class ToastManager {
    private static final int SLOT_COUNT = 5;
    private static final int ALL_SLOTS_OCCUPIED = -1;
    final Minecraft minecraft;
    private final List<ToastInstance<?>> visibleToasts = new ArrayList();
    private final BitSet occupiedSlots = new BitSet(5);
    private final Deque<Toast> queued = Queues.newArrayDeque();
    private final Set<SoundEvent> playedToastSounds = new HashSet<SoundEvent>();
    @Nullable
    private ToastInstance<NowPlayingToast> nowPlayingToast;

    public ToastManager(Minecraft minecraft, Options options) {
        this.minecraft = minecraft;
        if (options.showNowPlayingToast().get().booleanValue()) {
            this.createNowPlayingToast();
        }
    }

    public void update() {
        MutableBoolean mutableBoolean = new MutableBoolean(false);
        this.visibleToasts.removeIf(toastInstance -> {
            Toast.Visibility visibility = toastInstance.visibility;
            toastInstance.update();
            if (toastInstance.visibility != visibility && mutableBoolean.isFalse()) {
                mutableBoolean.setTrue();
                toastInstance.visibility.playSound(this.minecraft.getSoundManager());
            }
            if (toastInstance.hasFinishedRendering()) {
                this.occupiedSlots.clear(toastInstance.firstSlotIndex, toastInstance.firstSlotIndex + toastInstance.occupiedSlotCount);
                return true;
            }
            return false;
        });
        if (!this.queued.isEmpty() && this.freeSlotCount() > 0) {
            this.queued.removeIf(toast -> {
                int n = toast.occcupiedSlotCount();
                int n2 = this.findFreeSlotsIndex(n);
                if (n2 == -1) {
                    return false;
                }
                this.visibleToasts.add(new ToastInstance(this, toast, n2, n));
                this.occupiedSlots.set(n2, n2 + n);
                SoundEvent soundEvent = toast.getSoundEvent();
                if (soundEvent != null && this.playedToastSounds.add(soundEvent)) {
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(soundEvent, 1.0f, 1.0f));
                }
                return true;
            });
        }
        this.playedToastSounds.clear();
        if (this.nowPlayingToast != null) {
            this.nowPlayingToast.update();
        }
    }

    public void render(GuiGraphics guiGraphics) {
        if (this.minecraft.options.hideGui) {
            return;
        }
        int n = guiGraphics.guiWidth();
        if (!this.visibleToasts.isEmpty()) {
            guiGraphics.nextStratum();
        }
        for (ToastInstance<?> toastInstance : this.visibleToasts) {
            toastInstance.render(guiGraphics, n);
        }
        if (this.minecraft.options.showNowPlayingToast().get().booleanValue() && this.nowPlayingToast != null && (this.minecraft.screen == null || !(this.minecraft.screen instanceof PauseScreen))) {
            this.nowPlayingToast.render(guiGraphics, n);
        }
    }

    private int findFreeSlotsIndex(int n) {
        if (this.freeSlotCount() >= n) {
            int n2 = 0;
            for (int i = 0; i < 5; ++i) {
                if (this.occupiedSlots.get(i)) {
                    n2 = 0;
                    continue;
                }
                if (++n2 != n) continue;
                return i + 1 - n2;
            }
        }
        return -1;
    }

    private int freeSlotCount() {
        return 5 - this.occupiedSlots.cardinality();
    }

    @Nullable
    public <T extends Toast> T getToast(Class<? extends T> clazz, Object object) {
        for (ToastInstance<?> object2 : this.visibleToasts) {
            if (object2 == null || !clazz.isAssignableFrom(object2.getToast().getClass()) || !object2.getToast().getToken().equals(object)) continue;
            return (T)object2.getToast();
        }
        for (Toast toast : this.queued) {
            if (!clazz.isAssignableFrom(toast.getClass()) || !toast.getToken().equals(object)) continue;
            return (T)toast;
        }
        return null;
    }

    public void clear() {
        this.occupiedSlots.clear();
        this.visibleToasts.clear();
        this.queued.clear();
    }

    public void addToast(Toast toast) {
        this.queued.add(toast);
    }

    public void showNowPlayingToast() {
        if (this.nowPlayingToast != null) {
            this.nowPlayingToast.resetToast();
            this.nowPlayingToast.getToast().showToast(this.minecraft.options);
        }
    }

    public void hideNowPlayingToast() {
        if (this.nowPlayingToast != null) {
            this.nowPlayingToast.getToast().setWantedVisibility(Toast.Visibility.HIDE);
        }
    }

    public void createNowPlayingToast() {
        this.nowPlayingToast = new ToastInstance(this, (Toast)new NowPlayingToast(), 0, 0);
    }

    public void removeNowPlayingToast() {
        this.nowPlayingToast = null;
    }

    public Minecraft getMinecraft() {
        return this.minecraft;
    }

    public double getNotificationDisplayTimeMultiplier() {
        return this.minecraft.options.notificationDisplayTime().get();
    }

    class ToastInstance<T extends Toast> {
        private static final long SLIDE_ANIMATION_DURATION_MS = 600L;
        private final T toast;
        final int firstSlotIndex;
        final int occupiedSlotCount;
        private long animationStartTime;
        private long becameFullyVisibleAt;
        Toast.Visibility visibility;
        private long fullyVisibleFor;
        private float visiblePortion;
        protected boolean hasFinishedRendering;
        final /* synthetic */ ToastManager this$0;

        /*
         * WARNING - Possible parameter corruption
         */
        ToastInstance(T t, int n2, int n3) {
            this.this$0 = (ToastManager)n;
            this.toast = t;
            this.firstSlotIndex = n2;
            this.occupiedSlotCount = n3;
            this.resetToast();
        }

        public T getToast() {
            return this.toast;
        }

        public void resetToast() {
            this.animationStartTime = -1L;
            this.becameFullyVisibleAt = -1L;
            this.visibility = Toast.Visibility.HIDE;
            this.fullyVisibleFor = 0L;
            this.visiblePortion = 0.0f;
            this.hasFinishedRendering = false;
        }

        public boolean hasFinishedRendering() {
            return this.hasFinishedRendering;
        }

        private void calculateVisiblePortion(long l) {
            float f = Mth.clamp((float)(l - this.animationStartTime) / 600.0f, 0.0f, 1.0f);
            f *= f;
            this.visiblePortion = this.visibility == Toast.Visibility.HIDE ? 1.0f - f : f;
        }

        public void update() {
            long l = Util.getMillis();
            if (this.animationStartTime == -1L) {
                this.animationStartTime = l;
                this.visibility = Toast.Visibility.SHOW;
            }
            if (this.visibility == Toast.Visibility.SHOW && l - this.animationStartTime <= 600L) {
                this.becameFullyVisibleAt = l;
            }
            this.fullyVisibleFor = l - this.becameFullyVisibleAt;
            this.calculateVisiblePortion(l);
            this.toast.update(this.this$0, this.fullyVisibleFor);
            Toast.Visibility visibility = this.toast.getWantedVisibility();
            if (visibility != this.visibility) {
                this.animationStartTime = l - (long)((int)((1.0f - this.visiblePortion) * 600.0f));
                this.visibility = visibility;
            }
            boolean bl = this.hasFinishedRendering;
            boolean bl2 = this.hasFinishedRendering = this.visibility == Toast.Visibility.HIDE && l - this.animationStartTime > 600L;
            if (this.hasFinishedRendering && !bl) {
                this.toast.onFinishedRendering();
            }
        }

        public void render(GuiGraphics guiGraphics, int n) {
            if (this.hasFinishedRendering) {
                return;
            }
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate(this.toast.xPos(n, this.visiblePortion), this.toast.yPos(this.firstSlotIndex));
            this.toast.render(guiGraphics, this.this$0.minecraft.font, this.fullyVisibleFor);
            guiGraphics.pose().popMatrix();
        }
    }
}

