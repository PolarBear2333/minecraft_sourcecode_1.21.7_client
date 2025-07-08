/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.hash.HashCode
 *  javax.annotation.Nullable
 */
package net.minecraft.client.resources.server;

import com.google.common.hash.HashCode;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.resources.server.PackDownloader;
import net.minecraft.client.resources.server.PackLoadFeedback;
import net.minecraft.client.resources.server.PackReloadConfig;
import net.minecraft.server.packs.DownloadQueue;

public class ServerPackManager {
    private final PackDownloader downloader;
    final PackLoadFeedback packLoadFeedback;
    private final PackReloadConfig reloadConfig;
    private final Runnable updateRequest;
    private PackPromptStatus packPromptStatus;
    final List<ServerPackData> packs = new ArrayList<ServerPackData>();

    public ServerPackManager(PackDownloader packDownloader, PackLoadFeedback packLoadFeedback, PackReloadConfig packReloadConfig, Runnable runnable, PackPromptStatus packPromptStatus) {
        this.downloader = packDownloader;
        this.packLoadFeedback = packLoadFeedback;
        this.reloadConfig = packReloadConfig;
        this.updateRequest = runnable;
        this.packPromptStatus = packPromptStatus;
    }

    void registerForUpdate() {
        this.updateRequest.run();
    }

    private void markExistingPacksAsRemoved(UUID uUID) {
        for (ServerPackData serverPackData : this.packs) {
            if (!serverPackData.id.equals(uUID)) continue;
            serverPackData.setRemovalReasonIfNotSet(RemovalReason.SERVER_REPLACED);
        }
    }

    public void pushPack(UUID uUID, URL uRL, @Nullable HashCode hashCode) {
        if (this.packPromptStatus == PackPromptStatus.DECLINED) {
            this.packLoadFeedback.reportFinalResult(uUID, PackLoadFeedback.FinalResult.DECLINED);
            return;
        }
        this.pushNewPack(uUID, new ServerPackData(uUID, uRL, hashCode));
    }

    public void pushLocalPack(UUID uUID, Path path) {
        URL uRL;
        if (this.packPromptStatus == PackPromptStatus.DECLINED) {
            this.packLoadFeedback.reportFinalResult(uUID, PackLoadFeedback.FinalResult.DECLINED);
            return;
        }
        try {
            uRL = path.toUri().toURL();
        }
        catch (MalformedURLException malformedURLException) {
            throw new IllegalStateException("Can't convert path to URL " + String.valueOf(path), malformedURLException);
        }
        ServerPackData serverPackData = new ServerPackData(uUID, uRL, null);
        serverPackData.downloadStatus = PackDownloadStatus.DONE;
        serverPackData.path = path;
        this.pushNewPack(uUID, serverPackData);
    }

    private void pushNewPack(UUID uUID, ServerPackData serverPackData) {
        this.markExistingPacksAsRemoved(uUID);
        this.packs.add(serverPackData);
        if (this.packPromptStatus == PackPromptStatus.ALLOWED) {
            this.acceptPack(serverPackData);
        }
        this.registerForUpdate();
    }

    private void acceptPack(ServerPackData serverPackData) {
        this.packLoadFeedback.reportUpdate(serverPackData.id, PackLoadFeedback.Update.ACCEPTED);
        serverPackData.promptAccepted = true;
    }

    @Nullable
    private ServerPackData findPackInfo(UUID uUID) {
        for (ServerPackData serverPackData : this.packs) {
            if (serverPackData.isRemoved() || !serverPackData.id.equals(uUID)) continue;
            return serverPackData;
        }
        return null;
    }

    public void popPack(UUID uUID) {
        ServerPackData serverPackData = this.findPackInfo(uUID);
        if (serverPackData != null) {
            serverPackData.setRemovalReasonIfNotSet(RemovalReason.SERVER_REMOVED);
            this.registerForUpdate();
        }
    }

    public void popAll() {
        for (ServerPackData serverPackData : this.packs) {
            serverPackData.setRemovalReasonIfNotSet(RemovalReason.SERVER_REMOVED);
        }
        this.registerForUpdate();
    }

    public void allowServerPacks() {
        this.packPromptStatus = PackPromptStatus.ALLOWED;
        for (ServerPackData serverPackData : this.packs) {
            if (serverPackData.promptAccepted || serverPackData.isRemoved()) continue;
            this.acceptPack(serverPackData);
        }
        this.registerForUpdate();
    }

