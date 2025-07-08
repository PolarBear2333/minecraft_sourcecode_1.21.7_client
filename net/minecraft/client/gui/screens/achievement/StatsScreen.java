/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Sets
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.achievement;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.LoadingDotsWidget;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.Stats;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class StatsScreen
extends Screen {
    private static final Component TITLE = Component.translatable("gui.stats");
    static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");
    static final ResourceLocation HEADER_SPRITE = ResourceLocation.withDefaultNamespace("statistics/header");
    static final ResourceLocation SORT_UP_SPRITE = ResourceLocation.withDefaultNamespace("statistics/sort_up");
    static final ResourceLocation SORT_DOWN_SPRITE = ResourceLocation.withDefaultNamespace("statistics/sort_down");
    private static final Component PENDING_TEXT = Component.translatable("multiplayer.downloadingStats");
    static final Component NO_VALUE_DISPLAY = Component.translatable("stats.none");
    private static final Component GENERAL_BUTTON = Component.translatable("stat.generalButton");
    private static final Component ITEMS_BUTTON = Component.translatable("stat.itemsButton");
    private static final Component MOBS_BUTTON = Component.translatable("stat.mobsButton");
    protected final Screen lastScreen;
    private static final int LIST_WIDTH = 280;
    private static final int PADDING = 5;
    private static final int FOOTER_HEIGHT = 58;
    private HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this, 33, 58);
    @Nullable
    private GeneralStatisticsList statsList;
    @Nullable
    ItemStatisticsList itemStatsList;
    @Nullable
    private MobsStatisticsList mobsStatsList;
    final StatsCounter stats;
    @Nullable
    private ObjectSelectionList<?> activeList;
    private boolean isLoading = true;

    public StatsScreen(Screen screen, StatsCounter statsCounter) {
        super(TITLE);
        this.lastScreen = screen;
        this.stats = statsCounter;
    }

    @Override
    protected void init() {
        this.layout.addToContents(new LoadingDotsWidget(this.font, PENDING_TEXT));
        this.minecraft.getConnection().send(new ServerboundClientCommandPacket(ServerboundClientCommandPacket.Action.REQUEST_STATS));
    }

    public void initLists() {
        this.statsList = new GeneralStatisticsList(this.minecraft);
        this.itemStatsList = new ItemStatisticsList(this.minecraft);
        this.mobsStatsList = new MobsStatisticsList(this.minecraft);
    }

    public void initButtons() {
        HeaderAndFooterLayout headerAndFooterLayout = new HeaderAndFooterLayout(this, 33, 58);
        headerAndFooterLayout.addTitleHeader(TITLE, this.font);
        LinearLayout linearLayout = headerAndFooterLayout.addToFooter(LinearLayout.vertical()).spacing(5);
        linearLayout.defaultCellSetting().alignHorizontallyCenter();
        LinearLayout linearLayout2 = linearLayout.addChild(LinearLayout.horizontal()).spacing(5);
        linearLayout2.addChild(Button.builder(GENERAL_BUTTON, button -> this.setActiveList(this.statsList)).width(120).build());
        Button button2 = linearLayout2.addChild(Button.builder(ITEMS_BUTTON, button -> this.setActiveList(this.itemStatsList)).width(120).build());
        Button button3 = linearLayout2.addChild(Button.builder(MOBS_BUTTON, button -> this.setActiveList(this.mobsStatsList)).width(120).build());
        linearLayout.addChild(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).width(200).build());
        if (this.itemStatsList != null && this.itemStatsList.children().isEmpty()) {
            button2.active = false;
        }
        if (this.mobsStatsList != null && this.mobsStatsList.children().isEmpty()) {
            button3.active = false;
        }
        this.layout = headerAndFooterLayout;
        this.layout.visitWidgets(guiEventListener -> {
            AbstractWidget cfr_ignored_0 = (AbstractWidget)this.addRenderableWidget(guiEventListener);
        });
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        if (this.activeList != null) {
            this.activeList.updateSize(this.width, this.layout);
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.lastScreen);
    }

    public void onStatsUpdated() {
        if (this.isLoading) {
            this.initLists();
            this.setActiveList(this.statsList);
            this.initButtons();
            this.setInitialFocus();
            this.isLoading = false;
        }
    }

    public void setActiveList(@Nullable ObjectSelectionList<?> objectSelectionList) {
        if (this.activeList != null) {
            this.removeWidget(this.activeList);
        }
        if (objectSelectionList != null) {
            this.addRenderableWidget(objectSelectionList);
            this.activeList = objectSelectionList;
            this.repositionElements();
        }
    }

    static String getTranslationKey(Stat<ResourceLocation> stat) {
        return "stat." + stat.getValue().toString().replace(':', '.');
    }

    class GeneralStatisticsList
    extends ObjectSelectionList<Entry> {
        public GeneralStatisticsList(Minecraft minecraft) {
            super(minecraft, StatsScreen.this.width, StatsScreen.this.height - 33 - 58, 33, 14);
            ObjectArrayList objectArrayList = new ObjectArrayList(Stats.CUSTOM.iterator());
            objectArrayList.sort(Comparator.comparing(stat -> I18n.get(StatsScreen.getTranslationKey(stat), new Object[0])));
            for (Stat stat2 : objectArrayList) {
                this.addEntry(new Entry(stat2));
            }
        }

        @Override
        public int getRowWidth() {
            return 280;
        }

        class Entry
        extends ObjectSelectionList.Entry<Entry> {
            private final Stat<ResourceLocation> stat;
            private final Component statDisplay;

            Entry(Stat<ResourceLocation> stat) {
                this.stat = stat;
                this.statDisplay = Component.translatable(StatsScreen.getTranslationKey(stat));
            }

            private String getValueText() {
                return this.stat.format(StatsScreen.this.stats.getValue(this.stat));
            }

            @Override
            public void render(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
                int n8 = n2 + n5 / 2 - ((StatsScreen)StatsScreen.this).font.lineHeight / 2;
                int n9 = n % 2 == 0 ? -1 : -4539718;
                guiGraphics.drawString(StatsScreen.this.font, this.statDisplay, n3 + 2, n8, n9);
                String string = this.getValueText();
                guiGraphics.drawString(StatsScreen.this.font, string, n3 + n4 - StatsScreen.this.font.width(string) - 4, n8, n9);
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", Component.empty().append(this.statDisplay).append(CommonComponents.SPACE).append(this.getValueText()));
            }
        }
    }

    class ItemStatisticsList
    extends ObjectSelectionList<ItemRow> {
        private static final int SLOT_BG_SIZE = 18;
        private static final int SLOT_STAT_HEIGHT = 22;
        private static final int SLOT_BG_Y = 1;
        private static final int SORT_NONE = 0;
        private static final int SORT_DOWN = -1;
        private static final int SORT_UP = 1;
        private final ResourceLocation[] iconSprites;
        protected final List<StatType<Block>> blockColumns;
        protected final List<StatType<Item>> itemColumns;
        protected final Comparator<ItemRow> itemStatSorter;
        @Nullable
        protected StatType<?> sortColumn;
        protected int headerPressed;
        protected int sortOrder;

        public ItemStatisticsList(Minecraft minecraft) {
            boolean bl;
            super(minecraft, StatsScreen.this.width, StatsScreen.this.height - 33 - 58, 33, 22, 22);
            this.iconSprites = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("statistics/block_mined"), ResourceLocation.withDefaultNamespace("statistics/item_broken"), ResourceLocation.withDefaultNamespace("statistics/item_crafted"), ResourceLocation.withDefaultNamespace("statistics/item_used"), ResourceLocation.withDefaultNamespace("statistics/item_picked_up"), ResourceLocation.withDefaultNamespace("statistics/item_dropped")};
            this.itemStatSorter = new ItemRowComparator();
            this.headerPressed = -1;
            this.blockColumns = Lists.newArrayList();
            this.blockColumns.add(Stats.BLOCK_MINED);
            this.itemColumns = Lists.newArrayList((Object[])new StatType[]{Stats.ITEM_BROKEN, Stats.ITEM_CRAFTED, Stats.ITEM_USED, Stats.ITEM_PICKED_UP, Stats.ITEM_DROPPED});
            Set set = Sets.newIdentityHashSet();
            for (Item featureElement : BuiltInRegistries.ITEM) {
                bl = false;
                for (StatType<Item> statType : this.itemColumns) {
                    if (!statType.contains(featureElement) || StatsScreen.this.stats.getValue(statType.get(featureElement)) <= 0) continue;
                    bl = true;
                }
                if (!bl) continue;
                set.add(featureElement);
            }
            for (Block block : BuiltInRegistries.BLOCK) {
                bl = false;
                for (StatType<FeatureElement> statType : this.blockColumns) {
                    if (!statType.contains(block) || StatsScreen.this.stats.getValue(statType.get(block)) <= 0) continue;
                    bl = true;
                }
                if (!bl) continue;
                set.add(block.asItem());
            }
            set.remove(Items.AIR);
            for (Item item : set) {
                this.addEntry(new ItemRow(item));
            }
        }

        int getColumnX(int n) {
            return 75 + 40 * n;
        }

        @Override
        protected void renderHeader(GuiGraphics guiGraphics, int n, int n2) {
            ResourceLocation resourceLocation;
            int n3;
            if (!this.minecraft.mouseHandler.isLeftPressed()) {
                this.headerPressed = -1;
            }
            for (n3 = 0; n3 < this.iconSprites.length; ++n3) {
                resourceLocation = this.headerPressed == n3 ? SLOT_SPRITE : HEADER_SPRITE;
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation, n + this.getColumnX(n3) - 18, n2 + 1, 18, 18);
            }
            if (this.sortColumn != null) {
                n3 = this.getColumnX(this.getColumnIndex(this.sortColumn)) - 36;
                resourceLocation = this.sortOrder == 1 ? SORT_UP_SPRITE : SORT_DOWN_SPRITE;
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation, n + n3, n2 + 1, 18, 18);
            }
            for (n3 = 0; n3 < this.iconSprites.length; ++n3) {
                int n4 = this.headerPressed == n3 ? 1 : 0;
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.iconSprites[n3], n + this.getColumnX(n3) - 18 + n4, n2 + 1 + n4, 18, 18);
            }
        }

        @Override
        public boolean mouseClicked(double d, double d2, int n) {
            boolean bl = super.mouseClicked(d, d2, n);
            if (!bl && this.clickedHeader((int)(d - ((double)this.getX() + (double)this.width / 2.0 - (double)this.getRowWidth() / 2.0)), (int)(d2 - (double)this.getY()) + (int)this.scrollAmount() - 4)) {
                return true;
            }
            return bl;
        }

        protected boolean clickedHeader(int n, int n2) {
            this.headerPressed = -1;
            for (int i = 0; i < this.iconSprites.length; ++i) {
                int n3 = n - this.getColumnX(i);
                if (n3 < -36 || n3 > 0) continue;
                this.headerPressed = i;
                break;
            }
            if (this.headerPressed >= 0) {
                this.sortByColumn(this.getColumn(this.headerPressed));
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f));
                return true;
            }
            return false;
        }

        @Override
        public int getRowWidth() {
            return 280;
        }

        private StatType<?> getColumn(int n) {
            return n < this.blockColumns.size() ? this.blockColumns.get(n) : this.itemColumns.get(n - this.blockColumns.size());
        }

        private int getColumnIndex(StatType<?> statType) {
            int n = this.blockColumns.indexOf(statType);
            if (n >= 0) {
                return n;
            }
            int n2 = this.itemColumns.indexOf(statType);
            if (n2 >= 0) {
                return n2 + this.blockColumns.size();
            }
            return -1;
        }

        @Override
        protected void renderDecorations(GuiGraphics guiGraphics, int n, int n2) {
            if (n2 < this.getY() || n2 > this.getBottom()) {
                return;
            }
            ItemRow itemRow = (ItemRow)this.getHovered();
            int n3 = this.getRowLeft();
            if (itemRow != null) {
                if (n < n3 || n > n3 + 18) {
                    return;
                }
                Item item = itemRow.getItem();
                guiGraphics.setTooltipForNextFrame(StatsScreen.this.font, item.getName(), n, n2, item.components().get(DataComponents.TOOLTIP_STYLE));
            } else {
                Component component = null;
                int n4 = n - n3;
                for (int i = 0; i < this.iconSprites.length; ++i) {
                    int n5 = this.getColumnX(i);
                    if (n4 < n5 - 18 || n4 > n5) continue;
                    component = this.getColumn(i).getDisplayName();
                    break;
                }
                if (component != null) {
                    guiGraphics.setTooltipForNextFrame(StatsScreen.this.font, component, n, n2);
                }
            }
        }

        protected void sortByColumn(StatType<?> statType) {
            if (statType != this.sortColumn) {
                this.sortColumn = statType;
                this.sortOrder = -1;
            } else if (this.sortOrder == -1) {
                this.sortOrder = 1;
            } else {
                this.sortColumn = null;
                this.sortOrder = 0;
            }
            this.children().sort(this.itemStatSorter);
        }

        class ItemRowComparator
        implements Comparator<ItemRow> {
            ItemRowComparator() {
            }

            @Override
            public int compare(ItemRow itemRow, ItemRow itemRow2) {
                int n;
                int n2;
                Item item = itemRow.getItem();
                Item item2 = itemRow2.getItem();
                if (ItemStatisticsList.this.sortColumn == null) {
                    n2 = 0;
                    n = 0;
                } else if (ItemStatisticsList.this.blockColumns.contains(ItemStatisticsList.this.sortColumn)) {
                    StatType<?> statType = ItemStatisticsList.this.sortColumn;
                    n2 = item instanceof BlockItem ? StatsScreen.this.stats.getValue(statType, ((BlockItem)item).getBlock()) : -1;
                    n = item2 instanceof BlockItem ? StatsScreen.this.stats.getValue(statType, ((BlockItem)item2).getBlock()) : -1;
                } else {
                    StatType<?> statType = ItemStatisticsList.this.sortColumn;
                    n2 = StatsScreen.this.stats.getValue(statType, item);
                    n = StatsScreen.this.stats.getValue(statType, item2);
                }
                if (n2 == n) {
                    return ItemStatisticsList.this.sortOrder * Integer.compare(Item.getId(item), Item.getId(item2));
                }
                return ItemStatisticsList.this.sortOrder * Integer.compare(n2, n);
            }

            @Override
            public /* synthetic */ int compare(Object object, Object object2) {
                return this.compare((ItemRow)object, (ItemRow)object2);
            }
        }

        class ItemRow
        extends ObjectSelectionList.Entry<ItemRow> {
            private final Item item;

            ItemRow(Item item) {
                this.item = item;
            }

            public Item getItem() {
                return this.item;
            }

            @Override
            public void render(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_SPRITE, n3, n2, 18, 18);
                guiGraphics.renderFakeItem(this.item.getDefaultInstance(), n3 + 1, n2 + 1);
                if (StatsScreen.this.itemStatsList != null) {
                    int n8;
                    for (n8 = 0; n8 < StatsScreen.this.itemStatsList.blockColumns.size(); ++n8) {
                        Stat<Block> stat;
                        Item item = this.item;
                        if (item instanceof BlockItem) {
                            BlockItem blockItem = (BlockItem)item;
                            stat = StatsScreen.this.itemStatsList.blockColumns.get(n8).get(blockItem.getBlock());
                        } else {
                            stat = null;
                        }
                        this.renderStat(guiGraphics, stat, n3 + ItemStatisticsList.this.getColumnX(n8), n2 + n5 / 2 - ((StatsScreen)StatsScreen.this).font.lineHeight / 2, n % 2 == 0);
                    }
                    for (n8 = 0; n8 < StatsScreen.this.itemStatsList.itemColumns.size(); ++n8) {
                        this.renderStat(guiGraphics, StatsScreen.this.itemStatsList.itemColumns.get(n8).get(this.item), n3 + ItemStatisticsList.this.getColumnX(n8 + StatsScreen.this.itemStatsList.blockColumns.size()), n2 + n5 / 2 - ((StatsScreen)StatsScreen.this).font.lineHeight / 2, n % 2 == 0);
                    }
                }
            }

            protected void renderStat(GuiGraphics guiGraphics, @Nullable Stat<?> stat, int n, int n2, boolean bl) {
                Component component = stat == null ? NO_VALUE_DISPLAY : Component.literal(stat.format(StatsScreen.this.stats.getValue(stat)));
                guiGraphics.drawString(StatsScreen.this.font, component, n - StatsScreen.this.font.width(component), n2, bl ? -1 : -4539718);
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", this.item.getName());
            }
        }
    }

    class MobsStatisticsList
    extends ObjectSelectionList<MobRow> {
        public MobsStatisticsList(Minecraft minecraft) {
            super(minecraft, StatsScreen.this.width, StatsScreen.this.height - 33 - 58, 33, ((StatsScreen)StatsScreen.this).font.lineHeight * 4);
            for (EntityType entityType : BuiltInRegistries.ENTITY_TYPE) {
                if (StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED.get(entityType)) <= 0 && StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED_BY.get(entityType)) <= 0) continue;
                this.addEntry(new MobRow(entityType));
            }
        }

        @Override
        public int getRowWidth() {
            return 280;
        }

        class MobRow
        extends ObjectSelectionList.Entry<MobRow> {
            private final Component mobName;
            private final Component kills;
            private final Component killedBy;
            private final boolean hasKills;
            private final boolean wasKilledBy;

            public MobRow(EntityType<?> entityType) {
                this.mobName = entityType.getDescription();
                int n = StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED.get(entityType));
                if (n == 0) {
                    this.kills = Component.translatable("stat_type.minecraft.killed.none", this.mobName);
                    this.hasKills = false;
                } else {
                    this.kills = Component.translatable("stat_type.minecraft.killed", n, this.mobName);
                    this.hasKills = true;
                }
                int n2 = StatsScreen.this.stats.getValue(Stats.ENTITY_KILLED_BY.get(entityType));
                if (n2 == 0) {
                    this.killedBy = Component.translatable("stat_type.minecraft.killed_by.none", this.mobName);
                    this.wasKilledBy = false;
                } else {
                    this.killedBy = Component.translatable("stat_type.minecraft.killed_by", this.mobName, n2);
                    this.wasKilledBy = true;
                }
            }

            @Override
            public void render(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
                guiGraphics.drawString(StatsScreen.this.font, this.mobName, n3 + 2, n2 + 1, -1);
                guiGraphics.drawString(StatsScreen.this.font, this.kills, n3 + 2 + 10, n2 + 1 + ((StatsScreen)StatsScreen.this).font.lineHeight, this.hasKills ? -4539718 : -8355712);
                guiGraphics.drawString(StatsScreen.this.font, this.killedBy, n3 + 2 + 10, n2 + 1 + ((StatsScreen)StatsScreen.this).font.lineHeight * 2, this.wasKilledBy ? -4539718 : -8355712);
            }

            @Override
            public Component getNarration() {
                return Component.translatable("narrator.select", CommonComponents.joinForNarration(this.kills, this.killedBy));
            }
        }
    }
}

