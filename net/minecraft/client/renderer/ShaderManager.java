/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonParseException
 *  com.google.gson.JsonSyntaxException
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  it.unimi.dsi.fastutil.objects.ObjectArraySet
 *  javax.annotation.Nullable
 *  org.apache.commons.io.IOUtils
 *  org.slf4j.Logger
 */
package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.CompiledRenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.preprocessor.GlslPreprocessor;
import com.mojang.blaze3d.shaders.ShaderType;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.FileUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostChainConfig;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;

public class ShaderManager
extends SimplePreparableReloadListener<Configs>
implements AutoCloseable {
    static final Logger LOGGER = LogUtils.getLogger();
    public static final int MAX_LOG_LENGTH = 32768;
    public static final String SHADER_PATH = "shaders";
    private static final String SHADER_INCLUDE_PATH = "shaders/include/";
    private static final FileToIdConverter POST_CHAIN_ID_CONVERTER = FileToIdConverter.json("post_effect");
    final TextureManager textureManager;
    private final Consumer<Exception> recoveryHandler;
    private CompilationCache compilationCache = new CompilationCache(Configs.EMPTY);
    final CachedOrthoProjectionMatrixBuffer postChainProjectionMatrixBuffer = new CachedOrthoProjectionMatrixBuffer("post", 0.1f, 1000.0f, false);

    public ShaderManager(TextureManager textureManager, Consumer<Exception> consumer) {
        this.textureManager = textureManager;
        this.recoveryHandler = consumer;
    }

    @Override
    protected Configs prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        ImmutableMap.Builder builder = ImmutableMap.builder();
        Map<ResourceLocation, Resource> map = resourceManager.listResources(SHADER_PATH, ShaderManager::isShader);
        for (Map.Entry<ResourceLocation, Resource> entry : map.entrySet()) {
            ResourceLocation object = entry.getKey();
            ShaderType shaderType = ShaderType.byLocation(object);
            if (shaderType == null) continue;
            ShaderManager.loadShader(object, (Resource)entry.getValue(), shaderType, map, (ImmutableMap.Builder<ShaderSourceKey, String>)builder);
        }
        ImmutableMap.Builder builder2 = ImmutableMap.builder();
        for (Map.Entry entry : POST_CHAIN_ID_CONVERTER.listMatchingResources(resourceManager).entrySet()) {
            ShaderManager.loadPostChain((ResourceLocation)entry.getKey(), (Resource)entry.getValue(), (ImmutableMap.Builder<ResourceLocation, PostChainConfig>)builder2);
        }
        return new Configs((Map<ShaderSourceKey, String>)builder.build(), (Map<ResourceLocation, PostChainConfig>)builder2.build());
    }

    private static void loadShader(ResourceLocation resourceLocation, Resource resource, ShaderType shaderType, Map<ResourceLocation, Resource> map, ImmutableMap.Builder<ShaderSourceKey, String> builder) {
        ResourceLocation resourceLocation2 = shaderType.idConverter().fileToId(resourceLocation);
        GlslPreprocessor glslPreprocessor = ShaderManager.createPreprocessor(map, resourceLocation);
        try (BufferedReader bufferedReader = resource.openAsReader();){
            String string = IOUtils.toString((Reader)bufferedReader);
            builder.put((Object)new ShaderSourceKey(resourceLocation2, shaderType), (Object)String.join((CharSequence)"", glslPreprocessor.process(string)));
        }
        catch (IOException iOException) {
            LOGGER.error("Failed to load shader source at {}", (Object)resourceLocation, (Object)iOException);
        }
    }

    private static GlslPreprocessor createPreprocessor(final Map<ResourceLocation, Resource> map, ResourceLocation resourceLocation) {
        final ResourceLocation resourceLocation2 = resourceLocation.withPath(FileUtil::getFullResourcePath);
        return new GlslPreprocessor(){
            private final Set<ResourceLocation> importedLocations = new ObjectArraySet();

            @Override
            public String applyImport(boolean bl, String string) {
                String string3;
                block11: {
                    ResourceLocation resourceLocation;
                    try {
                        resourceLocation = bl ? resourceLocation2.withPath(string2 -> FileUtil.normalizeResourcePath(string2 + string)) : ResourceLocation.parse(string).withPrefix(ShaderManager.SHADER_INCLUDE_PATH);
                    }
                    catch (ResourceLocationException resourceLocationException) {
                        LOGGER.error("Malformed GLSL import {}: {}", (Object)string, (Object)resourceLocationException.getMessage());
                        return "#error " + resourceLocationException.getMessage();
                    }
                    if (!this.importedLocations.add(resourceLocation)) {
                        return null;
                    }
                    BufferedReader bufferedReader = ((Resource)map.get(resourceLocation)).openAsReader();
                    try {
                        string3 = IOUtils.toString((Reader)bufferedReader);
                        if (bufferedReader == null) break block11;
                    }
                    catch (Throwable throwable) {
                        try {
                            if (bufferedReader != null) {
                                try {
                                    ((Reader)bufferedReader).close();
                                }
                                catch (Throwable throwable2) {
                                    throwable.addSuppressed(throwable2);
                                }
                            }
                            throw throwable;
                        }
                        catch (IOException iOException) {
                            LOGGER.error("Could not open GLSL import {}: {}", (Object)resourceLocation, (Object)iOException.getMessage());
                            return "#error " + iOException.getMessage();
                        }
                    }
                    ((Reader)bufferedReader).close();
                }
                return string3;
            }
        };
    }

    private static void loadPostChain(ResourceLocation resourceLocation, Resource resource, ImmutableMap.Builder<ResourceLocation, PostChainConfig> builder) {
        ResourceLocation resourceLocation2 = POST_CHAIN_ID_CONVERTER.fileToId(resourceLocation);
        try (BufferedReader bufferedReader = resource.openAsReader();){
            JsonElement jsonElement = StrictJsonParser.parse(bufferedReader);
            builder.put((Object)resourceLocation2, (Object)((PostChainConfig)PostChainConfig.CODEC.parse((DynamicOps)JsonOps.INSTANCE, (Object)jsonElement).getOrThrow(JsonSyntaxException::new)));
        }
        catch (JsonParseException | IOException throwable) {
            LOGGER.error("Failed to parse post chain at {}", (Object)resourceLocation, (Object)throwable);
        }
    }

    private static boolean isShader(ResourceLocation resourceLocation) {
        return ShaderType.byLocation(resourceLocation) != null || resourceLocation.getPath().endsWith(".glsl");
    }

    @Override
    protected void apply(Configs configs, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        CompilationCache compilationCache = new CompilationCache(configs);
        HashSet<RenderPipeline> hashSet = new HashSet<RenderPipeline>(RenderPipelines.getStaticPipelines());
        ArrayList<ResourceLocation> arrayList = new ArrayList<ResourceLocation>();
        GpuDevice gpuDevice = RenderSystem.getDevice();
        gpuDevice.clearPipelineCache();
        for (RenderPipeline renderPipeline : hashSet) {
            CompiledRenderPipeline compiledRenderPipeline = gpuDevice.precompilePipeline(renderPipeline, compilationCache::getShaderSource);
            if (compiledRenderPipeline.isValid()) continue;
            arrayList.add(renderPipeline.getLocation());
        }
        if (!arrayList.isEmpty()) {
            gpuDevice.clearPipelineCache();
            throw new RuntimeException("Failed to load required shader programs:\n" + arrayList.stream().map(resourceLocation -> " - " + String.valueOf(resourceLocation)).collect(Collectors.joining("\n")));
        }
        this.compilationCache.close();
        this.compilationCache = compilationCache;
    }

    @Override
    public String getName() {
        return "Shader Loader";
    }

    private void tryTriggerRecovery(Exception exception) {
        if (this.compilationCache.triggeredRecovery) {
            return;
        }
        this.recoveryHandler.accept(exception);
        this.compilationCache.triggeredRecovery = true;
    }

    @Nullable
    public PostChain getPostChain(ResourceLocation resourceLocation, Set<ResourceLocation> set) {
        try {
            return this.compilationCache.getOrLoadPostChain(resourceLocation, set);
        }
        catch (CompilationException compilationException) {
            LOGGER.error("Failed to load post chain: {}", (Object)resourceLocation, (Object)compilationException);
            this.compilationCache.postChains.put(resourceLocation, Optional.empty());
            this.tryTriggerRecovery(compilationException);
            return null;
        }
    }

    @Override
    public void close() {
        this.compilationCache.close();
        this.postChainProjectionMatrixBuffer.close();
    }

    public String getShader(ResourceLocation resourceLocation, ShaderType shaderType) {
        return this.compilationCache.getShaderSource(resourceLocation, shaderType);
    }

    @Override
    protected /* synthetic */ Object prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        return this.prepare(resourceManager, profilerFiller);
    }

    class CompilationCache
    implements AutoCloseable {
        private final Configs configs;
        final Map<ResourceLocation, Optional<PostChain>> postChains = new HashMap<ResourceLocation, Optional<PostChain>>();
        boolean triggeredRecovery;

        CompilationCache(Configs configs) {
            this.configs = configs;
        }

        @Nullable
        public PostChain getOrLoadPostChain(ResourceLocation resourceLocation, Set<ResourceLocation> set) throws CompilationException {
            Optional<PostChain> optional = this.postChains.get(resourceLocation);
            if (optional != null) {
                return optional.orElse(null);
            }
            PostChain postChain = this.loadPostChain(resourceLocation, set);
            this.postChains.put(resourceLocation, Optional.of(postChain));
            return postChain;
        }

        private PostChain loadPostChain(ResourceLocation resourceLocation, Set<ResourceLocation> set) throws CompilationException {
            PostChainConfig postChainConfig = this.configs.postChains.get(resourceLocation);
            if (postChainConfig == null) {
                throw new CompilationException("Could not find post chain with id: " + String.valueOf(resourceLocation));
            }
            return PostChain.load(postChainConfig, ShaderManager.this.textureManager, set, resourceLocation, ShaderManager.this.postChainProjectionMatrixBuffer);
        }

        @Override
        public void close() {
            this.postChains.values().forEach(optional -> optional.ifPresent(PostChain::close));
            this.postChains.clear();
        }

        public String getShaderSource(ResourceLocation resourceLocation, ShaderType shaderType) {
            return this.configs.shaderSources.get(new ShaderSourceKey(resourceLocation, shaderType));
        }
    }

    public static final class Configs
    extends Record {
        final Map<ShaderSourceKey, String> shaderSources;
        final Map<ResourceLocation, PostChainConfig> postChains;
        public static final Configs EMPTY = new Configs(Map.of(), Map.of());

        public Configs(Map<ShaderSourceKey, String> map, Map<ResourceLocation, PostChainConfig> map2) {
            this.shaderSources = map;
            this.postChains = map2;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Configs.class, "shaderSources;postChains", "shaderSources", "postChains"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Configs.class, "shaderSources;postChains", "shaderSources", "postChains"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Configs.class, "shaderSources;postChains", "shaderSources", "postChains"}, this, object);
        }

        public Map<ShaderSourceKey, String> shaderSources() {
            return this.shaderSources;
        }

        public Map<ResourceLocation, PostChainConfig> postChains() {
            return this.postChains;
        }
    }

    record ShaderSourceKey(ResourceLocation id, ShaderType type) {
        @Override
        public String toString() {
            return String.valueOf(this.id) + " (" + String.valueOf((Object)this.type) + ")";
        }
    }

    public static class CompilationException
    extends Exception {
        public CompilationException(String string) {
            super(string);
        }
    }
}

