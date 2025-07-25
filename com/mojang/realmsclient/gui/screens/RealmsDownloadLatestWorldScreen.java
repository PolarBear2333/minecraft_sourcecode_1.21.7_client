/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.util.concurrent.RateLimiter
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.booleans.BooleanConsumer
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.RateLimiter;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.Unit;
import com.mojang.realmsclient.client.FileDownload;
import com.mojang.realmsclient.dto.WorldDownload;
import com.mojang.realmsclient.gui.screens.RealmsPopups;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.GameNarrator;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.realms.RealmsScreen;
import org.slf4j.Logger;

public class RealmsDownloadLatestWorldScreen
extends RealmsScreen {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ReentrantLock DOWNLOAD_LOCK = new ReentrantLock();
    private static final int BAR_WIDTH = 200;
    private static final int BAR_TOP = 80;
    private static final int BAR_BOTTOM = 95;
    private static final int BAR_BORDER = 1;
    private final Screen lastScreen;
    private final WorldDownload worldDownload;
    private final Component downloadTitle;
    private final RateLimiter narrationRateLimiter;
    private Button cancelButton;
    private final String worldName;
    private final DownloadStatus downloadStatus;
    @Nullable
    private volatile Component errorMessage;
    private volatile Component status = Component.translatable("mco.download.preparing");
    @Nullable
    private volatile String progress;
    private volatile boolean cancelled;
    private volatile boolean showDots = true;
    private volatile boolean finished;
    private volatile boolean extracting;
    @Nullable
    private Long previousWrittenBytes;
    @Nullable
    private Long previousTimeSnapshot;
    private long bytesPersSecond;
    private int animTick;
    private static final String[] DOTS = new String[]{"", ".", ". .", ". . ."};
    private int dotIndex;
    private boolean checked;
    private final BooleanConsumer callback;

    public RealmsDownloadLatestWorldScreen(Screen screen, WorldDownload worldDownload, String string, BooleanConsumer booleanConsumer) {
        super(GameNarrator.NO_TITLE);
        this.callback = booleanConsumer;
        this.lastScreen = screen;
        this.worldName = string;
        this.worldDownload = worldDownload;
        this.downloadStatus = new DownloadStatus();
        this.downloadTitle = Component.translatable("mco.download.title");
        this.narrationRateLimiter = RateLimiter.create((double)0.1f);
    }

    @Override
    public void init() {
        this.cancelButton = this.addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose()).bounds((this.width - 200) / 2, this.height - 42, 200, 20).build());
        this.checkDownloadSize();
    }

    private void checkDownloadSize() {
        if (this.finished || this.checked) {
            return;
        }
        this.checked = true;
        if (this.getContentLength(this.worldDownload.downloadLink) >= 0x140000000L) {
            MutableComponent mutableComponent = Component.translatable("mco.download.confirmation.oversized", Unit.humanReadable(0x140000000L));
            this.minecraft.setScreen(RealmsPopups.warningAcknowledgePopupScreen(this, mutableComponent, popupScreen -> {
                this.minecraft.setScreen(this);
                this.downloadSave();
            }));
        } else {
            this.downloadSave();
        }
    }

    private long getContentLength(String string) {
        FileDownload fileDownload = new FileDownload();
        return fileDownload.contentLength(string);
    }

    @Override
    public void tick() {
        super.tick();
        ++this.animTick;
        if (this.status != null && this.narrationRateLimiter.tryAcquire(1)) {
            Component component = this.createProgressNarrationMessage();
            this.minecraft.getNarrator().saySystemNow(component);
        }
    }

    private Component createProgressNarrationMessage() {
        ArrayList arrayList = Lists.newArrayList();
        arrayList.add(this.downloadTitle);
        arrayList.add(this.status);
        if (this.progress != null) {
            arrayList.add(Component.translatable("mco.download.percent", this.progress));
            arrayList.add(Component.translatable("mco.download.speed.narration", Unit.humanReadable(this.bytesPersSecond)));
        }
        if (this.errorMessage != null) {
            arrayList.add(this.errorMessage);
        }
        return CommonComponents.joinLines(arrayList);
    }

    @Override
    public void onClose() {
        this.cancelled = true;
        if (this.finished && this.callback != null && this.errorMessage == null) {
            this.callback.accept(true);
        }
        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.render(guiGraphics, n, n2, f);
        guiGraphics.drawCenteredString(this.font, this.downloadTitle, this.width / 2, 20, -1);
        guiGraphics.drawCenteredString(this.font, this.status, this.width / 2, 50, -1);
        if (this.showDots) {
            this.drawDots(guiGraphics);
        }
        if (this.downloadStatus.bytesWritten != 0L && !this.cancelled) {
            this.drawProgressBar(guiGraphics);
            this.drawDownloadSpeed(guiGraphics);
        }
        if (this.errorMessage != null) {
            guiGraphics.drawCenteredString(this.font, this.errorMessage, this.width / 2, 110, -65536);
        }
    }

    private void drawDots(GuiGraphics guiGraphics) {
        int n = this.font.width(this.status);
        if (this.animTick != 0 && this.animTick % 10 == 0) {
            ++this.dotIndex;
        }
        guiGraphics.drawString(this.font, DOTS[this.dotIndex % DOTS.length], this.width / 2 + n / 2 + 5, 50, -1);
    }

    private void drawProgressBar(GuiGraphics guiGraphics) {
        double d = Math.min((double)this.downloadStatus.bytesWritten / (double)this.downloadStatus.totalBytes, 1.0);
        this.progress = String.format(Locale.ROOT, "%.1f", d * 100.0);
        int n = (this.width - 200) / 2;
        int n2 = n + (int)Math.round(200.0 * d);
        guiGraphics.fill(n - 1, 79, n2 + 1, 96, -1);
        guiGraphics.fill(n, 80, n2, 95, -8355712);
        guiGraphics.drawCenteredString(this.font, Component.translatable("mco.download.percent", this.progress), this.width / 2, 84, -1);
    }

    private void drawDownloadSpeed(GuiGraphics guiGraphics) {
        if (this.animTick % 20 == 0) {
            if (this.previousWrittenBytes != null) {
                long l = Util.getMillis() - this.previousTimeSnapshot;
                if (l == 0L) {
                    l = 1L;
                }
                this.bytesPersSecond = 1000L * (this.downloadStatus.bytesWritten - this.previousWrittenBytes) / l;
                this.drawDownloadSpeed0(guiGraphics, this.bytesPersSecond);
            }
            this.previousWrittenBytes = this.downloadStatus.bytesWritten;
            this.previousTimeSnapshot = Util.getMillis();
        } else {
            this.drawDownloadSpeed0(guiGraphics, this.bytesPersSecond);
        }
    }

    private void drawDownloadSpeed0(GuiGraphics guiGraphics, long l) {
        if (l > 0L) {
            int n = this.font.width(this.progress);
            guiGraphics.drawString(this.font, Component.translatable("mco.download.speed", Unit.humanReadable(l)), this.width / 2 + n / 2 + 15, 84, -1);
        }
    }

    private void downloadSave() {
        new Thread(() -> {
            try {
                if (!DOWNLOAD_LOCK.tryLock(1L, TimeUnit.SECONDS)) {
                    this.status = Component.translatable("mco.download.failed");
                    return;
                }
                if (this.cancelled) {
                    this.downloadCancelled();
                    return;
                }
                this.status = Component.translatable("mco.download.downloading", this.worldName);
                FileDownload fileDownload = new FileDownload();
                fileDownload.contentLength(this.worldDownload.downloadLink);
                fileDownload.download(this.worldDownload, this.worldName, this.downloadStatus, this.minecraft.getLevelSource());
                while (!fileDownload.isFinished()) {
                    if (fileDownload.isError()) {
                        fileDownload.cancel();
                        this.errorMessage = Component.translatable("mco.download.failed");
                        this.cancelButton.setMessage(CommonComponents.GUI_DONE);
                        return;
                    }
                    if (fileDownload.isExtracting()) {
                        if (!this.extracting) {
                            this.status = Component.translatable("mco.download.extracting");
                        }
                        this.extracting = true;
                    }
                    if (this.cancelled) {
                        fileDownload.cancel();
                        this.downloadCancelled();
                        return;
                    }
                    try {
                        Thread.sleep(500L);
                    }
                    catch (InterruptedException interruptedException) {
                        LOGGER.error("Failed to check Realms backup download status");
                    }
                }
                this.finished = true;
                this.status = Component.translatable("mco.download.done");
                this.cancelButton.setMessage(CommonComponents.GUI_DONE);
            }
            catch (InterruptedException interruptedException) {
                LOGGER.error("Could not acquire upload lock");
            }
            catch (Exception exception) {
                this.errorMessage = Component.translatable("mco.download.failed");
                LOGGER.info("Exception while downloading world", (Throwable)exception);
            }
            finally {
                if (!DOWNLOAD_LOCK.isHeldByCurrentThread()) {
                    return;
                }
                DOWNLOAD_LOCK.unlock();
                this.showDots = false;
                this.finished = true;
            }
        }).start();
    }

    private void downloadCancelled() {
        this.status = Component.translatable("mco.download.cancelled");
    }

    public static class DownloadStatus {
        public volatile long bytesWritten;
        public volatile long totalBytes;
    }
}

