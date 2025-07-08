/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.BaseCommandBlock;

public abstract class AbstractCommandBlockEditScreen
extends Screen {
    private static final Component SET_COMMAND_LABEL = Component.translatable("advMode.setCommand");
    private static final Component COMMAND_LABEL = Component.translatable("advMode.command");
    private static final Component PREVIOUS_OUTPUT_LABEL = Component.translatable("advMode.previousOutput");
    protected EditBox commandEdit;
    protected EditBox previousEdit;
    protected Button doneButton;
    protected Button cancelButton;
    protected CycleButton<Boolean> outputButton;
    CommandSuggestions commandSuggestions;

    public AbstractCommandBlockEditScreen() {
        super(GameNarrator.NO_TITLE);
    }

    @Override
    public void tick() {
        if (!this.getCommandBlock().isValid()) {
            this.onClose();
        }
    }

    abstract BaseCommandBlock getCommandBlock();

    abstract int getPreviousY();

    @Override
    protected void init() {
        this.doneButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onDone()).bounds(this.width / 2 - 4 - 150, this.height / 4 + 120 + 12, 150, 20).build());
        this.cancelButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose()).bounds(this.width / 2 + 4, this.height / 4 + 120 + 12, 150, 20).build());
        boolean bl2 = this.getCommandBlock().isTrackOutput();
        this.outputButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.literal("O"), Component.literal("X")).withInitialValue(bl2).displayOnlyValue().create(this.width / 2 + 150 - 20, this.getPreviousY(), 20, 20, Component.translatable("advMode.trackOutput"), (cycleButton, bl) -> {
            BaseCommandBlock baseCommandBlock = this.getCommandBlock();
            baseCommandBlock.setTrackOutput((boolean)bl);
            this.updatePreviousOutput((boolean)bl);
        }));
        this.commandEdit = new EditBox(this.font, this.width / 2 - 150, 50, 300, 20, (Component)Component.translatable("advMode.command")){

            @Override
            protected MutableComponent createNarrationMessage() {
                return super.createNarrationMessage().append(AbstractCommandBlockEditScreen.this.commandSuggestions.getNarrationMessage());
            }
        };
        this.commandEdit.setMaxLength(32500);
        this.commandEdit.setResponder(this::onEdited);
        this.addWidget(this.commandEdit);
        this.previousEdit = new EditBox(this.font, this.width / 2 - 150, this.getPreviousY(), 276, 20, Component.translatable("advMode.previousOutput"));
        this.previousEdit.setMaxLength(32500);
        this.previousEdit.setEditable(false);
        this.previousEdit.setValue("-");
        this.addWidget(this.previousEdit);
        this.commandSuggestions = new CommandSuggestions(this.minecraft, this, this.commandEdit, this.font, true, true, 0, 7, false, Integer.MIN_VALUE);
        this.commandSuggestions.setAllowSuggestions(true);
        this.commandSuggestions.updateCommandInfo();
        this.updatePreviousOutput(bl2);
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.commandEdit);
    }

    @Override
    protected Component getUsageNarration() {
        if (this.commandSuggestions.isVisible()) {
            return this.commandSuggestions.getUsageNarration();
        }
        return super.getUsageNarration();
    }

    @Override
    public void resize(Minecraft minecraft, int n, int n2) {
        String string = this.commandEdit.getValue();
        this.init(minecraft, n, n2);
        this.commandEdit.setValue(string);
        this.commandSuggestions.updateCommandInfo();
    }

    protected void updatePreviousOutput(boolean bl) {
        this.previousEdit.setValue(bl ? this.getCommandBlock().getLastOutput().getString() : "-");
    }

    protected void onDone() {
        BaseCommandBlock baseCommandBlock = this.getCommandBlock();
        this.populateAndSendPacket(baseCommandBlock);
        if (!baseCommandBlock.isTrackOutput()) {
            baseCommandBlock.setLastOutput(null);
        }
        this.minecraft.setScreen(null);
    }

    protected abstract void populateAndSendPacket(BaseCommandBlock var1);

    private void onEdited(String string) {
        this.commandSuggestions.updateCommandInfo();
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (this.commandSuggestions.keyPressed(n, n2, n3)) {
            return true;
        }
        if (super.keyPressed(n, n2, n3)) {
            return true;
        }
        if (n == 257 || n == 335) {
            this.onDone();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double d, double d2, double d3, double d4) {
        if (this.commandSuggestions.mouseScrolled(d4)) {
            return true;
        }
        return super.mouseScrolled(d, d2, d3, d4);
    }

    @Override
    public boolean mouseClicked(double d, double d2, int n) {
        if (this.commandSuggestions.mouseClicked(d, d2, n)) {
            return true;
        }
        return super.mouseClicked(d, d2, n);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.render(guiGraphics, n, n2, f);
        guiGraphics.drawCenteredString(this.font, SET_COMMAND_LABEL, this.width / 2, 20, -1);
        guiGraphics.drawString(this.font, COMMAND_LABEL, this.width / 2 - 150 + 1, 40, -6250336);
        this.commandEdit.render(guiGraphics, n, n2, f);
        int n3 = 75;
        if (!this.previousEdit.getValue().isEmpty()) {
            guiGraphics.drawString(this.font, PREVIOUS_OUTPUT_LABEL, this.width / 2 - 150 + 1, (n3 += 5 * this.font.lineHeight + 1 + this.getPreviousY() - 135) + 4, -6250336);
            this.previousEdit.render(guiGraphics, n, n2, f);
        }
        this.commandSuggestions.render(guiGraphics, n, n2);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int n, int n2, float f) {
        this.renderTransparentBackground(guiGraphics);
    }
}

