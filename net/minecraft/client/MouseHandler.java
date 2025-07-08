/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.joml.Vector2i
 *  org.lwjgl.glfw.GLFWDropCallback
 *  org.slf4j.Logger
 */
package net.minecraft.client;

import com.mojang.blaze3d.Blaze3D;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.mojang.logging.LogUtils;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.InputType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ScrollWheelHandler;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;
import net.minecraft.util.SmoothDouble;
import net.minecraft.world.entity.player.Inventory;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFWDropCallback;
import org.slf4j.Logger;

public class MouseHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Minecraft minecraft;
    private boolean isLeftPressed;
    private boolean isMiddlePressed;
    private boolean isRightPressed;
    private double xpos;
    private double ypos;
    private int fakeRightMouse;
    private int activeButton = -1;
    private boolean ignoreFirstMove = true;
    private int clickDepth;
    private double mousePressedTime;
    private final SmoothDouble smoothTurnX = new SmoothDouble();
    private final SmoothDouble smoothTurnY = new SmoothDouble();
    private double accumulatedDX;
    private double accumulatedDY;
    private final ScrollWheelHandler scrollWheelHandler;
    private double lastHandleMovementTime = Double.MIN_VALUE;
    private boolean mouseGrabbed;

    public MouseHandler(Minecraft minecraft) {
        this.minecraft = minecraft;
        this.scrollWheelHandler = new ScrollWheelHandler();
    }

    private void onPress(long l, int n, int n2, int n3) {
        int n4;
        boolean bl;
        block32: {
            Window window = this.minecraft.getWindow();
            if (l != window.getWindow()) {
                return;
            }
            this.minecraft.getFramerateLimitTracker().onInputReceived();
            if (this.minecraft.screen != null) {
                this.minecraft.setLastInputType(InputType.MOUSE);
            }
            boolean bl2 = bl = n2 == 1;
            if (Minecraft.ON_OSX && n == 0) {
                if (bl) {
                    if ((n3 & 2) == 2) {
                        n = 1;
                        ++this.fakeRightMouse;
                    }
                } else if (this.fakeRightMouse > 0) {
                    n = 1;
                    --this.fakeRightMouse;
                }
            }
            n4 = n;
            if (bl) {
                if (this.minecraft.options.touchscreen().get().booleanValue() && this.clickDepth++ > 0) {
                    return;
                }
                this.activeButton = n4;
                this.mousePressedTime = Blaze3D.getTime();
            } else if (this.activeButton != -1) {
                if (this.minecraft.options.touchscreen().get().booleanValue() && --this.clickDepth > 0) {
                    return;
                }
                this.activeButton = -1;
            }
            if (this.minecraft.getOverlay() == null) {
                if (this.minecraft.screen == null) {
                    if (!this.mouseGrabbed && bl) {
                        this.grabMouse();
                    }
                } else {
                    double d = this.getScaledXPos(window);
                    double d2 = this.getScaledYPos(window);
                    Screen screen = this.minecraft.screen;
                    if (bl) {
                        screen.afterMouseAction();
                        try {
                            if (screen.mouseClicked(d, d2, n4)) {
                                return;
                            }
                            break block32;
                        }
                        catch (Throwable throwable) {
                            CrashReport crashReport = CrashReport.forThrowable(throwable, "mouseClicked event handler");
                            screen.fillCrashDetails(crashReport);
                            CrashReportCategory crashReportCategory = crashReport.addCategory("Mouse");
                            this.fillMousePositionDetails(crashReportCategory, window);
                            crashReportCategory.setDetail("Button", n4);
                            throw new ReportedException(crashReport);
                        }
                    }
                    try {
                        if (screen.mouseReleased(d, d2, n4)) {
                            return;
                        }
                    }
                    catch (Throwable throwable) {
                        CrashReport crashReport = CrashReport.forThrowable(throwable, "mouseReleased event handler");
                        screen.fillCrashDetails(crashReport);
                        CrashReportCategory crashReportCategory = crashReport.addCategory("Mouse");
                        this.fillMousePositionDetails(crashReportCategory, window);
                        crashReportCategory.setDetail("Button", n4);
                        throw new ReportedException(crashReport);
                    }
                }
            }
        }
        if (this.minecraft.screen == null && this.minecraft.getOverlay() == null) {
            if (n4 == 0) {
                this.isLeftPressed = bl;
            } else if (n4 == 2) {
                this.isMiddlePressed = bl;
            } else if (n4 == 1) {
                this.isRightPressed = bl;
            }
            KeyMapping.set(InputConstants.Type.MOUSE.getOrCreate(n4), bl);
            if (bl) {
                if (this.minecraft.player.isSpectator() && n4 == 2) {
                    this.minecraft.gui.getSpectatorGui().onMouseMiddleClick();
                } else {
                    KeyMapping.click(InputConstants.Type.MOUSE.getOrCreate(n4));
                }
            }
        }
    }

    public void fillMousePositionDetails(CrashReportCategory crashReportCategory, Window window) {
        crashReportCategory.setDetail("Mouse location", () -> String.format(Locale.ROOT, "Scaled: (%f, %f). Absolute: (%f, %f)", MouseHandler.getScaledXPos(window, this.xpos), MouseHandler.getScaledYPos(window, this.ypos), this.xpos, this.ypos));
        crashReportCategory.setDetail("Screen size", () -> String.format(Locale.ROOT, "Scaled: (%d, %d). Absolute: (%d, %d). Scale factor of %f", window.getGuiScaledWidth(), window.getGuiScaledHeight(), window.getWidth(), window.getHeight(), window.getGuiScale()));
    }

    private void onScroll(long l, double d, double d2) {
        if (l == Minecraft.getInstance().getWindow().getWindow()) {
            this.minecraft.getFramerateLimitTracker().onInputReceived();
            boolean bl = this.minecraft.options.discreteMouseScroll().get();
            double d3 = this.minecraft.options.mouseWheelSensitivity().get();
            double d4 = (bl ? Math.signum(d) : d) * d3;
            double d5 = (bl ? Math.signum(d2) : d2) * d3;
            if (this.minecraft.getOverlay() == null) {
                if (this.minecraft.screen != null) {
                    double d6 = this.getScaledXPos(this.minecraft.getWindow());
                    double d7 = this.getScaledYPos(this.minecraft.getWindow());
                    this.minecraft.screen.mouseScrolled(d6, d7, d4, d5);
                    this.minecraft.screen.afterMouseAction();
                } else if (this.minecraft.player != null) {
                    int n;
                    Vector2i vector2i = this.scrollWheelHandler.onMouseScroll(d4, d5);
                    if (vector2i.x == 0 && vector2i.y == 0) {
                        return;
                    }
                    int n2 = n = vector2i.y == 0 ? -vector2i.x : vector2i.y;
                    if (this.minecraft.player.isSpectator()) {
                        if (this.minecraft.gui.getSpectatorGui().isMenuActive()) {
                            this.minecraft.gui.getSpectatorGui().onMouseScrolled(-n);
                        } else {
                            float f = Mth.clamp(this.minecraft.player.getAbilities().getFlyingSpeed() + (float)vector2i.y * 0.005f, 0.0f, 0.2f);
                            this.minecraft.player.getAbilities().setFlyingSpeed(f);
                        }
                    } else {
                        Inventory inventory = this.minecraft.player.getInventory();
                        inventory.setSelectedSlot(ScrollWheelHandler.getNextScrollWheelSelection(n, inventory.getSelectedSlot(), Inventory.getSelectionSize()));
                    }
                }
            }
        }
    }

    private void onDrop(long l, List<Path> list, int n) {
        this.minecraft.getFramerateLimitTracker().onInputReceived();
        if (this.minecraft.screen != null) {
            this.minecraft.screen.onFilesDrop(list);
        }
        if (n > 0) {
            SystemToast.onFileDropFailure(this.minecraft, n);
        }
    }

    public void setup(long l3) {
        InputConstants.setupMouseCallbacks(l3, (l, d, d2) -> this.minecraft.execute(() -> this.onMove(l, d, d2)), (l, n, n2, n3) -> this.minecraft.execute(() -> this.onPress(l, n, n2, n3)), (l, d, d2) -> this.minecraft.execute(() -> this.onScroll(l, d, d2)), (l, n, l2) -> {
            int n2;
            ArrayList<Path> arrayList = new ArrayList<Path>(n);
            int n3 = 0;
            for (n2 = 0; n2 < n; ++n2) {
                String string = GLFWDropCallback.getName((long)l2, (int)n2);
                try {
                    arrayList.add(Paths.get(string, new String[0]));
                    continue;
                }
                catch (InvalidPathException invalidPathException) {
                    ++n3;
                    LOGGER.error("Failed to parse path '{}'", (Object)string, (Object)invalidPathException);
                }
            }
            if (!arrayList.isEmpty()) {
                n2 = n3;
                this.minecraft.execute(() -> this.onDrop(l, arrayList, n2));
            }
        });
    }

    private void onMove(long l, double d, double d2) {
        if (l != Minecraft.getInstance().getWindow().getWindow()) {
            return;
        }
        if (this.ignoreFirstMove) {
            this.xpos = d;
            this.ypos = d2;
            this.ignoreFirstMove = false;
            return;
        }
        if (this.minecraft.isWindowActive()) {
            this.accumulatedDX += d - this.xpos;
            this.accumulatedDY += d2 - this.ypos;
        }
        this.xpos = d;
        this.ypos = d2;
    }

    public void handleAccumulatedMovement() {
        double d = Blaze3D.getTime();
        double d2 = d - this.lastHandleMovementTime;
        this.lastHandleMovementTime = d;
        if (this.minecraft.isWindowActive()) {
            boolean bl;
            Screen screen = this.minecraft.screen;
            boolean bl2 = bl = this.accumulatedDX != 0.0 || this.accumulatedDY != 0.0;
            if (bl) {
                this.minecraft.getFramerateLimitTracker().onInputReceived();
            }
            if (screen != null && this.minecraft.getOverlay() == null && bl) {
                Window window = this.minecraft.getWindow();
                double d3 = this.getScaledXPos(window);
                double d4 = this.getScaledYPos(window);
                try {
                    screen.mouseMoved(d3, d4);
                }
                catch (Throwable throwable) {
                    CrashReport crashReport = CrashReport.forThrowable(throwable, "mouseMoved event handler");
                    screen.fillCrashDetails(crashReport);
                    CrashReportCategory crashReportCategory = crashReport.addCategory("Mouse");
                    this.fillMousePositionDetails(crashReportCategory, window);
                    throw new ReportedException(crashReport);
                }
                if (this.activeButton != -1 && this.mousePressedTime > 0.0) {
                    double d5 = MouseHandler.getScaledXPos(window, this.accumulatedDX);
                    double d6 = MouseHandler.getScaledYPos(window, this.accumulatedDY);
                    try {
                        screen.mouseDragged(d3, d4, this.activeButton, d5, d6);
                    }
                    catch (Throwable throwable) {
                        CrashReport crashReport = CrashReport.forThrowable(throwable, "mouseDragged event handler");
                        screen.fillCrashDetails(crashReport);
                        CrashReportCategory crashReportCategory = crashReport.addCategory("Mouse");
                        this.fillMousePositionDetails(crashReportCategory, window);
                        throw new ReportedException(crashReport);
                    }
                }
                screen.afterMouseMove();
            }
            if (this.isMouseGrabbed() && this.minecraft.player != null) {
                this.turnPlayer(d2);
            }
        }
        this.accumulatedDX = 0.0;
        this.accumulatedDY = 0.0;
    }

    public static double getScaledXPos(Window window, double d) {
        return d * (double)window.getGuiScaledWidth() / (double)window.getScreenWidth();
    }

    public double getScaledXPos(Window window) {
        return MouseHandler.getScaledXPos(window, this.xpos);
    }

    public static double getScaledYPos(Window window, double d) {
        return d * (double)window.getGuiScaledHeight() / (double)window.getScreenHeight();
    }

    public double getScaledYPos(Window window) {
        return MouseHandler.getScaledYPos(window, this.ypos);
    }

    private void turnPlayer(double d) {
        double d2;
        double d3;
        double d4 = this.minecraft.options.sensitivity().get() * (double)0.6f + (double)0.2f;
        double d5 = d4 * d4 * d4;
        double d6 = d5 * 8.0;
        if (this.minecraft.options.smoothCamera) {
            double d7 = this.smoothTurnX.getNewDeltaValue(this.accumulatedDX * d6, d * d6);
            double d8 = this.smoothTurnY.getNewDeltaValue(this.accumulatedDY * d6, d * d6);
            d3 = d7;
            d2 = d8;
        } else if (this.minecraft.options.getCameraType().isFirstPerson() && this.minecraft.player.isScoping()) {
            this.smoothTurnX.reset();
            this.smoothTurnY.reset();
            d3 = this.accumulatedDX * d5;
            d2 = this.accumulatedDY * d5;
        } else {
            this.smoothTurnX.reset();
            this.smoothTurnY.reset();
            d3 = this.accumulatedDX * d6;
            d2 = this.accumulatedDY * d6;
        }
        int n = 1;
        if (this.minecraft.options.invertYMouse().get().booleanValue()) {
            n = -1;
        }
        this.minecraft.getTutorial().onMouse(d3, d2);
        if (this.minecraft.player != null) {
            this.minecraft.player.turn(d3, d2 * (double)n);
        }
    }

    public boolean isLeftPressed() {
        return this.isLeftPressed;
    }

    public boolean isMiddlePressed() {
        return this.isMiddlePressed;
    }

    public boolean isRightPressed() {
        return this.isRightPressed;
    }

    public double xpos() {
        return this.xpos;
    }

    public double ypos() {
        return this.ypos;
    }

    public void setIgnoreFirstMove() {
        this.ignoreFirstMove = true;
    }

    public boolean isMouseGrabbed() {
        return this.mouseGrabbed;
    }

    public void grabMouse() {
        if (!this.minecraft.isWindowActive()) {
            return;
        }
        if (this.mouseGrabbed) {
            return;
        }
        if (!Minecraft.ON_OSX) {
            KeyMapping.setAll();
        }
        this.mouseGrabbed = true;
        this.xpos = this.minecraft.getWindow().getScreenWidth() / 2;
        this.ypos = this.minecraft.getWindow().getScreenHeight() / 2;
        InputConstants.grabOrReleaseMouse(this.minecraft.getWindow().getWindow(), 212995, this.xpos, this.ypos);
        this.minecraft.setScreen(null);
        this.minecraft.missTime = 10000;
        this.ignoreFirstMove = true;
    }

    public void releaseMouse() {
        if (!this.mouseGrabbed) {
            return;
        }
        this.mouseGrabbed = false;
        this.xpos = this.minecraft.getWindow().getScreenWidth() / 2;
        this.ypos = this.minecraft.getWindow().getScreenHeight() / 2;
        InputConstants.grabOrReleaseMouse(this.minecraft.getWindow().getWindow(), 212993, this.xpos, this.ypos);
    }

    public void cursorEntered() {
        this.ignoreFirstMove = true;
    }

    public void drawDebugMouseInfo(Font font, GuiGraphics guiGraphics) {
        Window window = this.minecraft.getWindow();
        double d = this.getScaledXPos(window);
        double d2 = this.getScaledYPos(window) - 8.0;
        String string = String.format(Locale.ROOT, "%.0f,%.0f", d, d2);
        guiGraphics.drawString(font, string, (int)d, (int)d2, -1);
    }
}