    public void rejectServerPacks() {
        this.packPromptStatus = PackPromptStatus.DECLINED;
        for (ServerPackData serverPackData : this.packs) {
            if (serverPackData.promptAccepted) continue;
            serverPackData.setRemovalReasonIfNotSet(RemovalReason.DECLINED);
        }
        this.registerForUpdate();
    }

    public void resetPromptStatus() {
        this.packPromptStatus = PackPromptStatus.PENDING;
    }

    public void tick() {
        boolean bl = this.updateDownloads();
        if (!bl) {
            this.triggerReloadIfNeeded();
        }
        this.cleanupRemovedPacks();
    }

    private void cleanupRemovedPacks() {
        this.packs.removeIf(serverPackData -> {
            if (serverPackData.activationStatus != ActivationStatus.INACTIVE) {
                return false;
            }
            if (serverPackData.removalReason != null) {
                PackLoadFeedback.FinalResult finalResult = serverPackData.removalReason.serverResponse;
                if (finalResult != null) {
                    this.packLoadFeedback.reportFinalResult(serverPackData.id, finalResult);
                }
                return true;
            }
            return false;
        });
    }

    private void onDownload(Collection<ServerPackData> collection, DownloadQueue.BatchResult batchResult) {
        if (!batchResult.failed().isEmpty()) {
            for (ServerPackData serverPackData : this.packs) {
                if (serverPackData.activationStatus == ActivationStatus.ACTIVE) continue;
                if (batchResult.failed().contains(serverPackData.id)) {
                    serverPackData.setRemovalReasonIfNotSet(RemovalReason.DOWNLOAD_FAILED);
                    continue;
                }
                serverPackData.setRemovalReasonIfNotSet(RemovalReason.DISCARDED);
            }
        }
        for (ServerPackData serverPackData : collection) {
            Path path = batchResult.downloaded().get(serverPackData.id);
            if (path == null) continue;
            serverPackData.downloadStatus = PackDownloadStatus.DONE;
            serverPackData.path = path;
            if (serverPackData.isRemoved()) continue;
            this.packLoadFeedback.reportUpdate(serverPackData.id, PackLoadFeedback.Update.DOWNLOADED);
        }
        this.registerForUpdate();
    }

    private boolean updateDownloads() {
        ArrayList<ServerPackData> arrayList = new ArrayList<ServerPackData>();
        boolean bl = false;
        for (ServerPackData object : this.packs) {
            if (object.isRemoved() || !object.promptAccepted) continue;
            if (object.downloadStatus != PackDownloadStatus.DONE) {
                bl = true;
            }
            if (object.downloadStatus != PackDownloadStatus.REQUESTED) continue;
            object.downloadStatus = PackDownloadStatus.PENDING;
            arrayList.add(object);
        }
        if (!arrayList.isEmpty()) {
            HashMap hashMap = new HashMap();
            for (ServerPackData serverPackData : arrayList) {
                hashMap.put(serverPackData.id, new DownloadQueue.DownloadRequest(serverPackData.url, serverPackData.hash));
            }
            this.downloader.download(hashMap, batchResult -> this.onDownload((Collection<ServerPackData>)arrayList, (DownloadQueue.BatchResult)batchResult));
        }
        return bl;
    }

