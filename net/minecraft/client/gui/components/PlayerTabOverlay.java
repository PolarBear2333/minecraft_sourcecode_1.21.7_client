/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.components;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class PlayerTabOverlay {
    private static final ResourceLocation PING_UNKNOWN_SPRITE = ResourceLocation.withDefaultNamespace("icon/ping_unknown");
    private static final ResourceLocation PING_1_SPRITE = ResourceLocation.withDefaultNamespace("icon/ping_1");
    private static final ResourceLocation PING_2_SPRITE = ResourceLocation.withDefaultNamespace("icon/ping_2");
    private static final ResourceLocation PING_3_SPRITE = ResourceLocation.withDefaultNamespace("icon/ping_3");
    private static final ResourceLocation PING_4_SPRITE = ResourceLocation.withDefaultNamespace("icon/ping_4");
    private static final ResourceLocation PING_5_SPRITE = ResourceLocation.withDefaultNamespace("icon/ping_5");
    private static final ResourceLocation HEART_CONTAINER_BLINKING_SPRITE = ResourceLocation.withDefaultNamespace("hud/heart/container_blinking");
    private static final ResourceLocation HEART_CONTAINER_SPRITE = ResourceLocation.withDefaultNamespace("hud/heart/container");
    private static final ResourceLocation HEART_FULL_BLINKING_SPRITE = ResourceLocation.withDefaultNamespace("hud/heart/full_blinking");
    private static final ResourceLocation HEART_HALF_BLINKING_SPRITE = ResourceLocation.withDefaultNamespace("hud/heart/half_blinking");
    private static final ResourceLocation HEART_ABSORBING_FULL_BLINKING_SPRITE = ResourceLocation.withDefaultNamespace("hud/heart/absorbing_full_blinking");
    private static final ResourceLocation HEART_FULL_SPRITE = ResourceLocation.withDefaultNamespace("hud/heart/full");
    private static final ResourceLocation HEART_ABSORBING_HALF_BLINKING_SPRITE = ResourceLocation.withDefaultNamespace("hud/heart/absorbing_half_blinking");
    private static final ResourceLocation HEART_HALF_SPRITE = ResourceLocation.withDefaultNamespace("hud/heart/half");
    private static final Comparator<PlayerInfo> PLAYER_COMPARATOR = Comparator.comparingInt(playerInfo -> -playerInfo.getTabListOrder()).thenComparingInt(playerInfo -> playerInfo.getGameMode() == GameType.SPECTATOR ? 1 : 0).thenComparing(playerInfo -> Optionull.mapOrDefault(playerInfo.getTeam(), PlayerTeam::getName, "")).thenComparing(playerInfo -> playerInfo.getProfile().getName(), String::compareToIgnoreCase);
    public static final int MAX_ROWS_PER_COL = 20;
    private final Minecraft minecraft;
    private final Gui gui;
    @Nullable
    private Component footer;
    @Nullable
    private Component header;
    private boolean visible;
    private final Map<UUID, HealthState> healthStates = new Object2ObjectOpenHashMap();

    public PlayerTabOverlay(Minecraft minecraft, Gui gui) {
        this.minecraft = minecraft;
        this.gui = gui;
    }

    public Component getNameForDisplay(PlayerInfo playerInfo) {
        if (playerInfo.getTabListDisplayName() != null) {
            return this.decorateName(playerInfo, playerInfo.getTabListDisplayName().copy());
        }
        return this.decorateName(playerInfo, PlayerTeam.formatNameForTeam(playerInfo.getTeam(), Component.literal(playerInfo.getProfile().getName())));
    }

    private Component decorateName(PlayerInfo playerInfo, MutableComponent mutableComponent) {
        return playerInfo.getGameMode() == GameType.SPECTATOR ? mutableComponent.withStyle(ChatFormatting.ITALIC) : mutableComponent;
    }

    public void setVisible(boolean bl) {
        if (this.visible != bl) {
            this.healthStates.clear();
            this.visible = bl;
            if (bl) {
                MutableComponent mutableComponent = ComponentUtils.formatList(this.getPlayerInfos(), Component.literal(", "), this::getNameForDisplay);
                this.minecraft.getNarrator().saySystemNow(Component.translatable("multiplayer.player.list.narration", mutableComponent));
            }
        }
    }

    private List<PlayerInfo> getPlayerInfos() {
        return this.minecraft.player.connection.getListedOnlinePlayers().stream().sorted(PLAYER_COMPARATOR).limit(80L).toList();
    }

    public void render(GuiGraphics guiGraphics, int n, Scoreboard scoreboard, @Nullable Objective objective) {
        int n2;
        int n3;
        int n4;
        int n5;
        int n6;
        List<PlayerInfo> list = this.getPlayerInfos();
        ArrayList<ScoreDisplayEntry> arrayList = new ArrayList<ScoreDisplayEntry>(list.size());
        int n7 = this.minecraft.font.width(" ");
        int n8 = 0;
        int n9 = 0;
        for (PlayerInfo playerInfo2 : list) {
            Component component = this.getNameForDisplay(playerInfo2);
            n8 = Math.max(n8, this.minecraft.font.width(component));
            n6 = 0;
            MutableComponent mutableComponent = null;
            n5 = 0;
            if (objective != null) {
                ScoreHolder scoreHolder = ScoreHolder.fromGameProfile(playerInfo2.getProfile());
                ReadOnlyScoreInfo readOnlyScoreInfo = scoreboard.getPlayerScoreInfo(scoreHolder, objective);
                if (readOnlyScoreInfo != null) {
                    n6 = readOnlyScoreInfo.value();
                }
                if (objective.getRenderType() != ObjectiveCriteria.RenderType.HEARTS) {
                    NumberFormat numberFormat = objective.numberFormatOrDefault(StyledFormat.PLAYER_LIST_DEFAULT);
                    mutableComponent = ReadOnlyScoreInfo.safeFormatValue(readOnlyScoreInfo, numberFormat);
                    n5 = this.minecraft.font.width(mutableComponent);
                    n9 = Math.max(n9, n5 > 0 ? n7 + n5 : 0);
                }
            }
            arrayList.add(new ScoreDisplayEntry(component, n6, mutableComponent, n5));
        }
        if (!this.healthStates.isEmpty()) {
            Set set = list.stream().map(playerInfo -> playerInfo.getProfile().getId()).collect(Collectors.toSet());
            this.healthStates.keySet().removeIf(uUID -> !set.contains(uUID));
        }
        int n10 = n4 = list.size();
        int n11 = 1;
        while (n10 > 20) {
            n10 = (n4 + ++n11 - 1) / n11;
        }
        int n12 = n6 = this.minecraft.isLocalServer() || this.minecraft.getConnection().getConnection().isEncrypted() ? 1 : 0;
        int n13 = objective != null ? (objective.getRenderType() == ObjectiveCriteria.RenderType.HEARTS ? 90 : n9) : 0;
        n5 = Math.min(n11 * ((n6 != 0 ? 9 : 0) + n8 + n13 + 13), n - 50) / n11;
        int n14 = n / 2 - (n5 * n11 + (n11 - 1) * 5) / 2;
        int n15 = 10;
        int n16 = n5 * n11 + (n11 - 1) * 5;
        List<FormattedCharSequence> list2 = null;
        if (this.header != null) {
            list2 = this.minecraft.font.split(this.header, n - 50);
            for (FormattedCharSequence iterator : list2) {
                n16 = Math.max(n16, this.minecraft.font.width(iterator));
            }
        }
        Object object = null;
        if (this.footer != null) {
            object = this.minecraft.font.split(this.footer, n - 50);
            Iterator n17 = object.iterator();
            while (n17.hasNext()) {
                FormattedCharSequence formattedCharSequence = (FormattedCharSequence)n17.next();
                n16 = Math.max(n16, this.minecraft.font.width(formattedCharSequence));
            }
        }
        if (list2 != null) {
            guiGraphics.fill(n / 2 - n16 / 2 - 1, n15 - 1, n / 2 + n16 / 2 + 1, n15 + list2.size() * this.minecraft.font.lineHeight, Integer.MIN_VALUE);
            for (FormattedCharSequence formattedCharSequence : list2) {
                n3 = this.minecraft.font.width(formattedCharSequence);
                guiGraphics.drawString(this.minecraft.font, formattedCharSequence, n / 2 - n3 / 2, n15, -1);
                n15 += this.minecraft.font.lineHeight;
            }
            ++n15;
        }
        guiGraphics.fill(n / 2 - n16 / 2 - 1, n15 - 1, n / 2 + n16 / 2 + 1, n15 + n10 * 9, Integer.MIN_VALUE);
        int n17 = this.minecraft.options.getBackgroundColor(0x20FFFFFF);
        for (int i = 0; i < n4; ++i) {
            int n18;
            int n19;
            n3 = i / n10;
            n2 = i % n10;
            int n20 = n14 + n3 * n5 + n3 * 5;
            int n21 = n15 + n2 * 9;
            guiGraphics.fill(n20, n21, n20 + n5, n21 + 8, n17);
            if (i >= list.size()) continue;
            PlayerInfo playerInfo2 = list.get(i);
            ScoreDisplayEntry scoreDisplayEntry = (ScoreDisplayEntry)arrayList.get(i);
            GameProfile gameProfile = playerInfo2.getProfile();
            if (n6 != 0) {
                Player player = this.minecraft.level.getPlayerByUUID(gameProfile.getId());
                n19 = player != null && LivingEntityRenderer.isEntityUpsideDown(player) ? 1 : 0;
                PlayerFaceRenderer.draw(guiGraphics, playerInfo2.getSkin().texture(), n20, n21, 8, playerInfo2.showHat(), n19 != 0, -1);
                n20 += 9;
            }
            guiGraphics.drawString(this.minecraft.font, scoreDisplayEntry.name, n20, n21, playerInfo2.getGameMode() == GameType.SPECTATOR ? -1862270977 : -1);
            if (objective != null && playerInfo2.getGameMode() != GameType.SPECTATOR && (n19 = (n18 = n20 + n8 + 1) + n13) - n18 > 5) {
                this.renderTablistScore(objective, n21, scoreDisplayEntry, n18, n19, gameProfile.getId(), guiGraphics);
            }
            this.renderPingIcon(guiGraphics, n5, n20 - (n6 != 0 ? 9 : 0), n21, playerInfo2);
        }
        if (object != null) {
            guiGraphics.fill(n / 2 - n16 / 2 - 1, (n15 += n10 * 9 + 1) - 1, n / 2 + n16 / 2 + 1, n15 + object.size() * this.minecraft.font.lineHeight, Integer.MIN_VALUE);
            Iterator iterator = object.iterator();
            while (iterator.hasNext()) {
                FormattedCharSequence formattedCharSequence = (FormattedCharSequence)iterator.next();
                n2 = this.minecraft.font.width(formattedCharSequence);
                guiGraphics.drawString(this.minecraft.font, formattedCharSequence, n / 2 - n2 / 2, n15, -1);
                n15 += this.minecraft.font.lineHeight;
            }
        }
    }

    protected void renderPingIcon(GuiGraphics guiGraphics, int n, int n2, int n3, PlayerInfo playerInfo) {
        ResourceLocation resourceLocation = playerInfo.getLatency() < 0 ? PING_UNKNOWN_SPRITE : (playerInfo.getLatency() < 150 ? PING_5_SPRITE : (playerInfo.getLatency() < 300 ? PING_4_SPRITE : (playerInfo.getLatency() < 600 ? PING_3_SPRITE : (playerInfo.getLatency() < 1000 ? PING_2_SPRITE : PING_1_SPRITE))));
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation, n2 + n - 11, n3, 10, 8);
    }

    private void renderTablistScore(Objective objective, int n, ScoreDisplayEntry scoreDisplayEntry, int n2, int n3, UUID uUID, GuiGraphics guiGraphics) {
        if (objective.getRenderType() == ObjectiveCriteria.RenderType.HEARTS) {
            this.renderTablistHearts(n, n2, n3, uUID, guiGraphics, scoreDisplayEntry.score);
        } else if (scoreDisplayEntry.formattedScore != null) {
            guiGraphics.drawString(this.minecraft.font, scoreDisplayEntry.formattedScore, n3 - scoreDisplayEntry.scoreWidth, n, -1);
        }
    }

    private void renderTablistHearts(int n, int n2, int n3, UUID uUID2, GuiGraphics guiGraphics, int n4) {
        int n5;
        HealthState healthState = this.healthStates.computeIfAbsent(uUID2, uUID -> new HealthState(n4));
        healthState.update(n4, this.gui.getGuiTicks());
        int n6 = Mth.positiveCeilDiv(Math.max(n4, healthState.displayedValue()), 2);
        int n7 = Math.max(n4, Math.max(healthState.displayedValue(), 20)) / 2;
        boolean bl = healthState.isBlinking(this.gui.getGuiTicks());
        if (n6 <= 0) {
            return;
        }
        int n8 = Mth.floor(Math.min((float)(n3 - n2 - 4) / (float)n7, 9.0f));
        if (n8 <= 3) {
            float f = Mth.clamp((float)n4 / 20.0f, 0.0f, 1.0f);
            int n9 = (int)((1.0f - f) * 255.0f) << 16 | (int)(f * 255.0f) << 8;
            float f2 = (float)n4 / 2.0f;
            MutableComponent mutableComponent = Component.translatable("multiplayer.player.list.hp", Float.valueOf(f2));
            MutableComponent mutableComponent2 = n3 - this.minecraft.font.width(mutableComponent) >= n2 ? mutableComponent : Component.literal(Float.toString(f2));
            guiGraphics.drawString(this.minecraft.font, mutableComponent2, (n3 + n2 - this.minecraft.font.width(mutableComponent2)) / 2, n, ARGB.opaque(n9));
            return;
        }
        ResourceLocation resourceLocation = bl ? HEART_CONTAINER_BLINKING_SPRITE : HEART_CONTAINER_SPRITE;
        for (n5 = n6; n5 < n7; ++n5) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation, n2 + n5 * n8, n, 9, 9);
        }
        for (n5 = 0; n5 < n6; ++n5) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation, n2 + n5 * n8, n, 9, 9);
            if (bl) {
                if (n5 * 2 + 1 < healthState.displayedValue()) {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, HEART_FULL_BLINKING_SPRITE, n2 + n5 * n8, n, 9, 9);
                }
                if (n5 * 2 + 1 == healthState.displayedValue()) {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, HEART_HALF_BLINKING_SPRITE, n2 + n5 * n8, n, 9, 9);
                }
            }
            if (n5 * 2 + 1 < n4) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, n5 >= 10 ? HEART_ABSORBING_FULL_BLINKING_SPRITE : HEART_FULL_SPRITE, n2 + n5 * n8, n, 9, 9);
            }
            if (n5 * 2 + 1 != n4) continue;
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, n5 >= 10 ? HEART_ABSORBING_HALF_BLINKING_SPRITE : HEART_HALF_SPRITE, n2 + n5 * n8, n, 9, 9);
        }
    }

    public void setFooter(@Nullable Component component) {
        this.footer = component;
    }

    public void setHeader(@Nullable Component component) {
        this.header = component;
    }

    public void reset() {
        this.header = null;
        this.footer = null;
    }

    static final class ScoreDisplayEntry
    extends Record {
        final Component name;
        final int score;
        @Nullable
        final Component formattedScore;
        final int scoreWidth;

        ScoreDisplayEntry(Component component, int n, @Nullable Component component2, int n2) {
            this.name = component;
            this.score = n;
            this.formattedScore = component2;
            this.scoreWidth = n2;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ScoreDisplayEntry.class, "name;score;formattedScore;scoreWidth", "name", "score", "formattedScore", "scoreWidth"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ScoreDisplayEntry.class, "name;score;formattedScore;scoreWidth", "name", "score", "formattedScore", "scoreWidth"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ScoreDisplayEntry.class, "name;score;formattedScore;scoreWidth", "name", "score", "formattedScore", "scoreWidth"}, this, object);
        }

        public Component name() {
            return this.name;
        }

        public int score() {
            return this.score;
        }

        @Nullable
        public Component formattedScore() {
            return this.formattedScore;
        }

        public int scoreWidth() {
            return this.scoreWidth;
        }
    }

    static class HealthState {
        private static final long DISPLAY_UPDATE_DELAY = 20L;
        private static final long DECREASE_BLINK_DURATION = 20L;
        private static final long INCREASE_BLINK_DURATION = 10L;
        private int lastValue;
        private int displayedValue;
        private long lastUpdateTick;
        private long blinkUntilTick;

        public HealthState(int n) {
            this.displayedValue = n;
            this.lastValue = n;
        }

        public void update(int n, long l) {
            if (n != this.lastValue) {
                long l2 = n < this.lastValue ? 20L : 10L;
                this.blinkUntilTick = l + l2;
                this.lastValue = n;
                this.lastUpdateTick = l;
            }
            if (l - this.lastUpdateTick > 20L) {
                this.displayedValue = n;
            }
        }

        public int displayedValue() {
            return this.displayedValue;
        }

        public boolean isBlinking(long l) {
            return this.blinkUntilTick > l && (this.blinkUntilTick - l) % 6L >= 3L;
        }
    }
}

