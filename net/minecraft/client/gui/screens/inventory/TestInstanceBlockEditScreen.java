/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.inventory;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.FittingMultiLineTextWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundTestInstanceBlockActionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.TestInstanceBlockEntity;

public class TestInstanceBlockEditScreen
extends Screen {
    private static final Component ID_LABEL = Component.translatable("test_instance_block.test_id");
    private static final Component SIZE_LABEL = Component.translatable("test_instance_block.size");
    private static final Component INCLUDE_ENTITIES_LABEL = Component.translatable("test_instance_block.entities");
    private static final Component ROTATION_LABEL = Component.translatable("test_instance_block.rotation");
    private static final int BUTTON_PADDING = 8;
    private static final int WIDTH = 316;
    private static final int COLOR_SILVER = -4144960;
    private final TestInstanceBlockEntity blockEntity;
    @Nullable
    private EditBox idEdit;
    @Nullable
    private EditBox sizeXEdit;
    @Nullable
    private EditBox sizeYEdit;
    @Nullable
    private EditBox sizeZEdit;
    @Nullable
    private FittingMultiLineTextWidget infoWidget;
    @Nullable
    private Button saveButton;
    @Nullable
    private Button exportButton;
    @Nullable
    private CycleButton<Boolean> includeEntitiesButton;
    @Nullable
    private CycleButton<Rotation> rotationButton;

    public TestInstanceBlockEditScreen(TestInstanceBlockEntity testInstanceBlockEntity) {
        super(testInstanceBlockEntity.getBlockState().getBlock().getName());
        this.blockEntity = testInstanceBlockEntity;
    }

    @Override
    protected void init() {
        int n = this.width / 2 - 158;
        boolean bl = SharedConstants.IS_RUNNING_IN_IDE;
        int n2 = bl ? 3 : 2;
        int n3 = TestInstanceBlockEditScreen.widgetSize(n2);
        this.idEdit = new EditBox(this.font, n, 40, 316, 20, Component.translatable("test_instance_block.test_id"));
        this.idEdit.setMaxLength(128);
        Optional<ResourceKey<GameTestInstance>> optional = this.blockEntity.test();
        if (optional.isPresent()) {
            this.idEdit.setValue(optional.get().location().toString());
        }
        this.idEdit.setResponder(string -> this.updateTestInfo(false));
        this.addRenderableWidget(this.idEdit);
        this.infoWidget = new FittingMultiLineTextWidget(n, 70, 316, 8 * this.font.lineHeight, Component.literal(""), this.font);
        this.addRenderableWidget(this.infoWidget);
        Vec3i vec3i = this.blockEntity.getSize();
        int n4 = 0;
        this.sizeXEdit = new EditBox(this.font, this.widgetX(n4++, 5), 160, TestInstanceBlockEditScreen.widgetSize(5), 20, Component.translatable("structure_block.size.x"));
        this.sizeXEdit.setMaxLength(15);
        this.addRenderableWidget(this.sizeXEdit);
        this.sizeYEdit = new EditBox(this.font, this.widgetX(n4++, 5), 160, TestInstanceBlockEditScreen.widgetSize(5), 20, Component.translatable("structure_block.size.y"));
        this.sizeYEdit.setMaxLength(15);
        this.addRenderableWidget(this.sizeYEdit);
        this.sizeZEdit = new EditBox(this.font, this.widgetX(n4++, 5), 160, TestInstanceBlockEditScreen.widgetSize(5), 20, Component.translatable("structure_block.size.z"));
        this.sizeZEdit.setMaxLength(15);
        this.addRenderableWidget(this.sizeZEdit);
        this.setSize(vec3i);
        this.rotationButton = this.addRenderableWidget(CycleButton.builder(TestInstanceBlockEditScreen::rotationDisplay).withValues((Rotation[])Rotation.values()).withInitialValue(this.blockEntity.getRotation()).displayOnlyValue().create(this.widgetX(n4++, 5), 160, TestInstanceBlockEditScreen.widgetSize(5), 20, ROTATION_LABEL, (cycleButton, rotation) -> this.updateSaveState()));
        this.includeEntitiesButton = this.addRenderableWidget(CycleButton.onOffBuilder(!this.blockEntity.ignoreEntities()).displayOnlyValue().create(this.widgetX(n4++, 5), 160, TestInstanceBlockEditScreen.widgetSize(5), 20, INCLUDE_ENTITIES_LABEL));
        n4 = 0;
        this.addRenderableWidget(Button.builder(Component.translatable("test_instance.action.reset"), button -> {
            this.sendToServer(ServerboundTestInstanceBlockActionPacket.Action.RESET);
            this.minecraft.setScreen(null);
        }).bounds(this.widgetX(n4++, n2), 185, n3, 20).build());
        this.saveButton = this.addRenderableWidget(Button.builder(Component.translatable("test_instance.action.save"), button -> {
            this.sendToServer(ServerboundTestInstanceBlockActionPacket.Action.SAVE);
            this.minecraft.setScreen(null);
        }).bounds(this.widgetX(n4++, n2), 185, n3, 20).build());
        if (bl) {
            this.exportButton = this.addRenderableWidget(Button.builder(Component.literal("Export Structure"), button -> {
                this.sendToServer(ServerboundTestInstanceBlockActionPacket.Action.EXPORT);
                this.minecraft.setScreen(null);
            }).bounds(this.widgetX(n4++, n2), 185, n3, 20).build());
        }
        this.addRenderableWidget(Button.builder(Component.translatable("test_instance.action.run"), button -> {
            this.sendToServer(ServerboundTestInstanceBlockActionPacket.Action.RUN);
            this.minecraft.setScreen(null);
        }).bounds(this.widgetX(0, 3), 210, TestInstanceBlockEditScreen.widgetSize(3), 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onDone()).bounds(this.widgetX(1, 3), 210, TestInstanceBlockEditScreen.widgetSize(3), 20).build());
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onCancel()).bounds(this.widgetX(2, 3), 210, TestInstanceBlockEditScreen.widgetSize(3), 20).build());
        this.updateTestInfo(true);
    }

    private void updateSaveState() {
        boolean bl;
        this.saveButton.active = bl = this.rotationButton.getValue() == Rotation.NONE && ResourceLocation.tryParse(this.idEdit.getValue()) != null;
        if (this.exportButton != null) {
            this.exportButton.active = bl;
        }
    }

    private static Component rotationDisplay(Rotation rotation) {
        return Component.literal(switch (rotation) {
            default -> throw new MatchException(null, null);
            case Rotation.NONE -> "0";
            case Rotation.CLOCKWISE_90 -> "90";
            case Rotation.CLOCKWISE_180 -> "180";
            case Rotation.COUNTERCLOCKWISE_90 -> "270";
        });
    }

    private void setSize(Vec3i vec3i) {
        this.sizeXEdit.setValue(Integer.toString(vec3i.getX()));
        this.sizeYEdit.setValue(Integer.toString(vec3i.getY()));
        this.sizeZEdit.setValue(Integer.toString(vec3i.getZ()));
    }

    private int widgetX(int n, int n2) {
        int n3 = this.width / 2 - 158;
        float f = TestInstanceBlockEditScreen.exactWidgetSize(n2);
        return (int)((float)n3 + (float)n * (8.0f + f));
    }

    private static int widgetSize(int n) {
        return (int)TestInstanceBlockEditScreen.exactWidgetSize(n);
    }

    private static float exactWidgetSize(int n) {
        return (float)(316 - (n - 1) * 8) / (float)n;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.render(guiGraphics, n, n2, f);
        int n3 = this.width / 2 - 158;
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, -1);
        guiGraphics.drawString(this.font, ID_LABEL, n3, 30, -4144960);
        guiGraphics.drawString(this.font, SIZE_LABEL, n3, 150, -4144960);
        guiGraphics.drawString(this.font, ROTATION_LABEL, this.rotationButton.getX(), 150, -4144960);
        guiGraphics.drawString(this.font, INCLUDE_ENTITIES_LABEL, this.includeEntitiesButton.getX(), 150, -4144960);
    }

    private void updateTestInfo(boolean bl) {
        boolean bl2 = this.sendToServer(bl ? ServerboundTestInstanceBlockActionPacket.Action.INIT : ServerboundTestInstanceBlockActionPacket.Action.QUERY);
        if (!bl2) {
            this.infoWidget.setMessage(Component.translatable("test_instance.description.invalid_id").withStyle(ChatFormatting.RED));
        }
        this.updateSaveState();
    }

    private void onDone() {
        this.sendToServer(ServerboundTestInstanceBlockActionPacket.Action.SET);
        this.onClose();
    }

    private boolean sendToServer(ServerboundTestInstanceBlockActionPacket.Action action) {
        Optional<ResourceLocation> optional = Optional.ofNullable(ResourceLocation.tryParse(this.idEdit.getValue()));
        Optional<ResourceKey<GameTestInstance>> optional2 = optional.map(resourceLocation -> ResourceKey.create(Registries.TEST_INSTANCE, resourceLocation));
        Vec3i vec3i = new Vec3i(TestInstanceBlockEditScreen.parseSize(this.sizeXEdit.getValue()), TestInstanceBlockEditScreen.parseSize(this.sizeYEdit.getValue()), TestInstanceBlockEditScreen.parseSize(this.sizeZEdit.getValue()));
        boolean bl = this.includeEntitiesButton.getValue() == false;
        this.minecraft.getConnection().send(new ServerboundTestInstanceBlockActionPacket(this.blockEntity.getBlockPos(), action, optional2, vec3i, this.rotationButton.getValue(), bl));
        return optional.isPresent();
    }

    public void setStatus(Component component2, Optional<Vec3i> optional) {
        MutableComponent mutableComponent = Component.empty();
        this.blockEntity.errorMessage().ifPresent(component -> mutableComponent.append(Component.translatable("test_instance.description.failed", Component.empty().withStyle(ChatFormatting.RED).append((Component)component))).append("\n\n"));
        mutableComponent.append(component2);
        this.infoWidget.setMessage(mutableComponent);
        optional.ifPresent(this::setSize);
    }

    private void onCancel() {
        this.onClose();
    }

    private static int parseSize(String string) {
        try {
            return Mth.clamp(Integer.parseInt(string), 1, 48);
        }
        catch (NumberFormatException numberFormatException) {
            return 1;
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int n, int n2, float f) {
        this.renderTransparentBackground(guiGraphics);
    }
}

