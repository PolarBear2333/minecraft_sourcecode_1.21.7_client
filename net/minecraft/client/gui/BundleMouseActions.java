/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector2i
 */
package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.ScrollWheelHandler;
import net.minecraft.client.gui.ItemSlotMouseAction;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ServerboundSelectBundleItemPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector2i;

public class BundleMouseActions
implements ItemSlotMouseAction {
    private final Minecraft minecraft;
    private final ScrollWheelHandler scrollWheelHandler;

    public BundleMouseActions(Minecraft minecraft) {
        this.minecraft = minecraft;
        this.scrollWheelHandler = new ScrollWheelHandler();
    }

    @Override
    public boolean matches(Slot slot) {
        return slot.getItem().is(ItemTags.BUNDLES);
    }

    @Override
    public boolean onMouseScrolled(double d, double d2, int n, ItemStack itemStack) {
        int n2;
        int n3;
        int n4;
        int n5 = BundleItem.getNumberOfItemsToShow(itemStack);
        if (n5 == 0) {
            return false;
        }
        Vector2i vector2i = this.scrollWheelHandler.onMouseScroll(d, d2);
        int n6 = n4 = vector2i.y == 0 ? -vector2i.x : vector2i.y;
        if (n4 != 0 && (n3 = BundleItem.getSelectedItem(itemStack)) != (n2 = ScrollWheelHandler.getNextScrollWheelSelection(n4, n3, n5))) {
            this.toggleSelectedBundleItem(itemStack, n, n2);
        }
        return true;
    }

    @Override
    public void onStopHovering(Slot slot) {
        this.unselectedBundleItem(slot.getItem(), slot.index);
    }

    @Override
    public void onSlotClicked(Slot slot, ClickType clickType) {
        if (clickType == ClickType.QUICK_MOVE || clickType == ClickType.SWAP) {
            this.unselectedBundleItem(slot.getItem(), slot.index);
        }
    }

    private void toggleSelectedBundleItem(ItemStack itemStack, int n, int n2) {
        if (this.minecraft.getConnection() != null && n2 < BundleItem.getNumberOfItemsToShow(itemStack)) {
            ClientPacketListener clientPacketListener = this.minecraft.getConnection();
            BundleItem.toggleSelectedItem(itemStack, n2);
            clientPacketListener.send(new ServerboundSelectBundleItemPacket(n, n2));
        }
    }

    public void unselectedBundleItem(ItemStack itemStack, int n) {
        this.toggleSelectedBundleItem(itemStack, n, -1);
    }
}

