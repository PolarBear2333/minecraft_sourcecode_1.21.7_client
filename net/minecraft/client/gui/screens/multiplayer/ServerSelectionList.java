/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.util.concurrent.ThreadFactoryBuilder
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.gui.screens.multiplayer;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.DefaultUncaughtExceptionHandler;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.FaviconTexture;
import net.minecraft.client.gui.screens.LoadingDotsText;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.server.LanServer;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import org.slf4j.Logger;

public class ServerSelectionList
extends ObjectSelectionList<Entry> {
    static final ResourceLocation INCOMPATIBLE_SPRITE = ResourceLocation.withDefaultNamespace("server_list/incompatible");
    static final ResourceLocation UNREACHABLE_SPRITE = ResourceLocation.withDefaultNamespace("server_list/unreachable");
    static final ResourceLocation PING_1_SPRITE = ResourceLocation.withDefaultNamespace("server_list/ping_1");
    static final ResourceLocation PING_2_SPRITE = ResourceLocation.withDefaultNamespace("server_list/ping_2");
    static final ResourceLocation PING_3_SPRITE = ResourceLocation.withDefaultNamespace("server_list/ping_3");
    static final ResourceLocation PING_4_SPRITE = ResourceLocation.withDefaultNamespace("server_list/ping_4");
    static final ResourceLocation PING_5_SPRITE = ResourceLocation.withDefaultNamespace("server_list/ping_5");
    static final ResourceLocation PINGING_1_SPRITE = ResourceLocation.withDefaultNamespace("server_list/pinging_1");
    static final ResourceLocation PINGING_2_SPRITE = ResourceLocation.withDefaultNamespace("server_list/pinging_2");
    static final ResourceLocation PINGING_3_SPRITE = ResourceLocation.withDefaultNamespace("server_list/pinging_3");
    static final ResourceLocation PINGING_4_SPRITE = ResourceLocation.withDefaultNamespace("server_list/pinging_4");
    static final ResourceLocation PINGING_5_SPRITE = ResourceLocation.withDefaultNamespace("server_list/pinging_5");
    static final ResourceLocation JOIN_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("server_list/join_highlighted");
    static final ResourceLocation JOIN_SPRITE = ResourceLocation.withDefaultNamespace("server_list/join");
    static final ResourceLocation MOVE_UP_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("server_list/move_up_highlighted");
    static final ResourceLocation MOVE_UP_SPRITE = ResourceLocation.withDefaultNamespace("server_list/move_up");
    static final ResourceLocation MOVE_DOWN_HIGHLIGHTED_SPRITE = ResourceLocation.withDefaultNamespace("server_list/move_down_highlighted");
    static final ResourceLocation MOVE_DOWN_SPRITE = ResourceLocation.withDefaultNamespace("server_list/move_down");
    static final Logger LOGGER = LogUtils.getLogger();
    static final ThreadPoolExecutor THREAD_POOL = new ScheduledThreadPoolExecutor(5, new ThreadFactoryBuilder().setNameFormat("Server Pinger #%d").setDaemon(true).setUncaughtExceptionHandler((Thread.UncaughtExceptionHandler)new DefaultUncaughtExceptionHandler(LOGGER)).build());
    static final Component SCANNING_LABEL = Component.translatable("lanServer.scanning");
    static final Component CANT_RESOLVE_TEXT = Component.translatable("multiplayer.status.cannot_resolve").withColor(-65536);
    static final Component CANT_CONNECT_TEXT = Component.translatable("multiplayer.status.cannot_connect").withColor(-65536);
    static final Component INCOMPATIBLE_STATUS = Component.translatable("multiplayer.status.incompatible");
    static final Component NO_CONNECTION_STATUS = Component.translatable("multiplayer.status.no_connection");
    static final Component PINGING_STATUS = Component.translatable("multiplayer.status.pinging");
    static final Component ONLINE_STATUS = Component.translatable("multiplayer.status.online");
    private final JoinMultiplayerScreen screen;
    private final List<OnlineServerEntry> onlineServers = Lists.newArrayList();
    private final Entry lanHeader = new LANHeader();
    private final List<NetworkServerEntry> networkServers = Lists.newArrayList();

    public ServerSelectionList(JoinMultiplayerScreen joinMultiplayerScreen, Minecraft minecraft, int n, int n2, int n3, int n4) {
        super(minecraft, n, n2, n3, n4);
        this.screen = joinMultiplayerScreen;
    }

    private void refreshEntries() {
        this.clearEntries();
        this.onlineServers.forEach(entry -> this.addEntry(entry));
        this.addEntry(this.lanHeader);
        this.networkServers.forEach(entry -> this.addEntry(entry));
    }

    @Override
    public void setSelected(@Nullable Entry entry) {
        super.setSelected(entry);
        this.screen.onSelectedChange();
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        Entry entry = (Entry)this.getSelected();
        return entry != null && entry.keyPressed(n, n2, n3) || super.keyPressed(n, n2, n3);
    }

    public void updateOnlineServers(ServerList serverList) {
        this.onlineServers.clear();
        for (int i = 0; i < serverList.size(); ++i) {
            this.onlineServers.add(new OnlineServerEntry(this.screen, serverList.get(i)));
        }
        this.refreshEntries();
    }

    public void updateNetworkServers(List<LanServer> list) {
        int n = list.size() - this.networkServers.size();
        this.networkServers.clear();
        for (LanServer object : list) {
            this.networkServers.add(new NetworkServerEntry(this.screen, object));
        }
        this.refreshEntries();
        for (int i = this.networkServers.size() - n; i < this.networkServers.size(); ++i) {
            NetworkServerEntry networkServerEntry = this.networkServers.get(i);
            int n2 = i - this.networkServers.size() + this.children().size();
            int n3 = this.getRowTop(n2);
            int n4 = this.getRowBottom(n2);
            if (n4 < this.getY() || n3 > this.getBottom()) continue;
            this.minecraft.getNarrator().saySystemQueued(Component.translatable("multiplayer.lan.server_found", networkServerEntry.getServerNarration()));
        }
    }

    @Override
    public int getRowWidth() {
        return 305;
    }

    public void removed() {
    }

    public static class LANHeader
    extends Entry {
        private final Minecraft minecraft = Minecraft.getInstance();

        @Override
        public void render(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
            int n8 = n2 + n5 / 2 - this.minecraft.font.lineHeight / 2;
            guiGraphics.drawString(this.minecraft.font, SCANNING_LABEL, this.minecraft.screen.width / 2 - this.minecraft.font.width(SCANNING_LABEL) / 2, n8, -1);
            String string = LoadingDotsText.get(Util.getMillis());
            guiGraphics.drawString(this.minecraft.font, string, this.minecraft.screen.width / 2 - this.minecraft.font.width(string) / 2, n8 + this.minecraft.font.lineHeight, -8355712);
        }

        @Override
        public Component getNarration() {
            return SCANNING_LABEL;
        }
    }

    public static abstract class Entry
    extends ObjectSelectionList.Entry<Entry>
    implements AutoCloseable {
        @Override
        public void close() {
        }
    }

    public class OnlineServerEntry
    extends Entry {
        private static final int ICON_WIDTH = 32;
        private static final int ICON_HEIGHT = 32;
        private static final int SPACING = 5;
        private static final int STATUS_ICON_WIDTH = 10;
        private static final int STATUS_ICON_HEIGHT = 8;
        private final JoinMultiplayerScreen screen;
        private final Minecraft minecraft;
        private final ServerData serverData;
        private final FaviconTexture icon;
        @Nullable
        private byte[] lastIconBytes;
        private long lastClickTime;
        @Nullable
        private List<Component> onlinePlayersTooltip;
        @Nullable
        private ResourceLocation statusIcon;
        @Nullable
        private Component statusIconTooltip;

        protected OnlineServerEntry(JoinMultiplayerScreen joinMultiplayerScreen, ServerData serverData) {
            this.screen = joinMultiplayerScreen;
            this.serverData = serverData;
            this.minecraft = Minecraft.getInstance();
            this.icon = FaviconTexture.forServer(this.minecraft.getTextureManager(), serverData.ip);
            this.refreshStatus();
        }

        @Override
        public void render(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
            byte[] byArray;
            int n8;
            if (this.serverData.state() == ServerData.State.INITIAL) {
                this.serverData.setState(ServerData.State.PINGING);
                this.serverData.motd = CommonComponents.EMPTY;
                this.serverData.status = CommonComponents.EMPTY;
                THREAD_POOL.submit(() -> {
                    try {
                        this.screen.getPinger().pingServer(this.serverData, () -> this.minecraft.execute(this::updateServerList), () -> {
                            this.serverData.setState(this.serverData.protocol == SharedConstants.getCurrentVersion().protocolVersion() ? ServerData.State.SUCCESSFUL : ServerData.State.INCOMPATIBLE);
                            this.minecraft.execute(this::refreshStatus);
                        });
                    }
                    catch (UnknownHostException unknownHostException) {
                        this.serverData.setState(ServerData.State.UNREACHABLE);
                        this.serverData.motd = CANT_RESOLVE_TEXT;
                        this.minecraft.execute(this::refreshStatus);
                    }
                    catch (Exception exception) {
                        this.serverData.setState(ServerData.State.UNREACHABLE);
                        this.serverData.motd = CANT_CONNECT_TEXT;
                        this.minecraft.execute(this::refreshStatus);
                    }
                });
            }
            guiGraphics.drawString(this.minecraft.font, this.serverData.name, n3 + 32 + 3, n2 + 1, -1);
            List<FormattedCharSequence> list = this.minecraft.font.split(this.serverData.motd, n4 - 32 - 2);
            for (n8 = 0; n8 < Math.min(list.size(), 2); ++n8) {
                guiGraphics.drawString(this.minecraft.font, list.get(n8), n3 + 32 + 3, n2 + 12 + this.minecraft.font.lineHeight * n8, -8355712);
            }
            this.drawIcon(guiGraphics, n3, n2, this.icon.textureLocation());
            if (this.serverData.state() == ServerData.State.PINGING) {
                n8 = (int)(Util.getMillis() / 100L + (long)(n * 2) & 7L);
                if (n8 > 4) {
                    n8 = 8 - n8;
                }
                this.statusIcon = switch (n8) {
                    default -> PINGING_1_SPRITE;
                    case 1 -> PINGING_2_SPRITE;
                    case 2 -> PINGING_3_SPRITE;
                    case 3 -> PINGING_4_SPRITE;
                    case 4 -> PINGING_5_SPRITE;
                };
            }
            n8 = n3 + n4 - 10 - 5;
            if (this.statusIcon != null) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.statusIcon, n8, n2, 10, 8);
            }
            if (!Arrays.equals(byArray = this.serverData.getIconBytes(), this.lastIconBytes)) {
                if (this.uploadServerIcon(byArray)) {
                    this.lastIconBytes = byArray;
                } else {
                    this.serverData.setIconBytes(null);
                    this.updateServerList();
                }
            }
            Component component = this.serverData.state() == ServerData.State.INCOMPATIBLE ? this.serverData.version.copy().withStyle(ChatFormatting.RED) : this.serverData.status;
            int n9 = this.minecraft.font.width(component);
            int n10 = n8 - n9 - 5;
            guiGraphics.drawString(this.minecraft.font, component, n10, n2 + 1, -8355712);
            if (this.statusIconTooltip != null && n6 >= n8 && n6 <= n8 + 10 && n7 >= n2 && n7 <= n2 + 8) {
                guiGraphics.setTooltipForNextFrame(this.statusIconTooltip, n6, n7);
            } else if (this.onlinePlayersTooltip != null && n6 >= n10 && n6 <= n10 + n9 && n7 >= n2 && n7 <= n2 - 1 + this.minecraft.font.lineHeight) {
                guiGraphics.setTooltipForNextFrame(Lists.transform(this.onlinePlayersTooltip, Component::getVisualOrderText), n6, n7);
            }
            if (this.minecraft.options.touchscreen().get().booleanValue() || bl) {
                guiGraphics.fill(n3, n2, n3 + 32, n2 + 32, -1601138544);
                int n11 = n6 - n3;
                int n12 = n7 - n2;
                if (this.canJoin()) {
                    if (n11 < 32 && n11 > 16) {
                        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, JOIN_HIGHLIGHTED_SPRITE, n3, n2, 32, 32);
                    } else {
                        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, JOIN_SPRITE, n3, n2, 32, 32);
                    }
                }
                if (n > 0) {
                    if (n11 < 16 && n12 < 16) {
                        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, MOVE_UP_HIGHLIGHTED_SPRITE, n3, n2, 32, 32);
                    } else {
                        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, MOVE_UP_SPRITE, n3, n2, 32, 32);
                    }
                }
                if (n < this.screen.getServers().size() - 1) {
                    if (n11 < 16 && n12 > 16) {
                        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, MOVE_DOWN_HIGHLIGHTED_SPRITE, n3, n2, 32, 32);
                    } else {
                        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, MOVE_DOWN_SPRITE, n3, n2, 32, 32);
                    }
                }
            }
        }

        private void refreshStatus() {
            this.onlinePlayersTooltip = null;
            switch (this.serverData.state()) {
                case INITIAL: 
                case PINGING: {
                    this.statusIcon = PING_1_SPRITE;
                    this.statusIconTooltip = PINGING_STATUS;
                    break;
                }
                case INCOMPATIBLE: {
                    this.statusIcon = INCOMPATIBLE_SPRITE;
                    this.statusIconTooltip = INCOMPATIBLE_STATUS;
                    this.onlinePlayersTooltip = this.serverData.playerList;
                    break;
                }
                case UNREACHABLE: {
                    this.statusIcon = UNREACHABLE_SPRITE;
                    this.statusIconTooltip = NO_CONNECTION_STATUS;
                    break;
                }
                case SUCCESSFUL: {
                    this.statusIcon = this.serverData.ping < 150L ? PING_5_SPRITE : (this.serverData.ping < 300L ? PING_4_SPRITE : (this.serverData.ping < 600L ? PING_3_SPRITE : (this.serverData.ping < 1000L ? PING_2_SPRITE : PING_1_SPRITE)));
                    this.statusIconTooltip = Component.translatable("multiplayer.status.ping", this.serverData.ping);
                    this.onlinePlayersTooltip = this.serverData.playerList;
                }
            }
        }

        public void updateServerList() {
            this.screen.getServers().save();
        }

        protected void drawIcon(GuiGraphics guiGraphics, int n, int n2, ResourceLocation resourceLocation) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, resourceLocation, n, n2, 0.0f, 0.0f, 32, 32, 32, 32);
        }

        private boolean canJoin() {
            return true;
        }

        private boolean uploadServerIcon(@Nullable byte[] byArray) {
            if (byArray == null) {
                this.icon.clear();
            } else {
                try {
                    this.icon.upload(NativeImage.read(byArray));
                }
                catch (Throwable throwable) {
                    LOGGER.error("Invalid icon for server {} ({})", new Object[]{this.serverData.name, this.serverData.ip, throwable});
                    return false;
                }
            }
            return true;
        }

        @Override
        public boolean keyPressed(int n, int n2, int n3) {
            if (Screen.hasShiftDown()) {
                ServerSelectionList serverSelectionList = this.screen.serverSelectionList;
                int n4 = serverSelectionList.children().indexOf(this);
                if (n4 == -1) {
                    return true;
                }
                if (n == 264 && n4 < this.screen.getServers().size() - 1 || n == 265 && n4 > 0) {
                    this.swap(n4, n == 264 ? n4 + 1 : n4 - 1);
                    return true;
                }
            }
            return super.keyPressed(n, n2, n3);
        }

        private void swap(int n, int n2) {
            this.screen.getServers().swap(n, n2);
            this.screen.serverSelectionList.updateOnlineServers(this.screen.getServers());
            Entry entry = (Entry)this.screen.serverSelectionList.children().get(n2);
            this.screen.serverSelectionList.setSelected(entry);
            ServerSelectionList.this.ensureVisible(entry);
        }

        @Override
        public boolean mouseClicked(double d, double d2, int n) {
            double d3 = d - (double)ServerSelectionList.this.getRowLeft();
            double d4 = d2 - (double)ServerSelectionList.this.getRowTop(ServerSelectionList.this.children().indexOf(this));
            if (d3 <= 32.0) {
                if (d3 < 32.0 && d3 > 16.0 && this.canJoin()) {
                    this.screen.setSelected(this);
                    this.screen.joinSelectedServer();
                    return true;
                }
                int n2 = this.screen.serverSelectionList.children().indexOf(this);
                if (d3 < 16.0 && d4 < 16.0 && n2 > 0) {
                    this.swap(n2, n2 - 1);
                    return true;
                }
                if (d3 < 16.0 && d4 > 16.0 && n2 < this.screen.getServers().size() - 1) {
                    this.swap(n2, n2 + 1);
                    return true;
                }
            }
            this.screen.setSelected(this);
            if (Util.getMillis() - this.lastClickTime < 250L) {
                this.screen.joinSelectedServer();
            }
            this.lastClickTime = Util.getMillis();
            return super.mouseClicked(d, d2, n);
        }

        public ServerData getServerData() {
            return this.serverData;
        }

        @Override
        public Component getNarration() {
            MutableComponent mutableComponent = Component.empty();
            mutableComponent.append(Component.translatable("narrator.select", this.serverData.name));
            mutableComponent.append(CommonComponents.NARRATION_SEPARATOR);
            switch (this.serverData.state()) {
                case INCOMPATIBLE: {
                    mutableComponent.append(INCOMPATIBLE_STATUS);
                    mutableComponent.append(CommonComponents.NARRATION_SEPARATOR);
                    mutableComponent.append(Component.translatable("multiplayer.status.version.narration", this.serverData.version));
                    mutableComponent.append(CommonComponents.NARRATION_SEPARATOR);
                    mutableComponent.append(Component.translatable("multiplayer.status.motd.narration", this.serverData.motd));
                    break;
                }
                case UNREACHABLE: {
                    mutableComponent.append(NO_CONNECTION_STATUS);
                    break;
                }
                case PINGING: {
                    mutableComponent.append(PINGING_STATUS);
                    break;
                }
                default: {
                    mutableComponent.append(ONLINE_STATUS);
                    mutableComponent.append(CommonComponents.NARRATION_SEPARATOR);
                    mutableComponent.append(Component.translatable("multiplayer.status.ping.narration", this.serverData.ping));
                    mutableComponent.append(CommonComponents.NARRATION_SEPARATOR);
                    mutableComponent.append(Component.translatable("multiplayer.status.motd.narration", this.serverData.motd));
                    if (this.serverData.players == null) break;
                    mutableComponent.append(CommonComponents.NARRATION_SEPARATOR);
                    mutableComponent.append(Component.translatable("multiplayer.status.player_count.narration", this.serverData.players.online(), this.serverData.players.max()));
                    mutableComponent.append(CommonComponents.NARRATION_SEPARATOR);
                    mutableComponent.append(ComponentUtils.formatList(this.serverData.playerList, Component.literal(", ")));
                }
            }
            return mutableComponent;
        }

        @Override
        public void close() {
            this.icon.close();
        }
    }

    public static class NetworkServerEntry
    extends Entry {
        private static final int ICON_WIDTH = 32;
        private static final Component LAN_SERVER_HEADER = Component.translatable("lanServer.title");
        private static final Component HIDDEN_ADDRESS_TEXT = Component.translatable("selectServer.hiddenAddress");
        private final JoinMultiplayerScreen screen;
        protected final Minecraft minecraft;
        protected final LanServer serverData;
        private long lastClickTime;

        protected NetworkServerEntry(JoinMultiplayerScreen joinMultiplayerScreen, LanServer lanServer) {
            this.screen = joinMultiplayerScreen;
            this.serverData = lanServer;
            this.minecraft = Minecraft.getInstance();
        }

        @Override
        public void render(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
            guiGraphics.drawString(this.minecraft.font, LAN_SERVER_HEADER, n3 + 32 + 3, n2 + 1, -1);
            guiGraphics.drawString(this.minecraft.font, this.serverData.getMotd(), n3 + 32 + 3, n2 + 12, -8355712);
            if (this.minecraft.options.hideServerAddress) {
                guiGraphics.drawString(this.minecraft.font, HIDDEN_ADDRESS_TEXT, n3 + 32 + 3, n2 + 12 + 11, -13619152);
            } else {
                guiGraphics.drawString(this.minecraft.font, this.serverData.getAddress(), n3 + 32 + 3, n2 + 12 + 11, -13619152);
            }
        }

        @Override
        public boolean mouseClicked(double d, double d2, int n) {
            this.screen.setSelected(this);
            if (Util.getMillis() - this.lastClickTime < 250L) {
                this.screen.joinSelectedServer();
            }
            this.lastClickTime = Util.getMillis();
            return super.mouseClicked(d, d2, n);
        }

        public LanServer getServerData() {
            return this.serverData;
        }

        @Override
        public Component getNarration() {
            return Component.translatable("narrator.select", this.getServerNarration());
        }

        public Component getServerNarration() {
            return Component.empty().append(LAN_SERVER_HEADER).append(CommonComponents.SPACE).append(this.serverData.getMotd());
        }
    }
}

