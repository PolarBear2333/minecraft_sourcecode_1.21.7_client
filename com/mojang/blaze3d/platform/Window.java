/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  javax.annotation.Nullable
 *  org.lwjgl.PointerBuffer
 *  org.lwjgl.glfw.Callbacks
 *  org.lwjgl.glfw.GLFW
 *  org.lwjgl.glfw.GLFWErrorCallback
 *  org.lwjgl.glfw.GLFWErrorCallbackI
 *  org.lwjgl.glfw.GLFWImage
 *  org.lwjgl.glfw.GLFWImage$Buffer
 *  org.lwjgl.glfw.GLFWWindowCloseCallback
 *  org.lwjgl.system.MemoryStack
 *  org.lwjgl.system.MemoryUtil
 *  org.lwjgl.util.tinyfd.TinyFileDialogs
 *  org.slf4j.Logger
 */
package com.mojang.blaze3d.platform;

import com.mojang.blaze3d.TracyFrameCapture;
import com.mojang.blaze3d.platform.DisplayData;
import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.IconSet;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.MacosUtil;
import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.ScreenManager;
import com.mojang.blaze3d.platform.VideoMode;
import com.mojang.blaze3d.platform.WindowEventHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.main.SilentInitException;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.IoSupplier;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWErrorCallbackI;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWWindowCloseCallback;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.slf4j.Logger;

