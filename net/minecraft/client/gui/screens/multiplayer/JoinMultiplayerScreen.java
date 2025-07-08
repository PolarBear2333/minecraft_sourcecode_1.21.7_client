/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.gui.screens.multiplayer;

import com.mojang.logging.LogUtils;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.EqualSpacingLayout;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.navigation.CommonInputs;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DirectJoinServerScreen;
import net.minecraft.client.gui.screens.EditServerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.server.LanServer;
import net.minecraft.client.server.LanServerDetection;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.slf4j.Logger;

public class JoinMultiplayerScreen
extends Screen {
    public static final int BUTTON_ROW_WIDTH = 308;
    public static final int TOP_ROW_BUTTON_WIDTH = 100;
    public static final int LOWER_ROW_BUTTON_WIDTH = 74;
    public static final int FOOTER_HEIGHT = 64;
    private static final Logger LOGGER = LogUtils.getLogger();
    private final ServerStatusPinger pinger = new ServerStatusPinger();
    private final Screen lastScreen;
    protected ServerSelectionList serverSelectionList;
    private ServerList servers;
    private Button editButton;
    private Button selectButton;
    private Button deleteButton;
    private ServerData editingServer;
    private LanServerDetection.LanServerList lanServerList;
    @Nullable
    private LanServerDetection.LanServerDetector lanServerDetector;
    private boolean initedOnce;

    public JoinMultiplayerScreen(Screen screen) {
        super(Component.translatable("multiplayer.title"));
        this.lastScreen = screen;
    }

    @Override
    protected void init() {
        if (this.initedOnce) {
            this.serverSelectionList.setRectangle(this.width, this.height - 64 - 32, 0, 32);
        } else {
            this.initedOnce = true;
            this.servers = new ServerList(this.minecraft);
            this.servers.load();
            this.lanServerList = new LanServerDetection.LanServerList();
            try {
                this.lanServerDetector = new LanServerDetection.LanServerDetector(this.lanServerList);
                this.lanServerDetector.start();
            }
            catch (Exception exception) {
                LOGGER.warn("Unable to start LAN server detection: {}", (Object)exception.getMessage());
            }
            this.serverSelectionList = new ServerSelectionList(this, this.minecraft, this.width, this.height - 64 - 32, 32, 36);
            this.serverSelectionList.updateOnlineServers(this.servers);
        }
        this.addRenderableWidget(this.serverSelectionList);
        this.selectButton = this.addRenderableWidget(Button.builder(Component.translatable("selectServer.select"), button -> this.joinSelectedServer()).width(100).build());
        Button button2 = this.addRenderableWidget(Button.builder(Component.translatable("selectServer.direct"), button -> {
            this.editingServer = new ServerData(I18n.get("selectServer.defaultName", new Object[0]), "", ServerData.Type.OTHER);
            this.minecraft.setScreen(new DirectJoinServerScreen(this, this::directJoinCallback, this.editingServer));
        }).width(100).build());
        Button button3 = this.addRenderableWidget(Button.builder(Component.translatable("selectServer.add"), button -> {
            this.editingServer = new ServerData(I18n.get("selectServer.defaultName", new Object[0]), "", ServerData.Type.OTHER);
            this.minecraft.setScreen(new EditServerScreen(this, this::addServerCallback, this.editingServer));
        }).width(100).build());
        this.editButton = this.addRenderableWidget(Button.builder(Component.translatable("selectServer.edit"), button -> {
            ServerSelectionList.Entry entry = (ServerSelectionList.Entry)this.serverSelectionList.getSelected();
            if (entry instanceof ServerSelectionList.OnlineServerEntry) {
                ServerData serverData = ((ServerSelectionList.OnlineServerEntry)entry).getServerData();
                this.editingServer = new ServerData(serverData.name, serverData.ip, ServerData.Type.OTHER);
                this.editingServer.copyFrom(serverData);
                this.minecraft.setScreen(new EditServerScreen(this, this::editServerCallback, this.editingServer));
            }
        }).width(74).build());
        this.deleteButton = this.addRenderableWidget(Button.builder(Component.translatable("selectServer.delete"), button -> {
            String string;
            ServerSelectionList.Entry entry = (ServerSelectionList.Entry)this.serverSelectionList.getSelected();
            if (entry instanceof ServerSelectionList.OnlineServerEntry && (string = ((ServerSelectionList.OnlineServerEntry)entry).getServerData().name) != null) {
                MutableComponent mutableComponent = Component.translatable("selectServer.deleteQuestion");
                MutableComponent mutableComponent2 = Component.translatable("selectServer.deleteWarning", string);
                MutableComponent mutableComponent3 = Component.translatable("selectServer.deleteButton");
                Component component = CommonComponents.GUI_CANCEL;
                this.minecraft.setScreen(new ConfirmScreen(this::deleteCallback, mutableComponent, mutableComponent2, mutableComponent3, component));
            }
        }).width(74).build());
        Button button4 = this.addRenderableWidget(Button.builder(Component.translatable("selectServer.refresh"), button -> this.refreshServerList()).width(74).build());
        Button button5 = this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> this.onClose()).width(74).build());
        LinearLayout linearLayout = LinearLayout.vertical();
        EqualSpacingLayout equalSpacingLayout = linearLayout.addChild(new EqualSpacingLayout(308, 20, EqualSpacingLayout.Orientation.HORIZONTAL));
        equalSpacingLayout.addChild(this.selectButton);
        equalSpacingLayout.addChild(button2);
        equalSpacingLayout.addChild(button3);
        linearLayout.addChild(SpacerElement.height(4));
        EqualSpacingLayout equalSpacingLayout2 = linearLayout.addChild(new EqualSpacingLayout(308, 20, EqualSpacingLayout.Orientation.HORIZONTAL));
        equalSpacingLayout2.addChild(this.editButton);
        equalSpacingLayout2.addChild(this.deleteButton);
        equalSpacingLayout2.addChild(button4);
        equalSpacingLayout2.addChild(button5);
        linearLayout.arrangeElements();
        FrameLayout.centerInRectangle(linearLayout, 0, this.height - 64, this.width, 64);
        this.onSelectedChange();
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public void tick() {
        super.tick();
        List<LanServer> list = this.lanServerList.takeDirtyServers();
        if (list != null) {
            this.serverSelectionList.updateNetworkServers(list);
        }
        this.pinger.tick();
    }

    @Override
    public void removed() {
        if (this.lanServerDetector != null) {
            this.lanServerDetector.interrupt();
            this.lanServerDetector = null;
        }
        this.pinger.removeAll();
        this.serverSelectionList.removed();
    }

    private void refreshServerList() {
        this.minecraft.setScreen(new JoinMultiplayerScreen(this.lastScreen));
    }

    private void deleteCallback(boolean bl) {
        ServerSelectionList.Entry entry = (ServerSelectionList.Entry)this.serverSelectionList.getSelected();
        if (bl && entry instanceof ServerSelectionList.OnlineServerEntry) {
            this.servers.remove(((ServerSelectionList.OnlineServerEntry)entry).getServerData());
            this.servers.save();
            this.serverSelectionList.setSelected((ServerSelectionList.Entry)null);
            this.serverSelectionList.updateOnlineServers(this.servers);
        }
        this.minecraft.setScreen(this);
    }

    private void editServerCallback(boolean bl) {
        ServerSelectionList.Entry entry = (ServerSelectionList.Entry)this.serverSelectionList.getSelected();
        if (bl && entry instanceof ServerSelectionList.OnlineServerEntry) {
            ServerData serverData = ((ServerSelectionList.OnlineServerEntry)entry).getServerData();
            serverData.name = this.editingServer.name;
            serverData.ip = this.editingServer.ip;
            serverData.copyFrom(this.editingServer);
            this.servers.save();
            this.serverSelectionList.updateOnlineServers(this.servers);
        }
        this.minecraft.setScreen(this);
    }

    private void addServerCallback(boolean bl) {
        if (bl) {
            ServerData serverData = this.servers.unhide(this.editingServer.ip);
            if (serverData != null) {
                serverData.copyNameIconFrom(this.editingServer);
                this.servers.save();
            } else {
                this.servers.add(this.editingServer, false);
                this.servers.save();
            }
            this.serverSelectionList.setSelected((ServerSelectionList.Entry)null);
            this.serverSelectionList.updateOnlineServers(this.servers);
        }
        this.minecraft.setScreen(this);
    }

    private void directJoinCallback(boolean bl) {
        if (bl) {
            ServerData serverData = this.servers.get(this.editingServer.ip);
            if (serverData == null) {
                this.servers.add(this.editingServer, true);
                this.servers.save();
                this.join(this.editingServer);
            } else {
                this.join(serverData);
            }
        } else {
            this.minecraft.setScreen(this);
        }
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        if (super.keyPressed(n, n2, n3)) {
            return true;
        }
        if (n == 294) {
            this.refreshServerList();
            return true;
        }
        if (this.serverSelectionList.getSelected() != null) {
            if (CommonInputs.selected(n)) {
                this.joinSelectedServer();
                return true;
            }
            return this.serverSelectionList.keyPressed(n, n2, n3);
        }
        return false;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        super.render(guiGraphics, n, n2, f);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, -1);
    }

    public void joinSelectedServer() {
        ServerSelectionList.Entry entry = (ServerSelectionList.Entry)this.serverSelectionList.getSelected();
        if (entry instanceof ServerSelectionList.OnlineServerEntry) {
            this.join(((ServerSelectionList.OnlineServerEntry)entry).getServerData());
        } else if (entry instanceof ServerSelectionList.NetworkServerEntry) {
            LanServer lanServer = ((ServerSelectionList.NetworkServerEntry)entry).getServerData();
            this.join(new ServerData(lanServer.getMotd(), lanServer.getAddress(), ServerData.Type.LAN));
        }
    }

    private void join(ServerData serverData) {
        ConnectScreen.startConnecting(this, this.minecraft, ServerAddress.parseString(serverData.ip), serverData, false, null);
    }

    public void setSelected(ServerSelectionList.Entry entry) {
        this.serverSelectionList.setSelected(entry);
        this.onSelectedChange();
    }

    protected void onSelectedChange() {
        this.selectButton.active = false;
        this.editButton.active = false;
        this.deleteButton.active = false;
        ServerSelectionList.Entry entry = (ServerSelectionList.Entry)this.serverSelectionList.getSelected();
        if (entry != null && !(entry instanceof ServerSelectionList.LANHeader)) {
            this.selectButton.active = true;
            if (entry instanceof ServerSelectionList.OnlineServerEntry) {
                this.editButton.active = true;
                this.deleteButton.active = true;
            }
        }
    }

    public ServerStatusPinger getPinger() {
        return this.pinger;
    }

    public ServerList getServers() {
        return this.servers;
    }
}

