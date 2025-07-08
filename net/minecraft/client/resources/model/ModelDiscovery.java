/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.Object2ObjectFunction
 *  it.unimi.dsi.fastutil.objects.Object2ObjectMap
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  javax.annotation.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.client.resources.model;

import com.google.common.collect.ImmutableMap;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectFunction;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReferenceArray;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.MissingBlockModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.UnbakedGeometry;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class ModelDiscovery {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Object2ObjectMap<ResourceLocation, ModelWrapper> modelWrappers = new Object2ObjectOpenHashMap();
    private final ModelWrapper missingModel;
    private final Object2ObjectFunction<ResourceLocation, ModelWrapper> uncachedResolver;
    private final ResolvableModel.Resolver resolver;
    private final Queue<ModelWrapper> parentDiscoveryQueue = new ArrayDeque<ModelWrapper>();

    public ModelDiscovery(Map<ResourceLocation, UnbakedModel> map, UnbakedModel unbakedModel) {
        this.missingModel = new ModelWrapper(MissingBlockModel.LOCATION, unbakedModel, true);
        this.modelWrappers.put((Object)MissingBlockModel.LOCATION, (Object)this.missingModel);
        this.uncachedResolver = object -> {
            ResourceLocation resourceLocation = (ResourceLocation)object;
            UnbakedModel unbakedModel = (UnbakedModel)map.get(resourceLocation);
            if (unbakedModel == null) {
                LOGGER.warn("Missing block model: {}", (Object)resourceLocation);
                return this.missingModel;
            }
            return this.createAndQueueWrapper(resourceLocation, unbakedModel);
        };
        this.resolver = this::getOrCreateModel;
    }

    private static boolean isRoot(UnbakedModel unbakedModel) {
        return unbakedModel.parent() == null;
    }

    private ModelWrapper getOrCreateModel(ResourceLocation resourceLocation) {
        return (ModelWrapper)this.modelWrappers.computeIfAbsent((Object)resourceLocation, this.uncachedResolver);
    }

    private ModelWrapper createAndQueueWrapper(ResourceLocation resourceLocation, UnbakedModel unbakedModel) {
        boolean bl = ModelDiscovery.isRoot(unbakedModel);
        ModelWrapper modelWrapper = new ModelWrapper(resourceLocation, unbakedModel, bl);
        if (!bl) {
            this.parentDiscoveryQueue.add(modelWrapper);
        }
        return modelWrapper;
    }

    public void addRoot(ResolvableModel resolvableModel) {
        resolvableModel.resolveDependencies(this.resolver);
    }

    public void addSpecialModel(ResourceLocation resourceLocation, UnbakedModel unbakedModel) {
        if (!ModelDiscovery.isRoot(unbakedModel)) {
            LOGGER.warn("Trying to add non-root special model {}, ignoring", (Object)resourceLocation);
            return;
        }
        ModelWrapper modelWrapper = (ModelWrapper)this.modelWrappers.put((Object)resourceLocation, (Object)this.createAndQueueWrapper(resourceLocation, unbakedModel));
        if (modelWrapper != null) {
            LOGGER.warn("Duplicate special model {}", (Object)resourceLocation);
        }
    }

    public ResolvedModel missingModel() {
        return this.missingModel;
    }

    public Map<ResourceLocation, ResolvedModel> resolve() {
        ArrayList<ModelWrapper> arrayList = new ArrayList<ModelWrapper>();
        this.discoverDependencies(arrayList);
        ModelDiscovery.propagateValidity(arrayList);
        ImmutableMap.Builder builder = ImmutableMap.builder();
        this.modelWrappers.forEach((resourceLocation, modelWrapper) -> {
            if (modelWrapper.valid) {
                builder.put(resourceLocation, modelWrapper);
            } else {
                LOGGER.warn("Model {} ignored due to cyclic dependency", resourceLocation);
            }
        });
        return builder.build();
    }

    private void discoverDependencies(List<ModelWrapper> list) {
        ModelWrapper modelWrapper;
        while ((modelWrapper = this.parentDiscoveryQueue.poll()) != null) {
            ModelWrapper modelWrapper2;
            ResourceLocation resourceLocation = Objects.requireNonNull(modelWrapper.wrapped.parent());
            modelWrapper.parent = modelWrapper2 = this.getOrCreateModel(resourceLocation);
            if (modelWrapper2.valid) {
                modelWrapper.valid = true;
                continue;
            }
            list.add(modelWrapper);
        }
    }

    private static void propagateValidity(List<ModelWrapper> list) {
        boolean bl = true;
        while (bl) {
            bl = false;
            Iterator<ModelWrapper> iterator = list.iterator();
            while (iterator.hasNext()) {
                ModelWrapper modelWrapper = iterator.next();
                if (!Objects.requireNonNull(modelWrapper.parent).valid) continue;
                modelWrapper.valid = true;
                iterator.remove();
                bl = true;
            }
        }
    }

    static class ModelWrapper
    implements ResolvedModel {
        private static final Slot<Boolean> KEY_AMBIENT_OCCLUSION = ModelWrapper.slot(0);
        private static final Slot<UnbakedModel.GuiLight> KEY_GUI_LIGHT = ModelWrapper.slot(1);
        private static final Slot<UnbakedGeometry> KEY_GEOMETRY = ModelWrapper.slot(2);
        private static final Slot<ItemTransforms> KEY_TRANSFORMS = ModelWrapper.slot(3);
        private static final Slot<TextureSlots> KEY_TEXTURE_SLOTS = ModelWrapper.slot(4);
        private static final Slot<TextureAtlasSprite> KEY_PARTICLE_SPRITE = ModelWrapper.slot(5);
        private static final Slot<QuadCollection> KEY_DEFAULT_GEOMETRY = ModelWrapper.slot(6);
        private static final int SLOT_COUNT = 7;
        private final ResourceLocation id;
        boolean valid;
        @Nullable
        ModelWrapper parent;
        final UnbakedModel wrapped;
        private final AtomicReferenceArray<Object> fixedSlots = new AtomicReferenceArray(7);
        private final Map<ModelState, QuadCollection> modelBakeCache = new ConcurrentHashMap<ModelState, QuadCollection>();

        private static <T> Slot<T> slot(int n) {
            Objects.checkIndex(n, 7);
            return new Slot(n);
        }

        ModelWrapper(ResourceLocation resourceLocation, UnbakedModel unbakedModel, boolean bl) {
            this.id = resourceLocation;
            this.wrapped = unbakedModel;
            this.valid = bl;
        }

        @Override
        public UnbakedModel wrapped() {
            return this.wrapped;
        }

        @Override
        @Nullable
        public ResolvedModel parent() {
            return this.parent;
        }

        @Override
        public String debugName() {
            return this.id.toString();
        }

        @Nullable
        private <T> T getSlot(Slot<T> slot) {
            return (T)this.fixedSlots.get(slot.index);
        }

        private <T> T updateSlot(Slot<T> slot, T t) {
            T t2 = this.fixedSlots.compareAndExchange(slot.index, null, t);
            if (t2 == null) {
                return t;
            }
            return t2;
        }

        private <T> T getSimpleProperty(Slot<T> slot, Function<ResolvedModel, T> function) {
            T t = this.getSlot(slot);
            if (t != null) {
                return t;
            }
            return this.updateSlot(slot, function.apply(this));
        }

        @Override
        public boolean getTopAmbientOcclusion() {
            return this.getSimpleProperty(KEY_AMBIENT_OCCLUSION, ResolvedModel::findTopAmbientOcclusion);
        }

        @Override
        public UnbakedModel.GuiLight getTopGuiLight() {
            return this.getSimpleProperty(KEY_GUI_LIGHT, ResolvedModel::findTopGuiLight);
        }

        @Override
        public ItemTransforms getTopTransforms() {
            return this.getSimpleProperty(KEY_TRANSFORMS, ResolvedModel::findTopTransforms);
        }

        @Override
        public UnbakedGeometry getTopGeometry() {
            return this.getSimpleProperty(KEY_GEOMETRY, ResolvedModel::findTopGeometry);
        }

        @Override
        public TextureSlots getTopTextureSlots() {
            return this.getSimpleProperty(KEY_TEXTURE_SLOTS, ResolvedModel::findTopTextureSlots);
        }

        @Override
        public TextureAtlasSprite resolveParticleSprite(TextureSlots textureSlots, ModelBaker modelBaker) {
            TextureAtlasSprite textureAtlasSprite = this.getSlot(KEY_PARTICLE_SPRITE);
            if (textureAtlasSprite != null) {
                return textureAtlasSprite;
            }
            return this.updateSlot(KEY_PARTICLE_SPRITE, ResolvedModel.resolveParticleSprite(textureSlots, modelBaker, this));
        }

        private QuadCollection bakeDefaultState(TextureSlots textureSlots, ModelBaker modelBaker, ModelState modelState) {
            QuadCollection quadCollection = this.getSlot(KEY_DEFAULT_GEOMETRY);
            if (quadCollection != null) {
                return quadCollection;
            }
            return this.updateSlot(KEY_DEFAULT_GEOMETRY, this.getTopGeometry().bake(textureSlots, modelBaker, modelState, this));
        }

        @Override
        public QuadCollection bakeTopGeometry(TextureSlots textureSlots, ModelBaker modelBaker, ModelState modelState2) {
            if (modelState2 == BlockModelRotation.X0_Y0) {
                return this.bakeDefaultState(textureSlots, modelBaker, modelState2);
            }
            return this.modelBakeCache.computeIfAbsent(modelState2, modelState -> {
                UnbakedGeometry unbakedGeometry = this.getTopGeometry();
                return unbakedGeometry.bake(textureSlots, modelBaker, (ModelState)modelState, this);
            });
        }
    }

    static final class Slot<T>
    extends Record {
        final int index;

        Slot(int n) {
            this.index = n;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Slot.class, "index", "index"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Slot.class, "index", "index"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Slot.class, "index", "index"}, this, object);
        }

        public int index() {
            return this.index;
        }
    }
}

