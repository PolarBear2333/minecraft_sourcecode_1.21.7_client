/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.mutable.MutableObject
 */
package net.minecraft.client.gui.screens.dialog;

import java.lang.runtime.SwitchBootstraps;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.dialog.DialogConnectionAccess;
import net.minecraft.client.gui.screens.dialog.DialogControlSet;
import net.minecraft.client.gui.screens.dialog.WaitingForResponseScreen;
import net.minecraft.client.gui.screens.dialog.body.DialogBodyHandlers;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.dialog.Dialog;
import net.minecraft.server.dialog.DialogAction;
import net.minecraft.server.dialog.Input;
import net.minecraft.server.dialog.body.DialogBody;
import org.apache.commons.lang3.mutable.MutableObject;

public abstract class DialogScreen<T extends Dialog>
extends Screen {
    public static final Component DISCONNECT = Component.translatable("menu.custom_screen_info.disconnect");
    private static final int WARNING_BUTTON_SIZE = 20;
    private static final WidgetSprites WARNING_BUTTON_SPRITES = new WidgetSprites(ResourceLocation.withDefaultNamespace("dialog/warning_button"), ResourceLocation.withDefaultNamespace("dialog/warning_button_disabled"), ResourceLocation.withDefaultNamespace("dialog/warning_button_highlighted"));
    private final T dialog;
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    @Nullable
    private final Screen previousScreen;
    @Nullable
    private ScrollableLayout bodyScroll;
    private Button warningButton;
    private final DialogConnectionAccess connectionAccess;
    private Supplier<Optional<ClickEvent>> onClose = DialogControlSet.EMPTY_ACTION;

    public DialogScreen(@Nullable Screen screen, T t, DialogConnectionAccess dialogConnectionAccess) {
        super(t.common().title());
        this.dialog = t;
        this.previousScreen = screen;
        this.connectionAccess = dialogConnectionAccess;
    }

    @Override
    protected final void init() {
        super.init();
        this.warningButton = this.createWarningButton();
        this.warningButton.setTabOrderGroup(-10);
        DialogControlSet dialogControlSet = new DialogControlSet(this);
        LinearLayout linearLayout = LinearLayout.vertical().spacing(10);
        linearLayout.defaultCellSetting().alignHorizontallyCenter();
        this.layout.addToHeader(this.createTitleWithWarningButton());
        for (DialogBody object : this.dialog.common().body()) {
            LayoutElement layoutElement = DialogBodyHandlers.createBodyElement(this, object);
            if (layoutElement == null) continue;
            linearLayout.addChild(layoutElement);
        }
        for (Input input : this.dialog.common().inputs()) {
            dialogControlSet.addInput(input, linearLayout::addChild);
        }
        this.populateBodyElements(linearLayout, dialogControlSet, this.dialog, this.connectionAccess);
        this.bodyScroll = new ScrollableLayout(this.minecraft, linearLayout, this.layout.getContentHeight());
        this.layout.addToContents(this.bodyScroll);
        this.updateHeaderAndFooter(this.layout, dialogControlSet, this.dialog, this.connectionAccess);
        this.onClose = dialogControlSet.bindAction(this.dialog.onCancel());
        this.layout.visitWidgets(abstractWidget -> {
            if (abstractWidget != this.warningButton) {
                this.addRenderableWidget(abstractWidget);
            }
        });
        this.addRenderableWidget(this.warningButton);
        this.repositionElements();
    }

    protected void populateBodyElements(LinearLayout linearLayout, DialogControlSet dialogControlSet, T t, DialogConnectionAccess dialogConnectionAccess) {
    }

    protected void updateHeaderAndFooter(HeaderAndFooterLayout headerAndFooterLayout, DialogControlSet dialogControlSet, T t, DialogConnectionAccess dialogConnectionAccess) {
    }

    @Override
    protected void repositionElements() {
        this.bodyScroll.setMaxHeight(this.layout.getContentHeight());
        this.layout.arrangeElements();
        this.makeSureWarningButtonIsInBounds();
    }

    protected LayoutElement createTitleWithWarningButton() {
        LinearLayout linearLayout = LinearLayout.horizontal().spacing(10);
        linearLayout.defaultCellSetting().alignHorizontallyCenter().alignVerticallyMiddle();
        linearLayout.addChild(new StringWidget(this.title, this.font));
        linearLayout.addChild(this.warningButton);
        return linearLayout;
    }

    protected void makeSureWarningButtonIsInBounds() {
        int n = this.warningButton.getX();
        int n2 = this.warningButton.getY();
        if (n < 0 || n2 < 0 || n > this.width - 20 || n2 > this.height - 20) {
            this.warningButton.setX(Math.max(0, this.width - 40));
            this.warningButton.setY(Math.min(5, this.height));
        }
    }

    private Button createWarningButton() {
        ImageButton imageButton = new ImageButton(0, 0, 20, 20, WARNING_BUTTON_SPRITES, button -> this.minecraft.setScreen(WarningScreen.create(this.minecraft, this)), Component.translatable("menu.custom_screen_info.button_narration"));
        imageButton.setTooltip(Tooltip.create(Component.translatable("menu.custom_screen_info.tooltip")));
        return imageButton;
    }

    @Override
    public boolean isPauseScreen() {
        return this.dialog.common().pause();
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return this.dialog.common().canCloseWithEscape();
    }

    @Override
    public void onClose() {
        this.runAction(this.onClose.get(), DialogAction.CLOSE);
    }

    public void runAction(Optional<ClickEvent> optional) {
        this.runAction(optional, this.dialog.common().afterAction());
    }

    public void runAction(Optional<ClickEvent> optional, DialogAction dialogAction) {
        Screen screen;
        switch (dialogAction) {
            default: {
                throw new MatchException(null, null);
            }
            case NONE: {
                Screen screen2 = this;
                break;
            }
            case CLOSE: {
                Screen screen2 = this.previousScreen;
                break;
            }
            case WAIT_FOR_RESPONSE: {
                Screen screen2 = screen = new WaitingForResponseScreen(this.previousScreen);
            }
        }
        if (optional.isPresent()) {
            this.handleDialogClickEvent(optional.get(), screen);
        } else {
            this.minecraft.setScreen(screen);
        }
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private void handleDialogClickEvent(ClickEvent clickEvent, @Nullable Screen screen) {
        ClickEvent clickEvent2 = clickEvent;
        Objects.requireNonNull(clickEvent2);
        ClickEvent clickEvent3 = clickEvent2;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{ClickEvent.RunCommand.class, ClickEvent.ShowDialog.class, ClickEvent.Custom.class}, (Object)clickEvent3, n)) {
            case 0: {
                ClickEvent.RunCommand runCommand = (ClickEvent.RunCommand)clickEvent3;
                try {
                    String string;
                    String string2 = string = runCommand.command();
                    this.connectionAccess.runCommand(Commands.trimOptionalPrefix(string2), screen);
                    return;
                }
                catch (Throwable throwable) {
                    throw new MatchException(throwable.toString(), throwable);
                }
            }
            case 1: {
                ClickEvent.ShowDialog showDialog = (ClickEvent.ShowDialog)clickEvent3;
                this.connectionAccess.openDialog(showDialog.dialog(), screen);
                return;
            }
            case 2: {
                ClickEvent.Custom custom = (ClickEvent.Custom)clickEvent3;
                this.connectionAccess.sendCustomAction(custom.id(), custom.payload());
                this.minecraft.setScreen(screen);
                return;
            }
        }
        DialogScreen.defaultHandleClickEvent(clickEvent, this.minecraft, screen);
    }

    @Nullable
    public Screen previousScreen() {
        return this.previousScreen;
    }

    protected static LayoutElement packControlsIntoColumns(List<? extends LayoutElement> list, int n) {
        GridLayout gridLayout = new GridLayout();
        gridLayout.defaultCellSetting().alignHorizontallyCenter();
        gridLayout.columnSpacing(2).rowSpacing(2);
        int n2 = list.size();
        int n3 = n2 / n;
        int n4 = n3 * n;
        for (int i = 0; i < n4; ++i) {
            gridLayout.addChild(list.get(i), i / n, i % n);
        }
        if (n2 != n4) {
            LinearLayout linearLayout = LinearLayout.horizontal().spacing(2);
            linearLayout.defaultCellSetting().alignHorizontallyCenter();
            for (int i = n4; i < n2; ++i) {
                linearLayout.addChild(list.get(i));
            }
            gridLayout.addChild(linearLayout, n3, 0, 1, n);
        }
        return gridLayout;
    }

    public static class WarningScreen
    extends ConfirmScreen {
        private final MutableObject<Screen> returnScreen;

        public static Screen create(Minecraft minecraft, Screen screen) {
            return new WarningScreen(minecraft, (MutableObject<Screen>)new MutableObject((Object)screen));
        }

        private WarningScreen(Minecraft minecraft, MutableObject<Screen> mutableObject) {
            super(bl -> {
                if (bl) {
                    PauseScreen.disconnectFromWorld(minecraft, DISCONNECT);
                } else {
                    minecraft.setScreen((Screen)mutableObject.getValue());
                }
            }, Component.translatable("menu.custom_screen_info.title"), Component.translatable("menu.custom_screen_info.contents"), CommonComponents.disconnectButtonLabel(minecraft.isLocalServer()), CommonComponents.GUI_BACK);
            this.returnScreen = mutableObject;
        }

        @Nullable
        public Screen returnScreen() {
            return (Screen)this.returnScreen.getValue();
        }

        public void updateReturnScreen(@Nullable Screen screen) {
            this.returnScreen.setValue((Object)screen);
        }
    }
}

