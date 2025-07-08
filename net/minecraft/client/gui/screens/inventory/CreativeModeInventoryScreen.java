/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Lists
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.inventory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.HotbarManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeInventoryListener;
import net.minecraft.client.gui.screens.inventory.EffectsInInventory;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.SessionSearchTrees;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.inventory.Hotbar;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;

public class CreativeModeInventoryScreen
extends AbstractContainerScreen<ItemPickerMenu> {
    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller_disabled");
    private static final ResourceLocation[] UNSELECTED_TOP_TABS = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_unselected_1"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_unselected_2"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_unselected_3"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_unselected_4"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_unselected_5"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_unselected_6"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_unselected_7")};
    private static final ResourceLocation[] SELECTED_TOP_TABS = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_selected_1"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_selected_2"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_selected_3"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_selected_4"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_selected_5"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_selected_6"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_selected_7")};
    private static final ResourceLocation[] UNSELECTED_BOTTOM_TABS = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_unselected_1"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_unselected_2"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_unselected_3"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_unselected_4"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_unselected_5"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_unselected_6"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_unselected_7")};
    private static final ResourceLocation[] SELECTED_BOTTOM_TABS = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_selected_1"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_selected_2"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_selected_3"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_selected_4"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_selected_5"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_selected_6"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_selected_7")};
    private static final int NUM_ROWS = 5;
    private static final int NUM_COLS = 9;
    private static final int TAB_WIDTH = 26;
    private static final int TAB_HEIGHT = 32;
    private static final int SCROLLER_WIDTH = 12;
    private static final int SCROLLER_HEIGHT = 15;
    static final SimpleContainer CONTAINER = new SimpleContainer(45);
    private static final Component TRASH_SLOT_TOOLTIP = Component.translatable("inventory.binSlot");
    private static CreativeModeTab selectedTab = CreativeModeTabs.getDefaultTab();
    private float scrollOffs;
    private boolean scrolling;
    private EditBox searchBox;
    @Nullable
    private List<Slot> originalSlots;
    @Nullable
    private Slot destroyItemSlot;
    private CreativeInventoryListener listener;
    private boolean ignoreTextInput;
    private boolean hasClickedOutside;
    private final Set<TagKey<Item>> visibleTags = new HashSet<TagKey<Item>>();
    private final boolean displayOperatorCreativeTab;
    private final EffectsInInventory effects;

    public CreativeModeInventoryScreen(LocalPlayer localPlayer, FeatureFlagSet featureFlagSet, boolean bl) {
        super(new ItemPickerMenu(localPlayer), localPlayer.getInventory(), CommonComponents.EMPTY);
        localPlayer.containerMenu = this.menu;
        this.imageHeight = 136;
        this.imageWidth = 195;
        this.displayOperatorCreativeTab = bl;
        this.tryRebuildTabContents(localPlayer.connection.searchTrees(), featureFlagSet, this.hasPermissions(localPlayer), localPlayer.level().registryAccess());
        this.effects = new EffectsInInventory(this);
    }

    private boolean hasPermissions(Player player) {
        return player.canUseGameMasterBlocks() && this.displayOperatorCreativeTab;
    }

    private void tryRefreshInvalidatedTabs(FeatureFlagSet featureFlagSet, boolean bl, HolderLookup.Provider provider) {
        ClientPacketListener clientPacketListener = this.minecraft.getConnection();
        if (this.tryRebuildTabContents(clientPacketListener != null ? clientPacketListener.searchTrees() : null, featureFlagSet, bl, provider)) {
            for (CreativeModeTab creativeModeTab : CreativeModeTabs.allTabs()) {
                Collection<ItemStack> collection = creativeModeTab.getDisplayItems();
                if (creativeModeTab != selectedTab) continue;
                if (creativeModeTab.getType() == CreativeModeTab.Type.CATEGORY && collection.isEmpty()) {
                    this.selectTab(CreativeModeTabs.getDefaultTab());
                    continue;
                }
                this.refreshCurrentTabContents(collection);
            }
        }
    }

    private boolean tryRebuildTabContents(@Nullable SessionSearchTrees sessionSearchTrees, FeatureFlagSet featureFlagSet, boolean bl, HolderLookup.Provider provider) {
        if (!CreativeModeTabs.tryRebuildTabContents(featureFlagSet, bl, provider)) {
            return false;
        }
        if (sessionSearchTrees != null) {
            List<ItemStack> list = List.copyOf(CreativeModeTabs.searchTab().getDisplayItems());
            sessionSearchTrees.updateCreativeTooltips(provider, list);
            sessionSearchTrees.updateCreativeTags(list);
        }
        return true;
    }

    private void refreshCurrentTabContents(Collection<ItemStack> collection) {
        int n = ((ItemPickerMenu)this.menu).getRowIndexForScroll(this.scrollOffs);
        ((ItemPickerMenu)this.menu).items.clear();
        if (selectedTab.getType() == CreativeModeTab.Type.SEARCH) {
            this.refreshSearchResults();
        } else {
            ((ItemPickerMenu)this.menu).items.addAll(collection);
        }
        this.scrollOffs = ((ItemPickerMenu)this.menu).getScrollForRowIndex(n);
        ((ItemPickerMenu)this.menu).scrollTo(this.scrollOffs);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        if (this.minecraft == null) {
            return;
        }
        LocalPlayer localPlayer = this.minecraft.player;
        if (localPlayer != null) {
            this.tryRefreshInvalidatedTabs(localPlayer.connection.enabledFeatures(), this.hasPermissions(localPlayer), localPlayer.level().registryAccess());
            if (!localPlayer.hasInfiniteMaterials()) {
                this.minecraft.setScreen(new InventoryScreen(localPlayer));
            }
        }
    }

    @Override
    protected void slotClicked(@Nullable Slot slot, int n, int n2, ClickType clickType) {
        if (this.isCreativeSlot(slot)) {
            this.searchBox.moveCursorToEnd(false);
            this.searchBox.setHighlightPos(0);
        }
        boolean bl = clickType == ClickType.QUICK_MOVE;
        ClickType clickType2 = clickType = n == -999 && clickType == ClickType.PICKUP ? ClickType.THROW : clickType;
        if (clickType == ClickType.THROW && !this.minecraft.player.canDropItems()) {
            return;
        }
        this.onMouseClickAction(slot, clickType);
        if (slot != null || selectedTab.getType() == CreativeModeTab.Type.INVENTORY || clickType == ClickType.QUICK_CRAFT) {
            if (slot != null && !slot.mayPickup(this.minecraft.player)) {
                return;
            }
            if (slot == this.destroyItemSlot && bl) {
                for (int i = 0; i < this.minecraft.player.inventoryMenu.getItems().size(); ++i) {
                    this.minecraft.player.inventoryMenu.getSlot(i).set(ItemStack.EMPTY);
                    this.minecraft.gameMode.handleCreativeModeItemAdd(ItemStack.EMPTY, i);
                }
            } else if (selectedTab.getType() == CreativeModeTab.Type.INVENTORY) {
                if (slot == this.destroyItemSlot) {
                    ((ItemPickerMenu)this.menu).setCarried(ItemStack.EMPTY);
                } else if (clickType == ClickType.THROW && slot != null && slot.hasItem()) {
                    ItemStack itemStack = slot.remove(n2 == 0 ? 1 : slot.getItem().getMaxStackSize());
                    ItemStack itemStack2 = slot.getItem();
                    this.minecraft.player.drop(itemStack, true);
                    this.minecraft.gameMode.handleCreativeModeItemDrop(itemStack);
                    this.minecraft.gameMode.handleCreativeModeItemAdd(itemStack2, ((SlotWrapper)slot).target.index);
                } else if (clickType == ClickType.THROW && n == -999 && !((ItemPickerMenu)this.menu).getCarried().isEmpty()) {
                    this.minecraft.player.drop(((ItemPickerMenu)this.menu).getCarried(), true);
                    this.minecraft.gameMode.handleCreativeModeItemDrop(((ItemPickerMenu)this.menu).getCarried());
                    ((ItemPickerMenu)this.menu).setCarried(ItemStack.EMPTY);
                } else {
                    this.minecraft.player.inventoryMenu.clicked(slot == null ? n : ((SlotWrapper)slot).target.index, n2, clickType, this.minecraft.player);
                    this.minecraft.player.inventoryMenu.broadcastChanges();
                }
            } else if (clickType != ClickType.QUICK_CRAFT && slot.container == CONTAINER) {
                ItemStack itemStack = ((ItemPickerMenu)this.menu).getCarried();
                ItemStack itemStack3 = slot.getItem();
                if (clickType == ClickType.SWAP) {
                    if (!itemStack3.isEmpty()) {
                        this.minecraft.player.getInventory().setItem(n2, itemStack3.copyWithCount(itemStack3.getMaxStackSize()));
                        this.minecraft.player.inventoryMenu.broadcastChanges();
                    }
                    return;
                }
                if (clickType == ClickType.CLONE) {
                    if (((ItemPickerMenu)this.menu).getCarried().isEmpty() && slot.hasItem()) {
                        ItemStack itemStack4 = slot.getItem();
                        ((ItemPickerMenu)this.menu).setCarried(itemStack4.copyWithCount(itemStack4.getMaxStackSize()));
                    }
                    return;
                }
                if (clickType == ClickType.THROW) {
                    if (!itemStack3.isEmpty()) {
                        ItemStack itemStack5 = itemStack3.copyWithCount(n2 == 0 ? 1 : itemStack3.getMaxStackSize());
                        this.minecraft.player.drop(itemStack5, true);
                        this.minecraft.gameMode.handleCreativeModeItemDrop(itemStack5);
                    }
                    return;
                }
                if (!itemStack.isEmpty() && !itemStack3.isEmpty() && ItemStack.isSameItemSameComponents(itemStack, itemStack3)) {
                    if (n2 == 0) {
                        if (bl) {
                            itemStack.setCount(itemStack.getMaxStackSize());
                        } else if (itemStack.getCount() < itemStack.getMaxStackSize()) {
                            itemStack.grow(1);
                        }
                    } else {
                        itemStack.shrink(1);
                    }
                } else if (itemStack3.isEmpty() || !itemStack.isEmpty()) {
                    if (n2 == 0) {
                        ((ItemPickerMenu)this.menu).setCarried(ItemStack.EMPTY);
                    } else if (!((ItemPickerMenu)this.menu).getCarried().isEmpty()) {
                        ((ItemPickerMenu)this.menu).getCarried().shrink(1);
                    }
                } else {
                    int n3 = bl ? itemStack3.getMaxStackSize() : itemStack3.getCount();
                    ((ItemPickerMenu)this.menu).setCarried(itemStack3.copyWithCount(n3));
                }
            } else if (this.menu != null) {
                ItemStack itemStack = slot == null ? ItemStack.EMPTY : ((ItemPickerMenu)this.menu).getSlot(slot.index).getItem();
                ((ItemPickerMenu)this.menu).clicked(slot == null ? n : slot.index, n2, clickType, this.minecraft.player);
                if (AbstractContainerMenu.getQuickcraftHeader(n2) == 2) {
                    for (int i = 0; i < 9; ++i) {
                        this.minecraft.gameMode.handleCreativeModeItemAdd(((ItemPickerMenu)this.menu).getSlot(45 + i).getItem(), 36 + i);
                    }
                } else if (slot != null && Inventory.isHotbarSlot(slot.getContainerSlot()) && selectedTab.getType() != CreativeModeTab.Type.INVENTORY) {
                    if (clickType == ClickType.THROW && !itemStack.isEmpty() && !((ItemPickerMenu)this.menu).getCarried().isEmpty()) {
                        int n4 = n2 == 0 ? 1 : itemStack.getCount();
                        ItemStack itemStack6 = itemStack.copyWithCount(n4);
                        itemStack.shrink(n4);
                        this.minecraft.player.drop(itemStack6, true);
                        this.minecraft.gameMode.handleCreativeModeItemDrop(itemStack6);
                    }
                    this.minecraft.player.inventoryMenu.broadcastChanges();
                }
            }
        } else if (!((ItemPickerMenu)this.menu).getCarried().isEmpty() && this.hasClickedOutside) {
            if (!this.minecraft.player.canDropItems()) {
                return;
            }
            if (n2 == 0) {
                this.minecraft.player.drop(((ItemPickerMenu)this.menu).getCarried(), true);
                this.minecraft.gameMode.handleCreativeModeItemDrop(((ItemPickerMenu)this.menu).getCarried());
                ((ItemPickerMenu)this.menu).setCarried(ItemStack.EMPTY);
            }
            if (n2 == 1) {
                ItemStack itemStack = ((ItemPickerMenu)this.menu).getCarried().split(1);
                this.minecraft.player.drop(itemStack, true);
                this.minecraft.gameMode.handleCreativeModeItemDrop(itemStack);
            }
        }
    }

    private boolean isCreativeSlot(@Nullable Slot slot) {
        return slot != null && slot.container == CONTAINER;
    }

    @Override
    protected void init() {
        if (this.minecraft.player.hasInfiniteMaterials()) {
            super.init();
            this.searchBox = new EditBox(this.font, this.leftPos + 82, this.topPos + 6, 80, this.font.lineHeight, Component.translatable("itemGroup.search"));
            this.searchBox.setMaxLength(50);
            this.searchBox.setBordered(false);
            this.searchBox.setVisible(false);
            this.searchBox.setTextColor(-1);
            this.addWidget(this.searchBox);
            CreativeModeTab creativeModeTab = selectedTab;
            selectedTab = CreativeModeTabs.getDefaultTab();
            this.selectTab(creativeModeTab);
            this.minecraft.player.inventoryMenu.removeSlotListener(this.listener);
            this.listener = new CreativeInventoryListener(this.minecraft);
            this.minecraft.player.inventoryMenu.addSlotListener(this.listener);
            if (!selectedTab.shouldDisplay()) {
                this.selectTab(CreativeModeTabs.getDefaultTab());
            }
        } else {
            this.minecraft.setScreen(new InventoryScreen(this.minecraft.player));
        }
    }

    @Override
    public void resize(Minecraft minecraft, int n, int n2) {
        int n3 = ((ItemPickerMenu)this.menu).getRowIndexForScroll(this.scrollOffs);
        String string = this.searchBox.getValue();
        this.init(minecraft, n, n2);
        this.searchBox.setValue(string);
        if (!this.searchBox.getValue().isEmpty()) {
            this.refreshSearchResults();
        }
        this.scrollOffs = ((ItemPickerMenu)this.menu).getScrollForRowIndex(n3);
        ((ItemPickerMenu)this.menu).scrollTo(this.scrollOffs);
    }

    @Override
    public void removed() {
        super.removed();
        if (this.minecraft.player != null && this.minecraft.player.getInventory() != null) {
            this.minecraft.player.inventoryMenu.removeSlotListener(this.listener);
        }
    }

    @Override
    public boolean charTyped(char c, int n) {
        if (this.ignoreTextInput) {
            return false;
        }
        if (selectedTab.getType() != CreativeModeTab.Type.SEARCH) {
            return false;
        }
        String string = this.searchBox.getValue();
        if (this.searchBox.charTyped(c, n)) {
            if (!Objects.equals(string, this.searchBox.getValue())) {
                this.refreshSearchResults();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        this.ignoreTextInput = false;
        if (selectedTab.getType() != CreativeModeTab.Type.SEARCH) {
            if (this.minecraft.options.keyChat.matches(n, n2)) {
                this.ignoreTextInput = true;
                this.selectTab(CreativeModeTabs.searchTab());
                return true;
            }
            return super.keyPressed(n, n2, n3);
        }
        boolean bl = !this.isCreativeSlot(this.hoveredSlot) || this.hoveredSlot.hasItem();
        boolean bl2 = InputConstants.getKey(n, n2).getNumericKeyValue().isPresent();
        if (bl && bl2 && this.checkHotbarKeyPressed(n, n2)) {
            this.ignoreTextInput = true;
            return true;
        }
        String string = this.searchBox.getValue();
        if (this.searchBox.keyPressed(n, n2, n3)) {
            if (!Objects.equals(string, this.searchBox.getValue())) {
                this.refreshSearchResults();
            }
            return true;
        }
        if (this.searchBox.isFocused() && this.searchBox.isVisible() && n != 256) {
            return true;
        }
        return super.keyPressed(n, n2, n3);
    }

    @Override
    public boolean keyReleased(int n, int n2, int n3) {
        this.ignoreTextInput = false;
        return super.keyReleased(n, n2, n3);
    }

    private void refreshSearchResults() {
        ((ItemPickerMenu)this.menu).items.clear();
        this.visibleTags.clear();
        String string = this.searchBox.getValue();
        if (string.isEmpty()) {
            ((ItemPickerMenu)this.menu).items.addAll(selectedTab.getDisplayItems());
        } else {
            ClientPacketListener clientPacketListener = this.minecraft.getConnection();
            if (clientPacketListener != null) {
                SearchTree<ItemStack> searchTree;
                SessionSearchTrees sessionSearchTrees = clientPacketListener.searchTrees();
                if (string.startsWith("#")) {
                    string = string.substring(1);
                    searchTree = sessionSearchTrees.creativeTagSearch();
                    this.updateVisibleTags(string);
                } else {
                    searchTree = sessionSearchTrees.creativeNameSearch();
                }
                ((ItemPickerMenu)this.menu).items.addAll(searchTree.search(string.toLowerCase(Locale.ROOT)));
            }
        }
        this.scrollOffs = 0.0f;
        ((ItemPickerMenu)this.menu).scrollTo(0.0f);
    }

    private void updateVisibleTags(String string) {
        Predicate<ResourceLocation> predicate;
        int n = string.indexOf(58);
        if (n == -1) {
            predicate = resourceLocation -> resourceLocation.getPath().contains(string);
        } else {
            String string2 = string.substring(0, n).trim();
            String string3 = string.substring(n + 1).trim();
            predicate = resourceLocation -> resourceLocation.getNamespace().contains(string2) && resourceLocation.getPath().contains(string3);
        }
        BuiltInRegistries.ITEM.getTags().map(HolderSet.Named::key).filter(tagKey -> predicate.test(tagKey.location())).forEach(this.visibleTags::add);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int n, int n2) {
        if (selectedTab.showTitle()) {
            guiGraphics.drawString(this.font, selectedTab.getDisplayName(), 8, 6, -12566464, false);
        }
    }

    @Override
    public boolean mouseClicked(double d, double d2, int n) {
        if (n == 0) {
            double d3 = d - (double)this.leftPos;
            double d4 = d2 - (double)this.topPos;
            for (CreativeModeTab creativeModeTab : CreativeModeTabs.tabs()) {
                if (!this.checkTabClicked(creativeModeTab, d3, d4)) continue;
                return true;
            }
            if (selectedTab.getType() != CreativeModeTab.Type.INVENTORY && this.insideScrollbar(d, d2)) {
                this.scrolling = this.canScroll();
                return true;
            }
        }
        return super.mouseClicked(d, d2, n);
    }

    @Override
    public boolean mouseReleased(double d, double d2, int n) {
        if (n == 0) {
            double d3 = d - (double)this.leftPos;
            double d4 = d2 - (double)this.topPos;
            this.scrolling = false;
            for (CreativeModeTab creativeModeTab : CreativeModeTabs.tabs()) {
                if (!this.checkTabClicked(creativeModeTab, d3, d4)) continue;
                this.selectTab(creativeModeTab);
                return true;
            }
        }
        return super.mouseReleased(d, d2, n);
    }

    private boolean canScroll() {
        return selectedTab.canScroll() && ((ItemPickerMenu)this.menu).canScroll();
    }

    private void selectTab(CreativeModeTab creativeModeTab) {
        Object object;
        int n;
        int n2;
        Object object2;
        CreativeModeTab creativeModeTab2 = selectedTab;
        selectedTab = creativeModeTab;
        this.quickCraftSlots.clear();
        ((ItemPickerMenu)this.menu).items.clear();
        this.clearDraggingState();
        if (selectedTab.getType() == CreativeModeTab.Type.HOTBAR) {
            object2 = this.minecraft.getHotbarManager();
            for (n2 = 0; n2 < 9; ++n2) {
                Hotbar hotbar = ((HotbarManager)object2).get(n2);
                if (hotbar.isEmpty()) {
                    for (n = 0; n < 9; ++n) {
                        if (n == n2) {
                            object = new ItemStack(Items.PAPER);
                            ((ItemStack)object).set(DataComponents.CREATIVE_SLOT_LOCK, Unit.INSTANCE);
                            Component component = this.minecraft.options.keyHotbarSlots[n2].getTranslatedKeyMessage();
                            Component component2 = this.minecraft.options.keySaveHotbarActivator.getTranslatedKeyMessage();
                            ((ItemStack)object).set(DataComponents.ITEM_NAME, Component.translatable("inventory.hotbarInfo", component2, component));
                            ((ItemPickerMenu)this.menu).items.add((ItemStack)object);
                            continue;
                        }
                        ((ItemPickerMenu)this.menu).items.add(ItemStack.EMPTY);
                    }
                    continue;
                }
                ((ItemPickerMenu)this.menu).items.addAll(hotbar.load(this.minecraft.level.registryAccess()));
            }
        } else if (selectedTab.getType() == CreativeModeTab.Type.CATEGORY) {
            ((ItemPickerMenu)this.menu).items.addAll(selectedTab.getDisplayItems());
        }
        if (selectedTab.getType() == CreativeModeTab.Type.INVENTORY) {
            object2 = this.minecraft.player.inventoryMenu;
            if (this.originalSlots == null) {
                this.originalSlots = ImmutableList.copyOf((Collection)((ItemPickerMenu)this.menu).slots);
            }
            ((ItemPickerMenu)this.menu).slots.clear();
            for (n2 = 0; n2 < ((AbstractContainerMenu)object2).slots.size(); ++n2) {
                int n3;
                if (n2 >= 5 && n2 < 9) {
                    int n4 = n2 - 5;
                    var8_12 = n4 / 2;
                    var9_14 = n4 % 2;
                    n3 = 54 + var8_12 * 54;
                    n = 6 + var9_14 * 27;
                } else if (n2 >= 0 && n2 < 5) {
                    n3 = -2000;
                    n = -2000;
                } else if (n2 == 45) {
                    n3 = 35;
                    n = 20;
                } else {
                    int n5 = n2 - 9;
                    var8_12 = n5 % 9;
                    var9_14 = n5 / 9;
                    n3 = 9 + var8_12 * 18;
                    n = n2 >= 36 ? 112 : 54 + var9_14 * 18;
                }
                object = new SlotWrapper(((AbstractContainerMenu)object2).slots.get(n2), n2, n3, n);
                ((ItemPickerMenu)this.menu).slots.add(object);
            }
            this.destroyItemSlot = new Slot(CONTAINER, 0, 173, 112);
            ((ItemPickerMenu)this.menu).slots.add(this.destroyItemSlot);
        } else if (creativeModeTab2.getType() == CreativeModeTab.Type.INVENTORY) {
            ((ItemPickerMenu)this.menu).slots.clear();
            ((ItemPickerMenu)this.menu).slots.addAll(this.originalSlots);
            this.originalSlots = null;
        }
        if (selectedTab.getType() == CreativeModeTab.Type.SEARCH) {
            this.searchBox.setVisible(true);
            this.searchBox.setCanLoseFocus(false);
            this.searchBox.setFocused(true);
            if (creativeModeTab2 != creativeModeTab) {
                this.searchBox.setValue("");
            }
            this.refreshSearchResults();
        } else {
            this.searchBox.setVisible(false);
            this.searchBox.setCanLoseFocus(true);
            this.searchBox.setFocused(false);
            this.searchBox.setValue("");
        }
        this.scrollOffs = 0.0f;
        ((ItemPickerMenu)this.menu).scrollTo(0.0f);
    }

    @Override
    public boolean mouseScrolled(double d, double d2, double d3, double d4) {
        if (super.mouseScrolled(d, d2, d3, d4)) {
            return true;
        }
        if (!this.canScroll()) {
            return false;
        }
        this.scrollOffs = ((ItemPickerMenu)this.menu).subtractInputFromScroll(this.scrollOffs, d4);
        ((ItemPickerMenu)this.menu).scrollTo(this.scrollOffs);
        return true;
    }

    @Override
    protected boolean hasClickedOutside(double d, double d2, int n, int n2, int n3) {
        boolean bl = d < (double)n || d2 < (double)n2 || d >= (double)(n + this.imageWidth) || d2 >= (double)(n2 + this.imageHeight);
        this.hasClickedOutside = bl && !this.checkTabClicked(selectedTab, d, d2);
        return this.hasClickedOutside;
    }

    protected boolean insideScrollbar(double d, double d2) {
        int n = this.leftPos;
        int n2 = this.topPos;
        int n3 = n + 175;
        int n4 = n2 + 18;
        int n5 = n3 + 14;
        int n6 = n4 + 112;
        return d >= (double)n3 && d2 >= (double)n4 && d < (double)n5 && d2 < (double)n6;
    }

    @Override
    public boolean mouseDragged(double d, double d2, int n, double d3, double d4) {
        if (this.scrolling) {
            int n2 = this.topPos + 18;
            int n3 = n2 + 112;
            this.scrollOffs = ((float)d2 - (float)n2 - 7.5f) / ((float)(n3 - n2) - 15.0f);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0f, 1.0f);
            ((ItemPickerMenu)this.menu).scrollTo(this.scrollOffs);
            return true;
        }
        return super.mouseDragged(d, d2, n, d3, d4);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        this.effects.renderEffects(guiGraphics, n, n2);
        super.render(guiGraphics, n, n2, f);
        this.effects.renderTooltip(guiGraphics, n, n2);
        for (CreativeModeTab creativeModeTab : CreativeModeTabs.tabs()) {
            if (this.checkTabHovering(guiGraphics, creativeModeTab, n, n2)) break;
        }
        if (this.destroyItemSlot != null && selectedTab.getType() == CreativeModeTab.Type.INVENTORY && this.isHovering(this.destroyItemSlot.x, this.destroyItemSlot.y, 16, 16, n, n2)) {
            guiGraphics.setTooltipForNextFrame(this.font, TRASH_SLOT_TOOLTIP, n, n2);
        }
        this.renderTooltip(guiGraphics, n, n2);
    }

    @Override
    public boolean showsActiveEffects() {
        return this.effects.canSeeEffects();
    }

    @Override
    public List<Component> getTooltipFromContainerItem(ItemStack itemStack) {
        boolean bl = this.hoveredSlot != null && this.hoveredSlot instanceof CustomCreativeSlot;
        boolean bl2 = selectedTab.getType() == CreativeModeTab.Type.CATEGORY;
        boolean bl3 = selectedTab.getType() == CreativeModeTab.Type.SEARCH;
        TooltipFlag.Default default_ = this.minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
        TooltipFlag.Default default_2 = bl ? default_.asCreative() : default_;
        List<Component> list = itemStack.getTooltipLines(Item.TooltipContext.of(this.minecraft.level), this.minecraft.player, default_2);
        if (list.isEmpty()) {
            return list;
        }
        if (!bl2 || !bl) {
            ArrayList arrayList = Lists.newArrayList(list);
            if (bl3 && bl) {
                this.visibleTags.forEach(tagKey -> {
                    if (itemStack.is((TagKey<Item>)tagKey)) {
                        arrayList.add(1, Component.literal("#" + String.valueOf(tagKey.location())).withStyle(ChatFormatting.DARK_PURPLE));
                    }
                });
            }
            int n = 1;
            for (CreativeModeTab creativeModeTab : CreativeModeTabs.tabs()) {
                if (creativeModeTab.getType() == CreativeModeTab.Type.SEARCH || !creativeModeTab.contains(itemStack)) continue;
                arrayList.add(n++, creativeModeTab.getDisplayName().copy().withStyle(ChatFormatting.BLUE));
            }
            return arrayList;
        }
        return list;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float f, int n, int n2) {
        for (CreativeModeTab creativeModeTab : CreativeModeTabs.tabs()) {
            if (creativeModeTab == selectedTab) continue;
            this.renderTabButton(guiGraphics, creativeModeTab);
        }
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, selectedTab.getBackgroundTexture(), this.leftPos, this.topPos, 0.0f, 0.0f, this.imageWidth, this.imageHeight, 256, 256);
        this.searchBox.render(guiGraphics, n, n2, f);
        int n3 = this.leftPos + 175;
        int n4 = this.topPos + 18;
        int n5 = n4 + 112;
        if (selectedTab.canScroll()) {
            ResourceLocation resourceLocation = this.canScroll() ? SCROLLER_SPRITE : SCROLLER_DISABLED_SPRITE;
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation, n3, n4 + (int)((float)(n5 - n4 - 17) * this.scrollOffs), 12, 15);
        }
        this.renderTabButton(guiGraphics, selectedTab);
        if (selectedTab.getType() == CreativeModeTab.Type.INVENTORY) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, this.leftPos + 73, this.topPos + 6, this.leftPos + 105, this.topPos + 49, 20, 0.0625f, n, n2, this.minecraft.player);
        }
    }

    private int getTabX(CreativeModeTab creativeModeTab) {
        int n = creativeModeTab.column();
        int n2 = 27;
        int n3 = 27 * n;
        if (creativeModeTab.isAlignedRight()) {
            n3 = this.imageWidth - 27 * (7 - n) + 1;
        }
        return n3;
    }

    private int getTabY(CreativeModeTab creativeModeTab) {
        int n = 0;
        n = creativeModeTab.row() == CreativeModeTab.Row.TOP ? (n -= 32) : (n += this.imageHeight);
        return n;
    }

    protected boolean checkTabClicked(CreativeModeTab creativeModeTab, double d, double d2) {
        int n = this.getTabX(creativeModeTab);
        int n2 = this.getTabY(creativeModeTab);
        return d >= (double)n && d <= (double)(n + 26) && d2 >= (double)n2 && d2 <= (double)(n2 + 32);
    }

    protected boolean checkTabHovering(GuiGraphics guiGraphics, CreativeModeTab creativeModeTab, int n, int n2) {
        int n3;
        int n4 = this.getTabX(creativeModeTab);
        if (this.isHovering(n4 + 3, (n3 = this.getTabY(creativeModeTab)) + 3, 21, 27, n, n2)) {
            guiGraphics.setTooltipForNextFrame(this.font, creativeModeTab.getDisplayName(), n, n2);
            return true;
        }
        return false;
    }

    protected void renderTabButton(GuiGraphics guiGraphics, CreativeModeTab creativeModeTab) {
        boolean bl = creativeModeTab == selectedTab;
        boolean bl2 = creativeModeTab.row() == CreativeModeTab.Row.TOP;
        int n = creativeModeTab.column();
        int n2 = this.leftPos + this.getTabX(creativeModeTab);
        int n3 = this.topPos - (bl2 ? 28 : -(this.imageHeight - 4));
        ResourceLocation[] resourceLocationArray = bl2 ? (bl ? SELECTED_TOP_TABS : UNSELECTED_TOP_TABS) : (bl ? SELECTED_BOTTOM_TABS : UNSELECTED_BOTTOM_TABS);
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocationArray[Mth.clamp(n, 0, resourceLocationArray.length)], n2, n3, 26, 32);
        int n4 = n2 + 13 - 8;
        int n5 = n3 + 16 - 8 + (bl2 ? 1 : -1);
        guiGraphics.renderItem(creativeModeTab.getIconItem(), n4, n5);
    }

    public boolean isInventoryOpen() {
        return selectedTab.getType() == CreativeModeTab.Type.INVENTORY;
    }

    public static void handleHotbarLoadOrSave(Minecraft minecraft, int n, boolean bl, boolean bl2) {
        LocalPlayer localPlayer = minecraft.player;
        RegistryAccess registryAccess = localPlayer.level().registryAccess();
        HotbarManager hotbarManager = minecraft.getHotbarManager();
        Hotbar hotbar = hotbarManager.get(n);
        if (bl) {
            List<ItemStack> list = hotbar.load(registryAccess);
            for (int i = 0; i < Inventory.getSelectionSize(); ++i) {
                ItemStack itemStack = list.get(i);
                localPlayer.getInventory().setItem(i, itemStack);
                minecraft.gameMode.handleCreativeModeItemAdd(itemStack, 36 + i);
            }
            localPlayer.inventoryMenu.broadcastChanges();
        } else if (bl2) {
            hotbar.storeFrom(localPlayer.getInventory(), registryAccess);
            Component component = minecraft.options.keyHotbarSlots[n].getTranslatedKeyMessage();
            Component component2 = minecraft.options.keyLoadHotbarActivator.getTranslatedKeyMessage();
            MutableComponent mutableComponent = Component.translatable("inventory.hotbarSaved", component2, component);
            minecraft.gui.setOverlayMessage(mutableComponent, false);
            minecraft.getNarrator().saySystemNow(mutableComponent);
            hotbarManager.save();
        }
    }

    public static class ItemPickerMenu
    extends AbstractContainerMenu {
        public final NonNullList<ItemStack> items = NonNullList.create();
        private final AbstractContainerMenu inventoryMenu;

        public ItemPickerMenu(Player player) {
            super(null, 0);
            this.inventoryMenu = player.inventoryMenu;
            Inventory inventory = player.getInventory();
            for (int i = 0; i < 5; ++i) {
                for (int j = 0; j < 9; ++j) {
                    this.addSlot(new CustomCreativeSlot(CONTAINER, i * 9 + j, 9 + j * 18, 18 + i * 18));
                }
            }
            this.addInventoryHotbarSlots(inventory, 9, 112);
            this.scrollTo(0.0f);
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        protected int calculateRowCount() {
            return Mth.positiveCeilDiv(this.items.size(), 9) - 5;
        }

        protected int getRowIndexForScroll(float f) {
            return Math.max((int)((double)(f * (float)this.calculateRowCount()) + 0.5), 0);
        }

        protected float getScrollForRowIndex(int n) {
            return Mth.clamp((float)n / (float)this.calculateRowCount(), 0.0f, 1.0f);
        }

        protected float subtractInputFromScroll(float f, double d) {
            return Mth.clamp(f - (float)(d / (double)this.calculateRowCount()), 0.0f, 1.0f);
        }

        public void scrollTo(float f) {
            int n = this.getRowIndexForScroll(f);
            for (int i = 0; i < 5; ++i) {
                for (int j = 0; j < 9; ++j) {
                    int n2 = j + (i + n) * 9;
                    if (n2 >= 0 && n2 < this.items.size()) {
                        CONTAINER.setItem(j + i * 9, this.items.get(n2));
                        continue;
                    }
                    CONTAINER.setItem(j + i * 9, ItemStack.EMPTY);
                }
            }
        }

        public boolean canScroll() {
            return this.items.size() > 45;
        }

        @Override
        public ItemStack quickMoveStack(Player player, int n) {
            Slot slot;
            if (n >= this.slots.size() - 9 && n < this.slots.size() && (slot = (Slot)this.slots.get(n)) != null && slot.hasItem()) {
                slot.setByPlayer(ItemStack.EMPTY);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
            return slot.container != CONTAINER;
        }

        @Override
        public boolean canDragTo(Slot slot) {
            return slot.container != CONTAINER;
        }

        @Override
        public ItemStack getCarried() {
            return this.inventoryMenu.getCarried();
        }

        @Override
        public void setCarried(ItemStack itemStack) {
            this.inventoryMenu.setCarried(itemStack);
        }
    }

    static class SlotWrapper
    extends Slot {
        final Slot target;

        public SlotWrapper(Slot slot, int n, int n2, int n3) {
            super(slot.container, n, n2, n3);
            this.target = slot;
        }

        @Override
        public void onTake(Player player, ItemStack itemStack) {
            this.target.onTake(player, itemStack);
        }

        @Override
        public boolean mayPlace(ItemStack itemStack) {
            return this.target.mayPlace(itemStack);
        }

        @Override
        public ItemStack getItem() {
            return this.target.getItem();
        }

        @Override
        public boolean hasItem() {
            return this.target.hasItem();
        }

        @Override
        public void setByPlayer(ItemStack itemStack, ItemStack itemStack2) {
            this.target.setByPlayer(itemStack, itemStack2);
        }

        @Override
        public void set(ItemStack itemStack) {
            this.target.set(itemStack);
        }

        @Override
        public void setChanged() {
            this.target.setChanged();
        }

        @Override
        public int getMaxStackSize() {
            return this.target.getMaxStackSize();
        }

        @Override
        public int getMaxStackSize(ItemStack itemStack) {
            return this.target.getMaxStackSize(itemStack);
        }

        @Override
        @Nullable
        public ResourceLocation getNoItemIcon() {
            return this.target.getNoItemIcon();
        }

        @Override
        public ItemStack remove(int n) {
            return this.target.remove(n);
        }

        @Override
        public boolean isActive() {
            return this.target.isActive();
        }

        @Override
        public boolean mayPickup(Player player) {
            return this.target.mayPickup(player);
        }
    }

    static class CustomCreativeSlot
    extends Slot {
        public CustomCreativeSlot(Container container, int n, int n2, int n3) {
            super(container, n, n2, n3);
        }

        @Override
        public boolean mayPickup(Player player) {
            ItemStack itemStack = this.getItem();
            if (super.mayPickup(player) && !itemStack.isEmpty()) {
                return itemStack.isItemEnabled(player.level().enabledFeatures()) && !itemStack.has(DataComponents.CREATIVE_SLOT_LOCK);
            }
            return itemStack.isEmpty();
        }
    }
}

