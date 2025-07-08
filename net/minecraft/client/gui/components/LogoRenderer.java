/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.components;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;

public class LogoRenderer {
    public static final ResourceLocation MINECRAFT_LOGO = ResourceLocation.withDefaultNamespace("textures/gui/title/minecraft.png");
    public static final ResourceLocation EASTER_EGG_LOGO = ResourceLocation.withDefaultNamespace("textures/gui/title/minceraft.png");
    public static final ResourceLocation MINECRAFT_EDITION = ResourceLocation.withDefaultNamespace("textures/gui/title/edition.png");
    public static final int LOGO_WIDTH = 256;
    public static final int LOGO_HEIGHT = 44;
    private static final int LOGO_TEXTURE_WIDTH = 256;
    private static final int LOGO_TEXTURE_HEIGHT = 64;
    private static final int EDITION_WIDTH = 128;
    private static final int EDITION_HEIGHT = 14;
    private static final int EDITION_TEXTURE_WIDTH = 128;
    private static final int EDITION_TEXTURE_HEIGHT = 16;
    public static final int DEFAULT_HEIGHT_OFFSET = 30;
    private static final int EDITION_LOGO_OVERLAP = 7;
    private final boolean showEasterEgg = (double)RandomSource.create().nextFloat() < 1.0E-4;
    private final boolean keepLogoThroughFade;

    public LogoRenderer(boolean bl) {
        this.keepLogoThroughFade = bl;
    }

    public void renderLogo(GuiGraphics guiGraphics, int n, float f) {
        this.renderLogo(guiGraphics, n, f, 30);
    }

    public void renderLogo(GuiGraphics guiGraphics, int n, float f, int n2) {
        int n3 = n / 2 - 128;
        float f2 = this.keepLogoThroughFade ? 1.0f : f;
        int n4 = ARGB.white(f2);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, this.showEasterEgg ? EASTER_EGG_LOGO : MINECRAFT_LOGO, n3, n2, 0.0f, 0.0f, 256, 44, 256, 64, n4);
        int n5 = n / 2 - 64;
        int n6 = n2 + 44 - 7;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, MINECRAFT_EDITION, n5, n6, 0.0f, 0.0f, 128, 14, 128, 16, n4);
    }

    public boolean keepLogoThroughFade() {
        return this.keepLogoThroughFade;
    }
}

