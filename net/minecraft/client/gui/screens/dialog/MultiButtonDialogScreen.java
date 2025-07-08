/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.dialog;

import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.dialog.ButtonListDialogScreen;
import net.minecraft.client.gui.screens.dialog.DialogConnectionAccess;
import net.minecraft.server.dialog.ActionButton;
import net.minecraft.server.dialog.MultiActionDialog;

public class MultiButtonDialogScreen
extends ButtonListDialogScreen<MultiActionDialog> {
    public MultiButtonDialogScreen(@Nullable Screen screen, MultiActionDialog multiActionDialog, DialogConnectionAccess dialogConnectionAccess) {
        super(screen, multiActionDialog, dialogConnectionAccess);
    }

    @Override
    protected Stream<ActionButton> createListActions(MultiActionDialog multiActionDialog, DialogConnectionAccess dialogConnectionAccess) {
        return multiActionDialog.actions().stream();
    }
}

