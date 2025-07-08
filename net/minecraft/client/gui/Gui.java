/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Ordering
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.tuple.Pair
 */
package net.minecraft.client.gui;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Ordering;
import com.mojang.blaze3d.platform.Window;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Optionull;
import net.minecraft.Util;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.CameraType;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.gui.contextualbar.ContextualBarRenderer;
import net.minecraft.client.gui.contextualbar.ExperienceBarRenderer;
import net.minecraft.client.gui.contextualbar.JumpableVehicleBarRenderer;
import net.minecraft.client.gui.contextualbar.LocatorBarRenderer;
import net.minecraft.client.gui.screens.ReceivingLevelScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import org.apache.commons.lang3.tuple.Pair;

public class Gui {
    private static final ResourceLocation CROSSHAIR_SPRITE = ResourceLocation.withDefaultNamespace("hud/crosshair");
    private static final ResourceLocation CROSSHAIR_ATTACK_INDICATOR_FULL_SPRITE = ResourceLocation.withDefaultNamespace("hud/crosshair_attack_indicator_full");
    private static final ResourceLocation CROSSHAIR_ATTACK_INDICATOR_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("hud/crosshair_attack_indicator_background");
    private static final ResourceLocation CROSSHAIR_ATTACK_INDICATOR_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("hud/crosshair_attack_indicator_progress");
    private static final ResourceLocation EFFECT_BACKGROUND_AMBIENT_SPRITE = ResourceLocation.withDefaultNamespace("hud/effect_background_ambient");
    private static final ResourceLocation EFFECT_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("hud/effect_background");
    private static final ResourceLocation HOTBAR_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar");
    private static final ResourceLocation HOTBAR_SELECTION_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar_selection");
    private static final ResourceLocation HOTBAR_OFFHAND_LEFT_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar_offhand_left");
    private static final ResourceLocation HOTBAR_OFFHAND_RIGHT_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar_offhand_right");
    private static final ResourceLocation HOTBAR_ATTACK_INDICATOR_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar_attack_indicator_background");
    private static final ResourceLocation HOTBAR_ATTACK_INDICATOR_PROGRESS_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar_attack_indicator_progress");
    private static final ResourceLocation ARMOR_EMPTY_SPRITE = ResourceLocation.withDefaultNamespace("hud/armor_empty");
    private static final ResourceLocation ARMOR_HALF_SPRITE = ResourceLocation.withDefaultNamespace("hud/armor_half");
    private static final ResourceLocation ARMOR_FULL_SPRITE = ResourceLocation.withDefaultNamespace("hud/armor_full");
    private static final ResourceLocation FOOD_EMPTY_HUNGER_SPRITE = ResourceLocation.withDefaultNamespace("hud/food_empty_hunger");
    private static final ResourceLocation FOOD_HALF_HUNGER_SPRITE = ResourceLocation.withDefaultNamespace("hud/food_half_hunger");
    private static final ResourceLocation FOOD_FULL_HUNGER_SPRITE = ResourceLocation.withDefaultNamespace("hud/food_full_hunger");
    private static final ResourceLocation FOOD_EMPTY_SPRITE = ResourceLocation.withDefaultNamespace("hud/food_empty");
    private static final ResourceLocation FOOD_HALF_SPRITE = ResourceLocation.withDefaultNamespace("hud/food_half");
    private static final ResourceLocation FOOD_FULL_SPRITE = ResourceLocation.withDefaultNamespace("hud/food_full");
    private static final ResourceLocation AIR_SPRITE = ResourceLocation.withDefaultNamespace("hud/air");
    private static final ResourceLocation AIR_POPPING_SPRITE = ResourceLocation.withDefaultNamespace("hud/air_bursting");
    private static final ResourceLocation AIR_EMPTY_SPRITE = ResourceLocation.withDefaultNamespace("hud/air_empty");
    private static final ResourceLocation HEART_VEHICLE_CONTAINER_SPRITE = ResourceLocation.withDefaultNamespace("hud/heart/vehicle_container");
    private static final ResourceLocation HEART_VEHICLE_FULL_SPRITE = ResourceLocation.withDefaultNamespace("hud/heart/vehicle_full");
    private static final ResourceLocation HEART_VEHICLE_HALF_SPRITE = ResourceLocation.withDefaultNamespace("hud/heart/vehicle_half");
    private static final ResourceLocation VIGNETTE_LOCATION = ResourceLocation.withDefaultNamespace("textures/misc/vignette.png");
    public static final ResourceLocation NAUSEA_LOCATION = ResourceLocation.withDefaultNamespace("textures/misc/nausea.png");
    private static final ResourceLocation SPYGLASS_SCOPE_LOCATION = ResourceLocation.withDefaultNamespace("textures/misc/spyglass_scope.png");
    private static final ResourceLocation POWDER_SNOW_OUTLINE_LOCATION = ResourceLocation.withDefaultNamespace("textures/misc/powder_snow_outline.png");
    private static final Comparator<PlayerScoreEntry> SCORE_DISPLAY_ORDER = Comparator.comparing(PlayerScoreEntry::value).reversed().thenComparing(PlayerScoreEntry::owner, String.CASE_INSENSITIVE_ORDER);
    private static final Component DEMO_EXPIRED_TEXT = Component.translatable("demo.demoExpired");
    private static final Component SAVING_TEXT = Component.translatable("menu.savingLevel");
    private static final float MIN_CROSSHAIR_ATTACK_SPEED = 5.0f;
    private static final int EXPERIENCE_BAR_DISPLAY_TICKS = 100;
    private static final int NUM_HEARTS_PER_ROW = 10;
    private static final int LINE_HEIGHT = 10;
    private static final String SPACER = ": ";
    private static final float PORTAL_OVERLAY_ALPHA_MIN = 0.2f;
    private static final int HEART_SIZE = 9;
    private static final int HEART_SEPARATION = 8;
    private static final int NUM_AIR_BUBBLES = 10;
    private static final int AIR_BUBBLE_SIZE = 9;
    private static final int AIR_BUBBLE_SEPERATION = 8;
    private static final int AIR_BUBBLE_POPPING_DURATION = 2;
    private static final int EMPTY_AIR_BUBBLE_DELAY_DURATION = 1;
    private static final float AIR_BUBBLE_POP_SOUND_VOLUME_BASE = 0.5f;
    private static final float AIR_BUBBLE_POP_SOUND_VOLUME_INCREMENT = 0.1f;
    private static final float AIR_BUBBLE_POP_SOUND_PITCH_BASE = 1.0f;
    private static final float AIR_BUBBLE_POP_SOUND_PITCH_INCREMENT = 0.1f;
    private static final int NUM_AIR_BUBBLE_POPPED_BEFORE_SOUND_VOLUME_INCREASE = 3;
    private static final int NUM_AIR_BUBBLE_POPPED_BEFORE_SOUND_PITCH_INCREASE = 5;
    private static final float AUTOSAVE_FADE_SPEED_FACTOR = 0.2f;
    private static final int SAVING_INDICATOR_WIDTH_PADDING_RIGHT = 5;
    private static final int SAVING_INDICATOR_HEIGHT_PADDING_BOTTOM = 5;
    private final RandomSource random = RandomSource.create();
    private final Minecraft minecraft;
    private final ChatComponent chat;
    private int tickCount;
    @Nullable
    private Component overlayMessageString;
    private int overlayMessageTime;
    private boolean animateOverlayMessageColor;
    private boolean chatDisabledByPlayerShown;
    public float vignetteBrightness = 1.0f;
    private int toolHighlightTimer;
    private ItemStack lastToolHighlight = ItemStack.EMPTY;
    private final DebugScreenOverlay debugOverlay;
    private final SubtitleOverlay subtitleOverlay;
    private final SpectatorGui spectatorGui;
    private final PlayerTabOverlay tabList;
    private final BossHealthOverlay bossOverlay;
    private int titleTime;
    @Nullable
    private Component title;
    @Nullable
    private Component subtitle;
    private int titleFadeInTime;
    private int titleStayTime;
    private int titleFadeOutTime;
    private int lastHealth;
    private int displayHealth;
    private long lastHealthTime;
    private long healthBlinkTime;
    private int lastBubblePopSoundPlayed;
    private float autosaveIndicatorValue;
    private float lastAutosaveIndicatorValue;
    private Pair<ContextualInfo, ContextualBarRenderer> contextualInfoBar = Pair.of((Object)((Object)ContextualInfo.EMPTY), (Object)ContextualBarRenderer.EMPTY);
    private final Map<ContextualInfo, Supplier<ContextualBarRenderer>> contextualInfoBarRenderers;
    private float scopeScale;

