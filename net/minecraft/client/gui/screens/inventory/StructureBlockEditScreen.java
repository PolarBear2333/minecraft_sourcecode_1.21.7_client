/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 */
package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.ImmutableList;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetStructureBlockPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.properties.StructureMode;

public class StructureBlockEditScreen
extends Screen {
    private static final Component NAME_LABEL = Component.translatable("structure_block.structure_name");
    private static final Component POSITION_LABEL = Component.translatable("structure_block.position");
    private static final Component SIZE_LABEL = Component.translatable("structure_block.size");
    private static final Component INTEGRITY_LABEL = Component.translatable("structure_block.integrity");
    private static final Component CUSTOM_DATA_LABEL = Component.translatable("structure_block.custom_data");
    private static final Component INCLUDE_ENTITIES_LABEL = Component.translatable("structure_block.include_entities");
    private static final Component STRICT_LABEL = Component.translatable("structure_block.strict");
    private static final Component DETECT_SIZE_LABEL = Component.translatable("structure_block.detect_size");
    private static final Component SHOW_AIR_LABEL = Component.translatable("structure_block.show_air");
    private static final Component SHOW_BOUNDING_BOX_LABEL = Component.translatable("structure_block.show_boundingbox");
    private static final ImmutableList<StructureMode> ALL_MODES = ImmutableList.copyOf((Object[])StructureMode.values());
    private static final ImmutableList<StructureMode> DEFAULT_MODES = (ImmutableList)ALL_MODES.stream().filter(structureMode -> structureMode != StructureMode.DATA).collect(ImmutableList.toImmutableList());
    private final StructureBlockEntity structure;
    private Mirror initialMirror = Mirror.NONE;
    private Rotation initialRotation = Rotation.NONE;
    private StructureMode initialMode = StructureMode.DATA;
    private boolean initialEntityIgnoring;
    private boolean initialStrict;
    private boolean initialShowAir;
    private boolean initialShowBoundingBox;
    private EditBox nameEdit;
    private EditBox posXEdit;
    private EditBox posYEdit;
    private EditBox posZEdit;
    private EditBox sizeXEdit;
    private EditBox sizeYEdit;
    private EditBox sizeZEdit;
    private EditBox integrityEdit;
    private EditBox seedEdit;
    private EditBox dataEdit;
    private Button saveButton;
    private Button loadButton;
    private Button rot0Button;
    private Button rot90Button;
    private Button rot180Button;
    private Button rot270Button;
    private Button detectButton;
    private CycleButton<Boolean> includeEntitiesButton;
    private CycleButton<Boolean> strictButton;
    private CycleButton<Mirror> mirrorButton;
    private CycleButton<Boolean> toggleAirButton;
    private CycleButton<Boolean> toggleBoundingBox;
    private final DecimalFormat decimalFormat = new DecimalFormat("0.0###");

    public StructureBlockEditScreen(StructureBlockEntity structureBlockEntity) {
        super(Component.translatable(Blocks.STRUCTURE_BLOCK.getDescriptionId()));
        this.structure = structureBlockEntity;
        this.decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.ROOT));
    }

    private void onDone() {
        if (this.sendToServer(StructureBlockEntity.UpdateType.UPDATE_DATA)) {
            this.minecraft.setScreen(null);
        }
    }

    private void onCancel() {
        this.structure.setMirror(this.initialMirror);
        this.structure.setRotation(this.initialRotation);
        this.structure.setMode(this.initialMode);
        this.structure.setIgnoreEntities(this.initialEntityIgnoring);
        this.structure.setStrict(this.initialStrict);
        this.structure.setShowAir(this.initialShowAir);
        this.structure.setShowBoundingBox(this.initialShowBoundingBox);
        this.minecraft.setScreen(null);
    }

    @Override
    protected void init() {
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onDone()).bounds(this.width / 2 - 4 - 150, 210, 150, 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onCancel()).bounds(this.width / 2 + 4, 210, 150, 20).build());
        this.initialMirror = this.structure.getMirror();
        this.initialRotation = this.structure.getRotation();
        this.initialMode = this.structure.getMode();
        this.initialEntityIgnoring = this.structure.isIgnoreEntities();
        this.initialStrict = this.structure.isStrict();
        this.initialShowAir = this.structure.getShowAir();
        this.initialShowBoundingBox = this.structure.getShowBoundingBox();
        this.saveButton = this.addRenderableWidget(Button.builder(Component.translatable("structure_block.button.save"), button -> {
            if (this.structure.getMode() == StructureMode.SAVE) {
                this.sendToServer(StructureBlockEntity.UpdateType.SAVE_AREA);
                this.minecraft.setScreen(null);
            }
        }).bounds(this.width / 2 + 4 + 100, 185, 50, 20).build());
        this.loadButton = this.addRenderableWidget(Button.builder(Component.translatable("structure_block.button.load"), button -> {
            if (this.structure.getMode() == StructureMode.LOAD) {
                this.sendToServer(StructureBlockEntity.UpdateType.LOAD_AREA);
                this.minecraft.setScreen(null);
            }
        }).bounds(this.width / 2 + 4 + 100, 185, 50, 20).build());
        this.addRenderableWidget(CycleButton.builder(structureMode -> Component.translatable("structure_block.mode." + structureMode.getSerializedName())).withValues((List<StructureMode>)DEFAULT_MODES, (List<StructureMode>)ALL_MODES).displayOnlyValue().withInitialValue(this.initialMode).create(this.width / 2 - 4 - 150, 185, 50, 20, Component.literal("MODE"), (cycleButton, structureMode) -> {
            this.structure.setMode((StructureMode)structureMode);
            this.updateMode((StructureMode)structureMode);
        }));
        this.detectButton = this.addRenderableWidget(Button.builder(Component.translatable("structure_block.button.detect_size"), button -> {
            if (this.structure.getMode() == StructureMode.SAVE) {
                this.sendToServer(StructureBlockEntity.UpdateType.SCAN_AREA);
                this.minecraft.setScreen(null);
            }
        }).bounds(this.width / 2 + 4 + 100, 120, 50, 20).build());
        this.includeEntitiesButton = this.addRenderableWidget(CycleButton.onOffBuilder(!this.structure.isIgnoreEntities()).displayOnlyValue().create(this.width / 2 + 4 + 100, 160, 50, 20, INCLUDE_ENTITIES_LABEL, (cycleButton, bl) -> this.structure.setIgnoreEntities(bl == false)));
        this.strictButton = this.addRenderableWidget(CycleButton.onOffBuilder(this.structure.isStrict()).displayOnlyValue().create(this.width / 2 + 4 + 100, 120, 50, 20, STRICT_LABEL, (cycleButton, bl) -> this.structure.setStrict((boolean)bl)));
        this.mirrorButton = this.addRenderableWidget(CycleButton.builder(Mirror::symbol).withValues((Mirror[])Mirror.values()).displayOnlyValue().withInitialValue(this.initialMirror).create(this.width / 2 - 20, 185, 40, 20, Component.literal("MIRROR"), (cycleButton, mirror) -> this.structure.setMirror((Mirror)mirror)));
        this.toggleAirButton = this.addRenderableWidget(CycleButton.onOffBuilder(this.structure.getShowAir()).displayOnlyValue().create(this.width / 2 + 4 + 100, 80, 50, 20, SHOW_AIR_LABEL, (cycleButton, bl) -> this.structure.setShowAir((boolean)bl)));
        this.toggleBoundingBox = this.addRenderableWidget(CycleButton.onOffBuilder(this.structure.getShowBoundingBox()).displayOnlyValue().create(this.width / 2 + 4 + 100, 80, 50, 20, SHOW_BOUNDING_BOX_LABEL, (cycleButton, bl) -> this.structure.setShowBoundingBox((boolean)bl)));
        this.rot0Button = this.addRenderableWidget(Button.builder(Component.literal("0"), button -> {
            this.structure.setRotation(Rotation.NONE);
            this.updateDirectionButtons();
        }).bounds(this.width / 2 - 1 - 40 - 1 - 40 - 20, 185, 40, 20).build());
        this.rot90Button = this.addRenderableWidget(Button.builder(Component.literal("90"), button -> {
            this.structure.setRotation(Rotation.CLOCKWISE_90);
            this.updateDirectionButtons();
        }).bounds(this.width / 2 - 1 - 40 - 20, 185, 40, 20).build());
        this.rot180Button = this.addRenderableWidget(Button.builder(Component.literal("180"), button -> {
            this.structure.setRotation(Rotation.CLOCKWISE_180);
            this.updateDirectionButtons();
        }).bounds(this.width / 2 + 1 + 20, 185, 40, 20).build());
        this.rot270Button = this.addRenderableWidget(Button.builder(Component.literal("270"), button -> {
            this.structure.setRotation(Rotation.COUNTERCLOCKWISE_90);
            this.updateDirectionButtons();
        }).bounds(this.width / 2 + 1 + 40 + 1 + 20, 185, 40, 20).build());
        this.nameEdit = new EditBox(this.font, this.width / 2 - 152, 40, 300, 20, (Component)Component.translatable("structure_block.structure_name")){

            @Override
            public boolean charTyped(char c, int n) {
                if (!StructureBlockEditScreen.this.isValidCharacterForName(this.getValue(), c, this.getCursorPosition())) {
                    return false;
                }
                return super.charTyped(c, n);
            }
        };
        this.nameEdit.setMaxLength(128);
        this.nameEdit.setValue(this.structure.getStructureName());
        this.addWidget(this.nameEdit);
        BlockPos blockPos = this.structure.getStructurePos();
        this.posXEdit = new EditBox(this.font, this.width / 2 - 152, 80, 80, 20, Component.translatable("structure_block.position.x"));
        this.posXEdit.setMaxLength(15);
        this.posXEdit.setValue(Integer.toString(blockPos.getX()));
        this.addWidget(this.posXEdit);
        this.posYEdit = new EditBox(this.font, this.width / 2 - 72, 80, 80, 20, Component.translatable("structure_block.position.y"));
        this.posYEdit.setMaxLength(15);
        this.posYEdit.setValue(Integer.toString(blockPos.getY()));
        this.addWidget(this.posYEdit);
        this.posZEdit = new EditBox(this.font, this.width / 2 + 8, 80, 80, 20, Component.translatable("structure_block.position.z"));
        this.posZEdit.setMaxLength(15);
        this.posZEdit.setValue(Integer.toString(blockPos.getZ()));
        this.addWidget(this.posZEdit);
        Vec3i vec3i = this.structure.getStructureSize();
        this.sizeXEdit = new EditBox(this.font, this.width / 2 - 152, 120, 80, 20, Component.translatable("structure_block.size.x"));
        this.sizeXEdit.setMaxLength(15);
        this.sizeXEdit.setValue(Integer.toString(vec3i.getX()));
        this.addWidget(this.sizeXEdit);
        this.sizeYEdit = new EditBox(this.font, this.width / 2 - 72, 120, 80, 20, Component.translatable("structure_block.size.y"));
        this.sizeYEdit.setMaxLength(15);
        this.sizeYEdit.setValue(Integer.toString(vec3i.getY()));
        this.addWidget(this.sizeYEdit);
        this.sizeZEdit = new EditBox(this.font, this.width / 2 + 8, 120, 80, 20, Component.translatable("structure_block.size.z"));
        this.sizeZEdit.setMaxLength(15);
        this.sizeZEdit.setValue(Integer.toString(vec3i.getZ()));
        this.addWidget(this.sizeZEdit);
        this.integrityEdit = new EditBox(this.font, this.width / 2 - 152, 120, 80, 20, Component.translatable("structure_block.integrity.integrity"));
        this.integrityEdit.setMaxLength(15);
        this.integrityEdit.setValue(this.decimalFormat.format(this.structure.getIntegrity()));
        this.addWidget(this.integrityEdit);
        this.seedEdit = new EditBox(this.font, this.width / 2 - 72, 120, 80, 20, Component.translatable("structure_block.integrity.seed"));
        this.seedEdit.setMaxLength(31);
        this.seedEdit.setValue(Long.toString(this.structure.getSeed()));
        this.addWidget(this.seedEdit);
        this.dataEdit = new EditBox(this.font, this.width / 2 - 152, 120, 240, 20, Component.translatable("structure_block.custom_data"));
        this.dataEdit.setMaxLength(128);
        this.dataEdit.setValue(this.structure.getMetaData());
        this.addWidget(this.dataEdit);
        this.updateDirectionButtons();
        this.updateMode(this.initialMode);
    }

    @Override
    protected void setInitialFocus() {
        this.setInitialFocus(this.nameEdit);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int n, int n2, float f) {
        this.renderTransparentBackground(guiGraphics);
    }

    @Override
    public void resize(Minecraft minecraft, int n, int n2) {
        String string = this.nameEdit.getValue();
        String string2 = this.posXEdit.getValue();
        String string3 = this.posYEdit.getValue();
        String string4 = this.posZEdit.getValue();
        String string5 = this.sizeXEdit.getValue();
        String string6 = this.sizeYEdit.getValue();
        String string7 = this.sizeZEdit.getValue();
        String string8 = this.integrityEdit.getValue();
        String string9 = this.seedEdit.getValue();
        String string10 = this.dataEdit.getValue();
        this.init(minecraft, n, n2);
        this.nameEdit.setValue(string);
        this.posXEdit.setValue(string2);
        this.posYEdit.setValue(string3);
        this.posZEdit.setValue(string4);
        this.sizeXEdit.setValue(string5);
        this.sizeYEdit.setValue(string6);
        this.sizeZEdit.setValue(string7);
        this.integrityEdit.setValue(string8);
        this.seedEdit.setValue(string9);
        this.dataEdit.setValue(string10);
    }

    private void updateDirectionButtons() {
        this.rot0Button.active = true;
        this.rot90Button.active = true;
        this.rot180Button.active = true;
        this.rot270Button.active = true;
        switch (this.structure.getRotation()) {
            case NONE: {
                this.rot0Button.active = false;
                break;
            }
            case CLOCKWISE_180: {
                this.rot180Button.active = false;
                break;
            }
            case COUNTERCLOCKWISE_90: {
                this.rot270Button.active = false;
                break;
            }
            case CLOCKWISE_90: {
                this.rot90Button.active = false;
            }
        }
    }

    private void updateMode(StructureMode structureMode) {
        this.nameEdit.setVisible(false);
        this.posXEdit.setVisible(false);
        this.posYEdit.setVisible(false);
        this.posZEdit.setVisible(false);
        this.sizeXEdit.setVisible(false);
        this.sizeYEdit.setVisible(false);
        this.sizeZEdit.setVisible(false);
        this.integrityEdit.setVisible(false);
        this.seedEdit.setVisible(false);
        this.dataEdit.setVisible(false);
        this.saveButton.visible = false;
        this.loadButton.visible = false;
        this.detectButton.visible = false;
        this.includeEntitiesButton.visible = false;
        this.strictButton.visible = false;
        this.mirrorButton.visible = false;
        this.rot0Button.visible = false;
        this.rot90Button.visible = false;
        this.rot180Button.visible = false;
        this.rot270Button.visible = false;
        this.toggleAirButton.visible = false;
        this.toggleBoundingBox.visible = false;
        switch (structureMode) {
            case SAVE: {
                this.nameEdit.setVisible(true);
                this.posXEdit.setVisible(true);
                this.posYEdit.setVisible(true);
                this.posZEdit.setVisible(true);
                this.sizeXEdit.setVisible(true);
                this.sizeYEdit.setVisible(true);
                this.sizeZEdit.setVisible(true);
                this.saveButton.visible = true;
                this.detectButton.visible = true;
                this.includeEntitiesButton.visible = true;
                this.strictButton.visible = false;
                this.toggleAirButton.visible = true;
                break;
            }
            case LOAD: {
                this.nameEdit.setVisible(true);
                this.posXEdit.setVisible(true);
                this.posYEdit.setVisible(true);
                this.posZEdit.setVisible(true);
                this.integrityEdit.setVisible(true);
                this.seedEdit.setVisible(true);
                this.loadButton.visible = true;
                this.includeEntitiesButton.visible = true;
                this.strictButton.visible = true;
                this.mirrorButton.visible = true;
                this.rot0Button.visible = true;
                this.rot90Button.visible = true;
                this.rot180Button.visible = true;
                this.rot270Button.visible = true;
                this.toggleBoundingBox.visible = true;
                this.updateDirectionButtons();
                break;
            }
            case CORNER: {
                this.nameEdit.setVisible(true);
                break;
            }
            case DATA: {
                this.dataEdit.setVisible(true);
            }
        }
    }

    private boolean sendToServer(StructureBlockEntity.UpdateType updateType) {
        BlockPos blockPos = new BlockPos(this.parseCoordinate(this.posXEdit.getValue()), this.parseCoordinate(this.posYEdit.getValue()), this.parseCoordinate(this.posZEdit.getValue()));
        Vec3i vec3i = new Vec3i(this.parseCoordinate(this.sizeXEdit.getValue()), this.parseCoordinate(this.sizeYEdit.getValue()), this.parseCoordinate(this.sizeZEdit.getValue()));
        float f = this.parseIntegrity(this.integrityEdit.getValue());
        long l = this.parseSeed(this.seedEdit.getValue());
        this.minecraft.getConnection().send(new ServerboundSetStructureBlockPacket(this.structure.getBlockPos(), updateType, this.structure.getMode(), this.nameEdit.getValue(), blockPos, vec3i, this.structure.getMirror(), this.structure.getRotation(), this.dataEdit.getValue(), this.structure.isIgnoreEntities(), this.structure.isStrict(), this.structure.getShowAir(), this.structure.getShowBoundingBox(), f, l));
        return true;
    }

    private long parseSeed(String string) {
        try {
            return Long.valueOf(string);
        }
        catch (NumberFormatException numberFormatException) {
            return 0L;
        }
    }

    private float parseIntegrity(String string) {
        try {
            return Float.valueOf(string).floatValue();
        }
        catch (NumberFormatException numberFormatException) {
            return 1.0f;
        }
    }

    private int parseCoordinate(String string) {
        try {
            return Integer.parseInt(string);
        }
        catch (NumberFormatException numberFormatException) {
            return 0;
        }
    }

    @Override
    public void onClose() {
        this.onCancel();
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
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
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.render(guiGraphics, n, n2, f);
        StructureMode structureMode = this.structure.getMode();
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, -1);
        if (structureMode != StructureMode.DATA) {
            guiGraphics.drawString(this.font, NAME_LABEL, this.width / 2 - 153, 30, -6250336);
            this.nameEdit.render(guiGraphics, n, n2, f);
        }
        if (structureMode == StructureMode.LOAD || structureMode == StructureMode.SAVE) {
            guiGraphics.drawString(this.font, POSITION_LABEL, this.width / 2 - 153, 70, -6250336);
            this.posXEdit.render(guiGraphics, n, n2, f);
            this.posYEdit.render(guiGraphics, n, n2, f);
            this.posZEdit.render(guiGraphics, n, n2, f);
            guiGraphics.drawString(this.font, INCLUDE_ENTITIES_LABEL, this.width / 2 + 154 - this.font.width(INCLUDE_ENTITIES_LABEL), 150, -6250336);
        }
        if (structureMode == StructureMode.SAVE) {
            guiGraphics.drawString(this.font, SIZE_LABEL, this.width / 2 - 153, 110, -6250336);
            this.sizeXEdit.render(guiGraphics, n, n2, f);
            this.sizeYEdit.render(guiGraphics, n, n2, f);
            this.sizeZEdit.render(guiGraphics, n, n2, f);
            guiGraphics.drawString(this.font, DETECT_SIZE_LABEL, this.width / 2 + 154 - this.font.width(DETECT_SIZE_LABEL), 110, -6250336);
            guiGraphics.drawString(this.font, SHOW_AIR_LABEL, this.width / 2 + 154 - this.font.width(SHOW_AIR_LABEL), 70, -6250336);
        }
        if (structureMode == StructureMode.LOAD) {
            guiGraphics.drawString(this.font, INTEGRITY_LABEL, this.width / 2 - 153, 110, -6250336);
            this.integrityEdit.render(guiGraphics, n, n2, f);
            this.seedEdit.render(guiGraphics, n, n2, f);
            guiGraphics.drawString(this.font, STRICT_LABEL, this.width / 2 + 154 - this.font.width(STRICT_LABEL), 110, -6250336);
            guiGraphics.drawString(this.font, SHOW_BOUNDING_BOX_LABEL, this.width / 2 + 154 - this.font.width(SHOW_BOUNDING_BOX_LABEL), 70, -6250336);
        }
        if (structureMode == StructureMode.DATA) {
            guiGraphics.drawString(this.font, CUSTOM_DATA_LABEL, this.width / 2 - 153, 110, -6250336);
            this.dataEdit.render(guiGraphics, n, n2, f);
        }
        guiGraphics.drawString(this.font, structureMode.getDisplayName(), this.width / 2 - 153, 174, -6250336);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

