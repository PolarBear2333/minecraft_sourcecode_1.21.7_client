/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.gui.screens;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.exception.RealmsDefaultUncaughtExceptionHandler;
import com.mojang.realmsclient.util.task.LongRunningTask;
import java.time.Duration;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.LoadingDotsWidget;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.realms.RepeatedNarrator;
import org.slf4j.Logger;

public class RealmsLongRunningMcoTaskScreen
extends RealmsScreen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final RepeatedNarrator REPEATED_NARRATOR = new RepeatedNarrator(Duration.ofSeconds(5L));
    private final List<LongRunningTask> queuedTasks;
    private final Screen lastScreen;
    protected final LinearLayout layout = LinearLayout.vertical();
    private volatile Component title;
    @Nullable
    private LoadingDotsWidget loadingDotsWidget;

    public RealmsLongRunningMcoTaskScreen(Screen screen, LongRunningTask ... longRunningTaskArray) {
        super(GameNarrator.NO_TITLE);
        this.lastScreen = screen;
        this.queuedTasks = List.of(longRunningTaskArray);
        if (this.queuedTasks.isEmpty()) {
            throw new IllegalArgumentException("No tasks added");
        }
        this.title = this.queuedTasks.get(0).getTitle();
        Runnable runnable = () -> {
            for (LongRunningTask longRunningTask : longRunningTaskArray) {
                this.setTitle(longRunningTask.getTitle());
                if (longRunningTask.aborted()) break;
                longRunningTask.run();
                if (!longRunningTask.aborted()) continue;
                return;
            }
        };
        Thread thread = new Thread(runnable, "Realms-long-running-task");
        thread.setUncaughtExceptionHandler(new RealmsDefaultUncaughtExceptionHandler(LOGGER));
        thread.start();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.loadingDotsWidget != null) {
            REPEATED_NARRATOR.narrate(this.minecraft.getNarrator(), this.loadingDotsWidget.getMessage());
        }
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (n == 256) {
            this.cancel();
            return true;
        }
        return super.keyPressed(n, n2, n3);
    }

    @Override
    public void init() {
        this.layout.defaultCellSetting().alignHorizontallyCenter();
        this.layout.addChild(RealmsLongRunningMcoTaskScreen.realmsLogo());
        this.loadingDotsWidget = new LoadingDotsWidget(this.font, this.title);
        this.layout.addChild(this.loadingDotsWidget, layoutSettings -> layoutSettings.paddingTop(10).paddingBottom(30));
        this.layout.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> this.cancel()).build());
        this.layout.visitWidgets(guiEventListener -> {
            AbstractWidget cfr_ignored_0 = (AbstractWidget)this.addRenderableWidget(guiEventListener);
        });
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        FrameLayout.centerInRectangle(this.layout, this.getRectangle());
    }

    protected void cancel() {
        for (LongRunningTask longRunningTask : this.queuedTasks) {
            longRunningTask.abortTask();
        }
        this.minecraft.setScreen(this.lastScreen);
    }

    public void setTitle(Component component) {
        if (this.loadingDotsWidget != null) {
            this.loadingDotsWidget.setMessage(component);
        }
        this.title = component;
    }
}

