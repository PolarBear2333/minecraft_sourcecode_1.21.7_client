/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.crafting.display;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.RecipeDisplayId;

public record RecipeDisplayEntry(RecipeDisplayId id, RecipeDisplay display, OptionalInt group, RecipeBookCategory category, Optional<List<Ingredient>> craftingRequirements) {
    public static final StreamCodec<RegistryFriendlyByteBuf, RecipeDisplayEntry> STREAM_CODEC = StreamCodec.composite(RecipeDisplayId.STREAM_CODEC, RecipeDisplayEntry::id, RecipeDisplay.STREAM_CODEC, RecipeDisplayEntry::display, ByteBufCodecs.OPTIONAL_VAR_INT, RecipeDisplayEntry::group, ByteBufCodecs.registry(Registries.RECIPE_BOOK_CATEGORY), RecipeDisplayEntry::category, Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()).apply(ByteBufCodecs::optional), RecipeDisplayEntry::craftingRequirements, RecipeDisplayEntry::new);

    public List<ItemStack> resultItems(ContextMap contextMap) {
        return this.display.result().resolveForStacks(contextMap);
    }

    public boolean canCraft(StackedItemContents stackedItemContents) {
        if (this.craftingRequirements.isEmpty()) {
            return false;
        }
        return stackedItemContents.canCraft(this.craftingRequirements.get(), null);
    }
}

