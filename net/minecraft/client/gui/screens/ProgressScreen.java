/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens;

import javax.annotation.Nullable;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProgressListener;

public class ProgressScreen
extends Screen
implements ProgressListener {
    @Nullable
    private Component header;
    @Nullable
    private Component stage;
    private int progress;
    private boolean stop;
    private final boolean clearScreenAfterStop;

    public ProgressScreen(boolean bl) {
        super(GameNarrator.NO_TITLE);
        this.clearScreenAfterStop = bl;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    @Override
    protected boolean shouldNarrateNavigation() {
        return false;
    }

    @Override
    public void progressStartNoAbort(Component component) {
        this.progressStart(component);
    }

    @Override
    public void progressStart(Component component) {
        this.header = component;
        this.progressStage(Component.translatable("menu.working"));
    }

    @Override
    public void progressStage(Component component) {
        this.stage = component;
        this.progressStagePercentage(0);
    }

    @Override
    public void progressStagePercentage(int n) {
        this.progress = n;
    }

    @Override
    public void stop() {
        this.stop = true;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        if (this.stop) {
            if (this.clearScreenAfterStop) {
                this.minecraft.setScreen(null);
            }
            return;
        }
        super.render(guiGraphics, n, n2, f);
        if (this.header != null) {
            guiGraphics.drawCenteredString(this.font, this.header, this.width / 2, 70, -1);
        }
        if (this.stage != null && this.progress != 0) {
            guiGraphics.drawCenteredString(this.font, Component.empty().append(this.stage).append(" " + this.progress + "%"), this.width / 2, 90, -1);
        }
    }
}