public final class Window
implements AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int BASE_WIDTH = 320;
    public static final int BASE_HEIGHT = 240;
    private final GLFWErrorCallback defaultErrorCallback = GLFWErrorCallback.create(this::defaultErrorCallback);
    private final WindowEventHandler eventHandler;
    private final ScreenManager screenManager;
    private final long window;
    private int windowedX;
    private int windowedY;
    private int windowedWidth;
    private int windowedHeight;
    private Optional<VideoMode> preferredFullscreenVideoMode;
    private boolean fullscreen;
    private boolean actuallyFullscreen;
    private int x;
    private int y;
    private int width;
    private int height;
    private int framebufferWidth;
    private int framebufferHeight;
    private int guiScaledWidth;
    private int guiScaledHeight;
    private int guiScale;
    private String errorSection = "";
    private boolean dirty;
    private boolean vsync;
    private boolean iconified;
    private boolean minimized;

    public Window(WindowEventHandler windowEventHandler, ScreenManager screenManager, DisplayData displayData, @Nullable String string, String string2) {
        this.screenManager = screenManager;
        this.setBootErrorCallback();
        this.setErrorSection("Pre startup");
        this.eventHandler = windowEventHandler;
        Optional<VideoMode> optional = VideoMode.read(string);
        this.preferredFullscreenVideoMode = optional.isPresent() ? optional : (displayData.fullscreenWidth().isPresent() && displayData.fullscreenHeight().isPresent() ? Optional.of(new VideoMode(displayData.fullscreenWidth().getAsInt(), displayData.fullscreenHeight().getAsInt(), 8, 8, 8, 60)) : Optional.empty());
        this.actuallyFullscreen = this.fullscreen = displayData.isFullscreen();
        Monitor monitor = screenManager.getMonitor(GLFW.glfwGetPrimaryMonitor());
        this.windowedWidth = this.width = Math.max(displayData.width(), 1);
        this.windowedHeight = this.height = Math.max(displayData.height(), 1);
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint((int)139265, (int)196609);
        GLFW.glfwWindowHint((int)139275, (int)221185);
        GLFW.glfwWindowHint((int)139266, (int)3);
        GLFW.glfwWindowHint((int)139267, (int)2);
        GLFW.glfwWindowHint((int)139272, (int)204801);
        GLFW.glfwWindowHint((int)139270, (int)1);
        this.window = GLFW.glfwCreateWindow((int)this.width, (int)this.height, (CharSequence)string2, (long)(this.fullscreen && monitor != null ? monitor.getMonitor() : 0L), (long)0L);
        if (monitor != null) {
            VideoMode videoMode = monitor.getPreferredVidMode(this.fullscreen ? this.preferredFullscreenVideoMode : Optional.empty());
            this.windowedX = this.x = monitor.getX() + videoMode.getWidth() / 2 - this.width / 2;
            this.windowedY = this.y = monitor.getY() + videoMode.getHeight() / 2 - this.height / 2;
        } else {
            int[] nArray = new int[1];
            int[] nArray2 = new int[1];
            GLFW.glfwGetWindowPos((long)this.window, (int[])nArray, (int[])nArray2);
            this.windowedX = this.x = nArray[0];
            this.windowedY = this.y = nArray2[0];
        }
        this.setMode();
        this.refreshFramebufferSize();
        GLFW.glfwSetFramebufferSizeCallback((long)this.window, this::onFramebufferResize);
        GLFW.glfwSetWindowPosCallback((long)this.window, this::onMove);
        GLFW.glfwSetWindowSizeCallback((long)this.window, this::onResize);
        GLFW.glfwSetWindowFocusCallback((long)this.window, this::onFocus);
        GLFW.glfwSetCursorEnterCallback((long)this.window, this::onEnter);
        GLFW.glfwSetWindowIconifyCallback((long)this.window, this::onIconify);
    }

    public static String getPlatform() {
        int n = GLFW.glfwGetPlatform();
        return switch (n) {
            case 0 -> "<error>";
            case 393217 -> "win32";
            case 393218 -> "cocoa";
            case 393219 -> "wayland";
            case 393220 -> "x11";
            case 393221 -> "null";
            default -> String.format(Locale.ROOT, "unknown (%08X)", n);
        };
    }

    public int getRefreshRate() {
        RenderSystem.assertOnRenderThread();
        return GLX._getRefreshRate(this);
    }

    public boolean shouldClose() {
        return GLX._shouldClose(this);
    }

    public static void checkGlfwError(BiConsumer<Integer, String> biConsumer) {
        try (MemoryStack memoryStack = MemoryStack.stackPush();){
            PointerBuffer pointerBuffer = memoryStack.mallocPointer(1);
            int n = GLFW.glfwGetError((PointerBuffer)pointerBuffer);
            if (n != 0) {
                long l = pointerBuffer.get();
                String string = l == 0L ? "" : MemoryUtil.memUTF8((long)l);
                biConsumer.accept(n, string);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void setIcon(PackResources packResources, IconSet iconSet) throws IOException {
        int n = GLFW.glfwGetPlatform();
        switch (n) {
            case 393217: 
            case 393220: {
                List<IoSupplier<InputStream>> list = iconSet.getStandardIcons(packResources);
                ArrayList<ByteBuffer> arrayList = new ArrayList<ByteBuffer>(list.size());
                try (MemoryStack memoryStack = MemoryStack.stackPush();){
                    GLFWImage.Buffer buffer = GLFWImage.malloc((int)list.size(), (MemoryStack)memoryStack);
                    for (int i = 0; i < list.size(); ++i) {
                        try (NativeImage nativeImage = NativeImage.read(list.get(i).get());){
                            ByteBuffer byteBuffer = MemoryUtil.memAlloc((int)(nativeImage.getWidth() * nativeImage.getHeight() * 4));
                            arrayList.add(byteBuffer);
                            byteBuffer.asIntBuffer().put(nativeImage.getPixelsABGR());
                            buffer.position(i);
                            buffer.width(nativeImage.getWidth());
                            buffer.height(nativeImage.getHeight());
                            buffer.pixels(byteBuffer);
                            continue;
                        }
                    }
                    GLFW.glfwSetWindowIcon((long)this.window, (GLFWImage.Buffer)((GLFWImage.Buffer)buffer.position(0)));
                    break;
                }
                finally {
                    arrayList.forEach(MemoryUtil::memFree);
                }
            }
            case 393218: {
                MacosUtil.loadIcon(iconSet.getMacIcon(packResources));
                break;
            }
            case 393219: 
            case 393221: {
                break;
            }
            default: {
                LOGGER.warn("Not setting icon for unrecognized platform: {}", (Object)n);
            }
        }
    }

    public void setErrorSection(String string) {
        this.errorSection = string;
    }

    private void setBootErrorCallback() {
        GLFW.glfwSetErrorCallback(Window::bootCrash);
    }

    private static void bootCrash(int n, long l) {
        String string = "GLFW error " + n + ": " + MemoryUtil.memUTF8((long)l);
        TinyFileDialogs.tinyfd_messageBox((CharSequence)"Minecraft", (CharSequence)(string + ".\n\nPlease make sure you have up-to-date drivers (see aka.ms/mcdriver for instructions)."), (CharSequence)"ok", (CharSequence)"error", (boolean)false);
        throw new WindowInitFailed(string);
    }

    public void defaultErrorCallback(int n, long l) {
        RenderSystem.assertOnRenderThread();
        String string = MemoryUtil.memUTF8((long)l);
        LOGGER.error("########## GL ERROR ##########");
        LOGGER.error("@ {}", (Object)this.errorSection);
        LOGGER.error("{}: {}", (Object)n, (Object)string);
    }

    public void setDefaultErrorCallback() {
        GLFWErrorCallback gLFWErrorCallback = GLFW.glfwSetErrorCallback((GLFWErrorCallbackI)this.defaultErrorCallback);
        if (gLFWErrorCallback != null) {
            gLFWErrorCallback.free();
        }
    }

    public void updateVsync(boolean bl) {
        RenderSystem.assertOnRenderThread();
        this.vsync = bl;
        GLFW.glfwSwapInterval((int)(bl ? 1 : 0));
    }

    @Override
    public void close() {
        RenderSystem.assertOnRenderThread();
        Callbacks.glfwFreeCallbacks((long)this.window);
        this.defaultErrorCallback.close();
        GLFW.glfwDestroyWindow((long)this.window);
        GLFW.glfwTerminate();
    }

    private void onMove(long l, int n, int n2) {
        this.x = n;
        this.y = n2;
    }

    private void onFramebufferResize(long l, int n, int n2) {
        if (l != this.window) {
            return;
        }
        int n3 = this.getWidth();
        int n4 = this.getHeight();
        if (n == 0 || n2 == 0) {
            this.minimized = true;
            return;
        }
        this.minimized = false;
        this.framebufferWidth = n;
        this.framebufferHeight = n2;
        if (this.getWidth() != n3 || this.getHeight() != n4) {
            try {
                this.eventHandler.resizeDisplay();
            }
            catch (Exception exception) {
                CrashReport crashReport = CrashReport.forThrowable(exception, "Window resize");
                CrashReportCategory crashReportCategory = crashReport.addCategory("Window Dimensions");
                crashReportCategory.setDetail("Old", n3 + "x" + n4);
                crashReportCategory.setDetail("New", n + "x" + n2);
                throw new ReportedException(crashReport);
            }
        }
    }

    private void refreshFramebufferSize() {
        int[] nArray = new int[1];
        int[] nArray2 = new int[1];
        GLFW.glfwGetFramebufferSize((long)this.window, (int[])nArray, (int[])nArray2);
        this.framebufferWidth = nArray[0] > 0 ? nArray[0] : 1;
        this.framebufferHeight = nArray2[0] > 0 ? nArray2[0] : 1;
    }

    private void onResize(long l, int n, int n2) {
        this.width = n;
        this.height = n2;
    }

    private void onFocus(long l, boolean bl) {
        if (l == this.window) {
            this.eventHandler.setWindowActive(bl);
        }
    }

    private void onEnter(long l, boolean bl) {
        if (bl) {
            this.eventHandler.cursorEntered();
        }
    }

    private void onIconify(long l, boolean bl) {
        this.iconified = bl;
    }

    public void updateDisplay(@Nullable TracyFrameCapture tracyFrameCapture) {
        RenderSystem.flipFrame(this.window, tracyFrameCapture);
        if (this.fullscreen != this.actuallyFullscreen) {
            this.actuallyFullscreen = this.fullscreen;
            this.updateFullscreen(this.vsync, tracyFrameCapture);
        }
    }

    public Optional<VideoMode> getPreferredFullscreenVideoMode() {
        return this.preferredFullscreenVideoMode;
    }

    public void setPreferredFullscreenVideoMode(Optional<VideoMode> optional) {
        boolean bl = !optional.equals(this.preferredFullscreenVideoMode);
        this.preferredFullscreenVideoMode = optional;
        if (bl) {
            this.dirty = true;
        }
    }

    public void changeFullscreenVideoMode() {
        if (this.fullscreen && this.dirty) {
            this.dirty = false;
            this.setMode();
            this.eventHandler.resizeDisplay();
        }
    }

    private void setMode() {
        boolean bl;
        boolean bl2 = bl = GLFW.glfwGetWindowMonitor((long)this.window) != 0L;
        if (this.fullscreen) {
            Monitor monitor = this.screenManager.findBestMonitor(this);
            if (monitor == null) {
                LOGGER.warn("Failed to find suitable monitor for fullscreen mode");
                this.fullscreen = false;
            } else {
                if (MacosUtil.IS_MACOS) {
                    MacosUtil.exitNativeFullscreen(this.window);
                }
                VideoMode videoMode = monitor.getPreferredVidMode(this.preferredFullscreenVideoMode);
                if (!bl) {
                    this.windowedX = this.x;
                    this.windowedY = this.y;
                    this.windowedWidth = this.width;
                    this.windowedHeight = this.height;
                }
                this.x = 0;
                this.y = 0;
                this.width = videoMode.getWidth();
                this.height = videoMode.getHeight();
                GLFW.glfwSetWindowMonitor((long)this.window, (long)monitor.getMonitor(), (int)this.x, (int)this.y, (int)this.width, (int)this.height, (int)videoMode.getRefreshRate());
                if (MacosUtil.IS_MACOS) {
                    MacosUtil.clearResizableBit(this.window);
                }
            }
        } else {
            this.x = this.windowedX;
            this.y = this.windowedY;
            this.width = this.windowedWidth;
            this.height = this.windowedHeight;
            GLFW.glfwSetWindowMonitor((long)this.window, (long)0L, (int)this.x, (int)this.y, (int)this.width, (int)this.height, (int)-1);
        }
    }

    public void toggleFullScreen() {
        this.fullscreen = !this.fullscreen;
    }

    public void setWindowed(int n, int n2) {
        this.windowedWidth = n;
        this.windowedHeight = n2;
        this.fullscreen = false;
        this.setMode();
    }

    private void updateFullscreen(boolean bl, @Nullable TracyFrameCapture tracyFrameCapture) {
        RenderSystem.assertOnRenderThread();
        try {
            this.setMode();
            this.eventHandler.resizeDisplay();
            this.updateVsync(bl);
            this.updateDisplay(tracyFrameCapture);
        }
        catch (Exception exception) {
            LOGGER.error("Couldn't toggle fullscreen", (Throwable)exception);
        }
    }

    public int calculateScale(int n, boolean bl) {
        int n2;
        for (n2 = 1; n2 != n && n2 < this.framebufferWidth && n2 < this.framebufferHeight && this.framebufferWidth / (n2 + 1) >= 320 && this.framebufferHeight / (n2 + 1) >= 240; ++n2) {
        }
        if (bl && n2 % 2 != 0) {
            ++n2;
        }
        return n2;
    }

    public void setGuiScale(int n) {
        this.guiScale = n;
        double d = n;
        int n2 = (int)((double)this.framebufferWidth / d);
        this.guiScaledWidth = (double)this.framebufferWidth / d > (double)n2 ? n2 + 1 : n2;
        int n3 = (int)((double)this.framebufferHeight / d);
        this.guiScaledHeight = (double)this.framebufferHeight / d > (double)n3 ? n3 + 1 : n3;
    }

    public void setTitle(String string) {
        GLFW.glfwSetWindowTitle((long)this.window, (CharSequence)string);
    }

    public long getWindow() {
        return this.window;
    }

    public boolean isFullscreen() {
        return this.fullscreen;
    }

    public boolean isIconified() {
        return this.iconified;
    }

    public int getWidth() {
        return this.framebufferWidth;
    }

    public int getHeight() {
        return this.framebufferHeight;
    }

    public void setWidth(int n) {
        this.framebufferWidth = n;
    }

    public void setHeight(int n) {
        this.framebufferHeight = n;
    }

    public int getScreenWidth() {
        return this.width;
    }

    public int getScreenHeight() {
        return this.height;
    }

    public int getGuiScaledWidth() {
        return this.guiScaledWidth;
    }

    public int getGuiScaledHeight() {
        return this.guiScaledHeight;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getGuiScale() {
        return this.guiScale;
    }

    @Nullable
    public Monitor findBestMonitor() {
        return this.screenManager.findBestMonitor(this);
    }

    public void updateRawMouseInput(boolean bl) {
        InputConstants.updateRawMouseInput(this.window, bl);
    }

    public void setWindowCloseCallback(Runnable runnable) {
        GLFWWindowCloseCallback gLFWWindowCloseCallback = GLFW.glfwSetWindowCloseCallback((long)this.window, l -> runnable.run());
        if (gLFWWindowCloseCallback != null) {
            gLFWWindowCloseCallback.free();
        }
    }

    public boolean isMinimized() {
        return this.minimized;
    }

    public static class WindowInitFailed
    extends SilentInitException {
        WindowInitFailed(String string) {
            super(string);
        }
    }
}

