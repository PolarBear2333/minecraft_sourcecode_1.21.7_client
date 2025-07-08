/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.google.common.collect.Sets
 *  com.google.common.collect.Sets$SetView
 *  javax.annotation.Nullable
 */
package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.resource.RenderTargetDescriptor;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.shaders.UniformType;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer;
import net.minecraft.client.renderer.PostChainConfig;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.ShaderManager;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

public class PostChain
implements AutoCloseable {
    public static final ResourceLocation MAIN_TARGET_ID = ResourceLocation.withDefaultNamespace("main");
    private final List<PostPass> passes;
    private final Map<ResourceLocation, PostChainConfig.InternalTarget> internalTargets;
    private final Set<ResourceLocation> externalTargets;
    private final Map<ResourceLocation, RenderTarget> persistentTargets = new HashMap<ResourceLocation, RenderTarget>();
    private final CachedOrthoProjectionMatrixBuffer projectionMatrixBuffer;

    private PostChain(List<PostPass> list, Map<ResourceLocation, PostChainConfig.InternalTarget> map, Set<ResourceLocation> set, CachedOrthoProjectionMatrixBuffer cachedOrthoProjectionMatrixBuffer) {
        this.passes = list;
        this.internalTargets = map;
        this.externalTargets = set;
        this.projectionMatrixBuffer = cachedOrthoProjectionMatrixBuffer;
    }

    public static PostChain load(PostChainConfig postChainConfig, TextureManager textureManager, Set<ResourceLocation> set, ResourceLocation resourceLocation2, CachedOrthoProjectionMatrixBuffer cachedOrthoProjectionMatrixBuffer) throws ShaderManager.CompilationException {
        Stream stream = postChainConfig.passes().stream().flatMap(PostChainConfig.Pass::referencedTargets);
        Set<ResourceLocation> set2 = stream.filter(resourceLocation -> !postChainConfig.internalTargets().containsKey(resourceLocation)).collect(Collectors.toSet());
        Sets.SetView setView = Sets.difference(set2, set);
        if (!setView.isEmpty()) {
            throw new ShaderManager.CompilationException("Referenced external targets are not available in this context: " + String.valueOf(setView));
        }
        ImmutableList.Builder builder = ImmutableList.builder();
        for (int i = 0; i < postChainConfig.passes().size(); ++i) {
            PostChainConfig.Pass pass = postChainConfig.passes().get(i);
            builder.add((Object)PostChain.createPass(textureManager, pass, resourceLocation2.withSuffix("/" + i)));
        }
        return new PostChain((List<PostPass>)builder.build(), postChainConfig.internalTargets(), set2, cachedOrthoProjectionMatrixBuffer);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static PostPass createPass(TextureManager textureManager, PostChainConfig.Pass pass, ResourceLocation resourceLocation) throws ShaderManager.CompilationException {
        RenderPipeline.Builder builder = RenderPipeline.builder(RenderPipelines.POST_PROCESSING_SNIPPET).withFragmentShader(pass.fragmentShaderId()).withVertexShader(pass.vertexShaderId()).withLocation(resourceLocation);
        for (PostChainConfig.Input arrayList2 : pass.inputs()) {
            builder.withSampler(arrayList2.samplerName() + "Sampler");
        }
        builder.withUniform("SamplerInfo", UniformType.UNIFORM_BUFFER);
        for (String string2 : pass.uniforms().keySet()) {
            builder.withUniform(string2, UniformType.UNIFORM_BUFFER);
        }
        RenderPipeline renderPipeline = builder.build();
        ArrayList<PostPass.Input> arrayList = new ArrayList<PostPass.Input>();
        Iterator<PostChainConfig.Input> iterator = pass.inputs().iterator();
        block9: while (true) {
            PostChainConfig.Input input2;
            if (!iterator.hasNext()) {
                return new PostPass(renderPipeline, pass.outputTarget(), pass.uniforms(), arrayList);
            }
            PostChainConfig.Input input = iterator.next();
            Objects.requireNonNull(input);
            int n = 0;
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{PostChainConfig.TextureInput.class, PostChainConfig.TargetInput.class}, (Object)input2, n)) {
                case 0: {
                    int n2;
                    Object object;
                    PostChainConfig.TextureInput textureInput = (PostChainConfig.TextureInput)input2;
                    Object object2 = object = textureInput.samplerName();
                    Object object3 = object = textureInput.location();
                    int n3 = n2 = textureInput.width();
                    int n4 = n2 = textureInput.height();
                    int n5 = n2 = (int)(textureInput.bilinear() ? 1 : 0);
                    object = textureManager.getTexture(((ResourceLocation)object3).withPath(string -> "textures/effect/" + string + ".png"));
                    ((AbstractTexture)object).setFilter(n5 != 0, false);
                    arrayList.add(new PostPass.TextureInput((String)object2, (AbstractTexture)object, n3, n4));
                    continue block9;
                }
                case 1: {
                    Object object = (PostChainConfig.TargetInput)input2;
                    try {
                        boolean bl;
                        Object object4 = ((PostChainConfig.TargetInput)object).samplerName();
                        String string3 = object4;
                        Object object5 = object4 = ((PostChainConfig.TargetInput)object).targetId();
                        boolean bl2 = bl = ((PostChainConfig.TargetInput)object).useDepthBuffer();
                        boolean bl3 = bl = ((PostChainConfig.TargetInput)object).bilinear();
                        arrayList.add(new PostPass.TargetInput(string3, (ResourceLocation)object5, bl2, bl3));
                    }
                    catch (Throwable throwable) {
                        throw new MatchException(throwable.toString(), throwable);
                    }
                    continue block9;
                }
            }
            break;
        }
        throw new MatchException(null, null);
    }

    public void addToFrame(FrameGraphBuilder frameGraphBuilder, int n, int n2, TargetBundle targetBundle) {
        GpuBufferSlice gpuBufferSlice = this.projectionMatrixBuffer.getBuffer(n, n2);
        HashMap<ResourceLocation, ResourceHandle<RenderTarget>> hashMap = new HashMap<ResourceLocation, ResourceHandle<RenderTarget>>(this.internalTargets.size() + this.externalTargets.size());
        for (ResourceLocation object : this.externalTargets) {
            hashMap.put(object, targetBundle.getOrThrow(object));
        }
        for (Map.Entry entry : this.internalTargets.entrySet()) {
            ResourceLocation resourceLocation = (ResourceLocation)entry.getKey();
            PostChainConfig.InternalTarget internalTarget = (PostChainConfig.InternalTarget)entry.getValue();
            RenderTargetDescriptor renderTargetDescriptor = new RenderTargetDescriptor(internalTarget.width().orElse(n), internalTarget.height().orElse(n2), true, internalTarget.clearColor());
            if (internalTarget.persistent()) {
                RenderTarget renderTarget = this.getOrCreatePersistentTarget(resourceLocation, renderTargetDescriptor);
                hashMap.put(resourceLocation, frameGraphBuilder.importExternal(resourceLocation.toString(), renderTarget));
                continue;
            }
            hashMap.put(resourceLocation, frameGraphBuilder.createInternal(resourceLocation.toString(), renderTargetDescriptor));
        }
        for (PostPass postPass : this.passes) {
            postPass.addToFrame(frameGraphBuilder, hashMap, gpuBufferSlice);
        }
        for (ResourceLocation resourceLocation : this.externalTargets) {
            targetBundle.replace(resourceLocation, (ResourceHandle)hashMap.get(resourceLocation));
        }
    }

    @Deprecated
    public void process(RenderTarget renderTarget, GraphicsResourceAllocator graphicsResourceAllocator) {
        FrameGraphBuilder frameGraphBuilder = new FrameGraphBuilder();
        TargetBundle targetBundle = TargetBundle.of(MAIN_TARGET_ID, frameGraphBuilder.importExternal("main", renderTarget));
        this.addToFrame(frameGraphBuilder, renderTarget.width, renderTarget.height, targetBundle);
        frameGraphBuilder.execute(graphicsResourceAllocator);
    }

    private RenderTarget getOrCreatePersistentTarget(ResourceLocation resourceLocation, RenderTargetDescriptor renderTargetDescriptor) {
        RenderTarget renderTarget = this.persistentTargets.get(resourceLocation);
        if (renderTarget == null || renderTarget.width != renderTargetDescriptor.width() || renderTarget.height != renderTargetDescriptor.height()) {
            if (renderTarget != null) {
                renderTarget.destroyBuffers();
            }
            renderTarget = renderTargetDescriptor.allocate();
            renderTargetDescriptor.prepare(renderTarget);
            this.persistentTargets.put(resourceLocation, renderTarget);
        }
        return renderTarget;
    }

    @Override
    public void close() {
        this.persistentTargets.values().forEach(RenderTarget::destroyBuffers);
        this.persistentTargets.clear();
        for (PostPass postPass : this.passes) {
            postPass.close();
        }
    }

    public static interface TargetBundle {
        public static TargetBundle of(final ResourceLocation resourceLocation, final ResourceHandle<RenderTarget> resourceHandle) {
            return new TargetBundle(){
                private ResourceHandle<RenderTarget> handle;
                {
                    this.handle = resourceHandle;
                }

                @Override
                public void replace(ResourceLocation resourceLocation2, ResourceHandle<RenderTarget> resourceHandle2) {
                    if (!resourceLocation2.equals(resourceLocation)) {
                        throw new IllegalArgumentException("No target with id " + String.valueOf(resourceLocation2));
                    }
                    this.handle = resourceHandle2;
                }

                @Override
                @Nullable
                public ResourceHandle<RenderTarget> get(ResourceLocation resourceLocation2) {
                    return resourceLocation2.equals(resourceLocation) ? this.handle : null;
                }
            };
        }

        public void replace(ResourceLocation var1, ResourceHandle<RenderTarget> var2);

        @Nullable
        public ResourceHandle<RenderTarget> get(ResourceLocation var1);

        default public ResourceHandle<RenderTarget> getOrThrow(ResourceLocation resourceLocation) {
            ResourceHandle<RenderTarget> resourceHandle = this.get(resourceLocation);
            if (resourceHandle == null) {
                throw new IllegalArgumentException("Missing target with id " + String.valueOf(resourceLocation));
            }
            return resourceHandle;
        }
    }
}

