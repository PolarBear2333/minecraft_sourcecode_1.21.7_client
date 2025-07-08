/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.cache.CacheBuilder
 *  com.google.common.cache.CacheLoader
 *  com.google.common.cache.LoadingCache
 *  com.google.common.hash.Hashing
 *  com.mojang.authlib.GameProfile
 *  com.mojang.authlib.SignatureState
 *  com.mojang.authlib.minecraft.MinecraftProfileTexture
 *  com.mojang.authlib.minecraft.MinecraftProfileTexture$Type
 *  com.mojang.authlib.minecraft.MinecraftProfileTextures
 *  com.mojang.authlib.minecraft.MinecraftSessionService
 *  com.mojang.authlib.properties.Property
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.resources;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.hash.Hashing;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.SignatureState;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.Optionull;
import net.minecraft.Util;
import net.minecraft.client.renderer.texture.SkinTextureDownloader;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class SkinManager {
    static final Logger LOGGER = LogUtils.getLogger();
    private final MinecraftSessionService sessionService;
    private final LoadingCache<CacheKey, CompletableFuture<Optional<PlayerSkin>>> skinCache;
    private final TextureCache skinTextures;
    private final TextureCache capeTextures;
    private final TextureCache elytraTextures;

    public SkinManager(Path path, final MinecraftSessionService minecraftSessionService, final Executor executor) {
        this.sessionService = minecraftSessionService;
        this.skinTextures = new TextureCache(path, MinecraftProfileTexture.Type.SKIN);
        this.capeTextures = new TextureCache(path, MinecraftProfileTexture.Type.CAPE);
        this.elytraTextures = new TextureCache(path, MinecraftProfileTexture.Type.ELYTRA);
        this.skinCache = CacheBuilder.newBuilder().expireAfterAccess(Duration.ofSeconds(15L)).build((CacheLoader)new CacheLoader<CacheKey, CompletableFuture<Optional<PlayerSkin>>>(){

            public CompletableFuture<Optional<PlayerSkin>> load(CacheKey cacheKey) {
                return ((CompletableFuture)CompletableFuture.supplyAsync(() -> {
                    Property property = cacheKey.packedTextures();
                    if (property == null) {
                        return MinecraftProfileTextures.EMPTY;
                    }
                    MinecraftProfileTextures minecraftProfileTextures = minecraftSessionService.unpackTextures(property);
                    if (minecraftProfileTextures.signatureState() == SignatureState.INVALID) {
                        LOGGER.warn("Profile contained invalid signature for textures property (profile id: {})", (Object)cacheKey.profileId());
                    }
                    return minecraftProfileTextures;
                }, Util.backgroundExecutor().forName("unpackSkinTextures")).thenComposeAsync(minecraftProfileTextures -> SkinManager.this.registerTextures(cacheKey.profileId(), (MinecraftProfileTextures)minecraftProfileTextures), executor)).handle((playerSkin, throwable) -> {
                    if (throwable != null) {
                        LOGGER.warn("Failed to load texture for profile {}", (Object)cacheKey.profileId, throwable);
                    }
                    return Optional.ofNullable(playerSkin);
                });
            }

            public /* synthetic */ Object load(Object object) throws Exception {
                return this.load((CacheKey)object);
            }
        });
    }

    public Supplier<PlayerSkin> lookupInsecure(GameProfile gameProfile) {
        CompletableFuture<Optional<PlayerSkin>> completableFuture = this.getOrLoad(gameProfile);
        PlayerSkin playerSkin = DefaultPlayerSkin.get(gameProfile);
        return () -> completableFuture.getNow(Optional.empty()).orElse(playerSkin);
    }

    public PlayerSkin getInsecureSkin(GameProfile gameProfile) {
        PlayerSkin playerSkin = this.getInsecureSkin(gameProfile, null);
        if (playerSkin != null) {
            return playerSkin;
        }
        return DefaultPlayerSkin.get(gameProfile);
    }

    @Nullable
    public PlayerSkin getInsecureSkin(GameProfile gameProfile, @Nullable PlayerSkin playerSkin) {
        return this.getOrLoad(gameProfile).getNow(Optional.empty()).orElse(playerSkin);
    }

    public CompletableFuture<Optional<PlayerSkin>> getOrLoad(GameProfile gameProfile) {
        Property property = this.sessionService.getPackedTextures(gameProfile);
        return (CompletableFuture)this.skinCache.getUnchecked((Object)new CacheKey(gameProfile.getId(), property));
    }

    CompletableFuture<PlayerSkin> registerTextures(UUID uUID, MinecraftProfileTextures minecraftProfileTextures) {
        Object object;
        PlayerSkin.Model model;
        CompletableFuture<ResourceLocation> completableFuture;
        MinecraftProfileTexture minecraftProfileTexture = minecraftProfileTextures.skin();
        if (minecraftProfileTexture != null) {
            completableFuture = this.skinTextures.getOrLoad(minecraftProfileTexture);
            model = PlayerSkin.Model.byName(minecraftProfileTexture.getMetadata("model"));
        } else {
            object = DefaultPlayerSkin.get(uUID);
            completableFuture = CompletableFuture.completedFuture(((PlayerSkin)object).texture());
            model = ((PlayerSkin)object).model();
        }
        object = Optionull.map(minecraftProfileTexture, MinecraftProfileTexture::getUrl);
        MinecraftProfileTexture minecraftProfileTexture2 = minecraftProfileTextures.cape();
        CompletableFuture<Object> completableFuture2 = minecraftProfileTexture2 != null ? this.capeTextures.getOrLoad(minecraftProfileTexture2) : CompletableFuture.completedFuture(null);
        MinecraftProfileTexture minecraftProfileTexture3 = minecraftProfileTextures.elytra();
        CompletableFuture<Object> completableFuture3 = minecraftProfileTexture3 != null ? this.elytraTextures.getOrLoad(minecraftProfileTexture3) : CompletableFuture.completedFuture(null);
        return CompletableFuture.allOf(completableFuture, completableFuture2, completableFuture3).thenApply(arg_0 -> SkinManager.lambda$registerTextures$1(completableFuture, (String)object, completableFuture2, completableFuture3, model, minecraftProfileTextures, arg_0));
    }

    private static /* synthetic */ PlayerSkin lambda$registerTextures$1(CompletableFuture completableFuture, String string, CompletableFuture completableFuture2, CompletableFuture completableFuture3, PlayerSkin.Model model, MinecraftProfileTextures minecraftProfileTextures, Void void_) {
        return new PlayerSkin((ResourceLocation)completableFuture.join(), string, (ResourceLocation)completableFuture2.join(), (ResourceLocation)completableFuture3.join(), model, minecraftProfileTextures.signatureState() == SignatureState.SIGNED);
    }

    static class TextureCache {
        private final Path root;
        private final MinecraftProfileTexture.Type type;
        private final Map<String, CompletableFuture<ResourceLocation>> textures = new Object2ObjectOpenHashMap();

        TextureCache(Path path, MinecraftProfileTexture.Type type) {
            this.root = path;
            this.type = type;
        }

        public CompletableFuture<ResourceLocation> getOrLoad(MinecraftProfileTexture minecraftProfileTexture) {
            String string = minecraftProfileTexture.getHash();
            CompletableFuture<ResourceLocation> completableFuture = this.textures.get(string);
            if (completableFuture == null) {
                completableFuture = this.registerTexture(minecraftProfileTexture);
                this.textures.put(string, completableFuture);
            }
            return completableFuture;
        }

        private CompletableFuture<ResourceLocation> registerTexture(MinecraftProfileTexture minecraftProfileTexture) {
            String string = Hashing.sha1().hashUnencodedChars((CharSequence)minecraftProfileTexture.getHash()).toString();
            ResourceLocation resourceLocation = this.getTextureLocation(string);
            Path path = this.root.resolve(string.length() > 2 ? string.substring(0, 2) : "xx").resolve(string);
            return SkinTextureDownloader.downloadAndRegisterSkin(resourceLocation, path, minecraftProfileTexture.getUrl(), this.type == MinecraftProfileTexture.Type.SKIN);
        }

        private ResourceLocation getTextureLocation(String string) {
            String string2 = switch (this.type) {
                default -> throw new MatchException(null, null);
                case MinecraftProfileTexture.Type.SKIN -> "skins";
                case MinecraftProfileTexture.Type.CAPE -> "capes";
                case MinecraftProfileTexture.Type.ELYTRA -> "elytra";
            };
            return ResourceLocation.withDefaultNamespace(string2 + "/" + string);
        }
    }

    static final class CacheKey
    extends Record {
        final UUID profileId;
        @Nullable
        private final Property packedTextures;

        CacheKey(UUID uUID, @Nullable Property property) {
            this.profileId = uUID;
            this.packedTextures = property;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{CacheKey.class, "profileId;packedTextures", "profileId", "packedTextures"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CacheKey.class, "profileId;packedTextures", "profileId", "packedTextures"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CacheKey.class, "profileId;packedTextures", "profileId", "packedTextures"}, this, object);
        }

        public UUID profileId() {
            return this.profileId;
        }

        @Nullable
        public Property packedTextures() {
            return this.packedTextures;
        }
    }
}

