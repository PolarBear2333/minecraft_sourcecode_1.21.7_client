/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Strings
 *  com.google.common.collect.Lists
 *  com.mojang.authlib.GameProfile
 *  it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.social;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.screens.social.PlayerEntry;
import net.minecraft.client.gui.screens.social.SocialInteractionsScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.multiplayer.chat.ChatLog;
import net.minecraft.client.multiplayer.chat.LoggedChatEvent;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;

public class SocialInteractionsPlayerList
extends ContainerObjectSelectionList<PlayerEntry> {
    private final SocialInteractionsScreen socialInteractionsScreen;
    private final List<PlayerEntry> players = Lists.newArrayList();
    @Nullable
    private String filter;

    public SocialInteractionsPlayerList(SocialInteractionsScreen socialInteractionsScreen, Minecraft minecraft, int n, int n2, int n3, int n4) {
        super(minecraft, n, n2, n3, n4);
        this.socialInteractionsScreen = socialInteractionsScreen;
    }

    @Override
    protected void renderListBackground(GuiGraphics guiGraphics) {
    }

    @Override
    protected void renderListSeparators(GuiGraphics guiGraphics) {
    }

    @Override
    protected void enableScissor(GuiGraphics guiGraphics) {
        guiGraphics.enableScissor(this.getX(), this.getY() + 4, this.getRight(), this.getBottom());
    }

    public void updatePlayerList(Collection<UUID> collection, double d, boolean bl) {
        HashMap<UUID, PlayerEntry> hashMap = new HashMap<UUID, PlayerEntry>();
        this.addOnlinePlayers(collection, hashMap);
        this.updatePlayersFromChatLog(hashMap, bl);
        this.updateFiltersAndScroll(hashMap.values(), d);
    }

    private void addOnlinePlayers(Collection<UUID> collection, Map<UUID, PlayerEntry> map) {
        ClientPacketListener clientPacketListener = this.minecraft.player.connection;
        for (UUID uUID : collection) {
            PlayerInfo playerInfo = clientPacketListener.getPlayerInfo(uUID);
            if (playerInfo == null) continue;
            boolean bl = playerInfo.hasVerifiableChat();
            map.put(uUID, new PlayerEntry(this.minecraft, this.socialInteractionsScreen, uUID, playerInfo.getProfile().getName(), playerInfo::getSkin, bl));
        }
    }

    private void updatePlayersFromChatLog(Map<UUID, PlayerEntry> map, boolean bl) {
        Collection<GameProfile> collection = SocialInteractionsPlayerList.collectProfilesFromChatLog(this.minecraft.getReportingContext().chatLog());
        for (GameProfile gameProfile : collection) {
            PlayerEntry playerEntry;
            if (bl) {
                playerEntry = map.computeIfAbsent(gameProfile.getId(), uUID -> {
                    PlayerEntry playerEntry = new PlayerEntry(this.minecraft, this.socialInteractionsScreen, gameProfile.getId(), gameProfile.getName(), this.minecraft.getSkinManager().lookupInsecure(gameProfile), true);
                    playerEntry.setRemoved(true);
                    return playerEntry;
                });
            } else {
                playerEntry = map.get(gameProfile.getId());
                if (playerEntry == null) continue;
            }
            playerEntry.setHasRecentMessages(true);
        }
    }

    private static Collection<GameProfile> collectProfilesFromChatLog(ChatLog chatLog) {
        ObjectLinkedOpenHashSet objectLinkedOpenHashSet = new ObjectLinkedOpenHashSet();
        for (int i = chatLog.end(); i >= chatLog.start(); --i) {
            LoggedChatMessage.Player player;
            LoggedChatEvent loggedChatEvent = chatLog.lookup(i);
            if (!(loggedChatEvent instanceof LoggedChatMessage.Player) || !(player = (LoggedChatMessage.Player)loggedChatEvent).message().hasSignature()) continue;
            objectLinkedOpenHashSet.add(player.profile());
        }
        return objectLinkedOpenHashSet;
    }

    private void sortPlayerEntries() {
        this.players.sort(Comparator.comparing(playerEntry -> {
            if (this.minecraft.isLocalPlayer(playerEntry.getPlayerId())) {
                return 0;
            }
            if (this.minecraft.getReportingContext().hasDraftReportFor(playerEntry.getPlayerId())) {
                return 1;
            }
            if (playerEntry.getPlayerId().version() == 2) {
                return 4;
            }
            if (playerEntry.hasRecentMessages()) {
                return 2;
            }
            return 3;
        }).thenComparing(playerEntry -> {
            int n;
            if (!playerEntry.getPlayerName().isBlank() && ((n = playerEntry.getPlayerName().codePointAt(0)) == 95 || n >= 97 && n <= 122 || n >= 65 && n <= 90 || n >= 48 && n <= 57)) {
                return 0;
            }
            return 1;
        }).thenComparing(PlayerEntry::getPlayerName, String::compareToIgnoreCase));
    }

    private void updateFiltersAndScroll(Collection<PlayerEntry> collection, double d) {
        this.players.clear();
        this.players.addAll(collection);
        this.sortPlayerEntries();
        this.updateFilteredPlayers();
        this.replaceEntries(this.players);
        this.setScrollAmount(d);
    }

    private void updateFilteredPlayers() {
        if (this.filter != null) {
            this.players.removeIf(playerEntry -> !playerEntry.getPlayerName().toLowerCase(Locale.ROOT).contains(this.filter));
            this.replaceEntries(this.players);
        }
    }

    public void setFilter(String string) {
        this.filter = string;
    }

    public boolean isEmpty() {
        return this.players.isEmpty();
    }

    public void addPlayer(PlayerInfo playerInfo, SocialInteractionsScreen.Page page) {
        UUID uUID = playerInfo.getProfile().getId();
        for (PlayerEntry playerEntry : this.players) {
            if (!playerEntry.getPlayerId().equals(uUID)) continue;
            playerEntry.setRemoved(false);
            return;
        }
        if ((page == SocialInteractionsScreen.Page.ALL || this.minecraft.getPlayerSocialManager().shouldHideMessageFrom(uUID)) && (Strings.isNullOrEmpty((String)this.filter) || playerInfo.getProfile().getName().toLowerCase(Locale.ROOT).contains(this.filter))) {
            PlayerEntry playerEntry;
            boolean bl = playerInfo.hasVerifiableChat();
            playerEntry = new PlayerEntry(this.minecraft, this.socialInteractionsScreen, playerInfo.getProfile().getId(), playerInfo.getProfile().getName(), playerInfo::getSkin, bl);
            this.addEntry(playerEntry);
            this.players.add(playerEntry);
        }
    }

    public void removePlayer(UUID uUID) {
        for (PlayerEntry playerEntry : this.players) {
            if (!playerEntry.getPlayerId().equals(uUID)) continue;
            playerEntry.setRemoved(true);
            return;
        }
    }

    public void refreshHasDraftReport() {
        this.players.forEach(playerEntry -> playerEntry.refreshHasDraftReport(this.minecraft.getReportingContext()));
    }
}

