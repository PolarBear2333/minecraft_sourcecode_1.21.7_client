/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Charsets
 *  com.google.common.base.MoreObjects
 *  com.google.common.base.Splitter
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Sets
 *  com.google.common.io.Files
 *  com.google.gson.Gson
 *  com.google.gson.JsonElement
 *  com.google.gson.reflect.TypeToken
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.ArrayUtils
 *  org.slf4j.Logger
 */
package net.minecraft.client;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.CameraType;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.InactivityFpsLimit;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.NarratorStatus;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.PrioritizeChunkUpdates;
import net.minecraft.client.ToggleKeyMapping;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.GpuWarnlistManager;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.MusicManager;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.LenientJsonParser;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.entity.player.PlayerModelPart;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;

public class Options {
    static final Logger LOGGER = LogUtils.getLogger();
    static final Gson GSON = new Gson();
    private static final TypeToken<List<String>> LIST_OF_STRINGS_TYPE = new TypeToken<List<String>>(){};
    public static final int RENDER_DISTANCE_TINY = 2;
    public static final int RENDER_DISTANCE_SHORT = 4;
    public static final int RENDER_DISTANCE_NORMAL = 8;
    public static final int RENDER_DISTANCE_FAR = 12;
    public static final int RENDER_DISTANCE_REALLY_FAR = 16;
    public static final int RENDER_DISTANCE_EXTREME = 32;
    private static final Splitter OPTION_SPLITTER = Splitter.on((char)':').limit(2);
    public static final String DEFAULT_SOUND_DEVICE = "";
    private static final Component ACCESSIBILITY_TOOLTIP_DARK_MOJANG_BACKGROUND = Component.translatable("options.darkMojangStudiosBackgroundColor.tooltip");
    private final OptionInstance<Boolean> darkMojangStudiosBackground = OptionInstance.createBoolean("options.darkMojangStudiosBackgroundColor", OptionInstance.cachedConstantTooltip(ACCESSIBILITY_TOOLTIP_DARK_MOJANG_BACKGROUND), false);
    private static final Component ACCESSIBILITY_TOOLTIP_HIDE_LIGHTNING_FLASHES = Component.translatable("options.hideLightningFlashes.tooltip");
    private final OptionInstance<Boolean> hideLightningFlash = OptionInstance.createBoolean("options.hideLightningFlashes", OptionInstance.cachedConstantTooltip(ACCESSIBILITY_TOOLTIP_HIDE_LIGHTNING_FLASHES), false);
    private static final Component ACCESSIBILITY_TOOLTIP_HIDE_SPLASH_TEXTS = Component.translatable("options.hideSplashTexts.tooltip");
    private final OptionInstance<Boolean> hideSplashTexts = OptionInstance.createBoolean("options.hideSplashTexts", OptionInstance.cachedConstantTooltip(ACCESSIBILITY_TOOLTIP_HIDE_SPLASH_TEXTS), false);
    private final OptionInstance<Double> sensitivity = new OptionInstance<Double>("options.sensitivity", OptionInstance.noTooltip(), (component, d) -> {
        if (d == 0.0) {
            return Options.genericValueLabel(component, Component.translatable("options.sensitivity.min"));
        }
        if (d == 1.0) {
            return Options.genericValueLabel(component, Component.translatable("options.sensitivity.max"));
        }
        return Options.percentValueLabel(component, 2.0 * d);
    }, OptionInstance.UnitDouble.INSTANCE, 0.5, d -> {});
    private final OptionInstance<Integer> renderDistance;
    private final OptionInstance<Integer> simulationDistance;
    private int serverRenderDistance = 0;
    private final OptionInstance<Double> entityDistanceScaling = new OptionInstance<Double>("options.entityDistanceScaling", OptionInstance.noTooltip(), Options::percentValueLabel, new OptionInstance.IntRange(2, 20).xmap(n -> (double)n / 4.0, d -> (int)(d * 4.0)), Codec.doubleRange((double)0.5, (double)5.0), 1.0, d -> {});
    public static final int UNLIMITED_FRAMERATE_CUTOFF = 260;
    private final OptionInstance<Integer> framerateLimit = new OptionInstance<Integer>("options.framerateLimit", OptionInstance.noTooltip(), (component, n) -> {
        if (n == 260) {
            return Options.genericValueLabel(component, Component.translatable("options.framerateLimit.max"));
        }
        return Options.genericValueLabel(component, Component.translatable("options.framerate", n));
    }, new OptionInstance.IntRange(1, 26).xmap(n -> n * 10, n -> n / 10), Codec.intRange((int)10, (int)260), 120, n -> Minecraft.getInstance().getFramerateLimitTracker().setFramerateLimit((int)n));
    private static final Component INACTIVITY_FPS_LIMIT_TOOLTIP_MINIMIZED = Component.translatable("options.inactivityFpsLimit.minimized.tooltip");
    private static final Component INACTIVITY_FPS_LIMIT_TOOLTIP_AFK = Component.translatable("options.inactivityFpsLimit.afk.tooltip");
    private final OptionInstance<InactivityFpsLimit> inactivityFpsLimit = new OptionInstance<InactivityFpsLimit>("options.inactivityFpsLimit", inactivityFpsLimit -> switch (inactivityFpsLimit) {
        default -> throw new MatchException(null, null);
        case InactivityFpsLimit.MINIMIZED -> Tooltip.create(INACTIVITY_FPS_LIMIT_TOOLTIP_MINIMIZED);
        case InactivityFpsLimit.AFK -> Tooltip.create(INACTIVITY_FPS_LIMIT_TOOLTIP_AFK);
    }, OptionInstance.forOptionEnum(), new OptionInstance.Enum<InactivityFpsLimit>(Arrays.asList(InactivityFpsLimit.values()), InactivityFpsLimit.CODEC), InactivityFpsLimit.AFK, inactivityFpsLimit -> {});
    private final OptionInstance<CloudStatus> cloudStatus = new OptionInstance<CloudStatus>("options.renderClouds", OptionInstance.noTooltip(), OptionInstance.forOptionEnum(), new OptionInstance.Enum<CloudStatus>(Arrays.asList(CloudStatus.values()), Codec.withAlternative(CloudStatus.CODEC, (Codec)Codec.BOOL, bl -> bl != false ? CloudStatus.FANCY : CloudStatus.OFF)), CloudStatus.FANCY, cloudStatus -> {});
    private final OptionInstance<Integer> cloudRange = new OptionInstance<Integer>("options.renderCloudsDistance", OptionInstance.noTooltip(), (component, n) -> Options.genericValueLabel(component, Component.translatable("options.chunks", n)), new OptionInstance.IntRange(2, 128, true), 128, n -> Minecraft.getInstance().levelRenderer.getCloudRenderer().markForRebuild());
    private static final Component GRAPHICS_TOOLTIP_FAST = Component.translatable("options.graphics.fast.tooltip");
    private static final Component GRAPHICS_TOOLTIP_FABULOUS = Component.translatable("options.graphics.fabulous.tooltip", Component.translatable("options.graphics.fabulous").withStyle(ChatFormatting.ITALIC));
    private static final Component GRAPHICS_TOOLTIP_FANCY = Component.translatable("options.graphics.fancy.tooltip");
    private final OptionInstance<GraphicsStatus> graphicsMode = new OptionInstance<GraphicsStatus>("options.graphics", graphicsStatus -> switch (graphicsStatus) {
        default -> throw new MatchException(null, null);
        case GraphicsStatus.FANCY -> Tooltip.create(GRAPHICS_TOOLTIP_FANCY);
        case GraphicsStatus.FAST -> Tooltip.create(GRAPHICS_TOOLTIP_FAST);
        case GraphicsStatus.FABULOUS -> Tooltip.create(GRAPHICS_TOOLTIP_FABULOUS);
    }, (component, graphicsStatus) -> {
        MutableComponent mutableComponent = Component.translatable(graphicsStatus.getKey());
        if (graphicsStatus == GraphicsStatus.FABULOUS) {
            return mutableComponent.withStyle(ChatFormatting.ITALIC);
        }
        return mutableComponent;
    }, new OptionInstance.AltEnum<GraphicsStatus>(Arrays.asList(GraphicsStatus.values()), Stream.of(GraphicsStatus.values()).filter(graphicsStatus -> graphicsStatus != GraphicsStatus.FABULOUS).collect(Collectors.toList()), () -> Minecraft.getInstance().isRunning() && Minecraft.getInstance().getGpuWarnlistManager().isSkippingFabulous(), (optionInstance, graphicsStatus) -> {
        Minecraft minecraft = Minecraft.getInstance();
        GpuWarnlistManager gpuWarnlistManager = minecraft.getGpuWarnlistManager();
        if (graphicsStatus == GraphicsStatus.FABULOUS && gpuWarnlistManager.willShowWarning()) {
            gpuWarnlistManager.showWarning();
            return;
        }
        optionInstance.set(graphicsStatus);
        minecraft.levelRenderer.allChanged();
    }, Codec.INT.xmap(GraphicsStatus::byId, GraphicsStatus::getId)), GraphicsStatus.FANCY, graphicsStatus -> {});
    private final OptionInstance<Boolean> ambientOcclusion = OptionInstance.createBoolean("options.ao", true, bl -> Minecraft.getInstance().levelRenderer.allChanged());
    private static final Component PRIORITIZE_CHUNK_TOOLTIP_NONE = Component.translatable("options.prioritizeChunkUpdates.none.tooltip");
    private static final Component PRIORITIZE_CHUNK_TOOLTIP_PLAYER_AFFECTED = Component.translatable("options.prioritizeChunkUpdates.byPlayer.tooltip");
    private static final Component PRIORITIZE_CHUNK_TOOLTIP_NEARBY = Component.translatable("options.prioritizeChunkUpdates.nearby.tooltip");
    private final OptionInstance<PrioritizeChunkUpdates> prioritizeChunkUpdates = new OptionInstance<PrioritizeChunkUpdates>("options.prioritizeChunkUpdates", prioritizeChunkUpdates -> switch (prioritizeChunkUpdates) {
        default -> throw new MatchException(null, null);
        case PrioritizeChunkUpdates.NONE -> Tooltip.create(PRIORITIZE_CHUNK_TOOLTIP_NONE);
        case PrioritizeChunkUpdates.PLAYER_AFFECTED -> Tooltip.create(PRIORITIZE_CHUNK_TOOLTIP_PLAYER_AFFECTED);
        case PrioritizeChunkUpdates.NEARBY -> Tooltip.create(PRIORITIZE_CHUNK_TOOLTIP_NEARBY);
    }, OptionInstance.forOptionEnum(), new OptionInstance.Enum<PrioritizeChunkUpdates>(Arrays.asList(PrioritizeChunkUpdates.values()), Codec.INT.xmap(PrioritizeChunkUpdates::byId, PrioritizeChunkUpdates::getId)), PrioritizeChunkUpdates.NONE, prioritizeChunkUpdates -> {});
    public List<String> resourcePacks = Lists.newArrayList();
    public List<String> incompatibleResourcePacks = Lists.newArrayList();
    private final OptionInstance<ChatVisiblity> chatVisibility = new OptionInstance<ChatVisiblity>("options.chat.visibility", OptionInstance.noTooltip(), OptionInstance.forOptionEnum(), new OptionInstance.Enum<ChatVisiblity>(Arrays.asList(ChatVisiblity.values()), Codec.INT.xmap(ChatVisiblity::byId, ChatVisiblity::getId)), ChatVisiblity.FULL, chatVisiblity -> {});
    private final OptionInstance<Double> chatOpacity = new OptionInstance<Double>("options.chat.opacity", OptionInstance.noTooltip(), (component, d) -> Options.percentValueLabel(component, d * 0.9 + 0.1), OptionInstance.UnitDouble.INSTANCE, 1.0, d -> Minecraft.getInstance().gui.getChat().rescaleChat());
    private final OptionInstance<Double> chatLineSpacing = new OptionInstance<Double>("options.chat.line_spacing", OptionInstance.noTooltip(), Options::percentValueLabel, OptionInstance.UnitDouble.INSTANCE, 0.0, d -> {});
    private static final Component MENU_BACKGROUND_BLURRINESS_TOOLTIP = Component.translatable("options.accessibility.menu_background_blurriness.tooltip");
    private static final int BLURRINESS_DEFAULT_VALUE = 5;
    private final OptionInstance<Integer> menuBackgroundBlurriness = new OptionInstance<Integer>("options.accessibility.menu_background_blurriness", OptionInstance.cachedConstantTooltip(MENU_BACKGROUND_BLURRINESS_TOOLTIP), Options::genericValueOrOffLabel, new OptionInstance.IntRange(0, 10), 5, n -> {});
    private final OptionInstance<Double> textBackgroundOpacity = new OptionInstance<Double>("options.accessibility.text_background_opacity", OptionInstance.noTooltip(), Options::percentValueLabel, OptionInstance.UnitDouble.INSTANCE, 0.5, d -> Minecraft.getInstance().gui.getChat().rescaleChat());
    private final OptionInstance<Double> panoramaSpeed = new OptionInstance<Double>("options.accessibility.panorama_speed", OptionInstance.noTooltip(), Options::percentValueLabel, OptionInstance.UnitDouble.INSTANCE, 1.0, d -> {});
    private static final Component ACCESSIBILITY_TOOLTIP_CONTRAST_MODE = Component.translatable("options.accessibility.high_contrast.tooltip");
    private final OptionInstance<Boolean> highContrast = OptionInstance.createBoolean("options.accessibility.high_contrast", OptionInstance.cachedConstantTooltip(ACCESSIBILITY_TOOLTIP_CONTRAST_MODE), false, bl -> {
        PackRepository packRepository = Minecraft.getInstance().getResourcePackRepository();
        boolean bl2 = packRepository.getSelectedIds().contains("high_contrast");
        if (!bl2 && bl.booleanValue()) {
            if (packRepository.addPack("high_contrast")) {
                this.updateResourcePacks(packRepository);
            }
        } else if (bl2 && !bl.booleanValue() && packRepository.removePack("high_contrast")) {
            this.updateResourcePacks(packRepository);
        }
    });
    private static final Component HIGH_CONTRAST_BLOCK_OUTLINE_TOOLTIP = Component.translatable("options.accessibility.high_contrast_block_outline.tooltip");
    private final OptionInstance<Boolean> highContrastBlockOutline = OptionInstance.createBoolean("options.accessibility.high_contrast_block_outline", OptionInstance.cachedConstantTooltip(HIGH_CONTRAST_BLOCK_OUTLINE_TOOLTIP), false);
    private final OptionInstance<Boolean> narratorHotkey = OptionInstance.createBoolean("options.accessibility.narrator_hotkey", OptionInstance.cachedConstantTooltip(Minecraft.ON_OSX ? Component.translatable("options.accessibility.narrator_hotkey.mac.tooltip") : Component.translatable("options.accessibility.narrator_hotkey.tooltip")), true);
    @Nullable
    public String fullscreenVideoModeString;
    public boolean hideServerAddress;
    public boolean advancedItemTooltips;
    public boolean pauseOnLostFocus = true;
    private final Set<PlayerModelPart> modelParts = EnumSet.allOf(PlayerModelPart.class);
    private final OptionInstance<HumanoidArm> mainHand = new OptionInstance<HumanoidArm>("options.mainHand", OptionInstance.noTooltip(), OptionInstance.forOptionEnum(), new OptionInstance.Enum<HumanoidArm>(Arrays.asList(HumanoidArm.values()), HumanoidArm.CODEC), HumanoidArm.RIGHT, humanoidArm -> {});
    public int overrideWidth;
    public int overrideHeight;
    private final OptionInstance<Double> chatScale = new OptionInstance<Double>("options.chat.scale", OptionInstance.noTooltip(), (component, d) -> {
        if (d == 0.0) {
            return CommonComponents.optionStatus(component, false);
        }
        return Options.percentValueLabel(component, d);
    }, OptionInstance.UnitDouble.INSTANCE, 1.0, d -> Minecraft.getInstance().gui.getChat().rescaleChat());
    private final OptionInstance<Double> chatWidth = new OptionInstance<Double>("options.chat.width", OptionInstance.noTooltip(), (component, d) -> Options.pixelValueLabel(component, ChatComponent.getWidth(d)), OptionInstance.UnitDouble.INSTANCE, 1.0, d -> Minecraft.getInstance().gui.getChat().rescaleChat());
    private final OptionInstance<Double> chatHeightUnfocused = new OptionInstance<Double>("options.chat.height.unfocused", OptionInstance.noTooltip(), (component, d) -> Options.pixelValueLabel(component, ChatComponent.getHeight(d)), OptionInstance.UnitDouble.INSTANCE, ChatComponent.defaultUnfocusedPct(), d -> Minecraft.getInstance().gui.getChat().rescaleChat());
    private final OptionInstance<Double> chatHeightFocused = new OptionInstance<Double>("options.chat.height.focused", OptionInstance.noTooltip(), (component, d) -> Options.pixelValueLabel(component, ChatComponent.getHeight(d)), OptionInstance.UnitDouble.INSTANCE, 1.0, d -> Minecraft.getInstance().gui.getChat().rescaleChat());
    private final OptionInstance<Double> chatDelay = new OptionInstance<Double>("options.chat.delay_instant", OptionInstance.noTooltip(), (component, d) -> {
        if (d <= 0.0) {
            return Component.translatable("options.chat.delay_none");
        }
        return Component.translatable("options.chat.delay", String.format(Locale.ROOT, "%.1f", d));
    }, new OptionInstance.IntRange(0, 60).xmap(n -> (double)n / 10.0, d -> (int)(d * 10.0)), Codec.doubleRange((double)0.0, (double)6.0), 0.0, d -> Minecraft.getInstance().getChatListener().setMessageDelay((double)d));
    private static final Component ACCESSIBILITY_TOOLTIP_NOTIFICATION_DISPLAY_TIME = Component.translatable("options.notifications.display_time.tooltip");
    private final OptionInstance<Double> notificationDisplayTime = new OptionInstance<Double>("options.notifications.display_time", OptionInstance.cachedConstantTooltip(ACCESSIBILITY_TOOLTIP_NOTIFICATION_DISPLAY_TIME), (component, d) -> Options.genericValueLabel(component, Component.translatable("options.multiplier", d)), new OptionInstance.IntRange(5, 100).xmap(n -> (double)n / 10.0, d -> (int)(d * 10.0)), Codec.doubleRange((double)0.5, (double)10.0), 1.0, d -> {});
    private final OptionInstance<Integer> mipmapLevels = new OptionInstance<Integer>("options.mipmapLevels", OptionInstance.noTooltip(), (component, n) -> {
        if (n == 0) {
            return CommonComponents.optionStatus(component, false);
        }
        return Options.genericValueLabel(component, n);
    }, new OptionInstance.IntRange(0, 4), 4, n -> {});
    public boolean useNativeTransport = true;
    private final OptionInstance<AttackIndicatorStatus> attackIndicator = new OptionInstance<AttackIndicatorStatus>("options.attackIndicator", OptionInstance.noTooltip(), OptionInstance.forOptionEnum(), new OptionInstance.Enum<AttackIndicatorStatus>(Arrays.asList(AttackIndicatorStatus.values()), Codec.INT.xmap(AttackIndicatorStatus::byId, AttackIndicatorStatus::getId)), AttackIndicatorStatus.CROSSHAIR, attackIndicatorStatus -> {});
    public TutorialSteps tutorialStep = TutorialSteps.MOVEMENT;
    public boolean joinedFirstServer = false;
    private final OptionInstance<Integer> biomeBlendRadius = new OptionInstance<Integer>("options.biomeBlendRadius", OptionInstance.noTooltip(), (component, n) -> {
        int n2 = n * 2 + 1;
        return Options.genericValueLabel(component, Component.translatable("options.biomeBlendRadius." + n2));
    }, new OptionInstance.IntRange(0, 7, false), 2, n -> Minecraft.getInstance().levelRenderer.allChanged());
    private final OptionInstance<Double> mouseWheelSensitivity = new OptionInstance<Double>("options.mouseWheelSensitivity", OptionInstance.noTooltip(), (component, d) -> Options.genericValueLabel(component, Component.literal(String.format(Locale.ROOT, "%.2f", d))), new OptionInstance.IntRange(-200, 100).xmap(Options::logMouse, Options::unlogMouse), Codec.doubleRange((double)Options.logMouse(-200), (double)Options.logMouse(100)), Options.logMouse(0), d -> {});
    private final OptionInstance<Boolean> rawMouseInput = OptionInstance.createBoolean("options.rawMouseInput", true, bl -> {
        Window window = Minecraft.getInstance().getWindow();
        if (window != null) {
            window.updateRawMouseInput((boolean)bl);
        }
    });
    public int glDebugVerbosity = 1;
    private final OptionInstance<Boolean> autoJump = OptionInstance.createBoolean("options.autoJump", false);
    private static final Component ACCESSIBILITY_TOOLTIP_ROTATE_WITH_MINECART = Component.translatable("options.rotateWithMinecart.tooltip");
    private final OptionInstance<Boolean> rotateWithMinecart = OptionInstance.createBoolean("options.rotateWithMinecart", OptionInstance.cachedConstantTooltip(ACCESSIBILITY_TOOLTIP_ROTATE_WITH_MINECART), false);
    private final OptionInstance<Boolean> operatorItemsTab = OptionInstance.createBoolean("options.operatorItemsTab", false);
    private final OptionInstance<Boolean> autoSuggestions = OptionInstance.createBoolean("options.autoSuggestCommands", true);
    private final OptionInstance<Boolean> chatColors = OptionInstance.createBoolean("options.chat.color", true);
    private final OptionInstance<Boolean> chatLinks = OptionInstance.createBoolean("options.chat.links", true);
    private final OptionInstance<Boolean> chatLinksPrompt = OptionInstance.createBoolean("options.chat.links.prompt", true);
    private final OptionInstance<Boolean> enableVsync = OptionInstance.createBoolean("options.vsync", true, bl -> {
        if (Minecraft.getInstance().getWindow() != null) {
            Minecraft.getInstance().getWindow().updateVsync((boolean)bl);
        }
    });
    private final OptionInstance<Boolean> entityShadows = OptionInstance.createBoolean("options.entityShadows", true);
    private final OptionInstance<Boolean> forceUnicodeFont = OptionInstance.createBoolean("options.forceUnicodeFont", false, bl -> Options.updateFontOptions());
    private final OptionInstance<Boolean> japaneseGlyphVariants = OptionInstance.createBoolean("options.japaneseGlyphVariants", OptionInstance.cachedConstantTooltip(Component.translatable("options.japaneseGlyphVariants.tooltip")), Options.japaneseGlyphVariantsDefault(), bl -> Options.updateFontOptions());
    private final OptionInstance<Boolean> invertYMouse = OptionInstance.createBoolean("options.invertMouse", false);
    private final OptionInstance<Boolean> discreteMouseScroll = OptionInstance.createBoolean("options.discrete_mouse_scroll", false);
    private static final Component REALMS_NOTIFICATIONS_TOOLTIP = Component.translatable("options.realmsNotifications.tooltip");
    private final OptionInstance<Boolean> realmsNotifications = OptionInstance.createBoolean("options.realmsNotifications", OptionInstance.cachedConstantTooltip(REALMS_NOTIFICATIONS_TOOLTIP), true);
    private static final Component ALLOW_SERVER_LISTING_TOOLTIP = Component.translatable("options.allowServerListing.tooltip");
    private final OptionInstance<Boolean> allowServerListing = OptionInstance.createBoolean("options.allowServerListing", OptionInstance.cachedConstantTooltip(ALLOW_SERVER_LISTING_TOOLTIP), true, bl -> {});
    private final OptionInstance<Boolean> reducedDebugInfo = OptionInstance.createBoolean("options.reducedDebugInfo", false);
    private final Map<SoundSource, OptionInstance<Double>> soundSourceVolumes = Util.makeEnumMap(SoundSource.class, soundSource -> this.createSoundSliderOptionInstance("soundCategory." + soundSource.getName(), (SoundSource)((Object)soundSource)));
    private final OptionInstance<Boolean> showSubtitles = OptionInstance.createBoolean("options.showSubtitles", false);
    private static final Component DIRECTIONAL_AUDIO_TOOLTIP_ON = Component.translatable("options.directionalAudio.on.tooltip");
    private static final Component DIRECTIONAL_AUDIO_TOOLTIP_OFF = Component.translatable("options.directionalAudio.off.tooltip");
    private final OptionInstance<Boolean> directionalAudio = OptionInstance.createBoolean("options.directionalAudio", bl -> bl != false ? Tooltip.create(DIRECTIONAL_AUDIO_TOOLTIP_ON) : Tooltip.create(DIRECTIONAL_AUDIO_TOOLTIP_OFF), false, bl -> {
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        soundManager.reload();
        soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    });
    private final OptionInstance<Boolean> backgroundForChatOnly = new OptionInstance<Boolean>("options.accessibility.text_background", OptionInstance.noTooltip(), (component, bl) -> bl != false ? Component.translatable("options.accessibility.text_background.chat") : Component.translatable("options.accessibility.text_background.everywhere"), OptionInstance.BOOLEAN_VALUES, true, bl -> {});
    private final OptionInstance<Boolean> touchscreen = OptionInstance.createBoolean("options.touchscreen", false);
    private final OptionInstance<Boolean> fullscreen = OptionInstance.createBoolean("options.fullscreen", false, bl -> {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getWindow() != null && minecraft.getWindow().isFullscreen() != bl.booleanValue()) {
            minecraft.getWindow().toggleFullScreen();
            this.fullscreen().set(minecraft.getWindow().isFullscreen());
        }
    });
    private final OptionInstance<Boolean> bobView = OptionInstance.createBoolean("options.viewBobbing", true);
    private static final Component MOVEMENT_TOGGLE = Component.translatable("options.key.toggle");
    private static final Component MOVEMENT_HOLD = Component.translatable("options.key.hold");
    private final OptionInstance<Boolean> toggleCrouch = new OptionInstance<Boolean>("key.sneak", OptionInstance.noTooltip(), (component, bl) -> bl != false ? MOVEMENT_TOGGLE : MOVEMENT_HOLD, OptionInstance.BOOLEAN_VALUES, false, bl -> {});
    private final OptionInstance<Boolean> toggleSprint = new OptionInstance<Boolean>("key.sprint", OptionInstance.noTooltip(), (component, bl) -> bl != false ? MOVEMENT_TOGGLE : MOVEMENT_HOLD, OptionInstance.BOOLEAN_VALUES, false, bl -> {});
    public boolean skipMultiplayerWarning;
    private static final Component CHAT_TOOLTIP_HIDE_MATCHED_NAMES = Component.translatable("options.hideMatchedNames.tooltip");
    private final OptionInstance<Boolean> hideMatchedNames = OptionInstance.createBoolean("options.hideMatchedNames", OptionInstance.cachedConstantTooltip(CHAT_TOOLTIP_HIDE_MATCHED_NAMES), true);
    private final OptionInstance<Boolean> showAutosaveIndicator = OptionInstance.createBoolean("options.autosaveIndicator", true);
    private static final Component CHAT_TOOLTIP_ONLY_SHOW_SECURE = Component.translatable("options.onlyShowSecureChat.tooltip");
    private final OptionInstance<Boolean> onlyShowSecureChat = OptionInstance.createBoolean("options.onlyShowSecureChat", OptionInstance.cachedConstantTooltip(CHAT_TOOLTIP_ONLY_SHOW_SECURE), false);
    public final KeyMapping keyUp = new KeyMapping("key.forward", 87, "key.categories.movement");
    public final KeyMapping keyLeft = new KeyMapping("key.left", 65, "key.categories.movement");
    public final KeyMapping keyDown = new KeyMapping("key.back", 83, "key.categories.movement");
    public final KeyMapping keyRight = new KeyMapping("key.right", 68, "key.categories.movement");
    public final KeyMapping keyJump = new KeyMapping("key.jump", 32, "key.categories.movement");
    public final KeyMapping keyShift = new ToggleKeyMapping("key.sneak", 340, "key.categories.movement", this.toggleCrouch::get);
    public final KeyMapping keySprint = new ToggleKeyMapping("key.sprint", 341, "key.categories.movement", this.toggleSprint::get);
    public final KeyMapping keyInventory = new KeyMapping("key.inventory", 69, "key.categories.inventory");
    public final KeyMapping keySwapOffhand = new KeyMapping("key.swapOffhand", 70, "key.categories.inventory");
    public final KeyMapping keyDrop = new KeyMapping("key.drop", 81, "key.categories.inventory");
    public final KeyMapping keyUse = new KeyMapping("key.use", InputConstants.Type.MOUSE, 1, "key.categories.gameplay");
    public final KeyMapping keyAttack = new KeyMapping("key.attack", InputConstants.Type.MOUSE, 0, "key.categories.gameplay");
    public final KeyMapping keyPickItem = new KeyMapping("key.pickItem", InputConstants.Type.MOUSE, 2, "key.categories.gameplay");
    public final KeyMapping keyChat = new KeyMapping("key.chat", 84, "key.categories.multiplayer");
    public final KeyMapping keyPlayerList = new KeyMapping("key.playerlist", 258, "key.categories.multiplayer");
    public final KeyMapping keyCommand = new KeyMapping("key.command", 47, "key.categories.multiplayer");
    public final KeyMapping keySocialInteractions = new KeyMapping("key.socialInteractions", 80, "key.categories.multiplayer");
    public final KeyMapping keyScreenshot = new KeyMapping("key.screenshot", 291, "key.categories.misc");
    public final KeyMapping keyTogglePerspective = new KeyMapping("key.togglePerspective", 294, "key.categories.misc");
    public final KeyMapping keySmoothCamera = new KeyMapping("key.smoothCamera", InputConstants.UNKNOWN.getValue(), "key.categories.misc");
    public final KeyMapping keyFullscreen = new KeyMapping("key.fullscreen", 300, "key.categories.misc");
    public final KeyMapping keySpectatorOutlines = new KeyMapping("key.spectatorOutlines", InputConstants.UNKNOWN.getValue(), "key.categories.misc");
    public final KeyMapping keyAdvancements = new KeyMapping("key.advancements", 76, "key.categories.misc");
    public final KeyMapping keyQuickActions = new KeyMapping("key.quickActions", 71, "key.categories.misc");
    public final KeyMapping[] keyHotbarSlots = new KeyMapping[]{new KeyMapping("key.hotbar.1", 49, "key.categories.inventory"), new KeyMapping("key.hotbar.2", 50, "key.categories.inventory"), new KeyMapping("key.hotbar.3", 51, "key.categories.inventory"), new KeyMapping("key.hotbar.4", 52, "key.categories.inventory"), new KeyMapping("key.hotbar.5", 53, "key.categories.inventory"), new KeyMapping("key.hotbar.6", 54, "key.categories.inventory"), new KeyMapping("key.hotbar.7", 55, "key.categories.inventory"), new KeyMapping("key.hotbar.8", 56, "key.categories.inventory"), new KeyMapping("key.hotbar.9", 57, "key.categories.inventory")};
    public final KeyMapping keySaveHotbarActivator = new KeyMapping("key.saveToolbarActivator", 67, "key.categories.creative");
    public final KeyMapping keyLoadHotbarActivator = new KeyMapping("key.loadToolbarActivator", 88, "key.categories.creative");
    public final KeyMapping[] keyMappings = (KeyMapping[])ArrayUtils.addAll((Object[])new KeyMapping[]{this.keyAttack, this.keyUse, this.keyUp, this.keyLeft, this.keyDown, this.keyRight, this.keyJump, this.keyShift, this.keySprint, this.keyDrop, this.keyInventory, this.keyChat, this.keyPlayerList, this.keyPickItem, this.keyCommand, this.keySocialInteractions, this.keyScreenshot, this.keyTogglePerspective, this.keySmoothCamera, this.keyFullscreen, this.keySpectatorOutlines, this.keySwapOffhand, this.keySaveHotbarActivator, this.keyLoadHotbarActivator, this.keyAdvancements, this.keyQuickActions}, (Object[])this.keyHotbarSlots);
    protected Minecraft minecraft;
    private final File optionsFile;
    public boolean hideGui;
    private CameraType cameraType = CameraType.FIRST_PERSON;
    public String lastMpIp = "";
    public boolean smoothCamera;
    private final OptionInstance<Integer> fov = new OptionInstance<Integer>("options.fov", OptionInstance.noTooltip(), (component, n) -> switch (n) {
        case 70 -> Options.genericValueLabel(component, Component.translatable("options.fov.min"));
        case 110 -> Options.genericValueLabel(component, Component.translatable("options.fov.max"));
        default -> Options.genericValueLabel(component, n);
    }, new OptionInstance.IntRange(30, 110), Codec.DOUBLE.xmap(d -> (int)(d * 40.0 + 70.0), n -> ((double)n.intValue() - 70.0) / 40.0), 70, n -> Minecraft.getInstance().levelRenderer.needsUpdate());
    private static final Component TELEMETRY_TOOLTIP = Component.translatable("options.telemetry.button.tooltip", Component.translatable("options.telemetry.state.minimal"), Component.translatable("options.telemetry.state.all"));
    private final OptionInstance<Boolean> telemetryOptInExtra = OptionInstance.createBoolean("options.telemetry.button", OptionInstance.cachedConstantTooltip(TELEMETRY_TOOLTIP), (component, bl) -> {
        Minecraft minecraft = Minecraft.getInstance();
        if (!minecraft.allowsTelemetry()) {
            return Component.translatable("options.telemetry.state.none");
        }
        if (bl.booleanValue() && minecraft.extraTelemetryAvailable()) {
            return Component.translatable("options.telemetry.state.all");
        }
        return Component.translatable("options.telemetry.state.minimal");
    }, false, bl -> {});
    private static final Component ACCESSIBILITY_TOOLTIP_SCREEN_EFFECT = Component.translatable("options.screenEffectScale.tooltip");
    private final OptionInstance<Double> screenEffectScale = new OptionInstance<Double>("options.screenEffectScale", OptionInstance.cachedConstantTooltip(ACCESSIBILITY_TOOLTIP_SCREEN_EFFECT), Options::percentValueOrOffLabel, OptionInstance.UnitDouble.INSTANCE, 1.0, d -> {});
    private static final Component ACCESSIBILITY_TOOLTIP_FOV_EFFECT = Component.translatable("options.fovEffectScale.tooltip");
    private final OptionInstance<Double> fovEffectScale = new OptionInstance<Double>("options.fovEffectScale", OptionInstance.cachedConstantTooltip(ACCESSIBILITY_TOOLTIP_FOV_EFFECT), Options::percentValueOrOffLabel, OptionInstance.UnitDouble.INSTANCE.xmap(Mth::square, Math::sqrt), Codec.doubleRange((double)0.0, (double)1.0), 1.0, d -> {});
    private static final Component ACCESSIBILITY_TOOLTIP_DARKNESS_EFFECT = Component.translatable("options.darknessEffectScale.tooltip");
    private final OptionInstance<Double> darknessEffectScale = new OptionInstance<Double>("options.darknessEffectScale", OptionInstance.cachedConstantTooltip(ACCESSIBILITY_TOOLTIP_DARKNESS_EFFECT), Options::percentValueOrOffLabel, OptionInstance.UnitDouble.INSTANCE.xmap(Mth::square, Math::sqrt), 1.0, d -> {});
    private static final Component ACCESSIBILITY_TOOLTIP_GLINT_SPEED = Component.translatable("options.glintSpeed.tooltip");
    private final OptionInstance<Double> glintSpeed = new OptionInstance<Double>("options.glintSpeed", OptionInstance.cachedConstantTooltip(ACCESSIBILITY_TOOLTIP_GLINT_SPEED), Options::percentValueOrOffLabel, OptionInstance.UnitDouble.INSTANCE, 0.5, d -> {});
    private static final Component ACCESSIBILITY_TOOLTIP_GLINT_STRENGTH = Component.translatable("options.glintStrength.tooltip");
    private final OptionInstance<Double> glintStrength = new OptionInstance<Double>("options.glintStrength", OptionInstance.cachedConstantTooltip(ACCESSIBILITY_TOOLTIP_GLINT_STRENGTH), Options::percentValueOrOffLabel, OptionInstance.UnitDouble.INSTANCE, 0.75, d -> {});
    private static final Component ACCESSIBILITY_TOOLTIP_DAMAGE_TILT_STRENGTH = Component.translatable("options.damageTiltStrength.tooltip");
    private final OptionInstance<Double> damageTiltStrength = new OptionInstance<Double>("options.damageTiltStrength", OptionInstance.cachedConstantTooltip(ACCESSIBILITY_TOOLTIP_DAMAGE_TILT_STRENGTH), Options::percentValueOrOffLabel, OptionInstance.UnitDouble.INSTANCE, 1.0, d -> {});
    private final OptionInstance<Double> gamma = new OptionInstance<Double>("options.gamma", OptionInstance.noTooltip(), (component, d) -> {
        int n = (int)(d * 100.0);
        if (n == 0) {
            return Options.genericValueLabel(component, Component.translatable("options.gamma.min"));
        }
        if (n == 50) {
            return Options.genericValueLabel(component, Component.translatable("options.gamma.default"));
        }
        if (n == 100) {
            return Options.genericValueLabel(component, Component.translatable("options.gamma.max"));
        }
        return Options.genericValueLabel(component, n);
    }, OptionInstance.UnitDouble.INSTANCE, 0.5, d -> {});
    public static final int AUTO_GUI_SCALE = 0;
    private static final int MAX_GUI_SCALE_INCLUSIVE = 0x7FFFFFFE;
    private final OptionInstance<Integer> guiScale = new OptionInstance<Integer>("options.guiScale", OptionInstance.noTooltip(), (component, n) -> n == 0 ? Component.translatable("options.guiScale.auto") : Component.literal(Integer.toString(n)), new OptionInstance.ClampingLazyMaxIntRange(0, () -> {
        Minecraft minecraft = Minecraft.getInstance();
        if (!minecraft.isRunning()) {
            return 0x7FFFFFFE;
        }
        return minecraft.getWindow().calculateScale(0, minecraft.isEnforceUnicode());
    }, 0x7FFFFFFE), 0, n -> this.minecraft.resizeDisplay());
    private final OptionInstance<ParticleStatus> particles = new OptionInstance<ParticleStatus>("options.particles", OptionInstance.noTooltip(), OptionInstance.forOptionEnum(), new OptionInstance.Enum<ParticleStatus>(Arrays.asList(ParticleStatus.values()), Codec.INT.xmap(ParticleStatus::byId, ParticleStatus::getId)), ParticleStatus.ALL, particleStatus -> {});
    private final OptionInstance<NarratorStatus> narrator = new OptionInstance<NarratorStatus>("options.narrator", OptionInstance.noTooltip(), (component, narratorStatus) -> {
        if (this.minecraft.getNarrator().isActive()) {
            return narratorStatus.getName();
        }
        return Component.translatable("options.narrator.notavailable");
    }, new OptionInstance.Enum<NarratorStatus>(Arrays.asList(NarratorStatus.values()), Codec.INT.xmap(NarratorStatus::byId, NarratorStatus::getId)), NarratorStatus.OFF, narratorStatus -> this.minecraft.getNarrator().updateNarratorStatus((NarratorStatus)((Object)narratorStatus)));
    public String languageCode = "en_us";
    private final OptionInstance<String> soundDevice = new OptionInstance<String>("options.audioDevice", OptionInstance.noTooltip(), (component, string) -> {
        if (DEFAULT_SOUND_DEVICE.equals(string)) {
            return Component.translatable("options.audioDevice.default");
        }
        if (string.startsWith("OpenAL Soft on ")) {
            return Component.literal(string.substring(SoundEngine.OPEN_AL_SOFT_PREFIX_LENGTH));
        }
        return Component.literal(string);
    }, new OptionInstance.LazyEnum<String>(() -> Stream.concat(Stream.of(DEFAULT_SOUND_DEVICE), Minecraft.getInstance().getSoundManager().getAvailableSoundDevices().stream()).toList(), (Function<String, Optional<String>>)((Function<String, Optional>)string -> {
        if (!Minecraft.getInstance().isRunning() || string == DEFAULT_SOUND_DEVICE || Minecraft.getInstance().getSoundManager().getAvailableSoundDevices().contains(string)) {
            return Optional.of(string);
        }
        return Optional.empty();
    }), (Codec<String>)Codec.STRING), "", string -> {
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        soundManager.reload();
        soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
    });
    public boolean onboardAccessibility = true;
    private static final Component MUSIC_FREQUENCY_TOOLTIP = Component.translatable("options.music_frequency.tooltip");
    private final OptionInstance<MusicManager.MusicFrequency> musicFrequency = new OptionInstance<MusicManager.MusicFrequency>("options.music_frequency", OptionInstance.cachedConstantTooltip(MUSIC_FREQUENCY_TOOLTIP), OptionInstance.forOptionEnum(), new OptionInstance.Enum<MusicManager.MusicFrequency>(Arrays.asList(MusicManager.MusicFrequency.values()), MusicManager.MusicFrequency.CODEC), MusicManager.MusicFrequency.DEFAULT, musicFrequency -> Minecraft.getInstance().getMusicManager().setMinutesBetweenSongs((MusicManager.MusicFrequency)musicFrequency));
    private static final Component NOW_PLAYING_TOAST_TOOLTIP = Component.translatable("options.showNowPlayingToast.tooltip");
    private final OptionInstance<Boolean> showNowPlayingToast = OptionInstance.createBoolean("options.showNowPlayingToast", OptionInstance.cachedConstantTooltip(NOW_PLAYING_TOAST_TOOLTIP), false, bl -> {
        if (bl.booleanValue()) {
            this.minecraft.getToastManager().createNowPlayingToast();
        } else {
            this.minecraft.getToastManager().removeNowPlayingToast();
        }
    });
    public boolean syncWrites;
    public boolean startedCleanly = true;

    public OptionInstance<Boolean> darkMojangStudiosBackground() {
        return this.darkMojangStudiosBackground;
    }

    public OptionInstance<Boolean> hideLightningFlash() {
        return this.hideLightningFlash;
    }

    public OptionInstance<Boolean> hideSplashTexts() {
        return this.hideSplashTexts;
    }

    public OptionInstance<Double> sensitivity() {
        return this.sensitivity;
    }

    public OptionInstance<Integer> renderDistance() {
        return this.renderDistance;
    }

    public OptionInstance<Integer> simulationDistance() {
        return this.simulationDistance;
    }

    public OptionInstance<Double> entityDistanceScaling() {
        return this.entityDistanceScaling;
    }

    public OptionInstance<Integer> framerateLimit() {
        return this.framerateLimit;
    }

    public OptionInstance<InactivityFpsLimit> inactivityFpsLimit() {
        return this.inactivityFpsLimit;
    }

    public OptionInstance<CloudStatus> cloudStatus() {
        return this.cloudStatus;
    }

    public OptionInstance<Integer> cloudRange() {
        return this.cloudRange;
    }

    public OptionInstance<GraphicsStatus> graphicsMode() {
        return this.graphicsMode;
    }

    public OptionInstance<Boolean> ambientOcclusion() {
        return this.ambientOcclusion;
    }

    public OptionInstance<PrioritizeChunkUpdates> prioritizeChunkUpdates() {
        return this.prioritizeChunkUpdates;
    }

    public void updateResourcePacks(PackRepository packRepository) {
        ImmutableList immutableList = ImmutableList.copyOf(this.resourcePacks);
        this.resourcePacks.clear();
        this.incompatibleResourcePacks.clear();
        for (Pack pack : packRepository.getSelectedPacks()) {
            if (pack.isFixedPosition()) continue;
            this.resourcePacks.add(pack.getId());
            if (pack.getCompatibility().isCompatible()) continue;
            this.incompatibleResourcePacks.add(pack.getId());
        }
        this.save();
        ImmutableList immutableList2 = ImmutableList.copyOf(this.resourcePacks);
        if (!immutableList2.equals(immutableList)) {
            this.minecraft.reloadResourcePacks();
        }
    }

    public OptionInstance<ChatVisiblity> chatVisibility() {
        return this.chatVisibility;
    }

    public OptionInstance<Double> chatOpacity() {
        return this.chatOpacity;
    }

    public OptionInstance<Double> chatLineSpacing() {
        return this.chatLineSpacing;
    }

    public OptionInstance<Integer> menuBackgroundBlurriness() {
        return this.menuBackgroundBlurriness;
    }

    public int getMenuBackgroundBlurriness() {
        return this.menuBackgroundBlurriness().get();
    }

    public OptionInstance<Double> textBackgroundOpacity() {
        return this.textBackgroundOpacity;
    }

    public OptionInstance<Double> panoramaSpeed() {
        return this.panoramaSpeed;
    }

    public OptionInstance<Boolean> highContrast() {
        return this.highContrast;
    }

    public OptionInstance<Boolean> highContrastBlockOutline() {
        return this.highContrastBlockOutline;
    }

    public OptionInstance<Boolean> narratorHotkey() {
        return this.narratorHotkey;
    }

    public OptionInstance<HumanoidArm> mainHand() {
        return this.mainHand;
    }

    public OptionInstance<Double> chatScale() {
        return this.chatScale;
    }

    public OptionInstance<Double> chatWidth() {
        return this.chatWidth;
    }

    public OptionInstance<Double> chatHeightUnfocused() {
        return this.chatHeightUnfocused;
    }

    public OptionInstance<Double> chatHeightFocused() {
        return this.chatHeightFocused;
    }

    public OptionInstance<Double> chatDelay() {
        return this.chatDelay;
    }

    public OptionInstance<Double> notificationDisplayTime() {
        return this.notificationDisplayTime;
    }

    public OptionInstance<Integer> mipmapLevels() {
        return this.mipmapLevels;
    }

    public OptionInstance<AttackIndicatorStatus> attackIndicator() {
        return this.attackIndicator;
    }

    public OptionInstance<Integer> biomeBlendRadius() {
        return this.biomeBlendRadius;
    }

    private static double logMouse(int n) {
        return Math.pow(10.0, (double)n / 100.0);
    }

    private static int unlogMouse(double d) {
        return Mth.floor(Math.log10(d) * 100.0);
    }

    public OptionInstance<Double> mouseWheelSensitivity() {
        return this.mouseWheelSensitivity;
    }

    public OptionInstance<Boolean> rawMouseInput() {
        return this.rawMouseInput;
    }

    public OptionInstance<Boolean> autoJump() {
        return this.autoJump;
    }

    public OptionInstance<Boolean> rotateWithMinecart() {
        return this.rotateWithMinecart;
    }

    public OptionInstance<Boolean> operatorItemsTab() {
        return this.operatorItemsTab;
    }

    public OptionInstance<Boolean> autoSuggestions() {
        return this.autoSuggestions;
    }

    public OptionInstance<Boolean> chatColors() {
        return this.chatColors;
    }

    public OptionInstance<Boolean> chatLinks() {
        return this.chatLinks;
    }

    public OptionInstance<Boolean> chatLinksPrompt() {
        return this.chatLinksPrompt;
    }

    public OptionInstance<Boolean> enableVsync() {
        return this.enableVsync;
    }

    public OptionInstance<Boolean> entityShadows() {
        return this.entityShadows;
    }

    private static void updateFontOptions() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getWindow() != null) {
            minecraft.updateFontOptions();
            minecraft.resizeDisplay();
        }
    }

    public OptionInstance<Boolean> forceUnicodeFont() {
        return this.forceUnicodeFont;
    }

    private static boolean japaneseGlyphVariantsDefault() {
        return Locale.getDefault().getLanguage().equalsIgnoreCase("ja");
    }

    public OptionInstance<Boolean> japaneseGlyphVariants() {
        return this.japaneseGlyphVariants;
    }

    public OptionInstance<Boolean> invertYMouse() {
        return this.invertYMouse;
    }

    public OptionInstance<Boolean> discreteMouseScroll() {
        return this.discreteMouseScroll;
    }

    public OptionInstance<Boolean> realmsNotifications() {
        return this.realmsNotifications;
    }

    public OptionInstance<Boolean> allowServerListing() {
        return this.allowServerListing;
    }

    public OptionInstance<Boolean> reducedDebugInfo() {
        return this.reducedDebugInfo;
    }

    public final float getFinalSoundSourceVolume(SoundSource soundSource) {
        if (soundSource == SoundSource.MASTER) {
            return this.getSoundSourceVolume(soundSource);
        }
        return this.getSoundSourceVolume(soundSource) * this.getSoundSourceVolume(SoundSource.MASTER);
    }

    public final float getSoundSourceVolume(SoundSource soundSource) {
        return this.getSoundSourceOptionInstance(soundSource).get().floatValue();
    }

    public final OptionInstance<Double> getSoundSourceOptionInstance(SoundSource soundSource) {
        return Objects.requireNonNull(this.soundSourceVolumes.get((Object)soundSource));
    }

    private OptionInstance<Double> createSoundSliderOptionInstance(String string, SoundSource soundSource) {
        return new OptionInstance<Double>(string, OptionInstance.noTooltip(), Options::percentValueOrOffLabel, OptionInstance.UnitDouble.INSTANCE, 1.0, d -> Minecraft.getInstance().getSoundManager().updateSourceVolume(soundSource, d.floatValue()));
    }

    public OptionInstance<Boolean> showSubtitles() {
        return this.showSubtitles;
    }

    public OptionInstance<Boolean> directionalAudio() {
        return this.directionalAudio;
    }

    public OptionInstance<Boolean> backgroundForChatOnly() {
        return this.backgroundForChatOnly;
    }

    public OptionInstance<Boolean> touchscreen() {
        return this.touchscreen;
    }

    public OptionInstance<Boolean> fullscreen() {
        return this.fullscreen;
    }

    public OptionInstance<Boolean> bobView() {
        return this.bobView;
    }

    public OptionInstance<Boolean> toggleCrouch() {
        return this.toggleCrouch;
    }

    public OptionInstance<Boolean> toggleSprint() {
        return this.toggleSprint;
    }

    public OptionInstance<Boolean> hideMatchedNames() {
        return this.hideMatchedNames;
    }

    public OptionInstance<Boolean> showAutosaveIndicator() {
        return this.showAutosaveIndicator;
    }

    public OptionInstance<Boolean> onlyShowSecureChat() {
        return this.onlyShowSecureChat;
    }

    public OptionInstance<Integer> fov() {
        return this.fov;
    }

    public OptionInstance<Boolean> telemetryOptInExtra() {
        return this.telemetryOptInExtra;
    }

    public OptionInstance<Double> screenEffectScale() {
        return this.screenEffectScale;
    }

    public OptionInstance<Double> fovEffectScale() {
        return this.fovEffectScale;
    }

    public OptionInstance<Double> darknessEffectScale() {
        return this.darknessEffectScale;
    }

    public OptionInstance<Double> glintSpeed() {
        return this.glintSpeed;
    }

    public OptionInstance<Double> glintStrength() {
        return this.glintStrength;
    }

    public OptionInstance<Double> damageTiltStrength() {
        return this.damageTiltStrength;
    }

    public OptionInstance<Double> gamma() {
        return this.gamma;
    }

    public OptionInstance<Integer> guiScale() {
        return this.guiScale;
    }

    public OptionInstance<ParticleStatus> particles() {
        return this.particles;
    }

    public OptionInstance<NarratorStatus> narrator() {
        return this.narrator;
    }

    public OptionInstance<String> soundDevice() {
        return this.soundDevice;
    }

    public void onboardingAccessibilityFinished() {
        this.onboardAccessibility = false;
        this.save();
    }

    public OptionInstance<MusicManager.MusicFrequency> musicFrequency() {
        return this.musicFrequency;
    }

    public OptionInstance<Boolean> showNowPlayingToast() {
        return this.showNowPlayingToast;
    }

    public Options(Minecraft minecraft, File file) {
        this.minecraft = minecraft;
        this.optionsFile = new File(file, "options.txt");
        boolean bl2 = Runtime.getRuntime().maxMemory() >= 1000000000L;
        this.renderDistance = new OptionInstance<Integer>("options.renderDistance", OptionInstance.noTooltip(), (component, n) -> Options.genericValueLabel(component, Component.translatable("options.chunks", n)), new OptionInstance.IntRange(2, bl2 ? 32 : 16, false), 12, n -> Minecraft.getInstance().levelRenderer.needsUpdate());
        this.simulationDistance = new OptionInstance<Integer>("options.simulationDistance", OptionInstance.noTooltip(), (component, n) -> Options.genericValueLabel(component, Component.translatable("options.chunks", n)), new OptionInstance.IntRange(5, bl2 ? 32 : 16, false), 12, n -> {});
        this.syncWrites = Util.getPlatform() == Util.OS.WINDOWS;
        this.load();
    }

    public float getBackgroundOpacity(float f) {
        return this.backgroundForChatOnly.get() != false ? f : this.textBackgroundOpacity().get().floatValue();
    }

    public int getBackgroundColor(float f) {
        return ARGB.colorFromFloat(this.getBackgroundOpacity(f), 0.0f, 0.0f, 0.0f);
    }

    public int getBackgroundColor(int n) {
        return this.backgroundForChatOnly.get() != false ? n : ARGB.colorFromFloat(this.textBackgroundOpacity.get().floatValue(), 0.0f, 0.0f, 0.0f);
    }

    private void processDumpedOptions(OptionAccess optionAccess) {
        optionAccess.process("ao", this.ambientOcclusion);
        optionAccess.process("biomeBlendRadius", this.biomeBlendRadius);
        optionAccess.process("enableVsync", this.enableVsync);
        optionAccess.process("entityDistanceScaling", this.entityDistanceScaling);
        optionAccess.process("entityShadows", this.entityShadows);
        optionAccess.process("forceUnicodeFont", this.forceUnicodeFont);
        optionAccess.process("japaneseGlyphVariants", this.japaneseGlyphVariants);
        optionAccess.process("fov", this.fov);
        optionAccess.process("fovEffectScale", this.fovEffectScale);
        optionAccess.process("darknessEffectScale", this.darknessEffectScale);
        optionAccess.process("glintSpeed", this.glintSpeed);
        optionAccess.process("glintStrength", this.glintStrength);
        optionAccess.process("prioritizeChunkUpdates", this.prioritizeChunkUpdates);
        optionAccess.process("fullscreen", this.fullscreen);
        optionAccess.process("gamma", this.gamma);
        optionAccess.process("graphicsMode", this.graphicsMode);
        optionAccess.process("guiScale", this.guiScale);
        optionAccess.process("maxFps", this.framerateLimit);
        optionAccess.process("inactivityFpsLimit", this.inactivityFpsLimit);
        optionAccess.process("mipmapLevels", this.mipmapLevels);
        optionAccess.process("narrator", this.narrator);
        optionAccess.process("particles", this.particles);
        optionAccess.process("reducedDebugInfo", this.reducedDebugInfo);
        optionAccess.process("renderClouds", this.cloudStatus);
        optionAccess.process("cloudRange", this.cloudRange);
        optionAccess.process("renderDistance", this.renderDistance);
        optionAccess.process("simulationDistance", this.simulationDistance);
        optionAccess.process("screenEffectScale", this.screenEffectScale);
        optionAccess.process("soundDevice", this.soundDevice);
    }

    private void processOptions(FieldAccess fieldAccess) {
        this.processDumpedOptions(fieldAccess);
        fieldAccess.process("autoJump", this.autoJump);
        fieldAccess.process("rotateWithMinecart", this.rotateWithMinecart);
        fieldAccess.process("operatorItemsTab", this.operatorItemsTab);
        fieldAccess.process("autoSuggestions", this.autoSuggestions);
        fieldAccess.process("chatColors", this.chatColors);
        fieldAccess.process("chatLinks", this.chatLinks);
        fieldAccess.process("chatLinksPrompt", this.chatLinksPrompt);
        fieldAccess.process("discrete_mouse_scroll", this.discreteMouseScroll);
        fieldAccess.process("invertYMouse", this.invertYMouse);
        fieldAccess.process("realmsNotifications", this.realmsNotifications);
        fieldAccess.process("showSubtitles", this.showSubtitles);
        fieldAccess.process("directionalAudio", this.directionalAudio);
        fieldAccess.process("touchscreen", this.touchscreen);
        fieldAccess.process("bobView", this.bobView);
        fieldAccess.process("toggleCrouch", this.toggleCrouch);
        fieldAccess.process("toggleSprint", this.toggleSprint);
        fieldAccess.process("darkMojangStudiosBackground", this.darkMojangStudiosBackground);
        fieldAccess.process("hideLightningFlashes", this.hideLightningFlash);
        fieldAccess.process("hideSplashTexts", this.hideSplashTexts);
        fieldAccess.process("mouseSensitivity", this.sensitivity);
        fieldAccess.process("damageTiltStrength", this.damageTiltStrength);
        fieldAccess.process("highContrast", this.highContrast);
        fieldAccess.process("highContrastBlockOutline", this.highContrastBlockOutline);
        fieldAccess.process("narratorHotkey", this.narratorHotkey);
        this.resourcePacks = fieldAccess.process("resourcePacks", this.resourcePacks, Options::readListOfStrings, arg_0 -> ((Gson)GSON).toJson(arg_0));
        this.incompatibleResourcePacks = fieldAccess.process("incompatibleResourcePacks", this.incompatibleResourcePacks, Options::readListOfStrings, arg_0 -> ((Gson)GSON).toJson(arg_0));
        this.lastMpIp = fieldAccess.process("lastServer", this.lastMpIp);
        this.languageCode = fieldAccess.process("lang", this.languageCode);
        fieldAccess.process("chatVisibility", this.chatVisibility);
        fieldAccess.process("chatOpacity", this.chatOpacity);
        fieldAccess.process("chatLineSpacing", this.chatLineSpacing);
        fieldAccess.process("textBackgroundOpacity", this.textBackgroundOpacity);
        fieldAccess.process("backgroundForChatOnly", this.backgroundForChatOnly);
        this.hideServerAddress = fieldAccess.process("hideServerAddress", this.hideServerAddress);
        this.advancedItemTooltips = fieldAccess.process("advancedItemTooltips", this.advancedItemTooltips);
        this.pauseOnLostFocus = fieldAccess.process("pauseOnLostFocus", this.pauseOnLostFocus);
        this.overrideWidth = fieldAccess.process("overrideWidth", this.overrideWidth);
        this.overrideHeight = fieldAccess.process("overrideHeight", this.overrideHeight);
        fieldAccess.process("chatHeightFocused", this.chatHeightFocused);
        fieldAccess.process("chatDelay", this.chatDelay);
        fieldAccess.process("chatHeightUnfocused", this.chatHeightUnfocused);
        fieldAccess.process("chatScale", this.chatScale);
        fieldAccess.process("chatWidth", this.chatWidth);
        fieldAccess.process("notificationDisplayTime", this.notificationDisplayTime);
        this.useNativeTransport = fieldAccess.process("useNativeTransport", this.useNativeTransport);
        fieldAccess.process("mainHand", this.mainHand);
        fieldAccess.process("attackIndicator", this.attackIndicator);
        this.tutorialStep = fieldAccess.process("tutorialStep", this.tutorialStep, TutorialSteps::getByName, TutorialSteps::getName);
        fieldAccess.process("mouseWheelSensitivity", this.mouseWheelSensitivity);
        fieldAccess.process("rawMouseInput", this.rawMouseInput);
        this.glDebugVerbosity = fieldAccess.process("glDebugVerbosity", this.glDebugVerbosity);
        this.skipMultiplayerWarning = fieldAccess.process("skipMultiplayerWarning", this.skipMultiplayerWarning);
        fieldAccess.process("hideMatchedNames", this.hideMatchedNames);
        this.joinedFirstServer = fieldAccess.process("joinedFirstServer", this.joinedFirstServer);
        this.syncWrites = fieldAccess.process("syncChunkWrites", this.syncWrites);
        fieldAccess.process("showAutosaveIndicator", this.showAutosaveIndicator);
        fieldAccess.process("allowServerListing", this.allowServerListing);
        fieldAccess.process("onlyShowSecureChat", this.onlyShowSecureChat);
        fieldAccess.process("panoramaScrollSpeed", this.panoramaSpeed);
        fieldAccess.process("telemetryOptInExtra", this.telemetryOptInExtra);
        this.onboardAccessibility = fieldAccess.process("onboardAccessibility", this.onboardAccessibility);
        fieldAccess.process("menuBackgroundBlurriness", this.menuBackgroundBlurriness);
        this.startedCleanly = fieldAccess.process("startedCleanly", this.startedCleanly);
        fieldAccess.process("showNowPlayingToast", this.showNowPlayingToast);
        fieldAccess.process("musicFrequency", this.musicFrequency);
        for (KeyMapping keyMapping : this.keyMappings) {
            String string;
            String string2 = keyMapping.saveString();
            if (string2.equals(string = fieldAccess.process("key_" + keyMapping.getName(), string2))) continue;
            keyMapping.setKey(InputConstants.getKey(string));
        }
        for (SoundSource soundSource : SoundSource.values()) {
            fieldAccess.process("soundCategory_" + soundSource.getName(), this.soundSourceVolumes.get((Object)soundSource));
        }
        for (PlayerModelPart playerModelPart : PlayerModelPart.values()) {
            boolean bl = this.modelParts.contains((Object)playerModelPart);
            boolean bl2 = fieldAccess.process("modelPart_" + playerModelPart.getId(), bl);
            if (bl2 == bl) continue;
            this.setModelPart(playerModelPart, bl2);
        }
    }

    public void load() {
        try {
            if (!this.optionsFile.exists()) {
                return;
            }
            CompoundTag compoundTag = new CompoundTag();
            try (Object object = Files.newReader((File)this.optionsFile, (Charset)Charsets.UTF_8);){
                ((BufferedReader)object).lines().forEach(string -> {
                    try {
                        Iterator iterator = OPTION_SPLITTER.split((CharSequence)string).iterator();
                        compoundTag.putString((String)iterator.next(), (String)iterator.next());
                    }
                    catch (Exception exception) {
                        LOGGER.warn("Skipping bad option: {}", string);
                    }
                });
            }
            object = this.dataFix(compoundTag);
            Optional<String> optional = ((CompoundTag)object).getString("fancyGraphics");
            if (optional.isPresent() && !((CompoundTag)object).contains("graphicsMode")) {
                this.graphicsMode.set(Options.isTrue(optional.get()) ? GraphicsStatus.FANCY : GraphicsStatus.FAST);
            }
            this.processOptions(new FieldAccess(){
                final /* synthetic */ CompoundTag val$options;
                {
                    this.val$options = compoundTag;
                }

                /*
                 * Enabled force condition propagation
                 * Lifted jumps to return sites
                 */
                @Nullable
                private String getValue(String string) {
                    Tag tag = this.val$options.get(string);
                    if (tag == null) {
                        return null;
                    }
                    if (!(tag instanceof StringTag)) throw new IllegalStateException("Cannot read field of wrong type, expected string: " + String.valueOf(tag));
                    StringTag stringTag = (StringTag)tag;
                    try {
                        String string2 = stringTag.value();
                        return string2;
                    }
                    catch (Throwable throwable) {
                        throw new MatchException(throwable.toString(), throwable);
                    }
                }

                @Override
                public <T> void process(String string, OptionInstance<T> optionInstance) {
                    String string2 = this.getValue(string);
                    if (string2 != null) {
                        JsonElement jsonElement = LenientJsonParser.parse(string2.isEmpty() ? "\"\"" : string2);
                        optionInstance.codec().parse((DynamicOps)JsonOps.INSTANCE, (Object)jsonElement).ifError(error -> LOGGER.error("Error parsing option value {} for option {}: {}", new Object[]{string2, optionInstance, error.message()})).ifSuccess(optionInstance::set);
                    }
                }

                @Override
                public int process(String string, int n) {
                    String string2 = this.getValue(string);
                    if (string2 != null) {
                        try {
                            return Integer.parseInt(string2);
                        }
                        catch (NumberFormatException numberFormatException) {
                            LOGGER.warn("Invalid integer value for option {} = {}", new Object[]{string, string2, numberFormatException});
                        }
                    }
                    return n;
                }

                @Override
                public boolean process(String string, boolean bl) {
                    String string2 = this.getValue(string);
                    return string2 != null ? Options.isTrue(string2) : bl;
                }

                @Override
                public String process(String string, String string2) {
                    return (String)MoreObjects.firstNonNull((Object)this.getValue(string), (Object)string2);
                }

                @Override
                public float process(String string, float f) {
                    String string2 = this.getValue(string);
                    if (string2 != null) {
                        if (Options.isTrue(string2)) {
                            return 1.0f;
                        }
                        if (Options.isFalse(string2)) {
                            return 0.0f;
                        }
                        try {
                            return Float.parseFloat(string2);
                        }
                        catch (NumberFormatException numberFormatException) {
                            LOGGER.warn("Invalid floating point value for option {} = {}", new Object[]{string, string2, numberFormatException});
                        }
                    }
                    return f;
                }

                @Override
                public <T> T process(String string, T t, Function<String, T> function, Function<T, String> function2) {
                    String string2 = this.getValue(string);
                    return string2 == null ? t : function.apply(string2);
                }
            });
            ((CompoundTag)object).getString("fullscreenResolution").ifPresent(string -> {
                this.fullscreenVideoModeString = string;
            });
            KeyMapping.resetMapping();
        }
        catch (Exception exception) {
            LOGGER.error("Failed to load options", (Throwable)exception);
        }
    }

    static boolean isTrue(String string) {
        return "true".equals(string);
    }

    static boolean isFalse(String string) {
        return "false".equals(string);
    }

    private CompoundTag dataFix(CompoundTag compoundTag) {
        int n = 0;
        try {
            n = compoundTag.getString("version").map(Integer::parseInt).orElse(0);
        }
        catch (RuntimeException runtimeException) {
            // empty catch block
        }
        return DataFixTypes.OPTIONS.updateToCurrentVersion(this.minecraft.getFixerUpper(), compoundTag, n);
    }

    public void save() {
        try (final PrintWriter printWriter = new PrintWriter(new OutputStreamWriter((OutputStream)new FileOutputStream(this.optionsFile), StandardCharsets.UTF_8));){
            printWriter.println("version:" + SharedConstants.getCurrentVersion().dataVersion().version());
            this.processOptions(new FieldAccess(){

                public void writePrefix(String string) {
                    printWriter.print(string);
                    printWriter.print(':');
                }

                @Override
                public <T> void process(String string, OptionInstance<T> optionInstance) {
                    optionInstance.codec().encodeStart((DynamicOps)JsonOps.INSTANCE, optionInstance.get()).ifError(error -> LOGGER.error("Error saving option " + String.valueOf(optionInstance) + ": " + String.valueOf(error))).ifSuccess(jsonElement -> {
                        this.writePrefix(string);
                        printWriter.println(GSON.toJson(jsonElement));
                    });
                }

                @Override
                public int process(String string, int n) {
                    this.writePrefix(string);
                    printWriter.println(n);
                    return n;
                }

                @Override
                public boolean process(String string, boolean bl) {
                    this.writePrefix(string);
                    printWriter.println(bl);
                    return bl;
                }

                @Override
                public String process(String string, String string2) {
                    this.writePrefix(string);
                    printWriter.println(string2);
                    return string2;
                }

                @Override
                public float process(String string, float f) {
                    this.writePrefix(string);
                    printWriter.println(f);
                    return f;
                }

                @Override
                public <T> T process(String string, T t, Function<String, T> function, Function<T, String> function2) {
                    this.writePrefix(string);
                    printWriter.println(function2.apply(t));
                    return t;
                }
            });
            String string = this.getFullscreenVideoModeString();
            if (string != null) {
                printWriter.println("fullscreenResolution:" + string);
            }
        }
        catch (Exception exception) {
            LOGGER.error("Failed to save options", (Throwable)exception);
        }
        this.broadcastOptions();
    }

    @Nullable
    private String getFullscreenVideoModeString() {
        Window window = this.minecraft.getWindow();
        if (window == null) {
            return this.fullscreenVideoModeString;
        }
        if (window.getPreferredFullscreenVideoMode().isPresent()) {
            return window.getPreferredFullscreenVideoMode().get().write();
        }
        return null;
    }

    public ClientInformation buildPlayerInformation() {
        int n = 0;
        for (PlayerModelPart playerModelPart : this.modelParts) {
            n |= playerModelPart.getMask();
        }
        return new ClientInformation(this.languageCode, this.renderDistance.get(), this.chatVisibility.get(), this.chatColors.get(), n, this.mainHand.get(), this.minecraft.isTextFilteringEnabled(), this.allowServerListing.get(), this.particles.get());
    }

    public void broadcastOptions() {
        if (this.minecraft.player != null) {
            this.minecraft.player.connection.broadcastClientInformation(this.buildPlayerInformation());
        }
    }

    public void setModelPart(PlayerModelPart playerModelPart, boolean bl) {
        if (bl) {
            this.modelParts.add(playerModelPart);
        } else {
            this.modelParts.remove((Object)playerModelPart);
        }
    }

    public boolean isModelPartEnabled(PlayerModelPart playerModelPart) {
        return this.modelParts.contains((Object)playerModelPart);
    }

    public CloudStatus getCloudsType() {
        return this.cloudStatus.get();
    }

    public boolean useNativeTransport() {
        return this.useNativeTransport;
    }

    public void loadSelectedResourcePacks(PackRepository packRepository) {
        LinkedHashSet linkedHashSet = Sets.newLinkedHashSet();
        Iterator<String> iterator = this.resourcePacks.iterator();
        while (iterator.hasNext()) {
            String string = iterator.next();
            Pack pack = packRepository.getPack(string);
            if (pack == null && !string.startsWith("file/")) {
                pack = packRepository.getPack("file/" + string);
            }
            if (pack == null) {
                LOGGER.warn("Removed resource pack {} from options because it doesn't seem to exist anymore", (Object)string);
                iterator.remove();
                continue;
            }
            if (!pack.getCompatibility().isCompatible() && !this.incompatibleResourcePacks.contains(string)) {
                LOGGER.warn("Removed resource pack {} from options because it is no longer compatible", (Object)string);
                iterator.remove();
                continue;
            }
            if (pack.getCompatibility().isCompatible() && this.incompatibleResourcePacks.contains(string)) {
                LOGGER.info("Removed resource pack {} from incompatibility list because it's now compatible", (Object)string);
                this.incompatibleResourcePacks.remove(string);
                continue;
            }
            linkedHashSet.add(pack.getId());
        }
        packRepository.setSelected(linkedHashSet);
    }

    public CameraType getCameraType() {
        return this.cameraType;
    }

    public void setCameraType(CameraType cameraType) {
        this.cameraType = cameraType;
    }

    private static List<String> readListOfStrings(String string) {
        ArrayList arrayList = GsonHelper.fromNullableJson(GSON, string, LIST_OF_STRINGS_TYPE);
        return arrayList != null ? arrayList : Lists.newArrayList();
    }

    public File getFile() {
        return this.optionsFile;
    }

    public String dumpOptionsForReport() {
        final ArrayList<Pair> arrayList = new ArrayList<Pair>();
        this.processDumpedOptions(new OptionAccess(){

            @Override
            public <T> void process(String string, OptionInstance<T> optionInstance) {
                arrayList.add(Pair.of((Object)string, optionInstance.get()));
            }
        });
        arrayList.add(Pair.of((Object)"fullscreenResolution", (Object)String.valueOf(this.fullscreenVideoModeString)));
        arrayList.add(Pair.of((Object)"glDebugVerbosity", (Object)this.glDebugVerbosity));
        arrayList.add(Pair.of((Object)"overrideHeight", (Object)this.overrideHeight));
        arrayList.add(Pair.of((Object)"overrideWidth", (Object)this.overrideWidth));
        arrayList.add(Pair.of((Object)"syncChunkWrites", (Object)this.syncWrites));
        arrayList.add(Pair.of((Object)"useNativeTransport", (Object)this.useNativeTransport));
        arrayList.add(Pair.of((Object)"resourcePacks", this.resourcePacks));
        return arrayList.stream().sorted(Comparator.comparing(Pair::getFirst)).map(pair -> (String)pair.getFirst() + ": " + String.valueOf(pair.getSecond())).collect(Collectors.joining(System.lineSeparator()));
    }

    public void setServerRenderDistance(int n) {
        this.serverRenderDistance = n;
    }

    public int getEffectiveRenderDistance() {
        return this.serverRenderDistance > 0 ? Math.min(this.renderDistance.get(), this.serverRenderDistance) : this.renderDistance.get();
    }

    private static Component pixelValueLabel(Component component, int n) {
        return Component.translatable("options.pixel_value", component, n);
    }

    private static Component percentValueLabel(Component component, double d) {
        return Component.translatable("options.percent_value", component, (int)(d * 100.0));
    }

    public static Component genericValueLabel(Component component, Component component2) {
        return Component.translatable("options.generic_value", component, component2);
    }

    public static Component genericValueLabel(Component component, int n) {
        return Options.genericValueLabel(component, Component.literal(Integer.toString(n)));
    }

    public static Component genericValueOrOffLabel(Component component, int n) {
        if (n == 0) {
            return Options.genericValueLabel(component, CommonComponents.OPTION_OFF);
        }
        return Options.genericValueLabel(component, n);
    }

    private static Component percentValueOrOffLabel(Component component, double d) {
        if (d == 0.0) {
            return Options.genericValueLabel(component, CommonComponents.OPTION_OFF);
        }
        return Options.percentValueLabel(component, d);
    }

    static interface OptionAccess {
        public <T> void process(String var1, OptionInstance<T> var2);
    }

    static interface FieldAccess
    extends OptionAccess {
        public int process(String var1, int var2);

        public boolean process(String var1, boolean var2);

        public String process(String var1, String var2);

        public float process(String var1, float var2);

        public <T> T process(String var1, T var2, Function<String, T> var3, Function<T, String> var4);
    }
}

