/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.properties.PropertyMap
 *  javax.annotation.Nullable
 */
package net.minecraft.client.main;

import com.mojang.authlib.properties.PropertyMap;
import com.mojang.blaze3d.platform.DisplayData;
import java.io.File;
import java.net.Proxy;
import java.nio.file.Path;
import javax.annotation.Nullable;
import net.minecraft.client.User;
import net.minecraft.client.resources.IndexedAssetSource;
import net.minecraft.util.StringUtil;

public class GameConfig {
    public final UserData user;
    public final DisplayData display;
    public final FolderData location;
    public final GameData game;
    public final QuickPlayData quickPlay;

    public GameConfig(UserData userData, DisplayData displayData, FolderData folderData, GameData gameData, QuickPlayData quickPlayData) {
        this.user = userData;
        this.display = displayData;
        this.location = folderData;
        this.game = gameData;
        this.quickPlay = quickPlayData;
    }

    public static class UserData {
        public final User user;
        public final PropertyMap userProperties;
        public final PropertyMap profileProperties;
        public final Proxy proxy;

        public UserData(User user, PropertyMap propertyMap, PropertyMap propertyMap2, Proxy proxy) {
            this.user = user;
            this.userProperties = propertyMap;
            this.profileProperties = propertyMap2;
            this.proxy = proxy;
        }
    }

    public static class FolderData {
        public final File gameDirectory;
        public final File resourcePackDirectory;
        public final File assetDirectory;
        @Nullable
        public final String assetIndex;

        public FolderData(File file, File file2, File file3, @Nullable String string) {
            this.gameDirectory = file;
            this.resourcePackDirectory = file2;
            this.assetDirectory = file3;
            this.assetIndex = string;
        }

        public Path getExternalAssetSource() {
            return this.assetIndex == null ? this.assetDirectory.toPath() : IndexedAssetSource.createIndexFs(this.assetDirectory.toPath(), this.assetIndex);
        }
    }

    public static class GameData {
        public final boolean demo;
        public final String launchVersion;
        public final String versionType;
        public final boolean disableMultiplayer;
        public final boolean disableChat;
        public final boolean captureTracyImages;
        public final boolean renderDebugLabels;

        public GameData(boolean bl, String string, String string2, boolean bl2, boolean bl3, boolean bl4, boolean bl5) {
            this.demo = bl;
            this.launchVersion = string;
            this.versionType = string2;
            this.disableMultiplayer = bl2;
            this.disableChat = bl3;
            this.captureTracyImages = bl4;
            this.renderDebugLabels = bl5;
        }
    }

    public record QuickPlayData(@Nullable String logPath, QuickPlayVariant variant) {
        public boolean isEnabled() {
            return this.variant.isEnabled();
        }
    }

    public record QuickPlayDisabled() implements QuickPlayVariant
    {
        @Override
        public boolean isEnabled() {
            return false;
        }
    }

    public record QuickPlayRealmsData(String realmId) implements QuickPlayVariant
    {
        @Override
        public boolean isEnabled() {
            return !StringUtil.isBlank(this.realmId);
        }
    }

    public record QuickPlayMultiplayerData(String serverAddress) implements QuickPlayVariant
    {
        @Override
        public boolean isEnabled() {
            return !StringUtil.isBlank(this.serverAddress);
        }
    }

    public record QuickPlaySinglePlayerData(@Nullable String worldId) implements QuickPlayVariant
    {
        @Override
        public boolean isEnabled() {
            return true;
        }
    }

    public static sealed interface QuickPlayVariant
    permits QuickPlaySinglePlayerData, QuickPlayMultiplayerData, QuickPlayRealmsData, QuickPlayDisabled {
        public static final QuickPlayVariant DISABLED = new QuickPlayDisabled();

        public boolean isEnabled();
    }
}

