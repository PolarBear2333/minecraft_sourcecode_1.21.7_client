/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.inventory;

import com.mojang.blaze3d.platform.Lighting;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.LoomMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

public class LoomScreen
extends AbstractContainerScreen<LoomMenu> {
    private static final ResourceLocation BANNER_SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot/banner");
    private static final ResourceLocation DYE_SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot/dye");
    private static final ResourceLocation PATTERN_SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot/banner_pattern");
    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("container/loom/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("container/loom/scroller_disabled");
    private static final ResourceLocation PATTERN_SELECTED_SPRITE = ResourceLocation.withDefaultNamespace("container/loom/pattern_selected");
    private static final ResourceLocation PATTERN_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("container/loom/pattern_highlighted");
    private static final ResourceLocation PATTERN_SPRITE = ResourceLocation.withDefaultNamespace("container/loom/pattern");
    private static final ResourceLocation ERROR_SPRITE = ResourceLocation.withDefaultNamespace("container/loom/error");
    private static final ResourceLocation BG_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/container/loom.png");
    private static final int PATTERN_COLUMNS = 4;
    private static final int PATTERN_ROWS = 4;
    private static final int SCROLLER_WIDTH = 12;
    private static final int SCROLLER_HEIGHT = 15;
    private static final int PATTERN_IMAGE_SIZE = 14;
    private static final int SCROLLER_FULL_HEIGHT = 56;
    private static final int PATTERNS_X = 60;
    private static final int PATTERNS_Y = 13;
    private static final float BANNER_PATTERN_TEXTURE_SIZE = 64.0f;
    private static final float BANNER_PATTERN_WIDTH = 21.0f;
    private static final float BANNER_PATTERN_HEIGHT = 40.0f;
    private ModelPart flag;
    @Nullable
    private BannerPatternLayers resultBannerPatterns;
    private ItemStack bannerStack = ItemStack.EMPTY;
    private ItemStack dyeStack = ItemStack.EMPTY;
    private ItemStack patternStack = ItemStack.EMPTY;
    private boolean displayPatterns;
    private boolean hasMaxPatterns;
    private float scrollOffs;
    private boolean scrolling;
    private int startRow;

    public LoomScreen(LoomMenu loomMenu, Inventory inventory, Component component) {
        super(loomMenu, inventory, component);
        loomMenu.registerUpdateListener(this::containerChanged);
        this.titleLabelY -= 2;
    }

    @Override
    protected void init() {
        super.init();
        this.flag = this.minecraft.getEntityModels().bakeLayer(ModelLayers.STANDING_BANNER_FLAG).getChild("flag");
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.render(guiGraphics, n, n2, f);
        this.renderTooltip(guiGraphics, n, n2);
    }

    private int totalRowCount() {
        return Mth.positiveCeilDiv(((LoomMenu)this.menu).getSelectablePatterns().size(), 4);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float f, int n, int n2) {
        int n3;
        int n4 = this.leftPos;
        int n5 = this.topPos;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BG_LOCATION, n4, n5, 0.0f, 0.0f, this.imageWidth, this.imageHeight, 256, 256);
        Slot slot = ((LoomMenu)this.menu).getBannerSlot();
        Slot slot2 = ((LoomMenu)this.menu).getDyeSlot();
        Slot slot3 = ((LoomMenu)this.menu).getPatternSlot();
        Slot slot4 = ((LoomMenu)this.menu).getResultSlot();
        if (!slot.hasItem()) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, BANNER_SLOT_SPRITE, n4 + slot.x, n5 + slot.y, 16, 16);
        }
        if (!slot2.hasItem()) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, DYE_SLOT_SPRITE, n4 + slot2.x, n5 + slot2.y, 16, 16);
        }
        if (!slot3.hasItem()) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, PATTERN_SLOT_SPRITE, n4 + slot3.x, n5 + slot3.y, 16, 16);
        }
        int n6 = (int)(41.0f * this.scrollOffs);
        ResourceLocation resourceLocation = this.displayPatterns ? SCROLLER_SPRITE : SCROLLER_DISABLED_SPRITE;
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation, n4 + 119, n5 + 13 + n6, 12, 15);
        if (this.resultBannerPatterns != null && !this.hasMaxPatterns) {
            DyeColor dyeColor = ((BannerItem)slot4.getItem().getItem()).getColor();
            n3 = n4 + 141;
            int n7 = n5 + 8;
            guiGraphics.submitBannerPatternRenderState(this.flag, dyeColor, this.resultBannerPatterns, n3, n7, n3 + 20, n7 + 40);
        } else if (this.hasMaxPatterns) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, ERROR_SPRITE, n4 + slot4.x - 5, n5 + slot4.y - 5, 26, 26);
        }
        if (this.displayPatterns) {
            int n8 = n4 + 60;
            n3 = n5 + 13;
            List<Holder<BannerPattern>> list = ((LoomMenu)this.menu).getSelectablePatterns();
            block0: for (int i = 0; i < 4; ++i) {
                for (int j = 0; j < 4; ++j) {
                    boolean bl;
                    int n9 = i + this.startRow;
                    int n10 = n9 * 4 + j;
                    if (n10 >= list.size()) break block0;
                    int n11 = n8 + j * 14;
                    int n12 = n3 + i * 14;
                    boolean bl2 = bl = n >= n11 && n2 >= n12 && n < n11 + 14 && n2 < n12 + 14;
                    ResourceLocation resourceLocation2 = n10 == ((LoomMenu)this.menu).getSelectedBannerPatternIndex() ? PATTERN_SELECTED_SPRITE : (bl ? PATTERN_HIGHLIGHTED_SPRITE : PATTERN_SPRITE);
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation2, n11, n12, 14, 14);
                    TextureAtlasSprite textureAtlasSprite = Sheets.getBannerMaterial(list.get(n10)).sprite();
                    this.renderBannerOnButton(guiGraphics, n11, n12, textureAtlasSprite);
                }
            }
        }
        Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_3D);
    }

    private void renderBannerOnButton(GuiGraphics guiGraphics, int n, int n2, TextureAtlasSprite textureAtlasSprite) {
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate((float)(n + 4), (float)(n2 + 2));
        float f = textureAtlasSprite.getU0();
        float f2 = f + (textureAtlasSprite.getU1() - textureAtlasSprite.getU0()) * 21.0f / 64.0f;
        float f3 = textureAtlasSprite.getV1() - textureAtlasSprite.getV0();
        float f4 = textureAtlasSprite.getV0() + f3 / 64.0f;
        float f5 = f4 + f3 * 40.0f / 64.0f;
        int n3 = 5;
        int n4 = 10;
        guiGraphics.fill(0, 0, 5, 10, DyeColor.GRAY.getTextureDiffuseColor());
        guiGraphics.blit(textureAtlasSprite.atlasLocation(), 0, 0, 5, 10, f, f2, f4, f5);
        guiGraphics.pose().popMatrix();
    }

    @Override
    public boolean mouseClicked(double d, double d2, int n) {
        this.scrolling = false;
        if (this.displayPatterns) {
            int n2 = this.leftPos + 60;
            int n3 = this.topPos + 13;
            for (int i = 0; i < 4; ++i) {
                for (int j = 0; j < 4; ++j) {
                    double d3 = d - (double)(n2 + j * 14);
                    double d4 = d2 - (double)(n3 + i * 14);
                    int n4 = i + this.startRow;
                    int n5 = n4 * 4 + j;
                    if (!(d3 >= 0.0) || !(d4 >= 0.0) || !(d3 < 14.0) || !(d4 < 14.0) || !((LoomMenu)this.menu).clickMenuButton(this.minecraft.player, n5)) continue;
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_LOOM_SELECT_PATTERN, 1.0f));
                    this.minecraft.gameMode.handleInventoryButtonClick(((LoomMenu)this.menu).containerId, n5);
                    return true;
                }
            }
            n2 = this.leftPos + 119;
            n3 = this.topPos + 9;
            if (d >= (double)n2 && d < (double)(n2 + 12) && d2 >= (double)n3 && d2 < (double)(n3 + 56)) {
                this.scrolling = true;
            }
        }
        return super.mouseClicked(d, d2, n);
    }

    @Override
    public boolean mouseDragged(double d, double d2, int n, double d3, double d4) {
        int n2 = this.totalRowCount() - 4;
        if (this.scrolling && this.displayPatterns && n2 > 0) {
            int n3 = this.topPos + 13;
            int n4 = n3 + 56;
            this.scrollOffs = ((float)d2 - (float)n3 - 7.5f) / ((float)(n4 - n3) - 15.0f);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0f, 1.0f);
            this.startRow = Math.max((int)((double)(this.scrollOffs * (float)n2) + 0.5), 0);
            return true;
        }
        return super.mouseDragged(d, d2, n, d3, d4);
    }

    @Override
    public boolean mouseScrolled(double d, double d2, double d3, double d4) {
        if (super.mouseScrolled(d, d2, d3, d4)) {
            return true;
        }
        int n = this.totalRowCount() - 4;
        if (this.displayPatterns && n > 0) {
            float f = (float)d4 / (float)n;
            this.scrollOffs = Mth.clamp(this.scrollOffs - f, 0.0f, 1.0f);
            this.startRow = Math.max((int)(this.scrollOffs * (float)n + 0.5f), 0);
        }
        return true;
    }

    @Override
    protected boolean hasClickedOutside(double d, double d2, int n, int n2, int n3) {
        return d < (double)n || d2 < (double)n2 || d >= (double)(n + this.imageWidth) || d2 >= (double)(n2 + this.imageHeight);
    }

    private void containerChanged() {
        ItemStack itemStack = ((LoomMenu)this.menu).getResultSlot().getItem();
        this.resultBannerPatterns = itemStack.isEmpty() ? null : itemStack.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
        ItemStack itemStack2 = ((LoomMenu)this.menu).getBannerSlot().getItem();
        ItemStack itemStack3 = ((LoomMenu)this.menu).getDyeSlot().getItem();
        ItemStack itemStack4 = ((LoomMenu)this.menu).getPatternSlot().getItem();
        BannerPatternLayers bannerPatternLayers = itemStack2.getOrDefault(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY);
        boolean bl = this.hasMaxPatterns = bannerPatternLayers.layers().size() >= 6;
        if (this.hasMaxPatterns) {
            this.resultBannerPatterns = null;
        }
        if (!(ItemStack.matches(itemStack2, this.bannerStack) && ItemStack.matches(itemStack3, this.dyeStack) && ItemStack.matches(itemStack4, this.patternStack))) {
            boolean bl2 = this.displayPatterns = !itemStack2.isEmpty() && !itemStack3.isEmpty() && !this.hasMaxPatterns && !((LoomMenu)this.menu).getSelectablePatterns().isEmpty();
        }
        if (this.startRow >= this.totalRowCount()) {
            this.startRow = 0;
            this.scrollOffs = 0.0f;
        }
        this.bannerStack = itemStack2.copy();
        this.dyeStack = itemStack3.copy();
        this.patternStack = itemStack4.copy();
    }
}

