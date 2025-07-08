/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.dialog;

import javax.annotation.Nullable;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.dialog.DialogConnectionAccess;
import net.minecraft.client.gui.screens.dialog.DialogControlSet;
import net.minecraft.client.gui.screens.dialog.DialogScreen;
import net.minecraft.server.dialog.ActionButton;
import net.minecraft.server.dialog.SimpleDialog;

public class SimpleDialogScreen<T extends SimpleDialog>
extends DialogScreen<T> {
    public SimpleDialogScreen(@Nullable Screen screen, T t, DialogConnectionAccess dialogConnectionAccess) {
        super(screen, t, dialogConnectionAccess);
    }

    @Override
    protected void updateHeaderAndFooter(HeaderAndFooterLayout headerAndFooterLayout, DialogControlSet dialogControlSet, T t, DialogConnectionAccess dialogConnectionAccess) {
        super.updateHeaderAndFooter(headerAndFooterLayout, dialogControlSet, t, dialogConnectionAccess);
        LinearLayout linearLayout = LinearLayout.horizontal().spacing(8);
        for (ActionButton actionButton : t.mainActions()) {
            linearLayout.addChild(dialogControlSet.createActionButton(actionButton).build());
        }
        headerAndFooterLayout.addToFooter(linearLayout);
    }
}

