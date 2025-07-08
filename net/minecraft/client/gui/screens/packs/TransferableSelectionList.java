/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.packs;

import java.util.Objects;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.util.FormattedCharSequence;

public class TransferableSelectionList
extends ObjectSelectionList<PackEntry> {
    static final ResourceLocation SELECT_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("transferable_list/select_highlighted");
    static final ResourceLocation SELECT_SPRITE = ResourceLocation.withDefaultNamespace("transferable_list/select");
    static final ResourceLocation UNSELECT_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("transferable_list/unselect_highlighted");
    static final ResourceLocation UNSELECT_SPRITE = ResourceLocation.withDefaultNamespace("transferable_list/unselect");
    static final ResourceLocation MOVE_UP_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("transferable_list/move_up_highlighted");
    static final ResourceLocation MOVE_UP_SPRITE = ResourceLocation.withDefaultNamespace("transferable_list/move_up");
    static final ResourceLocation MOVE_DOWN_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("transferable_list/move_down_highlighted");
    static final ResourceLocation MOVE_DOWN_SPRITE = ResourceLocation.withDefaultNamespace("transferable_list/move_down");
    static final Component INCOMPATIBLE_TITLE = Component.translatable("pack.incompatible");
    static final Component INCOMPATIBLE_CONFIRM_TITLE = Component.translatable("pack.incompatible.confirm.title");
    private final Component title;
    final PackSelectionScreen screen;

    public TransferableSelectionList(Minecraft minecraft, PackSelectionScreen packSelectionScreen, int n, int n2, Component component) {
        Objects.requireNonNull(minecraft.font);
        super(minecraft, n, n2, 33, 36, (int)(9.0f * 1.5f));
        this.screen = packSelectionScreen;
        this.title = component;
        this.centerListVertically = false;
    }

    @Override
    protected void renderHeader(GuiGraphics guiGraphics, int n, int n2) {
        MutableComponent mutableComponent = Component.empty().append(this.title).withStyle(ChatFormatting.UNDERLINE, ChatFormatting.BOLD);
        guiGraphics.drawString(this.minecraft.font, mutableComponent, n + this.width / 2 - this.minecraft.font.width(mutableComponent) / 2, Math.min(this.getY() + 3, n2), -1);
    }

    @Override
    public int getRowWidth() {
        return this.width;
    }

    @Override
    protected int scrollBarX() {
        return this.getRight() - 6;
    }

    @Override
    protected void renderSelection(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5) {
        if (this.scrollbarVisible()) {
            int n6 = 2;
            int n7 = this.getRowLeft() - 2;
            int n8 = this.getRight() - 6 - 1;
            int n9 = n - 2;
            int n10 = n + n3 + 2;
            guiGraphics.fill(n7, n9, n8, n10, n4);
            guiGraphics.fill(n7 + 1, n9 + 1, n8 - 1, n10 - 1, n5);
        } else {
            super.renderSelection(guiGraphics, n, n2, n3, n4, n5);
        }
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (this.getSelected() != null) {
            switch (n) {
                case 32: 
                case 257: {
                    ((PackEntry)this.getSelected()).keyboardSelection();
                    return true;
                }
            }
            if (Screen.hasShiftDown()) {
                switch (n) {
                    case 265: {
                        ((PackEntry)this.getSelected()).keyboardMoveUp();
                        return true;
                    }
                    case 264: {
                        ((PackEntry)this.getSelected()).keyboardMoveDown();
                        return true;
                    }
                }
            }
        }
        return super.keyPressed(n, n2, n3);
    }

    public static class PackEntry
    extends ObjectSelectionList.Entry<PackEntry> {
        private static final int MAX_DESCRIPTION_WIDTH_PIXELS = 157;
        private static final int MAX_NAME_WIDTH_PIXELS = 157;
        private static final String TOO_LONG_NAME_SUFFIX = "...";
        private final TransferableSelectionList parent;
        protected final Minecraft minecraft;
        private final PackSelectionModel.Entry pack;
        private final FormattedCharSequence nameDisplayCache;
        private final MultiLineLabel descriptionDisplayCache;
        private final FormattedCharSequence incompatibleNameDisplayCache;
        private final MultiLineLabel incompatibleDescriptionDisplayCache;

        public PackEntry(Minecraft minecraft, TransferableSelectionList transferableSelectionList, PackSelectionModel.Entry entry) {
            this.minecraft = minecraft;
            this.pack = entry;
            this.parent = transferableSelectionList;
            this.nameDisplayCache = PackEntry.cacheName(minecraft, entry.getTitle());
            this.descriptionDisplayCache = PackEntry.cacheDescription(minecraft, entry.getExtendedDescription());
            this.incompatibleNameDisplayCache = PackEntry.cacheName(minecraft, INCOMPATIBLE_TITLE);
            this.incompatibleDescriptionDisplayCache = PackEntry.cacheDescription(minecraft, entry.getCompatibility().getDescription());
        }

        private static FormattedCharSequence cacheName(Minecraft minecraft, Component component) {
            int n = minecraft.font.width(component);
            if (n > 157) {
                FormattedText formattedText = FormattedText.composite(minecraft.font.substrByWidth(component, 157 - minecraft.font.width(TOO_LONG_NAME_SUFFIX)), FormattedText.of(TOO_LONG_NAME_SUFFIX));
                return Language.getInstance().getVisualOrder(formattedText);
            }
            return component.getVisualOrderText();
        }

        private static MultiLineLabel cacheDescription(Minecraft minecraft, Component component) {
            return MultiLineLabel.create(minecraft.font, 157, 2, component);
        }

        @Override
        public Component getNarration() {
            return Component.translatable("narrator.select", this.pack.getTitle());
        }

        @Override
        public void render(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
            PackCompatibility packCompatibility = this.pack.getCompatibility();
            if (!packCompatibility.isCompatible()) {
                int n8 = n3 + n4 - 3 - (this.parent.scrollbarVisible() ? 7 : 0);
                guiGraphics.fill(n3 - 1, n2 - 1, n8, n2 + n5 + 1, -8978432);
            }
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, this.pack.getIconTexture(), n3, n2, 0.0f, 0.0f, 32, 32, 32, 32);
            FormattedCharSequence formattedCharSequence = this.nameDisplayCache;
            MultiLineLabel multiLineLabel = this.descriptionDisplayCache;
            if (this.showHoverOverlay() && (this.minecraft.options.touchscreen().get().booleanValue() || bl || this.parent.getSelected() == this && this.parent.isFocused())) {
                guiGraphics.fill(n3, n2, n3 + 32, n2 + 32, -1601138544);
                int n9 = n6 - n3;
                int n10 = n7 - n2;
                if (!this.pack.getCompatibility().isCompatible()) {
                    formattedCharSequence = this.incompatibleNameDisplayCache;
                    multiLineLabel = this.incompatibleDescriptionDisplayCache;
                }
                if (this.pack.canSelect()) {
                    if (n9 < 32) {
                        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SELECT_HIGHLIGHTED_SPRITE, n3, n2, 32, 32);
                    } else {
                        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SELECT_SPRITE, n3, n2, 32, 32);
                    }
                } else {
                    if (this.pack.canUnselect()) {
                        if (n9 < 16) {
                            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, UNSELECT_HIGHLIGHTED_SPRITE, n3, n2, 32, 32);
                        } else {
                            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, UNSELECT_SPRITE, n3, n2, 32, 32);
                        }
                    }
                    if (this.pack.canMoveUp()) {
                        if (n9 < 32 && n9 > 16 && n10 < 16) {
                            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, MOVE_UP_HIGHLIGHTED_SPRITE, n3, n2, 32, 32);
                        } else {
                            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, MOVE_UP_SPRITE, n3, n2, 32, 32);
                        }
                    }
                    if (this.pack.canMoveDown()) {
                        if (n9 < 32 && n9 > 16 && n10 > 16) {
                            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, MOVE_DOWN_HIGHLIGHTED_SPRITE, n3, n2, 32, 32);
                        } else {
                            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, MOVE_DOWN_SPRITE, n3, n2, 32, 32);
                        }
                    }
                }
            }
            guiGraphics.drawString(this.minecraft.font, formattedCharSequence, n3 + 32 + 2, n2 + 1, -1);
            multiLineLabel.renderLeftAligned(guiGraphics, n3 + 32 + 2, n2 + 12, 10, -8355712);
        }

        public String getPackId() {
            return this.pack.getId();
        }

        private boolean showHoverOverlay() {
            return !this.pack.isFixedPosition() || !this.pack.isRequired();
        }

        public void keyboardSelection() {
            if (this.pack.canSelect() && this.handlePackSelection()) {
                this.parent.screen.updateFocus(this.parent);
            } else if (this.pack.canUnselect()) {
                this.pack.unselect();
                this.parent.screen.updateFocus(this.parent);
            }
        }

        void keyboardMoveUp() {
            if (this.pack.canMoveUp()) {
                this.pack.moveUp();
            }
        }

        void keyboardMoveDown() {
            if (this.pack.canMoveDown()) {
                this.pack.moveDown();
            }
        }

        private boolean handlePackSelection() {
            if (this.pack.getCompatibility().isCompatible()) {
                this.pack.select();
                return true;
            }
            Component component = this.pack.getCompatibility().getConfirmation();
            this.minecraft.setScreen(new ConfirmScreen(bl -> {
                this.minecraft.setScreen(this.parent.screen);
                if (bl) {
                    this.pack.select();
                }
            }, INCOMPATIBLE_CONFIRM_TITLE, component));
            return false;
        }

        @Override
        public boolean mouseClicked(double d, double d2, int n) {
            double d3 = d - (double)this.parent.getRowLeft();
            double d4 = d2 - (double)this.parent.getRowTop(this.parent.children().indexOf(this));
            if (this.showHoverOverlay() && d3 <= 32.0) {
                this.parent.screen.clearSelected();
                if (this.pack.canSelect()) {
                    this.handlePackSelection();
                    return true;
                }
                if (d3 < 16.0 && this.pack.canUnselect()) {
                    this.pack.unselect();
                    return true;
                }
                if (d3 > 16.0 && d4 < 16.0 && this.pack.canMoveUp()) {
                    this.pack.moveUp();
                    return true;
                }
                if (d3 > 16.0 && d4 > 16.0 && this.pack.canMoveDown()) {
                    this.pack.moveDown();
                    return true;
                }
            }
            return super.mouseClicked(d, d2, n);
        }
    }
}

