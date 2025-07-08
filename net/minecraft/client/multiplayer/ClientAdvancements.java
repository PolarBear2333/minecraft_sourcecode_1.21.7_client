/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.multiplayer;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.AdvancementTree;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.telemetry.WorldSessionTelemetryManager;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class ClientAdvancements {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Minecraft minecraft;
    private final WorldSessionTelemetryManager telemetryManager;
    private final AdvancementTree tree = new AdvancementTree();
    private final Map<AdvancementHolder, AdvancementProgress> progress = new Object2ObjectOpenHashMap();
    @Nullable
    private Listener listener;
    @Nullable
    private AdvancementHolder selectedTab;

    public ClientAdvancements(Minecraft minecraft, WorldSessionTelemetryManager worldSessionTelemetryManager) {
        this.minecraft = minecraft;
        this.telemetryManager = worldSessionTelemetryManager;
    }

    public void update(ClientboundUpdateAdvancementsPacket clientboundUpdateAdvancementsPacket) {
        if (clientboundUpdateAdvancementsPacket.shouldReset()) {
            this.tree.clear();
            this.progress.clear();
        }
        this.tree.remove(clientboundUpdateAdvancementsPacket.getRemoved());
        this.tree.addAll(clientboundUpdateAdvancementsPacket.getAdded());
        for (Map.Entry<ResourceLocation, AdvancementProgress> entry : clientboundUpdateAdvancementsPacket.getProgress().entrySet()) {
            AdvancementNode advancementNode = this.tree.get(entry.getKey());
            if (advancementNode != null) {
                AdvancementProgress advancementProgress = entry.getValue();
                advancementProgress.update(advancementNode.advancement().requirements());
                this.progress.put(advancementNode.holder(), advancementProgress);
                if (this.listener != null) {
                    this.listener.onUpdateAdvancementProgress(advancementNode, advancementProgress);
                }
                if (clientboundUpdateAdvancementsPacket.shouldReset() || !advancementProgress.isDone()) continue;
                if (this.minecraft.level != null) {
                    this.telemetryManager.onAdvancementDone(this.minecraft.level, advancementNode.holder());
                }
                Optional<DisplayInfo> optional = advancementNode.advancement().display();
                if (!clientboundUpdateAdvancementsPacket.shouldShowAdvancements() || !optional.isPresent() || !optional.get().shouldShowToast()) continue;
                this.minecraft.getToastManager().addToast(new AdvancementToast(advancementNode.holder()));
                continue;
            }
            LOGGER.warn("Server informed client about progress for unknown advancement {}", (Object)entry.getKey());
        }
    }

    public AdvancementTree getTree() {
        return this.tree;
    }

    public void setSelectedTab(@Nullable AdvancementHolder advancementHolder, boolean bl) {
        ClientPacketListener clientPacketListener = this.minecraft.getConnection();
        if (clientPacketListener != null && advancementHolder != null && bl) {
            clientPacketListener.send(ServerboundSeenAdvancementsPacket.openedTab(advancementHolder));
        }
        if (this.selectedTab != advancementHolder) {
            this.selectedTab = advancementHolder;
            if (this.listener != null) {
                this.listener.onSelectedTabChanged(advancementHolder);
            }
        }
    }

    public void setListener(@Nullable Listener listener) {
        this.listener = listener;
        this.tree.setListener(listener);
        if (listener != null) {
            this.progress.forEach((advancementHolder, advancementProgress) -> {
                AdvancementNode advancementNode = this.tree.get((AdvancementHolder)advancementHolder);
                if (advancementNode != null) {
                    listener.onUpdateAdvancementProgress(advancementNode, (AdvancementProgress)advancementProgress);
                }
            });
            listener.onSelectedTabChanged(this.selectedTab);
        }
    }

    @Nullable
    public AdvancementHolder get(ResourceLocation resourceLocation) {
        AdvancementNode advancementNode = this.tree.get(resourceLocation);
        return advancementNode != null ? advancementNode.holder() : null;
    }

    public static interface Listener
    extends AdvancementTree.Listener {
        public void onUpdateAdvancementProgress(AdvancementNode var1, AdvancementProgress var2);

        public void onSelectedTabChanged(@Nullable AdvancementHolder var1);
    }
}

