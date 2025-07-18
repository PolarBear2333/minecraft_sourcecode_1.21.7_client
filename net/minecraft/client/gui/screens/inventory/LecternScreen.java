/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.inventory;

import java.util.Objects;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.LecternMenu;
import net.minecraft.world.item.ItemStack;

public class LecternScreen
extends BookViewScreen
implements MenuAccess<LecternMenu> {
    private final LecternMenu menu;
    private final ContainerListener listener = new ContainerListener(){

        @Override
        public void slotChanged(AbstractContainerMenu abstractContainerMenu, int n, ItemStack itemStack) {
            LecternScreen.this.bookChanged();
        }

        @Override
        public void dataChanged(AbstractContainerMenu abstractContainerMenu, int n, int n2) {
            if (n == 0) {
                LecternScreen.this.pageChanged();
            }
        }
    };

    public LecternScreen(LecternMenu lecternMenu, Inventory inventory, Component component) {
        this.menu = lecternMenu;
    }

    @Override
    public LecternMenu getMenu() {
        return this.menu;
    }

    @Override
    protected void init() {
        super.init();
        this.menu.addSlotListener(this.listener);
    }

    @Override
    public void onClose() {
        this.minecraft.player.closeContainer();
        super.onClose();
    }

    @Override
    public void removed() {
        super.removed();
        this.menu.removeSlotListener(this.listener);
    }

    @Override
    protected void createMenuControls() {
        if (this.minecraft.player.mayBuild()) {
            this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).bounds(this.width / 2 - 100, 196, 98, 20).build());
            this.addRenderableWidget(Button.builder(Component.translatable("lectern.take_book"), button -> this.sendButtonClick(3)).bounds(this.width / 2 + 2, 196, 98, 20).build());
        } else {
            super.createMenuControls();
        }
    }

    @Override
    protected void pageBack() {
        this.sendButtonClick(1);
    }

    @Override
    protected void pageForward() {
        this.sendButtonClick(2);
    }

    @Override
    protected boolean forcePage(int n) {
        if (n != this.menu.getPage()) {
            this.sendButtonClick(100 + n);
            return true;
        }
        return false;
    }

    private void sendButtonClick(int n) {
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, n);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    void bookChanged() {
        ItemStack itemStack = this.menu.getBook();
        this.setBookAccess(Objects.requireNonNullElse(BookViewScreen.BookAccess.fromItem(itemStack), BookViewScreen.EMPTY_ACCESS));
    }

    void pageChanged() {
        this.setPage(this.menu.getPage());
    }

    @Override
    protected void closeContainerOnServer() {
        this.minecraft.player.closeContainer();
    }

    @Override
    public /* synthetic */ AbstractContainerMenu getMenu() {
        return this.getMenu();
    }
}

