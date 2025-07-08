/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 */
package net.minecraft.client.gui.spectator.categories;

import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.SpectatorMenuCategory;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;
import net.minecraft.client.gui.spectator.categories.TeleportToPlayerMenuCategory;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

public class TeleportToTeamMenuCategory
implements SpectatorMenuCategory,
SpectatorMenuItem {
    private static final ResourceLocation TELEPORT_TO_TEAM_SPRITE = ResourceLocation.withDefaultNamespace("spectator/teleport_to_team");
    private static final Component TELEPORT_TEXT = Component.translatable("spectatorMenu.team_teleport");
    private static final Component TELEPORT_PROMPT = Component.translatable("spectatorMenu.team_teleport.prompt");
    private final List<SpectatorMenuItem> items;

    public TeleportToTeamMenuCategory() {
        Minecraft minecraft = Minecraft.getInstance();
        this.items = TeleportToTeamMenuCategory.createTeamEntries(minecraft, minecraft.level.getScoreboard());
    }

    private static List<SpectatorMenuItem> createTeamEntries(Minecraft minecraft, Scoreboard scoreboard) {
        return scoreboard.getPlayerTeams().stream().flatMap(playerTeam -> TeamSelectionItem.create(minecraft, playerTeam).stream()).toList();
    }

    @Override
    public List<SpectatorMenuItem> getItems() {
        return this.items;
    }

    @Override
    public Component getPrompt() {
        return TELEPORT_PROMPT;
    }

    @Override
    public void selectItem(SpectatorMenu spectatorMenu) {
        spectatorMenu.selectCategory(this);
    }

    @Override
    public Component getName() {
        return TELEPORT_TEXT;
    }

    @Override
    public void renderIcon(GuiGraphics guiGraphics, float f, float f2) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, TELEPORT_TO_TEAM_SPRITE, 0, 0, 16, 16, ARGB.colorFromFloat(f2, f, f, f));
    }

    @Override
    public boolean isEnabled() {
        return !this.items.isEmpty();
    }

    static class TeamSelectionItem
    implements SpectatorMenuItem {
        private final PlayerTeam team;
        private final Supplier<PlayerSkin> iconSkin;
        private final List<PlayerInfo> players;

        private TeamSelectionItem(PlayerTeam playerTeam, List<PlayerInfo> list, Supplier<PlayerSkin> supplier) {
            this.team = playerTeam;
            this.players = list;
            this.iconSkin = supplier;
        }

        public static Optional<SpectatorMenuItem> create(Minecraft minecraft, PlayerTeam playerTeam) {
            ArrayList<PlayerInfo> arrayList = new ArrayList<PlayerInfo>();
            for (String object2 : playerTeam.getPlayers()) {
                PlayerInfo playerInfo = minecraft.getConnection().getPlayerInfo(object2);
                if (playerInfo == null || playerInfo.getGameMode() == GameType.SPECTATOR) continue;
                arrayList.add(playerInfo);
            }
            if (arrayList.isEmpty()) {
                return Optional.empty();
            }
            GameProfile gameProfile = ((PlayerInfo)arrayList.get(RandomSource.create().nextInt(arrayList.size()))).getProfile();
            Supplier<PlayerSkin> supplier = minecraft.getSkinManager().lookupInsecure(gameProfile);
            return Optional.of(new TeamSelectionItem(playerTeam, arrayList, supplier));
        }

        @Override
        public void selectItem(SpectatorMenu spectatorMenu) {
            spectatorMenu.selectCategory(new TeleportToPlayerMenuCategory(this.players));
        }

        @Override
        public Component getName() {
            return this.team.getDisplayName();
        }

        @Override
        public void renderIcon(GuiGraphics guiGraphics, float f, float f2) {
            Integer n = this.team.getColor().getColor();
            if (n != null) {
                float f3 = (float)(n >> 16 & 0xFF) / 255.0f;
                float f4 = (float)(n >> 8 & 0xFF) / 255.0f;
                float f5 = (float)(n & 0xFF) / 255.0f;
                guiGraphics.fill(1, 1, 15, 15, ARGB.colorFromFloat(f2, f3 * f, f4 * f, f5 * f));
            }
            PlayerFaceRenderer.draw(guiGraphics, this.iconSkin.get(), 2, 2, 12, ARGB.colorFromFloat(f2, f, f, f));
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}

