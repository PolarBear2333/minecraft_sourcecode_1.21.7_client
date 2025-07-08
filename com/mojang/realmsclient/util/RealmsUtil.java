/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.yggdrasil.ProfileResult
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.util;

import com.mojang.authlib.yggdrasil.ProfileResult;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.exception.RealmsServiceException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

public class RealmsUtil {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component RIGHT_NOW = Component.translatable("mco.util.time.now");
    private static final int MINUTES = 60;
    private static final int HOURS = 3600;
    private static final int DAYS = 86400;

    public static Component convertToAgePresentation(long l) {
        if (l < 0L) {
            return RIGHT_NOW;
        }
        long l2 = l / 1000L;
        if (l2 < 60L) {
            return Component.translatable("mco.time.secondsAgo", l2);
        }
        if (l2 < 3600L) {
            long l3 = l2 / 60L;
            return Component.translatable("mco.time.minutesAgo", l3);
        }
        if (l2 < 86400L) {
            long l4 = l2 / 3600L;
            return Component.translatable("mco.time.hoursAgo", l4);
        }
        long l5 = l2 / 86400L;
        return Component.translatable("mco.time.daysAgo", l5);
    }

    public static Component convertToAgePresentationFromInstant(Date date) {
        return RealmsUtil.convertToAgePresentation(System.currentTimeMillis() - date.getTime());
    }

    public static void renderPlayerFace(GuiGraphics guiGraphics, int n, int n2, int n3, UUID uUID) {
        Minecraft minecraft = Minecraft.getInstance();
        ProfileResult profileResult = minecraft.getMinecraftSessionService().fetchProfile(uUID, false);
        PlayerSkin playerSkin = profileResult != null ? minecraft.getSkinManager().getInsecureSkin(profileResult.profile()) : DefaultPlayerSkin.get(uUID);
        PlayerFaceRenderer.draw(guiGraphics, playerSkin, n, n2, n3);
    }

    public static <T> CompletableFuture<T> supplyAsync(RealmsIoFunction<T> realmsIoFunction, @Nullable Consumer<RealmsServiceException> consumer) {
        return CompletableFuture.supplyAsync(() -> {
            RealmsClient realmsClient = RealmsClient.getOrCreate();
            try {
                return realmsIoFunction.apply(realmsClient);
            }
            catch (Throwable throwable) {
                if (throwable instanceof RealmsServiceException) {
                    RealmsServiceException realmsServiceException = (RealmsServiceException)throwable;
                    if (consumer != null) {
                        consumer.accept(realmsServiceException);
                    }
                } else {
                    LOGGER.error("Unhandled exception", throwable);
                }
                throw new RuntimeException(throwable);
            }
        }, Util.nonCriticalIoPool());
    }

    public static CompletableFuture<Void> runAsync(RealmsIoConsumer realmsIoConsumer, @Nullable Consumer<RealmsServiceException> consumer) {
        return RealmsUtil.supplyAsync(realmsIoConsumer, consumer);
    }

    public static Consumer<RealmsServiceException> openScreenOnFailure(Function<RealmsServiceException, Screen> function) {
        Minecraft minecraft = Minecraft.getInstance();
        return realmsServiceException -> minecraft.execute(() -> minecraft.setScreen((Screen)function.apply((RealmsServiceException)realmsServiceException)));
    }

    public static Consumer<RealmsServiceException> openScreenAndLogOnFailure(Function<RealmsServiceException, Screen> function, String string) {
        return RealmsUtil.openScreenOnFailure(function).andThen(realmsServiceException -> LOGGER.error(string, (Throwable)realmsServiceException));
    }

    @FunctionalInterface
    public static interface RealmsIoFunction<T> {
        public T apply(RealmsClient var1) throws RealmsServiceException;
    }

    @FunctionalInterface
    public static interface RealmsIoConsumer
    extends RealmsIoFunction<Void> {
        public void accept(RealmsClient var1) throws RealmsServiceException;

        @Override
        default public Void apply(RealmsClient realmsClient) throws RealmsServiceException {
            this.accept(realmsClient);
            return null;
        }
    }
}

