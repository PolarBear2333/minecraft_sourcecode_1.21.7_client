/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.inventory;

import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;
import net.minecraft.client.gui.screens.inventory.BookViewScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;

public class BookSignScreen
extends Screen {
    private static final Component EDIT_TITLE_LABEL = Component.translatable("book.editTitle");
    private static final Component FINALIZE_WARNING_LABEL = Component.translatable("book.finalizeWarning");
    private static final Component TITLE = Component.translatable("book.sign.title");
    private static final Component TITLE_EDIT_BOX = Component.translatable("book.sign.titlebox");
    private final BookEditScreen bookEditScreen;
    private final Player owner;
    private final List<String> pages;
    private final InteractionHand hand;
    private final Component ownerText;
    private EditBox titleBox;
    private String titleValue = "";

    public BookSignScreen(BookEditScreen bookEditScreen, Player player, InteractionHand interactionHand, List<String> list) {
        super(TITLE);
        this.bookEditScreen = bookEditScreen;
        this.owner = player;
        this.hand = interactionHand;
        this.pages = list;
        this.ownerText = Component.translatable("book.byAuthor", player.getName()).withStyle(ChatFormatting.DARK_GRAY);
    }

    @Override
    protected void init() {
        Button button2 = Button.builder(Component.translatable("book.finalizeButton"), button -> {
            this.saveChanges();
            this.minecraft.setScreen(null);
        }).bounds(this.width / 2 - 100, 196, 98, 20).build();
        button2.active = false;
        this.titleBox = this.addRenderableWidget(new EditBox(this.minecraft.font, (this.width - 114) / 2 - 3, 50, 114, 20, TITLE_EDIT_BOX));
        this.titleBox.setMaxLength(15);
        this.titleBox.setBordered(false);
        this.titleBox.setCentered(true);
        this.titleBox.setTextColor(-16777216);
        this.titleBox.setTextShadow(false);
        this.titleBox.setResponder(string -> {
            button.active = !StringUtil.isBlank(string);
        });
        this.titleBox.setValue(this.titleValue);
        this.addRenderableWidget(button2);
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> {
            this.titleValue = this.titleBox.getValue();
            this.minecraft.setScreen(this.bookEditScreen);
        }).bounds(this.width / 2 + 2, 196, 98, 20).build());
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.titleBox);
    }

    private void saveChanges() {
        int n = this.hand == InteractionHand.MAIN_HAND ? this.owner.getInventory().getSelectedSlot() : 40;
        this.minecraft.getConnection().send(new ServerboundEditBookPacket(n, this.pages, Optional.of(this.titleBox.getValue().trim())));
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (this.titleBox.isFocused() && !this.titleBox.getValue().isEmpty() && (n == 257 || n == 335)) {
            this.saveChanges();
            this.minecraft.setScreen(null);
            return true;
        }
        return super.keyPressed(n, n2, n3);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.render(guiGraphics, n, n2, f);
        int n3 = (this.width - 192) / 2;
        int n4 = 2;
        int n5 = this.font.width(EDIT_TITLE_LABEL);
        guiGraphics.drawString(this.font, EDIT_TITLE_LABEL, n3 + 36 + (114 - n5) / 2, 34, -16777216, false);
        int n6 = this.font.width(this.ownerText);
        guiGraphics.drawString(this.font, this.ownerText, n3 + 36 + (114 - n6) / 2, 60, -16777216, false);
        guiGraphics.drawWordWrap(this.font, FINALIZE_WARNING_LABEL, n3 + 36, 82, 114, -16777216, false);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int n, int n2, float f) {
        this.renderTransparentBackground(guiGraphics);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, BookViewScreen.BOOK_LOCATION, (this.width - 192) / 2, 2, 0.0f, 0.0f, 192, 192, 256, 256);
    }
}

