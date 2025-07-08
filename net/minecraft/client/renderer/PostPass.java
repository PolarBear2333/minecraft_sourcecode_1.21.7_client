/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  org.lwjgl.system.MemoryStack
 */
package net.minecraft.client.renderer;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.framegraph.FramePass;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.datafixers.util.Pair;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.UniformValue;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.system.MemoryStack;

public class PostPass
implements AutoCloseable {
    private static final int UBO_SIZE_PER_SAMPLER = new Std140SizeCalculator().putVec2().get();
    private final String name;
    private final RenderPipeline pipeline;
    private final ResourceLocation outputTargetId;
    private final Map<String, GpuBuffer> customUniforms = new HashMap<String, GpuBuffer>();
    private final MappableRingBuffer infoUbo;
    private final List<Input> inputs;

    public PostPass(RenderPipeline renderPipeline, ResourceLocation resourceLocation, Map<String, List<UniformValue>> map, List<Input> list) {
        this.pipeline = renderPipeline;
        this.name = renderPipeline.getLocation().toString();
        this.outputTargetId = resourceLocation;
        this.inputs = list;
        for (Map.Entry<String, List<UniformValue>> entry : map.entrySet()) {
            UniformValue uniformValue2;
            List<UniformValue> list2 = entry.getValue();
            if (list2.isEmpty()) continue;
            Std140SizeCalculator std140SizeCalculator = new Std140SizeCalculator();
            for (UniformValue uniformValue2 : list2) {
                uniformValue2.addSize(std140SizeCalculator);
            }
            int n = std140SizeCalculator.get();
            uniformValue2 = MemoryStack.stackPush();
            try {
                Std140Builder std140Builder = Std140Builder.onStack((MemoryStack)uniformValue2, n);
                for (UniformValue uniformValue3 : list2) {
                    uniformValue3.writeTo(std140Builder);
                }
                this.customUniforms.put(entry.getKey(), RenderSystem.getDevice().createBuffer(() -> this.name + " / " + (String)entry.getKey(), 128, std140Builder.get()));
            }
            finally {
                if (uniformValue2 == null) continue;
                uniformValue2.close();
            }
        }
        this.infoUbo = new MappableRingBuffer(() -> this.name + " SamplerInfo", 130, (list.size() + 1) * UBO_SIZE_PER_SAMPLER);
    }

    public void addToFrame(FrameGraphBuilder frameGraphBuilder, Map<ResourceLocation, ResourceHandle<RenderTarget>> map, GpuBufferSlice gpuBufferSlice) {
        FramePass framePass = frameGraphBuilder.addPass(this.name);
        for (Input input : this.inputs) {
            input.addToPass(framePass, map);
        }
        ResourceHandle resourceHandle2 = map.computeIfPresent(this.outputTargetId, (resourceLocation, resourceHandle) -> framePass.readsAndWrites(resourceHandle));
        if (resourceHandle2 == null) {
            throw new IllegalStateException("Missing handle for target " + String.valueOf(this.outputTargetId));
        }
        framePass.executes(() -> {
            Object object2;
            RenderTarget renderTarget = (RenderTarget)resourceHandle2.get();
            RenderSystem.backupProjectionMatrix();
            RenderSystem.setProjectionMatrix(gpuBufferSlice, ProjectionType.ORTHOGRAPHIC);
            CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
            List<Pair> list = this.inputs.stream().map(input -> Pair.of((Object)input.samplerName(), (Object)input.texture(map))).toList();
            try (AutoCloseable autoCloseable = commandEncoder.mapBuffer(this.infoUbo.currentBuffer(), false, true);){
                object2 = Std140Builder.intoBuffer(autoCloseable.data());
                ((Std140Builder)object2).putVec2(renderTarget.width, renderTarget.height);
                for (Pair object3 : list) {
                    ((Std140Builder)object2).putVec2(((GpuTextureView)object3.getSecond()).getWidth(0), ((GpuTextureView)object3.getSecond()).getHeight(0));
                }
            }
            autoCloseable = RenderSystem.getQuadVertexBuffer();
            object2 = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
            GpuBuffer gpuBuffer = ((RenderSystem.AutoStorageIndexBuffer)object2).getBuffer(6);
            try (RenderPass renderPass = commandEncoder.createRenderPass(() -> "Post pass " + this.name, renderTarget.getColorTextureView(), OptionalInt.empty(), renderTarget.useDepth ? renderTarget.getDepthTextureView() : null, OptionalDouble.empty());){
                renderPass.setPipeline(this.pipeline);
                RenderSystem.bindDefaultUniforms(renderPass);
                renderPass.setUniform("SamplerInfo", this.infoUbo.currentBuffer());
                for (Map.Entry<String, GpuBuffer> entry : this.customUniforms.entrySet()) {
                    renderPass.setUniform(entry.getKey(), entry.getValue());
                }
                renderPass.setVertexBuffer(0, (GpuBuffer)autoCloseable);
                renderPass.setIndexBuffer(gpuBuffer, ((RenderSystem.AutoStorageIndexBuffer)object2).type());
                for (Pair pair : list) {
                    renderPass.bindSampler((String)pair.getFirst() + "Sampler", (GpuTextureView)pair.getSecond());
                }
                renderPass.drawIndexed(0, 0, 6, 1);
            }
            this.infoUbo.rotate();
            RenderSystem.restoreProjectionMatrix();
            for (Input input2 : this.inputs) {
                input2.cleanup(map);
            }
        });
    }

    @Override
    public void close() {
        for (GpuBuffer gpuBuffer : this.customUniforms.values()) {
            gpuBuffer.close();
        }
        this.infoUbo.close();
    }

    public static interface Input {
        public void addToPass(FramePass var1, Map<ResourceLocation, ResourceHandle<RenderTarget>> var2);

        default public void cleanup(Map<ResourceLocation, ResourceHandle<RenderTarget>> map) {
        }

        public GpuTextureView texture(Map<ResourceLocation, ResourceHandle<RenderTarget>> var1);

        public String samplerName();
    }

    public record TargetInput(String samplerName, ResourceLocation targetId, boolean depthBuffer, boolean bilinear) implements Input
    {
        private ResourceHandle<RenderTarget> getHandle(Map<ResourceLocation, ResourceHandle<RenderTarget>> map) {
            ResourceHandle<RenderTarget> resourceHandle = map.get(this.targetId);
            if (resourceHandle == null) {
                throw new IllegalStateException("Missing handle for target " + String.valueOf(this.targetId));
            }
            return resourceHandle;
        }

        @Override
        public void addToPass(FramePass framePass, Map<ResourceLocation, ResourceHandle<RenderTarget>> map) {
            framePass.reads(this.getHandle(map));
        }

        @Override
        public void cleanup(Map<ResourceLocation, ResourceHandle<RenderTarget>> map) {
            if (this.bilinear) {
                this.getHandle(map).get().setFilterMode(FilterMode.NEAREST);
            }
        }

        @Override
        public GpuTextureView texture(Map<ResourceLocation, ResourceHandle<RenderTarget>> map) {
            GpuTextureView gpuTextureView;
            ResourceHandle<RenderTarget> resourceHandle = this.getHandle(map);
            RenderTarget renderTarget = resourceHandle.get();
            renderTarget.setFilterMode(this.bilinear ? FilterMode.LINEAR : FilterMode.NEAREST);
            GpuTextureView gpuTextureView2 = gpuTextureView = this.depthBuffer ? renderTarget.getDepthTextureView() : renderTarget.getColorTextureView();
            if (gpuTextureView == null) {
                throw new IllegalStateException("Missing " + (this.depthBuffer ? "depth" : "color") + "texture for target " + String.valueOf(this.targetId));
            }
            return gpuTextureView;
        }
    }

    public record TextureInput(String samplerName, AbstractTexture texture, int width, int height) implements Input
    {
        @Override
        public void addToPass(FramePass framePass, Map<ResourceLocation, ResourceHandle<RenderTarget>> map) {
        }

        @Override
        public GpuTextureView texture(Map<ResourceLocation, ResourceHandle<RenderTarget>> map) {
            return this.texture.getTextureView();
        }
    }
}

