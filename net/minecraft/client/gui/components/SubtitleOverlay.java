/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.audio.ListenerTransform;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEventListener;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class SubtitleOverlay
implements SoundEventListener {
    private static final long DISPLAY_TIME = 3000L;
    private final Minecraft minecraft;
    private final List<Subtitle> subtitles = Lists.newArrayList();
    private boolean isListening;
    private final List<Subtitle> audibleSubtitles = new ArrayList<Subtitle>();

    public SubtitleOverlay(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void render(GuiGraphics guiGraphics) {
        SoundManager soundManager = this.minecraft.getSoundManager();
        if (!this.isListening && this.minecraft.options.showSubtitles().get().booleanValue()) {
            soundManager.addListener(this);
            this.isListening = true;
        } else if (this.isListening && !this.minecraft.options.showSubtitles().get().booleanValue()) {
            soundManager.removeListener(this);
            this.isListening = false;
        }
        if (!this.isListening) {
            return;
        }
        ListenerTransform listenerTransform = soundManager.getListenerTransform();
        Vec3 vec3 = listenerTransform.position();
        Vec3 vec32 = listenerTransform.forward();
        Vec3 vec33 = listenerTransform.right();
        this.audibleSubtitles.clear();
        for (Subtitle subtitle : this.subtitles) {
            if (!subtitle.isAudibleFrom(vec3)) continue;
            this.audibleSubtitles.add(subtitle);
        }
        if (this.audibleSubtitles.isEmpty()) {
            return;
        }
        int n = 0;
        int n2 = 0;
        double d = this.minecraft.options.notificationDisplayTime().get();
        Iterator<Subtitle> iterator = this.audibleSubtitles.iterator();
        while (iterator.hasNext()) {
            Subtitle subtitle = iterator.next();
            subtitle.purgeOldInstances(3000.0 * d);
            if (!subtitle.isStillActive()) {
                iterator.remove();
                continue;
            }
            n2 = Math.max(n2, this.minecraft.font.width(subtitle.getText()));
        }
        n2 += this.minecraft.font.width("<") + this.minecraft.font.width(" ") + this.minecraft.font.width(">") + this.minecraft.font.width(" ");
        if (!this.audibleSubtitles.isEmpty()) {
            guiGraphics.nextStratum();
        }
        for (Subtitle subtitle : this.audibleSubtitles) {
            int n3 = 255;
            Component component = subtitle.getText();
            SoundPlayedAt soundPlayedAt = subtitle.getClosest(vec3);
            if (soundPlayedAt == null) continue;
            Vec3 vec34 = soundPlayedAt.location.subtract(vec3).normalize();
            double d2 = vec33.dot(vec34);
            double d3 = vec32.dot(vec34);
            boolean bl = d3 > 0.5;
            int n4 = n2 / 2;
            int n5 = this.minecraft.font.lineHeight;
            int n6 = n5 / 2;
            float f = 1.0f;
            int n7 = this.minecraft.font.width(component);
            int n8 = Mth.floor(Mth.clampedLerp(255.0f, 75.0f, (float)(Util.getMillis() - soundPlayedAt.time) / (float)(3000.0 * d)));
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate((float)guiGraphics.guiWidth() - (float)n4 * 1.0f - 2.0f, (float)(guiGraphics.guiHeight() - 35) - (float)(n * (n5 + 1)) * 1.0f);
            guiGraphics.pose().scale(1.0f, 1.0f);
            guiGraphics.fill(-n4 - 1, -n6 - 1, n4 + 1, n6 + 1, this.minecraft.options.getBackgroundColor(0.8f));
            int n9 = ARGB.color(255, n8, n8, n8);
            if (!bl) {
                if (d2 > 0.0) {
                    guiGraphics.drawString(this.minecraft.font, ">", n4 - this.minecraft.font.width(">"), -n6, n9);
                } else if (d2 < 0.0) {
                    guiGraphics.drawString(this.minecraft.font, "<", -n4, -n6, n9);
                }
            }
            guiGraphics.drawString(this.minecraft.font, component, -n7 / 2, -n6, n9);
            guiGraphics.pose().popMatrix();
            ++n;
        }
    }

    @Override
    public void onPlaySound(SoundInstance soundInstance, WeighedSoundEvents weighedSoundEvents, float f) {
        if (weighedSoundEvents.getSubtitle() == null) {
            return;
        }
        Component component = weighedSoundEvents.getSubtitle();
        if (!this.subtitles.isEmpty()) {
            for (Subtitle subtitle : this.subtitles) {
                if (!subtitle.getText().equals(component)) continue;
                subtitle.refresh(new Vec3(soundInstance.getX(), soundInstance.getY(), soundInstance.getZ()));
                return;
            }
        }
        this.subtitles.add(new Subtitle(component, f, new Vec3(soundInstance.getX(), soundInstance.getY(), soundInstance.getZ())));
    }

    static class Subtitle {
        private final Component text;
        private final float range;
        private final List<SoundPlayedAt> playedAt = new ArrayList<SoundPlayedAt>();

        public Subtitle(Component component, float f, Vec3 vec3) {
            this.text = component;
            this.range = f;
            this.playedAt.add(new SoundPlayedAt(vec3, Util.getMillis()));
        }

        public Component getText() {
            return this.text;
        }

        @Nullable
        public SoundPlayedAt getClosest(Vec3 vec3) {
            if (this.playedAt.isEmpty()) {
                return null;
            }
            if (this.playedAt.size() == 1) {
                return this.playedAt.getFirst();
            }
            return this.playedAt.stream().min(Comparator.comparingDouble(soundPlayedAt -> soundPlayedAt.location().distanceTo(vec3))).orElse(null);
        }

        public void refresh(Vec3 vec3) {
            this.playedAt.removeIf(soundPlayedAt -> vec3.equals(soundPlayedAt.location()));
            this.playedAt.add(new SoundPlayedAt(vec3, Util.getMillis()));
        }

        public boolean isAudibleFrom(Vec3 vec3) {
            if (Float.isInfinite(this.range)) {
                return true;
            }
            if (this.playedAt.isEmpty()) {
                return false;
            }
            SoundPlayedAt soundPlayedAt = this.getClosest(vec3);
            if (soundPlayedAt == null) {
                return false;
            }
            return vec3.closerThan(soundPlayedAt.location, this.range);
        }

        public void purgeOldInstances(double d) {
            long l = Util.getMillis();
            this.playedAt.removeIf(soundPlayedAt -> (double)(l - soundPlayedAt.time()) > d);
        }

        public boolean isStillActive() {
            return !this.playedAt.isEmpty();
        }
    }

    static final class SoundPlayedAt
    extends Record {
        final Vec3 location;
        final long time;

        SoundPlayedAt(Vec3 vec3, long l) {
            this.location = vec3;
            this.time = l;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{SoundPlayedAt.class, "location;time", "location", "time"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{SoundPlayedAt.class, "location;time", "location", "time"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{SoundPlayedAt.class, "location;time", "location", "time"}, this, object);
        }

        public Vec3 location() {
            return this.location;
        }

        public long time() {
            return this.time;
        }
    }
}

