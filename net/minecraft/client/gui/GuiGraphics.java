/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.joml.Matrix3x2f
 *  org.joml.Matrix3x2fStack
 *  org.joml.Matrix3x2fc
 *  org.joml.Quaternionf
 *  org.joml.Vector2ic
 *  org.joml.Vector3f
 */
package net.minecraft.client.gui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiSpriteManager;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.BlitRenderState;
import net.minecraft.client.gui.render.state.ColoredRectangleRenderState;
import net.minecraft.client.gui.render.state.GuiItemRenderState;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.GuiTextRenderState;
import net.minecraft.client.gui.render.state.pip.GuiBannerResultRenderState;
import net.minecraft.client.gui.render.state.pip.GuiBookModelRenderState;
import net.minecraft.client.gui.render.state.pip.GuiEntityRenderState;
import net.minecraft.client.gui.render.state.pip.GuiProfilerChartRenderState;
import net.minecraft.client.gui.render.state.pip.GuiSignRenderState;
import net.minecraft.client.gui.render.state.pip.GuiSkinRenderState;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.core.component.DataComponents;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ResultField;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix3x2fc;
import org.joml.Quaternionf;
import org.joml.Vector2ic;
import org.joml.Vector3f;

public class GuiGraphics {
    private static final int EXTRA_SPACE_AFTER_FIRST_TOOLTIP_LINE = 2;
    private final Minecraft minecraft;
    private final Matrix3x2fStack pose;
    private final ScissorStack scissorStack = new ScissorStack();
    private final GuiSpriteManager sprites;
    private final GuiRenderState guiRenderState;
    @Nullable
    private Runnable deferredTooltip;

    private GuiGraphics(Minecraft minecraft, Matrix3x2fStack matrix3x2fStack, GuiRenderState guiRenderState) {
        this.minecraft = minecraft;
        this.pose = matrix3x2fStack;
        this.sprites = minecraft.getGuiSprites();
        this.guiRenderState = guiRenderState;
    }

    public GuiGraphics(Minecraft minecraft, GuiRenderState guiRenderState) {
        this(minecraft, new Matrix3x2fStack(16), guiRenderState);
    }

    public int guiWidth() {
        return this.minecraft.getWindow().getGuiScaledWidth();
    }

    public int guiHeight() {
        return this.minecraft.getWindow().getGuiScaledHeight();
    }

    public void nextStratum() {
        this.guiRenderState.nextStratum();
    }

    public void blurBeforeThisStratum() {
        this.guiRenderState.blurBeforeThisStratum();
    }

    public Matrix3x2fStack pose() {
        return this.pose;
    }

    public void hLine(int n, int n2, int n3, int n4) {
        if (n2 < n) {
            int n5 = n;
            n = n2;
            n2 = n5;
        }
        this.fill(n, n3, n2 + 1, n3 + 1, n4);
    }

    public void vLine(int n, int n2, int n3, int n4) {
        if (n3 < n2) {
            int n5 = n2;
            n2 = n3;
            n3 = n5;
        }
        this.fill(n, n2 + 1, n + 1, n3, n4);
    }

    public void enableScissor(int n, int n2, int n3, int n4) {
        ScreenRectangle screenRectangle = new ScreenRectangle(n, n2, n3 - n, n4 - n2).transformAxisAligned((Matrix3x2f)this.pose);
        this.scissorStack.push(screenRectangle);
    }

    public void disableScissor() {
        this.scissorStack.pop();
    }

    public boolean containsPointInScissor(int n, int n2) {
        return this.scissorStack.containsPoint(n, n2);
    }

    public void fill(int n, int n2, int n3, int n4, int n5) {
        this.fill(RenderPipelines.GUI, n, n2, n3, n4, n5);
    }

    public void fill(RenderPipeline renderPipeline, int n, int n2, int n3, int n4, int n5) {
        int n6;
        if (n < n3) {
            n6 = n;
            n = n3;
            n3 = n6;
        }
        if (n2 < n4) {
            n6 = n2;
            n2 = n4;
            n4 = n6;
        }
        this.submitColoredRectangle(renderPipeline, TextureSetup.noTexture(), n, n2, n3, n4, n5, null);
    }

    public void fillGradient(int n, int n2, int n3, int n4, int n5, int n6) {
        this.submitColoredRectangle(RenderPipelines.GUI, TextureSetup.noTexture(), n, n2, n3, n4, n5, n6);
    }

    public void fill(RenderPipeline renderPipeline, TextureSetup textureSetup, int n, int n2, int n3, int n4) {
        this.submitColoredRectangle(renderPipeline, textureSetup, n, n2, n3, n4, -1, null);
    }

    private void submitColoredRectangle(RenderPipeline renderPipeline, TextureSetup textureSetup, int n, int n2, int n3, int n4, int n5, @Nullable Integer n6) {
        this.guiRenderState.submitGuiElement(new ColoredRectangleRenderState(renderPipeline, textureSetup, new Matrix3x2f((Matrix3x2fc)this.pose), n, n2, n3, n4, n5, n6 != null ? n6 : n5, this.scissorStack.peek()));
    }

