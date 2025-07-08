/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.server.bossevents;

import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;

public class CustomBossEvent
extends ServerBossEvent {
    private static final int DEFAULT_MAX = 100;
    private final ResourceLocation id;
    private final Set<UUID> players = Sets.newHashSet();
    private int value;
    private int max = 100;

    public CustomBossEvent(ResourceLocation resourceLocation, Component component) {
        super(component, BossEvent.BossBarColor.WHITE, BossEvent.BossBarOverlay.PROGRESS);
        this.id = resourceLocation;
        this.setProgress(0.0f);
    }

    public ResourceLocation getTextId() {
        return this.id;
    }

    @Override
    public void addPlayer(ServerPlayer serverPlayer) {
        super.addPlayer(serverPlayer);
        this.players.add(serverPlayer.getUUID());
    }

    public void addOfflinePlayer(UUID uUID) {
        this.players.add(uUID);
    }

    @Override
    public void removePlayer(ServerPlayer serverPlayer) {
        super.removePlayer(serverPlayer);
        this.players.remove(serverPlayer.getUUID());
    }

    @Override
    public void removeAllPlayers() {
        super.removeAllPlayers();
        this.players.clear();
    }

    public int getValue() {
        return this.value;
    }

    public int getMax() {
        return this.max;
    }

    public void setValue(int n) {
        this.value = n;
        this.setProgress(Mth.clamp((float)n / (float)this.max, 0.0f, 1.0f));
    }

    public void setMax(int n) {
        this.max = n;
        this.setProgress(Mth.clamp((float)this.value / (float)n, 0.0f, 1.0f));
    }

    public final Component getDisplayName() {
        return ComponentUtils.wrapInSquareBrackets(this.getName()).withStyle(style -> style.withColor(this.getColor().getFormatting()).withHoverEvent(new HoverEvent.ShowText(Component.literal(this.getTextId().toString()))).withInsertion(this.getTextId().toString()));
    }

    public boolean setPlayers(Collection<ServerPlayer> collection) {
        boolean bl;
        HashSet hashSet = Sets.newHashSet();
        HashSet hashSet2 = Sets.newHashSet();
        for (UUID object : this.players) {
            bl = false;
            for (ServerPlayer serverPlayer : collection) {
                if (!serverPlayer.getUUID().equals(object)) continue;
                bl = true;
                break;
            }
            if (bl) continue;
            hashSet.add(object);
        }
        for (ServerPlayer serverPlayer : collection) {
            bl = false;
            for (UUID uUID : this.players) {
                if (!serverPlayer.getUUID().equals(uUID)) continue;
                bl = true;
                break;
            }
            if (bl) continue;
            hashSet2.add(serverPlayer);
        }
        for (UUID uUID : hashSet) {
            for (ServerPlayer serverPlayer : this.getPlayers()) {
                if (!serverPlayer.getUUID().equals(uUID)) continue;
                this.removePlayer(serverPlayer);
                break;
            }
            this.players.remove(uUID);
        }
        for (ServerPlayer serverPlayer : hashSet2) {
            this.addPlayer(serverPlayer);
        }
        return !hashSet.isEmpty() || !hashSet2.isEmpty();
    }

    public static CustomBossEvent load(ResourceLocation resourceLocation, Packed packed) {
        CustomBossEvent customBossEvent = new CustomBossEvent(resourceLocation, packed.name);
        customBossEvent.setVisible(packed.visible);
        customBossEvent.setValue(packed.value);
        customBossEvent.setMax(packed.max);
        customBossEvent.setColor(packed.color);
        customBossEvent.setOverlay(packed.overlay);
        customBossEvent.setDarkenScreen(packed.darkenScreen);
        customBossEvent.setPlayBossMusic(packed.playBossMusic);
        customBossEvent.setCreateWorldFog(packed.createWorldFog);
        packed.players.forEach(customBossEvent::addOfflinePlayer);
        return customBossEvent;
    }

    public Packed pack() {
        return new Packed(this.getName(), this.isVisible(), this.getValue(), this.getMax(), this.getColor(), this.getOverlay(), this.shouldDarkenScreen(), this.shouldPlayBossMusic(), this.shouldCreateWorldFog(), Set.copyOf(this.players));
    }

    public void onPlayerConnect(ServerPlayer serverPlayer) {
        if (this.players.contains(serverPlayer.getUUID())) {
            this.addPlayer(serverPlayer);
        }
    }

    public void onPlayerDisconnect(ServerPlayer serverPlayer) {
        super.removePlayer(serverPlayer);
    }

    public static final class Packed
    extends Record {
        final Component name;
        final boolean visible;
        final int value;
        final int max;
        final BossEvent.BossBarColor color;
        final BossEvent.BossBarOverlay overlay;
        final boolean darkenScreen;
        final boolean playBossMusic;
        final boolean createWorldFog;
        final Set<UUID> players;
        public static final Codec<Packed> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)ComponentSerialization.CODEC.fieldOf("Name").forGetter(Packed::name), (App)Codec.BOOL.optionalFieldOf("Visible", (Object)false).forGetter(Packed::visible), (App)Codec.INT.optionalFieldOf("Value", (Object)0).forGetter(Packed::value), (App)Codec.INT.optionalFieldOf("Max", (Object)100).forGetter(Packed::max), (App)BossEvent.BossBarColor.CODEC.optionalFieldOf("Color", (Object)BossEvent.BossBarColor.WHITE).forGetter(Packed::color), (App)BossEvent.BossBarOverlay.CODEC.optionalFieldOf("Overlay", (Object)BossEvent.BossBarOverlay.PROGRESS).forGetter(Packed::overlay), (App)Codec.BOOL.optionalFieldOf("DarkenScreen", (Object)false).forGetter(Packed::darkenScreen), (App)Codec.BOOL.optionalFieldOf("PlayBossMusic", (Object)false).forGetter(Packed::playBossMusic), (App)Codec.BOOL.optionalFieldOf("CreateWorldFog", (Object)false).forGetter(Packed::createWorldFog), (App)UUIDUtil.CODEC_SET.optionalFieldOf("Players", Set.of()).forGetter(Packed::players)).apply((Applicative)instance, Packed::new));

        public Packed(Component component, boolean bl, int n, int n2, BossEvent.BossBarColor bossBarColor, BossEvent.BossBarOverlay bossBarOverlay, boolean bl2, boolean bl3, boolean bl4, Set<UUID> set) {
            this.name = component;
            this.visible = bl;
            this.value = n;
            this.max = n2;
            this.color = bossBarColor;
            this.overlay = bossBarOverlay;
            this.darkenScreen = bl2;
            this.playBossMusic = bl3;
            this.createWorldFog = bl4;
            this.players = set;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Packed.class, "name;visible;value;max;color;overlay;darkenScreen;playBossMusic;createWorldFog;players", "name", "visible", "value", "max", "color", "overlay", "darkenScreen", "playBossMusic", "createWorldFog", "players"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Packed.class, "name;visible;value;max;color;overlay;darkenScreen;playBossMusic;createWorldFog;players", "name", "visible", "value", "max", "color", "overlay", "darkenScreen", "playBossMusic", "createWorldFog", "players"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Packed.class, "name;visible;value;max;color;overlay;darkenScreen;playBossMusic;createWorldFog;players", "name", "visible", "value", "max", "color", "overlay", "darkenScreen", "playBossMusic", "createWorldFog", "players"}, this, object);
        }

        public Component name() {
            return this.name;
        }

        public boolean visible() {
            return this.visible;
        }

        public int value() {
            return this.value;
        }

        public int max() {
            return this.max;
        }

        public BossEvent.BossBarColor color() {
            return this.color;
        }

        public BossEvent.BossBarOverlay overlay() {
            return this.overlay;
        }

        public boolean darkenScreen() {
            return this.darkenScreen;
        }

        public boolean playBossMusic() {
            return this.playBossMusic;
        }

        public boolean createWorldFog() {
            return this.createWorldFog;
        }

        public Set<UUID> players() {
            return this.players;
        }
    }
}

