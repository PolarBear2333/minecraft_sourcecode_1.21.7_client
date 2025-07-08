/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ComparisonChain
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.gson.annotations.JsonAdapter
 *  com.google.gson.annotations.SerializedName
 *  com.mojang.logging.LogUtils
 *  com.mojang.util.UUIDTypeAdapter
 *  javax.annotation.Nullable
 *  org.apache.commons.lang3.builder.EqualsBuilder
 *  org.slf4j.Logger
 */
package com.mojang.realmsclient.dto;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.dto.Exclude;
import com.mojang.realmsclient.dto.GuardedSerializer;
import com.mojang.realmsclient.dto.PlayerInfo;
import com.mojang.realmsclient.dto.RealmsSlot;
import com.mojang.realmsclient.dto.ReflectionBasedSerialization;
import com.mojang.realmsclient.dto.RegionSelectionPreferenceDto;
import com.mojang.realmsclient.dto.ValueObject;
import com.mojang.util.UUIDTypeAdapter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.slf4j.Logger;

public class RealmsServer
extends ValueObject
implements ReflectionBasedSerialization {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int NO_VALUE = -1;
    public static final Component WORLD_CLOSED_COMPONENT = Component.translatable("mco.play.button.realm.closed");
    @SerializedName(value="id")
    public long id = -1L;
    @Nullable
    @SerializedName(value="remoteSubscriptionId")
    public String remoteSubscriptionId;
    @Nullable
    @SerializedName(value="name")
    public String name;
    @SerializedName(value="motd")
    public String motd = "";
    @SerializedName(value="state")
    public State state = State.CLOSED;
    @Nullable
    @SerializedName(value="owner")
    public String owner;
    @SerializedName(value="ownerUUID")
    @JsonAdapter(value=UUIDTypeAdapter.class)
    public UUID ownerUUID = Util.NIL_UUID;
    @SerializedName(value="players")
    public List<PlayerInfo> players = Lists.newArrayList();
    @SerializedName(value="slots")
    private List<RealmsSlot> slotList = RealmsServer.createEmptySlots();
    @Exclude
    public Map<Integer, RealmsSlot> slots = new HashMap<Integer, RealmsSlot>();
    @SerializedName(value="expired")
    public boolean expired;
    @SerializedName(value="expiredTrial")
    public boolean expiredTrial = false;
    @SerializedName(value="daysLeft")
    public int daysLeft;
    @SerializedName(value="worldType")
    public WorldType worldType = WorldType.NORMAL;
    @SerializedName(value="isHardcore")
    public boolean isHardcore = false;
    @SerializedName(value="gameMode")
    public int gameMode = -1;
    @SerializedName(value="activeSlot")
    public int activeSlot = -1;
    @Nullable
    @SerializedName(value="minigameName")
    public String minigameName;
    @SerializedName(value="minigameId")
    public int minigameId = -1;
    @Nullable
    @SerializedName(value="minigameImage")
    public String minigameImage;
    @SerializedName(value="parentWorldId")
    public long parentRealmId = -1L;
    @Nullable
    @SerializedName(value="parentWorldName")
    public String parentWorldName;
    @SerializedName(value="activeVersion")
    public String activeVersion = "";
    @SerializedName(value="compatibility")
    public Compatibility compatibility = Compatibility.UNVERIFIABLE;
    @Nullable
    @SerializedName(value="regionSelectionPreference")
    public RegionSelectionPreferenceDto regionSelectionPreference;

    public String getDescription() {
        return this.motd;
    }

    @Nullable
    public String getName() {
        return this.name;
    }

    @Nullable
    public String getMinigameName() {
        return this.minigameName;
    }

    public void setName(String string) {
        this.name = string;
    }

    public void setDescription(String string) {
        this.motd = string;
    }

    public static RealmsServer parse(GuardedSerializer guardedSerializer, String string) {
        try {
            RealmsServer realmsServer = guardedSerializer.fromJson(string, RealmsServer.class);
            if (realmsServer == null) {
                LOGGER.error("Could not parse McoServer: {}", (Object)string);
                return new RealmsServer();
            }
            RealmsServer.finalize(realmsServer);
            return realmsServer;
        }
        catch (Exception exception) {
            LOGGER.error("Could not parse McoServer: {}", (Object)exception.getMessage());
            return new RealmsServer();
        }
    }

    public static void finalize(RealmsServer realmsServer) {
        if (realmsServer.players == null) {
            realmsServer.players = Lists.newArrayList();
        }
        if (realmsServer.slotList == null) {
            realmsServer.slotList = RealmsServer.createEmptySlots();
        }
        if (realmsServer.slots == null) {
            realmsServer.slots = new HashMap<Integer, RealmsSlot>();
        }
        if (realmsServer.worldType == null) {
            realmsServer.worldType = WorldType.NORMAL;
        }
        if (realmsServer.activeVersion == null) {
            realmsServer.activeVersion = "";
        }
        if (realmsServer.compatibility == null) {
            realmsServer.compatibility = Compatibility.UNVERIFIABLE;
        }
        if (realmsServer.regionSelectionPreference == null) {
            realmsServer.regionSelectionPreference = RegionSelectionPreferenceDto.DEFAULT;
        }
        RealmsServer.sortInvited(realmsServer);
        RealmsServer.finalizeSlots(realmsServer);
    }

    private static void sortInvited(RealmsServer realmsServer) {
        realmsServer.players.sort((playerInfo, playerInfo2) -> ComparisonChain.start().compareFalseFirst(playerInfo2.getAccepted(), playerInfo.getAccepted()).compare((Comparable)((Object)playerInfo.getName().toLowerCase(Locale.ROOT)), (Comparable)((Object)playerInfo2.getName().toLowerCase(Locale.ROOT))).result());
    }

    private static void finalizeSlots(RealmsServer realmsServer) {
        realmsServer.slotList.forEach(realmsSlot -> realmsServer.slots.put(realmsSlot.slotId, (RealmsSlot)realmsSlot));
        for (int i = 1; i <= 3; ++i) {
            if (realmsServer.slots.containsKey(i)) continue;
            realmsServer.slots.put(i, RealmsSlot.defaults(i));
        }
    }

    private static List<RealmsSlot> createEmptySlots() {
        ArrayList<RealmsSlot> arrayList = new ArrayList<RealmsSlot>();
        arrayList.add(RealmsSlot.defaults(1));
        arrayList.add(RealmsSlot.defaults(2));
        arrayList.add(RealmsSlot.defaults(3));
        return arrayList;
    }

    public boolean isCompatible() {
        return this.compatibility.isCompatible();
    }

    public boolean needsUpgrade() {
        return this.compatibility.needsUpgrade();
    }

    public boolean needsDowngrade() {
        return this.compatibility.needsDowngrade();
    }

    public boolean shouldPlayButtonBeActive() {
        boolean bl = !this.expired && this.state == State.OPEN;
        return bl && (this.isCompatible() || this.needsUpgrade() || this.isSelfOwnedServer());
    }

    private boolean isSelfOwnedServer() {
        return Minecraft.getInstance().isLocalPlayer(this.ownerUUID);
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.id, this.name, this.motd, this.state, this.owner, this.expired});
    }

    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (object == this) {
            return true;
        }
        if (object.getClass() != this.getClass()) {
            return false;
        }
        RealmsServer realmsServer = (RealmsServer)object;
        return new EqualsBuilder().append(this.id, realmsServer.id).append((Object)this.name, (Object)realmsServer.name).append((Object)this.motd, (Object)realmsServer.motd).append((Object)this.state, (Object)realmsServer.state).append((Object)this.owner, (Object)realmsServer.owner).append(this.expired, realmsServer.expired).append((Object)this.worldType, (Object)this.worldType).isEquals();
    }

    public RealmsServer clone() {
        RealmsServer realmsServer = new RealmsServer();
        realmsServer.id = this.id;
        realmsServer.remoteSubscriptionId = this.remoteSubscriptionId;
        realmsServer.name = this.name;
        realmsServer.motd = this.motd;
        realmsServer.state = this.state;
        realmsServer.owner = this.owner;
        realmsServer.players = this.players;
        realmsServer.slotList = this.slotList.stream().map(RealmsSlot::clone).toList();
        realmsServer.slots = this.cloneSlots(this.slots);
        realmsServer.expired = this.expired;
        realmsServer.expiredTrial = this.expiredTrial;
        realmsServer.daysLeft = this.daysLeft;
        realmsServer.worldType = this.worldType;
        realmsServer.isHardcore = this.isHardcore;
        realmsServer.gameMode = this.gameMode;
        realmsServer.ownerUUID = this.ownerUUID;
        realmsServer.minigameName = this.minigameName;
        realmsServer.activeSlot = this.activeSlot;
        realmsServer.minigameId = this.minigameId;
        realmsServer.minigameImage = this.minigameImage;
        realmsServer.parentWorldName = this.parentWorldName;
        realmsServer.parentRealmId = this.parentRealmId;
        realmsServer.activeVersion = this.activeVersion;
        realmsServer.compatibility = this.compatibility;
        realmsServer.regionSelectionPreference = this.regionSelectionPreference != null ? this.regionSelectionPreference.clone() : null;
        return realmsServer;
    }

    public Map<Integer, RealmsSlot> cloneSlots(Map<Integer, RealmsSlot> map) {
        HashMap hashMap = Maps.newHashMap();
        for (Map.Entry<Integer, RealmsSlot> entry : map.entrySet()) {
            hashMap.put(entry.getKey(), new RealmsSlot(entry.getKey(), entry.getValue().options.clone(), entry.getValue().settings));
        }
        return hashMap;
    }

    public boolean isSnapshotRealm() {
        return this.parentRealmId != -1L;
    }

    public boolean isMinigameActive() {
        return this.worldType == WorldType.MINIGAME;
    }

    public String getWorldName(int n) {
        if (this.name == null) {
            return this.slots.get((Object)Integer.valueOf((int)n)).options.getSlotName(n);
        }
        return this.name + " (" + this.slots.get((Object)Integer.valueOf((int)n)).options.getSlotName(n) + ")";
    }

    public ServerData toServerData(String string) {
        return new ServerData(Objects.requireNonNullElse(this.name, "unknown server"), string, ServerData.Type.REALM);
    }

    public /* synthetic */ Object clone() throws CloneNotSupportedException {
        return this.clone();
    }

    public static enum State {
        CLOSED,
        OPEN,
        UNINITIALIZED;

    }

    public static enum WorldType {
        NORMAL,
        MINIGAME,
        ADVENTUREMAP,
        EXPERIENCE,
        INSPIRATION;

    }

    public static enum Compatibility {
        UNVERIFIABLE,
        INCOMPATIBLE,
        RELEASE_TYPE_INCOMPATIBLE,
        NEEDS_DOWNGRADE,
        NEEDS_UPGRADE,
        COMPATIBLE;


        public boolean isCompatible() {
            return this == COMPATIBLE;
        }

        public boolean needsUpgrade() {
            return this == NEEDS_UPGRADE;
        }

        public boolean needsDowngrade() {
            return this == NEEDS_DOWNGRADE;
        }
    }

    public static class McoServerComparator
    implements Comparator<RealmsServer> {
        private final String refOwner;

        public McoServerComparator(String string) {
            this.refOwner = string;
        }

        @Override
        public int compare(RealmsServer realmsServer, RealmsServer realmsServer2) {
            return ComparisonChain.start().compareTrueFirst(realmsServer.isSnapshotRealm(), realmsServer2.isSnapshotRealm()).compareTrueFirst(realmsServer.state == State.UNINITIALIZED, realmsServer2.state == State.UNINITIALIZED).compareTrueFirst(realmsServer.expiredTrial, realmsServer2.expiredTrial).compareTrueFirst(Objects.equals(realmsServer.owner, this.refOwner), Objects.equals(realmsServer2.owner, this.refOwner)).compareFalseFirst(realmsServer.expired, realmsServer2.expired).compareTrueFirst(realmsServer.state == State.OPEN, realmsServer2.state == State.OPEN).compare(realmsServer.id, realmsServer2.id).result();
        }

        @Override
        public /* synthetic */ int compare(Object object, Object object2) {
            return this.compare((RealmsServer)object, (RealmsServer)object2);
        }
    }
}