    public void textHighlight(int n, int n2, int n3, int n4) {
        this.fill(RenderPipelines.GUI_INVERT, n, n2, n3, n4, -1);
        this.fill(RenderPipelines.GUI_TEXT_HIGHLIGHT, n, n2, n3, n4, -16776961);
    }

    public void drawCenteredString(Font font, String string, int n, int n2, int n3) {
        this.drawString(font, string, n - font.width(string) / 2, n2, n3);
    }

    public void drawCenteredString(Font font, Component component, int n, int n2, int n3) {
        FormattedCharSequence formattedCharSequence = component.getVisualOrderText();
        this.drawString(font, formattedCharSequence, n - font.width(formattedCharSequence) / 2, n2, n3);
    }

    public void drawCenteredString(Font font, FormattedCharSequence formattedCharSequence, int n, int n2, int n3) {
        this.drawString(font, formattedCharSequence, n - font.width(formattedCharSequence) / 2, n2, n3);
    }

    public void drawString(Font font, @Nullable String string, int n, int n2, int n3) {
        this.drawString(font, string, n, n2, n3, true);
    }

    public void drawString(Font font, @Nullable String string, int n, int n2, int n3, boolean bl) {
        if (string == null) {
            return;
        }
        this.drawString(font, Language.getInstance().getVisualOrder(FormattedText.of(string)), n, n2, n3, bl);
    }

    public void drawString(Font font, FormattedCharSequence formattedCharSequence, int n, int n2, int n3) {
        this.drawString(font, formattedCharSequence, n, n2, n3, true);
    }

    public void drawString(Font font, FormattedCharSequence formattedCharSequence, int n, int n2, int n3, boolean bl) {
        if (ARGB.alpha(n3) == 0) {
            return;
        }
        this.guiRenderState.submitText(new GuiTextRenderState(font, formattedCharSequence, new Matrix3x2f((Matrix3x2fc)this.pose), n, n2, n3, 0, bl, this.scissorStack.peek()));
    }

    public void drawString(Font font, Component component, int n, int n2, int n3) {
        this.drawString(font, component, n, n2, n3, true);
    }

    public void drawString(Font font, Component component, int n, int n2, int n3, boolean bl) {
        this.drawString(font, component.getVisualOrderText(), n, n2, n3, bl);
    }

    public void drawWordWrap(Font font, FormattedText formattedText, int n, int n2, int n3, int n4) {
        this.drawWordWrap(font, formattedText, n, n2, n3, n4, true);
    }

    public void drawWordWrap(Font font, FormattedText formattedText, int n, int n2, int n3, int n4, boolean bl) {
        for (FormattedCharSequence formattedCharSequence : font.split(formattedText, n3)) {
            this.drawString(font, formattedCharSequence, n, n2, n4, bl);
            n2 += font.lineHeight;
        }
    }

    public void drawStringWithBackdrop(Font font, Component component, int n, int n2, int n3, int n4) {
        int n5 = this.minecraft.options.getBackgroundColor(0.0f);
        if (n5 != 0) {
            int n6 = 2;
            this.fill(n - 2, n2 - 2, n + n3 + 2, n2 + font.lineHeight + 2, ARGB.multiply(n5, n4));
        }
        this.drawString(font, component, n, n2, n4, true);
    }

    public void renderOutline(int n, int n2, int n3, int n4, int n5) {
        this.fill(n, n2, n + n3, n2 + 1, n5);
        this.fill(n, n2 + n4 - 1, n + n3, n2 + n4, n5);
        this.fill(n, n2 + 1, n + 1, n2 + n4 - 1, n5);
        this.fill(n + n3 - 1, n2 + 1, n + n3, n2 + n4 - 1, n5);
    }

    public void blitSprite(RenderPipeline renderPipeline, ResourceLocation resourceLocation, int n, int n2, int n3, int n4) {
        this.blitSprite(renderPipeline, resourceLocation, n, n2, n3, n4, -1);
    }

    public void blitSprite(RenderPipeline renderPipeline, ResourceLocation resourceLocation, int n, int n2, int n3, int n4, float f) {
        this.blitSprite(renderPipeline, resourceLocation, n, n2, n3, n4, ARGB.color(f, -1));
    }

    public void blitSprite(RenderPipeline renderPipeline, ResourceLocation resourceLocation, int n, int n2, int n3, int n4, int n5) {
        TextureAtlasSprite textureAtlasSprite = this.sprites.getSprite(resourceLocation);
        GuiSpriteScaling guiSpriteScaling = this.sprites.getSpriteScaling(textureAtlasSprite);
        if (guiSpriteScaling instanceof GuiSpriteScaling.Stretch) {
            this.blitSprite(renderPipeline, textureAtlasSprite, n, n2, n3, n4, n5);
        } else if (guiSpriteScaling instanceof GuiSpriteScaling.Tile) {
            GuiSpriteScaling.Tile tile = (GuiSpriteScaling.Tile)guiSpriteScaling;
            this.blitTiledSprite(renderPipeline, textureAtlasSprite, n, n2, n3, n4, 0, 0, tile.width(), tile.height(), tile.width(), tile.height(), n5);
        } else if (guiSpriteScaling instanceof GuiSpriteScaling.NineSlice) {
            GuiSpriteScaling.NineSlice nineSlice = (GuiSpriteScaling.NineSlice)guiSpriteScaling;
            this.blitNineSlicedSprite(renderPipeline, textureAtlasSprite, nineSlice, n, n2, n3, n4, n5);
        }
    }

