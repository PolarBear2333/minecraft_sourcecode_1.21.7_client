/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.google.common.hash.Hashing
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.mutable.MutableBoolean
 *  org.slf4j.Logger
 */
package net.minecraft.client.gui.screens.packs;

import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.NoticeWithLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.gui.screens.packs.TransferableSelectionList;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackDetector;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.level.validation.ForbiddenSymlinkInfo;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.slf4j.Logger;

public class PackSelectionScreen
extends Screen {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final Component AVAILABLE_TITLE = Component.translatable("pack.available.title");
    private static final Component SELECTED_TITLE = Component.translatable("pack.selected.title");
    private static final Component OPEN_PACK_FOLDER_TITLE = Component.translatable("pack.openFolder");
    private static final int LIST_WIDTH = 200;
    private static final Component DRAG_AND_DROP = Component.translatable("pack.dropInfo").withStyle(ChatFormatting.GRAY);
    private static final Component DIRECTORY_BUTTON_TOOLTIP = Component.translatable("pack.folderInfo");
    private static final int RELOAD_COOLDOWN = 20;
    private static final ResourceLocation DEFAULT_ICON = ResourceLocation.withDefaultNamespace("textures/misc/unknown_pack.png");
    private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final PackSelectionModel model;
    @Nullable
    private Watcher watcher;
    private long ticksToReload;
    private TransferableSelectionList availablePackList;
    private TransferableSelectionList selectedPackList;
    private final Path packDir;
    private Button doneButton;
    private final Map<String, ResourceLocation> packIcons = Maps.newHashMap();

    public PackSelectionScreen(PackRepository packRepository, Consumer<PackRepository> consumer, Path path, Component component) {
        super(component);
        this.model = new PackSelectionModel(this::populateLists, this::getPackIcon, packRepository, consumer);
        this.packDir = path;
        this.watcher = Watcher.create(path);
    }

    @Override
    public void onClose() {
        this.model.commit();
        this.closeWatcher();
    }

    private void closeWatcher() {
        if (this.watcher != null) {
            try {
                this.watcher.close();
                this.watcher = null;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
    }

    @Override
    protected void init() {
        LinearLayout linearLayout = this.layout.addToHeader(LinearLayout.vertical().spacing(5));
        linearLayout.defaultCellSetting().alignHorizontallyCenter();
        linearLayout.addChild(new StringWidget(this.getTitle(), this.font));
        linearLayout.addChild(new StringWidget(DRAG_AND_DROP, this.font));
        this.availablePackList = this.addRenderableWidget(new TransferableSelectionList(this.minecraft, this, 200, this.height - 66, AVAILABLE_TITLE));
        this.selectedPackList = this.addRenderableWidget(new TransferableSelectionList(this.minecraft, this, 200, this.height - 66, SELECTED_TITLE));
        LinearLayout linearLayout2 = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        linearLayout2.addChild(Button.builder(OPEN_PACK_FOLDER_TITLE, button -> Util.getPlatform().openPath(this.packDir)).tooltip(Tooltip.create(DIRECTORY_BUTTON_TOOLTIP)).build());
        this.doneButton = linearLayout2.addChild(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).build());
        this.reload();
        this.layout.visitWidgets(guiEventListener -> {
            AbstractWidget cfr_ignored_0 = (AbstractWidget)this.addRenderableWidget(guiEventListener);
        });
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        this.availablePackList.updateSize(200, this.layout);
        this.availablePackList.setX(this.width / 2 - 15 - 200);
        this.selectedPackList.updateSize(200, this.layout);
        this.selectedPackList.setX(this.width / 2 + 15);
    }

    @Override
    public void tick() {
        if (this.watcher != null) {
            try {
                if (this.watcher.pollForChanges()) {
                    this.ticksToReload = 20L;
                }
            }
            catch (IOException iOException) {
                LOGGER.warn("Failed to poll for directory {} changes, stopping", (Object)this.packDir);
                this.closeWatcher();
            }
        }
        if (this.ticksToReload > 0L && --this.ticksToReload == 0L) {
            this.reload();
        }
    }

    private void populateLists() {
        this.updateList(this.selectedPackList, this.model.getSelected());
        this.updateList(this.availablePackList, this.model.getUnselected());
        this.doneButton.active = !this.selectedPackList.children().isEmpty();
    }

    private void updateList(TransferableSelectionList transferableSelectionList, Stream<PackSelectionModel.Entry> stream) {
        transferableSelectionList.children().clear();
        TransferableSelectionList.PackEntry packEntry = (TransferableSelectionList.PackEntry)transferableSelectionList.getSelected();
        String string = packEntry == null ? "" : packEntry.getPackId();
        transferableSelectionList.setSelected(null);
        stream.forEach(entry -> {
            TransferableSelectionList.PackEntry packEntry = new TransferableSelectionList.PackEntry(this.minecraft, transferableSelectionList, (PackSelectionModel.Entry)entry);
            transferableSelectionList.children().add(packEntry);
            if (entry.getId().equals(string)) {
                transferableSelectionList.setSelected(packEntry);
            }
        });
    }

    public void updateFocus(TransferableSelectionList transferableSelectionList) {
        TransferableSelectionList transferableSelectionList2 = this.selectedPackList == transferableSelectionList ? this.availablePackList : this.selectedPackList;
        this.changeFocus(ComponentPath.path(transferableSelectionList2.getFirstElement(), new ContainerEventHandler[]{transferableSelectionList2, this}));
    }

    public void clearSelected() {
        this.selectedPackList.setSelected(null);
        this.availablePackList.setSelected(null);
    }

    private void reload() {
        this.model.findNewPacks();
        this.populateLists();
        this.ticksToReload = 0L;
        this.packIcons.clear();
    }

    protected static void copyPacks(Minecraft minecraft, List<Path> list, Path path) {
        MutableBoolean mutableBoolean = new MutableBoolean();
        list.forEach(path2 -> {
            try (Stream<Path> stream = Files.walk(path2, new FileVisitOption[0]);){
                stream.forEach(path3 -> {
                    try {
                        Util.copyBetweenDirs(path2.getParent(), path, path3);
                    }
                    catch (IOException iOException) {
                        LOGGER.warn("Failed to copy datapack file  from {} to {}", new Object[]{path3, path, iOException});
                        mutableBoolean.setTrue();
                    }
                });
            }
            catch (IOException iOException) {
                LOGGER.warn("Failed to copy datapack file from {} to {}", path2, (Object)path);
                mutableBoolean.setTrue();
            }
        });
        if (mutableBoolean.isTrue()) {
            SystemToast.onPackCopyFailure(minecraft, path.toString());
        }
    }

    @Override
    public void onFilesDrop(List<Path> list) {
        String string = PackSelectionScreen.extractPackNames(list).collect(Collectors.joining(", "));
        this.minecraft.setScreen(new ConfirmScreen(bl -> {
            if (bl) {
                ArrayList<Path> arrayList = new ArrayList<Path>(list.size());
                HashSet<Path> hashSet = new HashSet<Path>(list);
                PackDetector<Path> packDetector = new PackDetector<Path>(this, this.minecraft.directoryValidator()){

                    @Override
                    protected Path createZipPack(Path path) {
                        return path;
                    }

                    @Override
                    protected Path createDirectoryPack(Path path) {
                        return path;
                    }

                    @Override
                    protected /* synthetic */ Object createDirectoryPack(Path path) throws IOException {
                        return this.createDirectoryPack(path);
                    }

                    @Override
                    protected /* synthetic */ Object createZipPack(Path path) throws IOException {
                        return this.createZipPack(path);
                    }
                };
                ArrayList<ForbiddenSymlinkInfo> arrayList2 = new ArrayList<ForbiddenSymlinkInfo>();
                for (Path path : list) {
                    try {
                        Path path2 = (Path)packDetector.detectPackResources(path, arrayList2);
                        if (path2 == null) {
                            LOGGER.warn("Path {} does not seem like pack", (Object)path);
                            continue;
                        }
                        arrayList.add(path2);
                        hashSet.remove(path2);
                    }
                    catch (IOException iOException) {
                        LOGGER.warn("Failed to check {} for packs", (Object)path, (Object)iOException);
                    }
                }
                if (!arrayList2.isEmpty()) {
                    this.minecraft.setScreen(NoticeWithLinkScreen.createPackSymlinkWarningScreen(() -> this.minecraft.setScreen(this)));
                    return;
                }
                if (!arrayList.isEmpty()) {
                    PackSelectionScreen.copyPacks(this.minecraft, arrayList, this.packDir);
                    this.reload();
                }
                if (!hashSet.isEmpty()) {
                    String string = PackSelectionScreen.extractPackNames(hashSet).collect(Collectors.joining(", "));
                    this.minecraft.setScreen(new AlertScreen(() -> this.minecraft.setScreen(this), Component.translatable("pack.dropRejected.title"), Component.translatable("pack.dropRejected.message", string)));
                    return;
                }
            }
            this.minecraft.setScreen(this);
        }, Component.translatable("pack.dropConfirm"), Component.literal(string)));
    }

    private static Stream<String> extractPackNames(Collection<Path> collection) {
        return collection.stream().map(Path::getFileName).map(Path::toString);
    }

    /*
     * Enabled aggressive exception aggregation
     */
    private ResourceLocation loadPackIcon(TextureManager textureManager, Pack pack) {
        try (PackResources packResources = pack.open();){
            ResourceLocation resourceLocation;
            block16: {
                IoSupplier<InputStream> ioSupplier = packResources.getRootResource("pack.png");
                if (ioSupplier == null) {
                    ResourceLocation resourceLocation2 = DEFAULT_ICON;
                    return resourceLocation2;
                }
                String string = pack.getId();
                ResourceLocation resourceLocation3 = ResourceLocation.withDefaultNamespace("pack/" + Util.sanitizeName(string, ResourceLocation::validPathChar) + "/" + String.valueOf(Hashing.sha1().hashUnencodedChars((CharSequence)string)) + "/icon");
                InputStream inputStream = ioSupplier.get();
                try {
                    NativeImage nativeImage = NativeImage.read(inputStream);
                    textureManager.register(resourceLocation3, new DynamicTexture(resourceLocation3::toString, nativeImage));
                    resourceLocation = resourceLocation3;
                    if (inputStream == null) break block16;
                }
                catch (Throwable throwable) {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                inputStream.close();
            }
            return resourceLocation;
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to load icon from pack {}", (Object)pack.getId(), (Object)exception);
            return DEFAULT_ICON;
        }
    }

    private ResourceLocation getPackIcon(Pack pack) {
        return this.packIcons.computeIfAbsent(pack.getId(), string -> this.loadPackIcon(this.minecraft.getTextureManager(), pack));
    }

    static class Watcher
    implements AutoCloseable {
        private final WatchService watcher;
        private final Path packPath;

        public Watcher(Path path) throws IOException {
            this.packPath = path;
            this.watcher = path.getFileSystem().newWatchService();
            try {
                this.watchDir(path);
                try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path);){
                    for (Path path2 : directoryStream) {
                        if (!Files.isDirectory(path2, LinkOption.NOFOLLOW_LINKS)) continue;
                        this.watchDir(path2);
                    }
                }
            }
            catch (Exception exception) {
                this.watcher.close();
                throw exception;
            }
        }

        @Nullable
        public static Watcher create(Path path) {
            try {
                return new Watcher(path);
            }
            catch (IOException iOException) {
                LOGGER.warn("Failed to initialize pack directory {} monitoring", (Object)path, (Object)iOException);
                return null;
            }
        }

        private void watchDir(Path path) throws IOException {
            path.register(this.watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
        }

        public boolean pollForChanges() throws IOException {
            WatchKey watchKey;
            boolean bl = false;
            while ((watchKey = this.watcher.poll()) != null) {
                List<WatchEvent<?>> list = watchKey.pollEvents();
                for (WatchEvent<?> watchEvent : list) {
                    Path path;
                    bl = true;
                    if (watchKey.watchable() != this.packPath || watchEvent.kind() != StandardWatchEventKinds.ENTRY_CREATE || !Files.isDirectory(path = this.packPath.resolve((Path)watchEvent.context()), LinkOption.NOFOLLOW_LINKS)) continue;
                    this.watchDir(path);
                }
                watchKey.reset();
            }
            return bl;
        }

        @Override
        public void close() throws IOException {
            this.watcher.close();
        }
    }
}

