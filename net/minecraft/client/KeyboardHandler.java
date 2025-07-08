/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.MoreObjects
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client;

import com.google.common.base.MoreObjects;
import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.ClipboardManager;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.logging.LogUtils;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Locale;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.InputType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.NarratorStatus;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.debug.GameModeSwitcherScreen;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundChangeGameModePacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.VersionCommand;
import net.minecraft.util.Mth;
import net.minecraft.util.NativeModuleLister;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class KeyboardHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int DEBUG_CRASH_TIME = 10000;
    private final Minecraft minecraft;
    private final ClipboardManager clipboardManager = new ClipboardManager();
    private long debugCrashKeyTime = -1L;
    private long debugCrashKeyReportedTime = -1L;
    private long debugCrashKeyReportedCount = -1L;
    private boolean handledDebugKey;

    public KeyboardHandler(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    private boolean handleChunkDebugKeys(int n) {
        switch (n) {
            case 69: {
                this.minecraft.sectionPath = !this.minecraft.sectionPath;
                this.debugFeedbackFormatted("SectionPath: {0}", this.minecraft.sectionPath ? "shown" : "hidden");
                return true;
            }
            case 76: {
                this.minecraft.smartCull = !this.minecraft.smartCull;
                this.debugFeedbackFormatted("SmartCull: {0}", this.minecraft.smartCull ? "enabled" : "disabled");
                return true;
            }
            case 79: {
                boolean bl = this.minecraft.debugRenderer.toggleRenderOctree();
                this.debugFeedbackFormatted("Frustum culling Octree: {0}", bl ? "enabled" : "disabled");
                return true;
            }
            case 70: {
                boolean bl = FogRenderer.toggleFog();
                this.debugFeedbackFormatted("Fog: {0}", bl ? "enabled" : "disabled");
                return true;
            }
            case 85: {
                if (Screen.hasShiftDown()) {
                    this.minecraft.levelRenderer.killFrustum();
                    this.debugFeedbackFormatted("Killed frustum", new Object[0]);
                } else {
                    this.minecraft.levelRenderer.captureFrustum();
                    this.debugFeedbackFormatted("Captured frustum", new Object[0]);
                }
                return true;
            }
            case 86: {
                this.minecraft.sectionVisibility = !this.minecraft.sectionVisibility;
                this.debugFeedbackFormatted("SectionVisibility: {0}", this.minecraft.sectionVisibility ? "enabled" : "disabled");
                return true;
            }
            case 87: {
                this.minecraft.wireframe = !this.minecraft.wireframe;
                this.debugFeedbackFormatted("WireFrame: {0}", this.minecraft.wireframe ? "enabled" : "disabled");
                return true;
            }
        }
        return false;
    }

    private void showDebugChat(Component component) {
        this.minecraft.gui.getChat().addMessage(component);
        this.minecraft.getNarrator().saySystemQueued(component);
    }

    private static Component decorateDebugComponent(ChatFormatting chatFormatting, Component component) {
        return Component.empty().append(Component.translatable("debug.prefix").withStyle(chatFormatting, ChatFormatting.BOLD)).append(CommonComponents.SPACE).append(component);
    }

    private void debugWarningComponent(Component component) {
        this.showDebugChat(KeyboardHandler.decorateDebugComponent(ChatFormatting.RED, component));
    }

    private void debugFeedbackComponent(Component component) {
        this.showDebugChat(KeyboardHandler.decorateDebugComponent(ChatFormatting.YELLOW, component));
    }

    private void debugFeedbackTranslated(String string) {
        this.debugFeedbackComponent(Component.translatable(string));
    }

    private void debugFeedbackFormatted(String string, Object ... objectArray) {
        this.debugFeedbackComponent(Component.literal(MessageFormat.format(string, objectArray)));
    }

    private boolean handleDebugKeys(int n) {
        if (this.debugCrashKeyTime > 0L && this.debugCrashKeyTime < Util.getMillis() - 100L) {
            return true;
        }
        switch (n) {
            case 65: {
                this.minecraft.levelRenderer.allChanged();
                this.debugFeedbackTranslated("debug.reload_chunks.message");
                return true;
            }
            case 66: {
                boolean bl = !this.minecraft.getEntityRenderDispatcher().shouldRenderHitBoxes();
                this.minecraft.getEntityRenderDispatcher().setRenderHitBoxes(bl);
                this.debugFeedbackTranslated(bl ? "debug.show_hitboxes.on" : "debug.show_hitboxes.off");
                return true;
            }
            case 68: {
                if (this.minecraft.gui != null) {
                    this.minecraft.gui.getChat().clearMessages(false);
                }
                return true;
            }
            case 71: {
                boolean bl = this.minecraft.debugRenderer.switchRenderChunkborder();
                this.debugFeedbackTranslated(bl ? "debug.chunk_boundaries.on" : "debug.chunk_boundaries.off");
                return true;
            }
            case 72: {
                this.minecraft.options.advancedItemTooltips = !this.minecraft.options.advancedItemTooltips;
                this.debugFeedbackTranslated(this.minecraft.options.advancedItemTooltips ? "debug.advanced_tooltips.on" : "debug.advanced_tooltips.off");
                this.minecraft.options.save();
                return true;
            }
            case 73: {
                if (!this.minecraft.player.isReducedDebugInfo()) {
                    this.copyRecreateCommand(this.minecraft.player.hasPermissions(2), !Screen.hasShiftDown());
                }
                return true;
            }
            case 78: {
                if (!this.minecraft.player.hasPermissions(2)) {
                    this.debugFeedbackTranslated("debug.creative_spectator.error");
                } else if (!this.minecraft.player.isSpectator()) {
                    this.minecraft.player.connection.send(new ServerboundChangeGameModePacket(GameType.SPECTATOR));
                } else {
                    GameType gameType = (GameType)MoreObjects.firstNonNull((Object)this.minecraft.gameMode.getPreviousPlayerMode(), (Object)GameType.CREATIVE);
                    this.minecraft.player.connection.send(new ServerboundChangeGameModePacket(gameType));
                }
                return true;
            }
            case 293: {
                if (!this.minecraft.player.hasPermissions(2)) {
                    this.debugFeedbackTranslated("debug.gamemodes.error");
                } else {
                    this.minecraft.setScreen(new GameModeSwitcherScreen());
                }
                return true;
            }
            case 80: {
                this.minecraft.options.pauseOnLostFocus = !this.minecraft.options.pauseOnLostFocus;
                this.minecraft.options.save();
                this.debugFeedbackTranslated(this.minecraft.options.pauseOnLostFocus ? "debug.pause_focus.on" : "debug.pause_focus.off");
                return true;
            }
            case 81: {
                this.debugFeedbackTranslated("debug.help.message");
                this.showDebugChat(Component.translatable("debug.reload_chunks.help"));
                this.showDebugChat(Component.translatable("debug.show_hitboxes.help"));
                this.showDebugChat(Component.translatable("debug.copy_location.help"));
                this.showDebugChat(Component.translatable("debug.clear_chat.help"));
                this.showDebugChat(Component.translatable("debug.chunk_boundaries.help"));
                this.showDebugChat(Component.translatable("debug.advanced_tooltips.help"));
                this.showDebugChat(Component.translatable("debug.inspect.help"));
                this.showDebugChat(Component.translatable("debug.profiling.help"));
                this.showDebugChat(Component.translatable("debug.creative_spectator.help"));
                this.showDebugChat(Component.translatable("debug.pause_focus.help"));
                this.showDebugChat(Component.translatable("debug.help.help"));
                this.showDebugChat(Component.translatable("debug.dump_dynamic_textures.help"));
                this.showDebugChat(Component.translatable("debug.reload_resourcepacks.help"));
                this.showDebugChat(Component.translatable("debug.version.help"));
                this.showDebugChat(Component.translatable("debug.pause.help"));
                this.showDebugChat(Component.translatable("debug.gamemodes.help"));
                return true;
            }
            case 83: {
                Path path = this.minecraft.gameDirectory.toPath().toAbsolutePath();
                Path path2 = TextureUtil.getDebugTexturePath(path);
                this.minecraft.getTextureManager().dumpAllSheets(path2);
                MutableComponent mutableComponent = Component.literal(path.relativize(path2).toString()).withStyle(ChatFormatting.UNDERLINE).withStyle(style -> style.withClickEvent(new ClickEvent.OpenFile(path2)));
                this.debugFeedbackComponent(Component.translatable("debug.dump_dynamic_textures", mutableComponent));
                return true;
            }
            case 84: {
                this.debugFeedbackTranslated("debug.reload_resourcepacks.message");
                this.minecraft.reloadResourcePacks();
                return true;
            }
            case 76: {
                if (this.minecraft.debugClientMetricsStart(this::debugFeedbackComponent)) {
                    this.debugFeedbackComponent(Component.translatable("debug.profiling.start", 10));
                }
                return true;
            }
            case 67: {
                if (this.minecraft.player.isReducedDebugInfo()) {
                    return false;
                }
                ClientPacketListener clientPacketListener = this.minecraft.player.connection;
                if (clientPacketListener == null) {
                    return false;
                }
                this.debugFeedbackTranslated("debug.copy_location.message");
                this.setClipboard(String.format(Locale.ROOT, "/execute in %s run tp @s %.2f %.2f %.2f %.2f %.2f", this.minecraft.player.level().dimension().location(), this.minecraft.player.getX(), this.minecraft.player.getY(), this.minecraft.player.getZ(), Float.valueOf(this.minecraft.player.getYRot()), Float.valueOf(this.minecraft.player.getXRot())));
                return true;
            }
            case 86: {
                this.debugFeedbackTranslated("debug.version.header");
                VersionCommand.dumpVersion(this::showDebugChat);
                return true;
            }
            case 49: {
                this.minecraft.getDebugOverlay().toggleProfilerChart();
                return true;
            }
            case 50: {
                this.minecraft.getDebugOverlay().toggleFpsCharts();
                return true;
            }
            case 51: {
                this.minecraft.getDebugOverlay().toggleNetworkCharts();
                return true;
            }
        }
        return false;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private void copyRecreateCommand(boolean bl, boolean bl2) {
        HitResult hitResult = this.minecraft.hitResult;
        if (hitResult == null) {
            return;
        }
        switch (hitResult.getType()) {
            case BLOCK: {
                BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
                Level level = this.minecraft.player.level();
                BlockState blockState = level.getBlockState(blockPos);
                if (!bl) {
                    this.copyCreateBlockCommand(blockState, blockPos, null);
                    this.debugFeedbackTranslated("debug.inspect.client.block");
                    return;
                }
                if (bl2) {
                    this.minecraft.player.connection.getDebugQueryHandler().queryBlockEntityTag(blockPos, compoundTag -> {
                        this.copyCreateBlockCommand(blockState, blockPos, (CompoundTag)compoundTag);
                        this.debugFeedbackTranslated("debug.inspect.server.block");
                    });
                    return;
                }
                BlockEntity blockEntity = level.getBlockEntity(blockPos);
                CompoundTag compoundTag2 = blockEntity != null ? blockEntity.saveWithoutMetadata(level.registryAccess()) : null;
                this.copyCreateBlockCommand(blockState, blockPos, compoundTag2);
                this.debugFeedbackTranslated("debug.inspect.client.block");
                return;
            }
            case ENTITY: {
                Entity entity = ((EntityHitResult)hitResult).getEntity();
                ResourceLocation resourceLocation = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
                if (!bl) {
                    this.copyCreateEntityCommand(resourceLocation, entity.position(), null);
                    this.debugFeedbackTranslated("debug.inspect.client.entity");
                    return;
                }
                if (bl2) {
                    this.minecraft.player.connection.getDebugQueryHandler().queryEntityTag(entity.getId(), compoundTag -> {
                        this.copyCreateEntityCommand(resourceLocation, entity.position(), (CompoundTag)compoundTag);
                        this.debugFeedbackTranslated("debug.inspect.server.entity");
                    });
                    return;
                }
                try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(entity.problemPath(), LOGGER);){
                    TagValueOutput tagValueOutput = TagValueOutput.createWithContext(scopedCollector, entity.registryAccess());
                    entity.saveWithoutId(tagValueOutput);
                    this.copyCreateEntityCommand(resourceLocation, entity.position(), tagValueOutput.buildResult());
                }
                this.debugFeedbackTranslated("debug.inspect.client.entity");
                return;
            }
        }
    }

    private void copyCreateBlockCommand(BlockState blockState, BlockPos blockPos, @Nullable CompoundTag compoundTag) {
        StringBuilder stringBuilder = new StringBuilder(BlockStateParser.serialize(blockState));
        if (compoundTag != null) {
            stringBuilder.append(compoundTag);
        }
        String string = String.format(Locale.ROOT, "/setblock %d %d %d %s", blockPos.getX(), blockPos.getY(), blockPos.getZ(), stringBuilder);
        this.setClipboard(string);
    }

    private void copyCreateEntityCommand(ResourceLocation resourceLocation, Vec3 vec3, @Nullable CompoundTag compoundTag) {
        String string;
        if (compoundTag != null) {
            compoundTag.remove("UUID");
            compoundTag.remove("Pos");
            String string2 = NbtUtils.toPrettyComponent(compoundTag).getString();
            string = String.format(Locale.ROOT, "/summon %s %.2f %.2f %.2f %s", resourceLocation, vec3.x, vec3.y, vec3.z, string2);
        } else {
            string = String.format(Locale.ROOT, "/summon %s %.2f %.2f %.2f", resourceLocation, vec3.x, vec3.y, vec3.z);
        }
        this.setClipboard(string);
    }

    public void keyPress(long l, int n, int n2, int n3, int n4) {
        PauseScreen pauseScreen;
        Screen screen;
        boolean bl;
        Object object;
        Screen screen2;
        if (l != this.minecraft.getWindow().getWindow()) {
            return;
        }
        this.minecraft.getFramerateLimitTracker().onInputReceived();
        boolean bl2 = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 292);
        if (this.debugCrashKeyTime > 0L) {
            if (!InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 67) || !bl2) {
                this.debugCrashKeyTime = -1L;
            }
        } else if (InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 67) && bl2) {
            this.handledDebugKey = true;
            this.debugCrashKeyTime = Util.getMillis();
            this.debugCrashKeyReportedTime = Util.getMillis();
            this.debugCrashKeyReportedCount = 0L;
        }
        if ((screen2 = this.minecraft.screen) != null) {
            switch (n) {
                case 262: 
                case 263: 
                case 264: 
                case 265: {
                    this.minecraft.setLastInputType(InputType.KEYBOARD_ARROW);
                    break;
                }
                case 258: {
                    this.minecraft.setLastInputType(InputType.KEYBOARD_TAB);
                }
            }
        }
        if (!(n3 != 1 || this.minecraft.screen instanceof KeyBindsScreen && ((KeyBindsScreen)screen2).lastKeySelection > Util.getMillis() - 20L)) {
            if (this.minecraft.options.keyFullscreen.matches(n, n2)) {
                this.minecraft.getWindow().toggleFullScreen();
                boolean bl3 = this.minecraft.getWindow().isFullscreen();
                this.minecraft.options.fullscreen().set(bl3);
                this.minecraft.options.save();
                Screen screen3 = this.minecraft.screen;
                if (screen3 instanceof VideoSettingsScreen) {
                    VideoSettingsScreen videoSettingsScreen = (VideoSettingsScreen)screen3;
                    videoSettingsScreen.updateFullscreenButton(bl3);
                }
                return;
            }
            if (this.minecraft.options.keyScreenshot.matches(n, n2)) {
                if (Screen.hasControlDown()) {
                    // empty if block
                }
                Screenshot.grab(this.minecraft.gameDirectory, this.minecraft.getMainRenderTarget(), component -> this.minecraft.execute(() -> this.showDebugChat((Component)component)));
                return;
            }
        }
        if (n3 != 0) {
            boolean bl4;
            boolean bl5 = bl4 = screen2 == null || !(screen2.getFocused() instanceof EditBox) || !((EditBox)screen2.getFocused()).canConsumeInput();
            if (bl4) {
                if (Screen.hasControlDown() && n == 66 && this.minecraft.getNarrator().isActive() && this.minecraft.options.narratorHotkey().get().booleanValue()) {
                    boolean bl6 = this.minecraft.options.narrator().get() == NarratorStatus.OFF;
                    this.minecraft.options.narrator().set(NarratorStatus.byId(this.minecraft.options.narrator().get().getId() + 1));
                    this.minecraft.options.save();
                    if (screen2 != null) {
                        screen2.updateNarratorStatus(bl6);
                    }
                }
                object = this.minecraft.player;
            }
        }
        if (screen2 != null) {
            try {
                if (n3 == 1 || n3 == 2) {
                    screen2.afterKeyboardAction();
                    if (screen2.keyPressed(n, n2, n4)) {
                        return;
                    }
                } else if (n3 == 0 && screen2.keyReleased(n, n2, n4)) {
                    return;
                }
            }
            catch (Throwable throwable) {
                object = CrashReport.forThrowable(throwable, "keyPressed event handler");
                screen2.fillCrashDetails((CrashReport)object);
                CrashReportCategory crashReportCategory = ((CrashReport)object).addCategory("Key");
                crashReportCategory.setDetail("Key", n);
                crashReportCategory.setDetail("Scancode", n2);
                crashReportCategory.setDetail("Mods", n4);
                throw new ReportedException((CrashReport)object);
            }
        }
        InputConstants.Key key = InputConstants.getKey(n, n2);
        boolean bl7 = this.minecraft.screen == null;
        boolean bl8 = bl = bl7 || (screen = this.minecraft.screen) instanceof PauseScreen && !(pauseScreen = (PauseScreen)screen).showsPauseMenu();
        if (n3 == 0) {
            KeyMapping.set(key, false);
            if (bl && n == 292) {
                if (this.handledDebugKey) {
                    this.handledDebugKey = false;
                } else {
                    this.minecraft.getDebugOverlay().toggleOverlay();
                }
            }
            return;
        }
        boolean bl9 = false;
        if (bl) {
            if (n == 293 && this.minecraft.gameRenderer != null) {
                this.minecraft.gameRenderer.togglePostEffect();
            }
            if (n == 256) {
                this.minecraft.pauseGame(bl2);
                bl9 |= bl2;
            }
            this.handledDebugKey |= (bl9 |= bl2 && this.handleDebugKeys(n));
            if (n == 290) {
                boolean bl10 = this.minecraft.options.hideGui = !this.minecraft.options.hideGui;
            }
            if (this.minecraft.getDebugOverlay().showProfilerChart() && !bl2 && n >= 48 && n <= 57) {
                this.minecraft.getDebugOverlay().getProfilerPieChart().profilerPieChartKeyPress(n - 48);
            }
        }
        if (bl7) {
            if (bl9) {
                KeyMapping.set(key, false);
            } else {
                KeyMapping.set(key, true);
                KeyMapping.click(key);
            }
        }
    }

    private void charTyped(long l, int n, int n2) {
        if (l != this.minecraft.getWindow().getWindow()) {
            return;
        }
        Screen screen = this.minecraft.screen;
        if (screen == null || this.minecraft.getOverlay() != null) {
            return;
        }
        try {
            if (Character.isBmpCodePoint(n)) {
                screen.charTyped((char)n, n2);
            } else if (Character.isValidCodePoint(n)) {
                screen.charTyped(Character.highSurrogate(n), n2);
                screen.charTyped(Character.lowSurrogate(n), n2);
            }
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "charTyped event handler");
            screen.fillCrashDetails(crashReport);
            CrashReportCategory crashReportCategory = crashReport.addCategory("Key");
            crashReportCategory.setDetail("Codepoint", n);
            crashReportCategory.setDetail("Mods", n2);
            throw new ReportedException(crashReport);
        }
    }

    public void setup(long l2) {
        InputConstants.setupKeyboardCallbacks(l2, (l, n, n2, n3, n4) -> this.minecraft.execute(() -> this.keyPress(l, n, n2, n3, n4)), (l, n, n2) -> this.minecraft.execute(() -> this.charTyped(l, n, n2)));
    }

    public String getClipboard() {
        return this.clipboardManager.getClipboard(this.minecraft.getWindow().getWindow(), (n, l) -> {
            if (n != 65545) {
                this.minecraft.getWindow().defaultErrorCallback(n, l);
            }
        });
    }

    public void setClipboard(String string) {
        if (!string.isEmpty()) {
            this.clipboardManager.setClipboard(this.minecraft.getWindow().getWindow(), string);
        }
    }

    public void tick() {
        if (this.debugCrashKeyTime > 0L) {
            long l = Util.getMillis();
            long l2 = 10000L - (l - this.debugCrashKeyTime);
            long l3 = l - this.debugCrashKeyReportedTime;
            if (l2 < 0L) {
                if (Screen.hasControlDown()) {
                    Blaze3D.youJustLostTheGame();
                }
                String string = "Manually triggered debug crash";
                CrashReport crashReport = new CrashReport("Manually triggered debug crash", new Throwable("Manually triggered debug crash"));
                CrashReportCategory crashReportCategory = crashReport.addCategory("Manual crash details");
                NativeModuleLister.addCrashSection(crashReportCategory);
                throw new ReportedException(crashReport);
            }
            if (l3 >= 1000L) {
                if (this.debugCrashKeyReportedCount == 0L) {
                    this.debugFeedbackTranslated("debug.crash.message");
                } else {
                    this.debugWarningComponent(Component.translatable("debug.crash.warning", Mth.ceil((float)l2 / 1000.0f)));
                }
                this.debugCrashKeyReportedTime = l;
                ++this.debugCrashKeyReportedCount;
            }
        }
    }
}

