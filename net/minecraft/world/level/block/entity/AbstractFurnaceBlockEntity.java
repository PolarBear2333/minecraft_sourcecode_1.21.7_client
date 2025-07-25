/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.serialization.Codec
 *  it.unimi.dsi.fastutil.objects.Reference2IntMap$Entry
 *  it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
 *  javax.annotation.Nullable
 */
package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractFurnaceBlockEntity
extends BaseContainerBlockEntity
implements WorldlyContainer,
RecipeCraftingHolder,
StackedContentsCompatible {
    protected static final int SLOT_INPUT = 0;
    protected static final int SLOT_FUEL = 1;
    protected static final int SLOT_RESULT = 2;
    public static final int DATA_LIT_TIME = 0;
    private static final int[] SLOTS_FOR_UP = new int[]{0};
    private static final int[] SLOTS_FOR_DOWN = new int[]{2, 1};
    private static final int[] SLOTS_FOR_SIDES = new int[]{1};
    public static final int DATA_LIT_DURATION = 1;
    public static final int DATA_COOKING_PROGRESS = 2;
    public static final int DATA_COOKING_TOTAL_TIME = 3;
    public static final int NUM_DATA_VALUES = 4;
    public static final int BURN_TIME_STANDARD = 200;
    public static final int BURN_COOL_SPEED = 2;
    private static final Codec<Map<ResourceKey<Recipe<?>>, Integer>> RECIPES_USED_CODEC = Codec.unboundedMap(Recipe.KEY_CODEC, (Codec)Codec.INT);
    private static final short DEFAULT_COOKING_TIMER = 0;
    private static final short DEFAULT_COOKING_TOTAL_TIME = 0;
    private static final short DEFAULT_LIT_TIME_REMAINING = 0;
    private static final short DEFAULT_LIT_TOTAL_TIME = 0;
    protected NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
    int litTimeRemaining;
    int litTotalTime;
    int cookingTimer;
    int cookingTotalTime;
    protected final ContainerData dataAccess = new ContainerData(){

        @Override
        public int get(int n) {
            switch (n) {
                case 0: {
                    return AbstractFurnaceBlockEntity.this.litTimeRemaining;
                }
                case 1: {
                    return AbstractFurnaceBlockEntity.this.litTotalTime;
                }
                case 2: {
                    return AbstractFurnaceBlockEntity.this.cookingTimer;
                }
                case 3: {
                    return AbstractFurnaceBlockEntity.this.cookingTotalTime;
                }
            }
            return 0;
        }

        @Override
        public void set(int n, int n2) {
            switch (n) {
                case 0: {
                    AbstractFurnaceBlockEntity.this.litTimeRemaining = n2;
                    break;
                }
                case 1: {
                    AbstractFurnaceBlockEntity.this.litTotalTime = n2;
                    break;
                }
                case 2: {
                    AbstractFurnaceBlockEntity.this.cookingTimer = n2;
                    break;
                }
                case 3: {
                    AbstractFurnaceBlockEntity.this.cookingTotalTime = n2;
                    break;
                }
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };
    private final Reference2IntOpenHashMap<ResourceKey<Recipe<?>>> recipesUsed = new Reference2IntOpenHashMap();
    private final RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> quickCheck;

    protected AbstractFurnaceBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState, RecipeType<? extends AbstractCookingRecipe> recipeType) {
        super(blockEntityType, blockPos, blockState);
        this.quickCheck = RecipeManager.createCheck(recipeType);
    }

    private boolean isLit() {
        return this.litTimeRemaining > 0;
    }

    @Override
    protected void loadAdditional(ValueInput valueInput) {
        super.loadAdditional(valueInput);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(valueInput, this.items);
        this.cookingTimer = valueInput.getShortOr("cooking_time_spent", (short)0);
        this.cookingTotalTime = valueInput.getShortOr("cooking_total_time", (short)0);
        this.litTimeRemaining = valueInput.getShortOr("lit_time_remaining", (short)0);
        this.litTotalTime = valueInput.getShortOr("lit_total_time", (short)0);
        this.recipesUsed.clear();
        this.recipesUsed.putAll(valueInput.read("RecipesUsed", RECIPES_USED_CODEC).orElse(Map.of()));
    }

    @Override
    protected void saveAdditional(ValueOutput valueOutput) {
        super.saveAdditional(valueOutput);
        valueOutput.putShort("cooking_time_spent", (short)this.cookingTimer);
        valueOutput.putShort("cooking_total_time", (short)this.cookingTotalTime);
        valueOutput.putShort("lit_time_remaining", (short)this.litTimeRemaining);
        valueOutput.putShort("lit_total_time", (short)this.litTotalTime);
        ContainerHelper.saveAllItems(valueOutput, this.items);
        valueOutput.store("RecipesUsed", RECIPES_USED_CODEC, this.recipesUsed);
    }

    public static void serverTick(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, AbstractFurnaceBlockEntity abstractFurnaceBlockEntity) {
        boolean bl;
        boolean bl2 = abstractFurnaceBlockEntity.isLit();
        boolean bl3 = false;
        if (abstractFurnaceBlockEntity.isLit()) {
            --abstractFurnaceBlockEntity.litTimeRemaining;
        }
        ItemStack itemStack = abstractFurnaceBlockEntity.items.get(1);
        ItemStack itemStack2 = abstractFurnaceBlockEntity.items.get(0);
        boolean bl4 = !itemStack2.isEmpty();
        boolean bl5 = bl = !itemStack.isEmpty();
        if (abstractFurnaceBlockEntity.isLit() || bl && bl4) {
            SingleRecipeInput singleRecipeInput = new SingleRecipeInput(itemStack2);
            RecipeHolder recipeHolder = bl4 ? (RecipeHolder)abstractFurnaceBlockEntity.quickCheck.getRecipeFor(singleRecipeInput, serverLevel).orElse(null) : null;
            int n = abstractFurnaceBlockEntity.getMaxStackSize();
            if (!abstractFurnaceBlockEntity.isLit() && AbstractFurnaceBlockEntity.canBurn(serverLevel.registryAccess(), recipeHolder, singleRecipeInput, abstractFurnaceBlockEntity.items, n)) {
                abstractFurnaceBlockEntity.litTotalTime = abstractFurnaceBlockEntity.litTimeRemaining = abstractFurnaceBlockEntity.getBurnDuration(serverLevel.fuelValues(), itemStack);
                if (abstractFurnaceBlockEntity.isLit()) {
                    bl3 = true;
                    if (bl) {
                        Item item = itemStack.getItem();
                        itemStack.shrink(1);
                        if (itemStack.isEmpty()) {
                            abstractFurnaceBlockEntity.items.set(1, item.getCraftingRemainder());
                        }
                    }
                }
            }
            if (abstractFurnaceBlockEntity.isLit() && AbstractFurnaceBlockEntity.canBurn(serverLevel.registryAccess(), recipeHolder, singleRecipeInput, abstractFurnaceBlockEntity.items, n)) {
                ++abstractFurnaceBlockEntity.cookingTimer;
                if (abstractFurnaceBlockEntity.cookingTimer == abstractFurnaceBlockEntity.cookingTotalTime) {
                    abstractFurnaceBlockEntity.cookingTimer = 0;
                    abstractFurnaceBlockEntity.cookingTotalTime = AbstractFurnaceBlockEntity.getTotalCookTime(serverLevel, abstractFurnaceBlockEntity);
                    if (AbstractFurnaceBlockEntity.burn(serverLevel.registryAccess(), recipeHolder, singleRecipeInput, abstractFurnaceBlockEntity.items, n)) {
                        abstractFurnaceBlockEntity.setRecipeUsed(recipeHolder);
                    }
                    bl3 = true;
                }
            } else {
                abstractFurnaceBlockEntity.cookingTimer = 0;
            }
        } else if (!abstractFurnaceBlockEntity.isLit() && abstractFurnaceBlockEntity.cookingTimer > 0) {
            abstractFurnaceBlockEntity.cookingTimer = Mth.clamp(abstractFurnaceBlockEntity.cookingTimer - 2, 0, abstractFurnaceBlockEntity.cookingTotalTime);
        }
        if (bl2 != abstractFurnaceBlockEntity.isLit()) {
            bl3 = true;
            blockState = (BlockState)blockState.setValue(AbstractFurnaceBlock.LIT, abstractFurnaceBlockEntity.isLit());
            serverLevel.setBlock(blockPos, blockState, 3);
        }
        if (bl3) {
            AbstractFurnaceBlockEntity.setChanged(serverLevel, blockPos, blockState);
        }
    }

    private static boolean canBurn(RegistryAccess registryAccess, @Nullable RecipeHolder<? extends AbstractCookingRecipe> recipeHolder, SingleRecipeInput singleRecipeInput, NonNullList<ItemStack> nonNullList, int n) {
        if (nonNullList.get(0).isEmpty() || recipeHolder == null) {
            return false;
        }
        ItemStack itemStack = recipeHolder.value().assemble(singleRecipeInput, (HolderLookup.Provider)registryAccess);
        if (itemStack.isEmpty()) {
            return false;
        }
        ItemStack itemStack2 = nonNullList.get(2);
        if (itemStack2.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(itemStack2, itemStack)) {
            return false;
        }
        if (itemStack2.getCount() < n && itemStack2.getCount() < itemStack2.getMaxStackSize()) {
            return true;
        }
        return itemStack2.getCount() < itemStack.getMaxStackSize();
    }

    private static boolean burn(RegistryAccess registryAccess, @Nullable RecipeHolder<? extends AbstractCookingRecipe> recipeHolder, SingleRecipeInput singleRecipeInput, NonNullList<ItemStack> nonNullList, int n) {
        if (recipeHolder == null || !AbstractFurnaceBlockEntity.canBurn(registryAccess, recipeHolder, singleRecipeInput, nonNullList, n)) {
            return false;
        }
        ItemStack itemStack = nonNullList.get(0);
        ItemStack itemStack2 = recipeHolder.value().assemble(singleRecipeInput, (HolderLookup.Provider)registryAccess);
        ItemStack itemStack3 = nonNullList.get(2);
        if (itemStack3.isEmpty()) {
            nonNullList.set(2, itemStack2.copy());
        } else if (ItemStack.isSameItemSameComponents(itemStack3, itemStack2)) {
            itemStack3.grow(1);
        }
        if (itemStack.is(Blocks.WET_SPONGE.asItem()) && !nonNullList.get(1).isEmpty() && nonNullList.get(1).is(Items.BUCKET)) {
            nonNullList.set(1, new ItemStack(Items.WATER_BUCKET));
        }
        itemStack.shrink(1);
        return true;
    }

    protected int getBurnDuration(FuelValues fuelValues, ItemStack itemStack) {
        return fuelValues.burnDuration(itemStack);
    }

    private static int getTotalCookTime(ServerLevel serverLevel, AbstractFurnaceBlockEntity abstractFurnaceBlockEntity) {
        SingleRecipeInput singleRecipeInput = new SingleRecipeInput(abstractFurnaceBlockEntity.getItem(0));
        return abstractFurnaceBlockEntity.quickCheck.getRecipeFor(singleRecipeInput, serverLevel).map(recipeHolder -> ((AbstractCookingRecipe)recipeHolder.value()).cookingTime()).orElse(200);
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        if (direction == Direction.DOWN) {
            return SLOTS_FOR_DOWN;
        }
        if (direction == Direction.UP) {
            return SLOTS_FOR_UP;
        }
        return SLOTS_FOR_SIDES;
    }

    @Override
    public boolean canPlaceItemThroughFace(int n, ItemStack itemStack, @Nullable Direction direction) {
        return this.canPlaceItem(n, itemStack);
    }

    @Override
    public boolean canTakeItemThroughFace(int n, ItemStack itemStack, Direction direction) {
        if (direction == Direction.DOWN && n == 1) {
            return itemStack.is(Items.WATER_BUCKET) || itemStack.is(Items.BUCKET);
        }
        return true;
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> nonNullList) {
        this.items = nonNullList;
    }

    @Override
    public void setItem(int n, ItemStack itemStack) {
        Level level;
        ItemStack itemStack2 = this.items.get(n);
        boolean bl = !itemStack.isEmpty() && ItemStack.isSameItemSameComponents(itemStack2, itemStack);
        this.items.set(n, itemStack);
        itemStack.limitSize(this.getMaxStackSize(itemStack));
        if (n == 0 && !bl && (level = this.level) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.cookingTotalTime = AbstractFurnaceBlockEntity.getTotalCookTime(serverLevel, this);
            this.cookingTimer = 0;
            this.setChanged();
        }
    }

    @Override
    public boolean canPlaceItem(int n, ItemStack itemStack) {
        if (n == 2) {
            return false;
        }
        if (n == 1) {
            ItemStack itemStack2 = this.items.get(1);
            return this.level.fuelValues().isFuel(itemStack) || itemStack.is(Items.BUCKET) && !itemStack2.is(Items.BUCKET);
        }
        return true;
    }

    @Override
    public void setRecipeUsed(@Nullable RecipeHolder<?> recipeHolder) {
        if (recipeHolder != null) {
            ResourceKey<Recipe<?>> resourceKey = recipeHolder.id();
            this.recipesUsed.addTo(resourceKey, 1);
        }
    }

    @Override
    @Nullable
    public RecipeHolder<?> getRecipeUsed() {
        return null;
    }

    @Override
    public void awardUsedRecipes(Player player, List<ItemStack> list) {
    }

    public void awardUsedRecipesAndPopExperience(ServerPlayer serverPlayer) {
        List<RecipeHolder<?>> list = this.getRecipesToAwardAndPopExperience(serverPlayer.level(), serverPlayer.position());
        serverPlayer.awardRecipes(list);
        for (RecipeHolder<?> recipeHolder : list) {
            if (recipeHolder == null) continue;
            serverPlayer.triggerRecipeCrafted(recipeHolder, this.items);
        }
        this.recipesUsed.clear();
    }

    public List<RecipeHolder<?>> getRecipesToAwardAndPopExperience(ServerLevel serverLevel, Vec3 vec3) {
        ArrayList arrayList = Lists.newArrayList();
        for (Reference2IntMap.Entry entry : this.recipesUsed.reference2IntEntrySet()) {
            serverLevel.recipeAccess().byKey((ResourceKey)entry.getKey()).ifPresent(recipeHolder -> {
                arrayList.add(recipeHolder);
                AbstractFurnaceBlockEntity.createExperience(serverLevel, vec3, entry.getIntValue(), ((AbstractCookingRecipe)recipeHolder.value()).experience());
            });
        }
        return arrayList;
    }

    private static void createExperience(ServerLevel serverLevel, Vec3 vec3, int n, float f) {
        int n2 = Mth.floor((float)n * f);
        float f2 = Mth.frac((float)n * f);
        if (f2 != 0.0f && Math.random() < (double)f2) {
            ++n2;
        }
        ExperienceOrb.award(serverLevel, vec3, n2);
    }

    @Override
    public void fillStackedContents(StackedItemContents stackedItemContents) {
        for (ItemStack itemStack : this.items) {
            stackedItemContents.accountStack(itemStack);
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos blockPos, BlockState blockState) {
        super.preRemoveSideEffects(blockPos, blockState);
        Level level = this.level;
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            this.getRecipesToAwardAndPopExperience(serverLevel, Vec3.atCenterOf(blockPos));
        }
    }
}

