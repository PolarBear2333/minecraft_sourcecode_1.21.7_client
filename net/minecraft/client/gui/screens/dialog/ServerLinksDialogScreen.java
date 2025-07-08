/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.dialog;

import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.dialog.ButtonListDialogScreen;
import net.minecraft.client.gui.screens.dialog.DialogConnectionAccess;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.server.ServerLinks;
import net.minecraft.server.dialog.ActionButton;
import net.minecraft.server.dialog.CommonButtonData;
import net.minecraft.server.dialog.ServerLinksDialog;
import net.minecraft.server.dialog.action.StaticAction;

public class ServerLinksDialogScreen
extends ButtonListDialogScreen<ServerLinksDialog> {
    public ServerLinksDialogScreen(@Nullable Screen screen, ServerLinksDialog serverLinksDialog, DialogConnectionAccess dialogConnectionAccess) {
        super(screen, serverLinksDialog, dialogConnectionAccess);
    }

    @Override
    protected Stream<ActionButton> createListActions(ServerLinksDialog serverLinksDialog, DialogConnectionAccess dialogConnectionAccess) {
        return dialogConnectionAccess.serverLinks().entries().stream().map(entry -> ServerLinksDialogScreen.createDialogClickAction(serverLinksDialog, entry));
    }

    private static ActionButton createDialogClickAction(ServerLinksDialog serverLinksDialog, ServerLinks.Entry entry) {
        return new ActionButton(new CommonButtonData(entry.displayName(), serverLinksDialog.buttonWidth()), Optional.of(new StaticAction(new ClickEvent.OpenUrl(entry.link()))));
    }
}

