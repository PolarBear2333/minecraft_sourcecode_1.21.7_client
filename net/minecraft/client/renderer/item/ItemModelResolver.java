/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer.item;

import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemModelResolver {
    private final Function<ResourceLocation, ItemModel> modelGetter = modelManager::getItemModel;
    private final Function<ResourceLocation, ClientItem.Properties> clientProperties = modelManager::getItemProperties;

    public ItemModelResolver(ModelManager modelManager) {
    }

    public void updateForLiving(ItemStackRenderState itemStackRenderState, ItemStack itemStack, ItemDisplayContext itemDisplayContext, LivingEntity livingEntity) {
        this.updateForTopItem(itemStackRenderState, itemStack, itemDisplayContext, livingEntity.level(), livingEntity, livingEntity.getId() + itemDisplayContext.ordinal());
    }

    public void updateForNonLiving(ItemStackRenderState itemStackRenderState, ItemStack itemStack, ItemDisplayContext itemDisplayContext, Entity entity) {
        this.updateForTopItem(itemStackRenderState, itemStack, itemDisplayContext, entity.level(), null, entity.getId());
    }

    public void updateForTopItem(ItemStackRenderState itemStackRenderState, ItemStack itemStack, ItemDisplayContext itemDisplayContext, @Nullable Level level, @Nullable LivingEntity livingEntity, int n) {
        itemStackRenderState.clear();
        if (!itemStack.isEmpty()) {
            itemStackRenderState.displayContext = itemDisplayContext;
            this.appendItemLayers(itemStackRenderState, itemStack, itemDisplayContext, level, livingEntity, n);
        }
    }

    public void appendItemLayers(ItemStackRenderState itemStackRenderState, ItemStack itemStack, ItemDisplayContext itemDisplayContext, @Nullable Level level, @Nullable LivingEntity livingEntity, int n) {
        ClientLevel clientLevel;
        ResourceLocation resourceLocation = itemStack.get(DataComponents.ITEM_MODEL);
        if (resourceLocation == null) {
            return;
        }
        itemStackRenderState.setOversizedInGui(this.clientProperties.apply(resourceLocation).oversizedInGui());
        this.modelGetter.apply(resourceLocation).update(itemStackRenderState, itemStack, this, itemDisplayContext, level instanceof ClientLevel ? (clientLevel = (ClientLevel)level) : null, livingEntity, n);
    }

    public boolean shouldPlaySwapAnimation(ItemStack itemStack) {
        ResourceLocation resourceLocation = itemStack.get(DataComponents.ITEM_MODEL);
        if (resourceLocation == null) {
            return true;
        }
        return this.clientProperties.apply(resourceLocation).handAnimationOnSwap();
    }
}

