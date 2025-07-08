/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Lists
 */
package net.minecraft.client.gui.screens.options;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.VideoMode;
import com.mojang.blaze3d.platform.Window;
import java.util.ArrayList;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.UnsupportedGraphicsWarningScreen;
import net.minecraft.client.renderer.GpuWarnlistManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class VideoSettingsScreen
extends OptionsSubScreen {
    private static final Component TITLE = Component.translatable("options.videoTitle");
    private static final Component FABULOUS = Component.translatable("options.graphics.fabulous").withStyle(ChatFormatting.ITALIC);
    private static final Component WARNING_MESSAGE = Component.translatable("options.graphics.warning.message", FABULOUS, FABULOUS);
    private static final Component WARNING_TITLE = Component.translatable("options.graphics.warning.title").withStyle(ChatFormatting.RED);
    private static final Component BUTTON_ACCEPT = Component.translatable("options.graphics.warning.accept");
    private static final Component BUTTON_CANCEL = Component.translatable("options.graphics.warning.cancel");
    private final GpuWarnlistManager gpuWarnlistManager;
    private final int oldMipmaps;

    private static OptionInstance<?>[] options(Options options) {
        return new OptionInstance[]{options.graphicsMode(), options.renderDistance(), options.prioritizeChunkUpdates(), options.simulationDistance(), options.ambientOcclusion(), options.framerateLimit(), options.enableVsync(), options.inactivityFpsLimit(), options.guiScale(), options.attackIndicator(), options.gamma(), options.cloudStatus(), options.fullscreen(), options.particles(), options.mipmapLevels(), options.entityShadows(), options.screenEffectScale(), options.entityDistanceScaling(), options.fovEffectScale(), options.showAutosaveIndicator(), options.glintSpeed(), options.glintStrength(), options.menuBackgroundBlurriness(), options.bobView(), options.cloudRange()};
    }

    public VideoSettingsScreen(Screen screen, Minecraft minecraft, Options options) {
        super(screen, options, TITLE);
        this.gpuWarnlistManager = minecraft.getGpuWarnlistManager();
        this.gpuWarnlistManager.resetWarnings();
        if (options.graphicsMode().get() == GraphicsStatus.FABULOUS) {
            this.gpuWarnlistManager.dismissWarning();
        }
        this.oldMipmaps = options.mipmapLevels().get();
    }

    @Override
    protected void addOptions() {
        Object object;
        int n2;
        int n3 = -1;
        Window window = this.minecraft.getWindow();
        Monitor monitor = window.findBestMonitor();
        if (monitor == null) {
            n2 = -1;
        } else {
            object = window.getPreferredFullscreenVideoMode();
            n2 = ((Optional)object).map(monitor::getVideoModeIndex).orElse(-1);
        }
        object = new OptionInstance<Integer>("options.fullscreen.resolution", OptionInstance.noTooltip(), (component, n) -> {
            if (monitor == null) {
                return Component.translatable("options.fullscreen.unavailable");
            }
            if (n == -1) {
                return Options.genericValueLabel(component, Component.translatable("options.fullscreen.current"));
            }
            VideoMode videoMode = monitor.getMode((int)n);
            return Options.genericValueLabel(component, Component.translatable("options.fullscreen.entry", videoMode.getWidth(), videoMode.getHeight(), videoMode.getRefreshRate(), videoMode.getRedBits() + videoMode.getGreenBits() + videoMode.getBlueBits()));
        }, new OptionInstance.IntRange(-1, monitor != null ? monitor.getModeCount() - 1 : -1), n2, n -> {
            if (monitor == null) {
                return;
            }
            window.setPreferredFullscreenVideoMode(n == -1 ? Optional.empty() : Optional.of(monitor.getMode((int)n)));
        });
        this.list.addBig((OptionInstance<?>)object);
        this.list.addBig(this.options.biomeBlendRadius());
        this.list.addSmall(VideoSettingsScreen.options(this.options));
    }

    @Override
    public void onClose() {
        this.minecraft.getWindow().changeFullscreenVideoMode();
        super.onClose();
    }

    @Override
    public void removed() {
        if (this.options.mipmapLevels().get() != this.oldMipmaps) {
            this.minecraft.updateMaxMipLevel(this.options.mipmapLevels().get());
            this.minecraft.delayTextureReload();
        }
        super.removed();
    }

    @Override
    public boolean mouseClicked(double d, double d2, int n) {
        if (super.mouseClicked(d, d2, n)) {
            if (this.gpuWarnlistManager.isShowingWarning()) {
                String string;
                String string2;
                ArrayList arrayList = Lists.newArrayList((Object[])new Component[]{WARNING_MESSAGE, CommonComponents.NEW_LINE});
                String string3 = this.gpuWarnlistManager.getRendererWarnings();
                if (string3 != null) {
                    arrayList.add(CommonComponents.NEW_LINE);
                    arrayList.add(Component.translatable("options.graphics.warning.renderer", string3).withStyle(ChatFormatting.GRAY));
                }
                if ((string2 = this.gpuWarnlistManager.getVendorWarnings()) != null) {
                    arrayList.add(CommonComponents.NEW_LINE);
                    arrayList.add(Component.translatable("options.graphics.warning.vendor", string2).withStyle(ChatFormatting.GRAY));
                }
                if ((string = this.gpuWarnlistManager.getVersionWarnings()) != null) {
                    arrayList.add(CommonComponents.NEW_LINE);
                    arrayList.add(Component.translatable("options.graphics.warning.version", string).withStyle(ChatFormatting.GRAY));
                }
                this.minecraft.setScreen(new UnsupportedGraphicsWarningScreen(WARNING_TITLE, arrayList, (ImmutableList<UnsupportedGraphicsWarningScreen.ButtonOption>)ImmutableList.of((Object)new UnsupportedGraphicsWarningScreen.ButtonOption(BUTTON_ACCEPT, button -> {
                    this.options.graphicsMode().set(GraphicsStatus.FABULOUS);
                    Minecraft.getInstance().levelRenderer.allChanged();
                    this.gpuWarnlistManager.dismissWarning();
                    this.minecraft.setScreen(this);
                }), (Object)new UnsupportedGraphicsWarningScreen.ButtonOption(BUTTON_CANCEL, button -> {
                    this.gpuWarnlistManager.dismissWarningAndSkipFabulous();
                    this.minecraft.setScreen(this);
                }))));
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double d, double d2, double d3, double d4) {
        if (Screen.hasControlDown()) {
            OptionInstance<Integer> optionInstance = this.options.guiScale();
            OptionInstance.ValueSet<Integer> valueSet = optionInstance.values();
            if (valueSet instanceof OptionInstance.ClampingLazyMaxIntRange) {
                CycleButton cycleButton;
                OptionInstance.ClampingLazyMaxIntRange clampingLazyMaxIntRange = (OptionInstance.ClampingLazyMaxIntRange)valueSet;
                int n = optionInstance.get();
                int n2 = n == 0 ? clampingLazyMaxIntRange.maxInclusive() + 1 : n;
                int n3 = n2 + (int)Math.signum(d4);
                if (n3 != 0 && n3 <= clampingLazyMaxIntRange.maxInclusive() && n3 >= clampingLazyMaxIntRange.minInclusive() && (cycleButton = (CycleButton)this.list.findOption(optionInstance)) != null) {
                    optionInstance.set(n3);
                    cycleButton.setValue(n3);
                    this.list.setScrollAmount(0.0);
                    return true;
                }
            }
            return false;
        }
        return super.mouseScrolled(d, d2, d3, d4);
    }

    public void updateFullscreenButton(boolean bl) {
        AbstractWidget abstractWidget;
        if (this.list != null && (abstractWidget = this.list.findOption(this.options.fullscreen())) != null) {
            CycleButton cycleButton = (CycleButton)abstractWidget;
            cycleButton.setValue(bl);
        }
    }
}

