/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.realmsclient.gui.screens.configuration;

import com.mojang.realmsclient.dto.RealmsServer;

public interface RealmsConfigurationTab {
    public void updateData(RealmsServer var1);

    default public void onSelected(RealmsServer realmsServer) {
    }

    default public void onDeselected(RealmsServer realmsServer) {
    }
}