    public void blitSprite(RenderPipeline renderPipeline, ResourceLocation resourceLocation, int n, int n2, int n3, int n4, int n5, int n6, int n7, int n8) {
        this.blitSprite(renderPipeline, resourceLocation, n, n2, n3, n4, n5, n6, n7, n8, -1);
    }

    public void blitSprite(RenderPipeline renderPipeline, ResourceLocation resourceLocation, int n, int n2, int n3, int n4, int n5, int n6, int n7, int n8, int n9) {
        TextureAtlasSprite textureAtlasSprite = this.sprites.getSprite(resourceLocation);
        GuiSpriteScaling guiSpriteScaling = this.sprites.getSpriteScaling(textureAtlasSprite);
        if (guiSpriteScaling instanceof GuiSpriteScaling.Stretch) {
            this.blitSprite(renderPipeline, textureAtlasSprite, n, n2, n3, n4, n5, n6, n7, n8, n9);
        } else {
            this.enableScissor(n5, n6, n5 + n7, n6 + n8);
            this.blitSprite(renderPipeline, resourceLocation, n5 - n3, n6 - n4, n, n2, n9);
            this.disableScissor();
        }
    }

    public void blitSprite(RenderPipeline renderPipeline, TextureAtlasSprite textureAtlasSprite, int n, int n2, int n3, int n4) {
        this.blitSprite(renderPipeline, textureAtlasSprite, n, n2, n3, n4, -1);
    }

    public void blitSprite(RenderPipeline renderPipeline, TextureAtlasSprite textureAtlasSprite, int n, int n2, int n3, int n4, int n5) {
        if (n3 == 0 || n4 == 0) {
            return;
        }
        this.innerBlit(renderPipeline, textureAtlasSprite.atlasLocation(), n, n + n3, n2, n2 + n4, textureAtlasSprite.getU0(), textureAtlasSprite.getU1(), textureAtlasSprite.getV0(), textureAtlasSprite.getV1(), n5);
    }

    private void blitSprite(RenderPipeline renderPipeline, TextureAtlasSprite textureAtlasSprite, int n, int n2, int n3, int n4, int n5, int n6, int n7, int n8, int n9) {
        if (n7 == 0 || n8 == 0) {
            return;
        }
        this.innerBlit(renderPipeline, textureAtlasSprite.atlasLocation(), n5, n5 + n7, n6, n6 + n8, textureAtlasSprite.getU((float)n3 / (float)n), textureAtlasSprite.getU((float)(n3 + n7) / (float)n), textureAtlasSprite.getV((float)n4 / (float)n2), textureAtlasSprite.getV((float)(n4 + n8) / (float)n2), n9);
    }