    private void triggerReloadIfNeeded() {
        boolean bl = false;
        final ArrayList<ServerPackData> arrayList = new ArrayList<ServerPackData>();
        final ArrayList<ServerPackData> arrayList2 = new ArrayList<ServerPackData>();
        for (ServerPackData serverPackData : this.packs) {
            boolean bl2;
            if (serverPackData.activationStatus == ActivationStatus.PENDING) {
                return;
            }
            boolean bl3 = bl2 = serverPackData.promptAccepted && serverPackData.downloadStatus == PackDownloadStatus.DONE && !serverPackData.isRemoved();
            if (bl2 && serverPackData.activationStatus == ActivationStatus.INACTIVE) {
                arrayList.add(serverPackData);
                bl = true;
            }
            if (serverPackData.activationStatus != ActivationStatus.ACTIVE) continue;
            if (!bl2) {
                bl = true;
                arrayList2.add(serverPackData);
                continue;
            }
            arrayList.add(serverPackData);
        }
        if (bl) {
            for (ServerPackData serverPackData : arrayList) {
                if (serverPackData.activationStatus == ActivationStatus.ACTIVE) continue;
                serverPackData.activationStatus = ActivationStatus.PENDING;
            }
            for (ServerPackData serverPackData : arrayList2) {
                serverPackData.activationStatus = ActivationStatus.PENDING;
            }
            this.reloadConfig.scheduleReload(new PackReloadConfig.Callbacks(){

                @Override
                public void onSuccess() {
                    for (ServerPackData serverPackData : arrayList) {
                        serverPackData.activationStatus = ActivationStatus.ACTIVE;
                        if (serverPackData.removalReason != null) continue;
                        ServerPackManager.this.packLoadFeedback.reportFinalResult(serverPackData.id, PackLoadFeedback.FinalResult.APPLIED);
                    }
                    for (ServerPackData serverPackData : arrayList2) {
                        serverPackData.activationStatus = ActivationStatus.INACTIVE;
                    }
                    ServerPackManager.this.registerForUpdate();
                }

                @Override
                public void onFailure(boolean bl) {
                    if (!bl) {
                        arrayList.clear();
                        for (ServerPackData serverPackData : ServerPackManager.this.packs) {
                            switch (serverPackData.activationStatus.ordinal()) {
                                case 2: {
                                    arrayList.add(serverPackData);
                                    break;
                                }
                                case 1: {
                                    serverPackData.activationStatus = ActivationStatus.INACTIVE;
                                    serverPackData.setRemovalReasonIfNotSet(RemovalReason.ACTIVATION_FAILED);
                                    break;
                                }
                                case 0: {
                                    serverPackData.setRemovalReasonIfNotSet(RemovalReason.DISCARDED);
                                }
                            }
                        }
                        ServerPackManager.this.registerForUpdate();
                    } else {
                        for (ServerPackData serverPackData : ServerPackManager.this.packs) {
                            if (serverPackData.activationStatus != ActivationStatus.PENDING) continue;
                            serverPackData.activationStatus = ActivationStatus.INACTIVE;
                        }
                    }
                }

                @Override
                public List<PackReloadConfig.IdAndPath> packsToLoad() {
                    return arrayList.stream().map(serverPackData -> new PackReloadConfig.IdAndPath(serverPackData.id, serverPackData.path)).toList();
                }
            });
        }
    }

    public static enum PackPromptStatus {
        PENDING,
        ALLOWED,
        DECLINED;

    }

    static class ServerPackData {
        final UUID id;
        final URL url;
        @Nullable
        final HashCode hash;
        @Nullable
        Path path;
        @Nullable
        RemovalReason removalReason;
        PackDownloadStatus downloadStatus = PackDownloadStatus.REQUESTED;
        ActivationStatus activationStatus = ActivationStatus.INACTIVE;
        boolean promptAccepted;

        ServerPackData(UUID uUID, URL uRL, @Nullable HashCode hashCode) {
            this.id = uUID;
            this.url = uRL;
            this.hash = hashCode;
        }

        public void setRemovalReasonIfNotSet(RemovalReason removalReason) {
            if (this.removalReason == null) {
                this.removalReason = removalReason;
            }
        }

        public boolean isRemoved() {
            return this.removalReason != null;
        }
    }

    static enum RemovalReason {
        DOWNLOAD_FAILED(PackLoadFeedback.FinalResult.DOWNLOAD_FAILED),
        ACTIVATION_FAILED(PackLoadFeedback.FinalResult.ACTIVATION_FAILED),
        DECLINED(PackLoadFeedback.FinalResult.DECLINED),
        DISCARDED(PackLoadFeedback.FinalResult.DISCARDED),
        SERVER_REMOVED(null),
        SERVER_REPLACED(null);

        @Nullable
        final PackLoadFeedback.FinalResult serverResponse;

        private RemovalReason(PackLoadFeedback.FinalResult finalResult) {
            this.serverResponse = finalResult;
        }
    }

    static enum PackDownloadStatus {
        REQUESTED,
        PENDING,
        DONE;

    }

    static enum ActivationStatus {
        INACTIVE,
        PENDING,
        ACTIVE;

    }
}

