/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 *  javax.annotation.Nullable
 */
package net.minecraft.client.gui.screens.worldselection;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.GameRules;

public class EditGameRulesScreen
extends Screen {
    private static final Component TITLE = Component.translatable("editGamerule.title");
    private static final int SPACING = 8;
    final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
    private final Consumer<Optional<GameRules>> exitCallback;
    private final Set<RuleEntry> invalidEntries = Sets.newHashSet();
    private final GameRules gameRules;
    @Nullable
    private RuleList ruleList;
    @Nullable
    private Button doneButton;

    public EditGameRulesScreen(GameRules gameRules, Consumer<Optional<GameRules>> consumer) {
        super(TITLE);
        this.gameRules = gameRules;
        this.exitCallback = consumer;
    }

    @Override
    protected void init() {
        this.layout.addTitleHeader(TITLE, this.font);
        this.ruleList = this.layout.addToContents(new RuleList(this.gameRules));
        LinearLayout linearLayout = this.layout.addToFooter(LinearLayout.horizontal().spacing(8));
        this.doneButton = linearLayout.addChild(Button.builder(CommonComponents.GUI_DONE, button -> this.exitCallback.accept(Optional.of(this.gameRules))).build());
        linearLayout.addChild(Button.builder(CommonComponents.GUI_CANCEL, button -> this.onClose()).build());
        this.layout.visitWidgets(guiEventListener -> {
            AbstractWidget cfr_ignored_0 = (AbstractWidget)this.addRenderableWidget(guiEventListener);
        });
        this.repositionElements();
    }

    @Override
    protected void repositionElements() {
        this.layout.arrangeElements();
        if (this.ruleList != null) {
            this.ruleList.updateSize(this.width, this.layout);
        }
    }

    @Override
    public void onClose() {
        this.exitCallback.accept(Optional.empty());
    }

    private void updateDoneButton() {
        if (this.doneButton != null) {
            this.doneButton.active = this.invalidEntries.isEmpty();
        }
    }

    void markInvalid(RuleEntry ruleEntry) {
        this.invalidEntries.add(ruleEntry);
        this.updateDoneButton();
    }

    void clearInvalid(RuleEntry ruleEntry) {
        this.invalidEntries.remove(ruleEntry);
        this.updateDoneButton();
    }

    public class RuleList
    extends ContainerObjectSelectionList<RuleEntry> {
        private static final int ITEM_HEIGHT = 24;

        public RuleList(final GameRules gameRules) {
            super(Minecraft.getInstance(), EditGameRulesScreen.this.width, EditGameRulesScreen.this.layout.getContentHeight(), EditGameRulesScreen.this.layout.getHeaderHeight(), 24);
            final HashMap hashMap = Maps.newHashMap();
            gameRules.visitGameRuleTypes(new GameRules.GameRuleTypeVisitor(){

                @Override
                public void visitBoolean(GameRules.Key<GameRules.BooleanValue> key, GameRules.Type<GameRules.BooleanValue> type) {
                    this.addEntry(key, (component, list, string, booleanValue) -> new BooleanRuleEntry(EditGameRulesScreen.this, component, list, string, (GameRules.BooleanValue)booleanValue));
                }

                @Override
                public void visitInteger(GameRules.Key<GameRules.IntegerValue> key, GameRules.Type<GameRules.IntegerValue> type) {
                    this.addEntry(key, (component, list, string, integerValue) -> new IntegerRuleEntry(component, list, string, (GameRules.IntegerValue)integerValue));
                }

                private <T extends GameRules.Value<T>> void addEntry(GameRules.Key<T> key, EntryFactory<T> entryFactory) {
                    Object object;
                    ImmutableList immutableList;
                    MutableComponent mutableComponent = Component.translatable(key.getDescriptionId());
                    MutableComponent mutableComponent2 = Component.literal(key.getId()).withStyle(ChatFormatting.YELLOW);
                    T t = gameRules.getRule(key);
                    String string = ((GameRules.Value)t).serialize();
                    MutableComponent mutableComponent3 = Component.translatable("editGamerule.default", Component.literal(string)).withStyle(ChatFormatting.GRAY);
                    String string2 = key.getDescriptionId() + ".description";
                    if (I18n.exists(string2)) {
                        ImmutableList.Builder builder = ImmutableList.builder().add((Object)mutableComponent2.getVisualOrderText());
                        MutableComponent mutableComponent4 = Component.translatable(string2);
                        EditGameRulesScreen.this.font.split(mutableComponent4, 150).forEach(arg_0 -> ((ImmutableList.Builder)builder).add(arg_0));
                        immutableList = builder.add((Object)mutableComponent3.getVisualOrderText()).build();
                        object = mutableComponent4.getString() + "\n" + mutableComponent3.getString();
                    } else {
                        immutableList = ImmutableList.of((Object)mutableComponent2.getVisualOrderText(), (Object)mutableComponent3.getVisualOrderText());
                        object = mutableComponent3.getString();
                    }
                    hashMap.computeIfAbsent(key.getCategory(), category -> Maps.newHashMap()).put(key, entryFactory.create(mutableComponent, (List<FormattedCharSequence>)immutableList, (String)object, t));
                }
            });
            hashMap.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry2 -> {
                this.addEntry(new CategoryRuleEntry(Component.translatable(((GameRules.Category)((Object)((Object)entry2.getKey()))).getDescriptionId()).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW)));
                ((Map)entry2.getValue()).entrySet().stream().sorted(Map.Entry.comparingByKey(Comparator.comparing(GameRules.Key::getId))).forEach(entry -> this.addEntry((RuleEntry)entry.getValue()));
            });
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int n, int n2, float f) {
            super.renderWidget(guiGraphics, n, n2, f);
            RuleEntry ruleEntry = (RuleEntry)this.getHovered();
            if (ruleEntry != null && ruleEntry.tooltip != null) {
                guiGraphics.setTooltipForNextFrame(ruleEntry.tooltip, n, n2);
            }
        }
    }

    public class IntegerRuleEntry
    extends GameRuleEntry {
        private final EditBox input;

        public IntegerRuleEntry(Component component, List<FormattedCharSequence> list, String string2, GameRules.IntegerValue integerValue) {
            super(list, component);
            this.input = new EditBox(((EditGameRulesScreen)EditGameRulesScreen.this).minecraft.font, 10, 5, 44, 20, component.copy().append("\n").append(string2).append("\n"));
            this.input.setValue(Integer.toString(integerValue.get()));
            this.input.setResponder(string -> {
                if (integerValue.tryDeserialize((String)string)) {
                    this.input.setTextColor(-2039584);
                    EditGameRulesScreen.this.clearInvalid(this);
                } else {
                    this.input.setTextColor(-65536);
                    EditGameRulesScreen.this.markInvalid(this);
                }
            });
            this.children.add(this.input);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
            this.renderLabel(guiGraphics, n2, n3);
            this.input.setX(n3 + n4 - 45);
            this.input.setY(n2);
            this.input.render(guiGraphics, n6, n7, f);
        }
    }

    public class BooleanRuleEntry
    extends GameRuleEntry {
        private final CycleButton<Boolean> checkbox;

        public BooleanRuleEntry(EditGameRulesScreen editGameRulesScreen, Component component, List<FormattedCharSequence> list, String string, GameRules.BooleanValue booleanValue) {
            super(list, component);
            this.checkbox = CycleButton.onOffBuilder(booleanValue.get()).displayOnlyValue().withCustomNarration(cycleButton -> cycleButton.createDefaultNarrationMessage().append("\n").append(string)).create(10, 5, 44, 20, component, (cycleButton, bl) -> booleanValue.set((boolean)bl, null));
            this.children.add(this.checkbox);
        }

        @Override
        public void render(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
            this.renderLabel(guiGraphics, n2, n3);
            this.checkbox.setX(n3 + n4 - 45);
            this.checkbox.setY(n2);
            this.checkbox.render(guiGraphics, n6, n7, f);
        }
    }

    public abstract class GameRuleEntry
    extends RuleEntry {
        private final List<FormattedCharSequence> label;
        protected final List<AbstractWidget> children;

        public GameRuleEntry(List<FormattedCharSequence> list, Component component) {
            super(list);
            this.children = Lists.newArrayList();
            this.label = ((EditGameRulesScreen)EditGameRulesScreen.this).minecraft.font.split(component, 175);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return this.children;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return this.children;
        }

        protected void renderLabel(GuiGraphics guiGraphics, int n, int n2) {
            if (this.label.size() == 1) {
                guiGraphics.drawString(((EditGameRulesScreen)EditGameRulesScreen.this).minecraft.font, this.label.get(0), n2, n + 5, -1);
            } else if (this.label.size() >= 2) {
                guiGraphics.drawString(((EditGameRulesScreen)EditGameRulesScreen.this).minecraft.font, this.label.get(0), n2, n, -1);
                guiGraphics.drawString(((EditGameRulesScreen)EditGameRulesScreen.this).minecraft.font, this.label.get(1), n2, n + 10, -1);
            }
        }
    }

    @FunctionalInterface
    static interface EntryFactory<T extends GameRules.Value<T>> {
        public RuleEntry create(Component var1, List<FormattedCharSequence> var2, String var3, T var4);
    }

    public class CategoryRuleEntry
    extends RuleEntry {
        final Component label;

        public CategoryRuleEntry(Component component) {
            super(null);
            this.label = component;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int n, int n2, int n3, int n4, int n5, int n6, int n7, boolean bl, float f) {
            guiGraphics.drawCenteredString(((EditGameRulesScreen)EditGameRulesScreen.this).minecraft.font, this.label, n3 + n4 / 2, n2 + 5, -1);
        }

        @Override
        public List<? extends GuiEventListener> children() {
            return ImmutableList.of();
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return ImmutableList.of((Object)new NarratableEntry(){

                @Override
                public NarratableEntry.NarrationPriority narrationPriority() {
                    return NarratableEntry.NarrationPriority.HOVERED;
                }

                @Override
                public void updateNarration(NarrationElementOutput narrationElementOutput) {
                    narrationElementOutput.add(NarratedElementType.TITLE, CategoryRuleEntry.this.label);
                }
            });
        }
    }

    public static abstract class RuleEntry
    extends ContainerObjectSelectionList.Entry<RuleEntry> {
        @Nullable
        final List<FormattedCharSequence> tooltip;

        public RuleEntry(@Nullable List<FormattedCharSequence> list) {
            this.tooltip = list;
        }
    }
}

