/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.StringUtils
 *  org.slf4j.Logger
 */
package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.ErrorScreen;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.gui.screens.GenericMessageScreen;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.client.gui.screens.NoticeWithLinkScreen;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.EditWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.nbt.NbtException;
import net.minecraft.nbt.ReportedNbtException;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageException;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.validation.ContentValidationException;
import net.minecraft.world.level.validation.ForbiddenSymlinkInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

public class WorldSelectionList
extends ObjectSelectionList<Entry> {
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withZone(ZoneId.systemDefault());
    static final ResourceLocation ERROR_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("world_list/error_highlighted");
    static final ResourceLocation ERROR_SPRITE = ResourceLocation.withDefaultNamespace("world_list/error");
    static final ResourceLocation MARKED_JOIN_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("world_list/marked_join_highlighted");
    static final ResourceLocation MARKED_JOIN_SPRITE = ResourceLocation.withDefaultNamespace("world_list/marked_join");
    static final ResourceLocation WARNING_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("world_list/warning_highlighted");
    static final ResourceLocation WARNING_SPRITE = ResourceLocation.withDefaultNamespace("world_list/warning");
    static final ResourceLocation JOIN_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("world_list/join_highlighted");
    static final ResourceLocation JOIN_SPRITE = ResourceLocation.withDefaultNamespace("world_list/join");
    static final Logger LOGGER = LogUtils.getLogger();
    static final Component FROM_NEWER_TOOLTIP_1 = Component.translatable("selectWorld.tooltip.fromNewerVersion1").withStyle(ChatFormatting.RED);
    static final Component FROM_NEWER_TOOLTIP_2 = Component.translatable("selectWorld.tooltip.fromNewerVersion2").withStyle(ChatFormatting.RED);
    static final Component SNAPSHOT_TOOLTIP_1 = Component.translatable("selectWorld.tooltip.snapshot1").withStyle(ChatFormatting.GOLD);
    static final Component SNAPSHOT_TOOLTIP_2 = Component.translatable("selectWorld.tooltip.snapshot2").withStyle(ChatFormatting.GOLD);
    static final Component WORLD_LOCKED_TOOLTIP = Component.translatable("selectWorld.locked").withStyle(ChatFormatting.RED);
    static final Component WORLD_REQUIRES_CONVERSION = Component.translatable("selectWorld.conversion.tooltip").withStyle(ChatFormatting.RED);
    static final Component INCOMPATIBLE_VERSION_TOOLTIP = Component.translatable("selectWorld.incompatible.tooltip").withStyle(ChatFormatting.RED);
    static final Component WORLD_EXPERIMENTAL = Component.translatable("selectWorld.experimental");
    private final SelectWorldScreen screen;
    private CompletableFuture<List<LevelSummary>> pendingLevels;
    @Nullable
    private List<LevelSummary> currentlyDisplayedLevels;
    private String filter;
    private final LoadingHeader loadingHeader;

    public WorldSelectionList(SelectWorldScreen selectWorldScreen, Minecraft minecraft, int n, int n2, int n3, int n4, String string, @Nullable WorldSelectionList worldSelectionList) {
        super(minecraft, n, n2, n3, n4);
        this.screen = selectWorldScreen;
        this.loadingHeader = new LoadingHeader(minecraft);
        this.filter = string;
        this.pendingLevels = worldSelectionList != null ? worldSelectionList.pendingLevels : this.loadLevels();
        this.handleNewLevels(this.pollLevelsIgnoreErrors());
    }

    @Override
    protected void clearEntries() {
        this.children().forEach(Entry::close);
        super.clearEntries();
    }

    @Nullable
    private List<LevelSummary> pollLevelsIgnoreErrors() {
        try {
            return this.pendingLevels.getNow(null);
        }
        catch (CancellationException | CompletionException runtimeException) {
            return null;
        }
    }

    void reloadWorldList() {
        this.pendingLevels = this.loadLevels();
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        Optional<WorldListEntry> optional;
        if (CommonInputs.selected(n) && (optional = this.getSelectedOpt()).isPresent()) {
            if (optional.get().canJoin()) {
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                optional.get().joinWorld();
            }
            return true;
        }
        return super.keyPressed(n, n2, n3);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int n, int n2, float f) {
        List<LevelSummary> list = this.pollLevelsIgnoreErrors();
        if (list != this.currentlyDisplayedLevels) {
            this.handleNewLevels(list);
        }
        super.renderWidget(guiGraphics, n, n2, f);
    }

    private void handleNewLevels(@Nullable List<LevelSummary> list) {
        if (list == null) {
            this.fillLoadingLevels();
        } else {
            this.fillLevels(this.filter, list);
        }
        this.currentlyDisplayedLevels = list;
    }

    public void updateFilter(String string) {
        if (this.currentlyDisplayedLevels != null && !string.equals(this.filter)) {
            this.fillLevels(string, this.currentlyDisplayedLevels);
        }
        this.filter = string;
    }

    private CompletableFuture<List<LevelSummary>> loadLevels() {
        LevelStorageSource.LevelCandidates levelCandidates;
        try {
            levelCandidates = this.minecraft.getLevelSource().findLevelCandidates();
        }
        catch (LevelStorageException levelStorageException) {
            LOGGER.error("Couldn't load level list", (Throwable)levelStorageException);
            this.handleLevelLoadFailure(levelStorageException.getMessageComponent());
            return CompletableFuture.completedFuture(List.of());
        }
        if (levelCandidates.isEmpty()) {
            CreateWorldScreen.openFresh(this.minecraft, null);
            return CompletableFuture.completedFuture(List.of());
        }
        return this.minecraft.getLevelSource().loadLevelSummaries(levelCandidates).exceptionally(throwable -> {
            this.minecraft.delayCrash(CrashReport.forThrowable(throwable, "Couldn't load level list"));
            return List.of();
        });
    }

    private void fillLevels(String string, List<LevelSummary> list) {
        this.clearEntries();
        string = string.toLowerCase(Locale.ROOT);
        for (LevelSummary levelSummary : list) {
            if (!this.filterAccepts(string, levelSummary)) continue;
            this.addEntry(new WorldListEntry(this, levelSummary));
        }
        this.notifyListUpdated();
    }

    private boolean filterAccepts(String string, LevelSummary levelSummary) {
        return levelSummary.getLevelName().toLowerCase(Locale.ROOT).contains(string) || levelSummary.getLevelId().toLowerCase(Locale.ROOT).contains(string);
    }

    private void fillLoadingLevels() {
        this.clearEntries();
        this.addEntry(this.loadingHeader);
        this.notifyListUpdated();
    }

    private void notifyListUpdated() {
        this.refreshScrollAmount();
        this.screen.triggerImmediateNarration(true);
    }

    private void handleLevelLoadFailure(Component component) {
        this.minecraft.setScreen(new ErrorScreen(Component.translatable("selectWorld.unable_to_load"), component));
    }

    @Override
    public int getRowWidth() {
        return 270;
    }

    @Override
    public void setSelected(@Nullable Entry entry) {
        LevelSummary levelSummary;
        super.setSelected(entry);
        if (entry instanceof WorldListEntry) {
            WorldListEntry worldListEntry = (WorldListEntry)entry;
            levelSummary = worldListEntry.summary;
        } else {
            levelSummary = null;
        }
        this.screen.updateButtonStatus(levelSummary);
    }

    public Optional<WorldListEntry> getSelectedOpt() {
        Entry entry = (Entry)this.getSelected();
        if (entry instanceof WorldListEntry) {
            WorldListEntry worldListEntry = (WorldListEntry)entry;
            return Optional.of(worldListEntry);
        }
        return Optional.empty();
    }

    public SelectWorldScreen getScreen() {
        return this.screen;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        if (this.children().contains(this.loadingHeader)) {
            this.loadingHeader.updateNarration(narrationElementOutput);
            return;
        }
        super.updateWidgetNarration(narrationElementOutput);
    }

    public static class LoadingHeader
    extends Entry {
        private static final Component LOADING_LABEL = Component.translatable("selectWorld.loading_list");
        private final Minecraft minecraft;

        public LoadingHeader(Minecraft minecraft) {
            this.minecraft = minecraft;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
            int n8 = (this.minecraft.screen.width - this.minecraft.font.width(LOADING_LABEL)) / 2;
            int n9 = n2 + (n5 - this.minecraft.font.lineHeight) / 2;
            guiGraphics.drawString(this.minecraft.font, LOADING_LABEL, n8, n9, -1);
            String string = LoadingDotsText.get(Util.getMillis());
            int n10 = (this.minecraft.screen.width - this.minecraft.font.width(string)) / 2;
            int n11 = n9 + this.minecraft.font.lineHeight;
            guiGraphics.drawString(this.minecraft.font, string, n10, n11, -8355712);
        }

        @Override
        public Component getNarration() {
            return LOADING_LABEL;
        }
    }

    public final class WorldListEntry
    extends Entry {
        private static final int ICON_WIDTH = 32;
        private static final int ICON_HEIGHT = 32;
        private final Minecraft minecraft;
        private final SelectWorldScreen screen;
        final LevelSummary summary;
        private final FaviconTexture icon;
        @Nullable
        private Path iconFile;
        private long lastClickTime;

        public WorldListEntry(WorldSelectionList worldSelectionList2, LevelSummary levelSummary) {
            this.minecraft = worldSelectionList2.minecraft;
            this.screen = worldSelectionList2.getScreen();
            this.summary = levelSummary;
            this.icon = FaviconTexture.forWorld(this.minecraft.getTextureManager(), levelSummary.getLevelId());
            this.iconFile = levelSummary.getIcon();
            this.validateIconFile();
            this.loadIcon();
        }

        private void validateIconFile() {
            if (this.iconFile == null) {
                return;
            }
            try {
                BasicFileAttributes basicFileAttributes = Files.readAttributes(this.iconFile, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
                if (basicFileAttributes.isSymbolicLink()) {
                    List<ForbiddenSymlinkInfo> list = this.minecraft.directoryValidator().validateSymlink(this.iconFile);
                    if (!list.isEmpty()) {
                        LOGGER.warn("{}", (Object)ContentValidationException.getMessage(this.iconFile, list));
                        this.iconFile = null;
                    } else {
                        basicFileAttributes = Files.readAttributes(this.iconFile, BasicFileAttributes.class, new LinkOption[0]);
                    }
                }
                if (!basicFileAttributes.isRegularFile()) {
                    this.iconFile = null;
                }
            }
            catch (NoSuchFileException noSuchFileException) {
                this.iconFile = null;
            }
            catch (IOException iOException) {
                LOGGER.error("could not validate symlink", (Throwable)iOException);
                this.iconFile = null;
            }
        }

        @Override
        public Component getNarration() {
            MutableComponent mutableComponent = Component.translatable("narrator.select.world_info", this.summary.getLevelName(), Component.translationArg(new Date(this.summary.getLastPlayed())), this.summary.getInfo());
            if (this.summary.isLocked()) {
                mutableComponent = CommonComponents.joinForNarration(mutableComponent, WORLD_LOCKED_TOOLTIP);
            }
            if (this.summary.isExperimental()) {
                mutableComponent = CommonComponents.joinForNarration(mutableComponent, WORLD_EXPERIMENTAL);
            }
            return Component.translatable("narrator.select", mutableComponent);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
            Object object = this.summary.getLevelName();
            Object object2 = this.summary.getLevelId();
            long l = this.summary.getLastPlayed();
            if (l != -1L) {
                object2 = (String)object2 + " (" + DATE_FORMAT.format(Instant.ofEpochMilli(l)) + ")";
            }
            if (StringUtils.isEmpty((CharSequence)object)) {
                object = I18n.get("selectWorld.world", new Object[0]) + " " + (n + 1);
            }
            Component component = this.summary.getInfo();
            guiGraphics.drawString(this.minecraft.font, (String)object, n3 + 32 + 3, n2 + 1, -1);
            guiGraphics.drawString(this.minecraft.font, (String)object2, n3 + 32 + 3, n2 + this.minecraft.font.lineHeight + 3, -8355712);
            guiGraphics.drawString(this.minecraft.font, component, n3 + 32 + 3, n2 + this.minecraft.font.lineHeight + this.minecraft.font.lineHeight + 3, -8355712);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, this.icon.textureLocation(), n3, n2, 0.0f, 0.0f, 32, 32, 32, 32);
            if (this.minecraft.options.touchscreen().get().booleanValue() || bl) {
                ResourceLocation resourceLocation;
                guiGraphics.fill(n3, n2, n3 + 32, n2 + 32, -1601138544);
                int n8 = n6 - n3;
                boolean bl2 = n8 < 32;
                ResourceLocation resourceLocation2 = bl2 ? JOIN_HIGHLIGHTED_SPRITE : JOIN_SPRITE;
                ResourceLocation resourceLocation3 = bl2 ? WARNING_HIGHLIGHTED_SPRITE : WARNING_SPRITE;
                ResourceLocation resourceLocation4 = bl2 ? ERROR_HIGHLIGHTED_SPRITE : ERROR_SPRITE;
                ResourceLocation resourceLocation5 = resourceLocation = bl2 ? MARKED_JOIN_HIGHLIGHTED_SPRITE : MARKED_JOIN_SPRITE;
                if (this.summary instanceof LevelSummary.SymlinkLevelSummary || this.summary instanceof LevelSummary.CorruptedLevelSummary) {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation4, n3, n2, 32, 32);
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation, n3, n2, 32, 32);
                    return;
                }
                if (this.summary.isLocked()) {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation4, n3, n2, 32, 32);
                    if (bl2) {
                        guiGraphics.setTooltipForNextFrame(this.minecraft.font.split(WORLD_LOCKED_TOOLTIP, 175), n6, n7);
                    }
                } else if (this.summary.requiresManualConversion()) {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation4, n3, n2, 32, 32);
                    if (bl2) {
                        guiGraphics.setTooltipForNextFrame(this.minecraft.font.split(WORLD_REQUIRES_CONVERSION, 175), n6, n7);
                    }
                } else if (!this.summary.isCompatible()) {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation4, n3, n2, 32, 32);
                    if (bl2) {
                        guiGraphics.setTooltipForNextFrame(this.minecraft.font.split(INCOMPATIBLE_VERSION_TOOLTIP, 175), n6, n7);
                    }
                } else if (this.summary.shouldBackup()) {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation, n3, n2, 32, 32);
                    if (this.summary.isDowngrade()) {
                        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation4, n3, n2, 32, 32);
                        if (bl2) {
                            guiGraphics.setTooltipForNextFrame((List<FormattedCharSequence>)ImmutableList.of((Object)FROM_NEWER_TOOLTIP_1.getVisualOrderText(), (Object)FROM_NEWER_TOOLTIP_2.getVisualOrderText()), n6, n7);
                        }
                    } else if (!SharedConstants.getCurrentVersion().stable()) {
                        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation3, n3, n2, 32, 32);
                        if (bl2) {
                            guiGraphics.setTooltipForNextFrame((List<FormattedCharSequence>)ImmutableList.of((Object)SNAPSHOT_TOOLTIP_1.getVisualOrderText(), (Object)SNAPSHOT_TOOLTIP_2.getVisualOrderText()), n6, n7);
                        }
                    }
                } else {
                    guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation2, n3, n2, 32, 32);
                }
            }
        }

        @Override
        public boolean mouseClicked(double d, double d2, int n) {
            if (!this.summary.primaryActionActive()) {
                return true;
            }
            WorldSelectionList.this.setSelected(this);
            if (d - (double)WorldSelectionList.this.getRowLeft() <= 32.0 || Util.getMillis() - this.lastClickTime < 250L) {
                if (this.canJoin()) {
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                    this.joinWorld();
                }
                return true;
            }
            this.lastClickTime = Util.getMillis();
            return super.mouseClicked(d, d2, n);
        }

        public boolean canJoin() {
            return this.summary.primaryActionActive();
        }

        public void joinWorld() {
            if (!this.summary.primaryActionActive()) {
                return;
            }
            if (this.summary instanceof LevelSummary.SymlinkLevelSummary) {
                this.minecraft.setScreen(NoticeWithLinkScreen.createWorldSymlinkWarningScreen(() -> this.minecraft.setScreen(this.screen)));
                return;
            }
            this.minecraft.createWorldOpenFlows().openWorld(this.summary.getLevelId(), () -> {
                WorldSelectionList.this.reloadWorldList();
                this.minecraft.setScreen(this.screen);
            });
        }

        public void deleteWorld() {
            this.minecraft.setScreen(new ConfirmScreen(bl -> {
                if (bl) {
                    this.minecraft.setScreen(new ProgressScreen(true));
                    this.doDeleteWorld();
                }
                this.minecraft.setScreen(this.screen);
            }, Component.translatable("selectWorld.deleteQuestion"), Component.translatable("selectWorld.deleteWarning", this.summary.getLevelName()), Component.translatable("selectWorld.deleteButton"), CommonComponents.GUI_CANCEL));
        }

        public void doDeleteWorld() {
            LevelStorageSource levelStorageSource = this.minecraft.getLevelSource();
            String string = this.summary.getLevelId();
            try (LevelStorageSource.LevelStorageAccess levelStorageAccess = levelStorageSource.createAccess(string);){
                levelStorageAccess.deleteLevel();
            }
            catch (IOException iOException) {
                SystemToast.onWorldDeleteFailure(this.minecraft, string);
                LOGGER.error("Failed to delete world {}", (Object)string, (Object)iOException);
            }
            WorldSelectionList.this.reloadWorldList();
        }

        public void editWorld() {
            EditWorldScreen editWorldScreen;
            LevelStorageSource.LevelStorageAccess levelStorageAccess;
            this.queueLoadScreen();
            String string = this.summary.getLevelId();
            try {
                levelStorageAccess = this.minecraft.getLevelSource().validateAndCreateAccess(string);
            }
            catch (IOException iOException) {
                SystemToast.onWorldAccessFailure(this.minecraft, string);
                LOGGER.error("Failed to access level {}", (Object)string, (Object)iOException);
                WorldSelectionList.this.reloadWorldList();
                return;
            }
            catch (ContentValidationException contentValidationException) {
                LOGGER.warn("{}", (Object)contentValidationException.getMessage());
                this.minecraft.setScreen(NoticeWithLinkScreen.createWorldSymlinkWarningScreen(() -> this.minecraft.setScreen(this.screen)));
                return;
            }
            try {
                editWorldScreen = EditWorldScreen.create(this.minecraft, levelStorageAccess, bl -> {
                    levelStorageAccess.safeClose();
                    if (bl) {
                        WorldSelectionList.this.reloadWorldList();
                    }
                    this.minecraft.setScreen(this.screen);
                });
            }
            catch (IOException | NbtException | ReportedNbtException exception) {
                levelStorageAccess.safeClose();
                SystemToast.onWorldAccessFailure(this.minecraft, string);
                LOGGER.error("Failed to load world data {}", (Object)string, (Object)exception);
                WorldSelectionList.this.reloadWorldList();
                return;
            }
            this.minecraft.setScreen(editWorldScreen);
        }

        public void recreateWorld() {
            this.queueLoadScreen();
            try (LevelStorageSource.LevelStorageAccess levelStorageAccess = this.minecraft.getLevelSource().validateAndCreateAccess(this.summary.getLevelId());){
                Pair<LevelSettings, WorldCreationContext> pair = this.minecraft.createWorldOpenFlows().recreateWorldData(levelStorageAccess);
                LevelSettings levelSettings = (LevelSettings)pair.getFirst();
                WorldCreationContext worldCreationContext = (WorldCreationContext)pair.getSecond();
                Path path = CreateWorldScreen.createTempDataPackDirFromExistingWorld(levelStorageAccess.getLevelPath(LevelResource.DATAPACK_DIR), this.minecraft);
                worldCreationContext.validate();
                if (worldCreationContext.options().isOldCustomizedWorld()) {
                    this.minecraft.setScreen(new ConfirmScreen(bl -> this.minecraft.setScreen(bl ? CreateWorldScreen.createFromExisting(this.minecraft, this.screen, levelSettings, worldCreationContext, path) : this.screen), Component.translatable("selectWorld.recreate.customized.title"), Component.translatable("selectWorld.recreate.customized.text"), CommonComponents.GUI_PROCEED, CommonComponents.GUI_CANCEL));
                } else {
                    this.minecraft.setScreen(CreateWorldScreen.createFromExisting(this.minecraft, this.screen, levelSettings, worldCreationContext, path));
                }
            }
            catch (ContentValidationException contentValidationException) {
                LOGGER.warn("{}", (Object)contentValidationException.getMessage());
                this.minecraft.setScreen(NoticeWithLinkScreen.createWorldSymlinkWarningScreen(() -> this.minecraft.setScreen(this.screen)));
            }
            catch (Exception exception) {
                LOGGER.error("Unable to recreate world", (Throwable)exception);
                this.minecraft.setScreen(new AlertScreen(() -> this.minecraft.setScreen(this.screen), Component.translatable("selectWorld.recreate.error.title"), Component.translatable("selectWorld.recreate.error.text")));
            }
        }

        private void queueLoadScreen() {
            this.minecraft.forceSetScreen(new GenericMessageScreen(Component.translatable("selectWorld.data_read")));
        }

        private void loadIcon() {
            boolean bl;
            boolean bl2 = bl = this.iconFile != null && Files.isRegularFile(this.iconFile, new LinkOption[0]);
            if (bl) {
                try (InputStream inputStream = Files.newInputStream(this.iconFile, new OpenOption[0]);){
                    this.icon.upload(NativeImage.read(inputStream));
                }
                catch (Throwable throwable) {
                    LOGGER.error("Invalid icon for world {}", (Object)this.summary.getLevelId(), (Object)throwable);
                    this.iconFile = null;
                }
            } else {
                this.icon.clear();
            }
        }

        @Override
        public void close() {
            this.icon.close();
        }

        public String getLevelName() {
            return this.summary.getLevelName();
        }
    }

    public static abstract class Entry
    extends ObjectSelectionList.Entry<Entry>
    implements AutoCloseable {
        @Override
        public void close() {
        }
    }
}

