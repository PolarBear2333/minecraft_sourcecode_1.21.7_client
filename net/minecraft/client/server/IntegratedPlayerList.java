/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.server;

import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.net.SocketAddress;
import javax.annotation.Nullable;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.PlayerDataStorage;
import net.minecraft.world.level.storage.TagValueOutput;
import org.slf4j.Logger;

public class IntegratedPlayerList
extends PlayerList {
    private static final Logger LOGGER = LogUtils.getLogger();
    @Nullable
    private CompoundTag playerData;

    public IntegratedPlayerList(IntegratedServer integratedServer, LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, PlayerDataStorage playerDataStorage) {
        super(integratedServer, layeredRegistryAccess, playerDataStorage, 8);
        this.setViewDistance(10);
    }

    @Override
    protected void save(ServerPlayer serverPlayer) {
        if (this.getServer().isSingleplayerOwner(serverPlayer.getGameProfile())) {
            try (ProblemReporter.ScopedCollector scopedCollector = new ProblemReporter.ScopedCollector(serverPlayer.problemPath(), LOGGER);){
                TagValueOutput tagValueOutput = TagValueOutput.createWithContext(scopedCollector, serverPlayer.registryAccess());
                serverPlayer.saveWithoutId(tagValueOutput);
                this.playerData = tagValueOutput.buildResult();
            }
        }
        super.save(serverPlayer);
    }

    @Override
    public Component canPlayerLogin(SocketAddress socketAddress, GameProfile gameProfile) {
        if (this.getServer().isSingleplayerOwner(gameProfile) && this.getPlayerByName(gameProfile.getName()) != null) {
            return Component.translatable("multiplayer.disconnect.name_taken");
        }
        return super.canPlayerLogin(socketAddress, gameProfile);
    }

    @Override
    public IntegratedServer getServer() {
        return (IntegratedServer)super.getServer();
    }

    @Override
    @Nullable
    public CompoundTag getSingleplayerData() {
        return this.playerData;
    }

    @Override
    public /* synthetic */ MinecraftServer getServer() {
        return this.getServer();
    }
}

