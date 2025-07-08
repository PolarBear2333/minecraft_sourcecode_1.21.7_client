/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.worldselection;

import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;

public record InitialWorldCreationOptions(WorldCreationUiState.SelectedGameMode selectedGameMode, Set<GameRules.Key<GameRules.BooleanValue>> disabledGameRules, @Nullable ResourceKey<FlatLevelGeneratorPreset> flatLevelPreset) {
}

