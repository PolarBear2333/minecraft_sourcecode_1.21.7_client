/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.inventory;

import java.lang.runtime.SwitchBootstraps;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.WritableBookContent;
import net.minecraft.world.item.component.WrittenBookContent;

public class BookViewScreen
extends Screen {
    public static final int PAGE_INDICATOR_TEXT_Y_OFFSET = 16;
    public static final int PAGE_TEXT_X_OFFSET = 36;
    public static final int PAGE_TEXT_Y_OFFSET = 30;
    private static final int BACKGROUND_TEXTURE_WIDTH = 256;
    private static final int BACKGROUND_TEXTURE_HEIGHT = 256;
    private static final Component TITLE = Component.translatable("book.view.title");
    public static final BookAccess EMPTY_ACCESS = new BookAccess(List.of());
    public static final ResourceLocation BOOK_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/book.png");
    protected static final int TEXT_WIDTH = 114;
    protected static final int TEXT_HEIGHT = 128;
    protected static final int IMAGE_WIDTH = 192;
    protected static final int IMAGE_HEIGHT = 192;
    private BookAccess bookAccess;
    private int currentPage;
    private List<FormattedCharSequence> cachedPageComponents = Collections.emptyList();
    private int cachedPage = -1;
    private Component pageMsg = CommonComponents.EMPTY;
    private PageButton forwardButton;
    private PageButton backButton;
    private final boolean playTurnSound;

    public BookViewScreen(BookAccess bookAccess) {
        this(bookAccess, true);
    }

    public BookViewScreen() {
        this(EMPTY_ACCESS, false);
    }

    private BookViewScreen(BookAccess bookAccess, boolean bl) {
        super(TITLE);
        this.bookAccess = bookAccess;
        this.playTurnSound = bl;
    }

    public void setBookAccess(BookAccess bookAccess) {
        this.bookAccess = bookAccess;
        this.currentPage = Mth.clamp(this.currentPage, 0, bookAccess.getPageCount());
        this.updateButtonVisibility();
        this.cachedPage = -1;
    }

    public boolean setPage(int n) {
        int n2 = Mth.clamp(n, 0, this.bookAccess.getPageCount() - 1);
        if (n2 != this.currentPage) {
            this.currentPage = n2;
            this.updateButtonVisibility();
            this.cachedPage = -1;
            return true;
        }
        return false;
    }

    protected boolean forcePage(int n) {
        return this.setPage(n);
    }

    @Override
    protected void init() {
        this.createMenuControls();
        this.createPageControlButtons();
    }

    @Override
    public Component getNarrationMessage() {
        return CommonComponents.joinLines(super.getNarrationMessage(), this.getPageNumberMessage(), this.bookAccess.getPage(this.currentPage));
    }

    private Component getPageNumberMessage() {
        return Component.translatable("book.pageIndicator", this.currentPage + 1, Math.max(this.getNumPages(), 1));
    }

    protected void createMenuControls() {
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).bounds(this.width / 2 - 100, 196, 200, 20).build());
    }

    protected void createPageControlButtons() {
        int n = (this.width - 192) / 2;
        int n2 = 2;
        this.forwardButton = this.addRenderableWidget(new PageButton(n + 116, 159, true, button -> this.pageForward(), this.playTurnSound));
        this.backButton = this.addRenderableWidget(new PageButton(n + 43, 159, false, button -> this.pageBack(), this.playTurnSound));
        this.updateButtonVisibility();
    }

    private int getNumPages() {
        return this.bookAccess.getPageCount();
    }

    protected void pageBack() {
        if (this.currentPage > 0) {
            --this.currentPage;
        }
        this.updateButtonVisibility();
    }

    protected void pageForward() {
        if (this.currentPage < this.getNumPages() - 1) {
            ++this.currentPage;
        }
        this.updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        this.forwardButton.visible = this.currentPage < this.getNumPages() - 1;
        this.backButton.visible = this.currentPage > 0;
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (super.keyPressed(n, n2, n3)) {
            return true;
        }
        switch (n) {
            case 266: {
                this.backButton.onPress();
                return true;
            }
            case 267: {
                this.forwardButton.onPress();
                return true;
            }
        }
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.render(guiGraphics, n, n2, f);
        int n3 = (this.width - 192) / 2;
        int n4 = 2;
        if (this.cachedPage != this.currentPage) {
            Component component = this.bookAccess.getPage(this.currentPage);
            this.cachedPageComponents = this.font.split(component, 114);
            this.pageMsg = this.getPageNumberMessage();
        }
        this.cachedPage = this.currentPage;
        int n5 = this.font.width(this.pageMsg);
        guiGraphics.drawString(this.font, this.pageMsg, n3 - n5 + 192 - 44, 18, -16777216, false);
        int n6 = Math.min(128 / this.font.lineHeight, this.cachedPageComponents.size());
        for (int i = 0; i < n6; ++i) {
            FormattedCharSequence formattedCharSequence = this.cachedPageComponents.get(i);
            guiGraphics.drawString(this.font, formattedCharSequence, n3 + 36, 32 + i * this.font.lineHeight, -16777216, false);
        }
        Style style = this.getClickedComponentStyleAt(n, n2);
        if (style != null) {
            guiGraphics.renderComponentHoverEffect(this.font, style, n, n2);
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int n, int n2, float f) {
        this.renderTransparentBackground(guiGraphics);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BOOK_LOCATION, (this.width - 192) / 2, 2, 0.0f, 0.0f, 192, 192, 256, 256);
    }

    @Override
    public boolean mouseClicked(double d, double d2, int n) {
        Style style;
        if (n == 0 && (style = this.getClickedComponentStyleAt(d, d2)) != null && this.handleComponentClicked(style)) {
            return true;
        }
        return super.mouseClicked(d, d2, n);
    }

    /*
     * WARNING - Removed back jump from a try to a catch block - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    @Override
    protected void handleClickEvent(Minecraft minecraft, ClickEvent clickEvent) {
        LocalPlayer localPlayer = Objects.requireNonNull(minecraft.player, "Player not available");
        ClickEvent clickEvent2 = clickEvent;
        Objects.requireNonNull(clickEvent2);
        ClickEvent clickEvent3 = clickEvent2;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{ClickEvent.ChangePage.class, ClickEvent.RunCommand.class}, (Object)clickEvent3, n)) {
            case 0: {
                ClickEvent.ChangePage changePage = (ClickEvent.ChangePage)clickEvent3;
                try {
                    int n2;
                    int n3 = n2 = changePage.page();
                    this.forcePage(n3 - 1);
                    return;
                }
                catch (Throwable throwable) {
                    throw new MatchException(throwable.toString(), throwable);
                }
            }
            case 1: {
                String string2;
                ClickEvent.RunCommand runCommand = (ClickEvent.RunCommand)clickEvent3;
                {
                    String string;
                    string2 = string = runCommand.command();
                    this.closeContainerOnServer();
                }
                BookViewScreen.clickCommandAction(localPlayer, string2, null);
                return;
            }
        }
        BookViewScreen.defaultHandleGameClickEvent(clickEvent, minecraft, this);
    }

    protected void closeContainerOnServer() {
    }

    @Nullable
    public Style getClickedComponentStyleAt(double d, double d2) {
        if (this.cachedPageComponents.isEmpty()) {
            return null;
        }
        int n = Mth.floor(d - (double)((this.width - 192) / 2) - 36.0);
        int n2 = Mth.floor(d2 - 2.0 - 30.0);
        if (n < 0 || n2 < 0) {
            return null;
        }
        int n3 = Math.min(128 / this.font.lineHeight, this.cachedPageComponents.size());
        if (n <= 114 && n2 < this.minecraft.font.lineHeight * n3 + n3) {
            int n4 = n2 / this.minecraft.font.lineHeight;
            if (n4 >= 0 && n4 < this.cachedPageComponents.size()) {
                FormattedCharSequence formattedCharSequence = this.cachedPageComponents.get(n4);
                return this.minecraft.font.getSplitter().componentStyleAtWidth(formattedCharSequence, n);
            }
            return null;
        }
        return null;
    }

    public record BookAccess(List<Component> pages) {
        public int getPageCount() {
            return this.pages.size();
        }

        public Component getPage(int n) {
            if (n >= 0 && n < this.getPageCount()) {
                return this.pages.get(n);
            }
            return CommonComponents.EMPTY;
        }

        @Nullable
        public static BookAccess fromItem(ItemStack itemStack) {
            boolean bl = Minecraft.getInstance().isTextFilteringEnabled();
            WrittenBookContent writtenBookContent = itemStack.get(DataComponents.WRITTEN_BOOK_CONTENT);
            if (writtenBookContent != null) {
                return new BookAccess(writtenBookContent.getPages(bl));
            }
            WritableBookContent writableBookContent = itemStack.get(DataComponents.WRITABLE_BOOK_CONTENT);
            if (writableBookContent != null) {
                return new BookAccess(writableBookContent.getPages(bl).map(Component::literal).toList());
            }
            return null;
        }
    }
}

