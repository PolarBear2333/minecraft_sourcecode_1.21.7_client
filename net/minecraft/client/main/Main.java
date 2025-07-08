/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Stopwatch
 *  com.google.common.base.Ticker
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.mojang.authlib.properties.PropertyMap
 *  com.mojang.authlib.properties.PropertyMap$Serializer
 *  com.mojang.jtracy.TracyClient
 *  com.mojang.logging.LogUtils
 *  com.mojang.util.UndashedUuid
 *  javax.annotation.Nullable
 *  joptsimple.ArgumentAcceptingOptionSpec
 *  joptsimple.NonOptionArgumentSpec
 *  joptsimple.OptionParser
 *  joptsimple.OptionSet
 *  joptsimple.OptionSpec
 *  joptsimple.OptionSpecBuilder
 *  org.apache.commons.lang3.StringEscapeUtils
 *  org.slf4j.Logger
 */
package net.minecraft.client.main;

import com.google.common.base.Stopwatch;
import com.google.common.base.Ticker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.blaze3d.TracyBootstrap;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.jtracy.TracyClient;
import com.mojang.logging.LogUtils;
import com.mojang.util.UndashedUuid;
import java.io.File;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.Optionull;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.ClientBootstrap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.main.SilentInitException;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.client.telemetry.TelemetryProperty;
import net.minecraft.client.telemetry.events.GameLoadTimesEvent;
import net.minecraft.core.UUIDUtil;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.NativeModuleLister;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.profiling.jfr.Environment;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;

