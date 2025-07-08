/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.components.spectator;

import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;
import net.minecraft.client.gui.spectator.SpectatorMenuListener;
import net.minecraft.client.gui.spectator.categories.SpectatorPage;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public class SpectatorGui
implements SpectatorMenuListener {
    private static final ResourceLocation HOTBAR_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar");
    private static final ResourceLocation HOTBAR_SELECTION_SPRITE = ResourceLocation.withDefaultNamespace("hud/hotbar_selection");
    private static final long FADE_OUT_DELAY = 5000L;
    private static final long FADE_OUT_TIME = 2000L;
    private final Minecraft minecraft;
    private long lastSelectionTime;
    @Nullable
    private SpectatorMenu menu;

    public SpectatorGui(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    public void onHotbarSelected(int n) {
        this.lastSelectionTime = Util.getMillis();
        if (this.menu != null) {
            this.menu.selectSlot(n);
        } else {
            this.menu = new SpectatorMenu(this);
        }
    }

    private float getHotbarAlpha() {
        long l = this.lastSelectionTime - Util.getMillis() + 5000L;
        return Mth.clamp((float)l / 2000.0f, 0.0f, 1.0f);
    }

    public void renderHotbar(GuiGraphics guiGraphics) {
        if (this.menu == null) {
            return;
        }
        float f = this.getHotbarAlpha();
        if (f <= 0.0f) {
            this.menu.exit();
            return;
        }
        int n = guiGraphics.guiWidth() / 2;
        int n2 = Mth.floor((float)guiGraphics.guiHeight() - 22.0f * f);
        SpectatorPage spectatorPage = this.menu.getCurrentPage();
        this.renderPage(guiGraphics, f, n, n2, spectatorPage);
    }

    protected void renderPage(GuiGraphics guiGraphics, float f, int n, int n2, SpectatorPage spectatorPage) {
        int n3 = ARGB.white(f);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SPRITE, n - 91, n2, 182, 22, n3);
        if (spectatorPage.getSelectedSlot() >= 0) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_SELECTION_SPRITE, n - 91 - 1 + spectatorPage.getSelectedSlot() * 20, n2 - 1, 24, 23, n3);
        }
        for (int i = 0; i < 9; ++i) {
            this.renderSlot(guiGraphics, i, guiGraphics.guiWidth() / 2 - 90 + i * 20 + 2, n2 + 3, f, spectatorPage.getItem(i));
        }
    }

    private void renderSlot(GuiGraphics guiGraphics, int n, int n2, float f, float f2, SpectatorMenuItem spectatorMenuItem) {
        if (spectatorMenuItem != SpectatorMenu.EMPTY_SLOT) {
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate((float)n2, f);
            float f3 = spectatorMenuItem.isEnabled() ? 1.0f : 0.25f;
            spectatorMenuItem.renderIcon(guiGraphics, f3, f2);
            guiGraphics.pose().popMatrix();
            if (f2 > 0.0f && spectatorMenuItem.isEnabled()) {
                Component component = this.minecraft.options.keyHotbarSlots[n].getTranslatedKeyMessage();
                guiGraphics.drawString(this.minecraft.font, component, n2 + 19 - 2 - this.minecraft.font.width(component), (int)f + 6 + 3, ARGB.color(f2, -1));
            }
        }
    }

    public void renderAction(GuiGraphics guiGraphics) {
        float f = this.getHotbarAlpha();
        if (f > 0.0f && this.menu != null) {
            SpectatorMenuItem spectatorMenuItem = this.menu.getSelectedItem();
            Component component = spectatorMenuItem == SpectatorMenu.EMPTY_SLOT ? this.menu.getSelectedCategory().getPrompt() : spectatorMenuItem.getName();
            int n = this.minecraft.font.width(component);
            int n2 = (guiGraphics.guiWidth() - n) / 2;
            int n3 = guiGraphics.guiHeight() - 35;
            guiGraphics.drawStringWithBackdrop(this.minecraft.font, component, n2, n3, n, ARGB.color(f, -1));
        }
    }

    @Override
    public void onSpectatorMenuClosed(SpectatorMenu spectatorMenu) {
        this.menu = null;
        this.lastSelectionTime = 0L;
    }

    public boolean isMenuActive() {
        return this.menu != null;
    }

    public void onMouseScrolled(int n) {
        int n2;
        for (n2 = this.menu.getSelectedSlot() + n; !(n2 < 0 || n2 > 8 || this.menu.getItem(n2) != SpectatorMenu.EMPTY_SLOT && this.menu.getItem(n2).isEnabled()); n2 += n) {
        }
        if (n2 >= 0 && n2 <= 8) {
            this.menu.selectSlot(n2);
            this.lastSelectionTime = Util.getMillis();
        }
    }

    public void onMouseMiddleClick() {
        this.lastSelectionTime = Util.getMillis();
        if (this.isMenuActive()) {
            int n = this.menu.getSelectedSlot();
            if (n != -1) {
                this.menu.selectSlot(n);
            }
        } else {
            this.menu = new SpectatorMenu(this);
        }
    }
}

