/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.yggdrasil.ProfileResult
 */
package net.minecraft.client.gui.screens.inventory.tooltip;

import com.mojang.authlib.yggdrasil.ProfileResult;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public class ClientActivePlayersTooltip
implements ClientTooltipComponent {
    private static final int SKIN_SIZE = 10;
    private static final int PADDING = 2;
    private final List<ProfileResult> activePlayers;

    public ClientActivePlayersTooltip(ActivePlayersTooltip activePlayersTooltip) {
        this.activePlayers = activePlayersTooltip.profiles();
    }

    @Override
    public int getHeight(Font font) {
        return this.activePlayers.size() * 12 + 2;
    }

    @Override
    public int getWidth(Font font) {
        int n = 0;
        for (ProfileResult profileResult : this.activePlayers) {
            int n2 = font.width(profileResult.profile().getName());
            if (n2 <= n) continue;
            n = n2;
        }
        return n + 10 + 6;
    }

    @Override
    public void renderImage(Font font, int n, int n2, int n3, int n4, GuiGraphics guiGraphics) {
        for (int i = 0; i < this.activePlayers.size(); ++i) {
            ProfileResult profileResult = this.activePlayers.get(i);
            int n5 = n2 + 2 + i * 12;
            PlayerFaceRenderer.draw(guiGraphics, Minecraft.getInstance().getSkinManager().getInsecureSkin(profileResult.profile()), n + 2, n5, 10);
            guiGraphics.drawString(font, profileResult.profile().getName(), n + 10 + 4, n5 + 2, -1);
        }
    }

    public record ActivePlayersTooltip(List<ProfileResult> profiles) implements TooltipComponent
    {
    }
}

