/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Supplier
 *  com.google.common.base.Suppliers
 *  com.mojang.authlib.GameProfile
 *  javax.annotation.Nullable
 */
package net.minecraft.client.multiplayer;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.authlib.GameProfile;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.RemoteChatSession;
import net.minecraft.network.chat.SignedMessageValidator;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.PlayerTeam;

public class PlayerInfo {
    private final GameProfile profile;
    private final java.util.function.Supplier<PlayerSkin> skinLookup;
    private GameType gameMode = GameType.DEFAULT_MODE;
    private int latency;
    @Nullable
    private Component tabListDisplayName;
    private boolean showHat = true;
    @Nullable
    private RemoteChatSession chatSession;
    private SignedMessageValidator messageValidator;
    private int tabListOrder;

    public PlayerInfo(GameProfile gameProfile, boolean bl) {
        this.profile = gameProfile;
        this.messageValidator = PlayerInfo.fallbackMessageValidator(bl);
        Supplier supplier = Suppliers.memoize(() -> PlayerInfo.createSkinLookup(gameProfile));
        this.skinLookup = () -> PlayerInfo.lambda$new$1((java.util.function.Supplier)supplier);
    }

    private static java.util.function.Supplier<PlayerSkin> createSkinLookup(GameProfile gameProfile) {
        Minecraft minecraft = Minecraft.getInstance();
        SkinManager skinManager = minecraft.getSkinManager();
        CompletableFuture<Optional<PlayerSkin>> completableFuture = skinManager.getOrLoad(gameProfile);
        boolean bl = !minecraft.isLocalPlayer(gameProfile.getId());
        PlayerSkin playerSkin = DefaultPlayerSkin.get(gameProfile);
        return () -> {
            PlayerSkin playerSkin2 = completableFuture.getNow(Optional.empty()).orElse(playerSkin);
            if (bl && !playerSkin2.secure()) {
                return playerSkin;
            }
            return playerSkin2;
        };
    }

    public GameProfile getProfile() {
        return this.profile;
    }

    @Nullable
    public RemoteChatSession getChatSession() {
        return this.chatSession;
    }

    public SignedMessageValidator getMessageValidator() {
        return this.messageValidator;
    }

    public boolean hasVerifiableChat() {
        return this.chatSession != null;
    }

    protected void setChatSession(RemoteChatSession remoteChatSession) {
        this.chatSession = remoteChatSession;
        this.messageValidator = remoteChatSession.createMessageValidator(ProfilePublicKey.EXPIRY_GRACE_PERIOD);
    }

    protected void clearChatSession(boolean bl) {
        this.chatSession = null;
        this.messageValidator = PlayerInfo.fallbackMessageValidator(bl);
    }

    private static SignedMessageValidator fallbackMessageValidator(boolean bl) {
        return bl ? SignedMessageValidator.REJECT_ALL : SignedMessageValidator.ACCEPT_UNSIGNED;
    }

    public GameType getGameMode() {
        return this.gameMode;
    }

    protected void setGameMode(GameType gameType) {
        this.gameMode = gameType;
    }

    public int getLatency() {
        return this.latency;
    }

    protected void setLatency(int n) {
        this.latency = n;
    }

    public PlayerSkin getSkin() {
        return this.skinLookup.get();
    }

    @Nullable
    public PlayerTeam getTeam() {
        return Minecraft.getInstance().level.getScoreboard().getPlayersTeam(this.getProfile().getName());
    }

    public void setTabListDisplayName(@Nullable Component component) {
        this.tabListDisplayName = component;
    }

    @Nullable
    public Component getTabListDisplayName() {
        return this.tabListDisplayName;
    }

    public void setShowHat(boolean bl) {
        this.showHat = bl;
    }

    public boolean showHat() {
        return this.showHat;
    }

    public void setTabListOrder(int n) {
        this.tabListOrder = n;
    }

    public int getTabListOrder() {
        return this.tabListOrder;
    }

    private static /* synthetic */ PlayerSkin lambda$createSkinLookup$2(PlayerSkin playerSkin) {
        return playerSkin;
    }

    private static /* synthetic */ PlayerSkin lambda$new$1(java.util.function.Supplier supplier) {
        return (PlayerSkin)((java.util.function.Supplier)supplier.get()).get();
    }
}

