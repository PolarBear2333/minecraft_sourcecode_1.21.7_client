/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.inventory;

import java.util.Collection;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetTestBlockPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TestBlockEntity;
import net.minecraft.world.level.block.state.properties.TestBlockMode;

public class TestBlockEditScreen
extends Screen {
    private static final List<TestBlockMode> MODES = List.of(TestBlockMode.values());
    private static final Component TITLE = Component.translatable(Blocks.TEST_BLOCK.getDescriptionId());
    private static final Component MESSAGE_LABEL = Component.translatable("test_block.message");
    private final BlockPos position;
    private TestBlockMode mode;
    private String message;
    @Nullable
    private EditBox messageEdit;

    public TestBlockEditScreen(TestBlockEntity testBlockEntity) {
        super(TITLE);
        this.position = testBlockEntity.getBlockPos();
        this.mode = testBlockEntity.getMode();
        this.message = testBlockEntity.getMessage();
    }

    @Override
    public void init() {
        this.messageEdit = new EditBox(this.font, this.width / 2 - 152, 80, 240, 20, Component.translatable("test_block.message"));
        this.messageEdit.setMaxLength(128);
        this.messageEdit.setValue(this.message);
        this.addRenderableWidget(this.messageEdit);
        this.setInitialFocus(this.messageEdit);
        this.updateMode(this.mode);
        this.addRenderableWidget(CycleButton.builder(TestBlockMode::getDisplayName).withValues((Collection<TestBlockMode>)MODES).displayOnlyValue().withInitialValue(this.mode).create(this.width / 2 - 4 - 150, 185, 50, 20, TITLE, (cycleButton, testBlockMode) -> this.updateMode((TestBlockMode)testBlockMode)));
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onDone()).bounds(this.width / 2 - 4 - 150, 210, 150, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onCancel()).bounds(this.width / 2 + 4, 210, 150, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.render(guiGraphics, n, n2, f);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, -1);
        if (this.mode != TestBlockMode.START) {
            guiGraphics.drawString(this.font, MESSAGE_LABEL, this.width / 2 - 153, 70, -6250336);
        }
        guiGraphics.drawString(this.font, this.mode.getDetailedMessage(), this.width / 2 - 153, 174, -6250336);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void onDone() {
        this.message = this.messageEdit.getValue();
        this.minecraft.getConnection().send(new ServerboundSetTestBlockPacket(this.position, this.mode, this.message));
        this.onClose();
    }

    @Override
    public void onClose() {
        this.onCancel();
    }

    private void onCancel() {
        this.minecraft.setScreen(null);
    }

    private void updateMode(TestBlockMode testBlockMode) {
        this.mode = testBlockMode;
        this.messageEdit.visible = testBlockMode != TestBlockMode.START;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int n, int n2, float f) {
        this.renderTransparentBackground(guiGraphics);
    }
}

