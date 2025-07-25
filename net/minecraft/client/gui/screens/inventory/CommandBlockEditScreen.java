/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.inventory.AbstractCommandBlockEditScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.world.level.BaseCommandBlock;
import net.minecraft.world.level.block.entity.CommandBlockEntity;

public class CommandBlockEditScreen
extends AbstractCommandBlockEditScreen {
    private final CommandBlockEntity autoCommandBlock;
    private CycleButton<CommandBlockEntity.Mode> modeButton;
    private CycleButton<Boolean> conditionalButton;
    private CycleButton<Boolean> autoexecButton;
    private CommandBlockEntity.Mode mode = CommandBlockEntity.Mode.REDSTONE;
    private boolean conditional;
    private boolean autoexec;

    public CommandBlockEditScreen(CommandBlockEntity commandBlockEntity) {
        this.autoCommandBlock = commandBlockEntity;
    }

    @Override
    BaseCommandBlock getCommandBlock() {
        return this.autoCommandBlock.getCommandBlock();
    }

    @Override
    int getPreviousY() {
        return 135;
    }

    @Override
    protected void init() {
        super.init();
        this.modeButton = this.addRenderableWidget(CycleButton.builder(mode -> switch (mode) {
            default -> throw new MatchException(null, null);
            case CommandBlockEntity.Mode.SEQUENCE -> Component.translatable("advMode.mode.sequence");
            case CommandBlockEntity.Mode.AUTO -> Component.translatable("advMode.mode.auto");
            case CommandBlockEntity.Mode.REDSTONE -> Component.translatable("advMode.mode.redstone");
        }).withValues((CommandBlockEntity.Mode[])CommandBlockEntity.Mode.values()).displayOnlyValue().withInitialValue(this.mode).create(this.width / 2 - 50 - 100 - 4, 165, 100, 20, Component.translatable("advMode.mode"), (cycleButton, mode) -> {
            this.mode = mode;
        }));
        this.conditionalButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("advMode.mode.conditional"), Component.translatable("advMode.mode.unconditional")).displayOnlyValue().withInitialValue(this.conditional).create(this.width / 2 - 50, 165, 100, 20, Component.translatable("advMode.type"), (cycleButton, bl) -> {
            this.conditional = bl;
        }));
        this.autoexecButton = this.addRenderableWidget(CycleButton.booleanBuilder(Component.translatable("advMode.mode.autoexec.bat"), Component.translatable("advMode.mode.redstoneTriggered")).displayOnlyValue().withInitialValue(this.autoexec).create(this.width / 2 + 50 + 4, 165, 100, 20, Component.translatable("advMode.triggering"), (cycleButton, bl) -> {
            this.autoexec = bl;
        }));
        this.enableControls(false);
    }

    private void enableControls(boolean bl) {
        this.doneButton.active = bl;
        this.outputButton.active = bl;
        this.modeButton.active = bl;
        this.conditionalButton.active = bl;
        this.autoexecButton.active = bl;
    }

    public void updateGui() {
        BaseCommandBlock baseCommandBlock = this.autoCommandBlock.getCommandBlock();
        this.commandEdit.setValue(baseCommandBlock.getCommand());
        boolean bl = baseCommandBlock.isTrackOutput();
        this.mode = this.autoCommandBlock.getMode();
        this.conditional = this.autoCommandBlock.isConditional();
        this.autoexec = this.autoCommandBlock.isAutomatic();
        this.outputButton.setValue(bl);
        this.modeButton.setValue(this.mode);
        this.conditionalButton.setValue(this.conditional);
        this.autoexecButton.setValue(this.autoexec);
        this.updatePreviousOutput(bl);
        this.enableControls(true);
    }

    @Override
    public void resize(Minecraft minecraft, int n, int n2) {
        super.resize(minecraft, n, n2);
        this.enableControls(true);
    }

    @Override
    protected void populateAndSendPacket(BaseCommandBlock baseCommandBlock) {
        this.minecraft.getConnection().send(new ServerboundSetCommandBlockPacket(BlockPos.containing(baseCommandBlock.getPosition()), this.commandEdit.getValue(), this.mode, baseCommandBlock.isTrackOutput(), this.conditional, this.autoexec));
    }
}

