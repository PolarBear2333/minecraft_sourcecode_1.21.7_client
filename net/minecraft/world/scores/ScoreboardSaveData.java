/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.scores;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;

public class ScoreboardSaveData
extends SavedData {
    public static final String FILE_ID = "scoreboard";
    private final Scoreboard scoreboard;

    public ScoreboardSaveData(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    public void loadFrom(Packed packed) {
        packed.objectives().forEach(this.scoreboard::loadObjective);
        packed.scores().forEach(this.scoreboard::loadPlayerScore);
        packed.displaySlots().forEach((displaySlot, string) -> {
            Objective objective = this.scoreboard.getObjective((String)string);
            this.scoreboard.setDisplayObjective((DisplaySlot)displaySlot, objective);
        });
        packed.teams().forEach(this.scoreboard::loadPlayerTeam);
    }

    public Packed pack() {
        EnumMap<DisplaySlot, String> enumMap = new EnumMap<DisplaySlot, String>(DisplaySlot.class);
        for (DisplaySlot displaySlot : DisplaySlot.values()) {
            Objective objective = this.scoreboard.getDisplayObjective(displaySlot);
            if (objective == null) continue;
            enumMap.put(displaySlot, objective.getName());
        }
        return new Packed(this.scoreboard.getObjectives().stream().map(Objective::pack).toList(), this.scoreboard.packPlayerScores(), enumMap, this.scoreboard.getPlayerTeams().stream().map(PlayerTeam::pack).toList());
    }

    public record Packed(List<Objective.Packed> objectives, List<Scoreboard.PackedScore> scores, Map<DisplaySlot, String> displaySlots, List<PlayerTeam.Packed> teams) {
        public static final Codec<Packed> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Objective.Packed.CODEC.listOf().optionalFieldOf("Objectives", List.of()).forGetter(Packed::objectives), (App)Scoreboard.PackedScore.CODEC.listOf().optionalFieldOf("PlayerScores", List.of()).forGetter(Packed::scores), (App)Codec.unboundedMap(DisplaySlot.CODEC, (Codec)Codec.STRING).optionalFieldOf("DisplaySlots", Map.of()).forGetter(Packed::displaySlots), (App)PlayerTeam.Packed.CODEC.listOf().optionalFieldOf("Teams", List.of()).forGetter(Packed::teams)).apply((Applicative)instance, Packed::new));
    }
}

