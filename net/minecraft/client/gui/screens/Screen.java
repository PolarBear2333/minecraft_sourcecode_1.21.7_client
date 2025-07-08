/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.Lists
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.gui.screens;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import java.lang.runtime.SwitchBootstraps;
import java.net.URI;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.NarratorStatus;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.TabOrderedElement;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.ScreenNarrationCollector;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.common.ServerboundCustomClickActionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.Music;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.slf4j.Logger;

public abstract class Screen
extends AbstractContainerEventHandler
implements Renderable {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Component USAGE_NARRATION = Component.translatable("narrator.screen.usage");
    public static final ResourceLocation MENU_BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/menu_background.png");
    public static final ResourceLocation HEADER_SEPARATOR = ResourceLocation.withDefaultNamespace("textures/gui/header_separator.png");
    public static final ResourceLocation FOOTER_SEPARATOR = ResourceLocation.withDefaultNamespace("textures/gui/footer_separator.png");
    private static final ResourceLocation INWORLD_MENU_BACKGROUND = ResourceLocation.withDefaultNamespace("textures/gui/inworld_menu_background.png");
    public static final ResourceLocation INWORLD_HEADER_SEPARATOR = ResourceLocation.withDefaultNamespace("textures/gui/inworld_header_separator.png");
    public static final ResourceLocation INWORLD_FOOTER_SEPARATOR = ResourceLocation.withDefaultNamespace("textures/gui/inworld_footer_separator.png");
    protected static final float FADE_IN_TIME = 2000.0f;
    protected final Component title;
    private final List<GuiEventListener> children = Lists.newArrayList();
    private final List<NarratableEntry> narratables = Lists.newArrayList();
    @Nullable
    protected Minecraft minecraft;
    private boolean initialized;
    public int width;
    public int height;
    private final List<Renderable> renderables = Lists.newArrayList();
    protected Font font;
    private static final long NARRATE_SUPPRESS_AFTER_INIT_TIME;
    private static final long NARRATE_DELAY_NARRATOR_ENABLED;
    private static final long NARRATE_DELAY_MOUSE_MOVE = 750L;
    private static final long NARRATE_DELAY_MOUSE_ACTION = 200L;
    private static final long NARRATE_DELAY_KEYBOARD_ACTION = 200L;
    private final ScreenNarrationCollector narrationState = new ScreenNarrationCollector();
    private long narrationSuppressTime = Long.MIN_VALUE;
    private long nextNarrationTime = Long.MAX_VALUE;
    @Nullable
    protected CycleButton<NarratorStatus> narratorButton;
    @Nullable
    private NarratableEntry lastNarratable;
    protected final Executor screenExecutor = runnable -> this.minecraft.execute(() -> {
        if (this.minecraft.screen == this) {
            runnable.run();
        }
    });

    protected Screen(Component component) {
        this.title = component;
    }

    public Component getTitle() {
        return this.title;
    }

    public Component getNarrationMessage() {
        return this.getTitle();
    }

    public final void renderWithTooltip(GuiGraphics guiGraphics, int n, int n2, float f) {
        guiGraphics.nextStratum();
        this.renderBackground(guiGraphics, n, n2, f);
        guiGraphics.nextStratum();
        this.render(guiGraphics, n, n2, f);
        guiGraphics.renderDeferredTooltip();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int n, int n2, float f) {
        for (Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, n, n2, f);
        }
    }

    @Override
    public boolean keyPressed(int n, int n2, int n3) {
        FocusNavigationEvent.TabNavigation tabNavigation;
        if (n == 256 && this.shouldCloseOnEsc()) {
            this.onClose();
            return true;
        }
        if (super.keyPressed(n, n2, n3)) {
            return true;
        }
        switch (n) {
            case 263: {
                Record record = this.createArrowEvent(ScreenDirection.LEFT);
                break;
            }
            case 262: {
                Record record = this.createArrowEvent(ScreenDirection.RIGHT);
                break;
            }
            case 265: {
                Record record = this.createArrowEvent(ScreenDirection.UP);
                break;
            }
            case 264: {
                Record record = this.createArrowEvent(ScreenDirection.DOWN);
                break;
            }
            case 258: {
                Record record = this.createTabEvent();
                break;
            }
            default: {
                Record record = tabNavigation = null;
            }
        }
        if (tabNavigation != null) {
            ComponentPath componentPath = super.nextFocusPath(tabNavigation);
            if (componentPath == null && tabNavigation instanceof FocusNavigationEvent.TabNavigation) {
                this.clearFocus();
                componentPath = super.nextFocusPath(tabNavigation);
            }
            if (componentPath != null) {
                this.changeFocus(componentPath);
            }
        }
        return false;
    }

    private FocusNavigationEvent.TabNavigation createTabEvent() {
        boolean bl = !Screen.hasShiftDown();
        return new FocusNavigationEvent.TabNavigation(bl);
    }

    private FocusNavigationEvent.ArrowNavigation createArrowEvent(ScreenDirection screenDirection) {
        return new FocusNavigationEvent.ArrowNavigation(screenDirection);
    }

    protected void setInitialFocus() {
        FocusNavigationEvent.TabNavigation tabNavigation;
        ComponentPath componentPath;
        if (this.minecraft.getLastInputType().isKeyboard() && (componentPath = super.nextFocusPath(tabNavigation = new FocusNavigationEvent.TabNavigation(true))) != null) {
            this.changeFocus(componentPath);
        }
    }

    protected void setInitialFocus(GuiEventListener guiEventListener) {
        ComponentPath componentPath = ComponentPath.path(this, guiEventListener.nextFocusPath(new FocusNavigationEvent.InitialFocus()));
        if (componentPath != null) {
            this.changeFocus(componentPath);
        }
    }

    public void clearFocus() {
        ComponentPath componentPath = this.getCurrentFocusPath();
        if (componentPath != null) {
            componentPath.applyFocus(false);
        }
    }

    @VisibleForTesting
    protected void changeFocus(ComponentPath componentPath) {
        this.clearFocus();
        componentPath.applyFocus(true);
    }

    public boolean shouldCloseOnEsc() {
        return true;
    }

    public void onClose() {
        this.minecraft.setScreen(null);
    }

    protected <T extends GuiEventListener & Renderable> T addRenderableWidget(T t) {
        this.renderables.add(t);
        return this.addWidget(t);
    }

    protected <T extends Renderable> T addRenderableOnly(T t) {
        this.renderables.add(t);
        return t;
    }

    protected <T extends GuiEventListener & NarratableEntry> T addWidget(T t) {
        this.children.add(t);
        this.narratables.add(t);
        return t;
    }

    protected void removeWidget(GuiEventListener guiEventListener) {
        if (guiEventListener instanceof Renderable) {
            this.renderables.remove((Renderable)((Object)guiEventListener));
        }
        if (guiEventListener instanceof NarratableEntry) {
            this.narratables.remove((NarratableEntry)((Object)guiEventListener));
        }
        this.children.remove(guiEventListener);
    }

    protected void clearWidgets() {
        this.renderables.clear();
        this.children.clear();
        this.narratables.clear();
    }

    public static List<Component> getTooltipFromItem(Minecraft minecraft, ItemStack itemStack) {
        return itemStack.getTooltipLines(Item.TooltipContext.of(minecraft.level), minecraft.player, minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
    }

    protected void insertText(String string, boolean bl) {
    }

    public boolean handleComponentClicked(Style style) {
        ClickEvent clickEvent = style.getClickEvent();
        if (Screen.hasShiftDown()) {
            if (style.getInsertion() != null) {
                this.insertText(style.getInsertion(), false);
            }
        } else if (clickEvent != null) {
            this.handleClickEvent(this.minecraft, clickEvent);
            return true;
        }
        return false;
    }

    protected void handleClickEvent(Minecraft minecraft, ClickEvent clickEvent) {
        Screen.defaultHandleGameClickEvent(clickEvent, minecraft, this);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    protected static void defaultHandleGameClickEvent(ClickEvent clickEvent, Minecraft minecraft, @Nullable Screen screen) {
        LocalPlayer localPlayer = Objects.requireNonNull(minecraft.player, "Player not available");
        ClickEvent clickEvent2 = clickEvent;
        Objects.requireNonNull(clickEvent2);
        ClickEvent clickEvent3 = clickEvent2;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{ClickEvent.RunCommand.class, ClickEvent.ShowDialog.class, ClickEvent.Custom.class}, (Object)clickEvent3, n)) {
            case 0: {
                String string2;
                ClickEvent.RunCommand runCommand = (ClickEvent.RunCommand)clickEvent3;
                try {
                    String string;
                    string2 = string = runCommand.command();
                }
                catch (Throwable throwable) {
                    throw new MatchException(throwable.toString(), throwable);
                }
                Screen.clickCommandAction(localPlayer, string2, screen);
                return;
            }
            case 1: {
                ClickEvent.ShowDialog showDialog = (ClickEvent.ShowDialog)clickEvent3;
                localPlayer.connection.showDialog(showDialog.dialog(), screen);
                return;
            }
            case 2: {
                ClickEvent.Custom custom = (ClickEvent.Custom)clickEvent3;
                localPlayer.connection.send(new ServerboundCustomClickActionPacket(custom.id(), custom.payload()));
                if (minecraft.screen == screen) return;
                minecraft.setScreen(screen);
                return;
            }
        }
        Screen.defaultHandleClickEvent(clickEvent, minecraft, screen);
    }

    /*
     * Loose catch block
     */
    protected static void defaultHandleClickEvent(ClickEvent clickEvent, Minecraft minecraft, @Nullable Screen screen) {
        block12: {
            boolean bl2;
            ClickEvent clickEvent2 = clickEvent;
            Objects.requireNonNull(clickEvent2);
            ClickEvent clickEvent3 = clickEvent2;
            int n = 0;
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{ClickEvent.OpenUrl.class, ClickEvent.OpenFile.class, ClickEvent.SuggestCommand.class, ClickEvent.CopyToClipboard.class}, (Object)clickEvent3, n)) {
                case 0: {
                    Object object;
                    ClickEvent.OpenUrl openUrl = (ClickEvent.OpenUrl)clickEvent3;
                    Object object2 = object = openUrl.uri();
                    Screen.clickUrlAction(minecraft, screen, (URI)object2);
                    boolean bl2 = false;
                    break;
                }
                case 1: {
                    Object object = (ClickEvent.OpenFile)clickEvent3;
                    Util.getPlatform().openFile(((ClickEvent.OpenFile)object).file());
                    boolean bl2 = true;
                    break;
                }
                case 2: {
                    Object object;
                    ClickEvent.SuggestCommand suggestCommand = (ClickEvent.SuggestCommand)clickEvent3;
                    Object object3 = object = suggestCommand.command();
                    if (screen != null) {
                        screen.insertText((String)object3, true);
                    }
                    boolean bl2 = true;
                    break;
                }
                case 3: {
                    String string;
                    Object object = (ClickEvent.CopyToClipboard)clickEvent3;
                    String string2 = string = ((ClickEvent.CopyToClipboard)object).value();
                    minecraft.keyboardHandler.setClipboard(string2);
                    boolean bl2 = true;
                    break;
                }
                default: {
                    LOGGER.error("Don't know how to handle {}", (Object)clickEvent);
                    boolean bl2 = bl2 = true;
                }
            }
            if (bl2 && minecraft.screen != screen) {
                minecraft.setScreen(screen);
            }
            break block12;
            catch (Throwable throwable) {
                throw new MatchException(throwable.toString(), throwable);
            }
        }
    }

    protected static boolean clickUrlAction(Minecraft minecraft, @Nullable Screen screen, URI uRI) {
        if (!minecraft.options.chatLinks().get().booleanValue()) {
            return false;
        }
        if (minecraft.options.chatLinksPrompt().get().booleanValue()) {
            minecraft.setScreen(new ConfirmLinkScreen(bl -> {
                if (bl) {
                    Util.getPlatform().openUri(uRI);
                }
                minecraft.setScreen(screen);
            }, uRI.toString(), false));
        } else {
            Util.getPlatform().openUri(uRI);
        }
        return true;
    }

    protected static void clickCommandAction(LocalPlayer localPlayer, String string, @Nullable Screen screen) {
        localPlayer.connection.sendUnattendedCommand(Commands.trimOptionalPrefix(string), screen);
    }

    public final void init(Minecraft minecraft, int n, int n2) {
        this.minecraft = minecraft;
        this.font = minecraft.font;
        this.width = n;
        this.height = n2;
        if (!this.initialized) {
            this.init();
            this.setInitialFocus();
        } else {
            this.repositionElements();
        }
        this.initialized = true;
        this.triggerImmediateNarration(false);
        this.suppressNarration(NARRATE_SUPPRESS_AFTER_INIT_TIME);
    }

    protected void rebuildWidgets() {
        this.clearWidgets();
        this.clearFocus();
        this.init();
        this.setInitialFocus();
    }

    protected void fadeWidgets(float f) {
        for (GuiEventListener guiEventListener : this.children()) {
            if (!(guiEventListener instanceof AbstractWidget)) continue;
            AbstractWidget abstractWidget = (AbstractWidget)guiEventListener;
            abstractWidget.setAlpha(f);
        }
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return this.children;
    }

    protected void init() {
    }

    public void tick() {
    }

    public void removed() {
    }

    public void added() {
    }

    public void renderBackground(GuiGraphics guiGraphics, int n, int n2, float f) {
        if (this.minecraft.level == null) {
            this.renderPanorama(guiGraphics, f);
        }
        this.renderBlurredBackground(guiGraphics);
        this.renderMenuBackground(guiGraphics);
    }

    protected void renderBlurredBackground(GuiGraphics guiGraphics) {
        float f = this.minecraft.options.getMenuBackgroundBlurriness();
        if (f >= 1.0f) {
            guiGraphics.blurBeforeThisStratum();
        }
    }

    protected void renderPanorama(GuiGraphics guiGraphics, float f) {
        this.minecraft.gameRenderer.getPanorama().render(guiGraphics, this.width, this.height, true);
    }

    protected void renderMenuBackground(GuiGraphics guiGraphics) {
        this.renderMenuBackground(guiGraphics, 0, 0, this.width, this.height);
    }

    protected void renderMenuBackground(GuiGraphics guiGraphics, int n, int n2, int n3, int n4) {
        Screen.renderMenuBackgroundTexture(guiGraphics, this.minecraft.level == null ? MENU_BACKGROUND : INWORLD_MENU_BACKGROUND, n, n2, 0.0f, 0.0f, n3, n4);
    }

    public static void renderMenuBackgroundTexture(GuiGraphics guiGraphics, ResourceLocation resourceLocation, int n, int n2, float f, float f2, int n3, int n4) {
        int n5 = 32;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, resourceLocation, n, n2, f, f2, n3, n4, 32, 32);
    }

    public void renderTransparentBackground(GuiGraphics guiGraphics) {
        guiGraphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
    }

    public boolean isPauseScreen() {
        return true;
    }

    public static boolean hasControlDown() {
        if (Minecraft.ON_OSX) {
            return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 343) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 347);
        }
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 341) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 345);
    }

    public static boolean hasShiftDown() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344);
    }

    public static boolean hasAltDown() {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 342) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 346);
    }

    public static boolean isCut(int n) {
        return n == 88 && Screen.hasControlDown() && !Screen.hasShiftDown() && !Screen.hasAltDown();
    }

    public static boolean isPaste(int n) {
        return n == 86 && Screen.hasControlDown() && !Screen.hasShiftDown() && !Screen.hasAltDown();
    }

    public static boolean isCopy(int n) {
        return n == 67 && Screen.hasControlDown() && !Screen.hasShiftDown() && !Screen.hasAltDown();
    }

    public static boolean isSelectAll(int n) {
        return n == 65 && Screen.hasControlDown() && !Screen.hasShiftDown() && !Screen.hasAltDown();
    }

    protected void repositionElements() {
        this.rebuildWidgets();
    }

    public void resize(Minecraft minecraft, int n, int n2) {
        this.width = n;
        this.height = n2;
        this.repositionElements();
    }

    public void fillCrashDetails(CrashReport crashReport) {
        CrashReportCategory crashReportCategory = crashReport.addCategory("Affected screen", 1);
        crashReportCategory.setDetail("Screen name", () -> this.getClass().getCanonicalName());
    }

    protected boolean isValidCharacterForName(String string, char c, int n) {
        int n2 = string.indexOf(58);
        int n3 = string.indexOf(47);
        if (c == ':') {
            return (n3 == -1 || n <= n3) && n2 == -1;
        }
        if (c == '/') {
            return n > n2;
        }
        return c == '_' || c == '-' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '.';
    }

    @Override
    public boolean isMouseOver(double d, double d2) {
        return true;
    }

    public void onFilesDrop(List<Path> list) {
    }

    private void scheduleNarration(long l, boolean bl) {
        this.nextNarrationTime = Util.getMillis() + l;
        if (bl) {
            this.narrationSuppressTime = Long.MIN_VALUE;
        }
    }

    private void suppressNarration(long l) {
        this.narrationSuppressTime = Util.getMillis() + l;
    }

    public void afterMouseMove() {
        this.scheduleNarration(750L, false);
    }

    public void afterMouseAction() {
        this.scheduleNarration(200L, true);
    }

    public void afterKeyboardAction() {
        this.scheduleNarration(200L, true);
    }

    private boolean shouldRunNarration() {
        return this.minecraft.getNarrator().isActive();
    }

    public void handleDelayedNarration() {
        long l;
        if (this.shouldRunNarration() && (l = Util.getMillis()) > this.nextNarrationTime && l > this.narrationSuppressTime) {
            this.runNarration(true);
            this.nextNarrationTime = Long.MAX_VALUE;
        }
    }

    public void triggerImmediateNarration(boolean bl) {
        if (this.shouldRunNarration()) {
            this.runNarration(bl);
        }
    }

    private void runNarration(boolean bl) {
        this.narrationState.update(this::updateNarrationState);
        String string = this.narrationState.collectNarrationText(!bl);
        if (!string.isEmpty()) {
            this.minecraft.getNarrator().saySystemNow(string);
        }
    }

    protected boolean shouldNarrateNavigation() {
        return true;
    }

    protected void updateNarrationState(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.getNarrationMessage());
        if (this.shouldNarrateNavigation()) {
            narrationElementOutput.add(NarratedElementType.USAGE, USAGE_NARRATION);
        }
        this.updateNarratedWidget(narrationElementOutput);
    }

    protected void updateNarratedWidget(NarrationElementOutput narrationElementOutput) {
        List<NarratableEntry> list = this.narratables.stream().flatMap(narratableEntry -> narratableEntry.getNarratables().stream()).filter(NarratableEntry::isActive).sorted(Comparator.comparingInt(TabOrderedElement::getTabOrderGroup)).toList();
        NarratableSearchResult narratableSearchResult = Screen.findNarratableWidget(list, this.lastNarratable);
        if (narratableSearchResult != null) {
            if (narratableSearchResult.priority.isTerminal()) {
                this.lastNarratable = narratableSearchResult.entry;
            }
            if (list.size() > 1) {
                narrationElementOutput.add(NarratedElementType.POSITION, (Component)Component.translatable("narrator.position.screen", narratableSearchResult.index + 1, list.size()));
                if (narratableSearchResult.priority == NarratableEntry.NarrationPriority.FOCUSED) {
                    narrationElementOutput.add(NarratedElementType.USAGE, this.getUsageNarration());
                }
            }
            narratableSearchResult.entry.updateNarration(narrationElementOutput.nest());
        }
    }

    protected Component getUsageNarration() {
        return Component.translatable("narration.component_list.usage");
    }

    @Nullable
    public static NarratableSearchResult findNarratableWidget(List<? extends NarratableEntry> list, @Nullable NarratableEntry narratableEntry) {
        NarratableSearchResult narratableSearchResult = null;
        NarratableSearchResult narratableSearchResult2 = null;
        int n = list.size();
        for (int i = 0; i < n; ++i) {
            NarratableEntry narratableEntry2 = list.get(i);
            NarratableEntry.NarrationPriority narrationPriority = narratableEntry2.narrationPriority();
            if (narrationPriority.isTerminal()) {
                if (narratableEntry2 == narratableEntry) {
                    narratableSearchResult2 = new NarratableSearchResult(narratableEntry2, i, narrationPriority);
                    continue;
                }
                return new NarratableSearchResult(narratableEntry2, i, narrationPriority);
            }
            if (narrationPriority.compareTo(narratableSearchResult != null ? narratableSearchResult.priority : NarratableEntry.NarrationPriority.NONE) <= 0) continue;
            narratableSearchResult = new NarratableSearchResult(narratableEntry2, i, narrationPriority);
        }
        return narratableSearchResult != null ? narratableSearchResult : narratableSearchResult2;
    }

    public void updateNarratorStatus(boolean bl) {
        if (bl) {
            this.scheduleNarration(NARRATE_DELAY_NARRATOR_ENABLED, false);
        }
        if (this.narratorButton != null) {
            this.narratorButton.setValue(this.minecraft.options.narrator().get());
        }
    }

    public Font getFont() {
        return this.font;
    }

    public boolean showsActiveEffects() {
        return false;
    }

    @Override
    public ScreenRectangle getRectangle() {
        return new ScreenRectangle(0, 0, this.width, this.height);
    }

    @Nullable
    public Music getBackgroundMusic() {
        return null;
    }

    static {
        NARRATE_DELAY_NARRATOR_ENABLED = NARRATE_SUPPRESS_AFTER_INIT_TIME = TimeUnit.SECONDS.toMillis(2L);
    }

    public static class NarratableSearchResult {
        public final NarratableEntry entry;
        public final int index;
        public final NarratableEntry.NarrationPriority priority;

        public NarratableSearchResult(NarratableEntry narratableEntry, int n, NarratableEntry.NarrationPriority narrationPriority) {
            this.entry = narratableEntry;
            this.index = n;
            this.priority = narrationPriority;
        }
    }
}