public class Main {
    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @DontObfuscate
    public static void main(String[] stringArray) {
        GameConfig gameConfig;
        Object object;
        Object object2;
        Logger logger;
        Object object3;
        Object object4;
        Object object5;
        OptionParser optionParser = new OptionParser();
        optionParser.allowsUnrecognizedOptions();
        optionParser.accepts("demo");
        optionParser.accepts("disableMultiplayer");
        optionParser.accepts("disableChat");
        optionParser.accepts("fullscreen");
        optionParser.accepts("checkGlErrors");
        OptionSpecBuilder optionSpecBuilder = optionParser.accepts("renderDebugLabels");
        OptionSpecBuilder optionSpecBuilder2 = optionParser.accepts("jfrProfile");
        OptionSpecBuilder optionSpecBuilder3 = optionParser.accepts("tracy");
        OptionSpecBuilder optionSpecBuilder4 = optionParser.accepts("tracyNoImages");
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec = optionParser.accepts("quickPlayPath").withRequiredArg();
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec2 = optionParser.accepts("quickPlaySingleplayer").withOptionalArg();
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec3 = optionParser.accepts("quickPlayMultiplayer").withRequiredArg();
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec4 = optionParser.accepts("quickPlayRealms").withRequiredArg();
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec5 = optionParser.accepts("gameDir").withRequiredArg().ofType(File.class).defaultsTo((Object)new File("."), (Object[])new File[0]);
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec6 = optionParser.accepts("assetsDir").withRequiredArg().ofType(File.class);
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec7 = optionParser.accepts("resourcePackDir").withRequiredArg().ofType(File.class);
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec8 = optionParser.accepts("proxyHost").withRequiredArg();
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec9 = optionParser.accepts("proxyPort").withRequiredArg().defaultsTo((Object)"8080", (Object[])new String[0]).ofType(Integer.class);
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec10 = optionParser.accepts("proxyUser").withRequiredArg();
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec11 = optionParser.accepts("proxyPass").withRequiredArg();
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec12 = optionParser.accepts("username").withRequiredArg().defaultsTo((Object)("Player" + System.currentTimeMillis() % 1000L), (Object[])new String[0]);
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec13 = optionParser.accepts("uuid").withRequiredArg();
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec14 = optionParser.accepts("xuid").withOptionalArg().defaultsTo((Object)"", (Object[])new String[0]);
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec15 = optionParser.accepts("clientId").withOptionalArg().defaultsTo((Object)"", (Object[])new String[0]);
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec16 = optionParser.accepts("accessToken").withRequiredArg().required();
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec17 = optionParser.accepts("version").withRequiredArg().required();
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec18 = optionParser.accepts("width").withRequiredArg().ofType(Integer.class).defaultsTo((Object)854, (Object[])new Integer[0]);
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec19 = optionParser.accepts("height").withRequiredArg().ofType(Integer.class).defaultsTo((Object)480, (Object[])new Integer[0]);
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec20 = optionParser.accepts("fullscreenWidth").withRequiredArg().ofType(Integer.class);
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec21 = optionParser.accepts("fullscreenHeight").withRequiredArg().ofType(Integer.class);
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec22 = optionParser.accepts("userProperties").withRequiredArg().defaultsTo((Object)"{}", (Object[])new String[0]);
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec23 = optionParser.accepts("profileProperties").withRequiredArg().defaultsTo((Object)"{}", (Object[])new String[0]);
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec24 = optionParser.accepts("assetIndex").withRequiredArg();
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec25 = optionParser.accepts("userType").withRequiredArg().defaultsTo((Object)"legacy", (Object[])new String[0]);
        ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec26 = optionParser.accepts("versionType").withRequiredArg().defaultsTo((Object)"release", (Object[])new String[0]);
        NonOptionArgumentSpec nonOptionArgumentSpec = optionParser.nonOptions();
        OptionSet optionSet = optionParser.parse(stringArray);
        File file = (File)Main.parseArgument(optionSet, argumentAcceptingOptionSpec5);
        String string = (String)Main.parseArgument(optionSet, argumentAcceptingOptionSpec17);
        String string2 = "Pre-bootstrap";
        try {
            User.Type type;
            if (optionSet.has((OptionSpec)optionSpecBuilder2)) {
                JvmProfiler.INSTANCE.start(Environment.CLIENT);
            }
            if (optionSet.has((OptionSpec)optionSpecBuilder3)) {
                TracyBootstrap.setup();
            }
            object5 = Stopwatch.createStarted((Ticker)Ticker.systemTicker());
            object4 = Stopwatch.createStarted((Ticker)Ticker.systemTicker());
            GameLoadTimesEvent.INSTANCE.beginStep(TelemetryProperty.LOAD_TIME_TOTAL_TIME_MS, (Stopwatch)object5);
            GameLoadTimesEvent.INSTANCE.beginStep(TelemetryProperty.LOAD_TIME_PRE_WINDOW_MS, (Stopwatch)object4);
            SharedConstants.tryDetectVersion();
            TracyClient.reportAppInfo((String)("Minecraft Java Edition " + SharedConstants.getCurrentVersion().name()));
            object3 = DataFixers.optimize(DataFixTypes.TYPES_FOR_LEVEL_LIST);
            CrashReport.preload();
            logger = LogUtils.getLogger();
            string2 = "Bootstrap";
            Bootstrap.bootStrap();
            ClientBootstrap.bootstrap();
            GameLoadTimesEvent.INSTANCE.setBootstrapTime(Bootstrap.bootstrapDuration.get());
            Bootstrap.validate();
            string2 = "Argument parsing";
            object2 = optionSet.valuesOf((OptionSpec)nonOptionArgumentSpec);
            if (!object2.isEmpty()) {
                logger.info("Completely ignored arguments: {}", object2);
            }
            if ((type = User.Type.byName((String)(object = (String)argumentAcceptingOptionSpec25.value(optionSet)))) == null) {
                logger.warn("Unrecognized user type: {}", object);
            }
            String string3 = (String)Main.parseArgument(optionSet, argumentAcceptingOptionSpec8);
            Proxy proxy = Proxy.NO_PROXY;
            if (string3 != null) {
                try {
                    proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(string3, (int)((Integer)Main.parseArgument(optionSet, argumentAcceptingOptionSpec9))));
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            final String string4 = (String)Main.parseArgument(optionSet, argumentAcceptingOptionSpec10);
            final String string5 = (String)Main.parseArgument(optionSet, argumentAcceptingOptionSpec11);
            if (!proxy.equals(Proxy.NO_PROXY) && Main.stringHasValue(string4) && Main.stringHasValue(string5)) {
                Authenticator.setDefault(new Authenticator(){

                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(string4, string5.toCharArray());
                    }
                });
            }
            int n = (Integer)Main.parseArgument(optionSet, argumentAcceptingOptionSpec18);
            int n2 = (Integer)Main.parseArgument(optionSet, argumentAcceptingOptionSpec19);
            OptionalInt optionalInt = Main.ofNullable((Integer)Main.parseArgument(optionSet, argumentAcceptingOptionSpec20));
            OptionalInt optionalInt2 = Main.ofNullable((Integer)Main.parseArgument(optionSet, argumentAcceptingOptionSpec21));
            boolean bl = optionSet.has("fullscreen");
            boolean bl2 = optionSet.has("demo");
            boolean bl3 = optionSet.has("disableMultiplayer");
            boolean bl4 = optionSet.has("disableChat");
            boolean bl5 = !optionSet.has((OptionSpec)optionSpecBuilder4);
            boolean bl6 = optionSet.has((OptionSpec)optionSpecBuilder);
            Gson gson = new GsonBuilder().registerTypeAdapter(PropertyMap.class, (Object)new PropertyMap.Serializer()).create();
            PropertyMap propertyMap = GsonHelper.fromJson(gson, (String)Main.parseArgument(optionSet, argumentAcceptingOptionSpec22), PropertyMap.class);
            PropertyMap propertyMap2 = GsonHelper.fromJson(gson, (String)Main.parseArgument(optionSet, argumentAcceptingOptionSpec23), PropertyMap.class);
            String string6 = (String)Main.parseArgument(optionSet, argumentAcceptingOptionSpec26);
            File file2 = optionSet.has((OptionSpec)argumentAcceptingOptionSpec6) ? (File)Main.parseArgument(optionSet, argumentAcceptingOptionSpec6) : new File(file, "assets/");
            File file3 = optionSet.has((OptionSpec)argumentAcceptingOptionSpec7) ? (File)Main.parseArgument(optionSet, argumentAcceptingOptionSpec7) : new File(file, "resourcepacks/");
            UUID uUID = Main.hasValidUuid((OptionSpec<String>)argumentAcceptingOptionSpec13, optionSet, logger) ? UndashedUuid.fromStringLenient((String)((String)argumentAcceptingOptionSpec13.value(optionSet))) : UUIDUtil.createOfflinePlayerUUID((String)argumentAcceptingOptionSpec12.value(optionSet));
            String string7 = optionSet.has((OptionSpec)argumentAcceptingOptionSpec24) ? (String)argumentAcceptingOptionSpec24.value(optionSet) : null;
            String string8 = (String)optionSet.valueOf((OptionSpec)argumentAcceptingOptionSpec14);
            String string9 = (String)optionSet.valueOf((OptionSpec)argumentAcceptingOptionSpec15);
            String string10 = (String)Main.parseArgument(optionSet, argumentAcceptingOptionSpec);
            GameConfig.QuickPlayVariant quickPlayVariant = Main.getQuickPlayVariant(optionSet, (OptionSpec<String>)argumentAcceptingOptionSpec2, (OptionSpec<String>)argumentAcceptingOptionSpec3, (OptionSpec<String>)argumentAcceptingOptionSpec4);
            User user = new User((String)argumentAcceptingOptionSpec12.value(optionSet), uUID, (String)argumentAcceptingOptionSpec16.value(optionSet), Main.emptyStringToEmptyOptional(string8), Main.emptyStringToEmptyOptional(string9), type);
            gameConfig = new GameConfig(new GameConfig.UserData(user, propertyMap, propertyMap2, proxy), new DisplayData(n, n2, optionalInt, optionalInt2, bl), new GameConfig.FolderData(file, file3, file2, string7), new GameConfig.GameData(bl2, string, string6, bl3, bl4, bl5, bl6), new GameConfig.QuickPlayData(string10, quickPlayVariant));
            Util.startTimerHackThread();
            ((CompletableFuture)object3).join();
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, string2);
            CrashReportCategory crashReportCategory = crashReport.addCategory("Initialization");
            NativeModuleLister.addCrashSection(crashReportCategory);
            Minecraft.fillReport(null, null, string, null, crashReport);
            Minecraft.crash(null, file, crashReport);
            return;
        }
        object5 = new Thread("Client Shutdown Thread"){

            @Override
            public void run() {
                Minecraft minecraft = Minecraft.getInstance();
                if (minecraft == null) {
                    return;
                }
                IntegratedServer integratedServer = minecraft.getSingleplayerServer();
                if (integratedServer != null) {
                    integratedServer.halt(true);
                }
            }
        };
        ((Thread)object5).setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(logger));
        Runtime.getRuntime().addShutdownHook((Thread)object5);
        object4 = null;
        try {
            Thread.currentThread().setName("Render thread");
            RenderSystem.initRenderThread();
            object4 = new Minecraft(gameConfig);
        }
        catch (SilentInitException silentInitException) {
            Util.shutdownExecutors();
            logger.warn("Failed to create window: ", (Throwable)silentInitException);
            return;
        }
        catch (Throwable throwable) {
            object2 = CrashReport.forThrowable(throwable, "Initializing game");
            object = ((CrashReport)object2).addCategory("Initialization");
            NativeModuleLister.addCrashSection((CrashReportCategory)object);
            Minecraft.fillReport((Minecraft)object4, null, gameConfig.game.launchVersion, null, (CrashReport)object2);
            Minecraft.crash((Minecraft)object4, gameConfig.location.gameDirectory, (CrashReport)object2);
            return;
        }
        object3 = object4;
        ((Minecraft)object3).run();
        try {
            ((Minecraft)object3).stop();
        }
        finally {
            ((Minecraft)object3).destroy();
        }
    }

    private static GameConfig.QuickPlayVariant getQuickPlayVariant(OptionSet optionSet, OptionSpec<String> optionSpec, OptionSpec<String> optionSpec2, OptionSpec<String> optionSpec3) {
        long l = Stream.of(optionSpec, optionSpec2, optionSpec3).filter(arg_0 -> ((OptionSet)optionSet).has(arg_0)).count();
        if (l == 0L) {
            return GameConfig.QuickPlayVariant.DISABLED;
        }
        if (l > 1L) {
            throw new IllegalArgumentException("Only one quick play option can be specified");
        }
        if (optionSet.has(optionSpec)) {
            String string = Main.unescapeJavaArgument(Main.parseArgument(optionSet, optionSpec));
            return new GameConfig.QuickPlaySinglePlayerData(string);
        }
        if (optionSet.has(optionSpec2)) {
            String string = Main.unescapeJavaArgument(Main.parseArgument(optionSet, optionSpec2));
            return Optionull.mapOrDefault(string, GameConfig.QuickPlayMultiplayerData::new, GameConfig.QuickPlayVariant.DISABLED);
        }
        if (optionSet.has(optionSpec3)) {
            String string = Main.unescapeJavaArgument(Main.parseArgument(optionSet, optionSpec3));
            return Optionull.mapOrDefault(string, GameConfig.QuickPlayRealmsData::new, GameConfig.QuickPlayVariant.DISABLED);
        }
        return GameConfig.QuickPlayVariant.DISABLED;
    }

    @Nullable
    private static String unescapeJavaArgument(@Nullable String string) {
        if (string == null) {
            return null;
        }
        return StringEscapeUtils.unescapeJava((String)string);
    }

    private static Optional<String> emptyStringToEmptyOptional(String string) {
        return string.isEmpty() ? Optional.empty() : Optional.of(string);
    }

    private static OptionalInt ofNullable(@Nullable Integer n) {
        return n != null ? OptionalInt.of(n) : OptionalInt.empty();
    }

    @Nullable
    private static <T> T parseArgument(OptionSet optionSet, OptionSpec<T> optionSpec) {
        try {
            return (T)optionSet.valueOf(optionSpec);
        }
        catch (Throwable throwable) {
            ArgumentAcceptingOptionSpec argumentAcceptingOptionSpec;
            List list;
            if (optionSpec instanceof ArgumentAcceptingOptionSpec && !(list = (argumentAcceptingOptionSpec = (ArgumentAcceptingOptionSpec)optionSpec).defaultValues()).isEmpty()) {
                return (T)list.get(0);
            }
            throw throwable;
        }
    }

    private static boolean stringHasValue(@Nullable String string) {
        return string != null && !string.isEmpty();
    }

    private static boolean hasValidUuid(OptionSpec<String> optionSpec, OptionSet optionSet, Logger logger) {
        return optionSet.has(optionSpec) && Main.isUuidValid(optionSpec, optionSet, logger);
    }

    private static boolean isUuidValid(OptionSpec<String> optionSpec, OptionSet optionSet, Logger logger) {
        try {
            UndashedUuid.fromStringLenient((String)((String)optionSpec.value(optionSet)));
        }
        catch (IllegalArgumentException illegalArgumentException) {
            logger.warn("Invalid UUID: '{}", optionSpec.value(optionSet));
            return false;
        }
        return true;
    }

    static {
        System.setProperty("java.awt.headless", "true");
    }
}

