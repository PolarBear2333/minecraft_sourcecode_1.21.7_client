/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 */
package net.minecraft.client.gui.components;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.BossEvent;

public class BossHealthOverlay {
    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;
    private static final ResourceLocation[] BAR_BACKGROUND_SPRITES = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("boss_bar/pink_background"), ResourceLocation.withDefaultNamespace("boss_bar/blue_background"), ResourceLocation.withDefaultNamespace("boss_bar/red_background"), ResourceLocation.withDefaultNamespace("boss_bar/green_background"), ResourceLocation.withDefaultNamespace("boss_bar/yellow_background"), ResourceLocation.withDefaultNamespace("boss_bar/purple_background"), ResourceLocation.withDefaultNamespace("boss_bar/white_background")};
    private static final ResourceLocation[] BAR_PROGRESS_SPRITES = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("boss_bar/pink_progress"), ResourceLocation.withDefaultNamespace("boss_bar/blue_progress"), ResourceLocation.withDefaultNamespace("boss_bar/red_progress"), ResourceLocation.withDefaultNamespace("boss_bar/green_progress"), ResourceLocation.withDefaultNamespace("boss_bar/yellow_progress"), ResourceLocation.withDefaultNamespace("boss_bar/purple_progress"), ResourceLocation.withDefaultNamespace("boss_bar/white_progress")};
    private static final ResourceLocation[] OVERLAY_BACKGROUND_SPRITES = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("boss_bar/notched_6_background"), ResourceLocation.withDefaultNamespace("boss_bar/notched_10_background"), ResourceLocation.withDefaultNamespace("boss_bar/notched_12_background"), ResourceLocation.withDefaultNamespace("boss_bar/notched_20_background")};
    private static final ResourceLocation[] OVERLAY_PROGRESS_SPRITES = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("boss_bar/notched_6_progress"), ResourceLocation.withDefaultNamespace("boss_bar/notched_10_progress"), ResourceLocation.withDefaultNamespace("boss_bar/notched_12_progress"), ResourceLocation.withDefaultNamespace("boss_bar/notched_20_progress")};
    private final Minecraft minecraft;
    final Map<UUID, LerpingBossEvent> events = Maps.newLinkedHashMap();

    public BossHealthOverlay(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void render(GuiGraphics guiGraphics) {
        if (this.events.isEmpty()) {
            return;
        }
        guiGraphics.nextStratum();
        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("bossHealth");
        int n = guiGraphics.guiWidth();
        int n2 = 12;
        for (LerpingBossEvent lerpingBossEvent : this.events.values()) {
            int n3 = n / 2 - 91;
            int n4 = n2;
            this.drawBar(guiGraphics, n3, n4, lerpingBossEvent);
            Component component = lerpingBossEvent.getName();
            int n5 = this.minecraft.font.width(component);
            int n6 = n / 2 - n5 / 2;
            int n7 = n4 - 9;
            guiGraphics.drawString(this.minecraft.font, component, n6, n7, -1);
            if ((n2 += 10 + this.minecraft.font.lineHeight) < guiGraphics.guiHeight() / 3) continue;
            break;
        }
        profilerFiller.pop();
    }

    private void drawBar(GuiGraphics guiGraphics, int n, int n2, BossEvent bossEvent) {
        this.drawBar(guiGraphics, n, n2, bossEvent, 182, BAR_BACKGROUND_SPRITES, OVERLAY_BACKGROUND_SPRITES);
        int n3 = Mth.lerpDiscrete(bossEvent.getProgress(), 0, 182);
        if (n3 > 0) {
            this.drawBar(guiGraphics, n, n2, bossEvent, n3, BAR_PROGRESS_SPRITES, OVERLAY_PROGRESS_SPRITES);
        }
    }

    private void drawBar(GuiGraphics guiGraphics, int n, int n2, BossEvent bossEvent, int n3, ResourceLocation[] resourceLocationArray, ResourceLocation[] resourceLocationArray2) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocationArray[bossEvent.getColor().ordinal()], 182, 5, 0, 0, n, n2, n3, 5);
        if (bossEvent.getOverlay() != BossEvent.BossBarOverlay.PROGRESS) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocationArray2[bossEvent.getOverlay().ordinal() - 1], 182, 5, 0, 0, n, n2, n3, 5);
        }
    }

    public void update(ClientboundBossEventPacket clientboundBossEventPacket) {
        clientboundBossEventPacket.dispatch(new ClientboundBossEventPacket.Handler(){

            @Override
            public void add(UUID uUID, Component component, float f, BossEvent.BossBarColor bossBarColor, BossEvent.BossBarOverlay bossBarOverlay, boolean bl, boolean bl2, boolean bl3) {
                BossHealthOverlay.this.events.put(uUID, new LerpingBossEvent(uUID, component, f, bossBarColor, bossBarOverlay, bl, bl2, bl3));
            }

            @Override
            public void remove(UUID uUID) {
                BossHealthOverlay.this.events.remove(uUID);
            }

            @Override
            public void updateProgress(UUID uUID, float f) {
                BossHealthOverlay.this.events.get(uUID).setProgress(f);
            }

            @Override
            public void updateName(UUID uUID, Component component) {
                BossHealthOverlay.this.events.get(uUID).setName(component);
            }

            @Override
            public void updateStyle(UUID uUID, BossEvent.BossBarColor bossBarColor, BossEvent.BossBarOverlay bossBarOverlay) {
                LerpingBossEvent lerpingBossEvent = BossHealthOverlay.this.events.get(uUID);
                lerpingBossEvent.setColor(bossBarColor);
                lerpingBossEvent.setOverlay(bossBarOverlay);
            }

            @Override
            public void updateProperties(UUID uUID, boolean bl, boolean bl2, boolean bl3) {
                LerpingBossEvent lerpingBossEvent = BossHealthOverlay.this.events.get(uUID);
                lerpingBossEvent.setDarkenScreen(bl);
                lerpingBossEvent.setPlayBossMusic(bl2);
                lerpingBossEvent.setCreateWorldFog(bl3);
            }
        });
    }

    public void reset() {
        this.events.clear();
    }

    public boolean shouldPlayMusic() {
        if (!this.events.isEmpty()) {
            for (BossEvent bossEvent : this.events.values()) {
                if (!bossEvent.shouldPlayBossMusic()) continue;
                return true;
            }
        }
        return false;
    }

    public boolean shouldDarkenScreen() {
        if (!this.events.isEmpty()) {
            for (BossEvent bossEvent : this.events.values()) {
                if (!bossEvent.shouldDarkenScreen()) continue;
                return true;
            }
        }
        return false;
    }

    public boolean shouldCreateWorldFog() {
        if (!this.events.isEmpty()) {
            for (BossEvent bossEvent : this.events.values()) {
                if (!bossEvent.shouldCreateWorldFog()) continue;
                return true;
            }
        }
        return false;
    }
}

