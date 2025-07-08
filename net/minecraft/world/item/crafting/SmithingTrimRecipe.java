/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  javax.annotation.Nullable
 */
package net.minecraft.world.item.crafting;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimMaterials;
import net.minecraft.world.item.equipment.trim.TrimPattern;

public class SmithingTrimRecipe
implements SmithingRecipe {
    final Ingredient template;
    final Ingredient base;
    final Ingredient addition;
    final Holder<TrimPattern> pattern;
    @Nullable
    private PlacementInfo placementInfo;

    public SmithingTrimRecipe(Ingredient ingredient, Ingredient ingredient2, Ingredient ingredient3, Holder<TrimPattern> holder) {
        this.template = ingredient;
        this.base = ingredient2;
        this.addition = ingredient3;
        this.pattern = holder;
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput smithingRecipeInput, HolderLookup.Provider provider) {
        return SmithingTrimRecipe.applyTrim(provider, smithingRecipeInput.base(), smithingRecipeInput.addition(), this.pattern);
    }

    public static ItemStack applyTrim(HolderLookup.Provider provider, ItemStack itemStack, ItemStack itemStack2, Holder<TrimPattern> holder) {
        Optional<Holder<TrimMaterial>> optional = TrimMaterials.getFromIngredient(provider, itemStack2);
        if (optional.isPresent()) {
            ArmorTrim armorTrim;
            ArmorTrim armorTrim2 = itemStack.get(DataComponents.TRIM);
            if (Objects.equals(armorTrim2, armorTrim = new ArmorTrim(optional.get(), holder))) {
                return ItemStack.EMPTY;
            }
            ItemStack itemStack3 = itemStack.copyWithCount(1);
            itemStack3.set(DataComponents.TRIM, armorTrim);
            return itemStack3;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public Optional<Ingredient> templateIngredient() {
        return Optional.of(this.template);
    }

    @Override
    public Ingredient baseIngredient() {
        return this.base;
    }

    @Override
    public Optional<Ingredient> additionIngredient() {
        return Optional.of(this.addition);
    }

    @Override
    public RecipeSerializer<SmithingTrimRecipe> getSerializer() {
        return RecipeSerializer.SMITHING_TRIM;
    }

    @Override
    public PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.create(List.of(this.template, this.base, this.addition));
        }
        return this.placementInfo;
    }

    @Override
    public List<RecipeDisplay> display() {
        SlotDisplay slotDisplay = this.base.display();
        SlotDisplay slotDisplay2 = this.addition.display();
        SlotDisplay slotDisplay3 = this.template.display();
        return List.of(new SmithingRecipeDisplay(slotDisplay3, slotDisplay, slotDisplay2, new SlotDisplay.SmithingTrimDemoSlotDisplay(slotDisplay, slotDisplay2, this.pattern), new SlotDisplay.ItemSlotDisplay(Items.SMITHING_TABLE)));
    }

    public static class Serializer
    implements RecipeSerializer<SmithingTrimRecipe> {
        private static final MapCodec<SmithingTrimRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Ingredient.CODEC.fieldOf("template").forGetter(smithingTrimRecipe -> smithingTrimRecipe.template), (App)Ingredient.CODEC.fieldOf("base").forGetter(smithingTrimRecipe -> smithingTrimRecipe.base), (App)Ingredient.CODEC.fieldOf("addition").forGetter(smithingTrimRecipe -> smithingTrimRecipe.addition), (App)TrimPattern.CODEC.fieldOf("pattern").forGetter(smithingTrimRecipe -> smithingTrimRecipe.pattern)).apply((Applicative)instance, SmithingTrimRecipe::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, SmithingTrimRecipe> STREAM_CODEC = StreamCodec.composite(Ingredient.CONTENTS_STREAM_CODEC, smithingTrimRecipe -> smithingTrimRecipe.template, Ingredient.CONTENTS_STREAM_CODEC, smithingTrimRecipe -> smithingTrimRecipe.base, Ingredient.CONTENTS_STREAM_CODEC, smithingTrimRecipe -> smithingTrimRecipe.addition, TrimPattern.STREAM_CODEC, smithingTrimRecipe -> smithingTrimRecipe.pattern, SmithingTrimRecipe::new);

        @Override
        public MapCodec<SmithingTrimRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SmithingTrimRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}

