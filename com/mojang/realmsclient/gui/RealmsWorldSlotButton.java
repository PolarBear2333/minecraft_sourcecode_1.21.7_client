/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package com.mojang.realmsclient.gui;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.RealmsSlot;
import com.mojang.realmsclient.util.RealmsTextureManager;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;

public class RealmsWorldSlotButton
extends Button {
    private static final ResourceLocation SLOT_FRAME_SPRITE = ResourceLocation.withDefaultNamespace("widget/slot_frame");
    public static final ResourceLocation EMPTY_SLOT_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/realms/empty_frame.png");
    public static final ResourceLocation DEFAULT_WORLD_SLOT_1 = ResourceLocation.withDefaultNamespace("textures/gui/title/background/panorama_0.png");
    public static final ResourceLocation DEFAULT_WORLD_SLOT_2 = ResourceLocation.withDefaultNamespace("textures/gui/title/background/panorama_2.png");
    public static final ResourceLocation DEFAULT_WORLD_SLOT_3 = ResourceLocation.withDefaultNamespace("textures/gui/title/background/panorama_3.png");
    private static final Component SWITCH_TO_MINIGAME_SLOT_TOOLTIP = Component.translatable("mco.configure.world.slot.tooltip.minigame");
    private static final Component SWITCH_TO_WORLD_SLOT_TOOLTIP = Component.translatable("mco.configure.world.slot.tooltip");
    static final Component MINIGAME = Component.translatable("mco.worldSlot.minigame");
    private static final int WORLD_NAME_MAX_WIDTH = 64;
    private static final String DOTS = "...";
    private final int slotIndex;
    private State state;

    public RealmsWorldSlotButton(int n, int n2, int n3, int n4, int n5, RealmsServer realmsServer, Button.OnPress onPress) {
        super(n, n2, n3, n4, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
        this.slotIndex = n5;
        this.state = this.setServerData(realmsServer);
    }

    public State getState() {
        return this.state;
    }

    public State setServerData(RealmsServer realmsServer) {
        this.state = new State(realmsServer, this.slotIndex);
        this.setTooltipAndNarration(this.state, realmsServer.minigameName);
        return this.state;
    }

    private void setTooltipAndNarration(State state, @Nullable String string) {
        Component component;
        switch (state.action.ordinal()) {
            case 1: {
                Component component2;
                if (state.minigame) {
                    component2 = SWITCH_TO_MINIGAME_SLOT_TOOLTIP;
                    break;
                }
                component2 = SWITCH_TO_WORLD_SLOT_TOOLTIP;
                break;
            }
            default: {
                Component component2 = component = null;
            }
        }
        if (component != null) {
            this.setTooltip(Tooltip.create(component));
        }
        MutableComponent mutableComponent = Component.literal(state.slotName);
        if (state.minigame && string != null) {
            mutableComponent = mutableComponent.append(CommonComponents.SPACE).append(string);
        }
        this.setMessage(mutableComponent);
    }

    static Action getAction(RealmsServer realmsServer, boolean bl, boolean bl2) {
        if (!(bl2 || bl && realmsServer.expired)) {
            return Action.SWITCH_SLOT;
        }
        return Action.NOTHING;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int n, int n2, float f) {
        Object object;
        Font font;
        int n3 = this.getX();
        int n4 = this.getY();
        boolean bl = this.isHoveredOrFocused();
        ResourceLocation resourceLocation = this.state.minigame ? RealmsTextureManager.worldTemplate(String.valueOf(this.state.imageId), this.state.image) : (this.state.empty ? EMPTY_SLOT_LOCATION : (this.state.image != null && this.state.imageId != -1L ? RealmsTextureManager.worldTemplate(String.valueOf(this.state.imageId), this.state.image) : (this.slotIndex == 1 ? DEFAULT_WORLD_SLOT_1 : (this.slotIndex == 2 ? DEFAULT_WORLD_SLOT_2 : (this.slotIndex == 3 ? DEFAULT_WORLD_SLOT_3 : EMPTY_SLOT_LOCATION)))));
        int n5 = -1;
        if (!this.state.activeSlot) {
            n5 = ARGB.colorFromFloat(1.0f, 0.56f, 0.56f, 0.56f);
        }
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, resourceLocation, n3 + 1, n4 + 1, 0.0f, 0.0f, this.width - 2, this.height - 2, 74, 74, 74, 74, n5);
        if (bl && this.state.action != Action.NOTHING) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_FRAME_SPRITE, n3, n4, this.width, this.height);
        } else if (this.state.activeSlot) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_FRAME_SPRITE, n3, n4, this.width, this.height, ARGB.colorFromFloat(1.0f, 0.8f, 0.8f, 0.8f));
        } else {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_FRAME_SPRITE, n3, n4, this.width, this.height, ARGB.colorFromFloat(1.0f, 0.56f, 0.56f, 0.56f));
        }
        if (this.state.hardcore) {
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, RealmsMainScreen.HARDCORE_MODE_SPRITE, n3 + 3, n4 + 4, 9, 8);
        }
        if ((font = Minecraft.getInstance().font).width((String)(object = this.state.slotName)) > 64) {
            object = font.plainSubstrByWidth((String)object, 64 - font.width(DOTS)) + DOTS;
        }
        guiGraphics.drawCenteredString(font, (String)object, n3 + this.width / 2, n4 + this.height - 14, -1);
        if (this.state.activeSlot) {
            guiGraphics.drawCenteredString(font, RealmsMainScreen.getVersionComponent(this.state.slotVersion, this.state.compatibility.isCompatible()), n3 + this.width / 2, n4 + this.height + 2, -1);
        }
    }

    public static class State {
        final String slotName;
        final String slotVersion;
        final RealmsServer.Compatibility compatibility;
        final long imageId;
        @Nullable
        final String image;
        public final boolean empty;
        public final boolean minigame;
        public final Action action;
        public final boolean hardcore;
        public final boolean activeSlot;

        public State(RealmsServer realmsServer, int n) {
            boolean bl = this.minigame = n == 4;
            if (this.minigame) {
                this.slotName = MINIGAME.getString();
                this.imageId = realmsServer.minigameId;
                this.image = realmsServer.minigameImage;
                this.empty = realmsServer.minigameId == -1;
                this.slotVersion = "";
                this.compatibility = RealmsServer.Compatibility.UNVERIFIABLE;
                this.hardcore = false;
                this.activeSlot = realmsServer.isMinigameActive();
            } else {
                RealmsSlot realmsSlot = realmsServer.slots.get(n);
                this.slotName = realmsSlot.options.getSlotName(n);
                this.imageId = realmsSlot.options.templateId;
                this.image = realmsSlot.options.templateImage;
                this.empty = realmsSlot.options.empty;
                this.slotVersion = realmsSlot.options.version;
                this.compatibility = realmsSlot.options.compatibility;
                this.hardcore = realmsSlot.isHardcore();
                this.activeSlot = realmsServer.activeSlot == n && !realmsServer.isMinigameActive();
            }
            this.action = RealmsWorldSlotButton.getAction(realmsServer, this.minigame, this.activeSlot);
        }
    }

    public static enum Action {
        NOTHING,
        SWITCH_SLOT;

    }
}

