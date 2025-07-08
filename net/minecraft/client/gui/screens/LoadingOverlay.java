/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public class LoadingOverlay
extends Overlay {
    public static final ResourceLocation MOJANG_STUDIOS_LOGO_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/title/mojangstudios.png");
    private static final int LOGO_BACKGROUND_COLOR = ARGB.color(255, 239, 50, 61);
    private static final int LOGO_BACKGROUND_COLOR_DARK = ARGB.color(255, 0, 0, 0);
    private static final IntSupplier BRAND_BACKGROUND = () -> Minecraft.getInstance().options.darkMojangStudiosBackground().get() != false ? LOGO_BACKGROUND_COLOR_DARK : LOGO_BACKGROUND_COLOR;
    private static final int LOGO_SCALE = 240;
    private static final float LOGO_QUARTER_FLOAT = 60.0f;
    private static final int LOGO_QUARTER = 60;
    private static final int LOGO_HALF = 120;
    private static final float LOGO_OVERLAP = 0.0625f;
    private static final float SMOOTHING = 0.95f;
    public static final long FADE_OUT_TIME = 1000L;
    public static final long FADE_IN_TIME = 500L;
    private final Minecraft minecraft;
    private final ReloadInstance reload;
    private final Consumer<Optional<Throwable>> onFinish;
    private final boolean fadeIn;
    private float currentProgress;
    private long fadeOutStart = -1L;
    private long fadeInStart = -1L;

    public LoadingOverlay(Minecraft minecraft, ReloadInstance reloadInstance, Consumer<Optional<Throwable>> consumer, boolean bl) {
        this.minecraft = minecraft;
        this.reload = reloadInstance;
        this.onFinish = consumer;
        this.fadeIn = bl;
    }

    public static void registerTextures(TextureManager textureManager) {
        textureManager.registerAndLoad(MOJANG_STUDIOS_LOGO_LOCATION, new LogoTexture());
    }

    private static int replaceAlpha(int n, int n2) {
        return n & 0xFFFFFF | n2 << 24;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        float f2;
        int n3;
        float f3;
        int n4 = guiGraphics.guiWidth();
        int n5 = guiGraphics.guiHeight();
        long l = Util.getMillis();
        if (this.fadeIn && this.fadeInStart == -1L) {
            this.fadeInStart = l;
        }
        float f4 = this.fadeOutStart > -1L ? (float)(l - this.fadeOutStart) / 1000.0f : -1.0f;
        float f5 = f3 = this.fadeInStart > -1L ? (float)(l - this.fadeInStart) / 500.0f : -1.0f;
        if (f4 >= 1.0f) {
            if (this.minecraft.screen != null) {
                this.minecraft.screen.renderWithTooltip(guiGraphics, 0, 0, f);
            }
            n3 = Mth.ceil((1.0f - Mth.clamp(f4 - 1.0f, 0.0f, 1.0f)) * 255.0f);
            guiGraphics.nextStratum();
            guiGraphics.fill(0, 0, n4, n5, LoadingOverlay.replaceAlpha(BRAND_BACKGROUND.getAsInt(), n3));
            f2 = 1.0f - Mth.clamp(f4 - 1.0f, 0.0f, 1.0f);
        } else if (this.fadeIn) {
            if (this.minecraft.screen != null && f3 < 1.0f) {
                this.minecraft.screen.renderWithTooltip(guiGraphics, n, n2, f);
            }
            n3 = Mth.ceil(Mth.clamp((double)f3, 0.15, 1.0) * 255.0);
            guiGraphics.nextStratum();
            guiGraphics.fill(0, 0, n4, n5, LoadingOverlay.replaceAlpha(BRAND_BACKGROUND.getAsInt(), n3));
            f2 = Mth.clamp(f3, 0.0f, 1.0f);
        } else {
            n3 = BRAND_BACKGROUND.getAsInt();
            RenderSystem.getDevice().createCommandEncoder().clearColorTexture(this.minecraft.getMainRenderTarget().getColorTexture(), n3);
            f2 = 1.0f;
        }
        n3 = (int)((double)guiGraphics.guiWidth() * 0.5);
        int n6 = (int)((double)guiGraphics.guiHeight() * 0.5);
        double d = Math.min((double)guiGraphics.guiWidth() * 0.75, (double)guiGraphics.guiHeight()) * 0.25;
        int n7 = (int)(d * 0.5);
        double d2 = d * 4.0;
        int n8 = (int)(d2 * 0.5);
        int n9 = ARGB.white(f2);
        guiGraphics.blit(RenderPipelines.MOJANG_LOGO, MOJANG_STUDIOS_LOGO_LOCATION, n3 - n8, n6 - n7, -0.0625f, 0.0f, n8, (int)d, 120, 60, 120, 120, n9);
        guiGraphics.blit(RenderPipelines.MOJANG_LOGO, MOJANG_STUDIOS_LOGO_LOCATION, n3, n6 - n7, 0.0625f, 60.0f, n8, (int)d, 120, 60, 120, 120, n9);
        int n10 = (int)((double)guiGraphics.guiHeight() * 0.8325);
        float f6 = this.reload.getActualProgress();
        this.currentProgress = Mth.clamp(this.currentProgress * 0.95f + f6 * 0.050000012f, 0.0f, 1.0f);
        if (f4 < 1.0f) {
            this.drawProgressBar(guiGraphics, n4 / 2 - n8, n10 - 5, n4 / 2 + n8, n10 + 5, 1.0f - Mth.clamp(f4, 0.0f, 1.0f));
        }
        if (f4 >= 2.0f) {
            this.minecraft.setOverlay(null);
        }
        if (this.fadeOutStart == -1L && this.reload.isDone() && (!this.fadeIn || f3 >= 2.0f)) {
            try {
                this.reload.checkExceptions();
                this.onFinish.accept(Optional.empty());
            }
            catch (Throwable throwable) {
                this.onFinish.accept(Optional.of(throwable));
            }
            this.fadeOutStart = Util.getMillis();
            if (this.minecraft.screen != null) {
                this.minecraft.screen.init(this.minecraft, guiGraphics.guiWidth(), guiGraphics.guiHeight());
            }
        }
    }

    private void drawProgressBar(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, float f) {
        int n5 = Mth.ceil((float)(n3 - n - 2) * this.currentProgress);
        int n6 = Math.round(f * 255.0f);
        int n7 = ARGB.color(n6, 255, 255, 255);
        guiGraphics.fill(n + 2, n2 + 2, n + n5, n4 - 2, n7);
        guiGraphics.fill(n + 1, n2, n3 - 1, n2 + 1, n7);
        guiGraphics.fill(n + 1, n4, n3 - 1, n4 - 1, n7);
        guiGraphics.fill(n, n2, n + 1, n4, n7);
        guiGraphics.fill(n3, n2, n3 - 1, n4, n7);
    }

    @Override
    public boolean isPauseScreen() {
        return true;
    }

    static class LogoTexture
    extends ReloadableTexture {
        public LogoTexture() {
            super(MOJANG_STUDIOS_LOGO_LOCATION);
        }

        @Override
        public TextureContents loadContents(ResourceManager resourceManager) throws IOException {
            ResourceProvider resourceProvider = Minecraft.getInstance().getVanillaPackResources().asProvider();
            try (InputStream inputStream = resourceProvider.open(MOJANG_STUDIOS_LOGO_LOCATION);){
                TextureContents textureContents = new TextureContents(NativeImage.read(inputStream), new TextureMetadataSection(true, true));
                return textureContents;
            }
        }
    }
}