    private void blitNineSlicedSprite(RenderPipeline renderPipeline, TextureAtlasSprite textureAtlasSprite, GuiSpriteScaling.NineSlice nineSlice, int n, int n2, int n3, int n4, int n5) {
        GuiSpriteScaling.NineSlice.Border border = nineSlice.border();
        int n6 = Math.min(border.left(), n3 / 2);
        int n7 = Math.min(border.right(), n3 / 2);
        int n8 = Math.min(border.top(), n4 / 2);
        int n9 = Math.min(border.bottom(), n4 / 2);
        if (n3 == nineSlice.width() && n4 == nineSlice.height()) {
            this.blitSprite(renderPipeline, textureAtlasSprite, nineSlice.width(), nineSlice.height(), 0, 0, n, n2, n3, n4, n5);
            return;
        }
        if (n4 == nineSlice.height()) {
            this.blitSprite(renderPipeline, textureAtlasSprite, nineSlice.width(), nineSlice.height(), 0, 0, n, n2, n6, n4, n5);
            this.blitNineSliceInnerSegment(renderPipeline, nineSlice, textureAtlasSprite, n + n6, n2, n3 - n7 - n6, n4, n6, 0, nineSlice.width() - n7 - n6, nineSlice.height(), nineSlice.width(), nineSlice.height(), n5);
            this.blitSprite(renderPipeline, textureAtlasSprite, nineSlice.width(), nineSlice.height(), nineSlice.width() - n7, 0, n + n3 - n7, n2, n7, n4, n5);
            return;
        }
        if (n3 == nineSlice.width()) {
            this.blitSprite(renderPipeline, textureAtlasSprite, nineSlice.width(), nineSlice.height(), 0, 0, n, n2, n3, n8, n5);
            this.blitNineSliceInnerSegment(renderPipeline, nineSlice, textureAtlasSprite, n, n2 + n8, n3, n4 - n9 - n8, 0, n8, nineSlice.width(), nineSlice.height() - n9 - n8, nineSlice.width(), nineSlice.height(), n5);
            this.blitSprite(renderPipeline, textureAtlasSprite, nineSlice.width(), nineSlice.height(), 0, nineSlice.height() - n9, n, n2 + n4 - n9, n3, n9, n5);
            return;
        }
        this.blitSprite(renderPipeline, textureAtlasSprite, nineSlice.width(), nineSlice.height(), 0, 0, n, n2, n6, n8, n5);
        this.blitNineSliceInnerSegment(renderPipeline, nineSlice, textureAtlasSprite, n + n6, n2, n3 - n7 - n6, n8, n6, 0, nineSlice.width() - n7 - n6, n8, nineSlice.width(), nineSlice.height(), n5);
        this.blitSprite(renderPipeline, textureAtlasSprite, nineSlice.width(), nineSlice.height(), nineSlice.width() - n7, 0, n + n3 - n7, n2, n7, n8, n5);
        this.blitSprite(renderPipeline, textureAtlasSprite, nineSlice.width(), nineSlice.height(), 0, nineSlice.height() - n9, n, n2 + n4 - n9, n6, n9, n5);
        this.blitNineSliceInnerSegment(renderPipeline, nineSlice, textureAtlasSprite, n + n6, n2 + n4 - n9, n3 - n7 - n6, n9, n6, nineSlice.height() - n9, nineSlice.width() - n7 - n6, n9, nineSlice.width(), nineSlice.height(), n5);
        this.blitSprite(renderPipeline, textureAtlasSprite, nineSlice.width(), nineSlice.height(), nineSlice.width() - n7, nineSlice.height() - n9, n + n3 - n7, n2 + n4 - n9, n7, n9, n5);
        this.blitNineSliceInnerSegment(renderPipeline, nineSlice, textureAtlasSprite, n, n2 + n8, n6, n4 - n9 - n8, 0, n8, n6, nineSlice.height() - n9 - n8, nineSlice.width(), nineSlice.height(), n5);
        this.blitNineSliceInnerSegment(renderPipeline, nineSlice, textureAtlasSprite, n + n6, n2 + n8, n3 - n7 - n6, n4 - n9 - n8, n6, n8, nineSlice.width() - n7 - n6, nineSlice.height() - n9 - n8, nineSlice.width(), nineSlice.height(), n5);
        this.blitNineSliceInnerSegment(renderPipeline, nineSlice, textureAtlasSprite, n + n3 - n7, n2 + n8, n7, n4 - n9 - n8, nineSlice.width() - n7, n8, n7, nineSlice.height() - n9 - n8, nineSlice.width(), nineSlice.height(), n5);
    }

    private void blitNineSliceInnerSegment(RenderPipeline renderPipeline, GuiSpriteScaling.NineSlice nineSlice, TextureAtlasSprite textureAtlasSprite, int n, int n2, int n3, int n4, int n5, int n6, int n7, int n8, int n9, int n10, int n11) {
        if (n3 <= 0 || n4 <= 0) {
            return;
        }
        if (nineSlice.stretchInner()) {
            this.innerBlit(renderPipeline, textureAtlasSprite.atlasLocation(), n, n + n3, n2, n2 + n4, textureAtlasSprite.getU((float)n5 / (float)n9), textureAtlasSprite.getU((float)(n5 + n7) / (float)n9), textureAtlasSprite.getV((float)n6 / (float)n10), textureAtlasSprite.getV((float)(n6 + n8) / (float)n10), n11);
        } else {
            this.blitTiledSprite(renderPipeline, textureAtlasSprite, n, n2, n3, n4, n5, n6, n7, n8, n9, n10, n11);
        }
    }

    private void blitTiledSprite(RenderPipeline renderPipeline, TextureAtlasSprite textureAtlasSprite, int n, int n2, int n3, int n4, int n5, int n6, int n7, int n8, int n9, int n10, int n11) {
        if (n3 <= 0 || n4 <= 0) {
            return;
        }
        if (n7 <= 0 || n8 <= 0) {
            throw new IllegalArgumentException("Tiled sprite texture size must be positive, got " + n7 + "x" + n8);
        }
        for (int i = 0; i < n3; i += n7) {
            int n12 = Math.min(n7, n3 - i);
            for (int j = 0; j < n4; j += n8) {
                int n13 = Math.min(n8, n4 - j);
                this.blitSprite(renderPipeline, textureAtlasSprite, n9, n10, n5, n6, n + i, n2 + j, n12, n13, n11);
            }
        }
    }

    public void blit(RenderPipeline renderPipeline, ResourceLocation resourceLocation, int n, int n2, float f, float f2, int n3, int n4, int n5, int n6, int n7) {
        this.blit(renderPipeline, resourceLocation, n, n2, f, f2, n3, n4, n3, n4, n5, n6, n7);
    }

