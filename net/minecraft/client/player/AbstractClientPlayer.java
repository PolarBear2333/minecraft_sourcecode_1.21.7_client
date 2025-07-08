/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  javax.annotation.Nullable
 */
package net.minecraft.client.player;

import com.mojang.authlib.GameProfile;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractClientPlayer
extends Player {
    @Nullable
    private PlayerInfo playerInfo;
    protected Vec3 deltaMovementOnPreviousTick = Vec3.ZERO;
    public float elytraRotX;
    public float elytraRotY;
    public float elytraRotZ;
    public final ClientLevel clientLevel;
    public float walkDistO;
    public float walkDist;

    public AbstractClientPlayer(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
        this.clientLevel = clientLevel;
    }

    @Override
    @Nullable
    public GameType gameMode() {
        PlayerInfo playerInfo = this.getPlayerInfo();
        return playerInfo != null ? playerInfo.getGameMode() : null;
    }

    @Nullable
    protected PlayerInfo getPlayerInfo() {
        if (this.playerInfo == null) {
            this.playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(this.getUUID());
        }
        return this.playerInfo;
    }

    @Override
    public void tick() {
        this.walkDistO = this.walkDist;
        this.deltaMovementOnPreviousTick = this.getDeltaMovement();
        super.tick();
    }

    public Vec3 getDeltaMovementLerped(float f) {
        return this.deltaMovementOnPreviousTick.lerp(this.getDeltaMovement(), f);
    }

    public PlayerSkin getSkin() {
        PlayerInfo playerInfo = this.getPlayerInfo();
        return playerInfo == null ? DefaultPlayerSkin.get(this.getUUID()) : playerInfo.getSkin();
    }

    public float getFieldOfViewModifier(boolean bl, float f) {
        float f2;
        float f3;
        float f4 = 1.0f;
        if (this.getAbilities().flying) {
            f4 *= 1.1f;
        }
        if ((f3 = this.getAbilities().getWalkingSpeed()) != 0.0f) {
            f2 = (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) / f3;
            f4 *= (f2 + 1.0f) / 2.0f;
        }
        if (this.isUsingItem()) {
            if (this.getUseItem().is(Items.BOW)) {
                f2 = Math.min((float)this.getTicksUsingItem() / 20.0f, 1.0f);
                f4 *= 1.0f - Mth.square(f2) * 0.15f;
            } else if (bl && this.isScoping()) {
                return 0.1f;
            }
        }
        return Mth.lerp(f, 1.0f, f4);
    }
}

