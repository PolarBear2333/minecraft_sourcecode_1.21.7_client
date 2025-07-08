/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.dialog;

import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.dialog.DialogConnectionAccess;
import net.minecraft.client.gui.screens.dialog.DialogControlSet;
import net.minecraft.client.gui.screens.dialog.DialogScreen;
import net.minecraft.server.dialog.ActionButton;
import net.minecraft.server.dialog.ButtonListDialog;

public abstract class ButtonListDialogScreen<T extends ButtonListDialog>
extends DialogScreen<T> {
    public static final int FOOTER_MARGIN = 5;

    public ButtonListDialogScreen(@Nullable Screen screen, T t, DialogConnectionAccess dialogConnectionAccess) {
        super(screen, t, dialogConnectionAccess);
    }

    @Override
    protected void populateBodyElements(LinearLayout linearLayout, DialogControlSet dialogControlSet, T t, DialogConnectionAccess dialogConnectionAccess) {
        super.populateBodyElements(linearLayout, dialogControlSet, t, dialogConnectionAccess);
        List<Button> list = this.createListActions(t, dialogConnectionAccess).map(actionButton -> dialogControlSet.createActionButton((ActionButton)actionButton).build()).toList();
        linearLayout.addChild(ButtonListDialogScreen.packControlsIntoColumns(list, t.columns()));
    }

    protected abstract Stream<ActionButton> createListActions(T var1, DialogConnectionAccess var2);

    @Override
    protected void updateHeaderAndFooter(HeaderAndFooterLayout headerAndFooterLayout, DialogControlSet dialogControlSet, T t, DialogConnectionAccess dialogConnectionAccess) {
        super.updateHeaderAndFooter(headerAndFooterLayout, dialogControlSet, t, dialogConnectionAccess);
        t.exitAction().ifPresentOrElse(actionButton -> headerAndFooterLayout.addToFooter(dialogControlSet.createActionButton((ActionButton)actionButton).build()), () -> headerAndFooterLayout.setFooterHeight(5));
    }
}

