/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.realmsclient.gui.screens;

import java.util.function.Consumer;
import net.minecraft.client.gui.components.PopupScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class RealmsPopups {
    private static final int COLOR_INFO = 8226750;
    private static final Component INFO = Component.translatable("mco.info").withColor(8226750);
    private static final Component WARNING = Component.translatable("mco.warning").withColor(-65536);

    public static PopupScreen customPopupScreen(Screen screen, Component component, Component component2, Consumer<PopupScreen> consumer) {
        return new PopupScreen.Builder(screen, component).setMessage(component2).addButton(CommonComponents.GUI_CONTINUE, consumer).addButton(CommonComponents.GUI_CANCEL, PopupScreen::onClose).build();
    }

    public static PopupScreen infoPopupScreen(Screen screen, Component component, Consumer<PopupScreen> consumer) {
        return new PopupScreen.Builder(screen, INFO).setMessage(component).addButton(CommonComponents.GUI_CONTINUE, consumer).addButton(CommonComponents.GUI_CANCEL, PopupScreen::onClose).build();
    }

    public static PopupScreen warningPopupScreen(Screen screen, Component component, Consumer<PopupScreen> consumer) {
        return new PopupScreen.Builder(screen, WARNING).setMessage(component).addButton(CommonComponents.GUI_CONTINUE, consumer).addButton(CommonComponents.GUI_CANCEL, PopupScreen::onClose).build();
    }

    public static PopupScreen warningAcknowledgePopupScreen(Screen screen, Component component, Consumer<PopupScreen> consumer) {
        return new PopupScreen.Builder(screen, WARNING).setMessage(component).addButton(CommonComponents.GUI_OK, consumer).build();
    }
}