    public Gui(Minecraft minecraft) {
        this.minecraft = minecraft;
        this.debugOverlay = new DebugScreenOverlay(minecraft);
        this.spectatorGui = new SpectatorGui(minecraft);
        this.chat = new ChatComponent(minecraft);
        this.tabList = new PlayerTabOverlay(minecraft, this);
        this.bossOverlay = new BossHealthOverlay(minecraft);
        this.subtitleOverlay = new SubtitleOverlay(minecraft);
        this.contextualInfoBarRenderers = ImmutableMap.of((Object)((Object)ContextualInfo.EMPTY), () -> ContextualBarRenderer.EMPTY, (Object)((Object)ContextualInfo.EXPERIENCE), () -> new ExperienceBarRenderer(minecraft), (Object)((Object)ContextualInfo.LOCATOR), () -> new LocatorBarRenderer(minecraft), (Object)((Object)ContextualInfo.JUMPABLE_VEHICLE), () -> new JumpableVehicleBarRenderer(minecraft));
        this.resetTitleTimes();
    }

    public void resetTitleTimes() {
        this.titleFadeInTime = 10;
        this.titleStayTime = 70;
        this.titleFadeOutTime = 20;
    }

    public void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (this.minecraft.screen != null && this.minecraft.screen instanceof ReceivingLevelScreen) {
            return;
        }
        if (!this.minecraft.options.hideGui) {
            this.renderCameraOverlays(guiGraphics, deltaTracker);
            this.renderCrosshair(guiGraphics, deltaTracker);
            guiGraphics.nextStratum();
            this.renderHotbarAndDecorations(guiGraphics, deltaTracker);
            this.renderEffects(guiGraphics, deltaTracker);
            this.renderBossOverlay(guiGraphics, deltaTracker);
        }
        this.renderSleepOverlay(guiGraphics, deltaTracker);
        if (!this.minecraft.options.hideGui) {
            this.renderDemoOverlay(guiGraphics, deltaTracker);
            this.renderDebugOverlay(guiGraphics, deltaTracker);
            this.renderScoreboardSidebar(guiGraphics, deltaTracker);
            this.renderOverlayMessage(guiGraphics, deltaTracker);
            this.renderTitle(guiGraphics, deltaTracker);
            this.renderChat(guiGraphics, deltaTracker);
            this.renderTabList(guiGraphics, deltaTracker);
            this.renderSubtitleOverlay(guiGraphics, deltaTracker);
        }
    }

    private void renderBossOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        this.bossOverlay.render(guiGraphics);
    }

    private void renderDebugOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (this.debugOverlay.showDebugScreen()) {
            guiGraphics.nextStratum();
            this.debugOverlay.render(guiGraphics);
        }
    }

    private void renderSubtitleOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        this.subtitleOverlay.render(guiGraphics);
    }

    private void renderCameraOverlays(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        float f;
        if (Minecraft.useFancyGraphics()) {
            this.renderVignette(guiGraphics, this.minecraft.getCameraEntity());
        }
        LocalPlayer localPlayer = this.minecraft.player;
        float f2 = deltaTracker.getGameTimeDeltaTicks();
        this.scopeScale = Mth.lerp(0.5f * f2, this.scopeScale, 1.125f);
        if (this.minecraft.options.getCameraType().isFirstPerson()) {
            if (localPlayer.isScoping()) {
                this.renderSpyglassOverlay(guiGraphics, this.scopeScale);
            } else {
                this.scopeScale = 0.5f;
                for (EquipmentSlot equipmentSlot : EquipmentSlot.values()) {
                    ItemStack itemStack = localPlayer.getItemBySlot(equipmentSlot);
                    Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
                    if (equippable == null || equippable.slot() != equipmentSlot || !equippable.cameraOverlay().isPresent()) continue;
                    this.renderTextureOverlay(guiGraphics, equippable.cameraOverlay().get().withPath(string -> "textures/" + string + ".png"), 1.0f);
                }
            }
        }
        if (localPlayer.getTicksFrozen() > 0) {
            this.renderTextureOverlay(guiGraphics, POWDER_SNOW_OUTLINE_LOCATION, localPlayer.getPercentFrozen());
        }
        float f3 = deltaTracker.getGameTimeDeltaPartialTick(false);
        float f4 = Mth.lerp(f3, localPlayer.oPortalEffectIntensity, localPlayer.portalEffectIntensity);
        float f5 = localPlayer.getEffectBlendFactor(MobEffects.NAUSEA, f3);
        if (f4 > 0.0f) {
            this.renderPortalOverlay(guiGraphics, f4);
        } else if (f5 > 0.0f && (f = this.minecraft.options.screenEffectScale().get().floatValue()) < 1.0f) {
            float f6 = f5 * (1.0f - f);
            this.renderConfusionOverlay(guiGraphics, f6);
        }
    }

    private void renderSleepOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (this.minecraft.player.getSleepTimer() <= 0) {
            return;
        }
        Profiler.get().push("sleep");
        guiGraphics.nextStratum();
        float f = this.minecraft.player.getSleepTimer();
        float f2 = f / 100.0f;
        if (f2 > 1.0f) {
            f2 = 1.0f - (f - 100.0f) / 10.0f;
        }
        int n = (int)(220.0f * f2) << 24 | 0x101020;
        guiGraphics.fill(0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), n);
        Profiler.get().pop();
    }

    private void renderOverlayMessage(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Font font = this.getFont();
        if (this.overlayMessageString == null || this.overlayMessageTime <= 0) {
            return;
        }
        Profiler.get().push("overlayMessage");
        float f = (float)this.overlayMessageTime - deltaTracker.getGameTimeDeltaPartialTick(false);
        int n = (int)(f * 255.0f / 20.0f);
        if (n > 255) {
            n = 255;
        }
        if (n > 0) {
            guiGraphics.nextStratum();
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate((float)(guiGraphics.guiWidth() / 2), (float)(guiGraphics.guiHeight() - 68));
            int n2 = this.animateOverlayMessageColor ? Mth.hsvToArgb(f / 50.0f, 0.7f, 0.6f, n) : ARGB.color(n, -1);
            int n3 = font.width(this.overlayMessageString);
            guiGraphics.drawStringWithBackdrop(font, this.overlayMessageString, -n3 / 2, -4, n3, n2);
            guiGraphics.pose().popMatrix();
        }
        Profiler.get().pop();
    }

    private void renderTitle(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (this.title == null || this.titleTime <= 0) {
            return;
        }
        Font font = this.getFont();
        Profiler.get().push("titleAndSubtitle");
        float f = (float)this.titleTime - deltaTracker.getGameTimeDeltaPartialTick(false);
        int n = 255;
        if (this.titleTime > this.titleFadeOutTime + this.titleStayTime) {
            float f2 = (float)(this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime) - f;
            n = (int)(f2 * 255.0f / (float)this.titleFadeInTime);
        }
        if (this.titleTime <= this.titleFadeOutTime) {
            n = (int)(f * 255.0f / (float)this.titleFadeOutTime);
        }
        if ((n = Mth.clamp(n, 0, 255)) > 0) {
            guiGraphics.nextStratum();
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate((float)(guiGraphics.guiWidth() / 2), (float)(guiGraphics.guiHeight() / 2));
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().scale(4.0f, 4.0f);
            int n2 = font.width(this.title);
            int n3 = ARGB.color(n, -1);
            guiGraphics.drawStringWithBackdrop(font, this.title, -n2 / 2, -10, n2, n3);
            guiGraphics.pose().popMatrix();
            if (this.subtitle != null) {
                guiGraphics.pose().pushMatrix();
                guiGraphics.pose().scale(2.0f, 2.0f);
                int n4 = font.width(this.subtitle);
                guiGraphics.drawStringWithBackdrop(font, this.subtitle, -n4 / 2, 5, n4, n3);
                guiGraphics.pose().popMatrix();
            }
            guiGraphics.pose().popMatrix();
        }
        Profiler.get().pop();
    }

    private void renderChat(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!this.chat.isChatFocused()) {
            Window window = this.minecraft.getWindow();
            int n = Mth.floor(this.minecraft.mouseHandler.getScaledXPos(window));
            int n2 = Mth.floor(this.minecraft.mouseHandler.getScaledYPos(window));
            guiGraphics.nextStratum();
            this.chat.render(guiGraphics, this.tickCount, n, n2, false);
        }
    }

    private void renderScoreboardSidebar(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        DisplaySlot displaySlot;
        Scoreboard scoreboard = this.minecraft.level.getScoreboard();
        Objective objective = null;
        PlayerTeam playerTeam = scoreboard.getPlayersTeam(this.minecraft.player.getScoreboardName());
        if (playerTeam != null && (displaySlot = DisplaySlot.teamColorToSlot(playerTeam.getColor())) != null) {
            objective = scoreboard.getDisplayObjective(displaySlot);
        }
        DisplaySlot displaySlot2 = displaySlot = objective != null ? objective : scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
        if (displaySlot != null) {
            guiGraphics.nextStratum();
            this.displayScoreboardSidebar(guiGraphics, (Objective)((Object)displaySlot));
        }
    }

    private void renderTabList(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Scoreboard scoreboard = this.minecraft.level.getScoreboard();
        Objective objective = scoreboard.getDisplayObjective(DisplaySlot.LIST);
        if (this.minecraft.options.keyPlayerList.isDown() && (!this.minecraft.isLocalServer() || this.minecraft.player.connection.getListedOnlinePlayers().size() > 1 || objective != null)) {
            this.tabList.setVisible(true);
            guiGraphics.nextStratum();
            this.tabList.render(guiGraphics, guiGraphics.guiWidth(), scoreboard, objective);
        } else {
            this.tabList.setVisible(false);
        }
    }

    private void renderCrosshair(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Options options = this.minecraft.options;
        if (!options.getCameraType().isFirstPerson()) {
            return;
        }
        if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR && !this.canRenderCrosshairForSpectator(this.minecraft.hitResult)) {
            return;
        }
        if (!this.shouldRenderDebugCrosshair()) {
            guiGraphics.nextStratum();
            int n = 15;
            guiGraphics.blitSprite(RenderPipelines.CROSSHAIR, CROSSHAIR_SPRITE, (guiGraphics.guiWidth() - 15) / 2, (guiGraphics.guiHeight() - 15) / 2, 15, 15);
            if (this.minecraft.options.attackIndicator().get() == AttackIndicatorStatus.CROSSHAIR) {
                float f = this.minecraft.player.getAttackStrengthScale(0.0f);
                boolean bl = false;
                if (this.minecraft.crosshairPickEntity != null && this.minecraft.crosshairPickEntity instanceof LivingEntity && f >= 1.0f) {
                    bl = this.minecraft.player.getCurrentItemAttackStrengthDelay() > 5.0f;
                    bl &= this.minecraft.crosshairPickEntity.isAlive();
                }
                int n2 = guiGraphics.guiHeight() / 2 - 7 + 16;
                int n3 = guiGraphics.guiWidth() / 2 - 8;
                if (bl) {
                    guiGraphics.blitSprite(RenderPipelines.CROSSHAIR, CROSSHAIR_ATTACK_INDICATOR_FULL_SPRITE, n3, n2, 16, 16);
                } else if (f < 1.0f) {
                    int n4 = (int)(f * 17.0f);
                    guiGraphics.blitSprite(RenderPipelines.CROSSHAIR, CROSSHAIR_ATTACK_INDICATOR_BACKGROUND_SPRITE, n3, n2, 16, 4);
                    guiGraphics.blitSprite(RenderPipelines.CROSSHAIR, CROSSHAIR_ATTACK_INDICATOR_PROGRESS_SPRITE, 16, 4, 0, 0, n3, n2, n4, 4);
                }
            }
        }
    }

    public boolean shouldRenderDebugCrosshair() {
        return this.debugOverlay.showDebugScreen() && this.minecraft.options.getCameraType() == CameraType.FIRST_PERSON && !this.minecraft.player.isReducedDebugInfo() && this.minecraft.options.reducedDebugInfo().get() == false;
    }

    private boolean canRenderCrosshairForSpectator(@Nullable HitResult hitResult) {
        if (hitResult == null) {
            return false;
        }
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            return ((EntityHitResult)hitResult).getEntity() instanceof MenuProvider;
        }
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            ClientLevel clientLevel = this.minecraft.level;
            BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
            return clientLevel.getBlockState(blockPos).getMenuProvider(clientLevel, blockPos) != null;
        }
        return false;
    }

    private void renderEffects(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Collection<MobEffectInstance> collection = this.minecraft.player.getActiveEffects();
        if (collection.isEmpty() || this.minecraft.screen != null && this.minecraft.screen.showsActiveEffects()) {
            return;
        }
        int n = 0;
        int n2 = 0;
        for (MobEffectInstance mobEffectInstance : Ordering.natural().reverse().sortedCopy(collection)) {
            Holder<MobEffect> holder = mobEffectInstance.getEffect();
            if (!mobEffectInstance.showIcon()) continue;
            int n3 = guiGraphics.guiWidth();
            int n4 = 1;
            if (this.minecraft.isDemo()) {
                n4 += 15;
            }
            if (holder.value().isBeneficial()) {
                n3 -= 25 * ++n;
            } else {
                n3 -= 25 * ++n2;
                n4 += 26;
            }
            float f = 1.0f;
            if (mobEffectInstance.isAmbient()) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, EFFECT_BACKGROUND_AMBIENT_SPRITE, n3, n4, 24, 24);
            } else {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, EFFECT_BACKGROUND_SPRITE, n3, n4, 24, 24);
                if (mobEffectInstance.endsWithin(200)) {
                    int n5 = mobEffectInstance.getDuration();
                    int n6 = 10 - n5 / 20;
                    f = Mth.clamp((float)n5 / 10.0f / 5.0f * 0.5f, 0.0f, 0.5f) + Mth.cos((float)n5 * (float)Math.PI / 5.0f) * Mth.clamp((float)n6 / 10.0f * 0.25f, 0.0f, 0.25f);
                    f = Mth.clamp(f, 0.0f, 1.0f);
                }
            }
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, Gui.getMobEffectSprite(holder), n3 + 3, n4 + 3, 18, 18, ARGB.white(f));
        }
    }

    public static ResourceLocation getMobEffectSprite(Holder<MobEffect> holder) {
        return holder.unwrapKey().map(ResourceKey::location).map(resourceLocation -> resourceLocation.withPrefix("mob_effect/")).orElseGet(MissingTextureAtlasSprite::getLocation);
    }

    private void renderHotbarAndDecorations(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) {
            this.spectatorGui.renderHotbar(guiGraphics);
        } else {
            this.renderItemHotbar(guiGraphics, deltaTracker);
        }
        if (this.minecraft.gameMode.canHurtPlayer()) {
            this.renderPlayerHealth(guiGraphics);
        }
        this.renderVehicleHealth(guiGraphics);
        ContextualInfo contextualInfo = this.nextContextualInfoState();
        if (contextualInfo != this.contextualInfoBar.getKey()) {
            this.contextualInfoBar = Pair.of((Object)((Object)contextualInfo), (Object)this.contextualInfoBarRenderers.get((Object)contextualInfo).get());
        }
        ((ContextualBarRenderer)this.contextualInfoBar.getValue()).renderBackground(guiGraphics, deltaTracker);
        if (this.minecraft.gameMode.hasExperience() && this.minecraft.player.experienceLevel > 0) {
            ContextualBarRenderer.renderExperienceLevel(guiGraphics, this.minecraft.font, this.minecraft.player.experienceLevel);
        }
        ((ContextualBarRenderer)this.contextualInfoBar.getValue()).render(guiGraphics, deltaTracker);
        if (this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR) {
            this.renderSelectedItemName(guiGraphics);
        } else if (this.minecraft.player.isSpectator()) {
            this.spectatorGui.renderAction(guiGraphics);
        }
    }

    private void renderItemHotbar(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        float f;
        int n;
        int n2;
        int n3;
        Player player = this.getCameraPlayer();
        if (player == null) {
            return;
        }
        ItemStack itemStack = player.getOffhandItem();
        HumanoidArm humanoidArm = player.getMainArm().getOpposite();
        int n4 = guiGraphics.guiWidth() / 2;
        int n5 = 182;
        int n6 = 91;
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, n4 - 91, guiGraphics.guiHeight() - 22, 182, 22);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SELECTION_SPRITE, n4 - 91 - 1 + player.getInventory().getSelectedSlot() * 20, guiGraphics.guiHeight() - 22 - 1, 24, 23);
        if (!itemStack.isEmpty()) {
            if (humanoidArm == HumanoidArm.LEFT) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_LEFT_SPRITE, n4 - 91 - 29, guiGraphics.guiHeight() - 23, 29, 24);
            } else {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_OFFHAND_RIGHT_SPRITE, n4 + 91, guiGraphics.guiHeight() - 23, 29, 24);
            }
        }
        int n7 = 1;
        for (n3 = 0; n3 < 9; ++n3) {
            n2 = n4 - 90 + n3 * 20 + 2;
            n = guiGraphics.guiHeight() - 16 - 3;
            this.renderSlot(guiGraphics, n2, n, deltaTracker, player, player.getInventory().getItem(n3), n7++);
        }
        if (!itemStack.isEmpty()) {
            n3 = guiGraphics.guiHeight() - 16 - 3;
            if (humanoidArm == HumanoidArm.LEFT) {
                this.renderSlot(guiGraphics, n4 - 91 - 26, n3, deltaTracker, player, itemStack, n7++);
            } else {
                this.renderSlot(guiGraphics, n4 + 91 + 10, n3, deltaTracker, player, itemStack, n7++);
            }
        }
        if (this.minecraft.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR && (f = this.minecraft.player.getAttackStrengthScale(0.0f)) < 1.0f) {
            n2 = guiGraphics.guiHeight() - 20;
            n = n4 + 91 + 6;
            if (humanoidArm == HumanoidArm.RIGHT) {
                n = n4 - 91 - 22;
            }
            int n8 = (int)(f * 19.0f);
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_ATTACK_INDICATOR_BACKGROUND_SPRITE, n, n2, 18, 18);
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_ATTACK_INDICATOR_PROGRESS_SPRITE, 18, 18, 0, 18 - n8, n, n2 + 18 - n8, 18, n8);
        }
    }

    private void renderSelectedItemName(GuiGraphics guiGraphics) {
        Profiler.get().push("selectedItemName");
        if (this.toolHighlightTimer > 0 && !this.lastToolHighlight.isEmpty()) {
            int n;
            MutableComponent mutableComponent = Component.empty().append(this.lastToolHighlight.getHoverName()).withStyle(this.lastToolHighlight.getRarity().color());
            if (this.lastToolHighlight.has(DataComponents.CUSTOM_NAME)) {
                mutableComponent.withStyle(ChatFormatting.ITALIC);
            }
            int n2 = this.getFont().width(mutableComponent);
            int n3 = (guiGraphics.guiWidth() - n2) / 2;
            int n4 = guiGraphics.guiHeight() - 59;
            if (!this.minecraft.gameMode.canHurtPlayer()) {
                n4 += 14;
            }
            if ((n = (int)((float)this.toolHighlightTimer * 256.0f / 10.0f)) > 255) {
                n = 255;
            }
            if (n > 0) {
                guiGraphics.drawStringWithBackdrop(this.getFont(), mutableComponent, n3, n4, n2, ARGB.color(n, -1));
            }
        }
        Profiler.get().pop();
    }

    private void renderDemoOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!this.minecraft.isDemo()) {
            return;
        }
        Profiler.get().push("demo");
        guiGraphics.nextStratum();
        Component component = this.minecraft.level.getGameTime() >= 120500L ? DEMO_EXPIRED_TEXT : Component.translatable("demo.remainingTime", StringUtil.formatTickDuration((int)(120500L - this.minecraft.level.getGameTime()), this.minecraft.level.tickRateManager().tickrate()));
        int n = this.getFont().width(component);
        int n2 = guiGraphics.guiWidth() - n - 10;
        int n3 = 5;
        guiGraphics.drawStringWithBackdrop(this.getFont(), component, n2, 5, n, -1);
        Profiler.get().pop();
    }

    private void displayScoreboardSidebar(GuiGraphics guiGraphics, Objective objective) {
        int n2;
        Scoreboard scoreboard = objective.getScoreboard();
        NumberFormat numberFormat = objective.numberFormatOrDefault(StyledFormat.SIDEBAR_DEFAULT);
        final class DisplayEntry
        extends Record {
            final Component name;
            final Component score;
            final int scoreWidth;

            DisplayEntry(Component component, Component component2, int n) {
                this.name = component;
                this.score = component2;
                this.scoreWidth = n;
            }

            @Override
            public final String toString() {
                return ObjectMethods.bootstrap("toString", new MethodHandle[]{DisplayEntry.class, "name;score;scoreWidth", "name", "score", "scoreWidth"}, this);
            }

            @Override
            public final int hashCode() {
                return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{DisplayEntry.class, "name;score;scoreWidth", "name", "score", "scoreWidth"}, this);
            }

            @Override
            public final boolean equals(Object object) {
                return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{DisplayEntry.class, "name;score;scoreWidth", "name", "score", "scoreWidth"}, this, object);
            }

            public Component name() {
                return this.name;
            }

            public Component score() {
                return this.score;
            }

            public int scoreWidth() {
                return this.scoreWidth;
            }
        }
        DisplayEntry[] displayEntryArray = (DisplayEntry[])scoreboard.listPlayerScores(objective).stream().filter(playerScoreEntry -> !playerScoreEntry.isHidden()).sorted(SCORE_DISPLAY_ORDER).limit(15L).map(playerScoreEntry -> {
            PlayerTeam playerTeam = scoreboard.getPlayersTeam(playerScoreEntry.owner());
            Component component = playerScoreEntry.ownerName();
            MutableComponent mutableComponent = PlayerTeam.formatNameForTeam(playerTeam, component);
            MutableComponent mutableComponent2 = playerScoreEntry.formatValue(numberFormat);
            int n = this.getFont().width(mutableComponent2);
            return new DisplayEntry(mutableComponent, mutableComponent2, n);
        }).toArray(n -> new DisplayEntry[n]);
        Component component = objective.getDisplayName();
        int n3 = n2 = this.getFont().width(component);
        int n4 = this.getFont().width(SPACER);
        for (DisplayEntry displayEntry : displayEntryArray) {
            n3 = Math.max(n3, this.getFont().width(displayEntry.name) + (displayEntry.scoreWidth > 0 ? n4 + displayEntry.scoreWidth : 0));
        }
        int n5 = n3;
        int n6 = displayEntryArray.length;
        int n7 = n6 * this.getFont().lineHeight;
        int n8 = guiGraphics.guiHeight() / 2 + n7 / 3;
        int n9 = 3;
        int n10 = guiGraphics.guiWidth() - n5 - 3;
        int n11 = guiGraphics.guiWidth() - 3 + 2;
        int n12 = this.minecraft.options.getBackgroundColor(0.3f);
        int n13 = this.minecraft.options.getBackgroundColor(0.4f);
        int n14 = n8 - n6 * this.getFont().lineHeight;
        guiGraphics.fill(n10 - 2, n14 - this.getFont().lineHeight - 1, n11, n14 - 1, n13);
        guiGraphics.fill(n10 - 2, n14 - 1, n11, n8, n12);
        guiGraphics.drawString(this.getFont(), component, n10 + n5 / 2 - n2 / 2, n14 - this.getFont().lineHeight, -1, false);
        for (int i = 0; i < n6; ++i) {
            DisplayEntry displayEntry = displayEntryArray[i];
            int n15 = n8 - (n6 - i) * this.getFont().lineHeight;
            guiGraphics.drawString(this.getFont(), displayEntry.name, n10, n15, -1, false);
            guiGraphics.drawString(this.getFont(), displayEntry.score, n11 - displayEntry.scoreWidth, n15, -1, false);
        }
    }

    @Nullable
    private Player getCameraPlayer() {
        Player player;
        Entity entity = this.minecraft.getCameraEntity();
        return entity instanceof Player ? (player = (Player)entity) : null;
    }

    @Nullable
    private LivingEntity getPlayerVehicleWithHealth() {
        Player player = this.getCameraPlayer();
        if (player != null) {
            Entity entity = player.getVehicle();
            if (entity == null) {
                return null;
            }
            if (entity instanceof LivingEntity) {
                return (LivingEntity)entity;
            }
        }
        return null;
    }

    private int getVehicleMaxHearts(@Nullable LivingEntity livingEntity) {
        if (livingEntity == null || !livingEntity.showVehicleHealth()) {
            return 0;
        }
        float f = livingEntity.getMaxHealth();
        int n = (int)(f + 0.5f) / 2;
        if (n > 30) {
            n = 30;
        }
        return n;
    }

    private int getVisibleVehicleHeartRows(int n) {
        return (int)Math.ceil((double)n / 10.0);
    }

    private void renderPlayerHealth(GuiGraphics guiGraphics) {
        Player player = this.getCameraPlayer();
        if (player == null) {
            return;
        }
        int n = Mth.ceil(player.getHealth());
        boolean bl = this.healthBlinkTime > (long)this.tickCount && (this.healthBlinkTime - (long)this.tickCount) / 3L % 2L == 1L;
        long l = Util.getMillis();
        if (n < this.lastHealth && player.invulnerableTime > 0) {
            this.lastHealthTime = l;
            this.healthBlinkTime = this.tickCount + 20;
        } else if (n > this.lastHealth && player.invulnerableTime > 0) {
            this.lastHealthTime = l;
            this.healthBlinkTime = this.tickCount + 10;
        }
        if (l - this.lastHealthTime > 1000L) {
            this.displayHealth = n;
            this.lastHealthTime = l;
        }
        this.lastHealth = n;
        int n2 = this.displayHealth;
        this.random.setSeed(this.tickCount * 312871);
        int n3 = guiGraphics.guiWidth() / 2 - 91;
        int n4 = guiGraphics.guiWidth() / 2 + 91;
        int n5 = guiGraphics.guiHeight() - 39;
        float f = Math.max((float)player.getAttributeValue(Attributes.MAX_HEALTH), (float)Math.max(n2, n));
        int n6 = Mth.ceil(player.getAbsorptionAmount());
        int n7 = Mth.ceil((f + (float)n6) / 2.0f / 10.0f);
        int n8 = Math.max(10 - (n7 - 2), 3);
        int n9 = n5 - 10;
        int n10 = -1;
        if (player.hasEffect(MobEffects.REGENERATION)) {
            n10 = this.tickCount % Mth.ceil(f + 5.0f);
        }
        Profiler.get().push("armor");
        Gui.renderArmor(guiGraphics, player, n5, n7, n8, n3);
        Profiler.get().popPush("health");
        this.renderHearts(guiGraphics, player, n3, n5, n8, n10, f, n, n2, n6, bl);
        LivingEntity livingEntity = this.getPlayerVehicleWithHealth();
        int n11 = this.getVehicleMaxHearts(livingEntity);
        if (n11 == 0) {
            Profiler.get().popPush("food");
            this.renderFood(guiGraphics, player, n5, n4);
            n9 -= 10;
        }
        Profiler.get().popPush("air");
        this.renderAirBubbles(guiGraphics, player, n11, n9, n4);
        Profiler.get().pop();
    }

    private static void renderArmor(GuiGraphics guiGraphics, Player player, int n, int n2, int n3, int n4) {
        int n5 = player.getArmorValue();
        if (n5 <= 0) {
            return;
        }
        int n6 = n - (n2 - 1) * n3 - 10;
        for (int i = 0; i < 10; ++i) {
            int n7 = n4 + i * 8;
            if (i * 2 + 1 < n5) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, ARMOR_FULL_SPRITE, n7, n6, 9, 9);
            }
            if (i * 2 + 1 == n5) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, ARMOR_HALF_SPRITE, n7, n6, 9, 9);
            }
            if (i * 2 + 1 <= n5) continue;
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, ARMOR_EMPTY_SPRITE, n7, n6, 9, 9);
        }
    }

    private void renderHearts(GuiGraphics guiGraphics, Player player, int n, int n2, int n3, int n4, float f, int n5, int n6, int n7, boolean bl) {
        HeartType heartType = HeartType.forPlayer(player);
        boolean bl2 = player.level().getLevelData().isHardcore();
        int n8 = Mth.ceil((double)f / 2.0);
        int n9 = Mth.ceil((double)n7 / 2.0);
        int n10 = n8 * 2;
        for (int i = n8 + n9 - 1; i >= 0; --i) {
            int n11;
            boolean bl3;
            int n12 = i / 10;
            int n13 = i % 10;
            int n14 = n + n13 * 8;
            int n15 = n2 - n12 * n3;
            if (n5 + n7 <= 4) {
                n15 += this.random.nextInt(2);
            }
            if (i < n8 && i == n4) {
                n15 -= 2;
            }
            this.renderHeart(guiGraphics, HeartType.CONTAINER, n14, n15, bl2, bl, false);
            int n16 = i * 2;
            boolean bl4 = bl3 = i >= n8;
            if (bl3 && (n11 = n16 - n10) < n7) {
                boolean bl5 = n11 + 1 == n7;
                this.renderHeart(guiGraphics, heartType == HeartType.WITHERED ? heartType : HeartType.ABSORBING, n14, n15, bl2, false, bl5);
            }
            if (bl && n16 < n6) {
                n11 = n16 + 1 == n6 ? 1 : 0;
                this.renderHeart(guiGraphics, heartType, n14, n15, bl2, true, n11 != 0);
            }
            if (n16 >= n5) continue;
            n11 = n16 + 1 == n5 ? 1 : 0;
            this.renderHeart(guiGraphics, heartType, n14, n15, bl2, false, n11 != 0);
        }
    }

    private void renderHeart(GuiGraphics guiGraphics, HeartType heartType, int n, int n2, boolean bl, boolean bl2, boolean bl3) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, heartType.getSprite(bl, bl3, bl2), n, n2, 9, 9);
    }

    private void renderAirBubbles(GuiGraphics guiGraphics, Player player, int n, int n2, int n3) {
        int n4 = player.getMaxAirSupply();
        int n5 = Math.clamp((long)player.getAirSupply(), 0, n4);
        boolean bl = player.isEyeInFluid(FluidTags.WATER);
        if (bl || n5 < n4) {
            boolean bl2;
            n2 = this.getAirBubbleYLine(n, n2);
            int n6 = Gui.getCurrentAirSupplyBubble(n5, n4, -2);
            int n7 = Gui.getCurrentAirSupplyBubble(n5, n4, 0);
            int n8 = 10 - Gui.getCurrentAirSupplyBubble(n5, n4, Gui.getEmptyBubbleDelayDuration(n5, bl));
            boolean bl3 = bl2 = n6 != n7;
            if (!bl) {
                this.lastBubblePopSoundPlayed = 0;
            }
            for (int i = 1; i <= 10; ++i) {
                int n9 = n3 - (i - 1) * 8 - 9;
                if (i <= n6) {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, AIR_SPRITE, n9, n2, 9, 9);
                    continue;
                }
                if (bl2 && i == n7 && bl) {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, AIR_POPPING_SPRITE, n9, n2, 9, 9);
                    this.playAirBubblePoppedSound(i, player, n8);
                    continue;
                }
                if (i <= 10 - n8) continue;
                int n10 = n8 == 10 && this.tickCount % 2 == 0 ? this.random.nextInt(2) : 0;
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, AIR_EMPTY_SPRITE, n9, n2 + n10, 9, 9);
            }
        }
    }

    private int getAirBubbleYLine(int n, int n2) {
        int n3 = this.getVisibleVehicleHeartRows(n) - 1;
        return n2 -= n3 * 10;
    }

    private static int getCurrentAirSupplyBubble(int n, int n2, int n3) {
        return Mth.ceil((float)((n + n3) * 10) / (float)n2);
    }

    private static int getEmptyBubbleDelayDuration(int n, boolean bl) {
        return n == 0 || !bl ? 0 : 1;
    }

    private void playAirBubblePoppedSound(int n, Player player, int n2) {
        if (this.lastBubblePopSoundPlayed != n) {
            float f = 0.5f + 0.1f * (float)Math.max(0, n2 - 3 + 1);
            float f2 = 1.0f + 0.1f * (float)Math.max(0, n2 - 5 + 1);
            player.playSound(SoundEvents.BUBBLE_POP, f, f2);
            this.lastBubblePopSoundPlayed = n;
        }
    }

    private void renderFood(GuiGraphics guiGraphics, Player player, int n, int n2) {
        FoodData foodData = player.getFoodData();
        int n3 = foodData.getFoodLevel();
        for (int i = 0; i < 10; ++i) {
            ResourceLocation resourceLocation;
            ResourceLocation resourceLocation2;
            ResourceLocation resourceLocation3;
            int n4 = n;
            if (player.hasEffect(MobEffects.HUNGER)) {
                resourceLocation3 = FOOD_EMPTY_HUNGER_SPRITE;
                resourceLocation2 = FOOD_HALF_HUNGER_SPRITE;
                resourceLocation = FOOD_FULL_HUNGER_SPRITE;
            } else {
                resourceLocation3 = FOOD_EMPTY_SPRITE;
                resourceLocation2 = FOOD_HALF_SPRITE;
                resourceLocation = FOOD_FULL_SPRITE;
            }
            if (player.getFoodData().getSaturationLevel() <= 0.0f && this.tickCount % (n3 * 3 + 1) == 0) {
                n4 += this.random.nextInt(3) - 1;
            }
            int n5 = n2 - i * 8 - 9;
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation3, n5, n4, 9, 9);
            if (i * 2 + 1 < n3) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation, n5, n4, 9, 9);
            }
            if (i * 2 + 1 != n3) continue;
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation2, n5, n4, 9, 9);
        }
    }

    private void renderVehicleHealth(GuiGraphics guiGraphics) {
        LivingEntity livingEntity = this.getPlayerVehicleWithHealth();
        if (livingEntity == null) {
            return;
        }
        int n = this.getVehicleMaxHearts(livingEntity);
        if (n == 0) {
            return;
        }
        int n2 = (int)Math.ceil(livingEntity.getHealth());
        Profiler.get().popPush("mountHealth");
        int n3 = guiGraphics.guiHeight() - 39;
        int n4 = guiGraphics.guiWidth() / 2 + 91;
        int n5 = n3;
        int n6 = 0;
        while (n > 0) {
            int n7 = Math.min(n, 10);
            n -= n7;
            for (int i = 0; i < n7; ++i) {
                int n8 = n4 - i * 8 - 9;
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, HEART_VEHICLE_CONTAINER_SPRITE, n8, n5, 9, 9);
                if (i * 2 + 1 + n6 < n2) {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, HEART_VEHICLE_FULL_SPRITE, n8, n5, 9, 9);
                }
                if (i * 2 + 1 + n6 != n2) continue;
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, HEART_VEHICLE_HALF_SPRITE, n8, n5, 9, 9);
            }
            n5 -= 10;
            n6 += 20;
        }
    }

    private void renderTextureOverlay(GuiGraphics guiGraphics, ResourceLocation resourceLocation, float f) {
        int n = ARGB.white(f);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, resourceLocation, 0, 0, 0.0f, 0.0f, guiGraphics.guiWidth(), guiGraphics.guiHeight(), guiGraphics.guiWidth(), guiGraphics.guiHeight(), n);
    }

    private void renderSpyglassOverlay(GuiGraphics guiGraphics, float f) {
        float f2;
        float f3 = f2 = (float)Math.min(guiGraphics.guiWidth(), guiGraphics.guiHeight());
        float f4 = Math.min((float)guiGraphics.guiWidth() / f2, (float)guiGraphics.guiHeight() / f3) * f;
        int n = Mth.floor(f2 * f4);
        int n2 = Mth.floor(f3 * f4);
        int n3 = (guiGraphics.guiWidth() - n) / 2;
        int n4 = (guiGraphics.guiHeight() - n2) / 2;
        int n5 = n3 + n;
        int n6 = n4 + n2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, SPYGLASS_SCOPE_LOCATION, n3, n4, 0.0f, 0.0f, n, n2, n, n2);
        guiGraphics.fill(RenderPipelines.GUI, 0, n6, guiGraphics.guiWidth(), guiGraphics.guiHeight(), -16777216);
        guiGraphics.fill(RenderPipelines.GUI, 0, 0, guiGraphics.guiWidth(), n4, -16777216);
        guiGraphics.fill(RenderPipelines.GUI, 0, n4, n3, n6, -16777216);
        guiGraphics.fill(RenderPipelines.GUI, n5, n4, guiGraphics.guiWidth(), n6, -16777216);
    }

    private void updateVignetteBrightness(Entity entity) {
        BlockPos blockPos = BlockPos.containing(entity.getX(), entity.getEyeY(), entity.getZ());
        float f = LightTexture.getBrightness(entity.level().dimensionType(), entity.level().getMaxLocalRawBrightness(blockPos));
        float f2 = Mth.clamp(1.0f - f, 0.0f, 1.0f);
        this.vignetteBrightness += (f2 - this.vignetteBrightness) * 0.01f;
    }

    private void renderVignette(GuiGraphics guiGraphics, @Nullable Entity entity) {
        int n;
        WorldBorder worldBorder = this.minecraft.level.getWorldBorder();
        float f = 0.0f;
        if (entity != null) {
            float f2 = (float)worldBorder.getDistanceToBorder(entity);
            double d = Math.min(worldBorder.getLerpSpeed() * (double)worldBorder.getWarningTime() * 1000.0, Math.abs(worldBorder.getLerpTarget() - worldBorder.getSize()));
            double d2 = Math.max((double)worldBorder.getWarningBlocks(), d);
            if ((double)f2 < d2) {
                f = 1.0f - (float)((double)f2 / d2);
            }
        }
        if (f > 0.0f) {
            f = Mth.clamp(f, 0.0f, 1.0f);
            n = ARGB.colorFromFloat(1.0f, 0.0f, f, f);
        } else {
            float f3 = this.vignetteBrightness;
            f3 = Mth.clamp(f3, 0.0f, 1.0f);
            n = ARGB.colorFromFloat(1.0f, f3, f3, f3);
        }
        guiGraphics.blit(RenderPipelines.VIGNETTE, VIGNETTE_LOCATION, 0, 0, 0.0f, 0.0f, guiGraphics.guiWidth(), guiGraphics.guiHeight(), guiGraphics.guiWidth(), guiGraphics.guiHeight(), n);
    }

    private void renderPortalOverlay(GuiGraphics guiGraphics, float f) {
        if (f < 1.0f) {
            f *= f;
            f *= f;
            f = f * 0.8f + 0.2f;
        }
        int n = ARGB.white(f);
        TextureAtlasSprite textureAtlasSprite = this.minecraft.getBlockRenderer().getBlockModelShaper().getParticleIcon(Blocks.NETHER_PORTAL.defaultBlockState());
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, textureAtlasSprite, 0, 0, guiGraphics.guiWidth(), guiGraphics.guiHeight(), n);
    }

    private void renderConfusionOverlay(GuiGraphics guiGraphics, float f) {
        int n = guiGraphics.guiWidth();
        int n2 = guiGraphics.guiHeight();
        guiGraphics.pose().pushMatrix();
        float f2 = Mth.lerp(f, 2.0f, 1.0f);
        guiGraphics.pose().translate((float)n / 2.0f, (float)n2 / 2.0f);
        guiGraphics.pose().scale(f2, f2);
        guiGraphics.pose().translate((float)(-n) / 2.0f, (float)(-n2) / 2.0f);
        float f3 = 0.2f * f;
        float f4 = 0.4f * f;
        float f5 = 0.2f * f;
        guiGraphics.blit(RenderPipelines.GUI_NAUSEA_OVERLAY, NAUSEA_LOCATION, 0, 0, 0.0f, 0.0f, n, n2, n, n2, ARGB.colorFromFloat(1.0f, f3, f4, f5));
        guiGraphics.pose().popMatrix();
    }

    private void renderSlot(GuiGraphics guiGraphics, int n, int n2, DeltaTracker deltaTracker, Player player, ItemStack itemStack, int n3) {
        if (itemStack.isEmpty()) {
            return;
        }
        float f = (float)itemStack.getPopTime() - deltaTracker.getGameTimeDeltaPartialTick(false);
        if (f > 0.0f) {
            float f2 = 1.0f + f / 5.0f;
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate((float)(n + 8), (float)(n2 + 12));
            guiGraphics.pose().scale(1.0f / f2, (f2 + 1.0f) / 2.0f);
            guiGraphics.pose().translate((float)(-(n + 8)), (float)(-(n2 + 12)));
        }
        guiGraphics.renderItem(player, itemStack, n, n2, n3);
        if (f > 0.0f) {
            guiGraphics.pose().popMatrix();
        }
        guiGraphics.renderItemDecorations(this.minecraft.font, itemStack, n, n2);
    }

    public void tick(boolean bl) {
        this.tickAutosaveIndicator();
        if (!bl) {
            this.tick();
        }
    }

    private void tick() {
        if (this.overlayMessageTime > 0) {
            --this.overlayMessageTime;
        }
        if (this.titleTime > 0) {
            --this.titleTime;
            if (this.titleTime <= 0) {
                this.title = null;
                this.subtitle = null;
            }
        }
        ++this.tickCount;
        Entity entity = this.minecraft.getCameraEntity();
        if (entity != null) {
            this.updateVignetteBrightness(entity);
        }
        if (this.minecraft.player != null) {
            ItemStack itemStack = this.minecraft.player.getInventory().getSelectedItem();
            if (itemStack.isEmpty()) {
                this.toolHighlightTimer = 0;
            } else if (this.lastToolHighlight.isEmpty() || !itemStack.is(this.lastToolHighlight.getItem()) || !itemStack.getHoverName().equals(this.lastToolHighlight.getHoverName())) {
                this.toolHighlightTimer = (int)(40.0 * this.minecraft.options.notificationDisplayTime().get());
            } else if (this.toolHighlightTimer > 0) {
                --this.toolHighlightTimer;
            }
            this.lastToolHighlight = itemStack;
        }
        this.chat.tick();
    }

    private void tickAutosaveIndicator() {
        IntegratedServer integratedServer = this.minecraft.getSingleplayerServer();
        boolean bl = integratedServer != null && integratedServer.isCurrentlySaving();
        this.lastAutosaveIndicatorValue = this.autosaveIndicatorValue;
        this.autosaveIndicatorValue = Mth.lerp(0.2f, this.autosaveIndicatorValue, bl ? 1.0f : 0.0f);
    }

    public void setNowPlaying(Component component) {
        MutableComponent mutableComponent = Component.translatable("record.nowPlaying", component);
        this.setOverlayMessage(mutableComponent, true);
        this.minecraft.getNarrator().saySystemNow(mutableComponent);
    }

    public void setOverlayMessage(Component component, boolean bl) {
        this.setChatDisabledByPlayerShown(false);
        this.overlayMessageString = component;
        this.overlayMessageTime = 60;
        this.animateOverlayMessageColor = bl;
    }

    public void setChatDisabledByPlayerShown(boolean bl) {
        this.chatDisabledByPlayerShown = bl;
    }

    public boolean isShowingChatDisabledByPlayer() {
        return this.chatDisabledByPlayerShown && this.overlayMessageTime > 0;
    }

    public void setTimes(int n, int n2, int n3) {
        if (n >= 0) {
            this.titleFadeInTime = n;
        }
        if (n2 >= 0) {
            this.titleStayTime = n2;
        }
        if (n3 >= 0) {
            this.titleFadeOutTime = n3;
        }
        if (this.titleTime > 0) {
            this.titleTime = this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime;
        }
    }

    public void setSubtitle(Component component) {
        this.subtitle = component;
    }

    public void setTitle(Component component) {
        this.title = component;
        this.titleTime = this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime;
    }

    public void clearTitles() {
        this.title = null;
        this.subtitle = null;
        this.titleTime = 0;
    }

    public ChatComponent getChat() {
        return this.chat;
    }

    public int getGuiTicks() {
        return this.tickCount;
    }

    public Font getFont() {
        return this.minecraft.font;
    }

    public SpectatorGui getSpectatorGui() {
        return this.spectatorGui;
    }

    public PlayerTabOverlay getTabList() {
        return this.tabList;
    }

    public void onDisconnected() {
        this.tabList.reset();
        this.bossOverlay.reset();
        this.minecraft.getToastManager().clear();
        this.debugOverlay.reset();
        this.chat.clearMessages(true);
        this.clearTitles();
        this.resetTitleTimes();
    }

    public BossHealthOverlay getBossOverlay() {
        return this.bossOverlay;
    }

    public DebugScreenOverlay getDebugOverlay() {
        return this.debugOverlay;
    }

    public void clearCache() {
        this.debugOverlay.clearChunkCache();
    }

    public void renderSavingIndicator(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        int n;
        if (this.minecraft.options.showAutosaveIndicator().get().booleanValue() && (this.autosaveIndicatorValue > 0.0f || this.lastAutosaveIndicatorValue > 0.0f) && (n = Mth.floor(255.0f * Mth.clamp(Mth.lerp(deltaTracker.getRealtimeDeltaTicks(), this.lastAutosaveIndicatorValue, this.autosaveIndicatorValue), 0.0f, 1.0f))) > 0) {
            Font font = this.getFont();
            int n2 = font.width(SAVING_TEXT);
            int n3 = ARGB.color(n, -1);
            int n4 = guiGraphics.guiWidth() - n2 - 5;
            int n5 = guiGraphics.guiHeight() - font.lineHeight - 5;
            guiGraphics.nextStratum();
            guiGraphics.drawStringWithBackdrop(font, SAVING_TEXT, n4, n5, n2, n3);
        }
    }

    private boolean willPrioritizeExperienceInfo() {
        return this.minecraft.player.experienceDisplayStartTick + 100 > this.minecraft.player.tickCount;
    }

    private boolean willPrioritizeJumpInfo() {
        return this.minecraft.player.getJumpRidingScale() > 0.0f || Optionull.mapOrDefault(this.minecraft.player.jumpableVehicle(), PlayerRideableJumping::getJumpCooldown, 0) > 0;
    }

    private ContextualInfo nextContextualInfoState() {
        boolean bl = this.minecraft.player.connection.getWaypointManager().hasWaypoints();
        boolean bl2 = this.minecraft.player.jumpableVehicle() != null;
        boolean bl3 = this.minecraft.gameMode.hasExperience();
        if (bl) {
            if (bl2 && this.willPrioritizeJumpInfo()) {
                return ContextualInfo.JUMPABLE_VEHICLE;
            }
            if (bl3 && this.willPrioritizeExperienceInfo()) {
                return ContextualInfo.EXPERIENCE;
            }
            return ContextualInfo.LOCATOR;
        }
        if (bl2) {
            return ContextualInfo.JUMPABLE_VEHICLE;
        }
        if (bl3) {
            return ContextualInfo.EXPERIENCE;
        }
        return ContextualInfo.EMPTY;
    }

    static enum ContextualInfo {
        EMPTY,
        EXPERIENCE,
        LOCATOR,
        JUMPABLE_VEHICLE;

    }

    static enum HeartType {
        CONTAINER(ResourceLocation.withDefaultNamespace("hud/heart/container"), ResourceLocation.withDefaultNamespace("hud/heart/container_blinking"), ResourceLocation.withDefaultNamespace("hud/heart/container"), ResourceLocation.withDefaultNamespace("hud/heart/container_blinking"), ResourceLocation.withDefaultNamespace("hud/heart/container_hardcore"), ResourceLocation.withDefaultNamespace("hud/heart/container_hardcore_blinking"), ResourceLocation.withDefaultNamespace("hud/heart/container_hardcore"), ResourceLocation.withDefaultNamespace("hud/heart/container_hardcore_blinking")),
        NORMAL(ResourceLocation.withDefaultNamespace("hud/heart/full"), ResourceLocation.withDefaultNamespace("hud/heart/full_blinking"), ResourceLocation.withDefaultNamespace("hud/heart/half"), ResourceLocation.withDefaultNamespace("hud/heart/half_blinking"), ResourceLocation.withDefaultNamespace("hud/heart/hardcore_full"), ResourceLocation.withDefaultNamespace("hud/heart/hardcore_full_blinking"), ResourceLocation.withDefaultNamespace("hud/heart/hardcore_half"), ResourceLocation.withDefaultNamespace("hud/heart/hardcore_half_blinking")),
        POISIONED(ResourceLocation.withDefaultNamespace("hud/heart/poisoned_full"), ResourceLocation.withDefaultNamespace("hud/heart/poisoned_full_blinking"), ResourceLocation.withDefaultNamespace("hud/heart/poisoned_half"), ResourceLocation.withDefaultNamespace("hud/heart/poisoned_half_blinking"), ResourceLocation.withDefaultNamespace("hud/heart/poisoned_hardcore_full"), ResourceLocation.withDefaultNamespace("hud/heart/poisoned_hardcore_full_blinking"), ResourceLocation.withDefaultNamespace("hud/heart/poisoned_hardcore_half"), ResourceLocation.withDefaultNamespace("hud/heart/poisoned_hardcore_half_blinking")),
        WITHERED(ResourceLocation.withDefaultNamespace("hud/heart/withered_full"), ResourceLocation.withDefaultNamespace("hud/heart/withered_full_blinking"), ResourceLocation.withDefaultNamespace("hud/heart/withered_half"), ResourceLocation.withDefaultNamespace("hud/heart/withered_half_blinking"), ResourceLocation.withDefaultNamespace("hud/heart/withered_hardcore_full"), ResourceLocation.withDefaultNamespace("hud/heart/withered_hardcore_full_blinking"), ResourceLocation.withDefaultNamespace("hud/heart/withered_hardcore_half"), ResourceLocation.withDefaultNamespace("hud/heart/withered_hardcore_half_blinking")),
        ABSORBING(ResourceLocation.withDefaultNamespace("hud/heart/absorbing_full"), ResourceLocation.withDefaultNamespace("hud/heart/absorbing_full_blinking"), ResourceLocation.withDefaultNamespace("hud/heart/absorbing_half"), ResourceLocation.withDefaultNamespace("hud/heart/absorbing_half_blinking"), ResourceLocation.withDefaultNamespace("hud/heart/absorbing_hardcore_full"), ResourceLocation.withDefaultNamespace("hud/heart/absorbing_hardcore_full_blinking"), ResourceLocation.withDefaultNamespace("hud/heart/absorbing_hardcore_half"), ResourceLocation.withDefaultNamespace("hud/heart/absorbing_hardcore_half_blinking")),
        FROZEN(ResourceLocation.withDefaultNamespace("hud/heart/frozen_full"), ResourceLocation.withDefaultNamespace("hud/heart/frozen_full_blinking"), ResourceLocation.withDefaultNamespace("hud/heart/frozen_half"), ResourceLocation.withDefaultNamespace("hud/heart/frozen_half_blinking"), ResourceLocation.withDefaultNamespace("hud/heart/frozen_hardcore_full"), ResourceLocation.withDefaultNamespace("hud/heart/frozen_hardcore_full_blinking"), ResourceLocation.withDefaultNamespace("hud/heart/frozen_hardcore_half"), ResourceLocation.withDefaultNamespace("hud/heart/frozen_hardcore_half_blinking"));

        private final ResourceLocation full;
        private final ResourceLocation fullBlinking;
        private final ResourceLocation half;
        private final ResourceLocation halfBlinking;
        private final ResourceLocation hardcoreFull;
        private final ResourceLocation hardcoreFullBlinking;
        private final ResourceLocation hardcoreHalf;
        private final ResourceLocation hardcoreHalfBlinking;

        private HeartType(ResourceLocation resourceLocation, ResourceLocation resourceLocation2, ResourceLocation resourceLocation3, ResourceLocation resourceLocation4, ResourceLocation resourceLocation5, ResourceLocation resourceLocation6, ResourceLocation resourceLocation7, ResourceLocation resourceLocation8) {
            this.full = resourceLocation;
            this.fullBlinking = resourceLocation2;
            this.half = resourceLocation3;
            this.halfBlinking = resourceLocation4;
            this.hardcoreFull = resourceLocation5;
            this.hardcoreFullBlinking = resourceLocation6;
            this.hardcoreHalf = resourceLocation7;
            this.hardcoreHalfBlinking = resourceLocation8;
        }

        public ResourceLocation getSprite(boolean bl, boolean bl2, boolean bl3) {
            if (!bl) {
                if (bl2) {
                    return bl3 ? this.halfBlinking : this.half;
                }
                return bl3 ? this.fullBlinking : this.full;
            }
            if (bl2) {
                return bl3 ? this.hardcoreHalfBlinking : this.hardcoreHalf;
            }
            return bl3 ? this.hardcoreFullBlinking : this.hardcoreFull;
        }

        static HeartType forPlayer(Player player) {
            HeartType heartType = player.hasEffect(MobEffects.POISON) ? POISIONED : (player.hasEffect(MobEffects.WITHER) ? WITHERED : (player.isFullyFrozen() ? FROZEN : NORMAL));
            return heartType;
        }
    }

    public static interface RenderFunction {
        public void render(GuiGraphics var1, DeltaTracker var2);
    }
}

