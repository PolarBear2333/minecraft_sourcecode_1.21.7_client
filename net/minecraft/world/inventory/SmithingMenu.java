/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.inventory;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class SmithingMenu
extends ItemCombinerMenu {
    public static final int TEMPLATE_SLOT = 0;
    public static final int BASE_SLOT = 1;
    public static final int ADDITIONAL_SLOT = 2;
    public static final int RESULT_SLOT = 3;
    public static final int TEMPLATE_SLOT_X_PLACEMENT = 8;
    public static final int BASE_SLOT_X_PLACEMENT = 26;
    public static final int ADDITIONAL_SLOT_X_PLACEMENT = 44;
    private static final int RESULT_SLOT_X_PLACEMENT = 98;
    public static final int SLOT_Y_PLACEMENT = 48;
    private final Level level;
    private final RecipePropertySet baseItemTest;
    private final RecipePropertySet templateItemTest;
    private final RecipePropertySet additionItemTest;
    private final DataSlot hasRecipeError = DataSlot.standalone();

    public SmithingMenu(int n, Inventory inventory) {
        this(n, inventory, ContainerLevelAccess.NULL);
    }

    public SmithingMenu(int n, Inventory inventory, ContainerLevelAccess containerLevelAccess) {
        this(n, inventory, containerLevelAccess, inventory.player.level());
    }

    private SmithingMenu(int n, Inventory inventory, ContainerLevelAccess containerLevelAccess, Level level) {
        super(MenuType.SMITHING, n, inventory, containerLevelAccess, SmithingMenu.createInputSlotDefinitions(level.recipeAccess()));
        this.level = level;
        this.baseItemTest = level.recipeAccess().propertySet(RecipePropertySet.SMITHING_BASE);
        this.templateItemTest = level.recipeAccess().propertySet(RecipePropertySet.SMITHING_TEMPLATE);
        this.additionItemTest = level.recipeAccess().propertySet(RecipePropertySet.SMITHING_ADDITION);
        this.addDataSlot(this.hasRecipeError).set(0);
    }

    private static ItemCombinerMenuSlotDefinition createInputSlotDefinitions(RecipeAccess recipeAccess) {
        RecipePropertySet recipePropertySet = recipeAccess.propertySet(RecipePropertySet.SMITHING_BASE);
        RecipePropertySet recipePropertySet2 = recipeAccess.propertySet(RecipePropertySet.SMITHING_TEMPLATE);
        RecipePropertySet recipePropertySet3 = recipeAccess.propertySet(RecipePropertySet.SMITHING_ADDITION);
        return ItemCombinerMenuSlotDefinition.create().withSlot(0, 8, 48, recipePropertySet2::test).withSlot(1, 26, 48, recipePropertySet::test).withSlot(2, 44, 48, recipePropertySet3::test).withResultSlot(3, 98, 48).build();
    }

    @Override
    protected boolean isValidBlock(BlockState blockState) {
        return blockState.is(Blocks.SMITHING_TABLE);
    }

    @Override
    protected void onTake(Player player, ItemStack itemStack) {
        itemStack.onCraftedBy(player, itemStack.getCount());
        this.resultSlots.awardUsedRecipes(player, this.getRelevantItems());
        this.shrinkStackInSlot(0);
        this.shrinkStackInSlot(1);
        this.shrinkStackInSlot(2);
        this.access.execute((level, blockPos) -> level.levelEvent(1044, (BlockPos)blockPos, 0));
    }

    private List<ItemStack> getRelevantItems() {
        return List.of(this.inputSlots.getItem(0), this.inputSlots.getItem(1), this.inputSlots.getItem(2));
    }

    private SmithingRecipeInput createRecipeInput() {
        return new SmithingRecipeInput(this.inputSlots.getItem(0), this.inputSlots.getItem(1), this.inputSlots.getItem(2));
    }

    private void shrinkStackInSlot(int n) {
        ItemStack itemStack = this.inputSlots.getItem(n);
        if (!itemStack.isEmpty()) {
            itemStack.shrink(1);
            this.inputSlots.setItem(n, itemStack);
        }
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
        if (this.level instanceof ServerLevel) {
            boolean bl = this.getSlot(0).hasItem() && this.getSlot(1).hasItem() && this.getSlot(2).hasItem() && !this.getSlot(this.getResultSlot()).hasItem();
            this.hasRecipeError.set(bl ? 1 : 0);
        }
    }

    @Override
    public void createResult() {
        Optional<RecipeHolder<Object>> optional;
        SmithingRecipeInput smithingRecipeInput = this.createRecipeInput();
        Level level = this.level;
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            optional = serverLevel.recipeAccess().getRecipeFor(RecipeType.SMITHING, smithingRecipeInput, serverLevel);
        } else {
            optional = Optional.empty();
        }
        optional.ifPresentOrElse(recipeHolder -> {
            ItemStack itemStack = ((SmithingRecipe)recipeHolder.value()).assemble(smithingRecipeInput, this.level.registryAccess());
            this.resultSlots.setRecipeUsed((RecipeHolder<?>)recipeHolder);
            this.resultSlots.setItem(0, itemStack);
        }, () -> {
            this.resultSlots.setRecipeUsed(null);
            this.resultSlots.setItem(0, ItemStack.EMPTY);
        });
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
        return slot.container != this.resultSlots && super.canTakeItemForPickAll(itemStack, slot);
    }

    @Override
    public boolean canMoveIntoInputSlots(ItemStack itemStack) {
        if (this.templateItemTest.test(itemStack) && !this.getSlot(0).hasItem()) {
            return true;
        }
        if (this.baseItemTest.test(itemStack) && !this.getSlot(1).hasItem()) {
            return true;
        }
        return this.additionItemTest.test(itemStack) && !this.getSlot(2).hasItem();
    }

    public boolean hasRecipeError() {
        return this.hasRecipeError.get() > 0;
    }
}