    public void blit(RenderPipeline renderPipeline, ResourceLocation resourceLocation, int n, int n2, float f, float f2, int n3, int n4, int n5, int n6) {
        this.blit(renderPipeline, resourceLocation, n, n2, f, f2, n3, n4, n3, n4, n5, n6);
    }

    public void blit(RenderPipeline renderPipeline, ResourceLocation resourceLocation, int n, int n2, float f, float f2, int n3, int n4, int n5, int n6, int n7, int n8) {
        this.blit(renderPipeline, resourceLocation, n, n2, f, f2, n3, n4, n5, n6, n7, n8, -1);
    }

    public void blit(RenderPipeline renderPipeline, ResourceLocation resourceLocation, int n, int n2, float f, float f2, int n3, int n4, int n5, int n6, int n7, int n8, int n9) {
        this.innerBlit(renderPipeline, resourceLocation, n, n + n3, n2, n2 + n4, (f + 0.0f) / (float)n7, (f + (float)n5) / (float)n7, (f2 + 0.0f) / (float)n8, (f2 + (float)n6) / (float)n8, n9);
    }

    public void blit(ResourceLocation resourceLocation, int n, int n2, int n3, int n4, float f, float f2, float f3, float f4) {
        this.innerBlit(RenderPipelines.GUI_TEXTURED, resourceLocation, n, n3, n2, n4, f, f2, f3, f4, -1);
    }

    private void innerBlit(RenderPipeline renderPipeline, ResourceLocation resourceLocation, int n, int n2, int n3, int n4, float f, float f2, float f3, float f4, int n5) {
        GpuTextureView gpuTextureView = this.minecraft.getTextureManager().getTexture(resourceLocation).getTextureView();
        this.submitBlit(renderPipeline, gpuTextureView, n, n3, n2, n4, f, f2, f3, f4, n5);
    }

    private void submitBlit(RenderPipeline renderPipeline, GpuTextureView gpuTextureView, int n, int n2, int n3, int n4, float f, float f2, float f3, float f4, int n5) {
        this.guiRenderState.submitGuiElement(new BlitRenderState(renderPipeline, TextureSetup.singleTexture(gpuTextureView), new Matrix3x2f((Matrix3x2fc)this.pose), n, n2, n3, n4, f, f2, f3, f4, n5, this.scissorStack.peek()));
    }

    public void renderItem(ItemStack itemStack, int n, int n2) {
        this.renderItem(this.minecraft.player, this.minecraft.level, itemStack, n, n2, 0);
    }

    public void renderItem(ItemStack itemStack, int n, int n2, int n3) {
        this.renderItem(this.minecraft.player, this.minecraft.level, itemStack, n, n2, n3);
    }

    public void renderFakeItem(ItemStack itemStack, int n, int n2) {
        this.renderFakeItem(itemStack, n, n2, 0);
    }

    public void renderFakeItem(ItemStack itemStack, int n, int n2, int n3) {
        this.renderItem(null, this.minecraft.level, itemStack, n, n2, n3);
    }

    public void renderItem(LivingEntity livingEntity, ItemStack itemStack, int n, int n2, int n3) {
        this.renderItem(livingEntity, livingEntity.level(), itemStack, n, n2, n3);
    }

