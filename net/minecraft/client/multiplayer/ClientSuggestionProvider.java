/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.client.multiplayer;

import com.google.common.collect.Lists;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.commands.PermissionSource;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.protocol.game.ClientboundCustomChatCompletionsPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ClientSuggestionProvider
implements PermissionSource,
SharedSuggestionProvider {
    private final ClientPacketListener connection;
    private final Minecraft minecraft;
    private int pendingSuggestionsId = -1;
    @Nullable
    private CompletableFuture<Suggestions> pendingSuggestionsFuture;
    private final Set<String> customCompletionSuggestions = new HashSet<String>();
    private final boolean allowsRestrictedCommands;

    public ClientSuggestionProvider(ClientPacketListener clientPacketListener, Minecraft minecraft, boolean bl) {
        this.connection = clientPacketListener;
        this.minecraft = minecraft;
        this.allowsRestrictedCommands = bl;
    }

    @Override
    public Collection<String> getOnlinePlayerNames() {
        ArrayList arrayList = Lists.newArrayList();
        for (PlayerInfo playerInfo : this.connection.getOnlinePlayers()) {
            arrayList.add(playerInfo.getProfile().getName());
        }
        return arrayList;
    }

    @Override
    public Collection<String> getCustomTabSugggestions() {
        if (this.customCompletionSuggestions.isEmpty()) {
            return this.getOnlinePlayerNames();
        }
        HashSet<String> hashSet = new HashSet<String>(this.getOnlinePlayerNames());
        hashSet.addAll(this.customCompletionSuggestions);
        return hashSet;
    }

    @Override
    public Collection<String> getSelectedEntities() {
        if (this.minecraft.hitResult != null && this.minecraft.hitResult.getType() == HitResult.Type.ENTITY) {
            return Collections.singleton(((EntityHitResult)this.minecraft.hitResult).getEntity().getStringUUID());
        }
        return Collections.emptyList();
    }

    @Override
    public Collection<String> getAllTeams() {
        return this.connection.scoreboard().getTeamNames();
    }

    @Override
    public Stream<ResourceLocation> getAvailableSounds() {
        return this.minecraft.getSoundManager().getAvailableSounds().stream();
    }

    @Override
    public boolean hasPermission(int n) {
        return this.allowsRestrictedCommands || n == 0;
    }

    @Override
    public boolean allowsSelectors() {
        return this.allowsRestrictedCommands;
    }

    @Override
    public CompletableFuture<Suggestions> suggestRegistryElements(ResourceKey<? extends Registry<?>> resourceKey, SharedSuggestionProvider.ElementSuggestionType elementSuggestionType, SuggestionsBuilder suggestionsBuilder, CommandContext<?> commandContext) {
        return this.registryAccess().lookup(resourceKey).map(registry -> {
            this.suggestRegistryElements((HolderLookup<?>)registry, elementSuggestionType, suggestionsBuilder);
            return suggestionsBuilder.buildFuture();
        }).orElseGet(() -> this.customSuggestion(commandContext));
    }

    @Override
    public CompletableFuture<Suggestions> customSuggestion(CommandContext<?> commandContext) {
        if (this.pendingSuggestionsFuture != null) {
            this.pendingSuggestionsFuture.cancel(false);
        }
        this.pendingSuggestionsFuture = new CompletableFuture();
        int n = ++this.pendingSuggestionsId;
        this.connection.send(new ServerboundCommandSuggestionPacket(n, commandContext.getInput()));
        return this.pendingSuggestionsFuture;
    }

    private static String prettyPrint(double d) {
        return String.format(Locale.ROOT, "%.2f", d);
    }

    private static String prettyPrint(int n) {
        return Integer.toString(n);
    }

    @Override
    public Collection<SharedSuggestionProvider.TextCoordinates> getRelevantCoordinates() {
        HitResult hitResult = this.minecraft.hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) {
            return SharedSuggestionProvider.super.getRelevantCoordinates();
        }
        BlockPos blockPos = ((BlockHitResult)hitResult).getBlockPos();
        return Collections.singleton(new SharedSuggestionProvider.TextCoordinates(ClientSuggestionProvider.prettyPrint(blockPos.getX()), ClientSuggestionProvider.prettyPrint(blockPos.getY()), ClientSuggestionProvider.prettyPrint(blockPos.getZ())));
    }

    @Override
    public Collection<SharedSuggestionProvider.TextCoordinates> getAbsoluteCoordinates() {
        HitResult hitResult = this.minecraft.hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.BLOCK) {
            return SharedSuggestionProvider.super.getAbsoluteCoordinates();
        }
        Vec3 vec3 = hitResult.getLocation();
        return Collections.singleton(new SharedSuggestionProvider.TextCoordinates(ClientSuggestionProvider.prettyPrint(vec3.x), ClientSuggestionProvider.prettyPrint(vec3.y), ClientSuggestionProvider.prettyPrint(vec3.z)));
    }

    @Override
    public Set<ResourceKey<Level>> levels() {
        return this.connection.levels();
    }

    @Override
    public RegistryAccess registryAccess() {
        return this.connection.registryAccess();
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return this.connection.enabledFeatures();
    }

    public void completeCustomSuggestions(int n, Suggestions suggestions) {
        if (n == this.pendingSuggestionsId) {
            this.pendingSuggestionsFuture.complete(suggestions);
            this.pendingSuggestionsFuture = null;
            this.pendingSuggestionsId = -1;
        }
    }

    public void modifyCustomCompletions(ClientboundCustomChatCompletionsPacket.Action action, List<String> list) {
        switch (action) {
            case ADD: {
                this.customCompletionSuggestions.addAll(list);
                break;
            }
            case REMOVE: {
                list.forEach(this.customCompletionSuggestions::remove);
                break;
            }
            case SET: {
                this.customCompletionSuggestions.clear();
                this.customCompletionSuggestions.addAll(list);
            }
        }
    }

    public boolean allowsRestrictedCommands() {
        return this.allowsRestrictedCommands;
    }
}

