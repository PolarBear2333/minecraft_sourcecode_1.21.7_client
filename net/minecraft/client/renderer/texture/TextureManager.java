/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.gui.screens.AddRealmPopupScreen;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Dumpable;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

public class TextureManager
implements PreparableReloadListener,
Tickable,
AutoCloseable {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceLocation INTENTIONAL_MISSING_TEXTURE = ResourceLocation.withDefaultNamespace("");
    private final Map<ResourceLocation, AbstractTexture> byPath = new HashMap<ResourceLocation, AbstractTexture>();
    private final Set<Tickable> tickableTextures = new HashSet<Tickable>();
    private final ResourceManager resourceManager;

    public TextureManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
        NativeImage nativeImage = MissingTextureAtlasSprite.generateMissingImage();
        this.register(MissingTextureAtlasSprite.getLocation(), new DynamicTexture(() -> "(intentionally-)Missing Texture", nativeImage));
    }

    public void registerAndLoad(ResourceLocation resourceLocation, ReloadableTexture reloadableTexture) {
        try {
            reloadableTexture.apply(this.loadContentsSafe(resourceLocation, reloadableTexture));
        }
        catch (Throwable throwable) {
            CrashReport crashReport = CrashReport.forThrowable(throwable, "Uploading texture");
            CrashReportCategory crashReportCategory = crashReport.addCategory("Uploaded texture");
            crashReportCategory.setDetail("Resource location", reloadableTexture.resourceId());
            crashReportCategory.setDetail("Texture id", resourceLocation);
            throw new ReportedException(crashReport);
        }
        this.register(resourceLocation, reloadableTexture);
    }

    private TextureContents loadContentsSafe(ResourceLocation resourceLocation, ReloadableTexture reloadableTexture) {
        try {
            return TextureManager.loadContents(this.resourceManager, resourceLocation, reloadableTexture);
        }
        catch (Exception exception) {
            LOGGER.error("Failed to load texture {} into slot {}", new Object[]{reloadableTexture.resourceId(), resourceLocation, exception});
            return TextureContents.createMissing();
        }
    }

    public void registerForNextReload(ResourceLocation resourceLocation) {
        this.register(resourceLocation, new SimpleTexture(resourceLocation));
    }

    public void register(ResourceLocation resourceLocation, AbstractTexture abstractTexture) {
        AbstractTexture abstractTexture2 = this.byPath.put(resourceLocation, abstractTexture);
        if (abstractTexture2 != abstractTexture) {
            if (abstractTexture2 != null) {
                this.safeClose(resourceLocation, abstractTexture2);
            }
            if (abstractTexture instanceof Tickable) {
                Tickable tickable = (Tickable)((Object)abstractTexture);
                this.tickableTextures.add(tickable);
            }
        }
    }

    private void safeClose(ResourceLocation resourceLocation, AbstractTexture abstractTexture) {
        this.tickableTextures.remove(abstractTexture);
        try {
            abstractTexture.close();
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to close texture {}", (Object)resourceLocation, (Object)exception);
        }
    }

    public AbstractTexture getTexture(ResourceLocation resourceLocation) {
        AbstractTexture abstractTexture = this.byPath.get(resourceLocation);
        if (abstractTexture != null) {
            return abstractTexture;
        }
        SimpleTexture simpleTexture = new SimpleTexture(resourceLocation);
        this.registerAndLoad(resourceLocation, simpleTexture);
        return simpleTexture;
    }

    @Override
    public void tick() {
        for (Tickable tickable : this.tickableTextures) {
            tickable.tick();
        }
    }

    public void release(ResourceLocation resourceLocation) {
        AbstractTexture abstractTexture = this.byPath.remove(resourceLocation);
        if (abstractTexture != null) {
            this.safeClose(resourceLocation, abstractTexture);
        }
    }

    @Override
    public void close() {
        this.byPath.forEach(this::safeClose);
        this.byPath.clear();
        this.tickableTextures.clear();
    }

    @Override
    public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier preparationBarrier, ResourceManager resourceManager, Executor executor, Executor executor2) {
        ArrayList arrayList = new ArrayList();
        this.byPath.forEach((resourceLocation, abstractTexture) -> {
            if (abstractTexture instanceof ReloadableTexture) {
                ReloadableTexture reloadableTexture = (ReloadableTexture)abstractTexture;
                arrayList.add(TextureManager.scheduleLoad(resourceManager, resourceLocation, reloadableTexture, executor));
            }
        });
        return ((CompletableFuture)CompletableFuture.allOf((CompletableFuture[])arrayList.stream().map(PendingReload::newContents).toArray(CompletableFuture[]::new)).thenCompose(preparationBarrier::wait)).thenAcceptAsync(void_ -> {
            AddRealmPopupScreen.updateCarouselImages(this.resourceManager);
            for (PendingReload pendingReload : arrayList) {
                pendingReload.texture.apply(pendingReload.newContents.join());
            }
        }, executor2);
    }

    public void dumpAllSheets(Path path) {
        try {
            Files.createDirectories(path, new FileAttribute[0]);
        }
        catch (IOException iOException) {
            LOGGER.error("Failed to create directory {}", (Object)path, (Object)iOException);
            return;
        }
        this.byPath.forEach((resourceLocation, abstractTexture) -> {
            if (abstractTexture instanceof Dumpable) {
                Dumpable dumpable = (Dumpable)((Object)abstractTexture);
                try {
                    dumpable.dumpContents((ResourceLocation)resourceLocation, path);
                }
                catch (IOException iOException) {
                    LOGGER.error("Failed to dump texture {}", resourceLocation, (Object)iOException);
                }
            }
        });
    }

    private static TextureContents loadContents(ResourceManager resourceManager, ResourceLocation resourceLocation, ReloadableTexture reloadableTexture) throws IOException {
        try {
            return reloadableTexture.loadContents(resourceManager);
        }
        catch (FileNotFoundException fileNotFoundException) {
            if (resourceLocation != INTENTIONAL_MISSING_TEXTURE) {
                LOGGER.warn("Missing resource {} referenced from {}", (Object)reloadableTexture.resourceId(), (Object)resourceLocation);
            }
            return TextureContents.createMissing();
        }
    }

    private static PendingReload scheduleLoad(ResourceManager resourceManager, ResourceLocation resourceLocation, ReloadableTexture reloadableTexture, Executor executor) {
        return new PendingReload(reloadableTexture, CompletableFuture.supplyAsync(() -> {
            try {
                return TextureManager.loadContents(resourceManager, resourceLocation, reloadableTexture);
            }
            catch (IOException iOException) {
                throw new UncheckedIOException(iOException);
            }
        }, executor));
    }

    static final class PendingReload
    extends Record {
        final ReloadableTexture texture;
        final CompletableFuture<TextureContents> newContents;

        PendingReload(ReloadableTexture reloadableTexture, CompletableFuture<TextureContents> completableFuture) {
            this.texture = reloadableTexture;
            this.newContents = completableFuture;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{PendingReload.class, "texture;newContents", "texture", "newContents"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PendingReload.class, "texture;newContents", "texture", "newContents"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PendingReload.class, "texture;newContents", "texture", "newContents"}, this, object);
        }

        public ReloadableTexture texture() {
            return this.texture;
        }

        public CompletableFuture<TextureContents> newContents() {
            return this.newContents;
        }
    }
}