    private void renderItem(@Nullable LivingEntity livingEntity, @Nullable Level level, ItemStack itemStack, int n, int n2, int n3) {
        if (itemStack.isEmpty()) {
            return;
        }
        TrackingItemStackRenderState trackingItemStackRenderState = new TrackingItemStackRenderState();
        this.minecraft.getItemModelResolver().updateForTopItem(trackingItemStackRenderState, itemStack, ItemDisplayContext.GUI, level, livingEntity, n3);
        try {
            this.guiRenderState.submitItem(new GuiItemRenderState(itemStack.getItem().getName().toString(), new Matrix3x2f((Matrix3x2fc)this.pose), trackingItemStackRenderState, n, n2, this.scissorStack.peek()));
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Rendering item");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Item being rendered");
            crashReportCategory.setDetail("Item Type", () -> String.valueOf(itemStack.getItem()));
            crashReportCategory.setDetail("Item Components", () -> String.valueOf(itemStack.getComponents()));
            crashReportCategory.setDetail("Item Foil", () -> String.valueOf(itemStack.hasFoil()));
            throw new ReportedException(crashReport);
        }
    }

    public void renderItemDecorations(Font font, ItemStack itemStack, int n, int n2) {
        this.renderItemDecorations(font, itemStack, n, n2, null);
    }

    public void renderItemDecorations(Font font, ItemStack itemStack, int n, int n2, @Nullable String string) {
        if (itemStack.isEmpty()) {
            return;
        }
        this.pose.pushMatrix();
        this.renderItemBar(itemStack, n, n2);
        this.renderItemCooldown(itemStack, n, n2);
        this.renderItemCount(font, itemStack, n, n2, string);
        this.pose.popMatrix();
    }

    public void setTooltipForNextFrame(Component component, int n, int n2) {
        this.setTooltipForNextFrame(List.of(component.getVisualOrderText()), n, n2);
    }

    public void setTooltipForNextFrame(List<FormattedCharSequence> list, int n, int n2) {
        this.setTooltipForNextFrame(this.minecraft.font, list, DefaultTooltipPositioner.INSTANCE, n, n2, false);
    }

    public void setTooltipForNextFrame(Font font, ItemStack itemStack, int n, int n2) {
        this.setTooltipForNextFrame(font, Screen.getTooltipFromItem(this.minecraft, itemStack), itemStack.getTooltipImage(), n, n2, itemStack.get(DataComponents.TOOLTIP_STYLE));
    }

    public void setTooltipForNextFrame(Font font, List<Component> list, Optional<TooltipComponent> optional, int n, int n2) {
        this.setTooltipForNextFrame(font, list, optional, n, n2, null);
    }

    public void setTooltipForNextFrame(Font font, List<Component> list, Optional<TooltipComponent> optional, int n, int n2, @Nullable ResourceLocation resourceLocation) {
        List<ClientTooltipComponent> list2 = list.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).collect(Util.toMutableList());
        optional.ifPresent(tooltipComponent -> list2.add(list2.isEmpty() ? 0 : 1, ClientTooltipComponent.create(tooltipComponent)));
        this.setTooltipForNextFrameInternal(font, list2, n, n2, DefaultTooltipPositioner.INSTANCE, resourceLocation, false);
    }

    public void setTooltipForNextFrame(Font font, Component component, int n, int n2) {
        this.setTooltipForNextFrame(font, component, n, n2, null);
    }

    public void setTooltipForNextFrame(Font font, Component component, int n, int n2, @Nullable ResourceLocation resourceLocation) {
        this.setTooltipForNextFrame(font, List.of(component.getVisualOrderText()), n, n2, resourceLocation);
    }

    public void setComponentTooltipForNextFrame(Font font, List<Component> list, int n, int n2) {
        this.setComponentTooltipForNextFrame(font, list, n, n2, null);
    }

    public void setComponentTooltipForNextFrame(Font font, List<Component> list, int n, int n2, @Nullable ResourceLocation resourceLocation) {
        this.setTooltipForNextFrameInternal(font, list.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).toList(), n, n2, DefaultTooltipPositioner.INSTANCE, resourceLocation, false);
    }

    public void setTooltipForNextFrame(Font font, List<? extends FormattedCharSequence> list, int n, int n2) {
        this.setTooltipForNextFrame(font, list, n, n2, null);
    }

    public void setTooltipForNextFrame(Font font, List<? extends FormattedCharSequence> list, int n, int n2, @Nullable ResourceLocation resourceLocation) {
        this.setTooltipForNextFrameInternal(font, list.stream().map(ClientTooltipComponent::create).collect(Collectors.toList()), n, n2, DefaultTooltipPositioner.INSTANCE, resourceLocation, false);
    }

    public void setTooltipForNextFrame(Font font, List<FormattedCharSequence> list, ClientTooltipPositioner clientTooltipPositioner, int n, int n2, boolean bl) {
        this.setTooltipForNextFrameInternal(font, list.stream().map(ClientTooltipComponent::create).collect(Collectors.toList()), n, n2, clientTooltipPositioner, null, bl);
    }

    private void setTooltipForNextFrameInternal(Font font, List<ClientTooltipComponent> list, int n, int n2, ClientTooltipPositioner clientTooltipPositioner, @Nullable ResourceLocation resourceLocation, boolean bl) {
        if (list.isEmpty()) {
            return;
        }
        if (this.deferredTooltip == null || bl) {
            this.deferredTooltip = () -> this.renderTooltip(font, list, n, n2, clientTooltipPositioner, resourceLocation);
        }
    }

    public void renderTooltip(Font font, List<ClientTooltipComponent> list, int n, int n2, ClientTooltipPositioner clientTooltipPositioner, @Nullable ResourceLocation resourceLocation) {
        ClientTooltipComponent clientTooltipComponent;
        int n3;
        int n4 = 0;
        int n5 = list.size() == 1 ? -2 : 0;
        for (ClientTooltipComponent clientTooltipComponent2 : list) {
            int n6 = clientTooltipComponent2.getWidth(font);
            if (n6 > n4) {
                n4 = n6;
            }
            n5 += clientTooltipComponent2.getHeight(font);
        }
        int n7 = n4;
        int n8 = n5;
        Vector2ic vector2ic = clientTooltipPositioner.positionTooltip(this.guiWidth(), this.guiHeight(), n, n2, n7, n8);
        int n9 = vector2ic.x();
        int n10 = vector2ic.y();
        this.pose.pushMatrix();
        TooltipRenderUtil.renderTooltipBackground(this, n9, n10, n7, n8, resourceLocation);
        int n11 = n10;
        for (n3 = 0; n3 < list.size(); ++n3) {
            clientTooltipComponent = list.get(n3);
            clientTooltipComponent.renderText(this, font, n9, n11);
            n11 += clientTooltipComponent.getHeight(font) + (n3 == 0 ? 2 : 0);
        }
        n11 = n10;
        for (n3 = 0; n3 < list.size(); ++n3) {
            clientTooltipComponent = list.get(n3);
            clientTooltipComponent.renderImage(font, n9, n11, n7, n8, this);
            n11 += clientTooltipComponent.getHeight(font) + (n3 == 0 ? 2 : 0);
        }
        this.pose.popMatrix();
    }

    public void renderDeferredTooltip() {
        if (this.deferredTooltip != null) {
            this.nextStratum();
            this.deferredTooltip.run();
            this.deferredTooltip = null;
        }
    }

    private void renderItemBar(ItemStack itemStack, int n, int n2) {
        if (itemStack.isBarVisible()) {
            int n3 = n + 2;
            int n4 = n2 + 13;
            this.fill(RenderPipelines.GUI, n3, n4, n3 + 13, n4 + 2, -16777216);
            this.fill(RenderPipelines.GUI, n3, n4, n3 + itemStack.getBarWidth(), n4 + 1, ARGB.opaque(itemStack.getBarColor()));
        }
    }

    private void renderItemCount(Font font, ItemStack itemStack, int n, int n2, @Nullable String string) {
        if (itemStack.getCount() != 1 || string != null) {
            String string2 = string == null ? String.valueOf(itemStack.getCount()) : string;
            this.drawString(font, string2, n + 19 - 2 - font.width(string2), n2 + 6 + 3, -1, true);
        }
    }

    private void renderItemCooldown(ItemStack itemStack, int n, int n2) {
        float f;
        LocalPlayer localPlayer = this.minecraft.player;
        float f2 = f = localPlayer == null ? 0.0f : localPlayer.getCooldowns().getCooldownPercent(itemStack, this.minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(true));
        if (f > 0.0f) {
            int n3 = n2 + Mth.floor(16.0f * (1.0f - f));
            int n4 = n3 + Mth.ceil(16.0f * f);
            this.fill(RenderPipelines.GUI, n, n3, n + 16, n4, Integer.MAX_VALUE);
        }
    }

    /*
     * WARNING - Removed back jump from a try to a catch block - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void renderComponentHoverEffect(Font font, @Nullable Style style, int n, int n2) {
        if (style == null) return;
        if (style.getHoverEvent() == null) {
            return;
        }
        HoverEvent hoverEvent = style.getHoverEvent();
        Objects.requireNonNull(hoverEvent);
        HoverEvent hoverEvent2 = hoverEvent;
        int n3 = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{HoverEvent.ShowItem.class, HoverEvent.ShowEntity.class, HoverEvent.ShowText.class}, (Object)hoverEvent2, n3)) {
            case 0: {
                HoverEvent.ShowItem showItem = (HoverEvent.ShowItem)hoverEvent2;
                try {
                    ItemStack itemStack;
                    ItemStack itemStack2 = itemStack = showItem.item();
                    this.setTooltipForNextFrame(font, itemStack2, n, n2);
                    return;
                }
                catch (Throwable throwable) {
                    throw new MatchException(throwable.toString(), throwable);
                }
            }
            case 1: {
                HoverEvent.ShowEntity showEntity = (HoverEvent.ShowEntity)hoverEvent2;
                {
                    HoverEvent.EntityTooltipInfo entityTooltipInfo;
                    HoverEvent.EntityTooltipInfo entityTooltipInfo2 = entityTooltipInfo = showEntity.entity();
                    if (!this.minecraft.options.advancedItemTooltips) return;
                    this.setComponentTooltipForNextFrame(font, entityTooltipInfo2.getTooltipLines(), n, n2);
                    return;
                }
            }
            case 2: {
                HoverEvent.ShowText showText = (HoverEvent.ShowText)hoverEvent2;
                {
                    Component component;
                    Component component2 = component = showText.value();
                    this.setTooltipForNextFrame(font, font.split(component2, Math.max(this.guiWidth() / 2, 200)), n, n2);
                    return;
                }
            }
        }
    }

    public void submitMapRenderState(MapRenderState mapRenderState) {
        Minecraft minecraft = Minecraft.getInstance();
        TextureManager textureManager = minecraft.getTextureManager();
        GpuTextureView gpuTextureView = textureManager.getTexture(mapRenderState.texture).getTextureView();
        this.submitBlit(RenderPipelines.GUI_TEXTURED, gpuTextureView, 0, 0, 128, 128, 0.0f, 1.0f, 0.0f, 1.0f, -1);
        for (MapRenderState.MapDecorationRenderState mapDecorationRenderState : mapRenderState.decorations) {
            Object object;
            if (!mapDecorationRenderState.renderOnFrame) continue;
            this.pose.pushMatrix();
            this.pose.translate((float)mapDecorationRenderState.x / 2.0f + 64.0f, (float)mapDecorationRenderState.y / 2.0f + 64.0f);
            this.pose.rotate((float)Math.PI / 180 * (float)mapDecorationRenderState.rot * 360.0f / 16.0f);
            this.pose.scale(4.0f, 4.0f);
            this.pose.translate(-0.125f, 0.125f);
            TextureAtlasSprite textureAtlasSprite = mapDecorationRenderState.atlasSprite;
            if (textureAtlasSprite != null) {
                object = textureManager.getTexture(textureAtlasSprite.atlasLocation()).getTextureView();
                this.submitBlit(RenderPipelines.GUI_TEXTURED, (GpuTextureView)object, -1, -1, 1, 1, textureAtlasSprite.getU0(), textureAtlasSprite.getU1(), textureAtlasSprite.getV1(), textureAtlasSprite.getV0(), -1);
            }
            this.pose.popMatrix();
            if (mapDecorationRenderState.name == null) continue;
            object = minecraft.font;
            float f = ((Font)object).width(mapDecorationRenderState.name);
            float f2 = 25.0f / f;
            Objects.requireNonNull(object);
            float f3 = Mth.clamp(f2, 0.0f, 6.0f / 9.0f);
            this.pose.pushMatrix();
            this.pose.translate((float)mapDecorationRenderState.x / 2.0f + 64.0f - f * f3 / 2.0f, (float)mapDecorationRenderState.y / 2.0f + 64.0f + 4.0f);
            this.pose.scale(f3, f3);
            this.guiRenderState.submitText(new GuiTextRenderState((Font)object, mapDecorationRenderState.name.getVisualOrderText(), new Matrix3x2f((Matrix3x2fc)this.pose), 0, 0, -1, Integer.MIN_VALUE, false, this.scissorStack.peek()));
            this.pose.popMatrix();
        }
    }

    public void submitEntityRenderState(EntityRenderState entityRenderState, float f, Vector3f vector3f, Quaternionf quaternionf, @Nullable Quaternionf quaternionf2, int n, int n2, int n3, int n4) {
        this.guiRenderState.submitPicturesInPictureState(new GuiEntityRenderState(entityRenderState, vector3f, quaternionf, quaternionf2, n, n2, n3, n4, f, this.scissorStack.peek()));
    }

    public void submitSkinRenderState(PlayerModel playerModel, ResourceLocation resourceLocation, float f, float f2, float f3, float f4, int n, int n2, int n3, int n4) {
        this.guiRenderState.submitPicturesInPictureState(new GuiSkinRenderState(playerModel, resourceLocation, f2, f3, f4, n, n2, n3, n4, f, this.scissorStack.peek()));
    }

    public void submitBookModelRenderState(BookModel bookModel, ResourceLocation resourceLocation, float f, float f2, float f3, int n, int n2, int n3, int n4) {
        this.guiRenderState.submitPicturesInPictureState(new GuiBookModelRenderState(bookModel, resourceLocation, f2, f3, n, n2, n3, n4, f, this.scissorStack.peek()));
    }

    public void submitBannerPatternRenderState(ModelPart modelPart, DyeColor dyeColor, BannerPatternLayers bannerPatternLayers, int n, int n2, int n3, int n4) {
        this.guiRenderState.submitPicturesInPictureState(new GuiBannerResultRenderState(modelPart, dyeColor, bannerPatternLayers, n, n2, n3, n4, this.scissorStack.peek()));
    }

    public void submitSignRenderState(Model model, float f, WoodType woodType, int n, int n2, int n3, int n4) {
        this.guiRenderState.submitPicturesInPictureState(new GuiSignRenderState(model, woodType, n, n2, n3, n4, f, this.scissorStack.peek()));
    }

    public void submitProfilerChartRenderState(List<ResultField> list, int n, int n2, int n3, int n4) {
        this.guiRenderState.submitPicturesInPictureState(new GuiProfilerChartRenderState(list, n, n2, n3, n4, this.scissorStack.peek()));
    }

    static class ScissorStack {
        private final Deque<ScreenRectangle> stack = new ArrayDeque<ScreenRectangle>();

        ScissorStack() {
        }

        public ScreenRectangle push(ScreenRectangle screenRectangle) {
            ScreenRectangle screenRectangle2 = this.stack.peekLast();
            if (screenRectangle2 != null) {
                ScreenRectangle screenRectangle3 = Objects.requireNonNullElse(screenRectangle.intersection(screenRectangle2), ScreenRectangle.empty());
                this.stack.addLast(screenRectangle3);
                return screenRectangle3;
            }
            this.stack.addLast(screenRectangle);
            return screenRectangle;
        }

        @Nullable
        public ScreenRectangle pop() {
            if (this.stack.isEmpty()) {
                throw new IllegalStateException("Scissor stack underflow");
            }
            this.stack.removeLast();
            return this.stack.peekLast();
        }

        @Nullable
        public ScreenRectangle peek() {
            return this.stack.peekLast();
        }

        public boolean containsPoint(int n, int n2) {
            if (this.stack.isEmpty()) {
                return true;
            }
            return this.stack.peek().containsPoint(n, n2);
        }
    }
}

