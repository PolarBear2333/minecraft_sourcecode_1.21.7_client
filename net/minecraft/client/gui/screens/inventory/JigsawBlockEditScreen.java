/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.inventory;

import net.minecraft.client.GameNarrator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundJigsawGeneratePacket;
import net.minecraft.network.protocol.game.ServerboundSetJigsawBlockPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;

public class JigsawBlockEditScreen
extends Screen {
    private static final Component JOINT_LABEL = Component.translatable("jigsaw_block.joint_label");
    private static final Component POOL_LABEL = Component.translatable("jigsaw_block.pool");
    private static final Component NAME_LABEL = Component.translatable("jigsaw_block.name");
    private static final Component TARGET_LABEL = Component.translatable("jigsaw_block.target");
    private static final Component FINAL_STATE_LABEL = Component.translatable("jigsaw_block.final_state");
    private static final Component PLACEMENT_PRIORITY_LABEL = Component.translatable("jigsaw_block.placement_priority");
    private static final Component PLACEMENT_PRIORITY_TOOLTIP = Component.translatable("jigsaw_block.placement_priority.tooltip");
    private static final Component SELECTION_PRIORITY_LABEL = Component.translatable("jigsaw_block.selection_priority");
    private static final Component SELECTION_PRIORITY_TOOLTIP = Component.translatable("jigsaw_block.selection_priority.tooltip");
    private final JigsawBlockEntity jigsawEntity;
    private EditBox nameEdit;
    private EditBox targetEdit;
    private EditBox poolEdit;
    private EditBox finalStateEdit;
    private EditBox selectionPriorityEdit;
    private EditBox placementPriorityEdit;
    int levels;
    private boolean keepJigsaws = true;
    private CycleButton<JigsawBlockEntity.JointType> jointButton;
    private Button doneButton;
    private Button generateButton;
    private JigsawBlockEntity.JointType joint;

    public JigsawBlockEditScreen(JigsawBlockEntity jigsawBlockEntity) {
        super(GameNarrator.NO_TITLE);
        this.jigsawEntity = jigsawBlockEntity;
    }

    private void onDone() {
        this.sendToServer();
        this.minecraft.setScreen(null);
    }

    private void onCancel() {
        this.minecraft.setScreen(null);
    }

    private void sendToServer() {
        this.minecraft.getConnection().send(new ServerboundSetJigsawBlockPacket(this.jigsawEntity.getBlockPos(), ResourceLocation.parse(this.nameEdit.getValue()), ResourceLocation.parse(this.targetEdit.getValue()), ResourceLocation.parse(this.poolEdit.getValue()), this.finalStateEdit.getValue(), this.joint, this.parseAsInt(this.selectionPriorityEdit.getValue()), this.parseAsInt(this.placementPriorityEdit.getValue())));
    }

    private int parseAsInt(String string) {
        try {
            return Integer.parseInt(string);
        }
        catch (NumberFormatException numberFormatException) {
            return 0;
        }
    }

    private void sendGenerate() {
        this.minecraft.getConnection().send(new ServerboundJigsawGeneratePacket(this.jigsawEntity.getBlockPos(), this.levels, this.keepJigsaws));
    }

    @Override
    public void onClose() {
        this.onCancel();
    }

    @Override
    protected void init() {
        boolean bl2;
        this.poolEdit = new EditBox(this.font, this.width / 2 - 153, 20, 300, 20, POOL_LABEL);
        this.poolEdit.setMaxLength(128);
        this.poolEdit.setValue(this.jigsawEntity.getPool().location().toString());
        this.poolEdit.setResponder(string -> this.updateValidity());
        this.addWidget(this.poolEdit);
        this.nameEdit = new EditBox(this.font, this.width / 2 - 153, 55, 300, 20, NAME_LABEL);
        this.nameEdit.setMaxLength(128);
        this.nameEdit.setValue(this.jigsawEntity.getName().toString());
        this.nameEdit.setResponder(string -> this.updateValidity());
        this.addWidget(this.nameEdit);
        this.targetEdit = new EditBox(this.font, this.width / 2 - 153, 90, 300, 20, TARGET_LABEL);
        this.targetEdit.setMaxLength(128);
        this.targetEdit.setValue(this.jigsawEntity.getTarget().toString());
        this.targetEdit.setResponder(string -> this.updateValidity());
        this.addWidget(this.targetEdit);
        this.finalStateEdit = new EditBox(this.font, this.width / 2 - 153, 125, 300, 20, FINAL_STATE_LABEL);
        this.finalStateEdit.setMaxLength(256);
        this.finalStateEdit.setValue(this.jigsawEntity.getFinalState());
        this.addWidget(this.finalStateEdit);
        this.selectionPriorityEdit = new EditBox(this.font, this.width / 2 - 153, 160, 98, 20, SELECTION_PRIORITY_LABEL);
        this.selectionPriorityEdit.setMaxLength(3);
        this.selectionPriorityEdit.setValue(Integer.toString(this.jigsawEntity.getSelectionPriority()));
        this.selectionPriorityEdit.setTooltip(Tooltip.create(SELECTION_PRIORITY_TOOLTIP));
        this.addWidget(this.selectionPriorityEdit);
        this.placementPriorityEdit = new EditBox(this.font, this.width / 2 - 50, 160, 98, 20, PLACEMENT_PRIORITY_LABEL);
        this.placementPriorityEdit.setMaxLength(3);
        this.placementPriorityEdit.setValue(Integer.toString(this.jigsawEntity.getPlacementPriority()));
        this.placementPriorityEdit.setTooltip(Tooltip.create(PLACEMENT_PRIORITY_TOOLTIP));
        this.addWidget(this.placementPriorityEdit);
        this.joint = this.jigsawEntity.getJoint();
        this.jointButton = this.addRenderableWidget(CycleButton.builder(JigsawBlockEntity.JointType::getTranslatedName).withValues((JigsawBlockEntity.JointType[])JigsawBlockEntity.JointType.values()).withInitialValue(this.joint).displayOnlyValue().create(this.width / 2 + 54, 160, 100, 20, JOINT_LABEL, (cycleButton, jointType) -> {
            this.joint = jointType;
        }));
        this.jointButton.active = bl2 = JigsawBlock.getFrontFacing(this.jigsawEntity.getBlockState()).getAxis().isVertical();
        this.jointButton.visible = bl2;
        this.addRenderableWidget(new AbstractSliderButton(this.width / 2 - 154, 185, 100, 20, CommonComponents.EMPTY, 0.0){
            {
                this.updateMessage();
            }

            @Override
            protected void updateMessage() {
                this.setMessage(Component.translatable("jigsaw_block.levels", JigsawBlockEditScreen.this.levels));
            }

            @Override
            protected void applyValue() {
                JigsawBlockEditScreen.this.levels = Mth.floor(Mth.clampedLerp(0.0, 20.0, this.value));
            }
        });
        this.addRenderableWidget(CycleButton.onOffBuilder(this.keepJigsaws).create(this.width / 2 - 50, 185, 100, 20, Component.translatable("jigsaw_block.keep_jigsaws"), (cycleButton, bl) -> {
            this.keepJigsaws = bl;
        }));
        this.generateButton = this.addRenderableWidget(Button.builder(Component.translatable("jigsaw_block.generate"), button -> {
            this.onDone();
            this.sendGenerate();
        }).bounds(this.width / 2 + 54, 185, 100, 20).build());
        this.doneButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onDone()).bounds(this.width / 2 - 4 - 150, 210, 150, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onCancel()).bounds(this.width / 2 + 4, 210, 150, 20).build());
        this.updateValidity();
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.poolEdit);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int n, int n2, float f) {
        this.renderTransparentBackground(guiGraphics);
    }

    public static boolean isValidResourceLocation(String string) {
        return ResourceLocation.tryParse(string) != null;
    }

    private void updateValidity() {
        boolean bl;
        this.doneButton.active = bl = JigsawBlockEditScreen.isValidResourceLocation(this.nameEdit.getValue()) && JigsawBlockEditScreen.isValidResourceLocation(this.targetEdit.getValue()) && JigsawBlockEditScreen.isValidResourceLocation(this.poolEdit.getValue());
        this.generateButton.active = bl;
    }

    @Override
    public void resize(Minecraft minecraft, int n, int n2) {
        String string = this.nameEdit.getValue();
        String string2 = this.targetEdit.getValue();
        String string3 = this.poolEdit.getValue();
        String string4 = this.finalStateEdit.getValue();
        String string5 = this.selectionPriorityEdit.getValue();
        String string6 = this.placementPriorityEdit.getValue();
        int n3 = this.levels;
        JigsawBlockEntity.JointType jointType = this.joint;
        this.init(minecraft, n, n2);
        this.nameEdit.setValue(string);
        this.targetEdit.setValue(string2);
        this.poolEdit.setValue(string3);
        this.finalStateEdit.setValue(string4);
        this.levels = n3;
        this.joint = jointType;
        this.jointButton.setValue(jointType);
        this.selectionPriorityEdit.setValue(string5);
        this.placementPriorityEdit.setValue(string6);
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (super.keyPressed(n, n2, n3)) {
            return true;
        }
        if (this.doneButton.active && (n == 257 || n == 335)) {
            this.onDone();
            return true;
        }
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.render(guiGraphics, n, n2, f);
        guiGraphics.drawString(this.font, POOL_LABEL, this.width / 2 - 153, 10, -6250336);
        this.poolEdit.render(guiGraphics, n, n2, f);
        guiGraphics.drawString(this.font, NAME_LABEL, this.width / 2 - 153, 45, -6250336);
        this.nameEdit.render(guiGraphics, n, n2, f);
        guiGraphics.drawString(this.font, TARGET_LABEL, this.width / 2 - 153, 80, -6250336);
        this.targetEdit.render(guiGraphics, n, n2, f);
        guiGraphics.drawString(this.font, FINAL_STATE_LABEL, this.width / 2 - 153, 115, -6250336);
        this.finalStateEdit.render(guiGraphics, n, n2, f);
        guiGraphics.drawString(this.font, SELECTION_PRIORITY_LABEL, this.width / 2 - 153, 150, -6250336);
        this.placementPriorityEdit.render(guiGraphics, n, n2, f);
        guiGraphics.drawString(this.font, PLACEMENT_PRIORITY_LABEL, this.width / 2 - 50, 150, -6250336);
        this.selectionPriorityEdit.render(guiGraphics, n, n2, f);
        if (JigsawBlock.getFrontFacing(this.jigsawEntity.getBlockState()).getAxis().isVertical()) {
            guiGraphics.drawString(this.font, JOINT_LABEL, this.width / 2 + 53, 150, -6250336);
        }
    }
}

