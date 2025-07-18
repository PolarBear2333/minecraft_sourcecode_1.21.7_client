/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.crafting.RecipeType;

public class SmokerMenu
extends AbstractFurnaceMenu {
    public SmokerMenu(int n, Inventory inventory) {
        super(MenuType.SMOKER, RecipeType.SMOKING, RecipePropertySet.SMOKER_INPUT, RecipeBookType.SMOKER, n, inventory);
    }

    public SmokerMenu(int n, Inventory inventory, Container container, ContainerData containerData) {
        super(MenuType.SMOKER, RecipeType.SMOKING, RecipePropertySet.SMOKER_INPUT, RecipeBookType.SMOKER, n, inventory, container, containerData);
    }
}

