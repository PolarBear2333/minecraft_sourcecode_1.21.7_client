/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.StringUtils
 */
package net.minecraft.world.level.storage;

import java.nio.file.Path;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringUtil;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.storage.LevelVersion;
import org.apache.commons.lang3.StringUtils;

public class LevelSummary
implements Comparable<LevelSummary> {
    public static final Component PLAY_WORLD = Component.translatable("selectWorld.select");
    private final LevelSettings settings;
    private final LevelVersion levelVersion;
    private final String levelId;
    private final boolean requiresManualConversion;
    private final boolean locked;
    private final boolean experimental;
    private final Path icon;
    @Nullable
    private Component info;

    public LevelSummary(LevelSettings levelSettings, LevelVersion levelVersion, String string, boolean bl, boolean bl2, boolean bl3, Path path) {
        this.settings = levelSettings;
        this.levelVersion = levelVersion;
        this.levelId = string;
        this.locked = bl2;
        this.experimental = bl3;
        this.icon = path;
        this.requiresManualConversion = bl;
    }

    public String getLevelId() {
        return this.levelId;
    }

    public String getLevelName() {
        return StringUtils.isEmpty((CharSequence)this.settings.levelName()) ? this.levelId : this.settings.levelName();
    }

    public Path getIcon() {
        return this.icon;
    }

    public boolean requiresManualConversion() {
        return this.requiresManualConversion;
    }

    public boolean isExperimental() {
        return this.experimental;
    }

    public long getLastPlayed() {
        return this.levelVersion.lastPlayed();
    }

    @Override
    public int compareTo(LevelSummary levelSummary) {
        if (this.getLastPlayed() < levelSummary.getLastPlayed()) {
            return 1;
        }
        if (this.getLastPlayed() > levelSummary.getLastPlayed()) {
            return -1;
        }
        return this.levelId.compareTo(levelSummary.levelId);
    }

    public LevelSettings getSettings() {
        return this.settings;
    }

    public GameType getGameMode() {
        return this.settings.gameType();
    }

    public boolean isHardcore() {
        return this.settings.hardcore();
    }

    public boolean hasCommands() {
        return this.settings.allowCommands();
    }

    public MutableComponent getWorldVersionName() {
        if (StringUtil.isNullOrEmpty(this.levelVersion.minecraftVersionName())) {
            return Component.translatable("selectWorld.versionUnknown");
        }
        return Component.literal(this.levelVersion.minecraftVersionName());
    }

    public LevelVersion levelVersion() {
        return this.levelVersion;
    }

    public boolean shouldBackup() {
        return this.backupStatus().shouldBackup();
    }

    public boolean isDowngrade() {
        return this.backupStatus() == BackupStatus.DOWNGRADE;
    }

    public BackupStatus backupStatus() {
        WorldVersion worldVersion = SharedConstants.getCurrentVersion();
        int n = worldVersion.dataVersion().version();
        int n2 = this.levelVersion.minecraftVersion().version();
        if (!worldVersion.stable() && n2 < n) {
            return BackupStatus.UPGRADE_TO_SNAPSHOT;
        }
        if (n2 > n) {
            return BackupStatus.DOWNGRADE;
        }
        return BackupStatus.NONE;
    }

    public boolean isLocked() {
        return this.locked;
    }

    public boolean isDisabled() {
        if (this.isLocked() || this.requiresManualConversion()) {
            return true;
        }
        return !this.isCompatible();
    }

    public boolean isCompatible() {
        return SharedConstants.getCurrentVersion().dataVersion().isCompatible(this.levelVersion.minecraftVersion());
    }

    public Component getInfo() {
        if (this.info == null) {
            this.info = this.createInfo();
        }
        return this.info;
    }

    private Component createInfo() {
        MutableComponent mutableComponent;
        if (this.isLocked()) {
            return Component.translatable("selectWorld.locked").withStyle(ChatFormatting.RED);
        }
        if (this.requiresManualConversion()) {
            return Component.translatable("selectWorld.conversion").withStyle(ChatFormatting.RED);
        }
        if (!this.isCompatible()) {
            return Component.translatable("selectWorld.incompatible.info", this.getWorldVersionName()).withStyle(ChatFormatting.RED);
        }
        MutableComponent mutableComponent2 = mutableComponent = this.isHardcore() ? Component.empty().append(Component.translatable("gameMode.hardcore").withColor(-65536)) : Component.translatable("gameMode." + this.getGameMode().getName());
        if (this.hasCommands()) {
            mutableComponent.append(", ").append(Component.translatable("selectWorld.commands"));
        }
        if (this.isExperimental()) {
            mutableComponent.append(", ").append(Component.translatable("selectWorld.experimental").withStyle(ChatFormatting.YELLOW));
        }
        MutableComponent mutableComponent3 = this.getWorldVersionName();
        MutableComponent mutableComponent4 = Component.literal(", ").append(Component.translatable("selectWorld.version")).append(CommonComponents.SPACE);
        if (this.shouldBackup()) {
            mutableComponent4.append(mutableComponent3.withStyle(this.isDowngrade() ? ChatFormatting.RED : ChatFormatting.ITALIC));
        } else {
            mutableComponent4.append(mutableComponent3);
        }
        mutableComponent.append(mutableComponent4);
        return mutableComponent;
    }

    public Component primaryActionMessage() {
        return PLAY_WORLD;
    }

    public boolean primaryActionActive() {
        return !this.isDisabled();
    }

    public boolean canUpload() {
        return !this.requiresManualConversion() && !this.isLocked();
    }

    public boolean canEdit() {
        return !this.isDisabled();
    }

    public boolean canRecreate() {
        return !this.isDisabled();
    }

    public boolean canDelete() {
        return true;
    }

    @Override
    public /* synthetic */ int compareTo(Object object) {
        return this.compareTo((LevelSummary)object);
    }

    public static enum BackupStatus {
        NONE(false, false, ""),
        DOWNGRADE(true, true, "downgrade"),
        UPGRADE_TO_SNAPSHOT(true, false, "snapshot");

        private final boolean shouldBackup;
        private final boolean severe;
        private final String translationKey;

        private BackupStatus(boolean bl, boolean bl2, String string2) {
            this.shouldBackup = bl;
            this.severe = bl2;
            this.translationKey = string2;
        }

        public boolean shouldBackup() {
            return this.shouldBackup;
        }

        public boolean isSevere() {
            return this.severe;
        }

        public String getTranslationKey() {
            return this.translationKey;
        }
    }

    public static class CorruptedLevelSummary
    extends LevelSummary {
        private static final Component INFO = Component.translatable("recover_world.warning").withStyle(style -> style.withColor(-65536));
        private static final Component RECOVER = Component.translatable("recover_world.button");
        private final long lastPlayed;

        public CorruptedLevelSummary(String string, Path path, long l) {
            super(null, null, string, false, false, false, path);
            this.lastPlayed = l;
        }

        @Override
        public String getLevelName() {
            return this.getLevelId();
        }

        @Override
        public Component getInfo() {
            return INFO;
        }

        @Override
        public long getLastPlayed() {
            return this.lastPlayed;
        }

        @Override
        public boolean isDisabled() {
            return false;
        }

        @Override
        public Component primaryActionMessage() {
            return RECOVER;
        }

        @Override
        public boolean primaryActionActive() {
            return true;
        }

        @Override
        public boolean canUpload() {
            return false;
        }

        @Override
        public boolean canEdit() {
            return false;
        }

        @Override
        public boolean canRecreate() {
            return false;
        }

        @Override
        public /* synthetic */ int compareTo(Object object) {
            return super.compareTo((LevelSummary)object);
        }
    }

    public static class SymlinkLevelSummary
    extends LevelSummary {
        private static final Component MORE_INFO_BUTTON = Component.translatable("symlink_warning.more_info");
        private static final Component INFO = Component.translatable("symlink_warning.title").withColor(-65536);

        public SymlinkLevelSummary(String string, Path path) {
            super(null, null, string, false, false, false, path);
        }

        @Override
        public String getLevelName() {
            return this.getLevelId();
        }

        @Override
        public Component getInfo() {
            return INFO;
        }

        @Override
        public long getLastPlayed() {
            return -1L;
        }

        @Override
        public boolean isDisabled() {
            return false;
        }

        @Override
        public Component primaryActionMessage() {
            return MORE_INFO_BUTTON;
        }

        @Override
        public boolean primaryActionActive() {
            return true;
        }

        @Override
        public boolean canUpload() {
            return false;
        }

        @Override
        public boolean canEdit() {
            return false;
        }

        @Override
        public boolean canRecreate() {
            return false;
        }

        @Override
        public /* synthetic */ int compareTo(Object object) {
            return super.compareTo((LevelSummary)object);
        }
    }
}

